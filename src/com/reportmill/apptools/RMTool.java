package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.graphics.*;
import com.reportmill.text.RMFont;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;
import snap.web.*;

/**
 * This is the base class for tools in RM - the objects that provide GUI editing for RM shapes.
 */
public class RMTool <T extends RMShape> extends SwingOwner {

    // The newly created shape instance
    RMShape                     _shape;
    
    // The mouse down point that initiated last tool mouse loop
    RMPoint                     _downPoint;
    
    // A shared instance of the select tool
    static RMSelectTool         _selectTool = new RMSelectTool();
    
    // Map of tool instances by shape class
    static Map <Class, RMTool>  _tools = new HashMap();
    
    // The image for a shape handle
    static Image                _handle = Swing.getImage("Handle8x8.png", RMEditorShapePainter.class);
    
    // The inspector for paint/fill shape attributes
    static ShapeFills           _shapeFills = new ShapeFills();
    
    // Handle constants
    public static final byte HandleWidth = 8;
    public static final byte HandleNW = 0;
    public static final byte HandleNE = 1;
    public static final byte HandleSW = 2;
    public static final byte HandleSE = 3;
    public static final byte HandleW = 4;
    public static final byte HandleE = 5;
    public static final byte HandleN = 6;
    public static final byte HandleS = 7;

/**
 * Returns the shape class that this tool handles.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMShape.class; }

/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { return RMClassUtils.newInstance(getShapeClass()); }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Shape Inspector"; }

/**
 * Create UI.
 */
protected JComponent createUI()  { return getClass()==RMTool.class? new JLabel() : super.createUI(); }

/**
 * Returns the given shape's dataset entity.
 * Provides a level of indirection show shapes can show a different entity in the designer.
 */
public Entity getDatasetEntity(RMShape aShape)  { return aShape.getDatasetEntity(); }

/**
 * Returns the currently active editor.
 */
public RMEditor getEditor()  { return RMEditor.getMainEditor(); }

/**
 * Returns the currently active editor pane.
 */
public RMEditorPane getEditorPane()  { return RMEditorPane.getMainEditorPane(); }

/**
 * Returns the current selected shape for the current editor.
 */
public T getSelectedShape()
{
    RMEditor e = getEditor(); if(e==null) return null;
    RMShape s = e.getSelectedOrSuperSelectedShape();
    return RMClassUtils.getInstance(s, getShapeClass());
}

/**
 * Returns the current selected shapes for the current editor.
 */
public List <? extends RMShape> getSelectedShapes()  { return getEditor().getSelectedOrSuperSelectedShapes(); }

/**
 * Called when a tool is selected.
 */
public void activateTool()  { }

/**
 * Called when a tool is deselected (when another tool is selected).
 */
public void deactivateTool()  { }

/**
 * Called when a tool is selected even when it's already the current tool.
 */
public void reactivateTool()  { }

/**
 * Called when a tool is deselected to give an opportunity to finalize changes in progress.
 */
public void flushChanges(RMEditor anEditor, RMShape aShape)  { }

/**
 * Returns whether a given shape is selected in the editor.
 */
public boolean isSelected(RMShape aShape)  { return getEditor().isSelected(aShape); }

/**
 * Returns whether a given shape is superselected in the editor.
 */
public boolean isSuperSelected(RMShape aShape)  { return getEditor().isSuperSelected(aShape); }

/**
 * Returns whether a given shape is super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return aShape.superSelectable(); }

/**
 * Returns whether a given shape accepts children.
 */
public boolean getAcceptsChildren(RMShape aShape)  { return aShape.acceptsChildren(); }

/**
 * Returns whether a given shape can be ungrouped.
 */
public boolean isUngroupable(RMShape aShape)  { return aShape.getChildCount()>0; }

/**
 * Editor method - called when an instance of this tool's shape is super selected.
 */
public void didBecomeSuperSelectedShapeInEditor(RMShape aShape, RMEditor anEditor)  { }

/**
 * Editor method - called when an instance of this tool's shape in de-super-selected.
 */
public void willLoseSuperSelectionInEditor(RMShape aShape, RMEditor anEditor)  { }

/**
 * Returns the bounds of the shape in parent coords when super selected (same as getBoundsMarkedDeep by default).
 */
public RMRect getBoundsSuperSelected(RMShape aShape)  { return aShape.getBoundsMarkedDeep(); }

/**
 * Converts from shape units to tool units.
 */
public double getUnitsFromPoints(double aValue)
{
    RMEditor editor = getEditor(); RMDocument doc = editor.getDocument();
    return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
}

/**
 * Converts from tool units to shape units.
 */
public double getPointsFromUnits(double aValue)
{
    RMEditor editor = getEditor(); RMDocument doc = editor.getDocument();
    return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
}

/**
 * Returns the font for the given shape.
 */
public RMFont getFont(RMEditor anEditor, RMShape aShape)
{
    RMEditorTextEditor ted = anEditor.getTextEditor();
    if(ted!=null && ted.getTextShape()==aShape)
        return ted.getFont();
    return aShape.getFont();
}

/**
 * Sets the font for the given shape.
 */
public void setFont(RMEditor anEditor, RMShape aShape, RMFont aFont)
{
    RMEditorTextEditor ted = anEditor.getTextEditor();
    if(ted!=null && ted.getTextShape()==aShape)
        ted.setFont(aFont);
    else aShape.setFont(aFont);
}

/**
 * Returns the font for the given shape.
 */
public RMFont getFontDeep(RMEditor anEditor, RMShape aShape)
{
    RMFont font = getFont(anEditor, aShape);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax && font==null; i++) font = aShape.getChild(i).getFont();
    for(int i=0, iMax=aShape.getChildCount(); i<iMax && font==null; i++) {
        RMShape child = aShape.getChild(i); RMTool tool = getTool(child);
        font = tool.getFontDeep(anEditor, child);
    }
    return font;
}

/**
 * Sets the font family for given shape.
 */
public void setFontFamily(RMEditor anEditor, RMShape aShape, RMFont aFont)
{
    // Get new font for given font family font and current shape font size/style and set
    RMFont font = getFont(anEditor, aShape), font2 = aFont;
    if(font!=null) {
        if(font.isBold()!=font2.isBold() && font2.getBold()!=null) font2 = font2.getBold();
        if(font.isItalic()!=font2.isItalic() && font2.getItalic()!=null) font2 = font2.getItalic();
        font2 = font2.deriveFont(font.getSize());
    }
    setFont(anEditor, aShape, font2);
}

/**
 * Sets the font family for given shape.
 */
public void setFontFamilyDeep(RMEditor anEditor, RMShape aShape, RMFont aFont)
{
    // Set FontFamily for shape and recurse for children
    setFontFamily(anEditor, aShape, aFont);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        RMTool tool = getTool(child); tool.setFontFamilyDeep(anEditor, child, aFont); }
}

/**
 * Sets the font name for given shape.
 */
public void setFontName(RMEditor anEditor, RMShape aShape, RMFont aFont)
{
    // Get new font for name and current shape size and set
    RMFont font = getFont(anEditor, aShape);
    RMFont font2 = font!=null? aFont.deriveFont(font.getSize()) : aFont;
    setFont(anEditor, aShape, font2);
}

/**
 * Sets the font name for given shape.
 */
public void setFontNameDeep(RMEditor anEditor, RMShape aShape, RMFont aFont)
{
    // Set Font name for shape and recurse for children
    setFontName(anEditor, aShape, aFont);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        RMTool tool = getTool(child); tool.setFontNameDeep(anEditor, child, aFont); }
}

/**
 * Sets the font size for given shape.
 */
public void setFontSize(RMEditor anEditor, RMShape aShape, double aSize, boolean isRelative)
{
    // Get new font for current shape font at new size and set
    RMFont font = getFont(anEditor, aShape); if(font==null) return;
    RMFont font2 = isRelative? font.deriveFont(font.getSize() + aSize) : font.deriveFont(aSize);
    setFont(anEditor, aShape, font2);
}

/**
 * Sets the font size for given shape.
 */
public void setFontSizeDeep(RMEditor anEditor, RMShape aShape, double aSize, boolean isRelative)
{
    setFontSize(anEditor, aShape, aSize, isRelative);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        RMTool tool = getTool(child); tool.setFontSizeDeep(anEditor, child, aSize, isRelative); }    
}

/**
 * Sets the font to bold or not bold for given shape.
 */
public void setFontBold(RMEditor anEditor, RMShape aShape, boolean aFlag)
{
    RMFont font = getFont(anEditor, aShape); if(font==null || font.isBold()==aFlag) return;
    RMFont font2 = font.getBold(); if(font2==null) return;
    setFont(anEditor, aShape, font2);
}

/**
 * Sets the font to bold or not bold for given shape and its children.
 */
public void setFontBoldDeep(RMEditor anEditor, RMShape aShape, boolean aFlag)
{
    setFontBold(anEditor, aShape, aFlag);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        RMTool tool = getTool(child); tool.setFontBoldDeep(anEditor, child, aFlag); }    
}

/**
 * Sets the font to italic or not italic for given shape.
 */
public void setFontItalic(RMEditor anEditor, RMShape aShape, boolean aFlag)
{
    RMFont font = getFont(anEditor, aShape); if(font==null || font.isItalic()==aFlag) return;
    RMFont font2 = font.getItalic(); if(font2==null) return;
    setFont(anEditor, aShape, font2);
}

/**
 * Sets the font to italic or not italic for given shape and its children.
 */
public void setFontItalicDeep(RMEditor anEditor, RMShape aShape, boolean aFlag)
{
    setFontItalic(anEditor, aShape, aFlag);
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        RMTool tool = getTool(child); tool.setFontItalicDeep(anEditor, child, aFlag); }    
}

/**
 * Event handling - called on mouse move when this tool is active.
 */
public void mouseMoved(MouseEvent anEvent)  { }

/**
 * Event handling for shape creation.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Set undo title
    getEditor().undoerSetUndoTitle("Add Shape");

    // Save the mouse down point
    _downPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);

    // Create shape and move to downPoint
    _shape = newInstance();
    _shape.setXY(_downPoint);
    
    // Add shape to superSelectedShape and select shape
    getEditor().getSuperSelectedParentShape().addChild(_shape);
    getEditor().setSelectedShape(_shape);
}

/**
 * Event handling for shape creation.
 */
public void mouseDragged(MouseEvent anEvent)
{
    _shape.repaint();
    RMPoint currentPoint = getEditor().getEditorInputAdapter().getEventPointInShape(true);
    double x = Math.min(_downPoint.x,currentPoint.x);
    double y = Math.min(_downPoint.y, currentPoint.y);
    double width = Math.abs(currentPoint.x - _downPoint.x);
    double height = Math.abs(currentPoint.y - _downPoint.y);
    _shape.setFrame(x, y, width, height);
}

/**
 * Event handling for shape creation.
 */
public void mouseReleased(MouseEvent anEvent)  { getEditor().setCurrentToolToSelectTool(); _shape = null; }

/**
 * Event handling from select tool - called on mouse move when tool shape is super selected.
 * MouseMoved is useful for setting a custom cursor.
 */
public void mouseMoved(T aShape, MouseEvent anEvent)
{
    // Just return if shape isn't the super-selected shape
    if(aShape!=getEditor().getSuperSelectedShape()) return;
    
    // Get handle shape
    RMShapeHandle shapeHandle = getShapeHandleAtPoint(anEvent.getPoint());
    
    // Declare variable for cursor
    Cursor cursor = null;
    
    // If shape handle is non-null, set cursor and return
    if(shapeHandle!=null)
        cursor = shapeHandle.getTool().getHandleCursor(shapeHandle.getShape(), shapeHandle.getHandle());
    
    // If mouse not on handle, check for mouse over a shape
    else {
        
        // Get mouse over shape
        RMShape shape = getEditor().getShapeAtPoint(anEvent.getPoint());
        
        // If shape isn't super selected and it's parent doesn't superselect children immediately, choose move cursor
        if(!isSuperSelected(shape) && !shape.getParent().childrenSuperSelectImmediately())
            cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        
        // If shape is text and either super-selected or child of a super-select-immediately, choose text cursor
        if(shape instanceof RMTextShape && (isSuperSelected(shape) || shape.getParent().childrenSuperSelectImmediately()))
            cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    }
    
    // Set cursor if it differs
    if(getEditor().getCursor()!=cursor)
        getEditor().setCursor(cursor);
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aShape, MouseEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseDragged(T aShape, MouseEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseReleased(T aShape, MouseEvent anEvent)  { }

/**
 * Event hook during selection.
 */
public boolean mousePressedSelection(MouseEvent anEvent)  { return false; }

/**
 * Returns a tool tip string for given shape and event.
 */
public String getToolTipText(T aShape, MouseEvent anEvent)  { return null; }

/**
 * Editor method.
 */
public void processKeyEvent(T aShape, KeyEvent anEvent)  { }

/**
 * Paints when tool is active for things like SelectTool's handles & selection rect or polygon's in-progress path.
 */
public void paintTool(Graphics2D g)  { }

/**
 * Handles painting shape handles (or any indication that a shape is selected/super-selected).
 */
public void paintShapeHandles(T aShape, Graphics2D g, boolean isSuperSelected)
{
    // If no handles, just return
    if(getHandleCount(aShape)==0) return;
    
    // Turn off antialiasing and cache current composite
    RMAWTUtils.setAntialiasing(g, false);
    Composite sc = g.getComposite();
    
    // If super-selected, set composite to make drawing semi-transparent
    if(isSuperSelected)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .64f));
    
    // Determine if rect should be reduced if the shape is especially small
    boolean mini = aShape.getWidth()<16 || aShape.getHeight()<16;
        
    // Iterate over shape handles, get rect (reduce if needed) and draw
    for(int i=0, iMax=getHandleCount(aShape); i<iMax; i++) {
        RMRect hr = getHandleRect(aShape, i, isSuperSelected); if(mini) hr.inset(1, 1);
        g.drawImage(_handle, (int)hr.x, (int)hr.y, (int)hr.width, (int)hr.height, null);
    }
        
    // Restore composite and turn on antialiasing
    g.setComposite(sc);
    RMAWTUtils.setAntialiasing(g, true);
}

/**
 * Returns the number of handles for this shape.
 */
public int getHandleCount(T aShape)  { return 8; }

/**
 * Returns the point for the handle of the given shape at the given handle index in the given shape's coords.
 */
public RMPoint getHandlePoint(T aShape, int aHandle, boolean isSuperSelected)
{
    // Get bounds of given shape
    RMRect bounds = isSuperSelected? getBoundsSuperSelected(aShape).inset(-HandleWidth/2) : aShape.getBoundsInside();
    
    // Get minx and miny of given shape
    double minX = aShape.width()>=0? bounds.x : bounds.getMaxX();
    double minY = aShape.height()>=0? bounds.y : bounds.getMaxY();
    
    // Get maxx and maxy of givn shape
    double maxX = aShape.width()>=0? bounds.getMaxX() : bounds.x;
    double maxY = aShape.height()>=0? bounds.getMaxY() : bounds.y;
    
    // Get midx and midy of given shape
    double midX = minX + (maxX-minX)/2;
    double midY = minY + (maxY-minY)/2;
    
    // Get point for given handle
    switch(aHandle) {
        case HandleNW: return new RMPoint(minX, minY);
        case HandleNE: return new RMPoint(maxX, minY);
        case HandleSW: return new RMPoint(minX, maxY);
        case HandleSE: return new RMPoint(maxX, maxY);
        case HandleW: return new RMPoint(minX, midY);
        case HandleE: return new RMPoint(maxX, midY);
        case HandleN: return new RMPoint(midX, minY);
        case HandleS: return new RMPoint(midX, maxY);
    }
    
    // Return null if invalid handle
    return null;
}

/**
 * Returns the rect for the handle at the given index in editor coords.
 */
public RMRect getHandleRect(T aShape, int aHandle, boolean isSuperSelected)
{
    // Get handle point for given handle index in shape coords and editor coords
    RMPoint hp = getHandlePoint(aShape, aHandle, isSuperSelected);
    RMPoint hpEd = getEditor().convertPointFromShape(hp, aShape);
    
    // Get handle rect at handle point, outset rect by handle width and return
    RMRect hr = new RMRect(Math.round(hpEd.x), Math.round(hpEd.y), 0, 0);
    hr.inset(-HandleWidth/2);
    return hr;
}

/**
 * Returns the handle hit by the given editor coord point.
 */
public int getHandleAtPoint(T aShape, Point2D aPoint, boolean isSuperSelected)
{
    // Iterate over shape handles, get handle rect for current loop handle and return index if rect contains point
    for(int i=0, iMax=getHandleCount(aShape); i<iMax; i++) {
        RMRect hr = getHandleRect(aShape, i, isSuperSelected);
        if(hr.contains(aPoint))
            return i; }
    return -1; // Return -1 since no handle at given point
}

/**
 * Returns the cursor for given handle.
 */
public Cursor getHandleCursor(T aShape, int aHandle)
{
    // Get cursor for handle type
    switch(aHandle) {
        case HandleN: return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        case HandleS: return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        case HandleE: return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        case HandleW: return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        case HandleNW: return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
        case HandleNE: return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
        case HandleSW: return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
        case HandleSE: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
    }

    // Return null
    return null;
}

/**
 * Moves the handle at the given index to the given point.
 */
public void moveShapeHandle(T aShape, int aHandle, RMPoint toPoint)
{
    // Get handle point in shape coords and shape parent coords
    RMPoint p1 = getHandlePoint(aShape, aHandle, false);
    RMPoint p2 = aShape.convertPointFromShape(toPoint, aShape.getParent());
    
    // If middle handle is used, set delta and p2 of that component to 0
    boolean minX = false, maxX = false, minY = false, maxY = false;
    switch(aHandle) {
        case HandleNW: minX = minY = true; break;
        case HandleNE: maxX = minY = true; break;
        case HandleSW: minX = maxY = true; break;
        case HandleSE: maxX = maxY = true; break;
        case HandleW: minX = true; break;
        case HandleE: maxX = true; break;
        case HandleS: maxY = true; break;
        case HandleN: minY = true; break;
    }

    // Calculate new width and height for handle move
    double dx = p2.x - p1.x, dy = p2.y - p1.y;
    double nw = minX? aShape.width() - dx : maxX? aShape.width() + dx : aShape.width();
    double nh = minY? aShape.height() - dy : maxY? aShape.height() + dy : aShape.height();

    // Set new width and height, but calc new X & Y such that opposing handle is at same location w.r.t. parent
    RMPoint op = getHandlePoint(aShape, getHandleOpposing(aHandle), false);
    op = aShape.convertPointToShape(op, aShape.getParent());
    
    // Make sure new width and height are not too small
    if(Math.abs(nw)<.1) nw = RMMath.sign(nw)*.1f;
    if(Math.abs(nh)<.1) nh = RMMath.sign(nh)*.1f;

    // Set size
    aShape.setSize(nw, nh);
    
    // Get point
    RMPoint p = getHandlePoint(aShape, getHandleOpposing(aHandle), false);
    p = aShape.convertPointToShape(p, aShape.getParent());
    
    // Set frame
    aShape.setFrameXY(aShape.getFrameX() + op.x - p.x, aShape.getFrameY() + op.y - p.y);
}

/**
 * Returns the handle index that is across from given handle index.
 */
public int getHandleOpposing(int handle)
{
    // Return opposing handle from given panel
    switch(handle) {
        case HandleNW: return HandleSE;
        case HandleNE: return HandleSW;
        case HandleSW: return HandleNE;
        case HandleSE: return HandleNW;
        case HandleW: return HandleE;
        case HandleE: return HandleW;
        case HandleS: return HandleN;
        case HandleN: return HandleS;
    }
    
    // Return -1 if given handle is unknown
    return -1;
}

/**
 * Returns the fill inspector for this tool's shape class.
 */
public SwingOwner getShapeFillInspector()  { return _shapeFills; }

/**
 * An inner class describing a shape and a handle.
 */
public static class RMShapeHandle {

    // The shape, handle index and shape tool
    RMShape _shape; int _handle; RMTool _tool;
    
    /** Creates a new shape-handle. */
    public RMShapeHandle(RMShape aShape, int aHndl, RMTool aTool) { _shape = aShape; _handle = aHndl; _tool = aTool; }
    
    /** Returns the shape. */
    public RMShape getShape()  { return _shape; }
    
    /** Returns the handle. */
    public int getHandle()  { return _handle; }
    
    /** Returns the tool. */
    public RMTool getTool()  { return _tool; }
}

/**
 * Returns the shape handle for the given editor point.
 */
public RMShapeHandle getShapeHandleAtPoint(Point2D aPoint)
{
    // Declare variable for shape and handle and shape tool
    RMShape shape = null; int handle = -1; RMTool tool = null;

    // Check selected shapes for a selected handle index
    for(int i=0, iMax=getEditor().getSelectedShapeCount(); handle==-1 && i<iMax; i++) {
        shape = getEditor().getSelectedShape(i);
        tool = RMTool.getTool(shape);
        handle = tool.getHandleAtPoint(shape, aPoint, false);
    }

    // Check super selected shapes for a selected handle index
    for(int i=0, iMax=getEditor().getSuperSelectedShapeCount(); handle==-1 && i<iMax; i++) {
        shape = getEditor().getSuperSelectedShape(i);
        tool = RMTool.getTool(shape);
        handle = tool.getHandleAtPoint(shape, aPoint, true);
    }

    // Return shape handle
    return handle>=0? new RMShapeHandle(shape, handle, tool) : null;
}

/**
 * Implemented by shapes that can handle drag & drop.
 */
public boolean acceptsDrag(T aShape, DropTargetDragEvent anEvent)
{
    // Bogus, but currently the page accepts everything
    if(aShape.isRoot()) return true;
    
    // Handle Gallery drag
    if(Gallery.getDragShape()!=null)
        return RMTool.getTool(aShape).getAcceptsChildren(aShape);
    
    // Return true for Color drag or File drag
    if(anEvent.isDataFlavorSupported(ColorWell.getColorDataFlavor())) return true;
    
    // Handle file drag - really just want to check for images here, but can't ask for transferable contents yet
    if(anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        return true;
    
    // Return true in any case if accepts children
    return RMTool.getTool(aShape).getAcceptsChildren(aShape);
}

/**
 * Notifies tool that a something was dragged into of one of it's shapes with drag and drop.
 */
public void dragEnter(RMShape aShape, DropTargetDragEvent anEvent)  { }

/**
 * Notifies tool that a something was dragged out of one of it's shapes with drag and drop.
 */
public void dragExit(RMShape aShape, DropTargetDragEvent anEvent)  { }

/**
 * Notifies tool that something was dragged over one of it's shapes with drag and drop.
 */
public void dragOver(RMShape aShape, DropTargetDragEvent anEvent)  { }

/**
 * Notifies tool that something was dropped on one of it's shapes with drag and drop.
 */
public void drop(T aShape, DropTargetDropEvent anEvent)
{
    // If a binding key drop, apply binding
    if(KeysPanel.getDragKey()!=null)
        KeysPanel.dropDragKey(aShape, anEvent);

    // Handle Gallery drop
    else if(Gallery.getDragShape()!=null)
        Gallery.dropGalleryShape((RMParentShape)aShape, anEvent);
        
    // Handle String drop
    else if(anEvent.isDataFlavorSupported(DataFlavor.stringFlavor))
        dropString(aShape, anEvent);

    // Handle color panel drop
    else if(anEvent.isDataFlavorSupported(ColorWell.getColorDataFlavor()))
        dropColor(aShape, anEvent);

    // Handle File drop - get list of dropped files and add individually
    else if(anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        dropFiles(aShape, anEvent);
}

/**
 * Returns the bounds to be highlighted for a drag over a given shape.
 */
public RMRect getDragDisplayBounds(RMShape aShape, DropTargetDragEvent anEvent)  { return aShape.getBoundsInside(); }

/**
 * Called to handle dropping a string.
 */
public void dropString(T aShape, DropTargetDropEvent anEvent)  { }

/**
 * Called to handle dropping a color.
 */
public void dropColor(RMShape aShape, DropTargetDropEvent anEvent)
{
    // Get dropped color
    Color color;
    try { color = (Color)anEvent.getTransferable().getTransferData(ColorWell.getColorDataFlavor()); }
    catch(Exception e) { e.printStackTrace(); return; }
    
    // Set shape's fill to a new solid RMFill 
    getEditor().undoerSetUndoTitle("Set Fill Color");
    aShape.setFill(new RMFill(new RMColor(color)));
}

/**
 * Called to handle dropping a file.
 */
public void dropFiles(RMShape aShape, DropTargetDropEvent anEvent)
{
    // Get point
    RMPoint point = new RMPoint(anEvent.getLocation());
    
    // Get dropped files list
    List <File> filesList;
    try { filesList = (List)anEvent.getTransferable().getTransferData(DataFlavor.javaFileListFlavor); }
    catch(Exception e) { e.printStackTrace(); return; }
    
    // Iterate over files
    for(File file : filesList)
        point = dropFile(aShape, file, point);
}

/**
 * Called to handle a file drop on the editor.
 */
private RMPoint dropFile(RMShape aShape, File aFile, RMPoint aPoint)
{
    // If directory, recurse and return
    if(aFile.isDirectory()) {
        for(File file : aFile.listFiles())
            aPoint = dropFile(aShape, file, aPoint);
        return aPoint;
    }
    
    // Get path and extension (set to empty string if null)
    final String path = aFile.getPath();
    String ext = RMStringUtils.getPathExtension(path); if(ext==null) ext = "";

    // If xml file, pass it to setDataSource()
    if(ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("json"))
        getEditorPane().setDataSource(WebURL.getURL(aFile), aPoint);

    // If image file, add image shape
    else if(RMImageData.canRead(ext)) { final RMShape shape = aShape; final RMPoint point = aPoint;
        SwingUtilities.invokeLater(new Runnable() { public void run() { dropImageFile(shape, path, point); }}); }

    // If sound file, add sound shape
    else if(RMSoundData.canRead(ext))
        dropSoundFile(aShape, path, aPoint);

    // If reportmill file, addReportFile
    else if(ext.equalsIgnoreCase("rpt"))
        dropReportFile(aShape, path, aPoint);
    
    // Return point offset by 10
    return aPoint.offset(10, 10);
}

/**
 * Called to handle an image drop on the editor.
 */
private void dropImageFile(RMShape aShape, String aPath, RMPoint aPoint)
{
    // If image hit a real shape, see if user wants it to be a texture
    if(aShape!=getEditor().getSelectedPage()) {
        
        // Create drop image file options array
        String options[] = { "Image Shape", "Texture", "Cancel" };
        
        // Run drop image file options panel
        String msg = "Image can be either image shape or texture", title = "Image import";
        DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg); dbox.setOptions(options);
        switch(dbox.showOptionDialog(getEditor(), options[0])) {
        
            // Handle Create Image Shape
            case 0: while(!RMTool.getTool(aShape).getAcceptsChildren(aShape)) aShape = aShape.getParent(); break;
            
            // Handle Create Texture
            case 1: aShape.setFill(new RMImageFill(aPath));
            
            // Handle Cancel
            case 2: return;
        }
    }
    
    // Get parent to add image shape to and drop point in parent coords
    RMParentShape parent = aShape instanceof RMParentShape? (RMParentShape)aShape : aShape.getParent();
    RMPoint point = getEditor().convertPointToShape(aPoint, parent);
    
    // Create new image shape
    RMImageShape imageShape = new RMImageShape(aPath);
    
    // If image not PDF and is bigger than hit shape, shrink down
    if(!imageShape.getImageData().getType().equals("pdf"))
        if(imageShape.getWidth()>parent.getWidth() || imageShape.getHeight()>parent.getHeight()) {
            double w = imageShape.getWidth();
            double h = imageShape.getHeight();
            double w2 = w>h? 320 : 320/h*w;
            double h2 = h>w? 320 : 320/w*h;
            imageShape.setSize(w2, h2);
        }

    // Set bounds centered around point (or centered on page if image covers 75% of page or more)
    if(imageShape.getWidth()/getEditor().getWidth()>.75f || imageShape.getHeight()/getEditor().getHeight()>.75)
        imageShape.setXY(0, 0);
    else imageShape.setXY(point.x - imageShape.getWidth()/2, point.y - imageShape.getHeight()/2);

    // Add imageShape with undo
    getEditor().undoerSetUndoTitle("Add Image");
    parent.addChild(imageShape);
    
    // Select imageShape and SelectTool
    getEditor().setSelectedShape(imageShape);
    getEditor().setCurrentToolToSelectTool();
}

/**
 * Called to handle a sound drop on the editor.
 */
private void dropSoundFile(RMShape aShape, String aPath, RMPoint aPoint)
{
    // Find a parent shape that accepts children
    RMParentShape parent = aShape instanceof RMParentShape? (RMParentShape)aShape : aShape.getParent();
    while(!RMTool.getTool(parent).getAcceptsChildren(parent))
        parent = parent.getParent();
    
    // Create new sound shape
    RMSoundShape soundShape = new RMSoundShape(aPath);
    
    // Center shape around drop point in parent shape
    RMPoint point = getEditor().convertPointToShape(aPoint, parent);
    soundShape.setXY(point.x - soundShape.getWidth()/2, point.y - soundShape.getHeight()/2);

    // Add soundShape, select it and set selectTool
    getEditor().undoerSetUndoTitle("Add Sound");
    parent.addChild(soundShape);
    
    // Select sound shape and select tool
    getEditor().setSelectedShape(soundShape);
    getEditor().setCurrentToolToSelectTool();
}

/**
 * Called to handle a report file drop on the editor.
 */
private void dropReportFile(RMShape aShape, String aPath, RMPoint aPoint)
{
    // Find a parent shape that accepts children
    RMParentShape parent = aShape instanceof RMParentShape? (RMParentShape)aShape : aShape.getParent();
    while(!RMTool.getTool(parent).getAcceptsChildren(parent))
        parent = parent.getParent();
    
    // Get document for dropped file and embedded document shape for document
    RMDocument doc = RMDocument.getDoc(aPath);
    RMNestedDoc ndoc = new RMNestedDoc(); ndoc.setNestedDoc(doc);
    
    // Center embedded document around drop point
    RMPoint point = getEditor().convertPointToShape(aPoint, parent);
    ndoc.setXY(point);

    // Add edoc to document
    getEditor().undoerSetUndoTitle("Add Embedded Document");
    parent.addChild(ndoc);

    // Select edoc and selectTool
    getEditor().setSelectedShape(ndoc);
    getEditor().setCurrentToolToSelectTool();
}

/**
 * Returns a clone of a gallery shape (hook to allow extra configuration for subclasses).
 */
public RMShape getGalleryClone(T aShape)  { return aShape.cloneDeep(); }

/**
 * Returns the icon used to represent shapes that this tool represents.
 */
public Icon getIcon()  { return getIcon(getClass()); }

/**
 * Returns the icon for the given shape class.
 */
public static Icon getIcon(Class aClass)
{
    // Get image name
    String name = aClass.getSimpleName(); if(name.equals("Tool")) name = "Rect";
    name = RMStringUtils.delete(name, "Tool") + ".png";
    
    // Get icon (or return Rectangle icon by default)
    Icon icon = Swing.getIcon(name, aClass);
    return icon==null? Swing.getIcon("RMShape.png", RMRectShapeTool.class) : icon;
}

/**
 * Returns the SelectTool.
 */
public static RMSelectTool getSelectTool()  { return _selectTool; }

/**
 * Returns the specific tool for a list of shapes (if they have the same tool).
 */
public static RMTool getTool(List aList)
{
    Class commonClass = RMClassUtils.getCommonClass(aList); // Get class for first object
    return getTool(commonClass); // Return tool for common class
}

/**
 * Returns the specific tool for a given shape.
 */
public static RMTool getTool(Object anObj)
{
    // Get the shape class and tool from tools map - if not there, find and set
    Class sclass = RMClassUtils.getClass(anObj);
    RMTool tool = _tools.get(sclass);
    if(tool==null)
        _tools.put(sclass, tool = getToolImpl(sclass));
    return tool;
}

/**
 * Returns the specific tool for a given shape.
 */
private static RMTool getToolImpl(Class aClass)
{
    // Handle root
    if(aClass==RMShape.class) return new RMTool();
    
    // Declare variable for tool class
    Class tclass = null;
    
    // If class name starts with RM, check tool package for built-in RMShape tools
    String cname = aClass.getSimpleName();
    if(cname.startsWith("RM")) {
        tclass = RMClassUtils.getClassForName("com.reportmill.apptools." + cname + "Tool");
        if(tclass==null && cname.endsWith("Shape"))
            tclass = RMClassUtils.getClassForName("com.reportmill.apptools." + cname.replace("Shape", "Tool"));
    }

    // If not found, try looking in same package for shape class plus "Tool"
    if(tclass==null)
        tclass = RMClassUtils.getClassForName(aClass.getName() + "Tool", aClass);
    
    // If not found and class ends in "Shape", try looking in same package for class that ends with "Tool" instead
    if(tclass==null && cname.endsWith("Shape"))
        tclass = RMClassUtils.getClassForName(RMStringUtils.replace(aClass.getName(), "Shape", "Tool"), aClass);
    
    // If not found and class is some external shapes package, look in external tools package
    if(tclass==null && aClass.getName().indexOf(".shape.")>0) {
        String classPath = RMStringUtils.replace(aClass.getName(), ".shape.", ".tool.");
        String classPath2 = RMStringUtils.delete(classPath, "Shape") + "Tool";
        tclass = RMClassUtils.getClassForName(classPath2, aClass);
    }
    
    // If not found, try looking for inner class named "Tool"
    if(tclass==null)
        tclass = RMClassUtils.getClassForName(aClass.getName() + "$" + "Tool", aClass);
    
    // If tool class found, instantiate tool class
    if(tclass!=null)
        try { return (RMTool)tclass.newInstance(); }
        catch(Exception e) { throw new RuntimeException(e); }
        
    // Otherwise, get tool for super class
    return getTool(aClass.getSuperclass());
}

}