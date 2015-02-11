package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JCheckBoxShape;
import javax.swing.*;

/**
 * Provides an inspector for JCheckBoxShape.
 */
public class JCheckBoxTool extends AbstractButtonTool {
    
/**
 * Override to add JButtonTool UI to LabeledTool UI.
 */
protected JComponent createUI()
{
    JComponent lui = createUI(LabeledTool.class); lui.setMaximumSize(lui.getSize());
    JCheckBox cbox = new JCheckBox("Selected"); cbox.setName("SelectedCheckBox"); cbox.setFont(lui.getFont());
    addNodeBinding(cbox, "Selected", "SelectedShape.Selected");
    Box box = new Box(BoxLayout.Y_AXIS); box.add(lui); box.add(cbox); box.setAlignmentY(0);
    return box;
}

/**
 * Returns the shape that this tool is responsible for.
 */
public Class getShapeClass()  { return JCheckBoxShape.class; }

/**
 * Returns the name used to represent this tool in the inspector window title.
 */
public String getWindowTitle()  { return "CheckBox Inspector"; }

}