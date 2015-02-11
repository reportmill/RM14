package com.reportmill.apptools;
import com.reportmill.base.RMClassUtils;
import com.reportmill.shape.*;
import snap.swing.*;

/**
 * Provides UI inspection for GraphPartBars.
 */
public class RMGraphPartPieTool extends RMTool {

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure ExtrusionComboBox
    setNodeItems("ExtrusionComboBox", RMGraphPartPie.EXTRUSIONS);
}

/**
 * Resets the UI panel controls.
 */
public void resetUI()
{
    // Get the selected part pie (just return if null)
    RMGraphPartPie pie = getSelectedShape(); if(pie==null) return;

    // DrawWedgeLabelLinesCheckBox
    setNodeValue("DrawWedgeLabelLinesCheckBox", pie.getDrawWedgeLabelLines());
}

/**
 * Responds to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the selected part pie (just return if null)
    RMGraphPartPie pie = getSelectedShape(); if(pie==null) return;

    // Register graph for redisplay - shouldn't need this
    getSelectedGraph().repaint(); getSelectedGraph().relayout();

    // Handle DrawWedgeLabelLinesCheckBox
    if(anEvent.equals("DrawWedgeLabelLinesCheckBox"))
        pie.setDrawWedgeLabelLines(anEvent.getBoolValue());

    // Handle ExtrusionComboBox
    if(anEvent.equals("ExtrusionComboBox")) {
        pie.setExtrusionKey(anEvent.getStringValue());
        if(pie.getExtrusionKey().equals(RMGraphPartPie.EXTRUDE_CUSTOM)) {
            DialogBox dbox = new DialogBox("Custom Extrusion Key Panel"); dbox.setQuestionMessage("Extrusion Key:");
            String key = dbox.showInputDialog(getUI(), pie.getExtrusionKey());
            if(key!=null)
                pie.setExtrusionKey(key);
        }
    }
}

/**
 * Returns the currently selected RMGraphPartPie.
 */
public RMGraphPartPie getSelectedShape()
{
    RMGraph graph = getSelectedGraph();
    return graph!=null? graph.getPie() : null;
}

/**
 * Returns the currently selected graph shape.
 */
public RMGraph getSelectedGraph()
{
    return RMClassUtils.getInstance(super.getSelectedShape(), RMGraph.class);
}

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Pie Wedge Inspector"; }

}