package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.util.*;
import snap.util.*;

/**
 * A shape that can orient its children along a bezier path.
 * 
 * By being an RMPolygon subclass, this class inherits all the path editing behavior of the 
 * polygon.  Unlike the base polygon, however, this shape always has 1 or more children.
 * The children shapes will be positioned and oriented at _distance along the path.
 * 
 */
public class RMAnimPathShape extends RMPolygonShape {

    // The shape being animated
    RMShape     _animChild;
    
    // Location on the path, in the range [0-1]
    float       _distance;

    // Whether or not to orient children along the path
    boolean     _preserveOrientation;
  
    // Point of rotation for children (an edge 0-9)
    int         _childOrigin = 4;
  
    // A cached list of the path's segments as RMLine subclasses
    RMLine      _pathSegments[];
  
    // lengths of each segment
    double      _segmentLengths[];
  
    // The length of the whole path
    double      _totalLength;
    
    // whether to draw the partial path while animating
    boolean     _drawSubpath;
    

/**
 * Creates a new RMAnimPathShape.
 */
public RMAnimPathShape()  { setStroke(new RMStroke(RMColor.lightBlue, 1)); }

/**
 * Creates an animpath shape from another shape.
 */
public RMAnimPathShape(RMShape aShape)
{
    copyShape(aShape); // Copy shape attributes of given shape
    setPath(aShape.getPath()); // Copy the path
    setStroke(new RMStroke(RMColor.lightBlue, 1)); // Replace fill
}

/**
 * Returns the animated child shape.
 */
public RMShape getAnimChild()  { if(_animChild==null) _animChild = getChild(0); return _animChild; }

/**
 * Sets the shape which will be positioned along the path
 */
public void setAnimChild(RMShape aChild) 
{
    // If one is already there, remove it
    RMAnimPathGroup group = getChildWithClass(RMAnimPathGroup.class);
    if(group!=null)
        removeChild(group);
        
    // Make a new group for the child
    group = new RMAnimPathGroup(); group.setBounds(0, 0, aChild.getWidth(), aChild.getHeight());
    aChild.setXY(0, 0);
    group.addChild(aChild);
        
    // Add it to the path and update everything
    addChild(_animChild = group);
    pathChanged();
}

/**
 * Override add child to handle animated child shape special
 */
public void addChild(RMShape aChild, int anIndex)
{
    assert(aChild instanceof RMAnimPathGroup); super.addChild(aChild, anIndex);
}

/** 
 * Overridden to just reflect bounds of anim child.
 */
public RMRect getBoundsMarked()
{
    // If editing, do normal implementation, otherwise return bounds marked of anim child
    if(isEditing()) return super.getBoundsMarked();
    return getAnimChild().convertedRectToShape(getAnimChild().getBoundsMarked(), this);
}

/** 
 * Overridden to just reflect bounds of anim child.
 */
public RMRect getBoundsMarkedDeep()
{
    // If editing, do normal implementation, otherwise return bounds marked of anim child
    if(isEditing()) return super.getBoundsMarkedDeep();
    return getAnimChild().convertedRectToShape(getAnimChild().getBoundsMarkedDeep(), this);
}

/**
 * Overridden to just reflect bounds of anim child.
 */
public boolean contains(RMPoint aPoint)
{
    // If editing, do normal implementation, otherwise return bounds marked of anim child
    if(isEditing()) return super.contains(aPoint);
    return getAnimChild().contains(convertedPointToShape(aPoint, getAnimChild()));
}

/**
 * Standard clone implementation.
 */
public RMAnimPathShape clone()
{
    RMAnimPathShape clone = (RMAnimPathShape)super.clone(); // Get normal shape clone
    clone._animChild = null; // Clear anim child
    return clone; // Return clone
}

/**
 * Overridden to skip painting at preview time and to paint stroke (anim path) under child.
 */
public void paintShape(RMShapePainter aPntr)
{
    if(!aPntr.isEditing()) return;
    super.paintShape(aPntr);
    super.paintShapeOver(aPntr);
}

/**
 * Overridden to skip painting stroke on top.
 */
public void paintShapeOver(RMShapePainter aPntr)  { }

/**
 * Returns distance along the path where children are positioned.
 * @see #setDistance(float)
 */
public float getDistance()  { return _distance; }

/**
 * Sets distance along path to position children.  Distance is in the
 * range of [0-1] with 0 representing the start of the path and 1 the end.
 */
public void setDistance(float aValue) 
{
    // If value already set, just return
    if(aValue==_distance) return;
    
    // Cache old value
    float oldValue = _distance;
    
    // Set new value
    _distance = aValue;
    
    // Reposition children
    positionChildren();
    
    // Fire property change
    firePropertyChange("Distance", oldValue, _distance, -1);
}

/**
 * Returns the actual total arclength of the curve.  Used internally by the AnimPath
 * to convert a distance of 0 through 1 to an actual point on the path.
 */
public float getTotalLength()  { return (float)_totalLength; }

/**
 * Returns whether the children will have their rotation adjusted as they move along the path.
 * @see #setPreservesOrientation(boolean)
 */
public boolean getPreservesOrientation()  { return _preserveOrientation; }

/**
 * Sets whether the children's rotation should get adjusted as they move along the path.
 * If flag is true, the original rotation of the shape is 'preserved' relative to the 
 * tangent to the path at the current point.
 */
public void setPreservesOrientation(boolean aValue)
{
    if(aValue==_preserveOrientation) return; // If value already set, just return
    firePropertyChange("PreservesOrientation", _preserveOrientation, _preserveOrientation = aValue, -1);
    positionChildren(); // Reposition children
}

/**
 * Returns the child origin.
 */
public int getChildOrigin()  { return _childOrigin; }

/**
 * Sets child origin.
 */
public void setChildOrigin(int aValue)
{
    if(aValue==_childOrigin) return; // If value already set, just return
    _childOrigin = aValue; // Set new value
    positionChildren(); // Reposition children
}

/**
 * Called by the tool when the path has been changed.
 */
public void pathChanged()
{
    _pathSegments = null; _segmentLengths = null;
    positionChildren();
}

/** 
 * Overridden to mark the path dirty.
 */
public void setWidth(double aValue)  { super.setWidth(aValue); pathChanged(); }

/** 
 * Overridden to mark the path dirty.
 */
public void setHeight(double aValue)  { super.setHeight(aValue); pathChanged(); }

/**
 * Overridden to mark the path dirty
 */
public void setPath(RMPath aPath)  { super.setPath(aPath); pathChanged(); }

/**
 * Builds the segment list and caches it 
 */
public RMLine[] getSegments()  { if(_pathSegments==null) loadSegments(); return _pathSegments; }

/**
 * Builds the segment list and caches it 
 */
private void loadSegments()
{
    // Get path segments
    List <? extends RMLine> segments = getPathInBounds().getSegments();
    
    // Reset segment info            
    _pathSegments = new RMLine[segments.size()];
    _segmentLengths = new double[segments.size()];
    _totalLength = 0;
    
    // Iterate over segments and recalculate segment info
    for(int i=0, iMax=segments.size(); i<iMax; i++) {
        _pathSegments[i] = segments.get(i);
        _segmentLengths[i] = _pathSegments[i].getArcLength();
        _totalLength += _segmentLengths[i];
    }
}
            
/**
 * PositionChildren
 */
public void positionChildren()
{
    // don't do anything if the path hasn't been established yet or if there are no children
    if(getPath()==null || getChildCount()==0) return;
	
    // Get all the lines/quads/beziers
	RMLine segs[] = getSegments();
    
    // convert percentage of path into actual length
    double pathLocation = _distance*_totalLength;
    
    // locate the right path segment
    int i;
    for(i=0; i<segs.length && _segmentLengths[i]<pathLocation; ++i)
        pathLocation -= _segmentLengths[i];
    
    // If no segment found, complain
    if(i>=segs.length) {
        System.err.println("Can't locate position for "+(_distance*100)+"% of the distance along path"); return; }
    
    // scale pathLocation by length of found segment
    double len = pathLocation/_segmentLengths[i];
    
    // map the length to parameter
    double t = segs[i].getParameterForLength((float)len);
   
    // get the actual point along path and rotation angle
    RMPoint pt = segs[i].getPoint(t);
    double angle = segs[i].getAngle(t);
   
    RMAnimPathGroup child = (RMAnimPathGroup)getAnimChild();
    double dw = child.getWidth()*(_childOrigin%3)/2;
    double dh = child.getHeight()*(_childOrigin/3)/2;
    pt.x -= dw; pt.y -= dh;
    
    // Set new child position (with undoer disabled)
    child.undoerDisable();
    child.setXY(pt);
    child.undoerEnable();
    
    // Update roll
    if(_preserveOrientation) {
        child._por = new RMPoint(dw,dh); child._pRoll = angle; }
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("animpath");
    
    // Archive Distance, PreservesOrientation, ChildOrigin
    if(getDistance()>0) e.add("distance", getDistance());
    if(!getPreservesOrientation()) e.add("orients", false);
    if(getChildOrigin() != 4) e.add("child-origin", getChildOrigin());
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive Distance, PreservesOrientation, ChildOrigin
    setDistance(anElement.getAttributeFloatValue("distance"));
    setPreservesOrientation(anElement.getAttributeBoolValue("orients", true));
    setChildOrigin(anElement.getAttributeIntValue("child-origin", 4));
}

/**
 * A shape subclass that adds the ability to be rotated about an arbitrary point.
 * Might also want to make this store a _distance at some point, so that an arbitrary
 * number of shapes could be animated along the same path.
 */
public static class RMAnimPathGroup extends RMParentShape
{
    // the center of rotation
    RMPoint  _por;
    
    // the extra rotation added by the animpath 
    double   _pRoll;

    /** Returns a transform that rotates about the center of rotation, if set. */
    public RMTransform getTransform()
    {
        double porX = _por!=null? _por.getX() : getWidth()/2, porY = _por!=null? _por.getY() : getHeight()/2;
        return new RMTransform(getX(),getY(),_pRoll+getRoll(),porX,porY,getScaleX(),getScaleY(),getSkewX(),getSkewY());
    }
    
    /** Overridden so getTransform() will get called */
    public boolean isRSS()  { return super.isRSS() || _por!=null || _pRoll!=0; }
}

/**
 * A special shape subclass which can be used to draw a subsection of a polygon.
 * Can be used to trace out a path along the animpath.
 */
static class RMSubpathShape extends RMPolygonShape {

    // The original shape
    RMAnimPathShape _originalShape;
    
    // The subpath
    RMPath          _subpath;
    
    // The original segments
    List <RMLine>   _originalSegments;
    
    // The original path bounds
    RMRect          _originalPathBounds;
  
    /**
     * Creates a new subpath shape.
     */
    public RMSubpathShape(RMAnimPathShape original)
    {
        copyShape(original);
        _originalShape = original;
        _originalSegments = Arrays.asList(_originalShape.getSegments());
        _originalPathBounds = _originalShape.getPath().getBounds2D();
    }

    // always goes from 0 to endPt. If your animpath doesn't start at 0, make another one that does.
    public void setRange(int endSegment, float endPt)
    {
        if(_originalSegments==null || _originalSegments.size()==0) return;

        // copy untouched segments to new list
        List newList = new ArrayList(_originalSegments.subList(0, endSegment));
        
        // Get the end segment and trim it to the endPt
        RMLine endSeg = _originalSegments.get(endSegment);
        endSeg = endSeg.clone();
        endSeg.setEnd(endPt);
        newList.add(endSeg);
 
        // make a new path and add bogus points to force the bounds to be the same as the original path
        _subpath = new RMPath();
        _subpath.moveTo(_originalPathBounds.getOrigin());
        _subpath.moveTo(_originalPathBounds.getMaxX(), _originalPathBounds.getMaxY());
        
        // append the modified segments
        _subpath.addSegments(newList);
    }

    public RMPath getPath() { return _subpath; }
}

}