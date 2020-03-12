package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
public class NearestNeighbor extends Client {
  final int MAX_LAST_UPDATE_TIME = 300;
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
                  final int val = (this.communicator.retrieveClock() - lut.get(sid));
                  if (val <= MAX_LAST_UPDATE_TIME)
                    results.put(sid, val);
                }
                candidates = new HashMap<Integer, Integer>(results);
                if (DEBUG) {
                  System.out.printf("do map/filter: last update time\n");
                }
                if (DEBUG) {
                  System.out.printf("got candidates={ #%d }\n", candidates.size());
                }

                results.clear();
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

                  Entry<Integer, Integer> min = null;
                  for (final Entry<Integer, Integer> entry : candidates.entrySet()) {
                    if (min == null || min.getValue() > entry.getValue()) {
                      min = entry;
                    }
                  }
                  if (DEBUG) {
                    System.out.printf("got min={ %d, %d }\n", min.getKey(), min.getValue());
                  }

                  final int sid = min.getKey();
                  final int now = this.communicator.retrieveClock();

                  int[] bnew = new int[] { };
                  int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);
                  if (DEBUG) {
                    System.out.printf("got brem=\n");
                    for (int _i = 0; _i < (brem.length - 3); _i += 4) {
                      System.out.printf("  { %d, %d, %d, %d }\n",
                          brem[_i], brem[_i+1], brem[_i+2], brem[_i+3]);
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
                    for (int _i = 0; _i < (wact.length - 1); _i += 2) {
                      System.out.printf("  { %d, %d }\n",
                          wact[_i], wact[_i+1]);
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
                        for (int _i = 0; _i < (brem.length - 3); _i += 4) {
                          System.out.printf("  { %d, %d, %d, %d }\n",
                              brem[_i], brem[_i+1], brem[_i+2], brem[_i+3]);
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
                      for (int _i = 0; _i < (bnew.length - 3); _i += 4) {
                        System.out.printf("  { %d, %d, %d, %d }\n",
                            bnew[_i], bnew[_i+1], bnew[_i+2], bnew[_i+3]);
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
                      for (int _i = 0; _i < (bnew.length - 3); _i += 4) {
                        System.out.printf("  { %d, %d, %d, %d }\n",
                            bnew[_i], bnew[_i+1], bnew[_i+2], bnew[_i+3]);
                      }
                    }

                    int[] wnew = null;
                    int[] wbeg = new int[] { 0, 0 };

                    wbeg[0] = (wact[3] == 0 ? now : wact[2]);
                    wbeg[1] = (wact[3] == 0 ? wact[1] : wact[3]);
                    if (DEBUG) {
                      System.out.printf("set wbeg={ %d, %d }\n", wbeg[0], wbeg[1]);
                    }

                    {
                      final int p = (bnew.length/4);
                      final int[][] legs = new int[p][];

                      int[] leg = this.tools.computeRoute(wbeg[1], bnew[1], wbeg[0]);
                      int n = leg.length;
                      int t = leg[(n - 2)];

                      legs[0] = leg;
                      for (int i = 1; i < p; i++) {
                        // Extract vertices
                        final int u = bnew[(4*i - 3)];
                        final int v = bnew[(4*i + 1)];
                        // Compute path and store into legs
                        leg = this.tools.computeRoute(u, v, t);
                        legs[i] = leg;
                        // Update n and t
                        n += (leg.length - 2);
                        t = leg[leg.length - 2];
                      }
                      wnew = new int[n];
                      int k = 0;
                      for (int i = 0; i < legs.length; i++) {
                        final int rend = (legs[i].length - (i == (legs.length - 1) ? 0 : 2));
                        for (int j = 0; j < rend; j++) {
                          wnew[k] = legs[i][j];
                          k++;
                        }
                      }
                      for (int i = 1; i < legs.length; i++) {
                        bnew[(4*i - 4)] = legs[i][0];
                      }
                      bnew[(4*p - 4)] = t;
                    }

                    // if next waypoint is vehicle destination,
                    // reset route start time to last-visited time
                    if (wact[3] == 0) {
                      wnew[0] = lut.get(sid);
                    }

                    if (DEBUG) {
                      System.out.printf("set wnew=\n");
                      for (int _i = 0; _i < (wnew.length - 1); _i += 2) {
                        System.out.printf("  { %d, %d }\n",
                            wnew[_i], wnew[_i+1]);
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
