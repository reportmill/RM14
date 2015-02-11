package com.reportmill.viewer;
import com.reportmill.base.RMUtils;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.JButton;
import snap.swing.*;

/**
 * Swing UI controls for RMViewerPane bottom.
 */
public class RMViewerBottomToolBar extends SwingOwner {

    // The viewer associated with this tool bar
    RMViewerPane    _viewerPane;

/**
 * Creates a new bottom ui.
 */
public RMViewerBottomToolBar(RMViewerPane aViewerPane)  { _viewerPane = aViewerPane; }

/**
 * Returns the viewer pane.
 */
public RMViewerPane getViewerPane()  { return _viewerPane; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set right arrow in PageForwardButton
    Polygon p1 = new Polygon(new int[] { 4, 10, 4}, new int[] { 5, 11, 17 }, 3);
    getNode("PageForwardButton", JButton.class).setIcon(SwingUtils.getImageIcon(p1, Color.darkGray, 14, 22));
    
    // Set left arrow in PageBackButton
    Polygon p2 = new Polygon(new int[] { 10, 4, 10}, new int[] { 5, 11, 17 }, 3);
    getNode("PageBackButton", JButton.class).setIcon(SwingUtils.getImageIcon(p2, Color.darkGray, 14, 22));
    
    // Set left arrow plus stop bar in PageBackAllButton
    GeneralPath p3 = new GeneralPath(p2);
    p3.transform(AffineTransform.getTranslateInstance(2, 0));
    p3.append(new Rectangle(2, 6, 2, 10), false);
    getNode("PageBackAllButton", JButton.class).setIcon(SwingUtils.getImageIcon(p3, Color.darkGray, 14, 22));
    
    // Set right arrow plus stop bar in PageForwardAllButton
    GeneralPath p4 = new GeneralPath(p1);
    p4.transform(AffineTransform.getTranslateInstance(-2, 0));
    p4.append(new Rectangle(10, 6, 2, 10), false);
    getNode("PageForwardAllButton", JButton.class).setIcon(SwingUtils.getImageIcon(p4, Color.darkGray, 14, 22));
    
    // Reset PreferredSize
    getUI().setPreferredSize(new Dimension(500, 22));
}

/**
 * Resets UI.
 */
protected void resetUI()
{
    // Get viewer pane
    RMViewerPane viewerPane = getViewerPane();
    RMViewer viewer = viewerPane.getViewer();
    
    // Reset ZoomText
    setNodeValue("ZoomText", Math.round(viewer.getZoomFactor()*100) + "%");
    
    // Reset PageText field
    String pageText = "" + (viewer.getSelectedPageIndex()+1) + " of " + viewer.getPageCount();
    setNodeValue("PageText", pageText);
    
    // Reset pageforward enabled
    setNodeEnabled("PageBackButton", viewer.getSelectedPageIndex()>0);
    setNodeEnabled("PageBackAllButton", viewer.getSelectedPageIndex()>0);
    setNodeEnabled("PageForwardButton", viewer.getSelectedPageIndex()<viewer.getPageCount()-1);
    setNodeEnabled("PageForwardAllButton", viewer.getSelectedPageIndex()<viewer.getPageCount()-1);
}

/**
 * Responds to UI changes.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Get viewer pane and viewer
    RMViewerPane viewerPane = getViewerPane();
    RMViewer viewer = viewerPane.getViewer();
    
    // Handle ZoomComboBox
    if(anEvent.equals("ZoomText"))
        viewer.setZoomFactor(anEvent.getFloatValue()/100);
    
    // Handle ZoomMenuButton
    if(anEvent.equals("ZoomMenuItem"))
        viewer.setZoomFactor(RMUtils.floatValue(anEvent.getText())/100);
    
    // Handle ZoomToActualSizeMenuItem - use screen resolution to figure out zooming for actual size
    if(anEvent.equals("ZoomToActualSizeMenuItem"))
        viewer.setZoomFactor(viewer.getZoomToActualSizeFactor());
    
    // Handle ZoomToFitMenuItem
    if(anEvent.equals("ZoomToFitMenuItem"))
        viewer.setZoomMode(RMViewer.ZoomMode.ZoomToFit);
    
    // Handle ZoomAsNeededMenuItem
    if(anEvent.equals("ZoomAsNeededMenuItem"))
        viewer.setZoomMode(RMViewer.ZoomMode.ZoomAsNeeded);
    
    // Handle PageText
    if(anEvent.equals("PageText"))
        viewer.setSelectedPageIndex(anEvent.getIntValue()-1);
    
    // Handle PageBackButton
    if(anEvent.equals("PageBackButton"))
        viewer.pageBack();
    
    // Handle PageBackAllButton
    if(anEvent.equals("PageBackAllButton"))
        viewer.setSelectedPageIndex(0);
    
    // Handle PageForwardButton
    if(anEvent.equals("PageForwardButton"))
        viewer.pageForward();
    
    // Handle PageForwardAllButton
    if(anEvent.equals("PageForwardAllButton"))
        viewer.setSelectedPageIndex(viewer.getPageCount()-1);
    
    // Have viewer pane reset
    viewerPane.resetLater();
}

}