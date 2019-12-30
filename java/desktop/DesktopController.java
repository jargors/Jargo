package com.github.jargors.desktop;
import com.github.jargors.Client;
import com.github.jargors.Controller;
import com.github.jargors.Tools;
import com.github.jargors.Traffic;
import com.github.jargors.exceptions.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
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
  private Canvas can_road;
  private Canvas can_servers;
  private Canvas can_requests;
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
  private ScheduledFuture<?> cbFetcherOfMetrics = null;
  private RendererOfRoads ren_road = null;
  private RendererOfServers ren_servers = null;
  private RendererOfRequests ren_requests = null;
  private ScheduledExecutorService exe = null;
  private ScheduledFuture<?> cbFetcherOfLocations = null;
  private ScheduledFuture<?> cbFetcherOfRequests = null;
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
  private int ns = 0;
  private int nr = 0;
  private class RendererOfRequests extends AnimationTimer {
    private final Color BG = Color.web("0xD7FFFF");
    private final Color REQUEST_FILL = Color.web("0xff5500");
    private final int REQUEST_WIDTH = 2;
    private final int REQUEST_HEIGHT = 2;
    private Canvas canvas = null;
    private ConcurrentHashMap<Integer, double[]> buffer =
        new ConcurrentHashMap<Integer, double[]>();
    private final ConcurrentHashMap<Integer, Image> bufimg =
        new ConcurrentHashMap<Integer, Image>();
    private FetcherOfMapUnits muf = null;
    private GraphicsContext gc = null;
    private Image image = null;
    private int zoom = 1;
    private long now = 0;
    private long prev = 0;
    public RendererOfRequests(final GraphicsContext gc, final FetcherOfMapUnits muf) {
      super();
      this.canvas = gc.getCanvas();
      this.gc = gc;
      this.gc.setStroke(REQUEST_FILL);
      this.muf = muf;
      this.setZoom(1);
      for (int i = 0; i <= 10; i++) {
        WritableImage wi = new WritableImage(80, 14);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(new Color(0, 0, 0, 0));
        Text txt = new Text(i == 10 ? "R" : ""+i);
        txt.setStroke(REQUEST_FILL);
        txt.snapshot(parameters, wi);
        this.bufimg.put(i, wi);
      }
    }
    public void setZoom(final int zoom) {
      this.zoom = zoom;
      Rectangle shape = new Rectangle(this.REQUEST_WIDTH*zoom, this.REQUEST_HEIGHT*zoom);
      shape.setFill(this.REQUEST_FILL);
      WritableImage wi = new WritableImage(this.REQUEST_WIDTH*zoom, this.REQUEST_HEIGHT*zoom);
      SnapshotParameters parameters = new SnapshotParameters();
      shape.snapshot(parameters, wi);
      this.image = wi;
    }
    public void fillBuffer(final int rid, final double[] buffer) {
      this.buffer.put(rid, buffer);
    }
    public void clearBuffer() {
      this.buffer.clear();
    }
    public void handle(final long now) {
      if (!this.muf.getMapVisible()) {
        return;
      }
      if (now - prev > 500_000_000) {  // render every 0.5 sec
        prev = now;
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        for (final Map.Entry<Integer, double[]> kv : this.buffer.entrySet()) {
          final int rid = kv.getKey();
          final double[] buffer = kv.getValue();
          final double x = buffer[0]*this.muf.getUnit();
          final double y = this.canvas.getHeight() - buffer[1]*this.muf.getUnit();
          this.gc.drawImage(this.image, x, y);
          // this.gc.strokeText("R"+rid, x, y);
          int uid = rid;
          char[] digits = String.valueOf(uid).toCharArray();
          this.gc.drawImage(this.bufimg.get(10), x, y);
          for (int j = 0; j < digits.length; j++) {
            this.gc.drawImage(
              /*image*/this.bufimg.get(Character.getNumericValue(digits[j])),
              /*position*/(x + 8*(j+1)), y);
          }
        }
      }
    }
  }
  private class RendererOfServers extends AnimationTimer {
    private final Color BG = Color.web("0xD7FFFF");
    private final Color SERVER_FILL = Color.web("0x555555");
    private final int SERVER_WIDTH = 5;
    private final int SERVER_HEIGHT = 3;
    private Canvas canvas = null;
    private final ConcurrentHashMap<Integer, Integer>  bufidx =
        new ConcurrentHashMap<Integer, Integer>();
    private final ConcurrentHashMap<Integer, double[]> buffer =
        new ConcurrentHashMap<Integer, double[]>();
    private final ConcurrentHashMap<Integer, Image> bufimg =
        new ConcurrentHashMap<Integer, Image>();
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
      this.gc.setStroke(SERVER_FILL);
      this.isRealtime = isRealtime;
      this.lbl_fps = lbl_fps;
      this.muf = muf;
      this.setZoom(1);
      for (int i = 0; i <= 10; i++) {
        WritableImage wi = new WritableImage(80, 14);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(new Color(0, 0, 0, 0));
        Text txt = new Text(i == 10 ? "S" : ""+i);
        txt.setStroke(SERVER_FILL);
        txt.snapshot(parameters, wi);
        this.bufimg.put(i, wi);
      }
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
  /**
  if (sid == 1) System.out.printf("%.2f %.2f %.2f\n%.2f %.2f %.2f\n%.2f %.2f %.2f\n",
      buffer[0],
      buffer[1],
      buffer[2],
      buffer[3],
      buffer[4],
      buffer[5],
      buffer[6],
      buffer[7],
      buffer[8]);
  **/
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
          } else if (delta >= 0) {
            x = this.muf.getUnit()*(x1 + delta*(x2 - x1));
            y = this.canvas.getHeight() - this.muf.getUnit()*(y1 + delta*(y2 - y1));
          } else {
            // delta < 0 means we didn't get a buffer update
          }
  /**
  if (sid == 1) System.out.printf("S%d: (%.2f,%.2f) (%.2f,%.2f) bufidx=%d, delta=%.2f\n",
      sid, x1, y1, x2, y2, i, delta);
  **/
        } else {
          x = this.muf.getUnit()*x1;
          y = this.canvas.getHeight() - this.muf.getUnit()*y1;
        }
        this.gc.save();
        double angle = (360 - Math.toDegrees(Math.atan2((y2 - y1), (x2 - x1))));
        Rotate r = new Rotate(angle, x, y);
        this.gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        this.gc.drawImage(this.image, x, y);
        this.gc.restore();
        //this.gc.strokeText("S"+sid, x, y);
        int uid = sid;
        char[] digits = String.valueOf(uid).toCharArray();
        this.gc.drawImage(this.bufimg.get(10), x, y);
        for (int j = 0; j < digits.length; j++) {
          this.gc.drawImage(
            /*image*/this.bufimg.get(Character.getNumericValue(digits[j])),
            /*position*/(x + 8*(j+1)), y);
        }
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
              final double y1 = this.can_road.getHeight() - this.muf.getUnit()*(v1[1] - this.muf.getLatMin());
              final double y2 = this.can_road.getHeight() - this.muf.getUnit()*(v2[1] - this.muf.getLatMin());
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
    private int lng_max = 0;
    private int lat_max = 0;
    private boolean mapVisible = true;
    public FetcherOfMapUnits() { }
    public double getUnit() {
      return this.unit;
    }
    public int getLngMin() {
      return this.lng_min;
    }
    public int getLngMax() {
      return this.lng_max;
    }
    public int getLatMin() {
      return this.lat_min;
    }
    public int getLatMax() {
      return this.lat_max;
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
    public void setLngMax(final int lng_max) {
      this.lng_max = lng_max;
    }
    public void setLatMax(final int lat_max) {
      this.lat_max = lat_max;
    }
    public void setMapVisible(final boolean flag) {
      this.mapVisible = flag;
    }
  }
  private class FetcherOfRequests implements Runnable {
    private Controller controller = null;
    private FetcherOfMapUnits muf = null;
    private RendererOfRequests renderer = null;
    public FetcherOfRequests(
        final Controller controller,
        final FetcherOfMapUnits muf,
        final RendererOfRequests renderer) {
      this.controller = controller;
      this.muf = muf;
      this.renderer = renderer;
    }
    public void run() {
      if (!this.muf.getMapVisible()) {
        return;
      }
      final int t = this.controller.getClock();
      try {
        this.renderer.clearBuffer();
        int[] waiting = this.controller.queryRequestsWaiting(t);
        for (int i = 0; i < (waiting.length - 1); i += 2) {
          final int rid = waiting[(i + 0)];
          final int  ro = waiting[(i + 1)];
          int[] coordinates = this.controller.queryVertex(ro);
          final double x = (coordinates[0] - this.muf.getLngMin());
          final double y = (coordinates[1] - this.muf.getLatMin());
          this.renderer.fillBuffer(rid, new double[] { x, y });
        }
      } catch (SQLException se) {
        System.err.println("Warning: FetcherOfRequests failed to get requests");
        System.err.println(se.toString());
        se.printStackTrace();
      } catch (VertexNotFoundException ve) {
        System.err.println("Warning: FetcherOfRequests got unknown location!");
        System.err.println(ve.toString());
        ve.printStackTrace();
      } catch (Exception ee) {
        ee.printStackTrace();
      }
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
  if (this.buffer.containsKey(sid) && t1 == this.buffer.get(sid)[0]) {
    // we didn't get an update
    continue;
  }
  /**
  if (sid == 1) System.out.printf("%d %d\n%d %d\n%d %d\n",
      route[0],
      route[1],
      route[2],
      route[3],
      route[4],
      route[5]);
  **/
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
    }
  }
  private class FetcherOfMetrics implements Runnable {
    private Controller controller = null;
    private ConcurrentHashMap<String, SimpleXYChartSupport> lu_series = null;
    private long A0 = 0;
    private long t_ref = 0;
    private int ns_total = 0;
    private int nr_total = 0;
    private int ns = 0;
    private int nr = 0;
    public FetcherOfMetrics(
        final Controller controller,
        final ConcurrentHashMap<String, SimpleXYChartSupport> lu_series,
        final int ns,
        final int nr) {
      this.controller = controller;
      this.lu_series = lu_series;
      this.ns_total = ns;
      this.nr_total = nr;
      SimpleDateFormat sdf = new SimpleDateFormat("hhmm");
      try {
        this.t_ref = sdf.parse(this.controller.getClockReference()).getTime();
      } catch (ParseException ee) {
        ee.printStackTrace();
      }
    }
    public void run() {
      if (DEBUG) {
        this.A0 = System.currentTimeMillis();
      }
      final int t = this.controller.getClock();
      try {
        this.ns = this.controller.queryServersCountAppeared()[0];
        this.nr = this.controller.queryRequestsCountAppeared()[0];
        int[] output = new int[] { };
        Number val = null;
        output = this.controller.queryMetricServiceRateRunning();
        val = (output.length > 0 ? output[0] : 0);               final long y01 = val.longValue();
        output = this.controller.queryMetricServerDistanceRunning();
        final int val1 = (output.length > 0 ? output[0] : 0);
        output = this.controller.queryMetricRequestDistanceBaseUnassignedRunning();
        final int val2 = (output.length > 0 ? output[0] : 0);
        output = this.controller.queryMetricUserDistanceBaseRunning();
        final int val3 = (output.length > 0 ? output[0] : 0);
        val = (val3 == 0 ? 0 : (100.0*100*(1 - ((double) (val1 + val2)/val3))));           final long y02 = val.longValue();
        output = this.controller.queryMetricServerDistanceTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.ns) : 0);      final long y03 = val.longValue();
        output = this.controller.queryMetricServerDistanceServiceTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.ns) : 0);     final long y04 = val.longValue();
        output = this.controller.queryMetricServerDistanceCruisingTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.ns) : 0);    final long y05 = val.longValue();
  //      output = this.controller.queryMetricRequestDistanceBaseUnassignedTotal();
          val = (output.length > 0 ? output[0] : 0); final long y06 = val.longValue();
        output = this.controller.queryMetricRequestDistanceTransitTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.nr) : 0);    final long y07 = val.longValue();
        output = this.controller.queryMetricRequestDistanceDetourTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.nr) : 0);     final long y08 = val.longValue();
        output = this.controller.queryMetricServerDurationTravelTotal();
        val = (output.length > 0 && this.ns > 0
            ? Math.round(Math.max(0, output[0] - this.ns_total)/(double) this.ns) : 0);      final long y09 = val.longValue();
        output = this.controller.queryMetricServerDurationServiceTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.ns) : 0);     final long y10 = val.longValue();
        output = this.controller.queryMetricServerDurationCruisingTotal();
        val = (output.length > 0 && this.ns > 0
            ? Math.round(Math.max(0, output[0] - this.ns_total)/(double) this.ns) : 0);    final long y11 = val.longValue();
        output = this.controller.queryMetricRequestDurationTransitTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.nr) : 0);    final long y12 = val.longValue();
        output = this.controller.queryMetricRequestDurationTravelTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.nr) : 0);     final long y13 = val.longValue();
        output = this.controller.queryMetricRequestDurationPickupTotal();
        val = (output.length > 0 ? Math.round(output[0]/(double) this.nr) : 0);     final long y14 = val.longValue();
        val = this.controller.retrieveQueueSize();        final long y15 = val.longValue();
  //      output = this.controller.queryRequestsCountActive(t);
          val = (output.length > 0 ? output[0] : 0);       final long y16 = val.longValue();
  //      output = this.controller.queryRequestsCountCompleted(t);
          val = (output.length > 0 ? output[0] : 0);    final long y17 = val.longValue();
  //      output = this.controller.queryServersCountActive(t);
          val = (output.length > 0 ? output[0] : 0);        final long y18 = val.longValue();
        output = this.controller.queryMetricRequestTWViolationsTotal();
        val = (output.length > 0 ? output[0] : 0);   final long y19 = val.longValue();
        output = this.controller.queryMetricServerTWViolationsTotal();
        val = (output.length > 0 ? output[0] : 0);    final long y20 = val.longValue();
        val = this.controller.retrieveHandleRequestDur();       final long y21 = val.longValue();
        SwingUtilities.invokeLater(() -> {
           final long tf = (1000*t + t_ref);
           this.lu_series.get("lc_rates").addValues(tf, new long[] {
              y01,
              y02 });
           this.lu_series.get("lc_distances").addValues(tf, new long[] {
              y03,
              y04,
              y05,
  //            y06,
              y07,
              y08 });
           this.lu_series.get("lc_durations").addValues(tf, new long[] {
              y09,
              y10,
              y11,
              y12,
              y13,
              y14 });
           this.lu_series.get("lc_counts").addValues(tf, new long[] {
              y15,
  //            y16,
  //            y17,
  //            y18,
              y19,
              y20 });
           this.lu_series.get("lc_times").addValues(tf, new long[] {
              y21 });
        });
      } catch (SQLException se) {
        System.err.printf("SQL failure: %s\n", se.getMessage());
      } catch (Exception ee) {
        ee.printStackTrace();
      }
      if (DEBUG) {
        System.err.printf("t=%s, execution took %d ms\n", t, (System.currentTimeMillis() - this.A0));
      }
    }
  }
  private SimpleXYChartDescriptor lc_counts_descriptor = SimpleXYChartDescriptor.decimal(0, true, 3600);
  private SimpleXYChartDescriptor lc_distances_descriptor = SimpleXYChartDescriptor.decimal(0, true, 3600);
  private SimpleXYChartDescriptor lc_durations_descriptor = SimpleXYChartDescriptor.decimal(0, true, 3600);
  private SimpleXYChartDescriptor lc_rates_descriptor = SimpleXYChartDescriptor.decimal(0, true, 3600);
  private SimpleXYChartDescriptor lc_times_descriptor = SimpleXYChartDescriptor.decimal(0, true, 3600);
  private SimpleXYChartSupport lc_counts_support = null;
  private SimpleXYChartSupport lc_distances_support = null;
  private SimpleXYChartSupport lc_durations_support = null;
  private SimpleXYChartSupport lc_rates_support = null;
  private SimpleXYChartSupport lc_times_support = null;
  private SwingNode lc_counts = new SwingNode();
  private SwingNode lc_distances = new SwingNode();
  private SwingNode lc_durations = new SwingNode();
  private SwingNode lc_rates = new SwingNode();
  private SwingNode lc_times = new SwingNode();
  private String[] metric_rates = new String[] {
  /*y01*/      "Running Service Rate (%, 100x)",
  /*y02*/      "Running Distance Savings (%, 100x)"
      };
  private String[] metric_distances = new String[] {
  /*y03*/      "S-Travel",
  /*y04*/      "S-Service",
  /*y05*/      "S-Cruising",
  ///*y06*/      "R-Unassigned",
  /*y07*/      "R-Transit",
  /*y08*/      "R-Detour"
      };
  private String[] metric_durations = new String[] {
  /*y09*/      "S-Travel",
  /*y10*/      "S-Service",
  /*y11*/      "S-Cruising",
  /*y12*/      "R-Transit",
  /*y13*/      "R-Travel",
  /*y14*/      "R-Pickup"
      };
  private String[] metric_counts = new String[] {
  /*y15*/      "R-Queue",
  ///*y16*/      "R-Active",
  ///*y17*/      "R-Completed",
  ///*y18*/      "S-Active",
  /*y19*/      "R-Violations",
  /*y20*/      "S-Violations"
      };
  private String[] metric_times = new String[] {
  /*y21*/      "R-Handling",
      };
  private ConcurrentHashMap<String, SimpleXYChartSupport> lu_series
    = new ConcurrentHashMap<String, SimpleXYChartSupport>();
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
                 this.ns = this.controller.queryServersCount()[0];
                 this.nr = this.controller.queryRequestsCount()[0];
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
                 this.ns = this.controller.queryServersCount()[0];
                 this.nr = this.controller.queryRequestsCount()[0];
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
           for (String metric : this.metric_rates) {
             this.lc_rates_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_distances) {
             this.lc_distances_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_durations) {
             this.lc_durations_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_counts) {
             this.lc_counts_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_times) {
             this.lc_times_descriptor.addLineItems(metric);
           }
           this.lc_counts_descriptor.setYAxisDescription("<html>Count (#)</html>");
           this.lc_distances_descriptor.setYAxisDescription("<html>Avg. Distance (m)</html>");
           this.lc_durations_descriptor.setYAxisDescription("<html>Avg. Duration (sec)</html>");
           this.lc_rates_descriptor.setYAxisDescription("<html>Rate (%)</html>");
           this.lc_times_descriptor.setYAxisDescription("<html>Elapsed Time (ms)</html>");
           this.lc_counts_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_distances_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_durations_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_rates_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_times_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_counts_support = ChartFactory.createSimpleXYChart(this.lc_counts_descriptor);
           this.lc_distances_support = ChartFactory.createSimpleXYChart(this.lc_distances_descriptor);
           this.lc_durations_support = ChartFactory.createSimpleXYChart(this.lc_durations_descriptor);
           this.lc_rates_support = ChartFactory.createSimpleXYChart(this.lc_rates_descriptor);
           this.lc_times_support = ChartFactory.createSimpleXYChart(this.lc_times_descriptor);
           this.lu_series.put("lc_counts", this.lc_counts_support);
           this.lu_series.put("lc_distances", this.lc_distances_support);
           this.lu_series.put("lc_durations", this.lc_durations_support);
           this.lu_series.put("lc_rates", this.lc_rates_support);
           this.lu_series.put("lc_times", this.lc_times_support);
           this.lc_counts.setContent(this.lc_counts_support.getChart());
           this.lc_distances.setContent(this.lc_distances_support.getChart());
           this.lc_durations.setContent(this.lc_durations_support.getChart());
           this.lc_rates.setContent(this.lc_rates_support.getChart());
           this.lc_times.setContent(this.lc_times_support.getChart());
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
           this.ren_servers = new RendererOfServers(this.can_servers.getGraphicsContext2D(), this.lbl_fps, true, this.muf);
           this.ren_servers.start();
           this.ren_requests = new RendererOfRequests(this.can_requests.getGraphicsContext2D(), this.muf);
           this.ren_requests.start();
           this.circ_status  .setFill(C_SUCCESS);
           this.lbl_status   .setText("Simulation started.");
           this.exe = Executors.newScheduledThreadPool(3);
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
           this.cbFetcherOfMetrics = this.exe.scheduleAtFixedRate(
               new FetcherOfMetrics(this.controller, this.lu_series, this.ns, this.nr), 0, 1, TimeUnit.SECONDS);
           this.cbFetcherOfRequests = this.exe.scheduleAtFixedRate(
               new FetcherOfRequests(
                 this.controller, this.muf, this.ren_requests), 0, 1, TimeUnit.SECONDS);
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
           for (String metric : this.metric_rates) {
             this.lc_rates_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_distances) {
             this.lc_distances_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_durations) {
             this.lc_durations_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_counts) {
             this.lc_counts_descriptor.addLineItems(metric);
           }
           for (String metric : this.metric_times) {
             this.lc_times_descriptor.addLineItems(metric);
           }
           this.lc_counts_descriptor.setYAxisDescription("<html>Count (#)</html>");
           this.lc_distances_descriptor.setYAxisDescription("<html>Avg. Distance (m)</html>");
           this.lc_durations_descriptor.setYAxisDescription("<html>Avg. Duration (sec)</html>");
           this.lc_rates_descriptor.setYAxisDescription("<html>Rate (%)</html>");
           this.lc_times_descriptor.setYAxisDescription("<html>Elapsed Time (ms)</html>");
           this.lc_counts_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_distances_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_durations_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_rates_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_times_descriptor.setXAxisDescription("<html>World Time</html>");
           this.lc_counts_support = ChartFactory.createSimpleXYChart(this.lc_counts_descriptor);
           this.lc_distances_support = ChartFactory.createSimpleXYChart(this.lc_distances_descriptor);
           this.lc_durations_support = ChartFactory.createSimpleXYChart(this.lc_durations_descriptor);
           this.lc_rates_support = ChartFactory.createSimpleXYChart(this.lc_rates_descriptor);
           this.lc_times_support = ChartFactory.createSimpleXYChart(this.lc_times_descriptor);
           this.lu_series.put("lc_counts", this.lc_counts_support);
           this.lu_series.put("lc_distances", this.lc_distances_support);
           this.lu_series.put("lc_durations", this.lc_durations_support);
           this.lu_series.put("lc_rates", this.lc_rates_support);
           this.lu_series.put("lc_times", this.lc_times_support);
           this.lc_counts.setContent(this.lc_counts_support.getChart());
           this.lc_distances.setContent(this.lc_distances_support.getChart());
           this.lc_durations.setContent(this.lc_durations_support.getChart());
           this.lc_rates.setContent(this.lc_rates_support.getChart());
           this.lc_times.setContent(this.lc_times_support.getChart());
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
           this.ren_servers = new RendererOfServers(this.can_servers.getGraphicsContext2D(), this.lbl_fps, false, this.muf);
           this.ren_servers.start();
           this.ren_requests = new RendererOfRequests(this.can_requests.getGraphicsContext2D(), this.muf);
           this.ren_requests.start();
           this.circ_status  .setFill(C_SUCCESS);
           this.lbl_status   .setText("Simulation started.");
           this.exe = Executors.newScheduledThreadPool(3);
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
           this.cbFetcherOfMetrics = this.exe.scheduleAtFixedRate(
               new FetcherOfMetrics(this.controller, this.lu_series, this.ns, this.nr), 0, 1, TimeUnit.SECONDS);
           this.cbFetcherOfRequests = this.exe.scheduleAtFixedRate(
               new FetcherOfRequests(
                 this.controller, this.muf, this.ren_requests), 0, 1, TimeUnit.SECONDS);
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
           this.can_requests.setWidth(this.window_width*this.zoom);
           this.can_requests.setHeight(this.window_height*this.zoom);
           if (this.ren_road != null) {
             this.ren_road.forceRender();
           }
           if (this.ren_servers != null) {
             this.ren_servers.setZoom(this.zoom);
           }
           if (this.ren_requests != null) {
             this.ren_requests.setZoom(this.zoom);
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
         }
  private void initializeCanvas() {
            try {
              this.can_road     = new Canvas(this.window_width, this.window_height);
              this.can_servers  = new Canvas(this.window_width, this.window_height);
              this.can_requests = new Canvas(this.window_width, this.window_height);
              this.lbl_fps      = new Label("FPS");
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
              this.muf.setLngMax(this.mbr[1]);
              this.muf.setLatMax(this.mbr[3]);
              // Add canvas to pane
              this.container_canvas_container = new Pane(
                  this.can_road,
                  this.can_servers,
                  this.can_requests
          //        this.lbl_fps
              );
              this.container_canvas.setContent(this.container_canvas_container);
              // Register mouse event handlers
              // (can_requests is on top so it will trap all mouse events)
              this.can_requests.setOnScroll((e) -> { actionZoomCanvas(e); });
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
    Image image = new Image("res/icon.gif");
    this.logo = new ImageView();
    this.logo.setImage(image);
    this.logo.setFitWidth(64);
    this.logo.setPreserveRatio(true);
    this.logo.setSmooth(true);
    this.logo.setCache(true);
  }
}
