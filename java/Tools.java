/*line 19 "src/Tools.nw"*/
package com.github.jargors;
import com.github.jargors.gtreeJNI.*;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import com.github.jargors.exceptions.GtreeNotLoadedException;
import com.github.jargors.exceptions.GtreeIllegalSourceException;
import com.github.jargors.exceptions.GtreeIllegalTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.sql.SQLException;
import java.io.FileNotFoundException;
/*line 6 "src/Tools.nw"*/
public class Tools {
  
/*line 35 "src/Tools.nw"*/
private G_Tree gtree;
private boolean flag_gtree_loaded = false;
private ConcurrentHashMap<Integer, int[]> lu_vertices = new ConcurrentHashMap<Integer, int[]>();
private ConcurrentHashMap<Integer,
    ConcurrentHashMap<Integer, int[]>>    lu_edges    = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>>();
private ConcurrentHashMap<Integer, int[]> lu_users    = new ConcurrentHashMap<Integer, int[]>();
private final double CSHIFT = 10000000.0;
private final boolean DEBUG = "true".equals(System.getProperty("jargors.tools.debug"));
/*line 8 "src/Tools.nw"*/
  
/*line 47 "src/Tools.nw"*/
public Tools() { }
/*line 9 "src/Tools.nw"*/
  
/*line 254 "src/tex/0-Overview.nw"*/
public 
/*line 287 "src/tex/2-Reading.nw"*/
int[] DBQueryEdge(final int v1, final int v2) throws EdgeNotFoundException {
  if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
    throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
  }
  return this.lu_edges.get(v1).get(v2).clone();
}
/*line 255 "src/tex/0-Overview.nw"*/
public 
/*line 153 "src/tex/2-Reading.nw"*/
int[] DBQueryVertex(final int v) throws VertexNotFoundException {
  if (!this.lu_vertices.containsKey(v)) {
    throw new VertexNotFoundException("Vertex "+v+" not found.");
  }
  return this.lu_vertices.get(v).clone();
}
/*line 262 "src/tex/0-Overview.nw"*/
public 
/*line 10 "src/tex/5-Gtree.nw"*/
void GTGtreeLoad(final String p) throws FileNotFoundException {
  try {
    System.loadLibrary("gtree");
  } catch (UnsatisfiedLinkError e) {
    System.err.println("Native code library failed to load: "+e);
    System.exit(1);
  }
  if (p.length() > 0) {
    gtreeJNI.load(p);
    this.gtree = gtreeJNI.get();
    this.flag_gtree_loaded = true;
  } else {
    throw new FileNotFoundException("Bad path to gtree");
  }
}
/*line 263 "src/tex/0-Overview.nw"*/
public 
/*line 40 "src/tex/5-Gtree.nw"*/
void GTGtreeClose() {
  this.gtree = null;
  this.flag_gtree_loaded = false;
}
/*line 264 "src/tex/0-Overview.nw"*/
public 
/*line 1138 "src/tex/4-Administration.nw"*/
void setRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
  this.lu_edges = lu_edges;
}
/*line 265 "src/tex/0-Overview.nw"*/
public 
/*line 1145 "src/tex/4-Administration.nw"*/
void setRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
  this.lu_users = lu_users;
}
/*line 266 "src/tex/0-Overview.nw"*/
public 
/*line 1131 "src/tex/4-Administration.nw"*/
void setRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
  this.lu_vertices = lu_vertices;
}
/*line 267 "src/tex/0-Overview.nw"*/
public 
/*line 93 "src/Tools.nw"*/
int computeDuration(final int dd, final int nu) {
  int d = (int) Math.ceil(dd/(float) nu);
  return (d == 0 ? 1 : d);
}
/*line 268 "src/tex/0-Overview.nw"*/
public 
/*line 78 "src/Tools.nw"*/
int computeHaversine(final int u, final int v) throws VertexNotFoundException {
  if (!this.lu_vertices.containsKey(u)) {
    throw new VertexNotFoundException("Vertex "+u+" not found.");
  }
  if (!this.lu_vertices.containsKey(v)) {
    throw new VertexNotFoundException("Vertex "+v+" not found.");
  }
  return this.computeHaversine(
    this.lu_vertices.get(u)[0]/CSHIFT, this.lu_vertices.get(u)[1]/CSHIFT,
    this.lu_vertices.get(v)[0]/CSHIFT, this.lu_vertices.get(v)[1]/CSHIFT);
}
/*line 269 "src/tex/0-Overview.nw"*/
public 
/*line 54 "src/Tools.nw"*/
int computeHaversine(
    final double lng1, final double lat1, final double lng2, final double lat2) {
  final double  dlat = Math.toRadians((lat2 - lat1));
  final double  dlng = Math.toRadians((lng2 - lng1));
  final double rlat1 = Math.toRadians(lat1);
  final double rlat2 = Math.toRadians(lat2);
  final double a = Math.pow(Math.sin((dlat/2)), 2)
    + (Math.pow(Math.sin((dlng/2)), 2)*Math.cos(rlat1)*Math.cos(rlat2));
  final double c = 2*Math.asin(Math.sqrt(a));
  int d = (int) Math.round(c*6371000);
  
/*line 71 "src/Tools.nw"*/
if (d == 0 && (lng1 != lng2 || lat1 != lat2)) {
  d = 1;
}
/*line 65 "src/Tools.nw"*/
  return d;
}
/*line 270 "src/tex/0-Overview.nw"*/
public 
/*line 161 "src/Tools.nw"*/
int[] computeRoute(final int source, final int target, final int starttime)
throws GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
  int[] output = null;
  if (source == 0) {
    throw new GtreeIllegalSourceException("Source cannot be 0!");
  } else if (target == 0) {
    output = new int[] { starttime, source, starttime + 1, target };
  } else {
    int[] path = this.computeShortestPath(source, target);
    if (path == null) {
      throw new GtreeIllegalTargetException("No path from source to target!");
    } else {
      output = new int[(path.length*2)];
      output[0] = starttime;
      output[1] = source;
      int t = starttime;
      int j = 2;
      for (int i = 0; i < (path.length - 1); i++) {
        int u = path[(i + 0)];
        int v = path[(i + 1)];
        int[] edge = this.lu_edges.get(u).get(v);
        output[(j + 0)] = (t += computeDuration(edge[0], edge[1]));
        output[(j + 1)] = v;
        j += 2;
      }
    }
  }
  return output;
}
/*line 271 "src/tex/0-Overview.nw"*/
public 
/*line 110 "src/Tools.nw"*/
int[] computeShortestPath(final int u, final int v)
throws GtreeNotLoadedException, GtreeIllegalSourceException {
  int[] output = null;
  if (!this.flag_gtree_loaded) {
    throw new GtreeNotLoadedException("Gtree not loaded!");
  } else if (u == 0) {
    throw new GtreeIllegalSourceException("Source cannot be 0!");
  } else if (v == 0) {
    output = new int[] { u, v };
  } else if (u == v) {
    output = new int[] { u };
  } else {
    IntVector path = new IntVector();
    gtree.find_path((u - 1), (v - 1), path);        // L1
    if (path != null) {
      output = new int[path.size()];
      for (int i = 0; i < path.size(); i++) {
        output[i] = path.get(i) + 1;                // L2
      }
    }
  }
  return output;
}
/*line 272 "src/tex/0-Overview.nw"*/
public 
/*line 145 "src/Tools.nw"*/
int computeShortestPathDistance(final int u, final int v)
throws GtreeNotLoadedException, GtreeIllegalSourceException {
  int d = 0;
  if (!this.flag_gtree_loaded) {
    throw new GtreeNotLoadedException("GTree not loaded!");
  } else if (u == 0) {
    throw new GtreeIllegalSourceException("Source cannot be 0!");
  } else if (u != v && v != 0) {
    d = gtree.search((u - 1), (v - 1));
  }
  return d;
}
/*line 273 "src/tex/0-Overview.nw"*/
public 
/*line 220 "src/Tools.nw"*/
int[] filterByHaversine(final int ro, final int[] locs, final int threshold)
throws VertexNotFoundException {
  final int n = (locs.length/3);
  int[] temp = new int[n];
  int i = 0;
  for (int k = 0; k < n; k++) {
    if (this.computeHaversine(ro, locs[((3*k) + 2)]) < threshold) {
      temp[i++] = 3*k;
    }
  }
  return Arrays.copyOf(temp, i);
}
/*line 274 "src/tex/0-Overview.nw"*/
public 
/*line 244 "src/Tools.nw"*/
void printPath(final int[] p) {
  for (Integer i : p) {
    System.out.print(i+" ");
  }
  System.out.println();
}
/*line 275 "src/tex/0-Overview.nw"*/
public 
/*line 254 "src/Tools.nw"*/
void printRoute(final int[] w) {
  for (int i = 0; i < (w.length - 1); i += 2) {
    System.out.print("("+w[i]+", "+w[(i + 1)]+") ");
  }
  System.out.println();
}
/*line 276 "src/tex/0-Overview.nw"*/
public 
/*line 264 "src/Tools.nw"*/
void printSchedule(final int[] b) {
  for (int i = 0; i < (b.length - 3); i += 4) {
    System.out.print("("+b[i]+", "+b[(i + 1)]
      + ", "+b[(i + 2)]+", "+b[(i + 3)]+") ");
  }
  System.out.println();
}
/*line 277 "src/tex/0-Overview.nw"*/
public 
/*line 236 "src/Tools.nw"*/
void printUser(final int[] u) {
  System.out.println("User {uid="+u[0]+", q="+u[1]+", e="+u[2]+", l="+u[3]
    +", o="+u[4]+", d="+u[5]+", b="+u[6]+"}");
}
/*line 278 "src/tex/0-Overview.nw"*/
public static 
/*line 283 "src/Tools.nw"*/
void Print(final String b) {
  System.out.println(String.format("[*][%s] %s", LocalDateTime.now(), b));
}
/*line 279 "src/tex/0-Overview.nw"*/
public static 
/*line 276 "src/Tools.nw"*/
void Print(final String a, final String b) {
  System.out.println(String.format("[%s][%s] %s", a, LocalDateTime.now(), b));
}
/*line 280 "src/tex/0-Overview.nw"*/
public static 
/*line 290 "src/Tools.nw"*/
void PrintSQLException(SQLException e) {
  while (e != null) {
    System.err.println("\n----- SQLException -----");
    System.err.println("  SQL State:  " + e.getSQLState());
    System.err.println("  Error Code: " + e.getErrorCode());
    System.err.println("  Message:    " + e.getMessage());
    e.printStackTrace(System.err);
    e = e.getNextException();
  }
}
/*line 10 "src/Tools.nw"*/
}
