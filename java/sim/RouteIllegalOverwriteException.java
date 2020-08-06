package com.github.jargors.sim;
public class RouteIllegalOverwriteException extends Exception {
  public RouteIllegalOverwriteException () { }
  public RouteIllegalOverwriteException (String message) { super(message); }
  public RouteIllegalOverwriteException (Throwable cause) { super(cause); }
  public RouteIllegalOverwriteException (String message, Throwable cause) { super(message, cause); }
}
