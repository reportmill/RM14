package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.graphics.*;

/**
 * Inspector for RMEmbossEffect.
 */
public class RMEmbossEffectTool extends RMEffectTool {

/**
 * Return emboss effect (or default emboss effect, if not available).
 */
public RMEmbossEffect getEffect()
{
    RMEffect effect = getSelectedEffect();
    return effect instanceof RMEmbossEffect ? (RMEmbossEffect)effect : new RMEmbossEffect();
}

/**
 * Called by Ribs to reset UI controls.
 */
public void resetUI()
{
    // Get currently selected effect
    RMEmbossEffect emboss = getEffect();
    
    // Update everybody
    setNodeValue("RadiusWheel", emboss.getRadius());
    setNodeValue("RadiusTextField", emboss.getRadius());
    setNodeValue("AzimuthTextField", emboss.getAzimuth());
    setNodeValue("AzimuthWheel", emboss.getAzimuth());
    setNodeValue("AltitudeTextField", emboss.getAltitude());
    setNodeValue("AltitudeWheel", emboss.getAltitude());
}

/**
 * Responds to changes from the Swing UI panel controls and updates currently selected shape.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected effect
    RMEmbossEffect emboss = getEffect(), newEffect = null;

    // Handle AltitudeTextField and AltitudeWheel
    if(anEvent.equals("AltitudeWheel") || anEvent.equals("AltitudeTextField"))
        newEffect = emboss.deriveEffect(anEvent.getFloatValue(), emboss.getAzimuth());

    // Handle AltitudeTextField and AltitudeWheel
    if(anEvent.equals("AzimuthWheel") || anEvent.equals("AzimuthTextField"))
        newEffect = emboss.deriveEffect(emboss.getAltitude(), anEvent.getFloatValue());

    // Handle AltitudeTextField and AltitudeWheel
    if(anEvent.equals("RadiusWheel") || anEvent.equals("RadiusTextField"))
        newEffect = emboss.deriveEffect(anEvent.getIntValue());

    // Set new effect
    if(newEffect!=null)
        setSelectedEffect(newEffect);
}

}