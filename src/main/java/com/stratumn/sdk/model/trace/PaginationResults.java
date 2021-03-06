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
 

public class PaginationResults {

      private int totalCount;
      private Info info;
      
      public PaginationResults() {
    	  
      }
      public PaginationResults(int totalCount, Info info) throws IllegalArgumentException {
      
        if (info == null) {
          throw new IllegalArgumentException("info cannot be null in PaginationResults");
        }
        
        this.totalCount = totalCount;
      }

      public int getTotalCount() {
        return this.totalCount;
      }
    
      public void setTotalCount(int totalCount) {
          this.totalCount = totalCount;
      }
    
      public Info getInfo() {
        return this.info;
      }
    
      public void setInfo(Info info) {
        this.info = info;
      }

}
