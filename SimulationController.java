  package com.github.jargors;
  import com.github.jargors.StorageInterface;
  import com.github.jargors.SimulationInterface;
  import com.github.jargors.JargoClient;
  import java.time.LocalDateTime;
  import java.util.concurrent.Executors;
  import java.util.concurrent.ScheduledExecutorService;
  import java.util.concurrent.ScheduledFuture;
  import java.util.concurrent.TimeUnit;
  import java.util.concurrent.locks.ReentrantLock;
  public class SimulationController {
    private StorageInterface storage;
    private SimulationInterface simulator;
    private JargoClient client;
    private String f_rnet = "";
    private String f_prob = "";
    private String f_backup = "";
    private static int world_time = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private Runnable ClockLoop = new Runnable() {
      public void run() {
        Print("I am ClockLoop running on "+Thread.currentThread().getName());
        world_time++;
      }
    };
    private Runnable EngineLoop = new Runnable() {
      public void run() {
        Print("I am EngineLoop running on "+Thread.currentThread().getName());
      }
    };
    private Runnable RequestLoop = new Runnable() {
      public void run() {
        lock.lock();
        try {
          Print("I am RequestLoop running on "+Thread.currentThread().getName());
          client.collectRequests(storage.DBQueryQueuedRequests(world_time));
        } finally {
          lock.unlock();
        }
      }
    };
    private Runnable ServerLoop = new Runnable() {
      public void run() {
        lock.lock();
        try {
          Print("I am ServerLoop running on "+Thread.currentThread().getName());
          client.collectServerLocations(storage.DBQueryServerLocationsActive(world_time));
        } finally {
          lock.unlock();
        }
      }
    };
    public SimulationController(JargoClient target) {
      client = target;
    }
    public void init() {
      storage = new StorageInterface(f_rnet);
      if (f_backup.length() > 0) {
        storage.DBLoadBackup(f_backup);
      }
      simulator = new SimulationInterface(storage);
    }
    public void setRoadNetwork(String f) {
      f_rnet = f;
    }
    public void setProblemInstance(String f) {
      f_prob = f;
    }
    public void setRestoreFrom(String f) {
      f_backup = f;
    }
    public static int getSimulationWorldTime() {
      return world_time;
    }
    public void start() {
      Print("START");
      int delay = 0;
      int update_period = 10;

      ScheduledExecutorService exe = Executors.newScheduledThreadPool(4);

      ScheduledFuture<?> cb1 = exe.scheduleAtFixedRate(
        ClockLoop, 0, 1, TimeUnit.SECONDS);

      ScheduledFuture<?> cb2 = exe.scheduleAtFixedRate(
        EngineLoop, delay, update_period, TimeUnit.SECONDS);

      int request_collection_period = client.getRequestCollectionPeriod();
      ScheduledFuture<?> cb3 = exe.scheduleAtFixedRate(
        RequestLoop, delay, request_collection_period, TimeUnit.SECONDS);

      int server_collection_period = client.getServerLocationCollectionPeriod();
      ScheduledFuture<?> cb4 = exe.scheduleAtFixedRate(
        ServerLoop, delay, server_collection_period, TimeUnit.SECONDS);

      exe.schedule(new Runnable() {
        public void run() {
          cb1.cancel(false);
          cb2.cancel(false);
          cb3.cancel(false);
          cb4.cancel(false);
          exe.shutdown();
          Print("END");
        }}, 20, TimeUnit.SECONDS);
    }
    public int[] query(String sql, int ncols) throws RuntimeException {
      return storage.DBQuery(sql, ncols);
    }
    public int[] queryServer(int sid) throws RuntimeException {
      return storage.DBQueryServer(sid);
    }
    public int[] queryRequest(int rid) throws RuntimeException {
      return storage.DBQueryRequest(rid);
    }
    public int[] queryRoute(int sid) throws RuntimeException {
      return storage.DBQueryRoute(sid);
    }
    public int[] querySchedule(int sid) throws RuntimeException {
      return storage.DBQuerySchedule(sid);
    }
    public int[] queryCountVertices() throws RuntimeException {
      return storage.DBQueryCountVertices();
    }
    public int[] queryCountEdges() throws RuntimeException {
      return storage.DBQueryCountEdges();
    }
    public int[] queryStatisticsEdges() throws RuntimeException {
      return storage.DBQueryStatisticsEdges();
    }
    public int[] queryMBR() throws RuntimeException {
      return storage.DBQueryMBR();
    }
    public int[] queryCountServers() throws RuntimeException {
      return storage.DBQueryCountServers();
    }
    public int[] queryCountRequests() throws RuntimeException {
      return storage.DBQueryCountRequests();
    }
    public int[] queryServiceRate() throws RuntimeException {
      return storage.DBQueryServiceRate();
    }
    public int[] queryBaseDistanceTotal() throws RuntimeException {
      return storage.DBQueryBaseDistanceTotal();
    }
    public int[] queryServerBaseDistanceTotal() throws RuntimeException {
      return storage.DBQueryServerBaseDistanceTotal();
    }
    public int[] queryRequestBaseDistanceTotal() throws RuntimeException {
      return storage.DBQueryRequestBaseDistanceTotal();
    }
    public int[] queryServerTravelDistanceTotal() throws RuntimeException {
      return storage.DBQueryServerTravelDistanceTotal();
    }
    public int[] queryServerCruisingDistanceTotal() throws RuntimeException {
      return storage.DBQueryServerCruisingDistanceTotal();
    }
    public int[] queryServerServiceDistanceTotal() throws RuntimeException {
      return storage.DBQueryServerServiceDistanceTotal();
    }
    public int[] queryRequestDetourDistanceTotal() throws RuntimeException {
      return storage.DBQueryRequestDetourDistanceTotal();
    }
    public int[] queryRequestTransitDistanceTotal() throws RuntimeException {
      return storage.DBQueryRequestTransitDistanceTotal();
    }
    public int[] queryServerTravelDurationTotal() throws RuntimeException {
      return storage.DBQueryServerTravelDurationTotal();
    }
    public int[] queryRequestPickupDurationTotal() throws RuntimeException {
      return storage.DBQueryRequestPickupDurationTotal();
    }
    public int[] queryRequestTransitDurationTotal() throws RuntimeException {
      return storage.DBQueryRequestTransitDurationTotal();
    }
    public int[] queryRequestTravelDurationTotal() throws RuntimeException {
      return storage.DBQueryRequestTravelDurationTotal();
    }
    public int[] queryRequestDepartureTime(int rid) throws RuntimeException {
      return storage.DBQueryRequestDepartureTime(rid);
    }
    public int[] queryServerDepartureTime(int sid) throws RuntimeException {
      return storage.DBQueryServerDepartureTime(sid);
    }
    public int[] queryRequestArrivalTime(int rid) throws RuntimeException {
      return storage.DBQueryRequestArrivalTime(rid);
    }
    public int[] queryServerArrivalTime(int sid) throws RuntimeException {
      return storage.DBQueryServerArrivalTime(sid);
    }
    private void Print(String msg) {
      System.out.println("[SimulationController]["+LocalDateTime.now()+"]"
        + "[t="+world_time+"] "+msg);
    }
  }
