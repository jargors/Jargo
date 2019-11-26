package com.github.jargors.jmx;
import com.github.jargors.jmx.StorageMonitorMBean;
import com.github.jargors.Storage;
public class StorageMonitor implements StorageMonitorMBean {
  private Storage storage = null;
  public StorageMonitor(final Storage storage) {
    this.storage = storage;
  }
  public int getStatCountDBQueryServersLocationsActive() {
    return this.storage.getStatCountDBQueryServersLocationsActive();
  }
  public int getStatCountDBQueryServerScheduleRemaining() {
    return this.storage.getStatCountDBQueryServerScheduleRemaining();
  }
  public int getStatCountDBQueryRequestsQueued() {
    return this.storage.getStatCountDBQueryRequestsQueued();
  }
  public long getStatDurTotalDBQueryServersLocationsActive() {
    return this.storage.getStatDurTotalDBQueryServersLocationsActive();
  }
  public long getStatDurTotalDBQueryServerScheduleRemaining() {
    return this.storage.getStatDurTotalDBQueryServerScheduleRemaining();
  }
  public long getStatDurTotalDBQueryRequestsQueued() {
    return this.storage.getStatDurTotalDBQueryRequestsQueued();
  }
  public long getStatDurLastDBQueryServersLocationsActive() {
    return this.storage.getStatDurLastDBQueryServersLocationsActive();
  }
  public long getStatDurLastDBQueryServerScheduleRemaining() {
    return this.storage.getStatDurLastDBQueryServerScheduleRemaining();
  }
  public long getStatDurLastDBQueryRequestsQueued() {
    return this.storage.getStatDurLastDBQueryRequestsQueued();
  }
}

