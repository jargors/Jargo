package com.github.jargors;
import com.github.jargors.gtreeJNI.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
public class Tools {
  private G_Tree gtree;
  private boolean flag_gtree_loaded = false;
  private Map<Integer, int[]> lu_vertices = new HashMap<>();
  private Map<Integer, Map<Integer, int[]>> lu_edges = new HashMap<>();
  private Map<Integer, int[]> lu_users = new HashMap<>();
  private final double CSHIFT = 10000000.0;
    public void loadGTree(String p) {
      try {
        System.loadLibrary("gtree");
      } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library failed to load: "+e);
        System.exit(1);
      }
      if (p.length() > 0) {
        gtreeJNI.load(p);
        gtree = gtreeJNI.get();
        flag_gtree_loaded = true;
      } else {
        System.out.println("Bad path to gtree");
      }
    }
    public void registerVertices(Map<Integer, int[]> src) {
      lu_vertices = src;
    }
    public void registerEdges(Map<Integer, Map<Integer, int[]>> src) {
      lu_edges = src;
    }
    public void registerUsers(Map<Integer, int[]> src) {
      lu_users = src;
    }
    public int computeHaversine(double lng1, double lat1, double lng2, double lat2) {
      double dlat = Math.toRadians(lat2 - lat1);
      double dlng = Math.toRadians(lng2 - lng1);
      double rlat1 = Math.toRadians(lat1);
      double rlat2 = Math.toRadians(lat2);
      double a = Math.pow(Math.sin(dlat / 2), 2)
        + Math.pow(Math.sin(dlng / 2), 2)
        * Math.cos(rlat1) * Math.cos(rlat2);
      double c = 2 * Math.asin(Math.sqrt(a));
      int d = (int) Math.round(c * 6371000);
      if (d == 0 && (lng1 != lng2 || lat1 != lat2)) {
        d = 1;
      }
      return d;
    }
    public int computeHaversine(int u, int v) {
      return computeHaversine(
        lu_vertices.get(u)[0]/CSHIFT, lu_vertices.get(u)[1]/CSHIFT,
        lu_vertices.get(v)[0]/CSHIFT, lu_vertices.get(v)[1]/CSHIFT);
    }
    public int[] computeShortestPath(int u, int v) {
      int[] output = null;
      if (!flag_gtree_loaded) {
        throw new RuntimeException("GTree not loaded!");
      } else if (u == 0) {
        throw new RuntimeException(
            "Attempted to find shortest path originating from dummy vertex!");
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
    public int computeShortestPathDistance(int u, int v) {
      int d = 0;
      if (!flag_gtree_loaded) {
        throw new RuntimeException("GTree not loaded!");
      } else if (u == 0) {
        throw new RuntimeException(
            "Attempted to find shortest distance originating from dummy vertex!");
      } else if (u != v && v != 0) {
        d = gtree.search((u - 1), (v - 1));
      }
      return d;
    }
    public int[] filterByHaversine(int ro, int[] locs, int threshold) {
      int n = (locs.length/3);
      int[] temp = new int[n];
      int i = 0;
      for (int k = 0; k < n; k++) {
        if (computeHaversine(ro, locs[((3*k) + 2)]) < threshold) {
          temp[i++] = 3*k;
        }
      }
      return Arrays.copyOf(temp, i);
    }
    public void printUser(int[] u) {
      System.out.println("User {uid="+u[0]+", q="+u[1]+", e="+u[2]+", l="+u[3]
        +", o="+u[4]+", d="+u[5]+", b="+u[6]+"}");
    }
    public void printPath(int[] p) {
      for (Integer i : p) {
        System.out.print(i+" ");
      }
      System.out.println();
    }
    public void printRoute(int[] w) {
      for (int i = 0; i < (w.length - 1); i += 2) {
        System.out.print("("+w[i]+", "+w[(i + 1)]+") ");
      }
      System.out.println();
    }
    public void printSchedule(int[] b) {
      for (int i = 0; i < (b.length - 3); i += 4) {
        System.out.print("("+b[i]+", "+b[(i + 1)]
          + ", "+b[(i + 2)]+", "+b[(i + 3)]+") ");
      }
      System.out.println();
    }
}
