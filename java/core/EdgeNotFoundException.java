package com.github.jargors.core;
public class EdgeNotFoundException extends Exception {
  public EdgeNotFoundException () { }
  public EdgeNotFoundException (String message) { super(message); }
  public EdgeNotFoundException (Throwable cause) { super(cause); }
  public EdgeNotFoundException (String message, Throwable cause) { super(message, cause); }
}
