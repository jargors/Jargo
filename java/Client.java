package com.github.jargors;
import com.github.jargors.Communicator;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.ClientException;
import com.github.jargors.exceptions.ClientFatalException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileNotFoundException;
import com.github.jargors.jmx.ClientMonitor;
import java.lang.management.*;
import javax.management.*;
public abstract class Client {
  protected ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue<int[]>();
  protected Communicator communicator;
  protected Tools tools = new Tools();
  protected final boolean DEBUG =
      "true".equals(System.getProperty("jargors.client.debug"));
  private int    statClientHandleRequestCount = 0;
  private long   statClientHandleRequestDurLast = 0;
  private long   statClientHandleRequestDurMin = Integer.MAX_VALUE;
  private long   statClientHandleRequestDurMax = 0;
  private long   statClientHandleRequestDurTotal = 0;
  private double statClientHandleRequestDurAvg = 0;
  public Client() {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ClientMonitor mon = new ClientMonitor(this);
      mbs.registerMBean(mon, new ObjectName("com.github.jargors.jmx:type=ClientMonitor"));
    } catch (Exception e) {
      System.err.printf("ClientMonitor failed; reason: %s\n", e.toString());
      System.err.printf("Continuing with monitoring disabled\n");
    }
  }
  public int getStatClientQueueSize() {
           return this.queue.size();
         }
  public long getClientHandleRequestDurLast() {
           return this.statClientHandleRequestDurLast;
         }
  public long getClientHandleRequestDurMin() {
           return this.statClientHandleRequestDurMin;
         }
  public long getClientHandleRequestDurMax() {
           return this.statClientHandleRequestDurMax;
         }
  public double getClientHandleRequestDurAvg() {
           return this.statClientHandleRequestDurAvg;
         }
  public final void gtreeLoad(String p) throws FileNotFoundException {
                 this.tools.GTGtreeLoad(p);
               }
  public final void gtreeClose() {
                 this.tools.GTGtreeClose();
               }
  public final void forwardRefCacheEdges(final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, int[]>> lu_edges) {
                 this.tools.setRefCacheEdges(lu_edges);
               }
  public final void forwardRefCacheUsers(final ConcurrentHashMap<Integer, int[]> lu_users) {
                 this.tools.setRefCacheUsers(lu_users);
               }
  public final void forwardRefCacheVertices(final ConcurrentHashMap<Integer, int[]> lu_vertices) {
                 this.tools.setRefCacheVertices(lu_vertices);
               }
  public final void setRefCommunicator(final Communicator communicator) {
                 this.communicator = communicator;
               }
  public final void addRequest(final int[] r) {
                 this.queue.add(r);
               }
  public final void collectServerLocations(final int[] src) {
                 this.endCollectServerLocations(src.clone());
               }
  public final void notifyNew() throws ClientException, ClientFatalException {
                 while (!this.queue.isEmpty()) {
                   long A0 = System.currentTimeMillis();
                   this.handleRequest(this.queue.remove());
                       this.statClientHandleRequestCount++;
                       this.statClientHandleRequestDurLast = (System.currentTimeMillis() - A0);
                       this.statClientHandleRequestDurTotal +=
                       this.statClientHandleRequestDurLast;
                   if (this.statClientHandleRequestDurLast <
                       this.statClientHandleRequestDurMin) {
                       this.statClientHandleRequestDurMin =
                       this.statClientHandleRequestDurLast;}
                   if (this.statClientHandleRequestDurLast >
                       this.statClientHandleRequestDurMax) {
                       this.statClientHandleRequestDurMax =
                       this.statClientHandleRequestDurLast;}
                       this.statClientHandleRequestDurAvg = (double)
                       this.statClientHandleRequestDurTotal/
                       this.statClientHandleRequestCount;
                 }
               }
  protected void end() { }
  protected void endCollectServerLocations(final int[] locations) {
              for (int i = 0; i < (locations.length - 2); i += 3) {
                this.handleServerLocation(new int[] {
                  locations[i],
                  locations[(i + 1)],
                  locations[(i + 2)]
                });
              }
            }
  protected void handleRequest(final int[] r) throws ClientException, ClientFatalException { }
  protected void handleServerLocation(final int[] loc) { }
}
