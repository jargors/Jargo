import com.github.jargors.Controller;
import com.github.jargors.Storage;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.DuplicateVertexException;
import com.github.jargors.exceptions.DuplicateEdgeException;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.UserNotFoundException;
import com.github.jargors.exceptions.VertexNotFoundException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
public class StoragePerformanceTest {
  public static void main(String[] args) {
    Tools.Print("Starting storage performance tests");
    try {
      {
        Storage storage = new Storage();
        storage.DBCreateNewInstance();
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
        Tools.Print("_getConnection(0): "+_dur+" ms/call");
        for (Connection c : arr) {
          try {
            c.close();
          } catch (Exception e) { }
        }
        storage.DBCloseInstance();
      }
      {
        Controller controller = new Controller();
        controller.createNewInstance();
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
        Tools.Print("DBAddNewServer(1): "+_dur+" ms/call");
        controller.closeInstance();
      }
      {
        Controller controller = new Controller();
        controller.createNewInstance();
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
        Tools.Print("DBQueryQueuedRequests(1): "+_dur+" ms/call");
        controller.closeInstance();
      }
      {
        Storage storage = new Storage();
        storage.DBLoadBackup("data/db");
        storage.DBLoadRoadNetworkFromDB();
        storage.DBLoadUsersFromDB();
        // BACKUP DOES NOT HAVE INDEXES
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
          Tools.Print(e.toString());
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
        Tools.Print("DBQueryServerLocationsActive(1): "+_dur+" ms/call");
        storage.DBCloseInstance();
      }
      {
        Storage storage = new Storage();
        storage.DBLoadBackup("data/db");
        storage.DBLoadRoadNetworkFromDB();
        storage.DBLoadUsersFromDB();
        // BACKUP DOES NOT HAVE INDEXES
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
          Tools.Print(e.toString());
        }
        int[] output = new int[] { };
        long _t0 = System.currentTimeMillis();
        int _count = 0;
        float _dur = 0;
        for (int t = 0; t < 1800; t++) {
          for (int i = 0; i < 100; i++) {
            int sid = (int) (Math.random()*4999.0) + 1;
            output = storage.DBQueryServerRemainingSchedule(sid, t);
          }
          System.out.print("\r          \r"+_count);
          _count++;
        }
        long _t1 = System.currentTimeMillis();
        _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
        System.out.print("\r");
        Tools.Print("DBQueryServerRemainingSchedule(2): "+_dur+" ms/call");
        storage.DBCloseInstance();
      }
    } catch (SQLException e) {
      Tools.PrintSQLException(e);
    }
    Tools.Print("Complete!");
  }
}
