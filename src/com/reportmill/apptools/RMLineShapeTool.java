package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.graphics.RMPath;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * This class handles creation of lines.
 */
public class RMLineShapeTool <T extends RMLineShape> extends RMTool <T> {
    
    // Indicates whether line should try to be strictly horizontal or vertical
    boolean               _hysteresis = false;
    
    // The list of arrow head shapes
    List <RMLineShape>  _arrowShapes;

    // Constants for line segment points
    public static final byte HandleStartPoint = 0;
    public static final byte HandleEndPoint = 1;

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMLineShape.class; }

/**
 * Returns the name of this tool to be displayed by inspector.
 */
public String getWindowTitle()  { return "Line Inspector"; }

/**
 * Event handling - overridden to install cross-hair cursor.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Install cross-hair cursor if missing
    if(getEditor().getCursor()!=Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
}

/**
 * Handles mouse press for line creation.
 */
public void mousePressed(MouseEvent anEvent)
{
    super.mousePressed(anEvent);
    _hysteresis = true;
}

/**
 * Handles mouse drag for line creation.
 */
public void mouseDragged(MouseEvent anEvent)
{
    RMPoint currentPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    double dx = currentPoint.x - _downPoint.x;
    double dy = currentPoint.y - _downPoint.y;
    double breakingPoint = 20f;
    
    if(_hysteresis) {
        if(Math.abs(dx) > Math.abs(dy)) {
            if(Math.abs(dy) < breakingPoint) dy = 0;
            else _hysteresis = false;
        }
        
        else if(Math.abs(dx) < breakingPoint) dx = 0;
        else _hysteresis = false;
    }
    
    // Register shape for repaint
    _shape.repaint();
    
    // Set adjusted bounds
    _shape.setBounds(_downPoint.x, _downPoint.y, dx, dy);
}

/**
 * Editor method (returns the number of handles).
 */
public int getHandleCount(T aShape)  { return 2; }

/**
 * Editor method.
 */
public RMPoint getHandlePoint(T aShape, int anIndex, boolean isSuperSelected)
{
    return super.getHandlePoint(aShape, anIndex==HandleEndPoint? HandleSE : anIndex, isSuperSelected);
}

/**
 * Editor method.
 */
public void moveShapeHandle(T aShape, int aHandle, RMPoint aPoint)
{
    super.moveShapeHandle(aShape, aHandle==HandleEndPoint? HandleSE : aHandle, aPoint);
}

/**
 * Loads the list of arrow shapes from a .rpt file.
 */
public List <RMLineShape> getArrows()
{
    if(_arrowShapes==null) {

        // Load arrows doc
        RMDocument doc = new RMDocument(getClass().getResourceAsStream("RMLineShapeTool.ribs/ArrowHeads.rpt"));

        // Get arrow shape
        _arrowShapes = doc.getChildrenWithClass(getShapeClass());
    }
    
    // Return arrow shapes
    return _arrowShapes;
}

/**
 * Finds the arrow type for given line segment.
 */
public int findArrow(RMLineShape anArrow)
{
    List <RMLineShape> arrows = getArrows();
    RMPath headPath = anArrow.getArrowHead().getPath();
    for(int i=0, n=arrows.size(); i<n; ++i) {
        RMLineShape template = arrows.get(i);
        if(template.getArrowHead().getPath().equals(headPath))
            return i;
    }
    
    return -1;
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get arrows menu button
    MenuButton menuButton = getNode("ArrowsMenuButton", MenuButton.class);
        
    // Add arrows menu button
    for(RMLineShape line : getArrows()) {
        RMShapeIcon icon = new RMShapeIcon(line.getArrowHead(), 36, 24);
        JMenuItem menuItem = new JMenuItem(icon);
        menuItem.setName("ArrowsMenuButtonMenuItem");
        menuButton.getPopupMenu().add(menuItem);
    }
    
    // Add "None" menu item
    JMenuItem menuItem = new JMenuItem("None"); menuItem.setName("ArrowsMenuButtonMenuItem");
    menuButton.getPopupMenu().add(menuItem);
}

/**
 * Update UI panel.
 */
public void resetUI()
{
    // Get selected line
    RMLineShape line = getSelectedShape(); if(line==null) return;
    
    // Get arrow head
    RMLineShape.ArrowHead arrowHead = line.getArrowHead();

    // Update ArrowsMenuButton
    getNode("ArrowsMenuButton", JLabel.class).setIcon(new RMShapeIcon(arrowHead, 36, 24));

    // Update ScaleText and ScaleThumbWheel
    setNodeValue("ScaleText", arrowHead!=null? arrowHead.getScaleX() : 0);
    setNodeValue("ScaleThumbWheel", arrowHead!=null? arrowHead.getScaleX() : 0);
    setNodeEnabled("ScaleText", arrowHead!=null);
    setNodeEnabled("ScaleThumbWheel", arrowHead!=null);
}

/**
 * Respond to UI change.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected shape
    RMLineShape line = getSelectedShape();
    
    // Get arrow head
    RMLineShape.ArrowHead arrowHead = line.getArrowHead();

    // Handle ScaleText and ScaleThumbWheel
    if(anEvent.equals("ScaleText") || anEvent.equals("ScaleThumbWheel"))
        arrowHead.setScaleXY(anEvent.getFloatValue(), anEvent.getFloatValue());

    // Handle ArrowsMenuButtonMenuItem
    if(anEvent.equals("ArrowsMenuButtonMenuItem")) {
        RMShapeIcon icon = (RMShapeIcon)anEvent.getTarget(JMenuItem.class).getIcon();
        line.setArrowHead(icon==null? null : (RMLineShape.ArrowHead)icon.getShape().clone());
    }
}

}