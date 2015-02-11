package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import java.util.Map;

/**
 * A class to hold style attributes for a text run.
 */
public class RMTextStyle implements RMTextTypes, Cloneable {

    // The font
    RMFont             _font;
    
    // The color
    RMColor            _color;
    
    // Underline style
    int                _underline;
    
    // The scripting
    int                _scripting;
    
    // The char spacing
    double             _charSpacing;
    
    // The paragraph
    RMParagraph        _pgraph;
    
    // The format
    RMFormat           _format;
    
    // The outline
    RMXString.Outline  _outline;
    
    // The text that created this style
    RMXStringRun       _run;

/**
 * Creates a new RMTextStyle.
 */
public RMTextStyle(RMXStringRun aRun)  { _run = aRun; }

/**
 * Returns the font for this run.
 */
public RMFont getFont()  { return _font!=null? _font : (_font=getFontDefault()); }

/**
 * Returns the color for this run.
 */
public RMColor getColor()  { return _color!=null? _color : (_color=getColorDefault()); }

/**
 * Returns whether this run is underlined.
 */
public boolean isUnderlined()  { return _underline!=0; }

/**
 * Returns the underline style of this run.
 */
public int getUnderlineStyle()  { return _underline; }
    
/**
 * Returns the scripting for this run (1=SuperScripting, -1=Subscripting, 0=none).
 */
public int getScripting()  { return _scripting; }

/**
 * Returns the char spacing.
 */
public double getCharSpacing()  { return _charSpacing; }

/**
 * Returns the paragraph.
 */
public RMParagraph getParagraph()  { return _pgraph!=null? _pgraph : (_pgraph=getPGDefault()); }

/**
 * Returns the format.
 */
public RMFormat getFormat()  { return _format!=null? _format : (_format=getFormatDefault()); }

/**
 * Returns the outline.
 */
public RMXString.Outline getOutline()  { return _outline; }

/** Returns the defaults for font, color, format, paragraph. */
RMFont getFontDefault()  { return _run!=null && _run._xString!=null? _run._xString.getDefaultFont() : new RMFont(); }
RMColor getColorDefault()  { return _run!=null && _run._xString!=null? _run._xString.getDefaultColor() : null; }
RMFormat getFormatDefault()  { return _run!=null && _run._xString!=null? _run._xString.getDefaultFormat() : null; }
RMParagraph getPGDefault()  { return _run!=null && _run._xString!=null? _run._xString.getDefaultParagraph() : null; }

/**
 * Returns the default for a given key.
 */
public Object getAttribute(String aKey)
{
    if(aKey.equals(TEXT_FONT)) return getFont();
    if(aKey.equals(TEXT_COLOR)) return getColor();
    if(aKey.equals(TEXT_UNDERLINE)) return getUnderlineStyle();
    if(aKey.equals(TEXT_SCRIPTING)) return getScripting();
    if(aKey.equals(TEXT_CHAR_SPACING)) return getCharSpacing();
    if(aKey.equals(TEXT_PARAGRAPH)) return getParagraph();
    if(aKey.equals(TEXT_FORMAT)) return getFormat();
    if(aKey.equals(TEXT_OUTLINE)) return getOutline();
    return null;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    RMTextStyle other = anObj instanceof RMTextStyle? (RMTextStyle)anObj : null; if(other==null) return false;
    if(!RMUtils.equals(other.getFont(), getFont())) return false;
    if(!RMUtils.equals(other.getColor(), getColor())) return false;
    if(other._underline!=_underline) return false;
    if(other._scripting!=_scripting) return false;
    if(other._charSpacing!=_charSpacing) return false;
    if(!RMUtils.equals(other.getParagraph(), getParagraph())) return false;
    if(!RMUtils.equals(other.getFormat(), getFormat())) return false;
    if(!RMUtils.equals(other.getOutline(), getOutline())) return false;
    return true;
}

/**
 * Standard clone implementation.
 */
public RMTextStyle clone()
{
    RMTextStyle clone = null; try { clone = (RMTextStyle)super.clone(); }
    catch(CloneNotSupportedException e) { }
    return clone;
}

/**
 * Clone with font.
 */
public RMTextStyle clone(RMFont aFont) { RMTextStyle clone = clone(); clone._font = aFont; return clone; }

/**
 * Clone with color.
 */
public RMTextStyle clone(RMColor aColor)  { RMTextStyle clone = clone(); clone._color = aColor; return clone; }

/**
 * Clone with key/value.
 */
public RMTextStyle clone(String aKey, Object aValue)
{
    RMTextStyle clone = clone();
    if(aKey.equals(TEXT_FONT)) clone._font = (RMFont)aValue;
    else if(aKey.equals(TEXT_COLOR)) clone._color = (RMColor)aValue;
    else if(aKey.equals(TEXT_UNDERLINE)) clone._underline = RMUtils.intValue(aValue);
    else if(aKey.equals(TEXT_SCRIPTING)) clone._scripting = RMUtils.intValue(aValue);
    else if(aKey.equals(TEXT_CHAR_SPACING)) clone._charSpacing = RMUtils.doubleValue(aValue);
    else if(aKey.equals(TEXT_PARAGRAPH)) clone._pgraph = (RMParagraph)aValue;
    else if(aKey.equals(TEXT_FORMAT)) clone._format = (RMFormat)aValue;
    else if(aKey.equals(TEXT_OUTLINE)) clone._outline = (RMXString.Outline)aValue;
    else System.err.println("TextStyle: Unknown key: " + aKey);
    return clone;
}

/**
 * Clone with map.
 */
public RMTextStyle clone(Map<?,?> aMap)
{
    RMTextStyle clone = this;
    for(Map.Entry entry : aMap.entrySet())
        clone = clone.clone((String)entry.getKey(), entry.getValue());
    return clone;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer("Style { ");
    sb.append("Font=").append(getFont()).append(", ");
    sb.append("Color=").append(getColor()).append(" }");
    return sb.toString();
}

}