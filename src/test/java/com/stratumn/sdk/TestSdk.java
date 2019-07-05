package com.stratumn.sdk;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.stratumn.sdk.model.trace.*;

public class TestSdk {

  @Test
  public void testNewTrace() {
    SdkOptions opts = new SdkOptions("123");
    Sdk<Object> s = new Sdk<Object>(opts);

    String formId = "42";
    Object data = new Object();
    NewTraceInput<Object> input = new NewTraceInput<Object>(formId, data);
    assertThrows(UnsupportedOperationException.class, () -> {
      s.newTrace(input);
    });
  }
}
