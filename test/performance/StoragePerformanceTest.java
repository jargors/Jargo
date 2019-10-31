import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
public class StoragePerformanceTest {
  
  public static void main(String[] args) {
    Print("Starting storage performance tests");
    try {
      /*{
          Storage storage = new Storage();
          final int n = 10000;  // 10,000
          Connection[] arr = new Connection[n];
          long _t0 = System.currentTimeMillis();
          int _count = 0;
          float _dur = 0;
          for (int i = 0; i < n; i++) {
            arr[i] = storage._getConnection();
            System.out.print("\r          \r"+_count);
            _count++;
          }
          long _t1 = System.currentTimeMillis();
          _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
          System.out.print("\r");
          Print("_getConnection(0): "+_dur+" ms/call");
          for (Connection c : arr) {
            try {
              c.close();
            } catch (Exception e) { }
          }
        }*/
      /*{
          Controller controller = new Controller();
          controller.loadDataModel();
          controller.loadRoadNetwork("data/chengdu.rnet");
          controller.loadGTree("data/chengdu.gtree");
          final int n = 1000;  // 1,000
          long _t0 = System.currentTimeMillis();
          int _count = 0;
          float _dur = 0;
          for (int i = 0; i < n; i++) {
            int o = (int) Math.round(Math.random()*32934);
            int d = (int) Math.round(Math.random()*32934);
            controller.addNewServer(new int[] { i, -1, 0, 86400000, o, d, 1 });
            System.out.print("\r          \r"+_count);
            _count++;
          }
          long _t1 = System.currentTimeMillis();
          _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
          System.out.print("\r");
          Print("DBAddNewServer(1): "+_dur+" ms/call");
        }*/
      /*{
          Controller controller = new Controller();
          controller.loadDataModel();
          controller.loadRoadNetwork("data/chengdu.rnet");
          controller.loadGTree("data/chengdu.gtree");
          controller.loadProblem("data/chengdu.instance");
          int[] output = new int[] { };
          long _t0 = System.currentTimeMillis();
          int _count = 0;
          float _dur = 0;
          for (int t = 0; t < 1800; t++) {
            output = controller.queryQueuedRequests(t);
            System.out.print("\r          \r"+_count);
            _count++;
          }
          long _t1 = System.currentTimeMillis();
          _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
          System.out.print("\r");
          Print("DBQueryQueuedRequests(1): "+_dur+" ms/call");
        }*/
      {
        Storage storage = new Storage();
        storage.DBLoadBackup("data/db");
        storage.DBLoadRoadNetworkFromDB();
        storage.DBLoadUsersFromDB();
        // BACKUP DOS NOT HAVE INDEXES
        try {
          Connection conn = storage._getConnection();
          Statement stmt = conn.createStatement();
          stmt.addBatch("CREATE INDEX W_sid_t1 ON W (sid, t1)");
          stmt.addBatch("CREATE INDEX W_sid_t2 ON W (sid, t2)");
          stmt.addBatch("CREATE INDEX W_sid_v2 ON W (sid, v2)");
          stmt.addBatch("CREATE INDEX W_sid_t1_t2 ON W (sid, t1, t2)");
          stmt.executeBatch();
          conn.commit();
          conn.close();
        } catch (Exception e) {
          Print(e.toString());
        }
        int[] output = new int[] { };
        long _t0 = System.currentTimeMillis();
        int _count = 0;
        float _dur = 0;
        for (int t = 0; t < 1800; t+=100) {
          output = storage.DBQueryServerLocationsActive(t);
          System.out.print("\r          \r"+_count);
          _count++;
        }
        long _t1 = System.currentTimeMillis();
        _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
        System.out.print("\r");
        Print("DBQueryServerLocationsActive(1): "+_dur+" ms/call");
      }
    } catch (SQLException e) {
      Tools.PrintSQLException(e);
    }
    Print("Complete!");
  }
  private static void Print(String msg) {
    System.out.println("[StoragePerformanceTest]["+LocalDateTime.now()+"] "+msg);
  }
}
