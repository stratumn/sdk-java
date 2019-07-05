package com.stratumn.sdk.model.trace;

public class TracesState<TState> {

  TraceState<TState>[] traces;

  public TracesState(TraceState<TState>[] traces) {
    this.traces = traces;
  }

}
