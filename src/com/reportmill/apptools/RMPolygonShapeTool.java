package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * This class manages creation and editing of polygon shapes.
 */
public class RMPolygonShapeTool <T extends RMPolygonShape> extends RMTool <T> {
    
    // The current path being added
    RMPath       _path;
    
    // Whether path should be smoothed on mouse up
    boolean      _smoothPathOnMouseUp;
    
    // Used to determine which path element to start smoothing from
    int          _pointCountOnMouseDown;
    
    // The point (in path coords) for new control point additions
    RMPoint      _newPoint;
    
    // The path point handle hit by current mouse down
    static public int   _mouseDownPointIndex = 0;

/**
 * Override to return empty panel.
 */
protected JComponent createUI()  { return new SpringsPane(); }

/**
 * Handles the pop-up menu
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle DeletePointMenuItem
    if(anEvent.equals("DeletePointMenuItem"))
        deleteSelectedPoint();
    
    // Handle AddPointMenuItem
    if(anEvent.equals("AddPointMenuItem"))
        addNewPoint();
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMPolygonShape.class; }

/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { T shape = super.newInstance(); shape.setStroke(new RMStroke()); return shape; }

/**
 * Returns whether tool should smooth path segments during creation.
 */
public boolean getSmoothPathOnMouseUp()  { return Swing.isAltDown(); }

/**
 * Handles mouse pressed for polygon creation.
 */
public void mousePressed(MouseEvent anEvent)
{
    RMPoint point = getEditor().getEditorInputAdapter().getEventPointInDoc(!getSmoothPathOnMouseUp());

    // Register all selectedShapes dirty because their handles will probably need to be wiped out
    RMShapeUtils.repaint(getEditor().getSelectedShapes());

    // If this is the first mouseDown of a new path, create path and add moveTo. Otherwise add lineTo to current path
    if(_path==null) {
        _path = new RMPath();
        _path.moveTo(point);
    }

    else _path.lineTo(point);    

    // Get the value of _shouldSmoothPathOnMouseUp for the mouseDrag and store current pointCount
    _smoothPathOnMouseUp = getSmoothPathOnMouseUp();
    _pointCountOnMouseDown = _path.getPointCount();

    RMRect rect = _path.getBounds2D().insetRect(-10, -10);
    rect = getEditor().convertRectFromShape(rect, null);
    getEditor().repaint(rect);
}

/**
 * Handles mouse dragged for polygon creation.
 */
public void mouseDragged(MouseEvent anEvent)
{
    RMPoint point = getEditor().getEditorInputAdapter().getEventPointInDoc(!_smoothPathOnMouseUp);
    RMRect rect = _path.getBounds2D();

    if(_smoothPathOnMouseUp || _path.getPointCount()==1)
        _path.lineTo(point);
    else _path.setPoint(_path.getPointCount()-1, point);

    rect.union(_path.getBounds2D());
    rect.inset(-10, -10);
    rect = getEditor().convertRectFromShape(rect, null);
    getEditor().repaint(rect);
}

/**
 * Handles mouse released for polygon creation.
 */
public void mouseReleased(MouseEvent anEvent)
{
    if(_smoothPathOnMouseUp && _pointCountOnMouseDown<_path.getPointCount()) {
        getEditor().repaint();
        RMPathFitCurves.fitCurveFromPointIndex(_path, _pointCountOnMouseDown);
    }

    // Check to see if point landed in first point
    if(_path.getPointCount() > 2) {
        byte lastElement = _path.getElementLast();
        int lastPointIndex = _path.getPointCount() - (lastElement==RMPath.LINE_TO? 2 : 4);
        RMPoint beginPoint = _path.getPoint(0);
        RMPoint lastPoint = _path.getPoint(lastPointIndex);
        RMPoint thisPoint = _path.getPointLast();
        RMRect firstHandleRect = new RMRect(beginPoint.x - 3, beginPoint.y - 3, 6f, 6f);
        RMRect lastHandleRect = new RMRect(lastPoint.x - 3, lastPoint.y - 3, 6f, 6f);
        RMRect currentHandleRect = new RMRect(thisPoint.x - 3, thisPoint.y - 3, 6f, 6f);
        boolean createPath = false;

        // If mouseUp is in startPoint, create poly and surrender to selectTool
        if(currentHandleRect.intersectsRect(firstHandleRect)) {
            if(lastElement == RMPath.LINE_TO)
                _path.removeLastElement();
            _path.closePath();
            createPath = true;
        }

        // If mouseUp is in startPoint, create poly and surrender to selectTool
        if(currentHandleRect.intersectsRect(lastHandleRect)) {
            if(_path.getElementLast() == RMPath.LINE_TO)
                _path.removeLastElement();
            createPath = true;
        }
        
        // Create poly, register for redisplay and surrender to selectTool
        if(createPath) {
            createPoly();
            getEditor().repaint();
            getEditor().setCurrentToolToSelectTool();
        }
    }
}

/**
 * Event handling - overridden to maintain default cursor.
 */
public void mouseMoved(T aPolygon, MouseEvent anEvent)
{
    // Get the mouse down point in shape coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aPolygon);
    RMSize size = getPathHandleSize(aPolygon);
    
    // If control point is hit, change cursor to move
    if(aPolygon.getPath().handleAtPointForBounds(point, aPolygon.getBoundsInside(), _mouseDownPointIndex, size)>=0) {
        
        // Set cursor
        if(getEditor().getCursor()!=Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
            getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        
        // Consume event
        anEvent.consume();
    }
    
    // Otherwise, do normal mouse moved
    else super.mouseMoved(aPolygon, anEvent);
}

/**
 * Event handling for shape editing.
 */
public void mousePressed(T aPolygon, MouseEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aPolygon)) return;
    
    // Get mouse down point in shape coords (but don't snap to the grid)
    RMPoint point = getEditor().getEditorInputAdapter().getEventPointInShape(false);
    
    // Register shape for repaint
    aPolygon.repaint();
    
    // check for degenerate path
    if(aPolygon.getPath().getPointCount() < 2) 
        _mouseDownPointIndex = -1;
    
    // Otherwise, figure out the size of a handle in path coordinates and set index of path point hit by mouse down
    else {
        RMSize handles = getPathHandleSize(aPolygon);
        int oldSelectedPt = _mouseDownPointIndex;
        int hp = aPolygon.getPath().handleAtPointForBounds(point, aPolygon.getBoundsInside(), oldSelectedPt, handles);
        _mouseDownPointIndex = hp;
    
        if(anEvent.isPopupTrigger())
            runContextMenu(aPolygon, anEvent);
    }
    
    // Consume event
    anEvent.consume();
}

/**
 * Event handling for shape editing.
 */
public void mouseDragged(T aPolygon, MouseEvent anEvent)
{
    aPolygon.repaint();
    if(_mouseDownPointIndex>=0) {
        RMPoint point = getEditor().getEditorInputAdapter().getEventPointInShape(true);
        RMPath path = aPolygon.getPath();
        point = path.pointInPathCoordsFromPoint(point, aPolygon.getBoundsInside());
        
        // Clone path, move control point & do all the other path funny business, reset path
        RMPath newPath = path.clone();
        newPath.setPointStructured(_mouseDownPointIndex, point);
        aPolygon.resetPath(newPath);
    } 
}

/**
 * Actually creates a new polygon shape from the polygon tool's current path.
 */
private void createPoly()
{
    if(_path!=null && _path.getPointCount()>2) {
        RMPolygonShape poly = new RMPolygonShape();
        RMRect polyFrame = getEditor().getSuperSelectedShape().convertedRectFromShape(_path.getBounds2D(), null);
        poly.setFrame(polyFrame);
        poly.setStroke(new RMStroke());
        poly.setPath(_path);

        // Add shape to superSelectedShape (within an undo grouping).
        getEditor().undoerSetUndoTitle("Add Polygon");
        getEditor().getSuperSelectedParentShape().addChild(poly);

        // Select Shape
        getEditor().setSelectedShape(poly);
    }

    // Reset path
    _path = null;
}

/**
 * Overrides standard tool method to trigger polygon creation when the tool is deactivated.
 */
public void deactivateTool()  { createPoly(); }

/**
 * Overrides standard tool method to trigger polygon creation when the tool is reactivated.
 */
public void reactivateTool()  { createPoly(); }

/**
 * Editor method - called when an instance of this tool's shape in de-super-selected.
 */
public void willLoseSuperSelectionInEditor(RMShape aShape, RMEditor anEditor)
{
    super.willLoseSuperSelectionInEditor(aShape, anEditor);
    _mouseDownPointIndex = -1;
}

/**
 * Draws the polygon tool's path durring path creation.
 */
public void paintTool(Graphics2D g)
{
    if(_path!=null) {
        RMRect pageBounds = getEditor().getPageBounds();
        g.translate(pageBounds.x, pageBounds.y);
        g.scale(getEditor().getZoomFactor(), getEditor().getZoomFactor());
        g.setColor(Color.black);
        g.setStroke(RMAWTUtils.Stroke1);
        g.draw(_path);
        g.scale(1/getEditor().getZoomFactor(), 1/getEditor().getZoomFactor());
        g.translate(-pageBounds.x, -pageBounds.y);
    }
}

/**
 * Returns the bounds for this shape when it's super-selected.
 */
public RMRect getBoundsSuperSelected(RMShape aShape) 
{
    // Get shape bounds
    RMRect bounds = aShape.getBoundsInside();
    
    // Get shape path bounds
    RMRect pathBounds = aShape.getPath().getBounds2D();

    // Get 
    double mx1 = pathBounds.getMidX();
    double my1 = pathBounds.getMidY();
    double mx2 = bounds.getMidX();
    double my2 = bounds.getMidY();
    double sx = pathBounds.width==0? 1f : bounds.width/pathBounds.width;
    double sy = pathBounds.height==0? 1f : bounds.height/pathBounds.height;

    // Scale pathSSBounds.origin by sx and sy and translate it to the bounding rect's origin
    RMRect pathSSBounds = getControlPointBounds(aShape);
    double x = (pathSSBounds.x-mx1)*sx + mx2;
    double y = (pathSSBounds.y-my1)*sy + my2;
    double w = bounds.width*pathSSBounds.width/pathBounds.width;
    double h = bounds.height*pathSSBounds.height/pathBounds.height;
    
    // Get super selected bounds
    RMRect ssBounds = new RMRect(x, y, w, h);
    
    // Outset?
    ssBounds.inset(-3, -3);
    
    // Return super selected bounds
    return ssBounds;
}

/**
 * Returns the bounds for all the control points.
 */
private RMRect getControlPointBounds(RMShape aShape)
{
    // Get shape path
    RMPath path = aShape.getPath();
    
    // Get element index for selected control point handle
    int mouseDownElementIndex = path.getElementIndexForPointIndex(_mouseDownPointIndex);

    if((mouseDownElementIndex >= 0) &&
        (path.getElement(mouseDownElementIndex) == RMPath.CURVE_TO) &&
        (path.getElementPointIndex(mouseDownElementIndex) == _mouseDownPointIndex))
        mouseDownElementIndex--;

    RMPoint p1 = path.getPointCount()>0? new RMPoint(path.getPoint(0)) : new RMPoint();
    RMPoint p2 = path.getPointCount()>0? new RMPoint(path.getPoint(0)) : new RMPoint();
    RMPoint points[] = new RMPoint[3];
    
    // Iterate over path elements
    for(int i=1, iMax=path.getElementCount(); i<iMax; i++) {
        
        // Get current loop element
        byte type = path.getElement(i, points);

        // Handle different element types
        switch(type) {
        
            // Handle MOVE_TO
            case RMPath.MOVE_TO:
                
            // Handle LINE_TO
            case RMPath.LINE_TO: {
                p1.x = Math.min(p1.x, points[0].x); p1.y = Math.min(p1.y, points[0].y);
                p2.x = Math.max(p2.x, points[0].x); p2.y = Math.max(p2.y, points[0].y);
            } break;
            
            // Handle CURVE_TO
            case RMPath.CURVE_TO: {
                if((i-1)==mouseDownElementIndex) {
                    p1.x = Math.min(p1.x, points[0].x); p1.y = Math.min(p1.y, points[0].y);
                    p2.x = Math.max(p2.x, points[0].x); p2.y = Math.max(p2.y, points[0].y);
                }
                if(i==mouseDownElementIndex) {
                    p1.x = Math.min(p1.x, points[1].x); p1.y = Math.min(p1.y, points[1].y);
                    p2.x = Math.max(p2.x, points[1].x); p2.y = Math.max(p2.y, points[1].y);
                }
                p1.x = Math.min(p1.x, points[2].x); p1.y = Math.min(p1.y, points[2].y);
                p2.x = Math.max(p2.x, points[2].x); p2.y = Math.max(p2.y, points[2].y);
            } break;
            
            // Handle default
            default: break;
        }
    }
    
    // Create control point bounds rect
    RMRect controlPointBounds = new RMRect(p1.x, p1.y, Math.max(1, p2.x - p1.x), Math.max(1, p2.y - p1.y));
    
    // Union with path bounds
    controlPointBounds.union(path.getBounds2D());
    
    // Return control point bounds rect
    return controlPointBounds;
}

/**
 * Runs a context menu for the given event.
 */
public void runContextMenu(RMPolygonShape aPolyShape, MouseEvent anEvent)
{
    // Get the handle that was clicked on
    RMPath path = aPolyShape.getPath();
    int pindex = _mouseDownPointIndex;
    String mtitle = null, mname = null;
    
    // If clicked on a valid handle, add 'delete point' to menu, 
    if(pindex>=0) {
        if(path.pointOnPath(pindex)) { // Only on-path points can be deleted
            mtitle = "Delete Anchor Point"; mname ="DeletePointMenuItem"; }
    }
    
    // Otherwise if the path itself was hit, use 'add point'
    else {
        // get shape->path transform and convert event point to path coords
        RMTransform pathXform = aPolyShape.getPathTransform().invert();
        _newPoint = pathXform.transform(getEditor().convertPointToShape(anEvent.getPoint(), aPolyShape));
        
        // linewidth is probably in shape coords, and might need to get transformed to path coords here
        if(path.intersects(_newPoint, aPolyShape.getStrokeWidth()+4)) {
            mtitle = "Add Anchor Point"; mname = "AddPointMenuItem"; }
    }
    
    // return if there's nothing to be done
    if(mname==null) return;
    
    // Create new PopupMenu
    JPopupMenu pmenu = new JPopupMenu();
    JMenuItem mitem = new JMenuItem(mtitle); mitem.setName(mname); pmenu.add(mitem);
    initUI(pmenu);
    pmenu.show(anEvent.getComponent(), anEvent.getX(), anEvent.getY());
}

/**
 * Delete the selected control point and readjust shape bounds
 */
public void deleteSelectedPoint()
{
    RMPolygonShape p = getSelectedShape();
    
    // Make changes to a clone of the path so deletions can be undone
    RMPath path = p.getPath().clone();

    // get the index of the path element corresponding to the selected control point
    int elementIndex = path.getElementIndexForPointIndex(_mouseDownPointIndex);

    // mark for repaint & undo
    p.repaint();

    // delete the point from path in parent coords
    path.removeElement(elementIndex);

    // if all points have been removed, delete the shape itself
    if (path.getElementCount()==0) {
        getEditor().undoerSetUndoTitle("Delete Shape");
        p.getParent().repaint();
        p.removeFromParent();
        getEditor().setSelectedShape(null);
    }
    
    // otherwise update path and bounds and deselect the deleted point
    else {
        getEditor().undoerSetUndoTitle("Delete Control Point");
        p.resetPath(path);
        _mouseDownPointIndex = -1;
    }

}

/**
 * Add a point to the curve by subdividing the path segment at the hit point.
 */
public void addNewPoint()
{
    RMPolygonShape polygon = getSelectedShape();
    
    // Get all the segments as a list of subpaths
    List <List <? extends RMLine>> subpaths = polygon.getPath().getSubpathsSegments();
    
    // Find hitInfo of segment by intersecting with either horizontal or vertial line segment
    RMLine hor = new RMLine(_newPoint.x-2, _newPoint.y, _newPoint.x+2, _newPoint.y);
    RMLine vert = new RMLine(_newPoint.x, _newPoint.y-2, _newPoint.x, _newPoint.y+2);
    
    // Iterate over subpaths
    for(int i=0, iMax=subpaths.size(); i<iMax; i++) { List <? extends RMLine> subpath = subpaths.get(i);
    
        // Iterate over subpath segments
        for(int j=0, jMax=subpath.size(); j<jMax; j++) { RMLine segment = subpath.get(j);
        
            // Get hit info for segment
            RMHitInfo hit = segment.getHitInfo(hor);
            if (hit==null)
                hit = segment.getHitInfo(vert);
            
            // If hit found, subdivide segment at hit point and create new path
            if(hit != null) {
                
                // get parametric hit point for segment
                double hitPoint = hit.getR();
                
                // readjust the hit segment's endpoint
                RMLine tailSeg = segment.clone();
                segment.setEnd(hitPoint);
                
                // Set the start of the new tail to the hit point & insert into the list
                tailSeg.setStart(hitPoint);
                ((List)subpath).add(j+1, tailSeg);

                // Create new path and add subpaths
                RMPath newPath = new RMPath();
                for(int k=0, kMax=subpaths.size(); k<kMax; k++)
                    newPath.addSegments(subpaths.get(k));
                
                polygon.repaint();
                polygon.resetPath(newPath); //p._mouseDownPointIndex = ??
                return;
            }
        }
    }
}

/**
 * For hit testing - returns the size of the handles scaled to path coordinates
 */
public RMSize getPathHandleSize(RMPolygonShape aPoly)
{
    // Get the transform from shape coords -> path space
    RMTransform shapeToPath = aPoly.getPathTransform().invert();
    
    // RMEditorShapePainter draws 6x6 handles, and we add a litle for slop
    return shapeToPath.transform(new RMSize(9,9));
}

/**
 * This inner class defines a polygon tool subclass for drawing freehand pencil sketches instead.
 */
public static class PencilTool extends RMPolygonShapeTool {

    /** Overrides polygon tool method to flip default smoothing. */
    public boolean getSmoothPathOnMouseUp()  { return !Swing.isAltDown(); }
}

}