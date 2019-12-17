package com.github.jargors.jmx;
import com.github.jargors.jmx.ControllerMonitorMBean;
import com.github.jargors.Controller;
public class ControllerMonitor implements ControllerMonitorMBean {
  private Controller controller = null;
  public ControllerMonitor(final Controller controller) {
    this.controller = controller;
  }
  public int getStatControllerClock() {
    return this.controller.getStatControllerClock();
  }
  public int getStatControllerClockReferenceDay() {
    return this.controller.getStatControllerClockReferenceDay();
  }
  public int getStatControllerClockReferenceHour() {
    return this.controller.getStatControllerClockReferenceHour();
  }
  public int getStatControllerClockReferenceMinute() {
    return this.controller.getStatControllerClockReferenceMinute();
  }
  public int getStatControllerClockReferenceSecond() {
    return this.controller.getStatControllerClockReferenceSecond();
  }
  public int getStatControllerRequestCollectionSize() {
    return this.controller.getStatControllerRequestCollectionSize();
  }
  public int getStatControllerRequestCollectionDropped() {
    return this.controller.getStatControllerRequestCollectionDropped();
  }
  public long getStatControllerRequestCollectionDur() {
    return this.controller.getStatControllerRequestCollectionDur();
  }
  public long getStatQueryDur() {
    return this.controller.getStatQueryDur();
  }
  public long getStatQueryEdgeDur() {
    return this.controller.getStatQueryEdgeDur();
  }
  public long getStatQueryEdgeStatisticsDur() {
    return this.controller.getStatQueryEdgeStatisticsDur();
  }
  public long getStatQueryEdgesCountDur() {
    return this.controller.getStatQueryEdgesCountDur();
  }
  public long getStatQueryEdgesDur() {
    return this.controller.getStatQueryEdgesDur();
  }
  public long getStatQueryMBRDur() {
    return this.controller.getStatQueryMBRDur();
  }
  public long getStatQueryMetricRequestDistanceBaseTotalDur() {
    return this.controller.getStatQueryMetricRequestDistanceBaseTotalDur();
  }
  public long getStatQueryMetricRequestDistanceBaseUnassignedTotalDur() {
    return this.controller.getStatQueryMetricRequestDistanceBaseUnassignedTotalDur();
  }
  public long getStatQueryMetricRequestDistanceDetourTotalDur() {
    return this.controller.getStatQueryMetricRequestDistanceDetourTotalDur();
  }
  public long getStatQueryMetricRequestDistanceTransitTotalDur() {
    return this.controller.getStatQueryMetricRequestDistanceTransitTotalDur();
  }
  public long getStatQueryMetricRequestDurationPickupTotalDur() {
    return this.controller.getStatQueryMetricRequestDurationPickupTotalDur();
  }
  public long getStatQueryMetricRequestDurationTransitTotalDur() {
    return this.controller.getStatQueryMetricRequestDurationTransitTotalDur();
  }
  public long getStatQueryMetricRequestDurationTravelTotalDur() {
    return this.controller.getStatQueryMetricRequestDurationTravelTotalDur();
  }
  public long getStatQueryMetricRequestTWViolationsTotalDur() {
    return this.controller.getStatQueryMetricRequestTWViolationsTotalDur();
  }
  public long getStatQueryMetricServerDistanceBaseTotalDur() {
    return this.controller.getStatQueryMetricServerDistanceBaseTotalDur();
  }
  public long getStatQueryMetricServerDistanceCruisingTotalDur() {
    return this.controller.getStatQueryMetricServerDistanceCruisingTotalDur();
  }
  public long getStatQueryMetricServerDistanceServiceTotalDur() {
    return this.controller.getStatQueryMetricServerDistanceServiceTotalDur();
  }
  public long getStatQueryMetricServerDistanceTotalDur() {
    return this.controller.getStatQueryMetricServerDistanceTotalDur();
  }
  public long getStatQueryMetricServerDurationCruisingTotalDur() {
    return this.controller.getStatQueryMetricServerDurationCruisingTotalDur();
  }
  public long getStatQueryMetricServerDurationServiceTotalDur() {
    return this.controller.getStatQueryMetricServerDurationServiceTotalDur();
  }
  public long getStatQueryMetricServerDurationTravelTotalDur() {
    return this.controller.getStatQueryMetricServerDurationTravelTotalDur();
  }
  public long getStatQueryMetricServerTWViolationsTotalDur() {
    return this.controller.getStatQueryMetricServerTWViolationsTotalDur();
  }
  public long getStatQueryMetricServiceRateDur() {
    return this.controller.getStatQueryMetricServiceRateDur();
  }
  public long getStatQueryMetricUserDistanceBaseTotalDur() {
    return this.controller.getStatQueryMetricUserDistanceBaseTotalDur();
  }
  public long getStatQueryRequestTimeOfArrivalDur() {
    return this.controller.getStatQueryRequestTimeOfArrivalDur();
  }
  public long getStatQueryRequestTimeOfDepartureDur() {
    return this.controller.getStatQueryRequestTimeOfDepartureDur();
  }
  public long getStatQueryRequestsCountDur() {
    return this.controller.getStatQueryRequestsCountDur();
  }
  public long getStatQueryRequestsCountActiveDur() {
    return this.controller.getStatQueryRequestsCountActiveDur();
  }
  public long getStatQueryRequestsQueuedDur() {
    return this.controller.getStatQueryRequestsQueuedDur();
  }
  public long getStatQueryServerRouteActiveDur() {
    return this.controller.getStatQueryServerRouteActiveDur();
  }
  public long getStatQueryServerRouteDur() {
    return this.controller.getStatQueryServerRouteDur();
  }
  public long getStatQueryServerRouteRemainingDur() {
    return this.controller.getStatQueryServerRouteRemainingDur();
  }
  public long getStatQueryServerScheduleDur() {
    return this.controller.getStatQueryServerScheduleDur();
  }
  public long getStatQueryServerTimeOfDepartureDur() {
    return this.controller.getStatQueryServerTimeOfDepartureDur();
  }
  public long getStatQueryServersActiveDur() {
    return this.controller.getStatQueryServersActiveDur();
  }
  public long getStatQueryServersCountDur() {
    return this.controller.getStatQueryServersCountDur();
  }
  public long getStatQueryServersLocationsActiveDur() {
    return this.controller.getStatQueryServersLocationsActiveDur();
  }
  public long getStatQueryUserDur() {
    return this.controller.getStatQueryUserDur();
  }
  public long getStatQueryVertexDur() {
    return this.controller.getStatQueryVertexDur();
  }
  public long getStatQueryVerticesCountDur() {
    return this.controller.getStatQueryVerticesCountDur();
  }
  public long getStatQueryVerticesDur() {
    return this.controller.getStatQueryVerticesDur();
  }
}

