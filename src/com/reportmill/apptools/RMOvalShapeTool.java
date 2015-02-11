package com.reportmill.apptools;
import com.reportmill.graphics.RMStroke;
import com.reportmill.shape.*;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.*;
import snap.swing.SwingEvent;

/**
 * A tool subclass for editing RMOval.
 */
public class RMOvalShapeTool <T extends RMOvalShape> extends RMTool <T> {
    
/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { T shape = super.newInstance(); shape.setStroke(new RMStroke()); return shape; }

/**
 * Updates the Swing UI controls from the currently selected oval.
 */
public void resetUI()
{    
    RMOvalShape oval = getSelectedShape(); if(oval==null) return;
    setNodeValue("StartThumb", oval.getStartAngle());
    setNodeValue("StartText", oval.getStartAngle());
    setNodeValue("SweepThumb", oval.getSweepAngle());
    setNodeValue("SweepText", oval.getSweepAngle());
}

/**
 * Updates the currently selected oval from the Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    RMOvalShape oval = getSelectedShape(); if(oval==null) return;
    List <RMOvalShape> ovals = (List)getSelectedShapes();
    
    // Handle Start Angle ThumbWheel & Text
    if(anEvent.equals("StartThumb") || anEvent.equals("StartText")) {
        oval.undoerSetUndoTitle("Start Angle Change");
        for(RMOvalShape o : ovals)
            o.setStartAngle(anEvent.getFloatValue());
    }

    // Handle Sweep Angle ThumbWheel & Text
    if(anEvent.equals("SweepThumb") || anEvent.equals("SweepText")) {
        oval.undoerSetUndoTitle("Sweep Angle Change");
        for(RMOvalShape o : ovals)
            o.setSweepAngle(anEvent.getFloatValue());
    }
}

/**
 * Event handling - overridden to install crosshair cursor.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Install crosshair cursor if missing
    if(getEditor().getCursor()!=Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
}

/**
 * Returns the shape class this tool is responsible for.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMOvalShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Oval Tool"; }

}