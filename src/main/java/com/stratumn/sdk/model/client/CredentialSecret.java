package com.stratumn.sdk.model.client;

public class CredentialSecret {

  final public String email;
  final public String password;

  CredentialSecret(String email, String password) throws IllegalArgumentException {
    if (email == null) {
      throw new IllegalArgumentException("email cannot be null in CredentialSecret");
    }
    if (password == null) {
      throw new IllegalArgumentException("password cannot be null in CredentialSecret");
    }
    this.email = email;
    this.password = password;
  }

}
