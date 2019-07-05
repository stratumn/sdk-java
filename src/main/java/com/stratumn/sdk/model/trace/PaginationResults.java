package com.stratumn.sdk.model.trace;

class Info {
  boolean hasNext;
  boolean hasPrevious;
  String startCursor;
  String endCursor;
}

public class PaginationResults {
  int totalCount;
  Info info;
}
