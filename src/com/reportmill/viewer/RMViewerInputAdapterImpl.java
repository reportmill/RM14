package com.reportmill.viewer;
import com.reportmill.base.RMListUtils;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Stack;

/**
 * 
 */
public class RMViewerInputAdapterImpl extends RMViewerInputAdapter {

    // The last shape that was hit by a mouse press
    RMShape             _shapePressed;
    
    // The stack of shapes under the mouse for mouse moves
    Stack               _shapeUnderStack = new Stack();
    
    // The stack of cursors (one for each shape in shape stack) for mouse moves
    Stack               _shapeUnderCursorStack = new Stack();

/**
 * Creates a new viewer input adapter.
 */
public RMViewerInputAdapterImpl(RMViewer aViewer)  { super(aViewer); }

/**
 * Handle mouse pressed event.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Request focus
    getViewer().requestFocus();
    
    // Get deepest shape hit by that point that has a URL
    _shapePressed = getViewer().getShapeAtPoint(anEvent.getPoint(), true);
    while(_shapePressed!=null && !_shapePressed.acceptsMouse())
        _shapePressed = _shapePressed.getParent();
    
    // If shape has URL, open it
    if(_shapePressed!=null)
        _shapePressed.mousePressed(new RMShapeMouseEvent(_shapePressed, anEvent));
}

/**
 * Handle mouse dragged event.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get shape under drag point
    RMShape shape = getViewer().getShapeAtPoint(anEvent.getPoint(), true);
    while(shape!=null && !shape.acceptsMouse())
        shape = shape.getParent();
    
    // If shape under move point is different than that of last last move point, update shape under stack
    RMShape lastShapeUnder = _shapeUnderStack.isEmpty()? null : (RMShape)_shapeUnderStack.peek();
    if(shape!=lastShapeUnder)
        updateShapeUnderStack(shape, anEvent);
    
    // Send mouse dragged to pressed shape
    if(_shapePressed!=null)
        _shapePressed.mouseDragged(new RMShapeMouseEvent(_shapePressed, anEvent));
}

/**
 * Handle mouse released event.
 */
public void mouseReleased(MouseEvent anEvent)
{
    if(_shapePressed!=null)
        _shapePressed.mouseReleased(new RMShapeMouseEvent(_shapePressed, anEvent));
}

/**
 * Handle mouse clicked event.
 */
public void mouseClicked(MouseEvent anEvent)
{
    if(_shapePressed!=null)
        _shapePressed.mouseClicked(new RMShapeMouseEvent(_shapePressed, anEvent));
}

/**
 * Handle mouse entered.
 */
public void mouseEntered(MouseEvent anEvent)  { }

/**
 * Handle mouse moved event.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Get shape under move point
    RMShape shape = getViewer().getShapeAtPoint(anEvent.getPoint(), true);
    while(shape!=null && !shape.acceptsMouse())
        shape = shape.getParent();
    
    // If shape under move point is identical to shape under last move point, call its mouseMoved
    if(!_shapeUnderStack.isEmpty() && _shapeUnderStack.peek()==shape)
        shape.mouseMoved(new RMShapeMouseEvent(shape, anEvent));
    
    // If shape under move point is different from last shape under, update it
    else updateShapeUnderStack(shape, anEvent);
}

/**
 * Handle mouse exited.
 */
public void mouseExited(MouseEvent anEvent)  { }

/**
 * Handle paint.
 */
public void paint(Graphics2D g)  { }

/**
 * Handle copy.
 */
public void copy()  { }

/**
 * The shape under stack should always be a stack of descendants that acceptEvents.
 */
protected void updateShapeUnderStack(RMShape aShape, MouseEvent anEvent)
{
    // Get first ancestor that acceptsEvents
    RMShape parent = aShape==null? null : aShape.getParent();
    while(parent!=null && !parent.acceptsMouse())
        parent = parent.getParent();
    
    // If a parent acceptEvents, then empty _shapeUnderStack so it only contains parent
    if(parent!=null && !RMListUtils.containsId(_shapeUnderStack, parent))
        updateShapeUnderStack(parent, anEvent);
        
    // Empty _shapeUnderStack so it only contains aShape
    while(!_shapeUnderStack.isEmpty() && _shapeUnderStack.peek()!=parent && _shapeUnderStack.peek()!=aShape) {
        
        // Pop top shape and send mouse exited
        RMShape shape = (RMShape)_shapeUnderStack.pop();
        
        // Pop top cursor
        _shapeUnderCursorStack.pop();
        
        // Send mouse exited
        shape.mouseExited(new RMShapeMouseEvent(shape, anEvent));
        
        // Reset cursor
        getViewer().setCursor(_shapeUnderCursorStack.isEmpty()? Cursor.getDefaultCursor() : (Cursor)_shapeUnderCursorStack.peek());
    }
    
    // If aShape is no longer child of parent, just return (could happen if mouse over parent changes children)
    if(parent!=null && !parent.isDescendant(aShape))
        return;

    // Add aShape if non-null
    if(aShape!=null && (_shapeUnderStack.isEmpty() || _shapeUnderStack.peek()!=aShape)) {
        aShape.mouseEntered(new RMShapeMouseEvent(aShape, anEvent));
        _shapeUnderStack.push(aShape);
        _shapeUnderCursorStack.push(getViewer().getCursor());
    }
}

}