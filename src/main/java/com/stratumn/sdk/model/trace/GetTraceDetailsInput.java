package com.stratumn.sdk.model.trace;

public class GetTraceDetailsInput extends PaginationInfo {

  String traceId;

  public GetTraceDetailsInput(String traceId) throws IllegalArgumentException {
    if (traceId == null) {
      throw new IllegalArgumentException("formId cannot be null in AppendLinkInput");
    }
    this.traceId = traceId;
  }
}
