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
package com.stratumn.sdk;

public class HttpError extends Exception {
    /**
    * 
    */
   private static final long serialVersionUID = -1942318828228006196L;
   private int status;
    private String message;
  
    public HttpError(int status, String message) {
      this.status = status;
      this.message = message;
    }

    public String toString() {
        return "Http error [ Status : " + this.status + " Message : "+message+" ]";
     }

     public void setSatus(int status){
        this.status = status;
     }
     public int getSatus(){
         return this.status;
    }

    public void setMessage(String message){
        this.message = message;
     }
     public String getMessage(){
         return this.message;
    }
    
  }
  
