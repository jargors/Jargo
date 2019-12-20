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
public class SchedulesTest1 {
  public final Controller controller = new Controller();
  public final int   ok_sid = 1;
  public final int[] EMPTY     = new int[] { };
  public final int[] ok_route1 = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 41,4, 49,3, 50,0 };
  public final int[] ok_route2 = new int[] { 1,22, 10,1, 20,2, 30,4, 40,32, 50,4, 60,3, 70,29, 80,30, 81,0 };
  public final int[] ok_sched1 = new int[] { 50,0,1 };
  public final int[] ok_sched2 = new int[] { 9,1,11, 33,32,11, 50,0,1 };
  public final int[] ok_sched3 = new int[] { 9,1,11, 33,32,11, 33,32,12, 49,3,12, 50,0,1 };
  public final int[] ok_sched4 = new int[] { 10,1,11, 30,4,10, 40,32,11, 80,30,10, 81,0,1 };
  public Storage storage = null;
  @Before
  public void start() throws Exception {
    this.controller.instanceNew();
    this.controller.instanceInitialize();
    this.controller.gtreeLoad("data/cd0n4m2.gtree");
    this.controller.loadRoadNetworkFromFile("data/cd0n4m2.rnet");
    this.controller.loadProblem("data/cd0n4m2.instance");
    this.storage = controller.getRefStorage();
  }
  @After
  public void end() throws Exception {
    this.controller.instanceClose();
  }
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Test
  public void T01a() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) is not a subsequence of ok_route1 (param 2)
     * (waypoint (9,2) does not exist). Throws F46 violation because cannot
     * find the waypoint (9,2) for R11 in PD. */
    final int[] ko_sched = new int[] { 9,2,11, 33,32,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F46'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T01b() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) is not a subsequence of ok_route1 (param 2)
     * (waypoint (10,1) does not exist). Throws F23 violation because cannot
     * find the waypoint (10,1) in W. */
    final int[] ko_sched = new int[] { 10,1,11, 33,32,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F23'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T02() throws Exception {
    /* Neg: ko_sched (param 3) contains multiple server labels. */
    final int[] ko_sched = new int[] { 49,3,1, 50,0,1 };
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T03() throws Exception {
    /* Neg: ko_sched (param 3) server label not at server dest. */
    final int[] ko_sched = new int[] { 49,3,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T04() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) a request label appears more than two times. */
    final int[] ko_sched = new int[] { 9,1,11, 33,32,11, 41,4,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F46'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T06a() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) a waypoint is duplicated. */
    final int[] ko_sched = new int[] { 9,1,11, 9,1,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C105'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T06b() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) a waypoint is duplicated. */
    final int[] ko_sched = new int[] { 33,32,11, 33,32,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C105'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T07() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) a request label is missing destination. */
    final int[] ko_sched = new int[] { 9,1,11, 50,0,1 };
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T08() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) request label waypoints not in remaining sched. */
    final int[] ko_route = new int[] { 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 9,1,11, 33,32,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.storage.DBUpdateServerService(ok_sid, ko_route, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T09() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) request label waypoint not in remaining sched. */
    final int[] ko_route = new int[] { 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 33,32,11, 50,0,1 };
    this.thrown.expect(SQLException.class);
    this.storage.DBUpdateServerService(ok_sid, ko_route, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void T10() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched2, new int[] { 11 }, EMPTY);
    /* Neg: ko_sched (param 3) request label origin missing from ko_sched and
     * the origin is not traveled yet. */
    final int[] ko_sched = new int[] { 33,32,11, 50,0,1 };
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ko_sched, EMPTY, EMPTY);
  }
  @Test
  public void P01() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route1, ok_sched3, new int[] { 11, 12 }, EMPTY);
    assertArrayEquals("P01 pre-condition failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 33,32,0,12, 49,3,0,12, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Pos: change schedule to pick up 12 first */
    final int[] ko_route = new int[] {
        1,22, 10,1, 20,2, 30,4, 40,32, 50,4, 60,3, 70,4, 80,2, 90,1, 100,2, 110,4, 120,32, 121,0 };
    final int[] ko_sched = new int[] { 40,32,12, 60,3,12, 90,1,11, 120,32,11, 121,0,1 };
    this.storage.DBUpdateServerService(ok_sid, ko_route, ko_sched, EMPTY, EMPTY);
    assertArrayEquals("P01 post-condition failed",
        new int[] { 1,22,1,0, 40,32,0,12, 60,3,0,12, 90,1,0,11, 120,32,0,11, 121,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void P02() throws Exception {
    this.storage.DBUpdateServerService(ok_sid, ok_route2, ok_sched4, new int[] { 11, 10 }, EMPTY);
    assertArrayEquals("P02 pre-condition failed",
        new int[] { 1,22,1,0, 10,1,0,11, 30,4,0,10, 40,32,0,11, 80,30,0,10, 81,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    /* Pos: change schedule to drop off 10 first */
    final int[] ko_route = new int[] {
        30,4, 40,3, 50,29, 60,30, 70,29, 80,3, 90,4, 100,32, 101,0 };
    final int[] ko_sched = new int[] { 60,30,10, 100,32,11, 101,0,1 };
    this.storage.DBUpdateServerService(ok_sid, ko_route, ko_sched, EMPTY, EMPTY);
    assertArrayEquals("P02 post-condition failed",
        new int[] { 1,22,1,0, 10,1,0,11, 30,4,0,10, 60,30,0,10, 100,32,0,11, 101,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
}
