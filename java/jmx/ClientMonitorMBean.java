package com.github.jargors.jmx;
import com.github.jargors.Client;
public interface ClientMonitorMBean {
  public int getStatCountHandleRequest();
  public int getStatCountQueueSize();
  public long getStatDurTotalHandleRequest();
  public long getStatDurLastHandleRequest();
}
