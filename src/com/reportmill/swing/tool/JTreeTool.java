package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JTreeShape;

/**
 * Provides Swing UI editing for JTreeShape (JTree).
 */
public class JTreeTool <T extends JTreeShape> extends JComponentTool <T> {
    
/**
 * Updates the UI controls from the currently selected tree shape.
 */
/*public void resetUI()
{   
    // Get the currently selected tree shape and tree (just return if null)
    JTreeShape tree = getSelectedShape(); if(tree==null) return;
    
    // Update RootVisibleCheckBox, EditableCheckBox, ShowsRootHandlesCheckBox, RootTitleText
    setNodeValue("RootVisibleCheckBox", tree.isRootVisible());
    setNodeValue("EditableCheckBox", tree.isEditable());
    setNodeValue("ShowsRootHandlesCheckBox", tree.getShowsRootHandles());
    setNodeValue("RootTitleText", ((DefaultMutableTreeNode)tree.getModel().getRoot()).getUserObject());
}*/

/**
 * Updates the currently selected tree shape from the UI controls.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected tree shape and tree (just return if null)
    JTreeShape tree = getSelectedShape(); if(tree==null) return;
    
    // Handle RootVisibleCheckBox, EditableCheckBox, ShowsRootHandlesCheckBox, RootTitleText
    if(anEvent.equals("RootVisibleCheckBox")) tree.setRootVisible(anEvent.getBoolValue());
    if(anEvent.equals("EditableCheckBox")) tree.setEditable(anEvent.getBoolValue());
    if(anEvent.equals("ShowsRootHandlesCheckBox")) tree.setShowsRootHandles(anEvent.getBoolValue());
    if(anEvent.equals("RootTitleText"))
        ((DefaultMutableTreeNode)tree.getModel().getRoot()).setUserObject(anEvent.getStringValue());
}*/

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTreeShape.class; }

/**
 * Returns the string used in the inspector window title.
 */
public String getWindowTitle()  { return "Tree Inspector"; }

}