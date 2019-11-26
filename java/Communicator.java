/*line 24 "src/Communicator.nw"*/
package com.github.jargors;
/*line 27 "src/Communicator.nw"*/
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
/*line 6 "src/Communicator.nw"*/
public class Communicator {
  
/*line 47 "src/Communicator.nw"*/
private Storage storage;
private Controller controller;
private Traffic traffic = null;
private final boolean DEBUG = "true".equals(System.getProperty("jargors.communicator.debug"));
/*line 8 "src/Communicator.nw"*/
  
/*line 55 "src/Communicator.nw"*/
public Communicator() { }
/*line 9 "src/Communicator.nw"*/
  
/*line 190 "src/tex/0-Overview.nw"*/
public 
/*line 305 "src/tex/2-Reading.nw"*/
int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
  return this.storage.DBQueryEdge(v1, v2);
}
/*line 191 "src/tex/0-Overview.nw"*/
public 
/*line 1415 "src/tex/2-Reading.nw"*/
int[] queryServerDistanceRemaining(final int sid, final int t) throws SQLException {
  return this.storage.DBQueryServerDistanceRemaining(sid, t);
}
/*line 192 "src/tex/0-Overview.nw"*/
public 
/*line 1545 "src/tex/2-Reading.nw"*/
int[] queryServerDurationRemaining(final int sid, final int t) throws SQLException {
  return this.storage.DBQueryServerDurationRemaining(sid, t);
}
/*line 193 "src/tex/0-Overview.nw"*/
public 
/*line 1328 "src/tex/2-Reading.nw"*/
int[] queryServerLoadMax(final int sid, final int t) throws SQLException {
  return this.storage.DBQueryServerLoadMax(sid, t);
}
/*line 194 "src/tex/0-Overview.nw"*/
public 
/*line 1135 "src/tex/2-Reading.nw"*/
int[] queryServerRouteRemaining(final int sid, final int t) throws SQLException {
  return this.storage.DBQueryServerRouteRemaining(sid, t);
}
/*line 195 "src/tex/0-Overview.nw"*/
public 
/*line 1278 "src/tex/2-Reading.nw"*/
int[] queryServerScheduleRemaining(final int sid, final int t) throws SQLException {
  return this.storage.DBQueryServerScheduleRemaining(sid, t);
}
/*line 196 "src/tex/0-Overview.nw"*/
public 
/*line 1986 "src/tex/2-Reading.nw"*/
int[] queryServersLocationsActive(final int t) throws SQLException {
  return this.storage.DBQueryServersLocationsActive(t);
}
/*line 197 "src/tex/0-Overview.nw"*/
public 
/*line 484 "src/tex/2-Reading.nw"*/
int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
  return storage.DBQueryUser(rid);
}
/*line 198 "src/tex/0-Overview.nw"*/
public 
/*line 171 "src/tex/2-Reading.nw"*/
int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
  return this.storage.DBQueryVertex(v);
}
/*line 202 "src/tex/0-Overview.nw"*/
public 
/*line 890 "src/tex/3-Writing.nw"*/
void updateServerAddToSchedule(final int sid, final int[] route, final int[] sched, final int[] rid)
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
  
/*line 39 "src/tex/3-Writing.nw"*/
for (int k = 0; k < (sched.length - 2); k += 3) {
  final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
  if (sched[k] > tl) {
    throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
        +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
  }
}
/*line 913 "src/tex/3-Writing.nw"*/
  
/*line 8 "src/tex/3-Writing.nw"*/
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
/*line 914 "src/tex/3-Writing.nw"*/
  this.storage.DBUpdateServerAddToSchedule(sid, mutroute, mutsched, rid);
}
/*line 203 "src/tex/0-Overview.nw"*/
public 
/*line 1045 "src/tex/3-Writing.nw"*/
void updateServerRemoveFromSchedule( final int sid, final int[] route, final int[] sched, final int[] rid)
throws RouteIllegalOverwriteException, UserNotFoundException,
       EdgeNotFoundException, TimeWindowException, SQLException {
  if (route[0] >= this.retrieveClockNow()) {
    
/*line 39 "src/tex/3-Writing.nw"*/
for (int k = 0; k < (sched.length - 2); k += 3) {
  final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
  if (sched[k] > tl) {
    throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
        +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
  }
}
/*line 1050 "src/tex/3-Writing.nw"*/
    
/*line 8 "src/tex/3-Writing.nw"*/
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
/*line 1051 "src/tex/3-Writing.nw"*/
    this.storage.DBUpdateServerRemoveFromSchedule(sid, mutroute, mutsched, rid);
  } else {
    throw new RouteIllegalOverwriteException();
  }
}
/*line 204 "src/tex/0-Overview.nw"*/
public 
/*line 747 "src/tex/3-Writing.nw"*/
void updateServerRoute(final int sid, final int[] route, final int[] sched)
throws RouteIllegalOverwriteException, UserNotFoundException,
       EdgeNotFoundException, TimeWindowException, SQLException {
  if (route[0] >= this.retrieveClockNow()) {
    
/*line 39 "src/tex/3-Writing.nw"*/
for (int k = 0; k < (sched.length - 2); k += 3) {
  final int tl = this.storage.DBQueryUser(sched[(k + 2)])[3];
  if (sched[k] > tl) {
    throw new TimeWindowException("Waypoint time (t="+sched[k]+") "
        +"after late window (t="+tl+", uid="+sched[(k + 2)]+")");
  }
}
/*line 752 "src/tex/3-Writing.nw"*/
    
/*line 8 "src/tex/3-Writing.nw"*/
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
/*line 753 "src/tex/3-Writing.nw"*/
    this.storage.DBUpdateServerRoute(sid, mutroute, mutsched);
  } else {
    throw new RouteIllegalOverwriteException();
  }
}
/*line 208 "src/tex/0-Overview.nw"*/
public 
/*line 1161 "src/tex/4-Administration.nw"*/
int retrieveClockNow() {
  return this.controller.getClockNow();
}
/*line 209 "src/tex/0-Overview.nw"*/
public 
/*line 1175 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> retrieveRefCacheEdges() {
  return this.storage.getRefCacheEdges();
}
/*line 210 "src/tex/0-Overview.nw"*/
public 
/*line 1182 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> retrieveRefCacheUsers() {
  return this.storage.getRefCacheUsers();
}
/*line 211 "src/tex/0-Overview.nw"*/
public 
/*line 1168 "src/tex/4-Administration.nw"*/
final ConcurrentHashMap<Integer, int[]> retrieveRefCacheVertices() {
  return this.storage.getRefCacheVertices();
}
/*line 212 "src/tex/0-Overview.nw"*/
public 
/*line 1256 "src/tex/4-Administration.nw"*/
void setRefController(final Controller controller) {
  this.controller = controller;
}
/*line 213 "src/tex/0-Overview.nw"*/
public 
/*line 1263 "src/tex/4-Administration.nw"*/
void setRefStorage(final Storage storage) {
  this.storage = storage;
}
/*line 214 "src/tex/0-Overview.nw"*/
public 
/*line 1270 "src/tex/4-Administration.nw"*/
void setRefTraffic (final Traffic traffic) {
  this.traffic = traffic;
  this.traffic.forwardRefCacheVertices(this.storage.getRefCacheVertices());
  this.traffic.forwardRefCacheEdges(this.storage.getRefCacheEdges());
}
/*line 215 "src/tex/0-Overview.nw"*/
public 
/*line 61 "src/Communicator.nw"*/
void forwardReturnRequest(final int[] r) {
  this.controller.returnRequest(r);
}
/*line 10 "src/Communicator.nw"*/
}
