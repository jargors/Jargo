package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public class NearestA extends Client {
  final int MAX_DELTA = 300;  // seconds from last vehicle location update
  final int MAX_RANGE = 600;  // meters from last vehicle position
  final ConcurrentHashMap<Integer, int[]> locations =
    new ConcurrentHashMap<Integer, int[]>();
  protected void handleRequest(int[] r) throws ClientException, ClientFatalException {
              if (DEBUG) {
                System.out.printf("got request={ id=%d, q=%d, e=%d, l=%d, o=%d, d=%d, b=%d }\n",
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6]);
              }
              try {
                final Map<Integer, Integer> candidates = new HashMap<Integer, Integer>();
                Entry<Integer, Integer> min = null;
                final int now = this.communicator.retrieveClock();
                final int rid = r[0];
                final int rq  = r[1];
                final int ro  = r[4];
                final int rd  = r[5];
                boolean ok = false;
                if (DEBUG) {
                  System.out.printf("got now=%d\n", now);
                  System.out.printf("init candidates[]={ }\n");
                  System.out.printf("init min=null\n");
                  System.out.printf("init ok=false\n");
                }
                for (final Integer sid : locations.keySet()) {
                  final int t = locations.get(sid)[0];
                  final int v = locations.get(sid)[1];

                  final boolean in_delta = (t >= now - MAX_DELTA);
                  if (DEBUG) {
                    System.out.printf("got %d in_delta=%s\n", sid, (in_delta ? "true" : "false"));
                  }
                  if (!in_delta)
                    continue;

                  final int range = this.tools.computeHaversine(v, ro);
                  final boolean in_range = (0 != range && range <= MAX_RANGE);
                  if (DEBUG) {
                    System.out.printf("got %d in_range=%s\n", sid, (in_range ? "true" : "false"));
                  }
                  if (!in_range)
                    continue;

                  candidates.put(sid, range);
                  if (DEBUG) {
                    System.out.printf("put candidates[], key=%d, val=%d\n", sid, range);
                  }
                }
                while (ok == false && candidates.size() > 0) {
                  for (final Entry<Integer, Integer> entry : candidates.entrySet()) {
                    if (min == null || min.getValue() > entry.getValue()) {
                      min = entry;
                      if (DEBUG) {
                        System.out.printf("set min={ %d, %d }\n", min.getKey(), min.getValue());
                      }
                    }
                  }
                  final int sid = min.getKey();
                  final int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);
                  final int[] wact = this.communicator.queryServerRouteActive(sid);
                  int[] bnew = new int[] { };
                  int[] wnew = new int[] { };
                  if (DEBUG) {
                    System.out.printf("got brem: \n");
                    for (int i = 0; i < (brem.length - 3); i+=4) {
                      System.out.printf("  { t=%d, v=%d, ls=%d, lr=%d }\n",
                          brem[i], brem[i+1], brem[i+2], brem[i+3]);
                    }
                    System.out.printf("got wact: \n");
                    for (int i = 0; i < (wact.length - 1); i += 2) {
                      System.out.printf("  { t=%d, v=%d },\n",
                        wact[i], wact[i+1]);
                    }
                  }
                  if (brem[2] == sid) {
                    if (DEBUG) {
                      System.out.printf("goto case 1\n");
                    }
                    ok = true;
                    if (DEBUG) {
                      System.out.printf("set ok=%s\n", ok ? "true" : "false");
                    }
                    {
                      if (DEBUG) {
                        System.out.printf("prepend\n");
                      }
                      final int m = (brem.length/4);
                      bnew = new int[(3*(m + 2))];
                      if (DEBUG) {
                        System.out.printf("init bnew={ }, length=%d\n", bnew.length);
                      }
                      bnew[1] = ro;
                      bnew[2] = rid;
                      bnew[4] = rd;
                      bnew[5] = rid;
                      if (DEBUG) {
                        System.out.printf("set bnew[1]=%d\n", bnew[1]);
                        System.out.printf("set bnew[2]=%d\n", bnew[2]);
                        System.out.printf("set bnew[4]=%d\n", bnew[4]);
                        System.out.printf("set bnew[5]=%d\n", bnew[5]);
                      }
                      for (int i = 0; i < m; i++) {
                        // Extract vertex and labels
                        final int bv = brem[(4*i + 1)];
                        final int ls = brem[(4*i + 2)];
                        final int lr = brem[(4*i + 3)];
                        // Copy into bnew
                        bnew[(3*i + 7)] = bv;
                        bnew[(3*i + 8)] = (ls == 0 ? lr : ls);  // <-- squeeze the label
                        if (DEBUG) {
                          System.out.printf("set bnew[%d]=%d\n", (3*i + 7), bnew[(3*i + 7)]);
                          System.out.printf("set bnew[%d]=%d\n", (3*i + 8), bnew[(3*i + 8)]);
                        }
                      }
                    }
                  } else {
                    if (brem[0] == wact[2]) {
                      if (DEBUG) {
                        System.out.printf("goto case 2\n");
                      }
                      ok = (this.communicator.queryServerCapacityViolations(
                          sid, rq, brem[0], brem[4])[0] == 0);
                      if (DEBUG) {
                        System.out.printf("set ok=%s\n", ok ? "true" : "false");
                      }
                      if (ok) {
                        {
                          if (DEBUG) {
                            System.out.printf("prepend after\n");
                          }
                          final int m = (brem.length/4);
                          bnew = new int[(3*(m + 2))];
                          if (DEBUG) {
                            System.out.printf("init bnew={ }, length=%d\n", bnew.length);
                          }
                          bnew[4] = ro;
                          bnew[5] = rid;
                          bnew[7] = rd;
                          bnew[8] = rid;
                          if (DEBUG) {
                            System.out.printf("set bnew[4]=%d\n", bnew[4]);
                            System.out.printf("set bnew[5]=%d\n", bnew[5]);
                            System.out.printf("set bnew[7]=%d\n", bnew[7]);
                            System.out.printf("set bnew[8]=%d\n", bnew[8]);
                          }
                          for (int i = 0; i < m; i++) {
                            // Extract vertex and labels
                            final int bv = brem[(4*i + 1)];
                            final int ls = brem[(4*i + 2)];
                            final int lr = brem[(4*i + 3)];
                            // Copy into bnew and shift if not first event
                            bnew[(3*i + (i < 1 ? 1 : 7))] = bv;
                            bnew[(3*i + (i < 1 ? 2 : 8))] = (ls == 0 ? lr : ls);
                            if (DEBUG) {
                              if (i < 1) {
                                System.out.printf("set bnew[%d]=%d\n", (3*i + 1), bnew[(3*i + 1)]);
                                System.out.printf("set bnew[%d]=%d\n", (3*i + 2), bnew[(3*i + 2)]);
                              } else {
                                System.out.printf("set bnew[%d]=%d\n", (3*i + 7), bnew[(3*i + 7)]);
                                System.out.printf("set bnew[%d]=%d\n", (3*i + 8), bnew[(3*i + 8)]);
                              }
                            }
                          }
                        }
                        int[] temp = new int[(bnew.length - 3)];
                        for (int i = 0; i < temp.length; i++) {
                          temp[i] = bnew[(i + 3)];
                        }
                        bnew = temp;
                      }
                    } else {
                      if (DEBUG) {
                        System.out.printf("goto case 3\n");
                      }
                      ok = (this.communicator.queryServerCapacityViolations(
                          sid, rq, now, brem[0])[0] == 0);
                      if (DEBUG) {
                        System.out.printf("set ok=%s\n", ok ? "true" : "false");
                      }
                      if (ok) {
                        {
                          if (DEBUG) {
                            System.out.printf("prepend\n");
                          }
                          final int m = (brem.length/4);
                          bnew = new int[(3*(m + 2))];
                          if (DEBUG) {
                            System.out.printf("init bnew={ }, length=%d\n", bnew.length);
                          }
                          bnew[1] = ro;
                          bnew[2] = rid;
                          bnew[4] = rd;
                          bnew[5] = rid;
                          if (DEBUG) {
                            System.out.printf("set bnew[1]=%d\n", bnew[1]);
                            System.out.printf("set bnew[2]=%d\n", bnew[2]);
                            System.out.printf("set bnew[4]=%d\n", bnew[4]);
                            System.out.printf("set bnew[5]=%d\n", bnew[5]);
                          }
                          for (int i = 0; i < m; i++) {
                            // Extract vertex and labels
                            final int bv = brem[(4*i + 1)];
                            final int ls = brem[(4*i + 2)];
                            final int lr = brem[(4*i + 3)];
                            // Copy into bnew
                            bnew[(3*i + 7)] = bv;
                            bnew[(3*i + 8)] = (ls == 0 ? lr : ls);  // <-- squeeze the label
                            if (DEBUG) {
                              System.out.printf("set bnew[%d]=%d\n", (3*i + 7), bnew[(3*i + 7)]);
                              System.out.printf("set bnew[%d]=%d\n", (3*i + 8), bnew[(3*i + 8)]);
                            }
                          }
                        }
                      }
                    }
                  }
                  if (ok == true) {
                    // start route from next waypoint (not last-visited)
                    final int[] wbeg = new int[] { wact[2], wact[3] };
                    if (wact[3] == 0) {
                      // if next waypoint is vehicle destination, then start from (now, last-visited)
                      wbeg[0] = now;
                      wbeg[1] = wact[1];
                    }
                    if (DEBUG) {
                      System.out.printf("set wbeg[0..1]={%d, %d}\n", wbeg[0], wbeg[1]);
                    }
                    {
                      if (DEBUG) {
                        System.out.printf("shortest path\n");
                      }
                      final int p = (bnew.length/3);
                      final int[][] legs = new int[p][];
                      if (DEBUG) {
                        System.out.printf("init legs={ }\n");
                      }

                      int[] leg = this.tools.computeRoute(wbeg[1], bnew[1], wbeg[0]);
                      int n = leg.length;
                      int t = leg[(n - 2)];
                      if (DEBUG) {
                        System.out.printf("set n=%d\n", n);
                        System.out.printf("set t=%d\n", t);
                      }

                      legs[0] = leg;
                      if (DEBUG) {
                        System.out.printf("set legs[0]={ %d, %d, ..., %d, %d  }\n",
                            legs[0][0], legs[0][1], legs[0][legs[0].length - 2], legs[0][legs[0].length - 1]);
                      }
                      for (int i = 1; i < p; i++) {
                        // Extract vertices
                        final int u = bnew[(3*i - 2)];
                        final int v = bnew[(3*i + 1)];
                        // Compute path and store into legs
                        leg = this.tools.computeRoute(u, v, t);
                        legs[i] = leg;
                        if (DEBUG) {
                          System.out.printf("set legs[%d]={ %d, %d, ..., %d, %d  }\n", i,
                              legs[i][0], legs[i][1], legs[i][legs[i].length - 2], legs[i][legs[i].length - 1]);
                        }
                        // Update n and t
                        n += (leg.length - 2);
                        t = leg[leg.length - 2];
                        if (DEBUG) {
                          System.out.printf("set n=%d\n", n);
                          System.out.printf("set t=%d\n", t);
                        }
                      }
                      wnew = new int[n];
                      int k = 0;
                      for (int i = 0; i < legs.length; i++) {
                        int rend = (legs[i].length - (i == (legs.length - 1) ? 0 : 2));
                        for (int j = 0; j < rend; j++) {
                          wnew[k] = legs[i][j];
                          if (DEBUG) {
                            System.out.printf("set wnew[%d]=%d\n", k, wnew[k]);
                          }
                          k++;
                        }
                      }
                      for (int i = 1; i < legs.length; i++) {
                        bnew[(3*i - 3)] = legs[i][0];
                        if (DEBUG) {
                          System.out.printf("set bnew[%d]=%d\n", (3*i - 3), bnew[(3*i - 3)]);
                        }
                      }
                      bnew[(3*p - 3)] = t;
                      if (DEBUG) {
                        System.out.printf("set bnew[%d]=%d\n", (3*p - 3), bnew[(3*p - 3)]);
                      }
                    }
                    if (wact[3] == 0) {
                      // if next waypoint is vehicle destination, reset route start time to last-visited time
                      wnew[0] = locations.get(sid)[0];
                    }
                    if (DEBUG) {
                      System.out.printf("set wnew[0]=%d\n", wnew[0]);
                    }
                    this.communicator.updateServerService(sid, wnew, bnew,
                        new int[] { rid }, new int[] { });
                    if (DEBUG) {
                      System.out.printf("submit:\n");
                      System.out.printf("  server=%d\n", sid);
                      System.out.printf("  wnew={ %d, %d, ..., %d, %d }\n",
                          wnew[0], wnew[1], wnew[wnew.length - 2], wnew[wnew.length - 1]);
                      System.out.printf("  bnew={ %d, %d, %d, ..., %d, %d, %d }\n",
                          bnew[0], bnew[1], bnew[2],
                          bnew[bnew.length - 3], bnew[bnew.length - 2], bnew[bnew.length - 1]);
                      System.out.printf("  radd={ %d }\n", r[0]);
                      System.out.printf("  rsub={ }\n");
                    }
                  } else {
                    min = null;
                    candidates.remove(sid);
                    if (DEBUG) {
                      System.out.printf("set min=null, remove candidate %d\n", sid);
                    }
                  }
                }
              } catch (Exception e) {
                throw new ClientException(e);
              }
            }
  protected void handleServerLocation(int[] s) {
              this.locations.put(s[0], new int[] { s[1], s[2] });
              if (DEBUG) {
                System.out.printf("put locations[%d]=[ %d, %d ]\n", s[0], s[1], s[2]);
              }
            }
}
