package com.stratumn.sdk;

import java.io.BufferedInputStream;

import com.stratumn.sdk.model.media.*;
import com.stratumn.sdk.model.trace.*;

public interface ISdk<TState> {

  public <TLinkData> TraceState<TState> newTrace(NewTraceInput<TLinkData> input);

  public <TLinkData> TraceState<TState> appendLink(AppendLinkInput<TLinkData> input);

  public <TLinkData> TraceState<TState> pushTrace(PushTransferInput<TLinkData> input);

  public <TLinkData> TraceState<TState> pullTrace(PullTransferInput<TLinkData> input);

  public <TLinkData> TraceState<TState> acceptTransfer(TransferResponseInput<TLinkData> input);

  public <TLinkData> TraceState<TState> rejectTransfer(TransferResponseInput<TLinkData> input);

  public <TLinkData> TraceState<TState> cancelTransfer(TransferResponseInput<TLinkData> input);

  public TraceState<TState> getTraceState(GetTraceStateInput input);

  public TraceDetails getTraceDetails(GetTraceDetailsInput input);

  public TracesState<TState> getIncomingTraces(PaginationInfo paginationInfo);

  public TracesState<TState> getOutgoingTraces(PaginationInfo paginationInfo);

  public TracesState<TState> getBacklogTraces(PaginationInfo paginationInfo);

  public BufferedInputStream downloadFile(MediaRecord m);
}
