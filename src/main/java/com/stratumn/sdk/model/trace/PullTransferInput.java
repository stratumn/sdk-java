package com.stratumn.sdk.model.trace;

public class PullTransferInput<TLinkData> {

  String traceId;
  TLinkData data;
  TraceLink prevLink;

  public PullTransferInput(String traceId, TLinkData data, TraceLink prevLink) throws IllegalArgumentException {
    if (traceId == null && prevLink == null) {
      throw new IllegalArgumentException("traceId and prevLink cannot be both null in PullTransferInput");
    }

    this.traceId = traceId;
    this.data = data;
    this.prevLink = prevLink;
  }
}
