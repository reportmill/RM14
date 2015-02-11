package com.reportmill.swing.shape;
import com.reportmill.graphics.RMColor;
import javax.swing.border.Border;
import snap.util.*;

/**
 * A class to represent Swing borders for JComponentShape.
 */
public abstract class RMBorder implements XMLArchiver.Archivable {

    // The Swing border
    Border       _border;

/**
 * Returns the Swing border.
 */
public Border getBorder()  { return _border!=null? _border : (_border=createBorder()); }

/**
 * Creates the border.
 */
protected abstract Border createBorder();

/**
 * XML unarchival.
 */
public static RMBorder fromXMLBorder(XMLArchiver anArchiver, XMLElement anElement)
{
    String type = anElement.getAttributeValue("type", "");
    RMBorder border = null;
    if(type.equals("line")) border = new LineBorder();
    else if(type.equals("bevel")) border = new BevelBorder();
    else if(type.equals("etched")) border = new EtchedBorder();
    else if(type.equals("empty")) border = new EmptyBorder();
    else border = new NullBorder();
    border.fromXML(anArchiver, anElement);
    String title = anElement.getAttributeValue("title");
    if(title!=null) border = new TitledBorder(title, border);
    return border;
}

/**
 * A subclass for empty border.
 */
private static class NullBorder extends RMBorder {

    /** Creates the LineBorder. */
    protected Border createBorder()  { return null; }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)  { XMLElement e = new XMLElement("border"); return e; }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }
}

/**
 * A subclass for empty border.
 */
public static class EmptyBorder extends RMBorder {

    /** Creates the LineBorder. */
    protected Border createBorder()  { return new javax.swing.border.EmptyBorder(0, 0, 0, 0); }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "empty"); return e;
    }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }
}

/**
 * A subclass for line border.
 */
public static class LineBorder extends RMBorder {

    // The color
    RMColor         _color;
    
    // The width
    double          _width;
    
    /** Creates a new line border. */
    public LineBorder()  { _color = RMColor.black; _width = 1; }
    
    /** Creates a new line border. */
    public LineBorder(RMColor aColor, double aWidth)  { _color = aColor; _width = aWidth; }
    
    /** Creates the LineBorder. */
    protected Border createBorder()  { return new javax.swing.border.LineBorder(_color.awt(), (int)_width); }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "line");
        if(!_color.equals(RMColor.black)) e.add("line-color", '#' + _color.toHexString());
        return e;
    }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        if(anElement.hasAttribute("line-color"))
            _color = new RMColor(anElement.getAttributeValue("line-color"));
        return this;
    }
}

/**
 * A subclass for bevel border.
 */
public static class BevelBorder extends RMBorder {

    // The type
    int _type = 1; int RAISED = 0, LOWERED = 1;

    /** Creates the BevelBorder. */
    protected Border createBorder()  { return new javax.swing.border.BevelBorder(_type); }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "bevel");
        if(_type==RAISED) e.add("bevel-type", "raised");
        return e;
    }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        String type = anElement.getAttributeValue("bevel-type", "lowered");
        if(type.equals("raised")) _type = 0;
        return this;
    }
}

/**
 * A subclass for etched border.
 */
public static class EtchedBorder extends RMBorder {

    // The type
    int _type = 1; int RAISED = 0, LOWERED = 1;

    /** Creates the BevelBorder. */
    protected Border createBorder()  { return new javax.swing.border.EtchedBorder(_type); }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "etched");
        if(_type==RAISED) e.add("etch-type", "raised");
        return e;
    }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        String type = anElement.getAttributeValue("etch-type", "lowered");
        if(type.equals("raised")) _type = RAISED;
        return this;
    }
}

/**
 * A subclass for etched border.
 */
public static class TitledBorder extends RMBorder {

    // The title and other border
    String    _title;
    RMBorder  _border;
    
    /** Creates a new TitledBorder. */
    public TitledBorder(String aTitle, RMBorder aBorder)  { _title = aTitle; _border = aBorder; }

    /** Creates the BevelBorder. */
    protected Border createBorder()
    {
        return new javax.swing.border.TitledBorder(_border.getBorder(), _title);
    }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = _border.toXML(anArchiver); e.add("title", _title); return e;
    }
    
    /** XML Unarchival. */
    public RMBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        throw new RuntimeException("RMBorder.TitledBorder: unarchival shouldn't be possible");
    }
}

}