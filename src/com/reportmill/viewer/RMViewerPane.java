package com.reportmill.viewer;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import snap.swing.*;
import snap.web.WebURL;

/**
 * This class is a container for a viewer and tool bars. The default tool bars add document controls (save,
 * print, copy), input controls (select, pan, text select, image select), zoom controls and page controls. 
 */
public class RMViewerPane extends SwingOwner implements PropertyChangeListener {

    // The real viewer
    RMViewer          _viewer;
    
    // The scroll pane for this viewer
    JScrollPane       _scrollPane;
    
    // Insets for scroll pane with regard to tool bars
    Insets            _insets = new Insets(0, 0, 0, 0);
    
    // The controls at the top of the document
    SwingOwner        _topToolBar;
    
    // The controls at the bottom of the document
    SwingOwner        _btmToolBar;
    
/**
 * Initializes the UI.
 */
protected JComponent createUI()
{
    // Create and configure viewer
    _viewer = createViewer();
    _viewer.addPropertyChangeListener(this); // Listen to PropertyChanges
    _viewer.setComponentPopupMenu(new DynamicPopupMenu()); // Install DynamicPopupMenu
    _scrollPane = new JScrollPane(_viewer); // Add to ScrollPane
    
    // Get top and bottom tool bars
    SwingOwner topToolBar = getTopToolBar();
    SwingOwner bottomToolBar = getBottomToolBar();
    
    // Add Viewer ScrollPane and ToolBars
    JComponent ui = new JPanel(new BorderLayout());
    ui.add(_scrollPane, BorderLayout.CENTER);
    ui.add(topToolBar.getUI(), BorderLayout.NORTH);
    ui.add(bottomToolBar.getUI(), BorderLayout.SOUTH);
    return ui;
}

/**
 * Returns the viewer for this viewer pane.
 */
public RMViewer getViewer()  { getUI(); return _viewer; }

/**
 * Sets the viewer for this viewer pane.
 */
protected void setViewer(RMViewer aViewer)  { _viewer = aViewer; getScrollPane().setViewportView(_viewer); }

/**
 * Creates the real viewer for this viewer plus.
 */
protected RMViewer createViewer()  { return new RMViewer(); }

/**
 * Returns the scroll pane for this viewer plus.
 */
public JScrollPane getScrollPane()  { return _scrollPane; }

/**
 * Returns the viewer shape.
 */
public RMViewerShape getViewerShape()  { return getViewer().getViewerShape(); }

/**
 * Returns the content shape.
 */
public RMParentShape getContent()  { return getViewer().getContent(); }

/**
 * Returns the RMDocument associated with this viewer.
 */
public RMDocument getDocument()  { return getViewer().getDocument(); }

/**
 * Returns the document source.
 */
protected WebURL getSourceURL()  { return getViewer().getSourceURL(); }

/**
 * Returns the top controls.
 */
public SwingOwner getTopToolBar()  { return _topToolBar!=null? _topToolBar : (_topToolBar=createTopToolBar()); }

/**
 * Creates the top tool bar.
 */
public SwingOwner createTopToolBar()  { return new RMViewerTopToolBar(this); }

/**
 * Returns the bottom controls.
 */
public SwingOwner getBottomToolBar()  { return _btmToolBar!=null? _btmToolBar : (_btmToolBar=createBottomToolBar()); }

/**
 * Creates bottom tool bar.
 */
public SwingOwner createBottomToolBar()  { return new RMViewerBottomToolBar(this); }

/**
 * Saves the current viewer document.
 */
public void save()  { }

/**
 * Prints the current viewer document.
 */
public void print()  { getViewer().print(); }

/**
 * Copies the current viewer document selection.
 */
public void copy()  { getViewer().getInputAdapter().copy(); }

/**
 * Previews the current viewer document as pdf.
 */
public void previewPDF()
{
    getDocument().writePDF(RMUtils.getTempDir() + "RMPDFFile.pdf");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMPDFFile.pdf");
}

/**
 * Resets UI.
 */
protected void resetUI()
{
    getTopToolBar().resetLater();
    getBottomToolBar().resetLater();
}

/**
 * Respond to UI controls. 
 */
protected void respondUI(SwingEvent anEvent)
{
    // Handle AboutMenuItem
    if(anEvent.equals("AboutMenuItem")) {
        DialogBox dbox = new DialogBox("About Player");
        dbox.setMessage("ReportMill Player, Version " + RMUtils.getBuildInfo());
        dbox.showMessageDialog(getUI());
    }
    
    // Handle ShowFrameRateMenuItem
    else if(anEvent.equals("ShowFrameRateMenuItem"))
        getViewer().setPaintFrameRate(!getViewer().getPaintFrameRate());
    
    // Handle ShowAllDrawingMenuItem
    else if(anEvent.equals("ShowAllDrawingMenuItem")) {
        if(anEvent.getBoolValue())
            GraphicsDebugPane.installDebugPane(getViewer());
        else GraphicsDebugPane.removeDebugPane(getViewer());
    }
}

/**
 * ResetUI on PropertyChange.
 */
public void propertyChange(PropertyChangeEvent anEvent)  { resetLater(); }

/**
 * A dynamic popup menu class.
 */
protected class DynamicPopupMenu extends JPopupMenu {

    /** Override to call loadPopupMenu. */
    public void setVisible(boolean b)  { if(b) loadPopupMenu(this); super.setVisible(b); }
}

/**
 * Returns a popup menu.
 */
protected void loadPopupMenu(JPopupMenu popupMenu)
{
    // Empty popup menu and add new items
    popupMenu.removeAll();
    JMenuItem mi = new JMenuItem("About Viewer..."); mi.setName("AboutMenuItem"); popupMenu.add(mi);
    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Show Frame Rate", getViewer().getPaintFrameRate());
    cbmi.setName("ShowFrameRateMenuItem"); popupMenu.add(cbmi);
    cbmi = new JCheckBoxMenuItem("Show All Drawing", GraphicsDebugPane.isDebugPaneInstalled(getUI()));
    cbmi.setName("ShowAllDrawingMenuItem"); popupMenu.add(cbmi);
    Swing.getHelper(popupMenu).setOwner(popupMenu, null);
    initUI(popupMenu);
}

}