package com.github.jargors;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
public abstract class Traffic {
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.traffic.debug"));
  public Traffic() { }
  public float apply(int v1, int v2, int t) {
           System.out.println("Call apply("+v1+", "+v2+", "+t+")");
           return 0;
         }
}
