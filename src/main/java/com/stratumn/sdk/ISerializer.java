package com.stratumn.sdk;

public interface ISerializer<T> {
  public T deserialize(String path);
}
