package com.reportmill.swing.tool;
import com.reportmill.app.RMEditor;
import com.reportmill.apptools.RMTool;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import java.util.List;
import snap.swing.*;

/**
 * Provides fill inspector for JComponentShape Swing shape attributes.
 */
public class SwingFills extends SwingOwner {
    
/**
 * Updates UI panel from the currently selected shape.
 */
public void resetUI()
{
    // Get the JComponent shape, the component and helper
    RMShape s = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape(); if(!(s instanceof JComponentShape)) return;
    JComponentShape jshape = (JComponentShape)s;
    
    // Update enabled status of all components in the inspector
    getUI().setEnabled(true);
    
    // Get component name (if null, create a suggestion)
    String name = jshape.getName();
    if(name==null) {
        name = "My" + RMClassUtils.getClassSimpleName(jshape); name = RMStringUtils.replace(name, "MyJ", "My"); }
    
    // Update NameText, ToolTipText, SubclassText
    setNodeValue("NameText", name);
    setNodeValue("ToolTipText", jshape.getToolTipText());
    setNodeValue("SubclassText", jshape.getRealClassName());
    
    // Update ForegroundCheckBox, ForegroundColorWell, BackgroundCheckBox, BackgroundColorWell
    setNodeValue("ForegroundCheckBox", jshape.getForeground()!=null);
    setNodeValue("ForegroundColorWell", jshape.getForeground()!=null? jshape.getForeground().awt() : null);
    setNodeValue("BackgroundCheckBox", jshape.getBackground()!=null);
    setNodeValue("BackgroundColorWell", jshape.getBackground()!=null? jshape.getBackground().awt() : null);
    
    // Uppdate IsOpaqueCheckBox, IsOpaqueResetButton, IsEnabledCheckBox
    setNodeValue("IsOpaqueCheckBox", jshape.isOpaque());
    setNodeEnabled("IsOpaqueResetButton", jshape.isOpaque()!=null);
    setNodeValue("IsEnabledCheckBox", jshape.isEnabled());
    
    // Update SizeVariantComboBox
    String sizeVariant = (String)jshape.getClientProperty("JComponent.sizeVariant");
    setNodeValue("SizeVariantComboBox", sizeVariant!=null? sizeVariant : "regular");
}

/**
 * Updates the currently selected shape from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected Shape, JComponentShape, Component and selected shapes
    RMShape s = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape(); if(!(s instanceof JComponentShape)) return;
    JComponentShape jshape = (JComponentShape)s;
    List <JComponentShape> shapes = (List)RMEditor.getMainEditor().getSelectedOrSuperSelectedShapes();
    
    // Register for repaint (and thus undo)
    RMShapeUtils.repaint(shapes);
    
    // Handle NameText, ToolTipText, SubclassText
    if(anEvent.equals("NameText")) jshape.setName(anEvent.getStringValue());
    if(anEvent.equals("ToolTipText")) jshape.setToolTipText(anEvent.getStringValue());
    if(anEvent.equals("SubclassText")) jshape.setRealClassName(anEvent.getStringValue());
    
    // Handle ForegroundCheckBox, ForegroundColorWell, , 
    if(anEvent.equals("ForegroundCheckBox"))
        jshape.setForeground(anEvent.getBoolValue()? jshape.getForeground() : null);
    if(anEvent.equals("ForegroundColorWell"))
        jshape.setForeground(new RMColor(anEvent.getColorValue()));
    
    // Handle BackgroundCheckBox: If switch set, set background color to default, otherwise set to null
    if(anEvent.equals("BackgroundCheckBox")) {
        jshape.setBackground(anEvent.getBoolValue()? jshape.getBackground() : null);
        
        // If switch set, turn on opacity as convenience (might be wrong if bg is rounded rect)
        if(anEvent.getBoolValue())
            jshape.setOpaque(true);
    }
    
    // Handle BackgroundColorWell: Set background color
    if(anEvent.equals("BackgroundColorWell")) {
        jshape.setBackground(new RMColor(anEvent.getColorValue()));
        
        // Turn on opacity as an convenience (might be wrong if bg is rounded rect)
        jshape.setOpaque(true);
    }
    
    // Handle IsOpaqueCheckBox (go through helper so value is marked as having been explicitly changed)
    if(anEvent.equals("IsOpaqueCheckBox")) jshape.setOpaque(anEvent.getBooleanValue());
    
    // Handle IsOpaqueResetButton
    if(anEvent.equals("IsOpaqueResetButton")) jshape.setOpaque(null);
    
    // Handle IsEnabledCheckBox
    if(anEvent.equals("IsEnabledCheckBox")) jshape.setEnabled(anEvent.getBoolValue());
    
    // Handle SizeVariantComboBox
    if(anEvent.equals("SizeVariantComboBox")) {
        String sizeVariant = anEvent.getStringValue().equals("regular")? null : anEvent.getStringValue();
        for(JComponentShape shp : shapes)
            shp.getComponent().putClientProperty("JComponent.sizeVariant", sizeVariant);
    }
    
    // Handle StandardFillsButton
    if(anEvent.equals("StandardFillsButton"))
        ((JComponentTool)RMTool.getTool(JComponentShape.class)).setShowStandardFills(true);
}

/**
 * Returns the name to be used in the inspector's window title.
 */
public String getWindowTitle()  { return "General Inspector"; }

}