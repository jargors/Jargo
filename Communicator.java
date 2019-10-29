package com.github.jargors;
import com.github.jargors.Storage;
import java.util.function.Supplier;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
public class Communicator {
  private Storage storage;
  private int world_time = 0;
  private boolean DEBUG = false;
  public Communicator() { }
  public void setDebug(boolean flag) {
    DEBUG = flag;
  }
  public void setStorage(Storage src) {
    storage = src;
  }
  public void setSimulationWorldTime(int t) {
    world_time = t;
  }
  public int getSimulationWorldTime() {
    return world_time;
  }
  public final Map<Integer, int[]> getReferenceVerticesCache() {
    return storage.getReferenceVerticesCache();
  }
  public final Map<Integer, Map<Integer, int[]>> getReferenceEdgesCache() {
    return storage.getReferenceEdgesCache();
  }
  public final Map<Integer, int[]> getReferenceUsersCache() {
    return storage.getReferenceUsersCache();
  }
  public boolean updateServerRoute(int sid, int[] route, int[] sched) {
    boolean success = false;
    if (route[0] >= world_time) {
      storage.DBUpdateServerRoute(sid, route, sched);
      success = true;
    }
    return success;
  }
  public boolean updateServerAddToSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    final int t = world_time;
    final int[] current = storage.DBQueryServerRoute(sid);
    int i = 0;
    Print("Set i=0");
    Print("Set current <length>="+current.length);
    for (int q = 0; q < (current.length - 1); q += 2) {
      System.out.print("("+current[q]+", "+current[(q + 1)]+") ");
    }
    System.out.println();
    while (i < current.length && current[i] != route[0]) {
      Print("Detected current["+i+"]!=route[0] ("+current[i]+"!="+route[0]+")");
      i += 2;
    }
    Print("Got i="+i);
    if (i == current.length) {
      // branch point not found
      Print("Branch point not found");
      return false;
    } else {
      Print("Found branch point at i="+i);
    }
    int j = 0;
    while (j < route.length && route[j] <= t) {
      if (current[i] != route[j] || current[(i + 1)] != route[(j + 1)]) {
        // overwrite history occurred
        Print("Overwrite history detected, i="+i+", j="+j+" ("+current[i]+"!="+route[j]+", "
          +current[(i + 1)]+"!="+route[(j + 1)]+")");
        return false;
      } else {
        Print("Detected matching history, i="+i+", j="+j+" ("+current[i]+"="+route[j]+", "
          +current[(i + 1)]+"="+route[(j + 1)]+")");
      }
      i += 2;
      j += 2;
    }
    storage.DBUpdateServerAddToSchedule(sid, route, sched, rid);
    return true;
  }
  public boolean updateServerRemoveFromSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    boolean success = false;
    if (route[0] >= world_time) {
      storage.DBUpdateServerRemoveFromSchedule(sid, route, sched, rid);
    }
    return success;
  }
  public int[] queryVertex(int v) throws RuntimeException {
    return storage.DBQueryVertex(v);
  }
  public int[] queryEdge(int v1, int v2) throws RuntimeException {
    return storage.DBQueryEdge(v1, v2);
  }
  public int[] queryUser(int uid) throws RuntimeException {
    return storage.DBQueryUser(uid);
  }
  public int[] queryServerLocationsActive(int t) throws RuntimeException {
    return storage.DBQueryServerLocationsActive(t);
  }
  public int[] queryServerRemainingRoute(int sid, int t) throws RuntimeException {
    return storage.DBQueryServerRemainingRoute(sid, t);
  }
  public int[] queryServerRemainingSchedule(int sid, int t) throws RuntimeException {
    return storage.DBQueryServerRemainingSchedule(sid, t);
  }
  public int[] queryServerRemainingDistance(int sid, int t) throws RuntimeException {
    return storage.DBQueryServerRemainingDistance(sid, t);
  }
  public int[] queryServerRemainingDuration(int sid, int t) throws RuntimeException {
    return storage.DBQueryServerRemainingDuration(sid, t);
  }
  public int[] queryServerMaxLoad(int sid, int t) throws RuntimeException {
    return storage.DBQueryServerMaxLoad(sid, t);
  }
  private void Print(String msg) {
    if (DEBUG) {
      System.out.println("[Jargo][Communicator]["+LocalDateTime.now()+"] "+msg);
    }
  }
}
