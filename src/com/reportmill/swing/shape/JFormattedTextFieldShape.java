package com.reportmill.swing.shape;
import java.text.*;
import javax.swing.*;
import javax.swing.text.*;
import snap.util.*;

/**
 * An RMShape subclass for JFormattedTextField.
 */
public class JFormattedTextFieldShape extends JTextFieldShape {

    //
    DefaultFormatterFactory _factory;

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JFormattedTextField.class; }

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive text component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jformattedtextfield");
    
    // It would be good to archive out the formatter here, but of course that would involve creating xml archival
    // for all the FormatterFactories & Formatters. An interesting alternative may be to use the java beans api.
    // If the formatters and such already know how to write themselves out via the XMLEncoder and the bean package,
    // why reinvent the wheel?

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Let superclass do most of the work
    super.fromXMLShape(anArchiver, anElement);
    
    // Check for presence of a specific format.
    if(anElement.hasAttribute("format")) {
        String kind = anElement.getAttributeValue("format");
        Format format = null;

        if(kind.equals("number")) format = NumberFormat.getNumberInstance();
        else if(kind.equals("integer")) format = NumberFormat.getIntegerInstance();
        else if(kind.equals("currency")) format = NumberFormat.getCurrencyInstance();
        else if(kind.equals("date")) format = DateFormat.getDateInstance();
        else if(kind.equals("time")) format = DateFormat.getTimeInstance();
        else if(kind.equals("datetime")) format = DateFormat.getDateTimeInstance();

        // Get the right formatter for the given format
        JFormattedTextField.AbstractFormatter formatter = null;
        if(format instanceof NumberFormat) formatter = new NumberFormatter((NumberFormat)format);
        else if(format instanceof DateFormat) formatter = new DateFormatter((DateFormat)format);

        // And then a formatterFactory (give me a fucking break)
        _factory = new DefaultFormatterFactory(formatter);
    }
}

}