package com.stratumn.sdk.model.trace;

public class TransferResponseInput<TLinkData> {

  String traceId;
  TLinkData data;
  TraceLink prevLink;

  public TransferResponseInput(String traceId, TLinkData data, TraceLink prevLink) {
    this.traceId = traceId;
    this.data = data;
    this.prevLink = prevLink;
  }
}
