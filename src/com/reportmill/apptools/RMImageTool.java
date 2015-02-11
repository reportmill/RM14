package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.util.List;
import snap.swing.*;

/**
 * Provides Swing UI for RMImage shape editing.
 */
public class RMImageTool <T extends RMImageShape> extends RMTool <T> {
    
/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMImageShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle() { return "Image Tool"; }

/**
 * Updates the UI controls from the currently selected image.
 */
public void resetUI()
{    
    // Get selected image, image fill, image data and fill style (just return if null)
    RMImageShape image = getSelectedShape(); if(image==null) return;
    RMImageData idata = image.getImageData();
    
    // Reset KeyText, PageText, MarginsText, GrowToFitCheckBox, PreserveRatioCheckBox
    setNodeValue("KeyText", image.getKey());
    setNodeValue("PageText", image.getPageIndex()+1);
    setNodeValue("PaddingText", RMStringUtils.toString(getUnitsFromPoints(image.getPadding())));
    setNodeValue("GrowToFitCheckBox", image.isGrowToFit());
    setNodeValue("PreserveRatioCheckBox", image.getPreserveRatio());
    
    // Reset RoundingThumb and RoundingText
    setNodeValue("RoundingThumb", image.getRadius());
    setNodeValue("RoundingText", image.getRadius());
    
    // Reset TypeLabel
    if(idata==null || idata==RMImageData.EMPTY) setNodeValue("TypeLabel", "");
    else setNodeValue("TypeLabel", "Type: " + idata.getType() + "\nSize: " + idata.getWidth() + "x" + idata.getHeight()+
        " (" + (int)(image.getWidth()/idata.getWidth()*image.getScaleX()*100) + "%)");
    
    // Reset SaveButton enabled
    setNodeEnabled("SaveButton", idata!=null);
    
    // Reset JPEGButton enabled
    setNodeEnabled("JPEGButton", idata!=null && !idata.getType().equals("jpg"));
}

/**
 * Updates the currently selected image from the Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected image and images (just return if null)
    RMImageShape image = getSelectedShape(); if(image==null) return;
    List <RMImageShape> images = (List)getSelectedShapes();

    // Handle KeyText
    if(anEvent.equals("KeyText"))
        image.setKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
        
    // Handle KeysButton
    if(anEvent.equals("KeysButton"))
        getEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);

    // Handle PageText
    if(anEvent.equals("PageText"))
        image.setPageIndex(anEvent.getIntValue()-1);
    
    // Handle PaddingText
    if(anEvent.equals("PaddingText"))
        for(RMImageShape im : images) im.setPadding(anEvent.getIntValue());
    
    // Handle GrowToFitCheckBox, PreserveRatioCheckBox
    if(anEvent.equals("GrowToFitCheckBox"))
        for(RMImageShape im : images) im.setGrowToFit(anEvent.getBoolValue());
    if(anEvent.equals("PreserveRatioCheckBox"))
        for(RMImageShape im : images) im.setPreserveRatio(anEvent.getBoolValue());
    
    // Handle Rounding Radius Thumb & Text
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
        image.undoerSetUndoTitle("Rounding Change");
        float value = anEvent.getFloatValue();
        for(RMImageShape im : images) {
            im.setRadius(value);
            if(im.getStroke()==null)
                im.setStroke(new RMStroke());
        }
    }
    
    // Handle SaveButton
    if(anEvent.equals("SaveButton")) {
        RMImageData idata = image.getImageData(); if(idata==null) return;
        String type = idata.getType(); if(RMStringUtils.length(type)==0) return;
        String path = FileChooserUtils.showChooser(true, getEditor(), type.toUpperCase() + " File", "." + type);
        if(path==null) return;
        RMUtils.writeBytes(idata.getBytes(), path);
    }
    
    // Handle JPEGButton
    if(anEvent.equals("JPEGButton")) {
        RMImageData idata = image.getImageData(); if(idata==null) return;
        Image im = idata.getImage(); if(im==null) return;
        byte jpegBytes[] = RMAWTUtils.getBytesJPEG(im);
        image.setImageData(jpegBytes);
    }
}

}