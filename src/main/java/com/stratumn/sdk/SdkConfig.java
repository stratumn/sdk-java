package com.stratumn.sdk;

public class SdkConfig {
  String workflowId;
  String userId;
  String accountId;
  String groupId;
  String ownerId;
  // TODO: Replace this with a real signing key
  // cf: https://github.com/str4d/ed25519-java
  String signingPrivateKey;

  public SdkConfig(String workflowId, String userId, String accountId, String groupId, String ownerId,
      String signingPrivateKey) {
    this.workflowId = workflowId;
    this.userId = userId;
    this.accountId = accountId;
    this.groupId = groupId;
    this.ownerId = ownerId;
    this.signingPrivateKey = signingPrivateKey;
  }
}
