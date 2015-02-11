package com.reportmill.swing.tool;
import com.reportmill.apptools.RMParentShapeTool;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import java.awt.event.*;
import javax.swing.JComponent;
import snap.swing.*;

/**
 * The base tool class for ribs tool.
 */
public class JComponentTool <T extends JComponentShape> extends RMParentShapeTool <T> {

    // The inspector for paint/fill shape attributes
    static SwingFills  _swingFills = new SwingFills();
    
    // Returns whether tool should show standard fills
    static boolean     _showStandardFills = false;

/**
 * Returns the number of handles for this shape.
 */
public int getHandleCount(T aShape)
{
    // Get shape parent
    RMShape parent = aShape.getParent();
    
    // If parent is scrollpane or tab pane, return zero
    if(parent instanceof JScrollPaneShape || parent instanceof JTabbedPaneShape)
        return 0;
    
    // Otherwise, return normal count
    return super.getHandleCount(aShape);
}

/**
 * Returns the fill inspector for this tool's shape class.
 */
public SwingOwner getShapeFillInspector()  { return _showStandardFills? super.getShapeFillInspector() : _swingFills; }

/**
 * Sets whether tool should show standard fills.
 */
public void setShowStandardFills(boolean aValue)
{
    // Set value
    _showStandardFills = aValue;
    
    // If true, add listener to normal fill inspector, to turn this off when it get's removed
    if(_showStandardFills) {
        
        // Get normal fill inspector
        JComponent panel = super.getShapeFillInspector().getUI();
        
        // Add Heirarchy changed listener
        panel.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if(e.getComponent().getParent()==null) {
                    _showStandardFills = false;
                    e.getComponent().removeHierarchyListener(this);
                }
            }
        });
    }
}

}