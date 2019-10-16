package com.github.jargors;
import com.github.jargors.Storage;
import java.util.function.Supplier;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
public class Communicator {
  private Storage storage;
  private int world_time = 0;
  private boolean ENFORCE_STRICT_ROUTE_UPDATES = false;  // todo
  public Communicator() { }
  public void setStorage(Storage src) {
    storage = src;
  }
  public void setStrictRouteUpdates(boolean flag) {
    ENFORCE_STRICT_ROUTE_UPDATES = flag;
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
    if (!ENFORCE_STRICT_ROUTE_UPDATES || route[0] > world_time) {
      storage.DBUpdateServerRoute(sid, route, sched);
      success = true;
    }
    return success;
  }
  public boolean updateServerAddToSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    boolean success = false;
    if (!ENFORCE_STRICT_ROUTE_UPDATES || route[0] > world_time) {
      storage.DBUpdateServerAddToSchedule(sid, route, sched, rid);
      success = true;
    }
    return success;
  }
  public boolean updateServerRemoveFromSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    boolean success = false;
    if (!ENFORCE_STRICT_ROUTE_UPDATES || route[0] > world_time) {
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
  public int[] queryServer(int sid) throws RuntimeException {
    return storage.DBQueryServer(sid);
  }
  public int[] queryRequest(int rid) throws RuntimeException {
    return storage.DBQueryRequest(rid);
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
    System.out.println("[Jargo][Communicator]["+LocalDateTime.now()+"] "+msg);
  }
}
