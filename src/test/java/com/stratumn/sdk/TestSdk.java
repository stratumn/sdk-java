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
import java.util.Collections;
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

class DefaultSerializer implements ISerializer<Object>
{

   private Gson gson;

   public DefaultSerializer()
   {
      this.gson = new Gson();
   }

   public Object deserialize(String json)
   {
      return this.gson.fromJson(json, Object.class);
   }
}

public class TestSdk
{

   private static Gson gson = JsonHelper.getGson();
   private static final String ACCOUNT_RELEASE_URL = "https://account-api.staging.stratumn.com";
   private static final String TRACE_RELEASE_URL = "https://trace-api.staging.stratumn.com";
   private static final String MEDIA_RELEASE_URL = "https://media-api.staging.stratumn.com";

   private static String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRACaNT4cup/ZQAq4IULZCrlPB7eR1QTCN9V3Qzct8S\nYp57BqN4FipIrGpyclvbT1FKQfYLJpeBXeCi2OrrQMTgiw==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String WORFKLOW_ID = "591";
   private static String FORM_ID =  "8209";
   private static String MY_GROUP = "1744";

   private static Sdk<Object> sdk;

   public static Sdk<Object> getSdk()
   {

      if(sdk == null)
      {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints(ACCOUNT_RELEASE_URL, TRACE_RELEASE_URL, MEDIA_RELEASE_URL));
         opts.setEnableDebuging(true);
         sdk = new Sdk<Object>(opts);
         
      }
      return sdk;
   }

   @Test
   public void getTraceDetailsTest()
   {
      try
      {
         Sdk<Object> sdk = getSdk();
         String traceId = "a41257f9-2d9d-4d42-ab2a-fd0c83ea31df";
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
         Sdk<Object> sdk = getSdk();
         String traceId = "a41257f9-2d9d-4d42-ab2a-fd0c83ea31df";
         GetTraceStateInput input = new GetTraceStateInput(traceId);
         TraceState<Object, Object> state = sdk.getTraceState(input);
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
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getIncomingTraces(paginationInfo);
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
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getOutgoingTraces(paginationInfo);
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
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getAttestationTraces(FORM_ID, paginationInfo);
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
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getBacklogTraces(paginationInfo);
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
   private TraceState<Object, Object> someTraceState;

   @Test
   public void newTraceTest()
   {
      try
      {
         Sdk<Object> sdk = getSdk();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("weight", "123");
         data.put("valid", true);
         data.put("operators", new String[]{"1", "2" });
         data.put("operation", "my new operation 1");

         NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(FORM_ID, data);

         TraceState<Object, Object> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
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
         Sdk<Object> sdk = getSdk();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("weight", "123");
         data.put("valid", true);
         data.put("operators", new String[]{"1", "2" });
         data.put("operation", "my new operation 1");
//         data.put("Certificate" , FileWrapper.fromFilePath(Paths.get("src/test/resources/stratumn.png")));
         data.put("Certificate2" , FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFile1.txt")));
          data.put("Certificates",new Identifiable[] {
                         FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFile1.txt")) 
         } ); 
 
         NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(FORM_ID, data);

         TraceState<Object, Object> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         someTraceState = state;
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
         Map<String, Object> data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
            + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToMap(json);
         AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(FORM_ID, data, someTraceState.getTraceId());
         TraceState<Object, Object> state = getSdk().appendLink(appLinkInput);
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
         Map<String, Object> data = Collections.singletonMap("why", "because im testing the pushTrace 2");
         PushTransferInput<Object> push = new PushTransferInput<Object>(someTraceState.getTraceId(), "86", data, null);
         someTraceState = getSdk().pushTrace(push);
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
         Map<String, Object> data = Collections.singletonMap("why", "because im testing the pushTrace 2");
         PushTransferInput<Object> push = new PushTransferInput<Object>(someTraceState.getTraceId(), MY_GROUP, data, null);
         someTraceState = getSdk().pushTrace(push);
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

         Map<String, Object> data = Collections.singletonMap("why", "because im testing the pushTrace 2");
         PullTransferInput<Object> pull = new PullTransferInput<Object>(someTraceState.getTraceId(), data, null);
         TraceState<Object, Object> statepul = getSdk().pullTrace(pull);
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
         pushTraceToMyGroupTest();
         TransferResponseInput<Object> trInput = new TransferResponseInput<Object>(someTraceState.getTraceId(), null, null);
         TraceState<Object, Object> stateAccept = getSdk().acceptTransfer(trInput);
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
         TracesState<Object, Object> tracesIn = getSdk().getIncomingTraces(paginationInfo);
         
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
         TransferResponseInput<Object> trInput = new TransferResponseInput<Object>(traceId, null, null);
         TraceState<Object, Object> stateReject = getSdk().rejectTransfer(trInput);
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
         TransferResponseInput<Object> responseInput = new TransferResponseInput<Object>(someTraceState.getTraceId(), null, null);
         TraceState<Object, Object> statecancel = sdk.cancelTransfer(responseInput);
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
          TraceState<Object, Object> state;
         try
         {
            state = getSdk().getTraceState(new GetTraceStateInput("dee0dd04-5d58-4c4e-a72d-a759e37ae337"));
         }
         catch(Exception e)
         {  //trace not found
            newTraceUploadTest();
            state = someTraceState;
         }

         Object dataWithRecords = state.getHeadLink().formData();
         JsonObject dataWithFiles =(JsonObject) getSdk().downloadFilesInObject(dataWithRecords);
         Map<String, Property<FileWrapper>> fileWrappers = Helpers.extractFileWrappers(dataWithFiles);
         
         for ( Property<FileWrapper> fileWrapperProp: fileWrappers.values())
         {  writeFileToDisk(fileWrapperProp.getValue());
            //assert files are equal
         }
      }
      catch(Exception ex)
      { 
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
   
   
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

   public static void main(String[] args) throws Exception
   {
      Sdk<Object> sdk = getSdk();

      Map<String, Object> data;
      String json = "{  operation:\"new shipment XYZ for ABC\"," + "    weight: 123," + "    valid: true,"
         + "    operators: [\"Ludovic K.\", \"Bernard Q.\"]" + "  }";
      data = JsonHelper.objectToMap(json);
      NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(FORM_ID, data);
      TraceState<Object, Object> newState = sdk.newTrace(newTraceInput);
      // System.out.println("newTrace:" + "\r\n" + newState);

      PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
      TracesState<Object, Object> tracesIn = sdk.getIncomingTraces(paginationInfo);
      // System.out.println("getIncomingTraces:" + "\r\n" + tracesIn);

      paginationInfo = new PaginationInfo(10, null, null, null);
      TracesState<Object, Object> tracesOut = sdk.getOutgoingTraces(paginationInfo);
      // System.out.println("getOutgoingTraces:" + "\r\n" + tracesOut);

      paginationInfo = new PaginationInfo(10, null, null, null);
      TracesState<Object, Object> tracesBlog = sdk.getBacklogTraces(paginationInfo);
      // System.out.println("getBacklogTraces:" + "\r\n" + tracesBlog);

      paginationInfo = new PaginationInfo(10, null, null, null);
      TracesState<Object, Object> tracesAtt = sdk.getAttestationTraces(FORM_ID, paginationInfo);
      // System.out.println("getAttestationTraces:" + "\r\n" + tracesAtt);

      //append link
      json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", " + "    customsCheck: true, "
         + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
      data = JsonHelper.objectToMap(json);
      AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(FORM_ID, data, newState.getTraceId());
      TraceState<Object, Object> state = sdk.appendLink(appLinkInput);
      // System.out.println("appendLink:" + "\r\n" + state);

      //push to cancel
      data = Collections.singletonMap("why", "because im testing the pushTrace 2");
      PushTransferInput<Object> push = new PushTransferInput<Object>(state.getTraceId(), "86", data, null);
      TraceState<Object, Object> statepsh = sdk.pushTrace(push);
      // System.out.println("pushTrace:" + "\r\n" + statepsh);

      TransferResponseInput<Object> responseInput = new TransferResponseInput<Object>(statepsh.getTraceId(), null, null);
      TraceState<Object, Object> statecancel = sdk.cancelTransfer(responseInput);
      // System.out.println("cancelTransfer:" + "\r\n" + statecancel);

      //push to accept
      data = Collections.singletonMap("why", "because im testing the pushTrace 2");
      push = new PushTransferInput<Object>(state.getTraceId(), MY_GROUP, data, null);
      statepsh = sdk.pushTrace(push);

      responseInput = new TransferResponseInput<Object>(statepsh.getTraceId(), null, null);
      TraceState<Object, Object> stateAccept = sdk.acceptTransfer(responseInput);
      // System.out.println("acceptTransfer:" + "\r\n" + stateAccept);

      //push to reject then pull 
      data = Collections.singletonMap("why", "because im testing the pushTrace 2");
      push = new PushTransferInput<Object>(state.getTraceId(), MY_GROUP, data, null);
      statepsh = sdk.pushTrace(push);

      data = Collections.singletonMap("why", "No way!");
      responseInput = new TransferResponseInput<Object>(statepsh.getTraceId(), null, null);
      TraceState<Object, Object> stateReject = sdk.rejectTransfer(responseInput);
      // System.out.println("acceptTransfer:" + "\r\n" + stateReject);

      data = Collections.singletonMap("why", "because im testing the pushTrace 2");
      PullTransferInput<Object> pull = new PullTransferInput<Object>(statepsh.getTraceId(), data, null);
      TraceState<Object, Object> statepul = getSdk().pullTrace(pull);
      // System.out.println("pullTrace:" + "\r\n" + statepul);

   }

}
