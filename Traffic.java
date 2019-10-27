package com.github.jargors;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
public abstract class Traffic {
  protected Map<Integer, int[]> lu_vertices = new HashMap<>();
  protected Map<Integer, Map<Integer, int[]>> lu_edges = new HashMap<>();
  protected boolean DEBUG = false;
  public Traffic() { }
  public void setDebug(boolean flag) {
    DEBUG = flag;
  }
  public void registerVertices(Map<Integer, int[]> src) {
    lu_vertices = src;
  }
  public void registerEdges(Map<Integer, Map<Integer, int[]>> src) {
    lu_edges = src;
  }
  public void apply(int v1, int v2, int t) {
    Print("Call apply("+v1+", "+v2+", "+t+")");
  }
  protected void Print(String msg) {
    if (DEBUG) {
      System.out.println("[Traffic]["+LocalDateTime.now()+"] "+msg);
    }
  }
}
