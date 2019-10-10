package com.github.jargors;
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import java.util.function.Supplier;
import java.time.LocalDateTime;
public abstract class Client {
  protected int[] requests;
  protected int[] locations;
  protected int r_collection_period = 1;
  protected int s_collection_period = 10;
  protected Communicator communicator;
  protected Tools tools = new Tools();
  public Client() { }
    public void collectRequests(int[] src) {
      requests = src.clone();
      endCollectRequests();
    }
    public void collectServerLocations(int[] src) {
      locations = src.clone();
      endCollectServerLocations();
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
    public int computeHaversine(int u, int v) {
      int[] U = communicator.queryVertex(u);
      int[] V = communicator.queryVertex(v);
      return tools.computeHaversine(U[0], U[1], V[0], V[1]);
    }
    protected void endCollectRequests() {
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
    protected void endCollectServerLocations() {
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
