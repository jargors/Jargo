  package com.github.jargors;
  import com.github.jargors.SimulationInterface;
  import java.time.LocalDateTime;
  public abstract class JargoClient {
    protected int[] requests;
    protected int[] locations;
    protected int r_collection_period = 1;
    protected int s_collection_period = 1;
    protected SimulationInterface simulator;
    public JargoClient() { }
      public void collectRequests(int[] src) {
        requests = src.clone();
        endCollectRequests();
      }
      public void collectServerLocations(int[] src) {
        locations = src.clone();
        endCollectServerLocations();
      }
      public void setSimulationInterface(SimulationInterface src) {
        simulator = src;
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
      protected void handleRequest(int[] r) {
        Print("handleRequest(1) got Request "+r[0]);
      }
      protected void handleServerLocation(int[] loc) {
        Print("handleServerLocation(1) got Server "+loc[0]);
      }
      protected void Print(String msg) {
        System.out.println("[JargoClient]["+LocalDateTime.now()+"]"
          + "[t="+simulator.getSimulationWorldTime()+"] "+msg);
      }

  }
