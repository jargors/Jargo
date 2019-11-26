/*line 18 "src/Traffic.nw"*/
package com.github.jargors;
import com.github.jargors.Tools;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
/*line 6 "src/Traffic.nw"*/
public abstract class Traffic {
  
/*line 27 "src/Traffic.nw"*/
protected Tools tools = new Tools();
private final boolean DEBUG = "true".equals(System.getProperty("jargors.traffic.debug"));
/*line 8 "src/Traffic.nw"*/
  
/*line 34 "src/Traffic.nw"*/
public Traffic() { }
/*line 9 "src/Traffic.nw"*/
  
/*line 245 "src/tex/0-Overview.nw"*/
public 
/*line 1209 "src/tex/4-Administration.nw"*/
void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
  this.tools.setRefCacheEdges(lu_edges);
}
/*line 246 "src/tex/0-Overview.nw"*/
public 
/*line 1223 "src/tex/4-Administration.nw"*/
void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
  this.tools.setRefCacheVertices(lu_vertices);
}
/*line 247 "src/tex/0-Overview.nw"*/
public 
/*line 41 "src/Traffic.nw"*/
double apply(int v1, int v2, int t) {
  return 1.0;
}
/*line 10 "src/Traffic.nw"*/
}
