package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.viewer.*;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * The RMShapeMouseEvent is a subclass of MouseEvent which is sent to an RMShape's mouse listeners.
 * It is the same as a regular mouse event except for two differences:
 *   The shape the action was performed on can be retrieved with getShape(),
 *   and event point in the shape's coordinate system can be retrieved with getShapePoint().
 */
public class RMShapeMouseEvent extends MouseEvent {

    // The event shape
    RMShape             _shape;
    
    // The original mouse event
    MouseEvent          _mouseEvent;
    
    // The point in shape coords
    RMPoint             _point;
    
    // The point in parent coords
    RMPoint             _parentPoint;
    
    // The mouse down event
    static RMShapeMouseEvent   _mouseDown;
    
    // The most recently created event
    static RMShapeMouseEvent   _currentEvent;
    
/**
 * Creates a new shape mouse event.
 */
public RMShapeMouseEvent(RMShape aShape, MouseEvent anEvent)
{
    // Do normal mouse event init
    super(anEvent.getComponent(), anEvent.getID(), anEvent.getWhen(), anEvent.getModifiers() | anEvent.getModifiersEx(), 
          anEvent.getX(), anEvent.getY(), anEvent.getClickCount(), anEvent.isPopupTrigger());
    
    // Set event shape
    _shape = aShape;
    
    // Set mouse event
    _mouseEvent = anEvent;
    
    // Hold on to the mouse down event (and force points to cache)
    if(anEvent.getID()==MouseEvent.MOUSE_PRESSED) {
        _mouseDown = this;
        getPoint2D();
        getParentPoint();
    }
    
    // Set current event
    _currentEvent = this;
}

/**
 * Returns the event shape.
 */
public RMShape getShape()  { return _shape; }

/**
 * Returns the viewer.
 */
public RMViewer getViewer()  { return (RMViewer)getSource(); }

/**
 * Returns the original mouse event.
 */
public MouseEvent getMouseEvent()  { return _mouseEvent; }

/**
 * Returns the event point x.
 */
public double getX2D()  { return getPoint2D().getX(); }

/**
 * Returns the event point y.
 */
public double getY2D()  { return getPoint2D().getY(); }

/**
 * Returns the event point in shape coords.
 */
public RMPoint getPoint2D()
{
    return _point!=null? _point : (_point = getViewer().convertPointToShape(super.getPoint(), getShape()));
}

/**
 * Overrides MouseEvent version to return point in shape coords.
 */
public int getX()  { return (int)Math.round(getX2D()); }

/**
 * Overrides MouseEvent version to return point in shape coords.
 */
public int getY()  { return (int)Math.round(getY2D()); }

/**
 * Overrides MouseEvent version to return point in shape coords.
 */
public Point getPoint()  { return new Point(getX(), getY()); }

/**
 * Returns the event location in shape's parent's coords.
 */
public double getParentX()  { return getParentPoint().getX(); }

/**
 * Returns the event location in shape's parent's coords.
 */
public double getParentY()  { return getParentPoint().getY(); }

/**
 * Returns the event location in shape's parent's coords.
 */
public RMPoint getParentPoint()
{
    // If point hasn't been calculated yet, calculate it
    if(_parentPoint==null)
        _parentPoint = getViewer().convertPointToShape(super.getPoint(), getShape().getParent());
    
    // Return parent point
    return _parentPoint;
}

/**
 * Returns the event location in viewer coords.
 */
public int getViewerX()  { return super.getX(); }

/**
 * Returns the event location in viewer coords.
 */
public int getViewerY()  { return super.getY(); }

/**
 * Returns the event location in viewer coords.
 */
public Point getViewerPoint()  { return super.getPoint(); }

/**
 * Returns the mouse down event.
 */
public RMShapeMouseEvent getMouseDownEvent()  { return _mouseDown; }

/**
 * Overrides MouseEvent version to forward on to encapsulated event.
 */
public void consume()  { super.consume(); getMouseEvent().consume(); }

/**
 * Returns the most recently delivered event.
 */
public static RMShapeMouseEvent getCurrentEvent()  { return _currentEvent; }

}