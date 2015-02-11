/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * A class to provide utility methods for UI components.
 */
public abstract class UIHelper <T> {

    // The list or property names for objects of this helper
    List <String>              _propNames;

/**
 * Returns the name of the given object.
 */
public abstract String getName(T anObj);

/**
 * Returns given component's owner.
 */
public abstract UIOwner getOwner(T anObj);

/**
 * Sets given component's owner.
 */
public abstract void setOwner(T anObj, UIOwner anOwner);

/**
 * Initializes the given object to send Events to target.
 */
public void initUI(T anObj, UIOwner anOwner)
{
    // Set owner
    setOwner(anObj, anOwner);
    
    // Init Bindings (only need this because RMShape.addBinding() doesn't call setHelper()
    for(int i=0, iMax=getBindingCount(anObj); i<iMax; i++) { Binding binding = getBinding(anObj, i);
        binding.setHelper(anOwner.getNodeHelper(binding.getNode())); }
}

/**
 * Initializes the given object and its children to send Events to target.
 */
public void initUIDeep(T anObj, UIOwner anOwner)
{
    // If already initialized, just return
    if(getOwner(anObj)!=null) return;
    
    // Init instance
    initUI(anObj, anOwner);
    
    // Forward to children
    for(int i=0, iMax=getChildCount(anObj); i<iMax; i++) { Object child = getChild(anObj, i);
        getHelper(child).initUIDeep(child, anOwner); }
}

/**
 * Returns the parent object for given object.
 */
public abstract Object getParent(T anObj);

/**
 * Returns the number of children for given object.
 */
public abstract int getChildCount(T anObj);

/**
 * Returns the individual child object for given object and index.
 */
public abstract Object getChild(T anObj, int anIndex);

/**
 * Returns first child with matching name, recursively, depth first.
 */
public Object getChild(T anObj, String aName)
{
    for(int i=0, iMax=getChildCount(anObj); i<iMax; i++) { Object child = getChild(anObj, i);
        UIHelper chpr = getHelper(child); String cname = chpr.getName(child);
        if(aName.equals(cname)) return child;
        if(chpr.getChildCount(child)==0 || chpr.getOwner(child)!=getOwner(anObj)) continue;
        Object child2 = chpr.getChild(child, aName);
        if(child2!=null)
            return child2;
    }
    return null;
}

/**
 * Returns whether a given property name is valid for given object.
 */
public boolean isPropertyName(T anObj, String aPropertyName)
{
    String pname = getPropertyNameMapped(anObj, aPropertyName);
    List pnames = getPropertyNames(anObj);
    return pnames.contains(pname) || (pname!=aPropertyName && pnames.contains(aPropertyName));
}

/**
 * Returns the property names for helper's instance class.
 */
public List <String> getPropertyNames(T anObj)
{
    if(_propNames==null) { _propNames = new ArrayList(); addPropNames(); }
    return _propNames;
}

/**
 * Adds property names for this helper.
 */
protected void addPropNames()  { }

/**
 * Adds given property names to the front of property names list.
 */
protected final void addPropNames(String ... theNames)  { Collections.addAll(_propNames, theNames); }

/**
 * Returns a mapped property name name.
 */
public String getPropertyNameMapped(T anObj, String aPropertyName)  { return aPropertyName; }

/**
 * Returns the action for a node.
 */
public String getAction(T anObj)  { return null; }

/**
 * Sets the action for a node.
 */
public void setAction(T anObj, String anAction)  { throw new RuntimeException("SetAction not supported "); }

/**
 * Returns the number of bindings associated with given UI node.
 */
public int getBindingCount(T anObj)  { return 0; }

/**
 * Returns the individual binding at the given index for given UI node.
 */
public Binding getBinding(T anObj, int anIndex)  { return null; }

/**
 * Returns the individual binding for the given property name.
 */
public Binding getBinding(T anObj, String aPropertyName)
{
    // Iterate over bindings and if we find one for given property name, return it
    for(int i=0, iMax=getBindingCount(anObj); i<iMax; i++)
        if(getBinding(anObj, i).getPropertyName().equals(aPropertyName))
            return getBinding(anObj, i);
    
    // If property name is mapped, try again
    String mappedName = getPropertyNameMapped(anObj, aPropertyName);
    if(!aPropertyName.equals(mappedName))
        return getBinding(anObj, mappedName);
    
    // Return null since binding with property name not found
    return null;
}

/**
 * Adds the individual binding at the given index to given UI node.
 */
public void addBinding(T anObj, Binding aBinding)  { }

/**
 * Removes the binding at the given index from given UI node.
 */
public Binding removeBinding(T anObj, int anIndex)  { return null; }

/**
 * Removes the binding with given property name from given UI node.
 */
public boolean removeBinding(T anObj, String aPropertyName)
{
    // Iterate over binding and remove given binding
    for(int i=0, iMax=getBindingCount(anObj); i<iMax; i++) { Binding binding = getBinding(anObj, i);
        if(binding.getPropertyName().equals(aPropertyName)) {
            removeBinding(anObj, i); return true; }
    }
    
    // Return false since binding not found
    return false;
}

/**
 * Returns an object's value for given property name.
 */
public Object getValue(T anObj, String aPropertyName)
{
    // Map property name
    String pname = getPropertyNameMapped(anObj, aPropertyName);

    // Handle Enabled
    if(pname.equals("Enabled"))
        return isEnabled(anObj);
    
    // Handle Items
    else if(pname.equals("Items"))
        return getItems(anObj);
    
    // Handle SelectedItem
    else if(pname.equals("SelectedItem"))
        return getSelectedItem(anObj);
    
    // Handle SelectedIndex
    else if(pname.equals("SelectedIndex"))
        return getSelectedIndex(anObj);
    
    // Use key chain evaluator to get value
    return getKeyValue(anObj, pname);
}

/**
 * Sets an object's value for given property name.
 */
public void setValue(T anObj, String aPropertyName, Object aValue)
{
    // Map property name
    String pname = getPropertyNameMapped(anObj, aPropertyName);
    
    // Handle Enabled
    if(pname.equals("Enabled"))
        setEnabled(anObj, SnapUtils.boolValue(aValue));
    
    // Handle Items
    else if(pname.equals("Items")) {
        if(aValue instanceof List) setItems(anObj, (List)aValue);
        else if(aValue!=null && aValue.getClass().isArray()) setItems(anObj, (Object[])aValue);
        else setItems(anObj, Collections.emptyList());
    }
        
    // Handle SelectedItem
    else if(pname.equals("SelectedItem"))
        setSelectedItem(anObj, aValue);
    
    // Handle SelectedIndex
    else if(pname.equals("SelectedIndex")) {
        int index = aValue==null? -1 : SnapUtils.intValue(aValue);
        setSelectedIndex(anObj, index);
    }
    
    // Set value with key
    else setKeyValue(anObj, pname, aValue);
}

/**
 * Returns the text property of given object.
 */
public String getText(T anObj)  { return SnapUtils.stringValue(getValue(anObj, "Text")); }

/** 
 * Sets the text property of given object to given string.
 */
public void setText(T anObj, String aString)  { if(anObj!=null) setKeyValue(anObj, "Text", aString); }

/**
 * Returns the items for an object.
 */
public List getItems(T anObj)  { return (List)getValue(anObj, "Items"); }

/**
 * Sets the items for an object.
 */
public void setItems(T anObj, List theItems)  { setKeyValue(anObj, "Items", theItems); }

/**
 * Sets the items for an object.
 */
public void setItems(T anObj, Object theItems[])  { setItems(anObj, Arrays.asList(theItems)); }

/**
 * Sets the display key for UI node item.
 */
public String getItemDisplayKey(T anObj)  { return null; }

/**
 * Sets the display key for UI node item.
 */
public void setItemDisplayKey(T anObj, String aKey)
{ System.err.println(getClass().getName() + ".setItemDisplayKey: Not implemented"); }

/**
 * Returns the selected index property of given object.
 */
public int getSelectedIndex(T anObj)  { return SnapUtils.intValue(getKeyValue(anObj, "SelectedIndex")); }

/**
 * Sets the selected index property of given object to given value.
 */
public void setSelectedIndex(T anObj, int anIndex)  { setKeyValue(anObj, "SelectedIndex", anIndex); }

/**
 * Returns the selected index property of given object.
 */
public int[] getSelectedIndexs(T anObj)  { return (int[])getKeyValue(anObj, "SelectedIndexes");  }

/**
 * Sets the selected index property of given object to given value.
 */
public void setSelectedIndexes(T anObj, int theIndexes[])  { setKeyValue(anObj, "SelectedIndexes", theIndexes); }

/**
 * Returns the selected object property of given object.
 */
public Object getSelectedItem(T anObj)  { return getKeyValue(anObj, "SelectedItem"); }

/**
 * Sets the selected object property of given object to given value.
 */
public void setSelectedItem(T anObj, Object aValue)  { setKeyValue(anObj, "SelectedItem", aValue); }

/**
 * Returns whether UI node value is adjusting.
 */
public boolean isValueAdjusting(T anObj)  { return SnapUtils.boolValue(getKeyValue(anObj, "ValueIsAdjusting")); }

/**
 * Returns whether given UI node is enabled.
 */
public boolean isEnabled(T anObj)  { return SnapUtils.boolValue(getKeyValue(anObj, "Enabled")); }

/**
 * Sets whether given UI node is enabled.
 */
public void setEnabled(T anObj, boolean aValue)  { setKeyValue(anObj, "Enabled", aValue); }

/**
 * Returns whether given event is enabled.
 */
public abstract boolean isEnabled(T anObj, UIEvent.Type aType);

/**
 * Returns whether given event is enabled.
 */
public abstract void setEnabled(T anObj, UIEvent.Type aType, boolean aValue);

/**
 * Returns a key value.
 */
protected Object getKeyValue(Object anObj, String aKey)  { return Key.getValue(anObj, aKey); }

/**
 * Sets a KeyValue.
 */
protected void setKeyValue(Object anObj, String aKey, Object aValue)
{
    if(anObj==null) return;
    Key.setValueSafe(anObj, aKey, aValue);
}

/**
 * Called to enable events.
 */
public void enableEvents(T anObj, UIEvent.Type ... theTypes)
{
    for(UIEvent.Type type : theTypes) setEnabled(anObj, type, true);
}

/**
 * Returns the Helper object for a given object.
 */
public abstract UIHelper getHelper(Object anObj);

}