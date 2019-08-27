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
import java.security.PrivateKey;
import java.util.ArrayList;
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
import com.stratumn.chainscript.Link;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.graph.GraphQl;
import com.stratumn.sdk.model.client.PrivateKeySecret;
import com.stratumn.sdk.model.client.Secret;
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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
/**
 * The Stratumn java  Sdk
 */
public class Sdk<TState> implements ISdk<TState>
{

   private static final Gson gson = new Gson();
   private SdkOptions opts;

   private SdkConfig config;

   private Client client;

   public Sdk(SdkOptions opts)
   {
      this.opts = opts;
      this.client = new Client(opts);
      JsonHelper.registerTypeAdapter(FileWrapper.class, new FileWrapperSerializer());
   }
 

   /**
    * Retrieves the Sdk config for the given workflow. If the config has not yet
    * been computed, the Sdk will run a GraphQL query to retrieve the relevant info
    * and will generate the config.
    * 
    * @throws Exception
    * 
    * @returns the Sdk config object
    */
   private SdkConfig getConfig() throws Exception
   {
      // if the config already exists use it!
      if (this.config!=null)
         return this.config;
      
      String workflowId = this.opts.getWorkflowId();
        
      JsonObject jsonResponse =  this.client.graphql(GraphQl.Query.QUERY_CONFIG, Collections.singletonMap("workflowId", workflowId), null, JsonObject.class);
      JsonObject jsonData = jsonResponse.get("data").getAsJsonObject();

      // extract relevant info from the response
      JsonObject workflow = jsonData.get("workflow").getAsJsonObject() ;
      if(workflow == null || workflow.get("groups") == null)
      {
         throw new Exception("Cannot find workflow " + workflowId);
      }
      JsonObject groups = jsonData.getAsJsonObject("workflow").get("groups").getAsJsonObject(); 
      JsonObject jsonAccount = jsonData.getAsJsonObject("account"); 
      JsonObject memberOf = jsonAccount.get("memberOf").getAsJsonObject();

      String accountId = jsonAccount.get("accountId").getAsString();
      String userId = jsonAccount.get("userId").getAsString();

      List<String> myAccounts = new ArrayList<String>();
      // get all the account ids I am a member of
      Iterator<JsonElement> iteratorNodes = memberOf.get("nodes").getAsJsonArray().iterator();
      while(iteratorNodes.hasNext())
      {
         JsonElement element = iteratorNodes.next();
         myAccounts.add(element.getAsJsonObject().get("accountId").toString());
      }

      List<JsonElement> myGroups = new ArrayList<JsonElement>();
      // get all the groups that are owned by one of my accounts
      Iterator<JsonElement> iteratorGNodes = groups.get("nodes").getAsJsonArray().iterator();
      while(iteratorGNodes.hasNext())
      {
         JsonElement group = iteratorGNodes.next();
         if(myAccounts.contains(group.getAsJsonObject().get("accountId").toString()))
         {
            myGroups.add(group);
         }
      }

      // there must be at most one group!
      if(myGroups.size() > 1)
      {
         throw new Exception("More than one group to choose from.");
      }

      // // there must be at least one group!
      if(myGroups.size() == 0)
      {
         throw new Exception("No group to choose from.");
      }

      // extract info from my only group
      String groupId = myGroups.get(0).getAsJsonObject().get("groupId").getAsString();

      String ownerId = myGroups.get(0).getAsJsonObject().get("accountId").getAsString();
      PrivateKey signingPrivateKey = null;
      if(Secret.isPrivateKeySecret(opts.getSecret()))
      {
         // if the secret is a PrivateKeySecret, use it!
         final String privateKey = ((PrivateKeySecret) opts.getSecret()).getPrivateKey();
         signingPrivateKey = CryptoUtils.decodePrivateKey(privateKey);
      }
      else {
         JsonObject signingKey = jsonAccount.getAsJsonObject("account").get("signingKey").getAsJsonObject();
         JsonObject privateKey = signingKey.getAsJsonObject("privateKey");
         Boolean passwordProtected = privateKey.get("passwordProtected").getAsBoolean();
         String decrypted = privateKey.get("decrypted").getAsString();
         if (!passwordProtected)
         // otherwise use the key from the response
            // if it's not password protected!
            signingPrivateKey = CryptoUtils.decodePrivateKey(decrypted);
         else
            throw new Exception("Cannot get signing private key"); 
      }

      Map<String, String> actionNames = new HashMap<String, String>(); 
      this.config = new SdkConfig(workflowId, userId, accountId, groupId, ownerId, actionNames, signingPrivateKey);

      // return the new config
      return this.config;

   }

   /**
    * Builds the TraceState object from the TraceState fragment response
    *
    * @param trace
    *            the trace fragment response
    * @throws Exception
    * @throws IllegalArgumentException
    * @returns the trace state
    */
   private <TLinkData> TraceState<TState, TLinkData> makeTraceState(JsonObject trace) throws IllegalArgumentException, Exception
   {

      String raw = trace.get("head").getAsJsonObject().get("raw").toString();
      JsonElement data = trace.get("head").getAsJsonObject().get("data");

      TraceLink<TLinkData> headLink = TraceLink.fromObject(raw, (TLinkData) data);

      TraceState<TState, TLinkData> traceState = new TraceState<TState, TLinkData>(headLink.traceId(), headLink, headLink.createdAt(),
         headLink.createdBy(), (TState) trace.get("state").getAsJsonObject().get("data"), new String[0] //TODO parse this trace.tags || []
      );

      return traceState;
   }

   /**
    * Creates a new Link from the given builder, signs it and executes the GraphQL
    * mutation.
    *
    * @param input the input argument to create the Link
    * @throws Exception
    * @returns the new Trace
    */
   private <TLinkData> TraceState<TState, TLinkData> createLink(TraceLinkBuilder<TLinkData> linkBuilder) throws Exception
   {
      // extract signing key from config
      SdkConfig sdkConfig = this.getConfig();

      PrivateKey signingPrivateKey = sdkConfig.getSigningPrivateKey();

      // build the link
      TraceLink<TLinkData> link = linkBuilder.build();

      // sign the link
      link.sign(signingPrivateKey.getEncoded(), "[version,data,meta]");
      
     
      Map<String, Object> linkObj = JsonHelper.objectToMap(link.getLink());

      @SuppressWarnings("unchecked")
      Map<String, Object> dataObj = (Map<String, Object>) ((TraceLink<TLinkData>) link).formData();
      
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("link", linkObj); 
      variables.put("data", dataObj);

      // execute graphql query 
      JsonObject jsonResponse = this.client.graphql(GraphQl.Query.MUTATION_CREATELINK, variables, null, JsonObject.class); 
      JsonObject trace = jsonResponse.get("data").getAsJsonObject().get("createLink").getAsJsonObject().get("trace").getAsJsonObject();

      return this.makeTraceState(trace);
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
   private <TLinkData> TraceLink<TLinkData> getHeadLink(ParentLink<TLinkData> input) throws TraceSdkException  
   {
      TraceLink<TLinkData> headLink = input.getPrevLink();
      // if prevLink was not provided  
      if(headLink == null && input.getTraceId() != null)
      { 
         // execute graphql query
         JsonObject jsonResponse = this.client.graphql(GraphQl.Query.QUERY_GETHEADLINK, Collections.singletonMap("traceId", input.getTraceId()), null, JsonObject.class);
         JsonObject trace = jsonResponse.get("data").getAsJsonObject().get("trace").getAsJsonObject();

         String raw = trace.get("head").getAsJsonObject().get("raw").toString();
         TLinkData data = (TLinkData) trace.get("head").getAsJsonObject().get("data");

         // convert the raw response to a link object
         headLink = new TraceLink<TLinkData>(Link.fromObject(raw), data);
 
      }
      if (headLink!=null)
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
   private <TLinkData> TracesState<TState, TLinkData> getTracesInStage(TraceStageType stageType, PaginationInfo paginationInfo, String formId)
      throws Error, Exception
   {

      // formId can only be set in ATTESTATION case
      if(stageType == TraceStageType.ATTESTATION && formId == null)
      {
         throw new Exception("You must and can only provide formId when stageType is ATTESTATION");
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
      JsonObject jsonResponse = this.client.graphql(GraphQl.Query.QUERY_GETTRACESINSTAGE, variables, null, JsonObject.class);

      // extract relevant info from the response
      JsonArray stages = jsonResponse.get("data").getAsJsonObject().get("group").getAsJsonObject().get("stages").getAsJsonObject().get("nodes")
         .getAsJsonArray();

      // there must be exactly one stage
      if(stages.size() == 1)
      {
         JsonObject stage = stages.get(0).getAsJsonObject();

         JsonObject trace = stage.get("traces").getAsJsonObject();
         // extract traces response and pagination 
         JsonObject info = trace.get("info").getAsJsonObject();
         int totalCount = trace.get("totalCount").getAsInt();
         List<TraceState<TState, TLinkData>> traces = new ArrayList<TraceState<TState, TLinkData>>();

         // get all the groups that are owned by one of my accounts
         Iterator<JsonElement> iteratorNodes = trace.get("nodes").getAsJsonArray().iterator();
         while(iteratorNodes.hasNext())
         {
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
      if(formId != null)
      {
         stageDetail += formId;
      }
      // throw if no stages were found if
      if(stages.size() == 0)
      {
         throw new Exception("No " + stageDetail + " stage");
      }
      // throw if multiple stages were found throw new
      throw new Exception("Multiple " + stageDetail + " stages");

   }

   /**
    * Extract, upload and replace all file wrappers in a link data object.
    *
    * @param data the link data that contains file wrappers to upload
    * @throws TraceSdkException 
    * @throws ExecutionException 
    * @throws InterruptedException 
    */
   @SuppressWarnings("unchecked")
   private <TLinkData> TLinkData uploadFilesInLinkData(TLinkData data) throws InterruptedException, ExecutionException, TraceSdkException
   { 
       
      Map<String, FileWrapper> map  = Helpers.extractFileWrappers(data);
     
      uploadFiles(map);
      
      return data;
   }
   
   private  Map<String, FileRecord> uploadFiles ( Map<String, FileWrapper> idToFileWrapperMap) throws InterruptedException, ExecutionException, TraceSdkException
   {
     // if the map is empty return an empty map
      if ( idToFileWrapperMap.size()==0) {
        return new HashMap<String, FileRecord>();
      }
      
      client.uploadFiles(idToFileWrapperMap.values().toArray(new FileWrapper[idToFileWrapperMap.size()]));
      
      return null;
   }
   
   /**
    *  Extract, download and replace all file records in a data object. 
    * @param data the data that contains file records to download 
    * @return
    */
   public <TData> TData downloadFilesInObject(TData data) throws TraceSdkException, HttpError
   {
      Map<String, FileRecord> idToFileRecordMap  = Helpers.extractFileRecords(data);
      
      Map<String, FileWrapper> idFileWrapperMap = this.downloadFiles(idToFileRecordMap);
      //replace filerecords with fileWrappers
      
      TData newData = data; 
      
      return newData;
   }

   /*** 
    * @param idToFileRecordMap
    * @return
    * @throws HttpError 
    * @throws TraceSdkException 
    */
   private Map<String, FileWrapper> downloadFiles(Map<String, FileRecord> idToFileRecordMap) throws TraceSdkException, HttpError
   {
      Map<String, FileWrapper> fileWrapperMap = new HashMap<String, FileWrapper>();
      if(idToFileRecordMap.size() == 0)
      {
         return fileWrapperMap;
      }

      for(Entry<String, FileRecord> fileRecordElement : idToFileRecordMap.entrySet())
      {
         FileRecord fileRecord = fileRecordElement.getValue();
         ByteBuffer file = client.downloadFile(fileRecord);
         FileWrapper fWrapper = FileWrapper.fromFileBlob(file, fileRecord.getFileInfo());
         fileWrapperMap.put(fileRecordElement.getKey(), fWrapper);
      }
      return fileWrapperMap;
   }


   /**
    * Get the details of a given trace.
    *
    * @param input the getTraceDetails input
    * @return the trace details
    * @throws Exception
    */
   @Override
   public <TLinkData> TraceDetails<TLinkData> getTraceDetails(GetTraceDetailsInput input) throws Exception
   {

      Map<String, Object> getTraceDetailsInput =JsonHelper.objectToMap(input);  
      // execute graphql query
       JsonObject jsonResponse =this.client. graphql(GraphQl.Query.QUERY_GETTRACEDETAILS, getTraceDetailsInput, null, JsonObject.class);

      JsonObject trace = jsonResponse.get("data").getAsJsonObject().get("trace").getAsJsonObject();

      JsonObject info = trace.get("links").getAsJsonObject().get("info").getAsJsonObject();
      int totalCount = trace.get("links").getAsJsonObject().get("totalCount").getAsInt();
      List<TraceLink<TLinkData>> links = new ArrayList<TraceLink<TLinkData>>();

      // get all the groups that are owned by one of my accounts
      Iterator<JsonElement> iteratorNodes = trace.get("links").getAsJsonObject().get("nodes").getAsJsonArray().iterator();

      while(iteratorNodes.hasNext())
      {
         JsonObject node = (JsonObject) iteratorNodes.next();

         links.add((TraceLink<TLinkData>) TraceLink.fromObject(node.get("raw").toString(), node.get("data").toString()));
      }

      // construct the link objects from raw responses
      // the details response object
      return new TraceDetails<TLinkData>(links, totalCount, gson.fromJson(info, Info.class));
   }

   @Override
   public <TLinkData> TraceState<TState, TLinkData> getTraceState(GetTraceStateInput input) throws Exception
   {
      // create variables
      JsonObject jsonResponse = this.client.graphql(GraphQl.Query.QUERY_GETTRACESTATE, Collections.singletonMap("traceId", input.getTraceId()), null, JsonObject.class);
      JsonObject trace = jsonResponse.get("data").getAsJsonObject().get("trace").getAsJsonObject();

      return this.makeTraceState(trace);

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
   public <TLinkData> TracesState<TState, TLinkData> getAttestationTraces(String formId, PaginationInfo paginationInfo) throws Exception
   {

      return this.getTracesInStage(TraceStageType.ATTESTATION, paginationInfo, formId);
   }

   @Override
   public <TLinkData> TracesState<TState, TLinkData> getIncomingTraces(PaginationInfo paginationInfo) throws Error, Exception
   {
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
   public <TLinkData> TracesState<TState, TLinkData> getOutgoingTraces(PaginationInfo paginationInfo) throws Exception
   {
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
   public <TLinkData> TracesState<TState, TLinkData> getBacklogTraces(PaginationInfo paginationInfo) throws Exception
   {
      return this.getTracesInStage(TraceStageType.BACKLOG, paginationInfo, null);

   }

   /**
    * Creates a new Trace.
    *
    * @param input  the newTrace input argument
    * @returns the new Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> newTrace(NewTraceInput<TLinkData> input) throws Exception
   {

      //extract info from input
      String formId = input.getFormId();
      TLinkData data = input.getData();
     
      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String ownerId = sdkConfig.getOwnerId();
      String groupId = sdkConfig.getGroupId();
      Map<String, String> actionNames = sdkConfig.getActionNames();
      // upload files and transform data
      TLinkData dataAfterFileUpload = this.uploadFilesInLinkData(data);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      cfg.setWorkflowId(workflowId);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is an attestation
      linkBuilder.forAttestation(formId, actionNames.get(formId), dataAfterFileUpload)
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
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> acceptTransfer(TransferResponseInput<TLinkData> input) throws Exception
   {

      // retrieve parent link 
      TraceLink<TLinkData> parentLink = this.getHeadLink(input);

      //extract info from input
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
      linkBuilder.forAcceptTransfer(  data)
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
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> rejectTransfer(TransferResponseInput<TLinkData> input) throws Exception
   {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null, null);
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
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> cancelTransfer(TransferResponseInput<TLinkData> input) throws Exception
   {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null, null);
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
    * @param input  the input argument
    * @throws Exception 
    * @throws IllegalArgumentException 
    * @returns the Trace
    */
   public <TLinkData> TraceState<TState, TLinkData>  addTagsToTrace (AddTagsToTraceInput input) throws IllegalArgumentException, Exception {
       String traceId = input.getTraceId();
       String[] tags = input.getTags();

     
     // execute the graphql mutation
//     const rsp = await this.client.graphql<Response, Variables>(
//       // the graphql document
//       AddTagsToTraceMutation.document,
//       // export the link as object
//       { traceId, tags }
//     );

     // build and return the TraceState object
     return this.makeTraceState (null);//rsp.addTagsToTrace.trace);
   }

   /**
    * Appends a new Link to a Trace.
    *
    * @param input  the appendLink input argument
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> appendLink(AppendLinkInput<TLinkData> input) throws Exception
   {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null, null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      //extract info from input
      String formId = input.getFormId();
      TLinkData data = input.getData();

      SdkConfig sdkConfig = this.getConfig();

      String workflowId = sdkConfig.getWorkflowId();
      String userId = sdkConfig.getUserId();
      String ownerId = sdkConfig.getOwnerId();
      String groupId = sdkConfig.getGroupId();
      Map<String, String> actionNames = sdkConfig.getActionNames();
      // upload files and transform data
      TLinkData dataAfterFileUpload = this.uploadFilesInLinkData(data);

      TraceLinkBuilderConfig<TLinkData> cfg = new TraceLinkBuilderConfig<TLinkData>();
      // provide workflow id
      cfg.setWorkflowId(workflowId);
      // and parent link to append to the existing trace
      cfg.setParentLink(parentLink);
      // use a TraceLinkBuilder to create the first link
      // only provide workflowId to initiate a new trace
      TraceLinkBuilder<TLinkData> linkBuilder = new TraceLinkBuilder<TLinkData>(cfg);

      // this is an attestation
      linkBuilder.forAttestation(formId, actionNames.get(formId), dataAfterFileUpload)
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
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> pushTrace(PushTransferInput<TLinkData> input) throws Exception
   {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null, null);
      TraceLink<TLinkData> parentLink = this.getHeadLink(headLinkInput);

      //extract info from input
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
    * @returns the Trace
    */
   @Override
   public <TLinkData> TraceState<TState, TLinkData> pullTrace(PullTransferInput<TLinkData> input) throws Exception
   {

      // retrieve parent link
      TransferResponseInput<TLinkData> headLinkInput = new TransferResponseInput<TLinkData>(input.getTraceId(), null, null);
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
   public void searchTraces(SearchTracesFilter filter, PaginationInfo paginationInfo)
   {
      throw new NotImplementedException();
   }

}
