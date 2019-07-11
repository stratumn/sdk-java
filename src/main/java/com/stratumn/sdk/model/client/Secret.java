package com.stratumn.sdk.model.client;

public class Secret {

  final public SecretType type;

  private CredentialSecret credentialSecret;
  private PrivateKeySecret privateKeySecret;
  private ProtectedKeySecret protectedKeySecret;

  private Secret(CredentialSecret s) throws IllegalArgumentException {
    if (s == null) {
      throw new IllegalArgumentException("input cannot be null in Secret constructor");
    }
    this.type = SecretType.CREDENTIAL;
    this.credentialSecret = s;
  }

  private Secret(PrivateKeySecret s) throws IllegalArgumentException {
    if (s == null) {
      throw new IllegalArgumentException("input cannot be null in Secret constructor");
    }
    this.type = SecretType.PRIVATE_KEY;
    this.privateKeySecret = s;
  }

  private Secret(ProtectedKeySecret s) throws IllegalArgumentException {
    if (s == null) {
      throw new IllegalArgumentException("input cannot be null in Secret constructor");
    }
    this.type = SecretType.PROTECTED_KEY;
    this.protectedKeySecret = s;
  }

  public static Secret newCredentialSecret(String email, String password) {
    throw new UnsupportedOperationException("Not implemented yet");
    // return new Secret(new CredentialSecret(email, password));
  }

  public static Secret newPrivateKeySecret(String privateKey) {
    return new Secret(new PrivateKeySecret(privateKey));
  }

  public static Secret newProtectedKeySecret(String publicKey, String password) {
    throw new UnsupportedOperationException("Not implemented yet");
    // return new Secret(new ProtectedKeySecret(publicKey, password));
  }

  public String getEmail() {
    switch (this.type) {
    case CREDENTIAL:
      return this.credentialSecret.email;
    default:
      return null;
    }
  }

  public String getPassword() {
    switch (this.type) {
    case CREDENTIAL:
      return this.credentialSecret.password;
    case PROTECTED_KEY:
      return this.protectedKeySecret.password;
    default:
      return null;
    }
  }

  public String getPrivateKey() {
    switch (this.type) {
    case PRIVATE_KEY:
      return this.privateKeySecret.privateKey;
    default:
      return null;
    }
  }

  public String getPublicKey() {
    switch (this.type) {
    case PROTECTED_KEY:
      return this.protectedKeySecret.publicKey;
    default:
      return null;
    }
  }

}
