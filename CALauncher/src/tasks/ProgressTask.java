package tasks;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import launcher.WTLauncher;

public class ProgressTask extends Task<Void> {
  
  private Label updates;
  
  private WTLauncher primary;
  
  private int index;
  
  private int max;
  
  private ArrayList<String> publishedRepos;
  
  private ArrayList<String> repoNames;
  
  private boolean success;
  
  public ProgressTask(Label updates, WTLauncher primary) {
    this.updates = updates;
    this.primary = primary;
    index = 1;
    max = 100;
  }
  
  @Override
  protected Void call() {    
    /*
    for (int i = 0; i <= 10000; i++) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      updateProgress(i, 10000);
    }
    */
    CheckerTask task = new CheckerTask(this, updates, primary, index);
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
    /*
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
    */
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