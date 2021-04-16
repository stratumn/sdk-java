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

/**
 * Interface used as argument to append a new link to a trace. User must provide
 * the trace id, form id and form data. User can optionally provide the previous
 * link hash, if not it will be fetched from the API first. The key difference
 * with the NewTraceInput is that the trace id must be provided.
 */
public class AppendLinkInput<TLinkData> {

  private String action;
  private TLinkData data;
  private String traceId;
  private ITraceLink<TLinkData> prevLink;
  private String groupLabel;

  public AppendLinkInput(String action, TLinkData data, String traceId) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("action cannot be null in AppendLinkInput");
    }

    this.action = action;
    this.data = data;
    this.traceId = traceId;
  }

  public AppendLinkInput(String action, TLinkData data, TraceLink<TLinkData> prevLink) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("action cannot be null in AppendLinkInput");
    }
    this.action = action;
    this.data = data;
    this.prevLink = prevLink;
  }

  public String getFormId() {
    return this.action;
  }

  public void setFormId(String action) {
    this.action = action;
  }

  public String getAction() {
    return this.action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public TLinkData getData() {
    return this.data;
  }

  public void setData(TLinkData data) {
    this.data = data;
  }

  public String getTraceId() {
    return this.traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public ITraceLink<TLinkData> getPrevLink() {
    return this.prevLink;
  }

  public void setPrevLink(ITraceLink<TLinkData> prevLink) {
    this.prevLink = prevLink;
  }

  public String getGroupLabel() {
    return this.groupLabel;
  }

  public void setGroupLabel(String groupLabel) {
    this.groupLabel = groupLabel;
  }

}
