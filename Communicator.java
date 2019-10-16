package com.github.jargors;
import com.github.jargors.Storage;
import java.util.function.Supplier;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
public class Communicator {
  private Storage storage;
  private int world_time = 0;
  public Communicator() { }
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
  public void updateServerRoute(int sid, int[] route, int[] sched) {
    storage.DBUpdateServerRoute(sid, route, sched);
  }
  public void updateServerAddToSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    storage.DBUpdateServerAddToSchedule(sid, route, sched, rid);
  }
  public void updateServerRemoveFromSchedule(
      int sid, int[] route, int[] sched, int[] rid) {
    storage.DBUpdateServerRemoveFromSchedule(sid, route, sched, rid);
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
  public int[] queryRouteRemaining(int sid, int t) throws RuntimeException {
    return storage.DBQueryRouteRemaining(sid, t);
  }
  public int[] queryScheduleRemaining(int sid, int t) throws RuntimeException {
    return storage.DBQueryScheduleRemaining(sid, t);
  }
  public int[] queryCurrentLoad(int sid, int t) throws RuntimeException {
    return storage.DBQueryCurrentLoad(sid, t);
  }
  private void Print(String msg) {
    System.out.println("[Jargo][Communicator]["+LocalDateTime.now()+"] "+msg);
  }
}
