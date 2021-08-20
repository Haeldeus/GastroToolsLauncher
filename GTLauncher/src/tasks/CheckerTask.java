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
import launcher.GastroToolsLauncher;

/**
 * The Task, which Objects will check for the Repositories and their Names, that were added to the 
 * Launcher's List.

 * @author Haeldeus
 * @version 1.0
 */
public class CheckerTask extends Task<Void> {

  /**
   * The ProgressTask, which started this Task. This is used to update the UI via 
   * {@link ProgressTask#updateIndicator(int, String)}.
   */
  private ProgressTask prt;
  
  /**
   * The Launcher-Object, which is the primary Stage of this Application. Used to create a 
   * Fail-State, when the Update failed or no Connection could be established.
   */
  private GastroToolsLauncher primary;
  
  /**
   * The amount of steps already performed. This is used in the ProgressIndicator that is shown to 
   * the User and updated after each major step.
   */
  private int index;
  
  /**
   * The Constructor for all Objects of this Class. Sets all fields to the given Parameters. See 
   * the Fields' Documentation for further Information.

   * @param prt The ProgressTask, that will be set as {@link #prt}.
   * @param primary The Launcher-Object, that will be set as {@link #primary}.
   * @param index The Integer-Value, that will be set as {@link #index}.
   * @since 1.0
   */
  public CheckerTask(ProgressTask prt, GastroToolsLauncher primary, int index) {
    this.prt = prt;
    this.primary = primary;
    this.index = index;
  }
  
  @Override
  protected Void call() throws Exception {
    /*
     * Creates needed Variables. The URL is used to connect to the List and the Reader is used to 
     * read from the URL. The Index is used to differ between possible Exception causes.
     */
    URL url;
    BufferedReader br;
    int counter = 0;
    try {
      url = new URL("https://github.com/Haeldeus/CashAssetsLauncher/blob/master/List.txt");
      counter++;
      // Get the input stream through URL Connection
      URLConnection con = url.openConnection();
      counter++;
      con.connect();
      counter++;
      /*
       * Fetches the InputStream from the Connection and sets the Reader to read from this Stream.
       */
      InputStream is = con.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      /*
       * Updates the User via the Indicator and the given Text.
       */
      prt.updateIndicator(++index, "Verbindung hergestellt!");
    } catch (IOException e) {
      /*
       * Catches all possible Errors. Since the counter gets incremented after each Operation, that 
       * might throw an Exception, this counter is used to display different Texts to the User. 
       * This can be used for better troubleshooting.
       */
      if (counter == 0) {
        updateFailed("Fehler bei der URL-Auflösung. Bitte melden Sie diesen Fehler "
            + "dem Entwickler.");
      } else if (counter == 1) {
        updateFailed("Keine Verbindung zum Server möglich. Bitte überprüfen Sie Ihre "
            + "Internetverbindung. Index = " + counter);
      } else if (counter == 2) {
        updateFailed("Fehler bei der Kommunikation zum Server, bitte melden Sie dieses "
            + "Problem dem Entwickler. Index = " + counter);
      } else {
        updateFailed("Antwort vom Server nicht erfolgreich, bitte melden Sie dieses "
            + "Problem dem Entwickler. Index = " + counter);
      }
      /*
       * Creates a new Reader with null as InputStream to prevent Compiler Errors. Returns null 
       * afterwards to stop the execution of this Task.
       */
      br = new BufferedReader(null);
      return null;
    }
    /*
     * Creates an empty String to store all Repository's Names. After successfully completing the 
     * Task this String will contain all Names (clear Names and "true" names) of all Repositories 
     * added to the Launcher, each one separated from the others with a Line Separator.
     */
    String s = "";
    
    /*
     * A String, that saves the line, that was last read. Will be updated after each br.readLine().
     */
    String line = null;
    
    /*
     * A boolean to determine, if the current part of the InputStream contains 
     * information about the List of Repositories.
     */
    boolean list = false;
    
    /*
     * Go through each line of the InputStream to search for the List of Repositories.
     */
    try {
      while ((line = br.readLine()) != null) {
        /*
         * Checks, if the Process was cancelled. If yes, a default state will be given to the 
         * ProgressTask.
         */
        if (isCancelled()) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              prt.setResult(false);
              prt.setRepoNames(new ArrayList<String>());
              prt.setPublishedRepos(new ArrayList<String>());
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
            /*
             * Checks, if this Task was cancelled, because it took too long. In this case, the User 
             * will be updated.
             */
            if (isCancelled()) {
              updateFailed("Zeitüberschreitung!");
              return null;
            }
            /*
             * Removes the HTML Tags from the String.
             */
            line = line.replaceFirst(line.substring(line.indexOf("<"), 
                line.indexOf(">") + 1), "");
          }
          /*
           * If the Line still contains Information after removing all HTML-Code, then there 
           * are Repository Names stored in it. It is added to the String defined before.
           */
          if (line.trim().length() != 0) {
            s = s.concat(line.trim() + System.lineSeparator());
          }
        }
        /*
         * This Line is always at the start of the Information about the Repositories. This is 
         * checked here to remove an empty line from s.
         */
        if (line.contains("#Begin List File")) {
          list = true;
          prt.updateIndicator(++index, "Liste der Anwendungen gefunden!");
        }
      }
    } catch (IOException e) {
      /*
       * In case the Line couldn't be read, this catches the Exception.
       */
      e.printStackTrace();
    }
    /*
     * Updates the User, that the List was requested and the Task will now separate it.
     */
    prt.updateIndicator(++index, "Aufteilen der Liste...");
    /*
     * A StringTokenizer to separate each Line in the String of Lists.
     */
    StringTokenizer st = new StringTokenizer(s, System.lineSeparator());
    
    /*
     * Creates ArrayLists to save the Repositories' Names and their clear Names.
     */
    ArrayList<String> repos = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    
    /*
     * As long as there are still Tokens, these Tokens will be separated into the clear Names and 
     * true Names.
     */
    while (st.hasMoreTokens()) {
      /*
       * Replaces the Token, that is used in the List to separate the clear Name from the true Name 
       * with a Single character, that can be used with the Tokenizer.
       */
      String tmp = st.nextToken().replace("--SEP--", "|");
      /*
       * Creates a Tokenizer to separate each Line into the repository's Name and clear Name and 
       * adds these Strings into the Lists.
       */
      StringTokenizer tokenizer = new StringTokenizer(tmp, "|");
      repos.add(tokenizer.nextToken());
      names.add(tokenizer.nextToken());
    }
    /*
     * Updates the Fields in the parent ProgressTask and terminates this Task.
     */
    prt.setPublishedRepos(repos);
    prt.setRepoNames(names);
    prt.setResult(true);
    prt.updateIndicator(++index, "Liste überprüft.");
    return null;
  }

  /**
   * If an Exception is thrown when connecting to the URL in {@link #call()}, this Method will be 
   * called with the given Text, depending on the Type of Exception that was thrown.
   * <br> When the Operation to search for the List took too long, this Method will be called as 
   * well.
   * <br> This Method will set all Fields in {@link #prt}, that require ArrayLists as Values to 
   * empty ArrayLists and sets {@link ProgressTask#success} to false, since no connection could be 
   * established. This way, this Task can end normally and the parent ProgressTask can continue as 
   * usual, but it will recognize, that there was an Error and defaults to a save state.

   * @param text  The String value that will be displayed to the User with further Information.
   * @since 1.0
   */
  private void updateFailed(String text) {
    /*
     * Sets all Fields in the ProgressTask to default values. This will ensure, that the 
     * ProgressTask will know, that this Task is finished and still knows, that there was an Error 
     * when checking for the Names.
     */
    prt.setResult(false);
    prt.setRepoNames(new ArrayList<String>());
    prt.setPublishedRepos(new ArrayList<String>());
    /*
     * Displays the given Text to the User. This is done via runLater, since this Process isn't the 
     * Process that started the primary Stage.
     */
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        primary.showUpdateFailed(text);
      }        
    });
  }
  
  @Override
  public String toString() {
    return "CheckerTask " + this.hashCode();
  }
}
