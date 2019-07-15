package com.stratumn.sdk.model.client;

import com.stratumn.sdk.model.client.Environment;

public class Endpoints {
  public final String trace;
  public final String account;
  public final String media;

  public static final String TRACE_RELEASE_URL = "https://trace-api.stratumn.com";
  public static final String ACCOUNT_RELEASE_URL = "https://account-api.stratumn.com";
  public static final String MEDIA_RELEASE_URL = "https://media-api.stratumn.com";

  public static final String TRACE_DEMO_URL = "https://trace-api.demo.stratumn.com";
  public static final String ACCOUNT_DEMO_URL = "https://account-api.demo.stratumn.com";
  public static final String MEDIA_DEMO_URL = "https://media-api.demo.stratumn.com";

  public Endpoints(Environment env) {

    switch (env) {
    case DEMO:
      this.trace = TRACE_DEMO_URL;
      this.account = ACCOUNT_DEMO_URL;
      this.media = MEDIA_DEMO_URL;
      break;
    case RELEASE:
      this.trace = TRACE_RELEASE_URL;
      this.account = ACCOUNT_RELEASE_URL;
      this.media = MEDIA_RELEASE_URL;
      break;
    default:
      throw new IllegalArgumentException(String.format("unknown environment %s", env));
    }
  }
}
