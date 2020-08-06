package com.github.jargors.sim;
import com.github.jargors.sim.Storage;
import com.github.jargors.sim.Communicator;
import com.github.jargors.sim.Client;
import com.github.jargors.sim.Tools;
import com.github.jargors.sim.ClientException;
import com.github.jargors.sim.ClientFatalException;
import com.github.jargors.sim.DuplicateVertexException;
import com.github.jargors.sim.DuplicateEdgeException;
import com.github.jargors.sim.DuplicateUserException;
import com.github.jargors.sim.EdgeNotFoundException;
import com.github.jargors.sim.UserNotFoundException;
import com.github.jargors.sim.VertexNotFoundException;
import com.github.jargors.sim.GtreeNotLoadedException;
import com.github.jargors.sim.GtreeIllegalSourceException;
import com.github.jargors.sim.GtreeIllegalTargetException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
public class Controller {
  private Random random = new Random();
  private Storage storage;
  private Communicator communicator;
  private Tools tools = new Tools();
  private Client client;
  private Map<Integer, Boolean> lu_rseen = new HashMap<Integer, Boolean>();
  private Map<Integer, Boolean> lu_sseen = new HashMap<Integer, Boolean>();
  private String refTimeStr = "";
  private long refTimeMs = 0;
  private int simClock = 0;
  private int simClockReferenceDay = 0;
  private int simClockReferenceMinute = 0;
  private int simClockReferenceHour = 0;
  private int simClockReferenceSecond = 0;
  private long dur_query = 0;
  private int MEAN_DELAY =  // in minutes
      Integer.parseInt(System.getProperty("jargors.controller.mean_delay", "5"));
  private int STD_DELAY =   // in minutes
      Integer.parseInt(System.getProperty("jargors.controller.std_delay", "2"));
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
  private final boolean SNAPSHOT =
      "true".equals(System.getProperty("jargors.controller.snapshot"));
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
    this.simClock++;
    this.simClockReferenceSecond++;
    if (this.simClockReferenceSecond > 59) {
      this.simClockReferenceSecond = 0;
      this.simClockReferenceMinute++;
      if (SNAPSHOT) {
        try {
          this.instanceExport(String.format("snapshot%06d", this.simClock));
        } catch (SQLException e) {
          if (e.getErrorCode() == 40000) {
            System.err.println("Warning: database connection interrupted");
          } else {
            System.err.println("Encountered fatal error");
              System.err.println(e.toString());
              System.err.println(e.getErrorCode());
              e.printStackTrace();
          }
        }
      }
      if (this.simClockReferenceMinute > 59) {
        this.simClockReferenceMinute = 0;
        this.simClockReferenceHour++;
        if (this.simClockReferenceHour > 23) {
          this.simClockReferenceHour = 0;
          this.simClockReferenceDay++;
        }
      }
    }
    if (DEBUG) {
      System.out.printf("t=%d (day %d, %02d:%02d:%02d)\n",
          this.simClock,
          this.simClockReferenceDay,
          this.simClockReferenceHour,
          this.simClockReferenceMinute,
          this.simClockReferenceSecond);
    }
  };
  private Runnable RequestCollectionLoop = () -> {
    int  A1 = 0;
    int  A2 = 0;
    final int now = this.simClock;
    try {
      A2 = this.client.dropRequests(now - QUEUE_TIMEOUT);
      if (DEBUG) {
        System.out.printf("drop %d requests\n", A2);
      }
      int[] output = this.storage.DBQueryRequestsQueued(now);
      if (DEBUG) {
        System.out.printf("query %d unassigned requests\n", (output.length/7));
      }
      for (int i = 0; i < (output.length - 6); i += 7) {
        if (!this.lu_rseen.containsKey(output[i]) || this.lu_rseen.get(output[i]) == false) {
          this.client.addRequest(new int[] {
            output[(i + 0)],
            output[(i + 1)],
            output[(i + 2)],
            output[(i + 3)],
            output[(i + 4)],
            output[(i + 5)],
            output[(i + 6)] });
          this.lu_rseen.put(output[i], true);
          A1++;
        }
      }
      if (DEBUG) {
        System.out.printf("add %d new requests\n", A1);
      }
    } catch (SQLException e) {
      if (e.getErrorCode() == 40000) {
        System.err.println("Warning: database connection interrupted");
      } else {
        System.err.println("Encountered fatal error");
        try {
          instanceExport("crash-db");
          System.err.println(e.toString());
          System.err.println(e.getErrorCode());
          e.printStackTrace();
        } catch (Exception ee) {
          // ..
        } finally {
          System.exit(1);
        }
      }
    }
  };
  private Runnable RequestHandlingLoop = () -> {
    try {
      this.client.notifyNew();  // blocks this thread until queue is empty
    } catch (ClientException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientException: %s\n",
          this.simClock, e.toString());
      e.printStackTrace();
    } catch (ClientFatalException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientFatalException: %s\n",
          this.simClock, e.toString());
      e.printStackTrace();
      System.exit(1);
    } catch (Exception e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a unspecified Exception: %s\n",
          this.simClock, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
  };
  private Runnable ServerLoop = () -> {
    try {
      int[] output = this.storage.DBQueryServersLocationsActive(this.simClock);
      if (DEBUG) {
        System.out.printf("got %d servers\n", (output.length/3));
      }
      for (int i = 0; i < (output.length - 2); i += 3) {
        if (!this.lu_sseen.containsKey(output[i])) {
          this.lu_sseen.put(output[i], true);
        }
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
    } catch (Exception e) {
      System.err.printf("[t=%d] Controller.ServerLoop caught a unspecified Exception: %s\n",
          this.simClock, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
  };
  public Controller() {
    this.storage = new Storage();
    this.communicator = new Communicator();
    this.communicator.setRefStorage(this.storage);
    this.communicator.setRefController(this);
  }
  public int[] query(final String sql, final int ncols) throws SQLException {
           int[] output = this.storage.DBQuery(sql, ncols);
           return output;
         }
  public int[] queryQuick(final String sql, int[] outcols, ArrayList<String> header) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryQuick(sql, outcols, header);
           this.dur_query = System.currentTimeMillis() - A0;
           return output;
         }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           int[] output = this.storage.DBQueryEdge(v1, v2);
           return output;
         }
  public int[] queryEdgeStatistics() throws SQLException {
           int[] output = storage.DBQueryEdgeStatistics();
           return output;
         }
  public int[] queryEdges() throws SQLException {
           int[] output = this.storage.DBQueryEdges();
           return output;
         }
  public int[] queryEdgesCount() throws SQLException {
           int[] output = this.storage.DBQueryEdgesCount();
           return output;
         }
  public int[] queryMBR() throws SQLException {
           int[] output = this.storage.DBQueryMBR();
           return output;
         }
  public int[] queryMetricRequestDistanceBaseTotal() throws SQLException {
           int[] output = storage.DBQueryMetricRequestDistanceBaseTotal();
           return output;
         }
  public int[] queryMetricRequestDistanceBaseUnassignedTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestDistanceBaseUnassignedRunning()
         throws SQLException, UserNotFoundException {
           int[] output = new int[] { 0 };
           for (int rid : this.lu_rseen.keySet()) {
             if (this.storage.DBQueryRequestIsAssigned(rid, true).length == 0) {
               output[0] += this.storage.DBQueryUser(rid)[6];
             }
           }
           return output;
         }
  public int[] queryMetricRequestDistanceDetourTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDistanceDetourTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestDistanceTransitTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDistanceTransitTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestDurationPickupTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDurationPickupTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestDurationTransitTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDurationTransitTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestDurationTravelTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricRequestDurationTravelTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricRequestTWViolationsTotal() throws SQLException {
           int[] output = storage.DBQueryMetricRequestTWViolationsTotal();
           return output;
         }
  public int[] queryMetricServerDistanceBaseTotal() throws SQLException {
           int[] output = storage.DBQueryMetricServerDistanceBaseTotal();
           return output;
         }
  public int[] queryMetricServerDistanceCruisingTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDistanceCruisingTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerDistanceRunning() throws SQLException {
           int[] output = new int[] { 0 };
           for (int sid : this.lu_sseen.keySet()) {
             output[0] += this.storage.DBQueryServerDistance(sid, true)[0];
           }
           return output;
         }
  public int[] queryMetricServerDistanceServiceTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDistanceServiceTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerDistanceTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDistanceTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerDurationCruisingTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDurationCruisingTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerDurationServiceTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDurationServiceTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerDurationTravelTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServerDurationTravelTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricServerTWViolationsTotal() throws SQLException {
           int[] output = storage.DBQueryMetricServerTWViolationsTotal();
           return output;
         }
  public int[] queryMetricServiceRate(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricServiceRate(flag_usecache);
           return output;
         }
  public int[] queryMetricServiceRateRunning() throws SQLException {
           int[] output = new int[] {
               Math.min((int) (10000*(this.storage.DBQueryRequestsCountAssigned()[0]
                 / (double) this.lu_rseen.size())), 10000) };
           return output;
         }
  public int[] queryMetricUserDistanceBaseTotal(boolean flag_usecache) throws SQLException {
           int[] output = storage.DBQueryMetricUserDistanceBaseTotal(flag_usecache);
           return output;
         }
  public int[] queryMetricUserDistanceBaseRunning()
         throws SQLException, UserNotFoundException {
           int[] output = new int[] { 0 };
           for (int sid : this.lu_sseen.keySet()) {
             output[0] += this.storage.DBQueryUser(sid)[6];
           }
           for (int rid : this.lu_rseen.keySet()) {
             output[0] += this.storage.DBQueryUser(rid)[6];
           }
           return output;
         }
  public int[] queryRequestTimeOfArrival(final int rid) throws SQLException {
           int[] output = storage.DBQueryRequestTimeOfArrival(rid);
           return output;
         }
  public int[] queryRequestTimeOfDeparture(final int rid) throws SQLException {
           int[] output = storage.DBQueryRequestTimeOfDeparture(rid);
           return output;
         }
  public int[] queryRequestsCount() throws SQLException {
           int[] output = storage.DBQueryRequestsCount();
           return output;
         }
  public int[] queryRequestsCountActive(final int t) throws SQLException {
           int[] output = storage.DBQueryRequestsCountActive(t);
           return output;
         }
  public int[] queryRequestsCountAppeared() throws SQLException {
           int[] output = new int[] { this.lu_rseen.size() };
           return output;
         }
  public int[] queryRequestsCountAssigned() throws SQLException {
           int[] output = storage.DBQueryRequestsCountAssigned();
           return output;
         }
  public int[] queryRequestsCountCompleted(final int t) throws SQLException {
           int[] output = storage.DBQueryRequestsCountCompleted(t);
           return output;
         }
  public int[] queryRequestsQueued(final int t) throws SQLException {
           int[] output = storage.DBQueryRequestsQueued(t);
           return output;
         }
  public int[] queryRequestsWaiting(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestsWaiting(t);
           return output;
         }
  public int[] queryServerDistance(final int sid, boolean flag_usecache) throws SQLException {
           return this.storage.DBQueryServerDistance(sid, flag_usecache);
         }
  public int[] queryServerRoute(final int sid) throws SQLException {
           int[] output = storage.DBQueryServerRoute(sid);
           return output;
         }
  public int[] queryServerRouteActive(final int sid) throws SQLException {
           int[] output = this.storage.DBQueryServerRouteActive(sid);
           return output;
         }
  public int[] queryServerRouteRemaining(final int sid, final int t) throws SQLException {
           int[] output = this.storage.DBQueryServerRouteRemaining(sid, t);
           return output;
         }
  public int[] queryServerSchedule(final int sid) throws SQLException {
           int[] output = storage.DBQueryServerSchedule(sid);
           return output;
         }
  public int[] queryServerTimeOfDeparture(final int sid) throws SQLException {
           int[] output = storage.DBQueryServerTimeOfDeparture(sid);
           return output;
         }
  public int[] queryServersActive(final int t) throws SQLException {
           int[] output = this.storage.DBQueryServersActive(t);
           return output;
         }
  public int[] queryServersCount() throws SQLException {
           int[] output = storage.DBQueryServersCount();
           return output;
         }
  public int[] queryServersCountActive(final int t) throws SQLException {
           int[] output = this.storage.DBQueryServersCountActive(t);
           return output;
         }
  public int[] queryServersCountAppeared() throws SQLException {
           int[] output = new int[] { 0 };
           for (int sid : this.lu_sseen.keySet()) {
             if (this.storage.DBQueryServerDistance(sid, true)[0] > 0) {
               output[0]++;
             }
           }
           return output;
         }
  public int[] queryServersLocationsActive(final int t) throws SQLException {
           int[] output = this.storage.DBQueryServersLocationsActive(t);
           return output;
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           int[] output = storage.DBQueryUser(rid);
           return output;
         }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           int[] output = this.storage.DBQueryVertex(v);
           return output;
         }
  public int[] queryVertices() throws SQLException {
           int[] output = this.storage.DBQueryVertices();
           return output;
         }
  public int[] queryVerticesCount() throws SQLException {
           int[] output = this.storage.DBQueryVerticesCount();
           return output;
         }
  public int[] queryMetricRequestDistanceBaseUnassignedTotal() throws SQLException {
           return queryMetricRequestDistanceBaseUnassignedTotal(true);
         }
  public int[] queryMetricRequestDistanceDetourTotal() throws SQLException {
           return queryMetricRequestDistanceDetourTotal(true);
         }
  public int[] queryMetricRequestDistanceTransitTotal() throws SQLException {
           return queryMetricRequestDistanceTransitTotal(true);
         }
  public int[] queryMetricRequestDurationPickupTotal() throws SQLException {
           return queryMetricRequestDurationPickupTotal(true);
         }
  public int[] queryMetricRequestDurationTransitTotal() throws SQLException {
           return queryMetricRequestDurationTransitTotal(true);
         }
  public int[] queryMetricRequestDurationTravelTotal() throws SQLException {
           return queryMetricRequestDurationTravelTotal(true);
         }
  public int[] queryMetricServerDistanceCruisingTotal() throws SQLException {
           return queryMetricServerDistanceCruisingTotal(true);
         }
  public int[] queryMetricServerDistanceServiceTotal() throws SQLException {
           return queryMetricServerDistanceServiceTotal(true);
         }
  public int[] queryMetricServerDistanceTotal() throws SQLException {
           return queryMetricServerDistanceTotal(true);
         }
  public int[] queryMetricServerDurationCruisingTotal() throws SQLException {
           return queryMetricServerDurationCruisingTotal(true);
         }
  public int[] queryMetricServerDurationServiceTotal() throws SQLException {
           return queryMetricServerDurationServiceTotal(true);
         }
  public int[] queryMetricServerDurationTravelTotal() throws SQLException {
           return queryMetricServerDurationTravelTotal(true);
         }
  public int[] queryMetricServiceRate() throws SQLException {
           return queryMetricServiceRate(true);
         }
  public int[] queryMetricUserDistanceBaseTotal() throws SQLException {
           return queryMetricUserDistanceBaseTotal(true);
         }
  public void insertRequest(final int[] u) throws DuplicateUserException, SQLException {
           this.storage.DBInsertRequest(u);
         }
  public void insertServer(final int[] u)
         throws DuplicateUserException, EdgeNotFoundException, SQLException,
                GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
           this.storage.DBInsertServer(u, this.tools.computeRoute(u[4], u[5], u[2]));
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
  public void instanceReset() throws SQLException {
           this.storage.JargoInstanceReset();
         }
  public int getClock() {
           return this.simClock;
         }
  public int getClockStart() {
           return this.CLOCK_START;
         }
  public String getClockReference() {
           return this.refTimeStr;
         }
  public long getClockReferenceMs() {
           return this.refTimeMs;
         }
  public Communicator getRefCommunicator() {
           return this.communicator;
         }
  public Storage getRefStorage() {
           return this.storage;
         }
  public int retrieveQueueSize() {
           return this.client.getQueueSize();
         }
  public long retrieveHandleRequestDur() {
           return this.client.getHandleRequestDur();
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
           this.refTimeStr = clock_reference;
           try {
             this.refTimeMs = this.tools.parseClockReference(clock_reference);
           } catch (Exception pe) {
             throw new IllegalArgumentException("Invalid clock reference (parse failed)");
           }
           this.simClockReferenceHour= hour;
           this.simClockReferenceMinute = minute;
           if (DEBUG) {
             System.out.printf("refHr=%d, refMn=%d, refMs=%d\n",
               hour, minute, this.refTimeMs);
           }
         }
  public void setClockStart(final int clock_start) {
           this.CLOCK_START = clock_start;
           this.simClockReferenceSecond += clock_start;
           this.simClockReferenceSecond %= 60;
           this.simClockReferenceMinute += (clock_start / 60);
           this.simClockReferenceMinute %= 60;
           this.simClockReferenceHour += (clock_start / 3600);
           this.simClockReferenceHour %= 24;
           this.simClockReferenceDay += (clock_start / 86400);
           if (DEBUG) {
             System.out.printf("clock_start=%d\n", clock_start);
             System.out.printf("clock day %d %02d:%02d:%02d\n",
                 this.simClockReferenceDay,
                 this.simClockReferenceHour,
                 this.simClockReferenceMinute,
                 this.simClockReferenceSecond);
           }
         }
  public void setQueueTimeout(final int queue_timeout) {
           this.QUEUE_TIMEOUT = queue_timeout;
         }
  public void setRefClient(final Client client) {
           this.client = client;
         }
  public void gtreeClose() {
           this.tools.GTGtreeClose();
         }
  public void gtreeLoad(String p) throws FileNotFoundException {
           this.tools.GTGtreeLoad(p);
         }
  public int getClockReferenceDay() {
           return this.simClockReferenceDay;
         }
  public int getClockReferenceHour() {
           return this.simClockReferenceHour;
         }
  public int getClockReferenceMinute() {
           return this.simClockReferenceMinute;
         }
  public int getClockReferenceSecond() {
           return this.simClockReferenceSecond;
         }
  public long getQueryDur() {
           return this.dur_query;
         }
  public void loadProblem(String p)
         throws FileNotFoundException, DuplicateUserException, EdgeNotFoundException,
         SQLException, GtreeNotLoadedException, GtreeIllegalSourceException,
         GtreeIllegalTargetException {
           Scanner sc = new Scanner(new File(p));
           while (sc.hasNext()) {
             final int uid = sc.nextInt();
             final int  uo = sc.nextInt();
             final int  ud = sc.nextInt();
             final int  uq = sc.nextInt();
             final int  ue = sc.nextInt();
             final int  ub = this.tools.computeShortestPathDistance(uo, ud);
             final int  ul = (ud == 0)
                 ? Integer.MAX_VALUE
                 : ue + (int) Math.round((float) ub/10)  // TODO: 10 speed
                   + ( (int) Math.round(Math.abs
                       ( this.random.nextGaussian()*this.STD_DELAY + this.MEAN_DELAY ))
                         * 60 );
             final int[] user = new int[] { uid, uq, ue, ul, uo, ud, ub };
             if (uq < 0) {
               this.insertServer(user);
             } else {
               this.insertRequest(user);
             }
           }
         }
  public void loadRoadNetworkFromFile(final String f_rnet)
         throws FileNotFoundException, SQLException {
           try {
             this.storage.DBInsertVertex(0, 0, 0);
           } catch (DuplicateVertexException e) {
             // ...
           }
           Scanner sc = new Scanner(new File(f_rnet));
           while (sc.hasNext()) {
             final int col0 = sc.nextInt();
             final int col1 = sc.nextInt();
             final int col2 = sc.nextInt();
             final int col3 = (int) Math.round(sc.nextDouble()*CSHIFT);
             final int col4 = (int) Math.round(sc.nextDouble()*CSHIFT);
             final int col5 = (int) Math.round(sc.nextDouble()*CSHIFT);
             final int col6 = (int) Math.round(sc.nextDouble()*CSHIFT);
             final int dist = this.tools.computeHaversine(
                   col3/CSHIFT, col4/CSHIFT, col5/CSHIFT, col6/CSHIFT);
             try {
               this.storage.DBInsertVertex(col1, col3, col4);
             } catch (DuplicateVertexException e) { /*...*/ }
             try {
               this.storage.DBInsertVertex(col2, col5, col6);
             } catch (DuplicateVertexException e) { /*...*/ }
             try {
               this.storage.DBInsertEdge(col1, col2, dist, 10);  // TODO: 10 speed
             } catch (DuplicateEdgeException e) { /*...*/ }
             try {
               this.storage.DBInsertEdge(col1, 0, 0, 10);
             } catch (DuplicateEdgeException e) { /*...*/ }
             try {
               this.storage.DBInsertEdge(col2, 0, 0, 10);
             } catch (DuplicateEdgeException e) { /*...*/ }
           }
           this.tools.setRefCacheVertices(this.storage.getRefCacheVertices());
           this.tools.setRefCacheEdges(this.storage.getRefCacheEdges());
         }
  public void kill() {
           this.kill = true;
         }
  public final boolean isKilled() {
           return this.kill;
         }
  public void returnRequest(final int[] r) {
           if (this.simClock - r[2] < QUEUE_TIMEOUT) {
             this.lu_rseen.put(r[0], false);
           }
         }
  public void startRealtime(final Consumer<Boolean> app_cb) {
           this.kill = false;
           this.lu_rseen.clear();
           this.lu_sseen.clear();
           if (DEBUG) {
             System.out.printf("startRealtime(1)\n");
           }

           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           if (DEBUG) {
             System.out.printf("setRequestTimeout(1), arg1=%d\n", REQUEST_TIMEOUT);
           }

           this.simClock = CLOCK_START;
           if (DEBUG) {
             System.out.printf("simClock=%d\n", CLOCK_START);
           }

           int simulation_duration = (CLOCK_END - CLOCK_START);
           if (DEBUG) {
             System.out.printf("simulation_duration=%d\n", simulation_duration);
           }

           this.exe = Executors.newScheduledThreadPool(5);
           if (DEBUG) {
             System.out.printf("newScheduledThreadPool(1), arg1=5\n");
           }

           this.cb1 = exe.scheduleAtFixedRate(
             this.ClockLoop, 0, 1, TimeUnit.SECONDS);
           if (DEBUG) {
             System.out.printf("exe ClockLoop, delay=0, int=1\n");
           }

           this.cb2 = exe.scheduleAtFixedRate(
             this.RequestCollectionLoop, this.loop_delay, REQUEST_COLLECTION_PERIOD, TimeUnit.SECONDS);
           if (DEBUG) {
             System.out.printf("exe RequestCollectionLoop, delay=%d, int=%d\n",
                 this.loop_delay, REQUEST_COLLECTION_PERIOD);
           }

           this.cb3 = exe.scheduleAtFixedRate(
             this.RequestHandlingLoop, this.loop_delay, REQUEST_HANDLING_PERIOD, TimeUnit.MILLISECONDS);
           if (DEBUG) {
             System.out.printf("exe RequestHandlingLoop, delay=%d, int=%d\n",
                 this.loop_delay, REQUEST_HANDLING_PERIOD);
           }

           this.cb4 = exe.scheduleAtFixedRate(
             this.ServerLoop, this.loop_delay, SERVER_COLLECTION_PERIOD, TimeUnit.SECONDS);
           if (DEBUG) {
             System.out.printf("exe ServerLoop, delay=%d, int=%d\n",
                 this.loop_delay, SERVER_COLLECTION_PERIOD);
           }

           this.exe.schedule(() -> {
             this.stop(app_cb);
           }, simulation_duration, TimeUnit.SECONDS);
           if (DEBUG) {
             System.out.printf("exe stop, delay=%d\n",
                 simulation_duration);
           }
         }
  public void startSequential(final Consumer<Boolean> app_cb) throws Exception {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.simClock = CLOCK_START;
           this.kill = false;
           this.lu_rseen.clear();
           this.lu_sseen.clear();
           if (DEBUG) {
             System.out.printf("startSequential(1)\n");
             System.out.printf("clock set to %d..%d\n", simClock, CLOCK_END);
           }
           while (!kill && this.simClock < CLOCK_END) {
             this.working = true;
             this.step();
             this.working = false;
           }
           this.stop(app_cb);
         }
  public void step() {
           this.ClockLoop.run();
           this.ServerLoop.run();
           this.RequestCollectionLoop.run();
           this.RequestHandlingLoop.run();
         }
  public void stop(final Consumer<Boolean> app_cb) {
           if (DEBUG) {
             System.out.printf("stop(1)\n");
           }
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
