package com.github.jargors.core;

public class GtreeNotLoadedException extends Exception {
  public GtreeNotLoadedException() { }
  public GtreeNotLoadedException(String message) {
    super(message);
  }
  public GtreeNotLoadedException(Throwable cause) {
    super(cause);
  }
  public GtreeNotLoadedException(String message, Throwable cause) {
    super(message, cause);
  }
}

