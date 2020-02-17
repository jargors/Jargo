package com.github.jargors.jmx;
import com.github.jargors.core.Communicator;
import com.github.jargors.jmx.CommunicatorMonitorMBean;
public class CommunicatorMonitor implements CommunicatorMonitorMBean {
  private Communicator communicator = null;
  public CommunicatorMonitor(final Communicator communicator) {
    this.communicator = communicator;
  }
  public long getStatQueryEdgeDur() {
    return this.communicator.getStatQueryEdgeDur();
  }
  public long getStatQueryServerDistanceRemainingDur() {
    return this.communicator.getStatQueryServerDistanceRemainingDur();
  }
  public long getStatQueryServerDurationRemainingDur() {
    return this.communicator.getStatQueryServerDurationRemainingDur();
  }
  public long getStatQueryServerLoadMaxDur() {
    return this.communicator.getStatQueryServerLoadMaxDur();
  }
  public long getStatQueryServerRouteRemainingDur() {
    return this.communicator.getStatQueryServerRouteRemainingDur();
  }
  public long getStatQueryServerScheduleRemainingDur() {
    return this.communicator.getStatQueryServerScheduleRemainingDur();
  }
  public long getStatQueryServersLocationsActiveDur() {
    return this.communicator.getStatQueryServersLocationsActiveDur();
  }
  public long getStatQueryUserDur() {
    return this.communicator.getStatQueryUserDur();
  }
  public long getStatQueryVertexDur() {
    return this.communicator.getStatQueryVertexDur();
  }
}
