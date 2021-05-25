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
import com.stratumn.sdk.model.trace.SearchTracesFilter;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;

public class TestSdk {

   public static Gson gson = JsonHelper.getGson();
   private static Sdk<Object> sdk;
   private static Sdk<Object> otherSdk;

   public static Sdk<Object> getSdk() {

      if (sdk == null) {
         Secret s = Secret.newPrivateKeySecret(ConfigTest.PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(ConfigTest.WORKFLOW_ID, s);
         opts.setEndpoints(new Endpoints(ConfigTest.ACCOUNT_STAGING_URL, ConfigTest.TRACE_STAGING_URL,
               ConfigTest.MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(ConfigTest.MY_GROUP_LABEL);
         sdk = new Sdk<Object>(opts);

      }
      return sdk;
   }

   public static Sdk<Object> getOtherGroupSdk() {

      if (otherSdk == null) {
         Secret s = Secret.newPrivateKeySecret(ConfigTest.PEM_PRIVATEKEY_2);
         SdkOptions opts = new SdkOptions(ConfigTest.WORKFLOW_ID, s);
         opts.setEndpoints(new Endpoints(ConfigTest.ACCOUNT_STAGING_URL, ConfigTest.TRACE_STAGING_URL,
               ConfigTest.MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(ConfigTest.OTHER_GROUP_LABEL);
         otherSdk = new Sdk<Object>(opts);

      }
      return otherSdk;
   }

   @Test
   public void getTraceStateTest() {
      try {
         Sdk<Object> sdk = getSdk();
         String traceId = ConfigTest.TRACE_ID;
         GetTraceStateInput input = new GetTraceStateInput(traceId);
         TraceState<Object, Object> state = sdk.getTraceState(input);
         System.out.println("testTraceState" + gson.toJson(state));
         assertTrue(state.getTraceId().equals(traceId));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void getTraceDetailsTest() {
      try {
         Sdk<Object> sdk = getSdk();
         String traceId = ConfigTest.TRACE_ID;
         GetTraceDetailsInput input = new GetTraceDetailsInput(traceId, 5, null, null, null);

         TraceDetails<Object> details = sdk.getTraceDetails(input);

         assertTrue(details.getTotalCount() > 0);
         assertFalse(gson.toJson(details).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   // used to pass the trace from one test method to another
   private TraceState<Object, Object> someTraceState;
   private TraceState<Object, Object> traceStateWithFile;

   @Test
   public void newTraceTest() {
      try {
         Sdk<Object> sdk = getSdk();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("entity", ConfigTest.OTHER_GROUP_NAME);
         data.put("submissionPeriod", "2021.Q4");
         data.put("startDate", "2021-01-30");
         data.put("deadline", "2021-06-30");
         data.put("comment", "init comment");
         NewTraceInput<Object> newTraceInput = new NewTraceInput<Object>(ConfigTest.INIT_ACTION_KEY, data);
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
         String json = "{ comment: \"comment\" }";
         data = JsonHelper.objectToMap(json);
         AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(ConfigTest.COMMENT_ACTION_KEY, data,
               someTraceState.getTraceId());
         TraceState<Object, Object> state = getSdk().appendLink(appLinkInput);
         assertNotNull(state.getTraceId());
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
   }

   @Test
   public void newTraceUploadTest() {
      try {

         newTraceTest();

         Sdk<Object> sdk = getOtherGroupSdk();

         Map<String, Object> data = new HashMap<String, Object>();
         data.put("documents",
               new Identifiable[] { FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFileX.txt")) });

         AppendLinkInput<Object> newTraceInput = new AppendLinkInput<Object>(ConfigTest.UPLOAD_DOCUMENTS_ACTION_KEY,
               data, someTraceState.getTraceId());

         TraceState<Object, Object> state = sdk.appendLink(newTraceInput);
         assertNotNull(state.getTraceId());
         traceStateWithFile = state;
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
            state = getSdk().getTraceState(new GetTraceStateInput(ConfigTest.TRACE_ID));
         } catch (Exception e) { // trace not found
            newTraceUploadTest();
            state = traceStateWithFile;
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
   public void ImportDataCsvTest() {
      try {
         newTraceTest();

         Map<String, Object> data = new HashMap<String, Object>();
         String json = "[{ reference: \"reference\", entityName: \"entity\", currency:\"EUR\", amount: 500, endDate: \"2020-06-25\"},"
               + "{reference: \"reference 2\", entityName: \"entity 2\", currency: \"EUR\",amount: 1300, endDate: \"2020-06-28\"}"
               + "]";

         data.put("taSummary", JsonHelper.fromJson(json, Object.class));
         data.put("file", FileWrapper.fromFilePath(Paths.get("src/test/resources/TA.csv")));

         AppendLinkInput<Object> newTraceInput = new AppendLinkInput<Object>(ConfigTest.IMPORT_TA_ACTION_KEY, data,
               someTraceState.getTraceId());

         TraceState<Object, Object> state = sdk.appendLink(newTraceInput);
         assertNotNull(state.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void traceTagsRWTest() {
      try {
         String traceId = ConfigTest.TRACE_ID;

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
         tags.add("tag1");
         tags.add("tag2");
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
         assertEquals(ConfigTest.TRACE_ID, res.getTraces().get(0).getTraceId());
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
      assertEquals(someTraceState.getUpdatedByGroupId(), ConfigTest.MY_GROUP);
      try {
         Sdk<Object> sdk = getSdk();
         // Appendlink
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("comment", "commment");
         // comment action
         AppendLinkInput<Object> appLinkInput = new AppendLinkInput<Object>(ConfigTest.COMMENT_ACTION_KEY, dataMap,
               someTraceState.getTraceId());
         // change group for action
         appLinkInput.setGroupLabel(ConfigTest.OTHER_GROUP_LABEL);

         TraceState<Object, Object> state = sdk.appendLink(appLinkInput);
         assertEquals(state.getUpdatedByGroupId(), ConfigTest.OTHER_GROUP);

         AppendLinkInput<Object> appLinkInputWithGroupLabel = new AppendLinkInput<Object>(ConfigTest.COMMENT_ACTION_KEY,
               dataMap, someTraceState.getTraceId());
         appLinkInputWithGroupLabel.setGroupLabel(ConfigTest.MY_GROUP_LABEL);

         state = sdk.appendLink(appLinkInputWithGroupLabel);
         // should equal group2 id
         assertEquals(state.getUpdatedByGroupId(), ConfigTest.MY_GROUP);
      } catch (TraceSdkException e) {
         e.printStackTrace();
         fail(e.getMessage());
      }
   }
}