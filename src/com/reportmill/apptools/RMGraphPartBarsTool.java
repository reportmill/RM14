package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.base.RMClassUtils;
import com.reportmill.shape.*;

/**
 * Provides UI inspection for GraphPartBars.
 */
public class RMGraphPartBarsTool extends RMTool {

/**
 * Resets UI panel controls.
 */
public void resetUI()
{
    // Get the selected value axis
    RMGraphPartBars bars = getSelectedShape(); if(bars==null) return;
    
    // Update BarGapSpinner, SetGapSpinner, BarCountSpinner
    setNodeValue("BarGapSpinner", bars.getBarGap());
    setNodeValue("SetGapSpinner", bars.getSetGap());  
    setNodeValue("BarCountSpinner", bars.getBarCount());
}

/**
 * Responds to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the selected value axis
    RMGraphPartBars bars = getSelectedShape(); if(bars==null) return;
    
    // Register GraphArea for repaint/revalidate (shouldn't need this)
    getSelectedGraph().repaint(); getSelectedGraph().relayout();
    
    // Handle BarGapSpinner, SetGapSpinner, BarCountSpinner
    if(anEvent.equals("BarGapSpinner")) bars.setBarGap(anEvent.getFloatValue());
    if(anEvent.equals("SetGapSpinner")) bars.setSetGap(anEvent.getFloatValue());
    if(anEvent.equals("BarCountSpinner")) bars.setBarCount(anEvent.getIntValue());
}

/**
 * Returns the currently selected RMGraphPartBars.
 */
public RMGraphPartBars getSelectedShape()
{
    RMGraph graph = getSelectedGraph();
    return graph!=null? graph.getBars() : null;
}

/**
 * Returns the currently selected graph area shape.
 */
public RMGraph getSelectedGraph()
{
    return RMClassUtils.getInstance(super.getSelectedShape(), RMGraph.class);
}

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Bars Inspector"; }

}