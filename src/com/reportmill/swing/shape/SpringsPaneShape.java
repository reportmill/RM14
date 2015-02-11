package com.reportmill.swing.shape;
import com.reportmill.shape.RMSpringLayout;
import javax.swing.JComponent;
import snap.swing.SpringsPane;
import snap.util.*;

/**
 * JComponentShape subclass for SpringsPane.
 */
public class SpringsPaneShape extends JComponentShape {

    // The Window XML element
    XMLElement      _winXML;

/**
 * Creates a new SpringsPaneShape.
 */
public SpringsPaneShape()  { setLayout(new RMSpringLayout()); }

/**
 * Overrides shape implementation to declare panel shape super selectable.
 */
public boolean superSelectable()  { return true; }

/**
 * Overrides shape implementation to declare panel shape accepts children.
 */
public boolean acceptsChildren()  { return true; }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return SpringsPane.class; }

/**
 * XML Archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("panel");
    if(_winXML!=null) e.add(_winXML);
    return e;
}

/**
 * XML Unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLShape(anArchiver, anElement);
    _winXML = anElement.getElement("window");
}

}