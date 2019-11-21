package com.github.jargors.desktop;
import com.github.jargors.Controller;
import com.github.jargors.Client;
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
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
public class DesktopController {
  private Stage stage;
  private Canvas can_road;
  @FXML private ScrollPane container_canvas;
  @FXML private AnchorPane container_lctot;
  @FXML private Button btn_new;
  @FXML private Button btn_load;
  @FXML private Button btn_prob;
  @FXML private Button btn_road;
  @FXML private Button btn_gtree;
  @FXML private Button btn_client;
  @FXML private Button btn_startseq;
  @FXML private Button btn_startreal;
  @FXML private Button btn_pause;
  @FXML private Button btn_stop;
  @FXML private TextField tf_class;
  @FXML private TextField tf_t0;
  @FXML private TextField tf_t1;
  @FXML private Label lbl_status;
  @FXML private Circle circ_status;
  @FXML private VBox vbox_metrics;
  @FXML private CheckBox chk_service;
  @FXML private CheckBox chk_distsavings;
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
  private final Color C_SUCCESS = Color.GREEN;
  private final Color C_WARN = Color.YELLOW;
  private final Color C_ERROR = Color.RED;
  private final String TITLE_STRING = "Jargo Desktop";
  private Controller controller = null;
  private Client client = null;
  private String db = null;
  private String gtree = null;
  private String prob = null;
  private String road = null;
  private String jar = null;
  private String jarclass = null;
  private GraphicsContext gc = null;
  private double window_height = 0;
  private double window_width = 0;
  private double mouse_x = 0;
  private double mouse_y = 0;
  private int[] edges = null;
  private int[] mbr = null;
  private double xunit = 0;
  private double yunit = 0;
  private int t0 = 0;
  private int t1 = 0;
  private AnimationTimer timer = new MyTimer();
  private ScheduledExecutorService exe = null;
  private ScheduledFuture<?> cbUpdateMap = null;
  private ScheduledFuture<?> cbUpdateMetrics = null;
  private boolean flagServiceRate = false;
  private boolean flagDistanceSavings = false;
  private boolean flagServerTravelDistance = false;
  private boolean flagServerServiceDistance = false;
  private boolean flagServerCruisingDistance = false;
  private boolean flagServerTravelDuration = false;
  private boolean flagServerServiceDuration = false;
  private boolean flagServerCruisingDuration = false;
  private boolean flagRequestDistanceUnassigned = false;
  private boolean flagRequestTransitDistance = false;
  private boolean flagRequestDetourDistance = false;
  private boolean flagRequestTransitDuration = false;
  private boolean flagRequestDetourDuration = false;
  private boolean flagRequestTravelDuration = false;
  private boolean flagRequestPickupDuration = false;
  private class MyTimer extends AnimationTimer {
    public void handle(long now) {
      // System.out.println(now);
    }
  }
  private Runnable updateMap = () -> {
    System.out.println("t="+controller.getSimulationWorldTime());
  };
  private Runnable updateMetrics = () -> {
    final int t = this.controller.getSimulationWorldTime();
    if (flagServiceRate) {
      try {
        int[] output = this.controller.queryServiceRate();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.serviceRate.getData().add(new Data<Number, Number>(t, val/100.0));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagDistanceSavings) {
      try {
        int[] output = new int[] { };
        output = this.controller.queryServerTravelDistanceTotal();
        final int val1 = (output.length > 0 ? output[0] : 0);
        output = this.controller.queryRequestBaseDistanceUnassigned();
        final int val2 = (output.length > 0 ? output[0] : 0);
        output = this.controller.queryBaseDistanceTotal();
        final int val3 = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.distanceSavings.getData().add(new Data<Number, Number>(t, 100.0*(1 - ((double) (val1 + val2)/val3))));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagServerTravelDistance) {
      try {
        int[] output = this.controller.queryServerTravelDistanceTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.serverTravelDistance.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagServerServiceDistance) {
      try {
        int[] output = this.controller.queryServerServiceDistanceTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.serverServiceDistance.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagServerCruisingDistance) {
      try {
        int[] output = this.controller.queryServerCruisingDistanceTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.serverCruisingDistance.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagServerTravelDuration) {
      try {
        int[] output = this.controller.queryServerTravelDurationTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.serverTravelDuration.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    /*if (flagServerServiceDuration) {
        try {
          Platform.runLater(() -> {
          });
        } catch (SQLException se) {
          System.err.println("SQL failure: "+se.getMessage());
        }
      }*/
    /*if (flagServerCruisingDuration) {
        try {
          Platform.runLater(() -> {
          });
        } catch (SQLException se) {
          System.err.println("SQL failure: "+se.getMessage());
        }
      }*/
    if (flagRequestDistanceUnassigned) {
      try {
        int[] output = this.controller.queryRequestBaseDistanceUnassigned();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestDistanceUnassigned.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagRequestTransitDistance) {
      try {
        int[] output = this.controller.queryRequestTransitDistanceTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestTransitDistance.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagRequestDetourDistance) {
      try {
        int[] output = this.controller.queryRequestDetourDistanceTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestDetourDistance.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagRequestTransitDuration) {
      try {
        int[] output = this.controller.queryRequestTransitDurationTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestTransitDuration.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    /*if (flagRequestDetourDuration) {
        try {
          Platform.runLater(() -> {
          });
        } catch (SQLException se) {
          System.err.println("SQL failure: "+se.getMessage());
        }
      }*/
    if (flagRequestTravelDuration) {
      try {
        int[] output = this.controller.queryRequestTravelDurationTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestTravelDuration.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
    if (flagRequestPickupDuration) {
      try {
        int[] output = this.controller.queryRequestPickupDurationTotal();
        final int val = (output.length > 0 ? output[0] : 0);
        Platform.runLater(() -> {
          this.requestPickupDuration.getData().add(new Data<Number, Number>(t, val));
        });
      } catch (SQLException se) {
        System.err.println("SQL failure: "+se.getMessage());
      }
    }
  };
  private NumberAxis chart_x = new NumberAxis("Simulation World Time (seconds since start)", 0, 60, 5);
  private NumberAxis chart_y = new NumberAxis("Value", 0, 100, 10);
  private LineChart<Number, Number> lctot = new LineChart<Number, Number>(chart_x, chart_y);
  private Series<Number, Number> serviceRate = new Series<Number, Number>();
  private Series<Number, Number> distanceSavings = new Series<Number, Number>();
  private Series<Number, Number> serverTravelDistance = new Series<Number, Number>();
  private Series<Number, Number> serverServiceDistance = new Series<Number, Number>();
  private Series<Number, Number> serverCruisingDistance = new Series<Number, Number>();
  private Series<Number, Number> serverTravelDuration = new Series<Number, Number>();
  private Series<Number, Number> serverServiceDuration = new Series<Number, Number>();
  private Series<Number, Number> serverCruisingDuration = new Series<Number, Number>();
  private Series<Number, Number> requestDistanceUnassigned = new Series<Number, Number>();
  private Series<Number, Number> requestTransitDistance = new Series<Number, Number>();
  private Series<Number, Number> requestDetourDistance = new Series<Number, Number>();
  private Series<Number, Number> requestTransitDuration = new Series<Number, Number>();
  private Series<Number, Number> requestDetourDuration = new Series<Number, Number>();
  private Series<Number, Number> requestTravelDuration = new Series<Number, Number>();
  private Series<Number, Number> requestPickupDuration = new Series<Number, Number>();
  public void actionQuit(final ActionEvent e) {
           System.exit(0);
         }
  public void actionGitHub(final ActionEvent e) {
           // ...
         }
  public void actionAbout(final ActionEvent e) {
           // ...
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
               this.controller.createNewInstance();
             } catch (SQLException se) {
               System.err.println("Could not create new instance");
               System.exit(1);
             }
             this.controller.loadDataModel();
             this.db = "no-name";
             Platform.runLater(() -> {
               this.btn_road     .setDisable(false);
               this.btn_gtree    .setDisable(false);
               this.btn_stop     .setDisable(false);
               this.tf_class     .setDisable(false);
               this.tf_t0        .setDisable(false);
               this.tf_t1        .setDisable(false);
               this.circ_status.setFill(C_SUCCESS);
               this.lbl_status.setText("Created new Jargo instance.");
             });
           });
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
                 this.controller.loadBackup(this.db);
                 this.controller.loadRoadNetworkFromDB();
                 this.controller.loadUsersFromDB();
                 int nv = this.controller.queryCountVertices()[0];
                 int ne = this.controller.queryCountEdges()[0];
                 int ns = this.controller.queryCountServers()[0];
                 int nr = this.controller.queryCountRequests()[0];
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
                   this.drawRoadNetwork();
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
               this.controller.closeGtree();
             }
             this.gtree = gt.toString();
             this.lbl_status.setText("Load '"+this.gtree+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadGtree(this.gtree);
                 Platform.runLater(() -> {
                   this.btn_prob     .setDisable(state_btn_prob);
                   this.btn_road     .setDisable(state_btn_road);
                   this.btn_stop     .setDisable(false);
                   if (this.road != null && this.prob == null) {
                     this.btn_prob   .setDisable(false);
                   }
                   if (this.road != null && this.prob != null) {
                     this.btn_client .setDisable(false);
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
  public void actionRoad(final ActionEvent e) {
           boolean state_btn_gtree = this.btn_gtree.isDisabled();
           this.btn_road     .setDisable(true);
           this.btn_gtree    .setDisable(true);
           this.btn_stop     .setDisable(true);
           this.tf_class     .setDisable(true);
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
                 int nv = this.controller.queryCountVertices()[0];
                 int ne = this.controller.queryCountEdges()[0];
                 Platform.runLater(() -> {
                   this.btn_gtree    .setDisable(state_btn_gtree);
                   this.btn_stop     .setDisable(false);
                   this.btn_road     .setText(road.getName());
                   if (this.gtree != null) {
                     this.btn_prob   .setDisable(false);
                   }
                   this.tf_class     .setDisable(false);
                   this.tf_t0        .setDisable(false);
                   this.tf_t1        .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded "+road.getName()+" (#vertices="+nv+"; #edges="+ne+")");
                   this.drawRoadNetwork();
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
                 int ns = this.controller.queryCountServers()[0];
                 int nr = this.controller.queryCountRequests()[0];
                 Platform.runLater(() -> {
                   this.btn_prob     .setText(pb.getName());
                   this.btn_client   .setDisable(false);
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
  public void actionClient(final ActionEvent e) {
           this.btn_client   .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Select *.jar...");
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Client *.jar", "*.jar"));
           File cj = fc.showOpenDialog(this.stage);
           if (cj != null) {
             this.jar = cj.toString();

             try {
         /*https://stackoverflow.com/questions/15720822/how-to-get-names-of-classes-inside-a-jar-file*/
         List<String> classNames = new ArrayList<String>();
         ZipInputStream zip = new ZipInputStream(new FileInputStream(this.jar));
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
               this.jarclass = classNames.get(0);
             } catch (IOException ie) {
               System.err.println(ie.toString());
               return;
             }
             this.tf_class.setText(this.jarclass);
             this.btn_client   .setText(cj.getName());
             this.tf_class     .setDisable(false);
             this.tf_t0        .setDisable(false);
             this.tf_t1        .setDisable(false);
             this.btn_startseq .setDisable(false);
             this.btn_startreal.setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Loaded "+cj.getName());
           }
         }
  public void actionStop(final ActionEvent e) {
           if (this.controller != null) {
             this.btn_stop     .setDisable(true);
             this.vbox_metrics .setDisable(true);
             this.chk_service  .setSelected(false);
             this.timer.stop();
             this.exe.shutdown();
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Close '"+this.db+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.stop((status) -> {
                   Platform.runLater(() -> {
                     this.lbl_status.setText("Simulation stopped.");
                   });
                 });
                 this.controller.closeInstance();
                 this.controller.closeGtree();
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
                   this.client = null;
                   this.jar = null;
                   this.jarclass = null;
                   this.tf_class     .setDisable(true);
                   this.tf_class     .setText("");
                   this.tf_t0        .setDisable(true);
                   this.tf_t0        .setText("");
                   this.tf_t1        .setDisable(true);
                   this.tf_t1        .setText("");
                   this.btn_startseq .setDisable(true);
                   this.btn_startreal.setDisable(true);
                   this.db = null;
                   this.container_canvas.setContent(null);
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
  public void actionStartSequential(final ActionEvent e) {
           this.jarclass = this.tf_class.getText();
           if ("".equals(this.jarclass)) {
             System.err.println("Class empty!");
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
           this.tf_class     .setDisable(true);
           this.tf_t0        .setDisable(true);
           this.tf_t1        .setDisable(true);
           this.circ_status  .setFill(C_WARN);
           this.lbl_status   .setText("Loading '"+this.jarclass+"'...");
           try {
             URLClassLoader loader = new URLClassLoader(new URL[] {new URL("file://"+this.jar)},
                 this.getClass().getClassLoader());
             Class<?> tempclass = Class.forName(this.jarclass, true, loader);
             Constructor<?> tempcstor = tempclass.getDeclaredConstructor();
             this.client = (Client) tempcstor.newInstance();
             this.controller.registerClient(this.client);
             this.client.registerRoadNetwork();
             this.client.registerUsers();
             try {
               this.client.loadGtree(this.gtree);
             } catch (FileNotFoundException fe) {
               System.err.println(e.toString());
               return;
             }
             this.t0 = Integer.parseInt(this.tf_t0.getText());
             this.t1 = Integer.parseInt(this.tf_t1.getText());
             this.controller.setClockStart(t0);
             this.controller.setClockEnd(t1);
             this.timer.start();
             this.chart_x.setAutoRanging(true);
             this.chart_y.setAutoRanging(true);
             this.lctot.setCreateSymbols(false);
             this.lctot.setAnimated(false);
             this.lctot.setLegendVisible(false);
             this.lctot.getData().add(this.serviceRate);
             this.lctot.getData().add(this.distanceSavings);
             this.lctot.getData().add(this.serverTravelDistance);
             this.lctot.getData().add(this.serverServiceDistance);
             this.lctot.getData().add(this.serverCruisingDistance);
             this.lctot.getData().add(this.serverTravelDuration);
             this.lctot.getData().add(this.serverServiceDuration);
             this.lctot.getData().add(this.serverCruisingDuration);
             this.lctot.getData().add(this.requestDistanceUnassigned);
             this.lctot.getData().add(this.requestTransitDistance);
             this.lctot.getData().add(this.requestDetourDistance);
             this.lctot.getData().add(this.requestTransitDuration);
             this.lctot.getData().add(this.requestDetourDuration);
             this.lctot.getData().add(this.requestTravelDuration);
             this.lctot.getData().add(this.requestPickupDuration);
             this.container_lctot.getChildren().clear();
             this.container_lctot.setTopAnchor(this.lctot, 0.0);
             this.container_lctot.setLeftAnchor(this.lctot, 0.0);
             this.container_lctot.setRightAnchor(this.lctot, 0.0);
             this.container_lctot.setBottomAnchor(this.lctot, 0.0);
             this.container_lctot.getChildren().add(this.lctot);
             this.vbox_metrics .setDisable(false);
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Simulation started.");
             CompletableFuture.runAsync(() -> {
               this.controller.startSequential((status) -> {
                 Platform.runLater(() -> {
                   this.lbl_status.setText("Simulation "+(status ? "ended." : "failed."));
                 });
               });
             });
             this.exe = Executors.newScheduledThreadPool(2);
             this.cbUpdateMap = this.exe.scheduleAtFixedRate(this.updateMap, 0, 1, TimeUnit.SECONDS);
             this.cbUpdateMetrics = this.exe.scheduleAtFixedRate(this.updateMetrics, 0, 1, TimeUnit.SECONDS);
           } catch (MalformedURLException
               | ClassNotFoundException
               | NoSuchMethodException
               | InstantiationException
               | IllegalAccessException
               | InvocationTargetException me) {
             System.err.println(me.toString());
             return;
           }
         }
  public void actionStartRealtime(final ActionEvent e) {
         }
  public void actionRecordMousePress(MouseEvent e) {
           this.mouse_x = e.getX();
           this.mouse_y = e.getY();
           e.consume();
         }
  public void actionTranslateCanvas(MouseEvent e) {
           this.can_road.setTranslateX(this.can_road.getTranslateX() + e.getX() - this.mouse_x);
           this.can_road.setTranslateY(this.can_road.getTranslateY() + e.getY() - this.mouse_y);
           e.consume();
         }
  public void toggleServiceRate(ActionEvent e) {
           if (this.chk_service.isSelected()) {
             this.flagServiceRate = true;
           } else {
             this.serviceRate.getData().clear();
             this.flagServiceRate = false;
           }
         }
  public void toggleDistanceSavings(ActionEvent e) {
           if (this.chk_distsavings.isSelected()) {
             this.flagDistanceSavings = true;
           } else {
             this.distanceSavings.getData().clear();
             this.flagDistanceSavings = false;
           }
         }
  public void toggleServerTravelDistance(ActionEvent e) {
           if (this.chk_serverTravelDistance.isSelected()) {
             this.flagServerTravelDistance = true;
           } else {
             this.serverTravelDistance.getData().clear();
             this.flagServerTravelDistance = false;
           }
         }
  public void toggleServerServiceDistance(ActionEvent e) {
           if (this.chk_serverServiceDistance.isSelected()) {
             this.flagServerServiceDistance = true;
           } else {
             this.serverServiceDistance.getData().clear();
             this.flagServerServiceDistance = false;
           }
         }
  public void toggleServerCruisingDistance(ActionEvent e) {
           if (this.chk_serverCruisingDistance.isSelected()) {
             this.flagServerCruisingDistance = true;
           } else {
             this.serverCruisingDistance.getData().clear();
             this.flagServerCruisingDistance = false;
           }
         }
  public void toggleServerTravelDuration(ActionEvent e) {
           if (this.chk_serverTravelDuration.isSelected()) {
             this.flagServerTravelDuration = true;
           } else {
             this.serverTravelDuration.getData().clear();
             this.flagServerTravelDuration = false;
           }
         }
  public void toggleServerServiceDuration(ActionEvent e) {
           if (this.chk_serverServiceDuration.isSelected()) {
             this.flagServerServiceDuration = true;
           } else {
             this.serverServiceDuration.getData().clear();
             this.flagServerServiceDuration = false;
           }
         }
  public void toggleServerCruisingDuration(ActionEvent e) {
           if (this.chk_serverCruisingDuration.isSelected()) {
             this.flagServerCruisingDuration = true;
           } else {
             this.serverCruisingDuration.getData().clear();
             this.flagServerCruisingDuration = false;
           }
         }
  public void toggleRequestDistanceUnassigned(ActionEvent e) {
           if (this.chk_requestDistanceUnassigned.isSelected()) {
             this.flagRequestDistanceUnassigned = true;
           } else {
             this.requestDistanceUnassigned.getData().clear();
             this.flagRequestDistanceUnassigned = false;
           }
         }
  public void toggleRequestTransitDistance(ActionEvent e) {
           if (this.chk_requestTransitDistance.isSelected()) {
             this.flagRequestTransitDistance = true;
           } else {
             this.requestTransitDistance.getData().clear();
             this.flagRequestTransitDistance = false;
           }
         }
  public void toggleRequestDetourDistance(ActionEvent e) {
           if (this.chk_requestDetourDistance.isSelected()) {
             this.flagRequestDetourDistance = true;
           } else {
             this.requestDetourDistance.getData().clear();
             this.flagRequestDetourDistance = false;
           }
         }
  public void toggleRequestTransitDuration(ActionEvent e) {
           if (this.chk_requestTransitDuration.isSelected()) {
             this.flagRequestTransitDuration = true;
           } else {
             this.requestTransitDuration.getData().clear();
             this.flagRequestTransitDuration = false;
           }
         }
  public void toggleRequestDetourDuration(ActionEvent e) {
           if (this.chk_requestDetourDuration.isSelected()) {
             this.flagRequestDetourDuration = true;
           } else {
             this.requestDetourDuration.getData().clear();
             this.flagRequestDetourDuration = false;
           }
         }
  public void toggleRequestTravelDuration(ActionEvent e) {
           if (this.chk_requestTravelDuration.isSelected()) {
             this.flagRequestTravelDuration = true;
           } else {
             this.requestTravelDuration.getData().clear();
             this.flagRequestTravelDuration = false;
           }
         }
  public void toggleRequestPickupDuration(ActionEvent e) {
           if (this.chk_requestPickupDuration.isSelected()) {
             this.flagRequestPickupDuration = true;
           } else {
             this.requestPickupDuration.getData().clear();
             this.flagRequestPickupDuration = false;
           }
         }
  public void setWindowWidth(double w) {
           this.window_width = w;
         }
  public void setWindowHeight(double h) {
           this.window_height = h;
         }
  public void setStage(Stage s) {
           this.stage = s;
         }
  private void drawRoadNetwork() {
            try {
              this.edges    = this.controller.queryAllEdges();
              this.mbr      = this.controller.queryMBR();

              this.can_road = new Canvas(this.window_width, this.window_height);
              this.xunit    = this.can_road.getWidth() /(double) (this.mbr[1] - this.mbr[0]);
              this.yunit    = this.can_road.getHeight()/(double) (this.mbr[3] - this.mbr[2]);
              double unit   = Math.min(xunit, yunit);

              this.gc = this.can_road.getGraphicsContext2D();
              this.gc.setLineWidth(0.1);
              this.gc.setStroke(Color.BLUE);
              for (int i = 0; i < (this.edges.length - 3); i += 4) {
                if (this.edges[(i + 0)] != 0 && this.edges[(i + 1)] != 0) {
                  int[] v1 = this.controller.queryVertex(this.edges[(i + 0)]);
                  int[] v2 = this.controller.queryVertex(this.edges[(i + 1)]);
                  this.gc.strokeLine(unit*(v1[0] - this.mbr[0]), unit*(v1[1] - this.mbr[2]),
                                     unit*(v2[0] - this.mbr[0]), unit*(v2[1] - this.mbr[2]));
                }
              }
              this.container_canvas.setContent(this.can_road);
              this.can_road.setOnMousePressed((e) -> { actionRecordMousePress(e); });
              this.can_road.setOnMouseDragged((e) -> { actionTranslateCanvas(e); });
            } catch (SQLException se) {
              System.err.println("Failed with SQLException");
              Tools.PrintSQLException(se);
              return;
            } catch (VertexNotFoundException ve) {
              System.err.println(ve.toString());
              return;
            }
          }
  private void drawServersInitial() {
          }
  public void initialize() { }
}
