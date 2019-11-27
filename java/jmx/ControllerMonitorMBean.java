package com.github.jargors.jmx;
import com.github.jargors.Controller;
public interface ControllerMonitorMBean {
  public int    getStatQueryEdgeCount();
  public long   getStatQueryEdgeDurLast();
  public long   getStatQueryEdgeDurTotal();
  public long   getStatQueryEdgeDurMin();
  public long   getStatQueryEdgeDurMax();
  public double getStatQueryEdgeDurAvg();
}
