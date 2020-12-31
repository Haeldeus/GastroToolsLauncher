package tasks;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import launcher.GastroToolsLauncher;

public class ProgressTask extends Task<Void> {
  
  private Label updates;
  
  private GastroToolsLauncher primary;
  
  private int index;
  
  private int max;
  
  private ArrayList<String> publishedRepos;
  
  private ArrayList<String> repoNames;
  
  private boolean success;
  
  public ProgressTask(Label updates, GastroToolsLauncher primary) {
    this.updates = updates;
    this.primary = primary;
    index = 1;
    max = 100;
  }
  
  @Override
  protected Void call() {    
    CheckerTask task = new CheckerTask(this, updates, primary, index);
    new Thread(task).start();
    new Thread(() -> {
      try {
        Thread.sleep(5000);  
      } catch (InterruptedException ie) {
        //Testing Purposes, shouldn't be called.
        ie.printStackTrace();
      }
      task.cancel();
    }).start();
    while (publishedRepos == null) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ie) {
        //Nothing to do here, since this shouldn't be called at any time.
      }
    }
    if (success) {
      updateIndicator(max, "Liste aller Anwendungen gefunden!");
      primary.setNames(repoNames);
      primary.setRepos(publishedRepos);
      primary.buildLauncher(true);
      return null;
    } else {
      updateIndicator(max, updates.getText());
      primary.setNames(new ArrayList<String>());
      primary.setRepos(new ArrayList<String>());
      return null;
    }
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
  
  protected void setResult(boolean success) {
    this.success = success;
  }
  
  protected void setPublishedRepos(ArrayList<String> repos) {
    this.publishedRepos = repos;
  }
  
  protected void setRepoNames(ArrayList<String> repoNames) {
    this.repoNames = repoNames;
  }
}