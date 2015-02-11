package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JListShape;

/**
 * Provides UI editing for JListShape.
 */
public class JListTool <T extends JListShape> extends JComponentTool <T> {

/**
 * Updates UI controls for JListTool.
 */
/*public void resetUI()
{
    // Get the currently selected JListShape (just return if null)
    JListShape jlist = getSelectedShape(); if(jlist==null) return;
    
    // Update ItemsStringText, SelectedIndexText
    setNodeValue("ItemsStringText", jlist.getItemsString());
    setNodeValue("SelectedIndexText", jlist.getSelectedIndex());

    // Set the VisibleItemsText, EditableCheckBox
    setNodeValue("VisibleItemsSpinner", jlist.getVisibleRowCount());
    setNodeValue("EditableCheckBox", jlist.getSelectionMode()!=ListSelectionModel.SINGLE_SELECTION);
}*/

/**
 * Responds to UI controls.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected JList shape (just return if null)
    JListShape jlist = getSelectedShape(); if(jlist==null) return;
    
    // Handle ItemsStringText
    if(anEvent.equals("ItemsStringText")) listShape.setItemsString(anEvent.getStringValue());
    
    // Handle SelectedIndexText
    else if(anEvent.equals("SelectedIndexText")) jlist.setSelectedIndex(anEvent.getSelectedIndex());
    
    // Handle VisibleItemsText
    else if(anEvent.equals("VisibleItemsSpinner")) jlist.setVisibleRowCount(anEvent.getIntValue());

    // Handle EditableCheckBox
    else if(anEvent.is("EditableCheckBox")) { JCheckBox checkBox = anEvent.getTarget(JCheckBox.class);
        int mode = checkBox.isSelected()? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
            ListSelectionModel.SINGLE_SELECTION;
        jlist.setSelectionMode(mode);
    }
}*/

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JListShape.class; }

/**
 * Returns the string used in the inspector window title.
 */
public String getWindowTitle()  { return "List Inspector"; }
    
}