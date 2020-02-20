package com.github.jargors.core;
public class DuplicateUserException extends Exception {
  public DuplicateUserException () { }
  public DuplicateUserException (String message) { super(message); }
  public DuplicateUserException (Throwable cause) { super(cause); }
  public DuplicateUserException (String message, Throwable cause) { super(message, cause); }
}
