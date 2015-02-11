package com.reportmill.swing.shape;
import javax.swing.JComponent;
import snap.swing.ColorWell;
import snap.util.*;

/**
 * An RMShape subclass for ColorWell.
 */
public class ColorWellShape extends JComponentShape {

    // Whether selectable
    boolean     _selectable = true;

/**
 * Returns whether selectable.
 */
public boolean isSelectable()  { return _selectable; }

/**
 * Sets whether selectable.
 */
public void setSelectable(boolean aValue)
{
    firePropertyChange("Selectable", _selectable, _selectable = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return ColorWell.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get ColorWell and configure
    ColorWell cwell = (ColorWell)aComp;
    cwell.setSelectable(isSelectable());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("colorwell");
    if(!isSelectable()) e.add("selectable", false);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLShape(anArchiver, anElement);
    if(anElement.hasAttribute("selectable")) setSelectable(anElement.getAttributeBoolValue("selectable"));
}

}