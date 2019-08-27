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

import com.stratumn.sdk.TraceLink;

public class PushTransferInput<TLinkData> extends ParentLink<TLinkData> {
 
  private String recipient;
  private TLinkData data; 

  public PushTransferInput(String traceId, String recipient, TLinkData data, TraceLink<TLinkData> prevLink)
      throws IllegalArgumentException {
     super(traceId, prevLink);
     if (recipient == null) {
      throw new IllegalArgumentException("recipient cannot be null in PushTransferInput");
    }
    
     
    this.data = data; 
    this.recipient = recipient;
  }

  public TLinkData getData() {
    return this.data;
  }

  public void setData(TLinkData data) {
    this.data = data;
  }

  public String getRecipient() {
    return this.recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  
}
