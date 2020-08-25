package launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The Launcher for the CashAssets Application. This will check for an Update for the 
 * Launcher itself and, if one is found, asks the User to do an Update.
 * @author Haeldeus
 * @version 1.0
 */
public class MainLauncher extends Application {

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

  public static void main(String[] args) {
    MainLauncher.launch(args);
  }
  
  public static double checkVersion() {
    return version;
  }
  
}

class ProgressTask extends Task<Void> {
  @Override
  protected Void call() {
    for (int i = 0; i <= 10000; i++) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      updateProgress(i, 10000);
    }
    File f = new File(System.getProperty("user.dir") + "/WeyherCalculator.jar");
    // Run a java application in a separate system process
    Process proc;
    try {
      proc = Runtime.getRuntime().exec("java -jar " + f.getPath());
      // Then retrieve the process output
      InputStream in = proc.getInputStream();
      InputStream err = proc.getErrorStream();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}