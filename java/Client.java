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
/*line 8 "src/Client.nw"*/
  
/*line 48 "src/Client.nw"*/
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
/*line 1063 "src/tex/4-Administration.nw"*/
int getQueueSize() {
  return this.queue.size();
}
/*line 231 "src/tex/0-Overview.nw"*/
public 
/*line 1272 "src/tex/4-Administration.nw"*/
void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
  this.tools.setRefCacheEdges(lu_edges);
}
/*line 232 "src/tex/0-Overview.nw"*/
public 
/*line 1279 "src/tex/4-Administration.nw"*/
void forwardRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
  this.tools.setRefCacheUsers(lu_users);
}
/*line 233 "src/tex/0-Overview.nw"*/
public 
/*line 1286 "src/tex/4-Administration.nw"*/
void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
  this.tools.setRefCacheVertices(lu_vertices);
}
/*line 234 "src/tex/0-Overview.nw"*/
public 
/*line 1228 "src/tex/4-Administration.nw"*/
void setRefCommunicator(final Communicator communicator) {
  this.communicator = communicator;
}
/*line 235 "src/tex/0-Overview.nw"*/
public 
/*line 64 "src/Client.nw"*/
void addRequest(final int[] r) {
  this.queue.add(r);
}
/*line 236 "src/tex/0-Overview.nw"*/
public 
/*line 91 "src/Client.nw"*/
void collectServerLocations(final int[] src) {
  this.endCollectServerLocations(src.clone());
}
/*line 237 "src/tex/0-Overview.nw"*/
public 
/*line 55 "src/Client.nw"*/
void notifyNew() throws ClientException, ClientFatalException {
  while (!this.queue.isEmpty()) {
    this.handleRequest(this.queue.remove());
  }
}
/*line 238 "src/tex/0-Overview.nw"*/
protected 
/*line 112 "src/Client.nw"*/
void end() { }
/*line 239 "src/tex/0-Overview.nw"*/
protected 
/*line 99 "src/Client.nw"*/
void endCollectServerLocations(final int[] locations) {
  for (int i = 0; i < (locations.length - 2); i += 3) {
    this.handleServerLocation(new int[] {
      locations[i],
      locations[(i + 1)],
      locations[(i + 2)]
    });
  }
}
/*line 240 "src/tex/0-Overview.nw"*/
protected 
/*line 128 "src/Client.nw"*/
void handleRequest(final int[] r) throws ClientException, ClientFatalException { }
/*line 241 "src/tex/0-Overview.nw"*/
protected 
/*line 144 "src/Client.nw"*/
void handleServerLocation(final int[] loc) { }
/*line 10 "src/Client.nw"*/
}
