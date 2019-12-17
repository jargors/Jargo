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
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
public class DesktopController {
  private final Color C_ERROR   = Color.RED;
  private final Color C_SUCCESS = Color.GREEN;
  private final Color C_WARN    = Color.YELLOW;
  private final String TITLE_STRING = "Jargo Desktop";
  private final boolean DEBUG =
      "true".equals(System.getProperty("jargors.desktop.debug"));
  @FXML private AnchorPane container_lc_counts;
  @FXML private AnchorPane container_lc_distances;
  @FXML private AnchorPane container_lc_durations;
  @FXML private AnchorPane container_lc_rates;
  @FXML private AnchorPane container_lc_times;
  @FXML private Button btn_client;
  @FXML private Button btn_gtree;
  @FXML private Button btn_load;
  @FXML private Button btn_new;
  @FXML private Button btn_prob;
  @FXML private Button btn_road;
  @FXML private Button btn_startreal;
  @FXML private Button btn_startseq;
  @FXML private Button btn_stop;
  @FXML private Button btn_traffic;
  @FXML private CheckBox chk_countRequestsActive;
  @FXML private CheckBox chk_countRequestsCompleted;
  //@FXML private CheckBox chk_countRequestsFailed;
  @FXML private CheckBox chk_countRequestsQueue;
  @FXML private CheckBox chk_countRequestsViolations;
  @FXML private CheckBox chk_countServersActive;
  @FXML private CheckBox chk_countServersViolations;
  @FXML private CheckBox chk_distanceSavings;
  @FXML private CheckBox chk_requestDetourDistance;
  //@FXML private CheckBox chk_requestDetourDuration;
  @FXML private CheckBox chk_requestDistanceUnassigned;
  @FXML private CheckBox chk_requestPickupDuration;
  @FXML private CheckBox chk_requestTransitDistance;
  @FXML private CheckBox chk_requestTransitDuration;
  @FXML private CheckBox chk_requestTravelDuration;
  @FXML private CheckBox chk_serverCruisingDistance;
  @FXML private CheckBox chk_serverCruisingDuration;
  @FXML private CheckBox chk_serverServiceDistance;
  @FXML private CheckBox chk_serverServiceDuration;
  @FXML private CheckBox chk_serverTravelDistance;
  @FXML private CheckBox chk_serverTravelDuration;
  @FXML private CheckBox chk_serviceRate;
  @FXML private CheckBox chk_timeRequestHandling;
  //@FXML private CheckBox chk_timeServerHandling;
  @FXML private Circle circ_status;
  @FXML private Label lbl_status;
  @FXML private ScrollPane container_canvas;
  @FXML private Tab tab_map;
  @FXML private Tab tab_metrics;
  @FXML private TabPane tabpane;
  @FXML private TextField tf_client;
  @FXML private TextField tf_t0;
  @FXML private TextField tf_t1;
  @FXML private TextField tf_traffic;
  @FXML private VBox vbox_metrics_counts;
  @FXML private VBox vbox_metrics_distances;
  @FXML private VBox vbox_metrics_durations;
  @FXML private VBox vbox_metrics_rates;
  @FXML private VBox vbox_metrics_times;
  private Canvas can_road;
  private Canvas can_servers;
  private Client client = null;
  private Controller controller = null;
  private Label lbl_fps;
  private Pane container_canvas_container;
  private Stage stage;
  private String clientclass = null;
  private String clientjar = null;
  private String db = null;
  private String gtree = null;
  private String prob = null;
  private String road = null;
  private String trafficclass = null;
  private String trafficjar = null;
  private Traffic traffic = null;
  private ImageView logo = null;
  private int access_path = 1;  // 1="New", 2="Load"
  private FetcherOfMapUnits muf = null;
  private GraphicsContext gc = null;
  private Map<String, ScheduledFuture<?>> cbFetcherOfMetrics = null;
  private RendererOfRoads ren_road = null;
  private RendererOfServers ren_servers = null;
  private ScheduledExecutorService exe = null;
  private ScheduledFuture<?> cbFetcherOfLocations = null;
  private ScheduledFuture<?> cbSimulation = null;
  private double unit = 0;
  private double window_height = 0;
  private double window_width = 0;
  private double xunit = 0;
  private double yunit = 0;
  private int t0 = 0;
  private int t1 = 0;
  private int zoom = 1;
  private int[] edges = null;
  private int[] mbr = null;
  private class RendererOfServers extends AnimationTimer {
    private final Color BG = Color.web("0xD7FFFF");
    private final Color SERVER_FILL = Color.web("0x555555");
    private final int SERVER_WIDTH = 5;
    private final int SERVER_HEIGHT = 3;
    private Canvas canvas = null;
    private ConcurrentHashMap<Integer, Integer>  bufidx =
        new ConcurrentHashMap<Integer, Integer>();
    private ConcurrentHashMap<Integer, double[]> buffer =
        new ConcurrentHashMap<Integer, double[]>();
    private FetcherOfMapUnits muf = null;
    private GraphicsContext gc = null;
    private Image image = null;
    private Label lbl_fps = null;
    private boolean isRealtime = false;
    private int framecount = 0;
    private int zoom = 1;
    private long now = 0;
    private long prev = 0;
    public RendererOfServers(
        final GraphicsContext gc, final Label lbl_fps, final boolean isRealtime,
        final FetcherOfMapUnits muf) {
      super();
      this.canvas = gc.getCanvas();
      this.gc = gc;
      this.isRealtime = isRealtime;
      this.lbl_fps = lbl_fps;
      this.muf = muf;
      this.setZoom(1);
    }
    public void setZoom(final int zoom) {
      this.zoom = zoom;
      Rectangle shape = new Rectangle(this.SERVER_WIDTH*zoom, this.SERVER_HEIGHT*zoom);
      shape.setFill(this.SERVER_FILL);
      WritableImage wi = new WritableImage(this.SERVER_WIDTH*zoom, this.SERVER_HEIGHT*zoom);
      SnapshotParameters parameters = new SnapshotParameters();
      shape.snapshot(parameters, wi);
      this.image = wi;
    }
    public void fillBuffer(final int sid, final double[] buffer) {
      if (!this.bufidx.containsKey(sid)) {
        this.bufidx.put(sid, 0);
        this.buffer.put(sid, new double[] { 0,0,0, 0,0,0, 0,0,0 });
      }
      double[] ref = this.buffer.get(sid);
      int i = this.bufidx.get(sid);
      ref[(i + 0)] = this.now;
      ref[(i + 1)] = buffer[1];
      ref[(i + 2)] = buffer[2];
      i = (i + 3) % 9;
      ref[(i + 0)] = (this.now + (buffer[3] - buffer[0])*1_000_000_000);
      ref[(i + 1)] = buffer[4];
      ref[(i + 2)] = buffer[5];
      i = (i + 3) % 9;
      ref[(i + 0)] = (this.now + (buffer[6] - buffer[0])*1_000_000_000);
      ref[(i + 1)] = buffer[7];
      ref[(i + 2)] = buffer[8];
    }
    public void handle(final long now) {
      if (!this.muf.getMapVisible()) {
        return;
      }
      this.now = now;
      // Count FPS
      if (now - prev > 1_000_000_000) {
        this.lbl_fps.setText(String.format("%d",this.framecount));
        prev = now;
        framecount = 0;
      } else {
        framecount++;
      }
      // Draw servers
      this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
      for (final Map.Entry<Integer, Integer> kv : this.bufidx.entrySet()) {
        final int sid = kv.getKey();
        final int i = kv.getValue();
        final double[] buffer = this.buffer.get(sid);
        if (buffer == null) {
          continue;
        }
        final double t1 = buffer[((i + 0) % 9)];
        final double x1 = buffer[((i + 1) % 9)];
        final double y1 = buffer[((i + 2) % 9)];
        final double t2 = buffer[((i + 3) % 9)];
        final double x2 = buffer[((i + 4) % 9)];
        final double y2 = buffer[((i + 5) % 9)];
        double x = 0;
        double y = 0;
        if (this.isRealtime) {
          double delta = ((double) (now - t1)/(t2 - t1));
          if (delta >= 1) {
            this.bufidx.put(sid, (i + 3) % 9);
            delta = 1;
          }
          x = this.muf.getUnit()*(x1 + delta*(x2 - x1));
          y = this.muf.getUnit()*(y1 + delta*(y2 - y1));
        } else {
          x = this.muf.getUnit()*x1;
          y = this.muf.getUnit()*y1;
        }
        this.gc.save();
        double angle = Math.toDegrees(Math.atan2((y2 - y1), (x2 - x1)));
        Rotate r = new Rotate(angle, x, y);
        this.gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        this.gc.drawImage(this.image, x, y);
        this.gc.restore();
      }
    }
  }
  private class RendererOfRoads extends AnimationTimer {
    private long prev = 0;
    private GraphicsContext gc = null;
    private Canvas can_road = null;
    private Controller controller = null;
    private FetcherOfMapUnits muf = null;
    private Traffic traffic = null;
    private int[] edges = null;
    private final Color DEFAULT = Color.BLUE;
    private final Color BG = Color.web("0xD7FFFF");
    private final Color SLOW = Color.web("0xFF0000");
    private final Color MED = Color.web("0xFFD700");
    private final Color FAST = Color.web("0x5FD870");
    private final double LINEWIDTH = 0.3;
    private final long PERIOD = 10000_000_000L;  // 10 seconds
    public RendererOfRoads(
        final GraphicsContext gc,
        final Controller controller,
        final FetcherOfMapUnits muf) {
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
      this.gc.setLineWidth(LINEWIDTH);
    }
    public void setTraffic(final Traffic traffic) {
      this.traffic = traffic;
    }
    public void forceRender() {
      this.prev = 0;
    }
    public void handle(long now) {
      if (!this.muf.getMapVisible()) {
        return;
      }
      if ((now - this.prev) > PERIOD) {
        this.prev = now;
        // It is WAY faster to loop through and draw all the edges inside
        // handle(1) instead of drawing a single edge at a time inside handle(1).
        // My guess is because with the loop method, the internal graphics buffer
        // needs to be rendered to the screen only once for all edges but with
        // the single-edge method, it needs to be rendered once for each edge.
        this.gc.clearRect(0, 0, this.can_road.getWidth(), this.can_road.getHeight());
        for (int i = 0; i < (this.edges.length - 3); i += 4) {
          if (this.edges[(i + 0)] != 0 && this.edges[(i + 1)] != 0) {
            try {
              final int[] v1 = this.controller.queryVertex(this.edges[(i + 0)]);
              final int[] v2 = this.controller.queryVertex(this.edges[(i + 1)]);
              final double x1 = this.muf.getUnit()*(v1[0] - this.muf.getLngMin());
              final double x2 = this.muf.getUnit()*(v2[0] - this.muf.getLngMin());
              final double y1 = this.muf.getUnit()*(v1[1] - this.muf.getLatMin());
              final double y2 = this.muf.getUnit()*(v2[1] - this.muf.getLatMin());
              this.gc.setStroke(DEFAULT);
              if (this.traffic != null) {
                double x = this.traffic.apply(this.edges[i], this.edges[(i + 1)], this.controller.getClock());
                if (0.0 <= x && x <= 0.33) {
                  this.gc.setStroke(SLOW);
                } else if (0.33 < x && x <= 0.66) {
                  this.gc.setStroke(MED);
                } else if (0.66 < x && x <= 1.0) {
                  this.gc.setStroke(FAST);
                }
              }
              // It seems to be less laggy if we call strokeLine(4) right here
              // compared to using Platform.runLater(1). Not sure if
              // strokePolyline(3) would be even faster, but we can't use
              // strokePolyline(3) anyway because each of our lines might be a
              // different color depending on traffic.
              this.gc.strokeLine(x1, y1, x2, y2);
            } catch (VertexNotFoundException ve) {
              System.err.println("Warning: "+ve.toString());
            } catch (SQLException se) {
              System.err.println("Warning: FetcherOfLocations failed to get locations");
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
  private class FetcherOfMapUnits {
    private double unit = 0;
    private int lng_min = 0;
    private int lat_min = 0;
    private boolean mapVisible = true;
    public FetcherOfMapUnits() { }
    public double getUnit() {
      return this.unit;
    }
    public int getLngMin() {
      return this.lng_min;
    }
    public int getLatMin() {
      return this.lat_min;
    }
    public boolean getMapVisible() {
      return this.mapVisible;
    }
    public void setUnit(final double unit) {
      this.unit = unit;
    }
    public void setLngMin(final int lng_min) {
      this.lng_min = lng_min;
    }
    public void setLatMin(final int lat_min) {
      this.lat_min = lat_min;
    }
    public void setMapVisible(final boolean flag) {
      this.mapVisible = flag;
    }
  }
  private class FetcherOfLocations implements Runnable {
    private Controller controller = null;
    private Label lbl_status = null;
    private FetcherOfMapUnits muf = null;
    private Map<Integer, double[]> buffer = new HashMap<Integer, double[]>();
    private RendererOfServers renderer = null;
    public FetcherOfLocations(
        final Controller controller,
        final Label lbl_status,
        final FetcherOfMapUnits muf,
        final RendererOfServers renderer) {
      this.controller = controller;
      this.lbl_status = lbl_status;
      this.muf = muf;
      this.renderer = renderer;
    }
    public void run() {
      if (!this.muf.getMapVisible()) {
        return;
      }
      final int t = this.controller.getClock();
      try {
        int[] active = this.controller.queryServersActive(t);
        for (int i = 0; i < active.length; i++) {
          final int sid = active[i];
          if ((!this.buffer.containsKey(sid)) || (this.buffer.get(sid)[3] <= t)) {
            int[] route = this.controller.queryServerRouteActive(sid);
            if (route.length == 6) {  // ...
              final int t1 = route[0];
              final int v1 = route[1];
              final int t2 = route[2];
              final int v2 = route[3];
              final int t3 = route[4];
              final int v3 = route[5];
              int[] coordinates = this.controller.queryVertex(v1);
              final double x1 = (coordinates[0] - this.muf.getLngMin());
              final double y1 = (coordinates[1] - this.muf.getLatMin());
              coordinates = this.controller.queryVertex(v2);
              final double x2 = (coordinates[0] - this.muf.getLngMin());
              final double y2 = (coordinates[1] - this.muf.getLatMin());
              coordinates = this.controller.queryVertex(v3);
              final double x3 = (coordinates[0] - this.muf.getLngMin());
              final double y3 = (coordinates[1] - this.muf.getLatMin());
              double[] newbuf = new double[] { t1, x1, y1, t2, x2, y2, t3, x3, y3 };
              this.buffer.put(sid, newbuf);
              this.renderer.fillBuffer(sid, newbuf);
            }
          }
        }
      } catch (SQLException se) {
        System.err.println("Warning: FetcherOfLocations failed to get locations");
        System.err.println(se.toString());
        se.printStackTrace();
      } catch (VertexNotFoundException ve) {
        System.err.println("Warning: FetcherOfLocations got unknown location!");
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
  private class FetcherOfMetrics implements Runnable {
    private Controller controller = null;
    private ConcurrentHashMap<String, Series<Number, Number>> series = null;
    private String id = "";
    private long A0 = 0;
    public FetcherOfMetrics(
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
      final int t = this.controller.getClock();
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
          int[] output = this.controller.queryMetricServerDistanceServiceTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverCruisingDistance".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDistanceCruisingTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverTravelDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDurationTravelTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverServiceDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDurationServiceTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_serverCruisingDuration".equals(this.id)) {
          int[] output = this.controller.queryMetricServerDurationCruisingTotal();
          val = (output.length > 0 ? output[0] : 0);
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
          int[] output = this.controller.queryRequestsCountActive(t);
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_countRequestsCompleted".equals(this.id)) {
          int[] output = this.controller.queryRequestsCountCompleted(t);
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_countRequestsFailed".equals(this.id)) {
          
        } else if ("chk_countServersActive".equals(this.id)) {
          int[] output = this.controller.queryServersCountActive(t);
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_countRequestsViolations".equals(this.id)) {
          int[] output = this.controller.queryMetricRequestTWViolationsTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_countServersViolations".equals(this.id)) {
          int[] output = this.controller.queryMetricServerTWViolationsTotal();
          val = (output.length > 0 ? output[0] : 0);
        } else if ("chk_timeRequestHandling".equals(this.id)) {
          val = this.controller.retrieveHandleRequestDur();
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
           Alert alert = new Alert(AlertType.INFORMATION, "https:github.com/jargors");
           alert.setTitle("About");
           alert.setHeaderText("Jargo Desktop v1.0.0");
           alert.setGraphic(this.logo);
           alert.showAndWait();
         }
  public void actionClient(final ActionEvent e) {
           this.btn_client   .setDisable(true);
           this.tf_client    .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.jar...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Client *.jar", "*.jar"));
           File cj = fc.showOpenDialog(this.stage);
           if (cj != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
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
                 Platform.runLater(() -> {
                   this.circ_status  .setFill(C_ERROR);
                   this.lbl_status   .setText("Bad jar!");
                   Alert alert = new Alert(AlertType.ERROR, "Couldn't load client!");
                   alert.showAndWait();
                   this.btn_client   .setDisable(false);
                   this.tf_client    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Ready.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
                 return;
               }
               Platform.runLater(() -> {
                 this.clientclass = classNames.get(0);
                 this.btn_client   .setText(cj.getName());
                 this.tf_client    .setText(this.clientclass);
                 this.tf_client    .setDisable(false);
                 this.btn_traffic  .setDisable(false);
                 this.tf_traffic   .setDisable(false);
                 this.tf_t0        .setDisable(false);
                 this.tf_t1        .setDisable(false);
                 this.btn_startseq .setDisable(false);
                 this.btn_startreal.setDisable(false);
                 this.btn_stop     .setDisable(false);
                 this.tabpane      .setDisable(false);
                 this.circ_status  .setFill(C_SUCCESS);
                 this.lbl_status   .setText("Loaded "+cj.getName());
               });
               Platform.runLater(() -> {
                 this.stage.getScene().setCursor(Cursor.DEFAULT);
               });
             } catch (IOException ie) {
               Platform.runLater(() -> {
                 this.circ_status  .setFill(C_ERROR);
                 this.lbl_status   .setText("Bad jar!");
                 Alert alert = new Alert(AlertType.ERROR, "Couldn't load client!");
                 alert.showAndWait();
                 this.btn_client   .setDisable(false);
                 this.tf_client    .setDisable(false);
                 this.btn_stop     .setDisable(false);
                 this.tabpane      .setDisable(false);
                 this.circ_status  .setFill(C_SUCCESS);
                 this.lbl_status   .setText("Ready.");
               });
               Platform.runLater(() -> {
                 this.stage.getScene().setCursor(Cursor.DEFAULT);
               });
             }
           } else {
             // FD cancelled
             this.btn_client   .setDisable(false);
             this.tf_client    .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionGitHub(final ActionEvent e) {
           // ...
         }
  public void actionGtree(final ActionEvent e) {
           this.btn_gtree    .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.gtree...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("G-tree *.gtree", "*.gtree"));
           File gt = fc.showOpenDialog(this.stage);
           if (gt != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
             this.gtree = gt.toString();
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Load '"+this.gtree+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.gtreeLoad(this.gtree);
                 Platform.runLater(() -> {
                   if (this.access_path == 1) {
                     this.btn_prob   .setDisable(false);
                   } else if (this.access_path == 2) {
                     this.btn_client .setDisable(false);
                     this.tf_client  .setDisable(false);
                   }
                   this.btn_gtree    .setText(gt.getName());
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+gt.getName());
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               } catch (Exception ee) {
                 if (DEBUG) {
                   System.err.println("Failed: "+ee.toString());
                 }
                 Platform.runLater(() -> {
                   this.circ_status  .setFill(C_ERROR);
                   this.lbl_status   .setText("Failed to load G-tree!");
                   Alert alert = new Alert(AlertType.ERROR, "Couldn't load G-tree!");
                   alert.showAndWait();
                   this.btn_gtree    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Ready.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               }
             });
           } else {
             // FD cancelled
             this.btn_gtree    .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionLoad(final ActionEvent e) {
           this.btn_new      .setDisable(true);
           this.btn_load     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           DirectoryChooser dc = new DirectoryChooser();
           File db = dc.showDialog(this.stage);
           if (db != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
             this.db = db.toString();
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Load '"+this.db+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller = new Controller();
                 this.controller.instanceLoadInMem(this.db);
                 this.controller.cacheRoadNetworkFromDB();
                 this.controller.cacheUsersFromDB();
                 int nv = this.controller.queryVerticesCount()[0];
                 int ne = this.controller.queryEdgesCount()[0];
                 int ns = this.controller.queryServersCount()[0];
                 int nr = this.controller.queryRequestsCount()[0];
                 Platform.runLater(() -> {
                   this.access_path = 2;
                   this.btn_prob     .setText("*in-instance problem*");
                   this.prob = "*in-instance problem*";
                   this.btn_road     .setText("*in-instance road network*");
                   this.road = "*in-instance road network*";
                   this.btn_gtree    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded Jargo instance (#vertices="+nv+"; #edges="+ne+") (#servers="+ns+"; #requests="+nr+")");
                   this.container_canvas.setContent(null);
                   this.container_lc_rates.getChildren().clear();
                   this.container_lc_distances.getChildren().clear();
                   this.container_lc_durations.getChildren().clear();
                   this.initializeCanvas();
                   this.ren_road = new RendererOfRoads(this.can_road.getGraphicsContext2D(), this.controller, this.muf);
                   this.ren_road.start();
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               } catch (SQLException se) {
                 if (DEBUG) {
                   Tools.PrintSQLException(se);
                 }
                 Platform.runLater(() -> {
                   this.circ_status  .setFill(C_ERROR);
                   this.lbl_status   .setText("Failed to load snapshot!");
                   Alert alert = new Alert(AlertType.ERROR, "Couldn't load snapshot! (Not a valid Jargo instance?)");
                   alert.showAndWait();
                   this.btn_new      .setDisable(false);
                   this.btn_load     .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Ready.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               }
             });
           } else {
             // FD canceled
             this.btn_new      .setDisable(false);
             this.btn_load     .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionNew(final ActionEvent e) {
           Platform.runLater(() -> {
             this.stage.getScene().setCursor(Cursor.WAIT);
           });
           this.btn_new      .setDisable(true);
           this.btn_load     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.circ_status.setFill(C_WARN);
           this.lbl_status.setText("Create new Jargo instance...");
           CompletableFuture.runAsync(() -> {
             this.controller = new Controller();
             try {
               this.controller.instanceNew();
             } catch (SQLException se) {
               Alert alert = new Alert(AlertType.ERROR, se.getMessage());
               alert.showAndWait();
               System.exit(1);
             }
             this.controller.instanceInitialize();
             this.db = "no-name";
             Platform.runLater(() -> {
               this.access_path = 1;
               this.btn_road     .setDisable(false);
               this.btn_stop     .setDisable(false);
               this.tabpane      .setDisable(false);
               this.container_canvas.setContent(null);
               this.container_lc_rates.getChildren().clear();
               this.container_lc_distances.getChildren().clear();
               this.container_lc_durations.getChildren().clear();
               this.circ_status.setFill(C_SUCCESS);
               this.lbl_status.setText("Created new Jargo instance.");
             });
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.DEFAULT);
             });
           });
         }
  public void actionProb(final ActionEvent e) {
           this.btn_prob     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.instance...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Problem Instance *.instance", "*.instance"));
           File pb = fc.showOpenDialog(this.stage);
           if (pb != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
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
                   this.tf_client    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+pb.getName()+"(#servers="+ns+"; #requests="+nr+")");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               } catch (Exception ee) {
                 if (DEBUG) {
                   System.err.println(ee.toString());
                 }
                 Platform.runLater(() -> {
                   this.circ_status  .setFill(C_ERROR);
                   this.lbl_status   .setText("Failed to load problem!");
                   Alert alert = new Alert(AlertType.ERROR, "Couldn't load problem! (Not a valid Jargo instance?)");
                   alert.showAndWait();
                   this.btn_prob     .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Ready.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               }
             });
           } else {
             // FD cancelled
             this.btn_prob     .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionQuit(final ActionEvent e) {
           System.exit(0);
         }
  public void actionRoad(final ActionEvent e) {
           this.btn_road     .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.rnet...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Road network *.rnet", "*.rnet"));
           File road = fc.showOpenDialog(this.stage);
           if (road != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
             this.road = road.toString();
             this.lbl_status.setText("Load '"+this.road+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadRoadNetworkFromFile(this.road);
                 int nv = this.controller.queryVerticesCount()[0];
                 int ne = this.controller.queryEdgesCount()[0];
                 Platform.runLater(() -> {
                   this.btn_road     .setText(road.getName());
                   this.btn_stop     .setDisable(false);
                   this.btn_gtree    .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+road.getName()+" (#vertices="+nv+"; #edges="+ne+")");
                   this.initializeCanvas();
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                   this.ren_road = new RendererOfRoads(this.can_road.getGraphicsContext2D(), this.controller, this.muf);
                   this.ren_road.start();
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               } catch (Exception ee) {
                 if (DEBUG) {
                   System.err.println("Failed: "+ee.toString());
                 }
                 this.circ_status  .setFill(C_ERROR);
                 this.lbl_status   .setText("Failed to load road network!");
                 Alert alert = new Alert(AlertType.ERROR, "Couldn't load road network! (Not a valid Jargo *.rnet?)");
                 alert.showAndWait();
                 this.btn_road     .setDisable(false);
                 this.btn_stop     .setDisable(false);
                 this.tabpane      .setDisable(false);
                 this.circ_status  .setFill(C_SUCCESS);
                 this.lbl_status   .setText("Ready.");
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               }
             });
           } else {
             // FD cancelled
             this.btn_road     .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionStartRealtime(final ActionEvent e) {
           this.clientclass = this.tf_client.getText();
           if ("".equals(this.clientclass)) {
             System.err.println("Class empty!");
             return;
           }
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
           this.trafficclass = this.tf_traffic.getText();
           try {
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
           this.ren_servers = new RendererOfServers(this.can_servers.getGraphicsContext2D(), this.lbl_fps, true, this.muf);
           this.ren_servers.start();
           this.circ_status  .setFill(C_SUCCESS);
           this.lbl_status   .setText("Simulation started.");
           this.exe = Executors.newScheduledThreadPool(2);
           this.cbSimulation = this.exe.schedule(() -> {
             try {
               this.controller.startRealtime((status) -> {
                 Platform.runLater(() -> {
                   this.lbl_status.setText("Simulation "+(status ? "ended." : "failed."));
                 });
               });
             } catch (Exception ee) {
               System.err.println("Unexepected error in startRealtime");
               ee.printStackTrace();
               System.exit(1);
             }
           }, 0, TimeUnit.SECONDS);
           this.cbFetcherOfLocations = this.exe.scheduleAtFixedRate(
               new FetcherOfLocations(
                 this.controller, this.lbl_status, this.muf, this.ren_servers), 0, 1, TimeUnit.SECONDS);
         }
  public void actionStartSequential(final ActionEvent e) {
           this.clientclass = this.tf_client.getText();
           if ("".equals(this.clientclass)) {
             System.err.println("Class empty!");
             return;
           }
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
           this.trafficclass = this.tf_traffic.getText();
           try {
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
           this.ren_servers = new RendererOfServers(this.can_servers.getGraphicsContext2D(), this.lbl_fps, false, this.muf);
           this.ren_servers.start();
           this.circ_status  .setFill(C_SUCCESS);
           this.lbl_status   .setText("Simulation started.");
           this.exe = Executors.newScheduledThreadPool(2);
           this.cbSimulation = this.exe.schedule(() -> {
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
           this.cbFetcherOfLocations = this.exe.scheduleAtFixedRate(
               new FetcherOfLocations(
                 this.controller, this.lbl_status, this.muf, this.ren_servers), 0, 1, TimeUnit.SECONDS);
         }
  public void actionStop(final ActionEvent e) {
           Platform.runLater(() -> {
             this.stage.getScene().setCursor(Cursor.WAIT);
           });
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
             //this.chk_requestDetourDuration.setSelected(false);
             this.chk_requestTravelDuration.setSelected(false);
             this.chk_requestPickupDuration.setSelected(false);
             this.chk_countRequestsQueue.setSelected(false);
             this.chk_countRequestsActive.setSelected(false);
             this.chk_countRequestsCompleted.setSelected(false);
             //this.chk_countRequestsFailed.setSelected(false);
             this.chk_countServersActive.setSelected(false);
             this.chk_countRequestsViolations.setSelected(false);
             this.chk_countServersViolations.setSelected(false);
             this.chk_timeRequestHandling.setSelected(false);
             //this.chk_timeServerHandling.setSelected(false);
             if (this.ren_road != null) {
               this.ren_road.stop();
             }
             if (this.ren_servers != null) {
               this.ren_servers.stop();
             }
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
                   this.rates_x = new NumberAxis();
                   this.rates_y = new NumberAxis();
                   this.distances_x = new NumberAxis();
                   this.distances_y = new NumberAxis();
                   this.durations_x = new NumberAxis();
                   this.durations_y = new NumberAxis();
                   this.counts_x = new NumberAxis();
                   this.counts_y = new NumberAxis();
                   this.times_x = new NumberAxis();
                   this.times_y = new NumberAxis();
                   this.lc_rates = new LineChart<Number, Number>(rates_x, rates_y);
                   this.lc_distances = new LineChart<Number, Number>(distances_x, distances_y);
                   this.lc_durations = new LineChart<Number, Number>(durations_x, durations_y);
                   this.lc_counts = new LineChart<Number, Number>(counts_x, counts_y);
                   this.lc_times = new LineChart<Number, Number>(times_x, times_y);
                   this.lc_rates_series = new ConcurrentHashMap<String, Series<Number, Number>>();
                   this.lc_distances_series = new ConcurrentHashMap<String, Series<Number, Number>>();
                   this.lc_durations_series = new ConcurrentHashMap<String, Series<Number, Number>>();
                   this.lc_counts_series = new ConcurrentHashMap<String, Series<Number, Number>>();
                   this.lc_times_series = new ConcurrentHashMap<String, Series<Number, Number>>();
                   this.lu_series = new ConcurrentHashMap<String, ConcurrentHashMap<String, Series<Number, Number>>>();
                   this.circ_status.setFill(C_SUCCESS);
                   this.lbl_status.setText("Closed instance.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
               } catch (SQLException se) {
                 System.err.println("Failure");
                 System.exit(1);
               }
             });
           } else {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.DEFAULT);
             });
           }
         }
  public void actionTraffic(final ActionEvent e) {
           this.btn_traffic  .setDisable(true);
           this.tf_traffic   .setDisable(true);
           this.tf_client    .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tabpane      .setDisable(true);
           this.tf_t0        .setDisable(true);
           this.tf_t1        .setDisable(true);
           this.btn_startseq .setDisable(true);
           this.btn_startreal.setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.jar...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Traffic *.jar", "*.jar"));
           File cj = fc.showOpenDialog(this.stage);
           if (cj != null) {
             Platform.runLater(() -> {
               this.stage.getScene().setCursor(Cursor.WAIT);
             });
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
                 Platform.runLater(() -> {
                   this.circ_status  .setFill(C_ERROR);
                   this.lbl_status   .setText("Bad jar!");
                   Alert alert = new Alert(AlertType.ERROR, "Couldn't load traffic!");
                   alert.showAndWait();
                   this.btn_traffic  .setDisable(false);
                   this.tf_traffic   .setDisable(false);
                   this.tf_client    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tabpane      .setDisable(false);
                   this.tf_t0        .setDisable(false);
                   this.tf_t1        .setDisable(false);
                   this.btn_startseq .setDisable(false);
                   this.btn_startreal.setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Ready.");
                 });
                 Platform.runLater(() -> {
                   this.stage.getScene().setCursor(Cursor.DEFAULT);
                 });
                 return;
               }
               Platform.runLater(() -> {
                 this.trafficclass = classNames.get(0);
                 this.tf_traffic   .setText(this.trafficclass);
                 this.btn_traffic  .setText(cj.getName());
                 this.tf_traffic   .setDisable(false);
                 this.tf_client    .setDisable(false);
                 this.btn_stop     .setDisable(false);
                 this.tabpane      .setDisable(false);
                 this.tf_t0        .setDisable(false);
                 this.tf_t1        .setDisable(false);
                 this.btn_startseq .setDisable(false);
                 this.btn_startreal.setDisable(false);
                 this.circ_status  .setFill(C_SUCCESS);
                 this.lbl_status   .setText("Loaded "+cj.getName());
               });
               Platform.runLater(() -> {
                 this.stage.getScene().setCursor(Cursor.DEFAULT);
               });
             } catch (IOException ie) {
               Platform.runLater(() -> {
                 this.circ_status  .setFill(C_ERROR);
                 this.lbl_status   .setText("Bad jar!");
                 Alert alert = new Alert(AlertType.ERROR, "Couldn't load traffic!");
                 alert.showAndWait();
                 this.btn_traffic  .setDisable(false);
                 this.tf_traffic   .setDisable(false);
                 this.tf_client    .setDisable(false);
                 this.btn_stop     .setDisable(false);
                 this.tabpane      .setDisable(false);
                 this.tf_t0        .setDisable(false);
                 this.tf_t1        .setDisable(false);
                 this.btn_startseq .setDisable(false);
                 this.btn_startreal.setDisable(false);
                 this.circ_status  .setFill(C_SUCCESS);
                 this.lbl_status   .setText("Ready.");
               });
               Platform.runLater(() -> {
                 this.stage.getScene().setCursor(Cursor.DEFAULT);
               });
             }
           } else {
             // FD cancelled
             this.btn_traffic  .setDisable(false);
             this.tf_traffic   .setDisable(false);
             this.tf_client    .setDisable(false);
             this.btn_stop     .setDisable(false);
             this.tabpane      .setDisable(false);
             this.tf_t0        .setDisable(false);
             this.tf_t1        .setDisable(false);
             this.btn_startseq .setDisable(false);
             this.btn_startreal.setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Ready.");
           }
         }
  public void actionZoomCanvas(ScrollEvent e) {
           if (e.getDeltaY() > 0) {
             this.zoom += 1;
           }
           if (e.getDeltaY() < 0) {
             this.zoom -= 1;
           }
           this.zoom = Math.max(this.zoom, 1);
           this.zoom = Math.min(this.zoom, 5);
           this.muf.setUnit(this.unit*this.zoom);
           this.can_road.setWidth(this.window_width*this.zoom);
           this.can_road.setHeight(this.window_height*this.zoom);
           this.can_servers.setWidth(this.window_width*this.zoom);
           this.can_servers.setHeight(this.window_height*this.zoom);
           if (this.ren_road != null) {
             this.ren_road.forceRender();
           }
           if (this.ren_servers != null) {
             this.ren_servers.setZoom(this.zoom);
           }
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
             this.cbFetcherOfMetrics.put(id, this.exe.scheduleAtFixedRate(
                 new FetcherOfMetrics(this.controller, this.lu_series.get(id), id), 0, 1, TimeUnit.SECONDS));
             if (DEBUG) {
               System.err.printf("Schedule FetcherOfMetrics(%s)\n", id);
             }
           } else {
             if (DEBUG) {
               System.err.printf("Cancel FetcherOfMetrics(%s)", id);
             }
             this.cbFetcherOfMetrics.get(id).cancel(true);
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
              this.muf = new FetcherOfMapUnits();
              this.muf.setUnit(this.unit);
              this.muf.setLngMin(this.mbr[0]);
              this.muf.setLatMin(this.mbr[2]);
              // Add canvas to pane
              this.container_canvas_container = new Pane(this.can_road, this.can_servers, this.lbl_fps);
              this.container_canvas.setContent(this.container_canvas_container);
              // Register mouse event handlers
              // (can_servers is on top so it will trap all mouse events)
              this.can_servers.setOnScroll((e) -> { actionZoomCanvas(e); });
            } catch (SQLException se) {
              System.err.println("Failed with SQLException");
              Tools.PrintSQLException(se);
              return;
            }
          }
  public void initialize() {
    this.tabpane.getSelectionModel().selectedIndexProperty().addListener(
        (ov, oldTab, newTab) -> {
      if (this.muf != null) {
        this.muf.setMapVisible((newTab.intValue() == 0 ? true : false));
      }
    });
    this.cbFetcherOfMetrics = new HashMap<String, ScheduledFuture<?>>();
    Image image = new Image("res/icon.gif");
    this.logo = new ImageView();
    this.logo.setImage(image);
    this.logo.setFitWidth(64);
    this.logo.setPreserveRatio(true);
    this.logo.setSmooth(true);
    this.logo.setCache(true);
  }
}
