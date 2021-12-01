package launcher.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import loggingtool.LoggingTool;

/**
 * The Handler for the VersionInfo MenuItem of the Launcher.

 * @author Haeldeus
 * @version {@value launcher.GastroToolsLauncher#version}
 */
public class VersionHandler implements EventHandler<ActionEvent> {

  @Override
  public void handle(ActionEvent event) {
    // TODO Auto-generated method stub
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "Creating VersionInfo Dialog...");
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "Unfinished! Aborting...");
  }

}
