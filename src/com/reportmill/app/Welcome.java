package com.reportmill.app;
import com.reportmill.App;
import com.reportmill.base.*;
import com.reportmill.viewer.*;
import java.awt.event.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides the welcome panel for RM. 
  */
public class Welcome extends SwingOwner {

    // Whether welcome panel is enabled
    boolean         _enabled = true;
    
    // Shared welcome panel
    static Welcome  _shared;
    
    // The Welcome panel UI file
    static String   _welcomeFile;
    
/**
 * Returns the shared instance of the welcome panel.
 */
public static Welcome getShared()  { return _shared!=null? _shared : (_shared=new Welcome()); }

/**
 * Returns whether welcome panel is enabled.
 */
public boolean isEnabled()  { return _enabled; }

/**
 * Sets whether welcome panel is enabled.
 */
public void setEnabled(boolean aValue)  { _enabled = aValue; }

/**
 * Brings up the welcome panel.
 */
public void runWelcome()
{
    // If disabled, just return
    if(!isEnabled())
        return;
    
    // Get RecentFilesMenuButton (need to call getUI() because of RMViewer.getComponent())
    MenuButton recentButton = getNode("RecentFilesMenuButton", MenuButton.class);
    
    // If there are recent files, enable the recent button and add popup menu
    if(RMRecentFilesMenu.getRecentDocuments().size()>0) {
        recentButton.setEnabled(true);
        recentButton.setPopupMenu(new RMRecentFilesMenu(null).getMenu());        
    }
    
    // Otherwise, disable recent button
    else recentButton.setEnabled(false);

    // Make welcome panel visible
    getWindow().setVisible(true);
}

/**
 * Close welcome.
 */
public void close()  { getWindow().setVisible(false); }

/**
 * Initializes the UI panel.
 */
protected void initUI()
{
    // Install WelcomeAnim.Viewer
    RMViewer viewer = new WelcomeAnim().getViewer();
    JLabel welcomeLabel = getNode("WelcomeAnimLabel", JLabel.class);
    viewer.setBounds(welcomeLabel.getBounds());
    getUI().add(viewer);
    
    // Reset BuildLabel, JavaLabel, LicenseLabel
    String lstring = ReportMill.getLicense()==null? "Unlicensed Copy" : "License: " + ReportMill.getLicense();
    setNodeValue("BuildLabel", "Build: " + RMUtils.getBuildInfo());
    setNodeValue("JavaLabel", "Java: " + System.getProperty("java.runtime.version"));
    setNodeValue("LicenseLabel", lstring);
        
    // Configure Window: IconImage, Add WindowListener to indicate app should exit when close button clicked
    getWindow().setWindowClass(JFrame.class);
    getWindow().setIconImage(RMEditorPane.getFrameIcon());
    getWindow().setStyle(SwingWindow.Style.Small);
    getWindow().addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) {
        runLater(new Runnable() { public void run() { App.quitApp(); }}); }});
}

/**
 * Respond to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{        
    // Handle NewButton
    if(anEvent.equals("NewButton")) {
        
        // Get new editor pane
        RMEditorPane editorPane = newEditorPane().newDocument();
        
        // If alt is down, replace with movies sample
        if(Swing.isAltDown())
            editorPane = RMEditorPaneUtils.openSample("Movies");
        
        // Make editor window visible
        editorPane.setWindowVisible(true);
        
        // Show document inspector
        editorPane.getInspectorPanel().showDocumentInspector();
        
        // Order editor window front again (after delay) to get focus back from doc inspector
        final RMEditorPane ep = editorPane; runLater(new Runnable() { public void run() {
            ep.getWindow().toFront(); }});
        
        // Close welcome panel
        close();
    }
            
    // Handle OpenButton
    if(anEvent.equals("OpenButton")) {
        String path = null; if(Swing.isAltDown()) {
            DialogBox dbox = new DialogBox("Enter Document URL"); dbox.setMessage("Enter Document URL");
            path =  dbox.showInputDialog(getUI(), "http://localhost:8080/Movies.rpt"); }
        open(path);
    }
    
    // Handle FinishButton
    if(anEvent.equals("QuitButton"))
        App.quitApp();
}

/**
 * Opens a document.  If pathName is null, the open panel will be run.
 */
public void open(String aPath)
{
    // Get the new editor pane that will open the document
    RMEditorPane editorPane = newEditorPane();
    
    // if no pathname, have editor run open panel
    editorPane = aPath==null ? editorPane.open() : editorPane.open(aPath);
    
    // If no document opened, just return
    if(editorPane==null)
        return;
    
    // Make editor window visible
    editorPane.setWindowVisible(true);
    
    // Show document inspector
    editorPane.getInspectorPanel().showDocumentInspector();
    
    // Order editor window front again (after delay) to get focus back from doc inspector
    final RMEditorPane ep = editorPane; runLater(new Runnable() { public void run() {
        ep.getWindow().toFront(); }});
    
    // Close welcome panel
    close();
}


/**
 * Creates a new editor for new or opened documents.
 */
public RMEditorPane newEditorPane()  { return new RMEditorPane(); }

/**
 * A viewer owner to load/view WelcomePanel animation from WelcomePanelAnim.rpt.
 */
private static class WelcomeAnim extends RMViewerOwner { }

}