package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

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
   * A Constructor for the Area. This will set all Fields to the given values and initiates 
   * {@link #messageLabel} and {@link #buttons} to default values.

   * @param name  The name of the Application in this Area.
   * @param pathToIcon  The Path to the Icon for the Application.
   * @param path  The Path to the executable File.
   * @since 1.0
   */
  public AppDisplayArea(String name, String pathToIcon, String path) {
    this.name = name;
    this.path = path;
    this.pathToIcon = pathToIcon;
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
    messageLabel = new Label("Messages will be shown here");
    buttons = new ArrayList<Button>();
  }
  
  /**
   * Creates the DisplayArea as a BorderPane.

   * @return The Area as a BorderPane.
   * @since 1.0
   */
  public BorderPane createDisplayArea() {
    BorderPane bp = new BorderPane();
    Label lbName = new Label(name);
    lbName.setMaxWidth(300);
    lbName.setAlignment(Pos.CENTER);
    bp.setTop(lbName);
    
    Image img = new Image(pathToIcon);
    ImageView iw = new ImageView(img);
    iw.setFitWidth(50);
    iw.setFitHeight(50);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    grid.add(iw, 0, 0);
    
    GridPane buttonPane = new GridPane();
    buttonPane.setHgap(10);
    buttonPane.setVgap(10);
    buttonPane.setAlignment(Pos.CENTER);
    //TODO: Replace Button Text with Icons.
    Button start = new Button("S");
    start.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (!path.equals("")) {
          try {
            // Run a java application in a separate system process
            Process proc = Runtime.getRuntime().exec("java -jar " + path);
  
            // Then retrieve the process output
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            System.exit(0);
          } catch (IOException e) {
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
    
    Button download = new Button("DL");
    buttonPane.add(download, 1, 0);
    buttons.add(download);
    
    Button delete = new Button("D");
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
    
    grid.add(buttonPane, 1, 0);
    GridPane.setFillWidth(buttonPane, true);
    
    bp.setCenter(grid);
    
    bottomGrid = new GridPane();
    bottomGrid.add(messageLabel, 0, 0);
    
    pb = new ProgressBar();
    bottomGrid.add(pb, 0, 1);
    
    bp.setBottom(bottomGrid);
    return bp;
  }

  /**
   * Prepares the Area for the Download by removing all unwanted content from the BottomGrid and 
   * adding a ProgressBar to it.

   * @since 1.0
   */
  private void prepareDownload() {
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
      }
    });
  }
  
  /**
   * Adds a Handler to the DownloadButton to enable the Download. As of now, download is not yet 
   * implemented.

   * @param downloadPath  The Path to the File to be downloaded.
   * @since 1.0
   */
  public void enableDownload(String downloadPath) {
    buttons.get(1).setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        System.out.println("Path: " + path);
        //TODO: Implement Download.
        prepareDownload();
        messageLabel.setText("Download ist bislang noch nicht implementiert. Bitte manuell den "
            + "Ordner aktualisieren.");
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
  public void hideProgessBar() {
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
    File pathEx = new File(this.path);
    if (!pathEx.exists()) {
      this.path = "";
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
}