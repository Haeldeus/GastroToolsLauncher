package tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import launcher.GastroToolsLauncher;
import util.AppDisplayArea;

/**
 * A Task to check, if there is an update available for a specified Repository.

 * @author Haeldeus
 * @version 1.0
 */
public class UpdateTask extends Task<Void> {

  /**
   * The Path to the GitHub-Repository of the Repository, this Task was assigned to to check for an 
   * update.
   */
  private String path;
  
  /**
   * The path on the hard drive, the Application is working on. This has to be "some/dir/app/" and 
   * will later be used to check for an already installed Application and it's version.
   */
  private String localPath;
  
  /**
   * The Name of the Repository as it is saved on GitHub.
   */
  private String repo;
  
  /**
   * The Name of the Application for this Repository. This Name is saved in a List in the 
   * GastroToolsLauncher-Repository.
   */
  private String name;
  
  /**
   * The AppDisplayArea, this Task will use to display Status Updates in.
   */
  private AppDisplayArea area;
  
  /**
   * The maximum of steps needed to check for Updates. Is used to display the Progress to the user 
   * via a ProgressBar.
   */
  private int max;
  
  /**
   * The Version of the installed Application for the given Repository if existing. Will be 
   * {@code null} until {@link #checkVersion()} was called. Afterwards, it's either "" if no 
   * Version is installed or the String, that is specified in "localPath/name/Version.txt".
   */
  private String version;
  
  /**
   * A Counter for the Steps taken in this Task. Is used to display the Progress to the User.
   */
  private int counter;
  
  /**
   * The Index of this Repository in the List given in GastroToolsLauncher's Repository. Is used to 
   * restart this task if it failed to check for an update without having to restart all Tasks.
   */
  private int index;
  
  /**
   * The Launcher, that has started this Task. Used to restart a single Task via 
   * {@link GastroToolsLauncher#startSpecificUpdateTasks(ArrayList)}.
   */
  private GastroToolsLauncher primary;
  
  /**
   * A boolean value, if an update should be performed, regardless of the version File.
   */
  private boolean autoUpdate;
  
  /**
   * Creates a new Task, that will check for updates for the specified {@code repo} with the given 
   * {@code name}.

   * @param repo  The Repository that will be checked. This has to be the precise name of the 
   *      Repository since this Task will check for updates on the Version-Control-Site where the 
   *      Repository's Name is embedded into the Link.
   * @param name  The Name of the Application for the Repository. This Name is usually the 
   *      localized variant of the Repository's Name and is saved in a separate List in the 
   *      Launcher's Repository.
   * @param area  The {@link AppDisplayArea}, where this Task will update the MessageLabel in. 
   *      These Updates will contain standard Messages as well as a possibility to retry after 
   *      Failure.
   * @param localPath The path, the Application is working on. This has to be "some/dir/app/". Is 
   *      later used to check for already installed versions.
   * @param index The index of the Repository as it is seen in the List starting from the top, 
   *      where the first has the index 0. This is used to restart this task alone and not every 
   *      other Task as well, which might have been executed successfully even though this Task has 
   *      failed to check for an Update.
   * @param primary The GastroToolsLauncher, that started this Task. Is used to restart a single 
   *      UpdateTask via {@link GastroToolsLauncher#startSpecificUpdateTasks(ArrayList)}.
   * @param autoUpdate  A Boolean value, if an update is necessary, regardless of installed 
   *      version. This is the case, if no executable File was found.
   * @since 1.0
   * @see AppDisplayArea
   * @see GastroToolsLauncher
   * @see Task
   */
  public UpdateTask(String repo, String name, AppDisplayArea area, String localPath, int index, 
      GastroToolsLauncher primary, boolean autoUpdate) {
    this.repo = repo;
    this.name = name;
    this.localPath = localPath;
    //TODO: Support MultiDeveloping?
    path = "https://github.com/Haeldeus/" + repo + "/blob/master/version.txt";
    this.area = area;
    /*
     * A given Maximum of steps, that was chosen by the Developer.
     */
    this.max = 11;
    this.counter = 0;
    this.index = index;
    this.primary = primary;
    this.autoUpdate = autoUpdate;
  }
  
  @Override
  protected Void call() throws Exception {
    /*
     * If this Task was restarted by the Launcher, an EventHandler was added before. Since, as of 
     * now, it is not wanted to stop a running UpdateTask, this Handler has to be removed to 
     * prevent premature restarts.
     * Has to be executed in a try-catch Block to prevent Errors, when this Task is started for the 
     * first Time, since we don't want the Task to stop.
     */
    try {
      area.getMessageLabel().removeEventHandler(MouseEvent.MOUSE_CLICKED, 
          area.getMessageLabel().getOnMouseClicked());
    } catch (NullPointerException e) {
      //Nothing to do here, since the EventHandler wasn't yet added.
    }
    /*
     * Creates all needed variables for the Task. An URL, where the given path will be saved in, a 
     * BufferedReader to read the File on the Website and a boolean value, if a connection was 
     * established.
     */
    URL url;
    BufferedReader br;
    boolean connected = true;
    /*
     * Tries to connect to the Website. If no connection could be established, this Try-Block will 
     * throw an Error, which will be caught and a Restart is offered to the User.
     */
    try {
      /*
       * Tries to open a connection to the given path and connects to the Website.
       */
      url = new URL(path);
      // Get the input stream through URL Connection
      URLConnection con = url.openConnection();
      con.connect();
      /*
       * Gets the InputStream from the Connection and creates a new BufferedReader to read from it.
       */
      InputStream is = con.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      /*
       * Updates the User.
       */
      updateProgress(++counter, max);
      area.updateMessage("Verbindung hergestellt");
    } catch (IOException e) {
      /*
       * If an Error was thrown when connecting to the Website, this Catch-Block will be executed.
       * 
       * Adds an EventHandler to the Label in AppDisplayArea, which will be able to restart this 
       * Task, when the User clicks on the Label.
       */
      area.getMessageLabel().setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent arg0) {
          /*
           * Restarts this Task without restarting all other Tasks.
           */
          ArrayList<Integer> list = new ArrayList<Integer>();
          list.add(index);
          primary.startSpecificUpdateTasks(list);
        }  
      });
      /*
       * Updates the User, that no connection could be established.
       */
      area.updateMessage("Keine Internetverbindung entdeckt. Hier klicken für Neuversuch.");
      /*
       * Since there is no connection to the Website, this Reader cannot be created. Since it is 
       * used later on, it has to be set to null to prevent errors. Also sets connected to false to 
       * be able to hide the ProgressBar in the next step.
       */
      br = null;
      connected = false;
    }
    
    /*
     * If this Task couldn't connect to the Webpage, there is need for the ProgressBar anymore, so 
     * it will be hidden in this step.
     */
    if (!connected) {
      area.hideProgressBar();
      area.setPath(localPath + name + File.separator + repo + ".jar");
      System.out.println("DEBUG1: Path set:" + localPath + name + File.separator + repo + ".jar");
      System.out.println("DEBUG1: Path in area: " + area.getPath());
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
        /*
         * Checks if the Task was cancelled in the meantime to prevent deadlocks. In this case a 
         * new EventHandler is created, that is able to restart The Task. 
         */
        if (isCancelled()) {
          area.getMessageLabel().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
              /*
               * Restarts this Task.
               */
              ArrayList<Integer> list = new ArrayList<Integer>();
              list.add(index);
              primary.startSpecificUpdateTasks(list);
            }  
          });
          /*
           * Updates the User, that the Time limit was exceeded and he can try for a Restart of 
           * this Task.
           * Also stops the further execution of this Task by returning null.
           */
          area.updateMessage("Zeitüberschreitung! Hier klicken für Neuversuch");
          area.setPath(localPath + name + File.separator + repo + ".jar");
          System.out.println("DEBUG2: Path set:" + localPath + name + File.separator + repo + ".jar");
          System.out.println("DEBUG2: Path in area: " + area.getPath());
          return null;
        }
        /*
         * This String is always at the end of the version information.
         */
        if (line.contains("#End Version File")) {
          /*
           * Sets list to false to stop further reading from the File and updates the User, that 
           * the reading was finished.
           */
          list = false;
          updateProgress(++counter, max);
          area.updateMessage("Versionsabfrage abgeschlossen.");
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
             * Checks if the Task was cancelled in the meantime to prevent deadlocks. In this case 
             * a new EventHandler is created, that is able to restart The Task. 
             */
            if (isCancelled()) {
              area.getMessageLabel().setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent arg0) {
                  /*
                   * Restarts this Task.
                   */
                  ArrayList<Integer> list = new ArrayList<Integer>();
                  list.add(index);
                  primary.startSpecificUpdateTasks(list);
                }  
              });
              /*
               * Updates the User, that the Time limit was exceeded and he can try for a Restart of 
               * this Task.
               * Also stops the further execution of this Task by returning null.
               */
              area.updateMessage("Zeitüberschreitung! Hier klicken für Neuversuch");
              area.setPath(localPath + name + File.separator + repo + ".jar");
              System.out.println("DEBUG3: Path set:" + localPath + name + File.separator + repo + ".jar");
              System.out.println("DEBUG3: Path in area: " + area.getPath());
              return null;
            }
            /*
             * Removes the HTML-Code from the String.
             */
            line = line.replaceFirst(line.substring(line.indexOf("<"), 
                line.indexOf(">") + 1), "");
          }
          /*
           * If the Line still contains Information after removing all HTML-Code, then there 
           * are version numbers stored in it. It is added to the String defined before with a line 
           * Separator at the end to prevent malformed version Information.
           */
          if (line.trim().length() != 0) {
            /*
             * To prevent multiple executions of this updating process, this part checks, if this 
             * is the first addition to s, so it has to be the first version Information found.
             */
            if (s.length() == 0) {
              /*
               * Updates the User, that the version Information was read. Next Line will be 
               * #End Version File, so the Loop will now be stopped.
               */
              updateProgress(++counter, max);
              area.updateMessage("Versionsnummer wurde ausgelesen");
            }
            
            s = s.concat(line.trim() + System.lineSeparator());
          }
        }
        /*
         * This Line is always at the start of the Information about versions. This is checked 
         * here to remove a line from s.
         */
        if (line.contains("#Begin Version File")) {
          list = true;
          /*
           * Updates the User, that the Version Information was found.
           */
          updateProgress(++counter, max);
          area.updateMessage("Versionsnummern gefunden.");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    /*
     * Closes the Reader to prevent Ressource Leak.
     */
    br.close();
    
    /*
     * Updates the User, that the Found version Information are now compared to the installed 
     * version.
     */
    updateProgress(++counter, max);
    area.updateMessage("Verarbeite Versionsnummern...");
    /*
     * Creates a new StringTokenizer to separate single versions from the the Version Information 
     * found beforehand. Also deletes the first Token, since this will always be "Current Version:" 
     * and is not useful for the further Steps.
     */
    StringTokenizer st = new StringTokenizer(s, System.lineSeparator());
    st.nextToken();
    /*
     * Saves the second Token, which contains the latest published Version. Also deletes the next 
     * Token, since this will be an empty line.
     */
    final String publishedVersion = st.nextToken();
    st.nextToken();
    /*
     * Creates a new ArrayList, that will save all older Versions.
     */
    ArrayList<String> oldVersions = new ArrayList<String>();
    /*
     * Adds all remaining Tokens to the ArrayList.
     */
    while (st.hasMoreTokens()) {
      oldVersions.add(st.nextToken());
    }
    /*
     * Informs the User, that the published Version Information was checked.
     */
    updateProgress(++counter, max);
    area.updateMessage("Version überprüft.");
    
    //Space for possible Additions, that might be necessary in the future between these Steps.
    
    /*
     * Informs the User, that the Task will now check for an installed Version.
     */
    updateProgress(++counter, max);
    area.updateMessage("Überprüfe installierte Version...");
    
    /*
     * Checks for an installed Version via checkVersion(). If no Version is installed, it updates 
     * the User and stops the Task.
     */
    if (!checkVersion()) {
      System.out.println("DEBUG: No version found");
      updateProgress(max, max);
      area.updateMessage("Keine Versionsdatei gefunden. Neuinstallation empfohlen!");
      area.enableDownload("https://github.com/Haeldeus/" + repo + "/releases/download/v" 
          + publishedVersion + "/" + name + ".jar", version);
      area.hideProgressBar();
      area.setPath(localPath + name + File.separator + repo + ".jar");
      System.out.println("DEBUG4: Path set:" + localPath + name + File.separator + repo + ".jar");
      System.out.println("DEBUG4: Path in area: " + area.getPath());
      return null;
    }
    
    System.out.println("DEBUG: Version check completed");
    
    if (!autoUpdate) {
      /*
       * Checks, if the latest published Version is installed or an older version is installed. In 
       * any other case (wrong version Information, or some yet unknown errors), this If-Clause 
       * will go to the else-Part.
       */
      if (publishedVersion.contentEquals(version)) {
        /*
         * Informs the User, that the latest Version is installed and disables the DownloadButton.
         */
        updateProgress(max, max);
        area.updateMessage("Neuste Version vorhanden!");
        area.switchDownloadButton(true);
        System.out.println("DEBUG: Newest Version installed");
      } else if (oldVersions.contains(version)) {
        /*
         * Informs the User, that an update was found and enables the Download for this Update.
         */
        System.out.println("DEBUG: Old version found");
        updateProgress(++counter, max + 1);
        area.updateMessage("Update gefunden!");
        area.enableDownload("https://github.com/Haeldeus/" + repo + "/releases/download/v" 
            + publishedVersion + "/" + name + ".jar", version);
        updateProgress(max, max);
      } else {
        /*
         * Informs the User, that the installed version might be flawed and recommends an Update.
         * Also enables the Download to be able to update the Version.
         */
        System.out.println("DEBUG: Flawed version found");
        updateProgress(++counter, max + 1);
        area.updateMessage("Fehlerhafte Versionsnummer gefunden. Update empfohlen!");
        area.enableDownload("https://github.com/Haeldeus/" + repo + "/releases/download/v" 
            + publishedVersion + "/" + name + ".jar", version);
        updateProgress(max, max);
      }
    } else {
      System.out.println("DEBUG: AutoUpdate triggered");
      updateProgress(++counter, max + 1);
      area.updateMessage("Fehlerhafte Installation gefunden. Update notwendig!");
      area.enableDownload("https://github.com/Haeldeus/" + repo + "/releases/download/v" 
          + publishedVersion + "/" + name + ".jar", version);
      updateProgress(max, max);
    }
    /*
     * Sets the Path in the AppDisplayArea to the Executable File in the Folder, hides the 
     * ProgressBar, since this Task is finished and stops this Task.
     */
    area.setPath(localPath + name + File.separator + repo + ".jar");
    System.out.println("DEBUG5: Path set:" + localPath + name + File.separator + repo + ".jar");
    System.out.println("DEBUG5: Path in area: " + area.getPath());
    area.hideProgressBar();
    return null;
  }
  
  /**
   * Checks the local Folder for a Version-File and reads the version-Number from it if it exists.
   * This is the only way to check for an installed version that is appropriate. Every other 
   * possibility would go beyond the scope of the Specification.

   * @return  {@code true}, if a version was read, {@code false} else.
   * @since 1.0
   */
  private boolean checkVersion() {
    /*
     * Updates the User, that the local Version is being read.
     */
    updateProgress(++counter, max);
    area.updateMessage("Lade lokale Versionsdatei...");
    /*
     * Creates a String that contains the path to the Version-File.
     */
    String versionF = localPath + name + File.separator + "Version.txt";
    /*
     * Updates the User, that the File is being checked.
     */
    updateProgress(++counter, max);
    area.updateMessage("Überprüfe installierte Version");
    /*
     * If no Version-File exists, an Error will be thrown when trying to read from it. To prevent 
     * this Error from stopping the whole Task, this try-catch-Block is used.
     */
    try {
      /*
       * Creates a FileReader, which reads from the Version-File and a BufferedReader, which will 
       * be used to buffer the Lines from the Version File.
       */
      FileReader fr = new FileReader(versionF);
      BufferedReader br = new BufferedReader(fr);
      /*
       * Saves the Version in the given Field. Closes the Reader afterwards to prevent Ressource 
       * Leak and returns true, since the check was successful.
       */
      version = br.readLine();
      fr.close();
      return true;
    } catch (IOException e) {
      /*
       * If an Error occurred the Version has to be set to "", since the installed version couldn't 
       * be verified. Returns false, since the check wasn't successful.
       */
      version = "";   
      return false; 
    }
  }
}
