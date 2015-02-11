package com.reportmill.swing.shape;
import javax.swing.*;
import snap.swing.SwingUtils;
import snap.util.*;

/**
 * A JComponentShape for JMenuItem.
 */
public class JMenuItemShape extends AbstractButtonShape {

    // The accelerator string
    String           _key;
    
/**
 * Returns the key string.
 */
public String getAccelerator()  { return _key; }

/**
 * Sets the key string.
 */
public void setAccelerator(String aValue)
{
    firePropertyChange("Accelerator", _key, _key = aValue, -1);
}

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JMenuItem.class; }

/**
 * Override to configure component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    JMenuItem mi = (JMenuItem)aComp;
    if(getName()!=null)  mi.setName(getName());
    if(getText()!=null) mi.setText(getText());
    Icon icon = getIcon(getImageName()); if(icon!=null) mi.setIcon(icon);
    if(getHorizontalAlignment()>=0) mi.setHorizontalAlignment(getHorizontalAlignment());
    if(getIconTextGap()>=0) mi.setIconTextGap(getIconTextGap());
    if(getAccelerator()!=null)
        mi.setAccelerator(SwingUtils.getKeyStroke(getAccelerator()));
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Create element for JMenuItem
    XMLElement e = new XMLElement("jmenuitem");
    
    // Archive Name, Text and Image
    if(getName()!=null && getName().length()>0) e.add("name", getName());
    if(getText()!=null && getText().length()>0) e.add("text", getText());
    if(getImageName()!=null && getImageName().length()>0) e.add("image", getImageName());

    // Archive HorizontalAlignment and IconTextGap
    if(getHorizontalAlignment()>=0) e.add("align", getHorizontalAlignmentString(getHorizontalAlignment()));
    if(getIconTextGap()>=0) e.add("icon-text-gap", getIconTextGap());

    // Archive Accelerator
    if(getAccelerator()!=null && getAccelerator().length()>0) e.add("key", getAccelerator());

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive Name, Text and Image name
    setName(anElement.getAttributeValue("name"));
    setText(anElement.getAttributeValue("text", anElement.getAttributeValue("value")));
    setImageName(anElement.getAttributeValue("image"));
    
    // Unarchive HorizontalAlignment and IconTextGap
    Integer ah = getHorizontalAlignmentInt(anElement.getAttributeValue("align"));
    if(ah!=null) setHorizontalAlignment(ah);
    if(anElement.hasAttribute("icon-text-gap")) setIconTextGap(anElement.getAttributeIntValue("icon-text-gap"));
    
    // Unarchive Accelerator
    String key = anElement.getAttributeValue("key"); if(key!=null) setAccelerator(key);
}

}