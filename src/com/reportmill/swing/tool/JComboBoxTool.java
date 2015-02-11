package com.reportmill.swing.tool;
import snap.swing.SwingEvent;
import com.reportmill.swing.shape.JComboBoxShape;

/**
 * A class to act as JComboBoxShape's Tool
 */
public class JComboBoxTool <T extends JComboBoxShape> extends JComponentTool <T> {
    
/**
 * Updates the UI from the currently selected combo box shape.
 */
public void resetUI()
{
    // Get the currently selected combo box shape (just return if null)
    JComboBoxShape cbox = getSelectedShape(); if(cbox==null) return;

    // Update ItemsStringText, SelectedIndexText
    setNodeValue("ItemsStringText", cbox.getItemsString());
    //setNodeValue("SelectedIndexText", cbox.getSelectedIndex());

    // Set the VisibleItemsText, EditableCheckBox
    //setNodeValue("VisibleItemsSpinner", cbox.getMaximumRowCount());
    //setNodeValue("EditableCheckBox", cbox.isEditable());
}

/**
 * Updates the currently selected combo box shape from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected ComboBoxShape (just return if null)
    JComboBoxShape cbox = getSelectedShape(); if(cbox==null) return;
    
    // Handle ItemsStringText
    if(anEvent.equals("ItemsStringText")) cbox.setItemsString(anEvent.getStringValue());
    
    // Handle SelectedIndexText
    //else if(anEvent.equals("SelectedIndexText")) cbox.setSelectedIndex(anEvent.getSelectedIndex());
    
    // Handle VisibleItemsText
    //else if(anEvent.equals("VisibleItemsSpinner")) cbox.setMaximumRowCount(anEvent.getIntValue());
  
    // Handle EditableCheckBox: Set editable then resize to minimum height, since it can change with Editable
    /*else if(anEvent.equals("EditableCheckBox")) {
        cbox.setEditable(anEvent.getBooleanValue());
        cbox.setHeight(comboBox.getUI().getMinimumSize(comboBox).height);
    }*/
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JComboBoxShape.class; }

/**
 * Returns the string used in the inspector window title.
 */
public String getWindowTitle()  { return "ComboBox Inspector"; }

}