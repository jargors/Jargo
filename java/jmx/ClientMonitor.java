package com.github.jargors.jmx;
import com.github.jargors.sim.Client;
import com.github.jargors.jmx.ClientMonitorMBean;
public class ClientMonitor implements ClientMonitorMBean {
  private Client client = null;
  public ClientMonitor(final Client client) {
    this.client = client;
  }
  public int getStatClientQueueSize() {
    return this.client.getStatClientQueueSize();
  }
  public long getStatClientHandleRequestDur() {
    return this.client.getStatClientHandleRequestDur();
  }
}
