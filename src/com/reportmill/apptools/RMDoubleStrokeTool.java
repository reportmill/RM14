package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * Swing UI editing for RMDoubleStroke.
 */
public class RMDoubleStrokeTool extends RMStrokeTool {

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Do normal version
    super.initUI();
    
    // Fill in images for RuleComboBox
    JComboBox ruleComboBox  = getNode("RuleComboBox", JComboBox.class);
    String imageNames[] = { "inner_on1", "inner_on2", "outer_on2", "centered" };
    for(int i=0,n=imageNames.length; i<n; ++i) {
        Icon icon = Swing.getIcon(imageNames[i] + ".png", RMDoubleStrokeTool.class);
        ruleComboBox.addItem(icon);
    }        
}

/**
 * Called by Ribs to reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    
    // Update StrokeColorWell
    setNodeValue("StrokeColorWell", shape.getStrokeColor().awt());
    
    // Update RuleComboBox
    setNodeSelectedIndex("RuleComboBox", getRuleComboBoxIndex((RMDoubleStroke)shape.getStroke()));    
}

/**
 * Called by Ribs to respond to UI controls
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the current editor, selected shape and shapes (just return if null)
    RMEditor editor = getEditor(); if(editor==null) return;
    RMShape shape = editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
    List <RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();

    // Handle StrokeColorWell - get color and set in selected shapes
    if(anEvent.equals("StrokeColorWell")) {
        RMColor color = new RMColor(anEvent.getColorValue());
        for(RMShape s : shapes)
            s.setStrokeColor(color);
    }
    
    // Handle RuleComboBox
    if(anEvent.equals("RuleComboBox")) {
        
        // Get selected stroke index
        int strokeItemIndex = anEvent.getSelectedIndex();
        
        // Iterate over shapes - get adjusted stroke for selected index and set
        for(RMShape s : shapes) {
            RMStroke stroke = getRuleComboBoxStroke(strokeItemIndex, (RMDoubleStroke)s.getStroke());
            shape.setStroke(stroke);
        }
    }    
}

/**
 * Returns the RuleComboBox index for the given stroke.
 */
public int getRuleComboBoxIndex(RMDoubleStroke hs)
{
    // If not stroke, return 0
    if(hs==null) return 0;
    
    // Switch on rule positions
    switch(hs.getRulePositions()) {
        
        // Handle INNER_RULE_ON_PATH
        case RMDoubleStroke.INNER_RULE_ON_PATH: return hs.getOuterRuleWidth()>1? 1 : 0; 

        // Handle OUTER_RULE_ON_PATH
        case RMDoubleStroke.OUTER_RULE_ON_PATH: return 2;
            
        // Handle CENTERED_ABOUT_PATH
        case RMDoubleStroke.RULES_CENTERED_ABOUT_PATH: return 3;
        
        // Default?
        default: return 0;
    }
}

/**
 * Returns the RuleComboBox adjusted stroke for the given combobox index and an original base stroke.
 */
public RMStroke getRuleComboBoxStroke(int anIndex, RMDoubleStroke aBaseStroke)
{
    // Get stroke local ivar (if null, replace with default stroke) 
    RMStroke stroke = aBaseStroke!=null? aBaseStroke : new RMStroke();
    
    // Return new stroke based on RuleComboBox index
    switch(anIndex) {
        case 0: return new RMDoubleStroke(stroke.getColor(), 1,1,2,RMDoubleStroke.INNER_RULE_ON_PATH);
        case 1: return new RMDoubleStroke(stroke.getColor(), 2,1,2,RMDoubleStroke.INNER_RULE_ON_PATH);
        case 2: return new RMDoubleStroke(stroke.getColor(), 1,2,2,RMDoubleStroke.OUTER_RULE_ON_PATH);
        case 3: return new RMDoubleStroke(stroke.getColor(), 1,2,2,RMDoubleStroke.RULES_CENTERED_ABOUT_PATH);
        default: return null; // Shouldn't reach this point
    }
}

}