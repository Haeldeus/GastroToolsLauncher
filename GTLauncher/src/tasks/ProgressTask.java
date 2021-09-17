package tasks;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import launcher.GastroToolsLauncher;
import settingstool.Settings;
import settingstool.SettingsTool;
import tool.LoggingTool;

/**
 * A Task, that will call a CheckerTask and controls the Execution of that Task. Afterwards this 
 * will update the Launcher with the Lists that were fetched by the CheckerTask.

 * @author Haeldeus
 * @version {@value launcher.GastroToolsLauncher#version}
 */
public class ProgressTask extends Task<Void> {
  
  /**
   * The Label, that will display information to the User.
   */
  private Label updates;
  
  /**
   * The Launcher-Object that called this Task. This Field is used to update the Launcher with the 
   * fetched Lists.
   */
  private GastroToolsLauncher primary;
  
  /**
   * An Integer value, that counts the amount of steps done by this Task. Will be used to display 
   * the progress to the User.
   */
  private int index;
  
  /**
   * The maximum amount of steps to be done by this Task.
   */
  private int max;
  
  /**
   * The List of Repositories, that were published to the Launcher.
   */
  private ArrayList<String> publishedRepos;
  
  /**
   * The clear Names of the Repositories, that were published to the Launcher.
   */
  private ArrayList<String> repoNames;
  
  /**
   * A boolean value, that determines, if the CheckerTask was executed successfully.
   */
  private boolean success;
  
  /**
   * The time before the CheckerTask will be cancelled in milliseconds.
   */
  private int timeout;
  
  /**
   * The Constructor for this Task. Will set the Fields to the given Parameters and prepares the 
   * ProgressIndicator by initializing the {@link #index} and {@link #max}.

   * @param updates The Label, that will display information to the User.
   * @param primary The Launcher-Object, that called this Task.
   * @param iteration The current try to reach the Server.
   * @param settings  The SettingsTool to get the timeoutValue.
   * @since 1.0
   */
  public ProgressTask(Label updates, GastroToolsLauncher primary, int iteration, 
      SettingsTool settings) {
    this.updates = updates;
    this.primary = primary;
    this.timeout = iteration * Integer.parseInt(settings.getValue(Settings.timeout));
    index = 1;
    max = 7;
  }
  
  @Override
  protected Void call() {
    /*
     * Creates a new CheckerTask, that will be started by this Task.
     */
    CheckerTask task = new CheckerTask(this, primary, index);
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), 
        "Starting a new CheckerTask to get the List of supported Applications. Timeout is: " 
            + timeout + "ms");
    new Thread(task).start();
    /*
     * Starts a second Thread, that will cancel the CheckerTask after the specified Time.
     */
    new Thread(() -> {
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException ie) {
        //Testing Purposes, shouldn't be called.
        ie.printStackTrace();
      }
      task.cancel();
    }).start();
    /*
     * As long as the Lists are null, the CheckerTask isn't finished. So this Task will wait for 
     * 50ms and check again.
     */
    while (publishedRepos == null) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ie) {
        //Nothing to do here, since this shouldn't be called at any time.
      }
    }
    /*
     * If the CheckerTask finished successfully, the User will be informed, if not the current Text 
     * of the updates-Label is used as Text. This ensures, that a potential Error Message gets 
     * passed to the Launcher. Either way, the Launcher will be updated with the Lists and this 
     * Task is terminated.
     */
    //TODO: Check if this works! Else: add these lists in the if-clause.
    primary.setNames(repoNames);
    primary.setRepos(publishedRepos);
    if (success) {
      updateIndicator(max, "Liste aller Anwendungen gefunden!");
      primary.buildLauncher(true);
    } else {
      updateIndicator(max, updates.getText());
    }
    LoggingTool.log(getClass(), LoggingTool.getLineNumber(), "ProgressTask finished.");
    return null;
  }
  
  /**
   * Updates the ProgressIndicator with the given value and Text to be displayed.

   * @param value The Value, the Indicator will be updated with. The Percentage displayed will be 
   *      {@code (value/max)}%
   * @param text  The Text, that will be displayed in the updates-Label below the Indicator.
   * @see #max
   * @see #updates
   * @since 1.0
   */
  protected void updateIndicator(int value, String text) {
    index = value;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        updates.setText(text);
        updateProgress(value, max);
      }      
    });
  }
  
  /**
   * Sets {@link #success} to the given value. This determines, if the CheckerTask terminated 
   * successfully or ran into an Exception.

   * @param success The new value of {@link #success}. Determines, if the Task was successful.
   * @since 1.0
   */
  protected void setResult(boolean success) {
    this.success = success;
  }
  
  /**
   * Sets {@link #publishedRepos} to the given value. This is done in the CheckerTask.

   * @param repos The new value of {@link #publishedRepos}.
   * @since 1.0
   */
  protected void setPublishedRepos(ArrayList<String> repos) {
    this.publishedRepos = repos;
  }
  
  /**
   * Sets {@link #repoNames} to the given value. This is done in the CheckerTask.

   * @param repoNames The new value of {@link #repoNames}.
   * @since 1.0
   */
  protected void setRepoNames(ArrayList<String> repoNames) {
    this.repoNames = repoNames;
  }
  
  @Override
  public String toString() {
    return "ProgressTask " + this.hashCode();
  }
}