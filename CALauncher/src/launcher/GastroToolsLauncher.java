package launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import tasks.ProgressTask;
import tasks.UpdateTask;
import util.AppDisplayArea;

/**
 * The Launcher for the CashAssets Application. This will check for an Update for the 
 * Applications and, if one is found, asks the User to do an Update.
 * @author Haeldeus
 * @version 1.0
 */
public class GastroToolsLauncher extends Application {

  /**
   * The Version of this Launcher. This is needed to write the version Number on the hard disc, so 
   * the Updater can keep this Launcher updated.
   */
  private static final String version = "0.9";
  
  /**
   * The Label, that will display Messages to the User or general information.
   */
  private Label updatesLabel;
  
  /**
   * The BorderPane, that contains the content of the Scene. This is saved globally, so it hasn't 
   * to be passed to every Method.
   */
  private BorderPane bp;
  
  /**
   * The List of all published Repositories. This List contains the Names, that should be the 
   * Files' names. These are the Names of the Repositories, that were added to this Launcher.
   */
  private ArrayList<String> repos;
  
  /**
   * The Names of the Folders for the Applications. This are clear Names instead of the 
   * Repositories' names.
   */
  private ArrayList<String> names;
  
  /**
   * The current path, this Application is working on. Depending on whether the Launcher was 
   * started manually or by the Updater, this is either "some/dir/path/app" or "some/dir/path". 
   * After writing the Version, this will always be "../app".
   */
  private String path;
  
  /**
   * The Stage of the Application.
   * @see Stage
   */
  private Stage primaryStage;
  
  /**
   * All {@link AppDisplayArea}s, that were added to the Launcher.
   */
  private HashMap<String, AppDisplayArea> displayAreas;
  
  @Override
  public void start(Stage primary) throws Exception {
    /*
     * Sets the primary Stage of this Application, as well as path to the current working 
     * directory. Also writes the Version to the disc and creates a new BorderPane, which will 
     * contain all contents of this Scene.
     */
    this.primaryStage = primary;
    path = Paths.get("").toAbsolutePath().toString();
    writeVersion();
    bp = new BorderPane();
    /*
     * Sets the Size of the Scene, it's restrictions and the Stylesheet. Afterwards, it displays 
     * the primaryStage to the User.
     */
    Scene scene = new Scene(bp, 350, 300);
    //scene.getStylesheets().add(Util.getControlStyle());
    primaryStage.setScene(scene);
    primaryStage.setMinHeight(270);
    primaryStage.setMinWidth(350);
    primaryStage.show();
    /*
     * Starts the Checking for the List Task.
     */
    startCheckingTask();
  }
  
  /**
   * Writes the version into a new TextFile in the current working directory indicated by 
   * {@link #path}. Also sets {@link #path} to be working in the /app/ Folder of the current 
   * directory.
   * @since 1.0
   */
  private void writeVersion() {
    try {
      int index = path.lastIndexOf(File.separator + "app");
      if (index >= 0) {
        path = path.substring(0, index);
      }
      path = path.concat(File.separator + "app" + File.separator);
      FileWriter fw = new FileWriter(path + "Version.txt");
      fw.write("" + version);
      fw.close(); 
    } catch (IOException e) {
      //Nothing to do.
    } 
  }
  
  /**
   * Displays the given Message to the User. Since this is used, whenever something went wrong, 
   * the User afterwards can choose between starting the Launcher without updating the List or 
   * retry to check again.
   * @param text  The Text to be displayed in the Label.
   * @since 1.0
   */
  public void showUpdateFailed(String text) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        /*
         * Resets the BorderPane to default by setting the displayed Nodes to null.
         */
        bp.setCenter(null);
        bp.setBottom(null);
        /*
         * Creates a new GridPane, which will contain the Message and 2 Buttons to retry or ignore. 
         * Also configures this GridPane.
         */
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        /*
         * Sets the Text of the Label and adds it to the Grid.
         */
        updatesLabel.setText(text);
        grid.add(updatesLabel, 0, 0, 2, 1);
        /*
         * Creates the Retry-Button, configures it's EventHandler and adds it to the Grid.
         */
        Button btRetry = new Button("Wiederholen" + System.lineSeparator() + "(Empfohlen)");
        btRetry.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            startCheckingTask();
          }          
        });
        grid.add(btRetry, 0, 1);
        
        /*
         * Creates the Start-Button, which will ignore the Error, configures it's EventHandler and 
         * adds it to the Grid.
         */
        Button btStart = new Button("Ohne Update starten" + System.lineSeparator() 
            + "(Nicht Empfohlen)");
        btStart.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent ae) {
            startWithoutUpdate();
          }
        });
        grid.add(btStart, 1, 1);
        /*
         * Sets the Alignment of all Nodes in the GridPane to Center and adds the Grid to the 
         * BorderPane.
         */
        grid.setAlignment(Pos.CENTER);
        bp.setCenter(grid);
      }      
    });
  }  
  
  /**
   * Starts the Task, that will check for the List of Applications in the Repository.
   * @since 1.0
   * @see ProgressTask
   */
  private void startCheckingTask() {
    /*
     * Creates a new ProgressIndicator and Label, which will display the state of the Check to the 
     * User.
     */
    ProgressIndicator pi = new ProgressIndicator();
    updatesLabel = new Label();
    bp.setBottom(this.updatesLabel);
    bp.setCenter(pi);
    
    /*
     * Creates a new ProgressTask, binds it to the Indicator and starts a new Thread for the Task.
     */
    ProgressTask pt = new ProgressTask(this.updatesLabel, this);
    pi.progressProperty().bind(pt.progressProperty());
    new Thread(pt).start();
  }
  
  /**
   * Builds the Main Launcher Application. 
   * <br>In case no connection was found, indicated by passing 
   * {@code false} as parameter, this Method will add the local Folders/Files as executable 
   * Application. This List will be created in {@link #startWithoutUpdate()}. 
   * <br>Since there might be corrupted/unofficial/custom Folders in these Local Files, a check for 
   * executable ".jar"-Files will be made and in case no executable is found, this Folder is 
   * displayed, but when the User tries to start this Application, a Message will be displayed to 
   * the User and no start is tried.
   * <br><br>In case a connection was found, the List has been created in {@link ProgressTask} and 
   * the Launcher will display this List. This start will ignore custom Folders, so there is a need 
   * for a repair Method.
   * @param connection  Boolean value if a connection could be established in {@link 
   *      #startCheckingTask()}.
   * @since 1.0
   * @see #startCheckingTask()
   * @see #startWithoutUpdate()
   * @see ProgressTask
   */
  public void buildLauncher(boolean connection) {
    /*
     * Creates a new Runnable, to alter the Content of the Scene.
     */
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        /*
         * Resizes the Stage and deletes its' previous content.
         */
        primaryStage.setMinWidth(350);
        primaryStage.setWidth(350);
        primaryStage.setMinHeight(400);
        primaryStage.setHeight(400);
        bp.setCenter(null);
        bp.setBottom(null);
        /*
         * Creates a HashMap to store the DisplayAreas in. This is used to display Message 
         * individually.
         */
        displayAreas = new HashMap<String, AppDisplayArea>();
        /*
         * Creates a GridPane, that will contain all DisplayAreas.
         */
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setVgap(20);
        /*
         * Depending on whether a Connection was established, this Launcher has to fill the 
         * AppDisplayAreas differently, so this check is required.
         */
        if (connection) {
          for (int i = 0; i < names.size(); i++) {
            AppDisplayArea area = new AppDisplayArea();
            area.setName(names.get(i));
            boolean startDisable = false;
            if (repos.get(i) == "" || repos.get(i) == null) {
              area.setPath("");
              area.updateMessage("Keine ausführbare Datei gefunden!");
              startDisable = true;
            } else {
              area.setPath(names.get(i) + File.separator + repos.get(i));
            }
            area.setPathToIcon("/res/Default.png"); //TODO: Add real Icon.
            grid.add(area.createDisplayArea(), 0, i);
            area.switchButtons(startDisable, false, false);
            displayAreas.put(area.getName(), area);
          }
          startCheckUpdateTasks();
        } else {
          for (int i = 0; i < names.size(); i++) {
            AppDisplayArea area = new AppDisplayArea();
            area.setName(names.get(i));

            boolean startDisable = false;
            if (repos.get(i) == "" || repos.get(i) == null) {
              area.setPath("");
              area.updateMessage("Keine ausführbare Datei gefunden!");
              startDisable = true;
            } else {
              area.setPath(names.get(i) + File.separator + repos.get(i));
            }
            area.setPathToIcon("/res/Default.png");
            grid.add(area.createDisplayArea(), 0, i);
            area.switchButtons(startDisable, true, false);
            displayAreas.put(area.getName(), area);
          }
        }
        ScrollPane sp = new ScrollPane();
        sp.setContent(grid);
        bp.setCenter(sp);
        bp.setPadding(new Insets(10, 10, 10, 10));
      }     
    });
  }
  
  /**
   * Scans all added Repositories for a version File and checks, if a newer version is available to 
   * download.
   * @since 1.0 
   */
  private void startCheckUpdateTasks() {
    for (int i = 0; i < repos.size(); i++) {
      UpdateTask task = new UpdateTask(repos.get(i), names.get(i), displayAreas.get(names.get(i)), 
          path, i, this);
      displayAreas.get(names.get(i)).bindProgressBar(task);
      new Thread(task).start();
      new Thread(() -> {
        try {
          Thread.sleep(5000);  
        } catch (InterruptedException e) {
          //Testing Purposes, shouldn't be called.
          e.printStackTrace();
        }
        task.cancel();
      }).start();
    }
  }
  
  public void startSpecificUpdateTask(int index) {
    UpdateTask task = new UpdateTask(repos.get(index), names.get(index), 
        displayAreas.get(names.get(index)), path, index, this);
    displayAreas.get(names.get(index)).bindProgressBar(task);
    new Thread(task).start();
    new Thread(() -> {
      try {
        Thread.sleep(5000);  
      } catch (InterruptedException e) {
        //Testing Purposes, shouldn't be called.
        e.printStackTrace();
      }
      task.cancel();
    }).start();
  }
  
  /**
   * Starts the Launcher without updating/checking the List of published Applications. This will 
   * add installed Folders and .jar-Files to the Launcher, which can then be started.
   * @since 1.0
   */
  private void startWithoutUpdate() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        File f = new File(path);
        ArrayList<String> dirs = new ArrayList<String>();
        for (String s : f.list()) {
          if (!s.contains(".")) {
            dirs.add(s);
          }
        }
        setNames(dirs);
        ArrayList<String> jarList = new ArrayList<String>();
        for (String dir : dirs) {
          boolean added = false;
          f = new File(path + File.separator + dir);
          for (String file : f.list()) {
            if (file.contains(".jar")) {
              jarList.add(file);
              added = true;
            }
          }
          if (!added) {
            jarList.add("");
          }
        }
        setRepos(jarList);
        buildLauncher(false);
      }
    });
  }
  
  public void setRepos(ArrayList<String> repos) {
    this.repos = repos;
  }
  
  public void setNames(ArrayList<String> names) {
    this.names = names;
  }
  
  public static void main(String[] args) {
    GastroToolsLauncher.launch(args);
  }
}