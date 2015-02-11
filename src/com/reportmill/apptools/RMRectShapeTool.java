package com.reportmill.apptools;
import com.reportmill.graphics.RMStroke;
import com.reportmill.shape.*;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.*;
import snap.swing.SwingEvent;

/**
 * This class handles editing of rectangle shapes.
 */
public class RMRectShapeTool <T extends RMRectShape> extends RMTool <T> {
    
/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { T shape = super.newInstance(); shape.setStroke(new RMStroke()); return shape; }

/**
 * Updates the Swing UI controls from the currently selected rectangle.
 */
public void resetUI()
{
    // Get selected rectangle (just return if null)
    RMRectShape rect = getSelectedShape(); if(rect==null) return;
    
    // Update RoundingThumb and RoundingText
    setNodeValue("RoundingThumb", rect.getRadius());
    setNodeValue("RoundingText", rect.getRadius());
}

/**
 * Updates the currently selected rectangle from the Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the current rectangle (just return if null)
    RMRectShape rect = getSelectedShape(); if(rect==null) return;
    
    // Get selected rectangles
    List <RMRectShape> rects = (List)getSelectedShapes();
    
    // Register rects for repaint (and thus undo)
    RMShapeUtils.repaint(rects);

    // Handle Rounding Radius Thumb & Text
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
        rect.undoerSetUndoTitle("Rounding Change");
        float value = anEvent.getFloatValue();
        for(RMRectShape r : rects) {
            r.setRadius(value);
            if(r.getStroke()==null)
                r.setStroke(new RMStroke());
        }
    }
}

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
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMRectShape.class; }

/**
 * Returns the name to be presented to user.
 */
public String getWindowTitle()  { return "Rectangle Tool"; }

}