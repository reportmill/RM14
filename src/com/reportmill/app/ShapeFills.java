package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.util.List;
import snap.swing.*;

/**
 * This class provides UI for editing the currently selected shapes stroke, fill, effect, transparency.
 */
public class ShapeFills extends SwingOwner {
    
/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get array of known stroke names and initialize StrokeComboBox
    RMFillTool ftool = RMFillTool.getTool(); int scount = ftool.getStrokeCount();
    Object snames[] = new String[scount]; for(int i=0;i<scount;i++) snames[i] = ftool.getStroke(i).getName();
    setNodeItems("StrokeComboBox", snames);
    
    // Get array of known fill names and initialize FillComboBox
    int fcount = ftool.getFillCount();
    Object fnames[] = new String[fcount]; for(int i=0;i<fcount;i++) fnames[i] = ftool.getFill(i).getName();
    setNodeItems("FillComboBox", fnames);
    
    // Get array of known effect names and initialize EffectComboBox
    RMEffectTool etool = RMEffectTool.getTool(); int ecount = etool.getEffectCount();
    Object enames[] = new String[ecount]; for(int i=0;i<ecount;i++) enames[i] = etool.getEffect(i).getName();
    setNodeItems("EffectComboBox", enames);
}

/**
 * Reset UI controls from current selection.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
    int tabPaneIndex = getNodeSelectedIndex("TabPane");
    
    // If Stroke tab is showing, ensure proper inspector is showing and forward on
    if(tabPaneIndex==0) {
        
        // Get stroke from shape (or default, if not available)
        RMStroke stroke = shape.getStroke(); if(stroke==null) stroke = new RMStroke();

        // Update StrokeCheckBox, StrokeComboBox
        setNodeValue("StrokeCheckBox", shape.getStroke()!=null);
        setNodeValue("StrokeComboBox", stroke.getName());
        
        // Get stroke tool, install tool UI in stroke panel and ResetUI
        RMFillTool tool = RMFillTool.getTool(stroke);
        setNodeChildren("StrokePanel", tool.getUI());
        tool.resetLater();
    }
    
    // If Fill tab is showing, ensure proper inspector is showing and forward on
    else if(tabPaneIndex==1) {
        
        // Get fill from shape (or default, if not available)
        RMFill fill = shape.getFill(); if(fill==null) fill = new RMFill();

        // Update FillCheckBox, FillComboBox
        setNodeValue("FillCheckBox", shape.getFill()!=null);
        setNodeValue("FillComboBox", fill.getName());
        
        // Get fill tool, install tool UI in fill panel and ResetUI
        RMFillTool tool = RMFillTool.getTool(fill);
        setNodeChildren("FillPanel", tool.getUI());
        tool.resetLater();
    }
    
    // If Effect tab is showing, ensure proper inspector is showing and forward on
    else if(tabPaneIndex==2) {
        
        // Get effect from shape (or default, if not available)
        RMEffect effect = shape.getEffect(); if(effect==null) effect = new RMShadowEffect();

        // Update EffectCheckBox, EffectComboBox
        setNodeValue("EffectCheckBox", shape.getEffect()!=null);
        setNodeValue("EffectComboBox", effect.getName());
        
        // Get effect tool, install tool UI in effect panel and ResetUI
        RMEffectTool tool = RMEffectTool.getTool(effect);
        setNodeChildren("EffectPanel", tool.getUI());
        tool.resetLater();
    }
    
    // Get shape "transparency" - convert opacity (0 to 1) to transparency (0 to 100)
    float transparency = 100 - shape.getOpacity()*100;
    
    // Update TransparencySlider and TransparencyText
    setNodeValue("TransparencySlider", transparency);
    setNodeValue("TransparencyText", transparency);
}

/**
 * Updates currently selected shapes from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the current editor and currently selected shapes list (just return if null)
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    RMShape shape = editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
    List <RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    int tabPaneIndex = getNodeSelectedIndex("TabPane");
    
    // If Stroke tab is showing, handle basic StrokePanel stuff
    if(tabPaneIndex==0) {
        
        // Handle StrokeCheckBox: Iterate over shapes and add stroke if not there or remove if there
        if(anEvent.equals("StrokeCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(RMShape s : shapes) {
                if(selected && s.getStroke()==null) s.setStroke(new RMStroke()); // If requested and missing, add
                if(!selected && s.getStroke()!=null) s.setStroke(null); // If turned off and present, remove
            }
        }
        
        // Handle StrokeComboBox: Get selected stroke instance and iterate over shapes and add stroke if not there
        if(anEvent.equals("StrokeComboBox")) {
            RMStroke newStroke = RMFillTool.getTool().getStroke(anEvent.getSelectedIndex());
            for(RMShape s : shapes) s.setStroke(newStroke.clone());
        }
    }

    // If Fill tab is showing, handle basic FillPanel stuff
    else if(tabPaneIndex==1) {
        
        // Handle FillCheckBox: Iterate over shapes and add fill if not there or remove if there
        if(anEvent.equals("FillCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(RMShape s : shapes) {
                if(selected && s.getFill()==null) s.setFill(new RMFill()); // If requested and missing, add
                if(!selected && s.getFill()!=null) s.setFill(null); // If turned off and present, remove
            }
        }
        
        // Handle FillComboBox: Get selected fill instance and iterate over shapes and add fill if not there
        if(anEvent.equals("FillComboBox")) {
            RMFill newFill = RMFillTool.getTool().getFill(anEvent.getSelectedIndex());
            for(RMShape s : shapes) s.setFill(newFill.deriveFill(s.getFill()));
        }
    }
    
    // If Effect tab is showing, handle basic EffectPanel stuff
    else if(tabPaneIndex==2) {
        
        // Handle EffectCheckBox: Iterate over shapes and add effect if not there or remove if there
        if(anEvent.equals("EffectCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(RMShape s : shapes) {
                if(selected && s.getEffect()==null) s.setEffect(new RMShadowEffect()); // If requested and missing, add
                if(!selected && s.getEffect()!=null) s.setEffect(null); // If turned off and present, remove
            }
        }
        
        // Handle EffectComboBox: Get selected effect instance and iterate over shapes and add effect if not there
        if(anEvent.equals("EffectComboBox")) {
            RMEffect newEffect = RMEffectTool.getTool().getEffect(anEvent.getSelectedIndex());
            for(RMShape s : shapes) s.setEffect(newEffect.clone());
        }
    }
    
    // Handle Transparency Slider and Text
    if(anEvent.equals("TransparencySlider") || anEvent.equals("TransparencyText")) {
        shape.undoerSetUndoTitle("Transparency Change");
        float value = 1 - anEvent.getFloatValue()/100;
        for(RMShape s : shapes)
            s.setOpacity(value);
    }
}

/**
 * Returns the display name for the inspector.
 */
public String getWindowTitle()  { return "Paint/Fill Inspector"; }

}