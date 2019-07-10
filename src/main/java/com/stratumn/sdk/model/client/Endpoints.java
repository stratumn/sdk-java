package com.stratumn.sdk.model.client;

public class Endpoints {
  String trace;
  String account;
  String media;

  public static final String TRACE_RELEASE_URL = "https://trace.stratumn.com";
  public static final String ACCOUNT_RELEASE_URL = "https://account.stratumn.com";
  public static final String MEDIA_RELEASE_URL = "https://media.stratumn.com";

  public Endpoints() {
    this.trace = TRACE_RELEASE_URL;
    this.account = ACCOUNT_RELEASE_URL;
    this.media = MEDIA_RELEASE_URL;
  }
}
