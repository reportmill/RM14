package com.reportmill.apptools;
import com.reportmill.shape.*;
import java.util.List;
import javax.swing.JSlider;
import snap.swing.SwingEvent;

/**
 * Provides Swing UI editing for RMMorphShape.
 */
public class RMMorphShapeTool <T extends RMMorphShape> extends RMTool <T> {

/**
 * Updates the Swing UI controls from the currently selected oval.
 */
public void resetUI()
{
    // Get morph shape
    RMMorphShape morphShape = getSelectedShape(); if(morphShape==null) return;

    // Update MorphingSlider Value + Maximum
    setNodeValue("MorphingSlider", morphShape.getMorphing()*100);
    getNode("MorphingSlider", JSlider.class).setMaximum(100*(morphShape.getShapeCount()-1));
    
    // Update MorphingText
    setNodeValue("MorphingText", morphShape.getMorphing());
}

/**
* Updates the currently selected oval from the Swing UI controls.
*/
public void respondUI(SwingEvent anEvent)
{
    // Get selected morphg shape and selected shapes (just return if null)
    RMMorphShape morphShape = getSelectedShape(); if(morphShape==null) return;
    List <RMMorphShape> morphShapes = (List)getSelectedShapes();
    
    // Handle MorphingSlider
    if(anEvent.equals("MorphingSlider"))
        for(RMMorphShape shape : morphShapes)
            shape.setMorphing(anEvent.getFloatValue()/100f);

    // Handle MorphingText
    if(anEvent.equals("MorphingText"))
        for(RMMorphShape shape : morphShapes)
            shape.setMorphing(anEvent.getFloatValue());
}

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return RMMorphShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "MorphShape Tool"; }

}