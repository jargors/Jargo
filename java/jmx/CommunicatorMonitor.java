package com.github.jargors.jmx;
import com.github.jargors.jmx.CommunicatorMonitorMBean;
import com.github.jargors.Communicator;
public class CommunicatorMonitor implements CommunicatorMonitorMBean {
  private Communicator communicator = null;
  public CommunicatorMonitor(final Communicator communicator) {
    this.communicator = communicator;
  }
  public long   getStatQueryEdgeDurLast() {
    return this.communicator.getStatQueryEdgeDurLast();
  }
  public long   getStatQueryServerDistanceRemainingDurLast() {
    return this.communicator.getStatQueryServerDistanceRemainingDurLast();
  }
  public long   getStatQueryServerDurationRemainingDurLast() {
    return this.communicator.getStatQueryServerDurationRemainingDurLast();
  }
  public long   getStatQueryServerLoadMaxDurLast() {
    return this.communicator.getStatQueryServerLoadMaxDurLast();
  }
  public long   getStatQueryServerRouteRemainingDurLast() {
    return this.communicator.getStatQueryServerRouteRemainingDurLast();
  }
  public long   getStatQueryServerScheduleRemainingDurLast() {
    return this.communicator.getStatQueryServerScheduleRemainingDurLast();
  }
  public long   getStatQueryServersLocationsActiveDurLast() {
    return this.communicator.getStatQueryServersLocationsActiveDurLast();
  }
  public long   getStatQueryUserDurLast() {
    return this.communicator.getStatQueryUserDurLast();
  }
  public long   getStatQueryVertexDurLast() {
    return this.communicator.getStatQueryVertexDurLast();
  }
}

