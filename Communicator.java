package com.github.jargors;
import com.github.jargors.Storage;
import com.github.jargors.Controller;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.SQLException;
public class Communicator {
  private Storage storage;
  private Controller controller;
  private int world_time = 0;
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.communicator.debug"));
  public Communicator() { }
  public void registerStorage(final Storage src) {
    this.storage = src;
  }
  public void registerController(final Controller src) {
    this.controller = src;
  }
  public void setSimulationWorldTime(final int t) {
    this.world_time = t;
  }
  public int getSimulationWorldTime() {
    return this.world_time;
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceVerticesCache() {
    return this.storage.getReferenceVerticesCache();
  }
  public final ConcurrentHashMap<Integer,
      ConcurrentHashMap<Integer, int[]>> getReferenceEdgesCache() {
    return this.storage.getReferenceEdgesCache();
  }
  public final ConcurrentHashMap<Integer, int[]> getReferenceUsersCache() {
    return this.storage.getReferenceUsersCache();
  }
  public void returnRequest(final int rid) {
    this.controller.returnRequest(rid);
  }
  public boolean updateServerRoute(final int sid, final int[] route, final int[] sched)
  throws UserNotFoundException, EdgeNotFoundException, SQLException {
    boolean success = false;
    if (route[0] >= this.world_time) {
      this.storage.DBUpdateServerRoute(sid, route, sched);
      success = true;
    }
    return success;
  }
  public boolean updateServerAddToSchedule(
      final int sid, final int[] route, final int[] sched, final int[] rid)
  throws UserNotFoundException, EdgeNotFoundException, SQLException {
    final int t = this.world_time;
    final int[] current = this.storage.DBQueryServerRoute(sid);
    int i = 0;
    while (i < current.length && current[i] != route[0]) {
      i += 2;
    }
    if (i == current.length) {
      // branch point not found
      return false;
    }
    int j = 0;
    while (i < current.length && (current[i] <= t && current[(i + 1)] != 0)) {
      if (current[i] != route[j] || current[(i + 1)] != route[(j + 1)]) {
        // overwrite history occurred
        return false;
      }
      i += 2;
      j += 2;
    }
    this.storage.DBUpdateServerAddToSchedule(sid, route, sched, rid);
    return true;
  }
  public boolean updateServerRemoveFromSchedule(
      final int sid, final int[] route, final int[] sched, final int[] rid)
  throws UserNotFoundException, EdgeNotFoundException, SQLException {
    boolean success = false;
    if (route[0] >= this.world_time) {
      this.storage.DBUpdateServerRemoveFromSchedule(sid, route, sched, rid);
    }
    return success;
  }
  public int[] queryVertex(final int v) throws VertexNotFoundException {
    return this.storage.DBQueryVertex(v);
  }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException {
    return this.storage.DBQueryEdge(v1, v2);
  }
  public int[] queryUser(final int uid) throws UserNotFoundException {
    return this.storage.DBQueryUser(uid);
  }
  public int[] queryServerLocationsActive(final int t) throws SQLException {
    return this.storage.DBQueryServerLocationsActive(t);
  }
  public int[] queryServerRemainingRoute(final int sid, final int t) throws SQLException {
    return this.storage.DBQueryServerRemainingRoute(sid, t);
  }
  public int[] queryServerRemainingSchedule(final int sid, final int t) throws SQLException {
    return this.storage.DBQueryServerRemainingSchedule(sid, t);
  }
  public int[] queryServerRemainingDistance(final int sid, final int t) throws SQLException {
    return this.storage.DBQueryServerRemainingDistance(sid, t);
  }
  public int[] queryServerRemainingDuration(final int sid, final int t) throws SQLException {
    return this.storage.DBQueryServerRemainingDuration(sid, t);
  }
  public int[] queryServerMaxLoad(final int sid, final int t) throws SQLException {
    return this.storage.DBQueryServerMaxLoad(sid, t);
  }
}
