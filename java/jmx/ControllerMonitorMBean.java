package com.github.jargors.jmx;
import com.github.jargors.Controller;
public interface ControllerMonitorMBean {
  public int    getControllerClockNow();
  public int    getControllerClockReferenceDay();
  public int    getControllerClockReferenceHour();
  public int    getControllerClockReferenceMinute();
  public int    getControllerClockReferenceSecond();
  public int    getControllerRequestCollectionSizeLast();
  public long   getControllerRequestCollectionDurLast();
  public long   getStatQueryEdgeDurLast();
  public long   getStatQueryServersLocationsActiveDurLast();
  public long   getStatQueryUserDurLast();
  public long   getStatQueryVertexDurLast();
}
