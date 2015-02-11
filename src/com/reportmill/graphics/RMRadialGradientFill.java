package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.geom.Point2D;
import snap.util.*;

/**
 * Radial Gradient Fill.
 */
public class RMRadialGradientFill extends RMGradientFill {

    // points in the unit square that define the gradient.
    public Point2D     _beginPoint;
    public Point2D     _endPoint;
    
    int                _hiddenGradType=0;
    
/**
 * Creates an uninitialized radial fill.
 */
public RMRadialGradientFill()
{ 
    this(RMColor.white, RMColor.black, new Point2D.Float(0.5f, 0.5f), new Point2D.Float(1.0f, 0.5f));
}

/**
 * Creates a gradient fill from the given start color to the given end color with the given endpoints.
 */
public RMRadialGradientFill(RMColor aColor1, RMColor aColor2, Point2D begin, Point2D end)
{
    super(aColor1, aColor2,0);
    _beginPoint = begin;
    _endPoint = end;
}

/**
 * Returns the begin point.
 */
public Point2D getBeginPoint()  { return _beginPoint; }

/**
 * Returns the end point.
 */
public Point2D getEndPoint()  { return _endPoint; }

/**
 * Returns the angle.
 */
public double getFocusAngle()
{
    return Math.atan((_endPoint.getY() - _beginPoint.getY())/(_endPoint.getX() - _beginPoint.getX()));
}

/**
 * Returns the radius.
 */
public double getRadius()
{
    double a = _endPoint.getX() - _beginPoint.getX(), b = _endPoint.getY() - _beginPoint.getY();
    return Math.sqrt(a*a+b*b);
}

/**
 * Returns a new gradient which is a copy of this gradient with a different gradient axis.
 */
public RMRadialGradientFill deriveGradient(Point2D begin, Point2D end)
{
    RMRadialGradientFill newFill = clone();
    newFill._beginPoint = (Point2D)begin.clone();
    newFill._endPoint = (Point2D)end.clone();
    return newFill;
}

/**
 * Returns a new gradient which is a copy of this gradient but of a different type.
 */
public RMGradientFill deriveGradient(boolean isRadial)
{
    // If already radio, just return
    if(isRadial) return this;

    // Create new linear fill, set stops and return
    RMGradientFill newFill = new RMGradientFill();
    newFill.setStops(getColorStops());
    return newFill;
}

/** 
 * Returns the 2 points, in the shape's coordinate system, which define the gradient.
 */
public void getGradientAxis(RMShape aShape, RMPath aPath, Point2D p1, Point2D p2)
{
    // transform begin & end points into shape's coordinate system
    RMRect bounds = aShape.getBoundsInside();
    p1.setLocation(bounds.getMinX() + _beginPoint.getX()*bounds.getWidth(), 
                   bounds.getMinY() + _beginPoint.getY()*bounds.getHeight());
    p2.setLocation(bounds.getMinX() + _endPoint.getX()*bounds.getWidth(), 
            bounds.getMinY() + _endPoint.getY()*bounds.getHeight());
}

/**
 * Returns a java.awt.Paint instance to draw this gradient.
 */
public Paint getPaint(float startx, float starty, float endx, float endy)
{
    switch(_hiddenGradType) {
        case 1: return new RMMultipleStopGradient.DiamondGradient(startx, starty, endx, endy, this);
        case 2: return new RMMultipleStopGradient.AngleGradient(startx, starty, endx, endy, this);
        default: return new RMMultipleStopGradient.RadialGradient(startx, starty, endx, endy, this);
    }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, get other
    if(anObj==this) return true;
    if(!(anObj instanceof RMRadialGradientFill)) return false;
    RMRadialGradientFill other = (RMRadialGradientFill)anObj;
    
    // Check properties
    if(!_colorStops.equals(other._colorStops)) return false;
    if(!_beginPoint.equals(other._beginPoint)) return false; 
    if(!_endPoint.equals(other._endPoint)) return false;
    return true;
}

/**
 * Standard clone implementation.
 */
public RMRadialGradientFill clone()
{
    RMRadialGradientFill clone = (RMRadialGradientFill)super.clone();
    clone._beginPoint = (Point2D)(_beginPoint.clone());
    clone._endPoint = (Point2D)(_endPoint.clone());
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic fill attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "radial");
    
    // Archive points
    e.add("x0", _beginPoint.getX());
    e.add("y0", _beginPoint.getY());
    e.add("x1", _endPoint.getX());
    e.add("y1", _endPoint.getY());
    
    // preserve the undocumented type flag, if present
    if(_hiddenGradType != 0) e.add("subtype", _hiddenGradType);
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic fill attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive points
    float x = anElement.getAttributeFloatValue("x0");
    float y = anElement.getAttributeFloatValue("y0");
    _beginPoint = new Point2D.Float(x,y);
    
    x = anElement.getAttributeFloatValue("x1");
    y = anElement.getAttributeFloatValue("y1");
    _endPoint = new Point2D.Float(x,y);
    
    // Unarchive undocumented stupid gradient type flag
    _hiddenGradType = anElement.getAttributeIntValue("subtype");
    
    // Return this gradient fill
    return this;
}

/**
 * Returns the name of the fill (Returns superclass name so the pop-up doesn't get confused).
 */
public String getName()  { return "Gradient"; }

}