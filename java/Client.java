package com.github.jargors;
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileNotFoundException;
public abstract class Client {
  protected ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue();
  protected int r_collection_period = 1;  // how many sec before collecting new req?
  protected int r_handling_period = 1;  // how many msec before handling queued req?
  protected int s_collection_period = 10;
  protected Communicator communicator;
  protected Tools tools = new Tools();
  protected final boolean DEBUG = "true".equals(System.getProperty("jargors.client.debug"));
  public Client() { }
  public void loadGtree(String p) throws FileNotFoundException {
           this.tools.GTLoadGtree(p);
         }
  public void closeGtree() {
           this.tools.GTCloseGtree();
         }
  public void notifyNew() throws ClientException, ClientFatalException {
           if (!this.queue.isEmpty()) {
             this.handleRequest(this.queue.remove());
           }
         }
  public void collectRequest(final int[] r) {
           this.queue.add(r);
         }
  public void collectServerLocations(final int[] src) {
           this.endCollectServerLocations(src.clone());
         }
  public void registerCommunicator(final Communicator src) {
           this.communicator = src;
         }
  public int getRequestCollectionPeriod() {
           return this.r_collection_period;
         }
  public void setRequestCollectionPeriod(final int t) {
           this.r_collection_period = t;
         }
  public int getRequestHandlingPeriod() {
           return this.r_handling_period;
         }
  public void setRequestHandlingPeriod(final int t) {
           this.r_handling_period = t;
         }
  public int getServerLocationCollectionPeriod () {
           return this.s_collection_period;
         }
  public void setServerLocationCollectionPeriod(final int t) {
           this.s_collection_period = t;
         }
  public void registerRoadNetwork() {
           this.tools.registerVertices(this.communicator.forwardReferenceVerticesCache());
           this.tools.registerEdges(this.communicator.forwardReferenceEdgesCache());
         }
  public void registerUsers() {
           this.tools.registerUsers(this.communicator.forwardReferenceUsersCache());
         }
  protected void endCollectServerLocations(final int[] locations) {
              for (int i = 0; i < (locations.length - 2); i += 3) {
                this.handleServerLocation(new int[] {
                  locations[i],
                  locations[(i + 1)],
                  locations[(i + 2)]
                });
              }
            }
  protected void end() { }
  protected void handleRequest(final int[] r) throws ClientException, ClientFatalException { }
  protected void handleServerLocation(final int[] loc) { }
}
