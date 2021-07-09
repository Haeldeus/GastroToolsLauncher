package launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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
   * The Version of this Launcher. This is needed to write the version Number on the hard drive, so 
   * the Updater can keep this Launcher updated.
   */
  private static final String version = "0.99";
  
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
   * Repositories' names. These Names will be shown to the User as the Name of the Applications.
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
      //TODO: Check if replacement of "app" String is still necessary
      /*
       * Checks, if the current path has an "/app"-String in it. In this case, the Launcher was 
       * started directly and not by the Updater. Still, this Method should change the path, since 
       * there might be wrong, old or custom Folders in the Directory and this is one way to lower 
       * the Risk of corruption in the Files.
       */
      int index = path.lastIndexOf(File.separator + "app");
      /*
       * Erases the last "app"-String from the Path and everything after that.
       */
      if (index >= 0) {
        path = path.substring(0, index);
      }
      path = path.concat(File.separator + "app" + File.separator);
      File f = new File(path);
      if (!f.exists()) {
        f.mkdirs();
      }
      /*
       * Creates a new File called "Version.txt" in the newly created app-Folder.
       */
      FileWriter fw = new FileWriter(path + "Version.txt");
      System.out.println("DEBUG: Created File at: " + path + "Version.txt");
      /*
       * Writes the current version into the File and closes the FileWriter afterwards.
       */
      fw.write("" + version);
      fw.close(); 
    } catch (IOException e) {
      e.printStackTrace();
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
    //TODO: Add possibility to change the timeout of the CheckerTask. (Setting?)
    ProgressTask pt = new ProgressTask(this.updatesLabel, this, 5000);
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
          /*
           * Iterates through the list of Repositories and adds all of them to the Launcher.
           */
          for (int i = 0; i < names.size(); i++) {
            /*
             * Creates a new Area, which will show the Repository's executable File as well as a 
             * Possibility to download the latest Patch and delete the whole Folder.
             */
            AppDisplayArea area = new AppDisplayArea();
            /*
             * Sets the Name of the Area to the Name of the Repository. This will be shown as Title 
             * for this Area.
             */
            area.setName(names.get(i));
            /*
             * A boolean, which will determine if an executable File was detected in the Folder.
             */
            boolean startDisable = false;
            /*
             * Checks, if a valid executable Filename was entered into the list and added to the 
             * Hard Drive of the Client. If not, the Client is informed and the startButton will be 
             * disabled by the use of the boolean above.
             */
            System.out.println("DEBUG: Exec File at: " + path + names.get(i) + File.separator 
                + repos.get(i) + ".jar");
            if (repos.get(i) == "" || repos.get(i) == null 
                || !(new File(path + names.get(i) + File.separator + repos.get(i) 
                + ".jar").exists())) {
              area.setPath("");
              area.updateMessage("Keine ausführbare Datei gefunden!");
              area.setRepo("");
              System.out.println("DEBUG: No exec found");
              startDisable = true;
              /*
               * If a valid executable Filename was added to the List and this File is on the Hard 
               * Drive, the Path of the DisplayArea will be set to this File.
               */
            } else {
              area.setPath(path + names.get(i) + File.separator + repos.get(i));
              area.setRepo(repos.get(i));
            }
            /*
             * Adds an Icon to the AppDisplayArea.
             */
            //TODO: Add real Icon.
            area.setPathToIcon("/res/Default.png");
            /*
             * Adds the created Area to the Grid and enables/disables the buttons accordingly.
             */
            grid.add(area.createDisplayArea(), 0, i);
            area.switchButtons(startDisable, false, false);
            /*
             * Adds the created area to the List of Areas.
             */
            displayAreas.put(area.getName(), area);
          }
          /*
           * Starts the Task to check for Updates for all Areas.
           */
          startCheckUpdateTasks();
        } else {
          /*
           * In case that no connection was found, there might be different steps to ensure 
           * functionality. As of now, the only difference is in the switchButtons-Method, as 
           * "download" has to be disabled by default.
           */
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
        /*
         * Creates a ScrollPane, which enables scrolling through all DisplayAreas.
         */
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
    /*
     * Iterates through all Repositories added by the CheckerTask to check for updates.
     */
    for (int i = 0; i < repos.size(); i++) {
      
      /*
       * Gets the current displayArea, that will be checked for updates.
       */
      AppDisplayArea area = displayAreas.get(names.get(i));

      /*
       * The UpdateTask, that will be configured and initialized in the next step.
       */
      UpdateTask task;
      /*
       * If the Start Button was disabled, no executable File was found. Therefore, no check should 
       * be performed, instead a Download should be offered. This is done via the AutoUpdate 
       * parameter in the UpdateTask.
       */
      System.out.println("DEBUG: isStartDisabled: " + area.isStartDisabled());
      if (!area.isStartDisabled()) {
        task = new UpdateTask(repos.get(i), names.get(i), 
            displayAreas.get(names.get(i)), path, i, this, false);
      } else {
        task = new UpdateTask(repos.get(i), names.get(i), 
            displayAreas.get(names.get(i)), path, i, this, true);
      }
      /*
       * Binds the ProgressBar of the DisplayArea to the UpdateTask.
       */
      area.bindProgressBar(task);
      /*
       * Starts the Task. Also starts a second Thread to interrupt the Task after a given amount 
       * of time.
       */
      new Thread(task).start();
      new Thread(() -> {
        try {
          //TODO: Add possibility to change the timeout of the CheckerTask. (Setting?)
          Thread.sleep(5000);  
        } catch (InterruptedException e) {
          //Testing Purposes, shouldn't be called.
          e.printStackTrace();
        }
        task.cancel();
      }).start();
    }
  }

  /**
   * Starts the UpdateTasks specified by their index in the given ArrayList {@code indices}. This 
   * is used to only start a subset of the Tasks, which is useful, when having to manually update 
   * the Applications of this Launcher.

   * @param indices The ArrayList of Integers with all indices of the Tasks, that will be started 
   *      without starting the other Tasks.
   * @since 1.0
   */
  public void startSpecificUpdateTasks(ArrayList<Integer> indices) {
    /*
     * Basically the same as startCheckUpdateTasks(), except for this Iteration over the given 
     * ArrayList, that contains all indices for the UpdateTasks to start. Every other Task won't be 
     * started.
     */
    for (int index : indices) {
      /*
       * Gets the current displayArea, that will be checked for updates.
       */
      AppDisplayArea area = displayAreas.get(names.get(index));

      /*
       * The UpdateTask, that will be configured and initialized in the next step.
       */
      UpdateTask task;
      /*
       * If the Start Button was disabled, no executable File was found. Therefore, no check should 
       * be performed, instead a Download should be offered. This is done via the AutoUpdate 
       * parameter in the UpdateTask.
       */
      if (!area.isStartDisabled()) {
        task = new UpdateTask(repos.get(index), names.get(index), 
            displayAreas.get(names.get(index)), path, index, this, false);
      } else {
        task = new UpdateTask(repos.get(index), names.get(index), 
            displayAreas.get(names.get(index)), path, index, this, true);
      }
      displayAreas.get(names.get(index)).bindProgressBar(task);
      new Thread(task).start();
      new Thread(() -> {
        try {
          //TODO: Add possibility to change the timeout of the CheckerTask. (Setting?)
          Thread.sleep(5000);  
        } catch (InterruptedException e) {
          //Testing Purposes, shouldn't be called.
          e.printStackTrace();
        }
        task.cancel();
      }).start();
    }
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
        /*
         * Creates a new File, which is used to check for all Folders in the working Directory. 
         * These will be saved in the created ArrayList.
         */
        File f = new File(path);
        ArrayList<String> dirs = new ArrayList<String>();
        /*
         * Lists all Files and Folders in the current Directory and iterates through this List.
         */
        for (String s : f.list()) {
          /*
           * If a File contains a dot, it isn't a Folder but a File and can be disregarded.
           */
          if (!s.contains(".")) {
            /*
             * Since there is no dot, the current File is a Directory and will be added to dirs.
             */
            dirs.add(s);
          }
        }
        /*
         * Sets the created List as Names of Repositories.
         */
        setNames(dirs);
        /*
         * Creates a new ArrayList, where all executable Files will be stored in.
         */
        ArrayList<String> jarList = new ArrayList<String>();
        /*
         * Goes through all Directories and checks for executable Files.
         */
        for (String dir : dirs) {
          /*
           * Used to exit the Loop, whenever an executable File was found.
           */
          boolean added = false;
          /*
           * Concatenates the directory's Name to the path to list all Files in it.
           */
          f = new File(path + File.separator + dir);
          for (String file : f.list()) {
            /*
             * Checks, if an executable was added to the List, if yes this exits the Loop.
             */
            if (added) {
              break;
              /*
               * Currently, only .jar Files are considered as executable Files.
               */
            } else if (file.contains(".jar")) {
              /*
               * Adds the File to the List and sets added to true to exit the Loop.
               */
              jarList.add(file);
              added = true;
            }
          }
          /*
           * In case no executable was found, an empty String is added which will be identified 
           * by Launcher when building the Area as a no executable.
           */
          if (!added) {
            jarList.add("");
          }
        }
        /*
         * Sets the List as List of Repositories. Also builds the Launcher with >false< to let him 
         * know, that there is no connection.
         */
        setRepos(jarList);
        buildLauncher(false);
      }
    });
  }
  
  /**
   * Sets {@link #repos} to the given ArrayList of Strings.

   * @param repos The List of Strings, that describes the Names of the Repositories added to the 
   *      Launcher.
   * @since 1.0
   */
  public void setRepos(ArrayList<String> repos) {
    this.repos = repos;
  }
  
  /**
   * Sets {@link #names} to the given ArrayList of Strings.

   * @param names The List of Strings, that describes the Names of the Folders for each Repository. 
   *      These are the clear Names of each Repository instead of the Repository-Name itself.
   * @since 1.0
   */
  public void setNames(ArrayList<String> names) {
    this.names = names;
  }
  
  /**
   * The Main Method for this Application. This will start the Launcher.

   * @param args  Unused, but has to be passed to the Launch-MEthod. Can be {@code null}.
   * @since 1.0
   */
  public static void main(String[] args) {
    try {
      System.setOut(new PrintStream(new File("LogFile.txt")));
      System.setErr(new PrintStream(new File("ErrorLogs.txt")));
    } catch (Exception e) {
      e.printStackTrace();
    }
    GastroToolsLauncher.launch(args);
  }
}