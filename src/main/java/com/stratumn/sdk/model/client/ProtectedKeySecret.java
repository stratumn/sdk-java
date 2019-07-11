package com.stratumn.sdk.model.client;

public class ProtectedKeySecret {

  String publicKey;
  String password;

  ProtectedKeySecret(String publicKey, String password) throws IllegalArgumentException {
    if (publicKey == null) {
      throw new IllegalArgumentException("publicKey cannot be null in ProtectedKeySecret");
    }
    if (password == null) {
      throw new IllegalArgumentException("password cannot be null in ProtectedKeySecret");
    }
    this.publicKey = publicKey;
    this.password = password;
  }

}
