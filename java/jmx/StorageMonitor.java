package com.github.jargors.jmx;
import com.github.jargors.jmx.StorageMonitorMBean;
import com.github.jargors.Storage;
public class StorageMonitor implements StorageMonitorMBean {
  private Storage storage = null;
  public StorageMonitor(final Storage storage) {
    this.storage = storage;
  }
}

