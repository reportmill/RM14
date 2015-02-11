package com.reportmill.swing.tool;
import com.reportmill.app.RMEditor;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import snap.swing.*;

/**
 * Provides UI editing for JTextFieldShape (and JTextField).
 */
public class JTextFieldTool <T extends JTextFieldShape> extends JTextComponentTool <T> {
    
/**
 * Updates UI controls from the currently selected text field shape.
 */
public void resetUI()
{    
    // Get the currently selected text field shape and text and helper (just return if null)
    JTextFieldShape tfshape = getSelectedShape(); if(tfshape==null) return;
    
    // Set the IsBorderPaintedCheckBox, MarginText, AutoScrollsCheckBox, EditableCheckBox
    setNodeValue("IsBorderPaintedCheckBox", tfshape.getBorder() != null);
    setNodeValue("MarginText", tfshape.getMarginString());
    setNodeValue("AutoScrollsCheckBox", tfshape.getAutoscrolls());
    setNodeValue("EditableCheckBox", tfshape.isEditable());

    // Set the SendActionOnFocusLostCheckBox and SendActionOnDropStringCheckBox
    setNodeValue("SendActionOnFocusLostCheckBox", tfshape.getSendActionOnFocusLost());
    //setNodeValue("SendActionOnDropStringCheckBox", tfshape.getSendActionOnDropString());
}

/**
 * Updates the currently selected text field shape from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected text field shape and text and helper (just return if null)
    JTextFieldShape tfshape = getSelectedShape(); if(tfshape==null) return;
    
    // Handle IsBorderPaintedCheckBox
    /*if(anEvent.equals("IsBorderPaintedCheckBox")) {
        boolean wantBorder = getNodeBoolValue("IsBorderPaintedCheckBox");
        Border border = tfshape.getBorder();
        if(!wantBorder && border!=null) { tfshape.putClientProperty("oldBorder", border); border = null; }
        if(wantBorder) border = (Border)tfshape.getClientProperty("oldBorder");
        tfshape.setBorder(border);
    }*/
    
    // Handle MarginText, AutoScrollsCheckBox, EditableCheckBox
    if(anEvent.equals("MarginText")) tfshape.setMarginString(anEvent.getStringValue());
    if(anEvent.equals("AutoScrollsCheckBox")) tfshape.setAutoscrolls(anEvent.getBoolValue());
    if(anEvent.equals("EditableCheckBox")) tfshape.setEditable(anEvent.getBoolValue());
    
    // Handle SendActionOnDropStringCheckBox, SendActionOnDropStringCheckBox
    if(anEvent.equals("SendActionOnFocusLostCheckBox")) tfshape.setSendActionOnFocusLost(anEvent.getBoolValue());
    //if(anEvent.equals("SendActionOnDropStringCheckBox")) tfshape.setSendActionOnDropString(anEvent.getBoolValue());
}

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTextFieldShape.class; }

/**
 * Returns the string for the inspector window title.
 */
public String getWindowTitle()  { return "TextField Inspector"; }

/**
 * Returns a clone of a gallery shape. Default is just a clone deep, be do extra config here on subclass basis.
 */
public RMShape getGalleryClone(T aShape)
{
    // Do normal gallery clone, clear sample text and return
    JTextFieldShape clone = (JTextFieldShape)super.getGalleryClone(aShape);
    clone.setText(null);
    return clone;
}

/**
 * Declare LabeledShapes to be SuperSelectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overrides RMTool implementation to...
 */
public void didBecomeSuperSelectedShapeInEditor(RMShape aShape, RMEditor anEditor)
{
    // Do normal didBecomeSuperSelected
    super.didBecomeSuperSelectedShapeInEditor(aShape, anEditor);
    
    // Get JTextFieldShape and text shape
    JTextFieldShape lshape = (JTextFieldShape)aShape;
    RMTextShape tshape = new RMTextShape(); tshape.setBounds(lshape.getBoundsInside());
    if(lshape.getText()!=null) tshape.setText(lshape.getText());
    tshape.setFont(lshape.getFont());
    tshape.setName(lshape.getName());
    tshape.setAlignmentX(lshape.getAlignmentX());
    tshape.setAlignmentY(lshape.getAlignmentY());
    
    // Set TextShape as child of TextComponentShape and super-select
    lshape.undoerDisable();
    lshape.addChild(tshape);
    lshape.undoerEnable();
    anEditor.setSuperSelectedShape(tshape);
    
    // Clear labeled text
    lshape.setText("");
}

/**
 * Overrides RMTool implementation to...
 */
public void willLoseSuperSelectionInEditor(RMShape aShape, RMEditor anEditor)
{
    // Do normal willLoseSuperSelection
    super.willLoseSuperSelectionInEditor(aShape, anEditor);
    
    // Get JTextFieldShape and text shape
    JTextFieldShape lshape = (JTextFieldShape)aShape;
    RMTextShape tshape = (RMTextShape)lshape.getChild(0);
    
    // Set text, font and alignment in TextComponentShape
    lshape.setText(tshape.getText());
    lshape.setFont(tshape.getFont());
    lshape.setName(tshape.getName());
    lshape.setAlignmentX(tshape.getAlignmentX());
    lshape.setAlignmentY(tshape.getAlignmentY());

    // Remove TextShape as child of TextComponentShape
    lshape.undoerDisable();
    lshape.removeChildren();
    lshape.undoerEnable();
}

}