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
 package com.stratumn.sdk.model.client;
 
 /**
 * The response format for a login request
 */
public class LoginResponse {
  /**
   * The authentication token
   */
   private String token;

  public LoginResponse(String token) throws IllegalArgumentException {
    if (token == null) {
       throw new IllegalArgumentException("token cannot be null");
    }
    this.token = token;
  }

  public String getToken() {
    return this.token;
  }

  public void setToken(String token) {
    this.token = token;
  }

}
