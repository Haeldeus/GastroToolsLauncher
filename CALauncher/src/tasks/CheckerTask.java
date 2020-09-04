package tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import launcher.WTLauncher;

public class CheckerTask extends Task<Void> {

  private ProgressTask prt;
  private Label updates;
  private WTLauncher primary;
  private int index;
  
  public CheckerTask(ProgressTask prt, Label updates, WTLauncher primary, int index) {
    this.prt = prt;
    this.updates = updates;
    this.primary = primary;
    this.index = index;
  }
  
  @Override
  protected Void call() throws Exception {
    URL url;
    BufferedReader br;
    try {
      url = new URL("https://github.com/Haeldeus/CashAssetsLauncher/blob/master/List.txt");
      // Get the input stream through URL Connection
      URLConnection con = url.openConnection();
      con.connect();
      InputStream is = con.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      prt.updateIndicator(++index, "Verbindung hergestellt!");
    } catch (IOException e) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          updates.setText("Keine Verbindung zum Server möglich. Bitte überprüfen Sie Ihre "
              + "Internetverbindung.");
        }        
      });
      prt.setPublishedRepos(new ArrayList<String>());
      br = new BufferedReader(null);
      return null;
    }
    /*
     * Creates an empty String to store version numbers.
     */
    String s = "";
    
    /*
     * A String, that saves the line, that was last read. Will be updated after each br.readLine().
     */
    String line = null;
    
    /*
     * A boolean to determine, if the current part of the InputStream contains 
     * information about the Version.
     */
    boolean list = false;
    
    /*
     * Go through each line of the InputStream to search for version information.
     */
    try {
      while ((line = br.readLine()) != null) {
        if (isCancelled()) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              prt.setResult(false);
              primary.showUpdateFailed("Zeitüberschreitung!");
            }           
          });
          return null;
        }
        /*
         * This String is always at the end of the version information.
         */
        if (line.contains("#End List File")) {
          list = false;
          prt.updateIndicator(++index, "Liste der Anwendungen abgefragt.");
        }
        
        /*
         * We are now at the information part of the File. Here begins the allocation of 
         * all versions that were released.
         */
        if (list) {
          /*
           * Remove HTML-Code from the String.
           */
          while (line.contains(">")) {
            if (isCancelled()) {
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  prt.setResult(false);
                  primary.showUpdateFailed("Zeitüberschreitung!");
                }           
              });
              return null;
            }
            line = line.replaceFirst(line.substring(line.indexOf("<"), 
                line.indexOf(">") + 1), "");
          }
          /*
           * If the Line still contains Information after removing all HTML-Code, then there 
           * are version numbers stored in it. It is added to the String defined before.
           */
          if (line.trim().length() != 0) {
            s = s.concat(line.trim() + System.lineSeparator());
          }
        }
        /*
         * This Line is always at the start of the Information about versions. This is checked 
         * here to remove a line from s.
         */
        if (line.contains("#Begin Version File")) {
          list = true;
          prt.updateIndicator(++index, "Liste der Anwendungen gefunden!");
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    prt.updateIndicator(++index, "Aufteilen der Liste...");
    StringTokenizer st = new StringTokenizer(s, System.lineSeparator());
    
//    st.nextToken();
//    prt.setPublishedVersion(st.nextToken());
//    st.nextToken();
//    ArrayList<String> oldVersions = new ArrayList<String>();
//    while (st.hasMoreTokens()) {
//      oldVersions.add(st.nextToken());
//    }
//    prt.updateIndicator(++index, "Version überprüft.");
//    prt.setOlderVersions(oldVersions);
    return null;
  }

}
