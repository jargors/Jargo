package com.github.jargors.jmx;
import com.github.jargors.Communicator;
public interface CommunicatorMonitorMBean {
  public int getStatQueryServerScheduleRemainingCount();
  public long getStatQueryServerScheduleRemainingDurLast();
  public long getStatQueryServerScheduleRemainingDurTotal();
  public long getStatQueryServerScheduleRemainingDurMin();
  public long getStatQueryServerScheduleRemainingDurMax();
  public double getStatQueryServerScheduleRemainingDurAvg();
}
