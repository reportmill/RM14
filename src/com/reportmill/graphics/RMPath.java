package com.reportmill.graphics;
import com.reportmill.base.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import snap.util.*;

/******************************************************************************
    RMPath - A graphics path (moveTo, lineTo, etc.). It provides the following functionality:
        - Allows for easy initialization with the path constructions methods (moveToPoint:, lineToPoint:, etc.).
        - Provides simple methods for path operations (stroke, fill, clip, etc.).
        
    Iterate over path like this:
    
        RMPoint points[] = new RMPoint[3];
        for(int i=0, iMax=path.getElementCount(); i<iMax; i++) { int element = path.getElement(i, points);
            switch(element) {
                case RMPath.MOVE_TO:
                case RMPath.LINE_TO:
                case RMPath.QUAD_TO:
                case RMPath.CURVE_TO:
                case RMPath.CLOSE:
            }
        }

******************************************************************************/
public class RMPath implements Shape, Cloneable, XMLArchiver.Archivable {
    
    // Array of operators for path
    byte        _elements[];
    
    // Actual number of operators (can be less than _elements array length)
    int         _elementCount;
    
    // Array of RMPoints for path
    Vector      _points;
    
    // Rule describing how inner path perimeters are displayed when filled
    byte        _windingRule;
    
    // The rect that just contains the path
    RMRect      _bounds;

    // Constants describing how inner path perimeters are filled and clipped
    public static final byte WIND_NON_ZERO = 0; // Inner perimeters drawn in same dir as outer pmtr filled
    public static final byte WIND_EVEN_ODD = 1; // Inner perimeters are alternately not covered
    
    // Constants describing path element types (MoveToPoint, LineToPoint, CurveToPoint, Close)
    public static final byte MOVE_TO = 1;
    public static final byte LINE_TO = 3;
    public static final byte QUAD_TO = 20;
    public static final byte CURVE_TO = 5;
    public static final byte CLOSE = 10;
        
    // Cached "constant" paths
    public static final RMPath unitRectPath = RMPathUtils.appendShape(new RMPath(), RMRect.unitRect);

/**
 * Creates an empty path.
 */
public RMPath()
{
    _points = new Vector(8);
    _elements = new byte[4];
    _elementCount = 0;
    _bounds = null;
    _windingRule = WIND_NON_ZERO;
}

/**
 * Creates a path for the given shape.
 */
public RMPath(Shape aShape)
{
    this();
    RMPathUtils.appendShape(this, aShape);
}

/**
 * Adds a MoveTo element to the path for the given point.
 */
public void moveTo(RMPoint p) { moveTo(p.x, p.y); }

/**
 * Adds a MoveTo element to the path for the given point.
 */
public void moveTo(double px, double py)
{
    _addElement(MOVE_TO);
    _addPoint(px, py);
}
  
/**
 * Adds a LineTo element to the path for the given point.
 */
public void lineTo(RMPoint p) { lineTo(p.x, p.y); }

/**
 * Adds a LineTo element to the path for the given point.
 */
public void lineTo(double px, double py)
{
    _addElement(LINE_TO);
    _addPoint(px, py);
}

/**
 * Adds a QuadTo element to the path for the given point and control point.
 */
public void quadTo(RMPoint cp, RMPoint p) { quadTo(cp.x, cp.y, p.x, p.y); }

/**
 * Adds a QuadTo element to the path for the given point and control point.
 */
public void quadTo(double cpx, double cpy, double px, double py)
{
    _addElement(QUAD_TO);
    _addPoint(cpx, cpy);
    _addPoint(px, py);
}

/**
 * Adds a CurveTo element to the path for the given point and control points.
 */
public void curveTo(RMPoint cp1, RMPoint cp2, RMPoint p) { curveTo(cp1.x, cp1.y, cp2.x, cp2.y, p.x, p.y); }

/**
 * Adds a CurveTo element to the path for the given point and control points.
 */
public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double px, double py)
{
    _addElement(CURVE_TO);
    _addPoint(cp1x, cp1y);
    _addPoint(cp2x, cp2y);
    _addPoint(px, py);
}

/**
 * Adds a Close element to the given path.
 */
public void closePath()  { _addElement(CLOSE); }

/**
 * Resets the current path with no elements or points.
 */
public void reset()
{
    _elementCount = 0;
    _points.clear();
    _bounds = null;
}

/**
 * Adds a MoveTo element to the path for the given point offset from the path's current point.
 */
public void relativeMoveTo(RMPoint aPoint)
{
    RMPoint current = getPointLast();
    _addElement(MOVE_TO);
    _addPoint(aPoint.x + current.x, aPoint.y + current.y);
}

/**
 * Adds a LineTo element to the path for the given point offset from the path's current point.
 */
public void relativeLineTo(RMPoint aPoint)
{
    RMPoint current = getPointLast();
    _addElement(LINE_TO);
    _addPoint(aPoint.x + current.x, aPoint.y + current.y);
}

/**
 * Adds a CurveTo element to the path for the given point offset from the path's current point.
 */
public void relativeCurveTo(RMPoint cp1, RMPoint cp2, RMPoint aPoint)
{
    _addElement(CURVE_TO);
    RMPoint lastPoint = getPointLast();
    _addPoint(cp1.x + lastPoint.x, cp1.y + lastPoint.y);
    _addPoint(cp2.x + lastPoint.x, cp2.y + lastPoint.y);
    _addPoint(aPoint.x + lastPoint.x, aPoint.y + lastPoint.y);
}

/**
 * Adds an element to the elements array.
 */
private void _addElement(byte anElement)
{
    int capacity = _elements.length;

    if(_elementCount == capacity) {
        byte newElements[] = new byte[capacity*2];
        System.arraycopy(_elements, 0, newElements, 0, _elementCount);
        _elements = newElements;
    }
    _elements[_elementCount++] = anElement;
}

/**
 * Adds a point to the points list.
 */
private void _addPoint(double px, double py)
{
    RMPoint point = new RMPoint(px, py);
    _points.add(point);
    _bounds = null;
}

/**
 * Returns the winding rule which describes how inner path perimeters are filled and clipped.
 */
public byte getWindingRule()  { return _windingRule; }

/**
 * Sets the winding rule which describes how inner path perimeters are filled and clipped.
 */
public void setWindingRule(byte windingRule)  { _windingRule = windingRule; }

/**
 * Returns the X of the path.
 */
public double getX()  { return getBounds2D().getX(); }

/**
 * Returns the Y of the path.
 */
public double getY()  { return getBounds2D().getY(); }

/**
 * Returns the width of the path.
 */
public double getWidth()  { return getBounds2D().getWidth(); }

/**
 * Returns the height of the path.
 */
public double getHeight()  { return getBounds2D().getHeight(); }

/**
 * Returns the bounds for the path as an integer rect.
 */
public Rectangle getBounds()  { return getBounds2D().getBounds(); }

/**
 * Returns the bounds for the path.
 */
public RMRect getBounds2D()
{
    // If already set, just return
    if(_bounds!=null) return _bounds;

    // Create bounds rect and declare loop variables for bounds min/max points and segment points
    RMRect bounds = new RMRect();
    double p1x = Double.MAX_VALUE, p1y = Double.MAX_VALUE;
    double p2x = -Double.MAX_VALUE, p2y = -Double.MAX_VALUE;
    RMPoint pts[] = new RMPoint[3]; double lx = p1x, ly = p1y;

    // Iterate over path elements    
    for(int i=0, iMax=getElementCount(); i<iMax; i++) { byte type = getElement(i, pts);

        // Evaluate bounds expansion of segment based on type
        switch(type) {

            // Handle MOVE_TO, LINE_TO: Simply do min/max compare for path point
            case MOVE_TO: case LINE_TO: lx = pts[0].x; ly = pts[0].y;
                p1x = Math.min(p1x, lx); p2x = Math.max(p2x, lx);
                p1y = Math.min(p1y, ly); p2y = Math.max(p2y, ly); break;

            // Handle QuadTo: Get bounds rect for segment and evaluate bounds expansion for curve end-point
            case QUAD_TO:
                RMQuadratic.getBounds(lx, ly, pts[0].x, pts[0].y, lx=pts[1].x, ly=pts[1].y, bounds);
                p1x = Math.min(p1x, bounds.x); p2x = Math.max(p2x, bounds.getMaxX());
                p1y = Math.min(p1y, bounds.y); p2y = Math.max(p2y, bounds.getMaxY()); break;
          
            // Handle CurveTo: Get bounds rect for segment and evaluate bounds expansion for curve end-point
            case CURVE_TO:
                RMBezier.getBounds(lx, ly, pts[0].x, pts[0].y, pts[1].x, pts[1].y, lx=pts[2].x, ly=pts[2].y, bounds);
                p1x = Math.min(p1x, bounds.x); p2x = Math.max(p2x, bounds.getMaxX());
                p1y = Math.min(p1y, bounds.y); p2y = Math.max(p2y, bounds.getMaxY()); break;

            // Handle CLOSE or other
            default: break;
        }
    }
    
    // If valid value, set rect
    if(p1x!=Double.MAX_VALUE)
        bounds.setRect(p1x, p1y, p2x - p1x, p2y - p1y);

    // Return bounds rect
    return _bounds = bounds;
}

/**
 * Sets the bounds that the path is relative to.
 */
public void setBounds(RMRect bounds)  { _bounds = bounds; }

/**
 * Returns the rectangle that encloses all the control points.
 */
public RMRect getControlPointBounds()
{
    // Get list for non-MoveTo points
    ArrayList <RMPoint> points = new ArrayList(getPointCount());
    
    // Iterate over elements
    for(int i=0, iMax=getElementCount(), pindex=0; i<iMax; i++) { byte e = getElement(i);
    
        // Skip trailing or consecutive MoveTos
        if(e==MOVE_TO) {
            if((i==iMax-1) || getElement(i+1)==MOVE_TO) {
                ++pindex;
                continue;
            }
        }
        
        // Add element points
        for(int j=0, jMax=pointCountForElementType(e); j<jMax; j++)
            points.add(getPoint(pindex++));
    }
    
    // return rect enclosing all the good points
    return new RMRect(points.toArray(new RMPoint[points.size()]));
}

/**
 * Returns the number of elements in this path.
 */
public int getElementCount()  { return _elementCount; }

/**
 * Returns the element type at the given index.
 */
public byte getElement(int anIndex)  { return _elements[anIndex]; }

/**
 * Returns the element type at the given index and its associated points (returned in the given point array).
 */
public byte getElement(int anIndex, RMPoint points[])
{
    // Get the element type at anIndex
    byte element = getElement(anIndex);
    
    // Get point index
    int pointIndex = getElementPointIndex(anIndex);

    // Switch on element type
    switch(element) {
        case MOVE_TO:
        case LINE_TO: points[0] = getPoint(pointIndex); break;
        case QUAD_TO: points[0] = getPoint(pointIndex); points[1] = getPoint(pointIndex+1); break;
        case CURVE_TO: points[0] = getPoint(pointIndex); points[1] = getPoint(pointIndex+1);
            points[2] = getPoint(pointIndex+2); break;
        default: break;
    }

    // Return element type
    return element;
}

/**
 * Returns the last element.
 */
public byte getElementLast()  { return getElementCount()>0? _elements[getElementCount()-1] : 0; }

/**
 * Returns the number of points in the path.
 */
public int getPointCount()  { return _points.size(); }

/**
 * Returns the point at the given index.
 */
public RMPoint getPoint(int anIndex)  { return (RMPoint)_points.get(anIndex); }

/**
 * Returns the last point in the path.
 */
public RMPoint getPointLast()  { return getPointCount()>0? getPoint(getPointCount()-1) : RMPoint.zeroPoint; }

/**
 * Returns the point index for a given element.
 */
public int getElementPointIndex(int anIndex)
{
    // Declare counter for point index
    int pointIndex = 0;

    // Iterate over segments and increment point index
    for(int i=0; i<anIndex; i++)
        switch(getElement(i)) {
            case MOVE_TO:
            case LINE_TO: pointIndex++; break;
            case QUAD_TO: pointIndex += 2; break;
            case CURVE_TO: pointIndex += 3; break;
            default: break;
        }
    
    // Return calculated point index
    return pointIndex;
}

/**
 * Returns the element index for the given point index.
 */
public int getElementIndexForPointIndex(int index)
{
    // Declare counter for element index
    int elementIndex = 0;

    // Iterate over segments and increment point index
    for(int pointIndex=0; pointIndex<=index; elementIndex++)
        switch(getElement(elementIndex)) {
            case MOVE_TO:
            case LINE_TO: pointIndex++; break;
            case QUAD_TO: pointIndex += 2; break;
            case CURVE_TO: pointIndex += 3; break;
            default: break;
        }
    
    // Return calculated element index
    return elementIndex - 1;
}

/**
 * Returns the total number of points associated with a given type of path element.
 */
public int pointCountForElementType(int element)
{ 
    switch(element) {
        case MOVE_TO: 
        case LINE_TO: return 1;
        case QUAD_TO: return 2;
        case CURVE_TO: return 3;
        default: return 0;
    }
}

/**
 * Returns true of the point at pointIndex is on the path,
 * and false if it is on the convex hull.
 */ 
public boolean pointOnPath(int pointIndex)
{
    int elIndex = getElementIndexForPointIndex(pointIndex);
    int indexInElement = pointIndex - getElementPointIndex(elIndex);
    
    // Only the last point is actually on the path
    int elType = getElement(elIndex);
    int numPts = pointCountForElementType(elType);
    return indexInElement == numPts-1;
}
    
/**
 * Returns whether path has any open subpaths.
 */
public boolean isClosed()
{
    // Declare variable for last move-to point
    RMPoint m0point = null;
    
    // Declare variable for last segement end point
    RMPoint m1point = null;
    
    // Declare points array for path segment iteration
    RMPoint points[] = new RMPoint[3];
    
    // Declare variable for whether in path
    boolean inPath = false;
    
    // Iterate over path segments
    for(int i=0, iMax=getElementCount(); i<iMax; i++) {
        
        // Get segement type
        int type = getElement(i, points);
        
        // Switch on type
        switch(type) {
        
            // Handle MoveTo
            case RMPath.MOVE_TO:
                
                // If we were in a path, and last move-to isn't equal
                if(inPath && !m1point.equals(m0point))
                    return false;
                
                // Set last move-to point, set not in path and break
                m0point = points[0]; inPath = false; break;
                
            // Handle LineTo
            case RMPath.LINE_TO: m1point = points[0]; inPath = true; break;
            case RMPath.QUAD_TO: m1point = points[1]; inPath = true; break;
            case RMPath.CURVE_TO: m1point = points[2]; inPath = true; break;
                
            // Handle Close
            case RMPath.CLOSE: inPath = false; break;
        }
    }
    
    // Return false if we're still in path
    return !inPath;
}

/**
 * Returns a copy of the path scaled to exactly fit in the given rect.
 */
public RMPath getPathInRect(Rectangle2D aRect)
{
    // Get bounds (just return path if equal to rect)
    Rectangle2D bounds = getBounds2D();
    if(bounds.equals(aRect))
        return this;
    
    // Get scale x from current bounds to new bounds
    double sx = aRect.getWidth()/bounds.getWidth();
    if(Double.isNaN(sx))
        sx = 0;
    
    // Get scale y from current bounds to new bounds
    double sy = aRect.getHeight()/bounds.getHeight();
    if(Double.isNaN(sy))
        sy = 0;
    
    // Get translation from current bounds to new bounds
    double tx = aRect.getX() - bounds.getX()*sx;
    double ty = aRect.getY() - bounds.getY()*sy;
    
    // Get transform from current bounds to new bounds
    AffineTransform transform = new AffineTransform(sx, 0, 0, sy, tx, ty);
    
    // Return transformed path
    return createTransformedPath(transform);
}

/**
 * Returns whether the given point is inside the path.
 */
public boolean contains(Point2D aPoint)  { return contains(aPoint.getX(), aPoint.getY()); }

/**
 * Returns whether the given xy coordinate is inside the path.
 */
public boolean contains(double x, double y)
{
    // If point not in path bounds, return false
    if(!getBounds2D().contains(x, y))
        return false;
    
    // If path not closed, return false
    if(!isClosed())
        return false;
    
    // We can start using this when we're compiling with Java 6
    // try { return Path2D.contains(getPathIterator(null), x, y); }
    // catch(Throwable t) { }

    // If internal API fails, create an area from path shape and let it do the work
    return new Area(this).contains(x, y);
}

/**
 * Returns whether the interior of the shape entirely contains the specified rectangular area.
 */
public boolean contains(Rectangle2D aRect)
{
    return contains(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight());
}

/**
 * Returns whether the interior of the shape entirely contains the specified rectangular area.
 */
public boolean contains(double x, double y, double w, double h)
{
    // We can start using this when we're compiling with Java 6
    return Path2D.contains(getPathIterator(null), x, y, w, h);

    // Do simple check of corner points
    //return contains(x, y) && contains(x+w, y) && contains(x+w, y+h) && contains(x, y+h);
}

/**
 * Returns whether the interior of the path intersects the interior of a specified Rectangle2D.
 */
public boolean intersects(Rectangle2D aRect)
{
    return intersects(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight());
}

/**
 * Returns whether the interior of the path intersects the interior of a specified rectangular area.
 */
public boolean intersects(double x, double y, double w, double h)
{
    // We can start using this when we're compiling with Java 6
    // try { return intersects(getPathIterator(null), x, y, w, h); }
    // catch(Throwable t) { }
    
    // Try to use internal API to return intersects
    //try {
    //    sun.awt.geom.Crossings c = sun.awt.geom.Crossings.findCrossings(getPathIterator(null), x, y, x+w, y+h);
    //    return c==null || !c.isEmpty();
    //} catch(Throwable t) { }

    // Use RM stuff
    return intersects(x, y, w, h, 0);
}

/**
 * Returns whether path is hit by point for a path linewidth.
 */
public boolean intersects(RMPoint aPoint, float lineWidth)
{
    // Get distance from point to path
    double distance = RMPathUtils.getDistanceSigned(this, aPoint);
    
    // If distance is within linewidth tolerance, point is on path
    return distance < lineWidth/2;
}

/**
 * Returns whether path is hit by a line.
 */
public boolean intersects(RMLine aLine)  { return getHitInfo(aLine, false)!=null; }

/**
 * Returns whether path drawn with given linewidth is hit by given rect.
 */
public boolean intersects(double x, double y, double w, double h, float lineWidth)
{
    // Create path for rect
    RMPath path = new RMPath();
    path.moveTo(x, y);
    path.lineTo(x+w, y);
    path.lineTo(x+w, y+h);
    path.lineTo(x, y+h);
    path.closePath();

    // Return intersects path
    return intersects(path, lineWidth);
}

/**
 * Returns whether path drawn with given linewidth is hit by given path.
 */
public boolean intersects(RMPath aPath, float lineWidth)
{
    // If path bounds contains rect, just return true (seems bogus!)
    if(aPath.getBounds2D().containsRect(getBounds2D()))
        return true;
    
    // If path bounds don't even intersect, return false
    //if(!aPath.getBounds().intersectsRect(getBounds().insetRect(-lineWidth/2)))
    //    return false;
    
    // Declare variables for path segment points, last path point and last path move-to point
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;

    // Iterate over path segments
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        
        // Get current segment type and points
        byte type = aPath.getElement(i, points);

        // Evalutate bounds expansion of segment based on type
        switch(type) {

            // Handle MOVE_TO
            case MOVE_TO:
                
                // Just update last point & last move-to point and break
                lastPoint = lastMoveToPoint = points[0];
                break;

            // Handle CLOSE
            case CLOSE:
                
                // If last point is last move-to point, just break
                if(lastPoint.equals(lastMoveToPoint))
                    break;
                
                // Otherwise, set current segment point to last move-to point and fall through to LINE_TO
                points[0] = lastMoveToPoint;

            // Handle LINE_TO
            case LINE_TO:
                
                // If last point is same as last move-to, just see if point hits path
                if(lastPoint.equals(lastMoveToPoint))
                    if(intersects(lastPoint, lineWidth))
                        return true;
                
                // If current segment point hits path, return true
                if(intersects(points[0], lineWidth))
                    return true;
                
                // Create line for current path segment
                RMLine line = new RMLine(lastPoint, points[0]);
                
                // If path is hit by line, return true
                if(intersects(line))
                    return true;
                
                // Update last point and break
                lastPoint = points[0];
                break;

            // Complain if anyone is using this for path with curves
            default: System.err.println("Hit by Path: Element type not implemented yet"); break;
        }
    }

    // Return false if no path segments hit given path
    return false;
}

/**
 * Returns the hit info for the given line against this path.
 */
public RMHitInfo getHitInfo(RMLine aLine, boolean findFirstHit)
{
    // Declare variable for potential hit info
    RMHitInfo hitInfo = null;

    // Declare variables for path segment points, last path point and last path move-to point
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;

    // Iterate over path segments
    for(int i=0, iMax=getElementCount(); i<iMax; i++) {
        
        // Get current segment type and points
        byte type = getElement(i, points);

        // Handle specific segment type
        switch(type) {

            // Handle MOVE_TO
            case MOVE_TO:
                
                // Update last point & last move-to point and break
                lastPoint = lastMoveToPoint = points[0];
                break;

            // Handle CLOSE
            case CLOSE:
                
                // If last point is same as last move-to point, just break
                if(lastPoint.equals(lastMoveToPoint))
                   break;
                
                // Otherwise, update current segment point and fall through to LINE_TO
                points[0] = lastMoveToPoint;

            // Handle LINE_TO
            case LINE_TO: {
                
                // Get RMLine for last point and current point and do RMLine hit detection
                RMLine line = new RMLine(lastPoint, points[0]);
                RMHitInfo newHitInfo = aLine.getHitInfo(line);

                // If hit, see if we need to findFirstHit or just return hitInfo
                if(newHitInfo!=null) {

                    // If findFirstHit, see if newHitInfo hit is closer in than current hitInfo
                    if(findFirstHit) {
                        if(hitInfo==null || newHitInfo._r<hitInfo._r) {
                            hitInfo = newHitInfo;
                            hitInfo._index = i;
                        }
                    }

                    // If not findFirstHit, just return newHitInfo
                    else return newHitInfo;
                }

                // Cache last point and break
                lastPoint = points[0];
                break;
            }
            
            // If quad segment, calculate control points for equivalent cubic and fall through
            case QUAD_TO: 
                points[2] = points[1]; 
                points[1] = new RMPoint((2*points[0].x+points[1].x)/3,
                                        (2*points[0].y+points[1].y)/3);
                points[0] = new RMPoint((2*points[0].x+lastPoint.x)/3,
                                          (2*points[0].y+lastPoint.y)/3);
                // fall through

              // If curve segment, get simple RMBezier and do line/bezier hit detection
            case CURVE_TO: {
                
                // Get simple RMBezier for current segment and do line-bezier hit detection
                RMBezier bezier = new RMBezier(lastPoint, points[0], points[1], points[2]);
                RMHitInfo newHitInfo = aLine.getHitInfo(bezier);

                // If hit, see if we need to findFirstHit or just return hitInfo
                if(newHitInfo!=null) {

                    // If findFirstHit, see if newHitInfo hit is closer in than current hitInfo
                    if(findFirstHit) {
                        if(hitInfo==null || newHitInfo._r<hitInfo._r) {
                            hitInfo = newHitInfo;
                            hitInfo._index = i;
                        }
                    }

                    // If not findFirstHit, just return newHitInfo
                    else return newHitInfo;
                }

                // Cache last point and break
                lastPoint = points[2];
                break;
            }
        }
    }

    // Return hit info
    return hitInfo;
}

/**
 * Converts a path into a list of RMLine/RMQuadratic/RMBezier.
 */
public List <? extends RMLine> getSegments() 
{
    // Create list for segments
    List segments = new ArrayList();
    
    // Declare loop variables
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;
    
    // Iterate over elements
    for(int i=0, iMax=getElementCount(); i<iMax; i++) { byte type = getElement(i, points);

        // Handle specific segment type
        switch(type) {
        
            // Handle MoveTo
            case MOVE_TO: lastPoint = lastMoveToPoint = points[0]; break;

            // Handle Close: set points to last MoveTo and fall through to LineTo
            case CLOSE: points[0] = lastMoveToPoint;

            // Handle LineTo
            case LINE_TO:
                if(!lastPoint.equals(points[0]))
                    segments.add(new RMLine(lastPoint, lastPoint = points[0]));
                break;
                
            // Handle QuadTo
            case QUAD_TO: segments.add(new RMQuadratic(lastPoint, points[0], lastPoint = points[1])); break;
            
            // Handle CurveTo
            case CURVE_TO: segments.add(new RMBezier(lastPoint, points[0], points[1], lastPoint = points[2])); break;
        }
    }
    
    // Return paths
    return segments;
}

/**
 * Converts a path into a list subpath lists of RMLine/RMQuadratic/RMBezier.
 */
public List <List <? extends RMLine>> getSubpathsSegments() 
{
    // Create list for subpaths and segments
    List subpaths = new ArrayList();
    List segments = new ArrayList();
    
    // Get loop variables
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;
    
    // Iterate over elements
    for(int i=0, iMax=getElementCount(); i<iMax; i++) { byte type = getElement(i, points);

        // Handle specific segment type
        switch(type) {
        
            // Handle MoveTo
            case MOVE_TO: lastPoint = lastMoveToPoint = points[0];
                if(!segments.isEmpty()) {
                    subpaths.add(segments);
                    segments = new ArrayList();
                }
                break;

            // Handle Close: set points to last MoveTo and fall through to LineTo
            case CLOSE: points[0] = lastMoveToPoint;

            // Handle LineTo
            case LINE_TO:
                if(!lastPoint.equals(points[0]))
                    segments.add(new RMLine(lastPoint, lastPoint = points[0]));
                break;
                
            // Handle QuadTo
            case QUAD_TO: segments.add(new RMQuadratic(lastPoint, points[0], lastPoint = points[1])); break;
            
            // Handle CurveTo
            case CURVE_TO: segments.add(new RMBezier(lastPoint, points[0], points[1], lastPoint = points[2])); break;
        }
    }
    
    // Add the last subpath
    if(!segments.isEmpty())
        subpaths.add(segments);
    
    // Return the subpaths
    return subpaths;
}

/**
 * Adds the list of segments to the path, starting with a moveto.
 */
public void addSegments(List <? extends RMLine> theSegments)
{
    // Just return if empty
    if(theSegments.size()==0) return;
    
    // Get first segment start point and do MoveTo
    RMPoint startPoint = theSegments.get(0)._sp;
    moveTo(startPoint);
        
    // Iterate over segments
    for(int i=0, iMax=theSegments.size(); i<iMax; i++) { RMLine segment = theSegments.get(i);
        if(segment.getClass()==RMLine.class && segment._ep.equals(startPoint))
            closePath();
        else addSegment(segment);
    }
}

/**
 * Adds the list of segments to the path, starting with a moveto.
 */
public void addSegment(RMLine aSegment)
{
    // Handle Bezier
    if(aSegment instanceof RMBezier) { RMBezier b = (RMBezier)aSegment;
        curveTo(b._cp1, b._cp2, b._ep); }
   
    // Handle Quadratic
    else if(aSegment instanceof RMQuadratic) { RMQuadratic q = (RMQuadratic)aSegment;
        quadTo(q._cp1, q._ep); }
   
    // Handle basic Line
    else lineTo(aSegment._ep);
}

/**
 * Returns the total arc length of path segments.
 */
public double arcLength()
{
    // Declare variable for cumulative length
    double lenth = 0;
    
    // Get segments and iterate over to calculate total arc length
    List <? extends RMLine> segments = getSegments();
    for(int i=0, iMax=segments.size(); i<iMax; i++)
        lenth += segments.get(i).getArcLength();
    
    // Return length
    return lenth;
}
            
/**
 * Returns the hit info for the given bezier curve against this path.
 */
public RMHitInfo getHitInfo(RMBezier aBezier, boolean findFirstHit)
{
    // Declare variable for potential hit info
    RMHitInfo hitInfo = null;
    
    // Declare variables for path segment points, last path point and last path move-to point
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;

    // Iterate over path segments
    for(int i=0, iMax=getElementCount(); i<iMax; i++) {

        // Get current segment type and points
        byte type = getElement(i, points);

        // Handle specific segment type
        switch(type) {

            // Handle MOVE_TO
            case MOVE_TO:
                
                // Just update last point & last move-to point and break
                lastPoint = lastMoveToPoint = points[0];
                break;

            // Handle CLOSE
            case CLOSE:
                
                // If last point is last move-to point, just break
                if(lastPoint.equals(lastMoveToPoint))
                   break;
                
                // Otherwise set current segment point to last move-to point and fall through to LINE_TO
                points[0] = lastMoveToPoint;

            // Handle LINE_TO
            case LINE_TO: {
                
                // Create line for current path segment
                RMLine line = new RMLine(lastPoint, points[0]);
                
                // Get hit info for given beizer and current path segment
                RMHitInfo newHitInfo = aBezier.getHitInfo(line);

                // If hit, see if we need to findFirstHit or just return hitInfo
                if(newHitInfo!=null) {

                    // If findFirstHit, see if newHitInfo hit is closer in than current hitInfo
                    if(findFirstHit) {
                        if(hitInfo==null || newHitInfo._r<hitInfo._r) {
                            hitInfo = newHitInfo;
                            hitInfo._index = i;
                        }
                    }

                    // If not findFirstHit, just return newHitInfo
                    else return newHitInfo;
                }

                // Update last point and break
                lastPoint = points[0];
                break;
            }

            // Handle QUAD_TO
            case QUAD_TO:
                
                // Convert quad-to to curve-to and fall through to CURVE_TO
                points[2] = points[1];
                points[1] = points[0];

            // CURVE_TO
            case CURVE_TO: {
                
                // Create bezier for current path segment
                RMBezier bezier = new RMBezier(lastPoint, points[0], points[1], points[2]);
                
                // Get hit info for given bezier and current path segment
                RMHitInfo newHitInfo = aBezier.getHitInfo(bezier);

                // If hit, see if we need to findFirstHit or just return hitInfo
                if(newHitInfo!=null) {

                    // If findFirstHit, see if newHitInfo hit is closer in than current hitInfo
                    if(findFirstHit) {
                        if(hitInfo==null || newHitInfo._r<hitInfo._r) {
                            hitInfo = newHitInfo;
                            hitInfo._index = i;
                        }
                    }

                    // If not findFirstHit, just return newHitInfo
                    else return newHitInfo;
                }

                // Update last point and break
                lastPoint = points[2];
                break;
            }
        }
    }

    // Return hit info
    return hitInfo;
}

/**
 * Returns the handle index for a given point against this path scaled to the given rect.
 * Only returns points that are on the path, except for the control points of
 * selectedPoint (if not -1)
 */
public int handleAtPointForBounds(RMPoint aPoint, RMRect aRect, int selectedPoint, RMSize handleSize)
{
    // convert point from shape coords to path coords
    RMPoint point = pointInPathCoordsFromPoint(aPoint, aRect);

    // Check against off-path control points of selected path first,
    // otherwise you might never be able to select one
    if (selectedPoint != -1) {
        int offPathPoints[]=new int[2];
        int noffPathPoints=0;
        int totalElements = getElementCount();
        int elementIndex = getElementIndexForPointIndex(selectedPoint);
        int element = getElement(elementIndex);
        
        // If the selected point is one of the on path points, figure out the indices of the others
        if (pointOnPath(selectedPoint)) {
            
            // If the selected element is a curveto or quadto, the second to the last
            // control point will be active
            if ((element == CURVE_TO) || (element == QUAD_TO))
                offPathPoints[noffPathPoints++] = selectedPoint-1;

            // If the element following the selected element is a curveto, it's
            // first control point will be active
            if (elementIndex<totalElements-1 && getElement(elementIndex+1)==CURVE_TO)
                offPathPoints[noffPathPoints++] = selectedPoint+1;
        }
        else {
            // If the selected point is an off-path point, add it to the list to
            // check and then figure out what other point might be active
            offPathPoints[noffPathPoints++] = selectedPoint;
            
            // if the selected point is the first control point, check the previous
            // segment, otherwise check the next segment
            if (selectedPoint == getElementPointIndex(elementIndex)) {
                if ((elementIndex>0) && (getElement(elementIndex-1)==CURVE_TO))
                    offPathPoints[noffPathPoints++] = selectedPoint-2;
            }
            else {
                if ((elementIndex<totalElements-1) && (getElement(elementIndex+1)==CURVE_TO)) 
                    offPathPoints[noffPathPoints++] = selectedPoint+2;
            }
        }
        
        // hit test any selected off-path handles
        for(int i=0; i<noffPathPoints; ++i)
            if (hitHandle(point, offPathPoints[i], handleSize))
                return offPathPoints[i];
    }
    
    // Check the rest of the points, but only ones that are actually on the path
    for(int i=0, iMax=getPointCount(); i<iMax; i++) {
            if (hitHandle(point, i, handleSize) && pointOnPath(i))
                return i;
        }

    // nothing hit
    return -1;
}
        
/**
 * Hit test the point (in path coords) against a given path point.
 */
public boolean hitHandle(RMPoint aPoint, int ptIndex, RMSize handleSize) {
    RMPoint p = (RMPoint)_points.get(ptIndex);
    RMRect br = new RMRect(p.x-handleSize.width/2, p.y-handleSize.height/2, handleSize.width, handleSize.height);
    return aPoint.inRect(br);
}

/**
 * Returns the given point converted to path coords for given path bounds.
 */
public RMPoint pointInPathCoordsFromPoint(RMPoint aPoint, RMRect aRect)
{
    Rectangle2D bounds = getBounds2D();
    double sx = bounds.getWidth()/aRect.width;
    double sy = bounds.getHeight()/aRect.height;
    double x = (aPoint.x-aRect.getCenterX())*sx + bounds.getCenterX();
    double y = (aPoint.y-aRect.getCenterY())*sy + bounds.getCenterY();
    return new RMPoint(x, y);
}

/**
 * Removes the last element from the path.
 */
public void removeLastElement()
{
    // Handle specific element type
    switch(getElement(_elementCount-1)) {
        case CURVE_TO: RMListUtils.removeLast(_points);
        case QUAD_TO: RMListUtils.removeLast(_points);
        case MOVE_TO:
        case LINE_TO: RMListUtils.removeLast(_points); break;
        default: break;
    }
    
    // Decrement the element count
    _elementCount--;
    
    // invalidate the bounds rect
    _bounds = null;
}

/**
 * Removes an element, reconnecting the elements on either side of the deleted element.
 */
public void removeElement(int elementIndex) 
{
    // range check
    if ((elementIndex<0) || (elementIndex>=_elementCount))
        throw new IndexOutOfBoundsException("element index "+elementIndex+" out of bounds");
    
    // If this is the last element, nuke it
    if (elementIndex == _elementCount-1) {
        removeLastElement();
        // but don't leave a stray moveto sitting around
        if ((_elementCount>0) && (getElement(_elementCount-1)==MOVE_TO))
            removeLastElement();
        return;
    }
        
    // get the index to the first point for this element
    int pointIndex = getElementPointIndex(elementIndex);
    // the type of element (MOVETO,LINETO,etc)
    int elementType = getElement(elementIndex);
    // and how many points are associated with this element
    int nPts = pointCountForElementType(elementType);
    
    // how many points to delete from the points array
    int nDeletedPts=nPts;
    // how many elements to delete (usually 1)
    int nDeletedElements = 1;
    // the index of the element to delete from the element array
    // (usually the same as the original index)
    int deletedElementIndex = elementIndex;
    
    if (elementType == MOVE_TO) {
        // delete all poins but the last of the next segment
        nDeletedPts = pointCountForElementType(getElement(elementIndex+1));
        // delete the next element and preserve the MOVETO
        ++deletedElementIndex;
        }
    else {
        // If next element is a curveTo, we are merging 2 curves into one, so
        // delete points such that slopes at endpoints of new curve match the
        // starting and ending slopes of the originals.
        if (getElement(elementIndex+1)==CURVE_TO) {
            ++pointIndex;
        }
        
        // Deleting the only curve or a line in a subpath can leave a stray moveto.
        // If that happens, delete it, too
        else if ((getElement(elementIndex-1) == MOVE_TO) && (getElement(elementIndex+1)==MOVE_TO)){
          ++nDeletedElements;
          --deletedElementIndex;
          ++nDeletedPts;
          --pointIndex;
        }
    }
    
    // Remove the element
    System.arraycopy(_elements, deletedElementIndex+nDeletedElements, _elements, deletedElementIndex, _elementCount-deletedElementIndex-nDeletedElements);
    _elementCount -= nDeletedElements;
    
    // Remove the points
    RMListUtils.remove(_points, pointIndex, pointIndex+nDeletedPts);
    
    // invalidate bounds
    _bounds = null;
}
    
/**
 * Sets the path point at the given index to the given point.
 */
public void setPoint(int index, RMPoint point)
{
    _points.setElementAt(point, index);
    _bounds = null;
}

/**
 * Resets the point at the given index to the given point, while preserving something.
 */
public void setPointStructured(int index, RMPoint point)
{
    int elementIndex = getElementIndexForPointIndex(index);
    byte element = getElement(elementIndex);

    // If point at index is part of a curveto, perform structured set
    if(element == RMPath.CURVE_TO) {
        int pointIndexForElementIndex = getElementPointIndex(elementIndex);

        // If point index is control point 1, and previous element is a curveto, bring control point 2 of previous curveto in line
        if(index - pointIndexForElementIndex == 0) {
            if((elementIndex-1 > 0) && (getElement(elementIndex-1) == RMPath.CURVE_TO)) {
                RMPoint endPoint = getPoint(index-1), otherControlPoint = getPoint(index-2);
                // endpoint==point winds up putting a NaN in the path 
                if (!endPoint.equals(point)) {
                    RMSize size = new RMSize(point.x - endPoint.x, point.y - endPoint.y);
                    size.normalize(); size.negate();
                    RMSize size2 = new RMSize(otherControlPoint.x - endPoint.x, otherControlPoint.y - endPoint.y);
                    double mag = size2.magnitude();
                    setPoint(index-2, new RMPoint(endPoint.x+size.width*mag, endPoint.y + size.height*mag));
                }
                else {
                    // Illustrator pops the otherControlPoint here to what it was at the 
                    // start of the drag loop.  Not sure that's much better...
                }
            }
        }

        // If point index is control point 2, and next element is a curveto, bring control point 1 of next curveto in line
        else if(index - pointIndexForElementIndex == 1) {
            if((elementIndex+1 < _elementCount) && (getElement(elementIndex+1) == RMPath.CURVE_TO)) {
                RMPoint endPoint = getPoint(index+1), otherControlPoint = getPoint(index+2);
                // don't normalize a point
                if (!endPoint.equals(point)) {
                    RMSize size = new RMSize(point.x - endPoint.x, point.y - endPoint.y);
                    size.normalize(); size.negate();
                    RMSize size2 = new RMSize(otherControlPoint.x - endPoint.x, otherControlPoint.y - endPoint.y);
                    double mag = size2.magnitude();
                    setPoint(index+2, new RMPoint(endPoint.x+size.width*mag, endPoint.y + size.height*mag));
                }
                else { }
            }
        }

        // If point index is curve end point, move the second control point by the same amount as main point move
        else if(index - pointIndexForElementIndex == 2) {
            setPoint(index-1, new RMPoint(getPoint(index-1)).add(new RMPoint(point).subtract(getPoint(index))));
            if((elementIndex+1 < _elementCount) && (getElement(elementIndex+1) == RMPath.CURVE_TO))
                setPoint(index+1, new RMPoint(getPoint(index+1)).add(new RMPoint(point).subtract(getPoint(index))));
        }
    }

    // If there is a next element and it is a curveto, move its first control point by the same amount as main point move
    else if((elementIndex+1 < _elementCount) && (getElement(elementIndex+1) == RMPath.CURVE_TO))
        setPoint(index+1, new RMPoint(getPoint(index+1)).add(new RMPoint(point).subtract(getPoint(index))));

    // Set point at index to requested point
    setPoint(index, point);
}


/**
 * Returns a path iterator for this path and the given transform.
 */
public PathIterator getPathIterator(AffineTransform aTransform)  { return new RMPathIterator(this, aTransform); }

/**
 * Returns a path iterator for this path and the given transform and flatness.
 */
public PathIterator getPathIterator(AffineTransform aTransform, double flatness)
{
    return new FlatteningPathIterator(getPathIterator(aTransform), flatness);
}

/**
 * Transforms the points in the path by the given transform.
 */
public void transformBy(RMTransform aTransform)
{
    for(int i=0, iMax=_points.size(); i<iMax; i++)
        aTransform.transform(getPoint(i));
    _bounds = null;
}

/**
 * Returns a transformed version of this path.
 */
public RMPath createTransformedPath(AffineTransform aTransform)
{
    // Create path copy
    RMPath path = clone();
    
    // Iterate over path points and transform
    for(int i=0, iMax=getPointCount(); i<iMax; i++) {
        RMPoint point = path.getPoint(i);
        aTransform.transform(point, point);
    }
    
    // Clear path bounds
    path._bounds = null;
    
    // Return path
    return path;
}

/** 
 * Returns a new path with the same points as this path, but in reverse order.
 */
public RMPath getReversedPath()
{
    RMPath newPath = new RMPath();
    boolean newSubpath = true;
    int numElements = getElementCount();
    int srcPointIndex = getPointCount()-1;

    // Iterate over elements (backwards)
    while(--numElements>=0) {
        
        // Get the current (backwards) element
        byte element = getElement(numElements);
        
        // Declare a points counter
        int pointsToCopy = 0;
        
        // If starting a new subpath...
        if(newSubpath) {
            
            // A moveto at the end of a subpath is a stray. Just skip over it
            if (element == MOVE_TO) {
                --srcPointIndex;
                continue;
            }
            
            // always start out with a MOVE_TO
            newPath._addElement(MOVE_TO);
            pointsToCopy = 1;
            newSubpath = false;

            // If the last element of this subpath is a closepath, search backwards for moveTo that started the subpath
            if (getElement(numElements)==CLOSE) {
                int subElements = numElements;
                while(--subElements>=0) {
                    if (getElement(subElements)==MOVE_TO) {
                        RMPoint pathStart = getPoint(getElementPointIndex(subElements));
                        
                        // If origin of subpath is not the same as the current point, add a LINETO to simulate what
                        // the path does.  Otherwise, toss it.
                        if (!pathStart.equals(getPoint(srcPointIndex))) {
                            newPath._addPoint(pathStart.x, pathStart.y);
                            newPath._addElement(LINE_TO);
                        }
                        break;
                    }
                }
                
                // no originating moveto - either a screwed up path or something wrong in our logic
                if (subElements<0)
                    throw new RuntimeException("Invalid path - no starting MOVETO element");
                
                // Skip the closepath (no need for bounds check, since lone closepath will throw above exception)
                element = getElement(--numElements);
            }
        }
      
        // I think moveto immediately followed by closepath will mess up here, 
        // so add a check [although that path probably screws up everywhere]
        if (element == MOVE_TO)
            newSubpath = true; 
            
        // Copy over the element
        else {
            newPath._addElement(element);
            pointsToCopy += pointCountForElementType(element);
        }
      
        // Now copy over the points (in reverse order)
        while(--pointsToCopy>=0) {
            RMPoint p = getPoint(srcPointIndex);
            newPath._addPoint(p.x, p.y);
            --srcPointIndex;
        }
    }

    // Return reversed path
    return newPath;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity & class and get other path
    if(anObj==this) return true;
    if(!(anObj instanceof RMPath)) return false;
    RMPath path = (RMPath)anObj;
    
    // Check ElementCount, WindingRule, Elements and Points
    if(path._elementCount!=_elementCount) return false;
    if(path._windingRule!=_windingRule) return false;
    if(!RMArrayUtils.equals(path._elements, _elements)) return false;
    if(!RMUtils.equals(path._points, _points)) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public RMPath clone()
{
    // Do normal Object clone
    RMPath clone = null;
    try { clone = (RMPath)super.clone(); }
    catch(Exception e) { System.err.println(e); return null; }

    // Copy elements list
    clone._elements = _elements.clone();
    
    // Copy points list
    Vector points = new Vector(getPointCount());
    for(int i=0, iMax=getPointCount(); i<iMax; i++) points.add(new RMPoint(getPoint(i)));
    clone._points = points;
    
    // Return path clone
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named path
    XMLElement e = new XMLElement("path");
    
    // Archive winding rule
    if(_windingRule!=WIND_NON_ZERO)
        e.add("wind", "even-odd");

    // Archive individual elements/points
    RMPoint points[] = new RMPoint[3];
    for(int i=0, iMax=getElementCount(); i<iMax; i++) {
        int type = getElement(i, points);
        switch(type) {
        
            // Handle MoveTo
            case RMPath.MOVE_TO:
                XMLElement move = new XMLElement("mv");
                move.add("x", points[0].x);
                move.add("y", points[0].y);
                e.add(move);
                break;
            
            // Handle LineTo
            case RMPath.LINE_TO:
                XMLElement line = new XMLElement("ln");
                line.add("x", points[0].x);
                line.add("y", points[0].y);
                e.add(line);
                break;
                
            // Handle QuadTo
            case RMPath.QUAD_TO:
                XMLElement quad = new XMLElement("qd");
                quad.add("cx", points[0].x);
                quad.add("cy", points[0].y);
                quad.add("x", points[1].x);
                quad.add("y", points[1].y);
                e.add(quad);
                break;

            // Handle CurveTo
            case RMPath.CURVE_TO:
                XMLElement curve = new XMLElement("cv");
                curve.add("cp1x", points[0].x);
                curve.add("cp1y", points[0].y);
                curve.add("cp2x", points[1].x);
                curve.add("cp2y", points[1].y);
                curve.add("x", points[2].x);
                curve.add("y", points[2].y);
                e.add(curve);
                break;

            // Handle close
            case RMPath.CLOSE:
                XMLElement close = new XMLElement("cl");
                e.add(close);
                break;
        }
    }
    
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive winding rule
    if(anElement.getAttributeValue("wind", "non-zero").equals("even-odd"))
        setWindingRule(WIND_EVEN_ODD);

    // Unarchive individual elements/points
    for(int i=0, iMax=anElement.size(); i<iMax; i++) {
        XMLElement e = anElement.get(i);
        
        if(e.getName().equals("mv"))
            moveTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("ln"))
            lineTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("qd"))
            quadTo(e.getAttributeFloatValue("cx"), e.getAttributeFloatValue("cy"),
                e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("cv"))
            curveTo(e.getAttributeFloatValue("cp1x"), e.getAttributeFloatValue("cp1y"),
                e.getAttributeFloatValue("cp2x"), e.getAttributeFloatValue("cp2y"),
                e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
        else if(e.getName().equals("cl"))
            closePath();
    }
    
    // Return this path
    return this;
}

}