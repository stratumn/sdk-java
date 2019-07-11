package com.stratumn.sdk.model.trace;

public class PaginationInfo {

  int first;
  String after;
  int last;
  String before;

  public void setForward(int first, String after) {
    this.first = first;
    this.after = after;
  }

  public void setBackward(int last, String before) {
    this.last = last;
    this.before = before;
  }

}
