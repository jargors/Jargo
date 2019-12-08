package com.github.jargors.jmx;
import com.github.jargors.Controller;
public interface ControllerMonitorMBean {
  public int    getStatControllerClock();
  public int    getStatControllerClockReferenceDay();
  public int    getStatControllerClockReferenceHour();
  public int    getStatControllerClockReferenceMinute();
  public int    getStatControllerClockReferenceSecond();
  public int    getStatControllerRequestCollectionSize();
  public long   getStatControllerRequestCollectionDur();
  public long   getStatQueryEdgeDur();
  public long   getStatQueryServersLocationsActiveDur();
  public long   getStatQueryUserDur();
  public long   getStatQueryVertexDur();
}
