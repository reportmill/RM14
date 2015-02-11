package com.reportmill.app;
import com.reportmill.shape.*;
import snap.swing.SwingOwner;

/**
 * This class provides UI editing for the currently selected shapes layout attributes (in parent).
 */
public class ShapeLayout extends SwingOwner {

/** Returns the name to be used in the inspector's window title. */
public String getWindowTitle()  { return "Shape Layout Inspector"; }

/**
 * Updates UI controls from currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape and it's parent
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    RMParentShape parent = shape.getParent();
    
    // Get layout pane
    SwingOwner layoutPane = parent instanceof RMFlowShape? getFlowLayoutPane() : getSpringsLayoutPane();
    
    // Set LayoutUI
    setNodeChildren("SwitchPane", layoutPane.getUI());
    
    // ResetUI
    layoutPane.resetLater();
}

/**
 * Returns the SpringsLayoutPane.
 */
public SpringsLayoutPane getSpringsLayoutPane()  { return _slp!=null? _slp : (_slp=new SpringsLayoutPane()); }
SpringsLayoutPane _slp;

/**
 * Returns the FlowLayoutPane.
 */
public FlowLayoutPane getFlowLayoutPane()  { return _flp!=null? _flp : (_flp=new FlowLayoutPane()); }
FlowLayoutPane _flp;

}