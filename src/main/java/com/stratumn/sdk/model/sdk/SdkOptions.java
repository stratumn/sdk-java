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
package com.stratumn.sdk.model.sdk;

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.stratumn.sdk.model.client.*;

public class SdkOptions extends ClientOptions {
  private String workflowId;
  Proxy proxy;

  public SdkOptions(String workflowId, Secret secret) {
    super(null, secret);
    this.workflowId = workflowId;
  }

  public String getWorkflowId() {
    return this.workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public void setProxy(String host, int port) throws IllegalArgumentException {
    if (host == null) {
      throw new IllegalArgumentException("host cannot be null in proxy");
    }
    if (port == 0) {
      throw new IllegalArgumentException("port cannot be 0 in proxy");
    }

    this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
  }

}
