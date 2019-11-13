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
import com.github.jargors.exceptions.RouteIllegalOverwriteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.SQLException;
public class Communicator {
  private Storage storage;
  private Controller controller;
  private final boolean DEBUG = "true".equals(System.getProperty("jargors.communicator.debug"));
  public Communicator() { }
  public int[] queryVertex(final int v) throws VertexNotFoundException, SQLException {
           return storage.DBQueryVertex(v);
         }
  public int[] queryEdge(final int v1, final int v2) throws EdgeNotFoundException, SQLException {
           return storage.DBQueryEdge(v1, v2);
         }
  public int[] queryUser(final int rid) throws UserNotFoundException, SQLException {
           return storage.DBQueryUser(rid);
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
  public void updateServerRoute(final int sid, final int[] route, final int[] sched)
         throws RouteIllegalOverwriteException, UserNotFoundException, EdgeNotFoundException, SQLException {
           if (route[0] >= this.forwardSimulationWorldTime()) {
             this.storage.DBUpdateServerRoute(sid, route, sched);
           } else {
             throw new RouteIllegalOverwriteException();
           }
         }
  public void updateServerAddToSchedule( final int sid, final int[] route, final int[] sched, final int[] rid)
         throws RouteIllegalOverwriteException, UserNotFoundException, EdgeNotFoundException, SQLException {
           final int t = this.forwardSimulationWorldTime();
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
           this.storage.DBUpdateServerAddToSchedule(sid, route, sched, rid);
         }
  public void updateServerRemoveFromSchedule( final int sid, final int[] route, final int[] sched, final int[] rid)
         throws RouteIllegalOverwriteException, UserNotFoundException, EdgeNotFoundException, SQLException {
           if (route[0] >= this.forwardSimulationWorldTime()) {
             this.storage.DBUpdateServerRemoveFromSchedule(sid, route, sched, rid);
           } else {
             throw new RouteIllegalOverwriteException();
           }
         }
  public void registerStorage(final Storage src) {
           this.storage = src;
         }
  public void registerController(final Controller src) {
           this.controller = src;
         }
  public int forwardSimulationWorldTime() {
           return this.controller.getSimulationWorldTime();
         }
  public final ConcurrentHashMap<Integer, int[]> forwardReferenceVerticesCache() {
           return this.storage.getReferenceVerticesCache();
         }
  public final ConcurrentHashMap<Integer,
             ConcurrentHashMap<Integer, int[]>> forwardReferenceEdgesCache() {
           return this.storage.getReferenceEdgesCache();
         }
  public final ConcurrentHashMap<Integer, int[]> forwardReferenceUsersCache() {
           return this.storage.getReferenceUsersCache();
         }
  public void forwardReturnRequest(final int rid) {
           this.controller.returnRequest(rid);
         }
}
