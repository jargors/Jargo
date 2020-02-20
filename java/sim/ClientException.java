package com.github.jargors.core;
public class ClientException extends Exception {
  public ClientException() { }
  public ClientException(String message) {
    super(message);
  }
  public ClientException(Throwable cause) {
    super(cause);
  }
  public ClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
