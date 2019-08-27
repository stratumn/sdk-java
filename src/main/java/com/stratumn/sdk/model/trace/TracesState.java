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

import java.util.ArrayList;
import java.util.List;

public class TracesState<TState,TlinkData> extends PaginationResults {

  private List<TraceState<TState,TlinkData>> traces;

  public TracesState() {
    this.traces = new ArrayList<TraceState<TState,TlinkData>>();
  }

  public TracesState(List<TraceState<TState,TlinkData>> traces) {
    this.traces = traces;
  }

  public List<TraceState<TState,TlinkData>> getTraces() {
    return this.traces;
  }
  public void setTraces(List<TraceState<TState,TlinkData>> traces) {
      this.traces = traces;
  }

}
