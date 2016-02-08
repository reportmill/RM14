package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.text.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import snap.util.Undoer;

/**
 * A text editor subclass suitable for editing RMText shapes in the RMEditor.
 */
public class RMEditorTextEditor extends RMTextEditor implements PropertyChangeListener {
    
    // The RMEditor this editor text editor works for
    RMEditor                _editor;
    
    // The RMText shape this editor is editing
    RMTextShape             _textShape;
    
    // Whether editor should resize RMText whenever text changes
    boolean                 _updatingSize = false;
    
    // The minimum height of the RMText when editor text editor is updating size
    float                   _updatingMinHeight = 0;
    
    // Whether current event is pop-up trigger
    boolean                 _isPopUpTrigger;

/**
 * Creates an RMEditor text editor for the given RMEditor.
 */
public RMEditorTextEditor(RMEditor anOwner)  { _editor = anOwner; }

/**
 * Returns the RMText shape being edited.
 */
public RMTextShape getTextShape()  { return _textShape; }

/**
 * Sets the RMText shape to be edited.
 */
public void setTextShape(RMTextShape aText)
{
    // If given text is already editor text, just return
    if(aText==_textShape) return;
    
    // Stop listening to property changes in last text shape
    if(_textShape!=null)
        _textShape.removePropertyChangeListener(this);
    
    // Set editor text
    _textShape = aText;
    
    // Start listening to property changes in new text shape
    if(_textShape!=null)
        _textShape.addPropertyChangeListener(this);
    
    // Set text shape xstring
    setXStringFromTextShape();

    // Set that editor should resize RMText whenever text changes
    _updatingSize = false;
    
    // Set that minimum height for resizable RMText is zero
    _updatingMinHeight = 0;
}

/**
 * Sets the text editor xstring to text shape's xstring.
 */
protected void setXStringFromTextShape()
{
    // If no text, just return
    if(getTextShape()==null) return;
    
    // Set xstring
    setXString(getTextShape().getXString());
    
    // Reset selection
    setSel(getTextShape().length());    
}

/**
 * Returns whether text editor is updating size (usually when text is first created with text tool).
 */
public boolean isUpdatingSize()  { return _updatingSize; }

/**
 * Sets whether text editor is updating size (usually when text is first created with text tool).
 */
public void setUpdatingSize(boolean aValue)  { _updatingSize = aValue; }

/**
 * Returns the minimum height of the RMText when editor text editor is updating size.
 */
public float getUpdatingMinHeight()  { return _updatingMinHeight; }

/**
 * Sets the minimum height of the RMText when editor text editor is updating size.
 */
public void setUpdatingMinHeight(float aValue)  { _updatingMinHeight = aValue; }

/**
 * Returns the text layout for the RMText being edited.
 */
public RMTextLayout getLayout()  { return _textShape.getTextLayout(); }

/**
 * Override to forward to Editor.
 */
public void setSel(int aStart, int anEnd)
{
    super.setSel(aStart, anEnd);
    if(_editor!=null) _editor.setTextSelection(aStart, anEnd);
}

/**
 * Sets the attributes that are applied to current selection or newly typed chars.
 */
public void setInputAttribute(String aKey, Object aValue)
{
    // Set undo title
    if(getSelStart()!=getSelEnd())
        _editor.undoerSetUndoTitle(aKey + " Change");
    
    // Do normal set input attribute
    super.setInputAttribute(aKey, aValue);
}

/**
 * Sets the paragraph of the current selection or cursor position.
 */
public void setInputParagraph(RMParagraph ps)
{
    // Set undo title
    _editor.undoerSetUndoTitle("Paragraph Change");
    
    // Do normals setInputParagraph
    super.setInputParagraph(ps);
}

/**
 * Deletes the current selection.
 */
public void delete(int aStart, int anEnd, boolean doUpdateSelection)
{
    _editor.undoerSetUndoTitle("Delete");
    super.delete(aStart, anEnd, doUpdateSelection);
}

/**
 * Replaces the current selection with the given string.
 */
public void replace(String aString, Map theAttributes, int aStart, int anEnd, boolean doUpdateSelection)
{
    // Set undo title
    _editor.undoerSetUndoTitle("Text change");
    
    // Do normal replace
    super.replace(aString, theAttributes, aStart, anEnd, doUpdateSelection);
}

/**
 * Handles mouse pressed on the text editor.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Reset popup trigger ivar
    _isPopUpTrigger = anEvent.isPopupTrigger(); if(_isPopUpTrigger) return;
    
    // Do normal version with MouseEvent in TextShape coords
    super.mousePressed(getMouseEventInTextShape(anEvent));
}

/**
 * Handles mouse dragged on the text editor.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // If is popup trigger, just return
    if(_isPopUpTrigger) return;

    // If no _edText, then we must be moving a tableRow
    if(_textShape==null) {
        moveTableRowColumn(anEvent); return; }

    // Get mouse event in TextShape coords
    MouseEvent event = getMouseEventInTextShape(anEvent);

    // If text is a structured table row column and point is outside, start moveTableRow bit
    if(_textShape.isStructured() && ((event.getX()<0-20) ||
        (event.getX()>_textShape.getWidth()+10)) && _textShape.getParent().getChildCount()>1) {
        moveTableRowColumn(null); return; }

    // Do normal version with MouseEvent in TextShape coords
    super.mouseDragged(event);
}

/**
 * Handles mouse released on the text editor.
 */
public void mouseReleased(MouseEvent anEvent)
{
    // If moving table row column, do that, otherwise do normal version.
    if(_textShape==null)
        moveTableRowColumn(anEvent);
    else super.mouseReleased(getMouseEventInTextShape(anEvent));
}

/**
 * Returns the given MouseEvent converted to TextShape coords.
 */
private MouseEvent getMouseEventInTextShape(MouseEvent anEvent)
{
    Point2D point = _editor.convertPointToShape(anEvent.getPoint(), _textShape);
    MouseEvent event = new MouseEvent(anEvent.getComponent(), anEvent.getID(), anEvent.getWhen(),
        anEvent.getModifiers() | anEvent.getModifiersEx(), (int)Math.round(point.getX()), (int)Math.round(point.getY()),
        anEvent.getClickCount(), anEvent.isPopupTrigger());
    return event;
}

/**
 * Pastes the current clipboard data over the current selection.
 */
public void paste()
{
    // Get system clipboard and its contents (return if null)
    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = cb.getContents(null);
    if(contents==null)
        return;
    
    // If contents is RMDataFlavor, paste shape
    if(contents.isDataFlavorSupported(RMEditorClipboard.RMDataFlavor)) {
        
        // Unarchive shapes from clipboard bytes
        Object object = RMEditorClipboard.getShapesFromClipboard(_editor, contents);
        
        // If object is list, replace with first object
        if(object instanceof List)
            object = RMListUtils.get((List)object, 0);
        
        // If object is shape, paste into text
        if(object instanceof RMShape) {
            int selStart = getSelStart();
            replace("a");
            setSel(selStart, selStart+1);
            setInputAttribute(RMTextTypes.TEXT_EMBEDDED_SHAPE, object);
        }
    }
    
    // Otherwise, do normal paste
    else super.paste();
}

/**
 * Override so all property changes can trigger text shape repaint. 
 */
protected void firePropertyChange(PropertyChangeEvent anEvent)
{ super.firePropertyChange(anEvent); firedPropertyChange(); }

/**
 * Override so all property changes can trigger text shape repaint. 
 */
protected void firePropertyChange(String aName, Object oldVal, Object newVal, int anIndex)
{ super.firePropertyChange(aName, oldVal, newVal, anIndex); firedPropertyChange(); }

/**
 * Override so all property changes can trigger text shape repaint. 
 */
private void firedPropertyChange()
{
    // If no text shape, just return
    if(_textShape==null) return;
    
    // Repaint text shape (in case change represents selection change)
    _textShape.repaint();
    
    // If updating size, reset text width & height to accommodate text
    if(_updatingSize) {
        
        // Get preferred text shape size (with no newline Trim)
        double maxWidth = _updatingMinHeight==0? _textShape.getParent().getWidth() - _textShape.getX() :
            _textShape.getWidth();
        double prefWidth = _textShape.getPrefWidth(); if(prefWidth>maxWidth) prefWidth = maxWidth;
        double prefHeight = _textShape.getPrefHeight();

        // If width gets updated, get & set desired width (make sure it doesn't go beyond page border)
        if(_updatingMinHeight==0)
            _textShape.setWidth(prefWidth);

        // If PrefHeight or current height is greater than UpdatingMinHeight (which won't be zero if user drew a
        //  text box to enter text), set Height to PrefHeight
        if(prefHeight>_updatingMinHeight || _textShape.getHeight()>_updatingMinHeight)
            _textShape.setHeight(Math.max(prefHeight, _updatingMinHeight));
    }
}

/**
 * Called when there are changes to text shape.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Handle Text shape XString change
    if(anEvent.getSource()==getTextShape()) {
        if(anEvent.getPropertyName().equals("XString"))
            setXStringFromTextShape();
    }
    
    // Do normal version
    else super.propertyChange(anEvent);
}

/**
 * Override to get undoer from text shape.
 */
public Undoer getUndoer()  { return _textShape!=null? _textShape.getUndoer() : null; }

/**
 * Override to return selection for text shape.
 */
protected Object getUndoSelection()  { return new ETETextSelection();}

/**
 * A class for a text selection.
 */
public class ETETextSelection extends TextSelection {
    RMTextShape    textShape;
    ETETextSelection()  { textShape = _textShape; }
    public boolean equals(Object anObj) { ETETextSelection other = (ETETextSelection)anObj;
        return textShape==other.textShape && super.equals(other); }
    public int hashCode() { return start + end; }
    public void setSelection()  {
        _editor.setSuperSelectedShape(textShape);
        super.setSelection();
    }
}

/**
 * Override to forward to editor.
 */
public void undo()  { _editor.undo(); }

/**
 * Override to forward to editor.
 */
public void redo()  { _editor.redo(); }

/**
 * Move Table Row Column stuff (table row column re-ordering).
 */
public void moveTableRowColumn(MouseEvent anEvent)
{
    // Get editor, editor SelectedShape and TableRow
    RMEditor editor = RMEditor.getMainEditor();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    RMTableRow tableRow = (RMTableRow)shape.getParent(); tableRow.repaint();
    
    // If first time through (event is null), set undo title, de-select column text and return
    if(anEvent==null) {
        tableRow.undoerSetUndoTitle("Reorder columns");
        editor.setSelectedShape(shape); return; }
    
    // Get event x in TableRow coords and whether point is in TableRow
    RMPoint point = editor.convertPointToShape(anEvent.getPoint(), tableRow); point.y = 2;
    boolean inRow = tableRow.contains(point);
    
    // Handle MouseDragged: layout children by X (if outside row, skip drag shape)
    if(anEvent.getID()==MouseEvent.MOUSE_DRAGGED) {
        List <RMShape> children = new ArrayList(tableRow.getChildren()); RMSort.sort(children, "Frame.X"); float x = 0;
        for(RMShape child : children) {
            if(child==shape) { if(inRow) child.setX(point.x-child.getWidth()/2); else { child.setX(9999); continue; }}
            else child.setX(x); x += child.getWidth(); }
        tableRow.setNeedsLayout(false);
    }
    
    // Handle MouseReleased: reset children
    else if(anEvent.getID()==MouseEvent.MOUSE_RELEASED) {

        // If shape in row, set new index
        if(inRow) {
            int iold = shape.indexOf();
            int inew = 0; while(inew<tableRow.getChildCount() && tableRow.getChild(inew).getX()<=shape.getX()) inew++;
            if(iold!=inew) {
                tableRow.removeChild(iold); if(inew>iold) inew--;
                tableRow.addChild(shape, inew);
            }
        }
        
        // If shape is outside row, remove it
        else {
            tableRow.removeChild(shape);
            editor.setSuperSelectedShape(tableRow);
        }

        // Do layout again to snap shape back into place
        tableRow.layout();
    }
}

}