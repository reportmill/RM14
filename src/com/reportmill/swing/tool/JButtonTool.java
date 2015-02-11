package com.reportmill.swing.tool;
import com.reportmill.swing.shape.AbstractButtonShape;
import javax.swing.*;
import snap.swing.*;

/**
 * An inner class to act as JButtonShape's Tool
 */
public class JButtonTool <T extends AbstractButtonShape> extends AbstractButtonTool <T> {
    
/**
 * Override to add JButtonTool UI to LabeledTool UI.
 */
protected JComponent createUI()
{
    JComponent labeledUI = createUI(LabeledTool.class);
    JComponent buttonUI = createUI(JButtonTool.class);
    Box box = new Box(BoxLayout.Y_AXIS); box.add(labeledUI); box.add(buttonUI);
    return box;
}

/**
 * Updates the UI panel controls from the currently selected button shape.
 */
public void resetUI()
{
    // Do normal version
    super.resetUI();
    
    // Get currently selected button shape and button (and return if null)
    AbstractButtonShape bshape = getSelectedShape(); if(bshape==null) return;
    
    // Set IsBorderPaintedCheckBox, IsContentAreaFilledCheckBox, MarginText
    setNodeValue("IsBorderPaintedCheckBox", bshape.isBorderPainted());
    setNodeValue("IsContentAreaFilledCheckBox", bshape.isContentAreaFilled());
    setNodeValue("MarginText", bshape.getMarginString());
}

/**
 * Updates the currently selected button shape from the UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Do normal version
    super.respondUI(anEvent);
    
    // Get currently selected button shape, button and helper (return if null)
    AbstractButtonShape bshape = getSelectedShape(); if(bshape==null) return;
        
    // Handle IsBorderPaintedCheckBox, IsContentAreaFilledCheckBox, MarginText
    if(anEvent.equals("IsBorderPaintedCheckBox")) bshape.setBorderPainted(anEvent.getBoolValue());
    if(anEvent.equals("IsContentAreaFilledCheckBox")) bshape.setContentAreaFilled(anEvent.getBoolValue());
    if(anEvent.equals("MarginText")) bshape.setMarginString(anEvent.getStringValue());
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass() { return AbstractButtonShape.class; }

/**
 * Returns the name to be used for this tool in inspector window title.
 */
public String getWindowTitle() { return "Button Inspector"; }

}