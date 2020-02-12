import com.github.jargors.Client;
import com.github.jargors.Controller;
import com.github.jargors.Tools;
import com.github.jargors.Traffic;
import com.github.jargors.algo.*;
import com.github.jargors.exceptions.DuplicateUserException;
import com.github.jargors.exceptions.EdgeNotFoundException;
import com.github.jargors.exceptions.GtreeIllegalSourceException;
import com.github.jargors.exceptions.GtreeIllegalTargetException;
import com.github.jargors.exceptions.GtreeNotLoadedException;
import com.github.jargors.traffic.*;
import java.io.FileNotFoundException;
import java.sql.SQLException;
public class app {
  public static void main(String[] args) throws Exception {
    Client client = null;
    Traffic traffic = null;
    boolean hasTraf = true;

    if (args.length != 6) {
      System.out.println("usage: ['seq' | 'real'] ['grins' | 'grins-d'] ['none', 'random', 'norm1'] [*.rnet] [*.gtree] [*.instance]");
      System.exit(0);
    }

    // Initialize Jargo
    try {
      Controller controller = new Controller();
      controller.instanceNew();
      controller.instanceInitialize();
      controller.setRefClient(client);
      controller.forwardRefCommunicator(controller.getRefCommunicator());
      client.forwardRefCacheVertices(controller.retrieveRefCacheVertices());
      client.forwardRefCacheEdges(controller.retrieveRefCacheEdges());
      client.forwardRefCacheUsers(controller.retrieveRefCacheUsers());
      try {
        controller.gtreeLoad(args[4]);
        client.gtreeLoad(args[4]);
      } catch (FileNotFoundException e) {
        System.err.println("Gtree missing?");
        e.printStackTrace();
        System.exit(1);
      }
      try {
        controller.loadRoadNetworkFromFile(args[3]);
      } catch (FileNotFoundException e) {
        System.err.println("Road network missing?");
        e.printStackTrace();
        System.exit(1);
      }
      try {
        controller.loadProblem(args[5]);
      } catch (FileNotFoundException e) {
        System.err.println("Instance missing?");
        e.printStackTrace();
        System.exit(1);
      } catch (DuplicateUserException e) {
        System.err.println("Duplicate users?");
        e.printStackTrace();
        System.exit(1);
      } catch (EdgeNotFoundException e) {
        System.err.println("Corrupt gtree?");
        e.printStackTrace();
        System.exit(1);
      } catch (GtreeNotLoadedException e) {
        System.err.println("Gtree not loaded?");
        e.printStackTrace();
        System.exit(1);
      } catch (GtreeIllegalSourceException e) {
        System.err.println("Impossible origin?");
        e.printStackTrace();
        System.exit(1);
      } catch (GtreeIllegalTargetException e) {
        System.err.println("Impossible destination?");
        e.printStackTrace();
        System.exit(1);
      }

      // Set algorithm
      if (args[1].equals("grins")) {
        client = new GreedyInsertion();
        System.out.println("set client GreedyInsertion");
      } else if (args[1].equals("grins-d")) {
        client = new GreedyDelta();
        System.out.println("set client GreedyDelta");
      } else {
        System.out.println("Unknown client '"+args[1]+"'");
        System.exit(0);
      }
      client.init();

      // Set traffic
      if (args[2].equals("none")) {
        hasTraf = false;
      } else if (args[2].equals("random")) {
        traffic = new RandomTraffic();
      } else if (args[2].equals("norm1")) {
        traffic = new Norm1();
      } else {
        System.out.println("Uknown traffic '"+args[2]+"'");
        System.exit(0);
      }

      if (hasTraf) {
        traffic.forwardRefCacheEdges(controller.retrieveRefCacheEdges());
        traffic.forwardRefCacheVertices(controller.retrieveRefCacheVertices());
        traffic.init();
        controller.forwardRefTraffic(traffic);
      }

      // controller.instanceExport("db");
      // System.exit(1);

      if (args[0].equals("seq")) {
        controller.startSequential((statuscode) -> {
          try {
            controller.instanceExport("results/"+args[1]);
            int[] output = new int[] { };

            output = controller.queryMetricServiceRate();
            final double sr = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricUserDistanceBaseTotal();
            final int bd = (output.length > 0  ? output[0] : 0);

            output = controller.queryMetricServerDistanceTotal();
            final int td = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricRequestDistanceBaseUnassignedTotal();
            final int rb = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricServerTWViolationsTotal();
            int stw_sum = 0;
            int stw_count = 0;
            if (output.length > 0) {
              for (int i = 0; i < (output.length - 2); i += 2) {
                stw_sum += output[(i + 1)];
                stw_count++;
              }
            }
            final double stw_avg = (double) stw_sum/stw_count;

            output = controller.queryMetricRequestTWViolationsTotal();
            int rtw_sum = 0;
            int rtw_count = 0;
            if (output.length > 0) {
              for (int i = 0; i < (output.length - 2); i += 2) {
                rtw_sum += output[(i + 1)];
                rtw_count++;
              }
            }
            final double rtw_avg = (double) rtw_sum/rtw_count;

            System.out.printf("%-54s%10.2f\n", "Service rate (%):", sr);
            System.out.printf("%-54s%10d\n", "Base dist. (total):", bd);
            System.out.printf("%-54s%10d\n", "Server travel dist. (total):", td);
            System.out.printf("%-54s%10d\n", "Request base dist. (unassigned):", rb);
            System.out.printf("%-54s%10.2f\n", "Dist. savings (%):", (1-((float) (td+rb)/bd)));
            System.out.printf("%-54s%10d\n", "Server time window violations (total sec.):", stw_sum);
            System.out.printf("%-54s%10d\n", "Server time window violations (total #):", stw_count);
            System.out.printf("%-54s%10.2f\n", "Server time window violations (avg sec./serv.):", stw_avg);
            System.out.printf("%-54s%10d\n", "Request time window violations (total sec.):", rtw_sum);
            System.out.printf("%-54s%10d\n", "Request time window violations (total #):", rtw_count);
            System.out.printf("%-54s%10.2f\n", "Request time window violations (avg sec./req.):", rtw_avg);
          } catch (SQLException e1) {
            System.err.println("Something bad happened");
            Tools.PrintSQLException(e1);
            System.exit(1);
          }
        });
      } else if (args[0].equals("real")) {
        controller.startRealtime((statuscode) -> {
          try {
            controller.instanceExport("results/"+args[1]);
            int[] output = new int[] { };

            output = controller.queryMetricServiceRate();
            final double sr = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricUserDistanceBaseTotal();
            final int bd = (output.length > 0  ? output[0] : 0);

            output = controller.queryMetricServerDistanceTotal();
            final int td = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricRequestDistanceBaseUnassignedTotal();
            final int rb = (output.length > 0 ? output[0] : 0);

            output = controller.queryMetricServerTWViolationsTotal();
            int stw_sum = 0;
            int stw_count = 0;
            if (output.length > 0) {
              for (int i = 0; i < (output.length - 2); i += 2) {
                stw_sum += output[(i + 1)];
                stw_count++;
              }
            }
            final double stw_avg = (double) stw_sum/stw_count;

            output = controller.queryMetricRequestTWViolationsTotal();
            int rtw_sum = 0;
            int rtw_count = 0;
            if (output.length > 0) {
              for (int i = 0; i < (output.length - 2); i += 2) {
                rtw_sum += output[(i + 1)];
                rtw_count++;
              }
            }
            final double rtw_avg = (double) rtw_sum/rtw_count;

            System.out.printf("%-54s%10.2f\n", "Service rate (%):", sr);
            System.out.printf("%-54s%10d\n", "Base dist. (total):", bd);
            System.out.printf("%-54s%10d\n", "Server travel dist. (total):", td);
            System.out.printf("%-54s%10d\n", "Request base dist. (unassigned):", rb);
            System.out.printf("%-54s%10.2f\n", "Dist. savings (%):", (1-((float) (td+rb)/bd)));
            System.out.printf("%-54s%10d\n", "Server time window violations (total sec.):", stw_sum);
            System.out.printf("%-54s%10d\n", "Server time window violations (total #):", stw_count);
            System.out.printf("%-54s%10.2f\n", "Server time window violations (avg sec./serv.):", stw_avg);
            System.out.printf("%-54s%10d\n", "Request time window violations (total sec.):", rtw_sum);
            System.out.printf("%-54s%10d\n", "Request time window violations (total #):", rtw_count);
            System.out.printf("%-54s%10.2f\n", "Request time window violations (avg sec./req.):", rtw_avg);
          } catch (Exception e1) {
            System.err.println("Something bad happened");
            e1.printStackTrace();
            System.exit(1);
          }
        });
      } else {
        System.out.println("Unknown mode '"+args[0]+"'");
        System.exit(0);
      }
    } catch (Exception e2) {
      System.err.println("Something bad happened");
      e2.printStackTrace();
      System.exit(1);
    }
  }
}
