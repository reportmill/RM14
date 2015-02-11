package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.geom.*;
import snap.util.SnapMath;

/**
 * This class represents a simple 2D transform.
 */
public class RMTransform implements Cloneable {
    
    // Matrix components
    public double _a = 1;
    public double _b = 0;
    public double _c = 0;
    public double _d = 1;
    public double _tx = 0;
    public double _ty = 0;
    
    // Cached AWT version of transform for use with Java2D
    private AffineTransform _awt = null;
    
    // Identity transform
    public static final RMTransform identity = new RMTransform();

/** Creates a new identity transform. */
public RMTransform() { }

/** Creates a new transform from a given transform. */
public RMTransform(RMTransform aTransform)
{
    _a = aTransform._a; _b = aTransform._b;
    _c = aTransform._c; _d = aTransform._d;
    _tx = aTransform._tx; _ty = aTransform._ty;
}

/** Creates a transform initialized to given matrix components. */
public RMTransform(double a, double b, double c, double d, double tx, double ty)
{
    _a = a; _b = b; _c = c; _d = d; _tx = tx; _ty = ty;
}

/** Create a transform with translation, rotation, and skew. */
public RMTransform(double dx, double dy, double angle, double sx, double sy, double skx, double sky)
{
    this(dx, dy, angle, 0, 0, sx, sy, skx, sky);
}

/** Creates a transform with translation and given rotation/skew about a point. */
public RMTransform(double dx,double dy,double anAngle,double porx,double pory,double sx,double sy,double skx,double sky)
{
    // Translate point of rotation to origin
    translate(-porx, -pory);

    // Rotate by anAngle, scale by scale x & scale y and skew by skx & sky
    if(skx!=0 || sky!=0) skew(skx, sky);
    if(sx!=1 || sy!=1) scale(sx, sy);
    if(anAngle!=0) rotate(anAngle);
   
    // Translate point of rotation back plus add x & y
    translate(porx + dx, pory + dy);
}

/** Returns the first component of the transform matrix. */
public double a()  { return _a; }

/** Returns the second component of the transform matrix. */
public double b()  { return _b; }

/** Returns the third component of the transform matrix. */
public double c()  { return _c; }

/** Returns the fourth component of the transform matrix. */
public double d()  { return _d; }

/** Returns the x translation component of the transform matrix. */
public double tx()  { return _tx; }

/** Returns the y translation component of the transform matrix. */
public double ty()  { return _ty; }

/** Returns whether this transform is identity. */
public boolean isIdentity() { return equals(identity); }

/** Translates this transform by given x & y (returns this for convenience). */
public RMTransform translate(double dx, double dy) { _tx += dx; _ty += dy; return this; }

/** Rotates this transform by given angle in degrees (returns this for convenience). */
public RMTransform rotate(double anAngle)
{
    double angle = Math.toRadians(anAngle);
    double c = Math.cos(angle);
    double s = Math.sin(angle);
    return multiply(c, s, -s, c, 0, 0);
}

/** Scales this transform by given scale x and scale y (returns this for convenience). */
public RMTransform scale(double sx, double sy)
{
    return multiply(sx, 0, 0, sy, 0, 0);
}

/** Skews this transform by given skew x and skew y angles in degrees (returns this for convenience). */
public RMTransform skew(double aSkewX, double aSkewY)
{
    double skewX = Math.toRadians(aSkewX);
    double skewY = Math.toRadians(aSkewY);
    double tanSkewX = Math.tan(skewX);
    double tanSkewY = Math.tan(skewY);
    return multiply(1, tanSkewX, tanSkewY, 1, 0, 0);
}

/** Multiplies this transform by the given transform. */
public RMTransform multiply(RMTransform aTransform)
{
    return multiply(aTransform._a, aTransform._b, aTransform._c, aTransform._d, aTransform._tx, aTransform._ty);
}

/** Multiplies this transform by the given transform components (return this for convenience). */
public RMTransform multiply(double a, double b, double c, double d, double tx, double ty)
{
    double a2 = _a*a + _b*c;
    double b2 = _a*b + _b*d;
    double c2 = _c*a + _d*c;
    double d2 = _c*b + _d*d;
    double tx2 = _tx*a + _ty*c + tx;
    double ty2 = _tx*b + _ty*d + ty;
    _a = a2; _b = b2; _c = c2; _d = d2; _tx = tx2; _ty = ty2;
    return this;
}

/** Inverts this transform (and returns this for convenience). */
public RMTransform invert()
{
    double det = (_a*_d - _b*_c);
    
    if(det == 0) {
        _a = 1; _b = 0; _c = 0; _d = 1; _tx = _ty = 0;
    }
    
    else {
        double a = _d/det, b = -_b/det;
        double c = -_c/det, d = _a/det;
        double tx = (_c*_ty - _d*_tx)/det, ty = (_b*_tx - _a*_ty)/det;
        _a = a; _b = b;
        _c = c; _d = d;
        _tx = tx; _ty = ty;
    }
    
    return this;
}

/** Transforms the given point. */
public RMPoint transform(RMPoint aPoint)
{
    double x = aPoint.x, y = aPoint.y;
    aPoint.x = x*_a + y*_c + _tx;
    aPoint.y = x*_b + y*_d + _ty;
    return aPoint;
}

/** Transforms the given size. */
public RMSize transform(RMSize aSize)
{
    double w = aSize.width, h = aSize.height;
    aSize.width = Math.abs(w*_a) + Math.abs(h*_c);
    aSize.height = Math.abs(w*_b) + Math.abs(h*_d);
    return aSize;
}

/** Transforms the given rect. */
public RMRect transform(RMRect aRect)
{
    RMPoint p = new RMPoint(aRect.x, aRect.y); transform(p); // Transform original upper left point
    double p1x = p.x, p1y = p.y, p2x = p.x, p2y = p.y; // Initialize new upper-left/lower-right points
    p.x = aRect.x + aRect.width; p.y = aRect.y; transform(p); // Transform original upper right point
    if(p.x<p1x) p1x = p.x; if(p.y<p1y) p1y = p.y; // Update new upper-left/lower-right points
    if(p.x>p2x) p2x = p.x; if(p.y>p2y) p2y = p.y;
    p.x = aRect.x + aRect.width; p.y = aRect.y + aRect.height; transform(p); // Transform original lower right point
    if(p.x<p1x) p1x = p.x; if(p.y<p1y) p1y = p.y; // Update new upper-left/lower-right points
    if(p.x>p2x) p2x = p.x; if(p.y>p2y) p2y = p.y;
    p.x = aRect.x; p.y = aRect.y + aRect.height; transform(p); // Transform original lower left point
    if(p.x<p1x) p1x = p.x; if(p.y<p1y) p1y = p.y; // Update new upper-left/lower-right points
    if(p.x>p2x) p2x = p.x; if(p.y>p2y) p2y = p.y;
    aRect.x = p1x; aRect.y = p1y;
    aRect.width = p2x - p1x; aRect.height = p2y - p1y; // Reset rect from new upper-left/lower-right
    return aRect;
}

/** Transforms the given size as a vector (preserves negative values). */
public RMSize transformVector(RMSize aSize)
{
    double w = aSize.width, h = aSize.height;
    aSize.width = w*_a + h*_c;
    aSize.height = w*_b + h*_d;
    return aSize;
}

/** Standard equals implementation. */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    if(!(anObj instanceof RMTransform)) return false;
    RMTransform t = (RMTransform)anObj;
    if(!SnapMath.equals(t._a, _a)) return false;
    if(!SnapMath.equals(t._b, _b)) return false;
    if(!SnapMath.equals(t._c, _c)) return false;
    if(!SnapMath.equals(t._d, _d)) return false;
    if(!SnapMath.equals(t._tx, _tx)) return false;
    if(!SnapMath.equals(t._ty, _ty)) return false;
    return true;
}

/** Returns AWT version of transform for use with Java2D (caching it if needed). */
public AffineTransform awt()
{
    if(_awt==null)
        _awt = new AffineTransform(_a, _b, _c, _d, _tx, _ty);
    return _awt;
}

/**
 * Standard clone implementation.
 */
public Object clone()
{
    try { return super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
}

/** Returns a string representation of the transform. */
public String toString() { return "[" + _a + " " + _b + " " + _c + " " + _d + " " + _tx + " " + _ty + "]"; }

}