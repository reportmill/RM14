package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.util.List;
import snap.swing.*;

/**
 * This class provides Swing UI editing for the currently selected shapes location and size.
 */
public class ShapeLocationSize extends SwingOwner {
    
/** Returns the name to be used in the inspector's window title. */
public String getWindowTitle()  { return "Location/Size Inspector"; }

/**
 * Updates Swing UI controls from currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
    
    // Update XThumb & XText
    setNodeValue("XThumb", getUnitsFromPoints(shape.getFrameX()));
    setNodeValue("XText", getUnitsFromPoints(shape.getFrameX()));
    
    // Update YThumb & YText
    setNodeValue("YThumb", getUnitsFromPoints(shape.getFrameY()));
    setNodeValue("YText", getUnitsFromPoints(shape.getFrameY()));
    
    // Update WThumb & WText
    setNodeValue("WThumb", getUnitsFromPoints(shape.width()));
    setNodeValue("WText", getUnitsFromPoints(shape.width()));
    
    // Update HThumb & HText
    setNodeValue("HThumb", getUnitsFromPoints(shape.height()));
    setNodeValue("HText", getUnitsFromPoints(shape.height()));
    
    // Update MinWText and MinHText
    setNodeValue("MinWText", shape.isMinWidthSet()? shape.getMinWidth() : "-");
    setNodeValue("MinHText", shape.isMinHeightSet()? shape.getMinHeight() : "-");
    
    // Update PrefWText and PrefHText
    setNodeValue("PrefWText", shape.isPrefWidthSet()? shape.getPrefWidth() : "-");
    setNodeValue("PrefHText", shape.isPrefHeightSet()? shape.getPrefHeight() : "-");
    
    // Disable if document or page
    getUI().setEnabled(!(shape instanceof RMDocument || shape instanceof RMPage));
}

/**
 * Updates currently selected shape from Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected editor, document and shapes
    RMEditor editor = RMEditor.getMainEditor();
    List <? extends RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Handle X ThumbWheel and Text
    if(anEvent.equals("XThumb") || anEvent.equals("XText")) {
        editor.undoerSetUndoTitle("Location Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        for(RMShape shape : shapes) shape.setFrameX(value);
    }
    
    // Handle Y ThumbWheel and Text
    if(anEvent.equals("YThumb") || anEvent.equals("YText")) {
        editor.undoerSetUndoTitle("Location Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        for(RMShape shape : shapes) shape.setFrameY(value);
    }
    
    // Handle Width ThumbWheel and Text
    if(anEvent.equals("WThumb") || anEvent.equals("WText")) {
        editor.undoerSetUndoTitle("Size Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        if(Math.abs(value)<.1) value = RMMath.sign(value)*.1f;
        for(RMShape shape : shapes) shape.setWidth(value);
    }
    
    // Handle Height ThumbWheel and Text
    if(anEvent.equals("HThumb") || anEvent.equals("HText")) {
        editor.undoerSetUndoTitle("Size Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        if(Math.abs(value)<.1) value = RMMath.sign(value)*.1f;
        for(RMShape shape : shapes) shape.setHeight(value);
    }
    
    // Handle MinWText & MinHText
    if(anEvent.equals("MinWText"))
        for(RMShape shape : shapes) shape.setMinWidth(anEvent.getFloatValue());
    if(anEvent.equals("MinHText"))
        for(RMShape shape : shapes) shape.setMinHeight(anEvent.getFloatValue());
    
    // Handle MinWSyncButton & MinHSyncButton
    if(anEvent.equals("MinWSyncButton"))
        for(RMShape shape : shapes) shape.setMinWidth(shape.getWidth());
    if(anEvent.equals("MinHSyncButton"))
        for(RMShape shape : shapes) shape.setMinHeight(shape.getHeight());

    // Handle PrefWText & PrefHText
    if(anEvent.equals("PrefWText"))
        for(RMShape shape : shapes) shape.setPrefWidth(anEvent.getFloatValue());
    if(anEvent.equals("PrefHText"))
        for(RMShape shape : shapes) shape.setPrefHeight(anEvent.getFloatValue());
    
    // Handle PrefWSyncButton & PrefHSyncButton
    if(anEvent.equals("PrefWSyncButton"))
        for(RMShape shape : shapes) shape.setPrefWidth(shape.getWidth());
    if(anEvent.equals("PrefHSyncButton"))
        for(RMShape shape : shapes) shape.setPrefHeight(shape.getHeight());
}

/**
 * Converts from shape units to tool units.
 */
public double getUnitsFromPoints(double aValue)
{
    RMEditor editor = RMEditor.getMainEditor(); RMDocument doc = editor.getDocument();
    return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
}

/**
 * Converts from tool units to shape units.
 */
public double getPointsFromUnits(double aValue)
{
    RMEditor editor = RMEditor.getMainEditor(); RMDocument doc = editor.getDocument();
    return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
}

}