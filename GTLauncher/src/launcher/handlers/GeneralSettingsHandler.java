package launcher.handlers;

import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import loggingtool.LoggingTool;
import settingstool.Settings;
import settingstool.SettingsTool;

/**
 * The Handler for the Settings MenuItem in the MenuBar of the Launcher.

 * @author Haeldeus
 * @version {@value launcher.GastroToolsLauncher#version}
 */
public class GeneralSettingsHandler implements EventHandler<ActionEvent> {

  /**
   * The Stage, that this Handler was called from. Used to initialize the Owner of this 
   * Dialog and the Modality later on.
   */
  private Stage primaryStage;
  
  /**
   * The SettingsTool for this Launcher. Used to read and write to the Settings File.
   */
  private SettingsTool settings;
  
  /**
   * The Constructor for this Handler. Sets all Fields to the given Parameters.

   * @param settings  The SettingsTool for this Launcher.
   * @param primary The primaryStage, this Handler was created by.
   * @since 1.0
   */
  public GeneralSettingsHandler(SettingsTool settings, Stage primary) {
    this.primaryStage = primary;
    this.settings = settings;
  }
  
  @Override
  public void handle(ActionEvent event) {
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "Creates the Settings Dialog...");
    /*
     * Creates a new Stage to display the Settings Scene.
     */
    final Stage dialog = new Stage();
    dialog.initOwner(primaryStage);
    dialog.initStyle(StageStyle.UNDECORATED);
    dialog.initModality(Modality.APPLICATION_MODAL);
    
    /*
     * Creates a new GridPane, that will contain all fields of this Scene.
     */
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "Creates the GridPane...");
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), 
        "Creates the Content for the Settings Dialog.");
    /*
     * Creates a Label, that informs the User, that there are Tooltips with further Information.
     */
    Label desc = new Label("Bewegen Sie die Maus über die einzelnen Textpassagen um im Tooltip "
        + "weitere Informationen zu erhalten.");
    desc.setWrapText(true);
    desc.setMaxWidth(300);
    grid.add(desc, 0, 0);
    GridPane.setColumnSpan(desc, 10);
    GridPane.setRowSpan(desc, 2);
    
    /*
     * Creates a Label, that will describe the Nightmode setting to the User.
     */
    Label nightmode = new Label("Verwende Nachtmodus:");
    grid.add(nightmode, 0, 3);
    
    /*
     * Creates the CheckBox for the Nightmode Setting.
     */
    CheckBox cbNightmode = new CheckBox();
    cbNightmode.setSelected(settings.getValue(Settings.nightmode).equals("1"));
    grid.add(cbNightmode, 1, 3);
    
    /*
     * Creates a Label, that will describe the Timeout Setting to the User.
     */
    Label timeout = new Label("Zeitbeschränkung:");
    grid.add(timeout, 0, 4);
    
    /*
     * Creates the TextField for the Timeout Setting.
     */
    TextField tfTimeout = new TextField(settings.getValue(Settings.timeout));
    tfTimeout.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, 
          String newValue) {
        tfTimeout.setText(newValue.replaceAll("\\D", ""));
      }
    });
    grid.add(tfTimeout, 1, 4);
    
    /*
     * Creates a Save Button to save the settings.
     */
    Button btSave = new Button("Anwenden");
    btSave.setOnMouseClicked(new EventHandler<MouseEvent>() {

      /*
       * Handles the ClickEvent. Converts all Inputs in the Dialog to Information for 
       * the SettingsHandler. Closes the Dialog afterwards.
       */
      @Override
      public void handle(MouseEvent event) {
        HashMap<Settings, String> map = new HashMap<Settings, String>();
        if (cbNightmode.isSelected()) {
          map.put(Settings.nightmode, "1");
        } else {
          map.put(Settings.nightmode, "0");
        }
        map.put(Settings.timeout, tfTimeout.getText());
        LoggingTool.log(getClass(), LoggingTool.getLineNumber(), 
            "Saving the Settings with the Values: " + Settings.nightmode.toString() + "-" 
                + map.get(Settings.nightmode) + "; " + Settings.timeout.toString() + "-" 
                + map.get(Settings.timeout));
        settings.setValues(map);
        dialog.close();
      }
    });
    grid.add(btSave, 0, 6);
    
    /*
     * Creates a Cancel Button to cancel the editing of the Settings.
     */
    Button btCancel = new Button("Abbrechen");
    btCancel.setOnMouseClicked(new EventHandler<MouseEvent>() {

      /*
       * Handles the ClickEvent. Cancels the Editing by simply closing the Dialog without 
       * saving the Edits.
       */
      @Override
      public void handle(MouseEvent event) {
        LoggingTool.log(getClass(), LoggingTool.getLineNumber(), 
            "Closing the Settings Dialog without Saving!");
        dialog.close();
      }
    });
    grid.add(btCancel, 1, 6);
    
    /*
     * Adds a ScrollPane to ensure, that the Grid can be scrolled through, in case more 
     * Settings are added to it.
     */
    ScrollPane sp = new ScrollPane();
    sp.setContent(grid);
    
    /*
     * Adds a BorderPane to ensure, that the Grid is always at the Top of the Stage.
     */
    BorderPane bp = new BorderPane();
    bp.setTop(sp);
    /*
     * Basic Stage Settings, which control the Stage and it's Dimension. Also, displays 
     * the Stage to the User.
     */
    Scene dialogScene = new Scene(bp);
    dialogScene.getStylesheets().add("controlStyle1.css");
    dialog.setScene(dialogScene);
    dialog.setMaxHeight(500);
    dialog.setMaxWidth(200);
    dialog.setResizable(false);
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "Displaying the Settings Dialog!");
    dialog.show();
  }

}
