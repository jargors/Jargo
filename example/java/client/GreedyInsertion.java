package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
public class GreedyInsertion extends Client {
  final int MAX_PROXIMITY = 600;
  final int MAX_SCHEDULE_LENGTH = 8;
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

                /*
                results.clear();
                for (final int sid : candidates.keySet()) {
                  final int val = this.communicator.queryServerScheduleRemaining(sid,
                      this.communicator.retrieveClock()).length / 4;
                  if (val <= MAX_SCHEDULE_LENGTH)
                    results.put(sid, val);
                }
                candidates = new HashMap<Integer, Integer>(results);
                if (DEBUG) {
                  System.out.printf("do map/filter: schedule length\n");
                }
                if (DEBUG) {
                  System.out.printf("got candidates={ #%d }\n", candidates.size());
                }
                */

                // Remember minimum schedule, route, cost, server
                int[] wmin = null;
                int[] bmin = null;
                int cmin = Integer.MAX_VALUE;
                int smin = 0;

                while (!candidates.isEmpty()) {

                  Entry<Integer, Integer> cand = null;
                  {
                    Random random = new Random();
                    List<Integer> keys = new ArrayList<Integer>(candidates.keySet());
                    int randomKey = keys.get(random.nextInt(keys.size()));
                    cand = Map.entry(randomKey, candidates.get(randomKey));
                  }
                  if (DEBUG) {
                    System.out.printf("got cand={ %d, %d }\n", cand.getKey(), cand.getValue());
                  }

                  final int sid = cand.getKey();
                  final int now = this.communicator.retrieveClock();

                  int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);
                  if (DEBUG) {
                    System.out.printf("got brem=\n");
                    for (int __i = 0; __i < (brem.length - 3); __i += 4) {
                      System.out.printf("  { %d, %d, %d, %d }\n",
                          brem[__i], brem[__i+1], brem[__i+2], brem[__i+3]);
                    }
                  }

                  if (brem.length/4 > MAX_SCHEDULE_LENGTH) {
                    candidates.remove(sid);
                    continue;
                  }

                  final int[] wact = this.communicator.queryServerRouteActive(sid);
                  if (DEBUG) {
                    System.out.printf("got wact=\n");
                    for (int __i = 0; __i < (wact.length - 1); __i += 2) {
                      System.out.printf("  { %d, %d }\n",
                          wact[__i], wact[(__i + 1)]);
                    }
                  }

                  int[] wbeg = (wact[3] == 0
                      ? new int[] { now    , wact[1] }
                      : new int[] { wact[2], wact[3] });
                  if (DEBUG) {
                    System.out.printf("set wbeg={ %d, %d }\n", wbeg[0], wbeg[1]);
                  }

                  // if next events occurs at next waypoint and is not server's own
                  // destination, then delete these events from schedule (limitation #4).
                  if (brem[2] != sid && brem[0] == wact[2]) {
                    if (DEBUG) {
                      System.out.printf("detected limitation #4\n");
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

                  int imax = (brem.length/4);
                  int jmax = imax;
                  int cost = brem[(brem.length - 4)];
                  if (DEBUG) {
                    System.out.printf("set imax=%d, jmax=%d, cost=%d\n", imax, jmax, cost);
                  }

                  final int[] bold = brem;

                  // Try all insertion positions
                  if (DEBUG) {
                    System.out.printf("start insertion heuristic\n");
                  }
                  for (int i = 0; i < imax; i++) {
                    int tbeg = (i == 0 ? now : brem[4*(i - 1)]);

                    for (int j = i; j < jmax; j++) {
                      int tend = bold[4*j];

                      if (DEBUG) {
                        System.out.printf("set i=%d, j=%d\n", i, j);
                      }
                      if (DEBUG) {
                        System.out.printf("set tbeg=%d, tend=%d\n", tbeg, tend);
                      }

                      boolean ok = false;

                      if (DEBUG) {
                        System.out.printf("check capacity\n");
                      }
                      ok = (this.communicator.queryServerCapacityViolations(sid, rq, tbeg, tend)[0] == 0);
                      if (DEBUG) {
                        System.out.printf("set ok=%s\n", (ok ? "true" : "false"));
                      }

                      if (ok) {
                        brem = bold.clone();  // reset to original
                        int[] bnew = new int[] { };

                        int[] stop = new int[] { 0, ro, 0, rid };
                        int ipos = i;
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
                        ipos = (j + 1);
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

                        if (DEBUG) {
                          System.out.printf("check time window\n");
                        }
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
                        if (DEBUG) {
                          System.out.printf("set ok=%s\n", (ok ? "true" : "false"));
                        }

                        if (ok) {
                          int cdel = bnew[(bnew.length - 4)] - cost;
                          if (cdel < cmin) {
                            bmin = bnew;
                            wmin = wnew;
                            cmin = cdel;
                            smin = sid;
                            if (DEBUG) {
                              System.out.printf("got bnew=\n");
                              for (int __i = 0; __i < (bnew.length - 3); __i += 4) {
                                System.out.printf("  { %d, %d, %d, %d }\n",
                                    bnew[__i], bnew[__i+1], bnew[__i+2], bnew[__i+3]);
                              }
                            }
                            if (DEBUG) {
                              System.out.printf("set cmin=%d\n", cmin);
                            }
                          }
                        }

                      }
                    }
                  }
                  if (DEBUG) {
                    System.out.printf("end insertion heuristic\n");
                  }

                  candidates.remove(sid);
                  if (DEBUG) {
                    System.out.printf("remove candidate %d\n", sid);
                  }
                }

                if (DEBUG) {
                  System.out.printf("got candidates={ #%d }\n", candidates.size());
                }

                if (smin != 0) {
                  this.communicator.updateServerService(smin, wmin, bmin,
                      new int[] { rid }, new int[] { });
                }

              } catch (Exception e) {
                throw new ClientException(e);
              }
            }
}
