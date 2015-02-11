package com.reportmill.graphics;
import com.reportmill.base.RMRect;
import com.reportmill.shape.*;
import snap.util.*;

/**
 * This class represents a simple shape fill, drawing a given color in a provided path. Subclasses support things
 * like gradients, textures, etc.
 */
public class RMFill extends SnapObject implements XMLArchiver.Archivable {

    // Fill color
    RMColor        _color = RMColor.black;

/**
 * Creates a plain, black fill.
 */
public RMFill()  { }

/**
 * Creates a plain fill with the given color.
 */
public RMFill(RMColor aColor)  { setColor(aColor); }

/**
 * Returns the name of the fill.
 */
public String getName()
{
    if(getClass()==RMFill.class) return "Color Fill"; // Bogus name for plain color fill
    String name = getClass().getSimpleName(); // Get simple class name
    if(name.startsWith("RM")) name = name.substring(2); // If name starts with RM, strip it
    if(name.endsWith("Fill")) name = name.substring(0, name.length()-4); // If name ends with Fill, strip it
    return name; // Return name
}

/**
 * Returns the color associated with this fill.
 */
public RMColor getColor()  { return _color; }

/**
 * Sets the color associated with this fill.
 */
public void setColor(RMColor aColor)
{
    if(SnapUtils.equals(aColor, _color)) return; // If value already set, just return
    firePropertyChange("Color", _color, _color = aColor, -1); // Set value and fire property change
}

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)  { return aShape.getBoundsInside(); }

/**
 * Render this fill in a Java2D Graphics2D.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // Get shape path
    RMPath path = aShape.getPathInBounds();
            
    // Set color and fill
    aPntr.setColor(getColor().awt());
    aPntr.fill(path);
}

/**
 * Returns whether fill has transparency. 
 */
public boolean hasAlpha()  {  return getColor().getAlphaInt()!=255; }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(anObj==null || anObj.getClass()!=getClass()) return false;
    RMFill other = (RMFill)anObj;
    
    // Check Color
    if(!SnapUtils.equals(other._color, _color)) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public RMFill clone()  { return (RMFill)super.clone(); }

/**
 * Derives an instance of this class from another fill.
 */
public RMFill deriveFill(RMFill aFill)
{
    RMFill clone = clone();
    if(aFill!=null) clone.setColor(aFill.getColor());
    return clone;
}
  
/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = new XMLElement("fill");
    if(!getColor().equals(RMColor.black)) e.add("color", "#" + getColor().toHexString());
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    String color = anElement.getAttributeValue("color");
    if(color!=null) _color = new RMColor(color);
    return this;
}

/**
 * Returns a string representation.
 */
public String toString()
{
    return String.format("%s: { color:%s }", getClass().getSimpleName(), getColor().toHexString());
}

}