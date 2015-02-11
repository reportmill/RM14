package com.reportmill.shape;
import java.util.*;
import snap.util.UIEvent;

/**
 * A class to listen for Swing events and send them on.
 */
public class RMShapeEventAdapter {

    // The shape
    RMShape           _shape;
    
    // Bit set of enabled events
    BitSet            _bitset = new BitSet();
    
/**
 * Creates a new RMShapeEventAdapter for given shape.
 */
public RMShapeEventAdapter(RMShape aShape)  { _shape = aShape; }

/**
 * Returns the shape.
 */
public RMShape getShape()  { return _shape; }

/**
 * Returns whether given type is enabled.
 */
public boolean isEnabled(UIEvent.Type aType)  { return _bitset.get(aType.ordinal()); }

/**
 * Returns whether any of given types are enabled.
 */
public boolean isEnabled(UIEvent.Type ... theTypes)
{
    boolean enabled = false; for(UIEvent.Type type : theTypes) enabled |= isEnabled(type); return enabled;
}

/**
 * Sets whether a given type is enabled.
 */
public void setEnabled(UIEvent.Type aType, boolean aValue)
{
    // Set bit
    _bitset.set(aType.ordinal(), aValue);
}

/**
 * Returns an array of enabled events.
 */
public UIEvent.Type[] getEnabledEvents()
{
    List <UIEvent.Type> types = new ArrayList();
    for(UIEvent.Type type : UIEvent.Type.values()) if(isEnabled(type)) types.add(type);
    return types.toArray(new UIEvent.Type[types.size()]);
}

/**
 * Sets an array of enabled events.
 */
public void setEnabledEvents(UIEvent.Type ... theTypes)  { for(UIEvent.Type type : theTypes) setEnabled(type, true); }

/**
 * Returns the events string.
 */
public String getEnabledEventsString()
{
    UIEvent.Type events[] = getEnabledEvents(); if(events.length==0) return "";
    StringBuffer sb = new StringBuffer();
    for(UIEvent.Type event : events) sb.append(event).append(",");
    sb.delete(sb.length()-1, sb.length()); // Remove trailing comma
    return sb.toString();
}

/**
 * Sets the events string.
 */
public void setEnabledEventsString(String anEventsString)
{
    String eventStrings[] = anEventsString.split(",");
    for(String event : eventStrings) setEnabled(UIEvent.Type.valueOf(event), true);
}

}