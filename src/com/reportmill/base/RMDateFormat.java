package com.reportmill.base;
import com.reportmill.shape.RMDocument;
import java.util.*;
import java.text.*;
import snap.util.*;

/**
 * This is just a SimpleDateFormat subclass to support RM archiving and legacy formats.
 */
public class RMDateFormat extends SimpleDateFormat implements RMFormat {
    
    // The string to be substituted if asked to format null
    String       _nullString = "<N/A>";
    
    // The local of the format
    Locale       _locale;
    
    // Shared common formats
    public static RMDateFormat BASIC = new RMDateFormat("MM/dd/yyyy");
    public static RMDateFormat DEFAULT = new RMDateFormat("MMM dd, yyyy");
    public static RMDateFormat defaultFormat = DEFAULT;

/**
 * Creates a plain format.
 */
public RMDateFormat()  { }

/**
 * Creates a format from the given string format.
 */
public RMDateFormat(String aFormat)  { setFormatString(aFormat); }

/**
 * Returns the String that is substituted when this format is asked to provide stringForObjectValue(null).
 */
public String getNullString()  { return _nullString; }

/**
 * Sets the String that is substituted when this format is asked to provide stringForObjectValue(null).
 */
public void setNullString(String aString)  { _nullString = aString; }

/**
 * Returns the date format string.
 */
public String getFormatString()  { return toPattern(); }

/**
 * Sets the date format string. Has support for legacy RM formats and Java style.
 */
public void setFormatString(String aFormat)  { applyPattern(aFormat); }

/**
 * Formats the given object.
 */
public Object formatRM(Object obj) 
{
    // If locale hasn't been set, get it from RMDocument locale
    if(_locale != RMDocument._locale) {
        _locale = RMDocument._locale;
        setDateFormatSymbols(new DateFormatSymbols(RMDocument._locale));
    }

    // If object is date, return date format
    if(obj instanceof Date)
        return format((Date)obj);
    
    // If object isn't date, just return null string
    return _nullString;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Return true if given object is this format
    if(anObj==this) return true;
    
    // Return false if given object isn't a format
    if(!getClass().isInstance(anObj)) return false;
    
    // Get other date format
    RMDateFormat other = (RMDateFormat)anObj;
    
    // Return false if other format null string isn't equal to this null string
    if(!RMUtils.equals(other._nullString, _nullString)) return false;
    
    // Return result of super equals
    return super.equals(anObj);
}

/** XML archival. */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named format
    XMLElement e = new XMLElement("format");
    
    // Set type to date
    e.add("type", "date");
    
    // Archive pattern
    e.add("pattern", toPattern());
    
    // Archive _nullString
    if(_nullString!=null && _nullString.length()>0)
        e.add("null-string", _nullString);
    
    // Return element
    return e;
}

/** XML unarchival. */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive Pattern
    applyPattern(anElement.getAttributeValue("pattern"));
    
    // Unarchive _nullString
    _nullString = anElement.getAttributeValue("null-string");
    
    // Return this format
    return this;
}

/**
 * Returns string representation of this format.
 */
public String toString()  { return toPattern(); }

}