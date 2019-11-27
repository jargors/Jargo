package com.github.jargors;
import com.github.jargors.Storage;
import com.github.jargors.Controller;
import com.github.jargors.Traffic;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import com.github.jargors.exceptions.RouteIllegalOverwriteException;
import com.github.jargors.exceptions.TimeWindowException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.SQLException;
import com.github.jargors.jmx.*;
import java.lang.management.*;
import javax.management.*;
public class Communicator {
  private Storage storage;
  private Controller controller;
  private Traffic traffic = null;
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.communicator.debug"));
  private int    statQueryEdgeCount    = 0;
  private long   statQueryEdgeDurLast  = 0;
  private long   statQueryEdgeDurTotal = 0;
  private long   statQueryEdgeDurMin   = Integer.MAX_VALUE;
  private long   statQueryEdgeDurMax   = 0;
  private double statQueryEdgeDurAvg   = 0;
  private int    statQueryServerDistanceRemainingCount    = 0;
  private long   statQueryServerDistanceRemainingDurLast  = 0;
  private long   statQueryServerDistanceRemainingDurTotal = 0;
  private long   statQueryServerDistanceRemainingDurMin   = Integer.MAX_VALUE;
  private long   statQueryServerDistanceRemainingDurMax   = 0;
  private double statQueryServerDistanceRemainingDurAvg   = 0;
  private int    statQueryServerDurationRemainingCount    = 0;
  private long   statQueryServerDurationRemainingDurLast  = 0;
  private long   statQueryServerDurationRemainingDurTotal = 0;
  private long   statQueryServerDurationRemainingDurMin   = Integer.MAX_VALUE;
  private long   statQueryServerDurationRemainingDurMax   = 0;
  private double statQueryServerDurationRemainingDurAvg   = 0;
  private int    statQueryServerLoadMaxCount    = 0;
  private long   statQueryServerLoadMaxDurLast  = 0;
  private long   statQueryServerLoadMaxDurTotal = 0;
  private long   statQueryServerLoadMaxDurMin   = Integer.MAX_VALUE;
  private long   statQueryServerLoadMaxDurMax   = 0;
  private double statQueryServerLoadMaxDurAvg   = 0;
  private int    statQueryServerRouteRemainingCount    = 0;
  private long   statQueryServerRouteRemainingDurLast  = 0;
  private long   statQueryServerRouteRemainingDurTotal = 0;
  private long   statQueryServerRouteRemainingDurMin   = Integer.MAX_VALUE;
  private long   statQueryServerRouteRemainingDurMax   = 0;
  private double statQueryServerRouteRemainingDurAvg   = 0;
  private int    statQueryServerScheduleRemainingCount    = 0;
  private long   statQueryServerScheduleRemainingDurLast  = 0;
  private long   statQueryServerScheduleRemainingDurTotal = 0;
  private long   statQueryServerScheduleRemainingDurMin   = Integer.MAX_VALUE;
  private long   statQueryServerScheduleRemainingDurMax   = 0;
  private double statQueryServerScheduleRemainingDurAvg   = 0;
  private int    statQueryServersLocationsActiveCount    = 0;
  private long   statQueryServersLocationsActiveDurLast  = 0;
  private long   statQueryServersLocationsActiveDurTotal = 0;
  private long   statQueryServersLocationsActiveDurMin   = Integer.MAX_VALUE;
  private long   statQueryServersLocationsActiveDurMax   = 0;
  private double statQueryServersLocationsActiveDurAvg   = 0;
  private int    statQueryUserCount    = 0;
  private long   statQueryUserDurLast  = 0;
  private long   statQueryUserDurTotal = 0;
  private long   statQueryUserDurMin   = Integer.MAX_VALUE;
  private long   statQueryUserDurMax   = 0;
  private double statQueryUserDurAvg   = 0;
  private int    statQueryVertexCount    = 0;
  private long   statQueryVertexDurLast  = 0;
  private long   statQueryVertexDurTotal = 0;
  private long   statQueryVertexDurMin   = Integer.MAX_VALUE;
  private long   statQueryVertexDurMax   = 0;
  private double statQueryVertexDurAvg   = 0;
  public Communicator() {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      CommunicatorMonitor mon = new CommunicatorMonitor(this);
      mbs.registerMBean(mon, new ObjectName("com.github.jargors.jmx:type=CommunicatorMonitor"));
    } catch (Exception e) {
      System.err.printf("CommunicatorMonitor failed; reason: %s\n", e.toString());
      System.err.printf("Continuing with monitoring disabled\n");
    }
  }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryEdge(v1, v2);
               this.statQueryEdgeCount++;
               this.statQueryEdgeDurLast = (System.currentTimeMillis() - A0);
               this.statQueryEdgeDurTotal +=
               this.statQueryEdgeDurLast;
           if (this.statQueryEdgeDurLast <
               this.statQueryEdgeDurMin) {
               this.statQueryEdgeDurMin =
               this.statQueryEdgeDurLast;}
           if (this.statQueryEdgeDurLast >
               this.statQueryEdgeDurMax) {
               this.statQueryEdgeDurMax =
               this.statQueryEdgeDurLast;}
               this.statQueryEdgeDurAvg = (double)
               this.statQueryEdgeDurTotal/
               this.statQueryEdgeCount;
           return output;
         }
  public int[] queryServerDistanceRemaining(final int sid, final int t) throws SQLException {
           return this.storage.DBQueryServerDistanceRemaining(sid, t);
         }
  public int[] queryServerDurationRemaining(final int sid, final int t) throws SQLException {
           return this.storage.DBQueryServerDurationRemaining(sid, t);
         }
  public int[] queryServerLoadMax(final int sid, final int t) throws SQLException {
           return this.storage.DBQueryServerLoadMax(sid, t);
         }
  public int[] queryServerRouteRemaining(final int sid, final int t) throws SQLException {
           return this.storage.DBQueryServerRouteRemaining(sid, t);
         }
  public int[] queryServerScheduleRemaining(final int sid, final int t) throws SQLException {
           long A0 = System.currentTimeMillis();
           int[] output = this.storage.DBQueryServerScheduleRemaining(sid, t);
               this.statQueryServerScheduleRemainingCount++;
               this.statQueryServerScheduleRemainingDurLast = (System.currentTimeMillis() - A0);
               this.statQueryServerScheduleRemainingDurTotal +=
               this.statQueryServerScheduleRemainingDurLast;
           if (this.statQueryServerScheduleRemainingDurLast <
               this.statQueryServerScheduleRemainingDurMin) {
               this.statQueryServerScheduleRemainingDurMin =
               this.statQueryServerScheduleRemainingDurLast;}
           if (this.statQueryServerScheduleRemainingDurLast >
               this.statQueryServerScheduleRemainingDurMax) {
               this.statQueryServerScheduleRemainingDurMax =
               this.statQueryServerScheduleRemainingDurLast;}
               this.statQueryServerScheduleRemainingDurAvg = (double)
               this.statQueryServerScheduleRemainingDurTotal/
               this.statQueryServerScheduleRemainingCount;
           return output;
         }
  public int[] queryServersLocationsActive(final int t) throws SQLException {
           return this.storage.DBQueryServersLocationsActive(t);
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           return storage.DBQueryUser(rid);
         }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           return this.storage.DBQueryVertex(v);
         }
  public void updateServerAddToSchedule(final int sid, final int[] route, final int[] sched, final int[] rid)
         throws RouteIllegalOverwriteException, UserNotFoundException,
                EdgeNotFoundException, TimeWindowException, SQLException {
           final int t = this.retrieveClockNow();
           final int[] current = this.storage.DBQueryServerRoute(sid);
           int i = 0;
           while (i < current.length && current[i] != route[0]) {
             i += 2;
           }
           if (i == current.length) {
             // branch point not found
             throw new RouteIllegalOverwriteException();
           }
           int j = 0;
           while (i < current.length && (current[i] <= t && current[(i + 1)] != 0)) {
             if (current[i] != route[j] || current[(i + 1)] != route[(j + 1)]) {
               // overwrite history occurred
               throw new RouteIllegalOverwriteException();
             }
             i += 2;
             j += 2;
           }
           for (int k = 0; k < (sched.length - 2); k += 3) {
             final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
             if (sched[k] > tl) {
               throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
                   +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
             }
           }
           int[] mutroute = route.clone();
           int[] mutsched = sched.clone();
           if (this.traffic != null) {
             for (int k = 0; k < (mutroute.length - 3); k += 4) {
               final int t1 = mutroute[k];
               final int v1 = mutroute[(k + 1)];
               final int t2 = mutroute[(k + 2)];
               final int v2 = mutroute[(k + 3)];
               int[] ddnu = this.storage.DBQueryEdge(v1, v2);
               final int dd = ddnu[0];
               final int nu_old = ddnu[1];
               final int nu_new = Math.max(1, (int) Math.round(this.traffic.apply(v1, v2, t1)*nu_old));
               final int diff = ((dd/(t2 - t1)) > nu_new
                   ? ((int) Math.ceil((dd/(float) nu_new + t1))) - t2
                   : 0);
               if (diff != 0) {
                 for (int p = 0; p < (mutsched.length - 2); p += 3) {
                   if (mutsched[p] >= mutroute[(k + 2)]) {
                     mutsched[p] += diff;
                   }
                 }
                 for (int q = (k + 2); q < (mutroute.length - 1); q += 2) {
                   mutroute[q] += diff;
                 }
               }
             }
           }
           this.storage.DBUpdateServerAddToSchedule(sid, mutroute, mutsched, rid);
         }
  public void updateServerRemoveFromSchedule( final int sid, final int[] route, final int[] sched, final int[] rid)
         throws RouteIllegalOverwriteException, UserNotFoundException,
                EdgeNotFoundException, TimeWindowException, SQLException {
           if (route[0] >= this.retrieveClockNow()) {
             for (int k = 0; k < (sched.length - 2); k += 3) {
               final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
               if (sched[k] > tl) {
                 throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
                     +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
               }
             }
             int[] mutroute = route.clone();
             int[] mutsched = sched.clone();
             if (this.traffic != null) {
               for (int k = 0; k < (mutroute.length - 3); k += 4) {
                 final int t1 = mutroute[k];
                 final int v1 = mutroute[(k + 1)];
                 final int t2 = mutroute[(k + 2)];
                 final int v2 = mutroute[(k + 3)];
                 int[] ddnu = this.storage.DBQueryEdge(v1, v2);
                 final int dd = ddnu[0];
                 final int nu_old = ddnu[1];
                 final int nu_new = Math.max(1, (int) Math.round(this.traffic.apply(v1, v2, t1)*nu_old));
                 final int diff = ((dd/(t2 - t1)) > nu_new
                     ? ((int) Math.ceil((dd/(float) nu_new + t1))) - t2
                     : 0);
                 if (diff != 0) {
                   for (int p = 0; p < (mutsched.length - 2); p += 3) {
                     if (mutsched[p] >= mutroute[(k + 2)]) {
                       mutsched[p] += diff;
                     }
                   }
                   for (int q = (k + 2); q < (mutroute.length - 1); q += 2) {
                     mutroute[q] += diff;
                   }
                 }
               }
             }
             this.storage.DBUpdateServerRemoveFromSchedule(sid, mutroute, mutsched, rid);
           } else {
             throw new RouteIllegalOverwriteException();
           }
         }
  public void updateServerRoute(final int sid, final int[] route, final int[] sched)
         throws RouteIllegalOverwriteException, UserNotFoundException,
                EdgeNotFoundException, TimeWindowException, SQLException {
           if (route[0] >= this.retrieveClockNow()) {
             for (int k = 0; k < (sched.length - 2); k += 3) {
               final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
               if (sched[k] > tl) {
                 throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
                     +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
               }
             }
             int[] mutroute = route.clone();
             int[] mutsched = sched.clone();
             if (this.traffic != null) {
               for (int k = 0; k < (mutroute.length - 3); k += 4) {
                 final int t1 = mutroute[k];
                 final int v1 = mutroute[(k + 1)];
                 final int t2 = mutroute[(k + 2)];
                 final int v2 = mutroute[(k + 3)];
                 int[] ddnu = this.storage.DBQueryEdge(v1, v2);
                 final int dd = ddnu[0];
                 final int nu_old = ddnu[1];
                 final int nu_new = Math.max(1, (int) Math.round(this.traffic.apply(v1, v2, t1)*nu_old));
                 final int diff = ((dd/(t2 - t1)) > nu_new
                     ? ((int) Math.ceil((dd/(float) nu_new + t1))) - t2
                     : 0);
                 if (diff != 0) {
                   for (int p = 0; p < (mutsched.length - 2); p += 3) {
                     if (mutsched[p] >= mutroute[(k + 2)]) {
                       mutsched[p] += diff;
                     }
                   }
                   for (int q = (k + 2); q < (mutroute.length - 1); q += 2) {
                     mutroute[q] += diff;
                   }
                 }
               }
             }
             this.storage.DBUpdateServerRoute(sid, mutroute, mutsched);
           } else {
             throw new RouteIllegalOverwriteException();
           }
         }
  public int retrieveClockNow() {
           return this.controller.getClockNow();
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
  public void setRefController(final Controller controller) {
           this.controller = controller;
         }
  public void setRefStorage(final Storage storage) {
           this.storage = storage;
         }
  public void setRefTraffic (final Traffic traffic) {
           this.traffic = traffic;
           this.traffic.forwardRefCacheVertices(this.storage.getRefCacheVertices());
           this.traffic.forwardRefCacheEdges(this.storage.getRefCacheEdges());
         }
  public void forwardReturnRequest(final int[] r) {
           this.controller.returnRequest(r);
         }
  public int    getStatQueryEdgeCount() {
           return this.statQueryEdgeCount;
         }
  public long   getStatQueryEdgeDurLast() {
           return this.statQueryEdgeDurLast;
         }
  public long   getStatQueryEdgeDurTotal() {
           return this.statQueryEdgeDurTotal;
         }
  public long   getStatQueryEdgeDurMin() {
           return this.statQueryEdgeDurMin;
         }
  public long   getStatQueryEdgeDurMax() {
           return this.statQueryEdgeDurMax;
         }
  public double getStatQueryEdgeDurAvg() {
           return this.statQueryEdgeDurAvg;
         }
  public int    getStatQueryServerDistanceRemainingCount() {
           return this.statQueryServerDistanceRemainingCount;
         }
  public long   getStatQueryServerDistanceRemainingDurLast() {
           return this.statQueryServerDistanceRemainingDurLast;
         }
  public long   getStatQueryServerDistanceRemainingDurTotal() {
           return this.statQueryServerDistanceRemainingDurTotal;
         }
  public long   getStatQueryServerDistanceRemainingDurMin() {
           return this.statQueryServerDistanceRemainingDurMin;
         }
  public long   getStatQueryServerDistanceRemainingDurMax() {
           return this.statQueryServerDistanceRemainingDurMax;
         }
  public double getStatQueryServerDistanceRemainingDurAvg() {
           return this.statQueryServerDistanceRemainingDurAvg;
         }
  public int    getStatQueryServerDurationRemainingCount() {
           return this.statQueryServerDurationRemainingCount;
         }
  public long   getStatQueryServerDurationRemainingDurLast() {
           return this.statQueryServerDurationRemainingDurLast;
         }
  public long   getStatQueryServerDurationRemainingDurTotal() {
           return this.statQueryServerDurationRemainingDurTotal;
         }
  public long   getStatQueryServerDurationRemainingDurMin() {
           return this.statQueryServerDurationRemainingDurMin;
         }
  public long   getStatQueryServerDurationRemainingDurMax() {
           return this.statQueryServerDurationRemainingDurMax;
         }
  public double getStatQueryServerDurationRemainingDurAvg() {
           return this.statQueryServerDurationRemainingDurAvg;
         }
  public int    getStatQueryServerLoadMaxCount() {
           return this.statQueryServerLoadMaxCount;
         }
  public long   getStatQueryServerLoadMaxDurLast() {
           return this.statQueryServerLoadMaxDurLast;
         }
  public long   getStatQueryServerLoadMaxDurTotal() {
           return this.statQueryServerLoadMaxDurTotal;
         }
  public long   getStatQueryServerLoadMaxDurMin() {
           return this.statQueryServerLoadMaxDurMin;
         }
  public long   getStatQueryServerLoadMaxDurMax() {
           return this.statQueryServerLoadMaxDurMax;
         }
  public double getStatQueryServerLoadMaxDurAvg() {
           return this.statQueryServerLoadMaxDurAvg;
         }
  public int    getStatQueryServerRouteRemainingCount() {
           return this.statQueryServerRouteRemainingCount;
         }
  public long   getStatQueryServerRouteRemainingDurLast() {
           return this.statQueryServerRouteRemainingDurLast;
         }
  public long   getStatQueryServerRouteRemainingDurTotal() {
           return this.statQueryServerRouteRemainingDurTotal;
         }
  public long   getStatQueryServerRouteRemainingDurMin() {
           return this.statQueryServerRouteRemainingDurMin;
         }
  public long   getStatQueryServerRouteRemainingDurMax() {
           return this.statQueryServerRouteRemainingDurMax;
         }
  public double getStatQueryServerRouteRemainingDurAvg() {
           return this.statQueryServerRouteRemainingDurAvg;
         }
  public int    getStatQueryServerScheduleRemainingCount() {
           return this.statQueryServerScheduleRemainingCount;
         }
  public long   getStatQueryServerScheduleRemainingDurLast() {
           return this.statQueryServerScheduleRemainingDurLast;
         }
  public long   getStatQueryServerScheduleRemainingDurTotal() {
           return this.statQueryServerScheduleRemainingDurTotal;
         }
  public long   getStatQueryServerScheduleRemainingDurMin() {
           return this.statQueryServerScheduleRemainingDurMin;
         }
  public long   getStatQueryServerScheduleRemainingDurMax() {
           return this.statQueryServerScheduleRemainingDurMax;
         }
  public double getStatQueryServerScheduleRemainingDurAvg() {
           return this.statQueryServerScheduleRemainingDurAvg;
         }
  public int    getStatQueryServersLocationsActiveCount() {
           return this.statQueryServersLocationsActiveCount;
         }
  public long   getStatQueryServersLocationsActiveDurLast() {
           return this.statQueryServersLocationsActiveDurLast;
         }
  public long   getStatQueryServersLocationsActiveDurTotal() {
           return this.statQueryServersLocationsActiveDurTotal;
         }
  public long   getStatQueryServersLocationsActiveDurMin() {
           return this.statQueryServersLocationsActiveDurMin;
         }
  public long   getStatQueryServersLocationsActiveDurMax() {
           return this.statQueryServersLocationsActiveDurMax;
         }
  public double getStatQueryServersLocationsActiveDurAvg() {
           return this.statQueryServersLocationsActiveDurAvg;
         }
  public int    getStatQueryUserCount() {
           return this.statQueryUserCount;
         }
  public long   getStatQueryUserDurLast() {
           return this.statQueryUserDurLast;
         }
  public long   getStatQueryUserDurTotal() {
           return this.statQueryUserDurTotal;
         }
  public long   getStatQueryUserDurMin() {
           return this.statQueryUserDurMin;
         }
  public long   getStatQueryUserDurMax() {
           return this.statQueryUserDurMax;
         }
  public double getStatQueryUserDurAvg() {
           return this.statQueryUserDurAvg;
         }
  public int    getStatQueryVertexCount() {
           return this.statQueryVertexCount;
         }
  public long   getStatQueryVertexDurLast() {
           return this.statQueryVertexDurLast;
         }
  public long   getStatQueryVertexDurTotal() {
           return this.statQueryVertexDurTotal;
         }
  public long   getStatQueryVertexDurMin() {
           return this.statQueryVertexDurMin;
         }
  public long   getStatQueryVertexDurMax() {
           return this.statQueryVertexDurMax;
         }
  public double getStatQueryVertexDurAvg() {
           return this.statQueryVertexDurAvg;
         }
}
