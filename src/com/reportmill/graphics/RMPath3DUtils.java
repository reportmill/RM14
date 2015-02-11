package com.reportmill.graphics;
import com.reportmill.base.RMPoint;
import java.util.*;

/**
 * Helper methods for the RMPath3D class.
 */
public class RMPath3DUtils {

/**
 * Creates and returns a list of paths in 3D for a given 2D path and extrusion.
 */
public static List <RMPath3D> getPaths(RMPath aPath, float z1, float z2)  { return getPaths(aPath,z1,z2,0); }

/**
 * Creates and returns a list of paths in 3D for a given 2D path and extrusion. 
 * Also can take into account the width of a stroke applied to the side (extrusion) panels.
 */
public static List <RMPath3D> getPaths(RMPath aPath, double z1, double z2, double strokeWidth)
{
    // Create list to hold paths
    List <RMPath3D> paths = new ArrayList();

    // Declare local variable for back face
    RMPath3D back = null;
    
    // If path is closed, create path3d for front from aPath and z1
    if(aPath.isClosed()) {
        
        // Create path3d for front and back
        RMPath3D front = new RMPath3D(aPath, z1);
        back = new RMPath3D(aPath, z2);
        
        // Add front to paths list
        paths.add(front);
    
        // If front is pointing wrong way, reverse it
        if(front.getNormal().isAway(new RMVector3D(0, 0, -1), true))
            front.reverse();
        
        // Otherwise, reverse back
        else {
            back.reverse();
            aPath = back.getPath();
        }
    }
    
    // Declare iteration variables
    RMPoint points[] = new RMPoint[3];
    RMPoint lastPoint = null;
    RMPoint lastMove = null;

    // Make room for path stroke
    z1 += strokeWidth;
    z2 -= strokeWidth;
    
    // Iterate over path elements
    for(int i=0, iMax=aPath.getElementCount(); i<iMax; i++) {
        
        // Get current loop element type
        int type = aPath.getElement(i, points);
        
        // Handle types
        switch(type) {

            // Move to
            case RMPath3D.MOVE_TO:
                lastPoint = lastMove = points[0];
                break;
            
            // Line to
            case RMPath3D.LINE_TO: {
                //skip over points (NB: RMPoint.equals() does a fp ==)
                if (!lastPoint.equals(points[0])) {
                    RMPath3D path = new RMPath3D();
                    path.moveTo(lastPoint.x, lastPoint.y, z1);
                    path.lineTo(points[0].x, points[0].y, z1);
                    path.lineTo(points[0].x, points[0].y, z2);
                    path.lineTo(lastPoint.x, lastPoint.y, z2);
                    path.close();
                    double x = lastPoint.x + (points[0].x - lastPoint.x)/2;
                    double y = lastPoint.y + (points[0].y - lastPoint.y)/2;
                    path.setCenter(new RMPoint3D(x, y, z2/2));
                    paths.add(path);
                    lastPoint = points[0];
                }
            } break;
            
            // Quad-to
            case RMPath3D.QUAD_TO: {
                RMPath3D path = new RMPath3D();
                path.moveTo(lastPoint.x, lastPoint.y, z1);
                path.quadTo(points[0].x, points[0].y, z1, points[1].x, points[1].y, z1);
                path.lineTo(points[2].x, points[2].y, z2);
                path.quadTo(points[0].x, points[0].y, z2, lastPoint.x, lastPoint.y, z2);
                path.close();
                double x = lastPoint.x + (points[1].x - lastPoint.x)/2;
                double y = lastPoint.y + (points[1].y - lastPoint.y)/2;
                path.setCenter(new RMPoint3D(x, y, z2/2));
                paths.add(path);
                lastPoint = points[1];
            } break;
            
            // Curve-to
            case RMPath3D.CURVE_TO: {
                RMPath3D path = new RMPath3D();
                path.moveTo(lastPoint.x, lastPoint.y, z1);
                path.curveTo(points[0].x, points[0].y, z1, points[1].x, points[1].y, z1, points[2].x, points[2].y, z1);
                path.lineTo(points[2].x, points[2].y, z2);
                path.curveTo(points[1].x, points[1].y, z2, points[0].x, points[0].y, z2,
                        lastPoint.x, lastPoint.y, z2);
                path.close();
                double x = lastPoint.x + (points[2].x - lastPoint.x)/2;
                double y = lastPoint.y + (points[2].y - lastPoint.y)/2;
                path.setCenter(new RMPoint3D(x, y, z2/2));
                paths.add(path);
                lastPoint = points[2];
            } break;
            
            // Close
            case RMPath3D.CLOSE: {
                RMPath3D path = new RMPath3D();
                path.moveTo(lastPoint.x, lastPoint.y, z1);
                path.lineTo(lastMove.x, lastMove.y, z1);
                path.lineTo(lastMove.x, lastMove.y, z2);
                path.lineTo(lastPoint.x, lastPoint.y, z2);
                path.close();
                double x = lastPoint.x + (lastMove.x - lastPoint.x)/2;
                double y = lastPoint.y + (lastMove.y - lastPoint.y)/2;
                path.setCenter(new RMPoint3D(x, y, z2/2));
                paths.add(path);
            } break;
        }
    }
    
    // Add back face to paths
    if(back != null)
        paths.add(back);
    
    // Return paths
    return paths;
}

}