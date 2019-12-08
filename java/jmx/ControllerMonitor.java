package com.github.jargors.jmx;
import com.github.jargors.jmx.ControllerMonitorMBean;
import com.github.jargors.Controller;
public class ControllerMonitor implements ControllerMonitorMBean {
  private Controller controller = null;
  public ControllerMonitor(final Controller controller) {
    this.controller = controller;
  }
  public int    getStatControllerClock() {
    return this.controller.getStatControllerClock();
  }
  public int    getStatControllerClockReferenceDay() {
    return this.controller.getStatControllerClockReferenceDay();
  }
  public int    getStatControllerClockReferenceHour() {
    return this.controller.getStatControllerClockReferenceHour();
  }
  public int    getStatControllerClockReferenceMinute() {
    return this.controller.getStatControllerClockReferenceMinute();
  }
  public int    getStatControllerClockReferenceSecond() {
    return this.controller.getStatControllerClockReferenceSecond();
  }
  public int    getStatControllerRequestCollectionSize() {
    return this.controller.getStatControllerRequestCollectionSize();
  }
  public long   getStatControllerRequestCollectionDur() {
    return this.controller.getStatControllerRequestCollectionDur();
  }
  public long   getStatQueryEdgeDur() {
    return this.controller.getStatQueryEdgeDur();
  }
  public long   getStatQueryServersLocationsActiveDur() {
    return this.controller.getStatQueryServersLocationsActiveDur();
  }
  public long   getStatQueryUserDur() {
    return this.controller.getStatQueryUserDur();
  }
  public long   getStatQueryVertexDur() {
    return this.controller.getStatQueryVertexDur();
  }
}

