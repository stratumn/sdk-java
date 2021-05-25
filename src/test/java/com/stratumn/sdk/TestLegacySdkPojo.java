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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.misc.Identifiable;
import com.stratumn.sdk.model.sdk.SdkOptions;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;

public class TestLegacySdkPojo {

   private static Gson gson = JsonHelper.getGson();
   private static final String ACCOUNT_STAGING_URL = "https://account-api.staging.stratumn.com";
   private static final String TRACE_STAGING_URL = "https://trace-api.staging.stratumn.com";
   private static final String MEDIA_STAGING_URL = "https://media-api.staging.stratumn.com";

   private static String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRAjgtjpc1iOR4zYm+21McRGoWr0WM1NBkm26uZmFAx\n853QZ8CRL/HWGCPpEt18JrHZr9ZwA9UyoEosPR8gPakZFQ==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String WORFKLOW_ID = "591";
   private static String ACTION_KEY = "action1";
   private static String MY_GROUP_LABEL = "group1";

   private static String PEM_PRIVATEKEY_2 = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRArbo87/1Yd/nOqFwmmcuxm01T9/pqkeARQxK9y4iG\nF3Xe1W+/2UOr/rYuQPFHQC4a/F0r6nVJGgCI1Ghc/luHZw==\n-----END ED25519 PRIVATE KEY-----\n";
   private static String OTHER_GROUP = "1785";
   private static String OTHER_GROUP_LABEL = "stp";

   private static Sdk<StateExample> sdk;
   private static Sdk<StateExample> otherSdk;

   // used the same structure so its compatable with existing links
   class SomeClass {
      public int weight;
      public boolean valid;
      public String[] operators;
      public String operation;
      public Identifiable certificate;
      public Identifiable certificate2;
      public Identifiable[] certificates;
   }

   class StateExample {
      public String f1;
      public SomeClass data;

   }

   class ReasonClass {
      public String reason;
   }

   class OperationClass {
      public String operation;
      public String destination;
      public String eta;
   }

   class Step {

      public StepData data;
   }

   class StepData {
      public Identifiable[] stp_form_section;
   }

   public static Sdk<StateExample> getSdk() {

      if (sdk == null) {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints(ACCOUNT_STAGING_URL, TRACE_STAGING_URL, MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(MY_GROUP_LABEL);
         sdk = new Sdk<StateExample>(opts, StateExample.class);
      }
      return sdk;
   }

   public static Sdk<StateExample> getOtherGroupSdk() {

      if (otherSdk == null) {
         Secret s = Secret.newPrivateKeySecret(PEM_PRIVATEKEY_2);
         SdkOptions opts = new SdkOptions(WORFKLOW_ID, s);
         opts.setEndpoints(new Endpoints(ACCOUNT_STAGING_URL, TRACE_STAGING_URL, MEDIA_STAGING_URL));
         opts.setEnableDebuging(true);
         opts.setGroupLabel(OTHER_GROUP_LABEL);
         otherSdk = new Sdk<StateExample>(opts);

      }
      return otherSdk;
   }

   @Test
   public void getIncomingTracesTest() {
      try {
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getIncomingTraces(paginationInfo);
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
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getOutgoingTraces(paginationInfo);
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
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getAttestationTraces(ACTION_KEY, paginationInfo);
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
         Sdk<StateExample> sdk = getSdk();
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> state = sdk.getBacklogTraces(paginationInfo);
         // System.out.println("testBacklog " + gson.toJson(state));
         assertFalse(gson.toJson(state).contains("Error"));
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   // used to pass the trace from one test method to another
   private TraceState<StateExample, SomeClass> someTraceState;
   private TraceState<StateExample, Step> uploadState;

   @Test
   public void newTraceTest() {
      try {
         Sdk<StateExample> sdk = getSdk();
         Map<String, Object> dataMap = new HashMap<String, Object>();
         dataMap.put("weight", "123");
         dataMap.put("valid", true);
         dataMap.put("operators", new String[] { "1", "2" });
         dataMap.put("operation", "my new operation 1");
         // quickly convert existing map to object but the object can be created any way
         SomeClass data = JsonHelper.mapToObject(dataMap, SomeClass.class);
         NewTraceInput<SomeClass> newTraceInput = new NewTraceInput<SomeClass>(ACTION_KEY, data);

         TraceState<StateExample, SomeClass> state = sdk.newTrace(newTraceInput);
         assertNotNull(state.getTraceId());
         assertTrue(state.getData() instanceof StateExample);
         someTraceState = state;
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }

   @Test
   public void pushTraceTest() {
      try {
         newTraceTest();
         assertNotNull(someTraceState);
         PushTransferInput<Object> push = new PushTransferInput<Object>(OTHER_GROUP, new Object(),
               someTraceState.getTraceId());

         getSdk().pushTrace(push);
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
         PaginationInfo paginationInfo = new PaginationInfo(10, null, null, null);
         TracesState<StateExample, SomeClass> tracesIn = getSdk().getIncomingTraces(paginationInfo);

         if (tracesIn.getTotalCount() == 0) {
            pushTraceTest();
         } else {
            someTraceState = tracesIn.getTraces().get(0);
         }
         TransferResponseInput<SomeClass> trInput = new TransferResponseInput<SomeClass>(null,
               someTraceState.getTraceId());
         TraceState<StateExample, SomeClass> stateAccept = getOtherGroupSdk().acceptTransfer(trInput);
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
         TracesState<StateExample, SomeClass> tracesIn = getSdk().getIncomingTraces(paginationInfo);

         String traceId = null;
         if (tracesIn.getTotalCount() == 0) {
            pushTraceTest();
            traceId = someTraceState.getTraceId();
         } else {
            someTraceState = tracesIn.getTraces().get(0);
            traceId = someTraceState.getTraceId();
         }
         TransferResponseInput<SomeClass> trInput = new TransferResponseInput<SomeClass>(null, traceId);
         TraceState<StateExample, SomeClass> stateReject = getOtherGroupSdk().rejectTransfer(trInput);
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
         TransferResponseInput<SomeClass> responseInput = new TransferResponseInput<SomeClass>(null,
               someTraceState.getTraceId());
         TraceState<StateExample, SomeClass> statecancel = sdk.cancelTransfer(responseInput);
         // System.out.println("cancelTransfer:" + "\r\n" + statecancel);
         assertNotNull(statecancel.getTraceId());
      } catch (Exception ex) {
         ex.printStackTrace();
         fail(ex.getMessage());
      }
   }
}
