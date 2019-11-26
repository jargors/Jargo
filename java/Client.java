/*line 22 "src/Client.nw"*/
package com.github.jargors;
/*line 28 "src/Client.nw"*/
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileNotFoundException;
/*line 6 "src/Client.nw"*/
public abstract class Client {
  
/*line 39 "src/Client.nw"*/
protected ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue<int[]>();
protected Communicator communicator;
protected Tools tools = new Tools();
protected final boolean DEBUG =
    "true".equals(System.getProperty("jargors.client.debug"));
/*line 46 "src/Client.nw"*/
private int stat_count_handleRequest = 0;
private long stat_dur_total_handleRequest = 0;
private long stat_dur_last_handleRequest = 0;
/*line 8 "src/Client.nw"*/
  
/*line 53 "src/Client.nw"*/
public Client() { }
/*line 9 "src/Client.nw"*/
  
/*line 228 "src/tex/0-Overview.nw"*/
public 
/*line 33 "src/tex/5-Gtree.nw"*/
void gtreeLoad(String p) throws FileNotFoundException {
  this.tools.GTGtreeLoad(p);
}
/*line 229 "src/tex/0-Overview.nw"*/
public 
/*line 52 "src/tex/5-Gtree.nw"*/
void gtreeClose() {
  this.tools.GTGtreeClose();
}
/*line 230 "src/tex/0-Overview.nw"*/
public 
/*line 1084 "src/tex/4-Administration.nw"*/
int getStatCountHandleRequest() {
  return this.stat_count_handleRequest;
}
/*line 231 "src/tex/0-Overview.nw"*/
public 
/*line 1091 "src/tex/4-Administration.nw"*/
int getStatCountQueueSize() {
  return this.queue.size();
}
/*line 232 "src/tex/0-Overview.nw"*/
public 
/*line 1119 "src/tex/4-Administration.nw"*/
long getStatDurTotalHandleRequest() {
  return this.stat_dur_total_handleRequest;
}
/*line 233 "src/tex/0-Overview.nw"*/
public 
/*line 1147 "src/tex/4-Administration.nw"*/
long getStatDurLastHandleRequest() {
  return this.stat_dur_last_handleRequest;
}
/*line 234 "src/tex/0-Overview.nw"*/
public 
/*line 1293 "src/tex/4-Administration.nw"*/
void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
  this.tools.setRefCacheEdges(lu_edges);
}
/*line 235 "src/tex/0-Overview.nw"*/
public 
/*line 1300 "src/tex/4-Administration.nw"*/
void forwardRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
  this.tools.setRefCacheUsers(lu_users);
}
/*line 236 "src/tex/0-Overview.nw"*/
public 
/*line 1307 "src/tex/4-Administration.nw"*/
void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
  this.tools.setRefCacheVertices(lu_vertices);
}
/*line 237 "src/tex/0-Overview.nw"*/
public 
/*line 1249 "src/tex/4-Administration.nw"*/
void setRefCommunicator(final Communicator communicator) {
  this.communicator = communicator;
}
/*line 238 "src/tex/0-Overview.nw"*/
public 
/*line 74 "src/Client.nw"*/
void addRequest(final int[] r) {
  this.queue.add(r);
}
/*line 239 "src/tex/0-Overview.nw"*/
public 
/*line 101 "src/Client.nw"*/
void collectServerLocations(final int[] src) {
  this.endCollectServerLocations(src.clone());
}
/*line 240 "src/tex/0-Overview.nw"*/
public 
/*line 60 "src/Client.nw"*/
void notifyNew() throws ClientException, ClientFatalException {
  while (!this.queue.isEmpty()) {
    this.stat_count_handleRequest++;
    long A0 = System.currentTimeMillis();
    this.handleRequest(this.queue.remove());
    this.stat_dur_last_handleRequest = (System.currentTimeMillis() - A0);
    this.stat_dur_total_handleRequest +=
      this.stat_dur_last_handleRequest;
  }
}
/*line 241 "src/tex/0-Overview.nw"*/
protected 
/*line 122 "src/Client.nw"*/
void end() { }
/*line 242 "src/tex/0-Overview.nw"*/
protected 
/*line 109 "src/Client.nw"*/
void endCollectServerLocations(final int[] locations) {
  for (int i = 0; i < (locations.length - 2); i += 3) {
    this.handleServerLocation(new int[] {
      locations[i],
      locations[(i + 1)],
      locations[(i + 2)]
    });
  }
}
/*line 243 "src/tex/0-Overview.nw"*/
protected 
/*line 138 "src/Client.nw"*/
void handleRequest(final int[] r) throws ClientException, ClientFatalException { }
/*line 244 "src/tex/0-Overview.nw"*/
protected 
/*line 154 "src/Client.nw"*/
void handleServerLocation(final int[] loc) { }
/*line 10 "src/Client.nw"*/
}
