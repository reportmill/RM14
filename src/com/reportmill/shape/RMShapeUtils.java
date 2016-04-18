package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Some shape utility methods.
 */
public class RMShapeUtils {

    // The Hover timer
    static HoverTimer      _hoverTimer;

    // The Hover shape
    static RMShape         _hoverShape;

/**
 * Convenience to mark a whole list of shapes for repaint.
 */
public static void repaint(List <? extends RMShape> theShapes)
{
    // Iterate over shapes in list and call set needs repaint
    for(int i=0, iMax=theShapes.size(); i<iMax; i++)
        theShapes.get(i).repaint();
}

/**
 * Returns the bounds of a given subset of this shape's children.
 */
public static RMRect getBoundsOfChildren(RMShape aShape, List <? extends RMShape> aList)
{
    // If list is null or empty, return this shape's bounds inside
    if(aList==null || aList.size()==0)
        return aShape.getBoundsInside();
    
    // Declare and initialize a rect to frame of first shape in list
    RMRect rect = aList.get(0).getFrame();
    
    // Iterate over successive shapes in list and union their frames
    for(int i=1, iMax=aList.size(); i<iMax; i++) {
        RMShape child = aList.get(i);
        rect.unionEvenIfEmpty(child.getFrame());
    }
    
    // Return frame
    return rect;
}

/**
 * Adds the subset of children in given list into a given group shape, which is then added back to receiver.
 */
public static RMParentShape groupShapes(List <? extends RMShape> theShapes, RMParentShape groupShape)
{
    // Get copy of shapes, sorted by their original index in parent
    List <? extends RMShape> shapes = RMSort.sortedList(theShapes, "indexOf");
    
    // Get parent
    RMParentShape parent = shapes.get(0).getParent();
    
    // If no group shape, create one
    if(groupShape==null) {
        groupShape = new RMSpringShape();
        groupShape.setBounds(getBoundsOfChildren(parent, shapes));
    }

    // Add groupShape to the current parent (with no transform)
    parent.addChild(groupShape);

    // Remove children from current parent and add to groupShape
    for(RMShape child : shapes) {
        child.convertToShape(null);
        parent.removeChild(child);
        groupShape.addChild(child);
        child.convertFromShape(null);
    }

    // Return group shape
    return groupShape;
}

/**
 * Returns a polygon shape by combining paths of given shapes.
 */
public static RMPolygonShape getCombinedPathsShape(List <RMShape> theShapes)
{
    // Get first shape, parent and combined bounds
    RMShape shape0 = theShapes.size()>0? theShapes.get(0) : null; if(shape0==null) return null;
    RMShape parent = shape0.getParent();
    RMRect combinedBounds = getBoundsOfChildren(parent, theShapes);
    
    // Get the path of the combined shapes
    RMPath combinedPath = getCombinedPath(theShapes);

    // Create combined shape, configure and return
    RMPolygonShape shape = new RMPolygonShape(combinedPath);
    shape.copyShape(shape0); shape._rss = null;
    shape.setFrame(combinedBounds);
    return shape;
}

/**
 * Returns the combined path from given shapes.
 */
public static RMPath getCombinedPath(List <RMShape> theShapes)
{
    List <RMPath> paths = getPathsFromShapes(theShapes, 0); // Get shape paths
    return RMPathUtils.join(paths, RMPathUtils.JOIN_OP_ADD); // Return joined paths
}

/**
 * Returns a polygon shape by combining paths of given shapes.
 */
public static RMPolygonShape getSubtractedPathsShape(List <RMShape> theShapes, int anInset)
{
    // Get SubtractedPath by subtracting paths and its bounds
    RMPath subtractedPath = getSubtractedPath(theShapes, 0);
    RMRect subtractedBounds = subtractedPath.getBounds2D();

    // Create shape, configure and return
    RMPolygonShape shape = new RMPolygonShape(subtractedPath);
    shape.copyShape(theShapes.get(0)); shape._rss = null;
    shape.setBounds(subtractedBounds);
    return shape;
}

/**
 * Returns the combined path from given shapes.
 */
public static RMPath getSubtractedPath(List <RMShape> theShapes, int anInset)
{
    // Eliminate shapes that don't intersect first shape frame
    RMShape shape0 = theShapes.get(0);
    RMRect shape0Frame = shape0.getFrame();
    List <RMShape> shapes = theShapes;
    for(int i=shapes.size()-1; i>=0; i--) { RMShape shape = shapes.get(i);
        if(!shape.getFrame().intersects(shape0Frame)) {
            if(shapes==theShapes) shapes = new ArrayList(theShapes); shapes.remove(i); }}
    
    // Get shape paths and return paths subtracted from first path
    List <RMPath> paths = getPathsFromShapes(shapes, anInset);
    return RMPathUtils.join(paths, RMPathUtils.JOIN_OP_SUBTRACT);
}

/**
 * Returns the list of paths from the given shapes list.
 */
private static List <RMPath> getPathsFromShapes(List <RMShape> theShapes, int anInset)
{
    // Get first shape and parent
    RMShape shape0 = theShapes.get(0);
    RMShape parent = shape0.getParent(); // Should probably get common ancestor

    // Create a list for shape paths
    List paths = new ArrayList(theShapes.size());
    
    // Iterate over shapes, get bounds of each (inset), path of each (in parent coords) and add to list
    for(int i=0, iMax=theShapes.size(); i<iMax; i++) { RMShape shape = theShapes.get(i);
        RMRect bounds = shape.getBoundsInside(); if(anInset!=0 && i>0) bounds.inset(anInset);
        RMPath path = shape.getPath().getPathInRect(bounds);
        path = shape.convertPathToShape(path, parent);
        paths.add(path);
    }
    
    // Return paths list
    return paths;
}

/**
 * Returns the shared hover timer.
 */
public static HoverTimer getHoverTimer()  { return _hoverTimer; }

/**
 * Sets the hover timer.
 */
public static void setHoverTimer(HoverTimer aTimer)  { _hoverTimer = aTimer; }

/**
 * Returns the hover timer, creating it if missing.
 */
public static HoverTimer getHoverTimer(RMShapeMouseEvent anEvent)
{
    // If no timer, create and configure
    if(_hoverTimer==null) {
        _hoverTimer = new HoverTimer(anEvent);
        _hoverTimer.setRepeats(false);
        _hoverTimer.start();
    }
    
    // Otherwise restart with new event
    else {
        _hoverTimer.restart();
        _hoverTimer.setEvent(anEvent);
    }
    
    // Return timer
    return _hoverTimer;
}

/**
 * Returns the hover shape.
 */
public static RMShape getHoverShape()  { return _hoverShape; }

/**
 * A Timer subclass.
 */
public static class HoverTimer extends javax.swing.Timer {

    // The shape mouse event
    RMShapeMouseEvent  _event;

    /** Creates a new hover timer for shape mouse event. */
    public HoverTimer(RMShapeMouseEvent anEvent)
    {
        super(500,null);
        _event = anEvent;
    }
    
    /** Returns the shape mouse event. */
    public RMShapeMouseEvent getEvent()  { return _event; }
    
    /** Sets the event. */
    public void setEvent(RMShapeMouseEvent anEvent)  { _event = anEvent; }
    
    /** Return the shape. */
    public RMShape getShape()  { return _event.getShape(); }

    /** Called when timer fires. */
    protected void fireActionPerformed(ActionEvent e)
    {
        // If this timer is equal to shared hover timer, install hover shape
        if(_hoverTimer==this) {
            
            // Get shape and page shape
            RMShape shape = getShape();
            RMParentShape pageShape = shape.getPageShape();
            
            // Get hover shape from viewer and source shape
            _hoverShape = _event.getViewer().getHoverShape(shape.getHover());
            
            // If hover shape available, add to page
            if(_hoverShape!=null) {
                
                // Get point for hover shape bounds origin when corralled to page shape bounds
                RMPoint point = shape.convertPointToShape(_event.getPoint2D(), pageShape).offset(10,10);
                point = pageShape.getBoundsInside().getCorraledRect(
                        new RMRect(point.x, point.y, _hoverShape.getWidth(), _hoverShape.getHeight()),10).getOrigin();
                
                // Position shape and add to page
                _hoverShape.setXY(point);
                pageShape.addChild(_hoverShape);
            }
            
            // Clear hover timer
            _hoverTimer = null;
        }
    }
}

}