package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import tasks.UpdateTask;

public class AppDisplayArea {
  
  private String name;
  
  private String pathToIcon;
  
  private Label messageLabel;
  
  private String path;
  
  private ArrayList<Button> buttons;
  
  private ProgressBar pb;
  
  private GridPane bottomGrid;

  public AppDisplayArea(String name, String pathToIcon, String path) {
    this.name = name;
    this.path = path;
    this.pathToIcon = pathToIcon;
    messageLabel = new Label("Message will be shown here");
    buttons = new ArrayList<Button>();
  }
  
  public AppDisplayArea() {
    this.setPathToIcon("");
    this.setName("Placeholder");
    this.setPath("");
    messageLabel = new Label("Message will be shown here");
    buttons = new ArrayList<Button>();
  }
  
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

  private void prepareDownload() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        while (bottomGrid.getChildren().size() != 0) {
          bottomGrid.getChildren().remove(0);
        }
        pb = new ProgressBar();
        bottomGrid.add(pb, 0, 0);
      }
    });
  }
  
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
  
  public void bindProgressBar(Task task) {
    if (!pb.isVisible()) {
      pb.setVisible(true);
      bottomGrid.add(pb, 0, 1);
    }
    pb.progressProperty().bind(task.progressProperty());
  }
  
  public void updateMessage(String message) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        messageLabel.setText(message);
      }    
    });
  }
  
  public void hideProgessBar() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        pb.setVisible(false);
        bottomGrid.getChildren().remove(1);
      }
    });
  }
  
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
   * @param pathToIcon the Icon to set
   */
  public void setPathToIcon(String pathToIcon) {
    this.pathToIcon = pathToIcon;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
    File pathEx = new File(this.path);
    if (!pathEx.exists()) {
      this.path = "";
    }
  }
  
  public void switchButtons(boolean start, boolean download, boolean delete) {
    this.buttons.get(0).setDisable(start);
    this.buttons.get(1).setDisable(download);
    this.buttons.get(2).setDisable(delete);
  }
  
  public void switchDownloadButton(boolean disable) {
    this.buttons.get(1).setDisable(disable);
  }
}