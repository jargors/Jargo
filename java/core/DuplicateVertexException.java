package com.github.jargors.core;

public class DuplicateVertexException extends Exception {
  public DuplicateVertexException() { }
  public DuplicateVertexException(String message) {
    super(message);
  }
  public DuplicateVertexException(Throwable cause) {
    super(cause);
  }
  public DuplicateVertexException(String message, Throwable cause) {
    super(message, cause);
  }
}

