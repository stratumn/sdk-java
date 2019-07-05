package com.stratumn.sdk.model.trace;

public class TraceDetails extends PaginationResults {

  TraceLink[] links;

  public TraceDetails(TraceLink[] links) {
    this.links = links;
  }
};
