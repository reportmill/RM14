package com.reportmill.apptools;
import snap.swing.SwingEvent;
import com.reportmill.graphics.*;

/**
 * Swing UI editing for RMChiselEffect.
 */
public class RMChiselEffectTool extends RMEmbossEffectTool {

/**
 * Override to return a chisel instance.
 */
public RMChiselEffect getEffect()
{
    RMEffect effect = getSelectedEffect();
    return effect instanceof RMChiselEffect? (RMChiselEffect)effect : new RMChiselEffect();
}

public void resetUI()
{
    super.resetUI();
    RMChiselEffect f = getEffect();
    int bevdepth = (int)(f.getBevelDepth()*100+.5);
    setNodeValue("DepthWheel", bevdepth);
    setNodeValue("DepthTextField", bevdepth);
    setNodeValue("DirectionUpRadio", f.isDirectionUp());
    setNodeValue("DirectionDownRadio", !f.isDirectionUp());
}

public void respondUI(SwingEvent anEvent)
{
    // Handle DepthWheel and DepthTextField
    if(anEvent.equals("DepthWheel") || anEvent.equals("DepthTextField"))
        setSelectedEffect(getEffect().deriveEffect(anEvent.getIntValue()/100f));
    
    // Handle DirectionUpRadio and DirectionDownRadio
    else if(anEvent.equals("DirectionUpRadio") || anEvent.equals("DirectionDownRadio"))
        setSelectedEffect(getEffect().deriveEffect(anEvent.equals("DirectionUpRadio")));
    
    // Handle anything else
    else super.respondUI(anEvent);
}

}