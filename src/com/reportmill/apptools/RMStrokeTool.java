package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.graphics.*;
import com.reportmill.shape.RMShape;
import java.util.*;
import snap.swing.SwingEvent;

/**
 * UI editing for RMStroke.
 */
public class RMStrokeTool extends RMFillTool {

    // The last list of strokes provided to UI
    List <RMStroke>  _strokes;

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
public List <RMStroke> getStrokes()  { return _strokes; }

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
private List <RMStroke> createStrokes()
{
    RMEditor editor = getEditor();
    List <RMStroke> strokes = new ArrayList();
    for(RMShape shape : editor.getSelectedOrSuperSelectedShapes())
        strokes.add(shape.getStroke()!=null? shape.getStroke() : new RMStroke());
    return _strokes = strokes;
}

/**
 * Override to load Strokes list.
 */
public void processResetUI()
{
    _strokes = createStrokes();
    super.processResetUI();
}

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    
    // Update StrokeColorWell
    setNodeValue("StrokeColorWell", shape.getStrokeColor().awt());
}

/**
 * Respond to UI changes
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle StrokeColorWell - get color and set in selected shapes
    if(anEvent.equals("StrokeColorWell")) {
        RMEditor editor = getEditor();
        List <RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
        RMColor color = new RMColor(anEvent.getColorValue());
        for(RMShape s : shapes)
            s.setStrokeColor(color);
    }
    
    // If changes were made to stand-in stroke(s), install into respective shape(s)
    RMEditor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = editor.getSelectedOrSuperSelectedShape(i);
        if(shape.getStroke()!=_strokes.get(i))
            shape.setStroke(_strokes.get(i));
    }
}

}