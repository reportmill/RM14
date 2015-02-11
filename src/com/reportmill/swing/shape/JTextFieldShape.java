package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JTextField.
 */
public class JTextFieldShape extends JTextComponentShape {

    // Alignment horizontal
    int                _alignH = SwingConstants.LEADING;

/**
 * Returns the HorizontalAlignment.
 */
public int getHorizontalAlignment()  { return _alignH; }

/**
 * Sets the HorizontalAlignment.
 */
public void setHorizontalAlignment(int aValue)
{
    firePropertyChange("HorizontalAlignment", _alignH, _alignH = aValue, -1);
}

/**
 * Returns the horizontal alignment.
 */
public AlignX getAlignmentX()
{
    int ha = getHorizontalAlignment();
    switch(ha) {
        case SwingConstants.LEADING: return AlignX.Left;
        case SwingConstants.LEFT: return AlignX.Left;
        case SwingConstants.RIGHT: return AlignX.Right;
        default: return AlignX.Center;
    }
}

/**
 * Sets the alignment x.
 */
public void setAlignmentX(AlignX anAlignX)
{
    switch(anAlignX) {
        case Left: setHorizontalAlignment(SwingConstants.LEFT); break;
        case Center: setHorizontalAlignment(SwingConstants.CENTER); break;
        case Right: setHorizontalAlignment(SwingConstants.RIGHT); break;
    }
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JTextField.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Do textfield attributes
    JTextField tfield = (JTextField)aComp;
    tfield.setHorizontalAlignment(getHorizontalAlignment());
}

/**
 * Declare this for editing.
 */
public boolean childrenSuperSelectImmediately()  { return true; }

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive text component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver);
    e.setName(this instanceof JPasswordFieldShape? "jpasswordfield" : "jtextfield");
    
    // Archive horizontal alignment
    if(getHorizontalAlignment()!=SwingConstants.LEFT)
        e.add("align", LabeledShape.getHorizontalAlignmentString(getHorizontalAlignment()));

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive text component attributes
    super.fromXMLShape(anArchiver, anElement);

    // Unarchive horizontal alignment
    setHorizontalAlignment(LabeledShape.getHorizontalAlignmentInt(anElement.getAttributeValue("align", "left")));
}

}