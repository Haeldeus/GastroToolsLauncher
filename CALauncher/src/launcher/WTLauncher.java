package launcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tasks.ProgressTask;

/**
 * The Launcher for the CashAssets Application. This will check for an Update for the 
 * Launcher itself and, if one is found, asks the User to do an Update.
 * @author Haeldeus
 * @version 1.0
 */
public class WTLauncher extends Application {

  private static final double version = 0.5;
  
  @Override
  public void start(Stage primaryStage) throws Exception {
    BorderPane bp = new BorderPane();
    ProgressIndicator pi = new ProgressIndicator();
    bp.setCenter(pi);
    
    ProgressTask pt = new ProgressTask();
    pi.progressProperty().bind(pt.progressProperty());
    new Thread(pt).start();
    /*
     * Sets the Size of the Scene, it's restrictions and the Stylesheet. Afterwards, it displays 
     * the primaryStage to the User.
     */
    Scene scene = new Scene(bp, 600, 250);
//    scene.getStylesheets().add(Util.getControlStyle());
    primaryStage.setScene(scene);
    primaryStage.setMinHeight(270);
    primaryStage.setMinWidth(620);
    primaryStage.show();
  }
  
  public static double checkVersion() {
    return version;
  }
  
  public static void main(String[] args) {
    WTLauncher.launch(args);
  }
}