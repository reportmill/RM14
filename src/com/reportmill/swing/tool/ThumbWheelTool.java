package com.reportmill.swing.tool;
import snap.swing.ThumbWheel;
import snap.swing.SwingEvent;
import com.reportmill.swing.shape.ThumbWheelShape;

/**
 * An RMTool implementation for ThumbWheelShape.
 */
public class ThumbWheelTool <T extends ThumbWheelShape> extends JComponentTool <T> {
    
/**
 * Updates the UI controls from the currently selected thumbwheel shape.
 */
public void resetUI()
{
    // Get the thumbwheel shape and thumbwheel (just return if null)
    ThumbWheelShape tws = getSelectedShape(); if(tws==null) return;
    
    // Update MinText, MaxText, InitText
    setNodeValue("MinText", tws.getVisibleMin());
    setNodeValue("MaxText", tws.getVisibleMax());
    setNodeValue("InitText", tws.getValue());
    
    // Update AbsMinText, AbsMaxText
    if(tws.getAbsoluteMin()<-Float.MAX_VALUE/2) setNodeValue("AbsMinText", "-inf");
    else setNodeValue("AbsMinText", tws.getAbsoluteMin());
    if(tws.getAbsoluteMax()>Float.MAX_VALUE/2) setNodeValue("AbsMaxText",  "+inf");
    else setNodeValue("AbsMaxText", tws.getAbsoluteMax());
    
    // Update RoundToText
    setNodeValue("RoundToText", tws.getRound());
    
    // Update RadialRadioButton, LinearRadioButton
    setNodeValue("RadialRadioButton", tws.getType()==ThumbWheel.TYPE_RADIAL);
    setNodeValue("LinearRadioButton", tws.getType()==ThumbWheel.TYPE_LINEAR);
}

/**
 * Updates currently selected thumbwheel shape from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the thumbwheel shape and thumbwheel (just return if null)
    ThumbWheelShape tws = getSelectedShape(); if(tws==null) return;

    // Handle MinText, MaxText, InitText
    if(anEvent.equals("MinText")) tws.setVisibleMin(anEvent.getFloatValue());
    if(anEvent.equals("MaxText")) tws.setVisibleMax(anEvent.getFloatValue());
    if(anEvent.equals("InitText")) tws.setValue(anEvent.getFloatValue());
    
    // Handle AbsMinText, AbsMaxText
    if(anEvent.equals("AbsMinText"))
        tws.setAbsoluteMin(anEvent.getStringValue().equals("-inf")? -Float.MAX_VALUE : anEvent.getFloatValue());
    if(anEvent.equals("AbsMaxText"))
        tws.setAbsoluteMax(anEvent.getStringValue().equals("+inf")? Float.MAX_VALUE : anEvent.getFloatValue());
    
    // Handle RoundToText
    if(anEvent.equals("RoundToText"))
        tws.setRound(anEvent.getFloatValue());
        
    // Handle RadialRadioButton, LinearRadioButton
    if(anEvent.equals("RadialRadioButton")) tws.setType(ThumbWheel.TYPE_RADIAL);
    if(anEvent.equals("LinearRadioButton")) tws.setType(ThumbWheel.TYPE_LINEAR);
}

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return ThumbWheelShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "ThumbWheel Inspector"; }

}