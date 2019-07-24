package com.stratumn.sdk.model.media;

public class MediaRecord {

  private String digest;
  private String name;

  public String getDigest() {
    return digest;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

}
