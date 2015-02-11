package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.shape.*;

/**
 * Tool for RMFlowShape.
 */
public class RMFlowShapeTool <T extends RMFlowShape> extends RMParentShapeTool <T> {

/**
 * Override to return shape class.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMFlowShape.class; }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Flow Shape Inspector"; }

/**
 * Resets the UI associated with this tool.
 */
protected void resetUI()
{
    // Get selected shape (just return if null)
    RMFlowShape shape = getSelectedShape(); if(shape==null) return;
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    //if(shape.getAlignX()==snap.ui.LayoutTypes.AlignX.Left) setNodeValue("AlignLeftButton", true);
    //else if(shape.getAlignX()==snap.ui.LayoutTypes.AlignX.Center) setNodeValue("AlignCenterButton", true);
    //else if(shape.getAlignX()==snap.ui.LayoutTypes.AlignX.Right) setNodeValue("AlignRightButton", true);
    //else if(shape.getAlignX()==snap.ui.LayoutTypes.AlignX.Full) setNodeValue("AlignFullButton", true);
    
    // Handle AlignTopButton, AlignMiddleButton, AlignBottomButton
    //if(shape.getAlignY()==snap.ui.LayoutTypes.AlignY.Top) setNodeValue("AlignTopButton", true);
    //if(shape.getAlignY()==snap.ui.LayoutTypes.AlignY.Middle) setNodeValue("AlignMiddleButton", true);
    //if(shape.getAlignY()==snap.ui.LayoutTypes.AlignY.Bottom) setNodeValue("AlignBottomButton", true);
}

/**
 * Responder callback for the UI associated with this tool.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Get selected shape (just return if null)
    RMFlowShape shape = getSelectedShape(); if(shape==null) return;
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    //if(anEvent.equals("AlignLeftButton")) shape.setAlignX(snap.ui.LayoutTypes.AlignX.Left);
    //if(anEvent.equals("AlignCenterButton")) shape.setAlignX(snap.ui.LayoutTypes.AlignX.Center);
    //if(anEvent.equals("AlignRightButton")) shape.setAlignX(snap.ui.LayoutTypes.AlignX.Right);
    //if(anEvent.equals("AlignFullButton")) shape.setAlignX(snap.ui.LayoutTypes.AlignX.Full);
    
    // Handle AlignTopButton, AlignMiddleButton, AlignBottomButton
    //if(anEvent.equals("AlignTopButton")) shape.setAlignY(snap.ui.LayoutTypes.AlignY.Top);
    //if(anEvent.equals("AlignMiddleButton")) shape.setAlignY(snap.ui.LayoutTypes.AlignY.Middle);
    //if(anEvent.equals("AlignBottomButton")) shape.setAlignY(snap.ui.LayoutTypes.AlignY.Bottom);
}

/**
 * Returns whether a given shape is super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Returns whether a given shape accepts children.
 */
public boolean getAcceptsChildren(RMShape aShape)  { return true; }

}