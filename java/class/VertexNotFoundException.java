package com.github.jargors.core;

public class VertexNotFoundException extends Exception {
  public VertexNotFoundException() { }
  public VertexNotFoundException(String message) {
    super(message);
  }
  public VertexNotFoundException(Throwable cause) {
    super(cause);
  }
  public VertexNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

