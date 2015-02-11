package com.reportmill.swing.shape;
import javax.swing.*;
import snap.swing.ThumbWheel;
import snap.util.*;

/**
 * An RMShape subclass for ThumbWheel.
 */
public class ThumbWheelShape extends JComponentShape {

    // Value
    float            _value = 0;
    
    // Amount to round value to
    float            _round = 0;
    
    // Visible min/max
    float            _visibleMin = 0;
    float            _visibleMax = 100;
    
    // Absolute min/max
    float            _absoluteMin = -Float.MAX_VALUE;
    float            _absoluteMax = Float.MAX_VALUE;
    
    // Absolute mode
    byte             _absoluteMode = ABSOLUTE_BOUNDED;
    
    // Orientation (horizontal, vertical)
    int              _orientation = SwingConstants.HORIZONTAL;
    
    // The type of thumbwheel (radial or linear)
    byte             _type = TYPE_RADIAL;
    
    // Whether value is in the process of changing interactively
    boolean          _valueIsAdjusting = false;

    // Constants for type
    public static final byte TYPE_RADIAL = 0;
    public static final byte TYPE_LINEAR = 1;
    
    // Constants for absolute behavior
    public static final byte ABSOLUTE_BOUNDED = 0;
    public static final byte ABSOLUTE_WRAPPED = 1;

/**
 * Returns the value.
 */
public float getValue()  { return _value; }

/**
 * Sets the value.
 */
public void setValue(float aValue)
{
    firePropertyChange("Value", _value, _value = aValue, -1);
}

/** Returns the value that thumbwheel values are rounded to. */
public float getRound() { return _round; }

/** Sets the value that thumbwheel values are rounded to. */
public void setRound(float aValue) { _round = aValue; }

/** Returns the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
public float getVisibleMin() { return _visibleMin; }

/** Sets the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
public void setVisibleMin(float aValue) { _visibleMin = aValue; }

/** Returns the largest value in the visible range (ie, on the right side) of the thumbhweel. */
public float getVisibleMax() { return _visibleMax; }

/** Sets the largest value in the visible range (ie, on the right side) of the thumbhweel. */
public void setVisibleMax(float aValue) { _visibleMax = aValue; }

/** Returns the smallest value permitted by the thumbwheel (even when outside visible range). */
public float getAbsoluteMin() { return _absoluteMin; }

/** Sets the smallest value permitted by the thumbwheel (even when outside visible range). */
public void setAbsoluteMin(float aValue) { _absoluteMin = aValue; }

/** Returns the largest value permitted by the thumbwheel (even when outside visible range). */
public float getAbsoluteMax() { return _absoluteMax; }

/** Sets the largest value permitted by the thumbwheel (even when outside visible range). */
public void setAbsoluteMax(float aValue) { _absoluteMax = aValue; }

/** Returns the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
public byte getAbsoluteMode() { return _absoluteMode; }

/** Sets the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
public void setAbsoluteMode(byte aValue) { _absoluteMode = aValue; }

/** Returns the orientation (SwingConstants.VERTICAL or SwingConstants.HORIZONTAL). */
public int getOrientation() { return _orientation; }

/** Returns the orientation (SwingConstants.VERTICAL or SwingConstants.HORIZONTAL). */
public void setOrientation(int aValue) { _orientation = aValue; }

/**
 * Returns the type (radial or linear).
 */
public byte getType()  { return _type; }

/**
 * Sets the type (radial or linear).
 */
public void setType(byte aType)  { _type = aType; }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return ThumbWheel.class; }

/**
 * Override to configure attributes for this class.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get thumbwheel and configure
    ThumbWheel tw = (ThumbWheel)aComp;
    tw.setType(getType());
    tw.setVisibleMin(getVisibleMin());
    tw.setVisibleMax(getVisibleMax());
    tw.setAbsoluteMin(getAbsoluteMin());
    tw.setAbsoluteMax(getAbsoluteMax());
    tw.setRound(getRound());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("thumbwheel");
    
    // Archive Type, VisibleMin, VisibleMax, AbsoluteMin, AbsoluteMax and Round
    if(getType()!=ThumbWheel.TYPE_RADIAL) e.add("type", "linear");
    if(getVisibleMin()!=0) e.add("min", getVisibleMin());
    if(getVisibleMax()!=100) e.add("max", getVisibleMax());
    if(getAbsoluteMin()!=-Float.MAX_VALUE) e.add("absmin", getAbsoluteMin());
    if(getAbsoluteMax()!=Float.MAX_VALUE) e.add("absmax", getAbsoluteMax());
    if(getRound()!=0) e.add("round", getRound());
        
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive Type
    if(anElement.getAttributeValue("type", "radial").equals("linear"))
        setType(ThumbWheel.TYPE_LINEAR);
    
    // Unarchive VisibleMin, VisibleMax, AbsoluteMin, AbsoluteMax and Round
    setVisibleMin(anElement.getAttributeFloatValue("min", getVisibleMin()));
    setVisibleMax(anElement.getAttributeFloatValue("max", getVisibleMax()));
    setAbsoluteMin(anElement.getAttributeFloatValue("absmin", getAbsoluteMin()));
    setAbsoluteMax(anElement.getAttributeFloatValue("absmax", getAbsoluteMax()));
    setRound(anElement.getAttributeFloatValue("round"));
}

}