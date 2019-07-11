package com.stratumn.sdk;

import com.stratumn.sdk.model.client.*;

public class SdkOptions {
  String workflowId;
  Secret secret;
  Endpoints endpoints;

  public SdkOptions(String workflowId, Secret secret) {
    this.workflowId = workflowId;
    this.secret = secret;
    this.endpoints = new Endpoints();
  }
}