  import com.github.jargors.Storage;
  import java.time.LocalDateTime;
  public class StorageTest {
    private static int count_passed = 0;
    private static int count_failed = 0;
    public static void main(String[] args) {
      Print("Starting storage interface tests");
      Storage storage = new Storage("data/small-directed-dummy.rnet");
      storage.DBLoadBackup("data/db");
      {
        int output[] = storage.DBQuery("SELECT COUNT (*) FROM V", 1);
        if (output[0] != 47) {
          Print("[FAIL] DBQuery(2)");
          Print("\tExpected 47; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQuery(2)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServer(1);
        if (!(output[0] == 1
           && output[1] == -10
           && output[2] == 1
           && output[3] == 500
           && output[4] == 22
           && output[5] == 0
           && output[6] == 0)) {
          Print("[FAIL] DBQueryServer(1)");
          Print("\tExpected {uid=1, q=-10, e=1, l=500, o=22, d=0, b=0}; got");
          storage.printUser(output);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServer(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequest(10);
        if (!(output[0] == 10
           && output[1] == 1
           && output[2] == 0
           && output[3] == 500
           && output[4] == 4
           && output[5] == 30
           && output[6] == 172)) {
          Print("[FAIL] DBQueryRequest(1)");
          Print("\tExpected {uid=10, q=1, e=0, l=500, o=4, d=30, b=172}; got");
          storage.printUser(output);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequest(1)");
          count_passed++;
        }
      }
      {
        {
          int output[] = storage.DBQueryQueuedRequests(0);
          if (!(output.length/7 == 0)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (1/4)");
            Print("\tExpected 0; got "+output.length/7);
            count_failed++;
          } else {
            Print("[PASS] DBQueryQueuedRequests(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(5);
          if (!(output.length/7 == 1)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (2/4)");
            Print("\tExpected 1; got "+output.length/7);
            count_failed++;
          } else if (!(output[0] == 11)
            && (output[1] == 1)
            && (output[2] == 5)
            && (output[3] == 500)
            && (output[4] == 1)
            && (output[5] == 32)
            && (output[6] == 194)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (2/4)");
            Print("\tExpected {uid=11, q=1, e=5, l=500, o=1, d=32, b=194}; got");
            storage.printUser(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryQueuedRequests(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(35);
          if (!(output.length/7 == 1)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (3/4)");
            Print("\tExpected 1; got "+output.length/7);
            count_failed++;
          } else if (!(output[0] == 11)
            && (output[1] == 1)
            && (output[2] == 5)
            && (output[3] == 500)
            && (output[4] == 1)
            && (output[5] == 32)
            && (output[6] == 194)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (3/4)");
            Print("\tExpected {uid=11, q=1, e=5, l=500, o=1, d=32, b=194}; got");
            storage.printUser(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryQueuedRequests(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(36);
          if (!(output.length/7 == 0)) {
            Print("[FAIL] DBQueryQueuedRequests(1) (4/4)");
            Print("\tExpected 0; got "+output.length/7);
            count_failed++;
          } else {
            Print("[PASS] DBQueryQueuedRequests(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryServerLocationsAll(0);
          if (!(output.length/3 == 0)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (1/4)");
            Print("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsAll(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(44);
          if (!(output.length/3 == 2)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (2/4)");
            Print("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (2/4)");
            Print("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsAll(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(45);
          if (!(output.length/3 == 2)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (3/4)");
            Print("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (3/4)");
            Print("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsAll(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(45);
          if (!(output.length/3 == 2)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (4/4)");
            Print("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            Print("[FAIL] DBQueryServerLocationsAll(1) (4/4)");
            Print("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsAll(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryServerLocationsActive(0);
          if (!(output.length/3 == 0)) {
            Print("[FAIL] DBQueryServerLocationsActive(1) (1/4)");
            Print("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsActive(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(44);
          if (!(output.length/3 == 1)) {
            Print("[FAIL] DBQueryServerLocationsActive(1) (2/4)");
            Print("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30)) {
            Print("[FAIL] DBQueryServerLocationsActive(1) (2/4)");
            Print("Expected (sid=1, t=44, v=30); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+")");
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsActive(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(45);
          if (!(output.length/3 == 0)) {
            Print("[FAIL] DBQueryServerLocationsActive(1) (3/4)");
            Print("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsActive(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(46);
          if (!(output.length/3 == 0)) {
            Print("[FAIL] DBQueryServerLocationsActive(1) (4/4)");
            Print("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerLocationsActive(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        int output[] = storage.DBQueryRoute(2);
        if (!(output.length == 6)) {
          Print("[FAIL] DBQueryRoute(1)");
          Print("\tExpected 6; got "+output.length);
          count_failed++;
        } else if (!(output[0] == 10)
          && (output[1] == 45)
          && (output[2] == 19)
          && (output[3] == 40)
          && (output[4] == 28)
          && (output[5] == 5)) {
          Print("[FAIL] DBQueryRoute(1)");
          Print("\tExpected (10, 45) (19, 40) (28, 5); got ");
          storage.printRoute(output);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRoute(1)");
          count_passed++;
        }
      }
        {
          int output[] = storage.DBQueryRouteRemaining(2, 9);
          if (!(output.length == 6)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (1/3)");
            Print("\tExpected 6; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10
            && output[1] == 45
            && output[2] == 19
            && output[3] == 40
            && output[4] == 28
            && output[5] == 5)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (1/3)");
            Print("\tExpected (10, 45) (19, 40) (28 5); got ");
            storage.printRoute(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryRouteRemaining(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryRouteRemaining(2, 10);
          if (!(output.length == 4)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (2/3)");
            Print("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 19
            && output[1] == 40
            && output[2] == 28
            && output[3] == 5)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (2/3)");
            Print("\tExpected (19, 40) (28 5); got ");
            storage.printRoute(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryRouteRemaining(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryRouteRemaining(2, 11);
          if (!(output.length == 4)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (3/3)");
            Print("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 19
            && output[1] == 40
            && output[2] == 28
            && output[3] == 5)) {
            Print("[FAIL] DBQueryRouteRemaining(2) (3/3)");
            Print("\tExpected (19, 40) (28 5); got ");
            storage.printRoute(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryRouteRemaining(2) (3/3)");
            count_passed++;
          }
        }
      {
        int output[] = storage.DBQuerySchedule(1);
        if (!(output.length == 16)) {
          Print("[FAIL] DBQuerySchedule(1)");
          Print("\tExpected 16; got "+output.length);
          count_failed++;
        } else if (!(output[0] == 1
          && output[1] == 22
          && output[2] == 1
          && output[3] == 0
          && output[4] == 25
          && output[5] == 4
          && output[6] == 0
          && output[7] == 10
          && output[8] == 44
          && output[9] == 30
          && output[10] == 0
          && output[11] == 10
          && output[12] == 45
          && output[13] == 0
          && output[14] == 1
          && output[15] == 0)) {
          Print("[FAIL] DBQuerySchedule(1)");
          Print("\tExpected (1, 22, 1, 0) (25, 4, 0, 10) "
            + "(44, 30, 0, 10) (45, 0, 1, 0); got ");
          storage.printSchedule(output);
          count_failed++;
        } else {
          Print("[PASS] DBQuerySchedule(1)");
          count_passed++;
        }
      }
      {
        {
          int output[] = storage.DBQueryScheduleRemaining(1, 43);
          if (!(output.length == 8)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (1/4)");
            Print("\tExpected 8; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 44
            && output[1] == 30
            && output[2] == 0
            && output[3] == 10
            && output[4] == 45
            && output[5] == 0
            && output[6] == 1
            && output[7] == 0)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (1/4)");
            Print("\tExpected (44, 30, 0, 10) (45, 0, 1, 0); got ");
            storage.printSchedule(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryScheduleRemaining(2) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryScheduleRemaining(1, 44);
          if (!(output.length == 4)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (2/4)");
            Print("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (2/4)");
            Print("\tExpected (45, 0, 1, 0); got ");
            storage.printSchedule(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryScheduleRemaining(2) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryScheduleRemaining(1, 45);
          if (!(output.length == 4)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (3/4)");
            Print("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (3/4)");
            Print("\tExpected (45, 0, 1, 0); got ");
            storage.printSchedule(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryScheduleRemaining(2) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryScheduleRemaining(1, 46);
          if (!(output.length == 4)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (4/4)");
            Print("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            Print("[FAIL] DBQueryScheduleRemaining(2) (4/4)");
            Print("\tExpected (45, 0, 1, 0); got ");
            storage.printSchedule(output);
            count_failed++;
          } else {
            Print("[PASS] DBQueryScheduleRemaining(2) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryCurrentLoad(1, 0);
          if (!(output.length == 0)) {
            Print("[FAIL] DBQueryCurrentLoad(2) (1/5)");
            Print("\tExpected empty; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryCurrentLoad(2) (1/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryCurrentLoad(1, 24);
          if (!(output[0] == -10)) {
            Print("[FAIL] DBQueryCurrentLoad(2) (2/5)");
            Print("\tExpected -10; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryCurrentLoad(2) (2/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryCurrentLoad(1, 25);
          if (!(output[0] == -9)) {
            Print("[FAIL] DBQueryCurrentLoad(2) (3/5)");
            Print("\tExpected -9; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryCurrentLoad(2) (3/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryCurrentLoad(1, 26);
          if (!(output[0] == -9)) {
            Print("[FAIL] DBQueryCurrentLoad(2) (4/5)");
            Print("\tExpected -9; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryCurrentLoad(2) (4/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryCurrentLoad(1, 44);
          if (!(output[0] == -10)) {
            Print("[FAIL] DBQueryCurrentLoad(2) (5/5)");
            Print("\tExpected -10; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryCurrentLoad(2) (5/5)");
            count_passed++;
          }
        }
      }
      {
        int output[] = storage.DBQueryCountVertices();
        if (!(output[0] == 46)) {
          Print("[FAIL] DBQueryCountVertices(0)");
          Print("\tExpected 46; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryCountVertices(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountEdges();
        if (!(output[0] == 104)) {
          Print("[FAIL] DBQueryCountEdges(0)");
          Print("\tExpected 104; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryCountEdges(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryVertex(22);
        if (!(output[0] == 1040750029)
          && (output[1] == 306639030)) {
          Print("[FAIL] DBQueryVertex(1)");
          Print("\tExpected {1040750029, 306639030}; got "
            + "{"+output[0]+", "+output[1]+"}");
          count_failed++;
        } else {
          Print("[PASS] DBQueryVertex(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryEdge(22, 1);
        if (!(output[0] == 71)
          && (output[1] == 10)) {
          Print("[FAIL] DBQueryEdge(2)");
          Print("\tExpected {71, 10}; got "
            + "{"+output[0]+", "+output[1]+"}");
          count_failed++;
        } else {
          Print("[PASS] DBQueryEdge(2)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryStatisticsEdges();
        if (!(output[0] == 46)
          && (output[1] == 83)
          && (output[2] == 61)
          && (output[3] == 10)
          && (output[4] == 10)
          && (output[5] == 10)) {
          Print("[FAIL] DBQueryStatisticsEdges(0)");
          Print("\tExpected {46, 83, 61, 10, 10, 10}; got {"+output[0]+", "+output[1]
            + ", "+output[2]+", "+output[3]+", "+output[4]+", "+output[5]+"}");
          count_failed++;
        } else {
          Print("[PASS] DBQueryStatisticsEdges(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryMBR();
        if (!(output[0] == 1040738577)
          && (output[1] == 1040803253)
          && (output[2] == 306608700)
          && (output[3] == 306659874)) {
          Print("[FAIL] DBQueryMBR(0)");
          Print("\tExpected {1040738577, 1040803253, 306608700, 306659874}; "
            + "got {"+output[0]+", "+output[1]+", "+output[2]+", "+output[3]+"}");
          count_failed++;
        } else {
          Print("[PASS] DBQueryMBR(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountServers();
        if (!(output[0] == 2)) {
          Print("[FAIL] DBQueryCountServers(0)");
          Print("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryCountServers(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountRequests();
        if (!(output[0] == 2)) {
          Print("[FAIL] DBQueryCountRequests(0)");
          Print("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryCountRequests(0)");
          count_passed++;
        }
      }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1, 43);
          if (!(output.length == 1)) {
            Print("[FAIL] DBQueryServerPendingAssignments(2) (1/3)");
            Print("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            Print("[FAIL] DBQueryServerPendingAssignments(2) (1/3)");
            Print("\tExpected 10; got "+output[0]);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerPendingAssignments(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1,44);
          if (!(output.length == 0)) {
            Print("[FAIL] DBQueryServerPendingAssignments(2) (2/3)");
            Print("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerPendingAssignments(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1,45);
          if (!(output.length == 0)) {
            Print("[FAIL] DBQueryServerPendingAssignments(2) (3/3)");
            Print("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerPendingAssignments(2) (3/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 43);
          if (!(output.length == 0)) {
            Print("[FAIL] DBQueryServerCompletedAssignments(2) (1/3)");
            Print("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerCompletedAssignments(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 44);
          if (!(output.length == 1)) {
            Print("[FAIL] DBQueryServerCompletedAssignments(2) (2/3)");
            Print("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            Print("[FAIL] DBQueryServerCompletedAssignments(2) (2/3)");
            Print("\tExpected 10; got "+output.length);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerCompletedAssignments(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 45);
          if (!(output.length == 1)) {
            Print("[FAIL] DBQueryServerCompletedAssignments(2) (3/3)");
            Print("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            Print("[FAIL] DBQueryServerCompletedAssignments(2) (3/3)");
            Print("\tExpected 10; got "+output.length);
            count_failed++;
          } else {
            Print("[PASS] DBQueryServerCompletedAssignments(2) (3/3)");
            count_passed++;
          }
        }
      {
        int output[] = storage.DBQueryServiceRate();
        if (!(output[0] == 50)) {
          Print("[FAIL] DBQueryServiceRate(0)");
          Print("\tExpected 50; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServiceRate(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryBaseDistanceTotal();
        if (!(output[0] == 532)) {
          Print("[FAIL] DBQueryBaseDistanceTotal(0)");
          Print("\tExpected 532; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerBaseDistanceTotal();
        if (!(output[0] == 166)) {
          Print("[FAIL] DBQueryServerBaseDistanceTotal(0)");
          Print("\tExpected 166; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestBaseDistanceTotal();
        if (!(output[0] == 366)) {
          Print("[FAIL] DBQueryRequestBaseDistanceTotal(0)");
          Print("\tExpected 366; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDistance(1);
        if (!(output[0] == 387)) {
          Print("[FAIL] DBQueryServerTravelDistance(1)");
          Print("\tExpected 387; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerTravelDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDistanceTotal();
        if (!(output[0] == 553)) {
          Print("[FAIL] DBQueryServerTravelDistanceTotal(0)");
          Print("\tExpected 553; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerTravelDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerCruisingDistance(1);
        if (!(output[0] == 213)) {
          Print("[FAIL] DBQueryServerCruisingDistance(1)");
          Print("\tExpected 213; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerCruisingDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerCruisingDistanceTotal();
        if (!(output[0] == 379)) {
          Print("[FAIL] DBQueryServerCruisingDistanceTotal(0)");
          Print("\tExpected 379; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerCruisingDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerServiceDistance(1);
        if (!(output[0] == 174)) {
          Print("[FAIL] DBQueryServerServiceDistance(1)");
          Print("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerServiceDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerServiceDistanceTotal();
        if (!(output[0] == 174)) {
          Print("[FAIL] DBQueryServerServiceDistanceTotal(0)");
          Print("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerServiceDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDetourDistance(10);
        if (!(output[0] == 2)) {
          Print("[FAIL] DBQueryRequestDetourDistance(1)");
          Print("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestDetourDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDetourDistanceTotal();
        if (!(output[0] == 2)) {
          Print("[FAIL] DBQueryRequestDetourDistanceTotal(0)");
          Print("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestDetourDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDistance(10);
        if (!(output[0] == 174)) {
          Print("[FAIL] DBQueryRequestTransitDistance(1)");
          Print("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTransitDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDistanceTotal();
        if (!(output[0] == 174)) {
          Print("[FAIL] DBQueryRequestTransitDistanceTotal(0)");
          Print("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTransitDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDuration(1);
        if (!(output[0] == 44)) {
          Print("[FAIL] DBQueryServerTravelDuration(1)");
          Print("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerTravelDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDurationTotal();
        if (!(output[0] == 62)) {
          Print("[FAIL] DBQueryServerTravelDurationTotal(0)");
          Print("\tExpected 62; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerTravelDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestPickupDuration(10);
        if (!(output[0] == 25)) {
          Print("[FAIL] DBQueryRequestPickupDuration(1)");
          Print("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestPickupDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestPickupDurationTotal();
        if (!(output[0] == 25)) {
          Print("[FAIL] DBQueryRequestPickupDurationTotal()");
          Print("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestPickupDurationTotal()");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDuration(10);
        if (!(output[0] == 19)) {
          Print("[FAIL] DBQueryRequestTransitDuration(1)");
          Print("\tExpected 19; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTransitDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDurationTotal();
        if (!(output[0] == 19)) {
          Print("[FAIL] DBQueryRequestTransitDurationTotal(0)");
          Print("\tExpected 19; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTransitDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTravelDuration(10);
        if (!(output[0] == 44)) {
          Print("[FAIL] DBQueryRequestTravelDuration(1)");
          Print("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTravelDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTravelDurationTotal();
        if (!(output[0] == 44)) {
          Print("[FAIL] DBQueryRequestTravelDurationTotal(0)");
          Print("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestTravelDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDepartureTime(10);
        if (!(output[0] == 25)) {
          Print("[FAIL] DBQueryRequestDepartureTime(1)");
          Print("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestDepartureTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerDepartureTime(1);
        if (!(output[0] == 1)) {
          Print("[FAIL] DBQueryServerDepartureTime(1)");
          Print("\tExpected 1; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerDepartureTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestArrivalTime(10);
        if (!(output[0] == 44)) {
          Print("[FAIL] DBQueryRequestArrivalTime(1)");
          Print("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryRequestArrivalTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerArrivalTime(1);
        if (!(output[0] == 45)) {
          Print("[FAIL] DBQueryServerArrivalTime(1)");
          Print("\tExpected 45; got "+output[0]);
          count_failed++;
        } else {
          Print("[PASS] DBQueryServerArrivalTime(1)");
          count_passed++;
        }
      }
      
      
      
      
      
      
      
      Print("Complete! Passed: "+count_passed+"; Failed: "+count_failed);
    }
    private static void Print(String msg) {
      System.out.println("[StorageTest]["+LocalDateTime.now()+"] "+msg);
    }
  }
