package com.reportmill;
import com.apple.eawt.*;
import com.apple.eawt.AppEvent.*;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.text.RMFontUtils;
import java.awt.*;
import javax.swing.*;
import snap.swing.DialogBox;
import snap.swing.ExceptionReporter;

/************************************* - All files should be 120 chars wide - *****************************************/

/**
 * This is the main class for the ReportMill app. You can run it from the command line like this:
 *
 *     prompt> java -cp ReportMill.jar com.reportmill.App
 * 
 */
public class App {

/**
 * This is the static main method, called by Java when launching with com.reportmill.App.
 */
public static void main(final String args[])
{
    SwingUtilities.invokeLater(new Runnable() { public void run() {
        new App(args); }});
}

/**
 * Creates a new app instance.
 */
public App(String args[])
{
    // Set app is true
    RMUtils.isApp = true;
    
    // Install Ribs preferences class
    RMPrefsUtils.setPrefsClass(Shell.class);

    // This prevents Windows boxes from bringing up the Drive A not ready error
    System.setSecurityManager(null);
    
    // Allow XML parser to accept text blocks up to 128k (up from 64k)
    System.setProperty("entityExpansionLimit", "128000");
    
    // Turn on anti aliasing on Windows
    System.setProperty("swing.aatext", "true");
    
    // Mac specific stuff
    if(RMUtils.isMac) new AppleAppHandler().init();
    
    // Install Exception reporter
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionReporter());
    
    // Run welcome panel
    Welcome.getShared().runWelcome();

    // Start font loading in background
    RMFontUtils.startFontLoadingInBackground();

    // Turn on dyamic layout
    Toolkit.getDefaultToolkit().setDynamicLayout(true);
}

/**
 * Quits the app (can be invoked by anyone).
 */
public static void quitApp()
{
    int answer = 0;
            
    // Iterate over open Editors to see if any have unsaved changes
    for(int i=0, iMax=RMEditorPaneUtils.getEditorPaneCount(); i<iMax && iMax>1; i++) {
        
        // Turn off editor preview
        RMEditorPaneUtils.getEditorPane(i).setEditing(true);
        
        // If editor has undos, run Review Unsaved panel
        if(RMEditorPaneUtils.getEditorPane(i).getEditor().undoerHasUndos()) {
            
            // Run Reveiew Unsaved Panel and break
            DialogBox dbox = new DialogBox("Review Unsaved Documents");
            dbox.setWarningMessage("There are unsaved documents");
            dbox.setOptions("Review Unsaved", "Quit Anyway", "Cancel");
            answer = dbox.showOptionDialog(RMEditor.getMainEditor(), "Review Unsaved");
            break;
        }
    }

    // If user hit Cancel, just go away
    if(answer==2)
        return;
    
    // Disable welcome panel
    Welcome.getShared().setEnabled(false);

    // If Review Unsaved, iterate through _editors to see if they should be saved or if user wants to cancel instead
    if(answer==0)
        for(int i=0, iMax=RMEditorPaneUtils.getEditorPaneCount(); i<iMax; i++)
            if(!RMEditorPaneUtils.getEditorPane(0).close()) {
                Welcome.getShared().setEnabled(true);
                return;
            }

    // Flush Properties to registry and exit
    try { RMPrefsUtils.prefs().flush(); } catch(Exception e) { e.printStackTrace(); }
    System.exit(0);
}

/**
 * A class to handle apple events.
 */
private static class AppleAppHandler implements PreferencesHandler, QuitHandler, OpenFilesHandler {

    /** Initializes Apple Application handling. */
    public void init()
    {
        System.setProperty("apple.laf.useScreenMenuBar", "true"); // 1.4
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RMStudio 14");
        Application app = Application.getApplication();
        app.setPreferencesHandler(this); app.setQuitHandler(this); app.setOpenFileHandler(this);
    }

    /** Handle Preferences. */
    public void handlePreferences(PreferencesEvent arg0)  { new PreferencesPanel().showPanel(null); }

    /** Handle Preferences. */
    public void openFiles(OpenFilesEvent anEvent)
    {
        final java.io.File file = anEvent.getFiles().size()>0? anEvent.getFiles().get(0) : null; if(file==null) return;
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            Welcome.getShared().open(file.getPath()); }});    
    }

    /** Handle QuitRequest. */
    public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1)  { App.quitApp(); }
}

}