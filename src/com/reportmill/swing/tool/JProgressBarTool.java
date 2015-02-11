package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JProgressBarShape;

/**
 * Provides UI editing for JProgressBarShape (and JProgressBar).
 */
public class JProgressBarTool extends JComponentTool {

/**
 * Updates the UI from the currently selected progress bar shape.
 */
public void resetUI()
{
    // Get the currently selected progress bar shape
    JProgressBarShape pbar = (JProgressBarShape)getSelectedShape(); if(pbar==null) return;
    
    // Update HorizontalRadioButton, VerticalRadioButton, IsIndeterminateCheckBox
    //setNodeValue("HorizontalRadioButton", pbar.getOrientation()==JProgressBar.HORIZONTAL);
    //setNodeValue("VerticalRadioButton", pbar.getOrientation()==JProgressBar.VERTICAL);
    //setNodeValue("IsIndeterminateCheckBox", pbar.isIndeterminate());
    
    // Update MinValueText, MaxValueText, InitValueText, InitValueSlider
    //setNodeValue("MinValueText", pbar.getMinimum());
    //setNodeValue("MaxValueText", pbar.getMaximum());
    //setNodeValue("InitValueText", pbar.getValue());
    //setNodeValue("InitValueSlider", pbar.getValue());
}

/**
 * Updates the currently selected radio button shape from the UI.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected progress bar shape
    JProgressBarShape pbar = (JProgressBarShape)getSelectedShape(); if(pbar==null) return;
    
    // Handle HorizontalRadioButton - set new orientation and rotate 90 degrees about center
    if(anEvent.equals("HorizontalRadioButton") && pbar.getOrientation()!=JProgressBar.HORIZONTAL) {
        Rectangle b = pbar.getBounds();
        pbar.setOrientation(JProgressBar.HORIZONTAL);
        pbar.setBounds(b.x+(b.width-b.height)/2, b.y+(b.height-b.width)/2, b.height, b.width);
    }
    
    // Handle VerticalRadioButton - set new orientation and rotate 90 degrees about center
    else if(anEvent.equals("VerticalRadioButton") && pbar.getOrientation()!=JProgressBar.VERTICAL) {
        Rectangle b = pbar.getBounds();
        pbar.setOrientation(JProgressBar.VERTICAL);
        pbar.setBounds(b.x+(b.width-b.height)/2, b.y+(b.height-b.width)/2, b.height, b.width);
    }
    
    // Handle IsIndeterminateCheckBox
    if(anEvent.equals("IsIndeterminateCheckBox")) pbar.setIndeterminate(anEvent.getBoolValue());
    
    // Handle MinValueText
    if(anEvent.equals("MinValueText")) pbar.setMinimum(anEvent.getIntValue());
    
    // Handle MaxValueText
    if(anEvent.equals("MaxValueText")) pbar.setMaximum(anEvent.getIntValue());
    
    // Handle InitValueText
    if(anEvent.equals("InitValueText")) pbar.setValue(anEvent.getIntValue());
    
    // Handle InitValueSlider
    if(anEvent.equals("InitValueSlider")) {
        pbar.setValue(anEvent.getIntValue());
        setNodeValue("InitValueText", anEvent.getIntValue());
    }
}*/

/**
 * Returns the separator tool's class.
 */
public Class getShapeClass()  { return JProgressBarShape.class; }

/**
 * Returns the name for the inspector window title bar.
 */
public String getWindowTitle()  { return "JProgressBar Inspector"; }

}