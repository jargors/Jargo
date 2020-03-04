package com.github.jargors.client;
import com.github.jargors.sim.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public class NearestB extends Client {
  final int MAX_DELTA = 300;  // seconds from last vehicle location update
  final int MAX_RANGE = 600;  // meters from last vehicle position
  final ConcurrentHashMap<Integer, int[]> locations =
    new ConcurrentHashMap<Integer, int[]>();
  protected void handleRequest(int[] r) throws ClientException, ClientFatalException {
              if (DEBUG) {
                System.out.printf("got request={ id=%d, q=%d, e=%d, l=%d, o=%d, d=%d, b=%d }\n",
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6]);
              }
              final int rid = r[0];
              final int ro  = r[4];
              final int rd  = r[5];
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
                  //NearestA: Step 4: Prepare Scheduling
                  //Scheduling: Prepend After
                  //NearestB: Step 5: Prepare Routing
                  //Routing: Shortest Path
                  //NearestA: Step 6: Submit
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
