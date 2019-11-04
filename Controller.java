package com.github.jargors;
import com.github.jargors.Storage;
import com.github.jargors.Communicator;
import com.github.jargors.Client;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
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
import java.sql.SQLException;
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
  private Runnable ClockLoop = () -> {
    communicator.setSimulationWorldTime(++world_time);
    Tools.Print((world_time % 2 == 0 ? "*ping*" : "*pong*"));
  };
  private Runnable EngineLoop = () -> { };
  private Runnable RequestCollectionLoop = () -> {
    long A0 = System.currentTimeMillis();
    final int t0 = world_time;
    try {
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
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    final int t1 = world_time;
    long A1 = System.currentTimeMillis();
    Tools.Print("Time RCL: "+(A1 - A0)+" ms");
  };
  private Runnable RequestHandlingLoop = () -> {
    long A0 = System.currentTimeMillis();
    try {
      client.notifyNew();
    } catch (RuntimeException e) {
      System.out.println("client.notifyNew() exception: "+e.toString());
    }
    long A1 = System.currentTimeMillis();
    Tools.Print("Time RHL: "+(A1 - A0)+" ms");
  };
  private Runnable ServerLoop = () -> {
    long A0 = System.currentTimeMillis();
    try {
      int[] output = storage.DBQueryServerLocationsActive(world_time);
      client.collectServerLocations(output);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    long A1 = System.currentTimeMillis();
    Tools.Print("Time SL: "+(A1 - A0)+" ms");
  };
  public Controller() {
    storage = new Storage();
    communicator = new Communicator();
    communicator.setStorage(storage);
    communicator.setController(this);
  }
  public void createNewInstance() {
    try {
      storage.DBCreateNewInstance();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
  }
  public void closeInstance() {
    try {
      storage.DBCloseInstance();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
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
    try {
      storage.DBSaveBackup(p);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
  }
  public void loadBackup(String p) {
    try {
      storage.DBLoadBackup(p);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
  }
  public void loadDataModel() {
    storage.DBLoadDataModel();
  }
  public void loadRoadNetwork(final String f_rnet) {
    Tools.Print("Load road network ("+f_rnet+")");
    try {
      Scanner sc = new Scanner(new File(f_rnet));
      while (sc.hasNext()) {
        int[] col = new int[7];
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
        try {
          storage.DBAddNewVertex(col[1], col[3], col[4]);
        } catch (DuplicateVertexException e) {
          // System.err.println("Warning: duplicate vertex rejected");
          // System.err.println(e.toString());
        }
        try {
          storage.DBAddNewVertex(col[2], col[5], col[6]);
        } catch (DuplicateVertexException e) {
          // System.err.println("Warning: duplicate vertex rejected");
          // System.err.println(e.toString());
        }
        int dist = ((col[1] != 0 && col[2] != 0)
          ? tools.computeHaversine(
                col[3]/CSHIFT, col[4]/CSHIFT,
                col[5]/CSHIFT, col[6]/CSHIFT) : 0);
        try {
          storage.DBAddNewEdge(col[1], col[2], dist, 10);
        } catch (DuplicateEdgeException e) {
          // System.err.println("Warning: duplicate edge rejected");
          // System.err.println(e.toString());
        }
      }
    } catch (FileNotFoundException e) {
      System.err.println("Encountered fatal error");
      System.err.println(e.toString());
      e.printStackTrace();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    tools.registerVertices(storage.getReferenceVerticesCache());
    tools.registerEdges(storage.getReferenceEdgesCache());
  }
  public void loadProblem(String p) {
    Tools.Print("Load problem ("+p+")");
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
        } else {
          addNewRequest(new int[] { uid, uq, ue, ul, uo, ud, ub });
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
    Tools.Print("SIMULATION STARTED");

    world_time = initial_world_time;
    Tools.Print("Set world time to "+world_time);

    int simulation_duration = (final_world_time - initial_world_time);
    Tools.Print("Set world duration to "+simulation_duration+" (sec)");

    ScheduledExecutorService exe = Executors.newScheduledThreadPool(5);

    ScheduledFuture<?> cb1 = exe.scheduleAtFixedRate(
      ClockLoop, 0, 1, TimeUnit.SECONDS);
    Tools.Print("Set clock-loop period to 1 (sec)");

    ScheduledFuture<?> cb2 = exe.scheduleAtFixedRate(
      EngineLoop, loop_delay, engine_update_period, TimeUnit.SECONDS);
    Tools.Print("Set engine-loop period to "+engine_update_period+" (sec)");

    int request_collection_period = client.getRequestCollectionPeriod();
    ScheduledFuture<?> cb3 = exe.scheduleAtFixedRate(
      RequestCollectionLoop, loop_delay, request_collection_period, TimeUnit.SECONDS);
    Tools.Print("Set request-collection-loop period to "+request_collection_period+" (sec)");

    int request_handling_period = client.getRequestHandlingPeriod();
    ScheduledFuture<?> cb4 = exe.scheduleAtFixedRate(
      RequestHandlingLoop, loop_delay, request_handling_period, TimeUnit.MILLISECONDS);
    Tools.Print("Set request-handling-loop period to "+request_handling_period+" (msec)");

    int server_collection_period = client.getServerLocationCollectionPeriod();
    ScheduledFuture<?> cb5 = exe.scheduleAtFixedRate(
      ServerLoop, loop_delay, server_collection_period, TimeUnit.SECONDS);
    Tools.Print("Set server-loop period to "+server_collection_period+" (sec)");

    exe.schedule(() -> {
      cb1.cancel(false);
      cb2.cancel(false);
      cb3.cancel(false);
      cb4.cancel(false);
      cb5.cancel(false);
      exe.shutdown();
      client.end();
      Tools.Print("SIMULATION ENDED");
      app_cb.accept(true);
    }, simulation_duration, TimeUnit.SECONDS);
  }
  public void startStatic() {
    Tools.Print("SIMULATION STARTED -- STATIC MODE");

    world_time = initial_world_time;
    Tools.Print("Set world time to "+world_time);
    Tools.Print("Set final world time to "+final_world_time+" (sec)");

    while (world_time < final_world_time) {
      ClockLoop.run();
      EngineLoop.run();
      ServerLoop.run();
      RequestCollectionLoop.run();
      RequestHandlingLoop.run();
    }

    client.end();

    Tools.Print("SIMULATION ENDED");
  }
  public int[] query(String sql, int ncols) {
    int[] output = new int[] { };
    try {
      output = storage.DBQuery(sql, ncols);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryUser(int rid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryUser(rid);
    } catch (UserNotFoundException e) {
      System.err.println("Warning: user not found");
      System.err.println(e.toString());
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryQueuedRequests(int t) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryQueuedRequests(t);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRoute(int sid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerRoute(sid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] querySchedule(int sid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerSchedule(sid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryCountVertices() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryCountVertices();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryCountEdges() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryCountEdges();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryStatisticsEdges() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryStatisticsEdges();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryMBR() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryMBR();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryCountServers() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryCountServers();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryCountRequests() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryCountRequests();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServiceRate() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServiceRate();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryBaseDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryBaseDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerBaseDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerBaseDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestBaseDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestBaseDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestBaseDistanceUnassigned() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestBaseDistanceUnassigned();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerTravelDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerTravelDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerCruisingDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerCruisingDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerServiceDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerServiceDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestDetourDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestDetourDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestTransitDistanceTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestTransitDistanceTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerTravelDurationTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerTravelDurationTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestPickupDurationTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestPickupDurationTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestTransitDurationTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestTransitDurationTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestTravelDurationTotal() {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestTravelDurationTotal();
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestDepartureTime(int rid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestDepartureTime(rid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerDepartureTime(int sid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerDepartureTime(sid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryRequestArrivalTime(int rid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryRequestArrivalTime(rid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public int[] queryServerArrivalTime(int sid) {
    int[] output = new int[] { };
    try {
      output = storage.DBQueryServerArrivalTime(sid);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
    return output;
  }
  public void addNewServer(int[] u) {
    try {
      storage.DBAddNewServer(u, tools.computeRoute(u[4], u[5], u[2]));
    } catch (DuplicateUserException e) {
      System.err.println("Warning: duplicate user rejected");
      System.err.println(e.toString());
    } catch (EdgeNotFoundException e) {
      System.err.println("Warning: malformed route rejected");
      System.err.println(e.toString());
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
  }
  public void addNewRequest(int[] u) {
    try {
      storage.DBAddNewRequest(u);
    } catch (DuplicateUserException e) {
      System.err.println("Warning: duplicate user rejected");
      System.err.println(e.toString());
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      Tools.PrintSQLException(e);
      System.exit(1);
    }
  }
}
