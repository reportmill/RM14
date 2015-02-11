package com.reportmill.apptools;
import com.reportmill.shape.*;
import java.util.List;
import snap.swing.SwingEvent;

/**
 * Provides Swing UI editing for RMAnimPathShape.
 */
public class RMAnimPathShapeTool extends RMPolygonShapeTool {

/**
 * Updates the Swing UI controls from the currently selected oval.
 */
public void resetUI()
{
    // Get path shape
    RMAnimPathShape animpathShape = (RMAnimPathShape)getSelectedShape(); if(animpathShape==null) return;

    // Update position slider [show range in ui as 0-100]
    setNodeValue("PositionSlider", animpathShape.getDistance()*100);
    setNodeValue("PositionText", animpathShape.getDistance()*100);
    
    // Update alignment
    setNodeValue("AlignmentCheckBox", animpathShape.getPreservesOrientation());
    
    // Update origin matrix
    setNodeValue("align"+animpathShape.getChildOrigin(), true);
}

/**
* Updates the currently selected oval from the Swing UI controls.
*/
public void respondUI(SwingEvent anEvent)
{
    // Get selected path shape (just return if null)
    RMAnimPathShape animpathShape = (RMAnimPathShape)getSelectedShape(); if(animpathShape==null) return;
    
    // Let superclass handle pop-up menu
    super.respondUI(anEvent);
    
    // Get shapes
    List <RMAnimPathShape> pathShapes = getSelectedShapes();
    
    // Update all selected animpath shapes
    for(RMAnimPathShape shape : pathShapes) {
        
        // Handle distance slider or text field
        if(anEvent.equals("PositionSlider") || anEvent.equals("PositionText"))
            shape.setDistance(anEvent.getFloatValue()/100f);
        
        // Handle alignment checkbox
        else if(anEvent.equals("AlignmentCheckBox"))
            shape.setPreservesOrientation(anEvent.getBoolValue());
        
        // Handle origin matrix
        else if(anEvent.getName().startsWith("align"))
            shape.setChildOrigin(anEvent.getName().charAt(5)-'0'); // buttons in matrix are named align0-align9
        
        // Handle reverse path button - Bounds of path should be the same, so there's no reason to call resetPath()
        else if(anEvent.equals("ReversePathButton"))
            shape.setPath(shape.getPath().getReversedPath());
    }
}

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return RMAnimPathShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Animation Path Tool"; }

}