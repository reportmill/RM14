package com.reportmill.base;
import java.awt.geom.*;

/**
 * This class extends Rectanlge2D to add some convenient rect methods.
 */
public class RMRect extends Rectangle2D.Double {

    // A shared zero rect
    public static final RMRect zeroRect = new RMRect(0,0,0,0);
    
    // A shared unit rect
    public static final RMRect unitRect = new RMRect(0,0,1,1);

    // DivdeRect constants
    public static final byte MinXEdge = 1;
    public static final byte MinYEdge = 1<<1;
    public static final byte MaxXEdge = 1<<2;
    public static final byte MaxYEdge = 1<<3;

/**
 * Creates an empty rect.
 */
public RMRect() { }

/**
 * Creates a rect with the given x, y, width and height (doubles).
 */
public RMRect(double x, double y, double w, double h)  { super(x, y, w, h); }

/**
 * Creates a rect enclosing the given array of points.
 */
public RMRect(Point2D... points)
{
    // If no points, just return
    if(points.length==0) return;
    
    // Initialize points (we're going to treat width & height as max point for a little bit)
    x = width = points[0].getX();
    y = height = points[0].getY();

    // Iterate over remaining points
    for(int i=1; i<points.length; i++) {
        x = Math.min(x, points[i].getX());
        y = Math.min(y, points[i].getY());
        width = Math.max(width, points[i].getX());
        height = Math.max(height, points[i].getY());
    }
    
    // Recast width & height from max point
    width = width - x;
    height = height - y;
}

/**
 * Creates a rect from an awt rect.
 */
public RMRect(Rectangle2D aRect)  { this(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight()); }

/**
 * Returns the origin of the rect as an RMPoint.
 */
public RMPoint getOrigin()  { return new RMPoint(x, y); }

/**
 * Sets the origin of the rect.
 */
public RMRect setOrigin(double x, double y)  { this.x = x; this.y = y; return this; }

/**
 * Returns the x mid-point of the rect.
 */
public double getMidX()  { return x + width/2f; }

/**
 * Returns the y mid-point of the rect.
 */
public double getMidY()  { return y + height/2f; }

/**
 * Unions the receiver rect with the given rect.
 */
public void union(RMRect r2)  { unionEvenIfEmpty(r2); }

/**
 * Creates a rect encompassing the receiver and the given rect.
 */
public RMRect unionRect(RMRect r2)
{
    // Copy rect, do union, return rect
    RMRect copy = new RMRect(this);
    copy.unionEvenIfEmpty(r2);
    return copy;
}

/**
 * Unions the receiver rect with the given rect.
 */
public void unionEvenIfEmpty(RMRect r2)
{
    // If given rect is null, just return
    if(r2==null) return;
    
    // Reset rect
    double minX = x, maxX = getMaxX();
    double minY = y, maxY = getMaxY();
    x = Math.min(minX, r2.x);
    y = Math.min(minY, r2.y);
    width = Math.max(maxX, r2.getMaxX()) - x;
    height = Math.max(maxY, r2.getMaxY()) - y;
}

/**
 * Returns whether the receiver intersects with the given rect.
 */
public boolean intersectsRect(RMRect aRect)
{
    if(width<=0 || height<=0 || aRect.width<=0 || aRect.height<=0) return false;
    return intersectsRectEvenIfEmpty(aRect);
}

/**
 * Returns whether the receiver intersects with the given rect.
 */
public boolean intersectsRectEvenIfEmpty(RMRect aRect)
{
    if(x<aRect.x) { if(getMaxX()<=aRect.x) return false; }
    else if(aRect.getMaxX() <= x) return false;
    if(y<aRect.y) { if(getMaxY()<=aRect.y) return false; }
    else if(aRect.getMaxY() <= y) return false;
    return true;
}

/**
 * Returns the rect formed by the area overlapping with the given rect.
 */
public RMRect intersectedRect(RMRect r2)
{
    double minX = Math.max(x, r2.x);
    double minY = Math.max(y, r2.y);
    double maxX = Math.min(getMaxX(), r2.getMaxX());
    double maxY = Math.min(getMaxY(), r2.getMaxY());
    
    // If rect is empty, return empty rect
    if(minY>maxY || minX>maxX)
        return new RMRect();
    
    // Return intersection rect
    return new RMRect(minX, minY, maxX-minX, maxY-minY);
}

/**
 * Returns whether the receiver intersects with the given rect (horizontally only).
 */
public boolean widthsIntersect(RMRect r2)
{
    RMRect r1 = this;
    if(r1.width <= 0f || r2.width <= 0f) return false;
    if(r1.x < r2.x) { if(r1.x + r1.width <= r2.x) return false; }
    else { if(r2.x + r2.width <= r1.x) return false; }
    return true;
}

/**
 * Offsets the receiver by the given x & y.
 */
public RMRect offset(double dx, double dy)  { x += dx; y += dy; return this; }

/**
 * Creates a rect derived by offsetting the receiver by the given x & y.
 */
public RMRect offsetRect(double dx, double dy)  { return new RMRect(this).offset(dx, dy); }

/**
 * Creates a rect representing the largest square inside rect.
 */
public RMRect squareRectInRect()
{
    RMRect rect = new RMRect(this);
    if(rect.width > rect.height) { rect.x += (rect.width - rect.height)/2f; rect.width = rect.height; }
    else { rect.y += (rect.height - rect.width)/2f; rect.height = rect.width; }
    return rect;
}

/**
 * Returns whether the receiver contains the given rect.
 */
public boolean containsRect(RMRect aRect)
{
    return x<=aRect.x && getMaxX()>=aRect.getMaxX() && y<=aRect.y && getMaxY()>=aRect.getMaxY();
}

/**
 * Returns an array of four points containing each corner of the rect.
 */
public RMPoint[] getPoints()
{
    RMPoint p[] = new RMPoint[4];
    p[0] = new RMPoint(x, y);
    p[1] = new RMPoint(x + width, y);
    p[2] = new RMPoint(x + width, y + height);
    p[3] = new RMPoint(x, y + height);
    return p;
}

/**
 * Insets the receiver rect by the given amount.
 */
public RMRect inset(double anInset)  { return inset(anInset, anInset); }

/**
 * Insets the receiver rect by the given amount.
 */
public RMRect inset(double xInset, double yInset)
{
    x += xInset; width -= 2*xInset;
    y += yInset; height -= 2*yInset;
    return this;
}

/**
 * Creates a rect derived from the receiver inset by the given amount.
 */
public RMRect insetRect(double anInset)  { return insetRect(anInset, anInset); }

/**
 * Creates a rect derived from the receiver inset by the given amount.
 */
public RMRect insetRect(double xInset, double yInset)  { return new RMRect(this).inset(xInset, yInset); }

/**
 * Returns the mask of edges hit by the given point.
 */
public int getHitEdges(Point2D aPoint, double aRadius)
{
    // Declare mask for hit edges
    int hitEdges = 0;
    
    // Check MinXEdge, MaxXEdge, MinYEdge, MaxYEdge
    if(Math.abs(aPoint.getX()-getX()) < aRadius) hitEdges |= MinXEdge;
    else if(Math.abs(aPoint.getX()-getMaxX()) < aRadius) hitEdges |= MaxXEdge;
    if(Math.abs(aPoint.getY()-getY()) < aRadius) hitEdges |= MinYEdge;
    else if(Math.abs(aPoint.getY()-getMaxY()) < aRadius) hitEdges |= MaxYEdge;
    return hitEdges;
}

/**
 * Resets the edges of a rect, given a mask of edges and a new point.
 */
public void setHitEdges(Point2D aPoint, int anEdgeMask)
{
    // Handle MinXEdge drag
    if((anEdgeMask & MinXEdge) > 0) {
        double newX = Math.min(aPoint.getX(), getMaxX()-1);
        width = getMaxX() - newX; x = newX;
    }
    
    // Handle MaxXEdge drag
    else if((anEdgeMask & MaxXEdge) > 0)
        width = Math.max(1, aPoint.getX() - getX());
    
    // Handle MinYEdge drag
    if((anEdgeMask & MinYEdge) > 0) {
        double newY = Math.min(aPoint.getY(), getMaxY()-1);
        height = getMaxY() - newY; y = newY;
    }
    
    // Handle MaxYEdge drag
    else if((anEdgeMask & MaxYEdge) > 0)
        height = (float)Math.max(1, aPoint.getY() - getY());
}

/**
 * Slices rect by given amount (from given edge) - returns remainder.
 */
public RMRect divideRect(double anAmount, byte anEdge)  { return divideRect(anAmount, anEdge, new RMRect()); }

/**
 * Slices rect by given amount (from given edge) - returns remainder.
 */
public RMRect divideRect(double anAmount, byte anEdge, RMRect aRmndr)
{
    if(aRmndr!=null)
        aRmndr.setRect(this);
    
    switch(anEdge) {
        case MinXEdge: width = anAmount; if(aRmndr!=null) { aRmndr.x += anAmount; aRmndr.width -= anAmount; } break;
        case MinYEdge: height = anAmount; if(aRmndr!=null) { aRmndr.y += anAmount; aRmndr.height -= anAmount; } break;
        case MaxXEdge: x = getMaxX() - anAmount; width = anAmount; if(aRmndr!=null) aRmndr.width -= anAmount; break;
        case MaxYEdge: y = getMaxY() - anAmount; height = anAmount; if(aRmndr!=null) aRmndr.height -= anAmount; break;
    }
    
    return aRmndr==null? this : aRmndr;
}

/**
 * Scales the receiver rect by the given amount.
 */
public RMRect scale(double anAmount)
{
    x *= anAmount; y *= anAmount; width *= anAmount; height *= anAmount; return this;
}

/**
 * Creates a rect derived from the receiver scaled by the given amount.
 */
public RMRect scaledRect(double anAmount)  { return new RMRect(this).scale(anAmount); }

/**
 * Returns the point on the rectangle's perimeter that is intersected by a radial at the given angle from the
 * center of the rect. Zero degrees is at the 3 o'clock position.
 * 
 * @param anAngle Angle in degrees.
 * @param doEllipse Whether to scale radials into ellipse or leave them normal.
 * @return Returns point on perimeter of rect intersected by radial at given angle.
 */
public RMPoint getPerimeterPointForRadial(double anAngle, boolean doEllipse)
{
    // Equation for ellipse is:
    //    x = a cos(n)
    //    y = b sin(n)
    // Define the ellipse a & b axis length constants as half the rect width & height
    double a = width/2, b = height/2; if(a==0 || b==0) return new RMPoint();
    
    // If not elliptical, change a & b to min length so we use normal circle instead of elliptical radians 
    if(!doEllipse)
        a = b = Math.min(a, b);
    
    // Calculate the coordinates of the point on the ellipse/circle for the given angle
    double x1 = a * RMMath.cos(anAngle);
    double y1 = b * RMMath.sin(anAngle);
    
    // First, let's assume the perimeter x coord is on the rect's left or right border
    double x2 = width/2 * RMMath.sign(x1);
    
    // Then calculate the y perimeter coord by assuming y2/x2 = y1/x1
    double y2 = x2 * y1/x1;
    
    // If final perimeter height outside rect height, recalc but assume final perimeter y is top or bottom border
    if(Math.abs(y2)>b) {
        y2 = height/2 * RMMath.sign(y1);
        x2 = y2 * x1/y1;
    }
    
    // Get point in rect coords
    return new RMPoint(getMidX() + x2, getMidY() + y2);
}

/**
 * Returns the given rect moved, if needed, to be contained by this rect.
 */
public RMRect getCorraledRect(RMRect aRect, float anInset)
{
    RMRect rect = new RMRect(aRect);
    if(rect.getMaxX()>getMaxX()-anInset) rect.x = getMaxX() - anInset - rect.getWidth();
    if(rect.getMaxY()>getMaxY()-anInset) rect.y = getMaxY() - anInset - rect.getHeight();
    if(rect.getX()<getX()+anInset) rect.x = getX() + anInset;
    if(rect.getY()<getY()+anInset) rect.y = getY() + anInset;
    return rect;
}

/**
 * Returns a String reprsentation of this rect.
 */
public String toString()  { return "[" + toXMLString() + "]"; }

/**
 * Returns an XML string representation of this rect.
 */
public String toXMLString()
{
    StringBuffer sb = new StringBuffer();
    if(x==(int)x) sb.append((int)x); else sb.append(x); sb.append(' ');
    if(y==(int)y) sb.append((int)y); else sb.append(y); sb.append(' ');
    if(width==(int)width) sb.append((int)width); else sb.append(width); sb.append(' ');
    if(height==(int)height) sb.append((int)height); else sb.append(height);
    return sb.toString();
}

/**
 * Creates a rect from an String in XML format as defined in toXMLString().
 */
public static RMRect fromXMLString(String aString)
{
    double x = RMStringUtils.doubleValue(aString);
    int start = aString.indexOf(' ', 0);
    double y = RMStringUtils.doubleValue(aString, start + 1);
    start = aString.indexOf(' ', start + 1);
    double width = RMStringUtils.doubleValue(aString, start + 1);
    start = aString.indexOf(' ', start + 1);
    double height = RMStringUtils.doubleValue(aString, start + 1);
    return new RMRect(x, y, width, height);
}

/**
 * Standard clone implementation.
 */
public RMRect clone()  { return (RMRect)super.clone(); }

}