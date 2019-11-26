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
public class Communicator {
  private Storage storage;
  private Controller controller;
  private Traffic traffic = null;
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.communicator.debug"));
  public Communicator() { }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           return this.storage.DBQueryEdge(v1, v2);
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
           return this.storage.DBQueryServerScheduleRemaining(sid, t);
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
}
