package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;
import snap.util.*;

/**
 * This class is an RMShape subclass for handling rich text. Text is probably the most common and useful element in a
 * ReportMill template. You might use this class to programmatically build or modify a template, like this:
 * <p><blockquote><pre>
 *   RMXString xstring = new RMXString("Hello world!", RMFont.getFont("Arial", 12), RMColor.red);
 *   RMText text = new RMText(xstring);
 *   template.getPage(0).addChild(text);
 *   text.setXY(36, 36);
 *   text.setSizeToFit();
 * </pre></blockquote>
 */
public class RMTextShape extends RMRectShape {
    
    // The real backing store for text is an xstring
    RMXString              _xString;
    
    // The text margin (if different than default)
    Insets                 _margin = getMarginDefault();
    
    // Vertical alignment of text
    AlignY                 _alignmentY = AlignY.Top;
    
    // Specifies how text should handle overflow during RPG (ignore it, shrink it or paginate it)
    byte                   _wraps;
    
    // Whether to fit text on layout
    boolean                _fitText;
    
    // Whether text should wrap around other shapes that cause wrap
    boolean                _performsWrap = false;

    // Whether text should eliminate empty lines during RPG
    boolean                _coalesceNewlines;
    
    // Whether text should draw box around itself even if there's no stroke
    boolean                _drawsSelectionRect;

    // The linked text shape for rendering overflow, if there is one
    RMLinkedText           _linkedText;

    // The text layout, if there is a dedicated one
    RMTextLayout           _textLayout;
    
    // Cached Preferred size
    RMSize                 _prefSize;
    
    // The default text margin (top=1, left=2, bottom=0, right=2)
    static Insets          _marginDefault = new Insets(1, 2, 0, 2);
    
    // Constants for overflow behavior during RPG
    public static final byte WRAP_NONE = 0;
    public static final byte WRAP_BASIC = 1;
    public static final byte WRAP_SCALE = 2;
    
/**
 * Creates an empty text instance.
 */
public RMTextShape() { }

/**
 * Creates a text instance initialized with the given RMXString.
 */
public RMTextShape(RMXString string)  { _xString = string; }

/**
 * Creates a text instance initialized with the given plain text String.
 */
public RMTextShape(String plainText)  { getXString().addChars(plainText); }

/**
 * Returns the XString associated with this RMText.
 */
public RMXString getXString()
{
    if(_xString==null) {
        _xString = createXString();
        _xString.addPropertyChangeListener(this);
    }
    return _xString;
}

/**
 * Sets the XString associated with this RMText.
 */
public void setXString(RMXString xString)
{
    // If value already set, just return
    if(xString==_xString) return;
    
    // Stop listening to last XString and start listening to new XString
    if(_xString!=null) _xString.removePropertyChangeListener(this);
    if(xString!=null) xString.addPropertyChangeListener(this);
    
    // Set value and fire property change, and reset cached HeightToFit
    firePropertyChange("XString", _xString, _xString = xString, -1);
    revalidate(); repaint();
}

/**
 * Creates the xstring for this text.
 */
protected RMXString createXString()  { return new RMXString(); }

/**
 * Returns the length, in characters, of the XString associated with this RMText.
 */
public int length()  { return getXString().length(); }

/**
 * Returns the text associated with this RMText as a plain String.
 */
public String getText()  { return getXString().getText(); }

/**
 * Replaces the current text associated with this RMText with the given String.
 */
public void setText(String aString)  { getXString().replaceChars(aString, 0, length()); }

/**
 * Returns the first character index visible in this text.
 */
public int getVisibleStart()  { return 0; }

/**
 * Returns the last character index visible in this text.
 */
public int getVisibleEnd()  { RMTextLayout layout = getTextLayout(); return layout.getEnd(); }

/**
 * Returns whether all characters can be visibly rendered in text bounds.
 */
public boolean isAllTextVisible()
{
    double maxy = getMarginTop() + getTextLayout().getHeightToFit() + getMarginBottom();
    return maxy<=getHeight();
}

/**
 * Returns the font for char 0.
 */
public RMFont getFont()  { return getXString().getFontAt(0); }

/**
 * Sets the font for all characters.
 */
public void setFont(RMFont aFont)  { getXString().setAttribute(aFont); }

/**
 * Returns the format for char 0.
 */
public RMFormat getFormat()  { return getXString().getRunAt(0).getFormat(); }

/**
 * Sets the format for all characters.
 */
public void setFormat(RMFormat aFormat)  { getXString().setAttribute(aFormat, 0, length()); }

/**
 * Returns the color of the first character of the xstring associated with this RMText.
 */
public RMColor getTextColor()  { return getXString().getRunAt(0).getColor(); }

/**
 * Sets the color of the characters in the XString associated with this RMText.
 */
public void setTextColor(RMColor aColor)  { getXString().setAttribute(aColor); }

/**
 * Returns if char 0 is underlined.
 */
public boolean isUnderlined()  { return getXString().getRunAt(0).isUnderlined(); }

/**
 * Sets all chars to be underlined.
 */
public void setUnderlined(boolean aFlag)  { getXString().setUnderlined(aFlag); }

/**
 * Returns the outline for char 0.
 */
public RMXString.Outline getOutline()  { return getXString().getRunAt(0).getOutline(); }

/**
 * Sets the Outline for all characters.
 */
public void setOutline(RMXString.Outline anOutline)  { getXString().setAttribute(RMTextTypes.TEXT_OUTLINE, anOutline); }

/**
 * Returns the alignment for char 0.
 */
public AlignX getAlignmentX()  { return getXString().getAlignX(); }

/**
 * Sets the align for all chars.
 */
public void setAlignmentX(AlignX anAlignmentX)  { getXString().setAlignX(anAlignmentX); }

/**
 * Returns the alignment as a string, one of: "left", "center" or "right".
 */
public String getAlignString()  { return getAlignmentX().toString().toLowerCase(); }

/**
 * Returns the vertical alignment.
 */
public AlignY getAlignmentY()  { return _alignmentY; }

/**
 * Sets the vertical alignment.
 */
public void setAlignmentY(AlignY anAlignment)
{
    // Set alignment, fire property change, revalidate and repaint
    firePropertyChange("AlignmentY", _alignmentY, _alignmentY = anAlignment, -1);
    revalidate(); repaint();
}

/**
 * Returns the wrapping behavior for over-filled rpgCloned text (NONE, WRAP, SHRINK).
 */
public byte getWraps()  { return _wraps; }

/**
 * Sets the wrapping behavior for over-filled rpgCloned text (NONE, WRAP, SHRINK).
 */
public void setWraps(byte aValue)  { _wraps = aValue; }

/**
 * Returns whether text should wrap around other shapes that cause wrap.
 */
public boolean getPerformsWrap()  { return _performsWrap; }

/**
 * Sets whether text should wrap around other shapes that cause wrap.
 */
public void setPerformsWrap(boolean aFlag)  { _performsWrap = aFlag; }

/**
 * Returns whether text should coalesce consecutive newlines in rpgClone.
 */
public boolean getCoalesceNewlines()  { return _coalesceNewlines; }

/**
 * Sets whether text should coalesce consecutive newlines in rpgClone.
 */
public void setCoalesceNewlines(boolean aFlag)  { _coalesceNewlines = aFlag; }

/**
 * Returns whether text should always draw at least a light gray border (useful when editing).
 */
public boolean getDrawsSelectionRect()  { return _drawsSelectionRect; }

/**
 * Sets whether text should always draw at least a light-gray border (useful when editing).
 */
public void setDrawsSelectionRect(boolean aValue)  { _drawsSelectionRect = aValue; }

/**
 * Returns the char spacing at char 0.
 */
public float getCharSpacing()  { return getXString().getRun(0).getCharSpacing(); }

/**
 * Sets the char spacing for the text string.
 */
public void setCharSpacing(float aValue)
{
    getXString().setAttribute(RMTextTypes.TEXT_CHAR_SPACING, aValue==0? null : aValue);
}

/**
 * Returns the line spacing at char 0.
 */
public float getLineSpacing()  { return getXString().getParagraphAt(0).getLineSpacing(); }

/**
 * Sets the line spacing for all chars.
 */
public void setLineSpacing(float aHeight)
{
    RMParagraph ps = getXString().getParagraphAt(0).deriveLineSpacing(aHeight);
    getXString().setParagraph(ps, 0, length());
}

/**
 * Returns the line gap at char 0.
 */
public float getLineGap()  { return getXString().getParagraphAt(0).getLineGap(); }

/**
 * Sets the line gap for all chars.
 */
public void setLineGap(float aHeight)
{
    RMParagraph ps = getXString().getParagraphAt(0).deriveLineGap(aHeight);
    getXString().setParagraph(ps, 0, length());
}

/**
 * Returns the minimum line height at char 0.
 */
public float getLineHeightMin()  { return getXString().getParagraphAt(0).getLineHeightMin(); }

/**
 * Sets the minimum line height for all chars.
 */
public void setLineHeightMin(float aHeight)
{
    RMParagraph ps = getXString().getParagraphAt(0).deriveLineHeightMin(aHeight);
    getXString().setParagraph(ps, 0, length());
}

/**
 * Returns the maximum line height at char 0.
 */
public float getLineHeightMax()  { return getXString().getParagraphAt(0).getLineHeightMax(); }

/**
 * Sets the maximum line height for all chars.
 */
public void setLineHeightMax(float aHeight)
{
    RMParagraph ps = getXString().getParagraphAt(0).deriveLineHeightMax(aHeight);
    getXString().setParagraph(ps, 0, length());
}

/**
 * Returns margin.
 */
public Insets getMargin()  { return _margin; }

/**
 * Sets margin.
 */
public void setMargin(Insets aMargin)
{
    // If value already set, just return
    if(_margin.equals(aMargin)) return;
    
    // Set value, fire property change, revalidate and repaint
    firePropertyChange("Margin", _margin, _margin = aMargin, -1);
    revalidate(); repaint();    
}

/**
 * Returns the default margin of the text (top=1, left=2, right=2, bottom=0).
 */
public Insets getMarginDefault()  { return _marginDefault; }

/**
 * Returns the margin as a string.
 */
public String getMarginString()
{
    return getMarginTop() + ", " + getMarginLeft() + ", " + getMarginBottom() + ", " + getMarginRight();
}

/**
 * Sets the margin as a string.
 */
public void setMarginString(String aString)
{
    // If given string is empty, set default margins
    if(aString==null || aString.trim().length()==0) { setMargin(getMarginDefault()); return; }
    
    // Split the string by commas or spaces and get the parts
    String parts[] = aString.indexOf(",")>0? aString.split(",") : aString.split(" ");
    String p1 = parts[0];
    String p2 = parts[Math.min(1, parts.length-1)];
    String p3 = parts[Math.min(2, parts.length-1)];
    String p4 = parts[Math.min(3, parts.length-1)];
    
    // Set margin from parts
    setMargin(new Insets(RMUtils.intValue(p1), RMUtils.intValue(p2), RMUtils.intValue(p3), RMUtils.intValue(p4)));
}

/**
 * Returns the left margin of the text (default to 2).
 */
public int getMarginLeft()  { return getMargin().left; }

/**
 * Returns the right margin of the text (defaults to 2).
 */
public int getMarginRight()  { return getMargin().right; }

/**
 * Returns the top margin of the text (defaults to 1).
 */
public int getMarginTop()  { return getMargin().top; }

/**
 * Returns the bottom margin of the text (defaults to 0).
 */
public int getMarginBottom()  { return getMargin().bottom; }

/**
 * Override to revalidate.
 */
public void setWidth(double aValue)  { super.setWidth(aValue); revalidate(); }

/**
 * Override to revalidate.
 */
public void setHeight(double aValue)  { super.setHeight(aValue); revalidate(); }

/**
 * Returns the path for this shape (might be PathShape).
 */
public RMPath getPath()
{
    return getPathShape()!=null? getPathShape().getPath().getPathInRect(getBoundsInside()) : super.getPath();
}

/**
 * Overrides shape implementation to pass through getPathInBounds(inset).
 */
public RMPath getPathInBounds()  { return getPathInBounds(-3); }

/**
 * Returns the shape's path scaled to the shape's current bounds.
 */
public RMPath getPathInBounds(int anInset)
{
    // If text doesn't perform wrap or parent is null, return normal path in bounds
    if(!getPerformsWrap() || getParent()==null)
        return super.getPathInBounds();
    
    // Get peers who cause wrap (if none, just return super path in bounds)
    List peersWhoCauseWrap = getPeersWhoCauseWrap();
    if(peersWhoCauseWrap==null)
        return super.getPathInBounds();
    
    // Add this text to list
    peersWhoCauseWrap.add(0, this);
    
    // Get the path minus the neighbors, convert back to this shape, reset bounds to this shape
    RMPath path = RMShapeUtils.getSubtractedPath(peersWhoCauseWrap, anInset);
    path = convertPathFromShape(path, getParent());
    path.setBounds(getBounds());
    return path;
}

/**
 * Returns the subset of children that cause wrap.
 */
private List <RMShape> getPeersWhoCauseWrap()
{
    // Iterate over children and add any that intersect frame
    List list = null;
    for(int i=0, iMax=getParent().getChildCount(); i<iMax; i++) { RMShape child = getParent().getChild(i);
        if(child!=this && child.getFrame().intersects(getFrame())) {
            if(list==null) list = new ArrayList(); list.add(child); } }
    return list;
}

/**
 * This notification method is called when any peer is changed.
 */
public void peerDidChange(RMShape aShape)
{
    // If this text respects neighbors and shape intersects it, register for redraw
    if(getPerformsWrap() && aShape.getFrame().intersectsRect(getFrame())) {
        revalidate(); repaint(); }
}

/**
 * Returns the shape that provides the path for this text to wrap text to.
 */
public RMShape getPathShape()  { return _pathShape; } RMShape _pathShape;

/**
 * Sets the shape that provides the path for this text to wrap text to.
 */
public void setPathShape(RMShape aShape)
{
    // If changed, set and fire property change
    if(!RMUtils.equals(aShape, _pathShape)) {
        firePropertyChange("PathShape", _pathShape, _pathShape = aShape, -1);
        revalidate(); repaint();
    }
}

/**
 * Overrides rectangle implementation to potentially clear path shape.
 */
public void setRadius(float aValue)  { super.setRadius(aValue); setPathShape(null); }

/**
 * Returns the linked text for this text (if any).
 */
public RMLinkedText getLinkedText()  { return _linkedText; }

/**
 * Sets the linked text for this text (if any).
 */
public void setLinkedText(RMLinkedText aLinkedText)
{
    // Set linked text, and if non-null, set its previous text to this text
    _linkedText = aLinkedText;
    if(_linkedText!=null)
        _linkedText.setPreviousText(this);
    revalidate(); repaint();
}

/**
 * Returns a text layout.
 */
public RMTextLayout getTextLayout()
{
    if(_textLayout==null) _textLayout = createTextLayout();
    if(_fitText) _textLayout.layoutToFit();
    else _textLayout.layout();
    return _textLayout;
}

/**
 * Creates a new layout.
 */
public RMTextLayout createTextLayout()
{
    // Get text layout and configure (if path is not rect, replace with RMTextLayoutInPath)
    RMTextLayout layout = new RMTextLayout();
    if(getPath()!=RMPath.unitRectPath || getPerformsWrap())
        layout = new RMTextLayoutInPath(getPathInBounds(-3));

    // Set XString, Start index, AlignmentY
    layout.setXString(getXString());
    layout.setStart(getVisibleStart());
    layout.setAlignmentY(getAlignmentY());
    
    // Set layout bounds
    Insets pad = getMargin(); double pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
    double w = getWidth() - pl - pr;
    double h = getHeight() - pt - pb, lh = getLinkedText()!=null? h : Float.MAX_VALUE;
    layout.setRect(pl, pt, w, lh);
    layout.setAlignHeight(h);

    // Return layout
    return layout;
}

/**
 * Override to compute from RMTextLayout.
 */
protected double computePrefWidth(double aHeight)
{
    // If font scaling, return current size
    if(_wraps==WRAP_SCALE) return getWidth();
    if(length()==0) return getMarginLeft() + getMarginRight();
    
    // Get text layout and update layout bounds for given size
    RMTextLayout layout = new RMTextLayout();
    layout.setXString(getXString()); layout.setStart(getVisibleStart());
    layout.setRect(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
    layout.layout();
  
    // Get width from text layout and return
    return Math.ceil(getMarginLeft() + layout.getWidthToFit() + getMarginRight());
}

/**
 * Override to compute from RMTextLayout.
 */
protected double computePrefHeight(double aWidth)
{
    if(_wraps==WRAP_SCALE) return getHeight();
    if(length()==0) return getMarginTop() + getMarginBottom();
    RMTextLayout layout = getTextLayout();
    return Math.ceil(getMarginTop() + layout.getHeightToFit() + getMarginBottom());
}

/**
 * Resizes all the font's in text string by given factor.
 */
public void scaleFonts(double aFactor)  { RMXStringUtils.scaleFonts(getXString(), (float)aFactor); }

/**
 * Generate report.
 */
protected RMShape rpgShape(ReportOwner anRptOwner, RMShape aParent)
{
    RMTextShape clone = clone();
    RMXString string = clone.getXString();

    // Do xstring RPG (if no change due to RPG, just use normal) with FirePropertyChangeEnabled turned off
    string.setFirePropertyChangeEnabled(false);
    string.rpgClone(anRptOwner, null, clone, false);
        
    // If coalesce newlines is set, coalesce newlines
    if(getCoalesceNewlines())
        RMXStringUtils.coalesceNewlines(string);

    // Trim whitespace from end of string
    int len = string.length(), end = len; while(end>0 && Character.isWhitespace(string.charAt(end-1))) end--;
    if(end!=len)
        string.removeChars(end, len);

    // If WRAP_SCALE, set FitText ivar
    if(getWraps()==WRAP_SCALE) clone._fitText = true;
    
    // Enable string FirePropertyChangeEnabled and revalidate
    string.setFirePropertyChangeEnabled(true);
    clone.revalidate();
    
    // If paginating, swap in paginated parts (disable in table row)
    if(getWraps()==WRAP_BASIC && !(getParent() instanceof RMTableRow)) {
        ReportOwner.ShapeList shapes = new ReportOwner.ShapeList();
        for(RMTextShape text : clone.paginate())
            shapes.addChild(text);
        return shapes;
    }
    
    // Return clone
    return clone;
}

/**
 * Paginates this text by creating linked texts to show all text and returns a list of this text and the linked texts.
 */
protected List <RMTextShape> paginate()
{
    // Create pages list with this text in it
    List <RMTextShape> pages = new ArrayList(); pages.add(this);
    
    // Cache vertical alignment and set to Top
    AlignY verticalAlignment = getAlignmentY();
    setAlignmentY(AlignY.Top);
    
    // Get linked texts until all text visible
    RMTextShape text = this;
    while(!text.isAllTextVisible()) {
        text = new RMLinkedText(text);
        pages.add(text);
    }
    
    // Restore alignment on last text and return list
    text.setAlignmentY(verticalAlignment);
    return pages;
}

/**
 * Re-does the RPG clone to resolve any @Page@ keys (assumed to be present in userInfo).
 */
protected void resolvePageReferences(ReportOwner aRptOwner, Object userInfo)
{
    // Do normal shape resolve page references
    super.resolvePageReferences(aRptOwner, userInfo);
    
    // RPG clone xstring again and set
    RMXString xstringCloneRPG = _xString.rpgClone(aRptOwner, userInfo, null, true);
    setXString(xstringCloneRPG);
}

/**
 * Creates a shape suitable for the "remainder" portion of a divideShape call (just a clone by default).
 */
protected RMShape createDivideShapeRemainder(byte anEdge)
{
    return anEdge==RMRect.MinYEdge? new RMLinkedText(this) : clone();
}

/**
 * Overridden from RMShape to provide the outlines of all the glyphs
 */
public RMPath getMaskPath()  { return RMTextShapeUtils.getTextPath(this); }

/** Editor method - indicates that this shape can be super selected. */
public boolean superSelectable()  { return true; }

/** Editor method. */
public boolean isStructured()  { return _parent instanceof RMTableRow && ((RMTableRow)_parent).isStructured(); }

/**
 * Paints a text shape.
 */
public void paintShape(RMShapePainter aPntr)  { paintShapeBack(aPntr); paintShapeText(aPntr); }

/**
 * Paints the text shape background.
 */
public void paintShapeBack(RMShapePainter aPntr)  { super.paintShape(aPntr); }

/**
 * Paints the text shape text.
 */
public void paintShapeText(RMShapePainter aPntr)
{
    // Get clip bounds
    Shape clip = aPntr.getClip(); aPntr.clip(getBoundsInside());
    double maxy = getHeight();
    
    // Iterate through runs and paint them
    RMTextLayout layout = getTextLayout();
    for(RMTextRun run=layout.getRun(); run!=null; run=run.getNext()) {
        if(run.getY()>maxy) break;
        paintTextRun(aPntr, run);
    }
    
    // Restore clip
    aPntr.setClip(clip);
}

/**
 * Paints the layout in a given graphics.
 */
protected void paintTextRun(RMShapePainter aPntr, RMTextRun aRun)
{
    // Set run font/color
    aPntr.setFont(aRun.getFont().awt());
    aPntr.setColor(aRun.getColor().awt());

    // Get run x & y
    float x = (float)aRun.getX();
    float y = (float)aRun.getYBaseline();

    // If outlined, draw outlines
    if(aRun.getOutline()!=null) { RMXString.Outline outline = aRun.getOutline();
            
        // Get run glyph vector and its outline
        GlyphVector gv = aRun.glyphVector(aPntr.getGraphics());
        Shape shape = gv.getOutline(x, y);
            
        // If fill color exists, fill glyph vector
        if(outline.getFillColor()!=null) {
            aPntr.setColor(outline.getFillColor().awt()); aPntr.fill(shape);
            aPntr.setColor(aRun.getColor().awt());
        }
            
        // Stroke glyph vector
        aPntr.setStroke(RMAWTUtils.getStroke(outline.getStrokeWidth()));
        aPntr.draw(shape);
    }

    // If printing, just draw string
    else if(aRun.getCharSpacing()==0)
        aPntr.drawString(RMStringUtils.trimEnd(aRun.toString()), x, y);

    // If not printing, get GlyphVector for run and draw - I think we did this because Mac used to do kerning
    else {
        GlyphVector gv = aRun.glyphVector(aPntr.getGraphics());
        aPntr.drawGlyphVector(gv, x, y);
    }
        
    // Draw underline
    if(aRun.isUnderlined()) {
        aPntr.setStroke(new BasicStroke((float)aRun.getUnderlineStroke(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        double uly = y - aRun.getUnderlineY();
        aPntr.drawLine(x, uly, aRun.getMaxX(), uly);
    }
}

/**
 * Copies attributes from given object.
 */
public void copyText(RMTextShape aText)
{
    // Copy basic shape attributes and XString
    copyShape(aText);
    setXString(aText.getXString().clone());
    
    // Copy margin, vertical alignment, wraps, coalesce newlines and draws selection rect
    setMargin(aText.getMargin());
    setAlignmentY(aText.getAlignmentY());
    setWraps(aText.getWraps());
    setPerformsWrap(aText.getPerformsWrap());
    setCoalesceNewlines(aText.getCoalesceNewlines());
    setDrawsSelectionRect(aText.getDrawsSelectionRect());
}

/**
 * Adds the property names for this shape.
 */
protected void addPropNames() { addPropNames("Text", "Object"); super.addPropNames(); }

/**
 * Override to catch XString changes.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Do normal version and revalidate if XString change
    super.propertyChange(anEvent);
    if(anEvent.getSource() instanceof RMXString || anEvent.getSource() instanceof RMXStringRun) {
        revalidate(); repaint(); }
}

/**
 * Override to do home-brew layout.
 */
public void revalidate()
{
    _textLayout = null; _prefSize = null;
    if(getLinkedText()!=null) { getLinkedText().revalidate(); getLinkedText().repaint(); }
}

/**
 * Standard clone implementation.
 */
public RMTextShape clone()
{
    // Get normal shape clone, clone XString, clear layout and return
    RMTextShape clone = (RMTextShape)super.clone();
    clone.setXString(RMUtils.clone(_xString)); // clone._textLayout = null;
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name to text
    XMLElement e = super.toXML(anArchiver); e.setName("text");
    
    // Archive Margin, AlignmentY
    if(getMargin()!=getMarginDefault()) e.add("margin", getMarginString());
    if(_alignmentY!=AlignY.Top) e.add("valign", getAlignmentY().toString().toLowerCase());
    
    // Archive Wraps, PerformsWrap
    if(_wraps!=0) e.add("wrap", _wraps==WRAP_BASIC? "wrap" : "shrink");
    if(_performsWrap) e.add("WrapAround", true);
    
    // Archive CoalesceNewlines, DrawsSelectionRect
    if(_coalesceNewlines) e.add("coalesce-newlines", true);
    if(_drawsSelectionRect) e.add("draw-border", true);
    
    // Archive xstring
    if(!(this instanceof RMLinkedText)) {
        
        // Get the xml element for the xstring
        XMLElement xse = anArchiver.toXML(getXString());
        
        // Add individual child elements to this text's xml element
        for(int i=0, iMax=xse.size(); i<iMax; i++)
            e.add(xse.get(i));
    }
    
    // If linked text present, archive reference to it (it should be archived as normal part of shape hierarchy)
    if(getLinkedText()!=null)
        e.add("linked-text", anArchiver.getReference(getLinkedText()));
    
    // If there is a path shape, archive path shape
    if(getPathShape()!=null) {
        
        // Get path shape and an element (and add element to master element)
        RMShape pathShape = getPathShape();
        XMLElement pathShapeElement = new XMLElement("path-shape");
        e.add(pathShapeElement);
        
        // Archive path shape to path-shape element
        XMLElement pathShapeElementZero = anArchiver.toXML(pathShape);
        pathShapeElement.add(pathShapeElementZero);
    }
    
    // Return element for this shape
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive Margin, AlignmentY
    if(anElement.hasAttribute("margin")) setMarginString(anElement.getAttributeValue("margin"));
    if(anElement.hasAttribute("valign"))
        setAlignmentY(RMEnumUtils.valueOfIC(AlignY.class, anElement.getAttributeValue("valign")));
    
    // Unarchive Wraps, PerformsWrap
    String wrap = anElement.getAttributeValue("wrap", "none");
    if(wrap.equals("wrap")) setWraps(WRAP_BASIC); else if(wrap.equals("shrink")) setWraps(WRAP_SCALE);
    setPerformsWrap(anElement.getAttributeBoolValue("WrapAround"));
    
    // Unarchive CoalesceNewlines, DrawsSelectionRect
    setCoalesceNewlines(anElement.getAttributeBoolValue("coalesce-newlines"));
    if(anElement.getAttributeBoolValue("draw-border")) setDrawsSelectionRect(true);
    
    // Unarchive xString
    if(!(this instanceof RMLinkedText))
        _xString = getXString().fromXML(anArchiver, anElement);
    
    // Register for finish call
    anArchiver.getReference(anElement);
    
    // Unarchive path-shape if present
    if(anElement.get("path-shape")!=null) {
        
        // Get the dedicated path-shape element and its first child (the actual path-shape element)
        XMLElement pathShapeElement = anElement.get("path-shape");
        XMLElement pathShapeElementZero = pathShapeElement.get(0);
        
        // Unarchive the path shape and set
        RMShape pathShape = (RMShape)anArchiver.fromXML(pathShapeElementZero, null);
        setPathShape(pathShape);
    }
    
    // Return this shape
    return this;
}

/**
 * XML reference unarchival - to unarchive linked text.
 */
public void fromXMLFinish(XMLArchiver anArchiver, XMLElement anElement)
{
    // If linked-text, get referenced linked text and set
    if(!anElement.hasAttribute("linked-text")) return;
    RMLinkedText linkedText = (RMLinkedText)anArchiver.getReference("linked-text", anElement);
    setLinkedText(linkedText);
}

/**
 * Standard toSring implementation.
 */
public String toString()
{
    String string = super.toString();
    string = string.substring(0, string.length() - 1);
    return string + ", \"" + getXString() + "\"]";
}

}