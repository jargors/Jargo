package com.github.jargors.sim;
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
