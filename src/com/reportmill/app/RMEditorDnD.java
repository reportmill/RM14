package com.reportmill.app;
import com.reportmill.apptools.RMTool;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import javax.swing.*;
import snap.swing.SwingUtils;

/**
 * Handles editor methods specific to drag and drop operations.
 */
class RMEditorDnD implements DropTargetListener {
    
    // The editor that this class is working for
    RMEditor        _editor;
    
    // The last shape that a drag and drop action was over
    RMShape         _lastOverShape;

    // The last point sent to dragOver().  Used to coalesce events
    Point2D         _lastDragPoint;
    
/**
 * Creates a new editor drop target listener.
 */
public RMEditorDnD(RMEditor anEditor)  { _editor = anEditor; }

/**
 * Drop target listener method.
 */
public void dragEnter(DropTargetDragEvent anEvent)
{
    // Reset last over shape and last drag point
    _lastOverShape = null;
    _lastDragPoint = null;
    
    // Do a drag over to get things started
    dragOver(anEvent);
}

/**
 * Drop target listener method.
 */
public void dragOver(DropTargetDragEvent anEvent)
{
    // Windows calls this method continuously, as long as the mouse is held down
    if(anEvent.getLocation().equals(_lastDragPoint)) return;
    _lastDragPoint = anEvent.getLocation();
    
    // Get shape at drag point (or the page, if none there)
    RMShape overShape = _editor.getShapeAtPoint(anEvent.getLocation(), true);
    if(overShape==null)
        overShape = _editor.getSelectedPage();
    
    // Go up chain until we find a shape that accepts drag
    while(!RMTool.getTool(overShape).acceptsDrag(overShape, anEvent))
        overShape = overShape.getParent();
    
    // If new overShape, do drag exit/enter and reset border
    if(overShape!=_lastOverShape) {
        
        // Send drag exit
        if(_lastOverShape!=null)
            RMTool.getTool(_lastOverShape).dragExit(_lastOverShape, anEvent);
        
        // Send drag enter
        RMTool.getTool(overShape).dragEnter(overShape, anEvent);
        
        // Get bounds of over shape
        RMRect bounds = RMTool.getTool(overShape).getDragDisplayBounds(overShape, anEvent);
        
        // Convert bounds of over shape to editor coords
        bounds = _editor.convertRectFromShape(bounds, overShape);
        
        // Get border that insets to over-shape bounds compounded with blue line border
        javax.swing.border.Border b1 = BorderFactory.createEmptyBorder((int)bounds.y, (int)bounds.x,
            _editor.getHeight() - (int)bounds.getMaxY(), _editor.getWidth() - (int)bounds.getMaxX());
        javax.swing.border.Border b2 = BorderFactory.createLineBorder(Color.blue, 2);
        javax.swing.border.Border b3 = BorderFactory.createCompoundBorder(b1, b2);
        
        // Set border on editor
        _editor.setBorder(b3);
        
        // Update last drop shape
        _lastOverShape = overShape;
    }
    
    // If over shape didn't change, send drag over
    else RMTool.getTool(overShape).dragOver(overShape, anEvent);

    // If gallery drag, calculate proximity guides for dragshape's rect and children of overshape
    if(Gallery.getDragShape() != null) {
        
        // Get drag shape bounds in destination shape coords
        RMShape shape = Gallery.getDragShape();
        RMPoint dpoint = _editor.convertPointToShape(anEvent.getLocation(), overShape);
        if(DragSource.isDragImageSupported()) { dpoint.x -= shape.getWidth()/2; dpoint.y -= shape.getHeight()/2; }
        RMRect bounds = new RMRect(dpoint.x, dpoint.y, shape.getWidth(), shape.getHeight());
        
        // Create guidelines for drag
        RMEditorProxGuide.createGuidelines(_editor, overShape, bounds, overShape.getChildren());
    }

    // Accept drag
    anEvent.acceptDrag(DnDConstants.ACTION_COPY);
}

/**
 * Drop target listener method.
 */
public void dropActionChanged(DropTargetDragEvent anEvent)  { anEvent.acceptDrag(DnDConstants.ACTION_COPY); }

/**
 * Drop target listener method.
 */
public void dragExit(DropTargetEvent anEvent)
{
    // Reset border
    _editor.setBorder(null);
    
    // Reset proximity guide
    RMEditorProxGuide.clearGuidelines(_editor);
}

/**
 * Drop target listener method.
 */
public void drop(final DropTargetDropEvent anEvent)
{
    // Formally accept drop
    anEvent.acceptDrop(DnDConstants.ACTION_COPY);
    
    // Order window front (for any getMainEditor calls, but really should be true anyway)
    SwingUtils.getWindow(_editor).toFront();
    
    // Forward drop to last over shape
    RMTool.getTool(_lastOverShape).drop(_lastOverShape, anEvent);
    
    // Formally complete drop
    anEvent.dropComplete(true);
    
    // Reset border (which may have been set during dragOver)
    _editor.setBorder(null);
}

}