package com.github.jargors.sim;
import com.github.jargors.sim.Tools;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public abstract class Traffic {
  protected Tools tools = new Tools();
  protected final boolean DEBUG = "true".equals(System.getProperty("jargors.traffic.debug"));
  public Traffic() {
    if (DEBUG) {
      System.out.printf("create Traffic\n");
    }
  }
  public void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
           this.tools.setRefCacheEdges(lu_edges);
         }
  public void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
           this.tools.setRefCacheVertices(lu_vertices);
         }
  public double apply(int v1, int v2, long t) {
           return 1.0;
         }
  public void init() {
           // Custom initialization procedures
         }
}
