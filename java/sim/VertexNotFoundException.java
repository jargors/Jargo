package com.github.jargors.sim;
public class VertexNotFoundException extends Exception {
  public VertexNotFoundException () { }
  public VertexNotFoundException (String message) { super(message); }
  public VertexNotFoundException (Throwable cause) { super(cause); }
  public VertexNotFoundException (String message, Throwable cause) { super(message, cause); }
}
