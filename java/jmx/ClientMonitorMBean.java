package com.github.jargors.jmx;
import com.github.jargors.Client;
public interface ClientMonitorMBean {
  public int getStatClientQueueSize();
  public long getClientHandleRequestDurLast();
}
