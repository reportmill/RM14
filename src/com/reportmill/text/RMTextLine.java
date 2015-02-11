package com.reportmill.text;
import com.reportmill.base.*;
import java.awt.geom.*;

/**
 * A class to represent a line of text in a layout.
 */
public class RMTextLine extends RMTextRun {

    // The head run for this line
    RMTextRun        _runs;
    
    // The number of runs in this line
    int              _runCount;

/**
 * Adds chars to text line.
 */
public int addChars(RMXStringRun aRun, int aStart, int anEnd)
{
    // Get start
    int start = aStart;
    
    // While chars still available and line not locked, add lines
    while(start<anEnd && !isLocked()) {

        // Get last run - if run is null or locked, get new run
        RMTextRun run = getRunLast();
        if(run==null || run.isLocked())
            run = _layout.addRun(aRun, this);
        
        // Have run add chars (if it actually rewinds before this line, return)
        start = run.addChars(aRun, start, anEnd);
        if(start<aStart)
            return start;
    }
    
    // Return start
    return start;
}

/**
 * Adds a char to text line.
 */
protected void addChar(char aChar, double aWidth)
{
    _layout.addChar(aChar);
    width += aWidth; 
    _end++;
}

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)  { return _layout.charAt(getStart() + anIndex); }

/**
 * Returns a new char sequence that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd)
{
    return getLayout().subSequence(getStart() + aStart, getStart() + anEnd);
}

/**
 * Sets the bounds of this layout.
 */
protected void setBounds(double anX, double aY, double aWidth, double aHeight)
{
    // Get dx + dy
    double dx = anX - getX(), dy = aY - getY();
    
    // Set bounds
    setRect(anX, aY, aWidth, aHeight);
    
    // Move runs
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        run.setRect(run.getX() + dx, run.getY() + dy, run.getWidth(), run.getHeight()); }
}

/**
 * Returns the y position for this line (in same coords as the layout frame).
 */
public double getYBaseline()  { return getY() + getMaxAscent(); }

/**
 * Returns the max x position possible for layout's path. Override if not just layout max.
 */
public double getHitMaxX()  { return _layout.getMaxX(); }

/**
 * Returns the next run.
 */
public RMTextLine getNext()  { return (RMTextLine)_next; }

/**
 * Returns the number of runs in this line.
 */
public int getRunCount()  { return _runCount; }

/**
 * Returns the text run at the given index.
 */
public RMTextRun getRun(int anIndex)  { return _runs.getRun(anIndex); }

/**
 * Returns the last run.
 */
public RMTextRun getRunLast()  { int rc = getRunCount(); return rc>0? getRun(rc-1) : null; }

/**
 * Returns the total height of line runs.
 */
public double getRunsHeight()
{
    double h = 0; for(int i=0, iMax=getRunCount(); i<iMax; i++) h += getRun(i).getHeight(); return h;
}

/**
 * Returns the max ascent of the chars in this line.
 */
public double getMaxAscent()
{
    // Iterate over line runs to find max ascent of fonts    
    double ascent = 0;
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        ascent = Math.max(ascent, run.getFont().getMaxAscent()); }
    return ascent;
}

/**
 * Returns the vertical distance for any line below this line.
 */
public double getLineAdvance()
{
    // Declare vars for line ascent, descent and leading
    double ascent = 0, descent = 0, leading = 0;
    
    // Otherwise, iterate over runs to find max ascent, descent, leading of run fonts    
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i); RMFont font = run.getFont();
        ascent = Math.max(font.getMaxAscent(), ascent);
        descent = Math.max(font.getMaxDescent(), descent);
        leading = Math.max(font.getLeading(), leading);
    }
    
    // Height is combination of those
    double height = ascent + descent + leading;
    
    // If including line spacing, subject height to paragraph min/max line height, line spacing & line gap
    RMParagraph pgraph = getParagraph();
    height = RMMath.clamp((float)height, pgraph.getLineHeightMin(), pgraph.getLineHeightMax());
    height *= pgraph.getLineSpacing();
    height += pgraph.getLineGap();
    
    // Return height
    return height;
}

/**
 * Returns the x coord for the given character index.
 */
public double getXForChar(int anIndex)
{
    RMTextRun run = getRunAt(anIndex);
    return run!=null? run.getXForChar(_start + anIndex - run.getStart()) : getX();
}

/**
 * Returns the text run that contains the character at the given character index.
 */
private RMTextRun getRunAt(int anIndex)
{
    // Iterate over runs and return if in run range
    int index = _start + anIndex;
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        if(index<run.getEnd())
            return run; }
    
    // Return last run if present
    if(anIndex==length()) return getRunLast();
    throw new RuntimeException("Char index " + anIndex + " beyond range " + length());
}

/**
 * Returns the text origin for the given character index.
 */
public Point2D getPointForChar(int anIndex)  { return new RMPoint(getXForChar(anIndex), getYBaseline()); }

/**
 * Returns the line index for the given y value.
 */
public RMTextRun getRunForPointX(double anX)
{
    // Iterate over runs and return one that spans given x
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        if(anX < run.getMaxX())
            return run; }
    
    // If no line for given y, return last line
    return getRunLast();
}

/**
 * Returns the character index for the given x/y point.
 */
public int getCharIndex(double anX)
{
    // Get run for x coord (just return zero if null)
    RMTextRun run = getRunForPointX(anX);
    return run!=null? run.getStart() + run.getCharIndex(anX) - getStart(): 0;
}

/**
 * Returns the max stroke width of any underlined chars in this line.
 */ 
public double getUnderlineStroke()
{
    // Iterate over line runs to find max underline thickness of fonts for _underlined runs
    double stroke = 0;
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        if(run.isUnderlined())
            stroke = Math.max(stroke, run.getFont().getUnderlineThickness()); }
    return stroke;
}

/**
 * Returns the Y position of any underlined chars in this line.
 */
public double getUnderlineY()
{
    // Iterate over line runs to find min underline position of fonts for _underlined runs
    double y = 0;
    for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
        if(run.isUnderlined())
            y = Math.min(y, run.getFont().getUnderlineOffset()); }
    return y;
}

/**
 * Rewind last word from line.
 */
protected int deleteFromLastWord(CharSequence anInput, int aStart)
{
    // Set Layout Wrapped to true
    _layout._wrapped = true;
    
    // Get character index for beginning of last word
    int lastWordStart = getLastWordStart();
    
    // Get last hyphen character
    int hyphen = -1;
    if(RMTextEditor.isHyphenating) {
        int inputStart = aStart - length() + lastWordStart;
        if(inputStart>=0)
            hyphen = HyphenDict.getShared().getHyphen(anInput, inputStart, aStart);
        if(hyphen>0)
            lastWordStart = hyphen = hyphen - aStart + length();
    }
    
    // If no word break found, return false
    if(lastWordStart==0) {
        _layout._longWordFound = true;
        return 0;
    }

    // If there is a word break on this line, rewind chars back to it
    int deleteCount = length() - lastWordStart;
    _layout.deleteChars(_start + lastWordStart);
    if(lastWordStart==hyphen)
        getRunLast().setHyphenated(true);
    return deleteCount;
}

/**
 * Returns the index of the last word break character in the line.
 */
private int getLastWordStart()
{
    // Iterate over line chars (from end) and return first one that is word break char
    for(int i=length()-1; i>0; i--)
        if(Character.isWhitespace(charAt(i)) && charAt(i)!='\n' && charAt(i)!='\r')
            return i + 1;
    
    // If no word break chars found, just return start
    return 0;
}

/**
 * Sets whether text layout can add more characters.
 */
public void setLocked(boolean aFlag)
{
    // If value already set, just return
    if(aFlag==isLocked()) return;
    
    // Set value
    _locked = aFlag;
    
    // If locking, do some cleanup
    if(_locked) {
        
        // Lock last run
        getRunLast().setLocked(true);
        
        // If runs are available, set width from max x of last run
        width = getRunLast().getMaxX() - getX();
        
        // Get height from runs
        for(int i=0, iMax=getRunCount(); i<iMax; i++) { RMTextRun run = getRun(i);
            if(i==0 || run.getHeight()>getHeight())
                height = run.getHeight(); }
        
        // Update Layout.LineMaxX
        if(getMaxX()>_layout._linesMaxX) _layout._linesMaxX = getMaxX();
    }
}

/**
 * If line has any non-left tabs this method adjusts the width of the tab runs (and the positions of successive
 * runs to correct for shift of these tabs).
 */
public void performTabShift()
{
    // Iterate over runs
    for(int i=0, iMax=getRunCount()-1; i<iMax; i++) { RMTextRun run = getRun(i);
        
        // If run is not tab or is left tab, just continue, start shift
        if(!run.isTab() || run.getTabType()==RMParagraph.TAB_LEFT)
            continue;
            
        // Declare variable for last run effected by tab
        RMTextRun lastRun = run;
        
        // Declare variable for last char effected by tab
        int charEnd = 0;
        
        // Iterate over remaining runs & chars (up to next tab or line end) to get width of chars
        for(int j=i+1, jMax=iMax+1; j<jMax; j++) {
            
            // Get inner loop run (just break if tab)
            RMTextRun run2 = getRun(j);
            if(run2.isTab())
                break;
            
            // Set run end to loop run
            lastRun = run2;
            
            // If tab is decimal tab, set char end to decimal character (if present)
            if(run.getTabType()==RMParagraph.TAB_DECIMAL) {
                for(int k=0, kMax=run2.length(); k<kMax; k++) {
                    if(run.charAt(k)=='.')
                        charEnd = run.getStart() + k; }
            }
        }
        
        // If char end hasn't been set, set it
        if(charEnd==0)
            charEnd = lastRun.getEnd();
        
        // Get potential tab width and tab chars width
        double tabWidth = run.getTab() - run.getX();
        double charsWidth = getXForChar(charEnd) - run.getMaxX();
        
        // If center tab, cut chars width in half
        if(run.getTabType()==RMParagraph.TAB_CENTER)
            charsWidth /= 2;
        
        // Calculate actual tab width
        run.width = (float)Math.max(tabWidth - charsWidth, 0);
        
        // Iterate over runs to adjust locations - set run start to previous run end until we hit last run
        for(int j=i+1, jMax=iMax+1; j<jMax; j++) { RMTextRun runj = getRun(j);
            runj.x = (float)getRun(j-1).getMaxX();
            if(runj==lastRun)
                break;
        }
    }
}

/**
 * Performs alignment.
 */
protected void performAlignment()
{
    // Get paragraph
    RMParagraph pgraph = getParagraph();
    
    // If aligned left, just return
    if(pgraph.getAlignmentX()==RMTypes.AlignX.Left) return;
    
    // Calculate remainder space for line (minus trailing space)
    double remainder = getHitMaxX() - getMaxX();
    RMTextRun runLast = getRunLast();
    if(runLast.getLastChar()==' ') remainder += runLast.getCharAdvance(' ');
    
    // If paragraph is aligned right, shift line by remaining width
    if(pgraph.getAlignmentX()==RMTypes.AlignX.Right)
        setBounds(getX() + remainder, getY(), getWidth(), getHeight());

    // If paragraph is aligned center, shift line by half remaining width
    else if(pgraph.getAlignmentX()==RMTypes.AlignX.Center)
        setBounds(getX() + remainder/2, getY(), getWidth(), getHeight());
    
    // If paragraph is aligned full, break space characters into separate runs and widen
    else if(pgraph.getAlignmentX()==RMTypes.AlignX.Full) {
        
        // Return if line ends with newline (or is last line)
        if(getEndsWithNewline() || _layout.getLineLast()==this)
            return;
        
        // Reset bounds width to layout width
        width += remainder;
        
        // Get line word/spaces count (ignoring consecutive spcs). Still need to correct for lines that end with spaces
        int wordCount = 0;
        for(int i=0, iMax=getRunCount()-1; i<iMax; i++) { RMTextRun run = getRun(i);
            if(run.getEndsWithWhiteSpace())
                wordCount++; }

        // Calculate how much space to add to words
        double wordSpacing = wordCount==0? 0 : remainder/wordCount;
        
        // Iterate over runs to add word spacing
        double wordOffset = 0;
        for(int i=0; i<getRunCount()-1; i++) { RMTextRun run = getRun(i);
        
            // If run ends with space (and holds more than just space), correct word spacing
            if(run.getEndsWithWhiteSpace()) {
                
                // Add word spacing and cumulative offset to run
                run.width += wordSpacing;
                run.x += wordOffset;
                
                // Update word spacing for space added to current run
                wordOffset += wordSpacing;
            }
            
            // If run doesn't have space, just add cumulative offset
            else run.x += wordOffset;
        }
        
        // Manually add cumulative offset to last run
        if(getRunCount()>0)
            getRunLast().x += wordOffset;
    }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return subSequence(0, length()).toString(); }

}