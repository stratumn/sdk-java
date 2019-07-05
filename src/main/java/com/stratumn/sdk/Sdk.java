package com.stratumn.sdk;

import com.stratumn.sdk.model.trace.*;

public class Sdk<TState> implements ISdk<TState> {

  private SdkOptions opts;
  private SdkConfig config;

  public Sdk(SdkOptions opts) {
    this.opts = opts;
  }

  @Override
  public <TLinkData> TraceState<TState> newTrace(NewTraceInput<TLinkData> input) {
    // The next 2 lines are just here to use opts and config once
    // so that the linter doesn't complain.
    System.out.println(opts.workflowId);
    System.out.println(config.ownerId);
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> appendLink(AppendLinkInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> pushTrace(PushTransferInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> pullTrace(PullTransferInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> acceptTransfer(TransferResponseInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> rejectTransfer(TransferResponseInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public <TLinkData> TraceState<TState> cancelTransfer(TransferResponseInput<TLinkData> input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TraceState<TState> getTraceState(GetTraceStateInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TraceDetails getTraceDetails(GetTraceDetailsInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TracesState<TState> getIncomingTraces(PaginationInfo paginationInfo) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TracesState<TState> getOutgoingTraces(PaginationInfo paginationInfo) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TracesState<TState> getBacklogTraces(PaginationInfo paginationInfo) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
