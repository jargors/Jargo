package com.github.jargors;
import com.github.jargors.gtreeJNI.*;
public class Tools {
  private G_Tree gtree;
  private boolean flag_loaded = false;
  public Tools() { }
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
        flag_loaded = true;
      } else {
        System.out.println("Bad path to gtree");
      }
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
    public int[] computeShortestPath(int u, int v) {
      int[] output = null;
      if (!flag_loaded) {
        throw new RuntimeException("GTree not loaded!");
      } else if (u == 0) {
        throw new RuntimeException(
            "Attempted to find shortest path originating from dummy vertex!");
      } else if (v == 0) {
        output = new int[] { u, v };
      } else if (u == v) {
        output = new int[] { u };
      } else {
        IntVector path = null;
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
      if (!flag_loaded) {
        throw new RuntimeException("GTree not loaded!");
      } else if (u == 0) {
        throw new RuntimeException(
            "Attempted to find shortest distance originating from dummy vertex!");
      } else if (u != v && v != 0) {
        d = gtree.search((u - 1), (v - 1));
      }
      return d;
    }
}
