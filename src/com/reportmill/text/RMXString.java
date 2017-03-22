package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.beans.PropertyChangeEvent;
import java.util.*;
import snap.util.*;

/**
 * An RMXString is like a String that lets you apply attributes, like fonts and colors, to character ranges. These
 * character ranges with common attributes are represented internally as the inner class Run.
 *
 * You might use it like this:
 * <p><blockquote><pre>
 *    RMXString xstring = new RMXString("Hello World", RMColor.red);
 *    xstring.addAttribute(RMFont.getFont("Arail Bold", 12), 0, 5);
 *    xstring.addAttribute(RMFont.getFont("Arial BoldItalic", 12), 6, xstring.length());
 * </pre></blockquote><p>
 * Advanced applications, that need to disect or render strings, might iterate over the runs like this:
 * <p><blockquote><pre>
 *    for(int i=0; i<xstring.getRunCount(); i++) {
 *      RMXString.Run run = xstring.getRun(i);
 *      graphics.setFont(run.getFont().awt());
 *      graphics.setColor(run.getColor().awt());
 *      graphics.drawString(xstring.substring(run.start(), run.end()));
 *    }
 */
public class RMXString extends SnapObject implements CharSequence, RMTextTypes, XMLArchiver.Archivable {

    // The StringBuilder
    StringBuilder        _sb = new StringBuilder();
    
    // The list of adjacent character segments with identical properties
    RMXStringRun         _run = new RMXStringRun(this);
    
    // The string after it processed to make sure all characters are shown in a font that supports them
    RMXString            _representableString;
    
    // Whether fire property change is enabled
    boolean              _firePropertyChangeEnabled = true;

/**
 * Creates an empty RMXString.
 */
public RMXString()  { }

/**
 * Creates an RMXString initialized with the given String and no attributes.
 */
public RMXString(CharSequence theChars)  { addChars(theChars); }

/**
 * Creates an RMXString initialized with the given String with all characters set to the given style.
 */
public RMXString(CharSequence theChars, RMTextStyle aStyle)
{
    addChars(theChars); _run._style = aStyle;
}

/**
 * Creates an RMXString initialized with the given String with all characters set to the given attributes.
 */
public RMXString(CharSequence theChars, Object ... theAttrs)
{
    addChars(theChars);
    Map attrs = getAttributes(theAttrs);
    _run._style = _run._style.clone(attrs);
}

/**
 * Returns the simple String represented by this RMXString.
 */
public String getText()  { return toString(); }

/**
 * The length.
 */
public int length()  { return _sb.length(); }

/**
 * Returns the char at given index.
 */
public char charAt(int anIndex)  { return _sb.charAt(anIndex); }

/**
 * Returns a subsequence.
 */
public CharSequence subSequence(int aStart, int anEnd)  { return _sb.subSequence(aStart, anEnd); }

/**
 * Sets the simple String represented by this RMXString.
 */
public void setText(String aString)  { replaceChars(aString, 0, length()); }

/**
 * Returns the index within this string of the first occurrence of the given substring.
 */
public int indexOf(String aString)  { return toString().indexOf(aString); }

/**
 * Returns the index within this string of first occurrence of given substring, starting at given index.
 */
public int indexOf(String aString, int aStart)  { return toString().indexOf(aString, aStart); }

/**
 * Appends the given String to the end of this XString.
 */
public void addChars(CharSequence theChars)  { addChars(theChars, length()); }

/**
 * Appends the given chars with the given attribute(s).
 */
public void addChars(CharSequence theChars, Object ... theAttrs)  { addChars(theChars, getAttributes(theAttrs)); }

/**
 * Appends the given string to the end of this XString, with the given attributes.
 */
public void addChars(CharSequence theChars, Map theAttrs)  { addChars(theChars, theAttrs, length()); }

/**
 * Adds chars at index.
 */
public void addChars(CharSequence theChars, int anIndex)
{
    _sb.insert(anIndex, theChars);
    _run.addLength(theChars.length(), anIndex); // Add length to run
    if(hasListeners()) firePropertyChange(new CharsChange(null, theChars, anIndex)); // Fire property change
    _representableString = null;
}

/**
 * Appends the given string to this XString, with the given attributes, at the given index.
 */
public void addChars(CharSequence theChars, Map theAttrs, int anIndex)
{
    if(theChars.length()==0) return;
    RMXStringRun run = getRunAt(anIndex); RMTextStyle style = run.getStyle();
    if(theAttrs!=null) style = new RMTextStyle(run).clone(theAttrs);
    addChars(theChars, style, anIndex);
}

/**
 * Appends the given string to this XString, with the given attributes, at the given index.
 */
public void addChars(CharSequence theChars, RMTextStyle aStyle, int anIndex)
{
    if(theChars.length()==0) return;
    if(aStyle==null || aStyle.equals(getRunAt(anIndex).getStyle())) addChars(theChars, anIndex);
    else addString(new RMXString(theChars, aStyle), anIndex);
}

/**
 * Removes characters in given range.
 */
public void removeChars(int aStart, int anEnd)
{
    // If empty range, just return
    if(anEnd==aStart) return;
    
    // Get chars for range and do normal version
    CharSequence chars = null;
    if(hasListeners()) {
        setStyle(getStyleAt(aStart, false), aStart, anEnd);
        chars = subSequence(aStart, anEnd);
    }
    
    // Delete chars and remove length from run(s)
    _sb.delete(aStart, anEnd);
    _run.removeLength(anEnd - aStart, aStart);
    
    // Fire property change
    if(chars!=null) firePropertyChange(new CharsChange(chars, null, aStart));
    _representableString = null;
}

/**
 * Replaces chars in given range, with given String.
 */
public void replaceChars(CharSequence theChars, int aStart, int anEnd)
{
    RMTextStyle style = anEnd==aStart || getRun().getNext()==null? null : getStyleAt(aStart, false);
    if(anEnd>aStart) removeChars(aStart, anEnd);
    addChars(theChars, style, aStart);
}

/**
 * Replaces chars in given range, with given String, using the given attributes.
 */
public void replaceChars(CharSequence theChars, RMTextStyle aStyle, int aStart, int anEnd)
{
    if(anEnd>aStart) removeChars(aStart, anEnd);
    addChars(theChars, aStyle, aStart);
}

/**
 * Adds an XString to this string at given index.
 */
public void addString(RMXString aString, int anIndex)
{
    // Add chars and set style for given string runs
    addChars(aString, anIndex);
    for(RMXStringRun run = aString.getRun(); run!=null; run = run.getNext())
        setStyle(run.getStyle(), run.start() + anIndex, run.end() + anIndex);
}

/**
 * Replaces the chars in given range, with given XString.
 */
public void replaceString(RMXString xString, int aStart, int anEnd)
{
    if(anEnd>aStart) removeChars(aStart, anEnd);
    addString(xString, aStart);
}

/**
 * Returns the XString head run.
 */
public RMXStringRun getRun()  { return _run; }

/**
 * Returns the number of runs in this XString.
 */
public int getRunCount()  { return _run.getRunCount(); }

/**
 * Returns the specific Run at the given index in this XString.
 */
public RMXStringRun getRun(int anIndex)  { return _run.getRun(anIndex); }

/**
 * Returns the last run in this XString (convenience).
 */
public RMXStringRun getRunLast()  { return _run.getRunLast(); }

/**
 * Returns the XString run that contains or ends at given index.
 */
public final RMXStringRun getRunAt(int anIndex)  { return getRunAt(anIndex, true); }

/**
 * Returns the XString run that contains or ends (if given option is true) at given index.
 */
public RMXStringRun getRunAt(int anIndex, boolean isInclusive)  { return _run.getRunAt(anIndex, isInclusive); }

/**
 * Returns the text style for the run at the given character index.
 */
public RMTextStyle getStyleAt(int anIndex)  { return getStyleAt(anIndex, true); }
    
/**
 * Returns the text style for the run at the given character index.
 */
public RMTextStyle getStyleAt(int anIndex, boolean isInclusive)  { return getRunAt(anIndex, isInclusive).getStyle(); }
    
/**
 * Sets the text style for given range.
 */
public void setStyle(RMTextStyle aStyle, int aStart, int anEnd)
{
    // Iterate over effected runs
    for(RMXStringRun run=getRunAt(aStart, false); run!=null && run.start()<anEnd; run=run.getNext()) {
    
        // If style already set, increment char index and continue
        if(run.getStyle().equals(aStyle))
            continue;
    
        // If run starts or ends outside aStart or anEnd, trim it
        if(run.start()<aStart)
            run = run.split(aStart - run.start());
        if(anEnd<run.end())
            run.split(anEnd - run.start());
        
        // Set style in run
        RMTextStyle ostyle = run.getStyle(); run.setStyle(aStyle);
        
        // Fire property change
        if(hasListeners()) firePropertyChange(new StyleChange(ostyle, aStyle, run.start(), run.end()));
    }
    _representableString = null;
}

/**
 * Applies the given attribute to whole xstring, assuming it's a basic attr types (font, color, etc.).
 */
public void setAttribute(Object anAttr)  { setAttribute(anAttr, 0, length()); }

/**
 * Applies the given attribute to the given character range, assuming it's a basic attr type (font, color, etc.).
 */
public void setAttribute(Object anAttr, int aStart, int anEnd)
{
    String key = getAttributeKey(anAttr);
    if(key!=null) setAttribute(key, anAttr, aStart, anEnd);
}

/**
 * Adds a given attribute of given type to the whole string.
 */
public void setAttribute(String aKey, Object anAttr)  { setAttribute(aKey, anAttr, 0, length()); }

/**
 * Sets a given attribute to a given value for a given range.
 */
public void setAttribute(String aKey, Object aValue, int aStart, int anEnd)
{
    // If end greater than length, complain
    if(anEnd>length()) { System.err.println("Set attribute where end greater than length"); return; }
    
    // If Paragraph, expand range to line boundaries
    if(aKey==TEXT_PARAGRAPH) {
        while(aStart>0 && (charAt(aStart-1)!='\n' && charAt(aStart-1)!='\r')) aStart--;
        while(anEnd<length() && (charAt(anEnd)!='\n' && charAt(anEnd)!='\r')) anEnd++;
        if(anEnd<length() && (charAt(anEnd)=='\n' || charAt(anEnd)=='\r')) anEnd++;
    }
    
    // Iterate over effected runs
    for(RMXStringRun run=getRunAt(aStart, false); run!=null && run.start()<anEnd; run=run.getNext()) {
    
        // If attribute already set, increment char index and continue
        if(SnapUtils.equals(aValue, run.getStyle().getAttribute(aKey)))
            continue;
    
        // If run starts or ends outside aStart or anEnd, trim it
        if(run.start()<aStart)
            run = run.split(aStart - run.start());
        if(anEnd<run.end())
            run.split(anEnd - run.start());
        
        // Set attribute in run
        RMTextStyle ostyle = run.getStyle(), nstyle = ostyle.clone(aKey, aValue);
        run.setStyle(nstyle);
        
        // Fire property change
        if(hasListeners()) firePropertyChange(new StyleChange(ostyle, nstyle, run.start(), run.end()));
    }
}

/**
 * Returns the current font at the given character index.
 */
public RMFont getFontAt(int anIndex)  { return getRunAt(anIndex).getFont(); }

/**
 * Returns the current paragraph at the given character index.
 */
public RMParagraph getParagraphAt(int anIndex)  { return getRunAt(anIndex).getParagraph(); }

/**
 * Sets the paragraph for the given character index range.
 */
public void setParagraph(RMParagraph ps, int start, int end)  { setAttribute(TEXT_PARAGRAPH, ps, start, end); }

/**
 * Returns the most likely key for a given attribute.
 */
private static String getAttributeKey(Object anAttr)
{
    if(anAttr instanceof RMFont) return TEXT_FONT;
    if(anAttr instanceof RMColor) return TEXT_COLOR;
    if(anAttr instanceof RMFormat) return TEXT_FORMAT;
    if(anAttr instanceof Outline) return TEXT_OUTLINE;
    System.out.println("RMXString.getAttributeKey: Unknown key for " + (anAttr!=null? anAttr.getClass() : null));
    return null;
}

/**
 * Returns an attribute map for given attributes.
 */
private static Map getAttributes(Object ... theAttrs)
{
    Map map = new HashMap(theAttrs.length);
    for(Object attr : theAttrs) { String key = getAttributeKey(attr); if(key!=null) map.put(key, attr); }
    return map;
}

/**
 * Sets the xstring to be underlined.
 */
public void setUnderlined(boolean aFlag)  { setAttribute(TEXT_UNDERLINE, aFlag? 1 : null, 0, length()); }

/**
 * Returns the horizontal alignment of the first paragraph of the xstring.
 */
public AlignX getAlignX()  { return getParagraphAt(0).getAlignmentX(); }

/**
 * Sets the horizontal alignment of the xstring.
 */
public void setAlignX(AlignX anAlignX)
{
    // Get first paragraph (just return if alignment already matches)
    RMParagraph pgraph = getParagraphAt(0); if(pgraph.getAlignmentX()==anAlignX)  return;
    
    // Get paragraph with new alignment and set
    pgraph = pgraph.deriveAligned(anAlignX);
    setParagraph(pgraph, 0, length());
}

/**
 * Returns the default font for this string.
 */
public RMFont getDefaultFont()  { return RMFont.Helvetica12; }

/**
 * Returns the default color for this string.
 */
public RMColor getDefaultColor()  { return RMColor.black; }

/**
 * Returns the default paragraph for this string.
 */
public RMParagraph getDefaultParagraph()  { return RMParagraph.DEFAULT; }

/**
 * Returns the default format for this string.
 */
public RMFormat getDefaultFormat()  { return null; }

/**
 * Returns the default for a given key.
 */
public Object getDefaultAttribute(String aKey)
{
    if(aKey.equals(TEXT_FONT)) return getDefaultFont();
    if(aKey.equals(TEXT_COLOR)) return getDefaultColor();
    if(aKey.equals(TEXT_PARAGRAPH)) return getDefaultParagraph();
    if(aKey.equals(TEXT_FORMAT)) return getDefaultFormat();
    return null;
}

/**
 * Returns a version of this string that substitutes alternate fonts for any characters that cannot be displayed in 
 * their associated fonts (simply returns the receiver if all characters are valid).
 */
public RMXString getRepresentableString()
{
    if(_representableString==null) _representableString = RMXStringUtils.getRepresentableString(this);
    return _representableString;
}
         
/**
 * Returns an XString for given char range.
 */
public RMXString substring(int aStart)  { return substring(aStart, length()); }

/**
 * Returns an XString for given char range.
 */
public RMXString substring(int aStart, int anEnd)
{
    // Get first run and create string with chars in range and first run attributes
    RMXStringRun run0 = getRunAt(aStart);
    RMXString xstring = new RMXString(subSequence(aStart, anEnd), run0.getStyle());
    
    // Iterate over successive runs and set attributes
    for(RMXStringRun run=run0.getNext(); run!=null && run.end()<anEnd; run=run.getNext())
        xstring.setStyle(run.getStyle(), run.start() - aStart, Math.min(run.end(), anEnd) - aStart);
    
    // Return string
    return xstring;
}

/**
 * Creates a clone of the receiver, with substitution performed on @-sign delineated keys.
 */
public RMXString rpgClone(ReportOwner anRptOwner, Object userInfo, RMShape aShape, boolean doCopy)
{
    return RMXStringUtils.rpgClone(this, anRptOwner, userInfo, aShape, doCopy);
}

/**
 * Override so RMXStringRun can reach this and to reset representable string and version.
 */
protected void firePropertyChange(PropertyChangeEvent anEvent)
{ super.firePropertyChange(anEvent); _representableString = null; }

/**
 * Override so RMXStringRun can reach this and to reset representable string and version.
 */
protected void firePropertyChange(String aName, Object oldVal, Object newVal, int anIndex)
{ super.firePropertyChange(aName, oldVal, newVal, anIndex); _representableString = null; }

/** Sets whether string fires property change events. */
public void setFirePropertyChangeEnabled(boolean aValue)  { _firePropertyChangeEnabled = aValue; }

/** Override to return false if FirePropertyChangeEnabled is off. */
public boolean hasListeners()  { return _firePropertyChangeEnabled && super.hasListeners(); }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named xstring
    XMLElement e = new XMLElement("xstring");
    
    // Declare loop variable for xstring attributes: Font, Color, Paragraph, Format, Outline, Underline, Scripting, CS
    RMFont font = getDefaultFont(); RMColor color = getDefaultColor();
    RMParagraph pgraph = getDefaultParagraph(); RMFormat format = getDefaultFormat();
    Outline outline = null; boolean underline = false;
    int scripting = 0; float charSpacing = 0;
        
    // Iterate over runs
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMXStringRun run = getRun(i);
        
        // If font changed for run, write font element
        if(!SnapUtils.equals(font, run.getFont())) { font = run.getFont();
            e.add(anArchiver.toXML(font)); }
        
        // If color changed for run, write color
        if(!SnapUtils.equals(color, run.getColor())) { color = run.getColor();
            e.add(anArchiver.toXML(color)); }
        
        // If format changed for run, write format
        if(!SnapUtils.equals(format, run.getFormat())) { format = run.getFormat();
            if(format==null) e.add(new XMLElement("format"));
            else e.add(anArchiver.toXML(format));
        }
        
        // If paragraph style changed for run, write paragraph
        if(!SnapUtils.equals(pgraph, run.getParagraph())) { pgraph = run.getParagraph();
            e.add(anArchiver.toXML(pgraph)); }
        
        // If underline style changed, write underline
        if(underline!=run.isUnderlined()) { underline = run.isUnderlined();
            e.add(new XMLElement("underline"));
            if(!underline)
                e.get(e.size()-1).add("style", -1);
        }
        
        // If outline style changed, write outline
        if(!SnapUtils.equals(outline, run.getOutline())) { outline = run.getOutline();
            e.add(new XMLElement("outline"));
            if(outline==null) e.get(e.size()-1).add("off", true);
            else {
                if(outline.getStrokeWidth()!=1) e.get(e.size()-1).add("stroke", outline.getStrokeWidth());
                if(outline.getFillColor()!=null)
                    e.get(e.size()-1).add("color", "#" + outline.getFillColor().toHexString());
            }
        }
        
        // If scripting changed, write scripting
        if(scripting!=run.getScripting()) { scripting = run.getScripting();
            XMLElement se = new XMLElement("scripting");
            if(scripting!=0) se.add("val", scripting);
            e.add(se);
        }
        
        // If char spacing changed, write char spacing
        if(charSpacing!=run.getCharSpacing()) { charSpacing = run.getCharSpacing();
            XMLElement charSpacingXML = new XMLElement("char-spacing");
            charSpacingXML.add("value", charSpacing);
            e.add(charSpacingXML);
        }
        
        // If embedded shape, write embedded shape
        if(run.getAttribute(TEXT_EMBEDDED_SHAPE)!=null) {
            RMShape shape = (RMShape)run.getAttribute(TEXT_EMBEDDED_SHAPE);
            XMLElement shapeXML = new XMLElement("shape");
            shapeXML.add(shape.toXML(anArchiver));
            e.add(shapeXML);
        }
        
        // Write run string
        if(run.length()>0)
            e.add(new XMLElement("string", run.toString()));
    }
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public RMXString fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Get map for run attributes
    Map map = new HashMap();
    
    // Iterate over child elements to snag common attributes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement e = anElement.get(i);
        
        // Unarchive string
        if(e.getName().equals("string")) {
            String str = e.getValue(); if(str==null || str.length()==0) continue;
            addChars(str, map);
            map.remove(TEXT_EMBEDDED_SHAPE); // Clear embedded shape, if present
        }
        
        // Unarchive font element
        else if(e.getName().equals("font")) {
            RMFont font = (RMFont)anArchiver.fromXML(e, null);
            map.put(TEXT_FONT, font);
        }
        
        // Unarchive color element
        else if(e.getName().equals("color")) {
            RMColor color = (RMColor)anArchiver.fromXML(e, null);
            map.put(TEXT_COLOR, color);
        }
        
        // If format changed for segment, write format
        else if(e.getName().equals("format")) {
            RMFormat format;
            if(e.getAttributeValue("type")==null) format = null;
            else if(e.getAttributeValue("type").equals("number"))
                format = anArchiver.fromXML(e, RMNumberFormat.class, null);
            else format = anArchiver.fromXML(e, RMDateFormat.class, null);
            if(format==null) map.remove(TEXT_FORMAT);
            else map.put(TEXT_FORMAT, format);
        }
            
        // Unarchive pgraph element
        else if(e.getName().equals("pgraph")) {
            RMParagraph pgraph = (RMParagraph)anArchiver.fromXML(e, null);
            map.put(TEXT_PARAGRAPH, pgraph);
        }
        
        // Unarchive underline element
        else if(e.getName().equals("underline")) {
            if(e.getAttributeIntValue("style")<0) map.remove(TEXT_UNDERLINE);
            else map.put(TEXT_UNDERLINE, 1);
        }
        
        // Unarchive outline element
        else if(e.getName().equals("outline")) {
            if(e.getAttributeBoolValue("off")) map.remove(TEXT_OUTLINE);
            else {
                float stroke = e.getAttributeFloatValue("stroke", 1);
                String color = e.getAttributeValue("color");
                map.put(TEXT_OUTLINE, new Outline(stroke, color==null? null : new RMColor(color)));
            }
        }
        
        // Unarchive scripting
        else if(e.getName().equals("scripting")) {
            int scripting = e.getAttributeIntValue("val");
            if(scripting==0) map.remove(TEXT_SCRIPTING);
            else map.put(TEXT_SCRIPTING, scripting);
        }
        
        // Unarchive char spacing
        else if(e.getName().equals("char-spacing")) {
            float charSpacing = e.getAttributeFloatValue("value");
            if(charSpacing==0) map.remove(TEXT_CHAR_SPACING);
            else map.put(TEXT_CHAR_SPACING, charSpacing);
        }
        
        // Unarchive shape
        else if(e.getName().equals("shape")) {
            RMShape shape = (RMShape)anArchiver.fromXML(e.get(0), null);
            map.put(TEXT_EMBEDDED_SHAPE, shape);
        }
    }
    
    // If no string was read, apply attributes anyway
    if(length()==0)
        _run._style = _run._style.clone(map);
    
    // Return this xstring
    return this;
}

/**
 * Standard Object equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    RMXString other = anObj instanceof RMXString? (RMXString)anObj : null; if(other==null) return false;
    
    // Check Length, StringBuffer and Run
    if(other.length()!=length()) return false;
    for(int i=0, iMax=_sb.length(); i<iMax; i++) // Can't just do SB.equals()
        if(other._sb.charAt(i)!=_sb.charAt(i)) return false;
    return SnapUtils.equals(other._run, _run);
}
  
/**
 * Returns a clone of this x string.
 */
public RMXString clone()
{
    // Do basic clone and create new runs(s)
    RMXString clone = (RMXString)super.clone();
    clone._sb = new StringBuilder(_sb);
    clone._run = _run.cloneDeep(clone);
    
    // If representable string is this string, then make copied string's representable string copied string
    if(_representableString==this) clone._representableString = clone;
    return clone;
}

/**
 * Standard toString implementation.
 */
public String toString()  { return _sb.toString(); }

/**
 * The Outline inner class represents the attributes of outlined text: strokeWidth and fillColor.
 */
public static class Outline {
    
    // The stroke of the outline
    float    _strokeWidth = 1;
    
    // The fill color of the outline
    RMColor  _fillColor;
    
    /** Creates a new outline. */
    public Outline() { }
    
    /** Creates a new outline with the given stroke width and fill color. */
    public Outline(float aStrokeWidth, RMColor aFillColor)  { _strokeWidth = aStrokeWidth; _fillColor = aFillColor; }
    
    /** Returns the stroke width for this outline. */
    public float getStrokeWidth() { return _strokeWidth; }
    
    /** Returns the fill color for this outline. */
    public RMColor getFillColor() { return _fillColor; }
    
    /** Creates a new outline from this outline with the given stroke width. */
    public Outline deriveOutline(float aStrokeWidth) { return new Outline(aStrokeWidth, _fillColor); }
    
    /** Creates a new outline from this outline with the given fill color. */
    public Outline deriveOutline(RMColor aFillColor) { return new Outline(_strokeWidth, aFillColor); }
}

/**
 * A property change event for addChars/removeChars.
 */
public class CharsChange extends UndoPropChangeEvent {
    public CharsChange(Object oldV, Object newV, int anInd)  { super(RMXString.this, "Chars", oldV, newV, anInd); }
    public CharSequence getOldValue()  { return (CharSequence)super.getOldValue(); }
    public CharSequence getNewValue()  { return (CharSequence)super.getNewValue(); }
    public void doChange(Object oldValue, Object newValue) {
        if(oldValue!=null) removeChars(getIndex(), getIndex() + ((CharSequence)oldValue).length());
        else addChars((CharSequence)newValue, getIndex());
    }
    public UndoPropChangeEvent merge(UndoPropChangeEvent anEvent) { CharsChange event = (CharsChange)anEvent;
        if(getNewValue()!=null && event.getNewValue()!=null && getNewValue().length()+getIndex()==event.getIndex())
            return new CharsChange(null, new StringBuffer(getNewValue()).append(event.getNewValue()), getIndex());
        return null;
    }
}

/**
 * A property change event for RMXStringRun.Style change.
 */
public class StyleChange extends UndoPropChangeEvent {
    int _start, _end;
    public StyleChange(Object oV, Object nV, int aStart, int anEnd) {
        super(RMXString.this, "Style", oV, nV, -1); _start = aStart; _end = anEnd; }
    public void doChange(Object oldVal, Object newVal)  { setStyle((RMTextStyle)newVal, _start, _end); }
}

}