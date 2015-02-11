package com.reportmill.viewer;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.JComponentShape;
import java.util.*;
import snap.swing.SwingHelper;
import snap.util.*;

/**
 * A RibsHelper implementation for RMShape.
 */
public class RMViewerOwnerHpr <T extends RMShape> extends UIHelper <T> {

/**
 * Returns the name of the given shape.
 */
public String getName(T aShape)  { return aShape.getName(); }

/**
 * Returns given display object's owner.
 */
public UIOwner getOwner(T aShape)  { return (UIOwner)aShape.get("RibsOwner"); }

/**
 * Sets given display object's owner.
 */
public void setOwner(T aShape, UIOwner anOwner)  { aShape.put("RibsOwner", anOwner); }

/**
 * Returns the parent object for given object.
 */
public Object getParent(T aShape)  { return aShape.getParent(); }

/**
 * Returns the number of children for given shape.
 */
public int getChildCount(T aShape)
{
    return aShape instanceof RMParentShape? ((RMParentShape)aShape).getChildCount() : 0;
}

/**
 * Returns the individual child object for given display object and index.
 */
public Object getChild(T aShape, int anIndex)  { return ((RMParentShape)aShape).getChild(anIndex); }

/**
 * Returns the property names for given shape.
 */
public List <String> getPropertyNames(T aShape)  { return aShape.getPropertyNames(); }

/**
 * Returns mapped property name for shape.
 */
public String getPropertyNameMapped(T aShape, String aName)
{
    if(aShape instanceof RMTextShape && aName.equals("Value")) return "Text";
    return aShape.getPropertyNameMapped(aName);
}

/**
 * Returns the number of bindings associated with given display object.
 */
public int getBindingCount(T aShape)  { return aShape.getBindingCount(); }

/**
 * Returns the individual binding at the given index for given display object.
 */
public Binding getBinding(T aShape, int anIndex)  { return aShape.getBinding(anIndex); }

/**
 * Adds the individual binding at the given index to given display object.
 */
public void addBinding(T aShape, Binding aBinding)
{
    aBinding.setHelper(this);
    aShape.addBinding(aBinding);
}

/**
 * Removes the binding at the given index from given display object.
 */
public Binding removeBinding(T aShape, int anIndex)  { return aShape.removeBinding(anIndex); }

/**
 * Override to call RMShape.repaint().
 */
public void setValue(T aShape, String aPropertyName, Object aValue)
{
    aShape.repaint();
    super.setValue(aShape, aPropertyName, aValue);
}

/**
 * Returns whether given event is enabled.
 */
public boolean isEnabled(T aShape, UIEvent.Type aType)  { return aShape.isEnabled(aType); }

/**
 * Sets whether given event is enabled.
 */
public void setEnabled(T aShape, UIEvent.Type aType, boolean aValue)  { aShape.setEnabled(aType, aValue); }

/**
 * Return helper.
 */
public UIHelper getHelper(Object anObj)  { return GetHelper(anObj); }

/**
 * Override.
 */
public static UIHelper GetHelper(Object anObj)
{
    if(anObj instanceof JComponentShape) return _jcsh;
    if(anObj instanceof RMShape) return _sh;
    return SwingHelper.getSwingHelper(anObj);
}
static UIHelper _sh = new RMViewerOwnerHpr(), _jcsh = new RMViewerOwnerJHpr();

}