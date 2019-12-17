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
import com.github.jargors.jmx.*;
import java.lang.management.*;
import javax.management.*;
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
    this.statControllerClock++;
    this.statControllerClockReferenceSecond++;
    if (this.statControllerClockReferenceSecond > 59) {
      this.statControllerClockReferenceSecond = 0;
      this.statControllerClockReferenceMinute++;
      if (this.statControllerClockReferenceMinute > 59) {
        this.statControllerClockReferenceMinute = 0;
        this.statControllerClockReferenceHour++;
        if (this.statControllerClockReferenceHour > 23) {
          this.statControllerClockReferenceHour = 0;
          this.statControllerClockReferenceDay++;
        }
      }
    }
  };
  private Runnable RequestCollectionLoop = () -> {
    long A0 = System.currentTimeMillis();
    int  A1 = 0;
    int  A2 = 0;
    final int now = this.statControllerClock;
    try {
      A2 = this.client.dropRequests(now - QUEUE_TIMEOUT);
      int[] output = this.storage.DBQueryRequestsQueued(now);
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
          A1++;
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
    this.statControllerRequestCollectionSize = A1;
    this.statControllerRequestCollectionDropped = A2;
    this.statControllerRequestCollectionDur = (System.currentTimeMillis() - A0);
  };
  private Runnable RequestHandlingLoop = () -> {
    try {
      this.client.notifyNew();  // blocks this thread until queue is empty
    } catch (ClientException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientException: %s\n",
          this.statControllerClock, e.toString());
      e.printStackTrace();
    } catch (ClientFatalException e) {
      System.err.printf("[t=%d] Controller.RequestHandlingLoop caught a ClientFatalException: %s\n",
          this.statControllerClock, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
  };
  private Runnable ServerLoop = () -> {
    try {
      int[] output = this.storage.DBQueryServersLocationsActive(this.statControllerClock);
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
  };
  private int    statControllerClock;
  private int    statControllerClockReferenceDay;
  private int    statControllerClockReferenceHour;
  private int    statControllerClockReferenceMinute;
  private int    statControllerClockReferenceSecond;
  private int    statControllerRequestCollectionSize = 0;
  private int    statControllerRequestCollectionDropped = 0;
  private long   statControllerRequestCollectionDur = 0;
  private long statQueryDur = 0;
  private long statQueryEdgeDur = 0;
  private long statQueryEdgeStatisticsDur = 0;
  private long statQueryEdgesDur = 0;
  private long statQueryEdgesCountDur = 0;
  private long statQueryMBRDur = 0;
  private long statQueryMetricRequestDistanceBaseTotalDur = 0;
  private long statQueryMetricRequestDistanceBaseUnassignedTotalDur = 0;
  private long statQueryMetricRequestDistanceDetourTotalDur = 0;
  private long statQueryMetricRequestDistanceTransitTotalDur = 0;
  private long statQueryMetricRequestDurationPickupTotalDur = 0;
  private long statQueryMetricRequestDurationTransitTotalDur = 0;
  private long statQueryMetricRequestDurationTravelTotalDur = 0;
  private long statQueryMetricRequestTWViolationsTotalDur = 0;
  private long statQueryMetricServerDistanceBaseTotalDur = 0;
  private long statQueryMetricServerDistanceCruisingTotalDur = 0;
  private long statQueryMetricServerDistanceServiceTotalDur = 0;
  private long statQueryMetricServerDistanceTotalDur = 0;
  private long statQueryMetricServerDurationCruisingTotalDur = 0;
  private long statQueryMetricServerDurationServiceTotalDur = 0;
  private long statQueryMetricServerDurationTravelTotalDur = 0;
  private long statQueryMetricServerTWViolationsTotalDur = 0;
  private long statQueryMetricServiceRateDur = 0;
  private long statQueryMetricUserDistanceBaseTotalDur = 0;
  private long statQueryRequestTimeOfArrivalDur = 0;
  private long statQueryRequestTimeOfDepartureDur = 0;
  private long statQueryRequestsCountDur = 0;
  private long statQueryRequestsCountActiveDur = 0;
  private long statQueryRequestsCountCompletedDur = 0;
  private long statQueryRequestsQueuedDur = 0;
  private long statQueryServerRouteDur = 0;
  private long statQueryServerRouteActiveDur = 0;
  private long statQueryServerRouteRemainingDur = 0;
  private long statQueryServerScheduleDur = 0;
  private long statQueryServerTimeOfDepartureDur = 0;
  private long statQueryServersActiveDur = 0;
  private long statQueryServersCountDur = 0;
  private long statQueryServersCountActiveDur = 0;
  private long statQueryServersLocationsActiveDur = 0;
  private long statQueryUserDur = 0;
  private long statQueryVertexDur = 0;
  private long statQueryVerticesDur = 0;
  private long statQueryVerticesCountDur = 0;
  public Controller() {
    this.storage = new Storage();
    this.communicator = new Communicator();
    this.communicator.setRefStorage(this.storage);
    this.communicator.setRefController(this);
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ControllerMonitor mon = new ControllerMonitor(this);
      mbs.registerMBean(mon, new ObjectName("com.github.jargors.jmx:type=ControllerMonitor"));
    } catch (InstanceAlreadyExistsException e) {
      // ...
    } catch (Exception e) {
      System.err.printf("ControllerMonitor failed; reason: %s\n", e.toString());
      System.err.printf("Continuing with monitoring disabled\n");
    }
  }
  public int    getStatControllerClock() {
           return this.statControllerClock;
         }
  public int    getStatControllerClockReferenceDay() {
           return this.statControllerClockReferenceDay;
         }
  public int    getStatControllerClockReferenceHour() {
           return this.statControllerClockReferenceHour;
         }
  public int    getStatControllerClockReferenceMinute() {
           return this.statControllerClockReferenceMinute;
         }
  public int    getStatControllerClockReferenceSecond() {
           return this.statControllerClockReferenceSecond;
         }
  public int    getStatControllerRequestCollectionSize() {
           return this.statControllerRequestCollectionSize;
         }
  public int    getStatControllerRequestCollectionDropped() {
           return this.statControllerRequestCollectionDropped;
         }
  public long   getStatControllerRequestCollectionDur() {
           return this.statControllerRequestCollectionDur;
         }
  public long getStatQueryDur() {
           return this.statQueryDur;
         }
  public long getStatQueryEdgeDur() {
           return this.statQueryEdgeDur;
         }
  public long getStatQueryEdgeStatisticsDur() {
           return this.statQueryEdgeStatisticsDur;
         }
  public long getStatQueryEdgesCountDur() {
           return this.statQueryEdgesCountDur;
         }
  public long getStatQueryEdgesDur() {
           return this.statQueryEdgesDur;
         }
  public long getStatQueryMBRDur() {
           return this.statQueryMBRDur;
         }
  public long getStatQueryMetricRequestDistanceBaseTotalDur() {
           return this.statQueryMetricRequestDistanceBaseTotalDur;
         }
  public long getStatQueryMetricRequestDistanceBaseUnassignedTotalDur() {
           return this.statQueryMetricRequestDistanceBaseUnassignedTotalDur;
         }
  public long getStatQueryMetricRequestDistanceDetourTotalDur() {
           return this.statQueryMetricRequestDistanceDetourTotalDur;
         }
  public long getStatQueryMetricRequestDistanceTransitTotalDur() {
           return this.statQueryMetricRequestDistanceTransitTotalDur;
         }
  public long getStatQueryMetricRequestDurationPickupTotalDur() {
           return this.statQueryMetricRequestDurationPickupTotalDur;
         }
  public long getStatQueryMetricRequestDurationTransitTotalDur() {
           return this.statQueryMetricRequestDurationTransitTotalDur;
         }
  public long getStatQueryMetricRequestDurationTravelTotalDur() {
           return this.statQueryMetricRequestDurationTravelTotalDur;
         }
  public long getStatQueryMetricRequestTWViolationsTotalDur() {
           return this.statQueryMetricRequestTWViolationsTotalDur;
         }
  public long getStatQueryMetricServerDistanceBaseTotalDur() {
           return this.statQueryMetricServerDistanceBaseTotalDur;
         }
  public long getStatQueryMetricServerDistanceCruisingTotalDur() {
           return this.statQueryMetricServerDistanceCruisingTotalDur;
         }
  public long getStatQueryMetricServerDistanceServiceTotalDur() {
           return this.statQueryMetricServerDistanceServiceTotalDur;
         }
  public long getStatQueryMetricServerDistanceTotalDur() {
           return this.statQueryMetricServerDistanceTotalDur;
         }
  public long getStatQueryMetricServerDurationCruisingTotalDur() {
           return this.statQueryMetricServerDurationCruisingTotalDur;
         }
  public long getStatQueryMetricServerDurationServiceTotalDur() {
           return this.statQueryMetricServerDurationServiceTotalDur;
         }
  public long getStatQueryMetricServerDurationTravelTotalDur() {
           return this.statQueryMetricServerDurationTravelTotalDur;
         }
  public long getStatQueryMetricServerTWViolationsTotalDur() {
           return this.statQueryMetricServerTWViolationsTotalDur;
         }
  public long getStatQueryMetricServiceRateDur() {
           return this.statQueryMetricServiceRateDur;
         }
  public long getStatQueryMetricUserDistanceBaseTotalDur() {
           return this.statQueryMetricUserDistanceBaseTotalDur;
         }
  public long getStatQueryRequestTimeOfArrivalDur() {
           return this.statQueryRequestTimeOfArrivalDur;
         }
  public long getStatQueryRequestTimeOfDepartureDur() {
           return this.statQueryRequestTimeOfDepartureDur;
         }
  public long getStatQueryRequestsCountDur() {
           return this.statQueryRequestsCountDur;
         }
  public long getStatQueryRequestsCountActiveDur() {
           return this.statQueryRequestsCountActiveDur;
         }
  public long getStatQueryRequestsCountCompletedDur() {
           return this.statQueryRequestsCountCompletedDur;
         }
  public long getStatQueryRequestsQueuedDur() {
           return this.statQueryRequestsQueuedDur;
         }
  public long getStatQueryServerRouteActiveDur() {
           return this.statQueryServerRouteActiveDur;
         }
  public long getStatQueryServerRouteDur() {
           return this.statQueryServerRouteDur;
         }
  public long getStatQueryServerRouteRemainingDur() {
           return this.statQueryServerRouteRemainingDur;
         }
  public long getStatQueryServerScheduleDur() {
           return this.statQueryServerScheduleDur;
         }
  public long getStatQueryServerTimeOfDepartureDur() {
           return this.statQueryServerTimeOfDepartureDur;
         }
  public long getStatQueryServersActiveDur() {
           return this.statQueryServersActiveDur;
         }
  public long getStatQueryServersCountDur() {
           return this.statQueryServersCountDur;
         }
  public long getStatQueryServersCountActiveDur() {
           return this.statQueryServersCountActiveDur;
         }
  public long getStatQueryServersLocationsActiveDur() {
           return this.statQueryServersLocationsActiveDur;
         }
  public long getStatQueryUserDur() {
           return this.statQueryUserDur;
         }
  public long getStatQueryVertexDur() {
           return this.statQueryVertexDur;
         }
  public long getStatQueryVerticesCountDur() {
           return this.statQueryVerticesCountDur;
         }
  public long getStatQueryVerticesDur() {
           return this.statQueryVerticesDur;
         }
  public int[] query(final String sql, final int ncols) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQuery(sql, ncols);
           this.statQueryDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryEdge(v1, v2);
           this.statQueryEdgeDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryEdgeStatistics() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryEdgeStatistics();
           this.statQueryEdgeStatisticsDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryEdges() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryEdges();
           this.statQueryEdgesDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryEdgesCount() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryEdgesCount();
           this.statQueryEdgesCountDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMBR() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryMBR();
           this.statQueryMBRDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDistanceBaseTotal() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDistanceBaseTotal();
           this.statQueryMetricRequestDistanceBaseTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDistanceBaseUnassignedTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(flag_usecache);
           this.statQueryMetricRequestDistanceBaseUnassignedTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDistanceDetourTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDistanceDetourTotal(flag_usecache);
           this.statQueryMetricRequestDistanceDetourTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDistanceTransitTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDistanceTransitTotal(flag_usecache);
           this.statQueryMetricRequestDistanceTransitTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDurationPickupTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDurationPickupTotal(flag_usecache);
           this.statQueryMetricRequestDurationPickupTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDurationTransitTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDurationTransitTotal(flag_usecache);
           this.statQueryMetricRequestDurationTransitTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestDurationTravelTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestDurationTravelTotal(flag_usecache);
           this.statQueryMetricRequestDurationTravelTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricRequestTWViolationsTotal() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricRequestTWViolationsTotal();
           this.statQueryMetricRequestTWViolationsTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDistanceBaseTotal() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDistanceBaseTotal();
           this.statQueryMetricServerDistanceBaseTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDistanceCruisingTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDistanceCruisingTotal(flag_usecache);
           this.statQueryMetricServerDistanceCruisingTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDistanceServiceTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDistanceServiceTotal(flag_usecache);
           this.statQueryMetricServerDistanceServiceTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDistanceTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDistanceTotal(flag_usecache);
           this.statQueryMetricServerDistanceTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDurationCruisingTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDurationCruisingTotal(flag_usecache);
           this.statQueryMetricServerDurationCruisingTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDurationServiceTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDurationServiceTotal(flag_usecache);
           this.statQueryMetricServerDurationServiceTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerDurationTravelTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerDurationTravelTotal(flag_usecache);
           this.statQueryMetricServerDurationTravelTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServerTWViolationsTotal() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServerTWViolationsTotal();
           this.statQueryMetricServerTWViolationsTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricServiceRate(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricServiceRate(flag_usecache);
           this.statQueryMetricServiceRateDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryMetricUserDistanceBaseTotal(boolean flag_usecache) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryMetricUserDistanceBaseTotal(flag_usecache);
           this.statQueryMetricUserDistanceBaseTotalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestTimeOfArrival(final int rid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestTimeOfArrival(rid);
           this.statQueryRequestTimeOfArrivalDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestTimeOfDeparture(final int rid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestTimeOfDeparture(rid);
           this.statQueryRequestTimeOfDepartureDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestsCount() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestsCount();
           this.statQueryRequestsCountDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestsCountActive(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestsCountActive(t);
           this.statQueryRequestsCountActiveDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestsCountCompleted(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestsCountCompleted(t);
           this.statQueryRequestsCountCompletedDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryRequestsQueued(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryRequestsQueued(t);
           this.statQueryRequestsQueuedDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServerRoute(final int sid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryServerRoute(sid);
           this.statQueryServerRouteDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServerRouteActive(final int sid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServerRouteActive(sid);
           this.statQueryServerRouteActiveDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServerRouteRemaining(final int sid, final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServerRouteRemaining(sid, t);
           this.statQueryServerRouteRemainingDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServerSchedule(final int sid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryServerSchedule(sid);
           this.statQueryServerScheduleDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServerTimeOfDeparture(final int sid) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryServerTimeOfDeparture(sid);
           this.statQueryServerTimeOfDepartureDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServersActive(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServersActive(t);
           this.statQueryServersActiveDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServersCount() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryServersCount();
           this.statQueryServersCountDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServersCountActive(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServersCountActive(t);
           this.statQueryServersCountActiveDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryServersLocationsActive(final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServersLocationsActive(t);
           this.statQueryServersLocationsActiveDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = storage.DBQueryUser(rid);
           this.statQueryUserDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryVertex(v);
           this.statQueryVertexDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryVertices() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryVertices();
           this.statQueryVerticesDur = (System.currentTimeMillis() - A0);
           return output;
         }
  public int[] queryVerticesCount() throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryVerticesCount();
           this.statQueryVerticesCountDur = (System.currentTimeMillis() - A0);
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
  public void instanceLoadInMem(final String p) throws SQLException {
           this.storage.JargoInstanceLoadInMem(p);
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
  public int getClock() {
           return this.statControllerClock;
         }
  public Communicator getRefCommunicator() {
           return this.communicator;
         }
  public Storage getRefStorage() {
           return this.storage;
         }
  public int retrieveQueueSize() {
           return this.client.getStatClientQueueSize();
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
           this.statControllerClockReferenceHour= hour;
           this.statControllerClockReferenceMinute = minute;
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
           if (this.statControllerClock - r[2] < QUEUE_TIMEOUT) {
             this.lu_seen.put(r[0], false);
           }
         }
  public void startRealtime(final Consumer<Boolean> app_cb) {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.statControllerClock = CLOCK_START;

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
  public void startSequential(final Consumer<Boolean> app_cb) throws Exception {
           this.storage.setRequestTimeout(REQUEST_TIMEOUT);
           this.statControllerClock = CLOCK_START;
           while (!kill && this.statControllerClock < CLOCK_END) {
             this.working = true;
             this.ClockLoop.run();  // this.statControllerClock gets incremented here!
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
