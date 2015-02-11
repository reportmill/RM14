package com.reportmill.apptools;
import com.reportmill.shape.*;
import snap.swing.SwingEvent;

/**
 * Provides UI editing for RMAnimPathShape.
 */
public class RMAnimPathShapeTool <T extends RMAnimPathShape> extends RMTool <T> {

/**
 * Updates the UI controls from the currently selected oval.
 */
public void resetUI()
{
    // Get path shape
    RMAnimPathShape aps = getSelectedShape(); if(aps==null) return;

    // Update position slider [show range in ui as 0-100]
    setNodeValue("PositionSlider", aps.getDistance()*100);
    setNodeValue("PositionText", aps.getDistance()*100);
    
    // Update alignment
    setNodeValue("AlignmentCheckBox", aps.getPreservesOrientation());
    
    // Update origin matrix
    setNodeValue("align" + aps.getChildOrigin(), true);
}

/**
 * Updates the currently selected oval from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected path shape (just return if null)
    RMAnimPathShape aps = getSelectedShape(); if(aps==null) return;
    
    // Handle PositionSlider or PositionText
    if(anEvent.equals("PositionSlider") || anEvent.equals("PositionText"))
        aps.setDistance(anEvent.getFloatValue()/100f);
    
    // Handle AlignmentCheckBox
    if(anEvent.equals("AlignmentCheckBox"))
        aps.setPreservesOrientation(anEvent.getBoolValue());
    
    // Handle origin matrix
    if(anEvent.getName().startsWith("align"))
        aps.setChildOrigin(anEvent.getName().charAt(5)-'0'); // buttons in matrix are named align0-align9
    
    // Handle ReversePathButton - Bounds of path should be the same, so there's no reason to call resetPath()
    if(anEvent.equals("ReversePathButton"))
        aps.setPath(aps.getPath().getReversedPath());
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