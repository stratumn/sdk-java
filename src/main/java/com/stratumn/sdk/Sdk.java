/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stratumn.chainscript.ChainscriptException;
import com.stratumn.chainscript.Link;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.adapters.ByteBufferGsonAdapter;
import com.stratumn.sdk.adapters.FileWrapperGsonAdapter;
import com.stratumn.sdk.adapters.PathGsonAdapter;
import com.stratumn.sdk.graph.GraphQl;
import com.stratumn.sdk.model.api.GraphResponse;
import com.stratumn.sdk.model.client.PrivateKeySecret;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.file.MediaRecord;
import com.stratumn.sdk.model.misc.Property;
import com.stratumn.sdk.model.sdk.SdkConfig;
import com.stratumn.sdk.model.sdk.SdkOptions;
import com.stratumn.sdk.model.trace.AddTagsToTraceInput;
import com.stratumn.sdk.model.trace.AppendLinkInput;
import com.stratumn.sdk.model.trace.GetTraceDetailsInput;
import com.stratumn.sdk.model.trace.GetTraceStateInput;
import com.stratumn.sdk.model.trace.Info;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.ParentLink;
import com.stratumn.sdk.model.trace.PullTransferInput;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.SearchTracesFilter;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceLinkBuilderConfig;
import com.stratumn.sdk.model.trace.TraceStageType;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;
/**
 * The Stratumn java Sdk
 */
public class Sdk<TState> implements ISdk<TState> {

   private static final Gson gson = new Gson();
   private SdkOptions opts;

   private SdkConfig config;

   private Client client;

   public Sdk(SdkOptions opts) {
      this.opts = opts;
      this.client = new Client(opts);
      this.pingTrace();
   }

   private void pingTrace() {
      try {
         // ping trace to test network connectivity.
         String url = String.format("%s/healthz", this.opts.getEndpoints().getTrace());
         HttpURLConnection connection;
         if (this.opts.getProxy() != null) {
            connection = (HttpURLConnection) new URL(url).openConnection(this.opts.getProxy());
         } else {
            connection = (HttpURLConnection) new URL(url).openConnection();
         }
         int responseCode = connection.getResponseCode();
         if (responseCode != 200) {
            System.out.printf("Could not call Trace (%d)\n", responseCode);
         }

      } catch (Exception e) {
         System.err.print("Could not call Trace");
         e.printStackTrace();
         return;
      }

      System.out.println("================================================================================");
      System.out.println("                    Connection to trace established                             ");
      System.out.println("================================================================================");
   }

   /**
    * Retrieves the Sdk config for the given workflow. If the config has not yet
    * been computed, the Sdk will run a GraphQL query to retrieve the relevant info
    * and will generate the config.
    * 
    * @throws Exception
    * 
    * @return the Sdk config object
    */
   private SdkConfig getConfig() throws TraceSdkException {
      // if the config already exists use it!
      if (this.config != null)
         return this.config;

      String workflowId = this.opts.getWorkflowId();
      // execute graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_CONFIG,
            Collections.singletonMap("workflowId", workflowId), null, GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
      JsonElement groupNodes = response.getData("workflow.groups.nodes");
      if (groupNodes == null)
         throw new TraceSdkException("Workflow.groups object not found:\n" + response.toString());

      JsonElement memberNodes = response.getData("account.memberOf.nodes");
      String accountId = response.getData("account.accountId").getAsString();
      String userId = response.getData("account.userId").getAsString();

      List<String> myAccounts = new ArrayList<String>();
      // get all the account ids I am a member of
      Iterator<JsonElement> iteratorNodes = memberNodes.getAsJsonArray().iterator();
      while (iteratorNodes.hasNext()) {
         JsonElement element = iteratorNodes.next();
         myAccounts.add(element.getAsJsonObject().get("accountId").toString());
      }

      List<JsonElement> myGroups = new ArrayList<JsonElement>();
      // get all the groups that are owned by one of my accounts
      Iterator<JsonElement> iteratorGNodes = groupNodes.getAsJsonArray().iterator();
      while (iteratorGNodes.hasNext()) {
         JsonElement group = iteratorGNodes.next();
         if (myAccounts.contains(group.getAsJsonObject().get("accountId").toString())) {
            myGroups.add(group);
         }
      }

      // there must be at most one group!
      if (myGroups.size() > 1) {
         throw new TraceSdkException("More than one group to choose from.");
      }

      // // there must be at least one group!
      if (myGroups.size() == 0) {
         throw new TraceSdkException("No group to choose from.");
      }

      // extract info from my only group
      String groupId = myGroups.get(0).getAsJsonObject().get("groupId").getAsString();

      String ownerId = myGroups.get(0).getAsJsonObject().get("accountId").getAsString();
      PrivateKey signingPrivateKey = null;
      try {
         if (Secret.isPrivateKeySecret(opts.getSecret())) {
            // if the secret is a PrivateKeySecret, use it!
            final String privateKey = ((PrivateKeySecret) opts.getSecret()).getPrivateKey();
            signingPrivateKey = CryptoUtils.decodePrivateKey(privateKey);
         } else {
            JsonElement privateKeyElt = response.getData("account.signingKey.privateKey");
            JsonObject privateKey = privateKeyElt.getAsJsonObject();
            Boolean passwordProtected = privateKey.get("passwordProtected").getAsBoolean();
            String decrypted = privateKey.get("decrypted").getAsString();
            if (!passwordProtected)
               // otherwise use the key from the response
               // if it's not password protected!
               signingPrivateKey = CryptoUtils.decodePrivateKey(decrypted);
            else
               throw new TraceSdkException("Cannot get signing private key");
         }
      } catch (InvalidKeySpecException ex) {
         throw new TraceSdkException("Security key error", ex);
      }

      Map<String, String> actionNames = new HashMap<String, String>();
      this.config = new SdkConfig(workflowId, userId, accountId, groupId, ownerId, actionNames, signingPrivateKey);

      // return the new config
      return this.config;

   }

   /**
    * Builds the TraceState object from the TraceState fragment response
    *
    * @param trace the trace fragment response
    * @throws Exception
    * @throws IllegalArgumentException
    * @return the trace state
    */
   private <TLinkData> TraceState<TState, TLinkData> makeTraceState(JsonObject trace) throws TraceSdkException {
      String raw = trace.get("head").getAsJsonObject().get("raw").toString();
      JsonElement data = trace.get("head").getAsJsonObject().get("data");

      TraceLink<TLinkData> headLink = TraceLink.fromObject(raw, (TLinkData) data);
      TraceState<TState, TLinkData> traceState;
      try {
         traceState = new TraceState<TState, TLinkData>(headLink.traceId(), headLink, headLink.createdAt(),
               headLink.createdBy(), (TState) trace.get("state").getAsJsonObject().get("data"), new String[0] // TODO
                                                                                                              // parse
                                                                                                              // this
                                                                                                              // trace.tags
                                                                                                              // || []
         );
      } catch (ChainscriptException e) {
         throw new TraceSdkException("Error constructing traceState ", e);
      }
      return traceState;
   }

   /**
    * Creates a new Link from the given builder, signs it and executes the GraphQL
    * mutation.
    *
    * @param input the input argument to create the Link
    * @throws TraceSdkException
    * @throws ChainscriptException
    * @throws Exception
    * @return the new Trace
    */
   private <TLinkData> TraceState<TState, TLinkData> createLink(TraceLinkBuilder<TLinkData> linkBuilder)
         throws TraceSdkException, ChainscriptException {
      // extract signing key from config
      SdkConfig sdkConfig = this.getConfig();

      PrivateKey signingPrivateKey = sdkConfig.getSigningPrivateKey();

      // build the link
      TraceLink<TLinkData> link = linkBuilder.build();

      // sign the link
      link.sign(signingPrivateKey.getEncoded(), "[version,data,meta]");

      Map<String, Object> linkObj = JsonHelper.objectToMap(link.getLink());

      Map<String, Object> dataObj = JsonHelper.objectToMap(((TraceLink<TLinkData>) link).formData());

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("link", linkObj);
      variables.put("data", dataObj);

      // execute graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.MUTATION_CREATELINK, variables, null,
            GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
      JsonElement trace = response.getData("createLink.trace");
      if (trace == null)
         throw new TraceSdkException("Trace object not found:\n" + response.toString());
      return this.makeTraceState(trace.getAsJsonObject());
   }

   /**
    * Given a trace id or a previous link return the previous link.
    * 
    * @param <TLinkData>
    *
    * @param input       .traceId the id of the trace
    * @param input       .prevLink the previous link
    * @throws TraceSdkException
    * @throws IllegalArgumentException
    */
   private <TLinkData> TraceLink<TLinkData> getHeadLink(ParentLink<TLinkData> input) throws TraceSdkException {
      TraceLink<TLinkData> headLink = input.getPrevLink();
      // if prevLink was not provided
      if (headLink == null && input.getTraceId() != null) {
         // execute graphql query
         GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_GETHEADLINK,
               Collections.singletonMap("traceId", input.getTraceId()), null, GraphResponse.class);
         if (response.hasErrors())
            throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());

         String raw = response.getData("trace.head.raw").toString();
         JsonElement headData = response.getData("trace.head.data");
         TLinkData data = (TLinkData) (headData != null ? headData.getAsJsonObject() : null);

         // convert the raw response to a link object
         headLink = new TraceLink<TLinkData>(Link.fromObject(raw), data);

      }
      if (headLink != null)
         return headLink;
      else
         throw new TraceSdkException("Previous link or trace Id must be provided");

   }

   /**
    * Get the traces in a given stage (INCOMING, OUTGOING, BACKLOG, ATTESTATION)
    * When stageType=ATTESTATION, you must also provide the form id to identify the
    * stage. If no stage correspond to the stageType x formId, it will throw. If
    * more than one stage is found it will also throw.
    *
    * @param stageType      the stage type
    * @param paginationInfo the pagination info
    * @param formId         (optional) the formId in case of ATTESTATION
    * @return the traces in a given stage
    * @throws Error
    * @throws Exception
    */
   private <TLinkData> TracesState<TState, TLinkData> getTracesInStage(TraceStageType stageType,
         PaginationInfo paginationInfo, String formId) throws TraceSdkException {

      // formId can only be set in ATTESTATION case
      if (stageType == TraceStageType.ATTESTATION && formId == null) {
         throw new TraceSdkException("You must and can only provide formId when stageType is ATTESTATION");
      }
      // extract info from config
      SdkConfig sdkConfig = this.getConfig();

      String groupId = sdkConfig.getGroupId();

      // create variables
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("groupId", groupId);
      variables.put("stageType", stageType.toString());
      variables.put("formId", formId);
      Map<String, Object> variablesPaginationInfo = JsonHelper.objectToMap(paginationInfo);
      variables.putAll(variablesPaginationInfo);

      // execute the graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_GETTRACESINSTAGE, variables, null,
            GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
      // extract relevant info from the response
      JsonArray stages = response.getData("group.stages.nodes").getAsJsonArray();

      // there must be exactly one stage
      if (stages.size() == 1) {
         JsonObject stage = stages.get(0).getAsJsonObject();

         JsonObject trace = stage.get("traces").getAsJsonObject();
         // extract traces response and pagination
         JsonObject info = trace.get("info").getAsJsonObject();
         int totalCount = trace.get("totalCount").getAsInt();
         List<TraceState<TState, TLinkData>> traces = new ArrayList<TraceState<TState, TLinkData>>();

         // get all the groups that are owned by one of my accounts
         Iterator<JsonElement> iteratorNodes = trace.get("nodes").getAsJsonArray().iterator();
         while (iteratorNodes.hasNext()) {
            JsonObject node = (JsonObject) iteratorNodes.next();
            traces.add(this.makeTraceState(node));
         }

         TracesState<TState, TLinkData> tracesList = new TracesState<TState, TLinkData>();
         tracesList.setTraces(traces);

         tracesList.setTraces(traces);
         tracesList.setTotalCount(totalCount);
         tracesList.setInfo(gson.fromJson(info, Info.class));
         return tracesList;
      }
      
      // compute detail for error
      String stageDetail = stageType.toString() + (formId != null ? formId : "");
      if (formId != null) {
         stageDetail += formId;
      }
      // throw if no stages were found if
      if (stages.size() == 0) {
         throw new TraceSdkException("No " + stageDetail + " stage");
      }
      // throw if multiple stages were found throw new
      throw new TraceSdkException("Multiple " + stageDetail + " stages");

   }

   /**
    * Extract, upload and replace all file wrappers in a link data object.
    *
    * @param data the link data that contains file wrappers to upload
    * @throws TraceSdkException
    * @throws ExecutionException
    * @throws InterruptedException
    */
   private <TLinkData> void uploadFilesInLinkData(TLinkData data)
         throws InterruptedException, ExecutionException, TraceSdkException {
      // extract all FileWrappers from the data.
      Map<String, Property<FileWrapper>> fileWrapperMap = Helpers.extractFileWrappers(data);
      if (fileWrapperMap.size() == 0)
         return;

      List<FileWrapper> fileList = new ArrayList<FileWrapper>();
      for (Property<FileWrapper> fileProperty : fileWrapperMap.values()) {
         FileWrapper fileWrapper = fileProperty.getValue();
         fileList.add(fileWrapper);
      }
      MediaRecord[] mediaRecords = client.uploadFiles(fileList );

      List<Property<FileRecord>> fileRecordList = new ArrayList<>(fileWrapperMap.size());
      // find the filewrapper and build filerecord
      for (int i = 0; i < mediaRecords.length; i++) {
         MediaRecord mediaRecord = mediaRecords[i];
         // get the fileWrapper property by index of file in the list uploaded.
         Property<FileWrapper> fileWrapperProp = fileWrapperMap.get(fileList.get(i).getId());
         // build FileRecord property
         Property<FileRecord> fileRecordProp = fileWrapperProp
               .transform((fileWrapper) -> new FileRecord(mediaRecord, fileWrapper.info()));
         fileRecordList.add(fileRecordProp);
      }
      Helpers.assignObjects(fileRecordList);

   }
   /**
    * Extract, download and replace all file records in a data object.
    * 
    * @param data the data that contains file records to download
    * @return
    */
   public <TData> TData downloadFilesInObject(TData data) throws TraceSdkException, HttpError {
      Map<String, Property<FileRecord>> idToFileRecordMap = Helpers.extractFileRecords(data);

      List<Property<FileWrapper>> fileWrapperList = this.downloadFiles(idToFileRecordMap);
      // replace filerecords with fileWrappers
      Helpers.assignObjects(fileWrapperList);

      return data;
   }

   /***
    * @param idToFileRecordMap
    * @return
    * @throws HttpError
    * @throws TraceSdkException
    */
   private List<Property<FileWrapper>> downloadFiles(Map<String, Property<FileRecord>> idToFileRecordMap)
         throws TraceSdkException, HttpError {
      List<Property<FileWrapper>> fileWrapperList = new ArrayList<Property<FileWrapper>>();
      if (idToFileRecordMap.size() == 0) {
         return fileWrapperList;
      }

      for (Entry<String, Property<FileRecord>> fileRecordElt : idToFileRecordMap.entrySet()) {
         FileRecord fileRecord = fileRecordElt.getValue().getValue();
         ByteBuffer file = client.downloadFile(fileRecord);
         fileWrapperList.add(
               fileRecordElt.getValue().transform((T) -> FileWrapper.fromFileBlob(file, fileRecord.getFileInfo())));
      }
      return fileWrapperList;
   }

   /**
    * Get the details of a given trace.
    *
    * @param input the getTraceDetails input
    * @return the trace details
    * @throws Exception
    */
   @Override
   public <TLinkData> TraceDetails<TLinkData> getTraceDetails(GetTraceDetailsInput input) throws Exception {

      Map<String, Object> getTraceDetailsInput = JsonHelper.objectToMap(input);
      // execute graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_GETTRACEDETAILS, getTraceDetailsInput, null,
            GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());

      JsonObject info = response.getData("trace.links.info").getAsJsonObject();
      int totalCount = response.getData("trace.links.totalCount").getAsInt();
      List<TraceLink<TLinkData>> links = new ArrayList<TraceLink<TLinkData>>();

      // get all the groups that are owned by one of my accounts
      Iterator<JsonElement> iteratorNodes = response.getData("trace.links.nodes").getAsJsonArray().iterator();

      while (iteratorNodes.hasNext()) {
         JsonObject node = (JsonObject) iteratorNodes.next();

         links.add(
               (TraceLink<TLinkData>) TraceLink.fromObject(node.get("raw").toString(), node.get("data").toString()));
      }

      // construct the link objects from raw responses
      // the details response object
      return new TraceDetails<TLinkData>(links, totalCount, gson.fromJson(info, Info.class));
   }

   @Override
   public <TLinkData> TraceState<TState, TLinkData> getTraceState(GetTraceStateInput input) throws Exception {
      // create variables
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_GETTRACESTATE,
            Collections.singletonMap("traceId", input.getTraceId()), null, GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
      
      JsonElement traceElt = response.getData("trace");
      if (traceElt==null)  throw new TraceSdkException("Trace " + input.getTraceId() + " not found." );
      return this.makeTraceState(traceElt.getAsJsonObject());

   }

   /**
    * Get the traces in a given attestation stage.
    *
    * @param paginationInfo the pagination info
    * @return the backlog traces
    * @throws Error
    * @throws Exception
    */
   @Override
   public <TLinkData> TracesState<TState, TLinkData> getAttestationTraces(String formId, PaginationInfo paginationInfo)
         throws Exception {

      return this.getTracesInStage(TraceStageType.ATTESTATION, paginationInfo, formId);
   }

   @Override
   public <TLinkData> TracesState<TState, TLinkData> getIncomingTraces(PaginationInfo paginationInfo)
         throws Error, Exception {
      return this.getTracesInStage(TraceStageType.INCOMING, paginationInfo, null);
   }

   /**
    * Get the outgoing traces.
    *
    * @param paginationInfo the pagination info
    * @return the outgoing traces
    * @throws Error
    * @throws Exception
    */
   @Override
   public <TLinkData> TracesState<TState, TLinkData> getOutgoingTraces(PaginationInfo paginationInfo) throws Exception {
      return this.getTracesInStage(TraceStageType.OUTGOING, paginationInfo, null);
   }

   /**
    * Get the backlog traces.
    *
    * @param paginationInfo the pagination info
    * @return the backlog traces
    * @throws Error
    * @throws Exception
    */
   @Override
   public <TLinkData> TracesState<TState, TLinkData> getBacklogTraces(PaginationInfo paginationInfo) throws Exception {
      return this.getTracesInStage(TraceStageType.BACKLOG, paginationInfo, null);

   }

   /**
    * Creates a new Trace.
    *
    * @param input the newTrace input argument
    * @return the new Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> newTrace(NewTraceInput<TLinkData> input) throws Exception {

      // extract info from input
      String formId = input.getFormId();
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String ownerId = sdkConfig.getOwnerId();
      String groupId = sdkConfig.getGroupId();
      Map<String, String> actionNames = sdkConfig.getActionNames();
      // upload files and transform data
      this.uploadFilesInLinkData(data);
      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      cfg.setWorkflowId(workflowId);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is an attestation
      linkBuilder.forAttestation(formId, actionNames.get(formId), data)
            // add owner info
            .withOwner(ownerId)
            // add group info
            .withGroup(groupId)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return this.createLink(linkBuilder);
   }

   /**
    * Accept a transfer of ownership
    *
    * @param input the acceptTransfer input argument
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> acceptTransfer(TransferResponseInput<TLinkData> input)
         throws Exception {

      // retrieve parent link
      TraceLink<TLinkData> parentLink = this.getHeadLink(input);

      // extract info from input
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String ownerId = sdkConfig.getOwnerId();
      String groupId = sdkConfig.getGroupId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is an attestation
      linkBuilder.forAcceptTransfer(data)
            // add owner info
            .withOwner(ownerId)
            // add group info
            .withGroup(groupId)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);
   }

   /**
    * Reject a transfer of ownership
    *
    * @param input the rejectTransfer input argument
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> rejectTransfer(TransferResponseInput<TLinkData> input)
         throws Exception {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null,
            null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is a push transfer
      linkBuilder.forRejectTransfer(data)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);

   }

   /**
    * Cancel a transfer of ownership
    *
    * @param input the cancelTransfer input argument
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> cancelTransfer(TransferResponseInput<TLinkData> input)
         throws Exception {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null,
            null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      linkBuilder // this is to cancel the transfer
            .forCancelTransfer(data)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);

   }

   /**
    * Add tags to an existing trace.
    *
    * @param input the input argument
    * @throws Exception
    * @throws IllegalArgumentException
    * @return the Trace
    */
   public <TLinkData> TraceState<TState, TLinkData> addTagsToTrace(AddTagsToTraceInput input)
         throws IllegalArgumentException, Exception {
      String traceId = input.getTraceId();
      String[] tags = input.getTags();
      // TODO implement
      // build and return the TraceState object
      return this.makeTraceState(null);// rsp.addTagsToTrace.trace);
   }

   /**
    * Appends a new Link to a Trace.
    *
    * @param input the appendLink input argument
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> appendLink(AppendLinkInput<TLinkData> input) throws Exception {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null,
            null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      // extract info from input
      String formId = input.getFormId();
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String ownerId = sdkConfig.getOwnerId();
      String groupId = sdkConfig.getGroupId();
      Map<String, String> actionNames = sdkConfig.getActionNames();
      // upload files and transform data
      this.uploadFilesInLinkData(data);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is an attestation
      linkBuilder.forAttestation(formId, actionNames.get(formId), data)
            // add owner info
            .withOwner(ownerId)
            // add group info
            .withGroup(groupId)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);

   }

   /**
    * Push a trace to a recipient group.
    *
    * @param input the pushTrace input argument
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> pushTrace(PushTransferInput<TLinkData> input) throws Exception {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null,
            null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      // extract info from input
      String recipient = input.getRecipient();
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is a push transfer
      linkBuilder.forPushTransfer(recipient, data)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);
   }

   /**
    * Pull a trace from a group.
    *
    * @param input the pullTrace input argument
    * @throws ChainscriptException
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> pullTrace(PullTransferInput<TLinkData> input)
         throws ChainscriptException, TraceSdkException {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null,
            null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String groupId = sdkConfig.getGroupId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is a push transfer
      linkBuilder.forPullTransfer(groupId, data)
            // add creator info
            .withCreatedBy(userId);
      // call createLink helper
      return (TraceState<TState, TLinkData>) this.createLink(linkBuilder);
   }

   /**
    * Search all the traces of the workflow
    */
   public void searchTraces(SearchTracesFilter filter, PaginationInfo paginationInfo) {

   }

}
