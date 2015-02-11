package com.reportmill.graphics;
import com.reportmill.base.*;
import java.util.*;

/**
 * Offset path - a path that follows the curvature of the input path but is located a constant distance
 * away from the curve.
 *
 * Mathematically, the offset path is the path defined by tracing the normal to the curve, scaled by d,
 * along the entire path.
 */
public class RMPathOffsetter {

    // The number of points used to define the new curve segment
    private static final int NUM_FITPOINTS = 50;

/**
 * Create offset path.
 */
public static RMPath createOffsetPath(RMPath aPath, float offset)
{
    // Handle silly case
    if(offset==0) return aPath;
    
    // buffer for points
    RMPoint pathCoords[] = new RMPoint[6];
    
    // the current point
    RMPoint c = new RMPoint();
    
    // the start of the subpath (to handle closepath)
    RMPoint first = new RMPoint();
    int previous_segment = -1;
    boolean subpath = true;
    
    // slopes & normal
    double first_slope = 0, previous_slope = 0;
    RMSize normal = new RMSize();
    RMSize start_tan=null, tan=null;
    RMPoint fitPoints[]=null;
    RMPoint controlpts[]=null;
    
    // all the segments.
    List offsetSegments = new ArrayList(aPath.getElementCount());
    
    // The final path
    RMPath outPath = new RMPath();
    
    // Get offset paths for each segment, as if each segment were independant.
    // Then, adjust start point of offset segment and end point of previous segment so they coincide (miter joins)
    // We store all offset segments in an array. The segments are either cubic beziers or lines, and are stored as 
    // RMLines and RMBeziers. A second pass will concat all the segments into an RMPath once the fixups are done.

    // Iterate over path elements
    for(int el=0, iMax=aPath.getElementCount(); el<iMax; ++el) { int segment = aPath.getElement(el, pathCoords);
    
        // Handle element types
        switch(segment) {
        
        // Handle MoveTo
        case RMPath.MOVE_TO : 
            outPath.addSegments(offsetSegments);
            c.setLocation(pathCoords[0]);
            first.setLocation(pathCoords[0]);
            offsetSegments.clear();
            previous_segment = -1;
            subpath = true;
            break;
            
        /// Handle CurveTo
        case RMPath.CURVE_TO:
            
        // Handle QuadTo
        case RMPath.QUAD_TO: {
            
            // Get degree
            int degree = segment==RMPath.QUAD_TO? 2 : 3;
            
            // allocate (once) the array of points for the curvefit algorithm
            if (fitPoints==null) {
                fitPoints = new RMPoint[NUM_FITPOINTS];
                for(int i=0; i<NUM_FITPOINTS; i++) fitPoints[i] = new RMPoint();
                controlpts = new RMPoint[4];
                tan = new RMSize();
            }
            
            // fill controlpts array for the input curve
            controlpts[0] = c;
            System.arraycopy(pathCoords, 0, controlpts, 1, degree);
            
            // check for degenerate path and skip
            boolean badpath = true;
            for(int i=1; (i<degree+1) && (badpath); ++i)
                if (!controlpts[0].equals(controlpts[i]))
                    badpath=false;
            if (badpath) continue;
            
            for(int i=0; i<NUM_FITPOINTS; ++i) {
                evaluateBezierAndTangent(degree, ((double)i)/(NUM_FITPOINTS-1), controlpts, fitPoints[i], tan);
                normal.width = -tan.height;
                normal.height = tan.width;
                fitPoints[i].x += offset*normal.width;
                fitPoints[i].y += offset*normal.height;
                if(i==0)
                    start_tan = (RMSize)tan.clone();
            }
            
            RMPathFitCurves.getFitCurveSegments(fitPoints,start_tan,tan,offsetSegments);
            
            double slope = start_tan.height/start_tan.width;
            if(subpath) {
                first_slope = slope;
                subpath = false;
            }
             // fix up the join
            join_path_segments(offsetSegments, previous_slope, slope, previous_segment, previous_segment+1);
            previous_slope = tan.height/tan.width;
            previous_segment=offsetSegments.size()-1;
            c.setLocation(controlpts[degree]);
            subpath = false;
        } break;
           
        // Handle Close
        case RMPath.CLOSE :
            pathCoords[0] = first;
            if(RMMath.equals(c.x, first.x) && RMMath.equals(c.y, first.y)) {
                // A closepath for a closed segment doesn't add a lineto (which would be a point),
                // but it does mean that the last segment should be joined to the first.
                join_path_segments(offsetSegments,previous_slope,first_slope,offsetSegments.size()-1, 0);
                break;
            }
            // otherwise, fall through to line segment 
            
        // Handle LineTo
        case RMPath.LINE_TO :
            
            // Get normal
            normal.width = c.y-pathCoords[0].y;
            normal.height = pathCoords[0].x-c.x;
            
            // Offset path is undefined for a point - just skip it
            if(normal.width==0 && normal.height==0) { }
            
            // Do normal
            else {
                
                // having slope & normal is redundant, but it makes life easier
                double slope = -normal.width/normal.height;
                normal.normalize();
                double x = pathCoords[0].x + offset*normal.width;
                double y = pathCoords[0].y + offset*normal.height;
                RMLine line = new RMLine(c.x+offset*normal.width, c.y+offset*normal.height, x, y);
                offsetSegments.add(line);
                join_path_segments(offsetSegments,previous_slope,slope,offsetSegments.size()-2, offsetSegments.size()-1);
                
                // For a closepath, we have to fix up the start (done above) and then then end (bdone below)
                if(segment==RMPath.CLOSE)
                    join_path_segments(offsetSegments,slope,first_slope,offsetSegments.size()-1, 0);
                else if (subpath) {
                    first_slope=slope;
                    subpath=false;
                }
                previous_slope = slope;
                previous_segment=offsetSegments.size()-1;
            }
            c.setLocation(pathCoords[0]);
            break;
        }
    }

    // Do something
    if(!offsetSegments.isEmpty()) 
        outPath.addSegments(offsetSegments);
    
    // Return outpath
    return outPath;
}

/**
 *  Join two RMBeziers or RMLines from the list, preserving the slopes.
 *
 * Joining is done by determining if the segments intersect. If so, the control points are redifined so that new
 * curves (or lines) meet at the intersection point.
 *
 * If the curves do not intersect, straight line segments are added that match the slopes at the endpoints.  This
 * forms a mitre join between the segments. If these lines would be too great, a bevel join is formed instead.
 * Note that if the segment being fixed up is a line, it can just be extended, rather that having a new (colinear)
 * line segment added.
 *
 * 'before' is the index into the array of the segment whose endpoint is to be connected to segment at index 'after'.
 * While two paths are usually consecutive elements in the list, this is not the case for the 'closepath' case,
 * where the endpoint of the last element gets connected to the first.
 */
private static void join_path_segments(List segments, double slope1, double slope2, int before, int after)
{
    // If this is the first element in a subpath, no adjustments are needed
    if(segments.size()<=1 || before<0) return;
    
    // If the two slopes are equal, no fixup is needed (the normals would be equal, and therefore the offset
    // points would be equal). If the slopes are equal and the normals are in opposite directions, The path is
    // making a quick turnaround.  You could add an end cap in that case.
    if(RMMath.equals(slope1,slope2) || ((Double.isInfinite(slope1) && Double.isInfinite(slope2))))
        return;
    
    // Get current segment and previous segment
    RMLine seg = (RMLine)segments.get(after);
    RMLine previous_seg = (RMLine)segments.get(before);
    
    // There is a bug here.  The offset curve for a bezier is generated by the curve fit code,
    // and so it usually is made up of several bezier segments.  This code assumes that the intersection
    // would be with the first (or last) bezier segment, but it is possibile for the interection to occurr
    // at a segment in the middle, especially if the offset is large or the curve fit has generated many
    // small beziers.
    // If that were the case, you'd want to toss the earlier (or later) bezier segments and shorten the
    // hit one as below.  You probably don't want to do hit testing with every bezier segment, but you
    // may have to.

    RMHitInfo hi = hitInfo(previous_seg, seg);
    
    // no hit.  Append new lines along the slopes.
    if (hi==null) {
        RMPoint p1=previous_seg._ep;
        RMPoint p2=seg._sp;
        RMPoint new_intersect = new RMPoint();
        
        if (Double.isInfinite(slope2)) {
            new_intersect.y = slope1*(p2.x-p1.x) + p1.y;
            new_intersect.x = p2.x;
        }
        else {
            if (Double.isInfinite(slope1))
                new_intersect.x = p1.x;
            else
                new_intersect.x = (p1.y-p2.y+slope2*p2.x-slope1*p1.x)/(slope2-slope1);
            new_intersect.y = slope2*(new_intersect.x-p2.x) + p2.y;
        }

        //  Miter limit calculations
        //    The idea is to check the distance (or the angle) of the join and only extend the paths up to a certain
        //    distance, adding a third (bevel) linesegment to connect the two (shortened) extension segments.
        double dy = new_intersect.y-p1.y;
        double dx = new_intersect.x-p1.x;
        double max_extension = 8;
        double len=dy*dy+dx*dx;
        if (len > max_extension*max_extension) {
            //segments.add(before+1, new RMLine(p1,p2)); <<testing - just chop it off>>
            len = max_extension/Math.sqrt(len);
            RMPoint miter1 = new RMPoint(p1.x+dx*len, p1.y+dy*len);
            dy = new_intersect.y-p2.y;
            dx = new_intersect.x-p2.x;
            len=max_extension/Math.sqrt(dx*dx+dy*dy);
            RMPoint miter2 = new RMPoint(p2.x+dx*len, p2.y+dy*len);
            RMLine miterjoin = new RMLine(miter1,miter2);
            if (previous_seg instanceof RMBezier) {
                segments.add(before+1, new RMLine(p1,miter1));
                segments.add(before+2, miterjoin);
                segments.add(before+3, new RMLine(miter2,p2));
            }
            else {
                previous_seg._ep=miter1;
                segments.add(before+1,miterjoin);
                seg._sp=miter2;
            }
        }
        else {
            // segments that are lines are just extended.  
            // Beziers get these new lines to join their start & ends
            // Segments are never anything other than lines & beziers.
            if (previous_seg instanceof RMBezier) {
                segments.add(before+1, new RMLine(p1,new_intersect));
                segments.add(before+2, new RMLine(new_intersect,p2));
            }
            else {
                previous_seg._ep=new_intersect;
                seg._sp=new_intersect; // probably a really bad idea.  probably should make a new copy of the point
            }
        }
    }
    
    
    // Segments intersect - just reset the control points so the curves start and end at the intersection
    else {
        previous_seg.setEnd(hi._r);
        seg.setStart(hi._s);
    }
}

/**
 * Dispatches to the right call depending on the subclass.
 */
public static RMHitInfo hitInfo(RMLine l1, RMLine l2)
{
    // Handle bezier
    if(l2 instanceof RMBezier) 
        return l1.getHitInfo((RMBezier)l2);
    
    // Handle quadratic
    if(l2 instanceof RMQuadratic)
        return l1.getHitInfo((RMQuadratic)l2);
    
    // Handle line
    return l1.getHitInfo(l2);
}

/**
 * Simultaneously find point on curve, as well as the tangent at that point.
 */
public static void evaluateBezierAndTangent(int degree, double t, RMPoint cpts[], RMPoint tpoint, RMSize tan)
{
    // Handle start point.  If one (or more) of the control points is the same as an endpoint, the tangent
    // calculation in the de Casteljau algorithm will return a point instead of the real tangent.
    if(t==0) {
        tpoint.setLocation(cpts[0]);
        for(int i=1; i<=degree; ++i) 
            if(!cpts[i].equals(cpts[0])) {
                tan.width = cpts[i].x - cpts[0].x;
                tan.height = cpts[i].y - cpts[0].y;
                break;
            }
    }
    
    // Handle end point (same as above)
    else if(t==1) {
        tpoint.setLocation(cpts[degree]);
        for(int i=degree-1; i>=0; --i) 
            if(!cpts[i].equals(cpts[degree])) {
                tan.width = cpts[degree].x - cpts[i].x;
                tan.height = cpts[degree].y - cpts[i].y;
                break;
            }
    }
    
    // Handle intermediate points
    else {
        
        // Get float array of points
        double points[] = new double[2*(degree+1)];
        for(int i=0; i<=degree; i++) { points[i*2] = cpts[i].x; points[i*2+1] = cpts[i].y; }
        
        // Triangle computation
        for(int i=1; i<=degree; i++) {   
            if(i==degree) {
                tan.width = points[2] - points[0];
                tan.height = points[3] - points[1];
            }
            for(int j=0; j<=2*(degree-i)+1; j++)
                points[j] = (1 - t)*points[j] + t*points[j+2];
        }
        tpoint.x = points[0];
        tpoint.y = points[1];
    }
    
    // Normalize tangent
    tan.normalize();
}

}