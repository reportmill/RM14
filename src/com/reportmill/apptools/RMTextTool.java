package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.text.*;
import com.reportmill.shape.*;
import com.reportmill.graphics.RMStroke;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.text.DecimalFormat;
import java.util.List;
import snap.swing.*;

/**
 * This class provides Swing UI editing for text shapes.
 */
public class RMTextTool <T extends RMTextShape> extends RMTool <T> {
    
    // The text area
    RMTextArea        _textArea;
    
    // The shape hit by text tool on mouse down
    RMShape           _downShape;

    // Format used for line height controls
    DecimalFormat     _format = new DecimalFormat("0.##");

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get the text area
    _textArea = getNode("TextPane", RMTextArea.class);
    
    // Replace TextEditor with one that suppresses undoerAddPropertyChange since Editor.TextEditor does this
    _textArea.setTextEditor(new RMTextEditor() {
        protected void undoerAddPropertyChange(PropertyChangeEvent anEvent)  { } });
    
    // Capture TextArea KeyEvents so we can send to Editor.TextEditor (and MouseEvents so we can update its selection)
    enableEvents(_textArea, KeyPressed, KeyTyped, KeyReleased, MouseReleased);
    
    // Configure the format
    _format.setDecimalSeparatorAlwaysShown(false);
}

/**
 * Refreshes UI controls from currently selected text shape.
 */
public void resetUI()
{
    // Get editor and currently selected text
    RMEditor editor = RMEditor.getMainEditor();
    RMTextShape text = getSelectedShape(); if(text==null) return;
    
    // Get paragraph from text
    RMParagraph pgraph = text.getXString().getParagraphAt(0);
    
    // If editor is text editing, get paragraph from text editor instead
    if(editor.getTextEditor()!=null)
        pgraph = editor.getTextEditor().getInputParagraph();
    
    // Update AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    setNodeValue("AlignLeftButton", pgraph.getAlignmentX()==RMTypes.AlignX.Left);
    setNodeValue("AlignCenterButton", pgraph.getAlignmentX()==RMTypes.AlignX.Center);
    setNodeValue("AlignRightButton", pgraph.getAlignmentX()==RMTypes.AlignX.Right);
    setNodeValue("AlignFullButton", pgraph.getAlignmentX()==RMTypes.AlignX.Full);
    setNodeValue("AlignTopButton", text.getAlignmentY()==RMTypes.AlignY.Top);
    setNodeValue("AlignMiddleButton", text.getAlignmentY()==RMTypes.AlignY.Middle);
    setNodeValue("AlignBottomButton", text.getAlignmentY()==RMTypes.AlignY.Bottom); // Update AlignBottomButton
    
    // Set the xstring in text inspector
    RMXString xstring = text.getXString();
    if(!_textArea.isFocusOwner())
        _textArea.setXString(xstring);
    
    // Get text's background color
    Color color = null;
    for(RMShape shape=text; color==null && shape!=null;) {
        if(shape.getFill()==null)
            shape = shape.getParent();
        else color = shape.getFill().getColor().awt();
    }
    
    // Set text area background color to text shape background color
    _textArea.setBackground(color==null? Color.white : color);
    
    // Get xstring font size and scale up to 12pt if any string run is smaller
    double fontSize = 12;
    for(int i=0, iMax=xstring==null? 0 : xstring.getRunCount(); i<iMax; i++)
         fontSize = Math.min(fontSize, xstring.getRun(i).getFont().getSize());

    // Set text editor font scale
    _textArea.setFontScale(fontSize<12? 12/fontSize : 1);

    // Update PaginateRadio, ShrinkRadio, GrowRadio
    setNodeValue("PaginateRadio", text.getWraps()==RMTextShape.WRAP_BASIC);
    setNodeValue("ShrinkRadio", text.getWraps()==RMTextShape.WRAP_SCALE);
    setNodeValue("GrowRadio", text.getWraps()==RMTextShape.WRAP_NONE);
    
    // Update CharSpacingThumb and CharSpacingText
    setNodeValue("CharSpacingThumb", RMEditorShapes.getCharSpacing(editor));
    setNodeValue("CharSpacingText", RMEditorShapes.getCharSpacing(editor));
    
    // Update LineSpacingThumb and LineSpacingText
    setNodeValue("LineSpacingThumb", RMEditorShapes.getLineSpacing(editor));
    setNodeValue("LineSpacingText", RMEditorShapes.getLineSpacing(editor));
    
    // Update LineGapThumb and LineGapText
    setNodeValue("LineGapThumb", RMEditorShapes.getLineGap(editor));
    setNodeValue("LineGapText", RMEditorShapes.getLineGap(editor));
    
    // Get current line height min
    float lineHeightMin = RMEditorShapes.getLineHeightMin(editor);
    
    // If line height min not set (0), update LineHeightMinSpinner with current font size in light gray
    if(lineHeightMin==0) {
        setNodeValue("LineHeightMinSpinner", RMEditorShapes.getFont(editor).getSize());
        setNodeValue("LineHeightMinSpinner", Color.lightGray);
    }
    
    // If valid line height min, update LineHeightMinSpinner with line height in black
    else {
        setNodeValue("LineHeightMinSpinner", lineHeightMin);
        setNodeValue("LineHeightMinSpinner", Color.black);        
    }
    
    // Get current line height max
    float lineHeightMax = RMEditorShapes.getLineHeightMax(editor);
    
    // If line height max not set, update LineHeightMaxSpinner with current font size in light gray
    if(lineHeightMax>999) {
        setNodeValue("LineHeightMaxSpinner", RMEditorShapes.getFont(editor).getSize());
        setNodeValue("LineHeightMaxSpinner", Color.lightGray);
    }
    
    // If line height max is set, update LineHeightMaxSpinner with line height max in color black
    else {
        setNodeValue("LineHeightMaxSpinner", lineHeightMax);
        setNodeValue("LineHeightMaxSpinner", Color.black);        
    }
}

/**
 * Handles changes from UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get editor, currently selected text shape and text shapes (just return if null)
    RMEditor editor = getEditor();
    RMTextShape text = getSelectedShape(); if(text==null) return;
    List <RMTextShape> texts = (List)getSelectedShapes();
    
    // Register repaint for texts
    RMShapeUtils.repaint(texts);
    
    // Handle TextArea: Send KeyEvents to Editor.TextEditor (and update its selection after MouseEvents)
    if(anEvent.getTarget()==_textArea) {
        
        // Get Editor TextEditor (if not yet installed, SuperSelect text and try again)
        RMEditorTextEditor ted = editor.getTextEditor();
        if(ted==null) {
            getEditor().setSuperSelectedShape(text);
            ted = editor.getTextEditor(); if(ted==null) return;
        }
        
        // If KeyEvent, reroute to Editor.TextEditor
        if(anEvent.isKeyEvent()) {
            ted.processKeyEvent(anEvent.getKeyEvent()); anEvent.consume();
            if(anEvent.isKeyPressed()) _textArea.hideCursor();
            _textArea.setSel(ted.getSelStart(), ted.getSelEnd());
        }
        
        // If MouseEvent, update Editor.TextEditor selection
        if(anEvent.isMouseReleased())
            ted.setSel(_textArea.getSelStart(), _textArea.getSelEnd(), _textArea.getSelAnchor());
    }
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    if(anEvent.equals("AlignLeftButton")) RMEditorShapes.setAlignmentX(editor, RMTypes.AlignX.Left);
    if(anEvent.equals("AlignCenterButton")) RMEditorShapes.setAlignmentX(editor, RMTypes.AlignX.Center);
    if(anEvent.equals("AlignRightButton")) RMEditorShapes.setAlignmentX(editor, RMTypes.AlignX.Right);
    if(anEvent.equals("AlignFullButton")) RMEditorShapes.setAlignmentX(editor, RMTypes.AlignX.Full);
    if(anEvent.equals("AlignTopButton")) for(RMTextShape txt : texts) txt.setAlignmentY(RMTypes.AlignY.Top);
    if(anEvent.equals("AlignMiddleButton")) for(RMTextShape txt : texts) txt.setAlignmentY(RMTypes.AlignY.Middle);
    if(anEvent.equals("AlignBottomButton")) for(RMTextShape txt : texts) txt.setAlignmentY(RMTypes.AlignY.Bottom);
    
    // If RoundingThumb or RoundingText, make sure shapes have stroke
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText"))
        for(RMTextShape t : texts) t.setStroke(new RMStroke());

    // Handle PaginateRadio, ShrinkRadio, GrowRadio
    if(anEvent.equals("PaginateRadio")) for(RMTextShape txt : texts) txt.setWraps(RMTextShape.WRAP_BASIC);
    if(anEvent.equals("ShrinkRadio")) for(RMTextShape txt : texts) txt.setWraps(RMTextShape.WRAP_SCALE);
    if(anEvent.equals("GrowRadio")) for(RMTextShape txt : texts) txt.setWraps(RMTextShape.WRAP_NONE);
    
    // Handle CharSpacingThumb/CharSpacingText - have RMEditor set char spacing on currently selected texts
    if(anEvent.equals("CharSpacingThumb") || anEvent.equals("CharSpacingText"))
        RMEditorShapes.setCharSpacing(editor, anEvent.getFloatValue());
    
    // Handle LineSpacingThumb/LineSpacingText - have RMEditor set line spacing on currently selected texts
    if(anEvent.equals("LineSpacingThumb") || anEvent.equals("LineSpacingText"))
        RMEditorShapes.setLineSpacing(editor, anEvent.getFloatValue());

    // Handle LineSpacingSingleButton, LineSpacingDoubleButton
    if(anEvent.equals("LineSpacingSingleButton")) RMEditorShapes.setLineSpacing(editor, 1);
    if(anEvent.equals("LineSpacingDoubleButton")) RMEditorShapes.setLineSpacing(editor, 2);

    // Handle LineGapThumb/LineGapText - have RMEditor set line gap on currently selected texts
    if(anEvent.equals("LineGapThumb") || anEvent.equals("LineGapText"))
        RMEditorShapes.setLineGap(editor, anEvent.getFloatValue());

    // Handle LineHeightMinSpinner - set line height
    if(anEvent.equals("LineHeightMinSpinner"))
        RMEditorShapes.setLineHeightMin(editor, Math.max(anEvent.getFloatValue(), 0));

    // Handle LineHeightMaxSpinner - set line height max to value
    if(anEvent.equals("LineHeightMaxSpinner")) {
        float value = anEvent.getFloatValue(); if(value>=999) value = Float.MAX_VALUE;
        RMEditorShapes.setLineHeightMax(editor, value);
    }
    
    // Handle MakeMinWidthMenuItem, MakeMinHeightMenuItem
    if(anEvent.equals("MakeMinWidthMenuItem")) for(RMTextShape txt : texts) txt.setWidth(txt.getBestWidth());
    if(anEvent.equals("MakeMinHeightMenuItem")) for(RMTextShape txt : texts) txt.setHeight(txt.getBestHeight());
    
    // Handle TurnToPathMenuItem
    if(anEvent.equals("TurnToPathMenuItem"))
        for(int i=0; i<texts.size(); i++) {
            RMTextShape text1 = texts.get(i);
            RMShape textPathShape = RMTextShapeUtils.getTextPathShape(text1);
            RMParentShape parent = text1.getParent();
            parent.addChild(textPathShape, text1.indexOf());
            parent.removeChild(text1);
            editor.setSelectedShape(textPathShape);
        }
    
    // Handle TurnToCharsShapeMenuItem
    if(anEvent.equals("TurnToCharsShapeMenuItem"))
        for(int i=0; i<texts.size(); i++) {
            RMTextShape text1 = texts.get(i);
            RMShape textCharsShape = RMTextShapeUtils.getTextCharsShape(text1);
            RMParentShape parent = text1.getParent();
            parent.addChild(textCharsShape, text1.indexOf());
            parent.removeChild(text1);
            editor.setSelectedShape(textCharsShape);
        }
    
    // Handle LinkedTextMenuItem
    if(anEvent.equals("LinkedTextMenuItem")) {
        
        // Get linked text identical to original text and add to text's parent
        RMLinkedText linkedText = new RMLinkedText(text);
        text.getParent().addChild(linkedText);
        
        // Shift linked text down if there's room, otherwise right, otherwise just offset by quarter inch
        if(text.getFrameMaxY() + 18 + text.getFrame().height*.75 < text.getParent().getHeight())
            linkedText.offsetXY(0, text.getHeight() + 18);
        else if(text.getFrameMaxX() + 18 + text.getFrame().width*.75 < text.getParent().getWidth())
            linkedText.offsetXY(text.getWidth() + 18, 0);
        else linkedText.offsetXY(18, 18);
        
        // Select and repaint new linked text
        editor.setSelectedShape(linkedText);
        linkedText.repaint();
    }    
}

/**
 * Overrides standard tool method to deselect any currently editing text.
 */
public void activateTool()
{
    if(getEditor().getSuperSelectedShape() instanceof RMTextShape)
        getEditor().setSuperSelectedShape(getEditor().getSuperSelectedShape().getParent());
}

/**
 * Event handling - overridden to install text cursor.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Insall text cursor if missing
    if(getEditor().getCursor()!=Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
}

/**
 * Handles mouse pressed for text tool. Special support to super select any text hit by tool mouse pressed.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Register all selectedShapes dirty because their handles will probably need to be wiped out
    RMShapeUtils.repaint(getEditor().getSelectedShapes());

    // Get shape hit by down point
    _downShape = getEditor().getShapeAtPoint(anEvent.getPoint());
    
    // Get _downPoint from editor
    _downPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    
    // Create default text instance and set initial bounds to reasonable value
    _shape = new RMTextShape();
    _shape.setFrame(getDefaultBounds((RMTextShape)_shape, _downPoint));
    
    // Add shape to superSelectedShape (within an undo grouping) and superSelect
    getEditor().undoerSetUndoTitle("Add Text");
    getEditor().getSuperSelectedParentShape().addChild(_shape);
    getEditor().setSuperSelectedShape(_shape);
    getEditor().getTextEditor().setUpdatingSize(true);
}

/**
 * Handles mouse dragged for tool. If user doesn't really drag, then default text box should align the base line
 * of the text about the pressed point. If they do really drag, then text box should be the rect they drag out.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // If shape wasn't created in mouse down, just return
    if(_shape==null) return;
    
    // Set shape to repaint
    _shape.repaint();
    
    // Get event point in shape coords
    RMPoint point = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    
    // Convert point to parent
    _shape.convertPointToShape(point, _shape.getParent());
    
    // Get new bounds rect from down point and drag point
    Rectangle2D rect = new RMRect(point, _downPoint);
    
    // Get text default bounds
    Rectangle2D defaultBounds = getDefaultBounds((RMTextShape)_shape, _downPoint);

    // If drag rect less than default bounds, reset
    if(rect.getWidth()<defaultBounds.getWidth() || rect.getHeight()<defaultBounds.getHeight()) {
        rect = defaultBounds;
        getEditor().getTextEditor().setUpdatingMinHeight(0);
    }
    
    // If drag rect is outside of default bounds, set text bounds to drag rect
    else {
        RMEditor ed = getEditor();
        RMEditorTextEditor ted = ed.getTextEditor();
        ted.setUpdatingMinHeight((float)rect.getHeight());
    }
    
    // Set new shape bounds
    _shape.setFrame(rect);
}

/**
 * Event handling for text tool mouse loop.
 */
public void mouseReleased(MouseEvent e)
{
    // Get event point in shape coords
    RMPoint upPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    
    // Convert point to parent
    _shape.convertPointToShape(upPoint, _shape.getParent());
    
    // If upRect is really small, see if the user meant to conver a shape to text instead
    if(Math.abs(_downPoint.x - upPoint.x)<=3 && Math.abs(_downPoint.y - upPoint.y)<=3) {
        
        // If hit shape is text, just super-select that text and return
        if(_downShape instanceof RMTextShape) {
            _shape.removeFromParent();
            getEditor().setSuperSelectedShape(_downShape);
        }
        
        // If hit shape is Rectangle, Oval or Polygon, swap for RMText and return
        else if((_downShape instanceof RMRectShape || _downShape instanceof RMOvalShape ||
                _downShape instanceof RMPolygonShape || _downShape instanceof RMStarShape) &&
                (!_downShape.isLocked())) {
            _shape.removeFromParent();
            convertToText(_downShape, null);
        }
    }
    
    // Set editor current tool to select tool
    getEditor().setCurrentToolToSelectTool();
    
    // Reset tool shape
    _shape = null;
}

/**
 * Event handling for shape editing (just forwards to text editor).
 */
public void mousePressed(T aTextShape, MouseEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aTextShape)) return;
    
    // Forward on to editor
    getEditor().getTextEditor(true).mousePressed(anEvent);
    
    // Consume event
    anEvent.consume();
}

/**
 * Event handling for shape editing (just forwards to text editor).
 */
public void mouseDragged(T aTextShape, MouseEvent anEvent)  { getEditor().getTextEditor(true).mouseDragged(anEvent); }

/**
 * Event handling for shape editing (just forwards to text editor).
 */
public void mouseReleased(T aTextShape, MouseEvent anEvent)
{
    getEditor().getTextEditor(true).mouseReleased(anEvent);
}

/**
 * Key event handling for super selected text.
 */
public void processKeyEvent(T aTextShape, KeyEvent anEvent)
{
    // If tab was pressed and text is structured table row column, forward selection onto next column
    if(aTextShape.isStructured() &&
        anEvent.getID()!=KeyEvent.KEY_PRESSED &&
        anEvent.getKeyCode()==KeyEvent.VK_TAB &&
        !anEvent.isAltDown()) {
        
        // Get structured text table row
        RMParentShape tableRow = aTextShape.getParent();
        
        // Get child table rows
        List children = RMSort.sortedList(tableRow.getChildren(), "getX");
        
        // Get index of child
        int index = children.indexOf(aTextShape);
        
        // If shift is down, get index to the left, wrapped, otherwise get index to the right, wrapped
        if(anEvent.isShiftDown()) index = (index - 1 + children.size())%children.size();
        else index = (index + 1)%children.size();
        
        // Get next text and super-select
        RMShape nextText = (RMShape)children.get(index);
        getEditor().setSuperSelectedShape(nextText);
        
        // Consume event and return
        anEvent.consume(); return;
    }

    // Have text editor process key event
    getEditor().getTextEditor().processKeyEvent(anEvent);
}

/**
 * Editor method - installs this text in RMEditor's text editor.
 */
public void didBecomeSuperSelectedShapeInEditor(RMShape aShape, RMEditor anEditor)
{
    // Get the text
    RMTextShape text = (RMTextShape)aShape;
    
    // If not superselected by TextInspector pane, have editor request focus
    if(!isUISet() || !_textArea.hasFocus())
        anEditor.requestFocus();
    
    // Make sure editor text editor has this text
    anEditor.getTextEditor(text);
    
    // If UI is loaded, install string in text area
    if(isUISet())
        _textArea.getTextEditor().setXString(text.getXString());
}

/**
 * Event hook during selection.
 */
public boolean mousePressedSelection(MouseEvent anEvent)
{
    // Get list of selected shapes
    List shapes = getEditor().getSelectedOrSuperSelectedShapes();
    
    // Iterator over shapes and see if any has an overflow indicator box that was hit
    for(int i=0, iMax=shapes.size(); i<iMax; i++) { RMTextShape text = (RMTextShape)shapes.get(i);
        
        // If no linked text and not painting text indicator, just continue
        if(text.getLinkedText()==null && !isPaintingTextLinkIndicator(text)) continue;

        // Get point in text coords
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), text);
        
        // If pressed was in overflow indicator box, add linked text (or select existing one)
        if(point.x>=text.getWidth()-20 && point.x<=text.getWidth()-10 && point.y>=text.getHeight()-5) {

            // If not linked text, add it, otherwise, select it
            if(text.getLinkedText()==null) sendEvent("LinkedTextMenuItem");
            else getEditor().setSelectedShape(text.getLinkedText());

            // Return true so SelectTool goes to DragModeNone
            return true;
        }
    }
    
    // Return false is mouse point wasn't in overflow indicator box
    return false;
}

/**
 * Moves the handle at the given index to the given point.
 */
public void moveShapeHandle(T aShape, int aHandle, RMPoint toPoint)
{
    // If not structured, do normal version
    if(!aShape.isStructured()) { super.moveShapeHandle(aShape, aHandle, toPoint); return; }
    
    // Get handle point in shape coords and shape parent coords
    RMPoint p1 = getHandlePoint(aShape, aHandle, false);
    RMPoint p2 = aShape.convertPointFromShape(toPoint, aShape.getParent());
    
    // Get whether left handle and width change
    boolean left = aHandle==HandleW || aHandle==HandleNW || aHandle==HandleSW;
    double dw = p2.x - p1.x; if(left) dw = -dw;
    double nw = aShape.getWidth() + dw; if(nw<8) { nw = 8; dw = nw - aShape.getWidth(); }
    
    // Get shape to adjust and new width (make sure it's no less than 8)
    int index = aShape.indexOf(), index2 = left? index-1 : index+1;
    RMShape other = aShape.getParent().getChild(index2);
    double nw2 = other.getWidth() - dw; if(nw2<8) { nw2 = 8; dw = other.getWidth() - nw2; nw = aShape.getWidth() + dw; } 
    
    // Adjust shape and revalidate parent
    aShape.setWidth(nw);
    other.setWidth(nw2);
    aShape.getParent().relayout();
}

/**
 * Overrides tool tooltip method to return text string if some chars aren't visible.
 */
public String getToolTipText(T aTextShape, MouseEvent anEvent)
{
    // If all text is visible and greater than 8 pt, return null
    if(aTextShape.isAllTextVisible() && aTextShape.getFont().getSize()>=8) return null;
    
    // Get text string (just return if empty), trim to 64 chars or less and return
    String string = aTextShape.getText(); if(string==null || string.length()==0) return null;
    if(string.length()>64) string = string.substring(0,64) + "...";
    return string;
}

/**
 * Paints selected shape indicator, like handles (and maybe a text linking indicator).
 */
public void paintShapeHandles(T aTextShape, Graphics2D g, boolean isSuperSelected)
{
    // If text is structured, draw rectangle buttons
    if(aTextShape.isStructured()) {
        
        // Turn off anti-aliasing
        RMAWTUtils.setAntialiasing(g, false);

        // Iterate over shape handles, get rect and draw
        for(int i=0, iMax=getHandleCount(aTextShape); i<iMax; i++) {
            RMRect hr = getHandleRect(aTextShape, i, isSuperSelected);
            RMAWTUtils.drawButton(g, hr, false);
        }
        
        // Turn on antialiasing
        RMAWTUtils.setAntialiasing(g, true);
    }

    // If not structured or text linking, draw normal
    else if(!isSuperSelected)
        super.paintShapeHandles(aTextShape, g, isSuperSelected);
    
    // Call paintTextLinkIndicator
    if(isPaintingTextLinkIndicator(aTextShape))
        paintTextLinkIndicator(aTextShape, g);
}

/**
 * Returns whether to paint text link indicator.
 */
public boolean isPaintingTextLinkIndicator(RMTextShape aText)
{
    // If text is child of table row, return false
    if(aText.getParent() instanceof RMTableRow) return false;
    
    // If there is a linked text, return true
    if(aText.getLinkedText()!=null) return true;
    
    // If height is less than half-inch, return false
    if(aText.getHeight()<36) return false;
    
    // If all text visible, return false
    if(aText.isAllTextVisible()) return false;
    
    // Return true
    return true;
}

/**
 * Paints the text link indicator.
 */
public void paintTextLinkIndicator(RMTextShape aText, Graphics2D g)
{
    // Turn off anti-aliasing
    RMAWTUtils.setAntialiasing(g, false);

    // Get overflow indicator box center point in editor coords
    RMPoint point = new RMPoint(aText.getWidth()-15, aText.getHeight());
    point = getEditor().convertPointFromShape(point, aText);
    
    // Get overflow indicator box rect in editor coords
    RMRect rect = new RMRect(point.x - 5, point.y - 5, 10, 10);
        
    // Draw white background, black frame, and plus sign and turn off aliasing
    g.setColor(aText.getLinkedText()==null? Color.white : new Color(90, 200, 255)); g.fill(rect);
    g.setColor(aText.getLinkedText()==null? Color.black : Color.gray);
    g.setStroke(RMAWTUtils.Stroke1); g.draw(rect);
    g.setColor(aText.getLinkedText()==null? Color.black : Color.white);
    g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, 0));
    RMAWTUtils.drawLine(g, rect.getMidX(), rect.y + 2, rect.getMidX(), rect.getMaxY() - 2);
    RMAWTUtils.drawLine(g, rect.x + 2, rect.getMidY(), rect.getMaxX() - 2, rect.getMidY());

    // Turn on antialiasing
    RMAWTUtils.setAntialiasing(g, true);
}
    
/**
 * Editor method - returns handle count.
 */
public int getHandleCount(T aText)  { return aText.isStructured()? 2 : super.getHandleCount(aText); }

/**
 * Editor method - returns handle rect in editor coords.
 */
public RMRect getHandleRect(T aTextShape, int handle, boolean isSuperSelected)
{
    // If structured, return special handles (tall & thin)
    if(aTextShape.isStructured()) {
        
        // Get handle point in text bounds
        RMPoint cp = getHandlePoint(aTextShape, handle, true);
    
        // Get handle point in table row bounds
        cp = aTextShape.convertPointToShape(cp, aTextShape.getParent());
        
        // If point outside of parent, return bogus rect
        if(cp.x<0 || cp.x>aTextShape.getParent().getWidth())
           return new RMRect(-9999,-9999,0,0);

        // Get handle point in text coords
        cp = getHandlePoint(aTextShape, handle, false);
        
        // Get handle point in editor coords
        cp = getEditor().convertPointFromShape(cp, aTextShape);
        
        // Get handle rect
        RMRect hr = new RMRect(cp.x-3, cp.y, 6, aTextShape.height() * getEditor().getZoomFactor());
        
        // If super selected, offset
        if(isSuperSelected)
            hr.offset(handle==0? -2 : 2, 0);
        
        // Return handle rect
        return hr;
    }
    
    // Return normal shape handle rect
    return super.getHandleRect(aTextShape, handle, isSuperSelected);
}

/**
 * Editor method - uninstalls this text from RMEditor's text editor and removes new text if empty.
 */
public void willLoseSuperSelectionInEditor(RMShape aShape, RMEditor anEditor)
{
    // Get current editor text editor
    RMEditorTextEditor ted = anEditor.getTextEditor();
    
    // If text editor was really just an insertion point and ending text length is zero, remove text
    if(ted.isUpdatingSize() && ted.getTextShape().length()==0 &&
        RMTool.getSelectTool().getDragMode()==RMSelectTool.DragMode.None)
        ted.getTextShape().removeFromParent();
    
    // Set text editor's text shape to null
    ted.setTextShape(null);
}

/**
 * Overrides Tool implementation to accept KeysPanel drags.
 */
public boolean acceptsDrag(T aShape, DropTargetDragEvent anEvent)
{
    // If KeysPanel is dragging, return true
    if(KeysPanel.getDragKey()!=null)
        return true;
    
    // Otherwise, return normal
    return super.acceptsDrag(aShape, anEvent);
}

/**
 * Override normal implementation to handle KeysPanel drop.
 */
public void drop(T aShape, DropTargetDropEvent anEvent)
{
    // If a keys panel drop, add key to text
    if(KeysPanel.getDragKey()!=null) {
        String string = ClipboardUtils.getString(anEvent.getTransferable());
        RMTextShape text = aShape;
        if(text.length()==0)
            text.setText(string);
        else text.getXString().addChars(" " + string);
    }
    
    // Otherwise, do normal drop
    else super.drop(aShape, anEvent);
}

/**
 * Returns the shape class that this tool edits.
 */
public Class getShapeClass()  { return RMTextShape.class; }

/**
 * Returns the name of this tool to be displayed by inspector.
 */
public String getWindowTitle()  { return "Text Inspector"; }

/**
 * Converts a shape to a text shape.
 */
public static void convertToText(RMShape aShape, String aString)
{
    // If shape is null, just return
    if(aShape==null) return;
    
    // Get text shape for given shape (if given shape is text, just use it)
    RMTextShape text = aShape instanceof RMTextShape? (RMTextShape)aShape : new RMTextShape();
    
    // Copy attributes of given shape
    if(text!=aShape)
        text.copyShape(aShape);
    
    // Copy path of given shape
    if(text!=aShape)
        text.setPathShape(aShape);
    
    // Swap this shape in for original
    if(text!=aShape) {
        aShape.getParent().addChild(text, aShape.indexOf());
        aShape.getParent().removeChild(aShape);
    }
    
    // Install a bogus string for testing
    if(aString!=null && aString.equals("test"))
        aString = getTestString();
    
    // If aString is non-null, install in text
    if(aString!=null)
        text.setText(aString);
    
    // Select new shape
    RMEditor.getMainEditor().setSuperSelectedShape(text);
}

/**
 * Returns a rect suitable for the default bounds of a given text at a given point. This takes into account the font
 * and margins of the given text.
 */
private static Rectangle2D getDefaultBounds(RMTextShape aText, RMPoint aPoint)
{
    // Get text font (or default font, if not available)
    RMFont font = aText.getFont(); if(font==null) font = RMFont.getDefaultFont();
    
    // Get bounds
    double x = aPoint.x - aText.getMarginLeft();
    double y = aPoint.y - font.getMaxAscent() - aText.getMarginTop();
    double w = aPoint.x + 4 + aText.getMarginRight() - x;
    double h = aPoint.y + font.getMaxDescent() + aText.getMarginBottom() - y;
    
    // Return integral bounds
    return new RMRect(x, y, w, h).getBounds();
}

/**
 * Returns a test string.
 */
private static String getTestString()
{
    return "Leo vitae diam est luctus, ornare massa mauris urna, vitae sodales et ut facilisis dignissim, " +
    "imperdiet in diam, quis que ad ipiscing nec posuere feugiat ante velit. Viva mus leo quisque. Neque mi vitae, " +
    "nulla cras diam fusce lacus, nibh pellentesque libero. " +
    "Dolor at venenatis in, ac in quam purus diam mauris massa, dolor leo vehicula at commodo. Turpis condimentum " +
    "varius aliquet accumsan, sit nullam eget in turpis augue, vel tristique, fusce metus id consequat orci " +
    "penatibus. Ipsum vehicula euismod aliquet, pharetra. " +
    "Fusce lectus proin, neque cr as eget, integer quam facilisi a adipiscing posuere. Imper diet sem sapien. " +
    "Pretium natoque nibh, tristique odio eligendi odio molestie mas sa. Volutpat justo fringilla rut rum augue. " +
    "Lao reet ulla mcorper molestie.";
}

}