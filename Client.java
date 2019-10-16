package com.github.jargors;
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import java.util.function.Supplier;
import java.time.LocalDateTime;
public abstract class Client {
  protected int r_collection_period = 1;
  protected int s_collection_period = 10;
  protected Communicator communicator;
  protected Tools tools = new Tools();
  public Client() { }
    public void collectRequests(int[] src) {
      int[] requests = src.clone();
      endCollectRequests(requests);
    }
    public void collectServerLocations(int[] src) {
      int[] locations = src.clone();
      endCollectServerLocations(locations);
    }
    public void setCommunicator(Communicator src) {
      communicator = src;
    }
    public int getRequestCollectionPeriod() {
      return r_collection_period;
    }
    public void setRequestCollectionPeriod(int t) {
      r_collection_period = t;
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
    protected void endCollectRequests(int[] requests) {
      for (int i = 0; i < (requests.length - 6); i += 7) {
        handleRequest(new int[] {
          requests[i],
          requests[(i + 1)],
          requests[(i + 2)],
          requests[(i + 3)],
          requests[(i + 4)],
          requests[(i + 5)],
          requests[(i + 6)]
        });
      }
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
    protected void handleRequest(int[] r) { }
    protected void handleServerLocation(int[] loc) { }
    protected void Print(String msg) {
      System.out.println("[Client]["+LocalDateTime.now()+"]"
        + "[t="+communicator.getSimulationWorldTime()+"] "+msg);
    }

}
