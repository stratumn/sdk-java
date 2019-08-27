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
 * The protected key type of secret to authenticates
 * via msg signature using password protected private key 
 *
 */
public class ProtectedKeySecret extends Secret {

  private String publicKey;
  private String password;

  public ProtectedKeySecret(String publicKey, String password) throws IllegalArgumentException {
    if (publicKey == null) {
      throw new IllegalArgumentException("publicKey cannot be null in ProtectedKeySecret");
    }
    if (password == null) {
      throw new IllegalArgumentException("password cannot be null in ProtectedKeySecret");
    }
    this.publicKey = publicKey;
    this.password = password;
  }

  public String getPublicKey() {
    return this.publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
