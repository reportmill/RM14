package com.reportmill.swing.tool;
import com.reportmill.app.*;
import com.reportmill.apptools.RMTool;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.JTableColumnShape;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.geom.Point2D;
import snap.swing.*;

/**
 * This class provides an inspector for JTableColumnShape.
 */
public class JTableColumnTool <T extends JTableColumnShape> extends JComponentTool <T> {

/**
 * Updates the UI panel from selected table column shape.
 */
public void resetUI()
{
    // Get currently selected table column shape and column (and return if null)
    JTableColumnShape cshape = getSelectedShape(); if(cshape==null) return;
    
    // Update HeaderText
    setNodeValue("HeaderText", cshape.getHeaderValue());
    
    // Update ResizableCheckBox
    setNodeValue("ResizableCheckBox", cshape.isResizable());
}

/**
 * Updates the current selection from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected table column shape and column (and return if null)
    JTableColumnShape cshape = getSelectedShape(); if(cshape==null) return;
    
    // Handle HeaderText
    if(anEvent.equals("HeaderText"))
        cshape.setHeaderValue(anEvent.getStringValue());
    
    // Handle ResizableCheckBox
    if(anEvent.equals("ResizableCheckBox"))
        cshape.setResizable(anEvent.getBoolValue());
}

/**
 * Overrides Tool implementation to accept KeysPanel drags.
 */
public boolean acceptsDrag(T aShape, DropTargetDragEvent anEvent)
{
    // If KeysPanel is dragging, return true
    if(KeysPanel.getDragKey()!=null) {

        // Convert event point to shape coords
        Point2D point = getEditor().convertPointToShape(anEvent.getLocation(), aShape);
        
        // If shape is greater than 100 wide, inset by 20
        if(aShape.getWidth()>100)
            return point.getX()>20 && point.getX()<aShape.getWidth()-20;
            
        // If shape is greater than 50, inset by 10
        if(aShape.getWidth()>50)
            return point.getX()>10 && point.getX()<aShape.getWidth()-10;
            
        // If shape is greater than 25, inset by 5
        if(aShape.getWidth()>25)
            return point.getX()>5 && point.getX()<aShape.getWidth()-5;        
    }
    
    // Otherwise, return normal
    return super.acceptsDrag(aShape, anEvent);
}

/**
 * Returns rect for drag display bounds.
 */
public RMRect getDragDisplayBounds(RMShape aShape, DropTargetDragEvent anEvent)
{
    // Convert event point to shape coords
    Point2D point = getEditor().convertPointToShape(anEvent.getLocation(), aShape);
    
    // If shape is greater than 100 wide, inset by 20
    if(aShape.getWidth()>100 && point.getX()>20 && point.getX()<aShape.getWidth()-20)
        return aShape.getBoundsInside().inset(20);
        
    // If shape is greater than 50, inset by 10
    if(aShape.getWidth()>50 && point.getX()>10 && point.getX()<aShape.getWidth()-10)
        return aShape.getBoundsInside().inset(10);
        
    // If shape is greater than 25, inset by 5
    if(aShape.getWidth()>25 && point.getX()>5 && point.getX()<aShape.getWidth()-5)
        return aShape.getBoundsInside().inset(5);
    
    // Get shape bounds inset by 20
    return aShape.getBoundsInside();
}

/**
 * Override normal implementation to handle KeysPanel drop.
 */
public void drop(T aShape, DropTargetDropEvent anEvent)
{
    // If not a keys panel drop, do normal implementation
    if(KeysPanel.getDragKey()==null || KeysPanel.isSelectedToMany()) {
        super.drop(aShape, anEvent);
        return;
    }
    
    // Delete @-signs from string
    String string = RMStringUtils.delete(ClipboardUtils.getString(anEvent.getTransferable()), "@");
    
    // Get table column shape
    JTableColumnShape columnShape = aShape;
    
    // Get header by removing "get" from key
    String header = RMStringUtils.delete(string, "get");
    
    // Set table column header
    columnShape.setHeaderValue(header);
    
    // Set bind key as table column identifier
    columnShape.setItemDisplayKey(string);
}

/**
 * Returns the string to be used in the inspector window title.
 */
public String getWindowTitle()  { return "Table Column Inspector"; }

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTableColumnShape.class; }

/**
 * Highlights the selected cells or dividers.
 */
public void paintShapeHandles(T aShape, Graphics2D g, boolean isSuperSelected)
{
    g.setStroke(new BasicStroke(3)); g.setColor(new Color(253, 219, 19));
    RMRect bounds = getEditor().convertRectFromShape(aShape.getBoundsInside(), aShape);
    g.draw(bounds);
}

/**
 * Returns the rect for the handle at the given index in editor coords.
 */
public RMRect getHandleRect(T aShape, int aHandle, boolean isSuperSelected)
{
    // If handle isn't left/right middle handle, return bogus rect
    if(aHandle!=RMTool.HandleW && aHandle!=RMTool.HandleE)
        return new RMRect();
    
    // Get handle point for given handle index in shape coords
    RMPoint hp = getHandlePoint(aShape, aHandle, isSuperSelected);
    
    // Get handle rect in shape coords
    RMRect hr = new RMRect(hp.x-RMTool.HandleWidth/2, 0, RMTool.HandleWidth, aShape.getHeight());
    
    // Return handle rect in editor coords
    return getEditor().convertRectFromShape(hr, aShape);
}

}