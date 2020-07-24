package com.github.jargors.sim;
public class UserNotFoundException extends Exception {
  public UserNotFoundException () { }
  public UserNotFoundException (String message) { super(message); }
  public UserNotFoundException (Throwable cause) { super(cause); }
  public UserNotFoundException (String message, Throwable cause) { super(message, cause); }
}
