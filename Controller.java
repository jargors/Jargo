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
import java.util.function.Consumer;
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
  private Map<Integer, Boolean> lu_seen = new HashMap<>();
  private final double CSHIFT = 10000000.0;
  private static int world_time = 0;
  private int initial_world_time = 0;
  private int final_world_time = 86400;
  private int engine_update_period = 10;
  private int loop_delay = 0;
  // private int deviation_rate = 0.02;
  // private int breakdown_rate = 0.005;
  private boolean DEBUG = false;
  private Runnable ClockLoop = () -> {
    communicator.setSimulationWorldTime(++world_time);
    storage.printSQLDriverStatistics();
    Print((world_time % 2 == 0 ? "*ping*" : "*pong*"));
  };
  private Runnable EngineLoop = () -> { };
  private Runnable RequestCollectionLoop = () -> {
    long A0 = System.currentTimeMillis();
    final int t0 = world_time;
    int[] output = storage.DBQueryQueuedRequests(world_time);
    for (int i = 0; i < (output.length - 6); i += 7) {
      if (!lu_seen.containsKey(output[i]) || lu_seen.get(output[i]) == false) {
        client.collectRequest(new int[] {
          output[(i + 0)],
          output[(i + 1)],
          output[(i + 2)],
          output[(i + 3)],
          output[(i + 4)],
          output[(i + 5)],
          output[(i + 6)] });
        lu_seen.put(output[i], true);
      }
    }
    final int t1 = world_time;
    long A1 = System.currentTimeMillis();
    Print("Time RCL: "+(A1 - A0)+" ms");
  };
  private Runnable RequestHandlingLoop = () -> {
    long A0 = System.currentTimeMillis();
    try {
      client.notifyNew();
    } catch (RuntimeException e) {
      System.out.println("client.notifyNew() exception: "+e.toString());
    }
    long A1 = System.currentTimeMillis();
    Print("Time RHL: "+(A1 - A0)+" ms");
  };
  private Runnable ServerLoop = () -> {
    long A0 = System.currentTimeMillis();
    final int t0 = world_time;
    int[] output = storage.DBQueryServerLocationsActive(world_time);
    final int t1 = world_time;
    long A1 = System.currentTimeMillis();
    Print("Time SL: "+(A1 - A0)+" ms");
    Print("ServerLoop t0="+t0+", t1="+t1+", # of servers="+output.length/3);
    client.collectServerLocations(output);
  };
  public Controller() {
    storage = new Storage();
    communicator = new Communicator();
    communicator.setStorage(storage);
    communicator.setController(this);
  }
  public void setDebug(boolean flag) {
    DEBUG = flag;
  }
  public void setDebugStorage(boolean flag) {
    storage.setDebug(flag);
  }
  public void setDebugClient(boolean flag) {
    client.setDebug(flag);
  }
  public void setDebugCommunicator(boolean flag) {
    communicator.setDebug(flag);
  }
  public void setClient(Client target) {
    client = target;
    client.setCommunicator(communicator);
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
  public void returnRequest(int rid) {
    lu_seen.put(rid, false);
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
        storage.DBAddNewVertex(col[1], col[3], col[4]);
        storage.DBAddNewVertex(col[2], col[5], col[6]);
        dist = ((col[1] != 0 && col[2] != 0)
          ? tools.computeHaversine(
                col[3]/CSHIFT, col[4]/CSHIFT,
                col[5]/CSHIFT, col[6]/CSHIFT) : 0);
        storage.DBAddNewEdge(col[1], col[2], dist, 10);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    tools.registerVertices(storage.getReferenceVerticesCache());
    tools.registerEdges(storage.getReferenceEdgesCache());
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
          addNewServer(new int[] { uid, uq, ue, ul, uo, ud, ub });
          Print("Put server "+uid);
        } else {
          addNewRequest(new int[] { uid, uq, ue, ul, uo, ud, ub });
          Print("Put request "+uid);
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
  public void start(Consumer app_cb) {
    Print("SIMULATION STARTED");

    world_time = initial_world_time;
    Print("Set world time to "+world_time);

    int simulation_duration = (final_world_time - initial_world_time);
    Print("Set world duration to "+simulation_duration+" (sec)");

    ScheduledExecutorService exe = Executors.newScheduledThreadPool(5);

    ScheduledFuture<?> cb1 = exe.scheduleAtFixedRate(
      ClockLoop, 0, 1, TimeUnit.SECONDS);
    Print("Set clock-loop period to 1 (sec)");

    ScheduledFuture<?> cb2 = exe.scheduleAtFixedRate(
      EngineLoop, loop_delay, engine_update_period, TimeUnit.SECONDS);
    Print("Set engine-loop period to "+engine_update_period+" (sec)");

    int request_collection_period = client.getRequestCollectionPeriod();
    ScheduledFuture<?> cb3 = exe.scheduleAtFixedRate(
      RequestCollectionLoop, loop_delay, request_collection_period, TimeUnit.SECONDS);
    Print("Set request-collection-loop period to "+request_collection_period+" (sec)");

    int request_handling_period = client.getRequestHandlingPeriod();
    ScheduledFuture<?> cb4 = exe.scheduleAtFixedRate(
      RequestHandlingLoop, loop_delay, request_handling_period, TimeUnit.MILLISECONDS);
    Print("Set request-handling-loop period to "+request_handling_period+" (msec)");

    int server_collection_period = client.getServerLocationCollectionPeriod();
    ScheduledFuture<?> cb5 = exe.scheduleAtFixedRate(
      ServerLoop, loop_delay, server_collection_period, TimeUnit.SECONDS);
    Print("Set server-loop period to "+server_collection_period+" (sec)");

    exe.schedule(() -> {
      cb1.cancel(false);
      cb2.cancel(false);
      cb3.cancel(false);
      cb4.cancel(false);
      cb5.cancel(false);
      exe.shutdown();
      client.end();
      Print("SIMULATION ENDED");
      app_cb.accept(true);
    }, simulation_duration, TimeUnit.SECONDS);
  }
  public void startStatic() {
    Print("SIMULATION STARTED -- STATIC MODE");

    world_time = initial_world_time;
    Print("Set world time to "+world_time);
    Print("Set final world time to "+final_world_time+" (sec)");

    while (world_time < final_world_time) {
      ClockLoop.run();
      EngineLoop.run();
      ServerLoop.run();
      RequestCollectionLoop.run();
      RequestHandlingLoop.run();
    }

    client.end();

    Print("SIMULATION ENDED");
  }
  public int[] query(String sql, int ncols) throws RuntimeException {
    return storage.DBQuery(sql, ncols);
  }
  public int[] queryUser(int rid) throws RuntimeException {
    return storage.DBQueryUser(rid);
  }
  public int[] queryQueuedRequests(int t) throws RuntimeException {
    return storage.DBQueryQueuedRequests(t);
  }
  public int[] queryRoute(int sid) throws RuntimeException {
    return storage.DBQueryServerRoute(sid);
  }
  public int[] querySchedule(int sid) throws RuntimeException {
    return storage.DBQueryServerSchedule(sid);
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
  public int[] queryRequestBaseDistanceUnassigned() throws RuntimeException {
    return storage.DBQueryRequestBaseDistanceUnassigned();
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
  public void addNewServer(int[] u) {
    storage.DBAddNewServer(u, tools.computeRoute(u[4], u[5], u[2]));
  }
  public void addNewRequest(int[] u) {
    storage.DBAddNewRequest(u);
  }
  private void Print(String msg) {
    if (DEBUG) {
      System.out.println("[Jargo][Controller]["+LocalDateTime.now()+"]"
        + "[t="+world_time+"] "+msg);
    }
  }
}
