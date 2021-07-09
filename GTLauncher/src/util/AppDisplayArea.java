package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import tasks.DownloadTask;

/**
 * The Area that displays each single Application added to the Launcher. Every Area can also 
 * display the Update News for this Application.

 * @author Haeldeus
 * @version 1.0
 */
public class AppDisplayArea {
  
  /**
   * The Name of the Application to be displayed.
   */
  private String name;
  
  /**
   * The Path to the Icon for this Application. Might be a local Path as well as an URL.
   */
  private String pathToIcon;
  
  /**
   * A Label, where potential Message will be displayed.
   */
  private Label messageLabel;
  
  /**
   * The Path to the executable File.
   */
  private String path;
  
  /**
   * The Buttons, that are embedded in this Area in an ArrayList.
   */
  private ArrayList<Button> buttons;
  
  /**
   * The ProgressBar, which indicates the Progress of the Update.
   */
  private ProgressBar pb;
  
  /**
   * The Grid, that contains the bottom Part of the DisplayArea.
   */
  private GridPane bottomGrid;
  
  /**
   * The name of the Repository for this Application.
   */
  private String repo;

  /**
   * A Constructor for the Area. This will set all Fields to the given values and initiates 
   * {@link #messageLabel} and {@link #buttons} to default values.

   * @param name  The name of the Application in this Area.
   * @param pathToIcon  The Path to the Icon for the Application.
   * @param path  The Path to the executable File.
   * @param repo  The Name of the Repository for this Application. Used for downloading updates.
   * @since 1.0
   */
  public AppDisplayArea(String name, String pathToIcon, String path, String repo) {
    this.name = name;
    this.path = path;
    this.pathToIcon = pathToIcon;
    this.repo = repo;
    messageLabel = new Label("Messages will be shown here");
    buttons = new ArrayList<Button>();
  }
  
  /**
   * A default Constructor for the Area. This will set all Fields to default values for them to be 
   * set afterwards by the Use of the Setter-Methods.

   * @since 1.0
   */
  public AppDisplayArea() {
    this.setPathToIcon("");
    this.setName("Placeholder");
    this.setPath("");
    this.setRepo("");
    messageLabel = new Label("Messages will be shown here");
    buttons = new ArrayList<Button>();
  }
  
  /**
   * Creates the DisplayArea as a BorderPane.

   * @return The Area as a BorderPane.
   * @since 1.0
   */
  public BorderPane createDisplayArea() {
    /*
     * Creates a new BorderPane, that will inherit the DisplayArea.
     */
    BorderPane bp = new BorderPane();
    /*
     * Creates a new Label, that will display the Application in this DisplayArea and adds it to 
     * the top of the BorderPane.
     */
    Label lbName = new Label(name);
    lbName.setMaxWidth(300);
    lbName.setAlignment(Pos.CENTER);
    bp.setTop(lbName);
    
    /*
     * Creates an ImageView for the Icon of the Application in this DisplayArea.
     */
    Image img = new Image(pathToIcon);
    ImageView iw = new ImageView(img);
    iw.setFitWidth(50);
    iw.setFitHeight(50);
    
    /*
     * Creates a GridPane, that will contain the Icon and the Buttons for this DisplayArea.
     * It will be added to the Center of the BorderPane later.
     */
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    /*
     * Adds the ImageView to the Grid.
     */
    grid.add(iw, 0, 0);
    
    /*
     * Creates a GridPane, that will contain all Buttons for this DisplayArea.
     */
    GridPane buttonPane = new GridPane();
    buttonPane.setHgap(10);
    buttonPane.setVgap(10);
    buttonPane.setAlignment(Pos.CENTER);
    
    /*
     * Adds a Start Button to the DisplayArea with the defined ActionHandler.
     */
    Button start = new Button();    
    Image startImg = new Image("/res/Start.png");
    ImageView startView = new ImageView(startImg);
    startView.setFitWidth(20);
    startView.setFitHeight(20);
    start.setTooltip(new Tooltip("Anwendung starten"));
    start.setGraphic(startView);
    start.setOnAction(new EventHandler<ActionEvent>() {
      @SuppressWarnings("unused") //Will be deleted as soon as the Input Stream Problem is fixed.
      @Override
      public void handle(ActionEvent arg0) {
        /*
         * If the path isn't an empty String, there is an executable File, that will be executed 
         * and the Launcher will be closed afterwards. In case there wasn't an executable File, 
         * nothing happens, except for an Error Message in the Label.
         */
        if (!path.equals("")) {
          try {
            // Run a java application in a separate system process
            Process proc = Runtime.getRuntime().exec("java -jar " + path, null, 
                new File(path.substring(0, path.lastIndexOf(File.separator))));
  
            // Then retrieve the process output
            //TODO: Check if InputStreams can be added to the Console. (Delete SuppressWarnings!)
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            OutputStream out = proc.getOutputStream();
            /*
             * Exits this Process with the default integer 0.
             */
            System.exit(0);
          } catch (IOException e) {
            /*
             * This catch shouldn't happen. But in case a Path was set despite no executable File 
             * exists, this catch is necessary.
             */
            e.printStackTrace();
            messageLabel.setText("Fehler beim Starten von " + name + "!");
          }
        } else {
          messageLabel.setText("Konnte Anwendung nicht starten, da keine ausführbare Datei "
              + "vorhanden ist!");
        }
      }    
    }); 
    buttonPane.add(start, 0, 0);
    buttons.add(start);
    
    /*
     * Adds a Download Button to the Area. Since the Handler will be added later, when the 
     * Download Path is known, it won't be added here.
     */
    Button download = new Button();    
    Image downloadImg = new Image("/res/Download.png");
    ImageView downloadView = new ImageView(downloadImg);
    downloadView.setFitWidth(20);
    downloadView.setFitHeight(20);
    download.setTooltip(new Tooltip("Neue Version herunterladen"));
    download.setGraphic(downloadView);
    buttonPane.add(download, 1, 0);
    buttons.add(download);
    
    /*
     * Adds a Delete Button to the Area. A Handler is added to it as well, which will delete the 
     * Folder of this Application when pressed.
     */
    Button delete = new Button();    
    Image deleteImg = new Image("/res/Delete.png");
    ImageView deleteView = new ImageView(deleteImg);
    deleteView.setFitWidth(20);
    deleteView.setFitHeight(20);
    delete.setTooltip(new Tooltip("Anwendung löschen"));
    delete.setGraphic(deleteView);
    delete.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        //TODO: Implement Deleting Folders.
        messageLabel.setText("Löschen ist bislang noch nicht implementiert. Bitte manuell den "
            + "Ordner entfernen.");
      }
    });
    buttonPane.add(delete, 2, 0);
    buttons.add(delete);
    
    /*
     * Adds the Button Pane to the Grid and lets it fill the remaining space in the width.
     */
    grid.add(buttonPane, 1, 0);
    GridPane.setFillWidth(buttonPane, true);
    
    /*
     * Adds the Grid to the BorderPane.
     */
    bp.setCenter(grid);
    
    /*
     * Creates a new Grid where information can be displayed as well as a ProgressBar, that is 
     * needed when the Application is updated.
     * Adds the MessageLabel to this Grid.
     */
    bottomGrid = new GridPane();
    bottomGrid.add(messageLabel, 0, 0);
    
    /*
     * Creates a new ProgressBar and adds it to the Grid.
     */
    pb = new ProgressBar();
    bottomGrid.add(pb, 0, 1);
    
    /*
     * Adds the Grid to the Bottom of the BorderPane and returns said BorderPane afterwards.
     */
    bp.setBottom(bottomGrid);
    return bp;
  }

  /**
   * Prepares the Area for the Download by removing all unwanted content from the BottomGrid and 
   * adding a ProgressBar to it.

   * @param downloadPath  The URL to the File to be downloaded.
   * @param version The Version of the File to be downloaded.
   * @since 1.0
   */
  private void prepareDownload(String downloadPath, String version) {
    
    /*
     * Adds two new Labels to the bottomGrid to display information to the Client.
     */
    Label updates = new Label();
    Label length = new Label();
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        /*
         * Removes the first Child from the bottomGrid until there are no more Children to remove.
         */
        while (bottomGrid.getChildren().size() != 0) {
          bottomGrid.getChildren().remove(0);
        }
        /*
         * Adds a new ProgressBar to the BottomGrid.
         */
        pb = new ProgressBar();
        bottomGrid.add(pb, 0, 0);
        
        bottomGrid.add(updates, 0, 1);
        
        bottomGrid.add(length, 0, 2);
        
      }
    });
    DownloadTask task = new DownloadTask(downloadPath, new File(path), updates, length, 
        version, this);
    bindProgressBar(task);
    new Thread(task).start();
  }
  
  /**
   * The Method to be called, when the download of the Application was finished, either 
   * successfully or failed/timeout.

   * @param text  The Text, that will be displayed to the User after editing the Area.
   * @since 1.0
   */
  public void downloadFinished(String text) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        /*
         * Removes the first Child from the bottomGrid until there are no more Children to remove.
         */
        while (bottomGrid.getChildren().size() != 0) {
          bottomGrid.getChildren().remove(0);
        }
        
        messageLabel.setText(text);
        bottomGrid.add(messageLabel, 0, 0);
        
        pb = new ProgressBar();
        bottomGrid.add(pb, 0, 1);
        hideProgressBar();
        
        switchButtons(false, true, false);
      }
    });
  }
  
  /**
   * Adds a Handler to the DownloadButton to enable the Download. As of now, download is not yet 
   * implemented.

   * @param downloadPath  The Path to the File to be downloaded.
   * @since 1.0
   */
  public void enableDownload(String downloadPath, String version) {
    System.out.println("DEBUG: Came to Download");
    System.out.println("DEBUG: Path: " + path);
    switchDownloadButton(false);
    /*
     * Since the order of the Buttons is fixed, the Download Button can be called directly by 
     * getting Button 1 from the ArrayList of Buttons.
     */
    buttons.get(1).setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        /*
         * Testing Purposes. Will be deleted afterwards.
         */
        System.out.println("DEBUG: Path: " + path);
        prepareDownload(downloadPath, version);
      }     
    });
  }
  
  /**
   * Binds the ProgressBar to the given Task's ProgressProperty. This enables the Task to update 
   * the ProgressBar. Currently, this is only enabled for Void-Tasks, since there are no other 
   * Tasks, that have to be bound to the ProgressBar.

   * @param task  The Task, the ProgressBar will be bound to.
   * @since 1.0
   */
  public void bindProgressBar(Task<Void> task) {
    if (!pb.isVisible()) {
      pb.setVisible(true);
      bottomGrid.add(pb, 0, 1);
    }
    pb.progressProperty().bind(task.progressProperty());
  }
  
  /**
   * Sets the given Message as Text for the {@link #messageLabel} to inform the User with a Text.

   * @param message The Message to be displayed as a String.
   * @since 1.0
   */
  public void updateMessage(String message) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        messageLabel.setText(message);
      }    
    });
  }
  
  /**
   * Hides the ProgressBar from the BottomGrid. Since a new ProgressBar is added every time, it has 
   * to be shown again, it will also be removed from the BottomGrid to ensure it is correctly 
   * hidden. This might be fixed later to a better workaround.

   * @since 1.0
   */
  public void hideProgressBar() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        pb.setVisible(false);
        //TODO: See Comment below.
        //After calling prepareDownload(), there is only 1 child in bottomGrid, so this line might 
        //cause some Problems.
        bottomGrid.getChildren().remove(1);
      }
    });
  }
  
  /**
   * Returns the {@link #messageLabel}. This is used in the UpdateTask to edit the EventHandlers of 
   * the Label.

   * @return  The MessageLabel.
   * @since 1.0
   */
  public Label getMessageLabel() {
    return this.messageLabel;
  }
  
  /**
   * Returns the Name of the Application. In case, there was no Internet Connection 
   * detected upon starting the Launcher, the Folder Name will be displayed.

   * @return The Name of the Application.
   * @since 1.0
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the Name of the Application. This is used, if the Name wasn't set upon creating a new 
   * DisplayArea.

   * @param name The Name to be set. Will be saved in {@link #name}.
   * @since 1.0
   * @see #name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the Path to the Icon of the Application, that was added to this Area.

   * @return The Path to the Icon of the Application, that was either set upon creating the Area 
   *      or via {@link #setPathToIcon(String)}.
   * @since 1.0
   */
  public String getPathToIcon() {
    return pathToIcon;
  }

  /**
   * Sets the Path to the Icon of the Application. This is used, if the Path wasn't set upon 
   * creating a new DisplayArea.

   * @param pathToIcon  The Path to the Icon. This might be a local Path or a URL.
   * @since 1.0
   */
  public void setPathToIcon(String pathToIcon) {
    this.pathToIcon = pathToIcon;
  }

  /**
   * Returns the Path to the Executable File.

   * @return The Path to the Executable File as a String.
   * @since 1.0
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the Path to the executable File. If the sub Folders don't exist, they will be created.

   * @param path  The Path to the executable File.
   * @since 1.0
   */
  public void setPath(String path) {
    this.path = path;
    if (path != "") {
      File pathEx = new File(this.path.substring(0, path.lastIndexOf(File.separator)));
      if (!pathEx.exists()) {
        pathEx.mkdirs();
      }
    }
  }
  
  /**
   * Switches all Buttons' disabled Status to the given values.

   * @param start The disabled Status of the Start Button as a boolean.
   * @param download  The disabled Status of the Download Button as a boolean.
   * @param delete  The disabled Status of the Delete Button as a boolean.
   * @since 1.0
   */
  public void switchButtons(boolean start, boolean download, boolean delete) {
    this.buttons.get(0).setDisable(start);
    this.buttons.get(1).setDisable(download);
    this.buttons.get(2).setDisable(delete);
  }
  
  /**
   * Switches the disabled Status of the Download Button to the given value.

   * @param disable The disabled status of the Download Button as a boolean value.
   * @since 1.0
   */
  public void switchDownloadButton(boolean disable) {
    this.buttons.get(1).setDisable(disable);
  }
  
  /**
   * Returns, if the Start Button is disabled. This is used to prevent the Update Task from 
   * scanning for updates for an Application, that is corrupted.

   * @return  {@code true}, if the Start Button is disabled, {@code false} if it is enabled.
   * @since 1.0
   */
  public boolean isStartDisabled() {
    return this.buttons.get(0).isDisabled();
  }

  /**
   * Returns the Name of the Repository.

   * @return  The Name of the Repository for this Application.
   * @see #repo
   * @since 1.0
   */
  public String getRepo() {
    return repo;
  }

  /**
   * Sets the Name of the Repository to the given String.

   * @param repo  The new Name of the Repository.
   * @see #repo
   * @since 1.0
   */
  public void setRepo(String repo) {
    this.repo = repo;
  }
}