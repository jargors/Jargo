/* Call all the methods listed under <<[[Client]] methods>> in src/tex/JMX.nw
 */
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
  public long getStatClientHandleRequestDur() {
    return this.client.getStatClientHandleRequestDur();
  }
}
