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

public class Info {

    private boolean hasNext;
    private boolean hasPrevious;
    private String startCursor;
    private String endCursor;

    public Info(boolean hasNext, boolean hasPrevious, String startCursor, String endCursor) throws IllegalArgumentException {
        if (hasNext) {
          throw new IllegalArgumentException("hasNext cannot be null in Info");
        }
        if (hasPrevious) {
          throw new IllegalArgumentException("hasPrevious cannot be null in Info");
        }
        if (startCursor == null) {
          throw new IllegalArgumentException("startCursor cannot be null in Info");
        }
        if (endCursor == null) {
          throw new IllegalArgumentException("endCursor cannot be null in Info");
        }
    
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.startCursor = startCursor;
        this.endCursor = endCursor;
      }

      public boolean getHasNext() {
        return this.hasNext;
      }
    
      public void setHasNext(boolean hasNext) {
          this.hasNext = hasNext;
      }
    
      public boolean getHasPrevious() {
        return this.hasPrevious;
      }
    
      public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
      }
    
      public String getStartCursor() {
        return this.startCursor;
      }
    
      public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
      }
    
      public String getEndCursor() {
        return this.endCursor;
      }
    
      public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
      }

  }
