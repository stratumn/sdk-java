package com.stratumn.sdk;

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.stratumn.sdk.model.client.*;

public class SdkOptions {
  String workflowId;
  Secret secret;
  Endpoints endpoints;

  Proxy proxy;

  public SdkOptions(String workflowId, Secret secret) {
    this.workflowId = workflowId;
    this.secret = secret;
    this.endpoints = new Endpoints(Environment.RELEASE);
  }

  public SdkOptions(String workflowId, Secret secret, Environment env) {
    this.workflowId = workflowId;
    this.secret = secret;
    this.endpoints = new Endpoints(env);
  }

  public SdkOptions(String workflowId, Secret secret, Endpoints endpoints) {
    this.workflowId = workflowId;
    this.secret = secret;
    this.endpoints = endpoints;
  }

  public void setProxy(String host, int port) throws IllegalArgumentException {
    if (host == null) {
      throw new IllegalArgumentException("host cannot be null in proxy");
    }
    if (port == 0) {
      throw new IllegalArgumentException("port cannot be 0 in proxy");
    }

    this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
  }
}
