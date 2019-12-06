package com.github.jargors.jmx;
import com.github.jargors.jmx.ControllerMonitorMBean;
import com.github.jargors.Controller;
public class ControllerMonitor implements ControllerMonitorMBean {
  private Controller controller = null;
  public ControllerMonitor(final Controller controller) {
    this.controller = controller;
  }
  public int    getControllerClockNow() {
    return this.controller.getControllerClockNow();
  }
  public int    getControllerClockReferenceDay() {
    return this.controller.getControllerClockReferenceDay();
  }
  public int    getControllerClockReferenceHour() {
    return this.controller.getControllerClockReferenceHour();
  }
  public int    getControllerClockReferenceMinute() {
    return this.controller.getControllerClockReferenceMinute();
  }
  public int    getControllerClockReferenceSecond() {
    return this.controller.getControllerClockReferenceSecond();
  }
  public int    getControllerRequestCollectionSizeLast() {
    return this.controller.getControllerRequestCollectionSizeLast();
  }
  public long   getControllerRequestCollectionDurLast() {
    return this.controller.getControllerRequestCollectionDurLast();
  }
  public long   getStatQueryEdgeDurLast() {
    return this.controller.getStatQueryEdgeDurLast();
  }
  public long   getStatQueryServersLocationsActiveDurLast() {
    return this.controller.getStatQueryServersLocationsActiveDurLast();
  }
  public long   getStatQueryUserDurLast() {
    return this.controller.getStatQueryUserDurLast();
  }
  public long   getStatQueryVertexDurLast() {
    return this.controller.getStatQueryVertexDurLast();
  }
}

