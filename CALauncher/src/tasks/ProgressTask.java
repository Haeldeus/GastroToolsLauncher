package tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javafx.concurrent.Task;

public class ProgressTask extends Task<Void> {
  @Override
  protected Void call() {
    for (int i = 0; i <= 10000; i++) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      updateProgress(i, 10000);
    }
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
    return null;
  }
}