package com.stratumn.sdk.model.trace;

public class PushTransferInput<TLinkData> {

  String traceId;
  String recipient;
  TLinkData data;
  TraceLink prevLink;

  public PushTransferInput(String traceId, String recipient, TLinkData data, TraceLink prevLink)
      throws IllegalArgumentException {
    if (recipient == null) {
      throw new IllegalArgumentException("recipient cannot be null in PushTransferInput");
    }
    this.traceId = traceId;
    this.data = data;
    this.prevLink = prevLink;
  }
}
