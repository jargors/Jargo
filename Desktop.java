import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
public class Desktop extends Application {
  final int MIN_WIDTH = 1280;
  final int MIN_HEIGHT = 720;
  final String TITLE_STRING = "Jargo Desktop";
  public void start(Stage stage) {
    Menu menu_file = new Menu("File");
    MenuItem menu_file_new = new MenuItem("New Jargo Instance");
    MenuItem menu_file_load = new MenuItem("Restore from Backup...");
    MenuItem menu_file_save = new MenuItem("Export...");
    MenuItem menu_file_exit = new MenuItem("Quit");
    menu_file_exit.setOnAction(e -> System.exit(0));
    SeparatorMenuItem separator = new SeparatorMenuItem();
    menu_file.getItems().addAll(
        menu_file_new,
        menu_file_load,
        menu_file_save,
        separator,
        menu_file_exit
    );
    MenuBar menubar = new MenuBar(
      menu_file
    );
    TabPane container_tabs = new TabPane();
    Tab tab_map = new Tab("Map");
    Tab tab_log = new Tab("Log");
    container_tabs.getTabs().add(tab_map);
    container_tabs.getTabs().add(tab_log);
    VBox container_settings = new VBox(new Label("Settings"));
    Label status_msg = new Label("Status bar");
    HBox statusbar = new HBox(status_msg);
    BorderPane layout = new BorderPane();
    layout.setTop(menubar);
    layout.setCenter(container_tabs);
    layout.setRight(container_settings);
    layout.setBottom(statusbar);
    Scene scene = new Scene(layout);
    stage.setScene(scene);
    stage.setTitle(TITLE_STRING);
    stage.setMinWidth(MIN_WIDTH);
    stage.setMinHeight(MIN_HEIGHT);
    stage.show();
  }
}
