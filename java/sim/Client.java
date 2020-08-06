package com.github.jargors.sim;
import com.github.jargors.sim.Communicator;
import com.github.jargors.sim.Tools;
import com.github.jargors.sim.ClientException;
import com.github.jargors.sim.ClientFatalException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.sql.SQLException;
public abstract class Client {
  protected ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue<int[]>();
  protected Communicator communicator;
  protected Tools tools = new Tools();
  protected final boolean DEBUG =
      "true".equals(System.getProperty("jargors.client.debug"));
  protected ConcurrentHashMap<Integer, Integer> lut = new ConcurrentHashMap<Integer, Integer>();
  protected ConcurrentHashMap<Integer, Integer> luv = new ConcurrentHashMap<Integer, Integer>();
  protected long dur_handle_request = 0;
  public Client() {
    if (DEBUG) {
      System.out.printf("create Client\n");
    }
  }
  public void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
           this.tools.setRefCacheEdges(lu_edges);
         }
  public void forwardRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
           this.tools.setRefCacheUsers(lu_users);
         }
  public void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
           this.tools.setRefCacheVertices(lu_vertices);
         }
  public void setRefCommunicator(final Communicator communicator) {
           this.communicator = communicator;
         }
  public void gtreeLoad(String p) throws FileNotFoundException {
           this.tools.GTGtreeLoad(p);
         }
  public void gtreeClose() {
           this.tools.GTGtreeClose();
         }
  public void addRequest(final int[] r) {
           this.queue.add(r);
         }
  public void collectServerLocations(final int[] src) throws ClientException, ClientFatalException {
           for (int i = 0; i < (src.length - 2); i += 3) {
             this.handleServerLocation(new int[] {
               src[i],
               src[(i + 1)],
               src[(i + 2)]
             });
           }
         }
  public int dropRequests(final int deadline) {
           final int temp = this.queue.size();
           this.queue.removeIf((r) -> { return r[2] < deadline; });
           return Math.max(0, temp - this.queue.size());
         }
  public Map<Integer, Integer> filterByProximity(
             Map<Integer, Integer> candidates, int threshold, int rid)
             throws ClientException {
           Map<Integer, Integer> results = new HashMap<Integer, Integer>();
           try {
             final int ro = this.communicator.queryUser(rid)[4];
             for (final int sid : candidates.keySet()) {
               final int val = this.tools.computeHaversine(this.luv.get(sid), ro);
               if (0 < val && val <= threshold)
                 results.put(sid, val);
             }
           } catch (Exception e) {
             throw new ClientException(e);
           }
           return results;
         }
  public Map<Integer, Integer> filterByScheduleLength(
             Map<Integer, Integer> candidates, int threshold)
             throws SQLException {
           return filterByScheduleLength(candidates, threshold,
                    this.communicator.retrieveClock());
         }
  public Map<Integer, Integer> filterByScheduleLength(
             Map<Integer, Integer> candidates, int threshold, int time)
             throws SQLException {
           Map<Integer, Integer> results = new HashMap<Integer, Integer>();
           for (final int sid : candidates.keySet()) {
             final int val = this.communicator.queryServerScheduleRemaining(sid, time).length / 4;
             if (val <= threshold) {
               results.put(sid, val);
             }
           }
           return results;
         }
  public long getHandleRequestDur() {
           return this.dur_handle_request;
         }
  public int getQueueSize() {
           return this.queue.size();
         }
  public void init() { }
  public void notifyNew() throws ClientException, ClientFatalException {
           while (!this.queue.isEmpty()) {
             long A0 = System.currentTimeMillis();
             this.handleRequest(this.queue.remove());
             this.dur_handle_request = System.currentTimeMillis() - A0;
             if (DEBUG) {
               System.out.printf("handleRequest(1), arg1=[#]\n");
             }
           }
         }
  public int[] routeMinDistMinDur(int sid, int[] bnew, boolean strict) throws ClientException {
           int[] wnew = null;
           boolean ok = true;
           try {
             final int now = this.communicator.retrieveClock();
             int[] wact = this.communicator.queryServerRouteActive(sid);
             int[] wbeg = (wact[3] == 0
                 ? new int[] { now    , wact[1] }
                 : new int[] { wact[2], wact[3] });
             {
               final int _p = (bnew.length/4);
               final int[][] _legs = new int[_p][];

               int[] _leg = this.tools.computeRoute(wbeg[1], bnew[1], wbeg[0]);
               int _n = _leg.length;
               int _t = _leg[(_n - 2)];

               _legs[0] = _leg;
               for (int _i = 1; _i < _p; _i++) {
                 // Extract vertices
                 final int _u = bnew[(4*_i - 3)];
                 final int _v = bnew[(4*_i + 1)];
                 // Compute path and store into _legs
                 _leg = this.tools.computeRoute(_u, _v, _t);
                 _legs[_i] = _leg;
                 // Update _n and _t
                 _n += (_leg.length - 2);
                 _t = _leg[_leg.length - 2];
               }
               wnew = new int[_n];
               int _k = 0;
               for (int _i = 0; _i < _legs.length; _i++) {
                 final int _rend = (_legs[_i].length - (_i == (_legs.length - 1) ? 0 : 2));
                 for (int _j = 0; _j < _rend; _j++) {
                   wnew[_k] = _legs[_i][_j];
                   _k++;
                 }
               }
               // Populate times in the provided schedule
               for (int _i = 1; _i < _legs.length; _i++) {
                 bnew[(4*_i - 4)] = _legs[_i][0];
               }
               bnew[(4*_p - 4)] = _t;
             }

             // if next waypoint is vehicle destination,
             // reset route start time to last-visited time
             if (wact[3] == 0) {
               wnew[0] = lut.get(sid);
             }

             // Check time-windows
             for (int _i = 0; _i < (bnew.length - 3); _i += 4) {
               int _rid = bnew[(_i + 3)];
               int _rt  = bnew[(_i)];
               if (_rid != 0) {
                 int[] _u = this.communicator.queryUser(_rid);
                 int _ue = _u[2];
                 int _ul = _u[3];
                 if (_rt < _ue || _rt > _ul) {
                   ok = false;
                   break;
                 }
               }
             }
           } catch (Exception e) {
             throw new ClientException(e);
           }
           return (ok || !strict ? wnew : null);
         }
  public int[] scheduleMinDistInsertion(int sid, int rid) throws ClientException {
           int[] bmin = null;
           try {
             final int now = this.communicator.retrieveClock();
             final int[] r = this.communicator.queryUser(rid);
             final int rq  = r[1];
             final int ro  = r[4];
             final int rd  = r[5];

             int cmin = Integer.MAX_VALUE;

             int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);

             final int[] wact = this.communicator.queryServerRouteActive(sid);

             int[] wbeg = (wact[3] == 0
                 ? new int[] { now    , wact[1] }
                 : new int[] { wact[2], wact[3] });

             // if next events occurs at next waypoint and is not server's own
             // destination, then delete these events from schedule (limitation #4).
             if (brem[2] != sid && brem[0] == wact[2]) {
               while (brem[0] == wact[2]) {
                 brem = Arrays.copyOfRange(brem, 4, brem.length);
               }
             }

             int imax = (brem.length/4);
             int jmax = imax;
             int cost = brem[(brem.length - 4)];

             final int[] bold = brem;

             // Try all insertion positions
             for (int i = 0; i < imax; i++) {
               int tbeg = (i == 0 ? now : brem[4*(i - 1)]);

               for (int j = i; j < jmax; j++) {
                 int tend = bold[4*j];

                 boolean ok = (this.communicator.queryServerCapacityViolations(sid, rq, tbeg, tend)[0] == 0);

                 if (ok) {
                   brem = bold.clone();  // reset to original
                   int[] bnew = new int[] { };

                   int[] stop = new int[] { 0, ro, 0, rid };
                   int ipos = i;

                   // Insert
                   bnew = new int[(brem.length + 4)];
                   System.arraycopy(stop, 0, bnew, 4*ipos, 4);
                   System.arraycopy(brem, 0, bnew, 0, 4*ipos);
                   System.arraycopy(brem, 4*ipos, bnew, 4*(ipos + 1), brem.length - 4*ipos);

                   brem = bnew;

                   stop[1] = rd;
                   ipos = (j + 1);

                   // Insert
                   bnew = new int[(brem.length + 4)];
                   System.arraycopy(stop, 0, bnew, 4*ipos, 4);
                   System.arraycopy(brem, 0, bnew, 0, 4*ipos);
                   System.arraycopy(brem, 4*ipos, bnew, 4*(ipos + 1), brem.length - 4*ipos);

                   int cdel = bnew[(bnew.length - 4)] - cost;
                   if (cdel < cmin) {
                     bmin = bnew;
                     cmin = cdel;
                   }
                 }
               }
             }
           } catch (Exception e) {
             throw new ClientException(e);
           }
           return bmin;
         }
  protected void end() { }
  protected void handleRequest(final int[] r) throws ClientException, ClientFatalException { }
  protected void handleServerLocation(final int[] loc) throws ClientException, ClientFatalException {
              this.lut.put(loc[0], loc[1]);
              this.luv.put(loc[0], loc[2]);
            }
}
