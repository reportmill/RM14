package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * Handles editor methods specific to clipboard operations (cut, copy paste).
 */
public class RMEditorShapes {

    // The editor
    RMEditor          _editor;

    // The last color set by or returned to the color panel
    static RMColor    _lastColor = RMColor.black;

/**
 * Creates a new editor shapes helper.
 */
public RMEditorShapes(RMEditor anEditor) { _editor = anEditor; }

/**
 * Returns the editor.
 */
public RMEditor getEditor() { return _editor; }

/**
 * Groups the given shape list to the given group shape.
 * If given shapes list is null, use editor selected shapes.
 * If given group shape is null, create new generic group shape.
 */
public static void groupShapes(RMEditor anEditor, List <? extends RMShape> theShapes, RMParentShape aGroupShape)
{
    // If shapes not provided, use editor selected shapes
    if(theShapes==null)
        theShapes = anEditor.getSelectedShapes();
    
    // If there are less than 2 selected shapes play a beep (the user really should know better)
    if(theShapes.size()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Set undo title
    anEditor.undoerSetUndoTitle("Group");

    // Group shapes
    aGroupShape = RMShapeUtils.groupShapes(theShapes, aGroupShape);
    
    // Select group shape
    anEditor.setSelectedShape(aGroupShape);
}

/**
 * Ungroups any currently selected group shapes.
 */
public static void ungroupShapes(RMEditor anEditor)
{
    // Get currently super selected shape and create list to hold ungrouped shapes
    List <RMShape> ungroupedShapes = new Vector();
    
    // Register undo title for ungrouping
    anEditor.undoerSetUndoTitle("Ungroup");

    // See if any of the selected shapes can be ungrouped
    for(RMShape shape : anEditor.getSelectedShapes()) {
        
        // If shape cann't be ungrouped, skip
        if(!RMTool.getTool(shape).isUngroupable(shape)) continue;
        RMParentShape groupShape = (RMParentShape)shape;
        RMParentShape parent = groupShape.getParent();
            
        // Iterate over children, remove from groupShape, add to groupShape parent and add to ungroupedShapes list
        for(RMShape child : groupShape.getChildArray()) {
            
            // Convert child to world coords
            child.convertToShape(null);
            
            // Remove from group shape & add to group shape parent
            groupShape.removeChild(child);
            parent.addChild(child);
            ungroupedShapes.add(child);
            
            // Convert back from world coords
            child.convertFromShape(null);
        }

        // Remove groupShape from parent
        parent.removeChild(groupShape);
    }

    // If were some ungroupedShapes, select them (set selected objects for undo/redo)
    if(ungroupedShapes.size()>0)
        anEditor.setSelectedShapes(ungroupedShapes);

    // If no ungroupedShapes, beep at silly user
    else Toolkit.getDefaultToolkit().beep();
}

/**
 * Orders all currently selected shapes to the front.
 */
public static void bringToFront(RMEditor anEditor)
{
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    if(parent==null || anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Bring to Front");
    parent.bringShapesToFront(anEditor.getSelectedShapes());
}

/**
 * Orders all currently selected shapes to the back.
 */
public static void sendToBack(RMEditor anEditor)
{
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    if(parent==null || anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Send to Back");
    parent.sendShapesToBack(anEditor.getSelectedShapes());
}

/**
 * Arranges currently selected shapes in a row relative to their top.
 */
public static void makeRowTop(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Top");
    double minY = anEditor.getSelectedShape().getFrameY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(minY);
}

/**
 * Arranges currently selected shapes in a row relative to their center.
 */
public static void makeRowCenter(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Center");
    double midY = anEditor.getSelectedShape().getFrame().getMidY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(midY - shape.getHeight()/2);
}

/**
 * Arranges currently selected shapes in a row relative to their bottom.
 */
public static void makeRowBottom(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Bottom");
    double maxY = anEditor.getSelectedShape().getFrameMaxY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(maxY - shape.getHeight());
}

/**
 * Arranges currently selected shapes in a column relative to their left border.
 */
public static void makeColumnLeft(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Left");
    double minX = anEditor.getSelectedShape().getFrameX();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(minX);
}

/**
 * Arranges currently selected shapes in a column relative to their center.
 */
public static void makeColumnCenter(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Center");
    double midX = anEditor.getSelectedShape().getFrame().getMidX();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(midX - shape.getWidth()/2);
}

/**
 * Arranges currently selected shapes in a column relative to their right border.
 */
public static void makeColumnRight(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Right");
    double maxX = anEditor.getSelectedShape().getFrameMaxX();    
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(maxX - shape.getWidth());
}

/**
 * Makes currently selected shapes all have the same width and height as the first selected shape.
 */
public static void makeSameSize(RMEditor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    anEditor.undoerSetUndoTitle("Make Same Size");
    RMSize size = anEditor.getSelectedShape().getSize();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setSize(size.width, size.height);
}

/**
 * Makes currently selected shapes all have the same width as the first selected shape.
 */
public static void makeSameWidth(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Width");
    
    // Get first selected shape width
    double width = anEditor.getSelectedShape().getWidth();
    
    // Iterate over selected shapes and set width
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setWidth(width);
}

/**
 * Makes currently selected shapes all have the same height as the first selected shape.
 */
public static void makeSameHeight(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Height");
    
    // Get first selected shape height
    double height = anEditor.getSelectedShape().getHeight();
    
    // Iterate over selected shapes and set height
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setHeight(height);
}

/**
 * Makes currently selected shapes size to fit content.
 */
public static void setSizeToFit(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Size to Fit");
    
    // Iterate over shapes and size to fit
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setBestSize();
}

/**
 * Arranges currently selected shapes such that they have the same horizontal distance between them.
 */
public static void equallySpaceRow(RMEditor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    List <RMShape> shapes = RMSort.sortedList(anEditor.getSelectedShapes(), "getFrameX");
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameX() - shapes.get(i-1).getFrameMaxX();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);
    
    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Row");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        RMShape shape = shapes.get(i);
        RMShape lastShape = shapes.get(i-1);
        double tx = lastShape.getFrameMaxX() + spaceBetweenShapes;
        shape.setFrameX(tx);
    }
}

/**
 * Arranges currently selected shapes such that they have the same vertical distance between them.
 */
public static void equallySpaceColumn(RMEditor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    List <RMShape> shapes = RMSort.sortedList(anEditor.getSelectedShapes(), "getFrameY");
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameY() - shapes.get(i-1).getFrameMaxY();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);

    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Column");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        RMShape shape = shapes.get(i);
        RMShape lastShape = shapes.get(i-1);
        double ty = lastShape.getFrameMaxY() + spaceBetweenShapes;
        shape.setFrameY(ty);
    }
}

/**
 * Adds the selected shapes to a Switch Shape.
 */
public static void groupInSwitchShape(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selected shapes    
    List <RMShape> selectedShapes = anEditor.getSelectedShapes();
    
    // Get parent    
    RMShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Get new switch shape to group selected shapes in
    RMSwitchShape groupShape = new RMSwitchShape();
    
    // Set switch shape to combined bounds of children (outset by just a little)
    groupShape.setFrame(RMShapeUtils.getBoundsOfChildren(parent, selectedShapes).inset(-2));

    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Switch Shape");
    
    // Add shapes to group shape
    groupShapes(anEditor, selectedShapes, groupShape);
}

/**
 * Adds the selected shapes to a Scene3D Shape.
 */
public static void groupInScene3D(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selected shapes
    List <RMShape> selectedShapes = RMListUtils.clone(anEditor.getSelectedShapes());
    
    // Get parent
    RMParentShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Get new Scene3D to group selected shapes in
    RMScene3D groupShape = new RMScene3D();
    
    // Set scene3D to combined bounds of children
    groupShape.setFrame(RMShapeUtils.getBoundsOfChildren(parent, selectedShapes));

    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Scene3D");
    
    // Iterate over children and add to group shape
    for(int i=0, iMax=selectedShapes.size(); i<iMax; i++) {
        RMShape shape = selectedShapes.get(i);
        groupShape.addShape(shape);
        shape.removeFromParent();
        shape.setXY(shape.x() - groupShape.x(), shape.y() - groupShape.y());
    }
    
    // Add group shape to original parent
    parent.addChild(groupShape);
    
    // Select new shape
    anEditor.setSelectedShape(groupShape);
}

/**
 * Adds the selected shapes to a Morph Shape.
 */
public static void groupInMorphShape(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()<2) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // If any shape is non-singular, complain and return
    for(int i=0, iMax=anEditor.getSelectedShapeCount(); i<iMax; i++) {
        if(!new java.awt.geom.Area(anEditor.getSelectedShape(i).getPath()).isSingular()) {
            DialogBox dbox = new DialogBox("Cannot Handle Multiple Subpaths");
            dbox.setErrorMessage("One of the selected shapes has multiple sub-paths,\n");
            dbox.showMessageDialog(anEditor);
            return;
        }
    }
    
    // Get selected shape
    RMShape shape = anEditor.getSelectedShape();
    
    // Get selected shapes
    List <RMShape> selectedShapes = RMListUtils.clone(anEditor.getSelectedShapes());
    
    // Get parent
    RMParentShape parent = shape.getParent();
    
    // Get new Morph Shape to group selected shapes in
    RMMorphShape morphShape = new RMMorphShape();
    
    // Set morph shape fill and stroke to first shape's
    morphShape.setFill(shape.getFill());
    morphShape.setStroke(shape.getStroke());
    
    // Set scene3D to combined bounds of children
    morphShape.setFrame(anEditor.getSelectedShape(0).getBounds());

    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Morph Shape");
    
    // Iterate over children and add to group shape
    for(int i=0, iMax=selectedShapes.size(); i<iMax; i++) {
        RMShape shp = selectedShapes.get(i);
        morphShape.addShape(shp);
        shp.removeFromParent();
    }
    
    // Add group shape to original parent
    parent.addChild(morphShape);
    
    // Select new shape
    anEditor.setSelectedShape(morphShape);
}

/**
 * Creates an animation path from the selected shapes.
 */
public static void groupInAnimationPath(RMEditor anEditor)
{
    // If wrong number of shapes, complain and return
    if(anEditor.getSelectedShapeCount()!=2) {
        DialogBox dbox = new DialogBox("Cannot Create Animation Path");
        dbox.setErrorMessage("To create an animation path, two shapes must be selected.");
        dbox.showMessageDialog(anEditor); return;
    }
    
    
    // Find first RMPolygon in the selected shapes to use as the path
    List <RMShape> selectedShapes = RMListUtils.clone(anEditor.getSelectedShapes());
    RMShape shape = null;
    for(int i=0, iMax=selectedShapes.size(); i<iMax; ++i) {
        if (selectedShapes.get(i) instanceof RMPolygonShape) {
            shape = selectedShapes.remove(i);
            break;
        }
    }
    
    // No polygon in the list.  Use the fist shape in the list
    if(shape==null)
        shape = selectedShapes.remove(0);

    // Get the parent
    RMParentShape parent = shape.getParent();
    
    // Create new animPath from polygon
    RMAnimPathShape pathShape = new RMAnimPathShape(shape);
    
    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Animation Path");
    
    // Add anim child
    pathShape.setAnimChild(selectedShapes.get(0));
    
    // Add animpath back to original parent
    parent.addChild(pathShape);
    
    // remove original shape
    shape.removeFromParent();
    
    // Select new shape
    anEditor.setSelectedShape(pathShape);
}

/**
 * Converts the currently selected shapes to a artist shape.
 */
public static void groupInPainterShape(RMEditor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selected shapes    
    List <RMShape> selectedShapes = anEditor.getSelectedShapes();
    
    // Get parent    
    RMParentShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Get new painter shape to group selected shapes in
    RMPainterShape groupShape = new RMPainterShape();
    
    // Set shape to combined bounds of children
    groupShape.setFrame(RMShapeUtils.getBoundsOfChildren(parent, selectedShapes));
    parent.addChild(groupShape);

    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Painter Shape");
    
    // Add operations for shape paths
    for(RMShape shape : selectedShapes) {
        RMTransform transform = shape.getTransformToShape(groupShape);
        RMPath path = shape.getPathInBounds(); path.transformBy(transform);
        groupShape.setPaintColor(shape.getStrokeColor()!=null? shape.getStrokeColor().awt() : shape.getColor().awt());
        groupShape.setPaintStrokeWidth(shape.getStrokeWidth());
        groupShape.addOpsForPath(path.getPathIterator(null));
    }
    
    // Remove shapes
    for(int i=selectedShapes.size()-1; i>=0; i--)
        parent.removeChild(selectedShapes.get(i));
    
    // Set animator max time to execution time of painter shape
    if(groupShape.getExecutionTime()>parent.getChildAnimator(true).getMaxTime())
        parent.getChildAnimator().setMaxTimeSeconds(groupShape.getExecutionTime());
    
    // Select new shape
    anEditor.setSelectedShape(groupShape);
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void combinePaths(RMEditor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedShapeCount()<2) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selected shapes and create CombinedShape
    List <RMShape> selectedShapes = RMListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape combinedShape = RMShapeUtils.getCombinedPathsShape(selectedShapes);
    
    // Remove original children and replace with CombinedShape
    anEditor.undoerSetUndoTitle("Add Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(combinedShape);
    
    // Select CombinedShape
    anEditor.setSelectedShape(combinedShape);
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void subtractPaths(RMEditor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedShapeCount()<2) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Get selected shapes and create SubtractedShape
    List <RMShape> selectedShapes = RMListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape subtractedShape = RMShapeUtils.getSubtractedPathsShape(selectedShapes, 0);
    
    // Remove original children and replace with SubtractedShape
    anEditor.undoerSetUndoTitle("Subtract Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(subtractedShape);
    
    // Select SubtractedShape
    anEditor.setSelectedShape(subtractedShape);
}

/**
 * Converts currently selected shape to image.
 */
public static void convertToImage(RMEditor anEditor)
{
    // Get currently selected shape (if shape is null, just return)
    RMShape shape = anEditor.getSelectedShape(); if(shape==null) return;
    
    // Get image for shape, get PNG bytes for image and create new RMImageShape for bytes
    Image image = new RMShapeImager().createImage(shape);
    byte imageBytes[] = RMAWTUtils.getBytesPNG(image);
    RMImageShape imageShape = new RMImageShape(imageBytes);
    
    // Set ImageShape XY and add to parent
    imageShape.setXY(shape.getX() + shape.getBoundsMarked().x, shape.getY() + shape.getBoundsMarked().y);
    shape.getParent().addChild(imageShape, shape.indexOf());
    
    // Replace old selectedShape with image and remove original shape
    anEditor.setSelectedShape(imageShape);
    shape.removeFromParent();
}

/**
 * Moves all the currently selected shapes one point to the right.
 */
public static void moveRightOnePoint(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Right One Point");
    double offset = anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(shape.getFrameX() + offset);
}

/**
 * Moves all the currently selected shapes one point to the left.
 */
public static void moveLeftOnePoint(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Left One Point");
    double offset = anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(shape.getFrameX() - offset);
}

/**
 * Moves all the currently selected shapes one point up.
 */
public static void moveUpOnePoint(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Up One Point");
    double offset = anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(shape.getFrameY() - offset);
}

/**
 * Moves all the currently selected shapes one point down.
 */
public static void moveDownOnePoint(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Down One Point");
    double offset = anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(shape.getFrameY() + offset);
}

/**
 * Moves all the currently selected shapes to a new page layer.
 */
public static void moveToNewLayer(RMEditor anEditor)
{
    RMDocument doc = anEditor.getDocument();
    if(anEditor.getSelectedShapeCount()==0 || doc==null) { Toolkit.getDefaultToolkit().beep(); return; }
    doc.getSelectedPage().moveToNewLayer(anEditor.getSelectedShapes());
}

/**
 * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
 */
public static RMColor getSelectedColor(RMEditor anEditor)
{
    // Get selected or super selected shape
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    
    // If selected or super selected shape is page that doesn't draw color, return "last color" (otherwise, reset it)
    if((shape instanceof RMPage || shape instanceof RMDocument) && shape.getFill()==null)
        return _lastColor;
    else _lastColor = RMColor.black;
        
    // If text color and text editing, return color of text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getColor();
        
    // Return selected shape's color
    return anEditor.getSelectedOrSuperSelectedShape().getColor();
}

/**
 * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
 */
public static void setSelectedColor(RMEditor anEditor, RMColor aColor)
{
    // Get selected or super selected shape
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
        
    // If editor selected or super selected shape is document or page, set "last color" and return
    if(shape instanceof RMPage || shape instanceof RMDocument) {
        _lastColor = aColor;
        return;
    }

    // If text color and text editing, return color of text editor
    if(anEditor.getTextEditor()!=null) {
        
        // Get text editor
        RMTextEditor ted = anEditor.getTextEditor();
        
        // If command down, and text is outlined, set color of outline instead
        if(Swing.isMetaDown() && ted.getOutline()!=null)
            ted.setOutline(ted.getOutline().deriveOutline(aColor));
        
        // If no command down, set color of text editor
        else ted.setColor(aColor);
    }
    
    // If fill color, set selected shapes' fill color
    else {
    
        // If command-click, set gradient fill
        if(Swing.isMetaDown()) {
            RMColor c1 = shape.getFill()!=null? shape.getColor() : RMColor.clearWhite;
            shape.setFill(new RMGradientFill(c1, aColor, 0));
        }
        
        // If not command click, just set the color of all the selected shapes
        else setColor(anEditor, aColor);
    }
}

/**
 * Sets the fill color of the editor's selected shapes.
 */
public static void setColor(RMEditor anEditor, RMColor aColor)
{
    // Iterate over editor selected shapes or super selected shape
    for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setColor(aColor);
}

/**
 * Sets the stroke color of the editor's selected shapes.
 */
public static void setStrokeColor(RMEditor anEditor, RMColor aColor)
{
    // Iterate over editor selected shapes or super selected shape
    for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setStrokeColor(aColor);
}

/**
 * Sets the text color of the editor's selected shapes.
 */
public static void setTextColor(RMEditor anEditor, RMColor aColor)
{
    // If text editing, forward on to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setColor(aColor);
        
    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setTextColor(aColor);
}

/**
 * Returns the font of editor's selected shape.
 */
public static RMFont getFont(RMEditor anEditor)
{
    RMFont font = null;
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax && font==null; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        font = tool.getFont(anEditor, shape);
    }
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax && font==null; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        font = tool.getFontDeep(anEditor, shape);
    }
    return font!=null? font : RMFont.getDefaultFont();
}

/**
 * Sets the font family of editor's selected shape(s).
 */
public static void setFontFamily(RMEditor anEditor, RMFont aFont)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.setFontFamilyDeep(anEditor, shape, aFont);
    }
}

/**
 * Sets the font name of editor's selected shape(s).
 */
public static void setFontName(RMEditor anEditor, RMFont aFont)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.setFontNameDeep(anEditor, shape, aFont);
    }
}

/**
 * Sets the font size of editor's selected shape(s).
 */
public static void setFontSize(RMEditor anEditor, float aSize, boolean isRelative)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.setFontSizeDeep(anEditor, shape, aSize, isRelative);
    }
}

/**
 * Sets the "boldness" of text in the currently selected shapes.
 */
public static void setFontBold(RMEditor anEditor, boolean aFlag)
{
    anEditor.undoerSetUndoTitle("Make Bold");
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.setFontBoldDeep(anEditor, shape, aFlag);
    }
}

/**
 * Sets the italic state of text in the currently selected shapes.
 */
public static void setFontItalic(RMEditor anEditor, boolean aFlag)
{
    anEditor.undoerSetUndoTitle("Make Italic");
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = anEditor.getSelectedOrSuperSelectedShape(i);
        RMTool tool = RMTool.getTool(shape);
        tool.setFontItalicDeep(anEditor, shape, aFlag);
    }
}

/**
 * Returns whether the currently selected shape is underlined.
 */
public static boolean isUnderlined(RMEditor anEditor)
{
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().isUnderlined();
    return anEditor.getSelectedOrSuperSelectedShape().isUnderlined();
}

/**
 * Sets the currently selected shapes to be underlined.
 */
public static void setUnderlined(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Make Underlined");
    
    // If text editing, forward to text editor
    RMTextEditor ted = anEditor.getTextEditor();
    if(ted!=null)
        ted.setUnderlined(!ted.isUnderlined());
    
    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setUnderlined(!shape.isUnderlined());
}

/**
 * Returns the outline state of the currently selected shape (null if none).
 */
public static RMXString.Outline getOutline(RMEditor anEditor)
{
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getOutline();
    return anEditor.getSelectedOrSuperSelectedShape().getOutline();
}

/**
 * Sets the currently selected shapes to be outlined.
 */
public static void setOutline(RMEditor anEditor)
{
    setOutline(anEditor, getOutline(anEditor)==null? new RMXString.Outline() : null);
}

/**
 * Sets the outline state of the currently selected shapes.
 */
public static void setOutline(RMEditor anEditor, RMXString.Outline anOutline)
{
    anEditor.undoerSetUndoTitle("Make Outlined");
    
    // If there's a text editor, have it setOutline
    RMTextEditor ted = anEditor.getTextEditor();
    if(ted!=null)
        ted.setOutline(anOutline);
        
    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setOutline(anOutline);
}

/**
 * Returns the horizontal alignment of the text of the currently selected shapes.
 */
public static RMTypes.AlignX getAlignmentX(RMEditor anEditor)
{
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getAlignmentX();
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    return shape instanceof RMTextShape? ((RMTextShape)shape).getAlignmentX() : RMTypes.AlignX.Left;
}

/**
 * Sets the horizontal alignment of the text of the currently selected shapes.
 */
public static void setAlignmentX(RMEditor anEditor, RMTypes.AlignX anAlign)
{
    // Set undo title
    anEditor.undoerSetUndoTitle("Alignment Change");

    // Handle normal shape selected: Iterate over editor selected shapes or super selected shape
    if(anEditor.getTextEditor()==null)
        for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
            shape.setAlignmentX(anAlign);
    
    // Handle text super-selected
    else anEditor.getTextEditor().setAlignmentX(anAlign);
}

/**
 * Sets the currently selected shapes to show text as superscript.
 */
public static void setSuperscript(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Make Superscript");

    RMTextEditor ted = anEditor.getTextEditor();
    if(ted!=null)
        ted.setSuperscript();
}

/**
 * Sets the currently selected shapes to show text as subscript.
 */
public static void setSubscript(RMEditor anEditor)
{
    anEditor.undoerSetUndoTitle("Make Subscript");
    
    RMTextEditor ted = anEditor.getTextEditor();
    if(ted!=null)
        ted.setSubscript();
}

/**
 * Returns the characters spacing for the currently selected shape.
 */
public static float getCharSpacing(RMEditor anEditor)
{
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getCharSpacing();
    
    // If not editing text, ask current selected shape (if text) for it's character spacing
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    RMTextShape text = shape instanceof RMTextShape? (RMTextShape)shape : null;
    return text==null? 0 : text.getCharSpacing();
}

/**
 * Sets the character spacing for the currently selected shapes.
 */
public static void setCharSpacing(RMEditor anEditor, float aValue)
{
    // Register for undo
    anEditor.undoerSetUndoTitle("Char Spacing Change");
    
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setCharSpacing(aValue);
    
    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        if(shape instanceof RMTextShape)
            ((RMTextShape)shape).setCharSpacing(aValue);
}

/**
 * Returns the line spacing at char 0 (or selected char, if editing).
 */
public static float getLineSpacing(RMEditor anEditor)
{
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getLineSpacing();
    
    // If not editing text, ask the current selected shape (if text) for its line spacing
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    return shape instanceof RMTextShape? ((RMTextShape)shape).getLineSpacing() : 0;
}

/**
 * Sets the line spacing for all chars (or all selected chars, if editing).
 */
public static void setLineSpacing(RMEditor anEditor, float aHeight)
{
    // Register for undo
    anEditor.undoerSetUndoTitle("Line Spacing Change");
    
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setLineSpacing(aHeight);

    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        if(shape instanceof RMTextShape)
            ((RMTextShape)shape).setLineSpacing(aHeight);
}

/**
 * Returns the line gap at char 0 (or selected char, if editing).
 */
public static float getLineGap(RMEditor anEditor)
{
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getLineGap();
    
    // If not editing text, ask the current selected shape (if text) for its line gap
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    return shape instanceof RMTextShape? ((RMTextShape)shape).getLineGap() : 0;
}

/**
 * Sets the line gap for all chars (or all selected chars, if editing).
 */
public static void setLineGap(RMEditor anEditor, float aHeight)
{
    // Register for undo
    anEditor.undoerSetUndoTitle("Line Gap Change");
    
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setLineGap(aHeight);

    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        if(shape instanceof RMTextShape)
            ((RMTextShape)shape).setLineGap(aHeight);
}

/**
 * Returns the minimum line height at char 0 (or selected char, if editing).
 */
public static float getLineHeightMin(RMEditor anEditor)
{
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getLineHeightMin();
    
    // If not editing text, ask the current selected shape (if text) for its line height
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    return shape instanceof RMTextShape? ((RMTextShape)shape).getLineHeightMin() : 0;
}

/**
 * Sets the minimum line height for all chars (or all selected chars, if editing).
 */
public static void setLineHeightMin(RMEditor anEditor, float aHeight)
{
    // Register for undo
    anEditor.undoerSetUndoTitle("Min Line Height Change");
    
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setLineHeightMin(aHeight);

    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        if(shape instanceof RMTextShape)
            ((RMTextShape)shape).setLineHeightMin(aHeight);
}

/**
 * Returns the maximum line height at char 0 (or selected char, if editing).
 */
public static float getLineHeightMax(RMEditor anEditor)
{
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getLineHeightMax();
    
    // If not editing text, ask the current selected shape (if text) for its line height
    RMShape shape = anEditor.getSelectedOrSuperSelectedShape();
    return shape instanceof RMTextShape? ((RMTextShape)shape).getLineHeightMax() : 0;
}

/**
 * Sets the maximum line height for all chars (or all selected chars, if eiditing).
 */
public static void setLineHeightMax(RMEditor anEditor, float aHeight)
{
    // Register for undo
    anEditor.undoerSetUndoTitle("Max Line Height Change");
    
    // If editing text, just forward call to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setLineHeightMax(aHeight);

    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        if(shape instanceof RMTextShape)
            ((RMTextShape)shape).setLineHeightMax(aHeight);
}

/**
 * Returns the format of the editor's selected shape.
 */
public static RMFormat getFormat(RMEditor anEditor)
{
    // If text editing, forward on to text editor
    if(anEditor.getTextEditor()!=null)
        return anEditor.getTextEditor().getFormat();
    
    // Return first selected shape's format
    return anEditor.getSelectedOrSuperSelectedShape().getFormat();
}

/**
 * Sets the format of editor's selected shape(s).
 */
public static void setFormat(RMEditor anEditor, RMFormat aFormat)
{
    // If text editing, forward on to text editor
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().setFormat(aFormat);
        
    // Otherwise, iterate over editor selected shapes or super selected shape
    else for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        shape.setFormat(aFormat);
}

/**
 * Splits the selected shape in half on the horizontal axis.
 */
public static void splitHorizontal(RMEditor editor)
{
    editor.undoerSetUndoTitle("Split Column");
    RMShape shape = editor.getSuperSelectedShape();
    RMParentShape parent = shape.getParent();
    shape.repaint();
    shape = shape.divideShapeFromEdge(shape.getWidth()/2, RMRect.MinXEdge, null);
    parent.addChild(shape);
    editor.setSuperSelectedShape(shape);
}

/**
 * Adds an image placeholder to the given editor.
 */
public static void addImagePlaceholder(RMEditor anEditor)
{
    // Create image shape
    RMImageShape imageShape = new RMImageShape(null);
    
    // Get parent and move image shape to center
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    imageShape.setFrame((int)(parent.getWidth()/2 - 24), (int)(parent.getHeight()/2 - 24), 48, 48);

    // Set image in image shape and add imageShape to mainShape
    anEditor.undoerSetUndoTitle("Add Image");
    parent.addChild(imageShape);

    // Select imageShape, set selectTool and redisplay
    anEditor.setSelectedShape(imageShape);
    anEditor.setCurrentToolToSelectTool();
    anEditor.repaint();
}

/**
 * Adds a subreport to the given editor.
 */
public static void addSubreport(RMEditor anEditor)
{
    // Create image shape
    RMSubreport subreport = new RMSubreport();
    
    // Get parent and move shape to center
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    subreport.setFrame((int)(parent.getWidth()/2 - 200), (int)(parent.getHeight()/2 - 60), 400, 120);

    // Add shape to parent
    anEditor.undoerSetUndoTitle("Add Subreport");
    parent.addChild(subreport);

    // Select shape, set selectTool and repaint
    anEditor.setSelectedShape(subreport);
    anEditor.setCurrentToolToSelectTool();
    anEditor.repaint();
}

/**
 * Runs the dataset key panel to add a table, graph, crosstab or labels to given editor.
 */
public static void runDatasetKeyPanel(RMEditor anEditor, String aKeyPath)
{
    // Run dataset key panel to get dataset element type
    int type = new DatasetKeyPanel().showDatasetKeyPanel(anEditor);
    
    // Add appropriate dataset key element for returned type
    switch(type) {
        case DatasetKeyPanel.TABLE: RMTableTool.addTable(anEditor, aKeyPath); break;
        case DatasetKeyPanel.GRAPH: RMGraphTool.addGraph(anEditor, aKeyPath); break;
        case DatasetKeyPanel.LABELS: RMLabelsTool.addLabels(anEditor, aKeyPath); break;
        case DatasetKeyPanel.CROSSTAB: RMCrossTabTool.addCrossTab(anEditor, aKeyPath); break;
    }
    
    // If EditorPane.Inspector showing DataSource inspector, reset os ShapeSpecific
    RMEditorPane editorPane = anEditor.getEditorPane();
    if(editorPane.getInspectorPanel().isShowingDataSource())
        editorPane.getInspectorPanel().setVisible(0);
}

/**
 * Group currently selected shapes in panel.
 */
public static void groupInPanel(RMEditor anEditor)
{
    // If no shapes selected, just beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Set undo title for grouping
    anEditor.undoerSetUndoTitle("Group in Tabbed Pane");
    
    // Get copy of selected shapes
    List <RMShape> selectedShapes = anEditor.getSelectedShapes();
    
    // Get parent of selected shapes
    RMShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Create new panel shape for children from panel
    SpringsPaneShape panelShape = new SpringsPaneShape();
    
    // Get bounds of children (expanded and offset to provide some kind of margin)
    RMRect panelShapeBounds = RMShapeUtils.getBoundsOfChildren(parent, selectedShapes).inset(-10);

    // Set bounds of panel shape
    panelShape.setBounds(panelShapeBounds);

    // Do normal group shapes
    RMEditorShapes.groupShapes(anEditor, selectedShapes, panelShape);
}

/**
 * Group currently selected shapes in tabbed pane.
 */
public static void groupInTabbedPane(RMEditor anEditor)
{
    // If no shapes selected, just beep and return
    if(anEditor.getSelectedShapeCount()==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // Set undo title for grouping
    anEditor.undoerSetUndoTitle("Group in Tabbed Pane");
    
    // Group shapes in panel
    groupInPanel(anEditor);
    
    // Get copy of selected shapes
    List <RMShape> selectedShapes = anEditor.getSelectedShapes();
    
    // Get parent of selected shapes
    RMShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Get panel shape
    SpringsPaneShape panelShape = (SpringsPaneShape)anEditor.getSelectedShape();
    
    // Create new JTabbedPaneShape
    JTabbedPaneShape tabbedPaneShape = new JTabbedPaneShape();
    
    // Get bounds of children (expanded and offset to provide some kind of margin)
    RMRect panelShapeBounds = RMShapeUtils.getBoundsOfChildren(parent, selectedShapes).inset(-10, -20).offset(0, -10);

    // Set bounds of panel shape
    tabbedPaneShape.setBounds(panelShapeBounds);

    // Do normal group shapes
    RMEditorShapes.groupShapes(anEditor, selectedShapes, tabbedPaneShape);
    
    // Add panel shape
    tabbedPaneShape.addTab("One", panelShape);
}

/**
 * Group currently selected RJShapes in RJScrollPane.
 */
public static void groupInScrollPane(RMEditor anEditor)
{
    // If no shapes selected, just beep and return
    if(anEditor.getSelectedShapeCount()==0) {
        Toolkit.getDefaultToolkit().beep();
        return;
    }
    
    // Register for undo
    anEditor.undoerSetUndoTitle("Group in ScrollPane");
    
    // If multiple shapes selected or selected shape isn't JComponentShape, group in panel first
    if(anEditor.getSelectedShapeCount()>1 || !(anEditor.getSelectedShape() instanceof JComponentShape))
        groupInPanel(anEditor);
    
    // Get selected shape
    JComponentShape selectedShape = (JComponentShape)anEditor.getSelectedShape();
    
    // Create new scrollpane shape, configured with scroll bars
    JScrollPaneShape scrollPaneShape = new JScrollPaneShape();
    scrollPaneShape.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPaneShape.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    
    // Set the bounds
    scrollPaneShape.setBounds(selectedShape.getX(), selectedShape.getY(), selectedShape.getWidth()+20, selectedShape.getHeight()+20);
    
    // Do normal group shapes
    RMEditorShapes.groupShapes(anEditor, Arrays.asList(selectedShape), scrollPaneShape);
}

}