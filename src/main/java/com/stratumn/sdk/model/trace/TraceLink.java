package com.stratumn.sdk.model.trace;

import java.util.Date;
import com.stratumn.chainscript.Link;
import com.stratumn.sdk.model.account.Account;

public class TraceLink extends Link {

  public TraceLink(Link link) {
    super();
  }

  public <TLinkData> TLinkData data() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public String traceId() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public TraceLinkType type() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Account createdBy() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Date createdAt() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Account owner() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public TraceLinkMetaData metadata() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
