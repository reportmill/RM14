package com.reportmill.swing.tool;
import com.reportmill.swing.shape.*;
import com.reportmill.app.RMEditor;
import javax.swing.*;
import snap.swing.SwingEvent;

/**
 * This class provides Ribs editing of JScrollPaneShape.
 */
public class JScrollPaneTool extends JComponentTool {
    
/**
 * Updates the UI controls from the currently selected scroll pane shape.
 */
public void resetUI()
{
    // Get the currently selected scroll pane shape (just return if null)
    JScrollPaneShape spane = (JScrollPaneShape)getSelectedShape(); if(spane==null) return;
    
    // Get horizontal scrollbar policy
    int hpolicy = spane.getHorizontalScrollBarPolicy();
    
    // Update HAlwaysRadioButton, HNeverRadioButton, HAsNeededRadioButton
    setNodeValue("HAlwaysRadioButton", hpolicy==ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    setNodeValue("HNeverRadioButton", hpolicy==ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    setNodeValue("HAsNeededRadioButton", hpolicy==ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    // Get vertical scrollbar policy
    int vpolicy = spane.getVerticalScrollBarPolicy();
    
    // Handle VAlwaysRadioButton, VNeverRadioButton, VAsNeededRadioButton
    setNodeValue("VAlwaysRadioButton", vpolicy==ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    setNodeValue("VNeverRadioButton", vpolicy==ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    setNodeValue("VAsNeededRadioButton", vpolicy==ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // Update SelectViewButton text
    //Component view = spane.getViewport().getView();
    //String buttonText = "Select " + (view==null? "Viewport View" : RMClassUtils.getClassSimpleName(view)); 
    //setNodeText("SelectViewButton", buttonText);
}

/**
 * Updates the currently selected scroll pane shape from the UI controls.
 */ 
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected scroll pane shape (just return if null)
    JScrollPaneShape spane = (JScrollPaneShape)getSelectedShape(); if(spane==null) return;

    // Handle HAlwaysRadioButton, HNeverRadioButton, HAsNeededRadioButton
    if(anEvent.equals("HAlwaysRadioButton"))
        spane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    if(anEvent.equals("HNeverRadioButton"))
        spane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    if(anEvent.equals("HAsNeededRadioButton"))
        spane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Handle VAlwaysRadioButton, VNeverRadioButton, VAsNeededRadioButton
    if(anEvent.equals("VAlwaysRadioButton"))
        spane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    if(anEvent.equals("VNeverRadioButton"))
        spane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    if(anEvent.equals("VAsNeededRadioButton"))
        spane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // Handle SelectViewportButton
    if(anEvent.equals("SelectViewButton") && spane.getChildCount()>0)
        RMEditor.getMainEditor().setSuperSelectedShape(spane.getChild(0));
}

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JScrollPaneShape.class; }

/**
 * Returns the string used in the inspector window title.
 */
public String getWindowTitle()  { return "ScrollPane Inspector"; }

}