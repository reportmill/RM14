package com.reportmill.swing.tool;
import snap.swing.SwingEvent;
import com.reportmill.swing.shape.JSliderShape;

/**
 * An inner class to act as RJSliderShape's Tool
 */
public class JSliderTool extends JComponentTool {
    
/**
 * Updates the UI panel from the currently selected slider shape.
 */
public void resetUI()
{    
    // Get currently selected slider shape (and return if null)
    JSliderShape slider = (JSliderShape)getSelectedShape(); if(slider==null) return;
    
    setNodeValue("MinValueText", slider.getMinimum());
    setNodeValue("MaxValueText", slider.getMaximum());
    setNodeValue("InitValueText", slider.getValue());
    setNodeValue("PaintTicksCheckBox", slider.getPaintTicks());
    //setNodeValue("SnapTicksCheckBox", slider.getSnapToTicks());
    //setNodeValue("MajorTicksText", slider.getMajorTickSpacing());
    //setNodeValue("MinorTicksText", slider.getMinorTickSpacing());
    setNodeValue("PaintLabelsCheckBox", slider.getPaintLabels());
    //setNodeValue("PaintTrackCheckBox", slider.getPaintTrack());
    //setNodeValue("HorizontalRadioButton", slider.getOrientation()==SwingConstants.HORIZONTAL);
    //setNodeValue("VerticalRadioButton", slider.getOrientation()==SwingConstants.VERTICAL);
}

/**
 * Updates the currently selected slider shape from the UI panel.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected slider shape (and return if null)
    JSliderShape slider = (JSliderShape)getSelectedShape(); if(slider==null) return;
    
    // Handle MinValueText, MaxValueText, InitValueText
    if(anEvent.equals("MinValueText")) slider.setMinimum(anEvent.getIntValue());
    if(anEvent.equals("MaxValueText")) slider.setMaximum(anEvent.getIntValue());
    if(anEvent.equals("InitValueText")) slider.setValue(anEvent.getIntValue());
    
    // Handle PaintTicksCheckBox, SnapTickCheckBox, MajorTicksText, MinorTicksText
    if(anEvent.equals("PaintTicksCheckBox")) slider.setPaintTicks(anEvent.getBoolValue());
    //if(anEvent.equals("SnapTicksCheckBox")) slider.setSnapToTicks(anEvent.getBoolValue());
    //if(anEvent.equals("MajorTicksText")) slider.setMajorTickSpacing(anEvent.getIntValue());
    //if(anEvent.equals("MinorTicksText")) slider.setMinorTickSpacing(anEvent.getIntValue());
    
    // Handle PaintLabelsCheckBox, PaintTrackCheckBox
    //if(anEvent.equals("PaintLabelsCheckBox")) slider.setPaintLabels(anEvent.getBoolValue());
    //if(anEvent.equals("PaintTrackCheckBox")) slider.setPaintTrack(anEvent.getBoolValue());
    
    // Handle HorizontalRadioButton, VerticalRadioButton
    /*if(anEvent.equals("HorizontalRadioButton") || anEvent.equals("VerticalRadioButton")) {
        int or = anEvent.equals("HorizontalRadioButton")? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL;
        if(or!=slider.getOrientation()) {
            slider.setOrientation(or);
            sliderShape.setSize(sliderShape.getHeight(), sliderShape.getWidth());
        }
    }*/
    
    // Resize predominant axis if needed
    /*if(slider.getOrientation()==SwingConstants.HORIZONTAL)
        sliderShape.setHeight(Math.max(sliderShape.getHeight(), slider.getPreferredSize().height));
    else sliderShape.setWidth(Math.max(sliderShape.getWidth(), slider.getPreferredSize().width));*/
}

/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return JSliderShape.class; }

/**
 * Returns the inspector window title string.
 */
public String getWindowTitle()  { return "Slider Inspector"; }

}