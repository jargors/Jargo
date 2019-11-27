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
  public int    getControllerRequestCollectionSizeMin() {
    return this.controller.getControllerRequestCollectionSizeMin();
  }
  public int    getControllerRequestCollectionSizeMax() {
    return this.controller.getControllerRequestCollectionSizeMax();
  }
  public double getControllerRequestCollectionSizeAvg() {
    return this.controller.getControllerRequestCollectionSizeAvg();
  }
  public long   getControllerRequestCollectionDurLast() {
    return this.controller.getControllerRequestCollectionDurLast();
  }
  public long   getControllerRequestCollectionDurMin() {
    return this.controller.getControllerRequestCollectionDurMin();
  }
  public long   getControllerRequestCollectionDurMax() {
    return this.controller.getControllerRequestCollectionDurMax();
  }
  public double getControllerRequestCollectionDurAvg() {
    return this.controller.getControllerRequestCollectionDurAvg();
  }
  public int    getStatQueryEdgeCount() {
    return this.controller.getStatQueryEdgeCount();
  }
  public long   getStatQueryEdgeDurLast() {
    return this.controller.getStatQueryEdgeDurLast();
  }
  public long   getStatQueryEdgeDurTotal() {
    return this.controller.getStatQueryEdgeDurTotal();
  }
  public long   getStatQueryEdgeDurMin() {
    return this.controller.getStatQueryEdgeDurMin();
  }
  public long   getStatQueryEdgeDurMax() {
    return this.controller.getStatQueryEdgeDurMax();
  }
  public double getStatQueryEdgeDurAvg() {
    return this.controller.getStatQueryEdgeDurAvg();
  }
  public int    getStatQueryUserCount() {
    return this.controller.getStatQueryUserCount();
  }
  public long   getStatQueryUserDurLast() {
    return this.controller.getStatQueryUserDurLast();
  }
  public long   getStatQueryUserDurTotal() {
    return this.controller.getStatQueryUserDurTotal();
  }
  public long   getStatQueryUserDurMin() {
    return this.controller.getStatQueryUserDurMin();
  }
  public long   getStatQueryUserDurMax() {
    return this.controller.getStatQueryUserDurMax();
  }
  public double getStatQueryUserDurAvg() {
    return this.controller.getStatQueryUserDurAvg();
  }
  public int    getStatQueryVertexCount() {
    return this.controller.getStatQueryVertexCount();
  }
  public long   getStatQueryVertexDurLast() {
    return this.controller.getStatQueryVertexDurLast();
  }
  public long   getStatQueryVertexDurTotal() {
    return this.controller.getStatQueryVertexDurTotal();
  }
  public long   getStatQueryVertexDurMin() {
    return this.controller.getStatQueryVertexDurMin();
  }
  public long   getStatQueryVertexDurMax() {
    return this.controller.getStatQueryVertexDurMax();
  }
  public double getStatQueryVertexDurAvg() {
    return this.controller.getStatQueryVertexDurAvg();
  }
}

