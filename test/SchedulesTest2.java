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
public class SchedulesTest2 {
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
    this.controller.gtreeLoad("data/cd0n4m2.gtree");
    this.controller.loadRoadNetworkFromFile("data/cd0n4m2.rnet");
    this.controller.loadProblem("data/cd0n4m2.instance");
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
  public void P01a() throws Exception {
    /* Pos: update server 1's route/schedule to service request 11 */
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched2, new int[] { 11 }, EMPTY);
    assertArrayEquals("P01a failure",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void P01b() throws Exception {
    /* Pos: update server 1's route/schedule to service requests 11 and 12 */
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched3, new int[] { 11, 12 }, EMPTY);
    assertArrayEquals("P01b failure",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 33,32,0,12, 49,3,0,12, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04a() throws Exception {
    /* Neg: request 11 is listed in the positive set but is not found in
     * ok_sched1. */
    thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route, ok_sched1, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T04b() throws Exception {
    /* Neg: request 11 is listed in the positive set but only the origin is
     * labeled in ko_sched. */
    int[] ko_sched = new int[] { 9,1,11, 50,0,1 };
    thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T04c() throws Exception {
    /* Neg: request 11 is listed in the positive set but only the destination is
     * labeled in ko_sched. */
    int[] ko_sched = new int[] { 33,32,11, 50,0,1 };
    thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T04d() throws Exception {
    /* Neg: request 11 is listed in the positive set with three waypoints
     * labeled in ko_sched when only two are expected. */
    int[] ko_sched = new int[] { 9,1,11, 9,1,11, 33,32,11, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'C105'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T05() throws Exception {
    this.storage.DBUpdateServerService(2,
        new int[] { 10,45, 20,32, 30,4, 40,3, 50,4, 60,32, 70,45, 80,40, 90,5 },
        new int[] { 20,32,12, 40,3,12, 90,5,2 },
        new int[] { 12 }, EMPTY);
    assertArrayEquals("T05 pre-condition failure",
        new int[] { 10,45,2,0, 20,32,0,12, 40,3,0,12, 90,5,2,0 },
        this.storage.DBQueryServerSchedule(2));
    /* Neg: request 12 is listed in the positive set but it is already assigned
     * to server 2. */
    final int[] ko_sched = new int[] { 33,32,12, 49,3,12, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'C88'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 12 }, EMPTY);
  }
  @Test
  public void T06a() throws Exception {
    /* Neg: request 11 is listed in the positive set but origin label is wrong. */
    final int[] ko_sched = new int[] { 17,2,11, 33,32,11, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'C92'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T06b() throws Exception {
    /* Neg: request 11 is listed in the positive set but dest label is wrong. */
    final int[] ko_sched = new int[] { 9,1,11, 25,4,11, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'C94'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 11 }, EMPTY);
  }
  @Test
  public void T07() throws Exception {
    /* Neg: request 13 is listed in the positive set but pickup is before early time. */
    final int[] ko_sched = new int[] { 9,1,13, 33,32,13, 50,0,1 };
    thrown.expect(SQLException.class);
    thrown.expectMessage(containsString("'C91'"));
    this.storage.DBUpdateServerService(ok_sid, ok_route, ko_sched, new int[] { 13 }, EMPTY);
  }
}
