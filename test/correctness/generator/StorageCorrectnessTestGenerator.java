import com.github.jargors.Controller;

public class StorageCorrectnessTestGenerator {
  public static void main(String[] args) {
    Controller controller = new Controller();
    controller.loadDataModel();
    controller.loadRoadNetwork("data/test.rnet");
    controller.saveBackup("temp");
    System.out.print(
      "Success! Data model and road network exported to 'temp'.\n"
    + "Next steps:\n"
    + "  1. Launch ij (derbytools.jar)\n"
    + "  2. Enter the command\n"
    + "       ij> run 'data/test.sql';\n"
    + "  3. Use newly-loaded 'db' database for Storage test program.\n"
    );
  }
}
