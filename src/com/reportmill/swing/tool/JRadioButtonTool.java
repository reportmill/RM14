package com.reportmill.swing.tool;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.JRadioButtonShape;
import java.util.*;
import javax.swing.*;
import snap.swing.*;

/**
 * An inner class to act as RJRadioButtonShape's Tool.
 */
public class JRadioButtonTool extends AbstractButtonTool {
    
/**
 * Override to add JButtonTool UI to LabeledTool UI.
 */
protected JComponent createUI()
{
    JComponent labeledUI = createUI(LabeledTool.class);
    JComponent radioUI = createUI(JRadioButtonTool.class);
    Box box = new Box(BoxLayout.Y_AXIS); box.add(labeledUI); box.add(radioUI);
    return box;
}

/**
 * Updates the UI from the currently selected radio button shape.
 */
public void resetUI()
{
    // Get the currently selected radio button shape (and return if null)
    JRadioButtonShape bshape = (JRadioButtonShape)getSelectedShape(); if(bshape==null) return;
    
    // It makes no sense to set "selected" for multiple radio buttons
    setNodeEnabled("SelectedCheckBox", getSelectedShapes().size()==1);
    
    // Add all named buttongroups to the combobox and set the selected value
    JComboBox combobox = getNode("ButtonGroupNameComboBox", JComboBox.class);
    combobox.removeAllItems();
    
    // Get button parent
    /*JRadioButton button = bshape.getRadioButton();
    Object parent = button.getParent();
    if(parent instanceof SwingPanel) { SwingPanel panel = (SwingPanel)parent; // always?
        Map allGroups = panel.getButtonGroups();
        if(allGroups != null) {
            Object groupNames[] = allGroups.keySet().toArray();
            for(int i=0, n=groupNames.length; i<n; ++i)
                combobox.addItem(groupNames[i]);
        }
    }*/
  
    // Update ButtonGroupNameComboBox, SelectedCheckBox
    setNodeValue("ButtonGroupNameComboBox", bshape.getButtonGroupName());
    setNodeValue("SelectedCheckBox", bshape.isSelected());
}

/**
 * Updates the currently selected radio button shape from the UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected button shapes (and return if null)
    List <? extends RMShape> shapes = getSelectedShapes(); if(shapes==null || shapes.isEmpty()) return;
    
    // Apply changes to each selected RadioButton        
    for(RMShape shape : shapes) {
        
        // If shape not radio button, continue
        if(!(shape instanceof JRadioButtonShape)) continue;
            
        // Get radio button shape and radio button and button helper
        JRadioButtonShape bshape = (JRadioButtonShape)shape;
    
        // Handle ButtonGroupNameComboBox, SelectedCheckBox
        if(anEvent.equals("ButtonGroupNameComboBox")) bshape.setButtonGroupName(anEvent.getStringValue());
        if(anEvent.equals("SelectedCheckBox")) bshape.setSelected(anEvent.getBoolValue());
    }
}

/**
 * Returns the class that this inspector is responsible for.
 */
public Class getShapeClass()  { return JRadioButtonShape.class; }

/**
 * Returns the name to be used in the inspector window title.
 */
public String getWindowTitle()  { return "Radio Button Inspector"; }

}