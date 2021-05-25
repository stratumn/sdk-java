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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.gson.Gson;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.misc.Identifiable;
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

public class TestSdkPojo {

   private static Gson gson = JsonHelper.getGson();
   private static Sdk<StateExample> sdk;
   private static Sdk<StateExample> otherSdk;

   // used the same structure so its compatable with existing links
   class InitDataClass {
      public String entity;
      public String submissionPeriod;
      public String startDate;
      public String deadline;
      public String comment;

   }

   class CommentClass {
      public String comment;
   }

   class UploadDocumentsClass {
      public Identifiable[] documents;
      public String comment;
   }

   class StatusClass {
      public String value;
      public double progress;
   }

   class ImportTaClass {
      public Object taSummary;
      public Object file;
   }

   class StateExample {
      public String entity;
      public String submissionPeriod;
      public String startDate;
      public String deadline;
      public Object taSummary;
      public StatusClass status;
      public Object[] comments;
   }

   public static Sdk<StateExample> getSdk() {

      if (sdk == null) {
         Secret s = Secret.newPrivateKeySecret(ConfigTest.PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(ConfigTest.WORKFLOW_ID, s);
         opts.setEndpoints(new Endpoints(ConfigTest.ACCOUNT_STAGING_URL, ConfigTest.TRACE_STAGING_URL,
               ConfigTest.MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(ConfigTest.MY_GROUP_LABEL);
         sdk = new Sdk<StateExample>(opts, StateExample.class);
      }
      return sdk;
   }

   public static Sdk<StateExample> getOtherGroupSdk() {

      if (otherSdk == null) {
         Secret s = Secret.newPrivateKeySecret(ConfigTest.PEM_PRIVATEKEY_2);
         SdkOptions opts = new SdkOptions(ConfigTest.WORKFLOW_ID, s);
         opts.setEndpoints(new Endpoints(ConfigTest.ACCOUNT_STAGING_URL, ConfigTest.TRACE_STAGING_URL,
               ConfigTest.MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(ConfigTest.OTHER_GROUP_LABEL);
         otherSdk = new Sdk<StateExample>(opts);

      }
      return otherSdk;
   }

   private TraceState<StateExample, InitDataClass> initTraceState;

   @Test
   public void getTraceStateTest() {
      try {
         Sdk<StateExample> sdk = getSdk();
         String traceId = ConfigTest.TRACE_ID;
         GetTraceStateInput input = new GetTraceStateInput(traceId);
         TraceState<StateExample, StateExample> state = sdk.getTraceState(input);
         // // System.out.println("testTraceState" + gson.toJson(state));
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
         Sdk<StateExample> sdk = getSdk();
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

   @Test
   public void newTraceTest() {
      try {
         Sdk<StateExample> sdk = getSdk();
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("entity", ConfigTest.OTHER_GROUP_NAME);
         dataMap.put("submissionPeriod", "2021.Q4");
         dataMap.put("startDate", "2021-01-30");
         dataMap.put("deadline", "2021-06-30");
         dataMap.put("comment", "init comment");
         // quickly convert existing map to object but the object can be created any way
         InitDataClass data = JsonHelper.mapToObject(dataMap, InitDataClass.class);
         NewTraceInput<InitDataClass> newTraceInput = new NewTraceInput<InitDataClass>(ConfigTest.INIT_ACTION_KEY,
               data);

         TraceState<StateExample, InitDataClass> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         assertTrue(state.getData() instanceof StateExample);
         initTraceState = state;
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void appendLinkTest() {
      try {
         newTraceTest();
         assertNotNull(initTraceState);
         CommentClass data = new CommentClass();
         data.comment = "comment";

         AppendLinkInput<CommentClass> appLinkInput = new AppendLinkInput<CommentClass>(ConfigTest.COMMENT_ACTION_KEY,
               data, initTraceState.getTraceId());
         TraceState<StateExample, CommentClass> state = getSdk().appendLink(appLinkInput);
         assertNotNull(state.getTraceId());
      } catch (Exception ex) {
         fail(ex.getMessage());
      }
   }

   @Test
   public void newTraceUploadTest() {
      try {
         newTraceTest();
         UploadDocumentsClass s = new UploadDocumentsClass();
         s.documents = new Identifiable[] { FileWrapper.fromFilePath(Paths.get("src/test/resources/TestFileX.txt")) };
         s.comment = "upload comment";

         AppendLinkInput<UploadDocumentsClass> newTraceInput = new AppendLinkInput<UploadDocumentsClass>(
               ConfigTest.UPLOAD_DOCUMENTS_ACTION_KEY, s, initTraceState.getTraceId());

         TraceState<StateExample, UploadDocumentsClass> state = getOtherGroupSdk().appendLink(newTraceInput);
         assertNotNull(state.getTraceId());

      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void ImportDataCsvTest() {
      try {
         newTraceTest();

         ImportTaClass data = new ImportTaClass();
         String json = "[{ reference: \"reference\", entityName: \"entity\", currency:\"EUR\", amount: 500, endDate: \"2020-06-25\"},"
               + "{reference: \"reference 2\", entityName: \"entity 2\", currency: \"EUR\",amount: 1300, endDate: \"2020-06-28\"}"
               + "]";

         data.taSummary = JsonHelper.fromJson(json, Object.class);
         data.file = FileWrapper.fromFilePath(Paths.get("src/test/resources/TA.csv"));

         AppendLinkInput<ImportTaClass> newTraceInput = new AppendLinkInput<ImportTaClass>(
               ConfigTest.IMPORT_TA_ACTION_KEY, data, initTraceState.getTraceId());

         TraceState<StateExample, ImportTaClass> state = sdk.appendLink(newTraceInput);
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

         TraceState<StateExample, StateExample> t = getSdk().addTagsToTrace(input);
         assertEquals(traceId, t.getTraceId());

         // search the trace by tags
         List<String> tags = new ArrayList<String>();
         tags.add(randomUUIDString);
         tags.add("tag1");
         tags.add("tag2");
         SearchTracesFilter f = new SearchTracesFilter(tags);
         TracesState<StateExample, StateExample> res = sdk.searchTraces(f, new PaginationInfo());

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
         TracesState<StateExample, StateExample> res = getSdk().searchTraces(f, new PaginationInfo());
         assertEquals(1, res.getTotalCount());
         assertEquals(ConfigTest.TRACE_ID, res.getTraces().get(0).getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void changeGroupTest() {
      newTraceTest();
      assertNotNull(initTraceState);
      assertEquals(initTraceState.getUpdatedByGroupId(), ConfigTest.MY_GROUP);
      try {
         Sdk<StateExample> sdk = getSdk();
         // Appendlink
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("comment", "commment");
         AppendLinkInput<Map<String, Object>> appLinkInput = new AppendLinkInput<Map<String, Object>>(
               ConfigTest.COMMENT_ACTION_KEY, dataMap, initTraceState.getTraceId());
         // change group for action
         appLinkInput.setGroupLabel(ConfigTest.OTHER_GROUP_LABEL);

         TraceState<StateExample, Map<String, Object>> state = sdk.appendLink(appLinkInput);
         // should equal group2 id
         assertEquals(state.getUpdatedByGroupId(), ConfigTest.OTHER_GROUP);

         AppendLinkInput<Map<String, Object>> appLinkInputWithGroupLabel = new AppendLinkInput<Map<String, Object>>(
               ConfigTest.COMMENT_ACTION_KEY, dataMap, initTraceState.getTraceId());
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
