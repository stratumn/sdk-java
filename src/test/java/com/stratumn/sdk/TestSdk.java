package com.stratumn.sdk;

import org.junit.jupiter.api.Test;

import com.stratumn.sdk.model.trace.*;
import com.stratumn.sdk.model.client.*;

import com.google.gson.*;

class DefaultSerializer implements ISerializer<Object> {

  private Gson gson;

  public DefaultSerializer() {
    this.gson = new Gson();
  }

  public Object deserialize(String json) {
    return this.gson.fromJson(json, Object.class);
  }
}

public class TestSdk {

  @Test
  public void testNewTrace() {
    Secret s = Secret.newPrivateKeySecret("----- PRIVATE KEY ------");
    SdkOptions opts = new SdkOptions("123", s);
    Sdk<Object> sdk = new Sdk<Object>(opts);
    sdk.setSerializer(new DefaultSerializer());

    String formId = "42";
    Object data = new Object();
    NewTraceInput<Object> input = new NewTraceInput<Object>(formId, data);
    sdk.newTrace(input);
  }
}
