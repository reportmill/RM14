package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JSpinnerShape;

/**
 * An inner class to act as RJSliderShape's Tool
 */
public class JSpinnerTool extends JComponentTool {
    
/**
 * Updates the Swing UI panel from the currently selected spinner shape.
 */
/*public void resetUI()
{    
    // Get currently selected spinner shape (and return if null)
    JSpinnerShape spinner = (JSpinnerShape)getSelectedShape(); if(spinner==null) return;
    
    // Update MinEnabledCheckBox and MinValueText
    setNodeValue("MinEnabledCheckbox", spinner.getMinimum()!=null);
    setNodeEnabled("MinValueText", spinner.getMinimum()!=null);
    setNodeValue("MinValueText", spinner.getMinimum());
        
    // Update MaxEnabledCheckBox and MaxValueText
    setNodeValue("MaxEnabledCheckbox", spinner.getMaximum()!=null);
    setNodeEnabled("MaxValueText", spinner.getMaximum()!=null);
    setNodeValue("MaxValueText", spinner.getMaximum());

    // Update InitValueText
    setNodeValue("InitValueText", spinner.getNumber());
    
    // Update StepValueText
    setNodeValue("StepValueText", spinner.getStepSize());
}*/

/**
 * Updates the currently selected spinner shape from the Swing UI panel.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get currently selected spinner shape (and return if null)
    JSpinnerShape spinner = (JSpinnerShape)getSelectedShape(); if(spinner==null) return;
    
    // Handle InitValueText
    if(anEvent.equals("InitValueText")) spinner.setNumber(anEvent.getFloatValue());

    // Handle MinEnabledCheckbox
    if(anEvent.equals("MinEnabledCheckbox")) spinner.setMinimum(anEvent.getBoolValue()? 0f : null);
        
    // Handle MinValueText
    if(anEvent.equals("MinValueText")) spinner.setMinimum(anEvent.getFloatValue());

    // Handle MaxEnabledCheckbox
    if(anEvent.equals("MaxEnabledCheckbox")) spinner.setMaximum(anEvent.getBoolValue()? 0f : null);
        
    // Handle MaxValueText
    if(anEvent.equals("MaxValueText")) spinner.setMaximum(anEvent.getFloatValue());
        
    // Handle StepValueText
    if(anEvent.equals("StepValueText")) spinner.setStepSize(anEvent.getFloatValue());
}*/

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return JSpinnerShape.class; }

/**
 * Returns the inspector window title string.
 */
public String getWindowTitle()  { return "Spinner Inspector"; }

}