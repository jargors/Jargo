package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public class NearestNeighbor extends Client {
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
                final int now = this.communicator.retrieveClock();
                if (DEBUG) {
                  System.out.printf("got now=%d\n", now);
                }
                final Map<Integer, Integer> candidates = new HashMap<Integer, Integer>();
                if (DEBUG) {
                  System.out.printf("init candidates[]={ }\n");
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

                  final int range = this.tools.computeHaversine(v, r[4]);
                  final boolean in_range = (range <= MAX_RANGE);
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
                Entry<Integer, Integer> min = null;
                for (final Entry<Integer, Integer> entry : candidates.entrySet()) {
                  if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                    if (DEBUG) {
                      System.out.printf("set min={ %d, %d }\n", min.getKey(), min.getValue());
                    }
                  }
                }
                if (min != null) {
                  final int sid = min.getKey();
                  final int[] brem = this.communicator.queryServerScheduleRemaining(sid, now);
                  if (DEBUG) {
                    System.out.printf("got brem: \n");
                    for (int i = 0; i < (brem.length - 3); i+=4) {
                      System.out.printf("  { t=%d, v=%d, ls=%d, lr=%d }\n",
                          brem[i], brem[i+1], brem[i+2], brem[i+3]);
                    }
                  }
                  final int m = brem.length/4;
                  final int p = m + 2;
                  if (DEBUG) {
                    System.out.printf("got m=%d\n", m);
                    System.out.printf("got p=%d\n", p);
                  }
                  final int[] bnew = new int[3*p];
                  final int[] bnew_t = new int[p];
                  final int[] bnew_v = new int[p];
                  final int[] bnew_l = new int[p];
                  if (DEBUG) {
                    System.out.printf("init bnew={ }\n");
                    System.out.printf("init bnew_t={ }\n");
                    System.out.printf("init bnew_v={ }\n");
                    System.out.printf("init bnew_l={ }\n");
                  }
                  bnew_v[0] = r[4];
                  bnew_v[1] = r[5];
                  if (DEBUG) {
                    System.out.printf("set bnew_v[0]=%d\n", bnew_v[0]);
                    System.out.printf("set bnew_v[1]=%d\n", bnew_v[1]);
                  }
                  bnew_l[0] = r[0];
                  bnew_l[1] = r[0];
                  if (DEBUG) {
                    System.out.printf("set bnew_l[0]=%d\n", bnew_l[0]);
                    System.out.printf("set bnew_l[1]=%d\n", bnew_l[1]);
                  }
                  for (int i = 0; i < m; i++) {
                    final int bv = brem[(i + 1)];
                    final int ls = brem[(i + 2)];
                    final int lr = brem[(i + 3)];
                    bnew_v[(i + 2)] = bv;
                    bnew_l[(i + 2)] = (ls == 0 ? lr : ls);
                    if (DEBUG) {
                      System.out.printf("set bnew_v[%d]=%d\n", (i + 2), bnew_v[(i + 2)]);
                      System.out.printf("set bnew_l[%d]=%d\n", (i + 2), bnew_l[(i + 2)]);
                    }
                  }
                  final int[][] legs = new int[p][];
                  int[] leg = null;
                  if (DEBUG) {
                    System.out.printf("init legs={ }\n");
                  }
                  leg = this.tools.computeRoute(locations.get(sid)[1], r[4], now);
                  leg[0] = locations.get(sid)[0];
                  legs[0] = leg;
                  if (DEBUG) {
                    System.out.printf("set legs[0]={ %d, %d, ..., %d, %d  }\n",
                        legs[0][0], legs[0][1], legs[0][legs[0].length - 2], legs[0][legs[0].length - 1]);
                  }
                  int n = leg.length;
                  int t = leg[n - 2];
                  if (DEBUG) {
                    System.out.printf("set n=%d\n", n);
                    System.out.printf("set t=%d\n", t);
                  }
                  for (int i = 1; i < p; i++) {
                    final int u = bnew_v[(i - 1)];
                    final int v = bnew_v[(i - 0)];
                    leg = this.tools.computeRoute(u, v, t);
                    legs[i] = leg;
                    if (DEBUG) {
                      System.out.printf("set legs[%d]={ %d, %d, ..., %d, %d  }\n", i,
                          legs[i][0], legs[i][1], legs[i][legs[i].length - 2], legs[i][legs[i].length - 1]);
                    }

                    n += (leg.length - 2);
                    t = leg[leg.length - 2];
                    if (DEBUG) {
                      System.out.printf("set n=%d\n", n);
                      System.out.printf("set t=%d\n", t);
                    }
                  }
                  for (int i = 1; i < legs.length; i++) {
                    bnew_t[(i - 1)] = legs[i][0];
                    if (DEBUG) {
                      System.out.printf("set bnew_t[%d]=%d\n", (i - 1), bnew_t[(i - 1)]);
                    }
                  }
                  bnew_t[(p - 1)] = t;
                  if (DEBUG) {
                    System.out.printf("set bnew_t[%d]=%d\n", (p - 1), bnew_t[(p - 1)]);
                  }
                  for (int i = 0; i < p; i++) {
                    bnew[(3*i + 0)] = bnew_t[i];
                    bnew[(3*i + 1)] = bnew_v[i];
                    bnew[(3*i + 2)] = bnew_l[i];
                    if (DEBUG) {
                      System.out.printf("set bnew[%d..%d]={ t=%d, v=%d, l=%d }\n",
                          (3*i), (3*i + 2), bnew[(3*i + 0)], bnew[(3*i + 1)], bnew[(3*i + 2)]);
                    }
                  }
                  final int[] wnew = new int[n];
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
                  this.communicator.updateServerService(sid, wnew, bnew,
                      new int[] { r[0] }, new int[] { });
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
