package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.shape.*;

/**
 * Provides UI editing for graph part value axis
 */
public class RMGraphPartValueAxisTool <T extends RMGraphPartValueAxis> extends RMTool <T> {

/**
 * Resets UI panel controls.
 */
public void resetUI()
{
    // Get the selected value axis (just return if null)
    RMGraphPartValueAxis valueAxis = getSelectedShape(); if(valueAxis==null) return;
    
    // Update ShowLabelsCheckBox, ShowMajorGridCheckBox, ShowMinorGridCheckBox, LabelRollSpinner
    setNodeValue("ShowLabelsCheckBox", valueAxis.getShowAxisLabels());
    setNodeValue("ShowMajorGridCheckBox", valueAxis.getShowMajorGrid());
    setNodeValue("ShowMinorGridCheckBox", valueAxis.getShowMinorGrid() && valueAxis.getShowMajorGrid());
    setNodeEnabled("ShowMinorGridCheckBox", valueAxis.getShowMajorGrid());
    setNodeValue("LabelRollSpinner", valueAxis.getRoll());
    
    // Update AxisMinText, AxisMaxText, AxisCountSpinner
    if(valueAxis.getAxisMin()==Float.MIN_VALUE) setNodeValue("AxisMinText", "");
    else setNodeValue("AxisMinText", valueAxis.getAxisMin());
    if(valueAxis.getAxisMax()==Float.MIN_VALUE) setNodeValue("AxisMaxText", "");
    else setNodeValue("AxisMaxText", valueAxis.getAxisMax());
    setNodeValue("AxisCountSpinner", valueAxis.getAxisCount());
}

/**
 * Responds to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the selected value axis (just return if null)
    RMGraphPartValueAxis valueAxis = getSelectedShape(); if(valueAxis==null) return;
    
    // Handle ShowLabelsCheckBox, ShowMajorGridCheckBox, ShowMinorGridCheckBox, LabelRollSpinner
    if(anEvent.equals("ShowLabelsCheckBox")) valueAxis.setShowAxisLabels(anEvent.getBoolValue());
    if(anEvent.equals("ShowMajorGridCheckBox")) valueAxis.setShowMajorGrid(anEvent.getBoolValue());
    if(anEvent.equals("ShowMinorGridCheckBox")) valueAxis.setShowMinorGrid(anEvent.getBoolValue());
    if(anEvent.equals("LabelRollSpinner")) valueAxis.setRoll(anEvent.getFloatValue());
    
    // Handle AxisMinText, AxisMaxText, AxisCountSpinner
    if(anEvent.equals("AxisMinText"))
        valueAxis.setAxisMin(anEvent.getStringValue().length()>0? anEvent.getFloatValue() : Float.MIN_VALUE);
    if(anEvent.equals("AxisMaxText"))
        valueAxis.setAxisMax(anEvent.getStringValue().length()>0? anEvent.getFloatValue() : Float.MIN_VALUE);
    if(anEvent.equals("AxisCountSpinner"))
        valueAxis.setAxisCount(anEvent.getIntValue());
    
    // Rebuild Graph
    RMGraph graph = (RMGraph)valueAxis.getParent();
    graph.relayout(); graph.repaint();
}

/**
 * Override to return tool shape class.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMGraphPartValueAxis.class; }

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Value Axis Inspector"; }

/**
 * Override to remove handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}