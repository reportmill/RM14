package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JToggleButtonShape;
import javax.swing.*;
import snap.swing.*;

/**
 * Provides Swing UI editing for JToggleButtonShape.
 */
public class JToggleButtonTool <T extends JToggleButtonShape> extends AbstractButtonTool <T> {
    
/**
 * Override to add JButtonTool UI to LabeledTool UI.
 */
protected JComponent createUI()
{
    JComponent labeledUI = createUI(LabeledTool.class);
    JComponent toggleUI = createUI(JToggleButtonTool.class);
    JTabbedPane tpane = new JTabbedPane(); tpane.setFont(labeledUI.getFont());
    tpane.addTab("Label", labeledUI); tpane.addTab("Toggle", toggleUI);
    return tpane;
}

/**
 * Updates the UI from the currently selected toggle button shape.
 */
public void resetUI()
{    
    // Let the superclass handle all the button-specific ui
    super.resetUI();

    // Get currently selected button shape, button and helper (and return if null)
    JToggleButtonShape bshape = getSelectedShape(); if(bshape==null) return;
    
    // Set SelectedCheckBox, IsBorderPaintedCheckBox, IsContentAreaFilledCheckBox, MarginText
    setNodeValue("SelectedCheckBox", bshape.isSelected());
    setNodeValue("IsBorderPaintedCheckBox", bshape.isBorderPainted());
    setNodeValue("IsContentAreaFilledCheckBox", bshape.isContentAreaFilled());
    setNodeValue("MarginText", bshape.getMarginString());
        
    // Set the SelectedImageNameText
    setNodeValue("SelectedImageNameText", bshape.getSelectedImageName());
    
    // Add all named buttongroups to the combobox and set the selected value
    /*JComboBox cbox = getNode("ButtonGroupNameComboBox", JComboBox.class);
    cbox.removeAllItems();

    // Get parent
    JToggleButton button = bshape.getComponent();
    Object parent = button.getParent();
    if(parent instanceof SwingPanel) { SwingPanel panel = (SwingPanel)parent; // Always?
        Map allGroups = panel.getButtonGroups();
        if(allGroups != null) {
            Object groupNames[] = allGroups.keySet().toArray();
            for(int i=0, n=groupNames.length; i<n; ++i)
                cbox.addItem(groupNames[i]);
        }
    }*/
  
    // Select the right one from the list
    setNodeValue("ButtonGroupNameComboBox", bshape.getButtonGroupName());
}

/**
 * Updates the currently selected toggle button shape from the UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Let the superclass handle all the button-specific ui
    super.respondUI(anEvent);
    
    // Get currently selected button shape, button and helper (and return if null)
    JToggleButtonShape bshape = getSelectedShape(); if(bshape==null) return;
            
    // Handle SelectedCheckBox, IsBorderPaintedCheckBox, IsContentAreaFilledCheckBox, MarginText
    if(anEvent.equals("SelectedCheckBox")) bshape.setSelected(anEvent.getBoolValue());
    if(anEvent.equals("IsBorderPaintedCheckBox")) bshape.setBorderPainted(anEvent.getBoolValue());
    if(anEvent.equals("IsContentAreaFilledCheckBox")) bshape.setContentAreaFilled(anEvent.getBoolValue());
    if(anEvent.equals("MarginText")) bshape.setMarginString(anEvent.getStringValue());
        
    // Handle SelectedImageNameText, ButtonGroupNameComboBox
    if(anEvent.equals("SelectedImageNameText")) bshape.setSelectedImageName(anEvent.getStringValue());
    if(anEvent.equals("ButtonGroupNameComboBox")) bshape.setButtonGroupName(anEvent.getStringValue());
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass() { return JToggleButtonShape.class; }

/**
 * Returns the name for the inspector window title.
 */
public String getWindowTitle() { return "Toggle Button Inspector"; }

}