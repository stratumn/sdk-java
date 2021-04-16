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

import static org.junit.Assert.assertEquals;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.misc.Identifiable;
import com.stratumn.sdk.model.misc.Property;
import com.stratumn.sdk.model.sdk.SdkOptions;
import com.stratumn.sdk.model.trace.AddTagsToTraceInput;
import com.stratumn.sdk.model.trace.AppendLinkInput;
import com.stratumn.sdk.model.trace.GetTraceDetailsInput;
import com.stratumn.sdk.model.trace.GetTraceStateInput;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.SearchTracesFilter;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;

public class TestSdk {

   private static Gson gson = JsonHelper.getGson();
   private static final String ACCOUNT_RELEASE_URL = "https://account-api.staging.stratumn.com";
   private static final String TRACE_RELEASE_URL = "https://trace-api.staging.stratumn.com";
   private static final String MEDIA_RELEASE_URL = "https://media-api.staging.stratumn.com";

   private static String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRAjgtjpc1iOR4zYm+21McRGoWr0WM1NBkm26uZmFAx\n853QZ8CRL/HWGCPpEt18JrHZr9ZwA9UyoEosPR8gPakZFQ==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String WORFKLOW_ID = "591";
   private static String ACTION_KEY = "action1";
   private static String COMMENT_ACTION_KEY = "3HflvBg1mU";
   private static String MY_GROUP = "1744";
   private static String MY_GROUP_LABEL = "group1";

   private static String PEM_PRIVATEKEY_2 = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRArbo87/1Yd/nOqFwmmcuxm01T9/pqkeARQxK9y4iG\nF3Xe1W+/2UOr/rYuQPFHQC4a/F0r6nVJGgCI1Ghc/luHZw==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String OTHER_GROUP = "1785";
   private static String OTHER_GROUP_LABEL = "stp";

   private static Sdk<Object> sdk;
   private static Sdk<Object> otherSdk;

   public static Sdk<Object> getSdk() {

      if (sdk == null) {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints(ACCOUNT_RELEASE_URL, TRACE_RELEASE_URL, MEDIA_RELEASE_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(MY_GROUP_LABEL);
         sdk = new Sdk<Object>(opts);

      }
      return sdk;
   }

   public static Sdk<Object> getOtherGroupSdk() {

      if (otherSdk == null) {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY_2);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints(ACCOUNT_RELEASE_URL, TRACE_RELEASE_URL, MEDIA_RELEASE_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(OTHER_GROUP_LABEL);
         otherSdk = new Sdk<Object>(opts);

      }
      return otherSdk;
   }

   @Test
   public void getTraceDetailsTest() {
      try {
         Sdk<Object> sdk = getSdk();
         String traceId = "191516ec-5f8c-4757-9061-8c7ab06cf0a0";
         GetTraceDetailsInput input = new GetTraceDetailsInput(traceId, 5, null, null, null);

         TraceDetails<Object> details = sdk.getTraceDetails(input);

         assertTrue(details.getTotalCount() > 0);
         assertFalse(gson.toJson(details).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }

   }

   @Test
   public void getTraceStateTest() {
      try {
         Sdk<Object> sdk = getSdk();
         String traceId = "191516ec-5f8c-4757-9061-8c7ab06cf0a0";
         GetTraceStateInput input = new GetTraceStateInput(traceId);
         TraceState<Object, Object> state = sdk.getTraceState(input);
         System.out.println("testTraceState" + gson.toJson(state));
         assertTrue(state.getTraceId().equals(traceId));
         assertTrue(state.getTraceId().equals(traceId));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getIncomingTracesTest() {
      try {
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getIncomingTraces(paginationInfo);
         // System.out.println("testIncomingTraces " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getOutoingTracesTest() {
      try {
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getOutgoingTraces(paginationInfo);
         // System.out.println("testOutoingTraces " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getAttestationTracesTest() {
      try {
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getAttestationTraces(ACTION_KEY, paginationInfo);
         // System.out.println("testBacklog " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getBacklogTracesTest() {
      try {
         Sdk<Object> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> state = sdk.getBacklogTraces(paginationInfo);
         // System.out.println("testBacklog " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   // used to pass the trace from one test method to another
   private TraceState<Object, Object> someTraceState;

   public void newTraceTest() {
      try {
         Sdk<Object> sdk = getSdk();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("weight", "123");
         data.put("valid", true);
         data.put("operators", new String[] { "1", "2" });
         data.put("operation", "my new operation 1");
         NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(ACTION_KEY, data);
         TraceState<Object, Object> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         someTraceState = state;
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void newTraceUploadTest() {
      try {
         Sdk<Object> sdk = getSdk();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("weight", "123");
         data.put("valid", true);
         data.put("operators", new String[] { "1", "2" });
         data.put("operation", "my new operation 1");
         // data.put("Certificate" ,
         // FileWrapper.fromFilePath(Paths.get("src/test/resources/stratumn.png")));
         data.put("Certificate2", FileWrapper.fromFilePath(Paths.get("src/test/resources/rapport.pdf")));
         data.put("Certificates",
               new Identifiable[] { FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFileX.txt")) });

         NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(ACTION_KEY, data);

         TraceState<Object, Object> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         someTraceState = state;
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void appendLinkTest() {
      try {
         newTraceTest();
         assertNotNull(someTraceState);
         Map<String, Object> data;
         String json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", "
               + "    customsCheck: true, " + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
         data = JsonHelper.objectToMap(json);
         AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(ACTION_KEY, data,
               someTraceState.getTraceId());
         TraceState<Object, Object> state = getSdk().appendLink(appLinkInput);
         assertNotNull(state.getTraceId());
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
   }

   @Test
   public void pushTraceTest() {
      try {
         newTraceTest();
         Sdk<Object> sdk = getSdk();
         sdk.withGroupLabel(MY_GROUP);
         assertNotNull(someTraceState);
         PushTransferInput<Object> push = new PushTransferInput<Object>(OTHER_GROUP, new Object(),
               someTraceState.getTraceId());

         someTraceState = sdk.pushTrace(push);
         // System.out.println("test pushTrace " + gson.toJson(someTraceState));
         assertNotNull(push.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void acceptTransferTest() {
      try {
         pushTraceTest();
         TransferResponseInput<Object> trInput = new TransferResponseInput<Object>(null, someTraceState.getTraceId());
         Sdk<Object> sdk = getOtherGroupSdk();
         sdk.withGroupLabel(MY_GROUP);
         TraceState<Object, Object> stateAccept = sdk.acceptTransfer(trInput);
         // System.out.println("Accept Transfer:" + "\r\n" + stateAccept);
         assertNotNull(stateAccept.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void rejectTransferTest() {
      try {
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<Object, Object> tracesIn = getSdk().getIncomingTraces(paginationInfo);

         String traceId = null;
         if (tracesIn.getTotalCount() == 0) {
            pushTraceTest();
            traceId = someTraceState.getTraceId();
         } else {
            someTraceState = tracesIn.getTraces().get(0);
            traceId = someTraceState.getTraceId();
         }
         TransferResponseInput<Object> trInput = new TransferResponseInput<Object>(null, traceId);
         TraceState<Object, Object> stateReject = getOtherGroupSdk().rejectTransfer(trInput);
         // System.out.println("Reject Transfer:" + "\r\n" + stateReject);
         assertNotNull(stateReject.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void cancelTransferTest() {
      try {
         pushTraceTest();
         TransferResponseInput<Object> responseInput = new TransferResponseInput<Object>(null,
               someTraceState.getTraceId());
         TraceState<Object, Object> statecancel = sdk.cancelTransfer(responseInput);
         // System.out.println("cancelTransfer:" + "\r\n" + statecancel);
         assertNotNull(statecancel.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void downloadFilesInObjectTest() {
      try {
         TraceState<Object, Object> state;
         try {
            state = getSdk().getTraceState(new GetTraceStateInput("9e8b9094-08aa-447d-87b9-a764db26b646"));
         } catch (Exception e) { // trace not found
            newTraceUploadTest();
            state = someTraceState;
         }

         Object dataWithRecords = state.getHeadLink().formData();
         JsonObject dataWithFiles = (JsonObject) getSdk().downloadFilesInObject(dataWithRecords);
         Map<String, Property<FileWrapper>> fileWrappers = Helpers.extractFileWrappers(dataWithFiles);

         for (Property<FileWrapper> fileWrapperProp : fileWrappers.values()) {
            writeFileToDisk(fileWrapperProp.getValue());
            // assert files are equal
         }
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void traceTagsRWTest() {
      try {
         String traceId = "191516ec-5f8c-4757-9061-8c7ab06cf0a0";

         // Add a tag to a trace
         UUID uuid = UUID.randomUUID();
         String randomUUIDString = uuid.toString();
         AddTagsToTraceInput input = new AddTagsToTraceInput();
         input.setTraceId(traceId);
         input.setTags(new String[] { randomUUIDString });

         TraceState<Object, Object> t = getSdk().addTagsToTrace(input);
         assertEquals(traceId, t.getTraceId());

         // search the trace by tags
         List<String> tags = new ArrayList<String>();
         tags.add(randomUUIDString);
         SearchTracesFilter f = new SearchTracesFilter(tags);
         TracesState<Object, Object> res = sdk.searchTraces(f, new PaginationInfo());

         assertEquals(1, res.getTotalCount());
         assertEquals(traceId, res.getTraces().get(0).getTraceId());

      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void searchByMultipletags() {
      try {
         // search the trace by tags
         List<String> tags = new ArrayList<String>();
         tags.add("tag1");
         tags.add("tag2");
         SearchTracesFilter f = new SearchTracesFilter();
         f.setTags(tags);
         f.setSearchType(SearchTracesFilter.SEARCH_TYPE.TAGS_CONTAINS);
         TracesState<Object, Object> res = getSdk().searchTraces(f, new PaginationInfo());
         assertEquals(1, res.getTotalCount());
         assertEquals("5bf6d482-cfdc-4edc-a5ef-c96539da94d8", res.getTraces().get(0).getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   private void writeFileToDisk(FileWrapper fWrapper) throws TraceSdkException {

      ByteBuffer buffer = fWrapper.decryptedData();

      File file = Paths.get("src/test/resources/out/" + fWrapper.info().getName()).toFile();
      if (!file.getParentFile().exists())
         file.getParentFile().mkdirs();
      if (!file.exists())
         try {
            file.createNewFile();
         } catch (IOException e1) {
            throw new TraceSdkException("Failed to create output file");
         }
      try (FileOutputStream outputStream = new FileOutputStream(file, false);
            FileChannel channel = outputStream.getChannel()) {

         // Writes a sequence of bytes to this channel from the given buffer.
         channel.write(buffer);
      } catch (Exception e) {
         throw new TraceSdkException(e);
      }
   }

   @Test
   public void changeGroupTest() {
      newTraceTest();
      assertNotNull(someTraceState);
      assertEquals(someTraceState.getUpdatedByGroupId(), MY_GROUP);
      try {
         Sdk<Object> sdk = getSdk();
         // Appendlink
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("comment", "commment");
         // comment action
         AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(COMMENT_ACTION_KEY, dataMap,
               someTraceState.getTraceId());
         // change group for action
         appLinkInput.setGroupLabel(OTHER_GROUP_LABEL);

         TraceState<Object, Object> state = sdk.appendLink(appLinkInput);
         assertEquals(state.getUpdatedByGroupId(), OTHER_GROUP);

         AppendLinkInput<Object> appLinkInputWithGroupLabel = new AppendLinkInput<Object>(COMMENT_ACTION_KEY, dataMap,
               someTraceState.getTraceId());
         appLinkInputWithGroupLabel.setGroupLabel(MY_GROUP_LABEL);

         state = sdk.appendLink(appLinkInputWithGroupLabel);
         // should equal group2 id
         assertEquals(state.getUpdatedByGroupId(), MY_GROUP);
      } catch (TraceSdkException e) {
         e.printStackTrace();
         fail(e.getMessage());
      }

   }

   public static void main(String[] args) throws Exception {
      Sdk<Object> sdk = getSdk();

      Map<String, Object> data;
      String json = "{  operation:\"new shipment XYZ for ABC\"," + "    weight: 123," + "    valid: true,"
            + "    operators: [\"Ludovic K.\", \"Bernard Q.\"]" + "  }";
      data = JsonHelper.objectToMap(json);
      NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(ACTION_KEY, data);
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
      TracesState<Object, Object> tracesAtt = sdk.getAttestationTraces(ACTION_KEY, paginationInfo);
      // System.out.println("getAttestationTraces:" + "\r\n" + tracesAtt);

      // append link
      json = "{ operation: \"XYZ shipment departed port for ABC\"," + "    destination: \"ABC\", "
            + "    customsCheck: true, " + "    eta: \"2019-07-02T12:00:00.000Z\"" + "  }";
      data = JsonHelper.objectToMap(json);
      AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(ACTION_KEY, data, newState.getTraceId());
      TraceState<Object, Object> state = sdk.appendLink(appLinkInput);
      // System.out.println("appendLink:" + "\r\n" + state);

      // push to cancel
      PushTransferInput<Object> push = new PushTransferInput<Object>(OTHER_GROUP, new Object(), state.getTraceId());
      TraceState<Object, Object> statepsh = sdk.pushTrace(push);

      // System.out.println("pushTrace:" + "\r\n" + statepsh);

      TransferResponseInput<Object> responseInput = new TransferResponseInput<Object>(null, statepsh.getTraceId());
      TraceState<Object, Object> statecancel = sdk.cancelTransfer(responseInput);
      // System.out.println("cancelTransfer:" + "\r\n" + statecancel);

      // push to accept
      push = new PushTransferInput<Object>(OTHER_GROUP, new Object(), state.getTraceId());
      statepsh = sdk.pushTrace(push);

      responseInput = new TransferResponseInput<Object>(null, statepsh.getTraceId());
      TraceState<Object, Object> stateAccept = sdk.acceptTransfer(responseInput);
      // System.out.println("acceptTransfer:" + "\r\n" + stateAccept);

      // push to reject then pull
      push = new PushTransferInput<Object>(OTHER_GROUP, new Object(), state.getTraceId());
      statepsh = sdk.pushTrace(push);

      data = new HashMap<String, Object>(Collections.singletonMap("why", "No way!"));
      responseInput = new TransferResponseInput<Object>(null, statepsh.getTraceId());
      TraceState<Object, Object> stateReject = sdk.rejectTransfer(responseInput);
      // System.out.println("acceptTransfer:" + "\r\n" + stateReject);

   }

}