package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import snap.swing.*;

/**
 * Tool for start shape.
 */
public class RMStarShapeTool <T extends RMStarShape> extends RMTool <T> {

/**
 * Updates the UI controls from the currently selected star.
 */
public void resetUI()
{    
    RMStarShape star = getSelectedShape(); if(star==null) return;
    boolean poly = star.isPolygon();
    
    // Update the UI to match the shape
    if(poly==getNode("BloatPanel").isVisible()) {
        
        // Change the title of the box surrounding the spinner to either Points or Sides
        JPanel pointsPanel = getNode("PointsPanel", JPanel.class);
        TitledBorder border = (TitledBorder)pointsPanel.getBorder();
        border.setTitle(poly? "Sides" : "Points");
        
        // Show or hide the bloat controller
        getNode("BloatPanel", JPanel.class).setVisible(!poly);
        
        // select the correct radio/toggle button
        getNode(poly ? "PolysButton":"StarsButton", JToggleButton.class).setSelected(true);
    }
  
    setNodeValue("PointSpinner", star.getNumPoints());
    if(!poly) {
        setNodeValue("BloatWheel", star.getBloat());
        setNodeValue("BloatField", star.getBloat());
    }
}

/**
 * Updates the currently selected oval from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    List <RMStarShape> stars = (List)getSelectedShapes();
    
    // Set the number of sides from the spinner
    if(anEvent.equals("PointSpinner")) {
        int p = anEvent.getIntValue();
        for(RMStarShape s : stars)
            s.setNumPoints(p);
    }
    
    // Change to either polygon or star if one of the toggle buttons was hit
    else if(anEvent.equals("StarsButton") || anEvent.equals("PolysButton"))
        for(RMStarShape s : stars)
            s.setIsPolygon(anEvent.equals("PolysButton"));
    
    // bloat (only applicable to stars)
    else if(anEvent.equals("BloatWheel") || anEvent.equals("BloatField"))
        for(RMStarShape s : stars)
            s.setBloat(anEvent.getFloatValue());
}

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return RMStarShape.class; }

/**
 * Override to set stroke on new instance.
 */
protected T newInstance()  { T shape = super.newInstance(); shape.setStroke(new RMStroke()); return shape; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Star Tool"; }

/**
 * Event handling for shape creation.
 */
public void mouseDragged(MouseEvent anEvent)
{
    _shape.repaint();
    RMPoint currentPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    resizeStar((RMStarShape)_shape, _downPoint, currentPoint);
}

/**
 * Event handling for shape creation.
 */
public void mousePressed(MouseEvent anEvent)
{
    super.mousePressed(anEvent);
    
    // undocumented little feature
    if(Swing.isAltDown())
        ((RMStarShape)_shape).setStarType(RMStarShape.MAGIC);
}

/**
 * Returns the number of handles for this shape.
 */
public int getHandleCount(T aStarShape)  { return aStarShape.getNumPoints(); }

/**
 * Returns the point for the handle of the given shape at the given handle index in the given shape's coords.
 */
public RMPoint getHandlePoint(T aStarShape, int aHandle, boolean isSuperSelected)
{
    RMPath path = aStarShape.getPathInBounds();
    boolean poly = aStarShape.isPolygon();
    return path.getPoint(poly?aHandle : aHandle*2);
}

/**
 * Moves the handle at the given index to the given point.
 */
public void moveShapeHandle(T aShape, int aHandle, RMPoint toPoint)
{
    // Get center point of shape in parent's coords
    RMRect r = aShape.getFrame();
    RMPoint p1 = new RMPoint(r.getCenterX(), r.getCenterY());
    aShape.repaint();
    resizeStar(aShape, p1, toPoint);
}

/**
 * Returns the cursor for given handle.
 */
public Cursor getHandleCursor(T aShape, int aHandle) { return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR); }

/**
 * Set the frame of the star based on a center point and 
 * one of the vertices.
 */
public void resizeStar(RMStarShape s, RMPoint from, RMPoint to)
{
    double dx = to.x-from.x;
    double dy = to.y-from.y;
    double rad = Math.sqrt(dx*dx+dy*dy);
    double newTheta = Math.toDegrees(Math.atan2(dy,dx));

    s.setRoll(0);
    s.setStartAngle(0);
    s.setFrame(from.x-rad, from.y-rad, 2*rad, 2*rad);
    s.setRoll(newTheta);
}

}