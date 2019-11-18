package com.github.jargors;
import com.github.jargors.Storage;
import com.github.jargors.Communicator;
import com.github.jargors.Client;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import com.github.jargors.exceptions.GtreeNotLoadedException;
import com.github.jargors.exceptions.GtreeIllegalSourceException;
import com.github.jargors.exceptions.GtreeIllegalTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
public class Controller {
  private Storage storage;
  private Communicator communicator;
  private Tools tools = new Tools();
  private Client client;
  private Map<Integer, Boolean> lu_seen = new HashMap<Integer, Boolean>();
  private int clockstart =
      Integer.parseInt(System.getProperty("jargors.controller.clockstart", "0"));
  private int clockend =
      Integer.parseInt(System.getProperty("jargors.controller.clockend", "1800"));
  private int REQUEST_TIMEOUT =
      Integer.parseInt(System.getProperty("jargors.controller.request_timeout", "30"));
  private int QUEUE_TIMEOUT =
      Integer.parseInt(System.getProperty("jargors.controller.queue_timeout", "30"));
  private int world_time = 0;
  private int loop_delay = 0;
  private int engine_update_period = 10;
  // private int deviation_rate = 0.02;
  // private int breakdown_rate = 0.005;
  private final double CSHIFT = 10000000.0;
  private final boolean DEBUG =
      "true".equals(System.getProperty("jargors.controller.debug"));
  private Runnable ClockLoop = () -> {
    // TODO: The speed of the updateServer.. methods is about 50ms, meaning we
    // can do ~20 updates per second. If a problem instance has more than 20
    // requests per second and an algo is fast enough to do more than 20 updates
    // per second, the updates will become the bottleneck. It might be unfair
    // to the algo if we advance the clock while waiting for updates to finish.
    // So in this case we only advance the clock after the updates finish.
    // How to implement? We just measure the time it takes to do an update and
    // add that duration onto the clock. We can output a "clock rate" to show
    // the user the current simulation rate, i.e. clock_rate=1x means real-time,
    // clock_rate=0.5x means 1 simulated second takes 2 real seconds, etc.
    ++(this.world_time);
    if (DEBUG) {
      System.err.printf("[t=%d] Controller.ClockLoop says: %s!\n",
          this.world_time, (this.world_time % 2 == 0 ? "ping" : "pong"));
    }
  };
  private Runnable EngineLoop = () -> {
    // Idea: what if we apply traffic effects at the time a server route is
    // submitted? we can do it because we know what the future traffic will
    // be and we also know where the future location will be of the server.
    // then, is this loop necessary?
  };
  private Runnable RequestCollectionLoop = () -> {
    long A0 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
    }
    try {
      int[] output = this.storage.DBQueryQueuedRequests(this.world_time);
      for (int i = 0; i < (output.length - 6); i += 7) {
        if (!this.lu_seen.containsKey(output[i]) || this.lu_seen.get(output[i]) == false) {
          this.client.collectRequest(new int[] {
            output[(i + 0)],
            output[(i + 1)],
            output[(i + 2)],
            output[(i + 3)],
            output[(i + 4)],
            output[(i + 5)],
            output[(i + 6)] });
          this.lu_seen.put(output[i], true);
        }
      }
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      System.err.println(e.toString());
      e.printStackTrace();
      System.exit(1);
    }
    if (DEBUG) {
      System.err.printf("Controller.RequestCollectionLoop completed in %d ms\n",
          (System.currentTimeMillis() - A0));
    }
  };
  private Runnable RequestHandlingLoop = () -> {
    long A0 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
    }
    try {
      this.client.notifyNew();
    } catch (ClientException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientException: %s",
          this.world_time, e.toString());
      e.printStackTrace();
    } catch (ClientFatalException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientFatalException: %s",
          this.world_time, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
    if (DEBUG) {
      System.err.printf("Controller.RequestHandlingLoop completed in %d ms\n",
          (System.currentTimeMillis() - A0));
    }
  };
  private Runnable ServerLoop = () -> {
    long A0 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
    }
    try {
      int[] output = this.storage.DBQueryServerLocationsActive(this.world_time);
      this.client.collectServerLocations(output);
    } catch (SQLException e) {
      System.err.println("Encountered fatal error");
      System.err.println(e.toString());
      e.printStackTrace();
      System.exit(1);
    }
    if (DEBUG) {
      System.err.printf("Controller.ServerLoop completed in %d ms\n",
          (System.currentTimeMillis() - A0));
    }
  };
  public Controller() {
    this.storage = new Storage();
    this.communicator = new Communicator();
    this.communicator.registerStorage(storage);
    this.communicator.registerController(this);
  }
  public int[] query(String sql, int ncols) throws SQLException {
           return storage.DBQuery(sql, ncols);
         }
  public int[] queryAllVertices() throws SQLException {
           return storage.DBQueryAllVertices();
         }
  public int[] queryAllEdges() throws SQLException {
           return storage.DBQueryAllEdges();
         }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           return storage.DBQueryVertex(v);
         }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           return storage.DBQueryEdge(v1, v2);
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           return storage.DBQueryUser(rid);
         }
  public int[] queryQueuedRequests(final int t) throws SQLException {
           return storage.DBQueryQueuedRequests(t);
         }
  public int[] queryServerRoute(final int sid) throws SQLException {
           return storage.DBQueryServerRoute(sid);
         }
  public int[] queryServerSchedule(final int sid) throws SQLException {
           return storage.DBQueryServerSchedule(sid);
         }
  public int[] queryCountVertices() throws SQLException {
           return storage.DBQueryCountVertices();
         }
  public int[] queryCountEdges() throws SQLException {
           return storage.DBQueryCountEdges();
         }
  public int[] queryStatisticsEdges() throws SQLException {
           return storage.DBQueryStatisticsEdges();
         }
  public int[] queryMBR() throws SQLException {
           return storage.DBQueryMBR();
         }
  public int[] queryCountServers() throws SQLException {
           return storage.DBQueryCountServers();
         }
  public int[] queryCountRequests() throws SQLException {
           return storage.DBQueryCountRequests();
         }
  public int[] queryServiceRate() throws SQLException {
           return storage.DBQueryServiceRate();
         }
  public int[] queryBaseDistanceTotal() throws SQLException {
           return storage.DBQueryBaseDistanceTotal();
         }
  public int[] queryServerBaseDistanceTotal() throws SQLException {
           return storage.DBQueryServerBaseDistanceTotal();
         }
  public int[] queryRequestBaseDistanceTotal() throws SQLException {
           return storage.DBQueryRequestBaseDistanceTotal();
         }
  public int[] queryRequestBaseDistanceUnassigned() throws SQLException {
           return storage.DBQueryRequestBaseDistanceUnassigned();
         }
  public int[] queryServerTravelDistanceTotal() throws SQLException {
           return storage.DBQueryServerTravelDistanceTotal();
         }
  public int[] queryServerCruisingDistanceTotal() throws SQLException {
           return storage.DBQueryServerCruisingDistanceTotal();
         }
  public int[] queryServerServiceDistanceTotal() throws SQLException {
           return storage.DBQueryServerServiceDistanceTotal();
         }
  public int[] queryRequestDetourDistanceTotal() throws SQLException {
           return storage.DBQueryRequestDetourDistanceTotal();
         }
  public int[] queryRequestTransitDistanceTotal() throws SQLException {
           return storage.DBQueryRequestTransitDistanceTotal();
         }
  public int[] queryServerTravelDurationTotal() throws SQLException {
           return storage.DBQueryServerTravelDurationTotal();
         }
  public int[] queryRequestPickupDurationTotal() throws SQLException {
           return storage.DBQueryRequestPickupDurationTotal();
         }
  public int[] queryRequestTransitDurationTotal() throws SQLException {
           return storage.DBQueryRequestTransitDurationTotal();
         }
  public int[] queryRequestTravelDurationTotal() throws SQLException {
           return storage.DBQueryRequestTravelDurationTotal();
         }
  public int[] queryRequestDepartureTime(final int rid) throws SQLException {
           return storage.DBQueryRequestDepartureTime(rid);
         }
  public int[] queryServerDepartureTime(final int sid) throws SQLException {
           return storage.DBQueryServerDepartureTime(sid);
         }
  public int[] queryRequestArrivalTime(final int rid) throws SQLException {
           return storage.DBQueryRequestArrivalTime(rid);
         }
  public int[] queryServerTWViolations() throws SQLException {
           return storage.DBQueryServerTWViolations();
         }
  public int[] queryRequestTWViolations() throws SQLException {
           return storage.DBQueryRequestTWViolations();
         }
  public void addNewServer(final int[] u)
         throws DuplicateUserException, EdgeNotFoundException, SQLException,
                GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
           this.storage.DBAddNewServer(u, this.tools.computeRoute(u[4], u[5], u[2]));
         }
  public void addNewRequest(final int[] u) throws DuplicateUserException, SQLException {
           this.storage.DBAddNewRequest(u);
         }
  public void loadRoadNetworkFromFile(final String f_rnet) throws FileNotFoundException, SQLException {
           Scanner sc = new Scanner(new File(f_rnet));
           while (sc.hasNext()) {
         final int col0 = sc.nextInt();
         final int col1 = sc.nextInt();
         final int col2 = sc.nextInt();
         final int col3 = (col1 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
         final int col4 = (col1 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
         final int col5 = (col2 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
         final int col6 = (col2 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
         try {
           this.storage.DBAddNewVertex(col1, col3, col4);
         } catch (DuplicateVertexException e) {
           if (DEBUG) {
             // System.err.println("Warning! Duplicate vertex ignored.");
           }
         }
         try {
           this.storage.DBAddNewVertex(col2, col5, col6);
         } catch (DuplicateVertexException e) {
           if (DEBUG) {
             // System.err.println("Warning! Duplicate vertex ignored.");
           }
         }
         final int dist = ((col1 != 0 && col2 != 0)
           ? this.tools.computeHaversine(
                 col3/CSHIFT, col4/CSHIFT,
                 col5/CSHIFT, col6/CSHIFT) : 0);
         try {
           this.storage.DBAddNewEdge(col1, col2, dist, 10);
         } catch (DuplicateEdgeException e) {
           if (DEBUG) {
             // System.err.println("Warning! Duplicate edge ignored.");
           }
         }
           }
           this.tools.registerVertices(this.storage.getReferenceVerticesCache());
           this.tools.registerEdges(this.storage.getReferenceEdgesCache());
         }
  public void loadProblem(String p)
         throws FileNotFoundException, DuplicateUserException, EdgeNotFoundException, SQLException,
                GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
           Scanner sc = new Scanner(new File(p));
           for (int i = 0; i < 6; i++) {
             sc.nextLine();
           }
           while (sc.hasNext()) {
             final int uid = sc.nextInt();
             final int  uo = sc.nextInt();
             final int  ud = sc.nextInt();
             final int  uq = sc.nextInt();
             final int  ue = sc.nextInt();
             final int  ul = sc.nextInt();
             final int  ub = this.tools.computeShortestPathDistance(uo, ud);
             if (uq < 0) {
               this.addNewServer(new int[] { uid, uq, ue, ul, uo, ud, ub });
             } else {
               this.addNewRequest(new int[] { uid, uq, ue, ul, uo, ud, ub });
             }
           }
         }
  public void createNewInstance() throws SQLException {
           this.storage.DBCreateNewInstance();
         }
  public void closeInstance() throws SQLException {
           this.storage.DBCloseInstance();
         }
  public void loadDataModel() {
           this.storage.DBLoadDataModel();
         }
  public void loadBackup(final String p) throws SQLException {
           this.storage.DBLoadBackup(p);
         }
  public void loadRoadNetworkFromDB() throws SQLException {
           this.storage.DBLoadRoadNetworkFromDB();
         }
  public void loadUsersFromDB() throws SQLException {
           this.storage.DBLoadUsersFromDB();
         }
  public void saveBackup(final String p) throws SQLException {
           this.storage.DBSaveBackup(p);
         }
  public void setRequestTimeout(final int t) {
           this.REQUEST_TIMEOUT = t;
         }
  public void loadGtree(String p) throws FileNotFoundException {
           this.tools.GTLoadGtree(p);
         }
  public void closeGtree() {
           this.tools.GTCloseGtree();
         }
  public int getSimulationWorldTime() {
           return this.world_time;
         }
  public void setClockStart(final int t) {
           this.clockstart = t;
         }
  public void setClockEnd(final int t) {
           this.clockend = t;
         }
  public void setQueueTimeout(final int t) {
           this.QUEUE_TIMEOUT = t;
         }
  public void setEngineUpdatePeriod(final int t) {
           this.engine_update_period = t;
         }
  public void startRealtime(final Consumer<Boolean> app_cb) {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.world_time = this.clockstart;

           int simulation_duration = (this.clockend - this.clockstart);

           ScheduledExecutorService exe = Executors.newScheduledThreadPool(5);

           ScheduledFuture<?> cb1 = exe.scheduleAtFixedRate(
             this.ClockLoop, 0, 1, TimeUnit.SECONDS);

           ScheduledFuture<?> cb2 = exe.scheduleAtFixedRate(
             this.EngineLoop, this.loop_delay, this.engine_update_period, TimeUnit.SECONDS);

           int request_collection_period = this.client.getRequestCollectionPeriod();
           ScheduledFuture<?> cb3 = exe.scheduleAtFixedRate(
             this.RequestCollectionLoop, this.loop_delay, request_collection_period, TimeUnit.SECONDS);

           int request_handling_period = this.client.getRequestHandlingPeriod();
           ScheduledFuture<?> cb4 = exe.scheduleAtFixedRate(
             this.RequestHandlingLoop, this.loop_delay, request_handling_period, TimeUnit.MILLISECONDS);

           int server_collection_period = this.client.getServerLocationCollectionPeriod();
           ScheduledFuture<?> cb5 = exe.scheduleAtFixedRate(
             this.ServerLoop, this.loop_delay, server_collection_period, TimeUnit.SECONDS);

           exe.schedule(() -> {
             cb1.cancel(false);
             cb2.cancel(false);
             cb3.cancel(false);
             cb4.cancel(false);
             cb5.cancel(false);
             exe.shutdown();
             this.client.end();
             app_cb.accept(true);
           }, simulation_duration, TimeUnit.SECONDS);
         }
  public void startSequential(final Consumer<Boolean> app_cb) {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.world_time = this.clockstart;
           while (this.world_time < this.clockend) {
             this.ClockLoop.run();  // this.world_time gets incremented here!
             this.EngineLoop.run();
             this.ServerLoop.run();
             this.RequestCollectionLoop.run();
             this.RequestHandlingLoop.run();
           }
           this.client.end();
           app_cb.accept(true);
         }
  public void registerClient(final Client target) {
           this.client = target;
           this.client.registerCommunicator(this.communicator);
         }
  public void registerTraffic(final Traffic target) {
           this.communicator.forwardTraffic(target);
         }
  public void returnRequest(final int[] r) {
           if (this.world_time - r[2] < this.QUEUE_TIMEOUT) {
             this.lu_seen.put(r[0], false);
           }
         }
  public Storage internalRefStorage() {
           return this.storage;
         }
  public Communicator internalRefCommunicator() {
           return this.communicator;
         }
}
