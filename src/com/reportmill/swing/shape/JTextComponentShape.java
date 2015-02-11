package com.reportmill.swing.shape;
import com.reportmill.base.*;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import snap.util.*;

/**
 * A shape implementation for JTextComponent.
 */
public class JTextComponentShape extends JComponentShape {

    // The text
    String            _text;

    // Whether text component shape is editable
    boolean           _editable = true;

    // The margins
    Insets            _margin;

/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aString)
{
    firePropertyChange("Text", _text, _text = aString, -1);
}

/**
 * Returns whether Text shape is editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether Text shape is editable.
 */
public void setEditable(boolean aValue)
{
    firePropertyChange("Editable", _editable, _editable = aValue, -1);
}

/**
 * Returns the button margins.
 */
public Insets getMargin()  { return _margin; }

/**
 * Sets the button margins.
 */
public void setMargin(Insets theInsets)
{
    firePropertyChange("Margin", _margin, _margin = theInsets, -1);
}

/**
 * Returns the margin string.
 */
public String getMarginString()  { return _margin!=null? RMAWTUtils.toStringInsets(_margin) : null; }

/**
 * Sets the button margin string.
 */
public void setMarginString(String aString)
{
    Insets insets = aString!=null && aString.length()>0? RMAWTUtils.fromStringInsets(aString) : null;
    setMargin(insets);
}

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Add Text component attributes
    JTextComponent tcomp = (JTextComponent)aComp;
    if(getText()!=null) tcomp.setText(getText());
    tcomp.setEditable(isEditable());
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jtextcomponent");
    
    // Archive text string
    if(getText()!=null && getText().length()>0) e.add("text", getText());
    
    // Archive Editable
    if(!isEditable()) e.add("editable", false);
    
    // Archive Margin
    if(getMargin()!=null) e.add("margin", getMarginString());

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);

    // Unarchive text - Text can be specified as "text" or "value" attribute, or as content (CDATA or otherwise)
    String string = anElement.getAttributeValue("text",  anElement.getAttributeValue("value", anElement.getValue()));
    if(string!=null && string.length()>0)
        setText(string);
    
    // Unarchive isEditable
    if(anElement.getAttribute("editable")!=null)
        setEditable(anElement.getAttributeBoolValue("editable", true));
        
    // Unarchive Margin
    if(anElement.hasAttribute("margin"))
        setMarginString(anElement.getAttributeValue("margin"));
}

}