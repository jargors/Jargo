package com.github.jargors.jmx;
import com.github.jargors.Communicator;
public interface CommunicatorMonitorMBean {
  public long   getStatQueryEdgeDurLast();
  public long   getStatQueryServerDistanceRemainingDurLast();
  public long   getStatQueryServerDurationRemainingDurLast();
  public long   getStatQueryServerLoadMaxDurLast();
  public long   getStatQueryServerRouteRemainingDurLast();
  public long   getStatQueryServerScheduleRemainingDurLast();
  public long   getStatQueryServersLocationsActiveDurLast();
  public long   getStatQueryUserDurLast();
  public long   getStatQueryVertexDurLast();
}
