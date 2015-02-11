package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.graphics.*;

/**
 * Swing UI editing for RMShadowEffect.
 */
public class RMBlurEffectTool extends RMEffectTool {
    
/**
 * Called by Ribs to reset UI controls.
 */
public void resetUI()
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get blur effect from shape (or default, if not available)
    RMBlurEffect blurEffect = effect instanceof RMBlurEffect? (RMBlurEffect)effect : new RMBlurEffect();
    
    // Set BlurRadiusSpinner
    setNodeValue("BlurRadiusSpinner", blurEffect.getRadius());
    
    // Set BlurRadiusSlider
    setNodeValue("BlurRadiusSlider", blurEffect.getRadius());
}

/**
 * Responds to changes from the Swing GUI panel controls and updates currently selected shape.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get blur effect from shape (or default, if not available)
    RMBlurEffect blurEffect = effect instanceof RMBlurEffect? (RMBlurEffect)effect : new RMBlurEffect();
    
    // Handle BlurRadiusSpinner
    if(anEvent.equals("BlurRadiusSpinner"))
        blurEffect = blurEffect.deriveFill(anEvent.getIntValue());
    
    // Handle BlurRadiusSlider
    if(anEvent.equals("BlurRadiusSlider")) {
        blurEffect = blurEffect.deriveFill(anEvent.getIntValue());
        setNodeValue("BlurRadiusSpinner", anEvent.getIntValue());
    }
    
    // Set new shadow effect
    setSelectedEffect(blurEffect);
}

}