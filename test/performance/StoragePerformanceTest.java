import com.github.jargors.Storage;
import java.time.LocalDateTime;
public class StoragePerformanceTest {
  
  public static void main(String[] args) {
    Print("Starting storage performance tests");
    Storage storage = new Storage();
    storage.DBLoadBackup("data/db");
    {
      long _t0 = System.currentTimeMillis();
      int _count = 0;
      float _dur = 0;
      int[] output = new int[] { };
      for (int t = 0; t < 1800; t++) {
        output = storage.DBQueryQueuedRequests(t);
        System.out.print("\r          \r"+_count);
        _count++;
      }
      long _t1 = System.currentTimeMillis();
      _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
      System.out.print("\r");
      Print("DBQueryQueuedRequests(1): "+_dur+" ms/call");
    }
    {
      long _t0 = System.currentTimeMillis();
      int _count = 0;
      float _dur = 0;
      int[] output = new int[] { };
      for (int t = 0; t < 1800; t++) {
        output = storage.DBQueryServerLocationsActive(t);
        System.out.print("\r          \r"+_count);
        _count++;
      }
      long _t1 = System.currentTimeMillis();
      _dur=((_t1 - _t0)/(float)(_count == 0 ? 1 : _count));
      System.out.print("\r");
      Print("DBQueryServerLocationsActive(1): "+_dur+" ms/call");
    }
    Print("Complete!");
  }
  private static void Print(String msg) {
    System.out.println("[StoragePerformanceTest]["+LocalDateTime.now()+"] "+msg);
  }
}
