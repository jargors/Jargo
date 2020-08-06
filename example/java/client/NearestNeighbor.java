package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
public class NearestNeighbor extends Client {
  final int MAX_PROXIMITY = 600;
  protected void handleRequest(int[] r) throws ClientException, ClientFatalException {
              if (DEBUG) {
                System.out.printf("got request={ %d, %d, %d, %d, %d, %d, %d }\n",
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6]);
              }
              try {
                final int rid = r[0];
                final int rq  = r[1];
                final int ro  = r[4];
                final int rd  = r[5];

                Map<Integer, Integer> candidates = new HashMap<Integer, Integer>(lut);
                Map<Integer, Integer> results = new HashMap<Integer, Integer>();
                if (DEBUG) {
                  System.out.printf("got candidates={ #%d }\n", candidates.size());
                }

                for (final int sid : candidates.keySet()) {
                  final int val = this.tools.computeHaversine(luv.get(sid), ro);
                  if (0 < val && val <= MAX_PROXIMITY)
                    results.put(sid, val);
                }
                candidates = new HashMap<Integer, Integer>(results);
                if (DEBUG) {
                  System.out.printf("do map/filter: proximity\n");
                }
                if (DEBUG) {
                  System.out.printf("got candidates={ #%d }\n", candidates.size());
                }

                while (!candidates.isEmpty()) {

                  Entry<Integer, Integer> cand = null;
                  for (final Entry<Integer, Integer> entry : candidates.entrySet()) {
                    if (cand == null || cand.getValue() > entry.getValue()) {
                      cand = entry;
                    }
                  }
                  if (DEBUG) {
                    System.out.printf("got cand={ %d, %d }\n", cand.getKey(), cand.getValue());
                  }

                  final int sid = cand.getKey();
                  final int now = this.communicator.retrieveClock();

                  int[] bnew = new int[] { };
                  int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);
                  if (DEBUG) {
                    System.out.printf("got brem=\n");
                    for (int __i = 0; __i < (brem.length - 3); __i += 4) {
                      System.out.printf("  { %d, %d, %d, %d }\n",
                          brem[__i], brem[__i+1], brem[__i+2], brem[__i+3]);
                    }
                  }

                  int tbeg = now;
                  int tend = brem[0];
                  if (DEBUG) {
                    System.out.printf("set tbeg=%d, tend=%d\n", tbeg, tend);
                  }

                  final int[] wact = this.communicator.queryServerRouteActive(sid);
                  if (DEBUG) {
                    System.out.printf("got wact=\n");
                    for (int __i = 0; __i < (wact.length - 1); __i += 2) {
                      System.out.printf("  { %d, %d }\n",
                          wact[__i], wact[(__i + 1)]);
                    }
                  }

                  // if next events occurs at next waypoint and is not server's own
                  // destination, then delete these events from schedule (limitation #4).
                  if (brem[2] != sid && brem[0] == wact[2]) {
                    if (DEBUG) {
                      System.out.printf("detected limitation #4\n");
                    }
                    tbeg = brem[0];
                    tend = brem[4];
                    if (DEBUG) {
                      System.out.printf("set tbeg=%d, tend=%d\n", tbeg, tend);
                    }
                    while (brem[0] == wact[2]) {
                      brem = Arrays.copyOfRange(brem, 4, brem.length);
                      if (DEBUG) {
                        System.out.printf("remove event\n");
                      }
                      if (DEBUG) {
                        System.out.printf("got brem=\n");
                        for (int __i = 0; __i < (brem.length - 3); __i += 4) {
                          System.out.printf("  { %d, %d, %d, %d }\n",
                              brem[__i], brem[__i+1], brem[__i+2], brem[__i+3]);
                        }
                      }
                    }
                  }

                  boolean ok = false;

                  ok = (this.communicator.queryServerCapacityViolations(sid, rq, tbeg, tend)[0] == 0);
                  if (DEBUG) {
                    System.out.printf("set ok=%s\n", (ok ? "true" : "false"));
                  }

                  if (ok) {
                    int[] stop = new int[] { 0, ro, 0, rid };
                    int ipos = 0;
                    if (DEBUG) {
                      System.out.printf("set stop={ %d, %d, %d, %d }\n",
                          stop[0], stop[1], stop[2], stop[3]);
                    }
                    if (DEBUG) {
                      System.out.printf("set ipos=%d\n", ipos);
                    }
                    bnew = new int[(brem.length + 4)];
                    System.arraycopy(stop, 0, bnew, 4*ipos, 4);
                    System.arraycopy(brem, 0, bnew, 0, 4*ipos);
                    System.arraycopy(brem, 4*ipos, bnew, 4*(ipos + 1), brem.length - 4*ipos);
                    if (DEBUG) {
                      System.out.printf("got bnew=\n");
                      for (int __i = 0; __i < (bnew.length - 3); __i += 4) {
                        System.out.printf("  { %d, %d, %d, %d }\n",
                            bnew[__i], bnew[__i+1], bnew[__i+2], bnew[__i+3]);
                      }
                    }

                    brem = bnew;

                    stop[1] = rd;
                    ipos = 1;
                    if (DEBUG) {
                      System.out.printf("set stop={ %d, %d, %d, %d }\n",
                          stop[0], stop[1], stop[2], stop[3]);
                    }
                    if (DEBUG) {
                      System.out.printf("set ipos=%d\n", ipos);
                    }
                    bnew = new int[(brem.length + 4)];
                    System.arraycopy(stop, 0, bnew, 4*ipos, 4);
                    System.arraycopy(brem, 0, bnew, 0, 4*ipos);
                    System.arraycopy(brem, 4*ipos, bnew, 4*(ipos + 1), brem.length - 4*ipos);
                    if (DEBUG) {
                      System.out.printf("got bnew=\n");
                      for (int __i = 0; __i < (bnew.length - 3); __i += 4) {
                        System.out.printf("  { %d, %d, %d, %d }\n",
                            bnew[__i], bnew[__i+1], bnew[__i+2], bnew[__i+3]);
                      }
                    }

                    int[] wnew = null;
                    int[] wbeg = (wact[3] == 0
                        ? new int[] { now    , wact[1] }
                        : new int[] { wact[2], wact[3] });
                    if (DEBUG) {
                      System.out.printf("set wbeg={ %d, %d }\n", wbeg[0], wbeg[1]);
                    }

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

                    if (DEBUG) {
                      System.out.printf("set wnew=\n");
                      for (int __i = 0; __i < (wnew.length - 1); __i += 2) {
                        System.out.printf("  { %d, %d }\n",
                            wnew[__i], wnew[(__i + 1)]);
                      }
                    }

                    this.communicator.updateServerService(sid, wnew, bnew,
                        new int[] { rid }, new int[] { });

                    break;

                  } else {
                    candidates.remove(sid);
                    if (DEBUG) {
                      System.out.printf("remove candidate %d\n", sid);
                    }
                  }
                }
              } catch (Exception e) {
                throw new ClientException(e);
              }
            }
}
