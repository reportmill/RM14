package com.reportmill.text;
import com.reportmill.base.*;
import java.awt.geom.*;

/**
 * A class to layout text.
 */
public class RMTextLayout extends Rectangle2D.Double implements CharSequence, RMTextTypes {

    // The RMXString
    RMXString         _xString;
    
    // The font scale to apply to layout
    double            _fontScale = 1f;
    
    // Whether to use fractional character metrics
    boolean           _useFractionalMetrics = true;
    
    // The layout vertical alignment
    AlignY            _alignmentY = AlignY.Top;
    
    // Whether we need to perform layout
    boolean           _needsLayout;
    
    // The start/end indexes of laid out chars
    int               _start, _end;
    
    // The head line of lines list
    RMTextLine        _lines[] = new RMTextLine[8]; int _lineCount;

    // The head run of runs list
    RMTextRun         _runs;
    
    // Whether text layout is full
    boolean           _locked;
    
    // Whether layout had to wrap a line
    boolean           _wrapped;
    
    // Whether layout had to split a word for layout
    boolean           _longWordFound;
    
    // The max x and max y of all the lines
    double            _linesMaxX, _linesMaxY;

/**
 * Returns the RMXString.
 */
public RMXString getXString()  { return _xString; }

/**
 * Sets the RMXString.
 */
public void setXString(RMXString anXString)
{
    if(anXString==_xString) return;
    _xString = anXString;
    setNeedsLayout(true);
}

/**
 * Returns the start char index of the layout in XString.
 */
public int getStart()  { return _start; }

/**
 * Sets the start char index of the layout in XString.
 */
public void setStart(int aStart)  { _start = aStart; }

/**
 * Returns the end char index of the layout in XString.
 */
public int getEnd()  { return _end; }

/**
 * Returns whether text needs to perform layout.
 */
public boolean getNeedsLayout()  { return _needsLayout; }

/**
 * Sets whether text needs to perform layout.
 */
public void setNeedsLayout(boolean aValue)  { _needsLayout = aValue; }

/**
 * Performs text layout if needed.
 */
public void layout()  { if(getNeedsLayout()) layoutText(); }

/**
 * Performs text layout.
 */
protected void layoutText()
{
    // Reset everything
    _end = _start;  _lineCount = 0; _runs = null; // _lines = null; 
    _locked = _wrapped = _longWordFound = false;
    _linesMaxX = _linesMaxY = 0;
    
    // Iterate over runs
    RMXString xstring = _xString.getRepresentableString();
    for(RMXStringRun run=xstring.getRunAt(_start); run!=null; run=run.getNext()) {

        // Get run start and end
        int runStart = run.start(), end = run.length(), charStart = Math.max(_start, runStart) - runStart;
    
        // Add run chars: the whole run if starts after start, or partial if it ends after start
        end = runStart + addChars(run, charStart, end);
        
        // If addChars() actually rewound to before current run, addChars again from there
        while(end<runStart) {
            run = xstring.getRunAt(end);
            runStart = run.start(); charStart = end - runStart; end = run.length(); 
            end = runStart + addChars(run, charStart, end);
        }
    }
    
    // If last line ended with newline, add final new line
    RMTextLine lastLine = getLineLast();
    if(!isLocked() && (lastLine==null || lastLine.getEndsWithNewline()))
        addLine(xstring.getRunLast());
    
    // Lock string and reset valid index
    setLocked(true);
    setNeedsLayout(false);
    
    // If layout width unlimited, reset to Max Width and do horizontal alignment (by lines)
    if(width>10000) width = getWidthToFit();
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { RMTextLine line = _lines[i];
        line.performAlignment(); }
    
    // Vertical alignment - this needs to go!
    performAlignmentY();
}

/**
 * Adds chars to text layout.
 */
public int addChars(RMXStringRun aRun, int aStart, int anEnd)
{
    // Get start
    int start = aStart;
    
    // While chars still available and layout not locked, add lines
    while(start<anEnd && !isLocked()) {

        // Get last line - if null or locked, get new line
        RMTextLine line = getLineLast();
        if(line==null || line.isLocked())
            line = addLine(aRun);
        
        // Have line add chars (if it actually rewinds before given start, return)
        start = line.addChars(aRun, start, anEnd);
        if(start<aStart)
            return start;
        
        // If line bottom below layout bottom, remove line and lock
        if(line.getMaxY()>getMaxY()) {
            start = line.getStart(); removeLine(); setLocked(true); }
        
        // If empty line (assuming not LayoutInPath), lock layout
        if(line.length()==0 && !(this instanceof RMTextLayoutInPath))
            setLocked(true);
    }
    
    // Return last added char
    return start;
}

/**
 * Adds a char to the layout.
 */
protected void addChar(char aChar)  { _end++; }

/**
 * Deletes chars from index.
 */
public void deleteChars(int anIndex)
{
    // Get run at char index in line and either remove run or trim it
    RMTextRun run = _runs.getRunAt(anIndex, true);
    run.width = run.getXForChar(anIndex - run._start) - run.getX();
    run._end = anIndex;
    run._next = null;
    
    // Get line for char and reset line count to line (or before it, if deleting line)
    RMTextLine line = run.getLine();
    line._end = anIndex;
    line._runCount = line._runs.getRunCount();
    line.width = line.getRunLast().getMaxX() - line.getX();
    line.height = line.getRunsHeight();
    line._next = null;
    
    // Remove chars from buffer
    _end = _start + anIndex;
}

/**
 * Returns the length of this text layout.
 */
public int length()  { return _end - _start; }

/**
 * Returns the char value at the specified index.
 */
public char charAt(int anIndex)  { return _xString.charAt(_start + anIndex); }

/**
 * Returns a new char sequence that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd)  { return _xString.subSequence(_start + aStart, _start +anEnd); }

/**
 * Returns the font scale of the text layout.
 */
public double getFontScale()  { return _fontScale; }

/**
 * Sets the font scale of the text layout.
 */
public void setFontScale(double aValue)  { _fontScale = aValue; }

/**
 * Returns whether to use fractional character metrics.
 */
public boolean getUseFractionalMetrics()  { return _useFractionalMetrics; }

/**
 * Sets whether to use fractional character metrics.
 */
public void setUseFractionalMetrics(boolean aFlag)  { _useFractionalMetrics = aFlag; }

/**
 * Returns the vertical alignment of this layout.
 */
public AlignY getAlignmentY()  { return _alignmentY; }

/**
 * Sets the vertical alignment of this layout.
 */
public void setAlignmentY(AlignY anAlignment)  { _alignmentY = anAlignment; }

/**
 * Returns the width to fit.
 */
public double getWidthToFit()  { return _linesMaxX - getX(); }

/**
 * Returns the height to fit.
 */
public double getHeightToFit()  { return _linesMaxY - getY(); }

/**
 * Returns the head line of line linked list.
 */
public RMTextLine getLine()  { return _lineCount>0? _lines[0] : null; }

/**
 * Returns the number of lines.
 */
public int getLineCount() { return _lineCount; }  

/**
 * Returns the individual line at given index.
 */
public RMTextLine getLine(int anIndex)  { return _lines[anIndex]; }

/**
 * Returns the last line.
 */
public RMTextLine getLineLast()  { return _lineCount>0? _lines[_lineCount-1] : null; }

/**
 * Creates a new text line.
 */
protected RMTextLine createLine()  { return new RMTextLine(); }

/**
 * Adds a new line and returns it.
 */
public RMTextLine addLine(RMXStringRun aRun)
{
    // Get next start point
    Point2D startPoint = getNextLineStartPoint(aRun);

    // Get last line
    RMTextLine lastLine = getLineLast();
    
    // Get line at next unused index - if line doesn't exist yet, create it
    RMTextLine line = createLine(); line._layout = this;
    if(lastLine!=null) { lastLine._next = line; line._index = lastLine.getIndex()+1; }
    
    // Add line
    if(_lineCount==_lines.length) {
        RMTextLine lines[] = new RMTextLine[_lineCount*2];
        System.arraycopy(_lines, 0, lines, 0, _lineCount);
        _lines = lines;
    }
    _lines[_lineCount++] = line;

    // Initialize line
    line._run = aRun;
    line._start = line._end = length();
    RMFont font = aRun.getFont(); if(getFontScale()!=1) font = font.scaleFont(getFontScale());
    line.setRect(startPoint.getX(), startPoint.getY(), 0, font.getLineHeight());
    
    // Add default run
    line._runs = addRun(aRun, line);
    
    // Return new line
    return line;
}

/**
 * Removes a line.
 */
public void removeLine()
{
    RMTextLine lineLast = getLineLast();
    _end = _start + lineLast.getStart();
    if(lineLast.getIndex()==0) {
        _runs = null;  _lineCount--; }//_lines = null;
    else {
        RMTextLine newLastLine = getLine(lineLast.getIndex()-1);
        newLastLine._next = null; _lineCount--;
        newLastLine.getRunLast()._next = null;
    }
}

/**
 * Returns the head run in linked list of runs for the layout.
 */
public RMTextRun getRun()  { return _runs; }

/**
 * Returns the number of runs in this line.
 */
public int getRunCount()  { return _runs!=null? _runs.getRunCount() : 0; }

/**
 * Returns the text run at the given index.
 */
public RMTextRun getRun(int anIndex)  { return _runs.getRun(anIndex); }

/**
 * Returns the last run.
 */
public RMTextRun getRunLast()  { return _runs!=null? _runs.getRunLast() : null; }

/**
 * Creates a new run.
 */
public RMTextRun createRun()  { return new RMTextRun(); }

/**
 * Adds a new run.
 */
public RMTextRun addRun(RMXStringRun aRun, RMTextLine aLine)
{
    // Get next x
    double nextX = aLine.getRunCount()>0? aLine.getRunLast().getMaxX() : aLine.getX();

    // Get last run
    RMTextRun lastRun = getRunLast();
    
    // Get run at next unused index - if run doesn't exist yet, create it, otherwise, reset it
    RMTextRun run = createRun(); run._layout = this;
    if(lastRun!=null) { lastRun._next = run; run._index = lastRun.getIndex()+1; }
    else _runs = run;
    
    // Initialize new run
    run._line = aLine;
    run._run = aRun;
    RMFont font = aRun.getFont(); if(getFontScale()!=1) font = font.scaleFont(getFontScale());
    if(aRun.getScripting()!=0) font = font.scaleFont(.667f);
    run._font = font;
    run._start = run._end = length();
    run.setRect(nextX, aLine.getY(), 0, font.getLineHeight());
    
    // Initialize FontChanged, ColorChanged
    run._fontChanged = lastRun==null || !RMUtils.equals(lastRun.getFont(), font);
    run._colorChanged = lastRun==null || !RMUtils.equals(lastRun.getColor(), aRun.getColor());
    
    // Update line for new run
    aLine._runCount++;
    aLine.height = Math.max(aLine.height, run.height);
    
    // Return new run
    return run;
}

/**
 * Returns the character index for the given x/y point.
 */
public int getCharIndex(Point2D aPoint)  { return getCharIndex(aPoint.getX(), aPoint.getY()); }

/**
 * Returns the character index for the given x/y point.
 */
public int getCharIndex(double anX, double aY)
{
    RMTextLine line = getLineForPointY(aY); if(line==null) return 0;
    int index = line.getCharIndex(anX);
    if(index==line.length() && line.getEndsWithNewline()) index = RMTextUtils.lastIndexOfNewline(line, index);
    return line.getStart() + index;
}

/**
 * Returns the text origin for the given character index.
 */
public Point2D getPointForChar(int anIndex)
{
    // Get line for character (if no line, just return a guess at baseline)
    RMTextLine line = getLineForChar(anIndex);
    return line!=null? line.getPointForChar(anIndex-line.getStart()) : new RMPoint(getX(), getY() + 12);
}

/**
 * Returns the line for the given character index.
 */
public RMTextLine getLineForChar(int anIndex)
{
    // Iterate over lines and return line index for line that contains char index
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { RMTextLine line = _lines[i];
        if(anIndex<line.getEnd())// || (anIndex==line.getEnd() && !line.getEndsWithNewline()))
            return line; }
    
    // If no lines contain char index, return last line
    if(anIndex==length()) return getLineLast();
    throw new RuntimeException("Char index " + anIndex + " beyond range " + length());
}

/**
 * Returns the line for the given y value.
 */
public RMTextLine getLineForPointY(double aY)
{
    // If y less than zero, return null
    if(aY<0)
        return null;
    
    // Iterate over lines and return one that spans given y
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { RMTextLine line = _lines[i];
        if(aY < line.getMaxY())
            return line; }
    
    // If no line for given y, return last line
    return getLineLast();
}

/**
 * Returns the run for given x/y.
 */
public RMTextRun getRunForPoint(double anX, double aY)
{
    RMTextLine line = getLineForPointY(aY);
    return line!=null? line.getRunForPointX(anX) : null;
}

/**
 * Returns what this line thinks is the next line's x. Override if not infinity.
 */
public Point2D getNextLineStartPoint(RMXStringRun aRun)
{
    RMTextLine lastLine = getLineLast();
    _point.x = getX() + getIndent(aRun);
    _point.y = lastLine!=null? (lastLine.getY() + lastLine.getLineAdvance()) : getY();
    return _point;
} Point2D.Double _point = new Point2D.Double();

/**
 * Returns the indent for this line.
 */
public double getIndent(RMXStringRun aRun)
{
    // If previous line ended with newline, return first left indent
    RMTextLine lastLine = getLineLast();
    if(lastLine==null || lastLine.getEndsWithNewline())
        return aRun.getParagraph().getLeftIndentFirst();
    
    // If this line is on same vertical as last line, return zero
    if(RMMath.equals(getY(), lastLine.getY())) return 0;
    
    // Return paragraph left indent
    return aRun.getParagraph().getLeftIndent();
}

/**
 * Returns whether layout had to wrap a line.
 */
public boolean getWrapped()  { return _wrapped; }

/**
 * Returns whether a word was found that over-ran a whole line by itself.
 */
public boolean getLongWordFound()  { return _longWordFound; }

/**
 * Returns whether layout ran out of room trying to render chars.
 */
public boolean getRanOutOfRoom()  { return getHeightToFit() > _ayh; }

/**
 * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
 */
public GeneralPath getPathForChars(int aStart, int anEnd)
{
    // Create new path for return
    GeneralPath path = new GeneralPath();

    // Get start/end line
    RMTextLine startLine = getLineForChar(aStart);
    RMTextLine endLine = aStart==anEnd? startLine : getLineForChar(anEnd);
    
    // Get start/end points
    Point2D start = startLine.getPointForChar(aStart-startLine.getStart()); //getPointForChar(aStart);
    Point2D end = endLine.getPointForChar(anEnd-endLine.getStart()); //getPointForChar(anEnd);
    
    // Get start top/height
    double startTop = startLine.getY();
    double startHeight = startLine.getHeight();
    
    // Get path for upper left corner of sel start
    pathmoveTo(path, start.getX(), startTop + startHeight);
    pathlineTo(path, start.getX(), startTop);
    if(aStart==anEnd)
        return path;
    
    // If selection spans more than one line, add path components for middle lines and end line
    if(!RMMath.equals(start.getY(), end.getY())) {                
        double endTop = endLine.getY();
        double endHeight = endLine.getHeight();
        double maxX = getMaxX()!=java.lang.Float.MAX_VALUE? getMaxX() : 5000;
        pathlineTo(path, maxX, startTop);
        pathlineTo(path, maxX, endTop);
        pathlineTo(path, end.getX(), endTop);
        pathlineTo(path, end.getX(), endTop + endHeight);
        pathlineTo(path, getX(), endTop + endHeight);
        pathlineTo(path, getX(), startTop + startHeight);
    }
    
    // If selection spans only one line, add path components for upper-right, lower-right
    else {
        pathlineTo(path, end.getX(), startTop);
        pathlineTo(path, end.getX(), startTop + startHeight);
    }
    
    // Close path and return
    path.closePath();
    return path;
}

// For backwards compatibility with Java5.
protected void pathmoveTo(GeneralPath path, double x, double y)  { path.moveTo((float)x, (float)y); }
protected void pathlineTo(GeneralPath path, double x, double y)  { path.lineTo((float)x, (float)y); }

/**
 * Returns whether text layout is full.
 */
public boolean isLocked()  { return _locked; }

/**
 * Sets whether text layout can add more characters.
 */
public void setLocked(boolean aFlag)
{
    // If value already set, just return
    if(aFlag==isLocked()) return;
    
    // Set value
    _locked = aFlag;
    
    // Make sure last line is locked
    RMTextLine lastLine = getLineLast(); if(lastLine==null) return;
    if(_locked) lastLine.setLocked(true);
    
    // Update Layout.LineMaxY
    if(lastLine.getMaxY()>_linesMaxY)
        _linesMaxY = lastLine.getMaxY();
}

/**
 * Perform vertical alignment - this needs to move to RMTextShape!
 */
protected void performAlignmentY()
{
    if(getAlignmentY()==AlignY.Top || getRanOutOfRoom()) return;
    double remainder = _ayh - getHeightToFit(); if(remainder<=0) return;
    double shift = getAlignmentY()==AlignY.Middle? remainder/2 : remainder;
    if(shift!=0)
        for(int i=0, iMax=getLineCount(); i<iMax; i++) { RMTextLine line = _lines[i];
            line.setBounds(line.getX(), line.getY() + shift, line.getWidth(), line.getHeight()); }
}

/** Bogus! */
public void setAlignHeight(double aValue) { _ayh = aValue; } double _ayh;

/**
 * Scales font sizes of all text in an RMText shape to fit in bounds. Caches font scale factor in xstring.
 */
public void layoutToFit()
{
    // Do normal layout
    layout();
    if(!getRanOutOfRoom())
        return;
    
    // Declare starting fontScale factor and dampening variables
    double fontScale = 1, fsLo = 0, fsHi = 1;

    // Loop while dampening variables are normal
    while(true) {
        
        // Reset fontScale to mid-point of fsHi and fsLo
        fontScale = (fsLo + fsHi)/2;
        setFontScale(fontScale);
        layoutText();

        // If text exceeded layout bounds, reset fsHi to fontScale
        if(getRanOutOfRoom() || getLongWordFound()) {
            fsHi = fontScale;
            
            if((fsHi + fsLo)/2 == 0) {
                System.err.println("Error scaling text. Could only fit " + length() + " of " + _xString.length());
                break;
            }
        }
        
        // If text didn't exceed layout bounds, reset fsLo to fontScale
        else {
            fsLo = fontScale;
            if(_ayh - getHeightToFit() < 1 || fsHi - fsLo < .05)
                break;
        }
    }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return subSequence(0, length()).toString(); }

}