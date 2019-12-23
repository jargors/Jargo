import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertArrayEquals;
public class DBUpdateServerServiceT3 {
  public final Controller controller = new Controller();
  public final int   ok_sid = 1;
  public final int[] EMPTY     = new int[] { };
  public final int[] ok_route  = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 41,4, 49,3, 50,0 };
  public final int[] ok_sched1 = new int[] { 50,0,1 };
  public final int[] ok_sched2 = new int[] { 9,1,11, 33,32,11, 50,0,1 };
  public final int[] ok_sched3 = new int[] { 9,1,11, 33,32,11, 33,32,12, 49,3,12, 50,0,1 };
  public Storage storage = null;
  @Before
  public void start() throws Exception {
    this.controller.instanceNew();
    this.controller.instanceInitialize();
    this.controller.gtreeLoad("data/cd00DN.gtree");
    this.controller.loadRoadNetworkFromFile("data/cd01DY.rnet");
    this.controller.loadProblem("data/cd0m2n4.instance");
    this.storage = controller.getRefStorage();
    assertArrayEquals("pre-condition failure",
        new int[] { 1,22,1,0, 2,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @After
  public void end() throws Exception {
    this.controller.instanceClose();
  }
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Test
  public void P01() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, new int[] { 11 }, EMPTY);
    assertArrayEquals("P01 pre-condition failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Pos: update server 1's schedule to remove request 11 */
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched1, EMPTY, new int[] { 11 });
    assertArrayEquals("P01 post-condition failure",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void P04() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, new int[] { 11 }, EMPTY);
    assertArrayEquals("P04 pre-condition failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Neg: ko_sched still contains waypoints labeled with request 11 */
    int[] ko_sched = new int[] { 9,1,11, 33,32,11, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'F47'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, EMPTY, new int[] { 11 });
  }
  @Test
  public void P05a() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, new int[] { 11 }, EMPTY);
    assertArrayEquals("P05a pre-condition failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Neg: request 11 does not exist in server 1's remaining schedule. The first
      * elem of ko_route, 41, is taken to be the "current" time. Request 11 is
      * picked up and dropped off before this time, so it cannot be removed from
      * server 1's schedule as it has already been serviced. */
    int[] ko_route = new int[] { 41,4, 49,3, 50,0};
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'F47'"));
    this.storage.DBUpdateServerService(ok_sid, ko_route, ok_sched1, EMPTY, new int[] { 11 });
  }
  @Test
  public void P05b() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, new int[] { 11 }, EMPTY);
    assertArrayEquals("P05b pre-condition failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Neg: request 12 never exists in server 1's schedule. Nothing happens, as
     * the DELETE statement modifies 0 rows. Maybe we should warn the user? */
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, EMPTY, new int[] { 12 });
  }
}
