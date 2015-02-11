package com.reportmill.app;
import com.reportmill.base.*;
import java.util.*;
import javax.swing.*;
import snap.swing.*;
import java.io.*;
import java.util.prefs.*;

/**
 * This class maintains the list of the recently used files.  
 * It stores the list in the user's preferences, and maintains the 'recent documents' menu.
 */
public class RMRecentFilesMenu extends SwingOwner {
    
    // The menu bar that owns the file menu
    RMEditorPaneMenuBar   _editorMenuBar;

    // The recent docs menu
    JMenu                 _menu;
    
    // The pathnames corresponding to the items in the menu
    List <String>         _paths;

/** 
 * Creates a new recent file smenu for the given editor pane
 */
public RMRecentFilesMenu(RMEditorPaneMenuBar bar)
{
    _editorMenuBar = bar;
    rebuildMenu();
}

/**
 * Adds a new document to the list and updates the users preferences.
 */
public void addMostRecentDocument(String aPath)
{
    // Get the doc list from the preferences
    String path = aPath; if(RMStringUtils.startsWithIC(path, "file:")) path = path.substring(5);
    List <String> docs = getRecentDocuments();
    
    // Remove the path (if it was there) and add to front of list
    docs.remove(path);
    docs.add(0, path);
    
    // Add at most 10 files to the preferences list
    Preferences prefs = RMPrefsUtils.prefs().node("RecentDocuments");
    for(int i=0; i<docs.size() && i<10; i++) 
        prefs.put("index"+i, docs.get(i));
    
    // Rebuild the menu
    rebuildMenu();
}

/**
 * Returns the actual menu.
 */
public JMenu getMenu()  { return _menu; }

/**
 * Rebuilds the menu.
 */
protected void rebuildMenu()
{
    // If menu hasn't been created, create it
    if(_menu==null) {
        _menu = new JMenu("Open Recent"); _menu.setName("OpenRecentMenu");
        initUI(_menu);
    }
    
    // Otherwise, remove items
    else _menu.removeAll();
    
    // Get paths
    _paths = getRecentDocuments();
    
    // Iterate over paths and add menu items
    for(int i=0; i<_paths.size(); i++) { String file = RMStringUtils.getPathFileName(_paths.get(i));
        addMenuItem("RecentFileMenuItem", file); }
    
    // Add separator and ClearRecentMenuItem
    if(_paths.size()>0) {
        _menu.addSeparator(); addMenuItem("ClearRecentMenuItem", "Clear Menu"); }
    
    // Install menu
    installMenu();
}

/**
 * Installs recent files menu under editor pane OpenRecentMenu. 
 */
protected void installMenu()
{
    // If no editor pane, just return
    if(_editorMenuBar==null) return;
    
    // If it's already there, just enable it/disable it if there are any items
    JMenuItem recent = _editorMenuBar.getNode("OpenRecentMenu", JMenuItem.class);
    if(recent != null) {
        recent.setEnabled(_menu!=null && _menu.getItemCount()>0); return; }
    
    // It's not there yet, so first figure out where to put it (it goes right after the Open... menu item)
    JMenu fileMenu = _editorMenuBar.getNode("FileMenu", JMenu.class);
    JMenuItem openItem = _editorMenuBar.getNode("OpenMenuItem", JMenuItem.class);

    if(fileMenu==null || openItem==null || _menu==null) {
        System.err.println("Couldn't install recent files menu"); return; }
        
    // find Open in the file menu and insert the recent menu after it
    int openIndex = findMenuItem(fileMenu, openItem);
    fileMenu.insert(_menu, openIndex+1);
    _menu.setEnabled(_menu.getItemCount()>0);
}
    
/**
 * Returns the list of the recent documents as a list of strings.
 */
public static List <String> getRecentDocuments()
{
    // Get prefs for RecentDocuments (just return if missing)
    Preferences prefs = RMPrefsUtils.prefs();
    try { if(!prefs.nodeExists("RecentDocuments")) return new ArrayList(); }
    catch(BackingStoreException bse) { return new ArrayList(); }
    prefs = prefs.node("RecentDocuments");
    
    // Add to the list only if the file is around and readable
    List list = new ArrayList();
    for(int i=0; ; i++) {
        String fname = prefs.get("index"+i, null); if(fname==null) break;
        File file = new File(fname);
        if(file.exists() && file.canRead())
            list.add(fname);
    }
    
    // Return list
    return list;
}

/** Bogus implementation to return an empty panel. */
protected SpringsPane createUI()  { return new SpringsPane(); }

/**
 * Respond to any selection from the RecentFiles menu
 */
public void respondUI(SwingEvent anEvent) 
{
    // Handle ClearRecentMenuItem
    if(anEvent.equals("ClearRecentMenuItem"))
        clearMostRecentDocuments();
    
    // Handle RecentFileMenuItem
    else if(anEvent.equals("RecentFileMenuItem")) {
        
        // Get index and path
        int index = findMenuItem(_menu, anEvent.getTarget(JMenuItem.class)); if(index<0) return;
        String path = _paths.get(index);
            
        // If EditorMenuBar is present, have the editor pane do it
        if(_editorMenuBar!=null) {
                
            // Create new editor pane. If successful, rebuild the menu and open editor
            // Note that new doc will also have a RecentFilesMenu.
            // Multiple open docs won't get their RecentFilesMenu updated automatically.
            // This is probably dumb.  I wonder if you can use the same menu instance in multiple menubars.
            RMEditorPane editorPane = RMClassUtils.newInstance(_editorMenuBar.getEditorPane()).open(path);
            if(editorPane != null) {
                rebuildMenu();
                editorPane.setWindowVisible(true);
            }
        }
        
        // Otherwise, have the Welcome panel open the doc
        else com.reportmill.app.Welcome.getShared().open(path);
    }    
}

/**
 * Clears most recent documents from preferences.
 */
public void clearMostRecentDocuments()
{
    Preferences p = RMPrefsUtils.prefs();
    try { if(p.nodeExists("RecentDocuments")) p.node("RecentDocuments").removeNode(); }
    catch(BackingStoreException e) { }
    rebuildMenu();
}

/** Utility routine to find the index of a given JMenuItem in a JMenu. */
private int findMenuItem(JMenu aMenu, JMenuItem anItem)
{
    for(int i=0, iMax=aMenu.getItemCount(); i<iMax; i++) if(aMenu.getItem(i)==anItem) return i;
    return -1;
}
   
/** Adds a menu item to menu. */
private void addMenuItem(String aName, String theText)
{
    JMenuItem item = new JMenuItem(theText); item.setName(aName);
    initUI(item);
    _menu.add(item);
}

}