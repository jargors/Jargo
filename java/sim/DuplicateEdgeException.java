package com.github.jargors.sim;
public class DuplicateEdgeException extends Exception {
  public DuplicateEdgeException () { }
  public DuplicateEdgeException (String message) { super(message); }
  public DuplicateEdgeException (Throwable cause) { super(cause); }
  public DuplicateEdgeException (String message, Throwable cause) { super(message, cause); }
}
