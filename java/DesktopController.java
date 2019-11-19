package com.github.jargors;
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
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
public class DesktopController {
  private Stage stage;
  @FXML private ScrollPane container_canvas;
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
  private Canvas can_road;
  private AnimationTimer timer = new MyTimer();
  private GraphicsContext gc = null;
  private double window_height = 0;
  private double window_width = 0;
  private double mouse_x = 0;
  private double mouse_y = 0;
  private int[] edges = null;
  private int[] mbr = null;
  private double xunit = 0;
  private double yunit = 0;
  private class MyTimer extends AnimationTimer {
    public void handle(long now) {
      System.out.println(now);
    }
  }
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
                   this.btn_stop     .setDisable(false);
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
                   this.timer.stop();
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
             this.circ_status  .setFill(C_SUCCESS);
             this.lbl_status   .setText("Simulation started.");
             this.controller.registerClient(this.client);
             this.client.registerRoadNetwork();
             this.client.registerUsers();
             try {
               this.client.loadGtree(this.gtree);
             } catch (FileNotFoundException fe) {
               System.err.println(e.toString());
               return;
             }
             this.controller.setClockStart(Integer.parseInt(this.tf_t0.getText()));
             this.controller.setClockEnd(Integer.parseInt(this.tf_t1.getText()));
             CompletableFuture.runAsync(() -> {
               this.timer.start();
               this.controller.startSequential((status) -> {
                 Platform.runLater(() -> {
                   this.lbl_status.setText("Simulation "+(status ? "ended." : "failed."));
                 });
               });
             });
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
  public void initialize() { }
}
