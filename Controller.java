package com.github.jargors;
import com.github.jargors.Storage;
import com.github.jargors.Communicator;
import com.github.jargors.Client;
import com.github.jargors.Tools;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
public class Controller {
  private Storage storage;
  private Communicator communicator;
  private Tools tools = new Tools();
  private Client client;
  private Map<Integer, int[]> lu_vertices = new HashMap<>();
  private Map<int[], int[]> lu_edges = new HashMap<>();
  private final double CSHIFT = 10000000.0;
  private static int world_time = 0;
  private int initial_world_time = 0;
  private int final_world_time = 86400;
  private int engine_update_period = 10;
  private int loop_delay = 0;
  // private int deviation_rate = 0.02;
  // private int breakdown_rate = 0.005;
  private Runnable ClockLoop = new Runnable() {
    public void run() {
      communicator.setSimulationWorldTime(++world_time);
      storage.printSQLDriverStatistics();
    }
  };
  private Runnable EngineLoop = new Runnable() {
    public void run() {
      // Print("I am EngineLoop running on "+Thread.currentThread().getName());

      /* Here is where traffic, route deviations, and breakdowns happen.
       * Traffic is the most important one because it is most prevalent in the
       * real world. Deviations and breakdowns have less chance to occur, so
       * their impacts on ridesharing algorithm quality are probably small.
       */

      // applyTraffic(world_time);

      /* The method applyTraffic(1) works by changing the timing of the waypoints
       * in the affected routes to simulate slowdowns and speedups due to
       * traffic. Naive algorithm:
       *   1. load the traffic profile (list of all edges with their flow speeds)
       *   2. servers <- storage.DBQueryActiveServerLocations(world_time)
       *   3. for each server:
       *   4.   route <- storage.DBQueryRemainingRoute(server id)
       *   5.   for each edge in route:
       *   6.     if edge speed exceeds new flow speed,
       *   7.       change waypoint time so speed is less than flow speed
       *   8.     optionally, if speed is below flow speed,
       *   9.       change waypoint time so speed is at flow speed
       *  10.   storage.DBUpdateServerRoute(server id, new route, new sched)
       */

      // applyDeviation(world_time);
      // applyBreakdown(world_time);
    }
  };
  private Runnable RequestLoop = () -> {
    final int t0 = world_time;
    int[] output = storage.DBQueryQueuedRequests(world_time);
    final int t1 = world_time;
    Print("RequestLoop t0="+t0+", t1="+t1+", # of requests="+output.length/7);
    client.collectRequests(output);
  };
  private Runnable ServerLoop = () -> {
    final int t0 = world_time;
    int[] output = storage.DBQueryServerLocationsActive(world_time);
    final int t1 = world_time;
    Print("ServerLoop t0="+t0+", t1="+t1+", # of servers="+output.length/3);
    client.collectServerLocations(output);
  };
  public Controller() {
    storage = new Storage();
    communicator = new Communicator();
    communicator.setStorage(storage);
  }
  public void setClient(Client target) {
    client = target;
  }
  public void setInitialWorldTime(int t) {
    initial_world_time = t;
  }
  public void setFinalWorldTime(int t) {
    final_world_time = t;
  }
  public static int getSimulationWorldTime() {
    return world_time;
  }
  public final Map<Integer, int[]> getVerticesMap() {
    return lu_vertices;
  }
  public final Map<int[], int[]> getEdgesMap() {
    return lu_edges;
  }
  public void saveBackup(String p) {
    storage.DBSaveBackup(p);
  }
  public void loadBackup(String p) {
    storage.DBLoadBackup(p);
  }
  public void loadDataModel() throws RuntimeException {
    storage.DBLoadDataModel();
  }
  public void loadRoadNetwork(String f_rnet) throws RuntimeException {
    Print("Load road network ("+f_rnet+")");
    try {
      int[] col = new int[7];
      int dist;
      Scanner sc = new Scanner(new File(f_rnet));
      while (sc.hasNext()) {
        col[0] = sc.nextInt();
        col[1] = sc.nextInt();
        col[2] = sc.nextInt();
        col[3] = (int) Math.round(sc.nextDouble()*CSHIFT);
        col[4] = (int) Math.round(sc.nextDouble()*CSHIFT);
        col[5] = (int) Math.round(sc.nextDouble()*CSHIFT);
        col[6] = (int) Math.round(sc.nextDouble()*CSHIFT);
        if (col[1] == 0) {
          col[3] = 0;
          col[4] = 0;
        }
        if (col[2] == 0) {
          col[5] = 0;
          col[6] = 0;
        }
        if (!lu_vertices.containsKey(col[1])) {
          lu_vertices.put(col[1], new int[] { col[3], col[4] });
          storage.DBAddNewVertex(col[1], col[3], col[4]);
        }
        if (!lu_vertices.containsKey(col[2])) {
          lu_vertices.put(col[2], new int[] { col[5], col[6] });
          storage.DBAddNewVertex(col[2], col[5], col[6]);
        }
        dist = ((col[1] != 0 && col[2] != 0)
          ? tools.computeHaversine(
                col[3]/CSHIFT, col[4]/CSHIFT,
                col[5]/CSHIFT, col[6]/CSHIFT) : 0);
        storage.DBAddNewEdge(col[1], col[2], dist, 10);
        lu_edges.put(new int[] { col[1], col[2] }, new int[] { dist, 10 });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public void loadProblem(String p) {
    Print("Load problem ("+p+")");
    try {
      Scanner sc = new Scanner(new File(p));
      for (int i = 0; i < 6; i++) {
        sc.nextLine();
      }
      int[] col = new int[6];
      while (sc.hasNext()) {
        for (int i = 0; i < 6; i++) {
          col[i] = sc.nextInt();
        }
        int uid = col[0];
        int uo = col[1];
        int ud = col[2];
        int uq = col[3];
        int ue = col[4];
        int ul = col[5];
        int ub = tools.computeShortestPathDistance(uo, ud);
        if (uq < 0) {
          int[] server = new int[] { uid, uq, ue, ul, uo, ud, ub };
          int[] route = computeRoute(uo, ud, ue);
          storage.DBAddNewServer(server, route);
          Print("Put user "+uid);
        } else {
          int[] request = new int[] { uid, uq, ue, ul, uo, ud, ub };
          storage.DBAddNewRequest(request);
          Print("Put user "+uid);
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Bad path to problem instance");
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      System.out.println("Jargo runtime exception");
    }
  }
  public void loadGTree(String p) {
    tools.loadGTree(p);
  }
  public void start() {
    Print("SIMULATION STARTED");

    client.setCommunicator(communicator);

    world_time = initial_world_time;
    Print("Set world time to "+world_time);

    int simulation_duration = (final_world_time - initial_world_time);
    Print("Set world duration to "+simulation_duration+" (sec)");

    ScheduledExecutorService exe = Executors.newScheduledThreadPool(4);

    ScheduledFuture<?> cb1 = exe.scheduleAtFixedRate(
      ClockLoop, 0, 1, TimeUnit.SECONDS);
    Print("Set clock-loop period to 1 (sec)");

    ScheduledFuture<?> cb2 = exe.scheduleAtFixedRate(
      EngineLoop, loop_delay, engine_update_period, TimeUnit.SECONDS);
    Print("Set engine-loop period to "+engine_update_period+" (sec)");

    int request_collection_period = client.getRequestCollectionPeriod();
    ScheduledFuture<?> cb3 = exe.scheduleAtFixedRate(
      RequestLoop, loop_delay, request_collection_period, TimeUnit.SECONDS);
    Print("Set request-loop period to "+request_collection_period+" (sec)");

    int server_collection_period = client.getServerLocationCollectionPeriod();
    ScheduledFuture<?> cb4 = exe.scheduleAtFixedRate(
      ServerLoop, loop_delay, server_collection_period, TimeUnit.SECONDS);
    Print("Set server-loop period to "+server_collection_period+" (sec)");

    exe.schedule(() -> {
      cb1.cancel(false);
      cb2.cancel(false);
      cb3.cancel(false);
      cb4.cancel(false);
      exe.shutdown();
      Print("SIMULATION ENDED");
    }, simulation_duration, TimeUnit.SECONDS);
  }
  public void startStatic() {
    Print("SIMULATION STARTED -- STATIC MODE");

    client.setCommunicator(communicator);

    world_time = initial_world_time;
    Print("Set world time to "+world_time);
    Print("Set final world time to "+final_world_time+" (sec)");

    while (world_time < final_world_time) {
      ClockLoop.run();
      EngineLoop.run();
      ServerLoop.run();
      RequestLoop.run();
    }

    Print("SIMULATION ENDED");
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
    System.out.println("[Jargo][Controller]["+LocalDateTime.now()+"]"
      + "[t="+world_time+"] "+msg);
  }
  private int[] computeRoute(int source, int target, int starttime)
  throws RuntimeException {
    int[] output = null;
    if (source == 0) {
      throw new RuntimeException("ERROR computeRoute(3): source cannot be 0!");
    } else if (target == 0) {
      output = new int[] { starttime, source, starttime + 1, target };
    } else {
      int[] path = null;
      int[] edge = null;
      int u, v, dd, nu, t;
      path = tools.computeShortestPath(source, target);
      if (path == null) {
        throw new RuntimeException("ERROR computeRoute(3): null path!");
      } else {
        output = new int[(path.length*2)];
        output[0] = starttime;
        output[1] = source;
        for (int i = 1; i < path.length; i++) {
          u = path[(i - 1)] + 1;
          v = path[i] + 1;
          edge = storage.DBQueryEdge(u, v);
          dd = edge[0];
          nu = edge[1];
          t = (int) Math.ceil((float) dd/nu);
          output[(2*i + 0)] = t;
          output[(2*i + 1)] = v;
        }
      }
    }
    return output;
  }
}
