package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import com.reportmill.text.RMXString.Outline;
import java.util.*;

/**
 * The Run class represents a range of characters in an xstring that share common attributes.
 * 
 * This class makes a point to treat its attributes map as read-only so they can be shared among multiple runs.
 */
public class RMXStringRun implements RMTextTypes, Cloneable, CharSequence {
    
    // The xstring that this run is a part of
    RMXString     _xString;
    
    // The start/end char index of this run in string
    int           _start, _end;
    
    // The attributes of the Run (Font, Color, etc.)
    RMTextStyle   _style = new RMTextStyle(this);
    
    // The next run
    RMXStringRun  _next;
    
/**
 * Creates a new run.
 */
public RMXStringRun(RMXString anXString)  { _xString = anXString; }

/**
 * Returns the start character index for this run.
 */
public int start()  { return _start; }

/**
 * Returns the end character index for this run.
 */
public int end()  { return _end; }

/**
 * Returns the length in characters for this run.
 */
public int length()  { return _end - _start; }

/**
 * CharSequence method returning character at given index.
 */
public char charAt(int anIndex)  { return _xString.charAt(_start + anIndex); }

/**
 * CharSequence method return character sequence for range.
 */
public CharSequence subSequence(int aStart, int anEnd) { return _xString.subSequence(_start+aStart, _start+anEnd); }

/**
 * Returns the text style.
 */
public RMTextStyle getStyle()  { return _style; }

/**
 * Sets the text style.
 */
protected void setStyle(RMTextStyle aStyle)  { _style = aStyle; }

/**
 * Returns a specific attribute for the given key.
 */
public Object getAttribute(String aKey)  { return getStyle().getAttribute(aKey); }

/**
 * Returns the font for this run.
 */
public RMFont getFont()  { return getStyle().getFont(); }

/**
 * Returns the color for this run.
 */
public RMColor getColor()  { return getStyle().getColor(); }

/**
 * Returns the paragraph for this run.
 */
public RMParagraph getParagraph()  { return getStyle().getParagraph(); }

/**
 * Returns whether this run is underlined.
 */
public boolean isUnderlined()  { return getStyle().isUnderlined(); }

/**
 * Returns the underline style of this run.
 */
public int getUnderlineStyle()  { return getStyle().getUnderlineStyle(); }
    
/**
 * Returns the outline info for this run (null for none).
 */
public Outline getOutline()  { return getStyle().getOutline(); }

/**
 * Returns the Outline record for the given attributes map (or null for none).
 */
public static RMXString.Outline getOutline(Map attrs)  { return (RMXString.Outline)attrs.get(TEXT_OUTLINE); }

/**
 * Returns the format for this run.
 */
public RMFormat getFormat()  { return getStyle().getFormat(); }

/**
 * Returns the scripting for this run (1=SuperScripting, -1=Subscripting, 0=none).
 */
public int getScripting()  { return getStyle().getScripting(); }

/**
 * Returns the char spacing.
 */
public float getCharSpacing()  { return RMUtils.floatValue(getAttribute(TEXT_CHAR_SPACING)); }

/**
 * Return next run.
 */
public RMXStringRun getNext()  { return _next; }

/**
 * Returns the run count.
 */
public int getRunCount()
{
    int count = 1;
    for(RMXStringRun run=_next; run!=null; run=run.getNext()) count++;
    return count;
}

/**
 * Returns the run at given index.
 */
public RMXStringRun getRun(int anIndex)
{
    int index = anIndex; for(RMXStringRun run=this; run!=null; run=run.getNext(), index--) if(index==0) return run;
    throw new ArrayIndexOutOfBoundsException("RMXStringRun.getRun: " + anIndex + " beyond " + getRunCount());
}

/**
 * Returns the last run.
 */
public RMXStringRun getRunLast()  { for(RMXStringRun run=this;; run=run.getNext()) if(run._next==null) return run; }

/**
 * Returns the XString run that contains or ends (if given option is true) at given index.
 */
public RMXStringRun getRunAt(int anIndex, boolean isInclusive)
{
    for(RMXStringRun run=this; run!=null; run=run.getNext()) { int end = run.end();
        if(anIndex<end || anIndex==end && (isInclusive || run._next==null))
            return run; }
    throw new ArrayIndexOutOfBoundsException("RMXStringRun.getRunAt: " + anIndex + " beyond " + length());
}

/**
 * Adds length to run for char index.
 */
public void addLength(int aLength, int anIndex)
{
    // If within this run, add to end and shift next run(s)
    if(anIndex<=end()) {
        _end += aLength;
        if(_next!=null) _next.shift(aLength);
    }
    
    // Otherwise, pass on to next
    else _next.addLength(aLength, anIndex);
}

/**
 * Removes a length from run(s) for char index.
 */
public void removeLength(int aLength, int anIndex)
{
    // If within this run, remove from available chars (and next if necessary)
    if(anIndex<=end()) {
        
        // Remove from available chars
        int length = Math.min(end() - anIndex, aLength);
        _end -= length;
        if(_next!=null) _next.shift(-length);
        
        // If still more, continue (and remove link if empty)
        if(aLength>length) {
            _next.removeLength(aLength - length, anIndex);
            if(_next.length()==0)
                _next = _next._next;
        }
    }
    
    // Otherwise, pass on to next
    else _next.removeLength(aLength, anIndex);
}

/**
 * Shifts run by given amound.
 */
public void shift(int anAmount)
{
    for(RMXStringRun run=this; run!=null; run=run.getNext()) { run._start += anAmount; run._end += anAmount; }
}

/**
 * Splits the run at the given index and returns a run containing the remaining characters (and identical attributes).
 */
public RMXStringRun split(int anIndex)
{
    RMXStringRun remainder = clone(); // Get run for remainder
    _end = remainder._start = start() + anIndex; // Reset this run's Start and remainder's End to index
    remainder._next = _next; _next = remainder;
    return remainder;
}

/**
 * Returns whether this run is equal to the given object.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(!(anObj instanceof RMXStringRun)) return false;
    RMXStringRun other = (RMXStringRun)anObj;
    
    // Check Start, End, Style, Next
    if(other._start!=_start || other._end!=_end) return false;
    if(!RMUtils.equals(other.getStyle(), getStyle())) return false;
    if(!RMUtils.equals(other._next, _next)) return false;
    return true; // Return true since all checks passed
}

/**
 * Returns a basic clone of this object.
 */
public RMXStringRun clone()
{
    // Do normal clone and clear xstring
    RMXStringRun clone = null;
    try { clone = (RMXStringRun)super.clone(); }
    catch(CloneNotSupportedException e) { }
    
    // Clear next and return clone
    clone._next = null;
    return clone;
}

/**
 * Returns a clone of this run, including a clone of the next run(s) if present.
 */
public RMXStringRun cloneDeep(RMXString anXString)
{
    RMXStringRun clone = clone(); clone._xString = anXString;
    if(_next!=null) clone._next = _next.cloneDeep(anXString);
    return clone;
}

/**
 * Returns a string representation of this run.
 */
public String toString()  { return _xString.subSequence(_start, _end).toString(); }

}