package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JLabelShape;
import javax.swing.*;
import snap.swing.SwingEvent;

/**
 * This class provides a UI inspector for JLabelShape.
 */
public class JLabelTool <T extends JLabelShape> extends LabeledTool <T> {
    
/**
 * Override to add JLabelTool UI to LabeledTool UI.
 */
protected JComponent createUI()
{
    JComponent labeledUI = createUI(LabeledTool.class);
    JComponent labelUI = createUI(JLabelTool.class);
    Box box = new Box(BoxLayout.Y_AXIS); box.add(labeledUI); box.add(labelUI);
    return box;
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure border list
    JList list = getNode("BorderList", JList.class);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setCellRenderer(new SpringsPaneTool.BorderListCellRenderer());
    list.setVisibleRowCount(-1);
    list.setListData(SpringsPaneTool._borders);
}

/**
 * Updates the UI from the currently selected panel shape.
 */
public void resetUI()
{
    // Do normal resetUI
    super.resetUI();
    
    // Get the currently selected label shape and label (just return if null)
    JLabelShape lshape = getSelectedShape(); if(lshape==null) return;

    // Update BorderList
    /*Border border = lshape.getBorder();
    if(RMBorderUtils.isEmpty(border)) setNodeSelectedIndex("BorderList", 0);
    else setNodeValue("BorderList", RMBorderUtils.getBorder(border));*/
}

/**
 * Updates the currently selected panel shape from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Do normal respond UI
    super.respondUI(anEvent);
    
    // Get the currently selected label (just return if null)
    JLabelShape lshape = getSelectedShape(); if(lshape==null) return;
    
    // Handle BorderList
    //if(anEvent.equals("BorderList")) lshape.setBorder((Border)anEvent.getValue());
}

/**
 * Returns the label tool's class.
 */
public Class getShapeClass()  { return JLabelShape.class; }

/**
 * Returns the name for the inspector window title bar.
 */
public String getWindowTitle()  { return "Label Inspector"; }

}