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

import java.util.List;

import com.stratumn.sdk.TraceLink;

public class TraceDetails<TLinkData> extends PaginationResults {

  private List<TraceLink<TLinkData>> links;
  
  public TraceDetails() {}
  
  public TraceDetails(List<TraceLink<TLinkData>> links, int totalCount, Info info) {
	super(totalCount, info);
    this.links = links;
  }

  public List<TraceLink<TLinkData>> getLinks() {
    return this.links;
}

  public void setLinks(List<TraceLink<TLinkData>> links) {
      this.links = links;
  }

};
