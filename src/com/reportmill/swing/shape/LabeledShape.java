package com.reportmill.swing.shape;
import javax.swing.*;
import snap.swing.RMShapeIcon;
import snap.util.*;
import snap.web.*;

/**
 * A JComponentShape subclass for Labeled JComponents (JLabel, AbstractButton).
 */
public class LabeledShape extends JComponentShape {

    // The text
    String             _text;
    
    // The image name
    String             _imageName;
    
    // Alignment (horizontal/vertical
    int                _alignH = -1;
    int                _alignV = -1;
    
    // Text position (horizontal/vertical)
    int                _tposH = -1;
    int                _tposV = -1;
    
    // The icon text gap
    int                _itGap = -1;
    
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
 * Returns the image name.
 */
public String getImageName()  { return _imageName; }

/**
 * Sets the image name.
 */
public void setImageName(String aName)
{
    firePropertyChange("ImageName", _imageName, _imageName = aName, -1);
}

/**
 * Returns the image WebFile.
 */
public WebFile getImageFile()  { return getImageFile(getImageName()); }

/**
 * Returns the image WebFile.
 */
public WebFile getImageFile(String aName)
{
    if(aName==null) return null;
    WebURL url = getSourceURL(); if(url==null) return null;
    WebFile file = url.getFile(); if(file==null) return null;
    WebFile dir = file.getParent(); if(dir==null) return null;
    WebFile ifile = dir.getFile(aName);
    if(ifile==null) ifile = dir.getFile("pkg.images/" + aName); if(ifile==null) return null;
    return ifile;
}

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
 * Returns the default HorizontalAlignment.
 */
public int getHorizontalAlignmentDefault()  { return SwingConstants.CENTER; }

/**
 * Returns the horizontal alignment.
 */
public AlignX getAlignmentX()
{
    int ha = getHorizontalAlignment(); if(ha<0) ha = getHorizontalAlignmentDefault();
    switch(ha) {
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
 * Returns the VerticalAlignment.
 */
public int getVerticalAlignment()  { return _alignV; }

/**
 * Sets the VerticalAlignment.
 */
public void setVerticalAlignment(int aValue)
{
    firePropertyChange("VerticalAlignment", _alignV, _alignV = aValue, -1);
}

/**
 * Returns the default VerticalAlignment.
 */
public int getVerticalAlignmentDefault()  { return SwingConstants.CENTER; }

/**
 * Returns the vertical alignment.
 */
public AlignY getAlignmentY()
{
    int va = getVerticalAlignment(); if(va<0) va = getVerticalAlignmentDefault();
    switch(va) {
        case SwingConstants.TOP: return AlignY.Top;
        case SwingConstants.BOTTOM: return AlignY.Bottom;
        default: return AlignY.Middle;
    }
}

/**
 * Sets the alignment x.
 */
public void setAlignmentY(AlignY anAlignY)
{
    switch(anAlignY) {
        case Top: setVerticalAlignment(SwingConstants.TOP); break;
        case Middle: setVerticalAlignment(SwingConstants.CENTER); break;
        case Bottom: setVerticalAlignment(SwingConstants.BOTTOM); break;
    }
}

/**
 * Returns the HorizontalTextPosition.
 */
public int getHorizontalTextPosition()  { return _tposH; }

/**
 * Sets the HorizontalTextPosition.
 */
public void setHorizontalTextPosition(int aValue)
{
    firePropertyChange("HorizontalTextPosition", _tposH, _tposH = aValue, -1);
}

/**
 * Returns the VerticalTextPosition.
 */
public int getVerticalTextPosition()  { return _tposV; }

/**
 * Sets the VerticalTextPosition.
 */
public void setVerticalTextPosition(int aValue)
{
    firePropertyChange("VerticalTextPosition", _tposV, _tposV = aValue, -1);
}

/**
 * Returns the IconTextGap.
 */
public int getIconTextGap()  { return _itGap; }

/**
 * Sets the IconTextGap.
 */
public void setIconTextGap(int aValue)
{
    firePropertyChange("IconTextGap", _itGap, _itGap = aValue, -1);
}

/**
 * Returns the horizontal alignment of the given component as a string ("left", "center" or "right").
 */
public static String getHorizontalAlignmentString(int anInt)
{
    switch(anInt) {
        case SwingConstants.LEFT: return "left";
        case SwingConstants.CENTER: return "center";
        case SwingConstants.RIGHT: return "right";
        case SwingConstants.LEADING: return "leading";
        case SwingConstants.TRAILING: return "trailing";
        default: return "unknown";
    }
}

/**
 * Sets the horizontal alignment of the given component as a string ("left", "center" or "right").
 */
public static Integer getHorizontalAlignmentInt(String aString)
{
    Integer align = null;
    if("left".equals(aString)) align = SwingConstants.LEFT;
    else if("center".equals(aString)) align = SwingConstants.CENTER;
    else if("right".equals(aString)) align = SwingConstants.RIGHT;
    else if("leading".equals(aString)) align = SwingConstants.LEADING;
    else if("trailing".equals(aString)) align = SwingConstants.TRAILING;
    return align;
}

/**
 * Returns the vertical alignment of the given component as a string ("top", "center" or "bottom").
 */
public static String getVerticalAlignmentString(int anInt)
{
    switch(anInt) {
        case SwingConstants.TOP: return "top";
        case SwingConstants.CENTER: return "center";
        case SwingConstants.BOTTOM: return "bottom";
        default: return "unknown";
    }
}

/**
 * Sets the vertical alignment of the given component as a string ("top", "center" or "bottom").
 */
public static Integer getVerticalAlignmentInt(String aString)
{
    Integer align = null;
    if("top".equals(aString)) align = SwingConstants.TOP;
    else if("center".equals(aString)) align = SwingConstants.CENTER;
    else if("bottom".equals(aString)) align = SwingConstants.BOTTOM;
    return align;
}

/**
 * Returns the labels horizontal text position as a string.
 */
public static String getHorizontalTextPositionString(int anInt)
{
    switch(anInt) {
        case SwingConstants.LEFT: return "left";
        case SwingConstants.CENTER: return "center";
        case SwingConstants.RIGHT: return "right";
        case SwingConstants.LEADING: return "leading";
        case SwingConstants.TRAILING: return "trailing";
        default: return "unknown";
    }    
}

/**
 * Sets the label's horizontal text position as a string.
 */
public static Integer getHorizontalTextPositionInt(String aString)
{
    Integer position = null;
    if("left".equals(aString)) position = SwingConstants.LEFT;
    if("center".equals(aString)) position = SwingConstants.CENTER;
    if("right".equals(aString)) position = SwingConstants.RIGHT;
    if("leading".equals(aString)) position = SwingConstants.LEADING;
    if("trailing".equals(aString)) position = SwingConstants.TRAILING;    
    return position;
}

/**
 * Returns the labels vertical text position as a string.
 */
public static String getVerticalTextPositionString(int anInt)
{
    switch(anInt) {
        case SwingConstants.TOP: return "top";
        case SwingConstants.CENTER: return "center";
        case SwingConstants.BOTTOM: return "bottom";
        default: return "unknown";
    }
}

/**
 * Sets the label's vertical text position as a string.
 */
public static Integer getVerticalTextPositionInt(String aString)
{
    Integer position = null;
    if("top".equals(aString)) position = SwingConstants.TOP;
    if("center".equals(aString)) position = SwingConstants.CENTER;
    if("bottom".equals(aString)) position = SwingConstants.BOTTOM;
    return position;
}

/**
 * Returns the icon.
 */
public Icon getIcon()  { return _icon; }

/**
 * Sets the icon.
 */
public void setIcon(Icon anIcon)  { _icon = anIcon; } Icon _icon;

/**
 * Override to apply subclass attributes.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get icon
    Icon icon = _icon!=null? _icon : getIcon(getImageName());
    
    // Set JLabel attributes
    if(aComp instanceof JLabel) { JLabel label = (JLabel)aComp;
        if(getText()!=null) label.setText(getText());
        if(icon!=null) label.setIcon(icon);
        if(getHorizontalAlignment()>=0) label.setHorizontalAlignment(getHorizontalAlignment());
        if(getVerticalAlignment()>=0) label.setVerticalAlignment(getVerticalAlignment());
        if(getHorizontalTextPosition()>=0) label.setHorizontalTextPosition(getHorizontalTextPosition());
        if(getVerticalTextPosition()>=0) label.setVerticalTextPosition(getVerticalTextPosition());
        if(getIconTextGap()>=0) label.setIconTextGap(getIconTextGap());
    }
    
    // Set AbstractButton attributes
    if(aComp instanceof AbstractButton) { AbstractButton button = (AbstractButton)aComp;
        if(getText()!=null) button.setText(getText());
        if(icon!=null) button.setIcon(icon);
        if(getHorizontalAlignment()>=0) button.setHorizontalAlignment(getHorizontalAlignment());
        if(getVerticalAlignment()>=0) button.setVerticalAlignment(getVerticalAlignment());
        if(getHorizontalTextPosition()>=0) button.setHorizontalTextPosition(getHorizontalTextPosition());
        if(getVerticalTextPosition()>=0) button.setVerticalTextPosition(getVerticalTextPosition());
        if(getIconTextGap()>=0) button.setIconTextGap(getIconTextGap());
    }
}

/**
 * Returns an icon for given name.
 */
protected Icon getIcon(String aName)
{
    if(aName==null) return null;
    WebFile file = getImageFile(aName);
    if(file!=null) {
        if(file.getType().equals("rpt"))
            return new RMShapeIcon(file.getBytes(), 0, 0);
        return new ImageIcon(file.getBytes());
    }
    
    // Try to find image from UIManager
    Icon icon = UIManager.getIcon(aName); if(icon!=null) return icon;
    System.err.println("LabeledShape: Couldn't find icon " + aName + " at " + getSourceURL());
    return null;
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
    // Archive basic component attributes and set element name to label
    XMLElement e = super.toXMLShape(anArchiver);
    
    // Archive Text and Image (name)
    String text = getText(); if(text!=null && text.length()>0) e.add("text", text);
    String imageName = getImageName(); if(imageName!=null && imageName.length()>0) e.add("image", imageName);

    // Archive HorizontalAlignment and VerticalAlignment
    if(getHorizontalAlignment()>=0) e.add("align", getHorizontalAlignmentString(getHorizontalAlignment()));
    if(getVerticalAlignment()>=0) e.add("v-align", getVerticalAlignmentString(getVerticalAlignment()));
    
    // Archive HorizontalTextPosition and VerticalTextPosition
    if(getHorizontalTextPosition()>=0)
        e.add("text-position", getHorizontalTextPositionString(getHorizontalTextPosition()));
    if(getVerticalTextPosition()>=0)
        e.add("v-text-position", getVerticalTextPositionString(getVerticalTextPosition()));
    
    // Archive IconTextGap
    if(getIconTextGap()>=0)
        e.add("icon-text-gap", getIconTextGap());

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
    
    // Unarchive Text and Image name
    setText(anElement.getAttributeValue("text", anElement.getAttributeValue("value")));
    setImageName(anElement.getAttributeValue("image"));
    
    // Unarchive HorizontalAlignment, VerticalAlignment
    Integer ah = getHorizontalAlignmentInt(anElement.getAttributeValue("align"));
    if(ah!=null) setHorizontalAlignment(ah);
    Integer av = getVerticalAlignmentInt(anElement.getAttributeValue("v-align"));
    if(av!=null) setVerticalAlignment(av);
    
    // Unarchive  and VerticalTextPosition
    Integer tph = getHorizontalTextPositionInt(anElement.getAttributeValue("text-position"));
    if(tph!=null) setHorizontalTextPosition(tph);
    Integer tpv = getVerticalTextPositionInt(anElement.getAttributeValue("v-text-position"));
    if(tpv!=null) setVerticalTextPosition(tpv);
    
    // Unarchive IconTextGap
    if(anElement.hasAttribute("icon-text-gap")) setIconTextGap(anElement.getAttributeIntValue("icon-text-gap"));
}

}