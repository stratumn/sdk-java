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
package com.stratumn.sdk;

import com.stratumn.sdk.model.trace.AppendLinkInput;
import com.stratumn.sdk.model.trace.GetTraceDetailsInput;
import com.stratumn.sdk.model.trace.GetTraceStateInput;
import com.stratumn.sdk.model.trace.NewTraceInput;
import com.stratumn.sdk.model.trace.PaginationInfo;
import com.stratumn.sdk.model.trace.PullTransferInput;
import com.stratumn.sdk.model.trace.PushTransferInput;
import com.stratumn.sdk.model.trace.TraceDetails;
import com.stratumn.sdk.model.trace.TraceState;
import com.stratumn.sdk.model.trace.TracesState;
import com.stratumn.sdk.model.trace.TransferResponseInput;
 
public interface ISdk<TState> {

  public  <TLinkData> TraceState<TState,TLinkData>newTrace( NewTraceInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>appendLink(AppendLinkInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>pushTrace(PushTransferInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>pullTrace(PullTransferInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>acceptTransfer(TransferResponseInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>rejectTransfer(TransferResponseInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>cancelTransfer(TransferResponseInput<TLinkData> input) throws Exception;

  public <TLinkData> TraceState<TState,TLinkData>getTraceState(GetTraceStateInput input) throws Exception;

  public   <TLinkData> TraceDetails<TLinkData> getTraceDetails(GetTraceDetailsInput input) throws Exception;

  public <TLinkData>  TracesState<TState,TLinkData> getIncomingTraces(PaginationInfo paginationInfo) throws Exception;

  public  <TLinkData>  TracesState<TState,TLinkData> getOutgoingTraces(PaginationInfo paginationInfo) throws   Exception;

  public <TLinkData>  TracesState<TState,TLinkData>  getBacklogTraces(PaginationInfo paginationInfo) throws Exception;

  public <TLinkData>  TracesState<TState,TLinkData>  getAttestationTraces(String formId, PaginationInfo paginationInfo)
		throws   Exception;
}
