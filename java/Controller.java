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
import java.util.concurrent.ConcurrentHashMap;
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
  private int CLOCK_START =
      Integer.parseInt(System.getProperty("jargors.controller.clock_start", "0"));
  private int CLOCK_END =
      Integer.parseInt(System.getProperty("jargors.controller.clock_end", "1800"));
  private int REQUEST_TIMEOUT =
      Integer.parseInt(System.getProperty("jargors.controller.request_timeout", "30"));
  private int QUEUE_TIMEOUT =
      Integer.parseInt(System.getProperty("jargors.controller.queue_timeout", "30"));
  private int REQUEST_COLLECTION_PERIOD =
      Integer.parseInt(System.getProperty("jargors.controller.request_collection_period", "1"));
  private int REQUEST_HANDLING_PERIOD =
      Integer.parseInt(System.getProperty("jargors.controller.request_handling_period", "1"));
  private int SERVER_COLLECTION_PERIOD =
      Integer.parseInt(System.getProperty("jargors.controller.server_collection_period", "1"));
  private int clock_now = 0;
  private int clock_reference_day = 1;
  private int clock_reference_hour = 0;
  private int clock_reference_minute = 0;
  private int clock_reference_second = 0;
  private int loop_delay = 0;
  // private int deviation_rate = 0.02;
  // private int breakdown_rate = 0.005;
  private final double CSHIFT = Storage.CSHIFT;
  private boolean kill = false;
  private boolean working = false;
  private ScheduledExecutorService exe = null;
  private ScheduledFuture<?> cb1 = null;
  private ScheduledFuture<?> cb2 = null;
  private ScheduledFuture<?> cb3 = null;
  private ScheduledFuture<?> cb4 = null;
  private ScheduledFuture<?> cb5 = null;
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
    this.clock_now++;
    this.clock_reference_second++;
    if (this.clock_reference_second > 59) {
      this.clock_reference_second = 0;
      this.clock_reference_minute++;
      if (this.clock_reference_minute > 59) {
        this.clock_reference_minute = 0;
        this.clock_reference_hour++;
        if (this.clock_reference_hour > 23) {
          this.clock_reference_day++;
          this.clock_reference_hour = 0;
        }
      }
    }
    if (DEBUG) {
      System.err.printf("[t=%d; Day %d, %02d:%02d:%02d] Controller.ClockLoop says: %s!\n",
          this.clock_now,
          this.clock_reference_day,
          this.clock_reference_hour,
          this.clock_reference_minute,
          this.clock_reference_second,
          (this.clock_now % 2 == 0 ? "ping" : "pong"));
    }
  };
  private Runnable RequestCollectionLoop = () -> {
    long A0 = 0;
    int  A1 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
    }
    try {
      int[] output = this.storage.DBQueryRequestsQueued(this.clock_now);
      for (int i = 0; i < (output.length - 6); i += 7) {
        if (!this.lu_seen.containsKey(output[i]) || this.lu_seen.get(output[i]) == false) {
          this.client.addRequest(new int[] {
            output[(i + 0)],
            output[(i + 1)],
            output[(i + 2)],
            output[(i + 3)],
            output[(i + 4)],
            output[(i + 5)],
            output[(i + 6)] });
          this.lu_seen.put(output[i], true);
          if (DEBUG) {
            A1++;
          }
        }
      }
    } catch (SQLException e) {
      if (e.getErrorCode() == 40000) {
        System.err.println("Warning: database connection interrupted");
      } else {
        System.err.println("Encountered fatal error");
        System.err.println(e.toString());
        System.err.println(e.getErrorCode());
        e.printStackTrace();
        System.exit(1);
      }
    }
    if (DEBUG) {
      System.err.printf("Controller.RequestCollectionLoop collected %d requests in %d ms\n",
          A1, (System.currentTimeMillis() - A0));
    }
  };
  private Runnable RequestHandlingLoop = () -> {
    long A0 = 0;
    int  A1 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
      A1 = this.client.getStatCountQueueSize();
    }
    try {
      this.client.notifyNew();  // blocks this thread until queue is empty
    } catch (ClientException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientException: %s\n",
          this.clock_now, e.toString());
      e.printStackTrace();
    } catch (ClientFatalException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientFatalException: %s\n",
          this.clock_now, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
    if (DEBUG) {
      System.err.printf("Controller.RequestHandlingLoop handled %d requests in %d ms\n",
          A1, (System.currentTimeMillis() - A0));
    }
  };
  private Runnable ServerLoop = () -> {
    long A0 = 0;
    int  A1 = 0;
    if (DEBUG) {
      A0 = System.currentTimeMillis();
    }
    try {
      int[] output = this.storage.DBQueryServersLocationsActive(this.clock_now);
      if (DEBUG) {
        A1 = (output.length/3);
      }
      this.client.collectServerLocations(output);
    } catch (SQLException e) {
      if (e.getErrorCode() == 40000) {
        System.err.println("Warning: database connection interrupted");
      } else {
        System.err.println("Encountered fatal error");
        System.err.println(e.toString());
        System.err.println(e.getErrorCode());
        e.printStackTrace();
        System.exit(1);
      }
    }
    if (DEBUG) {
      System.err.printf("Controller.ServerLoop collected/handled %d in %d ms\n",
          A1, (System.currentTimeMillis() - A0));
    }
  };
  public Controller() {
    this.storage = new Storage();
    this.communicator = new Communicator();
    this.communicator.setRefStorage(this.storage);
    this.communicator.setRefController(this);
  }
  public int[] query(final String sql, final int ncols) throws SQLException {
           return this.storage.DBQuery(sql, ncols);
         }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           return this.storage.DBQueryEdge(v1, v2);
         }
  public int[] queryEdgeStatistics() throws SQLException {
           return storage.DBQueryEdgeStatistics();
         }
  public int[] queryEdges() throws SQLException {
           return this.storage.DBQueryEdges();
         }
  public int[] queryEdgesCount() throws SQLException {
           return this.storage.DBQueryEdgesCount();
         }
  public int[] queryMBR() throws SQLException {
           return this.storage.DBQueryMBR();
         }
  public int[] queryMetricRequestDistanceBaseTotal() throws SQLException {
           return storage.DBQueryMetricRequestDistanceBaseTotal();
         }
  public int[] queryMetricRequestDistanceBaseUnassignedTotal() throws SQLException {
           return storage.DBQueryMetricRequestDistanceBaseUnassignedTotal();
         }
  public int[] queryMetricRequestDistanceDetourTotal() throws SQLException {
           return storage.DBQueryMetricRequestDistanceDetourTotal();
         }
  public int[] queryMetricRequestDistanceTransitTotal() throws SQLException {
           return storage.DBQueryMetricRequestDistanceTransitTotal();
         }
  public int[] queryMetricRequestDurationPickupTotal() throws SQLException {
           return storage.DBQueryMetricRequestDurationPickupTotal();
         }
  public int[] queryMetricRequestDurationTransitTotal() throws SQLException {
           return storage.DBQueryMetricRequestDurationTransitTotal();
         }
  public int[] queryMetricRequestDurationTravelTotal() throws SQLException {
           return storage.DBQueryMetricRequestDurationTravelTotal();
         }
  public int[] queryMetricRequestTWViolationsTotal() throws SQLException {
           return storage.DBQueryMetricRequestTWViolationsTotal();
         }
  public int[] queryMetricServerDistanceBaseTotal() throws SQLException {
           return storage.DBQueryMetricServerDistanceBaseTotal();
         }
  public int[] queryMetricServerDistanceCruisingTotal() throws SQLException {
           return storage.DBQueryMetricServerDistanceCruisingTotal();
         }
  public int[] queryMetricServerDistanceServiceTotal() throws SQLException {
           return storage.DBQueryMetricServerDistanceServiceTotal();
         }
  public int[] queryMetricServerDistanceTotal() throws SQLException {
           return storage.DBQueryMetricServerDistanceTotal();
         }
  public int[] queryMetricServerDurationTravelTotal() throws SQLException {
           return storage.DBQueryMetricServerDurationTravelTotal();
         }
  public int[] queryMetricServerTWViolationsTotal() throws SQLException {
           return storage.DBQueryMetricServerTWViolationsTotal();
         }
  public int[] queryMetricServiceRate() throws SQLException {
           return storage.DBQueryMetricServiceRate();
         }
  public int[] queryMetricUserDistanceBaseTotal() throws SQLException {
           return storage.DBQueryMetricUserDistanceBaseTotal();
         }
  public int[] queryRequestTimeOfArrival(final int rid) throws SQLException {
           return storage.DBQueryRequestTimeOfArrival(rid);
         }
  public int[] queryRequestTimeOfDeparture(final int rid) throws SQLException {
           return storage.DBQueryRequestTimeOfDeparture(rid);
         }
  public int[] queryRequestsCount() throws SQLException {
           return storage.DBQueryRequestsCount();
         }
  public int[] queryRequestsQueued(final int t) throws SQLException {
           return storage.DBQueryRequestsQueued(t);
         }
  public int[] queryServerRoute(final int sid) throws SQLException {
           return storage.DBQueryServerRoute(sid);
         }
  public int[] queryServerSchedule(final int sid) throws SQLException {
           return storage.DBQueryServerSchedule(sid);
         }
  public int[] queryServerTimeOfDeparture(final int sid) throws SQLException {
           return storage.DBQueryServerTimeOfDeparture(sid);
         }
  public int[] queryServersCount() throws SQLException {
           return storage.DBQueryServersCount();
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           return storage.DBQueryUser(rid);
         }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           return this.storage.DBQueryVertex(v);
         }
  public int[] queryVertices() throws SQLException {
           return this.storage.DBQueryVertices();
         }
  public int[] queryVerticesCount() throws SQLException {
           return this.storage.DBQueryVerticesCount();
         }
  public void insertRequest(final int[] u) throws DuplicateUserException, SQLException {
           this.storage.DBInsertRequest(u);
         }
  public void insertServer(final int[] u)
         throws DuplicateUserException, EdgeNotFoundException, SQLException,
                GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
           this.storage.DBInsertServer(u, this.tools.computeRoute(u[4], u[5], u[2]));
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
               this.insertServer(new int[] { uid, uq, ue, ul, uo, ud, ub });
             } else {
               this.insertRequest(new int[] { uid, uq, ue, ul, uo, ud, ub });
             }
           }
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
           this.storage.DBInsertVertex(col1, col3, col4);
         } catch (DuplicateVertexException e) {
           if (DEBUG) {
             // System.err.println("Warning! Duplicate vertex ignored.");
           }
         }
         try {
           this.storage.DBInsertVertex(col2, col5, col6);
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
           this.storage.DBInsertEdge(col1, col2, dist, 10);
         } catch (DuplicateEdgeException e) {
           if (DEBUG) {
             // System.err.println("Warning! Duplicate edge ignored.");
           }
         }
           }
           this.tools.setRefCacheVertices(this.storage.getRefCacheVertices());
           this.tools.setRefCacheEdges(this.storage.getRefCacheEdges());
         }
  public void cacheRoadNetworkFromDB() throws SQLException {
           this.storage.JargoCacheRoadNetworkFromDB();
         }
  public void cacheUsersFromDB() throws SQLException {
           this.storage.JargoCacheUsersFromDB();
         }
  public void instanceClose() throws SQLException {
           this.storage.JargoInstanceClose();
         }
  public void instanceExport(final String p) throws SQLException {
           this.storage.JargoInstanceExport(p);
         }
  public void instanceInitialize() {
           this.storage.JargoInstanceInitialize();
         }
  public void instanceLoad(final String p) throws SQLException {
           this.storage.JargoInstanceLoad(p);
         }
  public void instanceNew() throws SQLException {
           this.storage.JargoInstanceNew();
         }
  public void gtreeClose() {
           this.tools.GTGtreeClose();
         }
  public void gtreeLoad(String p) throws FileNotFoundException {
           this.tools.GTGtreeLoad(p);
         }
  public int getClockNow() {
           return this.clock_now;
         }
  public Communicator getRefCommunicator() {
           return this.communicator;
         }
  public Storage getRefStorage() {
           return this.storage;
         }
  public int retrieveQueueSize() {
           return this.client.getStatCountQueueSize();
         }
  public final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> retrieveRefCacheEdges() {
           return this.storage.getRefCacheEdges();
         }
  public final ConcurrentHashMap<Integer, int[]> retrieveRefCacheUsers() {
           return this.storage.getRefCacheUsers();
         }
  public final ConcurrentHashMap<Integer, int[]> retrieveRefCacheVertices() {
           return this.storage.getRefCacheVertices();
         }
  public void forwardRefCommunicator(final Communicator communicator) {
           this.client.setRefCommunicator(communicator);
         }
  public void forwardRefTraffic(final Traffic traffic) {
           this.communicator.setRefTraffic(traffic);
         }
  public void setClockEnd(final int clock_end) {
           this.CLOCK_END = clock_end;
         }
  public void setClockReference(final String clock_reference) throws IllegalArgumentException {
           int hour = Integer.parseInt(clock_reference.substring(0, 2));
           if (!(0 <= hour && hour <= 23)) {
             throw new IllegalArgumentException("Invalid clock reference (hour got "+hour+"; must be between [00, 23])");
           }
           int minute = Integer.parseInt(clock_reference.substring(2, 4));
           if (!(0 <= minute && minute <= 59)) {
             throw new IllegalArgumentException("Invalid clock reference (minute got "+minute+"; must be between [00, 59])");
           }
           this.clock_reference_hour = hour;
           this.clock_reference_minute = minute;
         }
  public void setClockStart(final int clock_start) {
           this.CLOCK_START = clock_start;
         }
  public void setQueueTimeout(final int queue_timeout) {
           this.QUEUE_TIMEOUT = queue_timeout;
         }
  public void setRefClient(final Client client) {
           this.client = client;
         }
  public final boolean isKilled() {
           return this.kill;
         }
  public void returnRequest(final int[] r) {
           if (this.clock_now - r[2] < QUEUE_TIMEOUT) {
             this.lu_seen.put(r[0], false);
           }
         }
  public void startRealtime(final Consumer<Boolean> app_cb) {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.clock_now = CLOCK_START;

           int simulation_duration = (CLOCK_END - CLOCK_START);

           this.exe = Executors.newScheduledThreadPool(5);

           this.cb1 = exe.scheduleAtFixedRate(
             this.ClockLoop, 0, 1, TimeUnit.SECONDS);

           this.cb2 = exe.scheduleAtFixedRate(
             this.RequestCollectionLoop, this.loop_delay, REQUEST_COLLECTION_PERIOD, TimeUnit.SECONDS);

           this.cb3 = exe.scheduleAtFixedRate(
             this.RequestHandlingLoop, this.loop_delay, REQUEST_HANDLING_PERIOD, TimeUnit.MILLISECONDS);

           this.cb4 = exe.scheduleAtFixedRate(
             this.ServerLoop, this.loop_delay, SERVER_COLLECTION_PERIOD, TimeUnit.SECONDS);

           this.exe.schedule(() -> {
             this.stop(app_cb);
           }, simulation_duration, TimeUnit.SECONDS);
         }
  public void startSequential(final Consumer<Boolean> app_cb) {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.clock_now = CLOCK_START;
           while (!kill && this.clock_now < CLOCK_END) {
             this.working = true;
             this.ClockLoop.run();  // this.clock_now gets incremented here!
             this.ServerLoop.run();
             this.RequestCollectionLoop.run();
             this.RequestHandlingLoop.run();
             this.working = false;
           }
           this.stop(app_cb);
         }
  public void stop(final Consumer<Boolean> app_cb) {
           if (this.exe == null) {  // sequential mode
             this.kill = true;
             while (this.working) {
               try {
                 Thread.sleep(100);
               } catch (InterruptedException e) {
                 // ...
               }
             }
           } else {  // realtime mode
             this.cb1.cancel(true);
             this.cb2.cancel(true);
             this.cb3.cancel(true);
             this.cb4.cancel(true);
             this.exe.shutdown();
           }
           try {
             if (this.client != null) {
               this.client.end();
             }
             app_cb.accept(true);
           } catch (Exception e) {
             System.err.println("Error in ending callback");
             System.err.println(e.toString());
             e.printStackTrace();
             return;
           }
         }
}
