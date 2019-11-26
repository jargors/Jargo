package com.github.jargors.jmx;
import com.github.jargors.jmx.ClientMonitorMBean;
import com.github.jargors.Client;
public class ClientMonitor implements ClientMonitorMBean {
  private Client client = null;
  public ClientMonitor(final Client client) {
    this.client = client;
  }
  public int getStatCountHandleRequest() {
    return this.client.getStatCountHandleRequest();
  }
  public int getStatCountQueueSize() {
    return this.client.getStatCountQueueSize();
  }
  public long getStatDurTotalHandleRequest() {
    return this.client.getStatDurTotalHandleRequest();
  }
  public long getStatDurLastHandleRequest() {
    return this.client.getStatDurLastHandleRequest();
  }
}
