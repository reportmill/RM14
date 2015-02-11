package com.reportmill.apptools;
import com.reportmill.shape.RMGraphLegend;
import com.reportmill.shape.RMShape;

/**
 * Provides UI editing for RMGraphLegend.
 */
public class RMGraphLegendTool <T extends RMGraphLegend> extends RMFlowShapeTool <T> {

/**
 * Override to suppress superclass.
 */
protected void initUI()  { }

/**
 * Override to suppress superclass.
 */
public void resetUI()  { }

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Legend Inspector"; }

/**
 * Override to make RMGraphLegend not super-selectable. 
 */
public boolean isSuperSelectable(RMShape aShape)  { return false; }

}