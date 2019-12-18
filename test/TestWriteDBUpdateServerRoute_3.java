import com.github.jargors.Communicator;
import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.exceptions.*;
import java.sql.SQLException;
class TestWriteDBUpdateServerRoute_3 {
  private static final String INSTANCE = "data/cd0n3m2";
  public static void Reset(final Controller refController) throws Exception {
    refController.instanceClose();
    refController.instanceNew();
    refController.instanceInitialize();
    refController.loadRoadNetworkFromFile(INSTANCE+".rnet");
    refController.loadProblem(INSTANCE+".instance");
  }
  public static void main(String args[]) throws Exception {
    Controller controller = new Controller();
    Communicator communicator = controller.getRefCommunicator();
    Storage storage = controller.getRefStorage();
    controller.instanceNew();
    controller.instanceInitialize();
    controller.gtreeLoad(INSTANCE+".gtree");
    controller.loadRoadNetworkFromFile(INSTANCE+".rnet");
    controller.loadProblem(INSTANCE+".instance");
    // controller.instanceExport("cd0n3m2");
    // System.exit(1);
/*******************************************************************************/
/* BEGIN TestWriteDBUpdateServerRoute_3                                                            */
/*******************************************************************************/
int count_passed = 0;
int count_total  = 0;
final int ok_sid  =  1;
final int ko_sid1 =  0;  // not a user
final int ko_sid2 = 10;  // not a server
final int ok_rid1 = 11;
final int ok_rid2 = 12;
final int[] ok_route1 = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 34,0 };
final int[] ok_route2 = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 41,4, 49,3, 50,0 };
final int[] ko_route1 = new int[] { 1,22, 17,2, 25,4, 33,32, 34,0 };        // (22, 2) not an edge
final int[] ko_route2 = new int[] { 1,22, 2,100, 3,0 };                     // 100 not a vertex
final int[] ko_route3 = new int[] { 1,22, 17,1, 9,2, 25,4, 33,32, 34,0 };   // time not increasing
final int[] ko_route4 = new int[] { 9,1, 17,2, 25,4, 33,32, 34,0 };         // first wayp. not in W
final int[] ko_route5 = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32 };         // last wayp. not server dest.
final int[] ko_route6 = new int[] { 1,22, 9,1, 17,2, 25,4, 33,32, 501,0 };  // exceeds late window
final int[] ok_sched1 = new int[] { 34,0,1 };
final int[] ok_sched2 = new int[] { 33,32,1 };  // use with ko_route5
final int[] ok_sched3 = new int[] { 501,0,1 };  // use with ko_route6
final int[] ok_sched4 = new int[] { 9,1,11, 33,32,11, 34,0,1 }; // use with ok_route1
final int[] ok_sched5 = new int[] { 33,32,11, 9,1,11, 34,0,1 }; // use with ok_route1
final int[] ok_sched6 = new int[] { 9,1,11, 33,32,11, 33,32,12, 49,3,12, 50,0,1 }; // use with ok_route2
final int[] ko_sched1 = new int[] { 33,32,11, 34,0,1 };                     // contains a new wayp.
final int[] ko_sched2 = new int[] { 10,1,11, 33,32,11, 34,0,1 };            // time incorrect
final int[] ko_sched3 = new int[] { 9,2,11, 33,32,11, 34,0,1 };             // vertex incorrect
final int[] ko_sched4 = new int[] { 9,1,10, 33,32,11, 34,0,1 };             // label incorrect
final int[] ko_sched5 = new int[] { 33,32,11, 34,0,1 };                     // deletes wayp.
final int[] ko_sched6 = new int[] { 9,1,11, 33,32,11, 49,3,12, 33,32,12, 50,0,1 }; // multi-lab not side-by-side
final int[] ko_sched7 = new int[] { 9,1,11, 33,32,12, 33,32,11, 49,3,12, 50,0,1 }; // dropoff not written first
// DBUpdateServerRoute(3) ///////////////////////////////////////////////////////
System.out.printf("S01. Param 1 (sid) not a user"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ko_sid1, ok_route1, ok_sched1);
  } catch (UserNotFoundException e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S02. Param 1 (sid) not a server"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ko_sid2, ok_route1, ok_sched1);
  } catch (SQLException e) {
    if (e.getMessage().contains("'F17'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S03. route contains an edge not in Table E"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ko_route1, ok_sched1);
  } catch (EdgeNotFoundException e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S04. route contains a vertex not in Table V"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ko_route2, ok_sched1);
  } catch (EdgeNotFoundException e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S05. route time sequence not strictly increasing"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ko_route3, ok_sched1);
  } catch (SQLException e) {
    if (e.getMessage().contains("'C56'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S06. route[0], route[1] not in Table W"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ko_route4, ok_sched1);
  } catch (SQLException e) {
    if (e.getMessage().contains("'F20'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}


System.out.printf("S07. route[route.length-1] not sid's destination"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ko_route5, ok_sched2);
  } catch (SQLException e) {
    if (e.getMessage().contains("'C74'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S08. route[route.length-2] exceeds sid's late time"); {
  boolean flag = false;
  storage.DBUpdateServerRoute(ok_sid, ko_route6, ok_sched3);
  // To disallow time window violations, uncomment C73 and C93
  if (storage.DBQueryMetricServerTWViolationsTotal()[0] == 1) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S09. schedule contains new wayp not in Table CPD"); {
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ko_sched1);
  } catch (UserNotFoundException e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
}

System.out.printf("S10. schedule changes time of existing wayp"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route1, ok_sched4, new int[] { ok_rid1 });
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ko_sched2);
  } catch (SQLException e) {
    if (e.getMessage().contains("'F23'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S11. schedule changes vertex of existing wayp"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route1, ok_sched4, new int[] { ok_rid1 });
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ko_sched3);
  } catch (SQLException e) {
    if (e.getMessage().contains("'F46'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S12. schedule changes label of existing wayp"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route1, ok_sched4, new int[] { ok_rid1 });
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ko_sched4);
  } catch (UserNotFoundException e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S13. schedule changes order of existing wayp"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route1, ok_sched4, new int[] { ok_rid1 });
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ok_sched5);
    int[] test = storage.DBQueryServerSchedule(ok_sid);
    if (test[ 0] ==  1 && test[ 1] == 22 && test[ 2] == ok_sid && test[ 3] == 0
     && test[ 4] ==  9 && test[ 5] ==  1 && test[ 6] == 0      && test[ 7] == ok_rid1
     && test[ 8] == 33 && test[ 9] == 32 && test[10] == 0      && test[11] == ok_rid1
     && test[12] == 34 && test[13] ==  0 && test[14] == ok_sid && test[15] == 0) {
      count_passed++;
      flag = true;
    }
  } catch (Exception e) { }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S14. schedule deletes existing wayp"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route1, ok_sched4, new int[] { ok_rid1 });
  boolean flag = false;
  try {
    // This passes, with no actual deletion occurring. We probably should throw
    // an exception or at least a warning
    storage.DBUpdateServerRoute(ok_sid, ok_route1, ko_sched5);
  } catch (Exception e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S15. multi-labels are not written side-by-side in the schedule"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route2, ok_sched6, new int[] { ok_rid1, ok_rid2 });
  boolean flag = false;
  try {
    // This also passes, seems like multi-labels can be written non-adjacent
    // with no affect on the schedule
    storage.DBUpdateServerRoute(ok_sid, ok_route2, ko_sched6);
  } catch (Exception e) {
    count_passed++;
    flag = true;
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

System.out.printf("S16. pick-ups are written before drop-offs for a multi-label waypoint"); {
  storage.DBUpdateServerAddToSchedule(ok_sid, ok_route2, ok_sched6, new int[] { ok_rid1, ok_rid2 });
  boolean flag = false;
  try {
    storage.DBUpdateServerRoute(ok_sid, ok_route2, ko_sched7);
  } catch (Exception e) {
    if (e.getMessage().contains("'C102'")) {
      count_passed++;
      flag = true;
    }
  }
  System.out.printf(" [%s]\n", (flag ? "PASS" : "FAIL"));
  count_total++;
  Reset(controller);
}

/*******************************************************************************/
/* END TestWriteDBUpdateServerRoute_3                                                              */
/*******************************************************************************/
System.out.printf("Passed/Total: %d/%d\n", count_passed, count_total);
  }
}

