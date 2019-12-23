import com.github.jargors.Client;
import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.algo.GreedyInsertion;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class CacheT2 {
  public static final Controller controller = new Controller();
  public static Storage storage = null;
  public static Client client = null;
  @BeforeClass
  public static void setup() throws Exception {
    client = new GreedyInsertion();
    client.gtreeLoad("data/cd10DN.gtree");
    client.forwardRefCacheVertices(controller.retrieveRefCacheVertices());
    client.forwardRefCacheEdges(controller.retrieveRefCacheEdges());
    client.forwardRefCacheUsers(controller.retrieveRefCacheUsers());
    controller.setRefClient(client);
    controller.forwardRefCommunicator(controller.getRefCommunicator());
    controller.instanceNew();
    controller.instanceInitialize();
    controller.gtreeLoad("data/cd10DN.gtree");
    controller.loadRoadNetworkFromFile("data/cd11DY.rnet");
    controller.loadProblem("data/cd1m5kn8922.instance");
    storage = controller.getRefStorage();
  }
  @AfterClass
  public static void teardown() throws Exception {
    // ...
  }
  @Test
  public void T01() throws Exception {
    do {
      final int t = this.controller.getClock();
      System.out.printf("%d/%d\n", t, 120);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(false)[0],
          controller.queryMetricRequestDistanceBaseUnassignedTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDistanceBaseUnassignedTotal(false)[0],
          controller.queryMetricRequestDistanceBaseUnassignedTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDistanceDetourTotal(false)[0],
          controller.queryMetricRequestDistanceDetourTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDistanceTransitTotal(false)[0],
          controller.queryMetricRequestDistanceTransitTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDurationPickupTotal(false)[0],
          controller.queryMetricRequestDurationPickupTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDurationTransitTotal(false)[0],
          controller.queryMetricRequestDurationTransitTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricRequestDurationTravelTotal(false)[0],
          controller.queryMetricRequestDurationTravelTotal()[0]);
      // assertEquals("t="+t,
      //     storage.DBQueryMetricServerDistanceCruisingTotal(false)[0],
      //     controller.queryMetricServerDistanceCruisingTotal()[0]);
      // assertEquals("t="+t,
      //     storage.DBQueryMetricServerDistanceServiceTotal(false)[0],
      //     controller.queryMetricServerDistanceServiceTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricServerDistanceTotal(false)[0],
          controller.queryMetricServerDistanceTotal()[0]);
      // assertEquals("t="+t,
      //     storage.DBQueryMetricServerDurationCruisingTotal(false)[0],
      //     controller.queryMetricServerDurationCruisingTotal()[0]);
      // assertEquals("t="+t,
      //     storage.DBQueryMetricServerDurationServiceTotal(false)[0],
      //     controller.queryMetricServerDurationServiceTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricServerDurationTravelTotal(false)[0],
          controller.queryMetricServerDurationTravelTotal()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricServiceRate(false)[0],
          controller.queryMetricServiceRate()[0]);
      assertEquals("t="+t,
          storage.DBQueryMetricUserDistanceBaseTotal(false)[0],
          controller.queryMetricUserDistanceBaseTotal()[0]);
      this.controller.step();
    } while (this.controller.getClock() < 120);
  }
}
