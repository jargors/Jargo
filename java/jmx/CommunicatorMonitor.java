package com.github.jargors.jmx;
import com.github.jargors.jmx.CommunicatorMonitorMBean;
import com.github.jargors.Communicator;
public class CommunicatorMonitor implements CommunicatorMonitorMBean {
  private Communicator communicator = null;
  public CommunicatorMonitor(final Communicator communicator) {
    this.communicator = communicator;
  }
  public int getStatQueryServerScheduleRemainingCount() {
    return this.communicator.getStatQueryServerScheduleRemainingCount();
  }
  public long getStatQueryServerScheduleRemainingDurLast() {
    return this.communicator.getStatQueryServerScheduleRemainingDurLast();
  }
  public long getStatQueryServerScheduleRemainingDurTotal() {
    return this.communicator.getStatQueryServerScheduleRemainingDurTotal();
  }
  public long getStatQueryServerScheduleRemainingDurMin() {
    return this.communicator.getStatQueryServerScheduleRemainingDurMin();
  }
  public long getStatQueryServerScheduleRemainingDurMax() {
    return this.communicator.getStatQueryServerScheduleRemainingDurMax();
  }
  public double getStatQueryServerScheduleRemainingDurAvg() {
    return this.communicator.getStatQueryServerScheduleRemainingDurAvg();
  }
}

