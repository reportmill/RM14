package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.*;
import snap.util.*;

/**
 * This class is an RMShape subclass that encapsulates an arbitrary path. 
 */
public class RMPolygonShape extends RMParentShape {
    
    // The explicit path associated with this shape
    RMPath        _path;
    
/**
 * Creates a new empty polygon shape.
 */
public RMPolygonShape() { }

/**
 * Creates a new polygon shape for the given path.
 */
public RMPolygonShape(RMPath aPath)  { this(); _path = aPath; }

/**
 * Returns the path for this polygon shape.
 */
public RMPath getPath()  { return _path; }

/**
 * Sets the path for this polygon shape.
 */
public void setPath(RMPath aPath)  { _path = aPath; }

/**
 * Editor method - indicates that this shape can be super selected.
 */
public boolean superSelectable() { return true; }

/**
 * Handles painting a polygon shape.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Do normal shape painting
    super.paintShape(aPntr);
    
    // If not super selected, just return
    if(!aPntr.isSuperSelected(this)) return;
    
   // Get plygon path
    RMPath path = getPathInBounds();
    
    // Get selection point index. Bogus - we can't refer to tool in here! This should go in PolygonTool.
    int selectedPointIndex = com.reportmill.apptools.RMPolygonShapeTool._mouseDownPointIndex;
    
    // Declare some path iteration variables
    byte lastElement = -1;
    int currentPointIndex = 0;
    RMPoint pnts[] = new RMPoint[3];
    float HW = 6, HHW= HW/2;

    // Iterate over path segements
    for(int i=0; i<path.getElementCount(); i++) {
        
        // Get current loop segment point index
        int pointIndex = path.getElementPointIndex(i);
        
        // Get points
        pnts[0] = pointIndex < path.getPointCount()? path.getPoint(pointIndex++) : null;
        pnts[1] = pointIndex < path.getPointCount()? path.getPoint(pointIndex++) : null;
        pnts[2] = pointIndex < path.getPointCount()? path.getPoint(pointIndex++) : null;
        
        // Get segment type
        byte element = path.getElement(i);
        
        // Get next segment type
        byte nextElement = i+1<path.getElementCount()? path.getElement(i+1) : -1;

        // Set color black for control lines and so alpha is correct for buttons
        aPntr.setColor(Color.black);

        // Draw buttons for all segment endPoints
        switch(element) {

            // Handle MOVE_TO & LINE_TO: just draw button
            case RMPath.MOVE_TO:
            case RMPath.LINE_TO: {
                RMRect handleRect = new RMRect(pnts[0].x-HHW, pnts[0].y-HHW, HW, HW);
                aPntr.drawButton(handleRect, false);
                currentPointIndex++;
                break;
            }

            // Handle CURVE_TO: If selectedPointIndex is CurveTo, draw line to nearest endPoint and button
            case RMPath.CURVE_TO: {
                
                // Get handle rect
                RMRect handleRect = new RMRect(pnts[2].x-HHW, pnts[2].y-HHW, HW, HW);

                // If controlPoint1's point index is the selectedPointIndex or last end point was selectedPointIndex
                // or lastElement was a CurveTo and it's controlPoint2's pointIndex was the selectedPointIndex
                //   then draw control line from controlPoint1 to last end point and draw handle for control point 1
                if(currentPointIndex==selectedPointIndex || currentPointIndex-1==selectedPointIndex ||
                   (lastElement==RMPath.CURVE_TO && currentPointIndex-2==selectedPointIndex)) {
                    RMPoint lastPoint = path.getPoint(currentPointIndex-1);
                    aPntr.setStroke(RMAWTUtils.Stroke1);
                    aPntr.drawLine(pnts[0].getX(), pnts[0].getY(), lastPoint.getX(), lastPoint.getY());
                    aPntr.drawButton(pnts[0].x-HHW, pnts[0].y-HHW, HW, HW, false); // control pnt handle rect
                    aPntr.drawButton(lastPoint.x-HHW, lastPoint.y-HHW, HW, HW, false); // last pnt handle rect
                }

                // If controlPoint2's point index is selectedPointIndex or if end point's index is
                // selectedPointIndex or if next element is CurveTo and it's cp1 point index is
                // selectedPointIndex then draw control line from cp2 to end point and draw handle for cp2
                else if(currentPointIndex+1==selectedPointIndex || currentPointIndex+2==selectedPointIndex ||
                    (nextElement==RMPath.CURVE_TO && currentPointIndex+3==selectedPointIndex)) {
                    aPntr.setStroke(RMAWTUtils.Stroke1);
                    aPntr.drawLine(pnts[1].getX(), pnts[1].getY(), pnts[2].getX(), pnts[2].getY());
                    aPntr.drawButton(pnts[1].x-HHW, pnts[1].y-HHW, HW, HW, false);
                }

                // Draw button
                aPntr.drawButton(handleRect, false);
                currentPointIndex += 3;
                break;
            }

            // Break
            default: break;
        }

        // Remember last element
        lastElement = element;
    }
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("polygon");
    
    // Archive path
    e.add(_path.toXML(anArchiver));

    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive path
    XMLElement pathXML = anElement.get("path");
    _path = anArchiver.fromXML(pathXML, RMPath.class, this);
}

/**
 * Standard clone implementation.
 */
public RMPolygonShape clone()
{
    RMPolygonShape clone = (RMPolygonShape)super.clone();
    clone._path = RMUtils.clone(_path);
    return clone;
}

/**
 * Returns the matrix which transforms from path space to this shape's space (path space is arbitrarily defined,
 * and always gets scaled to fit exactly into getBoundsInside()).
 */
public RMTransform getPathTransform()
{
    RMRect pathRect = getPath().getBounds2D();
    RMRect shapeRect = getBoundsInside();
    double scalew = shapeRect.getWidth()/pathRect.getWidth();
    double scaleh = shapeRect.getHeight()/pathRect.getHeight();
    double tx = -pathRect.getX()*scalew;
    double ty = -pathRect.getY()*scaleh;
    return new RMTransform(scalew, 0f, 0f, scaleh, tx, ty);
}

/**
 * Replace the polygon's current path with a new path, adjusting the shape's bounds to match the new path.
 */
public void resetPath(RMPath newPath)
{
    // Get the transform from path space -> shape coords
    RMTransform pathXform = getPathTransform();
    
    // Get the transform from path space -> parent shape coords (make a clone so old one isn't clobbered)
    RMTransform pathToParent = ((RMTransform)pathXform.clone()).multiply(getTransform());  

    // set the new path
    setPath(newPath);
        
    // Readjust shape size & position for new path: first, get the new path's bounds
    RMRect newPathBounds = newPath.getBounds2D();
        
    // transform to shape coords for new width & height
    RMRect boundsInShape = pathXform.transform(newPathBounds.clone());
    setSize(boundsInShape.getWidth(), boundsInShape.getHeight());
        
    // transform to parent for new x & y
    RMRect boundsInParent = pathToParent.transform(newPathBounds.clone());
    setFrameXY(boundsInParent.getOrigin());
}

}