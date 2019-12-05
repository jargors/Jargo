package com.github.jargors.desktop;
import com.github.jargors.Controller;
import com.github.jargors.Client;
import com.github.jargors.Traffic;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
public class DesktopController {
  private final boolean DEBUG =
      "true".equals(System.getProperty("jargors.desktop.debug"));
  private Stage stage;
  private Canvas can_road;
  private Canvas can_servers;
  private Pane container_canvas_container;
  private Label lbl_fps;
  @FXML private Button btn_new;
  @FXML private Button btn_load;
  @FXML private Button btn_prob;
  @FXML private Button btn_road;
  @FXML private Button btn_gtree;
  @FXML private Button btn_client;
  @FXML private Button btn_traffic;
  @FXML private Button btn_startseq;
  @FXML private Button btn_startreal;
  @FXML private Button btn_pause;
  @FXML private Button btn_stop;
  @FXML private TextField tf_client;
  @FXML private TextField tf_traffic;
  @FXML private TextField tf_t0;
  @FXML private TextField tf_t1;
  @FXML private Label lbl_status;
  @FXML private Circle circ_status;
  @FXML private ScrollPane container_canvas;
  @FXML private AnchorPane container_lc_rates;
  @FXML private AnchorPane container_lc_distances;
  @FXML private AnchorPane container_lc_durations;
  @FXML private AnchorPane container_lc_counts;
  @FXML private AnchorPane container_lc_times;
  @FXML private VBox vbox_metrics_rates;
  @FXML private VBox vbox_metrics_distances;
  @FXML private VBox vbox_metrics_durations;
  @FXML private VBox vbox_metrics_counts;
  @FXML private VBox vbox_metrics_times;
  @FXML private CheckBox chk_serviceRate;
  @FXML private CheckBox chk_distanceSavings;
  @FXML private CheckBox chk_serverTravelDistance;
  @FXML private CheckBox chk_serverServiceDistance;
  @FXML private CheckBox chk_serverCruisingDistance;
  @FXML private CheckBox chk_serverTravelDuration;
  @FXML private CheckBox chk_serverServiceDuration;
  @FXML private CheckBox chk_serverCruisingDuration;
  @FXML private CheckBox chk_requestDistanceUnassigned;
  @FXML private CheckBox chk_requestTransitDistance;
  @FXML private CheckBox chk_requestDetourDistance;
  @FXML private CheckBox chk_requestTransitDuration;
  @FXML private CheckBox chk_requestDetourDuration;
  @FXML private CheckBox chk_requestTravelDuration;
  @FXML private CheckBox chk_requestPickupDuration;
  @FXML private CheckBox chk_countRequestsQueue;
  @FXML private CheckBox chk_countRequestsActive;
  @FXML private CheckBox chk_countRequestsCompleted;
  @FXML private CheckBox chk_countRequestsFailed;
  @FXML private CheckBox chk_countServersActive;
  @FXML private CheckBox chk_countRequestsViolations;
  @FXML private CheckBox chk_countServersViolations;
  @FXML private CheckBox chk_timeRequestHandling;
  @FXML private CheckBox chk_timeServerHandling;
  private final Color C_SUCCESS = Color.GREEN;
  private final Color C_WARN = Color.YELLOW;
  private final Color C_ERROR = Color.RED;
  private final String TITLE_STRING = "Jargo Desktop";
  private Controller controller = null;
  private Client client = null;
  private Traffic traffic = null;
  private String db = null;
  private String gtree = null;
  private String prob = null;
  private String road = null;
  private String clientjar = null;
  private String clientclass = null;
  private String trafficjar = null;
  private String trafficclass = null;
  private GraphicsContext gc = null;
  private MapUnitsFetcher muf = null;
  private double window_height = 0;
  private double window_width = 0;
  private double mouse_x = 0;
  private double mouse_y = 0;
  private int[] edges = null;
  private int[] mbr = null;
  private double xunit = 0;
  private double yunit = 0;
  private double unit = 0;
  private int t0 = 0;
  private int t1 = 0;
  private int zoom = 1;
  private ConcurrentHashMap<Integer, double[]> lu_position = new ConcurrentHashMap<Integer, double[]>();
  private ServersRenderer ren_servers = null;
  private RoadRenderer ren_road = null;
  private ScheduledExecutorService exe = null;
  private ScheduledFuture<?> cbSimRunner = null;
  private ScheduledFuture<?> cbServerLocationsFetcher = null;
  private Map<String, ScheduledFuture<?>> cbMetricFetcher = new HashMap<String, ScheduledFuture<?>>();
  private class ServersRenderer extends AnimationTimer {
    private long prev = 0;
    private int framecount = 0;
    private Image server_img = null;
    private int server_width = 4;
    private Color server_fill = Color.web("0x555555");
    private GraphicsContext gc = null;
    private Canvas can_servers = null;
    private ConcurrentHashMap<Integer, double[]> lu_position = null;
    private Label lbl_fps = null;
    public ServersRenderer(
        final GraphicsContext gc,
        final ConcurrentHashMap<Integer, double[]> lu_position,
        final Label lbl_fps) {
      super();
      this.gc = gc;
      this.lu_position = lu_position;
      this.can_servers = gc.getCanvas();
      this.lbl_fps = lbl_fps;
      // Initialize the server image
      Rectangle rect = new Rectangle(this.server_width, this.server_width);
      rect.setFill(this.server_fill);
      WritableImage wi = new WritableImage(this.server_width, this.server_width);
      SnapshotParameters parameters = new SnapshotParameters();
      rect.snapshot(parameters, wi);
      this.server_img = wi;
    }
    public void handle(long now) {
      // Count FPS
      if (now - prev > 1000000000) {
        this.lbl_fps.setText(String.format("%d",this.framecount));
        prev = now;
        framecount = 0;
      } else {
        framecount++;
      }
      // Draw servers
      this.gc.clearRect(0, 0, this.can_servers.getWidth(), this.can_servers.getHeight());
      for (final Map.Entry<Integer, double[]> kv : this.lu_position.entrySet()) {
        final int      sid = kv.getKey();
        final double[] pos = kv.getValue();
        final double     x = pos[0];
        final double     y = pos[1];
        Platform.runLater(() -> {
          this.gc.drawImage(this.server_img, x, y);
        });
      }
    }
  }
  private class RoadRenderer extends AnimationTimer {
    private long prev = 0;
    private GraphicsContext gc = null;
    private Canvas can_road = null;
    private Controller controller = null;
    private MapUnitsFetcher muf = null;
    private Traffic traffic = null;
    private int[] edges = null;
    private final Color DEFAULT = Color.BLUE;
    private final Color BG   = Color.web("0xD7FFFF");
    private final Color SLOW = Color.web("0xFF0000");
    private final Color MED  = Color.web("0xFFD700");
    private final Color FAST = Color.web("0x5FD870");
    public RoadRenderer(
        final GraphicsContext gc,
        final Controller controller,
        final MapUnitsFetcher muf) {
      super();
      this.gc = gc;
      this.can_road = gc.getCanvas();
      this.controller = controller;
      try {
        this.edges = this.controller.queryEdges();
      } catch (SQLException se) {
        System.err.println("Couldn't get edges for road renderer");
        System.err.println(se.getMessage());
        se.printStackTrace();
      }
      this.muf = muf;
      // Set edge default color
      this.gc.setLineWidth(0.3);
      this.gc.setStroke(DEFAULT);
    }
    public void setTraffic(final Traffic traffic) {
      this.traffic = traffic;
    }
    public void handle(long now) {
      if (now - prev > 10*1000000000) {  // only draw every 10 seconds
        prev = now;
        // Draw edges
        // this.gc.clearRect(0, 0, this.can_road.getWidth(), this.can_road.getHeight());
        for (int i = 0; i < (this.edges.length - 3); i += 4) {
          Color color = DEFAULT;
          if (this.edges[(i + 0)] != 0 && this.edges[(i + 1)] != 0) {
            try {
              final int[] v1 = this.controller.queryVertex(this.edges[(i + 0)]);
              final int[] v2 = this.controller.queryVertex(this.edges[(i + 1)]);
              if (this.traffic != null) {
                final double x = this.traffic.apply(this.edges[i], this.edges[(i + 1)], this.controller.getClockNow());
                if (0.0 <= x && x <= 0.33) {
                  color = SLOW;
                } else if (0.33 < x && x <= 0.66) {
                  color = MED;
                } else if (0.66 < x && x <= 1.0) {
                  color = FAST;
                }
              }
              final Color finalColor = color;
              final double x1 = this.muf.getUnit()*(v1[0] - this.muf.getLngMin());
              final double x2 = this.muf.getUnit()*(v2[0] - this.muf.getLngMin());
              final double y1 = this.muf.getUnit()*(v1[1] - this.muf.getLatMin());
              final double y2 = this.muf.getUnit()*(v2[1] - this.muf.getLatMin());
              Platform.runLater(() -> {
                this.gc.setStroke(BG);
                this.gc.strokeLine(x1, y1, x2, y2);
                this.gc.setStroke(finalColor);
                this.gc.strokeLine(x1, y1, x2, y2);
              });
            } catch (VertexNotFoundException ve) {
              System.err.println("Warning: "+ve.toString());
            } catch (SQLException se) {
              System.err.println("Warning: ServerLocationsFetcher failed to get locations");
              System.err.println(se.toString());
              se.printStackTrace();
            } catch (Exception ee) {
              ee.printStackTrace();
            }
          }
        }
      }
    }
  }
  private class MapUnitsFetcher {
    private double unit = 0;
    private int lng_min = 0;
    private int lat_min = 0;
    public MapUnitsFetcher() { }
    public double getUnit() {
      return this.unit;
    }
    public int getLngMin() {
      return this.lng_min;
    }
    public int getLatMin() {
      return this.lat_min;
    }
    public void setUnit(double unit) {
      this.unit = unit;
    }
    public void setLngMin(int lng_min) {
      this.lng_min = lng_min;
    }
    public void setLatMin(int lat_min) {
      this.lat_min = lat_min;
    }
  }
  private class ServerLocationsFetcher implements Runnable {
    private Controller controller = null;
    private Label lbl_status = null;
    private MapUnitsFetcher muf = null;
    private ConcurrentHashMap<Integer, double[]> lu_position = null;
    public ServerLocationsFetcher(
        final Controller controller,
        final Label lbl_status,
        final MapUnitsFetcher muf,
        final ConcurrentHashMap<Integer, double[]> lu_position) {
      this.controller = controller;
      this.lbl_status = lbl_status;
      this.muf = muf;
      this.lu_position = lu_position;
    }
    public void run() {
      final int t = this.controller.getClockNow();
      try {
        int[] locations = this.controller.queryServersLocationsActive(t);
        for (int i = 0; i < (locations.length - 2); i += 3) {
          final int sid = locations[(i + 0)];
          final int   v = locations[(i + 2)];
          int[] coordinates = this.controller.queryVertex(v);
          double x = this.muf.getUnit()*(coordinates[0] - this.muf.getLngMin());
          double y = this.muf.getUnit()*(coordinates[1] - this.muf.getLatMin());
          this.lu_position.put(sid, new double[] { x, y });
        }
      } catch (SQLException se) {
        System.err.println("Warning: ServerLocationsFetcher failed to get locations");
        System.err.println(se.toString());
        se.printStackTrace();
      } catch (VertexNotFoundException ve) {
        System.err.println("Warning: ServerLocationsFetcher got unknown location!");
        System.err.println(ve.toString());
        ve.printStackTrace();
      } catch (Exception ee) {
        ee.printStackTrace();
      }
      Platform.runLater(() -> {
        this.lbl_status.setText("Refresh server locations (t="+t+")");
      });
    };
  }
  private class MetricFetcher implements Runnable {
    private Controller controller = null;
    private ConcurrentHashMap<String, Series<Number, Number>> series = null;
    private String id = "";
    private long A0 = 0;
    public MetricFetcher(
        final Controller controller,
        final ConcurrentHashMap<String, Series<Number, Number>> series,
        final String id) {
      this.controller = controller;
      this.series = series;
      this.id = id;
    }
    public void run() {
      if (DEBUG) {
        this.A0 = System.currentTimeMillis();
      }
      final int t = this.controller.getClockNow();
      Number val = null;
      try {
        if ("chk_serviceRate".equals(this.id)) {
          int[] output = this.controller.queryMetricServiceRate();
          val = (output.length > 0 ? (output[0]/100.0) : 0);
        } else if ("chk_distanceSavings".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDistanceTotal();
          final int val1 = (output.length > 0 ? output[0] : 0);
          output = this.controller.queryMetricRequestDistanceBaseUnassignedTotal();
          final int val2 = (output.length > 0 ? output[0] : 0);
          output = this.controller.queryMetricUserDistanceBaseTotal();
          final int val3 = (output.length > 0 ? output[0] : 0);
          val = (val3 == 0 ? 0 : (100.0*(1 - ((double) (val1 + val2)/val3))));
        } else if ("chk_serverTravelDistance".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDistanceTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverServiceDistance".equals(this.id)) {
          // VERY SLOW AND BAD
          // int[] output = this.controller.queryServerServiceDistanceTotal();
          // val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverCruisingDistance".equals(this.id)) {
          // VERY SLOW AND BAD
          // int[] output = this.controller.queryServerCruisingDistanceTotal();
          // val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverTravelDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDurationTravelTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverServiceDuration".equals(this.id)) {
          
        } else if ("chk_serverCruisingDuration".equals(this.id)) {
          
        } else if ("chk_requestDistanceUnassigned".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDistanceBaseUnassignedTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_requestTransitDistance".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDistanceTransitTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_requestDetourDistance".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDistanceDetourTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_requestTransitDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDurationTransitTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_requestDetourDuration".equals(this.id)) {
          
        } else if ("chk_requestTravelDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDurationTravelTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_requestPickupDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestDurationPickupTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_countRequestsQueue".equals(this.id)) {
          val = this.controller.retrieveQueueSize();
        } else if ("chk_countRequestsActive".equals(this.id)) {
          
        } else if ("chk_countRequestsCompleted".equals(this.id)) {
          
        } else if ("chk_countRequestsFailed".equals(this.id)) {
          
        } else if ("chk_countServersActive".equals(this.id)) {
          
        } else if ("chk_countRequestsViolations".equals(this.id)) {
          
        } else if ("chk_countServersViolations".equals(this.id)) {
          
        } else if ("chk_timeRequestHandling".equals(this.id)) {
          
        } else if ("chk_timeServerHandling".equals(this.id)) {
          
        }
      } catch (SQLException se) {
        System.err.printf("SQL failure: %s\n", se.getMessage());
      }
      if (val != null) {
        final Number fval = val;
        Platform.runLater(() -> {
          this.series.get(this.id).getData().add(new Data<Number, Number>(t, fval));
        });
      }
      if (DEBUG) {
        System.err.printf("t=%s, %s execution took %d ms\n", t, id, (System.currentTimeMillis() - this.A0));
      }
    }
  }
  private NumberAxis rates_x = new NumberAxis();
  private NumberAxis rates_y = new NumberAxis();
  private NumberAxis distances_x = new NumberAxis();
  private NumberAxis distances_y = new NumberAxis();
  private NumberAxis durations_x = new NumberAxis();
  private NumberAxis durations_y = new NumberAxis();
  private NumberAxis counts_x = new NumberAxis();
  private NumberAxis counts_y = new NumberAxis();
  private NumberAxis times_x = new NumberAxis();
  private NumberAxis times_y = new NumberAxis();
  private LineChart<Number, Number> lc_rates = new LineChart<Number, Number>(rates_x, rates_y);
  private LineChart<Number, Number> lc_distances = new LineChart<Number, Number>(distances_x, distances_y);
  private LineChart<Number, Number> lc_durations = new LineChart<Number, Number>(durations_x, durations_y);
  private LineChart<Number, Number> lc_counts = new LineChart<Number, Number>(counts_x, counts_y);
  private LineChart<Number, Number> lc_times = new LineChart<Number, Number>(times_x, times_y);
  private ConcurrentHashMap<String, Series<Number, Number>> lc_rates_series =
      new ConcurrentHashMap<String, Series<Number, Number>>();
  private ConcurrentHashMap<String, Series<Number, Number>> lc_distances_series =
      new ConcurrentHashMap<String, Series<Number, Number>>();
  private ConcurrentHashMap<String, Series<Number, Number>> lc_durations_series =
      new ConcurrentHashMap<String, Series<Number, Number>>();
  private ConcurrentHashMap<String, Series<Number, Number>> lc_counts_series =
      new ConcurrentHashMap<String, Series<Number, Number>>();
  private ConcurrentHashMap<String, Series<Number, Number>> lc_times_series =
      new ConcurrentHashMap<String, Series<Number, Number>>();
  private String[] metric_rates = new String[] {
        "chk_serviceRate",
        "chk_distanceSavings"
      };
  private String[] metric_distances = new String[] {
        "chk_serverTravelDistance",
        "chk_serverServiceDistance",
        "chk_serverCruisingDistance",
        "chk_requestDistanceUnassigned",
        "chk_requestTransitDistance",
        "chk_requestDetourDistance"
      };
  private String[] metric_durations = new String[] {
        "chk_serverTravelDuration",
        "chk_serverServiceDuration",
        "chk_serverCruisingDuration",
        "chk_requestTransitDuration",
        "chk_requestDetourDuration",
        "chk_requestTravelDuration",
        "chk_requestPickupDuration"
      };
  private String[] metric_counts = new String[] {
        "chk_countRequestsQueue",
        "chk_countRequestsActive",
        "chk_countRequestsCompleted",
        "chk_countRequestsFailed",
        "chk_countServersActive",
        "chk_countRequestsViolations",
        "chk_countServersViolations"
      };
  private String[] metric_times = new String[] {
        "chk_timeRequestHandling",
        "chk_timeServerHandling"
      };
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Series<Number, Number>>> lu_series =
      new ConcurrentHashMap<String, ConcurrentHashMap<String, Series<Number, Number>>>();
  public void actionAbout(final ActionEvent e) {
           // ...
         }
  public void actionClient(final ActionEvent e) {
           this.btn_client   .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.jar...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Client *.jar", "*.jar"));
           File cj = fc.showOpenDialog(this.stage);
           if (cj != null) {
             this.clientjar = cj.toString();

             try {
         /*https://stackoverflow.com/questions/15720822/how-to-get-names-of-classes-inside-a-jar-file*/
         List<String> classNames = new ArrayList<String>();
         ZipInputStream zip = new ZipInputStream(new FileInputStream(this.clientjar));
         for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
           if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
             String className = entry.getName().replace('/', '.');
             classNames.add(className.substring(0, className.length() - ".class".length()));
           }
         }
         /******/
               if (classNames.size() == 0) {
                 System.err.println("Bad jar?");
                 return;
               }
               this.clientclass = classNames.get(0);
             } catch (IOException ie) {
               System.err.println(ie.toString());
               return;
             }
             this.tf_client.setText(this.clientclass);
             this.btn_client   .setText(cj.getName());
             this.tf_client     .setDisable(false);
             this.tf_t0        .setDisable(false);
             this.tf_t1        .setDisable(false);
             this.btn_startseq .setDisable(false);
             this.btn_startreal.setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Loaded "+cj.getName());
           }
         }
  public void actionGitHub(final ActionEvent e) {
           // ...
         }
  public void actionGtree(final ActionEvent e) {
           boolean state_btn_prob = this.btn_prob.isDisabled();
           boolean state_btn_road = this.btn_road.isDisabled();
           this.btn_prob     .setDisable(true);
           this.btn_road     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.btn_gtree    .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.gtree...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("G-tree *.gtree", "*.gtree"));
           File gt = fc.showOpenDialog(this.stage);
           if (gt != null) {
             if (this.gtree != null) {
               this.controller.gtreeClose();
             }
             this.gtree = gt.toString();
             this.lbl_status.setText("Load '"+this.gtree+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.gtreeLoad(this.gtree);
                 Platform.runLater(() -> {
                   this.btn_prob     .setDisable(state_btn_prob);
                   this.btn_road     .setDisable(state_btn_road);
                   this.btn_stop     .setDisable(false);
                   if (this.road != null && this.prob == null) {
                     this.btn_prob   .setDisable(false);
                   }
                   if (this.road != null && this.prob != null) {
                     this.btn_client .setDisable(false);
                     this.btn_traffic.setDisable(false);
                   }
                   this.btn_gtree    .setText(gt.getName());
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+gt.getName());
                 });
               } catch (FileNotFoundException fe) {
                 System.err.println("Failed: "+fe.toString());
                 return;
               }
             });
           } else {
             this.btn_prob     .setDisable(state_btn_prob);
             this.btn_road     .setDisable(state_btn_road);
             this.btn_stop     .setDisable(false);
             this.btn_gtree    .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Cancelled load gtree.");
           }
         }
  public void actionLoad(final ActionEvent e) {
           this.btn_new      .setDisable(true);
           this.btn_load     .setDisable(true);
           this.btn_stop     .setDisable(true);
           DirectoryChooser dc = new DirectoryChooser();
           File db = dc.showDialog(this.stage);
           if (db != null) {
             this.db = db.toString();
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Load '"+this.db+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller = new Controller();
                 this.controller.instanceLoad(this.db);
                 this.controller.cacheRoadNetworkFromDB();
                 this.controller.cacheUsersFromDB();
                 int nv = this.controller.queryVerticesCount()[0];
                 int ne = this.controller.queryEdgesCount()[0];
                 int ns = this.controller.queryServersCount()[0];
                 int nr = this.controller.queryRequestsCount()[0];
                 Platform.runLater(() -> {
                   this.btn_prob     .setDisable(true);
                   this.btn_prob     .setText("*in-instance problem*");
                   this.prob = "*in-instance problem*";
                   this.btn_road     .setDisable(true);
                   this.btn_road     .setText("*in-instance road network*");
                   this.road = "*in-instance road network*";
                   this.btn_gtree    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded Jargo instance (#vertices="+nv+"; #edges="+ne+") (#servers="+ns+"; #requests="+nr+")");
                   this.container_canvas.setContent(null);
                   this.container_lc_rates.getChildren().clear();
                   this.container_lc_distances.getChildren().clear();
                   this.container_lc_durations.getChildren().clear();
                   this.initializeCanvas();
                 });
               } catch (SQLException se) {
                 System.err.println("Failed");
                 Tools.PrintSQLException(se);
                 return;
               }
             });
           } else {
             this.btn_new      .setDisable(false);
             this.btn_load     .setDisable(false);
             this.btn_stop     .setDisable(false);
           }
         }
  public void actionNew(final ActionEvent e) {
           this.btn_new      .setDisable(true);
           this.btn_load     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.circ_status.setFill(C_WARN);
           this.lbl_status.setText("Create new Jargo instance...");
           CompletableFuture.runAsync(() -> {
             this.controller = new Controller();
             try {
               this.controller.instanceNew();
             } catch (SQLException se) {
               System.err.println("Could not create new instance");
               System.exit(1);
             }
             this.controller.instanceInitialize();
             this.db = "no-name";
             Platform.runLater(() -> {
               this.btn_road     .setDisable(false);
               this.btn_gtree    .setDisable(false);
               this.btn_stop     .setDisable(false);
               this.tf_client     .setDisable(false);
               this.tf_t0        .setDisable(false);
               this.tf_t1        .setDisable(false);
               this.container_canvas.setContent(null);
               this.container_lc_rates.getChildren().clear();
               this.container_lc_distances.getChildren().clear();
               this.container_lc_durations.getChildren().clear();
               this.circ_status.setFill(C_SUCCESS);
               this.lbl_status.setText("Created new Jargo instance.");
             });
           });
         }
  public void actionProb(final ActionEvent e) {
           this.btn_prob     .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.instance...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Problem Instance *.instance", "*.instance"));
           File pb = fc.showOpenDialog(this.stage);
           if (pb != null) {
             this.prob = pb.toString();
             this.lbl_status.setText("Load '"+this.prob+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadProblem(this.prob);
                 int ns = this.controller.queryServersCount()[0];
                 int nr = this.controller.queryRequestsCount()[0];
                 Platform.runLater(() -> {
                   this.btn_prob     .setText(pb.getName());
                   this.btn_client   .setDisable(false);
                   this.btn_traffic  .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+pb.getName()+"(#servers="+ns+"; #requests="+nr+")");
                 });
               } catch (FileNotFoundException fe) {
                 System.err.println("File not found: "+fe.toString());
                 return;
               } catch (DuplicateUserException de) {
                 System.err.println("Duplicate user: "+de.toString());
                 return;
               } catch (EdgeNotFoundException ee) {
                 System.err.println("Edge not found: "+ee.toString());
                 return;
               } catch (SQLException se) {
                 System.err.println("SQL error:");
                 Tools.PrintSQLException(se);
                 return;
               } catch (GtreeNotLoadedException ge) {
                 System.err.println("Gtree not loaded? "+ge.toString());
                 return;
               } catch (GtreeIllegalSourceException ge) {
                 System.err.println("Gtree illegal source? "+ge.toString());
                 return;
               } catch (GtreeIllegalTargetException ge) {
                 System.err.println("Gtree illegal target? "+ge.toString());
                 return;
               }
             });
           }
         }
  public void actionQuit(final ActionEvent e) {
           System.exit(0);
         }
  public void actionRecordMousePress(MouseEvent e) {
           this.mouse_x = e.getX();
           this.mouse_y = e.getY();
           e.consume();
         }
  public void actionRoad(final ActionEvent e) {
           boolean state_btn_gtree = this.btn_gtree.isDisabled();
           this.btn_road     .setDisable(true);
           this.btn_gtree    .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tf_client     .setDisable(true);
           this.tf_t0        .setDisable(true);
           this.tf_t1        .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.rnet...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Road network *.rnet", "*.rnet"));
           File road = fc.showOpenDialog(this.stage);
           if (road != null) {
             this.road = road.toString();
             this.lbl_status.setText("Load '"+this.road+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadRoadNetworkFromFile(this.road);
                 int nv = this.controller.queryVerticesCount()[0];
                 int ne = this.controller.queryEdgesCount()[0];
                 Platform.runLater(() -> {
                   this.btn_gtree    .setDisable(state_btn_gtree);
                   this.btn_stop     .setDisable(false);
                   this.btn_road     .setText(road.getName());
                   if (this.gtree != null) {
                     this.btn_prob   .setDisable(false);
                   }
                   this.tf_client     .setDisable(false);
                   this.tf_t0        .setDisable(false);
                   this.tf_t1        .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+road.getName()+" (#vertices="+nv+"; #edges="+ne+")");
                   this.initializeCanvas();
                   this.ren_road = new RoadRenderer(this.can_road.getGraphicsContext2D(), this.controller, this.muf);
                   this.ren_road.start();
                 });
               } catch (FileNotFoundException fe) {
                 System.err.println("Failed: "+fe.toString());
                 return;
               } catch (SQLException se) {
                 System.err.println("SQL error:");
                 Tools.PrintSQLException(se);
                 return;
               }
             });
           }
         }
  public void actionStartRealtime(final ActionEvent e) {
         }
  public void actionStartSequential(final ActionEvent e) {
           this.clientclass = this.tf_client.getText();
           if ("".equals(this.clientclass)) {
             System.err.println("Class empty!");
             return;
           }
           this.trafficclass = this.tf_traffic.getText();
           if ("".equals(this.tf_t0.getText())) {
             this.tf_t0.setText("0");
           }
           if ("".equals(this.tf_t1.getText())) {
             this.tf_t1.setText("1800");
           }
           this.btn_startseq .setDisable(true);
           this.btn_startreal.setDisable(true);
           this.tf_client     .setDisable(true);
           this.tf_t0        .setDisable(true);
           this.tf_t1        .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Loading '"+this.clientclass+"'...");
           try {
             URLClassLoader loader = new URLClassLoader(new URL[] {new URL("file://"+this.clientjar)},
                 this.getClass().getClassLoader());
             Class<?> tempclass = Class.forName(this.clientclass, true, loader);
             Constructor<?> tempcstor = tempclass.getDeclaredConstructor();
             this.client = (Client) tempcstor.newInstance();
             this.controller.setRefClient(this.client);
             this.controller.forwardRefCommunicator(this.controller.getRefCommunicator());
             this.client.forwardRefCacheVertices(this.controller.retrieveRefCacheVertices());
             this.client.forwardRefCacheEdges(this.controller.retrieveRefCacheEdges());
             this.client.forwardRefCacheUsers(this.controller.retrieveRefCacheUsers());
             if (this.trafficclass.length() > 0) {
               URLClassLoader loader2 = new URLClassLoader(new URL[] {new URL("file://"+this.trafficjar)},
                   this.getClass().getClassLoader());
               Class<?> tempclass2 = Class.forName(this.trafficclass, true, loader2);
               Constructor<?> tempcstor2 = tempclass2.getDeclaredConstructor();
               this.traffic = (Traffic) tempcstor2.newInstance();
               this.traffic.forwardRefCacheEdges(controller.retrieveRefCacheEdges());
               this.traffic.forwardRefCacheVertices(controller.retrieveRefCacheVertices());
               this.controller.forwardRefTraffic(this.traffic);
               this.ren_road.setTraffic(this.traffic);
             }
             try {
               this.client.gtreeLoad(this.gtree);
             } catch (FileNotFoundException fe) {
               System.err.println(e.toString());
               return;
             }
             this.t0 = Integer.parseInt(this.tf_t0.getText());
             this.t1 = Integer.parseInt(this.tf_t1.getText());
             this.controller.setClockStart(this.t0);
             this.controller.setClockEnd(this.t1);
             this.lc_rates.setCreateSymbols(false);
             this.lc_rates.setAnimated(false);
             this.lc_rates.setLegendVisible(false);
             this.lc_distances.setCreateSymbols(false);
             this.lc_distances.setAnimated(false);
             this.lc_distances.setLegendVisible(false);
             this.lc_durations.setCreateSymbols(false);
             this.lc_durations.setAnimated(false);
             this.lc_durations.setLegendVisible(false);
             this.lc_counts.setCreateSymbols(false);
             this.lc_counts.setAnimated(false);
             this.lc_counts.setLegendVisible(false);
             this.lc_times.setCreateSymbols(false);
             this.lc_times.setAnimated(false);
             this.lc_times.setLegendVisible(false);
             this.rates_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
             this.rates_y = new NumberAxis("Value (%)", 0, 100, 10);
             this.rates_x.setAutoRanging(true);
             this.rates_y.setAutoRanging(true);
             this.distances_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
             this.distances_y = new NumberAxis("Value (meters)", 0, 100, 10);
             this.distances_x.setAutoRanging(true);
             this.distances_y.setAutoRanging(true);
             this.durations_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
             this.durations_y = new NumberAxis("Value (seconds)", 0, 100, 10);
             this.durations_x.setAutoRanging(true);
             this.durations_y.setAutoRanging(true);
             this.counts_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
             this.counts_y = new NumberAxis("Value (count)", 0, 100, 10);
             this.counts_x.setAutoRanging(true);
             this.counts_y.setAutoRanging(true);
             this.times_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
             this.times_y = new NumberAxis("Value (milliseconds)", 0, 100, 10);
             this.times_x.setAutoRanging(true);
             this.times_y.setAutoRanging(true);
             for (String metric : this.metric_rates) {
               this.lc_rates_series.put(metric, new Series<Number, Number>());
               this.lc_rates.getData().add(this.lc_rates_series.get(metric));
               this.lu_series.put(metric, this.lc_rates_series);
             }
             for (String metric : this.metric_distances) {
               this.lc_distances_series.put(metric, new Series<Number, Number>());
               this.lc_distances.getData().add(this.lc_distances_series.get(metric));
               this.lu_series.put(metric, this.lc_distances_series);
             }
             for (String metric : this.metric_durations) {
               this.lc_durations_series.put(metric, new Series<Number, Number>());
               this.lc_durations.getData().add(this.lc_durations_series.get(metric));
               this.lu_series.put(metric, this.lc_durations_series);
             }
             for (String metric : this.metric_counts) {
               this.lc_counts_series.put(metric, new Series<Number, Number>());
               this.lc_counts.getData().add(this.lc_counts_series.get(metric));
               this.lu_series.put(metric, this.lc_counts_series);
             }
             for (String metric : this.metric_times) {
               this.lc_times_series.put(metric, new Series<Number, Number>());
               this.lc_times.getData().add(this.lc_times_series.get(metric));
               this.lu_series.put(metric, this.lc_times_series);
             }
             this.container_lc_rates.setTopAnchor(this.lc_rates, 0.0);
             this.container_lc_rates.setLeftAnchor(this.lc_rates, 0.0);
             this.container_lc_rates.setRightAnchor(this.lc_rates, 0.0);
             this.container_lc_rates.setBottomAnchor(this.lc_rates, 0.0);
             this.container_lc_rates.getChildren().add(this.lc_rates);
             this.container_lc_distances.setTopAnchor(this.lc_distances, 0.0);
             this.container_lc_distances.setLeftAnchor(this.lc_distances, 0.0);
             this.container_lc_distances.setRightAnchor(this.lc_distances, 0.0);
             this.container_lc_distances.setBottomAnchor(this.lc_distances, 0.0);
             this.container_lc_distances.getChildren().add(this.lc_distances);
             this.container_lc_durations.setTopAnchor(this.lc_durations, 0.0);
             this.container_lc_durations.setLeftAnchor(this.lc_durations, 0.0);
             this.container_lc_durations.setRightAnchor(this.lc_durations, 0.0);
             this.container_lc_durations.setBottomAnchor(this.lc_durations, 0.0);
             this.container_lc_durations.getChildren().add(this.lc_durations);
             this.container_lc_counts.setTopAnchor(this.lc_counts, 0.0);
             this.container_lc_counts.setLeftAnchor(this.lc_counts, 0.0);
             this.container_lc_counts.setRightAnchor(this.lc_counts, 0.0);
             this.container_lc_counts.setBottomAnchor(this.lc_counts, 0.0);
             this.container_lc_counts.getChildren().add(this.lc_counts);
             this.container_lc_times.setTopAnchor(this.lc_times, 0.0);
             this.container_lc_times.setLeftAnchor(this.lc_times, 0.0);
             this.container_lc_times.setRightAnchor(this.lc_times, 0.0);
             this.container_lc_times.setBottomAnchor(this.lc_times, 0.0);
             this.container_lc_times.getChildren().add(this.lc_times);
             this.vbox_metrics_rates.setDisable(false);
             this.vbox_metrics_distances.setDisable(false);
             this.vbox_metrics_durations.setDisable(false);
             this.vbox_metrics_counts.setDisable(false);
             this.vbox_metrics_times.setDisable(false);
             this.ren_servers = new ServersRenderer(this.can_servers.getGraphicsContext2D(), this.lu_position, this.lbl_fps);
             this.ren_servers.start();
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Simulation started.");
             this.exe = Executors.newScheduledThreadPool(2);
             this.cbSimRunner = this.exe.schedule(() -> {
               try {
                 this.controller.startSequential((status) -> {
                   Platform.runLater(() -> {
                     this.lbl_status.setText("Simulation "+(status ? "ended." : "failed."));
                   });
                 });
               } catch (Exception ee) {
                 System.err.println("Unexepected error in startSequential");
                 ee.printStackTrace();
                 System.exit(1);
               }
             }, 0, TimeUnit.SECONDS);
             this.cbServerLocationsFetcher = this.exe.scheduleAtFixedRate(
                 new ServerLocationsFetcher(this.controller, this.lbl_status, this.muf, this.lu_position), 0, 1, TimeUnit.SECONDS);
           } catch (MalformedURLException
               | ClassNotFoundException
               | NoSuchMethodException
               | InstantiationException
               | IllegalAccessException
               | InvocationTargetException me) {
             System.err.println(me.toString());
             me.printStackTrace();
             return;
           }
         }
  public void actionStop(final ActionEvent e) {
           if (this.controller != null) {
             this.btn_stop     .setDisable(true);
             this.vbox_metrics_rates.setDisable(true);
             this.vbox_metrics_distances.setDisable(true);
             this.vbox_metrics_durations.setDisable(true);
             this.vbox_metrics_counts.setDisable(true);
             this.vbox_metrics_times.setDisable(true);
             this.chk_serviceRate.setSelected(false);
             this.chk_distanceSavings.setSelected(false);
             this.chk_serverTravelDistance.setSelected(false);
             this.chk_serverServiceDistance.setSelected(false);
             this.chk_serverCruisingDistance.setSelected(false);
             this.chk_serverTravelDuration.setSelected(false);
             this.chk_serverServiceDuration.setSelected(false);
             this.chk_serverCruisingDuration.setSelected(false);
             this.chk_requestDistanceUnassigned.setSelected(false);
             this.chk_requestTransitDistance.setSelected(false);
             this.chk_requestDetourDistance.setSelected(false);
             this.chk_requestTransitDuration.setSelected(false);
             this.chk_requestDetourDuration.setSelected(false);
             this.chk_requestTravelDuration.setSelected(false);
             this.chk_requestPickupDuration.setSelected(false);
             this.chk_countRequestsQueue.setSelected(false);
             this.chk_countRequestsActive.setSelected(false);
             this.chk_countRequestsCompleted.setSelected(false);
             this.chk_countRequestsFailed.setSelected(false);
             this.chk_countServersActive.setSelected(false);
             this.chk_countRequestsViolations.setSelected(false);
             this.chk_countServersViolations.setSelected(false);
             this.chk_timeRequestHandling.setSelected(false);
             this.chk_timeServerHandling.setSelected(false);
             this.ren_road.stop();
             this.ren_servers.stop();
             if (this.exe != null) {
               this.exe.shutdown();
             }
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Close '"+this.db+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.stop((status) -> {
                   Platform.runLater(() -> {
                     this.lbl_status.setText("Simulation stopped.");
                   });
                 });
                 this.controller.instanceClose();
                 this.controller.gtreeClose();
                 Platform.runLater(() -> {
                   this.btn_new      .setDisable(false);
                   this.btn_load     .setDisable(false);
                   this.btn_prob     .setDisable(true);
                   this.btn_prob     .setText("(empty problem instance)");
                   this.prob = null;
                   this.btn_road     .setDisable(true);
                   this.btn_road     .setText("(empty road network)");
                   this.road = null;
                   this.btn_gtree    .setDisable(true);
                   this.btn_gtree    .setText("(empty G-tree)");
                   this.gtree = null;
                   this.btn_client   .setDisable(true);
                   this.btn_client   .setText("(empty client)");
                   this.btn_traffic  .setDisable(true);
                   this.btn_traffic  .setText("(empty traffic)");
                   this.client = null;
                   this.clientjar = null;
                   this.clientclass = null;
                   this.traffic = null;
                   this.trafficjar = null;
                   this.trafficclass = null;
                   this.tf_client    .setDisable(true);
                   this.tf_client    .setText("");
                   this.tf_traffic   .setDisable(true);
                   this.tf_traffic   .setText("");
                   this.tf_t0        .setDisable(true);
                   this.tf_t0        .setText("");
                   this.tf_t1        .setDisable(true);
                   this.tf_t1        .setText("");
                   this.btn_startseq .setDisable(true);
                   this.btn_startreal.setDisable(true);
                   this.db = null;
                   this.circ_status.setFill(C_SUCCESS);
                   this.lbl_status.setText("Closed instance.");
                 });
               } catch (SQLException se) {
                 System.err.println("Failure");
                 System.exit(1);
               }
             });
           } else {
             this.lbl_status.setText("Ready.");
           }
         }
  public void actionTraffic(final ActionEvent e) {
           this.btn_traffic  .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.jar...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Traffic *.jar", "*.jar"));
           File cj = fc.showOpenDialog(this.stage);
           if (cj != null) {
             this.trafficjar = cj.toString();

             try {
         /*https://stackoverflow.com/questions/15720822/how-to-get-names-of-classes-inside-a-jar-file*/
         List<String> classNames = new ArrayList<String>();
         ZipInputStream zip = new ZipInputStream(new FileInputStream(this.trafficjar));
         for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
           if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
             String className = entry.getName().replace('/', '.');
             classNames.add(className.substring(0, className.length() - ".class".length()));
           }
         }
         /******/
               if (classNames.size() == 0) {
                 System.err.println("Bad jar?");
                 return;
               }
               this.trafficclass = classNames.get(0);
             } catch (IOException ie) {
               System.err.println(ie.toString());
               return;
             }
             this.tf_traffic   .setText(this.trafficclass);
             this.btn_traffic  .setText(cj.getName());
             this.tf_traffic   .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Loaded "+cj.getName());
           }
         }
  public void actionTranslateCanvas(MouseEvent e) {
           this.can_road.setTranslateX(this.can_road.getTranslateX() + e.getX() - this.mouse_x);
           this.can_road.setTranslateY(this.can_road.getTranslateY() + e.getY() - this.mouse_y);
           this.can_servers.setTranslateX(this.can_servers.getTranslateX() + e.getX() - this.mouse_x);
           this.can_servers.setTranslateY(this.can_servers.getTranslateY() + e.getY() - this.mouse_y);
           e.consume();
         }
  public void actionZoomCanvas(ScrollEvent e) {
           if (e.getDeltaY() > 0) {
             this.zoom += 1;
           }
           if (e.getDeltaY() < 0) {
             this.zoom -= 1;
           }
           this.zoom = Math.max(this.zoom, 1);
           this.zoom = Math.min(this.zoom, 12);
           System.out.println(this.zoom);
           this.can_road.setScaleX(this.zoom);
           this.can_road.setScaleY(this.zoom);
           this.can_servers.setScaleX(this.zoom);
           this.can_servers.setScaleY(this.zoom);
           e.consume();
         }
  public void setStage(Stage s) {
           this.stage = s;
         }
  public void setWindowHeight(double h) {
           this.window_height = h;
         }
  public void setWindowWidth(double w) {
           this.window_width = w;
         }
  public void toggleMetric(final ActionEvent e) {
           CheckBox source = (CheckBox) e.getSource();
           final String id = source.getId();
           if (DEBUG) {
             System.err.printf("Toggle CheckBox(id=%s)\n", id);
           }
           if (source.isSelected()) {
             this.cbMetricFetcher.put(id, this.exe.scheduleAtFixedRate(
                 new MetricFetcher(this.controller, this.lu_series.get(id), id), 0, 1, TimeUnit.SECONDS));
             if (DEBUG) {
               System.err.printf("Schedule MetricFetcher(%s)\n", id);
             }
           } else {
             if (DEBUG) {
               System.err.printf("Cancel MetricFetcher(%s)", id);
             }
             this.cbMetricFetcher.get(id).cancel(true);
           }
         }
  private void initializeCanvas() {
            try {
              this.can_road    = new Canvas(this.window_width, this.window_height);
              this.can_servers = new Canvas(this.window_width, this.window_height);
              this.lbl_fps     = new Label("FPS");
              // Determine pixels-per-coordinate
              this.mbr   = this.controller.queryMBR();
              this.xunit = this.can_road.getWidth() /(double) (this.mbr[1] - this.mbr[0]);
              this.yunit = this.can_road.getHeight()/(double) (this.mbr[3] - this.mbr[2]);
              this.unit  = Math.min(this.xunit, this.yunit);
              // Set map units
              this.muf = new MapUnitsFetcher();
              this.muf.setUnit(this.unit);
              this.muf.setLngMin(this.mbr[0]);
              this.muf.setLatMin(this.mbr[2]);
              // Add canvas to pane
              this.container_canvas_container = new Pane(this.can_road, this.can_servers, this.lbl_fps);
              this.container_canvas.setContent(this.container_canvas_container);
              // Register mouse event handlers
              // (can_servers is on top so it will trap all mouse events)
              this.can_servers.setOnMousePressed((e) -> { actionRecordMousePress(e); });
              this.can_servers.setOnMouseDragged((e) -> { actionTranslateCanvas(e); });
              this.can_servers.setOnScroll((e) -> { actionZoomCanvas(e); });
            } catch (SQLException se) {
              System.err.println("Failed with SQLException");
              Tools.PrintSQLException(se);
              return;
            }
          }
  public void initialize() { }
}