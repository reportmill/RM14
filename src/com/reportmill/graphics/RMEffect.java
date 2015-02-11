package com.reportmill.graphics;
import com.reportmill.base.RMRect;
import com.reportmill.shape.*;
import java.beans.*;
import snap.util.*;

/**
 * An RMFill subclass for drawing effects that rely on shape's standard fill/stroke.
 */
public class RMEffect extends SnapObject implements PropertyChangeListener, DeepChangeListener, XMLArchiver.Archivable {

    // Whether this shadow fill is enabled
    boolean         _enabled = true;

/**
 * Returns whether effect is currently enabled.
 */
public boolean isEnabled()  { return _enabled; }

/**
 * Sets whether effect is currently enabled.
 */
public boolean setEnabled(boolean aValue)  { boolean oldValue = _enabled; _enabled = aValue; return oldValue; }

/**
 * Returns the name of the effect.
 */
public String getName()
{
    if(getClass()==RMEffect.class) return "Effect"; // Bogus name for plain color fill
    String name = getClass().getSimpleName(); // Get simple class name
    if(name.startsWith("RM")) name = name.substring(2); // If name starts with RM, strip it
    if(name.endsWith("Effect")) name = name.substring(0, name.length()-6); // Strip trailing "Effect"
    return name;
}

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)  { return aShape.getBoundsInside(); }

/**
 * Render this fill in a Java2D Graphics2D.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)  { aShape.paintShapeAll(aPntr); }

/**
 * Tells the effect to reset.
 */
public void reset()  { }

/**
 * PropertyChangeListener method. Forward to deepChange().
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    String pname = anEvent.getPropertyName();
    if(pname.equals("X") || pname.equals("Y") || pname.equals("Roll")) return;
    reset();
}

/**
 * DeepChangeListener method. When shape or shape child has property change clear cache and/or remove listener.
 */
public void deepChange(PropertyChangeListener aSource, PropertyChangeEvent anEvent)  { reset(); }

/**
 * Standard clone implementation.
 */
public RMEffect clone()  { return (RMEffect)super.clone(); }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)  { XMLElement e = new XMLElement("effect"); return e; }

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }

/**
 * Returns a string representation.
 */
public String toString()  { return getClass().getSimpleName(); }

}