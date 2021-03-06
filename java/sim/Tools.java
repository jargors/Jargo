package com.github.jargors.sim;
import com.github.jargors.sim.EdgeNotFoundException;
import com.github.jargors.sim.VertexNotFoundException;
import com.github.jargors.sim.GtreeNotLoadedException;
import com.github.jargors.sim.GtreeIllegalSourceException;
import com.github.jargors.sim.GtreeIllegalTargetException;
import com.github.jamjpan.gtree.GTree;
import com.github.jamjpan.gtree.gtreeJNI;
import com.github.jamjpan.gtree.IntVector;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
public class Tools {
  private GTree gtree;
  private boolean flag_gtree_loaded = false;
  private ConcurrentHashMap<Integer, int[]> lu_vertices = new ConcurrentHashMap<Integer, int[]>();
  private ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>>    lu_edges    = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>>();
  private ConcurrentHashMap<Integer, int[]> lu_users    = new ConcurrentHashMap<Integer, int[]>();
  private int[] bbox = new int[] { };
  private final double CSHIFT = Storage.CSHIFT;
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.tools.debug"));
  public Tools() {
    if (DEBUG) {
      System.out.printf("create Tools\n");
    }
  }
  public int[] DBQueryEdge(final int v1, final int v2) throws EdgeNotFoundException {
           if (v1 == v2) {
             return new int[] { 0, -1 };  // 0 distance, -1 speed
           }
           if (!(this.lu_edges.containsKey(v1) && this.lu_edges.get(v1).containsKey(v2))) {
             throw new EdgeNotFoundException("Edge ("+v1+", "+v2+") not found.");
           }
           return this.lu_edges.get(v1).get(v2).clone();
         }
  public int[] DBQueryVertex(final int v) throws VertexNotFoundException {
           if (!this.lu_vertices.containsKey(v)) {
             throw new VertexNotFoundException("Vertex "+v+" not found.");
           }
           int[] output = this.lu_vertices.get(v).clone();
           return new int[] { output[0], output[1], (int) Storage.CSHIFT };
         }
  public void setRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
           this.lu_edges = lu_edges;
         }
  public void setRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
           this.lu_users = lu_users;
         }
  public void setRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
           this.lu_vertices = lu_vertices;
         }
  public void GTGtreeLoad(final String p) throws FileNotFoundException {
           try {
             System.loadLibrary("gtree");
           } catch (UnsatisfiedLinkError e) {
             System.err.println("Native code library failed to load: "+e);
             System.exit(1);
           }
           if (p.length() > 0) {
             gtreeJNI.setIndex_path(p);
             this.gtree = new GTree();
             gtreeJNI.read_GTree(gtree);
             this.flag_gtree_loaded = true;
           } else {
             throw new FileNotFoundException("Bad path to gtree");
           }
         }
  public void GTGtreeClose() {
           this.gtree = null;
           this.flag_gtree_loaded = false;
         }
  public int[] computeBoundingBox() {
           if (this.bbox.length == 0) {
             int x_min = Integer.MAX_VALUE;
             int y_min = Integer.MAX_VALUE;
             int x_max = Integer.MIN_VALUE;
             int y_max = Integer.MIN_VALUE;
             for (int i : this.lu_vertices.keySet()) {
               if (i == 0) {
                 continue;
               }
               final int[] coord = this.lu_vertices.get(i);
               x_min = Math.min(x_min, coord[0]);
               y_min = Math.min(y_min, coord[1]);
               x_max = Math.max(x_max, coord[0]);
               y_max = Math.max(y_max, coord[1]);
             }
             this.bbox = new int[] { x_min, x_max, y_min, y_max };
           }
           return this.bbox.clone();
         }
  public int computeDuration(final int dd, final int nu) {
           int d = (int) Math.ceil(dd/(float) nu);
           return (d == 0 ? 1 : d);
         }
  public int computeHaversine(final int u, final int v) throws VertexNotFoundException {
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
  public int computeHaversine(
             final double lng1, final double lat1, final double lng2, final double lat2) {
           final double  dlat = Math.toRadians((lat2 - lat1));
           final double  dlng = Math.toRadians((lng2 - lng1));
           final double rlat1 = Math.toRadians(lat1);
           final double rlat2 = Math.toRadians(lat2);
           final double a = Math.pow(Math.sin((dlat/2)), 2)
             + (Math.pow(Math.sin((dlng/2)), 2)*Math.cos(rlat1)*Math.cos(rlat2));
           final double c = 2*Math.asin(Math.sqrt(a));
           int d = (int) Math.round(c*6371000);
           if (d == 0 && (lng1 != lng2 || lat1 != lat2)) {
             d = 1;
           }
           return d;
         }
  public int[] computeRoute(final int source, final int target, final int starttime)
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
  public int[] computeShortestPath(final int u, final int v)
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
             gtree.shortest_path_querying((u - 1), (v - 1)); // L1
             gtree.path_recovery((u - 1), (v - 1), path);
             if (path != null) {
               output = new int[path.size()];
               for (int i = 0; i < path.size(); i++) {
                 output[i] = path.get(i) + 1;                // L2
               }
             }
           }
           return output;
         }
  public int computeShortestPathDistance(final int u, final int v)
         throws GtreeNotLoadedException, GtreeIllegalSourceException {
           int d = 0;
           if (!this.flag_gtree_loaded) {
             throw new GtreeNotLoadedException("GTree not loaded!");
           } else if (u == 0) {
             throw new GtreeIllegalSourceException("Source cannot be 0!");
           } else if (u != v && v != 0) {
             d = gtree.shortest_path_querying((u - 1), (v - 1));
           }
           return d;
         }
  public int[] filterByHaversine(final int ro, final int[] locs, final int threshold)
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
  public long parseClockReference (final String refTimeStr) throws ParseException {
           SimpleDateFormat sdf = new SimpleDateFormat("HHmmyyyy");
           Date d = sdf.parse(refTimeStr+"1971");
           System.out.printf("hr=%d\n", d.getHours());
           System.out.printf("mn=%d\n", d.getMinutes());
           return d.getHours()*60*60*1000 + d.getMinutes()*60*1000;
         }
  public void printPath(final int[] p) {
           for (Integer i : p) {
             System.out.print(i+" ");
           }
           System.out.println();
         }
  public void printRoute(final int[] w) {
           for (int i = 0; i < (w.length - 1); i += 2) {
             System.out.print("("+w[i]+", "+w[(i + 1)]+") ");
           }
           System.out.println();
         }
  public void printSchedule(final int[] b) {
           for (int i = 0; i < (b.length - 3); i += 4) {
             System.out.print("("+b[i]+", "+b[(i + 1)]
               + ", "+b[(i + 2)]+", "+b[(i + 3)]+") ");
           }
           System.out.println();
         }
  public void printUser(final int[] u) {
           System.out.println("User {uid="+u[0]+", q="+u[1]+", e="+u[2]+", l="+u[3]
             +", o="+u[4]+", d="+u[5]+", b="+u[6]+"}");
         }
  public static void Print(final String b) {
                  System.out.println(String.format("[*][%s] %s", LocalDateTime.now(), b));
                }
  public static void Print(final String a, final String b) {
                  System.out.println(String.format("[%s][%s] %s", a, LocalDateTime.now(), b));
                }
  public static void PrintSQLException(SQLException e) {
                  while (e != null) {
                    System.err.println("\n----- SQLException -----");
                    System.err.println("  SQL State:  " + e.getSQLState());
                    System.err.println("  Error Code: " + e.getErrorCode());
                    System.err.println("  Message:    " + e.getMessage());
                    e.printStackTrace(System.err);
                    e = e.getNextException();
                  }
                }
}
