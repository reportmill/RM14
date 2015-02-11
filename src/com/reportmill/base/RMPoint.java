package com.reportmill.base;
import com.reportmill.graphics.*;
import java.awt.geom.*;

/**
 * This class extends Point2D.Float to add a few convenience methods.
 */
public class RMPoint extends Point2D.Double {
    
    // A shared zero point
    public static final RMPoint zeroPoint = new RMPoint();
    
/**
 * Creates a point initialized to 0,0.
 */
public RMPoint() { }

/**
 * Creates a point initialized to the given x and y.
 */
public RMPoint(double x1, double y1)  { x = x1; y = y1; }

/**
 * Creates a point initialized to the given AWT Point.
 */
public RMPoint(Point2D aPoint)  { x = aPoint.getX(); y = aPoint.getY(); }

/**
 * Offsets the receiver by the given x and y.
 */
public RMPoint offset(double dx, double dy)  { x += dx; y += dy; return this; }

/**
 * Adds the given point to this point.
 */
public RMPoint add(RMPoint aPoint)  { x += aPoint.x; y += aPoint.y; return this; }

/**
 * Subtracts the given point from this point.
 */
public RMPoint subtract(RMPoint aPoint)  { x -= aPoint.x; y -= aPoint.y; return this; }

/**
 * Multiplies this point by the given sx and sy.
 */
public void multiply(double sx, double sy)  { x = x*sx; y = y*sy; }

/**
 * Returns whether the receiver is in the given rect.
 */
public boolean inRect(RMRect aRect)  { return x>=aRect.x && x<=aRect.getMaxX() && y>=aRect.y && y<=aRect.getMaxY(); }

/**
 * Transforms the point by the given transform.
 */
public RMPoint transform(RMTransform aTransform)  { aTransform.transform(this); return this; }

/**
 * Standard equals implementation.
 */
public boolean equals(RMPoint aPoint)  { return RMMath.equals(aPoint.x, x) && RMMath.equals(aPoint.y, y); }

/**
 * Returns a string representation of the receiver in the form "[x y]".
 */
public String toString()  { return "[" + x + " " + y + "]"; }

/**
 * Returns an RMPoint version of the given Point2D (just the Point2D if it's already an RMPoint).
 */
public static RMPoint getPoint(Point2D aPoint)
{
    return aPoint instanceof RMPoint? (RMPoint)aPoint : new RMPoint(aPoint);
}

}