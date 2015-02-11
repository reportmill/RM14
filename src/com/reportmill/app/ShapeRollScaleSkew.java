package com.reportmill.app;
import com.reportmill.shape.*;
import java.util.List;
import snap.swing.*;

/**
 * This class provides Swing UI editing for advanced transforms such as rotation, scale and skew for the
 * currently selected shapes.
 */ 
public class ShapeRollScaleSkew extends SwingOwner {

/**
 * Resets the Swing UI controls from the currently selected shapes.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
    
    // Update RotationThumb and RotationText
    setNodeValue("RotationThumb", shape.getRoll());
    setNodeValue("RotationText", shape.getRoll());
    
    // Update ScaleXThumb and ScaleXText
    setNodeValue("ScaleXThumb", shape.getScaleX());
    setNodeValue("ScaleXText", shape.getScaleX());
    
    // Update ScaleYThumb and ScaleYText
    setNodeValue("ScaleYThumb", shape.getScaleY());
    setNodeValue("ScaleYText", shape.getScaleY());
    
    // Update SkewXThumb and SkewXText
    setNodeValue("SkewXThumb", shape.getSkewX());
    setNodeValue("SkewXText", shape.getSkewX());
    
    // Update SkewYThumb and SkewYText
    setNodeValue("SkewYThumb", shape.getSkewY());
    setNodeValue("SkewYText", shape.getSkewY());
    
    // Disable if document or page
    getUI().setEnabled(!(shape instanceof RMDocument || shape instanceof RMPage));
}

/**
 * Responds to changes from the Swing UI panel's controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected shape and shapes
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
    List <RMShape> shapes = RMEditor.getMainEditor().getSelectedOrSuperSelectedShapes();
    
    // Handle Rotation Thumb & Text
    if(anEvent.equals("RotationThumb") || anEvent.equals("RotationText")) {
        shape.undoerSetUndoTitle("Rotation Change");
        float value = anEvent.getFloatValue();
        for(RMShape s : shapes)
            s.setRoll(value);
    }

    // Handle ScaleX/ScaleY Thumb & Text
    if(anEvent.equals("ScaleXThumb") || anEvent.equals("ScaleXText") ||
        anEvent.equals("ScaleYThumb") || anEvent.equals("ScaleYText")) {
        shape.undoerSetUndoTitle("Scale Change");
        float value = anEvent.getFloatValue();
        boolean symmetrical = getNodeBoolValue("ScaleSymetricCheckBox");
        
        // Handle ScaleX (and symmetrical)
        if(anEvent.equals("ScaleXThumb") || anEvent.equals("ScaleXText") || symmetrical)
            for(RMShape s : shapes)
                s.setScaleX(value);

        // Handle ScaleY (and symmetrical)
        if(anEvent.equals("ScaleYThumb") || anEvent.equals("ScaleYText") || symmetrical)
            for(RMShape s : shapes)
                s.setScaleY(value);
    }

    // Handle SkewX Thumb & Text
    if(anEvent.equals("SkewXThumb") || anEvent.equals("SkewXText")) {
        shape.undoerSetUndoTitle("Skew Change");
        float value = anEvent.getFloatValue();
        for(RMShape s : shapes)
            s.setSkewX(value);
    }

    // Handle SkewY Thumb & Text
    if(anEvent.equals("SkewYThumb") || anEvent.equals("SkewYText")) {
        shape.undoerSetUndoTitle("Skew Change");
        float value = anEvent.getFloatValue();
        for(RMShape s : shapes)
            s.setSkewY(value);
    }
}

/**
 * Returns the name to be used in the inspector panel window title.
 */
public String getWindowTitle()  { return "Roll/Scale/Skew Inspector"; }

}