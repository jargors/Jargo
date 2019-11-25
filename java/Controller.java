/*line 23 "src/Controller.nw"*/
package com.github.jargors;
/*line 29 "src/Controller.nw"*/
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
/*line 46 "src/Controller.nw"*/
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
/*line 55 "src/Controller.nw"*/
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
/*line 60 "src/Controller.nw"*/
import java.util.Map;
import java.util.HashMap;
/*line 64 "src/Controller.nw"*/
import java.sql.SQLException;
/*line 6 "src/Controller.nw"*/
public class Controller {
  
/*line 78 "src/Controller.nw"*/
private Storage storage;
private Communicator communicator;
private Tools tools = new Tools();
private Client client;
private Map<Integer, Boolean> lu_seen = new HashMap<Integer, Boolean>();
/*line 87 "src/Controller.nw"*/
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
/*line 106 "src/Controller.nw"*/
private int clock_now = 0;
/*line 112 "src/Controller.nw"*/
private int loop_delay = 0;
// private int deviation_rate = 0.02;
// private int breakdown_rate = 0.005;
/*line 119 "src/Controller.nw"*/
private final double CSHIFT = Storage.CSHIFT;
private boolean kill = false;
private boolean working = false;
private ScheduledExecutorService exe = null;
private ScheduledFuture<?> cb1 = null;
private ScheduledFuture<?> cb2 = null;
private ScheduledFuture<?> cb3 = null;
private ScheduledFuture<?> cb4 = null;
private ScheduledFuture<?> cb5 = null;
/*line 133 "src/Controller.nw"*/
private final boolean DEBUG =
    "true".equals(System.getProperty("jargors.controller.debug"));
/*line 164 "src/Controller.nw"*/
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
  ++(this.clock_now);
  if (DEBUG) {
    System.err.printf("[t=%d] Controller.ClockLoop says: %s!\n",
        this.clock_now, (this.clock_now % 2 == 0 ? "ping" : "pong"));
  }
};
/*line 206 "src/Controller.nw"*/
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
/*line 266 "src/Controller.nw"*/
private Runnable RequestHandlingLoop = () -> {
  long A0 = 0;
  int  A1 = 0;
  if (DEBUG) {
    A0 = System.currentTimeMillis();
    A1 = this.client.getQueueSize();
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
/*line 316 "src/Controller.nw"*/
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
/*line 8 "src/Controller.nw"*/
  
/*line 363 "src/Controller.nw"*/
public Controller() {
  this.storage = new Storage();
  this.communicator = new Communicator();
  this.communicator.setRefStorage(this.storage);
  this.communicator.setRefController(this);
}
/*line 9 "src/Controller.nw"*/
  
/*line 104 "src/tex/0-Overview.nw"*/
public 
/*line 73 "src/tex/2-Reading.nw"*/
int[] query(final String sql, final int ncols) throws SQLException {
  return this.storage.DBQuery(sql, ncols);
}
/*line 105 "src/tex/0-Overview.nw"*/
public 
/*line 302 "src/tex/2-Reading.nw"*/
int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
  return this.storage.DBQueryEdge(v1, v2);
}
/*line 106 "src/tex/0-Overview.nw"*/
public 
/*line 572 "src/tex/2-Reading.nw"*/
int[] queryEdgeStatistics() throws SQLException {
  return storage.DBQueryEdgeStatistics();
}
/*line 107 "src/tex/0-Overview.nw"*/
public 
/*line 348 "src/tex/2-Reading.nw"*/
int[] queryEdges() throws SQLException {
  return this.storage.DBQueryEdges();
}
/*line 108 "src/tex/0-Overview.nw"*/
public 
/*line 391 "src/tex/2-Reading.nw"*/
int[] queryEdgesCount() throws SQLException {
  return this.storage.DBQueryEdgesCount();
}
/*line 109 "src/tex/0-Overview.nw"*/
public 
/*line 121 "src/tex/2-Reading.nw"*/
int[] queryMBR() throws SQLException {
  return this.storage.DBQueryMBR();
}
/*line 110 "src/tex/0-Overview.nw"*/
public 
/*line 2332 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDistanceBaseTotal() throws SQLException {
  return storage.DBQueryMetricRequestDistanceBaseTotal();
}
/*line 111 "src/tex/0-Overview.nw"*/
public 
/*line 2377 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDistanceBaseUnassignedTotal() throws SQLException {
  return storage.DBQueryMetricRequestDistanceBaseUnassignedTotal();
}
/*line 112 "src/tex/0-Overview.nw"*/
public 
/*line 2420 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDistanceDetourTotal() throws SQLException {
  return storage.DBQueryMetricRequestDistanceDetourTotal();
}
/*line 113 "src/tex/0-Overview.nw"*/
public 
/*line 2463 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDistanceTransitTotal() throws SQLException {
  return storage.DBQueryMetricRequestDistanceTransitTotal();
}
/*line 114 "src/tex/0-Overview.nw"*/
public 
/*line 2506 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDurationPickupTotal() throws SQLException {
  return storage.DBQueryMetricRequestDurationPickupTotal();
}
/*line 115 "src/tex/0-Overview.nw"*/
public 
/*line 2549 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDurationTransitTotal() throws SQLException {
  return storage.DBQueryMetricRequestDurationTransitTotal();
}
/*line 116 "src/tex/0-Overview.nw"*/
public 
/*line 2592 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestDurationTravelTotal() throws SQLException {
  return storage.DBQueryMetricRequestDurationTravelTotal();
}
/*line 117 "src/tex/0-Overview.nw"*/
public 
/*line 2608 "src/tex/2-Reading.nw"*/
int[] queryMetricRequestTWViolationsTotal() throws SQLException {
  return storage.DBQueryMetricRequestTWViolationsTotal();
}
/*line 118 "src/tex/0-Overview.nw"*/
public 
/*line 2144 "src/tex/2-Reading.nw"*/
int[] queryMetricServerDistanceBaseTotal() throws SQLException {
  return storage.DBQueryMetricServerDistanceBaseTotal();
}
/*line 119 "src/tex/0-Overview.nw"*/
public 
/*line 2187 "src/tex/2-Reading.nw"*/
int[] queryMetricServerDistanceCruisingTotal() throws SQLException {
  return storage.DBQueryMetricServerDistanceCruisingTotal();
}
/*line 120 "src/tex/0-Overview.nw"*/
public 
/*line 2230 "src/tex/2-Reading.nw"*/
int[] queryMetricServerDistanceServiceTotal() throws SQLException {
  return storage.DBQueryMetricServerDistanceServiceTotal();
}
/*line 121 "src/tex/0-Overview.nw"*/
public 
/*line 2101 "src/tex/2-Reading.nw"*/
int[] queryMetricServerDistanceTotal() throws SQLException {
  return storage.DBQueryMetricServerDistanceTotal();
}
/*line 122 "src/tex/0-Overview.nw"*/
public 
/*line 2273 "src/tex/2-Reading.nw"*/
int[] queryMetricServerDurationTravelTotal() throws SQLException {
  return storage.DBQueryMetricServerDurationTravelTotal();
}
/*line 123 "src/tex/0-Overview.nw"*/
public 
/*line 2289 "src/tex/2-Reading.nw"*/
int[] queryMetricServerTWViolationsTotal() throws SQLException {
  return storage.DBQueryMetricServerTWViolationsTotal();
}
/*line 124 "src/tex/0-Overview.nw"*/
public 
/*line 2015 "src/tex/2-Reading.nw"*/
int[] queryMetricServiceRate() throws SQLException {
  return storage.DBQueryMetricServiceRate();
}
/*line 125 "src/tex/0-Overview.nw"*/
public 
/*line 2058 "src/tex/2-Reading.nw"*/
int[] queryMetricUserDistanceBaseTotal() throws SQLException {
  return storage.DBQueryMetricUserDistanceBaseTotal();
}
/*line 126 "src/tex/0-Overview.nw"*/
public 
/*line 898 "src/tex/2-Reading.nw"*/
int[] queryRequestTimeOfArrival(final int rid) throws SQLException {
  return storage.DBQueryRequestTimeOfArrival(rid);
}
/*line 127 "src/tex/0-Overview.nw"*/
public 
/*line 849 "src/tex/2-Reading.nw"*/
int[] queryRequestTimeOfDeparture(final int rid) throws SQLException {
  return storage.DBQueryRequestTimeOfDeparture(rid);
}
/*line 128 "src/tex/0-Overview.nw"*/
public 
/*line 941 "src/tex/2-Reading.nw"*/
int[] queryRequestsCount() throws SQLException {
  return storage.DBQueryRequestsCount();
}
/*line 129 "src/tex/0-Overview.nw"*/
public 
/*line 1022 "src/tex/2-Reading.nw"*/
int[] queryRequestsQueued(final int t) throws SQLException {
  return storage.DBQueryRequestsQueued(t);
}
/*line 130 "src/tex/0-Overview.nw"*/
public 
/*line 1074 "src/tex/2-Reading.nw"*/
int[] queryServerRoute(final int sid) throws SQLException {
  return storage.DBQueryServerRoute(sid);
}
/*line 131 "src/tex/0-Overview.nw"*/
public 
/*line 1188 "src/tex/2-Reading.nw"*/
int[] queryServerSchedule(final int sid) throws SQLException {
  return storage.DBQueryServerSchedule(sid);
}
/*line 132 "src/tex/0-Overview.nw"*/
public 
/*line 1619 "src/tex/2-Reading.nw"*/
int[] queryServerTimeOfDeparture(final int sid) throws SQLException {
  return storage.DBQueryServerTimeOfDeparture(sid);
}
/*line 133 "src/tex/0-Overview.nw"*/
public 
/*line 1800 "src/tex/2-Reading.nw"*/
int[] queryServersCount() throws SQLException {
  return storage.DBQueryServersCount();
}
/*line 134 "src/tex/0-Overview.nw"*/
public 
/*line 481 "src/tex/2-Reading.nw"*/
int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
  return storage.DBQueryUser(rid);
}
/*line 135 "src/tex/0-Overview.nw"*/
public 
/*line 168 "src/tex/2-Reading.nw"*/
int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
  return this.storage.DBQueryVertex(v);
}
/*line 136 "src/tex/0-Overview.nw"*/
public 
/*line 213 "src/tex/2-Reading.nw"*/
int[] queryVertices() throws SQLException {
  return this.storage.DBQueryVertices();
}
/*line 137 "src/tex/0-Overview.nw"*/
public 
/*line 256 "src/tex/2-Reading.nw"*/
int[] queryVerticesCount() throws SQLException {
  return this.storage.DBQueryVerticesCount();
}
/*line 141 "src/tex/0-Overview.nw"*/
public 
/*line 544 "src/tex/3-Writing.nw"*/
void insertRequest(final int[] u) throws DuplicateUserException, SQLException {
  this.storage.DBInsertRequest(u);
}
/*line 142 "src/tex/0-Overview.nw"*/
public 
/*line 630 "src/tex/3-Writing.nw"*/
void insertServer(final int[] u)
throws DuplicateUserException, EdgeNotFoundException, SQLException,
       GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
  this.storage.DBInsertServer(u, this.tools.computeRoute(u[4], u[5], u[2]));
}
/*line 143 "src/tex/0-Overview.nw"*/
public 
/*line 442 "src/Controller.nw"*/
void loadProblem(String p)
throws FileNotFoundException, DuplicateUserException, EdgeNotFoundException, SQLException,
       GtreeNotLoadedException, GtreeIllegalSourceException, GtreeIllegalTargetException {
  Scanner sc = new Scanner(new File(p));
  
/*line 464 "src/Controller.nw"*/
for (int i = 0; i < 6; i++) {
  sc.nextLine();
}
/*line 447 "src/Controller.nw"*/
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
/*line 144 "src/tex/0-Overview.nw"*/
public 
/*line 375 "src/Controller.nw"*/
void loadRoadNetworkFromFile(final String f_rnet) throws FileNotFoundException, SQLException {
  Scanner sc = new Scanner(new File(f_rnet));
  while (sc.hasNext()) {
/*line 383 "src/Controller.nw"*/
final int col0 = sc.nextInt();
final int col1 = sc.nextInt();
final int col2 = sc.nextInt();
final int col3 = (col1 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
final int col4 = (col1 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
final int col5 = (col2 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
final int col6 = (col2 == 0 ? (int) (0*sc.nextDouble()) : (int) Math.round(sc.nextDouble()*CSHIFT));
/*line 393 "src/Controller.nw"*/
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
/*line 415 "src/Controller.nw"*/
final int dist = ((col1 != 0 && col2 != 0)
  ? this.tools.computeHaversine(
        col3/CSHIFT, col4/CSHIFT,
        col5/CSHIFT, col6/CSHIFT) : 0);
/*line 424 "src/Controller.nw"*/
try {
  this.storage.DBInsertEdge(col1, col2, dist, 10);
} catch (DuplicateEdgeException e) {
  if (DEBUG) {
    // System.err.println("Warning! Duplicate edge ignored.");
  }
}
/*line 434 "src/Controller.nw"*/
  }
  this.tools.setRefCacheVertices(this.storage.getRefCacheVertices());
  this.tools.setRefCacheEdges(this.storage.getRefCacheEdges());
}
/*line 148 "src/tex/0-Overview.nw"*/
public 
/*line 365 "src/tex/4-Administration.nw"*/
void cacheRoadNetworkFromDB() throws SQLException {
  this.storage.JargoCacheRoadNetworkFromDB();
}
/*line 149 "src/tex/0-Overview.nw"*/
public 
/*line 439 "src/tex/4-Administration.nw"*/
void cacheUsersFromDB() throws SQLException {
  this.storage.JargoCacheUsersFromDB();
}
/*line 150 "src/tex/0-Overview.nw"*/
public 
/*line 285 "src/tex/4-Administration.nw"*/
void instanceClose() throws SQLException {
  this.storage.JargoInstanceClose();
}
/*line 151 "src/tex/0-Overview.nw"*/
public 
/*line 246 "src/tex/4-Administration.nw"*/
void instanceExport(final String p) throws SQLException {
  this.storage.JargoInstanceExport(p);
}
/*line 152 "src/tex/0-Overview.nw"*/
public 
/*line 162 "src/tex/4-Administration.nw"*/
void instanceInitialize() {
  this.storage.JargoInstanceInitialize();
}
/*line 153 "src/tex/0-Overview.nw"*/
public 
/*line 208 "src/tex/4-Administration.nw"*/
void instanceLoad(final String p) throws SQLException {
  this.storage.JargoInstanceLoad(p);
}
/*line 154 "src/tex/0-Overview.nw"*/
public 
/*line 65 "src/tex/4-Administration.nw"*/
void instanceNew() throws SQLException {
  this.storage.JargoInstanceNew();
}
/*line 155 "src/tex/0-Overview.nw"*/
public 
/*line 52 "src/tex/5-Gtree.nw"*/
void gtreeClose() {
  this.tools.GTGtreeClose();
}
/*line 156 "src/tex/0-Overview.nw"*/
public 
/*line 33 "src/tex/5-Gtree.nw"*/
void gtreeLoad(String p) throws FileNotFoundException {
  this.tools.GTGtreeLoad(p);
}
/*line 157 "src/tex/0-Overview.nw"*/
public 
/*line 1054 "src/tex/4-Administration.nw"*/
int getClockNow() {
  return this.clock_now;
}
/*line 158 "src/tex/0-Overview.nw"*/
public 
/*line 1040 "src/tex/4-Administration.nw"*/
Communicator getRefCommunicator() {
  return this.communicator;
}
/*line 159 "src/tex/0-Overview.nw"*/
public 
/*line 1047 "src/tex/4-Administration.nw"*/
Storage getRefStorage() {
  return this.storage;
}
/*line 160 "src/tex/0-Overview.nw"*/
public 
/*line 1068 "src/tex/4-Administration.nw"*/
int retrieveQueueSize() {
  return this.client.getQueueSize();
}
/*line 161 "src/tex/0-Overview.nw"*/
public 
/*line 1089 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> retrieveRefCacheEdges() {
  return this.storage.getRefCacheEdges();
}
/*line 162 "src/tex/0-Overview.nw"*/
public 
/*line 1096 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> retrieveRefCacheUsers() {
  return this.storage.getRefCacheUsers();
}
/*line 163 "src/tex/0-Overview.nw"*/
public 
/*line 1082 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> retrieveRefCacheVertices() {
  return this.storage.getRefCacheVertices();
}
/*line 164 "src/tex/0-Overview.nw"*/
public 
/*line 1200 "src/tex/4-Administration.nw"*/
void forwardRefCommunicator(final Communicator communicator) {
  this.client.setRefCommunicator(communicator);
}
/*line 165 "src/tex/0-Overview.nw"*/
public 
/*line 1193 "src/tex/4-Administration.nw"*/
void forwardRefTraffic(final Traffic traffic) {
  this.communicator.setRefTraffic(traffic);
}
/*line 166 "src/tex/0-Overview.nw"*/
public 
/*line 1113 "src/tex/4-Administration.nw"*/
void setClockEnd(final int clock_end) {
  this.CLOCK_END = clock_end;
}
/*line 167 "src/tex/0-Overview.nw"*/
public 
/*line 1106 "src/tex/4-Administration.nw"*/
void setClockStart(final int clock_start) {
  this.CLOCK_START = clock_start;
}
/*line 168 "src/tex/0-Overview.nw"*/
public 
/*line 1120 "src/tex/4-Administration.nw"*/
void setQueueTimeout(final int queue_timeout) {
  this.QUEUE_TIMEOUT = queue_timeout;
}
/*line 169 "src/tex/0-Overview.nw"*/
public 
/*line 1156 "src/tex/4-Administration.nw"*/
void setRefClient(final Client client) {
  this.client = client;
}
/*line 170 "src/tex/0-Overview.nw"*/
public 
/*line 558 "src/Controller.nw"*/
final boolean isKilled() {
  return this.kill;
}
/*line 171 "src/tex/0-Overview.nw"*/
public 
/*line 549 "src/Controller.nw"*/
void returnRequest(final int[] r) {
  if (this.clock_now - r[2] < QUEUE_TIMEOUT) {
    this.lu_seen.put(r[0], false);
  }
}
/*line 172 "src/tex/0-Overview.nw"*/
public 
/*line 471 "src/Controller.nw"*/
void startRealtime(final Consumer<Boolean> app_cb) {
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
/*line 173 "src/tex/0-Overview.nw"*/
public 
/*line 499 "src/Controller.nw"*/
void startSequential(final Consumer<Boolean> app_cb) {
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
/*line 174 "src/tex/0-Overview.nw"*/
public 
/*line 516 "src/Controller.nw"*/
void stop(final Consumer<Boolean> app_cb) {
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
/*line 10 "src/Controller.nw"*/
}
