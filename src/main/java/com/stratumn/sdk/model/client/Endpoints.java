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

  public static final String TRACE_STAGING_URL = "https://trace-api.staging.stratumn.com";
  public static final String ACCOUNT_STAGING_URL = "https://account-api.staging.stratumn.com";
  public static final String MEDIA_STAGING_URL = "https://media-api.staging.stratumn.com";

  public Endpoints(String trace, String account, String media) {
    this.trace = trace;
    this.account = account;
    this.media = media;
  }

  public Endpoints(Environment env) {

    switch (env) {
    case STAGING:
      this.trace = TRACE_STAGING_URL;
      this.account = ACCOUNT_STAGING_URL;
      this.media = MEDIA_STAGING_URL;
      break;
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
