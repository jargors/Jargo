package com.github.jargors.jmx;
import com.github.jargors.jmx.ControllerMonitorMBean;
import com.github.jargors.Controller;
public class ControllerMonitor implements ControllerMonitorMBean {
  private Controller controller = null;
  public ControllerMonitor(final Controller controller) {
    this.controller = controller;
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
}

