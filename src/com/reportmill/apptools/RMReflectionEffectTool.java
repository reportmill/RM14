package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.graphics.*;

/**
 * Swing UI editing for RMReflectionEffect.
 */
public class RMReflectionEffectTool extends RMEffectTool {
    
/**
 * Called by Ribs to reset UI controls.
 */
public void resetUI()
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get shadow effect from shape (or default, if not available)
    RMReflectionEffect reflect = effect instanceof RMReflectionEffect? (RMReflectionEffect)effect : new RMReflectionEffect();
    
    // Update ReflectionHeightSpinner, FadeHeightSpinner, GapHeightSpinner
    setNodeValue("ReflectionHeightSpinner", reflect.getReflectionHeight());
    setNodeValue("FadeHeightSpinner", reflect.getFadeHeight());
    setNodeValue("GapHeightSpinner", reflect.getGapHeight());
}

/**
 * Responds to changes from the Swing GUI panel controls and updates currently selected shape.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get shadow effect from shape (or default, if not available)
    RMReflectionEffect reflect = effect instanceof RMReflectionEffect? (RMReflectionEffect)effect : new RMReflectionEffect();
    
    // Handle ReflectionHeightSpinner, FadeHeightSpinner, GapHeightSpinner
    if(anEvent.equals("ReflectionHeightSpinner"))
        reflect = reflect.deriveEffect(anEvent.getFloatValue(), -1, -1);
    if(anEvent.equals("FadeHeightSpinner"))
        reflect = reflect.deriveEffect(-1, anEvent.getFloatValue(), -1);
    if(anEvent.equals("GapHeightSpinner"))
        reflect = reflect.deriveEffect(-1, -1, anEvent.getFloatValue());
    
    // Set new effect
    setSelectedEffect(reflect);
}

}