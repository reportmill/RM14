package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.shape.RMGraphPartSeries.LabelPos;
import java.awt.Component;
import javax.swing.*;
import snap.swing.SwingEvent;

/**
 * Provides UI editing for graph part series.
 */
public class RMGraphPartSeriesTool <T extends RMGraphPartSeries> extends RMTool <T> {

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set LabelPositionsList Model and CellRenderer
    setNodeItems("LabelPositionsList", RMGraphPartSeries.LabelPos.values());
    getNode("LabelPositionsList", JList.class).setCellRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object val, int indx, boolean isSel, boolean hasFoc) {
            super.getListCellRendererComponent(list, val, indx, isSel, hasFoc);
            RMGraphPartSeries series = getSelectedShape(); if(series==null) return this;
            boolean active = series.getLabelShape((LabelPos)val).length()>0;
            setFont(active? RMAWTUtils.ArialBold11 : RMAWTUtils.Arial11);
            return this;
        }
    });
}

/**
 * Resets UI panel controls.
 */
public void resetUI()
{
    // Get the selected series shape
    RMGraphPartSeries series = getSelectedShape(); if(series==null) return;
    
    // Update TitleText, SeriesText, LabelRollSpinner
    setNodeValue("TitleText", series.getTitle());
    setNodeValue("SeriesText", series.getLabelShape(series.getPosition()).getText());
    setNodeValue("LabelRollSpinner", series.getRoll());    
}

/**
 * Responds to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the selected series shape
    RMGraphPartSeries series = getSelectedShape(); if(series==null) return;
    
    // Handle TitleText, SeriesText, LabelRollSpinner
    if(anEvent.equals("TitleText")) series.setTitle(anEvent.getStringValue());
    if(anEvent.equals("SeriesText")) series.getLabelShape(series.getPosition()).setText(anEvent.getStringValue());
    if(anEvent.equals("LabelRollSpinner")) series.setRoll(anEvent.getFloatValue());
    
    // Rebuild Graph
    RMGraph graph = (RMGraph)series.getParent();
    graph.relayout(); graph.repaint();
}

/**
 * Override to return tool shape class.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMGraphPartSeries.class; }

/**
 * Returns the name of the graph inspector.
 */
public String getWindowTitle()  { return "Graph Series Inspector"; }

/**
 * Override to remove handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}