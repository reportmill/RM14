package com.reportmill.swing.tool;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.swing.shape.JSeparatorShape;
import javax.swing.*;
import snap.swing.SwingEvent;

/**
 * Provides Swing UI editing for JSeparatorShape.
 */
public class JSeparatorTool <T extends JSeparatorShape> extends JComponentTool <T> {

/**
 * Updates the UI from the currently selected radio button shape.
 */
public void resetUI()
{    
    // Get currently selected separator shape (just return if null)
    JSeparatorShape seperator = getSelectedShape(); if(seperator==null) return;
    int orientation = seperator.getOrientation();
    
    // Update HorizontalRadioButton and VerticalRadioButton
    setNodeValue("HorizontalRadioButton", orientation==JSeparator.HORIZONTAL);
    setNodeValue("VerticalRadioButton", orientation==JSeparator.VERTICAL);
}

/**
 * Updates the currently selected radio button shape from the UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected separator shape (just return if null)
    JSeparatorShape separatorShape = getSelectedShape(); if(separatorShape==null) return;
    
    // Handle HorizontalRadioButton, VerticalRadioButton
    if(anEvent.equals("HorizontalRadioButton")) separatorShape.setOrientation(JSeparator.HORIZONTAL);
    else if(anEvent.equals("VerticalRadioButton")) separatorShape.setOrientation(JSeparator.VERTICAL);
}

/**
 * Returns the separator tool's class.
 */
public Class getShapeClass()  { return JSeparatorShape.class; }

/**
 * Returns the name for the inspector window title bar.
 */
public String getWindowTitle()  { return "Separator Inspector"; }

/**
 * Editor method (returns the number of handles).
 */
public int getHandleCount(T aShape)  { return 2; }

/**
 * Editor method.
 */
public RMPoint getHandlePoint(T aSepShape, int i, boolean iss)
{
    // Handle horizontal separator
    if(i==RMLineShapeTool.HandleStartPoint) {
        if(aSepShape.getOrientation()==JSeparator.HORIZONTAL) i = RMTool.HandleW;
        else i = RMTool.HandleN;
    }
    
    // Handle verticals separator
    else if(i==RMLineShapeTool.HandleEndPoint) { // really only other choice
        if(aSepShape.getOrientation()==JSeparator.HORIZONTAL) i = RMTool.HandleE;
        else i = RMTool.HandleS;
    }
    
    // If the impossible happens, do normal get handle point
    return super.getHandlePoint(aSepShape, i, iss);
}

/**
 * Editor method.
 */
public void moveShapeHandle(T aShape, int i, RMPoint toPoint)
{
    super.moveShapeHandle(aShape, i==RMLineShapeTool.HandleEndPoint? HandleSE : i, toPoint);
}

}