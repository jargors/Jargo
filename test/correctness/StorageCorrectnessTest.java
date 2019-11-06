import com.github.jargors.Storage;
public class StorageCorrectnessTest {
  private static int count_passed = 0;
  private static int count_failed = 0;
  public static void main(String[] args) {
    Storage storage = new Storage();
    System.out.println("Starting storage interface tests");
    try {
      storage.DBLoadBackup("data/db");
      storage.DBLoadRoadNetworkFromDB();
      storage.DBLoadUsersFromDB();
      {
        int output[] = storage.DBQuery("SELECT COUNT (*) FROM V", 1);
        if (output[0] != 47) {
          System.out.println("[FAIL] DBQuery(2)");
          System.out.println("\tExpected 47; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQuery(2)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryUser(1);
        if (!(output[0] == 1
           && output[1] == -10
           && output[2] == 1
           && output[3] == 500
           && output[4] == 22
           && output[5] == 0
           && output[6] == 0)) {
          System.out.println("[FAIL] DBQueryUser(1)");
          System.out.println("\tExpected {uid=1, q=-10, e=1, l=500, o=22, d=0, b=0}; got");
          printUser(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryUser(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryUser(10);
        if (!(output[0] == 10
           && output[1] == 1
           && output[2] == 0
           && output[3] == 500
           && output[4] == 4
           && output[5] == 30
           && output[6] == 172)) {
          System.out.println("[FAIL] DBQueryUser(1)");
          System.out.println("\tExpected {uid=10, q=1, e=0, l=500, o=4, d=30, b=172}; got");
          printUser(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryUser(1)");
          count_passed++;
        }
      }
      {
        {
          int output[] = storage.DBQueryQueuedRequests(0);
          if (!(output.length/7 == 0)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (1/4)");
            System.out.println("\tExpected 0; got "+output.length/7);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryQueuedRequests(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(5);
          if (!(output.length/7 == 1)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (2/4)");
            System.out.println("\tExpected 1; got "+output.length/7);
            count_failed++;
          } else if (!(output[0] == 11)
            && (output[1] == 1)
            && (output[2] == 5)
            && (output[3] == 500)
            && (output[4] == 1)
            && (output[5] == 32)
            && (output[6] == 194)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (2/4)");
            System.out.println("\tExpected {uid=11, q=1, e=5, l=500, o=1, d=32, b=194}; got");
            printUser(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryQueuedRequests(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(35);
          if (!(output.length/7 == 1)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (3/4)");
            System.out.println("\tExpected 1; got "+output.length/7);
            count_failed++;
          } else if (!(output[0] == 11)
            && (output[1] == 1)
            && (output[2] == 5)
            && (output[3] == 500)
            && (output[4] == 1)
            && (output[5] == 32)
            && (output[6] == 194)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (3/4)");
            System.out.println("\tExpected {uid=11, q=1, e=5, l=500, o=1, d=32, b=194}; got");
            printUser(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryQueuedRequests(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryQueuedRequests(36);
          if (!(output.length/7 == 0)) {
            System.out.println("[FAIL] DBQueryQueuedRequests(1) (4/4)");
            System.out.println("\tExpected 0; got "+output.length/7);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryQueuedRequests(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryActiveServers(0);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (1/4)");
            System.out.println("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryActiveServers(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryActiveServers(44);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (2/4)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (2/4)");
            System.out.println("Expected (1); got ("+output[0]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryActiveServers(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryActiveServers(45);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (3/4)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (3/4)");
            System.out.println("Expected (1); got ("+output[0]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryActiveServers(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryActiveServers(46);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (4/4)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1)) {
            System.out.println("[FAIL] DBQueryActiveServers(1) (4/4)");
            System.out.println("Expected (1); got ("+output[0]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryActiveServers(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryServerLocationsAll(0);
          if (!(output.length/3 == 0)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (1/4)");
            System.out.println("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsAll(1) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(44);
          if (!(output.length/3 == 2)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (2/4)");
            System.out.println("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (2/4)");
            System.out.println("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsAll(1) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(45);
          if (!(output.length/3 == 2)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (3/4)");
            System.out.println("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (3/4)");
            System.out.println("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsAll(1) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsAll(45);
          if (!(output.length/3 == 2)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (4/4)");
            System.out.println("\tExpected 2; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30
            && output[3] == 2
            && output[4] == 28
            && output[5] == 5)) {
            System.out.println("[FAIL] DBQueryServerLocationsAll(1) (4/4)");
            System.out.println("Expected (sid=1, t=44, v=30) (sid=2, t=28, v=5); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+") "
              + "(sid="+output[3]+", t="+output[4]+", v="+output[5]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsAll(1) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryServerLocationsActive(0);
          if (!(output.length/3 == 0)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (1/5)");
            System.out.println("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsActive(1) (1/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(1);
          if (!(output.length/3 == 1)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (2/5)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 1
            && output[2] == 22)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (2/5)");
            System.out.println("Expected (sid=1, t=1, v=22); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsActive(1) (2/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(44);
          if (!(output.length/3 == 1)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (3/5)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else if (!(output[0] == 1
            && output[1] == 44
            && output[2] == 30)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (3/5)");
            System.out.println("Expected (sid=1, t=44, v=30); got "
              + "(sid="+output[0]+", t="+output[1]+", v="+output[2]+")");
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsActive(1) (3/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(45);
          if (!(output.length/3 == 1)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (4/5)");
            System.out.println("\tExpected 1; got "+output.length/3);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsActive(1) (4/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerLocationsActive(500);
          if (!(output.length/3 == 0)) {
            System.out.println("[FAIL] DBQueryServerLocationsActive(1) (5/5)");
            System.out.println("\tExpected 0; got "+output.length/3);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerLocationsActive(1) (5/5)");
            count_passed++;
          }
        }
      }
      {
        int output[] = storage.DBQueryServerRoute(2);
        if (!(output.length == 6)) {
          System.out.println("[FAIL] DBQueryServerRoute(1)");
          System.out.println("\tExpected 6; got "+output.length);
          count_failed++;
        } else if (!(output[0] == 10)
          && (output[1] == 45)
          && (output[2] == 19)
          && (output[3] == 40)
          && (output[4] == 28)
          && (output[5] == 5)) {
          System.out.println("[FAIL] DBQueryServerRoute(1)");
          System.out.println("\tExpected (10, 45) (19, 40) (28, 5); got ");
          printRoute(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerRoute(1)");
          count_passed++;
        }
      }
        {
          int output[] = storage.DBQueryServerRemainingRoute(2, 9);
          if (!(output.length == 6)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (1/3)");
            System.out.println("\tExpected 6; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10
            && output[1] == 45
            && output[2] == 19
            && output[3] == 40
            && output[4] == 28
            && output[5] == 5)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (1/3)");
            System.out.println("\tExpected (10, 45) (19, 40) (28 5); got ");
            printRoute(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingRoute(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerRemainingRoute(2, 10);
          if (!(output.length == 4)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (2/3)");
            System.out.println("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 19
            && output[1] == 40
            && output[2] == 28
            && output[3] == 5)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (2/3)");
            System.out.println("\tExpected (19, 40) (28 5); got ");
            printRoute(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingRoute(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerRemainingRoute(2, 11);
          if (!(output.length == 4)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (3/3)");
            System.out.println("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 19
            && output[1] == 40
            && output[2] == 28
            && output[3] == 5)) {
            System.out.println("[FAIL] DBQueryServerRemainingRoute(2) (3/3)");
            System.out.println("\tExpected (19, 40) (28 5); got ");
            printRoute(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingRoute(2) (3/3)");
            count_passed++;
          }
        }
      {
        int output[] = storage.DBQueryServerSchedule(1);
        if (!(output.length == 16)) {
          System.out.println("[FAIL] DBQueryServerSchedule(1)");
          System.out.println("\tExpected 16; got "+output.length);
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
          System.out.println("[FAIL] DBQueryServerSchedule(1)");
          System.out.println("\tExpected (1, 22, 1, 0) (25, 4, 0, 10) "
            + "(44, 30, 0, 10) (45, 0, 1, 0); got ");
          printSchedule(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerSchedule(1)");
          count_passed++;
        }
      }
      {
        {
          int output[] = storage.DBQueryServerRemainingSchedule(1, 43);
          if (!(output.length == 8)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (1/4)");
            System.out.println("\tExpected 8; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 44
            && output[1] == 30
            && output[2] == 0
            && output[3] == 10
            && output[4] == 45
            && output[5] == 0
            && output[6] == 1
            && output[7] == 0)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (1/4)");
            System.out.println("\tExpected (44, 30, 0, 10) (45, 0, 1, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingSchedule(2) (1/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerRemainingSchedule(1, 44);
          if (!(output.length == 4)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (2/4)");
            System.out.println("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (2/4)");
            System.out.println("\tExpected (45, 0, 1, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingSchedule(2) (2/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerRemainingSchedule(1, 45);
          if (!(output.length == 4)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (3/4)");
            System.out.println("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (3/4)");
            System.out.println("\tExpected (45, 0, 1, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingSchedule(2) (3/4)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerRemainingSchedule(1, 46);
          if (!(output.length == 4)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (4/4)");
            System.out.println("\tExpected 4; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 45
            && output[1] == 0
            && output[2] == 1
            && output[3] == 0)) {
            System.out.println("[FAIL] DBQueryServerRemainingSchedule(2) (4/4)");
            System.out.println("\tExpected (45, 0, 1, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerRemainingSchedule(2) (4/4)");
            count_passed++;
          }
        }
      }
      {
        {
          int output[] = storage.DBQueryServerMaxLoad(1, 0);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBQueryServerMaxLoad(2) (1/5)");
            System.out.println("\tExpected empty; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerMaxLoad(2) (1/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerMaxLoad(1, 24);
          if (!(output[0] == -10)) {
            System.out.println("[FAIL] DBQueryServerMaxLoad(2) (2/5)");
            System.out.println("\tExpected -10; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerMaxLoad(2) (2/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerMaxLoad(1, 25);
          if (!(output[0] == -9)) {
            System.out.println("[FAIL] DBQueryServerMaxLoad(2) (3/5)");
            System.out.println("\tExpected -9; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerMaxLoad(2) (3/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerMaxLoad(1, 26);
          if (!(output[0] == -9)) {
            System.out.println("[FAIL] DBQueryServerMaxLoad(2) (4/5)");
            System.out.println("\tExpected -9; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerMaxLoad(2) (4/5)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerMaxLoad(1, 44);
          if (!(output[0] == -10)) {
            System.out.println("[FAIL] DBQueryServerMaxLoad(2) (5/5)");
            System.out.println("\tExpected -10; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerMaxLoad(2) (5/5)");
            count_passed++;
          }
        }
      }
      {
        int output[] = storage.DBQueryCountVertices();
        if (!(output[0] == 46)) {
          System.out.println("[FAIL] DBQueryCountVertices(0)");
          System.out.println("\tExpected 46; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryCountVertices(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountEdges();
        if (!(output[0] == 104)) {
          System.out.println("[FAIL] DBQueryCountEdges(0)");
          System.out.println("\tExpected 104; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryCountEdges(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryVertex(22);
        if (!(output[0] == 1040750029)
          && (output[1] == 306639030)) {
          System.out.println("[FAIL] DBQueryVertex(1)");
          System.out.println("\tExpected {1040750029, 306639030}; got "
            + "{"+output[0]+", "+output[1]+"}");
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryVertex(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryEdge(22, 1);
        if (!(output[0] == 71)
          && (output[1] == 10)) {
          System.out.println("[FAIL] DBQueryEdge(2)");
          System.out.println("\tExpected {71, 10}; got "
            + "{"+output[0]+", "+output[1]+"}");
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryEdge(2)");
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
          System.out.println("[FAIL] DBQueryStatisticsEdges(0)");
          System.out.println("\tExpected {46, 83, 61, 10, 10, 10}; got {"+output[0]+", "+output[1]
            + ", "+output[2]+", "+output[3]+", "+output[4]+", "+output[5]+"}");
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryStatisticsEdges(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryMBR();
        if (!(output[0] == 1040738577)
          && (output[1] == 1040803253)
          && (output[2] == 306608700)
          && (output[3] == 306659874)) {
          System.out.println("[FAIL] DBQueryMBR(0)");
          System.out.println("\tExpected {1040738577, 1040803253, 306608700, 306659874}; "
            + "got {"+output[0]+", "+output[1]+", "+output[2]+", "+output[3]+"}");
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryMBR(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountServers();
        if (!(output[0] == 2)) {
          System.out.println("[FAIL] DBQueryCountServers(0)");
          System.out.println("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryCountServers(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryCountRequests();
        if (!(output[0] == 2)) {
          System.out.println("[FAIL] DBQueryCountRequests(0)");
          System.out.println("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryCountRequests(0)");
          count_passed++;
        }
      }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1, 43);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryServerPendingAssignments(2) (1/3)");
            System.out.println("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            System.out.println("[FAIL] DBQueryServerPendingAssignments(2) (1/3)");
            System.out.println("\tExpected 10; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerPendingAssignments(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1,44);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBQueryServerPendingAssignments(2) (2/3)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerPendingAssignments(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerPendingAssignments(1,45);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBQueryServerPendingAssignments(2) (3/3)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerPendingAssignments(2) (3/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 43);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBQueryServerCompletedAssignments(2) (1/3)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerCompletedAssignments(2) (1/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 44);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryServerCompletedAssignments(2) (2/3)");
            System.out.println("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            System.out.println("[FAIL] DBQueryServerCompletedAssignments(2) (2/3)");
            System.out.println("\tExpected 10; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerCompletedAssignments(2) (2/3)");
            count_passed++;
          }
        }
        {
          int output[] = storage.DBQueryServerCompletedAssignments(1, 45);
          if (!(output.length == 1)) {
            System.out.println("[FAIL] DBQueryServerCompletedAssignments(2) (3/3)");
            System.out.println("\tExpected 1; got "+output.length);
            count_failed++;
          } else if (!(output[0] == 10)) {
            System.out.println("[FAIL] DBQueryServerCompletedAssignments(2) (3/3)");
            System.out.println("\tExpected 10; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBQueryServerCompletedAssignments(2) (3/3)");
            count_passed++;
          }
        }
      {
        int output[] = storage.DBQueryServiceRate();
        if (!(output[0] == 5000)) {
          System.out.println("[FAIL] DBQueryServiceRate(0)");
          System.out.println("\tExpected 5000; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServiceRate(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryBaseDistanceTotal();
        if (!(output[0] == 532)) {
          System.out.println("[FAIL] DBQueryBaseDistanceTotal(0)");
          System.out.println("\tExpected 532; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerBaseDistanceTotal();
        if (!(output[0] == 166)) {
          System.out.println("[FAIL] DBQueryServerBaseDistanceTotal(0)");
          System.out.println("\tExpected 166; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestBaseDistanceTotal();
        if (!(output[0] == 366)) {
          System.out.println("[FAIL] DBQueryRequestBaseDistanceTotal(0)");
          System.out.println("\tExpected 366; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestBaseDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDistance(1);
        if (!(output[0] == 387)) {
          System.out.println("[FAIL] DBQueryServerTravelDistance(1)");
          System.out.println("\tExpected 387; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerTravelDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDistanceTotal();
        if (!(output[0] == 553)) {
          System.out.println("[FAIL] DBQueryServerTravelDistanceTotal(0)");
          System.out.println("\tExpected 553; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerTravelDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerCruisingDistance(1);
        if (!(output[0] == 213)) {
          System.out.println("[FAIL] DBQueryServerCruisingDistance(1)");
          System.out.println("\tExpected 213; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerCruisingDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerCruisingDistanceTotal();
        if (!(output[0] == 379)) {
          System.out.println("[FAIL] DBQueryServerCruisingDistanceTotal(0)");
          System.out.println("\tExpected 379; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerCruisingDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerServiceDistance(1);
        if (!(output[0] == 174)) {
          System.out.println("[FAIL] DBQueryServerServiceDistance(1)");
          System.out.println("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerServiceDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerServiceDistanceTotal();
        if (!(output[0] == 174)) {
          System.out.println("[FAIL] DBQueryServerServiceDistanceTotal(0)");
          System.out.println("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerServiceDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDetourDistance(10);
        if (!(output[0] == 2)) {
          System.out.println("[FAIL] DBQueryRequestDetourDistance(1)");
          System.out.println("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestDetourDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDetourDistanceTotal();
        if (!(output[0] == 2)) {
          System.out.println("[FAIL] DBQueryRequestDetourDistanceTotal(0)");
          System.out.println("\tExpected 2; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestDetourDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDistance(10);
        if (!(output[0] == 174)) {
          System.out.println("[FAIL] DBQueryRequestTransitDistance(1)");
          System.out.println("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTransitDistance(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDistanceTotal();
        if (!(output[0] == 174)) {
          System.out.println("[FAIL] DBQueryRequestTransitDistanceTotal(0)");
          System.out.println("\tExpected 174; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTransitDistanceTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDuration(1);
        if (!(output[0] == 44)) {
          System.out.println("[FAIL] DBQueryServerTravelDuration(1)");
          System.out.println("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerTravelDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerTravelDurationTotal();
        if (!(output[0] == 62)) {
          System.out.println("[FAIL] DBQueryServerTravelDurationTotal(0)");
          System.out.println("\tExpected 62; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerTravelDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestPickupDuration(10);
        if (!(output[0] == 25)) {
          System.out.println("[FAIL] DBQueryRequestPickupDuration(1)");
          System.out.println("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestPickupDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestPickupDurationTotal();
        if (!(output[0] == 25)) {
          System.out.println("[FAIL] DBQueryRequestPickupDurationTotal()");
          System.out.println("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestPickupDurationTotal()");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDuration(10);
        if (!(output[0] == 19)) {
          System.out.println("[FAIL] DBQueryRequestTransitDuration(1)");
          System.out.println("\tExpected 19; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTransitDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTransitDurationTotal();
        if (!(output[0] == 19)) {
          System.out.println("[FAIL] DBQueryRequestTransitDurationTotal(0)");
          System.out.println("\tExpected 19; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTransitDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTravelDuration(10);
        if (!(output[0] == 44)) {
          System.out.println("[FAIL] DBQueryRequestTravelDuration(1)");
          System.out.println("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTravelDuration(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestTravelDurationTotal();
        if (!(output[0] == 44)) {
          System.out.println("[FAIL] DBQueryRequestTravelDurationTotal(0)");
          System.out.println("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestTravelDurationTotal(0)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestDepartureTime(10);
        if (!(output[0] == 25)) {
          System.out.println("[FAIL] DBQueryRequestDepartureTime(1)");
          System.out.println("\tExpected 25; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestDepartureTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerDepartureTime(1);
        if (!(output[0] == 1)) {
          System.out.println("[FAIL] DBQueryServerDepartureTime(1)");
          System.out.println("\tExpected 1; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerDepartureTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryRequestArrivalTime(10);
        if (!(output[0] == 44)) {
          System.out.println("[FAIL] DBQueryRequestArrivalTime(1)");
          System.out.println("\tExpected 44; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryRequestArrivalTime(1)");
          count_passed++;
        }
      }
      {
        int output[] = storage.DBQueryServerArrivalTime(1);
        if (!(output[0] == 45)) {
          System.out.println("[FAIL] DBQueryServerArrivalTime(1)");
          System.out.println("\tExpected 45; got "+output[0]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBQueryServerArrivalTime(1)");
          count_passed++;
        }
      }
      {
        storage.DBUpdateEdgeSpeed(22, 1, 11);
        int[] output = storage.DBQueryEdge(22, 1);
        if (!(output[1] == 11)) {
          System.out.println("[FAIL] DBUpdateEdgeSpeed(3)");
          System.out.println("\tExpected 11; got "+output[1]);
          count_failed++;
        } else {
          System.out.println("[PASS] DBUpdateEdgeSpeed(3)");
          count_passed++;
        }
      }
      {
        storage.DBAddNewRequest(new int[] { 12, 1, 0, 200, 45, 24, 63 });
        int[] output = storage.DBQueryUser(12);
        if (!(output[0] == 12
          && output[1] == 1
          && output[2] == 0
          && output[3] == 200
          && output[4] == 45
          && output[5] == 24
          && output[6] == 63)) {
          System.out.println("[FAIL] DBAddNewRequest(1)");
          System.out.println("\tExpected { 12, 1, 0, 200, 45, 24, 63 }; got ");
          printUser(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBAddNewRequest(1)");
          count_passed++;
        }
      }
      {
        int[] u = new int[] { 13, -1, 0, 200, 27, 0, 0 };
        int[] r = new int[] { 0, 27, 1, 0 };
        storage.DBAddNewServer(u, r);
        int[] output = storage.DBQueryUser(13);
        if (!(output[0] == 13
          && output[1] == -1
          && output[2] == 0
          && output[3] == 200
          && output[4] == 27
          && output[5] == 0
          && output[6] == 0)) {
          System.out.println("[FAIL] DBAddNewServer(2)");
          System.out.println("\tExpected { 13, -1, 0, 200, 27, 0, 0 }; got ");
          printUser(output);
          count_failed++;
        } else {
          output = storage.DBQueryServerRoute(13);
          if (!(output[0] == 0
            && output[1] == 27
            && output[2] == 1
            && output[3] == 0)) {
            System.out.println("[FAIL] DBAddNewServer(2)");
            System.out.println("\tExpected (0, 27) (1, 0); got ");
            printRoute(output);
            count_failed++;
          } else {
            output = storage.DBQueryServerSchedule(13);
            if (!(output[0] == 0
              && output[1] == 27
              && output[2] == 13
              && output[3] == 0
              && output[4] == 1
              && output[5] == 0
              && output[6] == 13
              && output[7] == 0)) {
              System.out.println("[FAIL] DBAddNewServer(2)");
              System.out.println("\tExpected (0, 27, 13, 0) (1, 0, 13, 0); got ");
              printSchedule(output);
              count_failed++;
            } else {
              System.out.println("[PASS] DBAddNewServer(2)");
              count_passed++;
            }
          }
        }
      }
      {
        storage.DBUpdateServerRoute(13,
          new int[] { 0, 27, 7, 44, 14, 45, 15, 0 },
          new int[] { 15, 0, 13 });
        int[] output = storage.DBQueryServerRoute(13);
        if (!(output[0] == 0
          && output[1] == 27
          && output[2] == 7
          && output[3] == 44
          && output[4] == 14
          && output[5] == 45
          && output[6] == 15
          && output[7] == 0)) {
          System.out.println("[FAIL] DBUpdateServerRoute(2)");
          System.out.println("\tExpected (0, 27) (7, 44) (14, 45) (15, 0); got ");
          printRoute(output);
          count_failed++;
        } else {
          System.out.println("[PASS] DBUpdateServerRoute(2)");
          count_passed++;
        }
      }
        {
          storage.DBUpdateServerAddToSchedule(13,
            new int[] { 7, 44, 14, 45, 21, 24, 22, 0 },
            new int[] { 14, 45, 12, 21, 24, 12, 22, 0, 13 },
            new int[] { 12 });
          int[] output = storage.DBQueryServerRoute(13);
          if (!(output[0] == 0
            && output[1] == 27
            && output[2] == 7
            && output[3] == 44
            && output[4] == 14
            && output[5] == 45
            && output[6] == 21
            && output[7] == 24
            && output[8] == 22
            && output[9] == 0)) {
            System.out.println("[FAIL] DBUpdateServerAddToSchedule(4) (1/5)");
            System.out.println("\tExpected (0, 27) (7, 44) (14, 45) (21, 24) (22, 0); got ");
            printRoute(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerAddToSchedule(4) (1/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryServerSchedule(13);
          if (!(output[0] == 0
            && output[1] == 27
            && output[2] == 13
            && output[3] == 0
            && output[4] == 14
            && output[5] == 45
            && output[6] == 0
            && output[7] == 12
            && output[8] == 21
            && output[9] == 24
            && output[10] == 0
            && output[11] == 12
            && output[12] == 22
            && output[13] == 0
            && output[14] == 13
            && output[15] == 0)) {
            System.out.println("[FAIL] DBAddNewServer(2) (2/5)");
            System.out.println("\tExpected (0, 27, 13, 0) (14, 45, 0, 12) (21, 24, 0, 12) "
              + "(22, 0, 13, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerAddToSchedule(4) (2/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 13);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBUpdateServerAddToSchedule(4) (3/5)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerAddToSchedule(4) (3/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 14);
          if (!(output[0] == 1)) {
            System.out.println("[FAIL] DBUpdateServerAddToSchedule(4) (4/5)");
            System.out.println("\tExpected 1; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerAddToSchedule(4) (4/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 21);
          if (!(output[0] == 2)) {
            System.out.println("[FAIL] DBUpdateServerAddToSchedule(4) (5/5)");
            System.out.println("\tExpected 2; got "+output[0]);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerAddToSchedule(4) (5/5)");
            count_passed++;
          }
        }
        {
          storage.DBUpdateServerRemoveFromSchedule(13,
            new int[] { 0, 27, 1, 0 },
            new int[] { 1, 0, 13 },
            new int[] { 12 });
          int[] output = storage.DBQueryServerRoute(13);
          if (!(output[0] == 0
            && output[1] == 27
            && output[2] == 1
            && output[3] == 0)) {
            System.out.println("[FAIL] DBUpdateServerRemoveFromSchedule(4) (1/5)");
            System.out.println("\tExpected (0, 27) (1, 0); got ");
            printRoute(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerRemoveFromSchedule(4) (1/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryServerSchedule(13);
          if (!(output[0] == 0
            && output[1] == 27
            && output[2] == 13
            && output[3] == 0
            && output[4] == 1
            && output[5] == 0
            && output[6] == 13
            && output[7] == 0)) {
            System.out.println("[FAIL] DBUpdateServerRemoveFromSchedule(4) (2/5)");
            System.out.println("\tExpected (0, 27, 13, 0) (1, 0, 13, 0); got ");
            printSchedule(output);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerRemoveFromSchedule(4) (2/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 13);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBUpdateServerRemoveFromSchedule(4) (3/5)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerRemoveFromSchedule(4) (3/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 14);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBUpdateServerRemoveFromSchedule(4) (4/5)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerRemoveFromSchedule(4) (4/5)");
            count_passed++;
          }
        }
        {
          int[] output = storage.DBQueryRequestStatus(12, 21);
          if (!(output.length == 0)) {
            System.out.println("[FAIL] DBUpdateServerRemoveFromSchedule(4) (5/5)");
            System.out.println("\tExpected 0; got "+output.length);
            count_failed++;
          } else {
            System.out.println("[PASS] DBUpdateServerRemoveFromSchedule(4) (5/5)");
            count_passed++;
          }
        }
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    System.out.println("Complete! Passed: "+count_passed+"; Failed: "+count_failed);
  }
  public static void printRoute(int[] w) {
    for (int i = 0; i < (w.length - 1); i += 2) {
      System.out.print("("+w[i]+", "+w[(i + 1)]+") ");
    }
    System.out.println();
  }

  public static void printSchedule(int[] b) {
    for (int i = 0; i < (b.length - 3); i += 4) {
      System.out.print("("+b[i]+", "+b[(i + 1)]
        + ", "+b[(i + 2)]+", "+b[(i + 3)]+") ");
    }
    System.out.println();
  }

  public static void printUser(int[] u) {
    System.out.println("User {uid="+u[0]+", q="+u[1]+", e="+u[2]+", l="+u[3]
      +", o="+u[4]+", d="+u[5]+", b="+u[6]+"}");
  }
}
