package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.List;

/**
 * Some utility methods for RMPaths.
 */
public class RMPathUtils {

    // Constants for path join operations
    public static final int  JOIN_OP_ADD = 0;
    public static final int  JOIN_OP_SUBTRACT = 1;
    public static final int  JOIN_OP_INTERSECT = 2;
    public static final int  JOIN_OP_XOR = 3;

/**
 * Returns a new path from an AWT shape.
 */
public static RMPath appendShape(RMPath aPath, Shape aShape)
{
    float pt[] = new float[6];
    
    for(PathIterator pi = aShape.getPathIterator(null); !pi.isDone(); pi.next()) {
        int type = pi.currentSegment(pt);
        switch(type) {
            case PathIterator.SEG_MOVETO: aPath.moveTo(pt[0], pt[1]); break;
            case PathIterator.SEG_LINETO: aPath.lineTo(pt[0], pt[1]); break;
            case PathIterator.SEG_QUADTO: aPath.quadTo(pt[0], pt[1], pt[2], pt[3]); break;
            case PathIterator.SEG_CUBICTO: aPath.curveTo(pt[0], pt[1], pt[2], pt[3], pt[4], pt[5]); break;
            case PathIterator.SEG_CLOSE: aPath.closePath();
        }
    }

    // Return path
    return aPath;
}

/**
 * Adds elements describing an oval in the given rect to this path.
 */
public static RMPath appendOval(RMPath aPath, RMRect aRect, float startAngle, float sweep, boolean connect)
{
    // Get half-width/height, the x/y mid-points and the "magic" oval factor I calculated in Mathematica one time
    double hw = aRect.width/2f, hh = aRect.height/2f;
    double midX = aRect.getMidX(), midY = aRect.getMidY();
    double magic = .5523f;
    
    // If connect was requested draw line from current point (or rect center) to start point of oval
    if(connect) {
        if(aPath.getElementCount()==0)
            aPath.moveTo(midX, midY);
        aPath.lineTo(midX + RMMath.cos(startAngle)*hw, midY + RMMath.sin(startAngle)*hh);
    }
    
    // If connect wasn't requested move to start point of oval. */
    else aPath.moveTo(midX + RMMath.cos(startAngle)*hw, midY + RMMath.sin(startAngle)*hh);

    // Make bezier for upper right quadrant PScurveto(-hw*f, -hh, -hw, -hh*f, -hw, 0);
    double angle = startAngle, endAngle = startAngle + sweep;
    for(; angle + 90 <= endAngle; angle += 90)
        aPath.curveTo(midX + RMMath.cos(angle)*hw - RMMath.sin(angle)*hw*magic,
                midY + RMMath.sin(angle)*hh + RMMath.cos(angle)*hh*magic,
                midX + RMMath.cos(angle+90)*hw + RMMath.sin(angle+90)*hw*magic,
                midY + RMMath.sin(angle+90)*hh - RMMath.cos(angle+90)*hh*magic,
                midX + RMMath.cos(angle+90)*hw,
                midY + RMMath.sin(angle+90)*hh);

    // If sweep did not end on a quadrant boundary, add remainder of quadrant
    if(angle < startAngle + sweep) {
        double sweepRatio = RMMath.mod(sweep, 90f)/90f; // Math.IEEEremainder(sweep, 90)/90;
        aPath.curveTo(midX + RMMath.cos(angle)*hw - RMMath.sin(angle)*hw*magic*sweepRatio,
                midY + RMMath.sin(angle)*hh + RMMath.cos(angle)*hh*magic*sweepRatio,
                midX + RMMath.cos(startAngle+sweep)*hw + RMMath.sin(startAngle+sweep)*hw*magic*sweepRatio,
                midY + RMMath.sin(startAngle+sweep)*hh - RMMath.cos(startAngle+sweep)*hh*magic*sweepRatio,
                midX + RMMath.cos(startAngle+sweep)*hw,
                midY + RMMath.sin(startAngle+sweep)*hh);
    }
    
    // Close path
    aPath.closePath();

    // Return this path
    return aPath;
}

/**
 * Returns whether path has any curve segments (QuadTo, CurveTo).
 */
public static boolean isPolygonal(RMPath aPath)
{
    // Iterate over path segments and return false if any are QUAD_TO or CURVE_TO
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        byte type = aPath.getElement(i);
        if(type==RMPath.QUAD_TO || type==RMPath.CURVE_TO)
            return false;
    }
    
    // Return true if no curve segments were found
    return true;
}

/**
 * Returns whether the path has any cubic curves in it.
 */
public static boolean hasCubics(RMPath aPath)
{
    // Iterate over path segements and return true if any are CURVE_TO
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++)
        if(aPath.getElement(i)==RMPath.CURVE_TO)
            return true;
    
    // Return false if no CURVE_TO segements were found
    return false;
}

/** Returns a path with only moveto, lineto. */
public static RMPath getPathFlattened(RMPath aPath)
{
    // If no curves in this path, just return this
    if(isPolygonal(aPath))
        return aPath;
    
    // Get a new path and point-array for path segment iteration
    RMPath path = new RMPath();
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = null;
    
    // Iterate over path segments
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        
        // Get segment type and points
        byte type = aPath.getElement(i, points);
        
        switch(type) {
            
            case RMPath.MOVE_TO:
                path.moveTo(points[0]); 
                lastPoint = points[0];
                break;
            case RMPath.LINE_TO: 
                path.lineTo(points[0]);
                lastPoint = points[0];
                break;
            case RMPath.CLOSE: path.closePath(); break;
                
            case RMPath.QUAD_TO:
                points[2] = points[1];
                points[1] = new RMPoint((2*points[0].x+points[1].x)/3,
                                        (2*points[0].y+points[1].y)/3);
                points[0] = new RMPoint((2*points[0].x+lastPoint.x)/3,
                                        (2*points[0].y+lastPoint.y)/3);
                // fall through
            case RMPath.CURVE_TO: {
                addBezier(path, new RMBezier(lastPoint, points[0], points[1], points[2]));
                lastPoint = points[2];
                break;
            }                
        }
    }
    
    // Set bounds of new path to be same as this bounds
    path.setBounds(aPath.getBounds2D());
    
    // Return new path
    return path;
}

/**
 * Returns a copy of the path with cubics demoted to one or more quadratics.
 */
public static RMPath getPathWithFlattendCubics(RMPath aPath)
{
    // If no cubics in this path, just return this
    if(!RMPathUtils.hasCubics(aPath))
        return aPath;
    
    // Get a new path and point-array for path segment iteration
    RMPath path = new RMPath();
    RMPoint points[] = new RMPoint[3];

    // Iterate over path segments
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        byte type = aPath.getElement(i, points);

        switch(type) {
            case RMPath.MOVE_TO: path.moveTo(points[0]); break;
            case RMPath.LINE_TO: path.lineTo(points[0]); break;
            case RMPath.QUAD_TO: path.quadTo(points[0], points[1]); break;
            case RMPath.CURVE_TO: quadraticCurveTo(path, points[0], points[1], points[2]); break;
            case RMPath.CLOSE: path.closePath(); break;
        }
    }

    // Set bounds of new path to be same as this bounds
    path.setBounds(aPath.getBounds2D());
    
    // Return new path
    return path;
}


/**
 * This is effectively a curveTo but does it internally with one or more approximated quadratics.
 *  I found a C++ version of this on http://homepages.tig.com.au/~dkl/swf/ - Jeff Martin.
 *  It illustrates how to approximate 4-point cubic Beziers with 3-point quadratic Beziers.  PostScript (and most apps)
 *  use cubics, Flash uses quadratics.
 */
public static void quadraticCurveTo(RMPath aPath, RMPoint cp1, RMPoint cp2, RMPoint aPoint)
{
    // A, D are endpoints, B, C are the "off points"
    RMPoint A = aPath.getPoint(aPath.getPointCount() - 1); // Last point
    RMPoint B = cp1, C = cp2, D = aPoint, E = null; // E is the "off" point for the Quadratic.
    double splitPoint = 0.5;

    // If all the points equal, just return
    if(B.equals(A) && C.equals(B) && D.equals(C))
        return;

    // If the end-points and off-points equal replace with line
    else if(B.equals(A) && D.equals(C)) {
        aPath.lineTo(aPoint);
        return;
    }

    else if(!C.equals(B)) {
        int SideA = sideOfPointForLine(A,B,C);
        int SideD = sideOfPointForLine(D,B,C);

        // If A and D are on opposite sides of BC, Cubic must be split in middle
        if(SideA!=SideD && SideA!=0 && SideD!=0)
            E = null;

        // If ABCD co-linear, this is a line!
        else if(SideA==0 && SideD==0) {
            aPath.lineTo(aPoint);
            return;
        }

        // If same sides or ABC colinear or BDC colinear, find point E
        else if((SideA == SideD) || (SideA == 0 && SideD != 0) || (SideA != 0 && SideD == 0)) {

            // B & C are equal, E is B
            if(!D.equals(A) && C.equals(B))
                E = B;
            
            // A & B are equal, E is  C
            else if(A.equals(B) && !B.equals(C) && !C.equals(D))
                E = C;
            
            // C & D are equal, E is B
            else if(C.equals(D) && !C.equals(B) && !B.equals(A))
                E = B;

            // If A & D are on the same side of BC, E is intersection of AB and CD
            else E = intersectionOfLines(A,B,C,D);
        }

        else System.err.println("RMPath:quadraticCurveToPoint: unexpected condition");
    }

    if(E!=null) {
        
        // Find max. distance between cubic and quadratic
        double maxDistance = 0.0f;
        int nTestPoints = 32;
        
        // This just tests the distance between the two curves at an abitrary number of test points.  Really should
        // do something better here, based on the length of the curve probably...
        for(int i=1; i<nTestPoints; i++) {
            float  t = i/(float)nTestPoints;
            RMPoint cubic = evaluateBezier(3, t, A, B, C, D);
            RMPoint quadratic = evaluateBezier(2, t, A, E, D, RMPoint.zeroPoint);
            double  distance = quadratic.distanceSq(cubic);

            if (distance > maxDistance) {
                splitPoint = t;
                maxDistance = distance;
            }
        }

        // If max distance less than half a pixel (squared), no need to split - replace Cubic with Quadratic
        if(maxDistance < .25f) {
            aPath.quadTo(E, aPoint);
            return;
        }
    }

    // Split the cubic at splitPoint and recurse
    splitCubicAtPoint(aPath, splitPoint, A, B, C, D);
}

/** Split Cubic bezier in two. */
private static void splitCubicAtPoint(RMPath aPath, double dSplit, RMPoint p1, RMPoint p2, RMPoint p3, RMPoint p4)
{
    double VtempX[][] = { {p1.x,p2.x,p3.x,p4.x}, {p1.x,p2.x,p3.x,p4.x}, {p1.x,p2.x,p3.x,p4.x}, {p1.x,p2.x,p3.x,p4.x} };
    double VtempY[][] = { {p1.y,p2.y,p3.y,p4.y}, {p1.y,p2.y,p3.y,p4.y}, {p1.y,p2.y,p3.y,p4.y}, {p1.y,p2.y,p3.y,p4.y} };

    // Triangle computation
    for(int i=1; i<=3; i++) {
        for(int j=0 ; j<=3-i; j++) {
            VtempX[i][j] = (1.0 - dSplit) * VtempX[i-1][j] + dSplit * VtempX[i-1][j+1];
            VtempY[i][j] = (1.0 - dSplit) * VtempY[i-1][j] + dSplit * VtempY[i-1][j+1];          
        }
    }

    // insert left cubic curve - for(j = 1; j<=3; j++) dpPoints[j-1] = Vtemp[j][0];
    // insert right cubic curve - for(j=1; j<=3; j++) dpPoints[j-1] = Vtemp[3-j][j];
    p1 = new RMPoint(VtempX[3][0], VtempY[3][0]);
    p2 = new RMPoint(VtempX[1][0], VtempY[1][0]);
    p3 = new RMPoint(VtempX[2][0], VtempY[2][0]);
    quadraticCurveTo(aPath, p2, p3, p1);
    p1 = new RMPoint(VtempX[0][3], VtempY[0][3]);
    p2 = new RMPoint(VtempX[2][1], VtempY[2][1]);
    p3 = new RMPoint(VtempX[1][2], VtempY[1][2]);
    quadraticCurveTo(aPath, p2, p3, p1);
}

/**
 * Evaluate a Bezier curve at a particular parameter value.
 *  degree (The degree of the bezier curve), V (Array of control points), t (Parametric value to find point at)
 */
private static RMPoint evaluateBezier(int degree, double t, RMPoint p1, RMPoint p2, RMPoint p3, RMPoint p4)
{
    double pointsX[] = { p1.x, p2.x, p3.x, p4.x };
    double pointsY[] = { p1.y, p2.y, p3.y, p4.y };
    
    // Triangle computation
    for(int i=1; i <= degree; i++) {   
        for(int j=0; j <= degree-i; j++) {
            pointsX[j] = (1.0 - t) * pointsX[j] + t * pointsX[j+1];
            pointsY[j] = (1.0 - t) * pointsY[j] + t * pointsY[j+1];
        }
    }

    return new RMPoint(pointsX[0], pointsY[0]);
}

/**
 * Return relative value of b for aPoint. y = mx+b. m = (y2-y1)/(m2-m1). b = y-mx. 
 * If line is vertical (x2==x1), returns relative value of aPoint.x compared to x1.
 */
private static int sideOfPointForLine(RMPoint aPoint, RMPoint lp1, RMPoint lp2)
{
    if(!RMMath.equals(0, lp2.x - lp1.x)) {
        double m = (lp2.y - lp1.y)/(lp2.x - lp1.x);
        double b = lp1.y - m*lp1.x;
        double nb = aPoint.y - m*aPoint.x;

        if(RMMath.equals(nb, b))
            return 0;
        if(nb>b)
            return 1;
        return -1;
    }

    if(RMMath.equals(aPoint.x, lp1.x))
        return 0;
    if(aPoint.x < lp1.x)
        return 1;
    return -1;
}

private static RMPoint intersectionOfLines(RMPoint l1p1, RMPoint l1p2, RMPoint l2p1, RMPoint l2p2)
{
    double l1dx = l1p2.x - l1p1.x, l1dy = l1p2.y - l1p1.y;
    double l2dx = l2p2.x - l2p1.x, l2dy = l2p2.y - l2p1.y;
    double x, y;

    // If neither line is vertical, calc intersection point by getting line slopes & y-intercepts
    if(!RMMath.equals(l1dx, 0) && !RMMath.equals(l2dx, 0)) {
        double m1 = l1dy/l1dx;
        double b1 = l1p1.y - m1*l1p1.x;
        double m2 = l2dy/l2dx;
        double b2 = l2p1.y - m2*l2p1.x;
        x = (b2 - b1)/(m1 - m2); // From y = m*x+b, which says: m1*x + b1 = m2*x + b2
        y = m1*x + b1;
    }

    // If line 1 is vertical
    else if(RMMath.equals(0, l1dx)) {
        double m2 = l2dy/l2dx;
        double b2 = l2p1.y - m2*l2p1.x;
        x = l1p2.x;
        y = m2*x + b2;
    }

    // If line 2 is vertical
    else {
        double m1 = l1dy/l1dx;
        double b1 = l1p1.y - m1*l1p1.x;
        x = l2p2.x;
        y = m1*x + b1;
    }
    
    // This is probably bogus, we should be able to determine this earlier
    if(Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y))
        return null;
    
    // Return new point
    return new RMPoint(x,y);
}

/**
 * Adds a bezier to the path as a series of approximated line segments.
 */
private static void addBezier(RMPath aPath, RMBezier aBezier)
{
    // Get simple line between bezier start/end points and if control points almost on line, return hit info for line
    RMLine bezierLine = new RMLine(aBezier._sp, aBezier._ep);
    double dist1 = bezierLine.getDistanceLine(aBezier._cp1);
    double dist2 = bezierLine.getDistanceLine(aBezier._cp2);
    if(dist1<.25 && dist2<.25) {
        aPath.lineTo(bezierLine._ep);
        return;
    }
    
    // Subdivide bezier and add pieces
    RMBezier b1 = new RMBezier();
    RMBezier b2 = new RMBezier();
    aBezier.subdivide(b1, b2, .5);
    addBezier(aPath, b1);
    addBezier(aPath, b2);
}

/**
 * Returns shortest distance from any point in path to given point.
 */
public static double getDistance(RMPath aPath, RMPoint aPoint)
{
    // Declare variable for starting distance
    double distance = Float.MAX_VALUE;

    // Declare variables for path segment points, last path point and last path move-to point
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    RMPoint lastMoveToPoint = RMPoint.zeroPoint;

    // Iterate over path segments
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        
        // Get current segment type and points
        byte type = aPath.getElement(i, points);

        // Handle specific segment types
        switch(type) {

            // Handle MOVE_TO
            case RMPath.MOVE_TO:
                
                // Update last point & last move-to point and break
                lastPoint = lastMoveToPoint = points[0];
                break;

            // Handle CLOSE
            case RMPath.CLOSE:
                
                // If last point is same as last move-to point, just break
                if(lastPoint.equals(lastMoveToPoint))
                    break;
                
                // Otherwise, update current segment point and fall through to LINE_TO
                points[0] = lastMoveToPoint;

            // Handle LINE_TO
            case RMPath.LINE_TO: {
                
                // Create line for current segment
                RMLine line = new RMLine(lastPoint, points[0]);
                
                // Get distance from given point to current segment and update min distance if needed
                double distance2 = line.getDistanceLine(aPoint);
                distance = Math.min(distance, distance2);
                
                // Update last point and break
                lastPoint = points[0];
                break;
            }
            
            // QUAD_TO - might be bogus, just resetting points to duplicate control point and falling through
            case RMPath.QUAD_TO: points[2] = points[1]; points[1] = points[0];

            // Handle CURVE_TO
            case RMPath.CURVE_TO: {
                
                // Create bezier for current segment
                RMBezier bezier = new RMBezier(lastPoint, points[0], points[1], points[2]);
                
                // Get distance from given point to current segment and update min distance if needed
                double distance2 = bezier.getDistanceBezier(aPoint);
                distance = Math.min(distance, distance2);
                
                // Update last point and break
                lastPoint = points[2];
                break;
            }
        }
    }

    // Return the distance
    return distance;
}

/**
 * Returns shortest distance from any point in path to given point (negative means inside the path).
 */
public static double getDistanceSigned(RMPath aPath, RMPoint aPoint)
{
    // Get distance from point to path
    double distance = getDistance(aPath, aPoint);
    
    // If path contains point, make distance negative
    if(aPath.contains(aPoint))
        distance = - distance;
    
    // Return distance
    return distance;
}

/**
 * Returns the combined paths from given paths.
 */
public static RMPath join(List paths, int aJoinOp)
{
    // If path list is less than two, return null
    if(paths.size()<2)
        return (RMPath)paths.get(0);
    
    // Get first path and an area for it
    RMPath p1 = (RMPath)paths.get(0);
    Area a1 = new Area(p1);
    
    // Iterate over successive paths and add them
    for(int i=1, iMax=paths.size(); i<iMax; i++) {
        RMPath p2 = (RMPath)paths.get(i);
        Area a2 = new Area(p2);
        
        // Handle PATH_ADD
        if(aJoinOp==JOIN_OP_ADD)
             a1.add(a2);
        
        // Handle PATH_SUBTRACT
        if(aJoinOp==JOIN_OP_SUBTRACT)
            a1.subtract(a2);
    }
    
    // Return a new path from combined area
    return RMPathUtils.appendShape(new RMPath(), a1);
}

}