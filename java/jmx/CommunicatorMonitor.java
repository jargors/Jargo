package com.github.jargors.jmx;
import com.github.jargors.jmx.CommunicatorMonitorMBean;
import com.github.jargors.Communicator;
public class CommunicatorMonitor implements CommunicatorMonitorMBean {
  private Communicator communicator = null;
  public CommunicatorMonitor(final Communicator communicator) {
    this.communicator = communicator;
  }
  public int    getStatQueryEdgeCount() {
    return this.communicator.getStatQueryEdgeCount();
  }
  public long   getStatQueryEdgeDurLast() {
    return this.communicator.getStatQueryEdgeDurLast();
  }
  public long   getStatQueryEdgeDurTotal() {
    return this.communicator.getStatQueryEdgeDurTotal();
  }
  public long   getStatQueryEdgeDurMin() {
    return this.communicator.getStatQueryEdgeDurMin();
  }
  public long   getStatQueryEdgeDurMax() {
    return this.communicator.getStatQueryEdgeDurMax();
  }
  public double getStatQueryEdgeDurAvg() {
    return this.communicator.getStatQueryEdgeDurAvg();
  }
  public int    getStatQueryServerDistanceRemainingCount() {
    return this.communicator.getStatQueryServerDistanceRemainingCount();
  }
  public long   getStatQueryServerDistanceRemainingDurLast() {
    return this.communicator.getStatQueryServerDistanceRemainingDurLast();
  }
  public long   getStatQueryServerDistanceRemainingDurTotal() {
    return this.communicator.getStatQueryServerDistanceRemainingDurTotal();
  }
  public long   getStatQueryServerDistanceRemainingDurMin() {
    return this.communicator.getStatQueryServerDistanceRemainingDurMin();
  }
  public long   getStatQueryServerDistanceRemainingDurMax() {
    return this.communicator.getStatQueryServerDistanceRemainingDurMax();
  }
  public double getStatQueryServerDistanceRemainingDurAvg() {
    return this.communicator.getStatQueryServerDistanceRemainingDurAvg();
  }
  public int    getStatQueryServerDurationRemainingCount() {
    return this.communicator.getStatQueryServerDurationRemainingCount();
  }
  public long   getStatQueryServerDurationRemainingDurLast() {
    return this.communicator.getStatQueryServerDurationRemainingDurLast();
  }
  public long   getStatQueryServerDurationRemainingDurTotal() {
    return this.communicator.getStatQueryServerDurationRemainingDurTotal();
  }
  public long   getStatQueryServerDurationRemainingDurMin() {
    return this.communicator.getStatQueryServerDurationRemainingDurMin();
  }
  public long   getStatQueryServerDurationRemainingDurMax() {
    return this.communicator.getStatQueryServerDurationRemainingDurMax();
  }
  public double getStatQueryServerDurationRemainingDurAvg() {
    return this.communicator.getStatQueryServerDurationRemainingDurAvg();
  }
  public int    getStatQueryServerLoadMaxCount() {
    return this.communicator.getStatQueryServerLoadMaxCount();
  }
  public long   getStatQueryServerLoadMaxDurLast() {
    return this.communicator.getStatQueryServerLoadMaxDurLast();
  }
  public long   getStatQueryServerLoadMaxDurTotal() {
    return this.communicator.getStatQueryServerLoadMaxDurTotal();
  }
  public long   getStatQueryServerLoadMaxDurMin() {
    return this.communicator.getStatQueryServerLoadMaxDurMin();
  }
  public long   getStatQueryServerLoadMaxDurMax() {
    return this.communicator.getStatQueryServerLoadMaxDurMax();
  }
  public double getStatQueryServerLoadMaxDurAvg() {
    return this.communicator.getStatQueryServerLoadMaxDurAvg();
  }
  public int    getStatQueryServerRouteRemainingCount() {
    return this.communicator.getStatQueryServerRouteRemainingCount();
  }
  public long   getStatQueryServerRouteRemainingDurLast() {
    return this.communicator.getStatQueryServerRouteRemainingDurLast();
  }
  public long   getStatQueryServerRouteRemainingDurTotal() {
    return this.communicator.getStatQueryServerRouteRemainingDurTotal();
  }
  public long   getStatQueryServerRouteRemainingDurMin() {
    return this.communicator.getStatQueryServerRouteRemainingDurMin();
  }
  public long   getStatQueryServerRouteRemainingDurMax() {
    return this.communicator.getStatQueryServerRouteRemainingDurMax();
  }
  public double getStatQueryServerRouteRemainingDurAvg() {
    return this.communicator.getStatQueryServerRouteRemainingDurAvg();
  }
  public int    getStatQueryServerScheduleRemainingCount() {
    return this.communicator.getStatQueryServerScheduleRemainingCount();
  }
  public long   getStatQueryServerScheduleRemainingDurLast() {
    return this.communicator.getStatQueryServerScheduleRemainingDurLast();
  }
  public long   getStatQueryServerScheduleRemainingDurTotal() {
    return this.communicator.getStatQueryServerScheduleRemainingDurTotal();
  }
  public long   getStatQueryServerScheduleRemainingDurMin() {
    return this.communicator.getStatQueryServerScheduleRemainingDurMin();
  }
  public long   getStatQueryServerScheduleRemainingDurMax() {
    return this.communicator.getStatQueryServerScheduleRemainingDurMax();
  }
  public double getStatQueryServerScheduleRemainingDurAvg() {
    return this.communicator.getStatQueryServerScheduleRemainingDurAvg();
  }
  public int    getStatQueryServersLocationsActiveCount() {
    return this.communicator.getStatQueryServersLocationsActiveCount();
  }
  public long   getStatQueryServersLocationsActiveDurLast() {
    return this.communicator.getStatQueryServersLocationsActiveDurLast();
  }
  public long   getStatQueryServersLocationsActiveDurTotal() {
    return this.communicator.getStatQueryServersLocationsActiveDurTotal();
  }
  public long   getStatQueryServersLocationsActiveDurMin() {
    return this.communicator.getStatQueryServersLocationsActiveDurMin();
  }
  public long   getStatQueryServersLocationsActiveDurMax() {
    return this.communicator.getStatQueryServersLocationsActiveDurMax();
  }
  public double getStatQueryServersLocationsActiveDurAvg() {
    return this.communicator.getStatQueryServersLocationsActiveDurAvg();
  }
  public int    getStatQueryUserCount() {
    return this.communicator.getStatQueryUserCount();
  }
  public long   getStatQueryUserDurLast() {
    return this.communicator.getStatQueryUserDurLast();
  }
  public long   getStatQueryUserDurTotal() {
    return this.communicator.getStatQueryUserDurTotal();
  }
  public long   getStatQueryUserDurMin() {
    return this.communicator.getStatQueryUserDurMin();
  }
  public long   getStatQueryUserDurMax() {
    return this.communicator.getStatQueryUserDurMax();
  }
  public double getStatQueryUserDurAvg() {
    return this.communicator.getStatQueryUserDurAvg();
  }
  public int    getStatQueryVertexCount() {
    return this.communicator.getStatQueryVertexCount();
  }
  public long   getStatQueryVertexDurLast() {
    return this.communicator.getStatQueryVertexDurLast();
  }
  public long   getStatQueryVertexDurTotal() {
    return this.communicator.getStatQueryVertexDurTotal();
  }
  public long   getStatQueryVertexDurMin() {
    return this.communicator.getStatQueryVertexDurMin();
  }
  public long   getStatQueryVertexDurMax() {
    return this.communicator.getStatQueryVertexDurMax();
  }
  public double getStatQueryVertexDurAvg() {
    return this.communicator.getStatQueryVertexDurAvg();
  }
}

