import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.exceptions.UserNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.rules.ExpectedException;
import java.sql.SQLException;
public class SchedulesTest {
  public final Controller controller = new Controller();
  public final int   ok_sid = 1;
  public final int[] ok_route  = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 41,4, 49,3, 50,0 };
  public final int[] ok_sched1 = new int[] { 50,0,1 };
  public final int[] ok_sched2 = new int[] { 9,1,11, 33,32,11, 50,0,1 };
  public final int[] ok_sched3 = new int[] { 9,1,11, 33,32,11, 33,32,12, 49,3,12, 50,0,1 };
  public Storage storage = null;
  @Before
  public void start() throws Exception {
    this.controller.instanceNew();
    this.controller.instanceInitialize();
    this.controller.gtreeLoad("data/cd0n3m2.gtree");
    this.controller.loadRoadNetworkFromFile("data/cd0n3m2.rnet");
    this.controller.loadProblem("data/cd0n3m2.instance");
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
    final int[] ko_sched = new int[] { 50,0,2 };
    /* Server sid (param 1) is not found in the schedule to be submitted.
     * Jargo throws a UserNotFoundException because it thinks "2" is a request,
     * and it cannot find this request in the server's existing schedule. */
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerRoute(ok_sid, ok_route, ko_sched);
  }
  @Test
  public void T01b() throws Exception {
    final int[] ko_sched = new int[] { 50,0,2 };
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ko_sched, new int[] { });
  }
  @Test
  public void T01c() throws Exception {
    final int[] ko_sched = new int[] { 50,0,2 };
    this.thrown.expect(UserNotFoundException.class);
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ok_route, ko_sched, new int[] { });
  }
  @Test
  public void T01d() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,11 };
    /* Server sid (param 1) is not found in the schedule to be submitted.
     * In this variation, we simulate submitting a schedule where the server
     * label has been mis-identified as a request it has already serviced.
     * Jargo throws a F46 violation because the mis-labeled waypoint does not
     * reference the existing pick-up/drop-off for the request. */
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F46'"));
    this.storage.DBUpdateServerRoute(ok_sid, ko_route, ko_sched);
  }
  @Test
  public void T01e() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,11 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F46'"));
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ko_route, ko_sched, new int[] { });
  }
  @Test
  public void T01f() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,11 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'F46'"));
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ko_route, ko_sched, new int[] { });
  }
  @Test
  public void T02a() throws Exception {
    final int[] ko_sched = new int[] { 49,3,1 };
    /* Server label is not on a waypoint where the vertex is the server's
     * destination. Jargo throws a C74 violation. */
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerRoute(ok_sid, ok_route, ko_sched);
  }
  @Test
  public void T02b() throws Exception {
    final int[] ko_sched = new int[] { 49,3,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ko_sched, new int[] { });
  }
  @Test
  public void T02c() throws Exception {
    final int[] ko_sched = new int[] { 49,3,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ok_route, ko_sched, new int[] { });
  }
  @Test
  public void T03a() throws Exception {
    assertArrayEquals("T03a pre-condition failed",
        new int[] { 1,22,1,0, 2,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    final int[] ko_sched = new int[] { 50,0,1 };
    /* Server label is on a waypoint where the vertex is the server's
     * destination. Positive test for T02. */
    this.storage.DBUpdateServerRoute(ok_sid, ok_route, ko_sched);
    assertArrayEquals("T03a failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T03b() throws Exception {
    assertArrayEquals("T03b pre-condition failed",
        new int[] { 1,22,1,0, 2,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    final int[] ko_sched = new int[] { 50,0,1 };
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ko_sched, new int[] { });
    assertArrayEquals("T03b failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T03c() throws Exception {
    assertArrayEquals("T03c pre-condition failed",
        new int[] { 1,22,1,0, 2,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
    final int[] ko_sched = new int[] { 50,0,1 };
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ok_route, ko_sched, new int[] { });
    assertArrayEquals("T03c failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04a() throws Exception {
    final int[] ko_sched = new int[] { 50,0,1, 50,0,1 };
    /* Server label is duplicated. Nothing bad happens, but we might want
     * to throw a warning or something. */
    this.storage.DBUpdateServerRoute(ok_sid, ok_route, ko_sched);
    assertArrayEquals("T04a failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04b() throws Exception {
    final int[] ko_sched = new int[] { 50,0,1, 50,0,1 };
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ko_sched, new int[] { });
    assertArrayEquals("T04b failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04c() throws Exception {
    final int[] ko_sched = new int[] { 50,0,1, 50,0,1 };
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ok_route, ko_sched, new int[] { });
    assertArrayEquals("T04c failed",
        new int[] { 1,22,1,0, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04d() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 50,0,1 };
    /* Server label is duplicated amongst a good label. Nothing bad
     * happens, but we might want to throw a warning or something. */
    this.storage.DBUpdateServerRoute(ok_sid, ko_route, ko_sched);
    assertArrayEquals("T04d failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04e() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 50,0,1 };
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ko_route, ko_sched, new int[] { });
    assertArrayEquals("T04e failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04f() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 50,0,1 };
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ko_route, ko_sched, new int[] { });
    assertArrayEquals("T04f failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04g() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 49,3,1, 33,32,11, 50,0,1 };
    /* One bad server label amongst a good server label. Nothing bad happens as
     * long as the good server label is after the bad server label. */
    this.storage.DBUpdateServerRoute(ok_sid, ko_route, ko_sched);
    assertArrayEquals("T04g failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04h() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 49,3,1, 33,32,11, 50,0,1 };
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ko_route, ko_sched, new int[] { });
    assertArrayEquals("T04h failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04i() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 49,3,1, 33,32,11, 50,0,1 };
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ko_route, ko_sched, new int[] { });
    assertArrayEquals("T04i failed",
        new int[] { 1,22,1,0, 9,1,0,11, 33,32,0,11, 50,0,1,0 },
        this.storage.DBQueryServerSchedule(ok_sid));
  }
  @Test
  public void T04j() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 49,3,1 };
    /* One bad server label amongst a good server label. Jargo throws a
     * C74 violation because it thinks the bad server label is on the wrong
     * vertex. */
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerRoute(ok_sid, ko_route, ko_sched);
  }
  @Test
  public void T04k() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 49,3,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ko_route, ko_sched, new int[] { });
  }
  @Test
  public void T04l() throws Exception {
    this.storage.DBUpdateServerAddToSchedule(ok_sid, ok_route, ok_sched2, new int[] { 11 });
    final int[] ko_route = new int[] { 25,4, 33,32, 41,4, 49,3, 50,0 };
    final int[] ko_sched = new int[] { 50,0,1, 33,32,11, 49,3,1 };
    this.thrown.expect(SQLException.class);
    this.thrown.expectMessage(containsString("'C74'"));
    this.storage.DBUpdateServerRemoveFromSchedule(ok_sid, ko_route, ko_sched, new int[] { });
  }
}
