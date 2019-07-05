package com.stratumn.sdk.model.trace;

public class AppendLinkInput<TLinkData> {

  String formId;
  TLinkData data;
  String traceId;
  TraceLink prevLink;

  public AppendLinkInput(String formId, TLinkData data, String traceId) throws IllegalArgumentException {
    if (formId == null) {
      throw new IllegalArgumentException("formId cannot be null in AppendLinkInput");
    }

    this.formId = formId;
    this.data = data;
    this.traceId = traceId;
  }

  public AppendLinkInput(String formId, TLinkData data, String traceId, TraceLink prevLink)
      throws IllegalArgumentException {
    if (formId == null) {
      throw new IllegalArgumentException("formId cannot be null in AppendLinkInput");
    }
    this.formId = formId;
    this.data = data;
    this.traceId = traceId;
    this.prevLink = prevLink;
  }

}
