package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.event.MouseEvent;
import snap.swing.SwingEvent;
import snap.web.Entity;

/**
 * Provides UI inspector for crosstab frame.
 */
public class RMCrossTabFrameTool <T extends RMCrossTabFrame> extends RMTool <T> {

/**
 * Updates UI controls from the currently selected crosstab frame.
 */
public void resetUI()
{
    // Get the currently selected crosstab frame and table (just return if null)
    RMCrossTabFrame tableFrame = getSelectedShape(); if(tableFrame==null) return;
    RMCrossTab table = tableFrame.getTable();
    
    // Update the DatasetKeyText, FilterKeyText, ReprintHeaderRowsCheckBox
    setNodeValue("DatasetKeyText", table.getDatasetKey());
    setNodeValue("FilterKeyText", table.getFilterKey());
    setNodeValue("ReprintHeaderRowsCheckBox", tableFrame.getReprintHeaderRows());
}

/**
 * Updates the currently selected crosstab from from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected crosstab frame and table (just return if null)
    RMCrossTabFrame tableFrame = getSelectedShape(); if(tableFrame==null) return;
    RMCrossTab table = tableFrame.getTable();
    
    // Handle DatasetKeyText, FilterKeyText, ReprintHeaderRowsCheckBox, AutoFormatButton
    if(anEvent.equals("DatasetKeyText")) table.setDatasetKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
    if(anEvent.equals("FilterKeyText")) table.setFilterKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
    if(anEvent.equals("ReprintHeaderRowsCheckBox")) tableFrame.setReprintHeaderRows(anEvent.getBoolValue());
    if(anEvent.equals("AutoFormatButton")) new RMCrossTabAutoFormatPanel().showPanel(getEditor(), table);
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aCTabFrame, MouseEvent anEvent)
{
    // Get event point in TableFrame coords 
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCTabFrame);
    
    // Handle mouse press in crosstab when not superselected
    if(getEditor().isSelected(aCTabFrame)) {
        
        // If click was inside table bounds, super select table and consume event
        if(point.getX()<aCTabFrame.getTable().getWidth() && point.getY()<aCTabFrame.getTable().getHeight()) {
            getEditor().setSuperSelectedShape(aCTabFrame.getTable());
            anEvent.consume();
        }
    }
    
    // Handle mouse press in super selected crosstab's buffer region
    if(getEditor().isSuperSelected(aCTabFrame)) {
        
        // If click was outside table bounds, make table frame just selected
        if(point.getX()>=aCTabFrame.getTable().getWidth() || point.getY()>=aCTabFrame.getTable().getHeight()) {
            getEditor().setSelectedShape(aCTabFrame);
            RMTool.getSelectTool().setRedoMousePressed(true); // Register for redo
        }
    }
}

/**
 * Returns the shape class this tool edits (RMTable).
 */
public Class getShapeClass()  { return RMCrossTabFrame.class; }

/**
 * Returns the display name for this tool ("Table Inspector").
 */
public String getWindowTitle()  { return "CrossTab Frame Inspector"; }

/**
 * Overridden to make crosstab frame super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overrides default implementation to get entity from table.
 */
public Entity getDatasetEntity(RMShape aShape)
{
    RMCrossTabFrame tframe = (RMCrossTabFrame)aShape; // Get crosstab frame
    return tframe.getTable().getDatasetEntity(); // Return entity of crosstab frame's table
}

}