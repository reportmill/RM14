package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.RMArchiver;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;
import snap.util.*;

/**
 * This class provides all of the event and drawing code necessary to edit text in the form of an RMXString
 * (separated from an actual UI Component).
 */
public class RMTextEditor extends SnapObject implements RMTextTypes, CharSequence, PropertyChangeListener {
    
    // The XString being edited
    RMXString           _xString;
    
    // The editor bounds
    RMRect              _bounds = new RMRect();
    
    // The text selection
    RMTextSel           _sel;
    
    // The global font scale factor
    double              _fontScale = 1;
    
    // The current text style for the cursor or selection
    RMTextStyle         _inputStyle;
    
    // The current paragraph for the cursor or selection
    RMParagraph         _inputParagraph;
    
    // The listeners
    List                _listeners = new ArrayList();
    
    // The object that really knows how to layout text
    RMTextLayout        _layout;
    
    // Whether the editor is word selecting (double click)
    boolean             _wordSelecting;
    
    // Whether the editor is paragraph selecting (triple click)
    boolean             _pgraphSelecting;
    
    // The mouse down point
    double              _downX, _downY;

    // The text pane undoer
    Undoer              _undoer = new Undoer();
    
    // The undo set this text editor last added a text change to - used for coalescing of adjacent typing
    UndoSet           _lastUndoSet;
    
    // Whether RM should be spell checking
    public static boolean isSpellChecking = RMPrefsUtils.prefs().getBoolean("SpellChecking", false);
    
    // Whether RM should be hyphenating
    public static boolean isHyphenating = RMPrefsUtils.prefs().getBoolean("Hyphenating", false);
    
/**
 * Creates a plain text editor.
 */
public RMTextEditor() { setXString(new RMXString()); }

/**
 * Returns the xstring that is being edited.
 */
public RMXString getXString()  { return _xString; }

/**
 * Sets the xstring that is to be edited.
 */
public void setXString(RMXString aString)
{
    // If value already set, just return
    if(aString==_xString) return;
    
    // Set new XString (stop/start PropertyChangeListener)
    if(_xString!=null) _xString.removePropertyChangeListener(this);
    _xString = aString;
    if(_xString!=null) _xString.addPropertyChangeListener(this);
    
    // Reset selection
    setSel(0);
    relayout();
}

/**
 * Returns the text editor bounds.
 */
public Rectangle2D getBounds()  { return _bounds; }

/**
 * Sets the text editor bounds.
 */
public void setBounds(Rectangle2D aRect) { setBounds(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight()); }

/**
 * Sets the text editor bounds.
 */
public void setBounds(double anX, double aY, double aWidth, double aHeight)
{
    // If already set, just return
    if(anX==getX() && aY==getY() && aWidth==getWidth() && aHeight==getHeight()) return;
    
    // Set bounds
    _bounds.setRect(anX, aY, aWidth, aHeight);
    relayout();
}

/**
 * Returns the bounds x.
 */
public double getX()  { return _bounds.getX(); }

/**
 * Returns the bounds y.
 */
public double getY()  { return _bounds.getY(); }

/**
 * Returns the bounds width.
 */
public double getWidth()  { return _bounds.getWidth(); }

/**
 * Returns the bounds height.
 */
public double getHeight()  { return _bounds.getHeight(); }

/**
 * Returns the number of characters in the text string.
 */
public int length()  { return getXString().length(); }

/**
 * Returns the individual character at given index.
 */
public char charAt(int anIndex)  { return getXString().charAt(anIndex); }

/**
 * Returns a new <code>CharSequence</code> that is a subsequence of this sequence.
 */
public CharSequence subSequence(int aStart, int anEnd)  { return getXString().subSequence(aStart, anEnd); }

/**
 * Returns whether editor is doing check-as-you-type spelling.
 */
public boolean isSpellChecking()  { return isSpellChecking; }

/**
 * Returns whether editor tries to hyphenate wrapped words.
 */
public boolean isHyphenating()  { return isHyphenating; }

/**
 * Returns whether the selection is empty.
 */
public boolean isSelEmpty()  { return getSelStart()==getSelEnd(); }

/**
 * Returns the text editor selection.
 */
public RMTextSel getSel()  { return _sel; }

/**
 * Sets the character index of the text cursor.
 */
public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int anEnd)  { setSel(aStart, anEnd, anEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int anEnd, int anAnchor)
{
    // Set new selection
    _sel = new RMTextSel(this, aStart, anEnd, anAnchor);
    
    // Reset input style/paragraph
    _inputStyle = null; _inputParagraph = null;
    
    // Fire property change
    firePropertyChange("Selection", aStart + ((long)anEnd)<<32, 0, -1);
}

/**
 * Returns the character index of the start of the text selection.
 */
public int getSelStart()  { return _sel.getStart(); }

/**
 * Returns the character index of the end of the text selection.
 */
public int getSelEnd()  { return _sel.getEnd(); }

/**
 * Returns the character index of the last explicitly selected char (confined to the bounds of the selection).
 */
public int getSelAnchor()  { return _sel.getAnchor(); }

/**
 * Selects all the characters in the text editor.
 */
public void selectAll()  { setSel(0, length()); }

/**
 * Returns the selected range that would result from the given two points.
 */
public RMTextSel getSel(double p1x, double p1y, double p2x, double p2y)
{
    // Get layout manager
    RMTextLayout layout = getLayout();
    
    // Get character index for point 1 & point 2
    int p1Char = layout.getCharIndex(p1x, p1y);
    int p2Char = layout.getCharIndex(p2x, p2y);
    
    // Set selection start and end for selected chars
    int selStart = Math.min(p1Char, p2Char);
    int selEnd = Math.max(p1Char, p2Char);
    
    // If word selecting, expand selection to word boundary
    if(_wordSelecting) {
        while(selStart>0 && Character.isLetterOrDigit(layout.charAt(selStart-1))) selStart--;
        while(selEnd<layout.length() && Character.isLetterOrDigit(layout.charAt(selEnd))) selEnd++;
    }
    
    // If paragraph selecting, expand selection to paragraph boundary
    else if(_pgraphSelecting) {
        while(selStart>0 && !RMTextUtils.isNewlineChar(layout, selStart-1)) selStart--;
        while(selEnd<layout.length() && !RMTextUtils.isNewlineChar(layout, selEnd)) selEnd++;
        if(selEnd<layout.length()) selEnd++;
    }

    // Return range
    return new RMTextSel(this, layout.getStart() + selStart, layout.getStart() + selEnd);
}

/**
 * Returns the number of lines.
 */
public int getLineCount()  { return getLayout().getLineCount(); }

/**
 * Returns the individual line at given index.
 */
public RMTextLine getLine(int anIndex)  { return getLayout().getLine(anIndex); }

/**
 * Returns the line index for the given character index.
 */
public RMTextLine getLineForChar(int anIndex)
{
    RMTextLayout layout = getLayout();
    return layout.getLineForChar(anIndex - layout.getStart());
}

/**
 * Returns the line at selection start.
 */
public RMTextLine getSelStartLine()
{
    RMTextLayout layout = getLayout();
    return layout.getLineForChar(getSelStart() - layout.getStart());
}

/**
 * Returns the scale applied to text when editing.
 */
public double getFontScale()  { return _fontScale; }

/**
 * Sets the scale applied to text when editing.
 */
public void setFontScale(double aScale)  { _fontScale = aScale; }

/**
 * Returns the configured text layout used to layout text.
 */
public RMTextLayout getLayout()
{
    if(_layout==null) _layout = createLayout();
    _layout.layout();
    return _layout;
}

/**
 * Creates a configured text layout used to layout text.
 */
public RMTextLayout createLayout()
{
    RMTextLayout layout = new RMTextLayout();
    layout.setRect(getBounds());
    layout.setFontScale(getFontScale());
    layout.setXString(getXString());
    return layout;
}

/**
 * Tells editor to rebuild layout.
 */
public void relayout()  { _layout = null; }

/**
 * Returns the char index for given point in text coordinate space.
 */
public int getCharIndex(Point2D aPoint)
{
    RMTextLayout layout = getLayout();
    return layout.getStart() + layout.getCharIndex(aPoint);
}

/**
 * Returns the text style applied to any input characters.
 */
public RMTextStyle getInputStyle()
{
    // If InputStyle has been cleared, reset from selection start
    if(_inputStyle==null)
        _inputStyle = getXString().getStyleAt(getSelStart(), isSelEmpty());
    return _inputStyle;
}

/**
 * Sets the attributes that are applied to current selection or newly typed chars.
 */
public void setInputAttribute(String aKey, Object aValue)
{
    // If selection is zero length, just modify input attributes
    if(isSelEmpty())
        _inputStyle = getInputStyle().clone(aKey, aValue);

    // If selection is multiple chars, apply attribute to xstring
    else {
        getXString().setAttribute(aKey, aValue, getSelStart(), getSelEnd()); // Apply to xstring
        _inputStyle = null; // Reset InputStyle
        undoerSaveChanges(); // Flush undo changes
    }
}

/**
 * Returns the paragraph of the current selection or cursor position.
 */
public RMParagraph getInputParagraph()
{
    // If no input paragraph, get from xstring
    if(_inputParagraph==null)
        _inputParagraph = getXString().getParagraphAt(getSelStart());
    return _inputParagraph;
}

/**
 * Sets the paragraph of the current selection or cursor position.
 */
public void setInputParagraph(RMParagraph ps)
{
    _inputParagraph = ps;
    getXString().setParagraph(ps, getSelStart(), getSelEnd());
}

/**
 * Returns the plain string of the xstring being edited.
 */
public String getString()  { return getXString().getText(); }

/**
 * Returns the color of the current selection or cursor.
 */
public RMColor getColor()  { return getInputStyle().getColor(); }

/**
 * Sets the color of the current selection or cursor.
 */
public void setColor(RMColor color)  { setInputAttribute(TEXT_COLOR, color); }

/**
 * Returns the font of the current selection or cursor.
 */
public RMFont getFont()  { return getInputStyle().getFont(); }

/**
 * Sets the font of the current selection or cursor.
 */
public void setFont(RMFont font)  { setInputAttribute(TEXT_FONT, font); }

/**
 * Returns the format of the current selection or cursor.
 */
public RMFormat getFormat()  { return getInputStyle().getFormat(); }

/**
 * Sets the format of the current selection or cursor, after trying to expand the selection to encompass currently
 * selected, @-sign delineated key.
 */
public void setFormat(RMFormat aFormat)
{
    // Get format selection range and select it (if non-null)
    RMTextSel sel = RMTextEditorUtils.smartFindFormatRange(this);
    if(sel!=null)
        setSel(sel.getStart(), sel.getEnd());

    // Return if we are at end of string (this should never happen)
    if(getSelStart()>=getString().length())
        return;

    // If there is a format, add it to current attributes and set for selected text
    setInputAttribute(TEXT_FORMAT, aFormat);
}

/**
 * Returns whether current selection is underlined.
 */
public boolean isUnderlined()  { return getInputStyle().isUnderlined(); }

/**
 * Sets whether current selection is underlined.
 */
public void setUnderlined(boolean aFlag)  { setInputAttribute(TEXT_UNDERLINE, aFlag? 1 : null); }

/**
 * Returns whether current selection is outlined.
 */
public RMXString.Outline getOutline()  { return getInputStyle().getOutline(); }

/**
 * Sets whether current selection is outlined.
 */
public void setOutline(RMXString.Outline anOutline)  { setInputAttribute(TEXT_OUTLINE, anOutline); }

/**
 * Sets current selection to superscript.
 */
public void setSuperscript()
{
    int state = getInputStyle().getScripting();
    setInputAttribute(TEXT_SCRIPTING, state==0? 1 : 0);
}

/**
 * Sets current selection to subscript.
 */
public void setSubscript()
{
    int state = getInputStyle().getScripting();
    setInputAttribute(TEXT_SCRIPTING, state==0? -1 : 0);
}

/**
 * Returns the alignment for current selection.
 */
public RMTypes.AlignX getAlignmentX()  { return getInputParagraph().getAlignmentX(); }

/**
 * Sets the alignment for current selection.
 */
public void setAlignmentX(RMTypes.AlignX anAlignmentX)
{
    // Get input paragraph (just return if given align matches this text editor's paragraph align)
    RMParagraph ps = getInputParagraph();
    if(ps.getAlignmentX()==anAlignmentX)
        return;
    
    // Get derived paragraph with new align
    ps = ps.deriveAligned(anAlignmentX);
    
    // Set new input paragraph
    setInputParagraph(ps);
}

/**
 * Returns the character spacing of the current selection or cursor.
 */
public float getCharSpacing()  { return (float)getInputStyle().getCharSpacing(); }

/**
 * Returns the character spacing of the current selection or cursor.
 */
public void setCharSpacing(float aValue)
{
    setInputAttribute(TEXT_CHAR_SPACING, aValue);
}

/**
 * Returns the line spacing for current selection.
 */
public float getLineSpacing()  { return getInputParagraph().getLineSpacing(); }

/**
 * Sets the line spacing for current selection.
 */
public void setLineSpacing(float aHeight)
{
    RMParagraph ps = getInputParagraph().deriveLineSpacing(aHeight);
    setInputParagraph(ps);
}

/**
 * Returns the line gap for current selection.
 */
public float getLineGap()  { return getInputParagraph().getLineGap(); }

/**
 * Sets the line gap for current selection.
 */
public void setLineGap(float aHeight)
{
    RMParagraph ps = getInputParagraph().deriveLineGap(aHeight);
    setInputParagraph(ps);
}

/**
 * Returns the min line height for current selection.
 */
public float getLineHeightMin()  { return getInputParagraph().getLineHeightMin(); }

/**
 * Sets the min line height for current selection.
 */
public void setLineHeightMin(float aHeight)
{
    RMParagraph ps = getInputParagraph().deriveLineHeightMin(aHeight);
    setInputParagraph(ps);
}

/**
 * Returns the maximum line height for a line of text (even if font size would dictate higher).
 */
public float getLineHeightMax()  { return getInputParagraph().getLineHeightMax(); }

/**
 * Sets the maximum line height for a line of text (even if font size would dictate higher).
 */
public void setLineHeightMax(float aHeight)
{
    RMParagraph ps = getInputParagraph().deriveLineHeightMax(aHeight);
    setInputParagraph(ps);
}

/**
 * Deletes the current selection.
 */
public void delete()
{
    // If no characters are selected, delete from previous char index to selection start and set selection back 1
    if(isSelEmpty() && getSelStart()>0) {
        int start = getSelStart() - 1;
        if(RMTextUtils.isAfterLineEnd(this, start + 1))
            start = RMTextUtils.lastIndexOfNewline(this, start + 1);
        delete(start, getSelStart(), true);
    }

    // Delete selection range from x string and set selection to selection start
    else if(!isSelEmpty())
        delete(getSelStart(), getSelEnd(), true);
}

/**
 * Deletes the given range of chars.
 */
public void delete(int aStart, int anEnd, boolean doUpdateSelection)
{
    // If bogus range, just return
    if(anEnd<=aStart) return;
    
    // Delete chars from string
    getXString().removeChars(aStart, anEnd);
    
    // If update selection requested, update selection to start of deleted range
    if(doUpdateSelection)
        setSel(aStart, aStart, aStart);
}

/**
 * Replaces the current selection with the given string.
 */
public void replace(String aString)
{
    replace(aString, (Map)null, getSelStart(), getSelEnd(), true);
}

/**
 * Replaces the current selection with the given string.
 */
public void replace(String aString, int aStart, int anEnd, boolean doUpdateSelection)
{
    replace(aString, (Map)null, aStart, anEnd, doUpdateSelection);
}

/**
 * Replaces the current selection with the given string.
 */
public void replace(String aString, Map theAttrs, int aStart, int anEnd, boolean doUpdateSelection)
{
    // Get attributes for string: get attributes at given start and add given attributes
    RMTextStyle style = aStart==getSelStart()? getInputStyle() : getXString().getStyleAt(aStart);
    if(theAttrs!=null)
        style = style.clone(theAttrs);
    replace(aString, style, aStart, anEnd, doUpdateSelection);
}

/**
 * Replaces the current selection with the given string.
 */
public void replace(String aString, RMTextStyle aStyle, int aStart, int anEnd, boolean doUpdateSelection)
{
    // Do replace in xstring with given string and current input attributes
    getXString().replaceChars(aString, aStyle, aStart, anEnd);
    
    // If paragraph attributes are different, replace them
    if(!getXString().getParagraphAt(aStart).equals(getInputParagraph()))
        getXString().setParagraph(getInputParagraph(), aStart, aStart + aString.length());
    
    // Update selection to be at end of new string
    if(doUpdateSelection)
        setSel(aStart + aString.length());
}

/**
 * Replaces the current selection with the given xstring.
 */
public void replace(RMXString anXString)  { replace(anXString, getSelStart(), getSelEnd(), true); }

/**
 * Replaces the current selection with the given xstring.
 */
public void replace(RMXString anXString, int aStart, int anEnd, boolean doUpdateSelection)
{
    // Iterate over string runs and do replace for each one individually
    int start = aStart, end = anEnd;
    for(int i=0, iMax=anXString.getRunCount(); i<iMax; i++) { RMXStringRun run = anXString.getRun(i);
        replace(run.toString(), run.getStyle(), start, end, false);
        start = end = start + run.length();
    }
    
    // Update selection to be at end of new string
    if(doUpdateSelection)
        setSel(aStart + anXString.length());
}

/**
 * Copies the current selection onto the clip board, then deletes the current selection.
 */
public void cut()  { copy(); delete(); }

/**
 * Copies the current selection onto the clipboard.
 */
public void copy()
{
    // If at least one character is selected...
    if(!isSelEmpty()) {
        
        // Get xstring for selected characters and install xstring transferable in system clipboard
        RMXString string = getXString().substring(getSelStart(), getSelEnd());
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(new RMTextEditorUtils.RMXStringTransferable(string), null);
    }
}

/**
 * Pasts the current clipboard data over the current selection.
 */
public void paste()
{
    // Clear last undo set so paste doesn't get lumped in to coalescing
    _lastUndoSet = null;
    
    // Get system clipboard and its contents (return if null)
    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = cb.getContents(null);
    if(contents==null)
        return;
    
    // If contents contains RMText, get contents and have it replace current selection
    if(contents.isDataFlavorSupported(RMTextEditorUtils.RMTextFlavor)) try {
        
        // Get clipboard bytes
        ByteArrayInputStream bis = (ByteArrayInputStream)contents.getTransferData(RMTextEditorUtils.RMTextFlavor);
        byte bytes[] = new byte[bis.available()];
        bis.read(bytes);
        
        // Unarchive xstring from clipboard bytes
        RMXString string = (RMXString)new RMArchiver().readObject(bytes);
        
        // Replace current text selection with unarchived xstring
        replace(string);
    }
    
    // Exception catch for above
    catch(Exception e) { e.printStackTrace(); }

    // If contents contains a string, get string and have it replace current selection
    else if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)) try {
        
        // Get clipboard string and replace current text selection with unarchived string
        String string = (String)contents.getTransferData(DataFlavor.stringFlavor);
        if(string!=null && string.length()>0)
            replace(string);
    }
    
    // Exception catch for above
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Returns the undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Called to undo the last edit operation in the editor.
 */
public void undo()
{
    if(_undoer.getUndoSetLast()!=null)
        _undoer.undo();
}

/**
 * Called to redo the last undo operation in the editor.
 */
public void redo()
{
    if(_undoer.getRedoSetLast()!=null)
        _undoer.redo();
}

/**
 * Called when characters where added, updated or deleted.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Notify that text needs layout
    relayout();
    
    // Add property change
    undoerAddPropertyChange(anEvent);

    // Forward on to text editor listeners
    firePropertyChange(anEvent);
}

/**
 * Adds a property change to undoer.
 */
protected void undoerAddPropertyChange(PropertyChangeEvent anEvent)
{
    // Get undoer and undoer ActiveUndoSet
    Undoer undoer = getUndoer(); if(undoer==null || !undoer.isEnabled()) return;
    UndoSet activeUndoSet = undoer.getActiveUndoSet();
    
    // If no changes in ActiveUndoSet yet, see if we should reinstate last undo or set selected objects
    if(activeUndoSet.getChangeCount()==0) {
        
        // If last undo set is non-null and not active set, and not chars delete, and selections match, reinstate it
        if(_lastUndoSet!=null && _lastUndoSet!=activeUndoSet && anEvent.getNewValue()!=null && 
            RMUtils.equals(_lastUndoSet.getRedoSelection(), getUndoSelection()))
                undoer.setActiveUndoSet(_lastUndoSet);
        
        // Otherwise, set undo selection
        else undoer.setUndoSelection(getUndoSelection());
    }

    // Add property
    undoer.addPropertyChange(anEvent);
    _lastUndoSet = activeUndoSet;
}

/**
 * Saves changes to undoer.
 */
protected void undoerSaveChanges()
{
    Undoer undoer = getUndoer(); if(undoer==null) return;
    if(_lastUndoSet!=null) _lastUndoSet.setRedoSelection(getUndoSelection());
    undoer.saveChanges();
}

/**
 * Returns a selection object for undoer.
 */
protected Object getUndoSelection()  { return new TextSelection(); }

/**
 * A class to act as text selection.
 */
public class TextSelection implements Undoer.Selection {
    public int start, end;
    public TextSelection() { start = getSelStart(); end = getSelEnd(); } // Use ivars to avoid min()
    public void setSelection()  { _lastUndoSet = null; RMTextEditor.this.setSel(start, end); }
    public boolean equals(Object anObj)  { TextSelection other = (TextSelection)anObj;
        return start==other.start && end==other.end; }
    public int hashCode() { return start + end; }
}

/**
 * Moves the insertion point forward a character (or if a range is selected, moves to end of range).
 */
public void keyForward(boolean isShiftDown)
{
    // If shift is down, extend selection forward
    if(isShiftDown) {
        if(getSelAnchor()==getSelStart() && !isSelEmpty()) setSel(getSelStart()+1, getSelEnd());
        else { setSel(getSelStart(), getSelEnd()+1, getSelEnd()+1); }
        return;
    }
    
    // Get new selection index from current end
    int index = getSelEnd();
    
    // If selection empty but not at end, get next char (or after newline, if at newline) 
    if(isSelEmpty() && index<length())
        index = RMTextUtils.isLineEnd(this, index)? RMTextUtils.indexAfterNewline(this, index) : (index + 1);
        
    // Set new selection
    setSel(index);
}

/**
 * Moves the insertion point backward a character (or if a range is selected, moves to beginning of range).
 */
public void keyBackward(boolean isShiftDown)
{
    // If shift is down, extend selection back
    if(isShiftDown) {
        if(getSelAnchor()==getSelEnd() && !isSelEmpty()) setSel(getSelStart(), getSelEnd()-1);
        else { setSel(getSelStart()-1, getSelEnd(), getSelStart()-1); }
        return;
    }
    
    // Get new selection index from current start
    int index = getSelStart();
    
    // If selection empty but not at start, get previous char (or before newline if after newline)
    if(isSelEmpty() && index>0)
        index = RMTextUtils.isAfterLineEnd(this, index)? RMTextUtils.lastIndexOfNewline(this, index) : (index - 1);

    // Set new selection
    setSel(index);
}

/**
 * Moves the insertion point up a line, trying to preserve distance from beginning of line.
 */
public void keyUp()
{
    RMTextLayout layout = getLayout();
    RMTextLine lastColumnLine = layout.getLineForChar(getSelAnchor()); if(lastColumnLine==null) return;
    int lastColumn = getSelAnchor() - lastColumnLine.getStart();
    RMTextLine currentLine = getSelStartLine(); if(currentLine==null || currentLine.getIndex()==0) return;
    RMTextLine nextLine = getLine(currentLine.getIndex()-1);
    setSel(layout.getStart() + nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn));
}

/**
 * Moves the insertion point down a line, trying preserve distance from beginning of line.
 */
public void keyDown()
{
    RMTextLayout layout = getLayout();
    RMTextLine lastColumnLine = layout.getLineForChar(getSelAnchor()); if(lastColumnLine==null) return;
    int lastColumn = getSelAnchor() - lastColumnLine.getStart();
    RMTextLine currentLine = getSelStartLine();
    RMTextLine nextLine = currentLine.getNext(); if(nextLine==null) return;
    setSel(layout.getStart() + nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn));
}

/**
 * Moves the insertion point to the beginning of line.
 */
public void selectLineStart()
{
    // Get index at beginning of current line and index of first non-whitespace char and set selection
    int index1 = RMTextUtils.lastIndexAfterNewline(this, getSelEnd()); if(index1<0) index1 = 0;
    int index2 = index1; while(index2<length() && charAt(index2)==' ') index2++;
    setSel(!isSelEmpty() || index2!=getSelStart()? index2 : index1);
}

/**
 * Moves the insertion point to next newline or text end.
 */
public void selectLineEnd()
{
    // Get index of newline and set selection
    int index = RMTextUtils.indexOfNewline(this, getSelEnd());
    setSel(index>=0? index : length());
}

/**
 * Deletes the character in front of the insertion point.
 */
public void deleteForward()
{
    if(isSelEmpty() && getSelEnd()<length()) {
        int end = getSelEnd() + 1;
        if(RMTextUtils.isLineEnd(this, end - 1)) end = RMTextUtils.indexAfterNewline(this, end - 1);
        delete(getSelStart(), end, true);
    }
    else if(!isSelEmpty())
        delete();
}

/**
 * Deletes the characters from the insertion point to the end of the line.
 */
public void deleteToLineEnd()
{
    // If there is a current selection, just delete it
    if(!isSelEmpty())
        delete();
    
    // Otherwise, if at line end, delete line end
    else if(RMTextUtils.isLineEnd(this, getSelEnd()))
        delete(getSelStart(), RMTextUtils.indexAfterNewline(this, getSelStart()), true);

    // Otherwise delete up to next newline or line end
    else {
        int index = RMTextUtils.indexOfNewline(this, getSelStart());
        delete(getSelStart(), index>=0? index : length(), true);
    }
}

/**
 * Returns the width needed to display all characters.
 */
public float getWidthToFit()  { return (float)getLayout().getWidthToFit(); }

/**
 * Returns the height needed to display all characters.
 */
public float getHeightToFit()  { return (float)getLayout().getHeightToFit(); }

/**
 * Handles key events.
 */
public void processKeyEvent(KeyEvent anEvent)
{
    // Forward event to keyPressed/keyTyped
    if(anEvent.getID()==KeyEvent.KEY_PRESSED) keyPressed(anEvent);
    else if(anEvent.getID()==KeyEvent.KEY_TYPED) keyTyped(anEvent);
    
    // If key released, save changes
    if(anEvent.getID()==KeyEvent.KEY_RELEASED)
        undoerSaveChanges();
}

/**
 * Handle keyPressed.
 */
protected void keyPressed(KeyEvent anEvent)
{
    // Get event info
    int keyCode = anEvent.getKeyCode(); boolean isShiftDown = anEvent.isShiftDown();
    boolean isCommandDown = RMAWTUtils.isCommandDown(anEvent), isControlDown = RMAWTUtils.isControlDown(anEvent);
    
    // Handle command keys
    if(isCommandDown) {
    
        // If shift-down, just return
        if(isShiftDown && keyCode!=KeyEvent.VK_Z)
            return;
        
        // Handle common command keys
        switch(keyCode) {
            case KeyEvent.VK_X: cut(); break; // Handle command-x cut
            case KeyEvent.VK_C: copy(); break; // Handle command-c copy
            case KeyEvent.VK_V: paste(); break; // Handle command-v paste
            case KeyEvent.VK_A: selectAll(); break; // Handle command-a select all
            case KeyEvent.VK_Z: if(isShiftDown) redo(); else undo(); break; // Command-z undo
            default: return; // Any other command keys just return
        }
    }
    
    // Handle control keys (not applicable on Windows, since they are handled by command key code above)
    else if(isControlDown) {
        
        // If shift down, just return
        if(isShiftDown) return;
        
        // Handle common emacs key bindings
        switch(keyCode) {
            case KeyEvent.VK_F: keyForward(false); break; // Handle control-f key forward
            case KeyEvent.VK_B: keyBackward(false); break; // Handle control-b key backward
            case KeyEvent.VK_P: keyUp(); break; // Handle control-p key up
            case KeyEvent.VK_N: keyDown(); break; // Handle control-n key down
            case KeyEvent.VK_A: selectLineStart(); break; // Handle control-a line start
            case KeyEvent.VK_E: selectLineEnd(); break; // Handle control-e line end
            case KeyEvent.VK_D: deleteForward(); break; // Handle control-d delete forward
            case KeyEvent.VK_K: deleteToLineEnd(); break; // Handle control-k delete line to end
            default: return; // Any other control keys, just return
        }
    }
    
    // Handle supported non-character keys
    else switch(keyCode) {
        case KeyEvent.VK_TAB: replace("\t"); break; // Handle tab
        case KeyEvent.VK_ENTER: replace("\n"); break; // Handle enter
        case KeyEvent.VK_LEFT: keyBackward(isShiftDown); break; // Handle left arrow
        case KeyEvent.VK_RIGHT: keyForward(isShiftDown); break; // Handle right arrow
        case KeyEvent.VK_UP: keyUp(); break; // Handle up arrow
        case KeyEvent.VK_DOWN: keyDown(); break; // Handle down arrow
        case KeyEvent.VK_HOME: selectLineStart(); break; // Handle home key
        case KeyEvent.VK_END: selectLineEnd(); break; // Handle end key
        case KeyEvent.VK_BACK_SPACE: delete(); break; // Handle backspace key
        case KeyEvent.VK_DELETE: deleteForward(); break; // Handle delete key
        default: return; // Any other non-character key, just return
    }
    
    // Consume the event
    anEvent.consume();
}

/**
 * Handle keyTyped.
 */
protected void keyTyped(KeyEvent anEvent)
{
    char keyChar = anEvent.getKeyChar();
    boolean isCharDefined = keyChar!=KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar);
    boolean isCommandDown = RMAWTUtils.isCommandDown(anEvent), isControlDown = RMAWTUtils.isControlDown(anEvent);
    
    // If KEY_TYPED with defined char and no command/control modifier, call TextEditor.replace(), consume and return
    if(isCharDefined && !isCommandDown && !isControlDown) {
        replace(Character.toString(keyChar));
        anEvent.consume();
    }
}

/**
 * Handles mouse pressed.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Store the mouse down point
    _downX = anEvent.getX(); _downY = anEvent.getY();
    
    // Determine if word or paragraph selecting
    if(!anEvent.isShiftDown()) _wordSelecting = _pgraphSelecting = false;
    if(anEvent.getClickCount()==2) _wordSelecting = true;
    else if(anEvent.getClickCount()==3) _pgraphSelecting = true;
    
    // Get selection for down point
    RMTextSel sel = getSel(_downX, _downY, _downX, _downY);
    int start = sel.getStart(), end = sel.getEnd();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        if(start<=getSelStart()) end = getSelEnd();
        else start = getSelStart();
    }
    
    // Set selection
    setSel(start, end);
}

/**
 * Handles mouse dragged.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get selection for down point and drag point
    RMTextSel sel = getSel(_downX, _downY, anEvent.getX(), anEvent.getY());
    int start = sel.getStart(), end = sel.getEnd();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        if(start<=getSelStart()) end = getSelEnd();
        else start = getSelStart();
    }
    
    // Set selection
    setSel(start, end);
}

/**
 * Handles mouse released.
 */
public void mouseReleased(MouseEvent anEvent)  { }

/**
 * Paints a given layout in a given graphics.
 */
public void paint(Graphics2D g)
{
    // Get selection path
    GeneralPath path = getSelPath();

    // If empty selection, draw caret
    if(isSelEmpty() && path!=null) {
        g.setColor(Color.black); g.setStroke(RMAWTUtils.Stroke1); // Set color and stroke of cursor
        RMAWTUtils.setAntialiasing(g, false); g.draw(path); // Draw cursor (antialias off for sharpness)
        RMAWTUtils.setAntialiasing(g, true);
    }

    // If selection, get selection path and fill
    else {
        g.setColor(new Color(128, 128, 128, 128));
        g.fill(path);
    }

    // If spell checking, get path for misspelled words and draw
    if(isSpellChecking() && length()>0) {
        GeneralPath spath = getSpellingPath();
        if(spath!=null) {
            g.setColor(Color.red); g.setStroke(RMAWTUtils.StrokeDash1);
            g.draw(spath);
        }
    }
}

/**
 * Returns the path for the current selection.
 */
public GeneralPath getSelPath()
{
    RMTextLayout layout = getLayout();
    int layoutLength = layout.getStart() + layout.length();
    int start = getSelStart(); if(start>layoutLength) return null;
    int end = Math.min(getSelEnd(), layoutLength);
    return layout.getPathForChars(start - layout.getStart(), end - layout.getStart());
}

/**
 * Returns a path for misspelled word underlining.
 */
public GeneralPath getSpellingPath()
{
    // Get text layout
    RMTextLayout layout = getLayout();
    
    // Create new path for return
    GeneralPath path = new GeneralPath();
    
    // Get text string
    String string = getXString().getRepresentableString().getText();
    
    // Declare iteration variable
    RMSpellCheck.Word word = null;
    
    // Iterate over text
    for(word=RMSpellCheck.getMisspelledWord(string, 0); word!=null;
        word=RMSpellCheck.getMisspelledWord(string, word.getEnd())) {
        
        // Get word bounds
        int start = word.getStart(); if(start>layout.getStart() + layout.length()) break;
        int end = word.getEnd(); if(end>layout.getStart() + layout.length()) end = layout.getStart() + layout.length();
        
        // If text editor selection starts in word bounds, just continue - they are still working on this word
        if(start<=getSelStart() && getSelStart()<=end)
            continue;
        
        // Get the selection's start line index and end line index
        int startLineIndex = getLineForChar(start).getIndex();
        int endLineIndex = getLineForChar(end).getIndex();
        
        // Iterate over selected lines
        for(int i=startLineIndex; i<=endLineIndex; i++) { RMTextLine line = getLine(i);
            
            // Get the bounds of line
            double x1 = line.getX();
            double x2 = line.getMaxX();
            double y = line.getYBaseline() + 3;
            
            // If starting line, adjust x1 for starting character
            if(i==startLineIndex)
                x1 = line.getXForChar(start - line.getStart() - layout.getStart());
            
            // If ending line, adjust x2 for ending character
            if(i==endLineIndex)
                x2 = line.getXForChar(end - line.getStart() - layout.getStart());
            
            // Append rect for line to path
            path.moveTo((float)x1, (float)y); // Need to stay float until Java5 support not needed
            path.lineTo((float)x2, (float)y);
        }
    }
    
    // Return path
    return path;
}

}