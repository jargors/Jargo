package com.github.jargors.sim;
public class TimeWindowException extends Exception {
  public TimeWindowException () { }
  public TimeWindowException (String message) { super(message); }
  public TimeWindowException (Throwable cause) { super(cause); }
  public TimeWindowException (String message, Throwable cause) { super(message, cause); }
}
