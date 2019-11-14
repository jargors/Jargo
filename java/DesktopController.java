package com.github.jargors;
import com.github.jargors.Controller;
import com.github.jargors.Client;
import com.github.jargors.Tools;
import com.github.jargors.exceptions.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
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
  private Canvas can_road;
  private GraphicsContext gc = null;
  private double window_height = 0;
  private double window_width = 0;
  private double mouse_x = 0;
  private double mouse_y = 0;
  private int[] edges = null;
  private int[] mbr = null;
  private double xunit = 0;
  private double yunit = 0;
  public void actionQuit(ActionEvent e) {
           System.exit(0);
         }
  public void actionGitHub(ActionEvent e) {
           // ...
         }
  public void actionAbout(ActionEvent e) {
           // ...
         }
  public void actionNew(ActionEvent e) {
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
  public void actionLoad(ActionEvent e) {
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
                 Platform.runLater(() -> {
                   this.btn_prob     .setDisable(true);
                   this.btn_road     .setDisable(true);
                   this.btn_gtree    .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.tf_class     .setDisable(false);
                   this.tf_t0        .setDisable(false);
                   this.tf_t1        .setDisable(false);
                   this.circ_status  .setFill(C_SUCCESS);
                   this.lbl_status   .setText("Loaded Jargo instance (#vertices="+nv+"; #edges="+ne+")");
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
  public void actionGtree(ActionEvent e) {
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
             this.lbl_status.setText("/Load '"+this.gtree+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadGtree(this.gtree);
                 Platform.runLater(() -> {
                   this.btn_prob     .setDisable(state_btn_prob);
                   this.btn_road     .setDisable(state_btn_road);
                   this.btn_client   .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.btn_gtree    .setDisable(false);
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
  public void actionRoad(ActionEvent e) {
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Road network *.rnet", "*.rnet"));
           File road = fc.showOpenDialog(this.stage);
           if (road != null) {
             this.road = road.toString();
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadRoadNetworkFromFile(this.road);
                 Platform.runLater(() -> {
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
  public void actionProb(ActionEvent e) {
           FileChooser fc = new FileChooser();
           fc.getExtensionFilters().addAll(new ExtensionFilter("Problem Instance *.instance", "*.instance"));
           File prob = fc.showOpenDialog(this.stage);
           if (prob != null) {
             this.prob = prob.toString();
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.loadProblem(this.prob);
                 Platform.runLater(() -> {

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
  public void actionStop(ActionEvent e) {
           if (this.controller != null) {
             this.btn_stop     .setDisable(true);
             this.circ_status.setFill(C_WARN);
             this.lbl_status.setText("Close '"+this.db+"'...");
             CompletableFuture.runAsync(() -> {
               try {
                 this.controller.closeInstance();
                 this.controller.closeGtree();
                 Platform.runLater(() -> {
                   this.btn_new      .setDisable(false);
                   this.btn_load     .setDisable(false);
                   this.btn_stop     .setDisable(false);
                   this.btn_prob     .setDisable(true);
                   this.btn_road     .setDisable(true);
                   this.btn_gtree    .setDisable(true);
                   this.btn_gtree    .setText("empty G-tree");
                   this.btn_client   .setDisable(true);
                   this.tf_class     .setDisable(true);
                   this.tf_t0        .setDisable(true);
                   this.tf_t1        .setDisable(true);
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
