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
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import com.google.gson.Gson;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.sdk.SdkOptions;
import com.stratumn.sdk.model.trace.AppendLinkInput;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;

public class TestLegacySdk {

    private static Gson gson = JsonHelper.getGson();
    private static final String ACCOUNT_RELEASE_URL = "https://account-api.staging.stratumn.com";
    private static final String TRACE_RELEASE_URL = "https://trace-api.staging.stratumn.com";
    private static final String MEDIA_RELEASE_URL = "https://media-api.staging.stratumn.com";

    private static String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRAjgtjpc1iOR4zYm+21McRGoWr0WM1NBkm26uZmFAx\n853QZ8CRL/HWGCPpEt18JrHZr9ZwA9UyoEosPR8gPakZFQ==\n-----END ED25519 PRIVATE KEY-----\n";
    private static String WORFKLOW_ID = "591";
    private static String ACTION_KEY = "action1";
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public void pushTraceTest() {
        try {
            newTraceTest();
            Sdk<Object> sdk = getSdk();
            sdk.withGroupLabel(MY_GROUP_LABEL);
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
    @Deprecated
    public void acceptTransferTest() {
        try {
            pushTraceTest();
            TransferResponseInput<Object> trInput = new TransferResponseInput<Object>(null,
                    someTraceState.getTraceId());
            Sdk<Object> sdk = getOtherGroupSdk();
            sdk.withGroupLabel(MY_GROUP_LABEL);
            TraceState<Object, Object> stateAccept = sdk.acceptTransfer(trInput);
            // System.out.println("Accept Transfer:" + "\r\n" + stateAccept);
            assertNotNull(stateAccept.getTraceId());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    @Deprecated
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
    @Deprecated
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