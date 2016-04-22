package com.reportmill.apptools;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import snap.swing.*;

/**
 * UI editing for RMImageFill.
 */
public class RMImageFillTool extends RMFillTool {

/**
 * Updates the UI controls from the currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape (just return if null) and image fill (if none, use default instance)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    RMImageFill fill = shape.getFill() instanceof RMImageFill? (RMImageFill)shape.getFill() : _imageFill;
    
    // Update TiledCheckBox
    setNodeValue("TiledCheckBox", fill.isTiled());
    
    // Update XSpinner, YSpinner, ScaleXSpinner and ScaleYSpinner
    setNodeValue("XSpinner", fill.getX());
    setNodeValue("YSpinner", fill.getY());
    setNodeValue("ScaleXSpinner", fill.getScaleX());
    setNodeValue("ScaleYSpinner", fill.getScaleY());
}

/**
 * Updates the currently selected shape from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected shape (just return if null) and image fill (if none, use default instance)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    RMImageFill fill = shape.getFill() instanceof RMImageFill? (RMImageFill)shape.getFill() : _imageFill;
    
    // Handle TiledCheckBox
    if(anEvent.equals("TiledCheckBox"))
        fill = fill.deriveFill(RMImageFill.ATTRIBUTE_TILED, anEvent.getBooleanValue()? 1 : 0);
    
    // Handle XSpinner, YSpinner, ScaleXSpinner, ScaleXSpinner, ScaleYSpinner
    if(anEvent.equals("XSpinner"))
        fill = fill.deriveFill(RMImageFill.ATTRIBUTE_X, anEvent.getFloatValue());
    if(anEvent.equals("YSpinner"))
        fill = fill.deriveFill(RMImageFill.ATTRIBUTE_Y, anEvent.getFloatValue());
    if(anEvent.equals("ScaleXSpinner"))
        fill = fill.deriveFill(RMImageFill.ATTRIBUTE_SCALE_X, anEvent.getFloatValue());
    if(anEvent.equals("ScaleYSpinner"))
        fill = fill.deriveFill(RMImageFill.ATTRIBUTE_SCALE_Y, anEvent.getFloatValue());
    
    // Handle ChooseButton
    if(anEvent.equals("ChooseButton")) {
        String path = FileChooserUtils.showChooser(false, null, "Image File", ".png", ".jpg", ".gif");
        if(path!=null) {
            RMImageData imageData = RMImageData.getImageData(path);
            if(imageData!=null)
                fill = fill.deriveFill(imageData);
        }
    }

    // Set new fill
    setSelectedFill(fill);
}

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Fill Inspector (Texture)"; }

}