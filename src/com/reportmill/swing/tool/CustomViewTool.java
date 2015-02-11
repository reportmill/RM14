package com.reportmill.swing.tool;
import snap.swing.SwingEvent;
import com.reportmill.swing.shape.CustomViewShape;

/**
 * Provides RMTool implementation for CustomViewShape.
 */
public class CustomViewTool <T extends CustomViewShape> extends JComponentTool <T> {
    
/**
 * Updates the UI controls from the currently selected custom view shape.
 */
public void resetUI()
{
    CustomViewShape cvs = getSelectedShape(); if(cvs==null) return;
    setNodeValue("ClassNameText", cvs.getRealClassName());
}

/**
 * Updates the currently selected custom view shape from the Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    CustomViewShape cvs = getSelectedShape(); if(cvs==null) return;
    if(anEvent.equals("ClassNameText")) cvs.setRealClassName(anEvent.getStringValue());
}

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Custom View Inspector"; }

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return CustomViewShape.class; }

}