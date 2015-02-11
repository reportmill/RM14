package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import com.reportmill.base.RMClassUtils;

/**
 * Swing UI editing for RMGradientFill.
 */
public class RMGradientFillTool extends RMFillTool {

/**
 * Updates the UI controls from the currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape and shape gradient fill (just return if null)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    RMGradientFill fill = getDefaultFill(shape);
    
    RMColorStopPicker picker = getNode("ColorStopPicker", RMColorStopPicker.class);
    picker.setGradient(fill);
    
    // set the type popup and swap in the proper controls
    boolean fillIsRadial = fill instanceof RMRadialGradientFill;
    setNodeSelectedIndex("TypeComboBox", fillIsRadial ? 1 : 0);
    getNode("RadialPicker").setVisible(fillIsRadial);
    getNode("LinearControls").setVisible(!fillIsRadial);
        
    // Update angle controls for a linear gradient
    if(!fillIsRadial) {
        setNodeValue("AngleThumb", fill.getRoll());
        setNodeValue("AngleText", fill.getRoll());
    }
    
    // or the axis picker for a radial gradient
    else {
        RMRadialAxisPicker radialControl = getNode("RadialPicker", RMRadialAxisPicker.class);
        radialControl.setGradient((RMRadialGradientFill)fill);
    }
}

/**
 * Updates the currently selected shape from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected shape and its fill (just return if null)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    RMGradientFill oldfill = getDefaultFill(shape), newFill = null;
    
    // Handle ColorStopPicker
    if(anEvent.equals("ColorStopPicker")) {
        RMColorStopPicker picker = anEvent.getTarget(RMColorStopPicker.class);
        RMGradientFill pickerFill = picker.getGradient();
        newFill = oldfill.clone();
        newFill.setStops(pickerFill.getColorStops());
    }
    
    // Handle ReverseStopsButton
    else if(anEvent.equals("ReverseStopsButton")) {
        newFill = oldfill.clone();
        newFill.reverseColors();
    }
    
    // Handle AngleThumb and AngleText
    else if(anEvent.equals("AngleThumb") || anEvent.equals("AngleText")) {
        float angle = anEvent.equals("AngleThumb")? (float)anEvent.getIntValue(): anEvent.getFloatValue();
        newFill = oldfill.deriveGradient(angle);
    }
    
    // Handle linear/radial popup
    else if(anEvent.equals("TypeComboBox"))
        newFill = oldfill.deriveGradient(anEvent.getSelectedIndex()==1);

    // Handle radial axis control
    else if(anEvent.equals("RadialPicker")) {
        RMRadialAxisPicker p = anEvent.getTarget(RMRadialAxisPicker.class);
        newFill = ((RMRadialGradientFill)oldfill).deriveGradient(p.getStartPoint(), p.getEndPoint());
    }
    
    // Reset fill of all selected shapes
    if(newFill!=null)
        setSelectedFill(newFill);
}

/**
 * Returns the gradient for the shape.  Creates one if the shape doesn't have a gradient fill.
 */
public RMGradientFill getDefaultFill(RMShape shape)
{
    // Get shape gradient fill, if present
    RMGradientFill fill = RMClassUtils.getInstance(shape.getFill(), RMGradientFill.class);
    
    // If missing, create one - second color defaults to black, unless that would result in a black-black gradient
    if(fill==null) {
        RMColor c = shape.getColor();
        RMColor c2 = c.equals(RMColor.black)? RMColor.white : RMColor.black;
        fill = new RMGradientFill(c, c2, 0);
    }
    
    // Return fill
    return fill;
}

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Fill Inspector (Gradient)"; }

}