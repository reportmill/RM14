package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * Represents a run of characters in a line.
 */
public class RMTextRun extends Rectangle2D.Double implements CharSequence {

    // The text layout
    RMTextLayout      _layout;
    
    // The text line
    RMTextLine        _line;

    // The RMXStringRun for this text run
    RMXStringRun      _run;
    
    // The font for this text run (might be scaled by Layout FontScale)
    RMFont            _font;

    // The index of this run in line
    int               _index;
    
    // Returns the char start of this run
    int               _start;
    
    // Returns the char end of this run
    int               _end;
    
    // Whether this run is hyphenated
    boolean           _hyphenated;
    
    // The scripting of this run
    int               _scripting;
    
    // The char spacing of this run
    float             _charSpacing;
    
    // Whether font has changed from last run
    boolean           _fontChanged;
    
    // Whether color has changed from last run
    boolean           _colorChanged;

    // Whether run is full
    boolean           _locked;
    
    // The next run
    RMTextRun         _next;

/**
 * Returns the layout.
 */
public final RMTextLayout getLayout()  { return _layout; }

/**
 * Returns the line this run is associated with.
 */
public final RMTextLine getLine()  { return _line; }

/**
 * Returns the RMXString for this run.
 */
public RMXString getXString()  { return _layout._xString; }

/**
 * Returns the RMXStringRun for this text run.
 */
public RMXStringRun getRun()  { return _run; }

/**
 * Returns the index of this run in the line.
 */
public int getIndex()  { return _index; }

/**
 * Adds chars to text run.
 */
public int addChars(RMXStringRun aRun, int aStart, int anEnd)
{
    // Get start
    int start = aStart;
    
    // Get/calculate Line.MaxX
    double lineMaxX = getLine().getHitMaxX();
    
    // Cache scripting and char spacing to avoid run map lookups
    _scripting = aRun.getScripting();
    _charSpacing = aRun.getCharSpacing();
    
    // While chars still available and line not locked, add chars
    while(start<anEnd && !isLocked()) {
    
        // Get next char
        char nextChar = aRun.charAt(start);
        
        // If char is newline, add char, lock line and return
        if(nextChar=='\n' || nextChar=='\r') {
            addChar(nextChar, 0); start++;
            if(nextChar=='\r' && start<anEnd && aRun.charAt(start)=='\n') {
                addChar('\n', 0); start++; }
            getLine().setLocked(true);
            return start;
        }
            
        // If char is tab, set this run to tabstop and make sure tab is in run by itself
        if(nextChar=='\t') {
            
            // Get paragraph
            RMParagraph pgraph = getParagraph();
            
            // Find next tab
            int tabIndex = 0;
            while(tabIndex<pgraph.getTabCount() && pgraph.getTab(tabIndex)<=getMaxX() &&
                pgraph.getTab(tabIndex)<getLine().getHitMaxX())
                tabIndex++;
            
            // If next tab out of bounds, lock line and return
            if(tabIndex>=pgraph.getTabCount() || pgraph.getTab(tabIndex)>getLine().getHitMaxX()) {
                if(getLine().length()==0) {
                    addChar('\t', getLine().getWidth()); start++; }
                getLine().setLocked(true);
                return start;
            }
                
            // Add tab char, lock and return
            addChar('\t', pgraph.getTab(tabIndex) - getMaxX()); start++;
            setLocked(true);
            return start;
        }
        
        // Disabled because we can't get kern info in Java2D
        // Get the last char: char lastChar = length()>0? aLayout._chars[aStart-1] : (char)0;
        // Get char kern: float charKern = 0; //lastChar!=0? getCharKern(lastChar, nextChar) : 0;
        // If char kern not zero, start new run: if(charKern!=0 && length()>0) return getEnd();
        
        // Get character width, char MaxX
        double charWidth = getCharAdvance(nextChar);
        double charMaxX = getMaxX() + charWidth; // + charKern;
        
        // If char hits right margin and isn't whitespace, force newline
        if(charMaxX>lineMaxX && !Character.isWhitespace(nextChar)) {
            start -= getLine().deleteFromLastWord(aRun, start);
            getLine().setLocked(true);
            return start;
        }
        
        // Add a char
        addChar(nextChar, charWidth); start++;
            
        // If alignment is full and char was space, force new run
        if(nextChar==' ' && getParagraph().getAlignmentX()==RMTypes.AlignX.Full)
            setLocked(true);
    }
    
    // Lock run
    setLocked(true);
    
    // Return start
    return start;
}

/**
 * Adds a char to text run.
 */
protected void addChar(char aChar, double aWidth)
{
    if(_end>_start) aWidth += _charSpacing;
    _line.addChar(aChar, aWidth);
    width += aWidth;
    _end++;
    
    // Tell run to perform tab shift (to potentially account for right, center or decimal tabs)
    _line.performTabShift();
}

/**
 * Returns the char start of this line.
 */
public int getStart()  { return _start; }

/**
 * Returns the char end of this line.
 */
public int getEnd()  { return _end; }

/**
 * Returns the length of this text layout.
 */
public int length()  { return _end - _start; }

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)  { return _layout.charAt(_start + anIndex); }

/**
 * Returns a new char sequence that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd) { return _layout.subSequence(_start + aStart, _start + anEnd); }

/**
 * Returns the run font.
 */
public RMFont getFont()  { return _font; }

/**
 * Returns the run color.
 */
public RMColor getColor()  { return _run.getColor(); }

/**
 * Returns the run paragraph.
 */
public RMParagraph getParagraph()  { return _run.getParagraph(); }

/**
 * Returns whether the run is underlined.
 */
public boolean isUnderlined()  { return _run.isUnderlined(); }

/**
 * Returns the stroke width for the underline.
 */
public double getUnderlineStroke()  { return _line.getUnderlineStroke(); }

/**
 * Returns the Y position for the underline.
 */
public double getUnderlineY()  { return _line.getUnderlineY(); }

/**
 * Returns the underline style of this run.
 */
public int getUnderlineStyle()  { return _run.getUnderlineStyle(); }
    
/**
 * Returns whether the run should be outline.
 */
public RMXString.Outline getOutline()  { return _run.getOutline(); }

/**
 * Returns the run's scripting.
 */
public int getScripting()  { return _scripting; }

/**
 * Returns the run's char spacing.
 */
public float getCharSpacing()  { return _charSpacing; }

/**
 * Returns the char advance for a given character.
 */
protected double getCharAdvance(char aChar)
{
    double advance = getFont().charAdvance(aChar);
    if(!_layout.getUseFractionalMetrics())
        advance = (float)Math.ceil(advance);
    return advance;
}

/**
 * Returns whether this run has a hyphen at the end.
 */
public boolean isHyphenated()  { return _hyphenated; }

/**
 * Sets whether this run has a hyphen at the end.
 */
public void setHyphenated(boolean aFlag)
{
    // If value already set, just return
    if(aFlag==isHyphenated()) return;
    
    // Set value
    _hyphenated = aFlag;
    
    // Update bounds width
    width += getCharAdvance('-')*(aFlag? 1 : -1);
}

/**
 * Returns whether font changed from last run.
 */
public boolean getFontChanged()  { return _fontChanged; }

/**
 * Returns whether color changed from last run.
 */
public boolean getColorChanged()  { return _colorChanged; }

/**
 * Returns the y baseline position for this run (in the same coords as the layout frame).
 */
public double getYBaseline()
{
    // Return line baseline plus possible y offset due to superscripting
    double offset = getScripting()!=0? getFont().getSize()*(getScripting()<0? .4f : -.6f) : 0;
    return getLine().getYBaseline() + offset;
}

/**
 * Returns whether text layout is full.
 */
public boolean isLocked()  { return _locked; }

/**
 * Sets whether text layout can add more characters.
 */
public void setLocked(boolean aFlag)  { _locked = aFlag; }

/**
 * Returns the character index for the given x/y point.
 */
public int getCharIndex(double anX)
{
    // If beyond bounds, return length
    if(anX>=getMaxX()) return getEndsWithNewline() || getEndsWithWhiteSpace()? length() - 1 : length();
    
    // Iterate over chars
    double advance = getX();
    for(int i=0, iMax=length(); i<iMax; i++) {
        double charAdvance = getCharAdvance(charAt(i));
        if(anX < advance + charAdvance/2)
            return i;
        advance += charAdvance + (i>0? getCharSpacing() : 0);
    }
    
    // Since beyond mid-point of last char, return length
    return length();
}

/**
 * Returns the char advance to given char index.
 */
public double getXForChar(int anIndex)
{
    // If index is zero, return run X, if length or ends with newline, return MaxX
    if(anIndex==0) return getX();
    if(anIndex==length() || RMTextUtils.isNewlineChar(this, anIndex)) return getMaxX();
    
    // Otherwise get X + char advance of chars + char spacing
    double advance = getX() + getCharAdvance(charAt(0));
    for(int i=1; i<anIndex; i++) advance += getCharAdvance(charAt(i)) + _charSpacing;
    return advance;
}

/**
 * Returns the text origin for the given character index.
 */
public Point2D getPointForChar(int anIndex)  { return new RMPoint(getXForChar(anIndex), getYBaseline()); }

/**
 * Returns whether run is just a tab.
 */
public boolean isTab()  { return length()==1 && charAt(0)=='\t'; }

/**
 * Returns the tab location if this run is a tab.
 */
public float getTab()
{
    // Get tab index for run location
    int tabIndex = getParagraph().getTabIndex((float)getX());
    
    // Return tab location for run location
    return tabIndex>=0? getParagraph().getTab(tabIndex) : -1;
}

/**
 * Returns tab type if this run is a tab.
 */
public char getTabType()
{
    // Get tab index for run location
    int tabIndex = getParagraph().getTabIndex((float)getX());
    
    // Return tab type for tab index
    return tabIndex>=0? getParagraph().getTabType(tabIndex) : RMParagraph.TAB_LEFT;
}

/**
 * Returns whether line ends with space.
 */
public boolean getEndsWithWhiteSpace()  { char c = getLastChar(); return c==' ' || c=='\t'; }

/**
 * Returns whether run ends with newline.
 */
public boolean getEndsWithNewline()  { int len = length(); return len>0 && RMTextUtils.isNewlineChar(this, len-1); }

/**
 * Returns the last char.
 */
public char getLastChar()  { int len = length(); return len>0? charAt(len-1) : 0; }

/**
 * Returns the next run.
 */
public RMTextRun getNext()  { return _next; }

/**
 * Returns the run count.
 */
public int getRunCount()  { int c = 1; for(RMTextRun run=_next; run!=null; run=run._next) c++; return c; }

/**
 * Returns the run at given index.
 */
public RMTextRun getRun(int anIndex)
{
    int index = anIndex; for(RMTextRun run=this; run!=null; run=run.getNext(), index--) if(index==0) return run;
    throw new ArrayIndexOutOfBoundsException("RMTextRun.getRun: " + anIndex + " beyond " + getRunCount());
}

/**
 * Returns the last run.
 */
public RMTextRun getRunLast()  { for(RMTextRun run=this;; run=run.getNext()) if(run._next==null) return run; }

/**
 * Returns the run that contains or ends (if given option is true) at given index.
 */
public RMTextRun getRunAt(int anIndex, boolean isInclusive)
{
    for(RMTextRun run=this; run!=null; run=run.getNext()) { int end = run.getEnd();
        if(anIndex<end || anIndex==end && (isInclusive || run._next==null))
            return run; }
    throw new ArrayIndexOutOfBoundsException("RMXStringRun.getRunAt: " + anIndex + " beyond " + length());
}

/**
 * Returns the glyph vector for the run.
 */
public GlyphVector glyphVector(Graphics2D g)
{
    // Get font render context
    FontRenderContext c = g==null? RMFontFile.getGraphics2D().getFontRenderContext() : g.getFontRenderContext();
    
    // Get glyph vector from font
    GlyphVector gv = getFont().awt().createGlyphVector(c, RMStringUtils.trimEnd(toString()));
    
    // If char spacing, adjust glyph positions
    if(getCharSpacing()!=0) {
        Point2D.Double p = new Point2D.Double(0, 0);
        for(int i=0, iMax=length()-1; i<iMax; i++) {
            p.x += getCharAdvance(charAt(i)) + getCharSpacing();
            gv.setGlyphPosition(i+1, p);
        }
    }
    
    // Return glyph vector
    return gv;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer(this); if(isHyphenated()) sb.append('-'); return sb.toString();
    //return String.format("%s: [%f %f %f %f]: %s%s", getClass().getSimpleName(), getX(), getY(),
    //    getWidth(), getHeight(), subSequence(0,length()), getHyphenated()? "-" : "");
}

}