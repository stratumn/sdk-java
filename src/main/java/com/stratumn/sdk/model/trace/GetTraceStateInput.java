package com.stratumn.sdk.model.trace;

public class GetTraceStateInput {

  String traceId;

  public GetTraceStateInput(String traceId) throws IllegalArgumentException {
    if (traceId == null) {
      throw new IllegalArgumentException("traceId cannot be null in GetTraceStateInput");
    }
    this.traceId = traceId;
  }
}
