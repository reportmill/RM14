package com.reportmill.swing.shape;
import com.reportmill.base.*;
import javax.swing.*;
import snap.util.*;

/**
 * This class provides a RM shape/inspector for editing JSeparator.
 */
public class JSeparatorShape extends JComponentShape {

    // The orientation
    int       _orient = SwingConstants.HORIZONTAL;

/**
 * Returns the orientation of the JSeparator.
 */
public int getOrientation()  { return _orient; }

/**
 * Resets the orientation of the separator component and resizes the shape.
 */
public void setOrientation(int aValue)
{
    firePropertyChange("Orientation", _orient, _orient = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JSeparator.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    super.configureComponent(aBldr, aComp);
    ((JSeparator)aComp).setOrientation(getOrientation());
}

/**
 * Override from RMShape to increase hit test rectangle.
 */
public boolean contains(RMPoint aPoint)
{
    RMRect bounds = getBoundsInside(); 
    if(getOrientation() == JSeparator.HORIZONTAL) bounds.inset(0, -5);
    else bounds.inset(-5, 0);
    return aPoint.inRect(bounds);
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Do normal component archival and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jseparator");
    if(getOrientation()!=SwingConstants.HORIZONTAL) e.add("orientation", getOrientation());
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    if(anElement.hasAttribute("orientation")) setOrientation(anElement.getAttributeIntValue("orientation"));
}

}