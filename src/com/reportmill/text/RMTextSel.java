package com.reportmill.text;

/**
 * A class to represent a selection of text.
 */
public class RMTextSel {

    // The Text
    RMTextEditor _text;

    // The start/end
    int          _start, _end;
    
    // The anchor (usually End)
    int          _anchor;

/**
 * Creates a new selection.
 */
public RMTextSel(RMTextEditor aText, int aStart, int anEnd) { this(aText, aStart, anEnd, anEnd); }

/**
 * Creates a new selection.
 */
public RMTextSel(RMTextEditor aText, int aStart, int anEnd, int anAnchor)
{
    // Make sure start is before end
    if(anEnd<aStart) { int temp = anEnd; anEnd = aStart; aStart = temp; }
    
    // Set stuff
    _text = aText; if(aText==null) {_start = aStart; _end = anEnd; _anchor = anAnchor; return; }
    _start = Math.max(aStart, 0); _end = Math.min(anEnd, _text.length());
    _anchor = Math.min(Math.max(anAnchor,0), _text.length());
}

/**
 * Returns the selected range that would result from the given two points.
 */
/*public RMTextSel(RMTextEditor aText, double x1, double y1, double x2, double y2, boolean isWordSel, boolean isParaSel)
{
    // Get text
    _text = aText;
    
    // Get character index for point 1 & point 2
    int p1Char = _text.getCharIndex(x1, y1);
    int p2Char = _text.getCharIndex(x2, y2);
    
    // Set selection start and end for selected chars
    int selStart = Math.min(p1Char, p2Char);
    int selEnd = Math.max(p1Char, p2Char);
    
    // If word selecting, expand selection to word boundary
    if(isWordSel) {
        while(selStart>0 && isWordChar(_text.charAt(selStart-1))) selStart--;
        while(selEnd<_text.length() && isWordChar(_text.charAt(selEnd))) selEnd++;
    }
    
    // If paragraph selecting, expand selection to paragraph boundary
    else if(isParaSel) {
        while(selStart>0 && !RMTextUtils.isNewlineChar(_text, selStart-1)) selStart--;
        while(selEnd<_text.length() && !RMTextUtils.isNewlineChar(_text, selEnd)) selEnd++;
        if(selEnd<_text.length()) selEnd++;
    }

    // Set values
    _start = selStart; _end = selEnd; _anchor = selEnd;
}*/

/**
 * Returns the text.
 */
public RMTextEditor getText()  { return _text; }

/**
 * Returns the start.
 */
public int getStart()  { return Math.min(_start, _text.length()); }
    
/**
 * Returns the end.
 */
public int getEnd()  { return Math.min(_end, _text.length()); }

/**
 * Returns the anchor.
 */
public int getAnchor()  { return _anchor<=getStart()? getStart() : getEnd(); }

/**
 * The length.
 */
public int getSize()  { return _end - _start; }

/**
 * Returns whether selection is empty.
 */
public boolean isEmpty()  { return _start==_end; }
    
/**
 * Returns the selected text string.
 */
public String getString()  { return _text.subSequence(getStart(), getEnd()).toString(); }

/**
 * Moves the selection index forward a character (or if a range is selected, moves to end of range).
 */
public int getCharRight()
{
    // If selection empty but not at end, get next char (or after newline, if at newline) 
    int index = getEnd();
    if(isEmpty() && index<_text.length())
        index = RMTextUtils.isLineEnd(_text, index)? RMTextUtils.indexAfterNewline(_text, index) : (index + 1);
    return index;
}

/**
 * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
 */
public int getCharLeft()
{
    // If selection empty but not at start, get previous char (or before newline if after newline)
    int index = getStart();
    if(isEmpty() && index>0)
        index = RMTextUtils.isAfterLineEnd(_text, index)? RMTextUtils.lastIndexOfNewline(_text, index) : (index - 1);
    return index;
}

/**
 * Moves the selection index up a line, trying to preserve distance from beginning of line.
 */
/*public int getCharUp()
{
    RMTextLine lastColumnLine = _text.getLineFor(_anchor);
    int lastColumn = _anchor - lastColumnLine.getStart();
    RMTextLine thisLine = getStartLine(), nextLine = thisLine.getPreviousLine();
    int index = nextLine!=null? nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn) : getStart();
    return index;
}*/

/**
 * Moves the selection index down a line, trying preserve distance from beginning of line.
 */
/*public int getCharDown()
{
    RMTextLine lastColumnLine = _text.getLineFor(_anchor);
    int lastColumn = _anchor - lastColumnLine.getStart();
    RMTextLine thisLine = getEndLine(), nextLine = thisLine.getNextLine();
    int index = nextLine!=null? nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn) : getEnd();
    return index;
}*/

/**
 * Moves the insertion point to the beginning of line.
 */
public int getLineStart()
{
    // Get index at beginning of current line and index of first non-whitespace char and set selection
    int index1 = RMTextUtils.lastIndexAfterNewline(_text, getEnd()); if(index1<0) index1 = 0;
    int index2 = index1; while(index2<_text.length() && _text.charAt(index2)==' ') index2++;
    return !isEmpty() || index2!=getStart()? index2 : index1;
}

/**
 * Moves the insertion point to next newline or text end.
 */
public int getLineEnd()
{
    // Get index of newline and set selection
    int index = RMTextUtils.indexOfNewline(_text, getEnd());
    return index>=0? index : _text.length();
}

/**
 * Returns the line at selection start.
 */
//public RMTextLine getStartLine()  { return _text.getLineFor(getStart()); }

/**
 * Returns the line at selection end.
 */
//public RMTextLine getEndLine()  { return _text.getLineFor(getEnd()); }

/**
 * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
 */
/*public GeneralPath getPath(double aWidth)
{
    // Create new path for return
    int aStart = getStart(), anEnd = getEnd();
    GeneralPath path = new GeneralPath();

    // Get StartLine, EndLine and start/end points
    RMTextLine startLine = getStartLine(), endLine = aStart==anEnd? startLine : getEndLine();
    double startX = startLine.getXForChar(aStart-startLine.getStart()), startY = startLine.getBaselineY();
    double endX = endLine.getXForChar(anEnd-endLine.getStart()), endY = endLine.getBaselineY();
    
    // Get start top/height
    double startTop = startLine.getY();
    double startHeight = startLine.getHeight() + 1;
    
    // Get path for upper left corner of sel start
    path.moveTo(startX, startTop + startHeight);
    path.lineTo(startX, startTop);
    if(aStart==anEnd)
        return path;
    
    // If selection spans more than one line, add path components for middle lines and end line
    if(!RMMath.equals(startY, endY)) {                
        double endTop = endLine.getY();
        double endHeight = endLine.getHeight() + 1;
        path.lineTo(aWidth, startTop);
        path.lineTo(aWidth, endTop);
        path.lineTo(endX, endTop);
        path.lineTo(endX, endTop + endHeight);
        path.lineTo(getText().getX(), endTop + endHeight);
        path.lineTo(getText().getX(), startTop + startHeight);
    }
    
    // If selection spans only one line, add path components for upper-right, lower-right
    else {
        path.lineTo(endX, startTop);
        path.lineTo(endX, startTop + startHeight);
    }
    
    // Close path and return
    path.closePath();
    return path;
}*/

/**
 * Returns whether a character should be considered is part of a word when WordSelecting.
 */
protected boolean isWordChar(char c)  { return Character.isLetterOrDigit(c) || c=='_'; }

}