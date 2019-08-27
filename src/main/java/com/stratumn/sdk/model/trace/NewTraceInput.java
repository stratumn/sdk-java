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
/**
 * Interface used as argument to create a new trace.
 * User must provide the form id to use and the form data.
 */
public class NewTraceInput<TLinkData> {

  private String formId;
  private TLinkData data;

  public NewTraceInput(String formId, TLinkData data) throws IllegalArgumentException {
    if (formId == null) {
      throw new IllegalArgumentException("formId cannot be null in NewTraceInput");
    }
    this.formId = formId;
    this.data = data;
  }


  public String getFormId() {
    return this.formId;
  }

  public void setFormId(String formId) {
      this.formId = formId;
  }

  public TLinkData getData() {
    return this.data;
  }

  public void setData(TLinkData data) {
    this.data = data;
  }
  
}
