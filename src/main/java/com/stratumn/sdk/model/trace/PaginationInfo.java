/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk.model.trace;

public class PaginationInfo {

  Integer first;
  String after;
  Integer last;
  String before;
  
  public PaginationInfo() {}
  
  public PaginationInfo(Integer first, String after, Integer last, String before) throws IllegalArgumentException {
    if (first != null) {
    	this.first = first;
    }
    if (after != null) {
    	 this.after = after;
    }
    if (last != null) {
    	this.last = last;
    }
    if (before != null) {
    	this.before = before;
    }
 
  }
  

  public void setForward(int first, String after) {
    this.first = first;
    this.after = after;
  }

  public void setBackward(int last, String before) {
    this.last = last;
    this.before = before;
  }

  public Integer getFirst() {
    return this.first;
  }

  public void setFirst(int first) {
      this.first = first;
  }

  public String getAfter() {
    return this.after;
  }

  public void setAfter(String after) {
    this.after = after;
  }

  public Integer getLast() {
    return this.last;
  }

  public void setLast(int last) {
    this.last = last;
  }

  public String getBefore() {
    return this.before;
  }

  public void setBefore(String before) {
    this.before = before;
  }
}
