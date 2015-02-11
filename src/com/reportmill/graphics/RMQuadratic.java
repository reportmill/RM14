package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.geom.Rectangle2D;

/**
 * This class models a simple quadratic curve, providing methods for extracting points, distance calculation, bisection,
 * hit detection and such.
 */
public class RMQuadratic extends RMLine {
    
    // Quadratic control point (inherits start & end from line)
    public RMPoint _cp1;
  
/**
 * Creates a new quadratic curve.
 */
public RMQuadratic()  { _cp1 = RMPoint.zeroPoint; }

/**
 * Creates a new quadratic curve for this gvein start point, control point and end point.
 */
public RMQuadratic(RMPoint startPoint, RMPoint controlPoint, RMPoint endPoint)
{
    super(startPoint, endPoint);
    _cp1 = controlPoint;
}

/**
 * Creates a new quadratic curve for this gvein start point, control point and end point.
 */
public RMQuadratic(double x1, double y1, double x2, double y2, double x3, double y3)
{
    super(new RMPoint(x1, y1), new RMPoint(x3, y3));
    _cp1 = new RMPoint(x2, y2);
}

/**
 * Sets the curve values.
 */
public void setCurve(double x1, double y1, double x2, double y2, double x3, double y3)
{
    _sp = new RMPoint(x1, y1);
    _cp1 = new RMPoint(x2, y2);
    _ep = new RMPoint(x3, y3);
}

/**
 * Sets the curve values.
 */
public void setCurve(RMPoint p1, RMPoint p2, RMPoint p3)  { _sp = p1; _cp1 = p2; _ep = p3; }

/**
 * Returns the point on this curve at the parametric location t (defined from 0-1).
 */
public RMPoint getPoint(double t, RMPoint aPoint)
{
    // p' = (1-t)^2*p0 + 2*t*(1-t)*p1 + t^2*p2
    double s = 1 - t, s2 = s*s, t2 = t*t;
    double x = s2*_sp.x + 2*t*s*_cp1.x + t2*_ep.x;
    double y = s2*_sp.y + 2*t*s*_cp1.y + t2*_ep.y;
    if(aPoint==null)
        return new RMPoint(x, y);
    aPoint.setLocation(x, y);
    return aPoint;
}

/**
 * Returns the minimum distance from the given point to this segment.
 */
public double getDistance(RMPoint aPoint)  { return getDistanceQuadratic(aPoint); }

/**
 * Returns the minimum distance from the given point to the curve.
 */
public double getDistanceQuadratic(RMPoint aPoint)
{
    // If control points almost on end ponts line, return distance to line
    double dist = getDistanceLine(_cp1);
    if(dist<.255)
        return getDistanceLine(aPoint);

    // Split the curve and recurse
    RMQuadratic c1 = new RMQuadratic();
    RMQuadratic c2 = new RMQuadratic();
    subdivide(c1, c2);
    double dist1 = c1.getDistanceQuadratic(aPoint);
    double dist2 = c2.getDistanceQuadratic(aPoint);
    return Math.min(dist1, dist2);
}

/**
 * Returns the point count of segment.
 */
public int getPointCount()  { return 3; }

/**
 * Returns the x of point at given index.
 */
public double getPointX(int anIndex)  { return anIndex==0? _sp.x : anIndex==1? _cp1.x : _ep.x; }

/**
 * Returns the y of point at given index.
 */
public double getPointY(int anIndex)  { return anIndex==0? _sp.y : anIndex==1? _cp1.y : _ep.y; }

/**
 * Subdivides this curve into the given left and right curves.
 */
public void subdivide(RMQuadratic left, RMQuadratic right)
{
    // Calculate new control points
    double x1 = _sp.x, y1 = _sp.y;
    double x2 = _ep.x, y2 = _ep.y;
    double ctrlx1 = (_sp.x + _cp1.x) / 2f;
    double ctrly1 = (_sp.y + _cp1.y) / 2f;
    double ctrlx2 = (_ep.x + _cp1.x) / 2f;
    double ctrly2 = (_ep.y + _cp1.y) / 2f;
    double midpx = (ctrlx1 + ctrlx2) / 2f;
    double midpy = (ctrly1 + ctrly2) / 2f;
    
    // Set new curve values if curves are present
    if(left!=null)
        left.setCurve(x1, y1, ctrlx1, ctrly1, midpx, midpy);
    if(right!=null)
        right.setCurve(midpx, midpy, ctrlx2, ctrly2, x2, y2);
}

/**
 * Returns the min x point of this bezier.
 */
public double getMinX()  { return Math.min(super.getMinX(), _cp1.x); }

/**
 * Returns the min y point of this bezier.
 */
public double getMinY()  { return Math.min(super.getMinY(), _cp1.y); }

/**
 * Returns the max x point of this bezier.
 */
public double getMaxX()  { return Math.max(super.getMaxX(), _cp1.x); }

/**
 * Returns the max y point of this bezier.
 */
public double getMaxY()  { return Math.max(super.getMaxY(), _cp1.y); }

/**
 * Returns the bounds.
 */
public void getBounds(Rectangle2D aRect)  { getBounds(_sp.x, _sp.y, _cp1.x, _cp1.y, _ep.x, _ep.y, aRect); }

/**
 * Returns the bounds of the bezier.
 */
public static void getBounds(double x0, double y0, double x1, double y1, double x2, double y2, Rectangle2D aRect)
{
    // Declare coords for min/max points
    double p1x = x0;
    double p1y = y0;
    double p2x = x0;
    double p2y = y0;

    // For quadratic, slope at point t is just linear interpolation of slopes at the endpoints.
    // Find solution to LERP(slope0,slope1,t) == 0
    double d = x0 - 2*x1 + x2;
    double t = d==0 ? 0 : (x0 - x1) / d;

    // If there's a valid solution, get actual x point at t and add it to the rect
    if(t>0 && t<1) {
        double turningpoint = x0*(1-t)*(1-t) + 2*x1*(1-t)*t + x2*t*t;
        p1x = Math.min(p1x, turningpoint);
        p2x = Math.max(p2x, turningpoint);
    }
    
    // Do the same for y
    d = y0 - 2*y1 + y2;
    t = d==0? 0 : (y0 - y1)/d;
    if(t>0 && t<1) {
        double turningpoint = y0*(1-t)*(1-t) + 2*y1*(1-t)*t + y2*t*t;
        p1y = Math.min(p1y, turningpoint);
        p2y = Math.max(p2y, turningpoint);
    }
    
    // Include endpoint
    p1x = Math.min(p1x, x2);
    p2x = Math.max(p2x, x2);
    p1y = Math.min(p1y, y2);
    p2y = Math.max(p2y, y2);    
    
    // Set rect
    aRect.setRect(p1x, p1y, p2x - p1x, p2y - p1y);
}

/**
 * Returns the arc length of the segment up to parametric value t
 */
public float getArcLength(float start, float end)
{ 
    RMMath.RMFunc alen = getArcLengthFunction();
    double l = alen.f(end);
    if (start > 0)
        l -= alen.f(start);
    return (float)l;
}

/**
 * Returns an RMFunc which calculates the arclength of the curve up to t
 */
public RMMath.RMFunc getArcLengthFunction()
{ 
    // Arc length of parametric curve is defined by: len = Integral[0,t, Sqrt[(dx/dt)^2 + (dy/dt)^2]]
    // We calculate dx/dt and dy/dt by integrating the first level of the de Castlejau algorithm
    //   d(t)/dt = 1,  d(1-t)/dy = -1
    final double cx[] = { _cp1.x - _sp.x, _ep.x - _cp1.x };
    final double cy[] = { _cp1.y - _sp.y, _ep.y - _cp1.y };
    
    // create function for 2*Sqrt[(dx/dt)^2 + (dy/dt)^2]
    final RMMath.RMFunc integrand = new RMMath.RMFunc() {
        public double f(double t) { 
            double ti = 1-t;
            double dxdt = (cx[0]*ti+cx[1]*t);
            double dydt = (cy[0]*ti+cy[1]*t);
            return 2*Math.sqrt(dxdt*dxdt + dydt*dydt);
        }
    };
   
    // return the integration function
    return new RMMath.RMFunc() {
        public double f(double t) { return integrand.integrate(0,t,100); }
        public double fprime(double t, int level) { 
            return level==1 ? integrand.f(t) : super.fprime(t,level);
        }
    };
}

/**
 * Returns parametric point t that corresponds to a given length along the curve.
 * l is in the range [0-1] (ie. percentage of total arclength)
 */
public float getParameterForLength(float l)
{
    // NB: This uses the exact solution to get the position. The JFX will use the interpolated solution.  It might
    // be better to use the interpolated solution here, too, in case things don't quite match up.
    RMMath.RMFunc solution = getInverseArcLengthFunction();
    return (float)solution.f(l);
}

/**
 * Returns an RMFunc which calculates t for a percentage length along the curve.
 */
public RMMath.RMFunc getInverseArcLengthFunction()
{ 
    // Get the arclength function, Scale it to the range 0-1, and return the inverse of the scaled function
    RMMath.RMFunc alen = getArcLengthFunction();
    RMMath.RMFunc scaled = new RMCurveFit.ScaledFunc(alen);
    return new RMCurveFit.InverseFunc(scaled);
}

}