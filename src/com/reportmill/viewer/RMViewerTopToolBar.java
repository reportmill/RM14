package com.reportmill.viewer;
import javax.swing.JButton;
import snap.swing.*;

/**
 * Swing UI controls for RMViewerPlus top.
 */
public class RMViewerTopToolBar extends SwingOwner {

    // The viewer associated with this tool bar
    RMViewerPane    _viewerPane;
    
/**
 * Creates a new top ui.
 */
public RMViewerTopToolBar(RMViewerPane aViewerPane)  { _viewerPane = aViewerPane; }

/**
 * Returns the viewer pane.
 */
public RMViewerPane getViewerPane()  { return _viewerPane; }

/**
 * Returns the viewer.
 */
public RMViewer getViewer()  { return getViewerPane().getViewer(); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set roll over icons for tool bar buttons
    IconUtils.setRolloverIcons(getNode("SaveButton", JButton.class), false);
    IconUtils.setRolloverIcons(getNode("PreviewPDFButton", JButton.class), false);
    IconUtils.setRolloverIcons(getNode("PrintButton", JButton.class), false);
    IconUtils.setRolloverIcons(getNode("CopyButton", JButton.class), false);
}

/**
 * Resets to Swing UI.
 */
public void resetUI()  { }

/**
 * Responds to Swing UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle SaveButton
    if(anEvent.equals("SaveButton"))
        getViewerPane().save();
    
    // Handle PrintButton
    if(anEvent.equals("PrintButton"))
        getViewerPane().print();
    
    // Handle CopyButton
    if(anEvent.equals("CopyButton"))
        getViewerPane().copy();
    
    // Handle File PreviewPDFButton
    if(anEvent.equals("PreviewPDFButton"))
        getViewerPane().previewPDF();
        
    // Handle MoveButton
    if(anEvent.equals("MoveButton"))
        getViewer().setInputAdapter(new RMViewerInputAdapterImpl(getViewer()));
    
    // Handle TextButton
    if(anEvent.equals("TextButton"))
        getViewer().setInputAdapter(new RMViewerTextSelector(getViewer()));

    // Handle SelectButton
    if(anEvent.equals("SelectButton"))
        getViewer().setInputAdapter(new RMViewerImageSelector(getViewer()));
}

}