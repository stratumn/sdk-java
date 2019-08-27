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
 *  * The private key type of secret to authenticates
 * via msg signature using provided private key 
 *
 */
public class PrivateKeySecret extends Secret {

  private String privateKey;

  public PrivateKeySecret(String privateKey) throws IllegalArgumentException {
    if (privateKey == null) {
      throw new IllegalArgumentException("privateKey cannot be null in PrivateKeySecret");
    }
    this.privateKey = privateKey;
  }

  public String getPrivateKey() {
    return this.privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

}
