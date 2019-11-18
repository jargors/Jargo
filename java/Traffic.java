package com.github.jargors;
import com.github.jargors.Tools;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public abstract class Traffic {
  protected Tools tools = new Tools();
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.traffic.debug"));
  public Traffic() { }
  public float apply(int v1, int v2, int t) {
           System.out.println("Call apply("+v1+", "+v2+", "+t+")");
           return 0;
         }
  final public void forwardVertices(final ConcurrentHashMap<Integer, int[]> src) {
                 this.tools.registerVertices(src);
               }
  final public void forwardEdges(final ConcurrentHashMap<Integer,
                   ConcurrentHashMap<Integer, int[]>> src) {
                 this.tools.registerEdges(src);
               }
}
