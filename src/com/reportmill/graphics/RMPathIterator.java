package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.geom.*;

/**
 * An implementation of path iterator for RMPath.
 */
public class RMPathIterator implements PathIterator {

    // The path that this iterator works for
    RMPath            _path;
    
    // The transform to apply to the path
    AffineTransform   _transform;
    
    // The current segment index
    int               _index = 0;
    
    // The current point index
    int               _pIndex = 0;

/**
 * Creates a new path iterator for given path.
 */
public RMPathIterator(RMPath path, AffineTransform aTransform)
{
    _path = path;
    _transform = aTransform;
}

/**
 * Returns the current segment's segment type and segment points (by reference).
 */
public int currentSegment(double coords[])
{
    // Get RMPath element type and points
    byte type = _path.getElement(_index);
    
    // Declare variable for point count
    int pointCount = 0;
    
    // Get point count for segment
    switch(type) {
        case RMPath.MOVE_TO: pointCount = 1; break;
        case RMPath.LINE_TO: pointCount = 1; break;
        case RMPath.QUAD_TO: pointCount = 2; break;
        case RMPath.CURVE_TO: pointCount = 3; break;
        default: break;
    }
    
    // Fill in coords
    for(int i=0; i<pointCount; i++) {
        RMPoint point = _path.getPoint(_pIndex++);
        coords[i*2] = point.x; coords[i*2+1] = point.y;
    }
    
    // Transform points
    if(_transform!=null)
        _transform.transform(coords, 0, coords, 0, pointCount);
    
    // Return PathIterator segment type
    return getPathIteratorSegmentType(type);
}

/**
 * Returns the current segment's segment type and segment points (by reference) as floats.
 */
public int currentSegment(float coords[])
{
    // Get RMPath element type and points
    byte type = _path.getElement(_index);
    
    // Declare variable for point count
    int pointCount = 0;
    
    // Get point count for segment
    switch(type) {
        case RMPath.MOVE_TO: pointCount = 1; break;
        case RMPath.LINE_TO: pointCount = 1; break;
        case RMPath.QUAD_TO: pointCount = 2; break;
        case RMPath.CURVE_TO: pointCount = 3; break;
        default: break;
    }
    
    // Fill in coords
    for(int i=0; i<pointCount; i++) {
        RMPoint point = _path.getPoint(_pIndex++);
        coords[i*2] = (float)point.x; coords[i*2+1] = (float)point.y;
    }
    
    // Transform points
    if(_transform!=null)
        _transform.transform(coords, 0, coords, 0, pointCount);
    
    // Return PathIterator segment type
    return getPathIteratorSegmentType(type);
}

/**
 * Returns the path winding rule.
 */
public int getWindingRule()
{
    return _path.getWindingRule() == RMPath.WIND_NON_ZERO? PathIterator.WIND_NON_ZERO : PathIterator.WIND_EVEN_ODD;
}

/**
 * Returns whether path iterator is done.
 */
public boolean isDone()
{
    int nElements = _path._elementCount;
    
    // skip over any moveTos that have no effect
    if (_index<nElements && (_path.getElement(_index)==RMPath.MOVE_TO)) {
        // movetos don't do anything if they're followed by another moveto
        while((_index+1<nElements) && (_path.getElement(_index+1)==RMPath.MOVE_TO)) {
            ++_index;
            ++_pIndex;
        }
        // nor does one that's at the end of the path
        // (although it does change the current point)
        if (_index==nElements-1)
            return true;
    }
    
    return _index >= nElements;
}

/**
 * Increments path iterator to the next segment.
 */
public void next()  { _index++; }

/**
 * Returns a PathIterator segment type for a given RMPath element type
 */
private int getPathIteratorSegmentType(byte type)
{
    // Switch over given RMPath element type
    switch(type) {
        case RMPath.MOVE_TO: return PathIterator.SEG_MOVETO;
        case RMPath.LINE_TO: return PathIterator.SEG_LINETO;
        case RMPath.QUAD_TO: return PathIterator.SEG_QUADTO;
        case RMPath.CURVE_TO: return PathIterator.SEG_CUBICTO;
        case RMPath.CLOSE: return PathIterator.SEG_CLOSE;
        default: return PathIterator.SEG_MOVETO;
    }
}

}