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

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.stratumn.sdk.adapters.IdentifiableGsonAdapter;
import com.stratumn.sdk.adapters.PathGsonAdapter;
import com.stratumn.sdk.adapters.TimestampAdapter;
import com.stratumn.sdk.graph.GraphQl;
import com.stratumn.sdk.model.api.GraphResponse;
import com.stratumn.sdk.model.client.PrivateKeySecret;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.file.FileInfo;
import com.stratumn.sdk.model.file.MediaRecord;
import com.stratumn.sdk.model.misc.Identifiable;
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
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.SearchTracesFilter;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceLinkBuilderConfig;
import com.stratumn.sdk.model.trace.TraceStageType;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;

import org.springframework.web.client.HttpClientErrorException;

/**
 * The Stratumn java Sdk
 */
public class Sdk<TState> implements ISdk<TState> {

   private static final String ERROR_CONFIG_DEPRECATED = "link config deprecated";

   private static final Gson gson = new Gson();
   private SdkOptions opts;

   private SdkConfig config;

   private Client client;
   // the class Tstate if none set then all methods assume TState is a JsonObject
   private Class<TState> classOfTState;

   public Sdk(SdkOptions opts) {
      this(opts, null);
   }

   /***
    * overloaded Constructor to provide proper casting
    * 
    * @param opts
    * @param classOfTState
    */
   public Sdk(SdkOptions opts, Class<TState> classOfTState) {
      if (classOfTState != null)
         this.classOfTState = classOfTState;
      this.opts = opts;
      this.client = new Client(opts);
      JsonHelper.registerTypeHierarchyAdapter(ByteBuffer.class, new ByteBufferGsonAdapter());
      JsonHelper.registerTypeHierarchyAdapter(Path.class, new PathGsonAdapter());
      JsonHelper.registerTypeAdapter(FileWrapper.class, new FileWrapperGsonAdapter());
      JsonHelper.registerTypeAdapter(Identifiable.class, new IdentifiableGsonAdapter());
      JsonHelper.registerTypeAdapter(Date.class, new TimestampAdapter());

   }

   /**
    * Retrieves the Sdk config for the given workflow. If the config has not yet
    * been computed, the Sdk will run a GraphQL query to retrieve the relevant info
    * and will generate the config.
    * 
    * @param forceUpdate set to true if we want to force update the config
    * @throws Exception
    * 
    * @return the Sdk config object
    */
   private SdkConfig getConfig(boolean forceUpdate) throws TraceSdkException {
      // update the config if doesn't exist or force
      if (this.config == null || forceUpdate) {
         String workflowId = this.opts.getWorkflowId();
         // execute graphql query
         GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_CONFIG,
               Collections.singletonMap("workflowId", workflowId), null, GraphResponse.class);
         if (response.hasErrors())
            throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
         JsonElement groupNodes = response.getData("workflow.groups.nodes");
         if (groupNodes == null)
            throw new TraceSdkException("Workflow.groups object not found:\n" + response.toString());

         String accountId = response.getData("account.accountId").getAsString();

         String configId = response.getData("workflow.config.id").getAsString();

         JsonElement userMemberOf = response.getData("account.user.memberOf.nodes");
         JsonElement botTeams = response.getData("account.bot.teams.nodes");

         List<String> myAccounts = new ArrayList<String>();
         Iterator<JsonElement> iteratorNodes = null;
         // get all the account ids I am a member of
         if (null != userMemberOf) {
            iteratorNodes = userMemberOf.getAsJsonArray().iterator();
         } else if (null != botTeams) {
            iteratorNodes = botTeams.getAsJsonArray().iterator();
         }
         if (null != iteratorNodes) {
            while (iteratorNodes.hasNext()) {
               JsonElement element = iteratorNodes.next();
               myAccounts.add(element.getAsJsonObject().get("accountId").toString());
            }
         }

         // get all the groups I belong to
         // i.e. where I belong to one of the account members
         List<JsonElement> myGroups = new ArrayList<JsonElement>();
         Map<String, String> groupLabelToIdMap = new HashMap<String, String>();

         Iterator<JsonElement> iteratorGNodes = groupNodes.getAsJsonArray().iterator();
         while (iteratorGNodes.hasNext()) {
            JsonElement group = iteratorGNodes.next();

            Iterator<JsonElement> members = group.getAsJsonObject().get("members").getAsJsonObject().get("nodes")
                  .getAsJsonArray().iterator();
            while (members.hasNext()) {
               JsonElement member = members.next();
               if (myAccounts.contains(member.getAsJsonObject().get("accountId").toString())) {
                  myGroups.add(group);
                  groupLabelToIdMap.put(group.getAsJsonObject().get("label").getAsString(),
                        group.getAsJsonObject().get("groupId").getAsString());
                  break;
               }
            }
         }

         // there must be at least one group!
         if (myGroups.size() == 0) {
            throw new TraceSdkException("No group to choose from.");
         }

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

         this.config = new SdkConfig(workflowId, configId, accountId, groupLabelToIdMap, signingPrivateKey);

      }

      // sets the group id in any case
      if (null != this.opts.getGroupLabel()) {
         this.config.setGroupLabel(this.opts.getGroupLabel());
      }

      // return the new config
      return this.config;

   }

   private SdkConfig getConfig() throws TraceSdkException {
      return this.getConfig(false);
   }

   public Sdk<TState> withGroupLabel(String groupLabel) {
      this.opts.setGroupLabel(groupLabel);
      return this;
   }

   /***
    * Builds the TraceState object form the TraceState fragement response for a
    * specifc type
    * 
    * @param trace
    * @param classOfTLinkData
    * @return
    * @throws TraceSdkException
    */
   private <TLinkData> TraceState<TState, TLinkData> makeTraceState(JsonObject trace, Class<TLinkData> classOfTLinkData)
         throws TraceSdkException {
      String raw = trace.get("head").getAsJsonObject().get("raw").toString();
      JsonElement dataElt = trace.get("head").getAsJsonObject().get("data");

      @SuppressWarnings("unchecked")
      TLinkData data = classOfTLinkData == null ? (TLinkData) dataElt
            : JsonHelper.objectToObject(dataElt, classOfTLinkData);
      TraceLink<TLinkData> headLink = TraceLink.fromObject(raw, data);
      TraceState<TState, TLinkData> traceState;
      try {
         List<String> tags = new ArrayList<String>();
         if (trace.get("tags") != null) {
            JsonArray tagsElt = trace.get("tags").getAsJsonArray();
            tagsElt.forEach((elt) -> tags.add(elt.toString()));
         }
         @SuppressWarnings("unchecked")
         TState tState = classOfTState != null
               ? JsonHelper.fromJson(trace.get("state").getAsJsonObject().get("data"), classOfTState)
               : (TState) trace.get("state").getAsJsonObject().get("data");
         traceState = new TraceState<TState, TLinkData>(headLink.traceId(), headLink, headLink.createdAt(),
               headLink.createdBy(), tState, tags.toArray(new String[tags.size()]), headLink.group());
      } catch (ChainscriptException e) {
         throw new TraceSdkException("Error constructing traceState ", e);
      }
      return traceState;
   }

   /***
    * * Creates a new Link from the given builder, signs it and executes the
    * GraphQL mutation.
    * 
    * @param linkBuilder
    * @param classOfTLinkData
    * @param firstTry         if this is not the first retry, do not retry
    * @return
    * @throws TraceSdkException
    * @throws ChainscriptException
    */
   private <TLinkData> TraceState<TState, TLinkData> createLink(TraceLinkBuilder<TLinkData> linkBuilder,
         Class<TLinkData> classOfTLinkData, boolean firstTry) throws TraceSdkException, ChainscriptException {
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

      try {
         // execute graphql query
         GraphResponse response = this.client.graphql(GraphQl.Query.MUTATION_CREATELINK, variables, null,
               GraphResponse.class);
         if (response.hasErrors()) {
            throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());
         }
         JsonElement trace = response.getData("createLink.trace");
         if (trace == null)
            throw new TraceSdkException("Trace object not found:\n" + response.toString());

         return this.makeTraceState(trace.getAsJsonObject(), classOfTLinkData);

      } catch (HttpClientErrorException e) {
         if (firstTry && e.getMessage().contains(ERROR_CONFIG_DEPRECATED)) {
            sdkConfig = this.getConfig(true);
            linkBuilder.withConfigId(sdkConfig.getConfigId());
            return this.createLink(linkBuilder, classOfTLinkData, false);
         }
         throw e;
      }
   }

   /***
    * * Creates a new Link from the given builder, signs it and executes the
    * GraphQL mutation.
    * 
    * @param linkBuilder
    * @param classOfTLinkData
    * @return
    * @throws TraceSdkException
    * @throws ChainscriptException
    */
   private <TLinkData> TraceState<TState, TLinkData> createLink(TraceLinkBuilder<TLinkData> linkBuilder,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException, ChainscriptException {
      return this.createLink(linkBuilder, classOfTLinkData, true);
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
    * stage. If no stage correspond to the stageType x actionKey, it will throw. If
    * more than one stage is found it will also throw.
    *
    * @param stageType      the stage type
    * @param paginationInfo the pagination info
    * @param actionKey      (optional) the actionKey in case of ATTESTATION
    * @return the traces in a given stage
    * @throws Error
    * @throws Exception
    */
   @Deprecated
   private <TLinkData> TracesState<TState, TLinkData> getTracesInStage(TraceStageType stageType,
         PaginationInfo paginationInfo, String actionKey, Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // actionKey can only be set in ATTESTATION case
      if (stageType == TraceStageType.ATTESTATION && actionKey == null) {
         throw new TraceSdkException("You must and can only provide actionKey when stageType is ATTESTATION");
      }
      // extract info from config
      SdkConfig sdkConfig = this.getConfig();

      String groupId = sdkConfig.getGroupId();

      // create variables
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("groupId", groupId);
      variables.put("stageType", stageType.toString());

      if (actionKey != null) {
         variables.put("actionKey", actionKey);
      }

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
            traces.add(this.makeTraceState(node, classOfTLinkData));
         }

         TracesState<TState, TLinkData> tracesList = new TracesState<TState, TLinkData>();
         tracesList.setTraces(traces);

         tracesList.setTotalCount(totalCount);
         tracesList.setInfo(gson.fromJson(info, Info.class));
         return tracesList;
      }

      // compute detail for error
      String stageDetail = stageType.toString() + (actionKey != null ? actionKey : "");

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
    */
   private <TLinkData> void uploadFilesInLinkData(TLinkData data) throws TraceSdkException {
      // extract all FileWrappers from the data.
      Map<String, Property<FileWrapper>> fileWrapperMap = Helpers.extractFileWrappers(data);
      if (fileWrapperMap.size() == 0)
         return;

      List<FileWrapper> fileList = new ArrayList<FileWrapper>();
      for (Property<FileWrapper> fileProperty : fileWrapperMap.values()) {
         FileWrapper fileWrapper = fileProperty.getValue();
         fileList.add(fileWrapper);
      }
      MediaRecord[] mediaRecords = client.uploadFiles(fileList);

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
         FileInfo info = fileRecord.getFileInfo();
         fileWrapperList.add(fileRecordElt.getValue()
               .transform((T) -> new FileBlobWrapper(file, info, info.getKey() == null || info.getKey() == "")));
      }
      return fileWrapperList;
   }

   /**
    * Get the details of a given trace.
    *
    * @param input the getTraceDetails input
    * @return the trace details
    * @throws TraceSdkException
    */
   @Override
   public <TLinkData> TraceDetails<TLinkData> getTraceDetails(GetTraceDetailsInput input) throws TraceSdkException {

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
   public <TLinkData> TraceState<TState, TLinkData> getTraceState(GetTraceStateInput input) throws TraceSdkException {
      return getTraceState(input, null);

   }

   @Override
   public <TLinkData> TraceState<TState, TLinkData> getTraceState(GetTraceStateInput input,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {
      // create variables
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_GETTRACESTATE,
            Collections.singletonMap("traceId", input.getTraceId()), null, GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());

      JsonElement traceElt = response.getData("trace");
      if (traceElt == null)
         throw new TraceSdkException("Trace " + input.getTraceId() + " not found.");
      return this.makeTraceState(traceElt.getAsJsonObject(), classOfTLinkData);

   }

   /**
    * Get the traces in a given attestation stage.
    *
    * @param paginationInfo the pagination info
    * @return the backlog traces
    * @throws TraceSdkException
    */
   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getAttestationTraces(String actionKey,
         PaginationInfo paginationInfo) throws TraceSdkException {

      return this.getTracesInStage(TraceStageType.ATTESTATION, paginationInfo, actionKey, null);
   }

   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getAttestationTraces(String actionKey,
         PaginationInfo paginationInfo, Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      return this.getTracesInStage(TraceStageType.ATTESTATION, paginationInfo, actionKey, classOfTLinkData);
   }

   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getIncomingTraces(PaginationInfo paginationInfo)
         throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.INCOMING, paginationInfo, null, null);
   }

   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getIncomingTraces(PaginationInfo paginationInfo,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.INCOMING, paginationInfo, null, classOfTLinkData);
   }

   /**
    * Get the outgoing traces.
    *
    * @param paginationInfo the pagination info
    * @return the outgoing traces
    * @throws TraceSdkException
    */
   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getOutgoingTraces(PaginationInfo paginationInfo)
         throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.OUTGOING, paginationInfo, null, null);
   }

   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getOutgoingTraces(PaginationInfo paginationInfo,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.OUTGOING, paginationInfo, null, classOfTLinkData);
   }

   /**
    * Get the backlog traces.
    *
    * @param paginationInfo the pagination info
    * @return the backlog traces
    * @throws TraceSdkException
    */
   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getBacklogTraces(PaginationInfo paginationInfo)
         throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.BACKLOG, paginationInfo, null, null);

   }

   @Override
   @Deprecated
   public <TLinkData> TracesState<TState, TLinkData> getBacklogTraces(PaginationInfo paginationInfo,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {
      return this.getTracesInStage(TraceStageType.BACKLOG, paginationInfo, null, classOfTLinkData);

   }

   /**
    * Creates a new Trace.
    *
    * @param input the newTrace input argument
    * @throws TraceSdkException
    * @return the new Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> newTrace(NewTraceInput<TLinkData> input) throws TraceSdkException {

      // extract info from input
      String action = input.getAction();
      TLinkData data = input.getData();
      String groupLabel = input.getGroupLabel();

      // Set the group label if it is set
      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String groupId = sdkConfig.getGroupId(groupLabel);
      String accountId = sdkConfig.getAccountId();
      // upload files and transform data
      this.uploadFilesInLinkData(data);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());

      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder;
      try {
         linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }

      // this is an attestation
      linkBuilder.forAttestation(action, data)
            // add group info
            .withGroup(groupId)
            // add creator info
            .withCreatedBy(accountId);
      @SuppressWarnings("unchecked")
      Class<TLinkData> dataClass = (Class<TLinkData>) data.getClass();
      // call createLink helper
      try {
         return this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }
   }

   /**
    * Accept a transfer of ownership
    *
    * @param input the acceptTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> acceptTransfer(TransferResponseInput<TLinkData> input)
         throws TraceSdkException {
      return acceptTransfer(input, null);
   }

   /**
    * Accept a transfer of ownership
    *
    * @param input the acceptTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> acceptTransfer(TransferResponseInput<TLinkData> input,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // retrieve parent link
      TraceLink<TLinkData> parentLink = this.getHeadLink(input);

      // extract info from input
      TLinkData data = input.getData();
      String groupLabel = input.getGroupLabel();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String accountId = sdkConfig.getAccountId();
      String groupId = sdkConfig.getGroupId(groupLabel);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder;
      try {
         linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

         // this is an attestation
         linkBuilder.forAcceptTransfer(data)
               // add group info
               .withGroup(groupId)
               // add creator info
               .withCreatedBy(accountId);
         // try to read the type from the data otherwise use the type parameter
         @SuppressWarnings("unchecked")
         Class<TLinkData> dataClass = data != null ? (Class<TLinkData>) data.getClass() : classOfTLinkData;

         // call createLink helper
         return (TraceState<TState, TLinkData>) this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }
   }

   /**
    * Reject a transfer of ownership
    *
    * @param input the rejectTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> rejectTransfer(TransferResponseInput<TLinkData> input)
         throws TraceSdkException {
      return rejectTransfer(input, null);
   }

   /**
    * Reject a transfer of ownership
    *
    * @param input the rejectTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> rejectTransfer(TransferResponseInput<TLinkData> input,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(null, input.getTraceId());
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      TLinkData data = input.getData();
      String groupLabel = input.getGroupLabel();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String accountId = sdkConfig.getAccountId();
      String groupId = sdkConfig.getGroupId(groupLabel);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      try {
         // use a TraceLinkBuilder to create the first link
         // only provide workflowId to initiate a new trace
         TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

         // this is a push transfer
         linkBuilder.forRejectTransfer(data)
               // add group info
               .withGroup(groupId)
               // add creator info
               .withCreatedBy(accountId);
         // try to read the type from the data otherwise use the parameter
         @SuppressWarnings("unchecked")
         Class<TLinkData> dataClass = data != null ? (Class<TLinkData>) data.getClass() : classOfTLinkData;
         // call createLink helper
         return (TraceState<TState, TLinkData>) this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }

   }

   /**
    * Cancel a transfer of ownership
    *
    * @param input the cancelTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> cancelTransfer(TransferResponseInput<TLinkData> input)
         throws TraceSdkException {

      return cancelTransfer(input, null);

   }

   /**
    * Cancel a transfer of ownership
    *
    * @param input the cancelTransfer input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> cancelTransfer(TransferResponseInput<TLinkData> input,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(null, input.getTraceId());
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String accountId = sdkConfig.getAccountId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      try {
         // use a TraceLinkBuilder to create the first link
         // only provide workflowId to initiate a new trace
         TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

         linkBuilder // this is to cancel the transfer
               .forCancelTransfer(data)
               // add creator info
               .withCreatedBy(accountId);
         // try to read type from data else use the class parameter
         @SuppressWarnings("unchecked")
         Class<TLinkData> dataClass = data != null ? (Class<TLinkData>) data.getClass() : classOfTLinkData;
         // call createLink helper
         return (TraceState<TState, TLinkData>) this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }

   }

   /**
    * Appends a new Link to a Trace.
    *
    * @param input the appendLink input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> appendLink(AppendLinkInput<TLinkData> input)
         throws TraceSdkException {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(null, input.getTraceId());
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      // extract info from input
      String action = input.getAction();
      TLinkData data = input.getData();
      String groupLabel = input.getGroupLabel();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String accountId = sdkConfig.getAccountId();
      String groupId = sdkConfig.getGroupId(groupLabel);
      // upload files and transform data
      this.uploadFilesInLinkData(data);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      try {
         // use a TraceLinkBuilder to create the first link
         // only provide workflowId to initiate a new trace
         TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

         // this is an attestation
         linkBuilder.forAttestation(action, data)
               // add group info
               .withGroup(groupId)
               // add creator info
               .withCreatedBy(accountId);

         // try to read type from data else use the class parameter
         @SuppressWarnings("unchecked")
         Class<TLinkData> dataClass = data != null ? (Class<TLinkData>) data.getClass() : null;
         // call createLink helper
         return (TraceState<TState, TLinkData>) this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }

   }

   /**
    * Push a trace to a recipient group.
    *
    * @param input the pushTrace input argument
    * @throws TraceSdkException
    * @return the Trace
    */
   @Override
   @Deprecated
   public <TLinkData> TraceState<TState, TLinkData> pushTrace(PushTransferInput<TLinkData> input)
         throws TraceSdkException {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(null, input.getTraceId());
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      // extract info from input
      String recipient = input.getRecipient();
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String configId = sdkConfig.getConfigId();
      String accountId = sdkConfig.getAccountId();

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      cfg.setConfigId(configId);
      cfg.setEnableDebuging(this.opts.isEnableDebuging());
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      try {
         // use a TraceLinkBuilder to create the first link
         // only provide workflowId to initiate a new trace
         TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

         // this is a push transfer
         linkBuilder.forPushTransfer(recipient, data)
               // add creator info
               .withCreatedBy(accountId);
         // try to read type from data else use the class parameter
         @SuppressWarnings("unchecked")
         Class<TLinkData> dataClass = data != null ? (Class<TLinkData>) data.getClass() : null;
         // call createLink helper
         return (TraceState<TState, TLinkData>) this.createLink(linkBuilder, dataClass);
      } catch (ChainscriptException e) {
         throw new TraceSdkException(e);
      }
   }

   /**
    * Add tags to an existing trace.
    *
    * @param input the input argument
    * @throws IllegalArgumentException
    * @return the Trace
    */
   public <TLinkData> TraceState<TState, TLinkData> addTagsToTrace(AddTagsToTraceInput input) throws TraceSdkException {
      return this.addTagsToTrace(input, null);
   }

   public <TLinkData> TraceState<TState, TLinkData> addTagsToTrace(AddTagsToTraceInput input,
         Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // build variables
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("traceId", input.getTraceId());
      variables.put("tags", input.getTags());

      // execute graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.MUTATION_ADDTAGSTOTRACE, variables, null,
            GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());

      return this.makeTraceState(response.getData("addTagsToTrace.trace").getAsJsonObject(), classOfTLinkData);
   }

   /**
    * Search all the traces of the workflow
    * 
    * @param filter the filter to use in the search
    * @throws TraceSdkException
    * @return the list of traces
    */
   public <TLinkData> TracesState<TState, TLinkData> searchTraces(SearchTracesFilter filter,
         PaginationInfo paginationInfo, Class<TLinkData> classOfTLinkData) throws TraceSdkException {

      // create variables
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("filter", filter.getFilters());
      variables.put("workflowId", this.getConfig().getWorkflowId());
      Map<String, Object> variablesPaginationInfo = JsonHelper.objectToMap(paginationInfo);
      variables.putAll(variablesPaginationInfo);

      // execute graphql query
      GraphResponse response = this.client.graphql(GraphQl.Query.QUERY_SEARCHTRACES, variables, null,
            GraphResponse.class);
      if (response.hasErrors())
         throw new TraceSdkException(Arrays.asList(response.getErrors()).toString());

      List<TraceState<TState, TLinkData>> traces = new ArrayList<TraceState<TState, TLinkData>>();

      // get all the traces
      JsonObject traceResponse = response.getData("workflow.traces").getAsJsonObject();
      Iterator<JsonElement> iteratorNodes = traceResponse.get("nodes").getAsJsonArray().iterator();

      while (iteratorNodes.hasNext()) {
         JsonObject node = (JsonObject) iteratorNodes.next();

         traces.add(this.makeTraceState(node, classOfTLinkData));
      }

      // construct the traces list object
      TracesState<TState, TLinkData> tracesList = new TracesState<TState, TLinkData>();
      tracesList.setTraces(traces);

      tracesList.setTotalCount(traceResponse.get("totalCount").getAsInt());
      tracesList.setInfo(gson.fromJson(traceResponse.get("info").getAsJsonObject(), Info.class));
      return tracesList;

   }

   public <TLinkData> TracesState<TState, TLinkData> searchTraces(SearchTracesFilter filter,
         PaginationInfo paginationInfo) throws TraceSdkException {

      return this.searchTraces(filter, paginationInfo, null);

   }

}
