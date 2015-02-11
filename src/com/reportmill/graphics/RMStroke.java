package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import snap.util.*;

/**
 * An RMFill subclass specifically designed to describe strokes.
 */
public class RMStroke extends RMFill {
    
    // The stroke width
    float        _width = 1;
    
    // The dash array
    float        _dashArray[];
    
    // The dash phase
    float        _dashPhase = 0;
    
    // The java2d stroke
    Stroke       _stroke;

/**
 * Creates a plain, black stroke.
 */
public RMStroke()  { }

/**
 * Creates a stroke with the given color and line width.
 */
public RMStroke(RMColor aColor, float aStrokeWidth)  { _color = aColor; _width = aStrokeWidth; }

/**
 * Returns the line width of this stroke.
 */
public float getWidth()  { return _width; }

/**
 * Sets the line width of this stroke.
 */
public void setWidth(float aValue)
{
    if(aValue==getWidth()) return; // If value already set, just return
    firePropertyChange("Width", _width, _width = aValue, -1); // Set value and fire PropertyChange
    _stroke = null;
}

/**
 * Returns the dash array for this stroke.
 */
public float[] getDashArray()  { return _dashArray; }

/**
 * Sets the dash array for this stroke.
 */
public void setDashArray(float[] anArray)
{
    if(RMArrayUtils.equals(anArray, _dashArray)) return; // If value already set, just return
    firePropertyChange("DashArray", _dashArray, _dashArray = anArray, -1); // Set value and fire PropertyChange
    _stroke = null;
}

/**
 * Returns the dash array for this stroke as a string.
 */
public String getDashArrayString()  { return getDashArrayString(getDashArray(), ", "); }

/**
 * Sets the dash array for this stroke from a string.
 */
public void setDashArrayString(String aString)  { setDashArray(getDashArray(aString, ",")); }

/**
 * Returns a dash array for given dash array string and delimeter.
 */
public static float[] getDashArray(String aString, String aDelimeter)
{
    // Just return null if empty
    if(aString==null || aString.length()==0) return null;
    
    String dashStrings[] = aString.split(",");
    float dashArray[] = new float[dashStrings.length];
    for(int i=0; i<dashStrings.length; i++) dashArray[i] = RMUtils.floatValue(dashStrings[i]);
    return dashArray;
}

/**
 * Returns the dash array for this stroke as a string.
 */
public static String getDashArrayString(float dashArray[], String aDelimiter)
{
    // Just return null if empty
    if(dashArray==null || dashArray.length==0) return null;
    
    // Build dash array string
    String dashArrayString = RMUtils.stringValue(dashArray[0]);
    for(int i=1; i<dashArray.length; i++) dashArrayString += aDelimiter + RMUtils.stringValue(dashArray[i]);
    
    // Return dash array string
    return dashArrayString;
}

/**
 * Returns the dash phase.
 */
public float getDashPhase()  { return _dashPhase; }

/**
 * Sets the dash phase.
 */
public void setDashPhase(float aValue)
{
    firePropertyChange("DashPhase", _dashPhase, _dashPhase = Math.max(aValue, 0), -1); _stroke = null;
}

/**
 * Overrides fill version to clear java 2d stroke.
 */
public void setColor(RMColor aColor)  { super.setColor(aColor); _stroke = null; }

/**
 * Returns the java2d stroke to be used.
 */
protected Stroke getStroke()
{
    // If stroke isn't set, set it
    if(_stroke==null)
        _stroke = new BasicStroke(getWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10,
            getDashArray(), getDashPhase());

    // Return stroke
    return _stroke;
}

/**
 * Returns the path to be stroked, transformed from the input path.
 */
public Shape getStrokePath(RMShape aShape)  { return aShape.getPathInBounds(); }

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)
{
    // Get bounds inset by stroke width
    RMRect bounds = aShape.getBoundsInside().insetRect(-getWidth()/2);
    return bounds;
}

/**
 * Render this fill in a shape painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    aPntr.setColor(getColor().awt()); // Set fill color
    aPntr.setStroke(getStroke()); // Set fill stroke
    aPntr.draw(getStrokePath(aShape)); // Draw path
}

/**
 * Returns the name of the fill.
 */
public String getName()
{
    if(getClass()==RMStroke.class) return "Stroke"; // If class is base stroke, just return Stroke
    String name = super.getName(); // Get normal name
    if(name.endsWith("Stroke")) name = name.substring(0, name.length()-6); // If name ends with Stroke, strip it
    return name; // Return name
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, superclass and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMStroke other = (RMStroke)anObj;
    
    // Check Width, DashArray, DashPhase
    if(!RMMath.equals(other._width, _width)) return false;
    if(!RMArrayUtils.equals(other._dashArray, _dashArray)) return false;
    if(other._dashPhase!=_dashPhase) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public RMStroke clone()  { return (RMStroke)super.clone(); }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic fill attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("stroke");
    
    // Archive Width, DashArray, DashPhase
    if(_width!=1) e.add("width", _width);
    if(getDashArrayString()!=null && getDashArrayString().length()>0) e.add("dash-array", getDashArrayString());
    if(getDashPhase()!=0) e.add("dash-phase", getDashPhase());
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic fill attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive Width, DashArray, DashPhase
    if(anElement.hasAttribute("width")) setWidth(anElement.getAttributeFloatValue("width", 1));
    else if(anElement.hasAttribute("linewidth")) setWidth(anElement.getAttributeFloatValue("linewidth", 1));
    if(anElement.hasAttribute("dash-array")) setDashArrayString(anElement.getAttributeValue("dash-array"));
    if(anElement.hasAttribute("dash-phase")) setDashPhase(anElement.getAttributeFloatValue("dash-phase"));
    return this;
}

}