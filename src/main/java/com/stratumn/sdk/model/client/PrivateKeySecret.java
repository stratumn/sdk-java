package com.stratumn.sdk.model.client;

public class PrivateKeySecret {

  String privateKey;

  PrivateKeySecret(String privateKey) throws IllegalArgumentException {
    if (privateKey == null) {
      throw new IllegalArgumentException("privateKey cannot be null in PrivateKeySecret");
    }
    this.privateKey = privateKey;
  }
}
