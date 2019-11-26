package com.github.jargors;
import com.github.jargors.Tools;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public abstract class Traffic {
  protected Tools tools = new Tools();
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.traffic.debug"));
  public Traffic() { }
  public void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
           this.tools.setRefCacheEdges(lu_edges);
         }
  public void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
           this.tools.setRefCacheVertices(lu_vertices);
         }
  public double apply(int v1, int v2, int t) {
           return 1.0;
         }
}
