package com.github.jargors.jmx;
import com.github.jargors.core.Client;
public interface ClientMonitorMBean {
  public int getStatClientQueueSize();
  public long getStatClientHandleRequestDur();
}
