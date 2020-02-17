package com.github.jargors.jmx;
import com.github.jargors.core.Communicator;
public interface CommunicatorMonitorMBean {
  public long getStatQueryEdgeDur();
  public long getStatQueryServerDistanceRemainingDur();
  public long getStatQueryServerDurationRemainingDur();
  public long getStatQueryServerLoadMaxDur();
  public long getStatQueryServerRouteRemainingDur();
  public long getStatQueryServerScheduleRemainingDur();
  public long getStatQueryServersLocationsActiveDur();
  public long getStatQueryUserDur();
  public long getStatQueryVertexDur();
}
