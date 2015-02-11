package com.reportmill.app;
import com.reportmill.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.viewer.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import snap.swing.*;
import snap.web.*;

/**
 * This class is a container for an RMEditor in an enclosing scroll pane with tool bars for editing.
 */
public class RMEditorPane extends RMViewerPane {

    // The menu bar owner
    RMEditorPaneMenuBar    _menuBar;

    // The recent files menu
    RMRecentFilesMenu      _recentFilesMenu;

    // The editor horizontal ruler object
    RMEditorRuler          _horizontalRuler;
    
    // The editor vertical ruler object
    RMEditorRuler          _verticalRuler;
    
    // The original editor, if in preview mode
    RMEditor               _realEditor;
    
    // The shared editor inspector
    static InspectorPanel  _inspectorPanel = new InspectorPanel();
    
    // The shared attributes inspector (go ahead and create to get RMColorPanel created)
    static AttributesPanel _attributesPanel = new AttributesPanel();
    
    // The image for a window frame icon
    static Image           _frameIcon = Swing.getImage("ReportMill16x16.png", RMEditorPane.class);

/**
 * Returns the editor pane that is the most recently activated.
 */
public static RMEditorPane getMainEditorPane()  { return RMEditorPaneUtils.getMainEditorPane(); }

/**
 * Returns the viewer as an editor.
 */
public RMEditor getEditor()  { return (RMEditor)getViewer(); }

/**
 * Overridden to return an RMEditor.
 */
protected RMViewer createViewer()  { RMEditor ed = new RMEditor(); ed._ep = this; return ed; }

/**
 * Override to return as RMEditorPaneToolBar.
 */
public RMEditorPaneToolBar getTopToolBar()  { return (RMEditorPaneToolBar)super.getTopToolBar(); }

/**
 * Creates the top tool bar.
 */
public SwingOwner createTopToolBar()  { return new RMEditorPaneToolBar(this); }

/**
 * Returns the SwingOwner for the menu bar.
 */
public RMEditorPaneMenuBar getMenuBar()  { return _menuBar!=null? _menuBar : (_menuBar = createMenuBar()); }

/**
 * Creates the RMEditorPaneMenuBar for the menu bar.
 */
public RMEditorPaneMenuBar createMenuBar()
{
    // Create MenuBar, create RecentFilesMenu and return
    RMEditorPaneMenuBar mbar = new RMEditorPaneMenuBar(this);
    _recentFilesMenu = new RMRecentFilesMenu(mbar);
    return mbar;
}

/**
 * Returns the datasource associated with the editor's document.
 */
public RMDataSource getDataSource()  { return getEditor().getDataSource(); }

/**
 * Sets the datasource for the panel.
 */
public void setDataSource(RMDataSource aDataSource)
{
    // Set DataSource in editor, show DataSource inspector, KeysBrowser and refocus window
    getEditor().setDataSource(aDataSource);
    getAttributesPanel().setVisible(AttributesPanel.KEYS);
    if(getWindow().isVisible()) getWindow().toFront();
}

/**
 * Sets a datasource from a given URL at a given point (if dragged in).
 */
public void setDataSource(WebURL aURL, Point2D aPoint)
{
    // Create DataSource and load dataset
    RMDataSource dsource = new RMDataSource(aURL);
    try { dsource.getDataset(); }
    
    // If failed, get error message and run error panel
    catch(Throwable t) {
        while(t.getCause()!=null) t = t.getCause(); // Get root cause
        String e1 = RMStringUtils.wrap(t.toString(), 40);
        Object line = RMKey.getValue(t, "LineNumber"), column = RMKey.getValue(t, "ColumnNumber");
        if(line!=null || column!=null) e1 += "\nLine: " + line + ", Column: " + column;
        else t.printStackTrace();
        final String error = e1;
        runLater(new Runnable() { public void run() {
            DialogBox dbox = new DialogBox("Error Parsing XML"); dbox.setErrorMessage(error);
            dbox.showMessageDialog(getUI()); }});
        return;
    }        
        
    // Set datasource in editor pane
    setDataSource(dsource);
    RMEditor._xmlLoc = aPoint;
}

/**
 * Returns whether editor pane shows rulers.
 */
public boolean getShowRulers()  { return _horizontalRuler!=null; }

/**
 * Sets whether editor pane shows rulers.
 */
public void setShowRulers(boolean aValue)
{
    // If showing rulers is already equal value, just return
    if(aValue==getShowRulers()) return;
    
    // Determine if we should resize window after toggle (depending on whether window is at preferred size)
    boolean doPack = getWindow().getSize().equals(getWindow().getPreferredSize());
    
    // If no rulers, create and add them
    if(_horizontalRuler==null) {
        _horizontalRuler = new RMEditorRuler(getEditor(), RMEditorRuler.HORIZONTAL);
        _verticalRuler = new RMEditorRuler(getEditor(), RMEditorRuler.VERTICAL);
        getScrollPane().setRowHeaderView(_verticalRuler);
        getScrollPane().setColumnHeaderView(_horizontalRuler);
        getScrollPane().setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, _horizontalRuler._corner);
    }
    
    // Otherwise, remove and clear them
    else {
        getScrollPane().setRowHeaderView(null);
        getScrollPane().setColumnHeaderView(null);
        getScrollPane().setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, null);
        _horizontalRuler = _verticalRuler = null;
    }
    
    // Resize window if window was previously at preferred size
    if(doPack)
        getWindow().pack();
}

/**
 * Returns whether editor is really doing editing.
 */
public boolean isEditing()  { return getEditor().isEditing(); }

/**
 * Sets whether editor is really doing editing.
 */
public void setEditing(boolean aFlag)
{
    // If editor already has requested editing state, just return
    if(aFlag == isEditing()) return;
    
    // If not yet previewing, store current template then generate report and swap it in
    if(!aFlag) {
                
        // Cache current editor and flush any current editing
        _realEditor = getEditor();
        _realEditor.flushEditingChanges();
        
        // Generate report and restore filename
        RMDocument report = getDocument().generateReport(getEditor().getDataSourceDataset());
        
        // Create new editor, set editing to false and set report document
        RMEditor editor = new RMEditor(); editor._ep = this;
        editor.setEditing(false);
        editor.setContent(report);
        
        // If generateReport hit any keyChain parsing errors, run message dialog
        if(RMKeyChain.getError()!=null) { String err = RMKeyChain.getAndResetError();
            DialogBox dbox = new DialogBox("Error Parsing KeyChain"); dbox.setErrorMessage(err);
            dbox.showMessageDialog(getUI());
        }
        
        // Set new editor
        setViewer(editor);
        editor.addPropertyChangeListener(this);
    }

    // If turning preview off, restore real editor
    else {
        getEditor().removePropertyChangeListener(this);
        setViewer(_realEditor);
    }
    
    // Focus on editor
    requestFocus(getEditor());
}

/**
 * Override to configure Window.
 */
protected void initUI()
{
    // Do normal version
    super.initUI();
    
    // Enable MouseClicked Event for editor
    enableEvents(getEditor(), SwingEvent.Type.MouseClicked);
    
    // Configure Window ClassName, IconImage, MenuBar
    SwingWindow window = getWindow();
    window.setIconImage(getFrameIcon());
    window.setMenuBar(getMenuBar().getMenuBar());
    window.setWindowCall(new Callable() { public Window call() { return createWindowX(); }});
}

/**
 * Updates the editor's UI panels.
 */
protected void resetUI()
{
    // Do normal update
    super.resetUI();
    
    // If title has changed, update window title
    String title = getWindowTitle(); SwingWindow swin = getWindow();
    if(swin.isWindowSet() && !RMUtils.equals(title, swin.getTitle())) {
        swin.setTitle(title);
        WebFile dfile = getSourceURL()!=null? getSourceURL().getFile() : null;
        File file = dfile!=null? dfile.getStandardFile() : null;
        if(file!=null && file.exists()) swin.setDocumentFile(file);
    }
    
    // Update the rulers if visible
    if(_horizontalRuler!=null) {
        _horizontalRuler.setNeedsUpdate();
        _verticalRuler.setNeedsUpdate();
    }
    
    // Reset MenuBar, InspectorPanel and AttributesPanel
    getMenuBar().resetLater();
    if(getInspectorPanel().isVisible()) getInspectorPanel().resetLater();
    if(getAttributesPanel().isVisible()) getAttributesPanel().resetLater();
}

/**
 * Handles changes to the editor's UI controls.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Forward on to menu bar
    getMenuBar().sendEvent(anEvent);
    
    // Do normal version
    super.respondUI(anEvent);
    
    // If Editor.MouseClicked and DataSource is set and we're editing and DataSource icon clicked, show DS Inspector
    if(anEvent.isMouseClicked() && getDataSource()!=null && isEditing()) {
        Rectangle r = getEditor().getVisibleRect(); // Get visible rect
        if(anEvent.getX()>r.getMaxX()-53 && anEvent.getY()>r.getMaxY()-53) { // If DataSource icon clicked
            if(anEvent.isCommandDown()) setDataSource(null); // If cmd key down, clear the DataSource
            else getInspectorPanel().setVisible(7); // otherwise show DataSource inspector
        }
    
        // If mouse isn't in lower right corner and DataSource inspector is showing, show shape specific inspector
        else if(getInspectorPanel().isShowingDataSource())
            getInspectorPanel().setVisible(0);
    }
}

/**
 * Creates the window.
 */
protected Window createWindowX()
{
    // Create frame
    JFrame frame = new JFrame();
    
    // Add WindowListner
    frame.addWindowListener(new WindowAdapter() {
        public void windowActivated(WindowEvent e) {
            RMEditorPaneUtils.setMainEditorPane(RMEditorPane.this);
            setWindowMaximizedBounds();
        }
        public void windowGainedFocus(WindowEvent e) { RMEditorPaneUtils.setMainEditorPane(RMEditorPane.this); }
        public void windowOpened(WindowEvent e) { RMEditorPaneUtils.setMainEditorPane(RMEditorPane.this); }
        public void windowClosing(WindowEvent e) { close(); }
    });
    
    // Add frame Component Listener
    frame.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) { windowResized(); }});
    
    // Configure frame to do nothing on close (we'll handle everything)
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    return frame;
}

/**
 * Returns the editor inspector (shared).
 */
public InspectorPanel getInspectorPanel()  { return _inspectorPanel; }

/**
 * Returns the editor attributes panel (shared).
 */
public AttributesPanel getAttributesPanel()  { return _attributesPanel; }

/**
 * Returns extension for editor document.
 */
public String[] getFileExtensions()  { return new String[] { ".rpt", ".rib", ".jfx", ".pdf"}; }

/**
 * Returns the description for the editor document for use in open/save panels.
 */
public String getFileDescription()  { return Swing.isAltDown()? "Ribs files (.rib)" : "ReportMill files (.rpt)"; }

/**
 * Returns the window title.
 */
public String getWindowTitle()
{
    // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
    String title = getSourceURL()!=null? getSourceURL().getPath() : null; if(title==null) title = "Untitled";

    // If has undos, add asterisk. If zoomed, add ZoomFactor
    if(getEditor().getUndoer()!=null && getEditor().getUndoer().hasUndos()) title = "* " + title;
    if(!RMMath.equals(getEditor().getZoomFactor(), 1f))
        title += " @ " + Math.round(getEditor().getZoomFactor()*100) + "%";

    // If previewing, add "(Previewing)" and return
    if(getEditor().isPreview()) title += " (Previewing)";
    return title;
}

/**
 * Returns the icon for the editor window frame.
 */
public static Image getFrameIcon()  { return _frameIcon; }

/**
 * Creates a new default editor pane.
 */
public RMEditorPane newDocument()  { return open(new RMDocument(612, 792)); }

/**
 * Creates a new editor window from an open panel.
 */
public RMEditorPane open()
{
    // Get component to center open panel about
    JComponent comp = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    
    // Get path from open panel for supported file extensions (.rpt, .pdf, .rib)
    String path = FileChooserUtils.showChooser(false, comp, getFileDescription(), getFileExtensions());
    
    // Open file path
    return open(path);
}

/**
 * Creates a new editor window by opening the document from the given source.
 */
public RMEditorPane open(Object aSource)
{
    // If document source is null, just return null
    if(aSource==null) return null;
    
    // Get Source URL
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    
    // If source is already opened, return editor pane
    if(!RMUtils.equals(url, getSourceURL()))
        for(int i=0, iMax=RMEditorPaneUtils.getEditorPaneCount(); i<iMax; i++)
            if(RMUtils.equals(url, RMEditorPaneUtils.getEditorPane(i).getSourceURL()))
                return RMEditorPaneUtils.getEditorPane(i);
    
    // Load document
    RMParentShape shape = null; try { shape = (RMParentShape)new RMArchiver().getShape(aSource, null); }
    
    // If there was an XML parse error loading aSource, show error dialog
    catch(Exception e) {
        e.printStackTrace();
        final String msg = RMStringUtils.wrap("Error reading file:\n" + e.getMessage(), 40);
        runLater(new Runnable() { public void run() {
            DialogBox dbox = new DialogBox("Error Reading File"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(RMEditor.getMainEditor()); }});
    }
    
    // If no document, just return null
    if(shape==null) return null;

    // If old version, warn user that saving document will make it unreadable by RM7
    if(shape instanceof RMDocument && ((RMDocument)shape).getVersion()<7.0) {
        String msg = "This document has been upgraded from an older version.\n" +
            "If saved, it will not open in earlier versions.";
        DialogBox dbox = new DialogBox("Warning: Document Upgrade"); dbox.setWarningMessage(msg);
        dbox.showMessageDialog(getEditor());
    }
    
    // Set document
    getViewer().setContent(shape);
    
    // If source is string, add to recent files menu
    if(url!=null && _recentFilesMenu!=null)
        _recentFilesMenu.addMostRecentDocument(url.getString());
    
    // Return the editor
    return this;
}

/**
 * Saves the current editor document, running the save panel.
 */
public void saveAs()
{
    // Make sure editor isn't previewing
    setEditing(true);
    
    // Get extensions - if there is an existing extension, make sure it's first in the exts array
    String exts[] = getFileExtensions();
    if(getSourceURL()!=null && RMStringUtils.getPathExtension(getSourceURL().getPath())!=null) {
        List ex = new ArrayList(Arrays.asList(exts));
        ex.add(0, "." + RMStringUtils.getPathExtension(getSourceURL().getPath()));
        exts = (String[])ex.toArray(new String[ex.size()]);
    }
    
    // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
    String path = FileChooserUtils.showChooser(true, getEditor(), getFileDescription(), exts); if(path==null) return;
    getViewerShape().setSourceURL(WebURL.getURL(path));
    save();
}

/**
 * Saves the current editor document, running the save panel if needed.
 */
public void save()
{
    // If can't save to current source, do SaveAs instead
    if(getSourceURL()==null) { saveAs(); return; }
    
    // Make sure editor isn't previewing
    setEditing(true);
    
    // Do actual save - if exception, print stack trace and set error string
    try { saveImpl(); }
    catch(Throwable e) {
        e.printStackTrace();
        String msg = "The file " + getSourceURL().getPath() + " could not be saved (" + e + ").";
        DialogBox dbox = new DialogBox("Error on Save"); dbox.setErrorMessage(msg);
        dbox.showMessageDialog(getEditor());
        return;
    }
    
    // Add URL.String to RecentFilesMenu, clear undoer and reset UI
    if(getSourceURL()!=null) _recentFilesMenu.addMostRecentDocument(getSourceURL().getString());
    getViewerShape().getUndoer().reset();
    resetLater();
}

/**
 * The real save method.
 */
protected void saveImpl() throws Exception
{
    WebURL url = getSourceURL();
    WebFile file = url.getFile();
    if(file==null) file = url.createFile(false);
    file.setBytes(getViewer().getViewerShape().getContentXML().getBytes());
    file.save();
}

/**
 * Reloads the current editor document from the last saved version.
 */
public void revert()
{
    // Get filename (just return if null)
    WebURL surl = getSourceURL(); if(surl==null) return;

    // Run option panel for revert confirmation (just return if denied)
    String msg = "Revert to saved version of " + surl.getPathName() + "?";
    DialogBox dbox = new DialogBox("Revert to Saved"); dbox.setQuestionMessage(msg);
    if(!dbox.showConfirmDialog(getEditor())) return;
        
    // Re-open filename
    getSourceURL().getFile().refresh();
    open(getSourceURL());
}

/**
 * Window resized notification - if resized within 10 pts of preferred size, snap to preferred size.
 */
protected void windowResized()
{
    //Dimension wsize = getWindow().getSize(), psize = getWindow().getPreferredSize();
    //if(Math.abs(wsize.width - psize.width)<=10) wsize.width = psize.width;
    //if(Math.abs(wsize.height - psize.height)<=10) wsize.height = psize.height;
    //if(getWindow().getWidth()!=wsize.width || getWindow().getHeight()!=wsize.height) getWindow().setSize(wsize);
}

/**
 * Returns the suggested maximized bounds for window.
 */
protected void setWindowMaximizedBounds()
{
    if(!getWindow().isWindowSet()) return;
    Dimension size = getWindow().getPreferredSize();
    Rectangle bounds = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, size.width, size.height);
    getWindow().setMaximizedBounds(bounds);
}

/**
 * Closes this editor pane
 */
public boolean close()
{
    // Make sure editor isn't previewing
    setEditing(true);
    
    // If unsaved changes, run panel to request save
    if(getEditor().undoerHasUndos()) {
        String filename = getSourceURL()==null? "untitled document" : getSourceURL().getPathName();
        DialogBox dbox = new DialogBox("Unsaved Changes");
        dbox.setWarningMessage("Save changes to " + filename + "?"); dbox.setOptions("Save", "Don't Save", "Cancel");
        switch(dbox.showOptionDialog(getUI(), "Save")) {
            case 0: save();
            case 1: break;
            default: return false;
        }
    }
    
    // Deactive current tool, so it doesn't reference this editor
    getEditor().getCurrentTool().deactivateTool();
    
    // Remove editor from allEditors list
    RMEditorPaneUtils.removeEditorPane(this);
    
    // Close inspectors, close window, called EditorClosed and return true to indicate we closed the window
    closeInspectors();
    getWindow().windowDispose();
    editorClosed();
    return true;
}

/**
 * Called to close inspectors when window is closed.
 */
protected void closeInspectors()
{
    getInspectorPanel().setVisible(false);
    getAttributesPanel().setVisible(false);
}

/**
 * Called when editor is closed.
 */
protected void editorClosed()
{
    // If another open editor is available focus on it, otherwise run WelcomePanel
    if(RMEditorPaneUtils.getEditorPaneCount()>0)
        RMEditorPaneUtils.getEditorPane(0).getEditor().requestFocus();
    else Welcome.getShared().runWelcome();
}

/**
 * Property change listener to respond to editor chanes. 
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Handle ZoomFactor
    if(anEvent.getPropertyName().equals("ZoomFactor"))
        setWindowMaximizedBounds();
    
    // Do normal version
    super.propertyChange(anEvent);
}

/**
 * Catch Editor DeepChanges to register resetUI.
 */
public void deepChange(PropertyChangeListener aShape, PropertyChangeEvent anEvent)  { resetLater(); }

/**
 * Called when the app is about to exit to gracefully handle any open documents.
 */
public void quit()  { App.quitApp(); }

/**
 * Returns a popup menu for the editor.
 */
public void loadPopupMenu(JPopupMenu popupMenu)
{
    // Get selected shape (just return if page is selected)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    if(shape instanceof RMPage) {
        super.loadPopupMenu(popupMenu); return; }
    
    // Empty popup menu
    popupMenu.removeAll(); JMenuItem mi;

    // If RMTextShape, get copy of Format menu
    if(shape instanceof RMTextShape) { RMTextShape text = (RMTextShape)shape;

        // Get editor pane format menu and add menu items to popup
        JMenu formatMenu = getNode("FormatMenu", JMenu.class);
        JMenu formatMenuCopy = cloneMenuItem(formatMenu);
        while(formatMenuCopy.getItemCount()>0)
            popupMenu.add(formatMenuCopy.getItem(0));

        // If structured tablerow, add AddColumnMenuItem and SplitColumnMenuItem
        if(text.isStructured()) {
            mi = new JMenuItem("Add Column"); mi.setName("AddColumnMenuItem"); popupMenu.add(mi);
            mi = new JMenuItem("Split Column"); mi.setName("SplitColumnMenuItem"); popupMenu.add(mi);
        }
    }
    
    // Get copy of shapes menu and add menu items to popup
    JMenu shapesMenu = getNode("ShapesMenu", JMenu.class);
    JMenu shapesMenuCopy = cloneMenuItem(shapesMenu);
    while(shapesMenuCopy.getItemCount()>0)
        popupMenu.add(shapesMenuCopy.getItem(0));
    
    // Initialize popup menu items to send RibsEvents to menu bar
    Swing.getHelper(popupMenu).setOwner(popupMenu, null);
    getMenuBar().initUI(popupMenu);
}

/**
 * Copies a menu item.
 */
private <T extends JMenuItem> T cloneMenuItem(T aMI)
{
    T clone;
    if(aMI instanceof JMenu) { JMenu menu = (JMenu)aMI; clone = (T)new JMenu();
        for(int i=0, iMax=menu.getItemCount(); i<iMax; i++) { JMenuItem child = menu.getItem(i);
            JMenuItem cclone = cloneMenuItem(child); clone.add(cclone); }}
    else if(aMI instanceof JRadioButtonMenuItem) clone = (T)new JRadioButtonMenuItem();
    else if(aMI instanceof JCheckBoxMenuItem) clone = (T)new JCheckBoxMenuItem();
    else clone = (T)new JMenuItem(aMI.getText(), aMI.getIcon());
    clone.setText(aMI.getText()); clone.setIcon(aMI.getIcon()); clone.setName(aMI.getName());
    clone.setSelected(aMI.isSelected());
    return clone;
}

}