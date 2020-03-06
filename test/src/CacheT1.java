import com.github.jargors.sim.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class CacheT1 {
  public static final Controller controller = new Controller();
  public static Storage storage = null;
  @BeforeClass
  public static void setup() throws Exception {
    controller.instanceLoad("data/solution");
    controller.cacheRoadNetworkFromDB();
    controller.cacheUsersFromDB();
    storage = controller.getRefStorage();
  }
  @AfterClass
  public static void teardown() throws Exception {
      // ...
  }
  @Test
  public void T01() throws Exception {
    assertEquals("T01 query failure",
        storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(false)[0],
        controller.queryMetricRequestDistanceBaseUnassignedTotal(false)[0]);
    assertEquals("T01 cache failure",
        storage.DBQueryMetricRequestDistanceBaseUnassignedTotal()[0],
        controller.queryMetricRequestDistanceBaseUnassignedTotal()[0]);
  }
  @Test
  public void T02() throws Exception {
  assertEquals("T02 query failure",
      storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(false)[0],
      controller.queryMetricRequestDistanceBaseUnassignedTotal(false)[0]);
  assertEquals("T02 cache failure",
      storage.DBQueryMetricRequestDistanceBaseUnassignedTotal()[0],
      controller.queryMetricRequestDistanceBaseUnassignedTotal()[0]);
  }
  @Test
  public void T03() throws Exception {
  assertEquals("T03 query failure",
      storage.DBQueryMetricRequestDistanceDetourTotal(false)[0],
      controller.queryMetricRequestDistanceDetourTotal(false)[0]);
  assertEquals("T03 cache failure",
      storage.DBQueryMetricRequestDistanceDetourTotal()[0],
      controller.queryMetricRequestDistanceDetourTotal()[0]);
  }
  @Test
  public void T04() throws Exception {
  assertEquals("T04 query failure",
      storage.DBQueryMetricRequestDistanceTransitTotal(false)[0],
      controller.queryMetricRequestDistanceTransitTotal(false)[0]);
  assertEquals("T04 cache failure",
      storage.DBQueryMetricRequestDistanceTransitTotal()[0],
      controller.queryMetricRequestDistanceTransitTotal()[0]);
  }
  @Test
  public void T05() throws Exception {
  assertEquals("T05 query failure",
      storage.DBQueryMetricRequestDurationPickupTotal(false)[0],
      controller.queryMetricRequestDurationPickupTotal(false)[0]);
  assertEquals("T05 cache failure",
      storage.DBQueryMetricRequestDurationPickupTotal()[0],
      controller.queryMetricRequestDurationPickupTotal()[0]);
  }
  @Test
  public void T06() throws Exception {
  assertEquals("T06 query failure",
      storage.DBQueryMetricRequestDurationTransitTotal(false)[0],
      controller.queryMetricRequestDurationTransitTotal(false)[0]);
  assertEquals("T06 cache failure",
      storage.DBQueryMetricRequestDurationTransitTotal()[0],
      controller.queryMetricRequestDurationTransitTotal()[0]);
  }
  @Test
  public void T07() throws Exception {
  assertEquals("T07 query failure",
      storage.DBQueryMetricRequestDurationTravelTotal(false)[0],
      controller.queryMetricRequestDurationTravelTotal(false)[0]);
  assertEquals("T07 cache failure",
      storage.DBQueryMetricRequestDurationTravelTotal()[0],
      controller.queryMetricRequestDurationTravelTotal()[0]);
  }
  // @Test
  // public void T08() throws Exception {
  // assertEquals("T08 query failure",
  //     storage.DBQueryMetricServerDistanceCruisingTotal(false)[0],
  //     controller.queryMetricServerDistanceCruisingTotal(false)[0]);
  // assertEquals("T08 cache failure",
  //     storage.DBQueryMetricServerDistanceCruisingTotal()[0],
  //     controller.queryMetricServerDistanceCruisingTotal()[0]);
  // }
  // @Test
  // public void T09() throws Exception {
  // assertEquals("T09 query failure",
  //     storage.DBQueryMetricServerDistanceServiceTotal(false)[0],
  //     controller.queryMetricServerDistanceServiceTotal(false)[0]);
  // assertEquals("T09 cache failure",
  //     storage.DBQueryMetricServerDistanceServiceTotal()[0],
  //     controller.queryMetricServerDistanceServiceTotal()[0]);
  // }
  @Test
  public void T10() throws Exception {
  assertEquals("T10 query failure",
      storage.DBQueryMetricServerDistanceTotal(false)[0],
      controller.queryMetricServerDistanceTotal(false)[0]);
  assertEquals("T10 cache failure",
      storage.DBQueryMetricServerDistanceTotal()[0],
      controller.queryMetricServerDistanceTotal()[0]);
  }
  // @Test
  // public void T11() throws Exception {
  // assertEquals("T11 query failure",
  //     storage.DBQueryMetricServerDurationCruisingTotal(false)[0],
  //     controller.queryMetricServerDurationCruisingTotal(false)[0]);
  // assertEquals("T11 cache failure",
  //     storage.DBQueryMetricServerDurationCruisingTotal()[0],
  //     controller.queryMetricServerDurationCruisingTotal()[0]);
  // }
  // @Test
  // public void T12() throws Exception {
  // assertEquals("T12 query failure",
  //     storage.DBQueryMetricServerDurationServiceTotal(false)[0],
  //     controller.queryMetricServerDurationServiceTotal(false)[0]);
  // assertEquals("T12 cache failure",
  //     storage.DBQueryMetricServerDurationServiceTotal()[0],
  //     controller.queryMetricServerDurationServiceTotal()[0]);
  // }
  @Test
  public void T13() throws Exception {
  assertEquals("T13 query failure",
      storage.DBQueryMetricServerDurationTravelTotal(false)[0],
      controller.queryMetricServerDurationTravelTotal(false)[0]);
  assertEquals("T13 cache failure",
      storage.DBQueryMetricServerDurationTravelTotal()[0],
      controller.queryMetricServerDurationTravelTotal()[0]);
  }
  @Test
  public void T14() throws Exception {
  assertEquals("T14 query failure",
      storage.DBQueryMetricServiceRate(false)[0],
      controller.queryMetricServiceRate(false)[0]);
  assertEquals("T14 cache failure",
      storage.DBQueryMetricServiceRate()[0],
      controller.queryMetricServiceRate()[0]);
  }
  @Test
  public void T15() throws Exception {
  assertEquals("T15 query failure",
      storage.DBQueryMetricUserDistanceBaseTotal(false)[0],
      controller.queryMetricUserDistanceBaseTotal(false)[0]);
  assertEquals("T15 cache failure",
      storage.DBQueryMetricUserDistanceBaseTotal()[0],
      controller.queryMetricUserDistanceBaseTotal()[0]);
  }
}
