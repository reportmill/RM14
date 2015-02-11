package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.shape.*;

/**
 * Provides UI editing for graph part label axis.
 */
public class RMGraphPartLabelAxisTool <T extends RMGraphPartLabelAxis> extends RMTool <T> {

/**
 * Resets UI controls.
 */
public void resetUI()
{
    // Get the selected label axis
    RMGraphPartLabelAxis labelAxis = getSelectedShape(); if(labelAxis==null) return;
    
    // Update ShowLabelsCheckBox, ShowGridLinesCheckBox, ItemKeyText, LabelRollSpinner
    setNodeValue("ShowLabelsCheckBox", labelAxis.getShowAxisLabels());
    setNodeValue("ShowGridLinesCheckBox", labelAxis.getShowGridLines());
    setNodeValue("ItemKeyText", labelAxis.getItemKey());
    setNodeValue("LabelRollSpinner", labelAxis.getRoll());
}

/**
 * Responds to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the selected label axis
    RMGraphPartLabelAxis labelAxis = getSelectedShape(); //graphArea.getLabelAxis();
    
    // Handle ShowLabelsCheckBox, ShowGridLinesCheckBox, ItemKeyText, LabelRollSpinner
    if(anEvent.equals("ShowLabelsCheckBox")) labelAxis.setShowAxisLabels(anEvent.getBoolValue());
    if(anEvent.equals("ShowGridLinesCheckBox")) labelAxis.setShowGridLines(anEvent.getBoolValue());
    if(anEvent.equals("ItemKeyText")) labelAxis.setItemKey(anEvent.getStringValue());
    if(anEvent.equals("LabelRollSpinner")) labelAxis.setRoll(anEvent.getFloatValue());

    // Rebuild Graph
    RMGraph graph = (RMGraph)labelAxis.getParent();
    graph.relayout(); graph.repaint();
}

/**
 * Override to return tool shape class.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMGraphPartLabelAxis.class; }

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Label Axis Inspector"; }

/**
 * Override to remove handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}