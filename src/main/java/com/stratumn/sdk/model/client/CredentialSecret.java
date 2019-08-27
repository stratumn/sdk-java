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
/***
 * The credential type of secret to authenticates
 * via email + password 
 *
 */
public class CredentialSecret extends Secret {

  private String email;
  private String password;

  public CredentialSecret(String email, String password) throws IllegalArgumentException {
     super();
    if (email == null) {
      throw new IllegalArgumentException("email cannot be null in CredentialSecret");
    }
    if (password == null) {
      throw new IllegalArgumentException("password cannot be null in CredentialSecret");
    }
    this.email = email;
    this.password = password;
  }

   public String getEmail() {
        return this.email;
   }
 
   public void setEmail(String email) {
        this.email = email;
   }

   public String getPassword() {
        return this.password;
   }
 
   public void setPassword(String password) {
        this.password = password;
   }
}
