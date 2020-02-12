package com.github.jargors.core;

public class ClientFatalException extends Exception {
  public ClientFatalException() { }
  public ClientFatalException(String message) {
    super(message);
  }
  public ClientFatalException(Throwable cause) {
    super(cause);
  }
  public ClientFatalException(String message, Throwable cause) {
    super(message, cause);
  }
}

