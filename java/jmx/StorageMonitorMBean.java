package com.github.jargors.jmx;
import com.github.jargors.Storage;
public interface StorageMonitorMBean {
  public int getStatCountDBQueryServersLocationsActive();
  public int getStatCountDBQueryServerScheduleRemaining();
  public int getStatCountDBQueryRequestsQueued();
  public long getStatDurTotalDBQueryServersLocationsActive();
  public long getStatDurTotalDBQueryServerScheduleRemaining();
  public long getStatDurTotalDBQueryRequestsQueued();
  public long getStatDurLastDBQueryServersLocationsActive();
  public long getStatDurLastDBQueryServerScheduleRemaining();
  public long getStatDurLastDBQueryRequestsQueued();
}
