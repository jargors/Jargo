package com.github.jargors;
import com.github.jargors.Controller;
import com.github.jargors.Client;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
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
      this.controller.createNewInstance();
      this.controller.loadDataModel();
      this.db = "no-name";
      Platform.runLater(() -> {
        this.btn_prob     .setDisable(false);
        this.btn_road     .setDisable(false);
        this.btn_gtree    .setDisable(false);
        this.btn_client   .setDisable(false);
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
        this.controller = new Controller();
        this.controller.loadBackup(this.db);
        int nv = this.controller.queryCountVertices()[0];
        int ne = this.controller.queryCountEdges()[0];
        Platform.runLater(() -> {
          this.btn_prob     .setDisable(true);
          this.btn_road     .setDisable(true);
          this.btn_gtree    .setDisable(false);
          this.btn_client   .setDisable(false);
          this.btn_stop     .setDisable(false);
          this.tf_class     .setDisable(false);
          this.tf_t0        .setDisable(false);
          this.tf_t1        .setDisable(false);
          this.circ_status.setFill(C_SUCCESS);
          this.lbl_status.setText("Loaded Jargo instance (#vertices="+nv+"; #edges="+ne+")");
          this.drawRoadNetwork();
        });
      });
    } else {
      this.btn_new      .setDisable(false);
      this.btn_load     .setDisable(false);
      this.btn_stop     .setDisable(false);
    }
  }
  public void actionStop(ActionEvent e) {
    if (this.controller != null) {
      this.btn_stop     .setDisable(true);
      this.circ_status.setFill(C_WARN);
      this.lbl_status.setText("Close '"+this.db+"'...");
      CompletableFuture.runAsync(() -> {
        this.controller.closeInstance();
        Platform.runLater(() -> {
          this.btn_new      .setDisable(false);
          this.btn_load     .setDisable(false);
          this.btn_stop     .setDisable(false);
          this.btn_prob     .setDisable(true);
          this.btn_road     .setDisable(true);
          this.btn_gtree    .setDisable(true);
          this.btn_client   .setDisable(true);
          this.tf_class     .setDisable(true);
          this.tf_t0        .setDisable(true);
          this.tf_t1        .setDisable(true);
          this.container_canvas.setContent(null);
          this.circ_status.setFill(C_SUCCESS);
          this.lbl_status.setText("Closed instance.");
        });
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
  }
  public void initialize() { }
}
