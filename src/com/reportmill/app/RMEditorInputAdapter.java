package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.viewer.*;
import java.awt.*;
import java.awt.event.*;
import snap.swing.Swing;

/**
 * Handles editor methods specific to event operations.
 */
public class RMEditorInputAdapter extends RMViewerInputAdapterImpl {
    
    // The cached current event for any mouse loop handled by this editor events
    MouseEvent   _currentEvent;
    
    // The down point for any mouse loop handled by this editor events
    RMPoint      _downPoint;
    
    // Whether to override editor preview mode
    boolean      _overridePreview;
    
    // Constants for guide orientation
    private static final byte GUIDE_HORIZONTAL = 0;
    private static final byte GUIDE_VERTICAL = 1;

/**
 * Creates a new editor events object.
 */
public RMEditorInputAdapter(RMViewer aViewer)  { super(aViewer); }

/**
 * Returns the viewer as an editor.
 */
public RMEditor getEditor()  { return (RMEditor)getViewer(); }

/**
 * Handle mouse pressed.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // If in preview mode, call viewer input adapter version
    if(editor.isPreview() && !getOverridePreview()) { super.mousePressed(anEvent); return; }
    
    // Turn on value is adjusting for mouse loop
    editor._valueIsAdjusting = true;
    
    // Request focus for editor
    editor.requestFocusInWindow();
    
    // Cache current event
    _currentEvent = anEvent;
    
    // Set downpoint and last point to current event point in document coords
    _downPoint = editor.convertPointToShape(anEvent.getPoint(), null);

    // If current tool isn't select tool, see if super selected shape needs to be updated
    if(editor.getCurrentTool() != RMTool.getSelectTool()) {
        RMShape shape = editor.firstSuperSelectedShapeThatAcceptsChildrenAtPoint(_downPoint);
        if(shape!=editor.getSuperSelectedShape())
            editor.setSuperSelectedShape(shape);
    }

    // Forward mouse pressed to current tool
    editor.getCurrentTool().mousePressed(anEvent);
}

/**
 * Handle mouse dragged.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // If in preview mode, call viewer input adapter version
    if(editor.isPreview() && !getOverridePreview()) { super.mouseDragged(anEvent); return; }
    
    // If drag event is within 10 pts or 1/100 sec of down event, skip it so user clicks don't annoyingly move things
    if(_currentEvent!=null && _currentEvent.getID()==MouseEvent.MOUSE_PRESSED)
        if(Math.abs(anEvent.getX()-_currentEvent.getX())<10 && Math.abs(anEvent.getY()-_currentEvent.getY())<10)
            if(anEvent.getWhen()-_currentEvent.getWhen()<125)
                return;

    // Cache current event
    _currentEvent = anEvent;
    
    // Forward mouse dragged to current tool
    editor.getCurrentTool().mouseDragged(anEvent);
    
    // Autoscroll
    editor.scrollRectToVisible(new Rectangle(anEvent.getX(), anEvent.getY(), 1, 1));
}

/**
 * Handle mouse released.
 */
public void mouseReleased(MouseEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // If in preview mode, call viewer input adapter version
    if(editor.isPreview() && !getOverridePreview()) { super.mouseReleased(anEvent); return; }
    
    // Turn off value is adjusting for mouse loop
    editor._valueIsAdjusting = false;
    
    // Cache current event
    _currentEvent = anEvent;
    
    // Forward mouse released to current tool
    editor.getCurrentTool().mouseReleased(anEvent);
    
    // Reset current event
    _currentEvent = null;
}

/**
 * Handle mouse moved event.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // If in preview mode, call viewer input adapter version
    if(editor.isPreview() && !getOverridePreview()) { super.mouseMoved(anEvent); return; }
    
    // Otherwise, call tool mouseMoved to do stuff like set cursors
    else if(RMEditor.getMainEditor()==editor)
        editor.getCurrentTool().mouseMoved(anEvent);
}

/**
 * Handle key released.
 */
public void keyReleased(KeyEvent anEvent)
{
    // If in preview mode, call viewer input adapter version
    if(getEditor().isPreview() && !getOverridePreview()) { super.keyReleased(anEvent); return; }
}

/**
 * Handle key pressed.
 */
public void keyPressed(KeyEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // If in preview mode, call viewer input adapter version
    if(editor.isPreview() && !getOverridePreview()) {
        
        // Do normal version
        super.keyPressed(anEvent);
        
        // If event not consumed and user hit x, turn on preview override
        if(!anEvent.isConsumed() && anEvent.getKeyCode()==KeyEvent.VK_X)
            setOverridePreview(true);
        return; // Return
    }
    
    // If event is command key, just return
    if(RMAWTUtils.isCommandDown(anEvent)) return;
    
    // Get key code & key char
    int keyCode = anEvent.getKeyCode();
    char keyChar = anEvent.getKeyChar();
    
    // Handle escape (assuming mouse isn't down)
    if(keyCode==KeyEvent.VK_ESCAPE && !Swing.isMouseDown())
        editor.popSelection();
    
    // Handle backspace or delete key
    else if(keyCode==KeyEvent.VK_BACK_SPACE || keyCode==KeyEvent.VK_DELETE)
        editor.delete();
    
    // Handle left, right, up, down arrows
    else if(keyCode==KeyEvent.VK_LEFT) RMEditorShapes.moveLeftOnePoint(editor);
    else if(keyCode==KeyEvent.VK_RIGHT) RMEditorShapes.moveRightOnePoint(editor);
    else if(keyCode==KeyEvent.VK_UP) RMEditorShapes.moveUpOnePoint(editor);
    else if(keyCode==KeyEvent.VK_DOWN) RMEditorShapes.moveDownOnePoint(editor);

    // If 6 key, show Undo inspector (for undo debugging)
    else if(keyChar=='6')
        editor.getEditorPane().getInspectorPanel().setVisible(6);
    
    // If 8 key, show Animation inspector
    else if(keyChar=='8')
        editor.getEditorPane().getInspectorPanel().setVisible(8);
    
    // If T key, swap in linked text
    else if(keyChar=='t')
        RMTextTool.convertToText(editor.getSelectedShape(), "test");
    
    // Otherwise, set consume to false
    else return;
    
    // Consume event
    anEvent.consume();
}

/**
 * Handle key pressed.
 */
public void keyTyped(KeyEvent anEvent)
{
    // If in preview mode, call viewer input adapter version
    if(getEditor().isPreview() && !getOverridePreview()) { super.keyTyped(anEvent); return; }
}

/**
 * Handles key press events.
 */
public void processKeyEvent(KeyEvent anEvent)
{
    // If editing, send event to tool: Get super selected shape and its tool and send event
    if(!getEditor().isPreview() || getOverridePreview()) {
        RMShape superSelectedShape = getEditor().getSuperSelectedShape();
        RMTool tool = RMTool.getTool(superSelectedShape);
        tool.processKeyEvent(superSelectedShape, anEvent);
    }
    
    // If event not consumed, do normal version
    if(!anEvent.isConsumed())
        super.processKeyEvent(anEvent);
}

/**
 * Returns the current event.
 */
public MouseEvent getCurrentEvent()  { return _currentEvent; }

/**
 * Returns the current event point in document coords.
 */
public RMPoint getEventPointInDoc()  { return getEventPointInDoc(false); }

/**
 * Returns the current event point in document coords with an option to adjust to conform to grid.
 */
public RMPoint getEventPointInDoc(boolean snapToGrid)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // Get current event point in document coords
    RMPoint point = editor.convertPointToShape(_currentEvent.getPoint(), null);
    
    // Round point to integers
    point.x = Math.round(point.x);
    point.y = Math.round(point.y);
    
    // If shift key is down, constrain values to increments of 45 degrees from _downPoint
    if(_currentEvent.isShiftDown() && !editor.isCurrentToolSelectToolAndSelecting()) {

        // Get absolute values of delta X and delta Y relative to mouseDown point
        double absX = Math.abs(point.x - _downPoint.x), absY = Math.abs(point.y - _downPoint.y);

        // If X is greater than Y set Y to either X or zero
        if(absX > absY) {
            // If X is twice as big as Y or more, set Y to 0, If X is less than twice as big as Y, set Y to X
            if(absX > 2*absY) point.y = _downPoint.y;
            else point.y = _downPoint.y + RMMath.sign(point.y - _downPoint.y)*absX;
        }

        // If Y is greater than X, set X to either Y or zero
        else {
            // If X is twice as big as Y or more, set Y to 0, If X is less than twice as big as Y, set Y to X
            if(absY > 2*absX) point.x = _downPoint.x;
            else point.x = _downPoint.x + RMMath.sign(point.x - _downPoint.x)*absY;
        }
    }
    
    // If requested point snapped to grid, adjust point for grid
    if(snapToGrid)
        point = pointSnapped(point, false);
    
    // Return point
    return point;
}

/**
 * Returns the current event point in super-selected shape coords, optionally snapped to grid.
 */
public RMPoint getEventPointInShape(boolean snapToGrid)  { return getEventPointInShape(snapToGrid, false); }

/**
 * Returns the current event point in super-selected shape coords with an option to adjust to conform to grid.
 */
public RMPoint getEventPointInShape(boolean snapToGrid, boolean snapEdges)
{
    // Get event point in doc coords
    RMPoint point = getEventPointInDoc();
    
    // If requested point snapped to grid, adjust point for grid
    if(snapToGrid)
        point = pointSnapped(point, snapEdges);
    
    // Return point converted to super selected point
    return getEditor().getSuperSelectedShape().convertedPointFromShape(point, null);
}

/**
 * Returns the given point corrected for grids and guides.
 */
private RMPoint pointSnapped(RMPoint aPoint, boolean snapEdges)
{
    // Get the editor and editor shape
    RMEditor editor = getEditor();
    RMDocument doc = editor.getDocument(); if(doc==null) return aPoint;
    
    // Get local copy of point
    RMPoint point = aPoint;
    double x = point.getX(), y = point.getY();

    // If doc snaps to grid, adjust for snap 
    if(doc.getSnapGrid())
        point = pointSnappedToGrid(point, snapEdges);
    
    // If doc has guides, adjust for guides
    else if(getGuideCount(doc)>0)
        point = pointSnappedToGuides(point, snapEdges);
    
    // If points haven't changed, adjust for proximity guides
    if(x==point.x && y==point.y)
        point = RMEditorProxGuide.pointSnappedToProximityGuides(editor, point);
    
    // Return point
    return point;
}

/**
 * Returns a given point adjusted for grids & guides.
 */
private RMPoint pointSnappedToGrid(RMPoint aPoint, boolean snapEdges)
{
    // Get the editor and editor shape
    RMEditor editor = getEditor();
    RMDocument doc = editor.getDocument(); if(doc==null) return aPoint;
    
    // Get document frame
    RMRect docFrame = editor.convertRectFromShape(editor.getSelectedPage().getBoundsInside(), editor.getSelectedPage());
    
    // Get grid spacing
    double gridSpacing = doc.getGridSpacing()*editor.getZoomFactor();
    
    // Get dx/dy for maximum offsets
    double dx = gridSpacing/2 + .001f;
    double dy = dx;
    
    // If not snapping to all edges, round aPoint to nearest grid or guide
    if(!snapEdges) {
        
        // Get point in editor coords
        aPoint = editor.convertPointFromShape(aPoint, null);
    
        // Get dx/dy to nearest grid
        double px = RMMath.round(aPoint.x - docFrame.x, gridSpacing) + docFrame.x;
        double py = RMMath.round(aPoint.y - docFrame.y, gridSpacing) + docFrame.y;
        dx = px - aPoint.x;
        dy = py - aPoint.y;
    }
    
    // If _snapEdges, find dx/dy for all edges of selected shapes to nearest grid or guide
    else {
    
        // Iterate over selected shapes
        for(int i=0, iMax=editor.getSelectedShapeCount(); i<iMax; i++) { RMShape shape = editor.getSelectedShape(i);
            
            // Get shape bounds in editor coords
            RMRect rect = editor.convertRectFromShape(shape.getBoundsInside(), shape);
            
            // Find dx/dy to nearest grid
            double px = RMMath.round(rect.x - docFrame.x, gridSpacing) + docFrame.x;
            double py = RMMath.round(rect.y - docFrame.y, gridSpacing) + docFrame.y;
            double pmx = RMMath.round(rect.getMaxX() - docFrame.x, gridSpacing) + docFrame.x;
            double pmy = RMMath.round(rect.getMaxY() - docFrame.y, gridSpacing) + docFrame.y;
            if(Math.abs(px - rect.x)<Math.abs(dx))
                dx = px - rect.x;
            if(Math.abs(py - rect.y)<Math.abs(dy))
                dy = py - rect.y;
            if(Math.abs(pmx - rect.getMaxX())<Math.abs(dx))
                dx = pmx - rect.getMaxX();
            if(Math.abs(pmy - rect.getMaxY())<Math.abs(dy))
                dy = pmy - rect.getMaxY();
        }
        
        // Adjust offsets and grid spacing for zoom factor
        dx /= editor.getZoomFactor();
        dy /= editor.getZoomFactor();
        gridSpacing /= editor.getZoomFactor();
    }

    // Go ahead and offset aPoint if necessary
    if(Math.abs(dx)<=gridSpacing/2) aPoint.x += dx;
    if(Math.abs(dy)<=gridSpacing/2) aPoint.y += dy;
        
    // Covert back to shape if we need to
    if(!snapEdges)
        aPoint = editor.convertPointToShape(aPoint, null); // Get aPoint in world coords
    
    // Return point
    return aPoint;
}

/**
 * Returns a given point adjusted for grids & guides.
 */
private RMPoint pointSnappedToGuides(RMPoint aPoint, boolean snapEdges)
{
    // Get the editor, document and document frame
    RMEditor editor = getEditor();
    RMDocument doc = editor.getDocument(); if(doc==null) return aPoint;
    RMRect docFrame = editor.convertRectFromShape(editor.getSelectedPage().getBoundsInside(), editor.getSelectedPage());
    
    // Get grid spacing and dx/dy for maximum offsets
    double gridSpacing = doc.getGridSpacing()*editor.getZoomFactor();
    double dx = gridSpacing/2 + .001f;
    double dy = dx;
    
    // If not snapping to all edges, round aPoint to nearest grid or guide
    if(!snapEdges) {
        
        // Get point in editor coords
        aPoint = editor.convertPointFromShape(aPoint, null);
    
        // Find min dx/dy to nearest guide
        for(int j=0, jMax=getGuideCount(doc); j<jMax; j++) {
            byte orientation = getGuideOrientation(j);
            double location = getGuideLocation(doc, j)*editor.getZoomFactor() +
                (orientation==GUIDE_VERTICAL? docFrame.x : docFrame.y);

            if(orientation==GUIDE_VERTICAL) {
                if(Math.abs(location - aPoint.x)<Math.abs(dx))
                    dx = location - aPoint.x;
            }
            else if(Math.abs(location - aPoint.y)<Math.abs(dy))
                    dy = location - aPoint.y;
        }
    }
    
    // If _snapEdges, find dx/dy for all edges of selected shapes to nearest grid or guide
    else {
    
        // Iterate over selected shapes
        for(int i=0, iMax=editor.getSelectedShapeCount(); i<iMax; i++) { RMShape shape = editor.getSelectedShape(i);
            
            // Get shape bounds in editor coords
            RMRect rect = editor.convertRectFromShape(shape.getBoundsInside(), shape);
            
            // Iterate over guides to find dx/dy to nearest guide
            for(int j=0, jMax=getGuideCount(doc); j<jMax; j++) {
                
                // Get current loop guide orientation 
                int orientation = getGuideOrientation(j);
                
                // Get current loop guide location
                double location = getGuideLocation(doc, j)*editor.getZoomFactor() +
                    (orientation==GUIDE_VERTICAL? docFrame.x : docFrame.y);
                
                // If vertical...
                if(orientation==GUIDE_VERTICAL) {
                    double minxDx = location - rect.x, maxxDx = location - rect.getMaxX();
                    if(Math.abs(minxDx)<Math.abs(dx))
                        dx = minxDx;
                    if(Math.abs(maxxDx)<Math.abs(dx))
                        dx = maxxDx;
                }
                
                // If horizontal...
                if(orientation==GUIDE_HORIZONTAL) {
                    double minyDy = location - rect.y, maxyDy = location - rect.getMaxY();
                    if(Math.abs(minyDy)<Math.abs(dy)) dy = minyDy;
                    if(Math.abs(maxyDy)<Math.abs(dy)) dy = maxyDy;
                }
            }
        }
        
        // Adjust offsets and grid spacing for zoom factor
        dx /= editor.getZoomFactor();
        dy /= editor.getZoomFactor();
        gridSpacing /= editor.getZoomFactor();
    }

    // Go ahead and offset aPoint if necessary
    if(Math.abs(dx)<=gridSpacing/2) aPoint.x += dx;
    if(Math.abs(dy)<=gridSpacing/2) aPoint.y += dy;
        
    // Covert back to shape if we need to
    if(!snapEdges)
        aPoint = editor.convertPointToShape(aPoint, null); // Get aPoint in world coords
    
    // Return point
    return aPoint;
}

/**
 * Returns the number of guides (4 if snapping to margin, otherwise zero).
 */
public static int getGuideCount(RMDocument aDoc)  { return aDoc.getSnapMargin()? 4 : 0; }

/**
 * Returns the guide location for the given index.
 */
public static double getGuideLocation(RMDocument aDoc, int anIndex)
{
    switch(anIndex) {
        case 0: return aDoc.getMarginLeft();
        case 1: return aDoc.getSelectedPage().getWidth() - aDoc.getMarginRight();
        case 2: return aDoc.getMarginTop();
        case 3: return aDoc.getSelectedPage().getHeight() - aDoc.getMarginBottom();
    }
    return 0;
}

/**
 * Returns the guide orientation for the given index.
 */
private byte getGuideOrientation(int anIndex)  { return anIndex==0 || anIndex==1? GUIDE_VERTICAL : GUIDE_HORIZONTAL; }

/**
 * Returns whether to override preview mode.
 */
public boolean getOverridePreview()  { return _overridePreview; }

/**
 * Sets whether to override preview mode.
 */
public void setOverridePreview(boolean aValue)  { _overridePreview = aValue; }

}