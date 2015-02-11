package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.app.RMEditorProxGuide;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import snap.swing.Swing;

/**
 * This class handles mouse selection and manipulation of shapes, including:
 *   - Click on a shape selects a shape
 *   - Double click on a shape super-selects a shape
 *   - Drag a rect selects shapes
 *   - Shift click or shift drag XORs selection
 *   - Click and drag handle resizes shape
 */
public class RMSelectTool extends RMTool {
    
    // The mode of current even loop (Move, Resize, etc.)
    DragMode      _dragMode = DragMode.None;
    
    // The point of last mouse
    RMPoint       _lastMousePoint;
    
    // A construct representing a shape whose handle was hit and the handle
    RMShapeHandle _shapeHandle;
    
    // The shape handling mouse events
    RMShape       _eventShape;

    // The current selection rect (during DragModeSelect)
    RMRect        _selectionRect = new RMRect();
    
    // The list of shapes currently selected while selecting
    List          _whileSelectingSelectedShapes = new Vector();
    
    // Whether to re-enter mouse pressed
    boolean       _redoMousePressed;

    // Drag mode constants
    public enum DragMode { None, Move, Rotate, Resize, Select, EventDispatch };
    
/**
 * Creates a new select tool.
 */
public RMSelectTool() { }

/**
 * Handles mouse moved - forward on to super selected shape tool.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Iterate over super selected shapes and forward mouseMoved for each shape
    for(int i=1, iMax=getEditor().getSuperSelectedShapeCount(); i<iMax && !anEvent.isConsumed(); i++) {
        
        // Get super selected shape and it's tool and forward mouse moved to it
        RMShape shape = getEditor().getSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.mouseMoved(shape, anEvent);
    }
}

/**
 * Handles mouse pressed for the select tool.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Get current editor
    RMEditor editor = getEditor();

    // Call setNeedsRepaint on superSelectedShapes to wipe out handles
    RMShapeUtils.repaint(editor.getSuperSelectedShapes());

    // See if tool wants to handle this one
    RMTool toolShared = RMTool.getTool(editor.getSelectedOrSuperSelectedShapes());
    if(toolShared!=null && toolShared.mousePressedSelection(anEvent)) {
        _dragMode = DragMode.None; return; }
    
    // Reset re-enter flag
    _redoMousePressed = false;

    // Set downPoint to event location.
    _downPoint = editor.getEditorInputAdapter().getEventPointInDoc();
    
    // Get shape handle for event point
    _shapeHandle = getShapeHandleAtPoint(anEvent.getPoint());

    // If shape handle was found for event point, set mode to resize.
    if(_shapeHandle!=null) {
        
        // Set _dragMode to Resize
        _dragMode = DragMode.Resize;
        
        // Register shape handle shape for repaint
        _shapeHandle.getShape().repaint();

        // If _selectedShape is superSelected, select it instead
        if(isSuperSelected(_shapeHandle.getShape()))
            editor.setSelectedShape(_shapeHandle.getShape());

        // Just return
        return;
    }
    
    // Get selected shape at event point
    RMShape selectedShape = editor.getShapeAtPoint(anEvent.getPoint());
    
    // If hit shape is super selected, then forward the event
    if(isSuperSelected(selectedShape)) {

        // If selectedShape isn't editor superSelectedShape, superSelect it (ie., pop the selection)
        if(selectedShape != editor.getSuperSelectedShape())
            editor.setSuperSelectedShape(selectedShape);
        
        // Set drag mode to select
        _dragMode = DragMode.Select;
    }

    // If mouseDown was Multi-click and _selectedShape is super-selectable
    else if(anEvent.getClickCount()>1 && RMTool.getTool(selectedShape).isSuperSelectable(selectedShape)) {
        
        // Super select selectedShape
        editor.setSuperSelectedShape(selectedShape);
        
        // Create new mouse event with reduced click count
        MouseEvent event = new MouseEvent(anEvent.getComponent(), anEvent.getID(), anEvent.getWhen(),
                anEvent.getModifiers(), anEvent.getX(), anEvent.getY(), anEvent.getClickCount()-1, false);
        
        // Re-enter mouse pressed with new event and return
        mousePressed(event); return;
    }

    // If event was shift click, either add or remove hit shape from list
    else if(anEvent.isShiftDown()) {
            
        // If mouse pressed shape is already selected, remove it and reset drag mode to none
        if(isSelected(selectedShape)) {
            editor.removeSelectedShape(selectedShape); _dragMode = DragMode.None; }
        
        // If shape wasn't yet selected, add it to selected shapes
        else { editor.addSelectedShape(selectedShape); _dragMode = DragMode.Move; }
    }
        
    // Otherwise, handle normal mouse press on shape
    else {
            
        // If hit shape isn't selected then select it
        if(!isSelected(selectedShape))
            editor.setSelectedShape(selectedShape);
        
        // Set drag mode to move
        _dragMode = !Swing.isAltDown()? DragMode.Move : DragMode.Rotate;
    }
    
    // If a shape was selected whose parent childrenSuperSelectImmediately, go ahead and super select it
    if(editor.getSelectedShape()!=null && editor.getSuperSelectedShape().childrenSuperSelectImmediately()) {
        
        // Super select selected shape, re-enter mouse pressed and return
        editor.setSuperSelectedShape(editor.getSelectedShape());
        mousePressed(anEvent); return;
    }
    
    // Set last point to event point in super selected shape coords
    _lastMousePoint = editor.getEditorInputAdapter().getEventPointInShape(false);
    
    // Get editor super selected shape
    RMShape superSelectedShape = editor.getSuperSelectedShape();
        
    // Call mouse pressed for superSelectedShape's tool
    RMTool.getTool(superSelectedShape).mousePressed(superSelectedShape, anEvent);
    
    // If redo mouse pressed was requested, do redo
    if(getRedoMousePressed()) {
        mousePressed(anEvent); return; }
        
    // If event was consumed, set event shape and DragMode to event dispatch and return
    if(anEvent.isConsumed()) {
        _eventShape = superSelectedShape; _dragMode = DragMode.EventDispatch; return; }
    
    // Get the shape at the event point
    RMShape mousePressedShape = editor.getShapeAtPoint(anEvent.getPoint());
    
    // If mousePressedShape is the editor's selected shape, call mouse pressed on mousePressedShape's tool
    if(isSelected(mousePressedShape)) {
        
        // Call mouse pressed on mousePressedShape's tool
        RMTool.getTool(mousePressedShape).mousePressed(mousePressedShape, anEvent);
        
        // If redo mouse pressed was requested, do redo
        if(getRedoMousePressed()) {
            mousePressed(anEvent); return; }
            
        // If event was consumed, set event shape and drag mode to event dispatch and return
        if(anEvent.isConsumed()) {
            _eventShape = mousePressedShape; _dragMode = DragMode.EventDispatch; return; }
    }
}

/**
 * Handles mouse dragged for the select tool.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get current editor
    RMEditor editor = getEditor();
    
    // Holding ctrl down at any point during a drag prevents snapping 
    boolean shouldSnap = ((anEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0);

    // Handle specific drag modes
    switch(_dragMode) {

        // Handle DragModeMove
        case Move:
            
            // Set undo title
            editor.undoerSetUndoTitle("Move");
            
            // Get SuperSelectedShape and disable ParentTracksBoundsOfChildren
            RMParentShape parent = editor.getSuperSelectedParentShape();
            
            // Get event point in super selected shape coords
            RMPoint point = editor.getEditorInputAdapter().getEventPointInShape(false);

            // Move shapes once to event point without SnapToGrid
            moveShapes(_lastMousePoint, point);
                        
            // Get event point snapped to grid & edges, since SnapEdges will now be valid
            RMPoint pointSnapped = editor.getEditorInputAdapter().getEventPointInShape(shouldSnap, shouldSnap);
            RMPoint pointSnappedDoc = parent.convertedPointToShape(pointSnapped, null);
            
            // Move shapes again to snapped point
            moveShapes(point, pointSnapped);
            
            // Get PointSnapped in (potentially) new bounds and break
            _lastMousePoint = parent.convertedPointFromShape(pointSnappedDoc, null);
            break;
            
        // Handle Rotate
        case Rotate:

            // Set Undo title
            editor.undoerSetUndoTitle("Rotate");
            RMPoint point2 = editor.getEditorInputAdapter().getEventPointInShape(false);
            
            // Iterate over selected shapes and update roll
            for(RMShape shape : editor.getSelectedShapes()) { if(shape.isLocked()) continue;
                shape.setRoll(shape.getRoll() + point2.y - _lastMousePoint.y); }

            // Reset last point and break
            _lastMousePoint = point2;
            break;

        // Handle DragModeResize
        case Resize:
            
            // Register undo title "Resize"
            editor.undoerSetUndoTitle("Resize");
            
            // Get event point in super selected shape coords snapped to grid 
            RMPoint resizePoint = editor.getEditorInputAdapter().getEventPointInShape(shouldSnap);
            
            // Move handle to current point and break
            _shapeHandle.getTool().moveShapeHandle(_shapeHandle.getShape(), _shapeHandle.getHandle(), resizePoint);
            break;

        // Handle DragModeSelect
        case Select:

            // Get current hit shapes
            List newShapes = getHitShapes();
            
            // Set current selected shapes to be redrawn
            for(int i=0, iMax=_whileSelectingSelectedShapes.size(); i<iMax; i++)
                ((RMShape)_whileSelectingSelectedShapes.get(i)).repaint();
            
            // Set current selection rect to be redrawn
            editor.repaint(editor.convertRectFromShape(_selectionRect.insetRect(-2, -2), null));
            
            // Get new _selectionRect and clear _whileSelectingSelectedShapes
            _selectionRect = new RMRect(_downPoint, editor.convertPointToShape(anEvent.getPoint(), null));
            _whileSelectingSelectedShapes.clear();

            // If shift key was down, exclusive OR (xor) newShapes with selectedShapes
            if(anEvent.isShiftDown()) {
                List xor = RMListUtils.clone(editor.getSelectedShapes());
                RMListUtils.xor(xor, newShapes);
                _whileSelectingSelectedShapes.addAll(xor);
            }
            
            // If shit key not down, select all new shapes
            else _whileSelectingSelectedShapes.addAll(newShapes);

            // Set newly selected shapes and new selection rect to be redrawn
            for(int i=0, iMax=_whileSelectingSelectedShapes.size(); i<iMax; i++)
                ((RMShape)_whileSelectingSelectedShapes.get(i)).repaint();
            editor.repaint(editor.convertRectFromShape(_selectionRect.insetRect(-2, -2), null));

            // break
            break;

        // Handle DragModeSuperSelect: Forward mouse drag on to super selected shape's mouse dragged and break
        case EventDispatch: RMTool.getTool(_eventShape).mouseDragged(_eventShape, anEvent); break;

        // Handle DragModeNone
        case None: break;
    }
    
    // Create guidelines
    RMEditorProxGuide.createGuidelines(editor);
}

/**
 * Handles mouse released for the select tool.
 */
public void mouseReleased(MouseEvent anEvent)
{
    RMEditor editor = getEditor();
    
    // Handle DragModes
    switch(_dragMode) {

        // Handle Select
        case Select:
            
            // Get hit shapes
            List newShapes = getHitShapes();
            
            // If shift key was down, exclusive OR (xor) newShapes with selectedShapes. Else select new shapes
            if(newShapes.size()>0) {
                if(anEvent.isShiftDown()) {
                    List xor = RMListUtils.clone(editor.getSelectedShapes());
                    RMListUtils.xor(xor, newShapes);
                    editor.setSelectedShapes(xor);
                }
                else editor.setSelectedShapes(newShapes);
            }
            
            // If no shapes were selected, clear selectedShapes
            else editor.setSuperSelectedShape(editor.getSuperSelectedShape());

            // Reset _whileSelectingSelectedShapes and _selectionRect since we don't need them anymore
            _whileSelectingSelectedShapes.clear();
            _selectionRect.setRect(0,0,0,0);
            break;

        // Handle EventDispatch
        case EventDispatch:
            RMTool tool = RMTool.getTool(_eventShape);
            tool.mouseReleased(_eventShape, anEvent);
            _eventShape = null;
            break;
    }
    
    // Clear proximity guidelines
    RMEditorProxGuide.clearGuidelines(editor);

    // Repaint editor
    editor.repaint();
    
    // Reset drag mode
    _dragMode = DragMode.None;
}

/**
 * Moves the currently selected shapes from a point to a point.
 */
private void moveShapes(RMPoint fromPoint, RMPoint toPoint)
{
    // Iterate over selected shapes
    for(int i=0, iMax=getEditor().getSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = getEditor().getSelectedShape(i); if(shape.isLocked()) continue;
        shape.setFrameXY(shape.getFrameX() + toPoint.x - fromPoint.x, shape.getFrameY() + toPoint.y - fromPoint.y);
    }
}

/**
 * Returns the list of shapes hit by the selection rect formed by the down point and current point.
 */
private List <RMShape> getHitShapes()
{
    // Get selection path from rect around currentPoint and _downPoint
    RMEditor editor = getEditor();
    RMParentShape superShape = editor.getSuperSelectedParentShape(); if(superShape==null)return Collections.emptyList();
    RMPoint currentPoint = editor.getEditorInputAdapter().getEventPointInDoc();
    RMRect selectionRect = new RMRect(currentPoint, _downPoint);
    RMPath pathInWorldCoords = RMPathUtils.appendShape(new RMPath(), selectionRect);
    RMPath path = superShape.convertPathFromShape(pathInWorldCoords, null);

    // If selection rect is outside super selected shape, move up shape hierarchy
    while(superShape.getParent()!=null &&
        !path.getBounds2D().intersectsRectEvenIfEmpty(RMTool.getTool(superShape).getBoundsSuperSelected(superShape))) {
        RMParentShape parent = superShape.getParent();
        editor.setSuperSelectedShape(parent);
        path = superShape.convertPathToShape(path, parent);
        superShape = parent;
    }

    // Make sure page is worst case
    if(superShape == editor.getDocument()) {
        superShape = editor.getSelectedPage();
        path = superShape.convertPathFromShape(pathInWorldCoords, null);
        editor.setSuperSelectedShape(superShape);
    }

    // Returns the children of the super-selected shape that intersect selection path
    return superShape.getChildrenIntersecting(path);
}

/**
 * Returns the last drag mode handled by the select tool.
 */
public DragMode getDragMode()  { return _dragMode; }

/**
 * Returns whether select tool should redo current mouse down.
 */
public boolean getRedoMousePressed()  { return _redoMousePressed; }

/**
 * Sets whether select tool should redo current mouse dwon.
 */
public void setRedoMousePressed(boolean aFlag)  { _redoMousePressed = aFlag; }

/**
 * Paints tool specific things, like handles.
 */
public void paintTool(Graphics2D g)
{
    // Iterate over super selected shapes and have tool paint SuperSelected
    for(int i=1, iMax=getEditor().getSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = getEditor().getSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.paintShapeHandles(shape, g, true);
    }
    
    // Get selected shapes
    List selectedShapes = getEditor().getSelectedShapes();
    
    // If in mouse loop, substitute "while selecting shapes"
    if(getEditor().getValueIsAdjusting())
        selectedShapes = _whileSelectingSelectedShapes;

    // Iterate over SelectedShapes and have tool paint Selected
    for(int i=0, iMax=selectedShapes.size(); i<iMax; i++) { RMShape shape = (RMShape)selectedShapes.get(i);
        RMTool tool = RMTool.getTool(shape);
        tool.paintShapeHandles(shape, g, false);
    }

    // Draw _selectionRect
    if(!_selectionRect.isEmpty()) {

        // Get selection rect in editor coords
        RMRect rect = getEditor().convertRectFromShape(_selectionRect, null);

        // Draw selection content as light transparent rect
        g.setColor(new Color(.9f, .9f, .9f, .5f));
        g.fill(rect);

        // Draw selection frame as darker transparent border
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(.6f, .6f, .6f, .6f));
        g.draw(rect);
    }
}

/**
 * Tool callback selects parent of selected shapes (or just shape, if it's super-selected).
 */
public void reactivateTool()  { getEditor().popSelection(); }

}