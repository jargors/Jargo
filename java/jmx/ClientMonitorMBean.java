package com.github.jargors.jmx;
import com.github.jargors.sim.Client;
public interface ClientMonitorMBean {
  public int getStatClientQueueSize();
  public long getStatClientHandleRequestDur();
}
