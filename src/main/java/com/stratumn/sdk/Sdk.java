package com.stratumn.sdk;

import com.stratumn.sdk.model.account.*;
import com.stratumn.sdk.model.trace.*;
import com.stratumn.sdk.model.media.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratumn.chainscript.*;

public class Sdk<TState> implements ISdk<TState> {

  private ISerializer<TState> serializer;

  public void setSerializer(ISerializer<TState> s) {
    this.serializer = s;
  }

  private SdkOptions opts;
  private SdkConfig config;

  // This is temporary until we plug the SDK to the APIs.
  private TState getMockState() {
    return this.serializer.deserialize(null);
  }

  public Sdk(SdkOptions opts) {
    this.opts = opts;

    this.pingTrace();
  }

  private void pingTrace() {
    try {
      // ping trace to test network connectivity.
      String url = String.format("%s/healthz", this.opts.endpoints.trace);
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        System.out.printf("Could not call Trace (%d)\n", responseCode);
      }

    } catch (Exception e) {
      System.err.print("Could not call Trace");
      e.printStackTrace();
      return;
    }

    System.out.println("================================================================================");
    System.out.println("                    Connection to trace established                             ");
    System.out.println("================================================================================");
  }

  @Override
  public <TLinkData> TraceState<TState> newTrace(NewTraceInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> appendLink(AppendLinkInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> pushTrace(PushTransferInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> pullTrace(PullTransferInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> acceptTransfer(TransferResponseInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> rejectTransfer(TransferResponseInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public <TLinkData> TraceState<TState> cancelTransfer(TransferResponseInput<TLinkData> input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public TraceState<TState> getTraceState(GetTraceStateInput input) {
    return new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(),
        this.getMockState());
  }

  @Override
  public TraceDetails getTraceDetails(GetTraceDetailsInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TracesState<TState> getIncomingTraces(PaginationInfo paginationInfo) {
    List<TraceState<TState>> res = new ArrayList<TraceState<TState>>();
    res.add(
        new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(), this.getMockState()));
    return new TracesState<TState>(res);
  }

  @Override
  public TracesState<TState> getOutgoingTraces(PaginationInfo paginationInfo) {
    List<TraceState<TState>> res = new ArrayList<TraceState<TState>>();
    res.add(
        new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(), this.getMockState()));
    return new TracesState<TState>(res);
  }

  @Override
  public TracesState<TState> getBacklogTraces(PaginationInfo paginationInfo) {
    List<TraceState<TState>> res = new ArrayList<TraceState<TState>>();
    res.add(
        new TraceState<TState>("trace ID", new TraceLink(new Link()), new Date(), new Account(), this.getMockState()));
    return new TracesState<TState>(res);
  }

  @Override
  public BufferedInputStream downloadFile(MediaRecord m) {
    // mock the return value
    return new BufferedInputStream(new ByteArrayInputStream(new byte[1]));
  }

}
