package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.event.MouseEvent;
import snap.swing.SwingEvent;

/**
 * Provides UI editing for RMSound shapes.
 */
public class RMSoundShapeTool <T extends RMSoundShape> extends RMTool <T> {
    
/**
 * Creates a new sound tool.
 */
public Class getShapeClass()  { return RMSoundShape.class; }

/**
 * Sets the tooltip to the name of the sound file
 */
public String getToolTipText(T aShape, MouseEvent anEvent)  { return aShape.getSoundName(); }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Sound Inspector"; }

/**
 * Updates the UI controls from the currently selected sound.
 */
public void resetUI()
{
    // Get selected shape (just return if null)
    RMSoundShape shape = getSelectedShape(); if(shape==null) return;
    
    // Update SoundKeyText, DelayText, LoopCountText, PlayingCheckBox
    setNodeValue("SoundKeyText", shape.getKey());
    setNodeValue("DelayText", shape.getDelay());
    setNodeValue("LoopCountText", shape.getLoopCount());
    setNodeValue("PlayingCheckBox", shape.getPlaying());
}

/**
 * Updates the currently selected sound from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected shape (just return if null)
    RMSoundShape shape = getSelectedShape(); if(shape==null) return;
    
    // Handle SoundKeyText, DelayText, LoopCountText, PlayingCheckBox
    if(anEvent.equals("SoundKeyText")) shape.setKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
    if(anEvent.equals("DelayText")) shape.setDelay(anEvent.getFloatValue());
    if(anEvent.equals("LoopCountText")) shape.setLoopCount(anEvent.getIntValue());
    if(anEvent.equals("PlayingCheckBox")) shape.setPlaying(anEvent.getBoolValue());

    // Handle KeysButton
    //if(anEvent.equals("KeysButton")) AttributesPanel.shared().setVisible(AttributesPanel.KEYS);
}

}