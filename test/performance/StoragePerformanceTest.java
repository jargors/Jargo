import com.github.jargors.Storage;
import java.time.LocalDateTime;
public class StoragePerformanceTest {
  
  public static void main(String[] args) {
    Print("Starting storage performance tests");
    Storage storage = new Storage();
    storage.DBLoadBackup("data/db");
    Print("Complete!");
  }
  private static void Print(String msg) {
    System.out.println("[StoragePerformanceTest]["+LocalDateTime.now()+"] "+msg);
  }
}
