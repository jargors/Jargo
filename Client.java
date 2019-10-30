package com.github.jargors;
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.LocalDateTime;
public abstract class Client {
  protected ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue();
  protected int r_collection_period = 1;  // how many sec before collecting new req?
  protected int r_handling_period = 1;  // how many msec before handling queued req?
  protected int s_collection_period = 10;
  protected Communicator communicator;
  protected Tools tools = new Tools();
  protected boolean DEBUG = false;
  public Client() { }
    public void notifyNew() {
      if (!queue.isEmpty()) {
        handleRequest(queue.remove());
      }
    }
    public void collectRequest(int[] r) {
      queue.add(r);
    }
    public void collectServerLocations(int[] src) {
      int[] locations = src.clone();
      endCollectServerLocations(locations);
    }
    public void setCommunicator(Communicator src) {
      communicator = src;
    }
    public void setDebug(boolean flag) {
      DEBUG = flag;
    }
    public int getRequestCollectionPeriod() {
      return r_collection_period;
    }
    public void setRequestCollectionPeriod(int t) {
      r_collection_period = t;
    }
    public int getRequestHandlingPeriod() {
      return r_handling_period;
    }
    public void setRequestHandlingPeriod(int t) {
      r_handling_period = t;
    }
    public int getServerLocationCollectionPeriod () {
      return s_collection_period;
    }
    public void setServerLocationCollectionPeriod(int t) {
      s_collection_period = t;
    }
    public void loadGTree(String p) {
      tools.loadGTree(p);
    }
    public void registerRoadNetwork() {
      tools.registerVertices(communicator.getReferenceVerticesCache());
      tools.registerEdges(communicator.getReferenceEdgesCache());
    }
    public void registerUsers() {
      tools.registerUsers(communicator.getReferenceUsersCache());
    }
    protected void endCollectServerLocations(int[] locations) {
      for (int i = 0; i < (locations.length - 2); i += 3) {
        handleServerLocation(new int[] {
          locations[i],
          locations[(i + 1)],
          locations[(i + 2)]
        });
      }
    }
    protected void end() { }
    protected void handleRequest(int[] r) { }
    protected void handleServerLocation(int[] loc) { }
    protected void Print(String msg) {
      if (DEBUG) {
        System.out.println("[Client]["+LocalDateTime.now()+"]"
          + "[t="+communicator.getSimulationWorldTime()+"] "+msg);
      }
    }
}
