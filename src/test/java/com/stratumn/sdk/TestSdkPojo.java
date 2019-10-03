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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.misc.Identifiable;
import com.stratumn.sdk.model.misc.Property;
import com.stratumn.sdk.model.sdk.SdkOptions;
import com.stratumn.sdk.model.trace.AppendLinkInput;
import com.stratumn.sdk.model.trace.GetTraceDetailsInput;
import com.stratumn.sdk.model.trace.GetTraceStateInput;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.PullTransferInput;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;

//used the same structure so its compatable with existing links
class SomeClass {
   public int weight ; 
   public boolean valid ;
   public String[] operators;
   public String operation;
   public Identifiable certificate ;
   public Identifiable certificate2;
   public Identifiable[] certificates; 
}

class StateExample   {
   public String f1;
   public SomeClass data ;
   
}

class ReasonClass{
   public String reason; 
}

class OperationClass{
   public String operation;
   public String destination;
   public String eta;
}


  class Step
{

    public StepData data;
}

  class StepData
{
    public Identifiable[] stp_form_section;
}



public class TestSdkPojo
{

   private static Gson gson = JsonHelper.getGson();
   private static final String ACCOUNT_STAGING_URL = "https://account-api.staging.stratumn.com";
   private static final String TRACE_STAGING_URL = "https://trace-api.staging.stratumn.com";
   private static final String MEDIA_STAGING_URL = "https://media-api.staging.stratumn.com";

   private static String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRACaNT4cup/ZQAq4IULZCrlPB7eR1QTCN9V3Qzct8S\nYp57BqN4FipIrGpyclvbT1FKQfYLJpeBXeCi2OrrQMTgiw==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String WORFKLOW_ID = "591";
   private static String FORM_ID =  "8209";
   private static String MY_GROUP = "1744";

   private static Sdk<StateExample> sdk;

   public static Sdk<StateExample> getSdk()
   {

      if(sdk == null)
      {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints("https://account-api.staging.stratumn.com", "https://trace-api.staging.stratumn.com", "https://media-api.staging.stratumn.com"));
         opts.setEnableDebuging(true);
         sdk = new Sdk<StateExample>(opts, StateExample.class);
         
      }
      return sdk;
   }
   
   @Test
   public void getTraceDetailsTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         String traceId = "18014bb9-1c3c-402f-91f8-5cab2c23b292";
         GetTraceDetailsInput input = new GetTraceDetailsInput(traceId, 5, null, null, null);

         TraceDetails<Object> details = sdk.getTraceDetails(input);
         
         assertTrue(details.getTotalCount() > 0);
         assertFalse(gson.toJson(details).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }

   }

   @Test
   public void getTraceStateTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         String traceId = "191516ec-5f8c-4757-9061-8c7ab06cf0a0";
         GetTraceStateInput input = new GetTraceStateInput(traceId);
         TraceState<StateExample, SomeClass> state = sdk.getTraceState(input);
         //      // System.out.println("testTraceState" + gson.toJson(state));
         assertTrue(state.getTraceId().equals(traceId));
         assertTrue(state.getTraceId().equals(traceId));
         assertFalse(gson.toJson(state).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getIncomingTracesTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getIncomingTraces(paginationInfo);
         // System.out.println("testIncomingTraces " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getOutoingTracesTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getOutgoingTraces(paginationInfo);
         // System.out.println("testOutoingTraces " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getAttestationTracesTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getAttestationTraces(FORM_ID, paginationInfo);
         // System.out.println("testBacklog " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getBacklogTracesTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getBacklogTraces(paginationInfo);
         // System.out.println("testBacklog " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }



   //used to pass the trace from one test method to another
   private TraceState<StateExample, SomeClass> someTraceState;
   private TraceState<StateExample,OperationClass> anotherTraceState;
    private TraceState<StateExample, Step> uploadState;
    
   @Test
   public void newTraceTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("weight", "123");
         dataMap.put("valid", true);
         dataMap.put("operators", new String[]{"1", "2" });
         dataMap.put("operation", "my new operation 1");
         //quickly convert existing map to object but the object can be created any way
         SomeClass data= JsonHelper.mapToObject(dataMap, SomeClass.class);
         NewTraceInput<SomeClass> newTraceInput = new NewTraceInput<SomeClass>(FORM_ID, data);

         TraceState<StateExample, SomeClass> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         assertTrue(state.getData() instanceof StateExample);
         someTraceState = state;
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
 


   @Test
   public void newTraceUploadTest()
   {
      try
      {
         Sdk<StateExample> sdk = getSdk();
        
         // Step s = new Step();
         StepData s = new StepData();
 
         //This fails on Identifiable deserialzation 
         s.stp_form_section =new Identifiable[] { FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFileX.txt")) };

         NewTraceInput<StepData> newTraceInput = new NewTraceInput<StepData>(FORM_ID, s);
 

         TraceState<StateExample, StepData> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
        
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
 
   @Test
   public void appendLinkTest()
   {
      try
      {
         newTraceTest();
         assertNotNull(someTraceState);
         OperationClass data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
            + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToObject( json, OperationClass.class);
         AppendLinkInput<OperationClass> appLinkInput = new AppendLinkInput<OperationClass>(FORM_ID, data, someTraceState.getTraceId());
         TraceState<StateExample, OperationClass> state = getSdk().appendLink(appLinkInput);
         assertNotNull(state.getTraceId());
      }
      catch(Exception ex)
      { 
         fail(ex.getMessage());
      }
   }

   @Test
   public void pushTraceTest()
   {
      try
      {
         newTraceTest();
         assertNotNull(someTraceState);
         OperationClass data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
            + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToObject( json, OperationClass.class);
         PushTransferInput<OperationClass> push = new PushTransferInput<OperationClass>("86", data, someTraceState.getTraceId());
         anotherTraceState = getSdk().pushTrace(push);
         // System.out.println("test pushTrace " + gson.toJson(someTraceState));
         assertNotNull(push.getTraceId());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void pushTraceToMyGroupTest()
   {
      try
      {
         newTraceTest();
         assertNotNull(someTraceState);
         OperationClass data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
            + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToObject( json, OperationClass.class);
         PushTransferInput<OperationClass> push = new PushTransferInput<OperationClass>(MY_GROUP, data, someTraceState.getTraceId());
         anotherTraceState = getSdk().pushTrace(push);
         // System.out.println("test pushTrace " + gson.toJson(someTraceState));
         assertNotNull(push.getTraceId());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void pullTraceTest()
   {
      try
      {
         rejectTransferTest();
         OperationClass data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
            + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToObject( json, OperationClass.class);
         PullTransferInput<OperationClass> pull = new PullTransferInput<OperationClass>(data, someTraceState.getTraceId());
         TraceState<StateExample, OperationClass> statepul = getSdk().pullTrace(pull);
         // System.out.println("pullTrace:" + "\r\n" + statepul);
         assertNotNull(statepul.getTraceId());
      }
      catch(Exception ex)
      { 
         ex.printStackTrace();
         fail(ex.getMessage());
      }

   }

   @Test
   public void acceptTransferTest()
   {
      try
      {
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> tracesIn = getSdk().getIncomingTraces(paginationInfo);
         
         String traceId = null;
         if (tracesIn.getTotalCount()==0)
         { 
            pushTraceToMyGroupTest();
            traceId = someTraceState.getTraceId(); 
         }
         else {
             someTraceState = tracesIn.getTraces().get(0);
             traceId=someTraceState.getTraceId();
         }
         TransferResponseInput<SomeClass> trInput = new TransferResponseInput<SomeClass>(null,someTraceState.getTraceId());
         TraceState<StateExample, SomeClass> stateAccept = getSdk().acceptTransfer(trInput);
         // System.out.println("Accept Transfer:" + "\r\n" + stateAccept);
         assertNotNull(stateAccept.getTraceId());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void rejectTransferTest()
   {
      try
      {
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> tracesIn = getSdk().getIncomingTraces(paginationInfo);
         
         String traceId = null;
         if (tracesIn.getTotalCount()==0)
         { 
            pushTraceToMyGroupTest();
            traceId = someTraceState.getTraceId(); 
         }
         else {
        	 someTraceState = tracesIn.getTraces().get(0);
        	 traceId=someTraceState.getTraceId();
         }
         TransferResponseInput<SomeClass> trInput = new TransferResponseInput<SomeClass>(null,traceId);
         TraceState<StateExample, SomeClass> stateReject = getSdk().rejectTransfer(trInput);
         // System.out.println("Reject Transfer:" + "\r\n" + stateReject);
         assertNotNull(stateReject.getTraceId());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void cancelTransferTest()
   {
      try
      {
         pushTraceTest();
         TransferResponseInput<SomeClass> responseInput = new TransferResponseInput<SomeClass>(null,someTraceState.getTraceId());
         TraceState<StateExample, SomeClass> statecancel = sdk.cancelTransfer(responseInput);
         // System.out.println("cancelTransfer:" + "\r\n" + statecancel);
         assertNotNull(statecancel.getTraceId());
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void downloadFilesInObjectTest()
   {
       try
      {
          TraceState<StateExample, Step> state;
         try
         {
            state = getSdk().getTraceState(new GetTraceStateInput("9565be68-5a11-4262-9a55-bd3fe6bfa3f0"));
         }
         catch(Exception e)
         {  //trace not found
            newTraceUploadTest();
            state = uploadState;
         }

         Object dataWithRecords = state.getHeadLink().formData();
         JsonObject dataWithFiles =(JsonObject) getSdk().downloadFilesInObject(dataWithRecords);
         Map<String, Property<FileWrapper>> fileWrappers = Helpers.extractFileWrappers(dataWithFiles);
         
         for ( Property<FileWrapper> fileWrapperProp: fileWrappers.values())
         {  
            writeFileToDisk(fileWrapperProp.getValue());
            //assert files are equal
         }
      }
      catch(Exception ex)
      { 
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
   
   /***
    * Writes the files to the output folder
    * @param fWrapper
    * @throws TraceSdkException
    */
   private void writeFileToDisk(FileWrapper fWrapper) throws TraceSdkException
   { 

     ByteBuffer buffer= fWrapper.decryptedData();
     
     File file = Paths.get("src/test/resources/out/" + fWrapper.info().getName()).toFile();
     if (!file.getParentFile().exists())
        file.getParentFile().mkdirs();
      if(!file.exists()) try
      {
         file.createNewFile();
      }
      catch(IOException e1)
      {
         throw new TraceSdkException("Failed to create output file");
      }
     try(FileOutputStream outputStream=new FileOutputStream(file, false);FileChannel channel =outputStream.getChannel())
     {
      
        // Writes a sequence of bytes to this channel from the given buffer.
        channel.write( buffer);
     }
     catch (Exception e)
     {
        throw new TraceSdkException(e);
     }
   }
   
   
 


}
