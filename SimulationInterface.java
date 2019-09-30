  package com.github.jargors;
  import com.github.jargors.StorageInterface;
  import java.time.LocalDateTime;
  public class SimulationInterface {
    private StorageInterface storage;
    public SimulationInterface() { }
    public void setStorageInterface(StorageInterface src) {
      storage = src;
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
      System.out.println("[SimulationInterface]["+LocalDateTime.now()+"] "+msg);
    }
  }
