package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.util.*;
import snap.swing.*;

/**
 * Provides a tool for editing RMFills.
 */
public class RMEffectTool extends SwingOwner {

    // Map of tool instances by shape class
    static Map       _tools = new Hashtable();
    
    // List of known effects
    static RMEffect  _effects[] = { new RMShadowEffect(), new RMReflectionEffect(), new RMBlurEffect(),
        new RMEmbossEffect(), new RMChiselEffect() };
    
/**
 * Returns the currently active editor.
 */
public RMEditor getEditor()  { return RMEditor.getMainEditor(); }

/**
 * Returns the number of known effects.
 */
public int getEffectCount()  { return _effects.length; }

/**
 * Returns an individual effect at given index.
 */
public RMEffect getEffect(int anIndex)  { return _effects[anIndex]; }

/**
 * Returns the currently selected shape's effect.
 */
public RMEffect getSelectedEffect()
{
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
    return shape.getEffect();
}

/**
 * Iterate over editor selected shapes and set fill.
 */
public void setSelectedEffect(RMEffect anEffect)
{
    RMEditor editor = RMEditor.getMainEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = editor.getSelectedOrSuperSelectedShape(i);
        shape.setEffect(i==0? anEffect : anEffect.clone());
    }
}

/**
 * Returns the shared base tool instance.
 */
public static RMEffectTool getTool()  { return getTool(RMEffect.class); }

/**
 * Returns the specific tool for a given shape.
 */
public static RMEffectTool getTool(Object anObj)
{
    // Get tool from tools map - just return if present
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    RMEffectTool tool = (RMEffectTool)_tools.get(cls);
    if(tool==null)
        _tools.put(cls, tool=getToolImpl(cls));
    return tool;
}

/**
 * Returns the specific tool for a given effect.
 */
static RMEffectTool getToolImpl(Class aClass)
{
    // Get class name
    String cname = aClass.getSimpleName();
    
    // Declare variable for tool class
    Class tclass = null;
    
    // If shape class starts with RM, check panels package for built-in effect tools
    if(cname.startsWith("RM"))
        tclass = RMClassUtils.getClassForName("com.reportmill.apptools." + cname + "Tool");
    
    // If not found, try looking in same package for shape class plus "Tool"
    if(tclass==null)
        tclass = RMClassUtils.getClassForName(aClass.getName() + "Tool", aClass);
    
    // If not found, try looking for inner class named "Tool"
    if(tclass==null)
        tclass = RMClassUtils.getClassForName(aClass.getName() + "$" + "Tool", aClass);
    
    // If tool class found, instantiate tool class
    if(tclass!=null)
        try { return (RMEffectTool)tclass.newInstance(); }
        catch(Exception ie) { ie.printStackTrace(); }
        
    // Otherwise, get tool for super class
    return getTool(aClass.getSuperclass());
}

}