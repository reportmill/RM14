package com.reportmill.apptools;
import com.reportmill.shape.*;
import javax.swing.*;
import snap.swing.SwingEvent;

/**
 * This class is responsible for UI editing of nested document shape.
 */
public class RMNestedDocTool extends RMTool {
    
/**
 * Refreshes Swing UI panel controls form the currently selected nested doc.
 */
public void resetUI()
{    
    RMNestedDoc edoc = (RMNestedDoc)getSelectedShape(); if(edoc==null) return;
    setNodeValue("ScaleFactorText", edoc.getScaleFactor());
    setNodeValue("InitialDelayText", edoc.getDelay());
    setNodeValue("GapDelayText", edoc.getGapDelay());
}

/**
 * Handles changes from Swing UI panel controls to currently selected nested doc.
 */
public void respondUI(SwingEvent anEvent)
{
    RMNestedDoc edoc = (RMNestedDoc)getSelectedShape(); if(edoc==null) return;
    if(anEvent.equals("ScaleFactorText")) edoc.setScaleFactor(anEvent.getFloatValue());
    if(anEvent.equals("InitialDelayText")) edoc.setDelay(anEvent.getFloatValue());
    if(anEvent.equals("GapDelayText")) edoc.setGapDelay(anEvent.getFloatValue());
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMNestedDoc.class; }

/**
 * Returns the name to be presented to the user.
 */
public String getWindowTitle()  { return "Embedded Document Inspector";}

/**
 * Returns the icon used to represent our shape class.
 */
public Icon getIcon()  { return getIcon(RMPageTool.class); }

}