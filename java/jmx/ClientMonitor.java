package com.github.jargors.jmx;
import com.github.jargors.jmx.ClientMonitorMBean;
import com.github.jargors.Client;
public class ClientMonitor implements ClientMonitorMBean {
  private Client client = null;
  public ClientMonitor(final Client client) {
    this.client = client;
  }
  public int getStatClientQueueSize() {
    return this.client.getStatClientQueueSize();
  }
  public long getClientHandleRequestDurLast() {
    return this.client.getClientHandleRequestDurLast();
  }
  public long getClientHandleRequestDurMin() {
    return this.client.getClientHandleRequestDurMin();
  }
  public long getClientHandleRequestDurMax() {
    return this.client.getClientHandleRequestDurMax();
  }
  public double getClientHandleRequestDurAvg() {
    return this.client.getClientHandleRequestDurAvg();
  }
}
