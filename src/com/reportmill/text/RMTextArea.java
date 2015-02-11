package com.reportmill.text;
import com.reportmill.base.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.beans.*;
import javax.swing.*;
import snap.swing.ClipboardUtils;

/**
 * A text editor subclass for editing text in a Swing component.
 */ 
public class RMTextArea extends JComponent implements Scrollable, DropTargetListener, PropertyChangeListener {

    // The text margin
    Insets                  _margin = new Insets(0,0,0,0);

    // Whether to wrap lines to pane width
    boolean                 _lineWrap;
    
    // The text editor object really holds all the text editing logic (TEP provides only events and a place to draw)
    RMTextEditor            _textEditor;
   
/**
 * Creates a new text area.
 */
public RMTextArea()
{
    // Enable mouse, key and focus events
    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    enableEvents(AWTEvent.FOCUS_EVENT_MASK);
    
    // Indicate that we want to accept focus with mouse presses
    setRequestFocusEnabled(true);
    
    // Create a new drop target
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    
    // Set background to white
    setBackground(Color.white);
}

/**
 * Returns the text margins.
 */
public Insets getMargin()  { return _margin; }

/**
 * Sets the text margins.
 */
public void setMargin(Insets theInsets)
{
    // If value already set, just return
    if(_margin.equals(theInsets)) return;
    
    // Set new margin
    _margin.set(theInsets.top, theInsets.left, theInsets.bottom, theInsets.right);
    
    // Reset text bounds
    getTextEditor().setBounds(getTextBounds());
}

/**
 * Returns the text rect.
 */
public Rectangle2D getTextBounds()
{
    double x = getMargin().left, y = getMargin().top;
    double w = getLineWrap()? getWidth() - x - getMargin().right : Float.MAX_VALUE;
    return new Rectangle2D.Double(x, y, w, Float.MAX_VALUE);
}

/**
 * Returns whether to wrap lines to pane width.
 */
public boolean getLineWrap()  { return _lineWrap; }

/**
 * Sets whether to wrap lines to pane width.
 */
public void setLineWrap(boolean aValue)
{
    // Set line wrap
    _lineWrap = aValue;
    
    // Reset text bounds
    getTextEditor().setBounds(getTextBounds());
}

/**
 * Returns the text editor associated with this text area.
 */
public RMTextEditor getTextEditor()
{
    if(_textEditor==null)
        setTextEditor(createTextEditor());
    return _textEditor;
}

/**
 * Sets the text editor associated with this text area.
 */
public void setTextEditor(RMTextEditor aTE)
{
    // If there is an existing listener, remove it
    if(_textEditor!=null)
        _textEditor.removePropertyChangeListener(this);
    
    // Set new text editor
    _textEditor = aTE;
    
    // Add listener back
    _textEditor.addPropertyChangeListener(this);
    _textEditor.setBounds(getTextBounds());
}

/**
 * Creates a new text editor.
 */
public RMTextEditor createTextEditor()  { return new RMTextEditor(); }

/**
 * Returns text string of text editor.
 */
public String getText()  { return getTextEditor().getString(); }

/**
 * Set text string of text editor.
 */
public void setText(String aString)
{
    RMFont font = new RMFont(getFont().getName(), getFont().getSize2D());
    getTextEditor().setXString(new RMXString(aString, font));
    revalidate(); repaint();
}

/**
 * Append Text.
 */
public void append(String aString)
{
    // If bogus string, just return
    if(aString==null || aString.length()==0) return;
    
    // Add text to text area
    getTextEditor().replace(aString, length(), length(), true);
}

/**
 * Append Text.
 */
public void appendln(String aString)  { append(aString); if(!aString.endsWith("\n")) append("\n"); }

/**
 * Appends an XString.
 */
public void append(RMXString anXString)  { getTextEditor().replace(anXString, length(), length(), true); }

/**
 * Returns the text length.
 */
public int length()  { return getTextEditor().length(); }

/**
 * Convenience - returns text editor string.
 */
public RMXString getXString()  { return getTextEditor().getXString(); }

/**
 * Convenience - sets text editor string.
 */
public void setXString(RMXString aString)
{
    getTextEditor().setXString(aString);
    revalidate(); repaint();
}

/**
 * Convenience - returns text editor font scale.
 */
public double getFontScale()  { return getTextEditor().getFontScale(); }

/**
 * Convenience - sets text editor font scale.
 */
public void setFontScale(double aScale)  { getTextEditor().setFontScale(aScale); }

/**
 * When text editor changes Selection or XString changes Runs or XStringRuns change Chars/Attribute, repaint. 
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // If selection changes, set visible rect around new selection
    if(anEvent.getPropertyName().equals("Selection")) {
        Rectangle rect = getVisibleRect();
        Rectangle rect2 = getTextEditor().getSelPath().getBounds();
        if(rect2.getWidth()==0) rect2.setRect(rect2.getX(), rect2.getY(), 1, rect2.getHeight());
        if(!rect.contains(rect2))
            scrollRectToVisible(rect2);
    }
    
    // Any other change should do a revalidate in case bounds changed
    else revalidate();
    
    // Repaint for any change
    repaint();
}

/**
 * Overrides JComponent to forward key event to text editor.
 */
protected void processComponentKeyEvent(KeyEvent anEvent)
{
    // Forward to text editor and hide cursor
    _textEditor.processKeyEvent(anEvent);
    hideCursor();
}

/**
 * Overrides JComponent method to forward mouse pressed/released to text editor.
 */
public void processMouseEvent(MouseEvent anEvent)
{
    // Do normal version
    super.processMouseEvent(anEvent);
    
    // Forward mouse pressed to text editor and request repaint
    if(anEvent.getID()==MouseEvent.MOUSE_PRESSED) {
        _textEditor.mousePressed(anEvent);
        repaint();
    }
    
    // Forward mouse released to text editor and request repaint
    else if(anEvent.getID()==MouseEvent.MOUSE_RELEASED) {
        _textEditor.mouseReleased(anEvent);
        repaint();
        requestFocus();
    }
}

/**
 * Override to show hidden cursor.
 */
protected void processFocusEvent(FocusEvent e)  { super.processFocusEvent(e); showCursor(); }

/**
 * Overrides JComponent method to forward mouse drags to text editor.
 */
public void processMouseMotionEvent(MouseEvent anEvent)
{
    // Do normal version
    super.processMouseMotionEvent(anEvent);
    
    // Forward mouse dragged to text editor and request repaint
    if(anEvent.getID()==MouseEvent.MOUSE_DRAGGED) {
        _textEditor.mouseDragged(anEvent);
        repaint();
    }
    
    // If mouse moved, change cursor to text
    else if(anEvent.getID()==MouseEvent.MOUSE_MOVED)
        showCursor();
}

/**
 * Shows the cursor.
 */
public void showCursor()
{
    if(getCursor()!=Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
}

/**
 * Hides the cursor.
 */
public void hideCursor()  { if(getCursor()!=RMAWTUtils.getHiddenCursor()) setCursor(RMAWTUtils.getHiddenCursor()); }

/** Overrides JComponent method so we get VK_TAB events. */
public boolean isManagingFocus()  { return true; }

/**
 * Overrides JComponent to paint text editor text.
 */
public void paintComponent(Graphics g)
{
    // Get graphics2D and cache/set RenderingHints
    Graphics2D g2 = (Graphics2D)g;
    RenderingHints hints = g2.getRenderingHints(); setRenderingHints(g2);
    
    // Paint Background, TextEditor and text
    paintBackground(g2);
    paintTextEditor(g2);
    paintText(g2);
    
    // Restore original rendering hints
    g2.setRenderingHints(hints);
}

/**
 * Paints the layout in a given graphics.
 */
protected void paintText(Graphics2D aGraphics)
{
    // Get clip bounds
    Rectangle clip = aGraphics.getClipBounds();
    double miny = clip.getMinY(), maxy = clip.getMaxY();
    
    // Iterate through runs and paint them
    RMTextLayout layout = _textEditor.getLayout();
    for(RMTextRun run=layout.getRun(); run!=null; run=run.getNext()) {
        if(run.getMaxY()<miny) continue; if(run.getY()>maxy) break;
        paintTextRun(aGraphics, run);
    }
}

/**
 * Paints the layout in a given graphics.
 */
protected void paintTextRun(Graphics2D g, RMTextRun aRun)
{
    // Set run font/color
    g.setFont(aRun.getFont().awt());
    g.setColor(aRun.getColor().awt());

    // Get run x & y
    float x = (float)aRun.getX();
    float y = (float)aRun.getYBaseline();

    // If outlined, draw outlines
    if(aRun.getOutline()!=null) { RMXString.Outline outline = aRun.getOutline();
            
        // Get run glyph vector and its outline
        GlyphVector gv = aRun.glyphVector(g);
        Shape shape = gv.getOutline(x, y);
            
        // If fill color exists, fill glyph vector
        if(outline.getFillColor()!=null) {
            g.setColor(outline.getFillColor().awt()); g.fill(shape);
            g.setColor(aRun.getColor().awt());
        }
            
        // Stroke glyph vector
        g.setStroke(RMAWTUtils.getStroke(outline.getStrokeWidth()));
        g.draw(shape);
    }

    // If printing, just draw string
    else if(aRun.getCharSpacing()==0)
        g.drawString(RMStringUtils.trimEnd(aRun.toString()), x, y);

    // If not printing, get GlyphVector for run and draw - I think we did this because Mac used to do kerning
    else {
        GlyphVector gv = aRun.glyphVector(g);
        g.drawGlyphVector(gv, x, y);
    }
        
    // Draw underline
    if(aRun.isUnderlined()) {
        g.setStroke(new BasicStroke((float)aRun.getUnderlineStroke(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        double uly = y - aRun.getUnderlineY();
        RMAWTUtils.drawLine(g, x, uly, aRun.getMaxX(), uly);
    }
}

/**
 * Paints a given layout in a given graphics.
 */
public void paintTextEditor(Graphics2D g)
{
    // Get selection path
    GeneralPath path = _textEditor.getSelPath();

    // If empty selection, draw caret
    if(_textEditor.isSelEmpty() && path!=null) {
        
        // Set color and stroke of cursor
        g.setColor(Color.black);
        g.setStroke(RMAWTUtils.Stroke1);
            
        // Draw cursor (antialias off for sharpness)
        RMAWTUtils.setAntialiasing(g, false); g.draw(path);
        RMAWTUtils.setAntialiasing(g, true);
    }

    // If selection, get selection path and fill
    else {
        g.setColor(new Color(128, 128, 128, 128));
        g.fill(path);
    }

    // If spell checking, get path for misspelled words and draw
    if(_textEditor.isSpellChecking() && length()>0) {

        // Get path for misspelled words and draw
        GeneralPath spellPath = _textEditor.getSpellingPath();
        if(spellPath!=null) {
            g.setColor(Color.red); g.setStroke(RMAWTUtils.StrokeDash1);
            g.draw(spellPath);
        }
    }
}

/**
 * Paints background.
 */
protected void paintBackground(Graphics2D aGraphics)
{
    aGraphics.setColor(getBackground());
    aGraphics.fillRect(0, 0, getWidth(), getHeight());
}

/**
 * Sets rendering hints for text render.
 */
protected void setRenderingHints(Graphics2D aGraphics)
{
    aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    aGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    if(getTextEditor().getLayout().getUseFractionalMetrics())
        aGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    else aGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
}

/**
 * Overrides JComponent method to declare minimum size to be current size.
 */
public Dimension getMinimumSize()  { return getPreferredSize(); }

/**
 * Overrides JComponent method to declare preferred size to be text editor's optimal height.
 */
public Dimension getPreferredSize()
{
    int w = getLineWrap()? getParent().getWidth() : (int)Math.ceil(_textEditor.getWidthToFit());
    int h = (int)Math.ceil(_textEditor.getHeightToFit());
    return new Dimension(w, h);
}

/**
 * Overrides JComponent method to tell text editor the bounds.
 */
public void setBounds(int x, int y, int w, int h)
{
    // Do normal set bounds
    super.setBounds(x, y, w, h);
    
    // If doing line wrap, set text editor max size
    getTextEditor().setBounds(getTextBounds());
}

/** Scrollable method. */
public Dimension getPreferredScrollableViewportSize()  { return getPreferredSize(); }

/** Scrollable method. */
public boolean getScrollableTracksViewportWidth() { return getLineWrap() || getParent().getWidth()>getPreferredSize().width; }

/** Scrollable method. */
public boolean getScrollableTracksViewportHeight() { return getParent().getHeight()>getPreferredSize().height; }

/** Scrollable method. */
public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
{
    return orientation==SwingConstants.VERTICAL? visibleRect.height : visibleRect.width;
}

/** Scrollable method. */
public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
{
    return orientation==SwingConstants.VERTICAL? visibleRect.height/10 : visibleRect.width/10;
}

/** DropTargetListener method. */
public void dragEnter(DropTargetDragEvent dtde)
{
    // Cache current selection
    _dragStartSelectionStart = getSelStart();
    _dragStartSelectionEnd = getSelEnd();
    
    // Set selection to drag point
    getTextEditor().setSel(getTextEditor().getLayout().getCharIndex(dtde.getLocation()));
}

// Drag start selection start/end
private int  _dragStartSelectionStart, _dragStartSelectionEnd;

/** DropTargetListener method. */
public void dragExit(DropTargetEvent dte)
{
    getTextEditor().setSel(_dragStartSelectionStart, _dragStartSelectionEnd);
}

/** DropTargetListener method. */
public void dragOver(DropTargetDragEvent dtde)
{
    if(dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        
        // Accept drag
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
        
        // Set selection to drag point
        getTextEditor().setSel(getTextEditor().getLayout().getCharIndex(dtde.getLocation()));
    }
}

/** DropTargetListener method. */
public void drop(DropTargetDropEvent dtde)
{
    // Accept drag
    if(dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        getTextEditor().replace(ClipboardUtils.getString(dtde.getTransferable()));
        dtde.dropComplete(true);
    }
    
    // Otherwise restore selection
    else getTextEditor().setSel(_dragStartSelectionStart, _dragStartSelectionEnd);
}

/** DropTargetListener method. */
public void dropActionChanged(DropTargetDragEvent dtde)  { }

/**
 * Sets the character index of the text cursor.
 */
public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int anEnd)  { getTextEditor().setSel(aStart, anEnd); }

/**
 * Returns the selection start.
 */
public int getSelStart()  { return getTextEditor().getSelStart(); }

/**
 * Returns the selection end.
 */
public int getSelEnd()  { return getTextEditor().getSelEnd(); }

/**
 * Returns the selection anchor.
 */
public int getSelAnchor()  { return getTextEditor().getSelAnchor(); }

/**
 * Returns the char index for given point in text coordinate space.
 */
public int getCharIndex(Point2D aPoint)  { return getTextEditor().getCharIndex(aPoint); }

/**
 * Return AWT Font for current selection.
 */
public Font getFont()  { return getTextEditor().getFont().awt(); }

/**
 * Set AWT Font for current selection.
 */
public void setFont(Font aFont)  { getTextEditor().setFont(new RMFont(aFont.getName(), aFont.getSize2D())); }

}