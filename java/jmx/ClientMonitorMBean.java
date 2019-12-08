/* Call all the methods listed under <<[[Client]] methods>> in src/tex/JMX.nw
 */
package com.github.jargors.jmx;
import com.github.jargors.Client;
public interface ClientMonitorMBean {
  public int getStatClientQueueSize();
  public long getStatClientHandleRequestDur();
}
