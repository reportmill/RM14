package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import com.reportmill.viewer.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import javax.swing.*;
import snap.swing.*;
import java.util.*;
import java.util.List;
import snap.util.*;

/**
 * This class subclasses RMViewer to support RMDocument editing.
 */
public class RMEditor extends RMViewer implements DeepChangeListener {

    // Whether we're really editing
    boolean             _editing = true;
    
    // List of currently selected shapes
    List <RMShape>      _selectedShapes = new ArrayList();
    
    // List of super selected shapes (all ancestors of selected shapes)
    List <RMShape>      _superSelectedShapes = new ArrayList();
    
    // The object that handles editor text editing
    RMEditorTextEditor  _textEditor = new RMEditorTextEditor(this);
    
    // The last shape that was copied to the clipboard (used for smart paste)
    RMShape             _lastCopyShape;
    
    // The last shape that was pasted from the clipboard (used for smart paste)
    RMShape             _lastPasteShape;
    
    // A helper class providing utilities for shape
    RMEditorShapes      _shapesHelper = createShapesHelper();
    
    // Whether editor is in a state of rapid change (like in mouse loop)
    boolean             _valueIsAdjusting;
    
    // The current editor tool
    static RMTool       _currentTool = RMTool.getSelectTool();

    // The time of last drop of XML file
    long                _dropTime;

    // Icon for XML image
    static Image        _xmlImage = Swing.createImage("DS_XML.png", RMEditor.class);
    
    // XML Image location for animation
    static Point2D      _xmlLoc;
    
    // Constants for PropertyChanges
    public static final String CurrentTool_Prop = "CurrentTool";
    public static final String SelectedShapes_Prop = "SelectedShapes";
    public static final String SuperSelectedShape_Prop = "SuperSelectedShape";
    public static final String TextSelection_Prop = "TextSelection";
    
/**
 * Creates a new editor.
 */
public RMEditor()
{
    // SuperSelect ViewerShape
    setSuperSelectedShape(getViewerShape());
    
    // Create new drag and drop helper
    createDropTarget();
    
    // Register with the tooltip manager so getToolTipText gets called
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // Disable Tab stealing FocusTraversalKeys
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
}

/**
 * Returns the editor that was most recently activated.
 */
public static RMEditor getMainEditor()  { return RMEditorPaneUtils.getMainEditor(); }

/**
 * Returns the editor pane for this editor, if there is one.
 */
public RMEditorPane getEditorPane()  { return _ep; } RMEditorPane _ep;

/**
 * Override to return as editor shape.
 */
public RMEditorShape getViewerShape()  { return (RMEditorShape)super.getViewerShape(); }

/**
 * Creates the viewer shape.
 */
protected RMViewerShape createViewerShape()  { return new RMEditorShape(this); }

/**
 * Returns whether viewer is really doing editing.
 */
public boolean isEditing()  { return _editing; }

/**
 * Sets whether viewer is really doing editing.
 */
public void setEditing(boolean aFlag)  { _editing = aFlag; }

/**
 * Returns whether the editor is in a state of constant change, like during a mouse drag loop.
 */
public boolean getValueIsAdjusting()  { return _valueIsAdjusting; }

/**
 * Creates the object that is actually responsible for paining shapes in the viewer.
 */
protected RMShapePainterJ2D createShapePainter(Graphics2D aGr)  { return new RMEditorShapePainter(aGr, this); }

/**
 * Called to enabled drag and drop.
 */
public DropTarget createDropTarget()
{
    return new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new RMEditorDnD(this));
}

/**
 * Returns the text editor (or null if not editing).
 */
public RMEditorTextEditor getTextEditor()  { return _textEditor._textShape!=null? _textEditor : null; }

/**
 * Returns the text editor (even if not editing).
 */
public RMEditorTextEditor getTextEditor(boolean evenIfNotActive)  { return _textEditor; }

/**
 * Returns the text editor, primed for editing the given text shape.
 */
public RMEditorTextEditor getTextEditor(RMTextShape aText)  { _textEditor.setTextShape(aText); return _textEditor; }

/**
 * Called to notify the TextEditor has changed selection.
 */
protected void setTextSelection(int aStart, int anEnd)  { firePropertyChange(TextSelection_Prop, null, null); }

/**
 * Returns the shapes helper.
 */
public RMEditorShapes getShapesHelper()  { return _shapesHelper; }

/**
 * Creates the shapes helper.
 */
public RMEditorShapes createShapesHelper()  { return new RMEditorShapes(this); }

/**
 * Returns the first selected shape.
 */
public RMShape getSelectedShape()  { return getSelectedShapeCount()==0? null : getSelectedShape(0); }

/**
 * Selects the given shape.
 */
public void setSelectedShape(RMShape aShape)  { setSelectedShapes(aShape==null? null : Arrays.asList(aShape)); }

/**
 * Returns the number of selected shapes.
 */
public int getSelectedShapeCount()  { return _selectedShapes.size(); }

/**
 * Returns the selected shape at the given index.
 */
public RMShape getSelectedShape(int anIndex)  { return RMListUtils.get(_selectedShapes, anIndex); }

/**
 * Returns the selected shapes list.
 */
public List <RMShape> getSelectedShapes()  { return _selectedShapes; }

/**
 * Selects the shapes in the given list.
 */
public void setSelectedShapes(List <RMShape> theShapes)
{
    // If shapes already set, just return
    if(RMListUtils.equalsId(theShapes, _selectedShapes)) return;
    
    // If shapes is null or empty super-select the selected page and return
    if(theShapes==null || theShapes.size()==0) {
        setSuperSelectedShape(getSelectedPage()); return; }
    
    // Get the first shape in given shapes list
    RMShape shape = theShapes.get(0);
    
    // If shapes contains superSelectedShapes, superSelect last and return (hidden trick for undoSelectedObjects)
    if(theShapes.size()>1 && shape==getDocument()) {
        setSuperSelectedShape(theShapes.get(theShapes.size()-1)); return; }
    
    // Get the shape's parent
    RMShape shapesParent = shape.getParent();
    
    // If shapes parent is the document, super select shape instead
    if(shapesParent==getDocument()) {
        setSuperSelectedShape(shape); return; }
    
    // Super select shapes parent
    setSuperSelectedShape(shapesParent);
    
    // Add shapes to selected list
    _selectedShapes.addAll(theShapes);
    
    // Fire PropertyChange
    firePropertyChange(SelectedShapes_Prop, null, theShapes);
}

/**
 * Add a shape to the selected shapes list.
 */
public void addSelectedShape(RMShape aShape)
{
    List list = new ArrayList(getSelectedShapes()); list.add(aShape);
    setSelectedShapes(list);
}

/**
 * Remove a shape from the selected shapes list.
 */
public void removeSelectedShape(RMShape aShape)
{
    List list = new ArrayList(getSelectedShapes()); list.remove(aShape);
    setSelectedShapes(list);
}

/**
 * Returns the first super-selected shape.
 */
public RMShape getSuperSelectedShape()
{
    return getSuperSelectedShapeCount()==0? null : getSuperSelectedShape(getSuperSelectedShapeCount()-1);
}

/**
 * Returns the first super selected shape, if parent shape.
 */
public RMParentShape getSuperSelectedParentShape()
{
    RMShape ss = getSuperSelectedShape(); return ss instanceof RMParentShape? (RMParentShape)ss : null;
}

/**
 * Super select a shape.
 */
public void setSuperSelectedShape(RMShape aShape)
{
    // If given shape is null, reset to selected page
    RMShape shape = aShape!=null? aShape : getSelectedPage();
    
    // Unselect selected shapes
    _selectedShapes.clear();

    // Remove current super-selected shapes that aren't an ancestor of given shape    
    while(shape!=getSuperSelectedShape() && !shape.isAncestor(getSuperSelectedShape())) {
        RMShape ssShape = getSuperSelectedShape();
        RMTool.getTool(ssShape).willLoseSuperSelectionInEditor(ssShape, this);
        RMListUtils.removeLast(_superSelectedShapes);
    }

    // Add super selected shape (recursively adds parents if missing)
    if(shape!=getSuperSelectedShape())
        addSuperSelectedShape(shape);
    
    // Fire PropertyChange and repaint
    firePropertyChange(SuperSelectedShape_Prop, null, aShape);
    repaint();
}

/**
 * Adds a super selected shape.
 */
private void addSuperSelectedShape(RMShape aShape)
{
    // If parent isn't super selected, add parent first
    if(aShape.getParent()!=null && !isSuperSelected(aShape.getParent()))
        addSuperSelectedShape(aShape.getParent());

    // Add ancestor to super selected list
    _superSelectedShapes.add(aShape);
    
    // Notify tool
    RMTool.getTool(aShape).didBecomeSuperSelectedShapeInEditor(aShape, this);

    // If ancestor is page but not document's selected page, make it the selected page
    if(aShape instanceof RMPage && aShape!=getDocument().getSelectedPage())
        getDocument().setSelectedPage((RMPage)aShape);
}

/**
 * Returns whether a given shape is selected in the editor.
 */
public boolean isSelected(RMShape aShape)  { return RMListUtils.containsId(_selectedShapes, aShape); }

/**
 * Returns whether a given shape is super-selected in the editor.
 */
public boolean isSuperSelected(RMShape aShape)  { return RMListUtils.containsId(_superSelectedShapes, aShape); }

/**
 * Returns the number of super-selected shapes.
 */
public int getSuperSelectedShapeCount()  { return _superSelectedShapes.size(); }

/**
 * Returns the super-selected shape at the given index.
 */
public RMShape getSuperSelectedShape(int anIndex)  { return _superSelectedShapes.get(anIndex); }

/**
 * Returns the super selected shape list.
 */
public List <RMShape> getSuperSelectedShapes()  { return _superSelectedShapes; }

/**
 * Returns the number of currently selected shapes or simply 1, if a shape is super-selected.
 */
public int getSelectedOrSuperSelectedShapeCount()
{
    return getSelectedShapeCount()>0? getSelectedShapeCount() : 1;
}

/**
 * Returns the currently selected shape at the given index, or the super-selected shape.
 */
public RMShape getSelectedOrSuperSelectedShape(int anIndex)
{
    return getSelectedShapeCount()>0? getSelectedShape(anIndex) : getSuperSelectedShape();
}

/**
 * Returns the currently selected shape or, if none, the super-selected shape.
 */
public RMShape getSelectedOrSuperSelectedShape()
{
    return getSelectedShapeCount()>0? getSelectedShape() : getSuperSelectedShape();
}
    
/**
 * Returns the currently selected shapes or, if none, the super-selected shape in a list.
 */
public List <RMShape> getSelectedOrSuperSelectedShapes()
{
    return getSelectedShapeCount()>0? _selectedShapes : Arrays.asList(getSuperSelectedShape());
}
    
/**
 * Un-SuperSelect currently super selected shape.
 */
public void popSelection()
{
    // If there are selected shapes, empty current selection
    if(getSelectedShapeCount()>0)
        setSuperSelectedShape(getSelectedShape().getParent());

    // Otherwise select super-selected shape (or its parent if it has childrenSuperSelectImmediately)
    else if(getSuperSelectedShapeCount()>1) {
        if(getSuperSelectedShape() instanceof RMTextShape)
            setSelectedShape(getSuperSelectedShape());
        else if(getSuperSelectedShape().getParent().childrenSuperSelectImmediately())
            setSuperSelectedShape(getSuperSelectedShape().getParent());
        else setSelectedShape(getSuperSelectedShape());
    }
}

/**
 * Overrides RMViewer implementation to account for selected shapes potentially having different bounds.
 */
public RMRect getRepaintBoundsForShape(RMShape aShape)
{
    // If shape is selected, return marked bounds corrected for handles
    if(isSelected(aShape)) {
    
        // Get shape marked bounds in shape coords, outset for handles, convert to viewer coords and return
        RMRect bounds = aShape.getBoundsMarkedDeep();
        bounds.inset(-4, -4);
        bounds = convertRectFromShape(bounds, aShape);
        return bounds;
    }
    
    // If shape is super-selected, get super-selected bounds corrected for handles
    else if(isSuperSelected(aShape)) {
        
        // Get shape super-selected bounds, outset for handles, convert to viewer coords and return
        RMRect bounds = RMTool.getTool(aShape).getBoundsSuperSelected(aShape);
        bounds.inset(-16, -16);
        bounds = convertRectFromShape(bounds, aShape);
        return bounds;
    }
    
    // Otherwise, return normal viewer implementation
    else return super.getRepaintBoundsForShape(aShape);
}

/**
 * This method finalizes any (potentially cached) changes in progress in the editor (like from text editing).
 */
public void flushEditingChanges()
{
    // Get super-selected shape and its tool and tell tool to flushChanges
    RMShape shape = getSuperSelectedShape();
    RMTool tool = RMTool.getTool(shape);
    tool.flushChanges(this, shape);
}

/**
 * Returns first shape hit by point given in View coords.
 */
public RMShape getShapeAtPoint(Point2D aPoint)
{
    // Get superSelectedShape
    RMShape superSelectedShape = getSuperSelectedShape();
    
    // If superSelectedShape is document, start with the selected page instead (maybe should go)
    if(superSelectedShape==getDocument())
        superSelectedShape = getSelectedPage();

    // Get the point in superSelectedShape's coords
    RMPoint point = convertPointToShape(aPoint, superSelectedShape);

    // Get child of superSelectedShape hit by point
    RMShape shapeAtPoint = getChildShapeAtPoint(superSelectedShape, point);
    
    // If no superSelectedShape child hit by point, find first superSelectedShape that is hit & set to shapeAtPoint
    while(superSelectedShape!=null && shapeAtPoint==null) {
        superSelectedShape.convertPointToShape(point, superSelectedShape.getParent());
        superSelectedShape = superSelectedShape.getParent();
        shapeAtPoint = getChildShapeAtPoint(superSelectedShape, point);
    }

    // See if point really hits an upper level shape that overlaps shapeAtPoint
    if(shapeAtPoint!=null && shapeAtPoint!=getSelectedPage()) {
        
        // Declare shape/point variables used to iterate up shape hierarchy
        RMShape ssShape = shapeAtPoint;
        RMPoint pnt = point;

        // Iterate up shape hierarchy
        while(ssShape!=getSelectedPage() && ssShape.getParent()!=null) {
            
            // Get child of parent hit point point
            RMShape hitChild = getChildShapeAtPoint(ssShape.getParent(), pnt);
            
            // If child not equal to original shape, change shapeAtPoint
            if(hitChild != ssShape) {
                superSelectedShape = ssShape.getParent();
                shapeAtPoint = hitChild;
                point = pnt;
            }
            
            // Update loop shape/point variables
            ssShape = ssShape.getParent();
            pnt = ssShape.convertedPointToShape(pnt, ssShape.getParent());
        }
    }

    // Make sure page is worst case
    if(shapeAtPoint==null || shapeAtPoint==getDocument())
        shapeAtPoint = getSelectedPage();

    // Return shape at point
    return shapeAtPoint;
}

/**
 * Returns the child of the given shape hit by the given point.
 */
public RMShape getChildShapeAtPoint(RMShape aShape, RMPoint aPoint)
{
    // If given shape is null, return null
    if(aShape==null) return null;
    
    // Iterate over shape children
    for(int i=aShape.getChildCount(); i>0; i--) { RMShape child = aShape.getChild(i-1);
        
        // If not hittable, continue
        if(!child.isHittable()) continue;
        
        // Get given point in child shape coords
        RMPoint point = child.convertedPointFromShape(aPoint, aShape);

        // If child is super selected and point is in child super selected bounds, return child
        if(isSuperSelected(child) && point.inRect(RMTool.getTool(child).getBoundsSuperSelected(child)))
            return child;
        
        // If child isn't super selected and contains point, return child
        else if(child.contains(point))
            return child;
    }
    
    // Return null if no children hit by point
    return null;
}

/**
 * Returns the first SuperSelectedShape that accepts children.
 */
public RMParentShape firstSuperSelectedShapeThatAcceptsChildren()
{
    // Get super selected shape
    RMShape shape = getSuperSelectedShape();
    RMParentShape parent = shape instanceof RMParentShape? (RMParentShape)shape : shape.getParent();

    // Iterate up hierarchy until we find a shape that acceptsChildren
    while(!RMTool.getTool(parent).getAcceptsChildren(parent))
        parent = parent.getParent();

    // Make sure page is worst case
    if(parent==getDocument())
        parent = getSelectedPage();

    // Return parent
    return parent;
}

/**
 * Returns the first SuperSelected shape that accepts children at a given point.
 */
public RMShape firstSuperSelectedShapeThatAcceptsChildrenAtPoint(RMPoint aPoint)
{
    // Go up chain of superSelectedShapes until one acceptsChildren and is hit by aPoint
    RMShape shape = getSuperSelectedShape();
    RMParentShape parent = shape instanceof RMParentShape? (RMParentShape)shape : shape.getParent();

    // Iterate up shape hierarchy until we find a shape that is hit and accepts children
    while(!RMTool.getTool(parent).getAcceptsChildren(parent) || !parent.contains(parent.convertedPointFromShape(aPoint, null))) {

        // If shape childrenSuperSelImmd and shape hitByPt, see if any of shape's children qualify (otherwise use parent)
        if(parent.childrenSuperSelectImmediately() && parent.contains(parent.convertedPointFromShape(aPoint, null))) {
            RMShape childShape = parent.getChildContaining(parent.convertedPointFromShape(aPoint, null));
            if(childShape!=null && RMTool.getTool(childShape).getAcceptsChildren(childShape))
                parent = (RMParentShape)childShape;
            else parent = parent.getParent();
        }

        // If shape's children don't superSelectImmediately or it is not hit by aPoint, just go up parent chain
        else parent = parent.getParent();

        if(parent==null)
            return getSelectedPage();
    }

    // Make sure page is worst case
    if(parent==getDocument())
        parent = getSelectedPage();

    // Return shape
    return parent;
}

/**
 * Standard clipboard cut functionality.
 */
public void cut()  { RMEditorClipboard.cut(this); }

/**
 * Standard clipboard copy functionality.
 */
public void copy()  { RMEditorClipboard.copy(this); }

/**
 * Standard clipbard paste functionality.
 */
public void paste()  { RMEditorClipboard.paste(this); }

/**
 * Causes all the children of the current super selected shape to become selected.
 */
public void selectAll()
{
    // If text editing, forward to text editor
    if(getTextEditor()!=null)
        getTextEditor().selectAll();
    
    // Otherwise, select all children
    else if(getSuperSelectedShape().getChildCount()>0) {
        
        // Get list of all hittable children of super-selected shape
        List shapes = new ArrayList();
        for(RMShape shape : getSuperSelectedShape().getChildren())
            if(shape.isHittable())
                shapes.add(shape);
        
        // Select shapes
        setSelectedShapes(shapes);
    }
}

/**
 * Deletes all the currently selected shapes.
 */
public void delete()
{
    // Get copy of selected shapes (just beep and return if no selected shapes)
    List <RMShape> shapes = RMListUtils.clone(_selectedShapes);
    if(shapes.size()==0) {
        if(getTextEditor()==null) Toolkit.getDefaultToolkit().beep(); return; }

    // Get/superSelect parent of selected shapes
    RMParentShape parent = getSelectedShape().getParent();
    setSuperSelectedShape(parent);

    // Set undo title
    undoerSetUndoTitle(getSelectedShapeCount()>1? "Delete Shapes" : "Delete Shape");
    
    // Remove all shapes from their parent
    for(int i=0, iMax=shapes.size(); i<iMax; i++) { RMShape shape = shapes.get(i);
        parent.removeChild(shape);
        if(_lastPasteShape==shape) _lastPasteShape = null;
        if(_lastCopyShape==shape) _lastCopyShape = null;
    }
}

/**
 * Adds shapes as children to given shape.
 */
public void addShapesToShape(List <? extends RMShape> theShapes, RMParentShape aShape, boolean withCorrection)
{
    // If no shapes, just return
    if(theShapes.size()==0) return;
    
    // Declare variables for dx, dy, dr
    double dx = 0, dy = 0, dr = 0;

    // Smart paste
    if(withCorrection) {

        // If there is an last-copy-shape and new shapes will be it's peer, set offset
        if(_lastCopyShape!=null && _lastCopyShape.getParent()==aShape) {

            if(_lastPasteShape!=null) {
                RMShape firstShape = theShapes.get(0);
                dx = 2*_lastPasteShape.x() - _lastCopyShape.x() - firstShape.x();
                dy = 2*_lastPasteShape.y() - _lastCopyShape.y() - firstShape.y();
                dr = 2*_lastPasteShape.getRoll() - _lastCopyShape.getRoll() - firstShape.getRoll();
            }

            else dx = dy = getViewerShape().getGridSpacing();
        }
    }

    // Get each individual shape and add it to the superSelectedShape
    for(int i=0, iMax=theShapes.size(); i<iMax; i++) { RMShape shape = theShapes.get(i);
        
        // Add current loop shape to given parent shape
        aShape.addChild(shape);

        // Smart paste
        if(withCorrection) {
            RMRect parentShapeRect = aShape.getBoundsInside();
            shape.setXY(shape.x() + dx, shape.y() + dy);
            shape.setRoll(shape.getRoll() + dr);
            RMRect rect = shape.getFrame();
            rect.width = Math.max(1, rect.width);
            rect.height = Math.max(1, rect.height);
            if(!parentShapeRect.intersectsRect(rect))
                shape.setXY(0, 0);
        }
    }
}

/**
 * Adds a page to the document after current page.
 */
public void addPage()  { addPage(null, getSelectedPageIndex()+1); }

/**
 * Adds a page to the document before current page.
 */
public void addPagePrevious()  { addPage(null, getSelectedPageIndex()); }

/**
 * Adds a given page to the current document at the given index.
 */
public void addPage(RMPage aPage, int anIndex)
{
    RMDocument doc = getDocument(); if(doc==null) { Toolkit.getDefaultToolkit().beep(); return; }
    RMPage page = aPage!=null? aPage : doc.createPage();
    doc.addPage(page, anIndex);
    setSelectedPageIndex(anIndex);
}

/**
 * Removes current page from document.
 */
public void removePage()
{
    RMDocument doc = getDocument();
    if(doc==null || doc.getPageCount()<=1) { Toolkit.getDefaultToolkit().beep(); return; }
    removePage(getSelectedPageIndex());
}

/**
 * Removes the document page at the given index.
 */
public void removePage(int anIndex)
{
    // Register for Undo, remove page and set page to previous one
    RMDocument doc = getDocument(); if(doc==null) { Toolkit.getDefaultToolkit().beep(); return; }
    undoerSetUndoTitle("Remove Page");
    doc.removePage(anIndex);
    setSelectedPageIndex(Math.min(anIndex, doc.getPageCount()-1));
}

/**
 * Tool method - returns the currently selected tool.
 */
public RMTool getCurrentTool()  { return _currentTool; }

/**
 * Tool method - sets the currently select tool to the given tool.
 */
public void setCurrentTool(RMTool aTool)
{
    // If tool is already current tool, just reactivate and return
    if(aTool==_currentTool) {
        aTool.reactivateTool(); return; }

    // Deactivate current tool and reset to new tool
    _currentTool.deactivateTool();
    
    // Set new current tool
    firePropertyChange(CurrentTool_Prop, _currentTool, _currentTool = aTool);
        
    // Activate new tool and have editor repaint
    _currentTool.activateTool();
        
    // Repaint editor
    repaint();
}

/**
 * Returns whether the select tool is currently selected.
 */
public boolean isCurrentToolSelectTool()  { return _currentTool==RMTool.getSelectTool(); }

/**
 * Sets the current tool to the select tool.
 */
public void setCurrentToolToSelectTool()
{
    if(getCurrentTool()!=RMTool.getSelectTool())
        setCurrentTool(RMTool.getSelectTool());
}

/**
 * Tool method - Returns whether the select tool is currently selected and if it's currently being used to select.
 */
public boolean isCurrentToolSelectToolAndSelecting()
{
    return isCurrentToolSelectTool() && RMTool.getSelectTool().getDragMode()==RMSelectTool.DragMode.Select;
}

/**
 * Resets the currently selected tool.
 */
public void resetCurrentTool()
{
    _currentTool.deactivateTool();
    _currentTool.activateTool();
}

/**
 * Override viewer method to reset selected shapes on page change.
 */
public void setSelectedPageIndex(int anIndex)
{
    super.setSelectedPageIndex(anIndex); // Do normal version
    setSuperSelectedShape(getSelectedPage()); // Super-select new page
}

/**
 * Scrolls selected shapes to visible.
 */
public RMRect getSelectedShapesBounds()
{
    // Scroll to center of selected children to visible
    List <? extends RMShape> shapes = getSelectedOrSuperSelectedShapes();
    
    // Get parent (just return if parent is null or document)
    RMShape parent = shapes.get(0).getParent();
    if(parent==null || parent instanceof RMDocument)
        return getDocumentBounds();
    
    // Get select shapes rect in viewer coords
    RMRect shapeBounds = RMShapeUtils.getBoundsOfChildren(parent, shapes);
    shapeBounds = convertRectFromShape(shapeBounds, parent);
    
    // Return rect
    return shapeBounds;
}

/**
 * Override to have zoom focus on selected shapes rect.
 */
public Rectangle getZoomFocusRect()
{
    RMRect shapesBounds = getSelectedShapesBounds();
    Rectangle visibleRect = getVisibleRect();
    shapesBounds.inset((shapesBounds.width - visibleRect.width)/2, (shapesBounds.height - visibleRect.height)/2);
    return shapesBounds.getBounds();
}

/**
 * Overrides JComponent implementation to paint viewer shapes and page, margin, grid, etc.
 */
public void paintComponent(Graphics g)
{
    // Do normal paint
    super.paintComponent(g);
    
    // Get graphics 2D
    Graphics2D g2 = (Graphics2D)g;
    
    // Draw selection handles and current tool
    if(this == getMainEditor()) {
        if(getCurrentTool()!=RMTool.getSelectTool())
            RMTool.getSelectTool().paintTool(g2);
        getCurrentTool().paintTool(g2);
    }
   
    // Paint proximity guides
    RMEditorProxGuide.paintProximityGuides(this, g2);
    
    // If datasource is present and we're editing, draw _xmlImage in lower right corner of document
    RMDataSource dataSource = getDataSource();
    if(dataSource!=null && isEditing()) {

        // Drawing something relative to the visible rect means we have to disable BLIT_SCROLL_MODE
        if(getParent() instanceof JViewport) { JViewport vp = (JViewport)getParent();
            if(vp.getScrollMode()!=JViewport.SIMPLE_SCROLL_MODE) vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE); }
        
        // Get visible rect and Viewport X & Y
        Rectangle vrect = getVisibleRect();
        int x = (int)vrect.getMaxX() - 53;
        int y = (int)vrect.getMaxY() - 53;
        
        // Ease in animation over 1.8 seconds (1800ms)
        if(_xmlLoc!=null) {
            
            // If DropTime is 0, set from current time
            if(_dropTime==0) _dropTime = System.currentTimeMillis();
            
            // Get time from previous time to now
            float t = (System.currentTimeMillis() - _dropTime)/1800f;
            
            // If time greater than 1, reset location, otherwise, increment location and register for repaint
            if(t>=1) { _xmlLoc = null; _dropTime = 0; }
            else {
                x = (int)(_xmlLoc.getX() + (x - _xmlLoc.getX())*(1-Math.pow(1-t,3)));
                y = (int)(_xmlLoc.getY() + (y - _xmlLoc.getY())*(1-Math.pow(1-t,3)));
                repaint(x-20, y-20, 73, 73);
            }
        }
        
        // Draw semi-transparent image: Cache previous composite, set semi-transparent composite, paint image, restore
        Composite sc = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .9f));
        g2.drawImage(_xmlImage, x, y, 53, 53, null);
        g2.setComposite(sc);
    }
}

/**
 * Creates an editor input adapter for viewer adapter.
 */
public RMViewerInputAdapter createInputAdapter()  { return new RMEditorInputAdapter(this); }

/**
 * Returns the even helper object.
 */
public RMEditorInputAdapter getEditorInputAdapter()  { return (RMEditorInputAdapter)getInputAdapter(); }

/**
 * Override to revalidate when ideal size changes.
 */
protected void processEvent(AWTEvent e)
{
    // Do normal version
    super.processEvent(e);
    
    // See if zoom needs to be reset for any input events
    if(e instanceof InputEvent && (e.getID()==MouseEvent.MOUSE_DRAGGED ||
        e.getID()==MouseEvent.MOUSE_RELEASED || e.getID()==KeyEvent.KEY_RELEASED)) {
        
        // If zoom to factor, revalidate when preferred size changes
        if(isZoomToFactor()) {
            if(!getSize().equals(getPreferredSize()))
                revalidate();
            if(!getVisibleRect().contains(getSelectedShapesBounds()) &&
                RMSelectTool.getSelectTool().getDragMode()==RMSelectTool.DragMode.Move)
                scrollRectToVisible(getSelectedShapesBounds().getBounds());
        }
        
        // If zoom to fit, update zoom to fit factor (just returns if unchanged)
        else setZoomToFitFactor();
    }        
}

/**
 * Returns the ideal size of the viewer.
 */
protected Dimension getPrefSize()
{
    // Get normal ideal size (just return if preview)
    Dimension size = super.getPrefSize(); if(isPreview()) return size;
    
    // If there is rendering outside of page bounds, extend view size to encompass it
    RMParentShape page = getSelectedPage();
    RMRect pageBounds = page.getBoundsInside().inset(-8);
    RMRect childBounds = page.getBoundsOfChildren();
    if(!pageBounds.contains(childBounds)) {
        double width = Math.max(childBounds.x<0? -childBounds.x : 0, childBounds.getMaxX() - page.getWidth());
        double height = Math.max(childBounds.y<0? -childBounds.y : 0, childBounds.getMaxY() - page.getHeight());
        width = Math.min(width, 200);
        height = Math.min(height, 200);
        size.width += Math.ceil(width*2);
        size.height += Math.ceil(height*2);
    }
    
    // Return preferred size
    return size;
}

/** 
 * Returns a tool tip string by asking deepest shape's tool.
 */
public String getToolTipText(MouseEvent anEvent)
{
    // If not editing, do normal get tool tip text
    if(!isEditing())
        return super.getToolTipText(anEvent);
    
    // Get deepest shape under point (just return if null)
    RMShape shape = getShapeAtPoint(anEvent.getPoint(), true);
    if(shape==null)
        return null;
    
    // Get shape tool
    RMTool tool = RMTool.getTool(shape);
    
    // Return tool tip for shape
    return tool.getToolTipText(shape, anEvent);
}

/**
 * Returns the datasource associated with the editor's document.
 */
public RMDataSource getDataSource()  { RMDocument d = getDocument(); return d!=null? d.getDataSource() : null; }

/**
 * Sets the datasource associated with the editor's document.
 */
public void setDataSource(RMDataSource aDataSource)
{
    getDocument().setDataSource(aDataSource);
    repaint();
}

/**
 * Returns the sample dataset from the document's datasource.
 */
public Object getDataSourceDataset()  { RMDataSource ds = getDataSource(); return ds!=null? ds.getDataset() : null; }

/**
 * Called to undo the last edit operation in the editor.
 */
public void undo()
{
    // If undoer exists, do undo, select shapes and repaint
    if(getUndoer()!=null && getUndoer().getUndoSetLast()!=null) {
        UndoSet undoSet = getUndoer().undo();
        setUndoSelection(undoSet.getUndoSelection());
        repaint();
    }

    // Otherwise beep
    else Toolkit.getDefaultToolkit().beep();
}

/**
 * Called to redo the last undo operation in the editor.
 */
public void redo()
{
    // If undoer exists, do undo, select shapes and repaint
    if(getUndoer()!=null && getUndoer().getRedoSetLast()!=null) {
        UndoSet redoSet = getUndoer().redo();
        setUndoSelection(redoSet.getRedoSelection());
        repaint();
    }

    // Otherwise beep
    else Toolkit.getDefaultToolkit().beep();
}

/**
 * Sets the undo selection.
 */
protected void setUndoSelection(Object aSelection)
{
    // Handle List <RMShape>
    if(aSelection instanceof List)
        setSelectedShapes((List)aSelection);
}

/**
 * Property change.
 */
public void deepChange(PropertyChangeListener aShape, PropertyChangeEvent anEvent)
{
    // If deep change for EditorTextEditor, just return since it registers Undo itself (with better coalesce)
    if(getTextEditor()!=null && getTextEditor().getTextShape()==aShape &&
        (anEvent.getSource() instanceof RMXString || anEvent.getSource() instanceof RMXStringRun))
        return;
    
    // If undoer exists, set selected objects and add property change
    Undoer undoer = getUndoer();
    if(undoer!=null) {
        
        // If no changes yet, set selected objects
        if(undoer.getActiveUndoSet().getChangeCount()==0)
            undoer.setUndoSelection(new ArrayList(getSelectedOrSuperSelectedShapes()));
        
        // Add property change
        undoer.addPropertyChange(anEvent);
        
        // If adding child, add to child animator newborns
        String pname = anEvent.getPropertyName();
        if(pname.equals("Child") && anEvent.getNewValue()!=null) {
            RMShape parent = (RMShape)anEvent.getSource(), child = (RMShape)anEvent.getNewValue();
            if(parent.getChildAnimator()!=null)
                parent.getChildAnimator().addNewborn(child);
        }
        
        // Save UndoerChanges after delay
        saveUndoerChangesLater();
    }
    
    // Forward DeepChanges to EditorPane. Should have add/removeDeepChagneLister methods for this.
    RMEditorPane ep = getEditorPane(); if(ep!=null) ep.deepChange(this, anEvent);
}

/**
 * Saves Undo Changes.
 */
protected void saveUndoerChanges()
{
    // If MouseIsDown, come back later
    if(Swing.isMouseDown()) {
        saveUndoerChangesLater(); return; }

    // Get undoer
    Undoer undoer = getUndoer(); if(undoer==null || !undoer.isEnabled()) return;
    
    // Set undo selected-shapes
    List shapes = getSelectedShapeCount()>0? getSelectedShapes() : getSuperSelectedShapes();
    if(undoer.getRedoSelection()==null)
        undoer.setRedoSelection(new ArrayList(shapes));
    
    // Save undo changes
    undoer.saveChanges();
    
    // Re-enable animator
    RMShape shape = getSelectedOrSuperSelectedShape();
    if(shape.getAnimator()!=null)
        shape.getAnimator().setEnabled(true);
}

/**
 * Saves undo changes after a delay.
 */
protected void saveUndoerChangesLater()  { SwingUtils.invokeLaterOnce("SaveChangesLater", _saveChangesRunnable); }
private Runnable _saveChangesRunnable = new Runnable() { public void run() { saveUndoerChanges(); }};

}