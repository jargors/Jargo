package com.github.jargors.jmx;
import com.github.jargors.Controller;
public interface ControllerMonitorMBean {

  public int    getControllerClockNow();
  public int    getControllerClockReferenceDay();
  public int    getControllerClockReferenceHour();
  public int    getControllerClockReferenceMinute();
  public int    getControllerClockReferenceSecond();
  public int    getControllerRequestCollectionSizeLast();
  public int    getControllerRequestCollectionSizeMin();
  public int    getControllerRequestCollectionSizeMax();
  public double getControllerRequestCollectionSizeAvg();
  public long   getControllerRequestCollectionDurLast();
  public long   getControllerRequestCollectionDurMin();
  public long   getControllerRequestCollectionDurMax();
  public double getControllerRequestCollectionDurAvg();

  public int    getStatQueryEdgeCount();
  public long   getStatQueryEdgeDurLast();
  public long   getStatQueryEdgeDurTotal();
  public long   getStatQueryEdgeDurMin();
  public long   getStatQueryEdgeDurMax();
  public double getStatQueryEdgeDurAvg();
  public int    getStatQueryUserCount();
  public long   getStatQueryUserDurLast();
  public long   getStatQueryUserDurTotal();
  public long   getStatQueryUserDurMin();
  public long   getStatQueryUserDurMax();
  public double getStatQueryUserDurAvg();
  public int    getStatQueryVertexCount();
  public long   getStatQueryVertexDurLast();
  public long   getStatQueryVertexDurTotal();
  public long   getStatQueryVertexDurMin();
  public long   getStatQueryVertexDurMax();
  public double getStatQueryVertexDurAvg();
}
