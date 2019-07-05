package com.stratumn.sdk.model.trace;

import java.util.Date;

import com.stratumn.sdk.model.account.Account;

public class TraceState<TState> {

  String traceId;
  TraceLink headLink;
  Date updatedAt;
  Account updatedBy;
  TState data;

  public TraceState(String traceId, TraceLink headLink, Date updatedAt, Account updatedBy, TState data)
      throws IllegalArgumentException {
    if (traceId == null) {
      throw new IllegalArgumentException("traceId cannot be null in a TraceState object");
    }

    this.traceId = traceId;
    this.headLink = headLink;
    this.updatedAt = updatedAt;
    this.updatedBy = updatedBy;
    this.data = data;

  }

  public String getTraceId() {
    return this.traceId;
  }

  public TraceLink getHeadLink() {
    return this.headLink;
  }

  public Date getUpdatedAt() {
    return this.updatedAt;
  }

  public Account getUpdatedBy() {
    return this.updatedBy;
  }

  public TState getData() {
    return this.data;
  }

}
