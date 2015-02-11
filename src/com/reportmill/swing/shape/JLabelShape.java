package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JLabel.
 */
public class JLabelShape extends LabeledShape {
  
/**
 * Returns a mapped property name.
 */
public String getPropertyNameMapped(String aName)
{
    if(aName.equals("Value")) return "Text"; // Remap Value to Text
    return super.getPropertyNameMapped(aName);
}

/**
 * Returns the default HorizontalAlignment.
 */
public int getHorizontalAlignmentDefault()  { return SwingConstants.LEFT; }

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JLabel.class; }

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jlabel");
    return e;
}

}