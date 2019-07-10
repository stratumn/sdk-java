package com.stratumn.sdk.model.trace;

import java.util.ArrayList;
import java.util.List;

public class TracesState<TState> {

  List<TraceState<TState>> traces;

  public TracesState() {
    this.traces = new ArrayList<TraceState<TState>>();
  }

  public TracesState(List<TraceState<TState>> traces) {
    this.traces = traces;
  }

}
