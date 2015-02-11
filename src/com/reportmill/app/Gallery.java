package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.viewer.*;
import java.awt.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides UI for showing an RMParentShape and allowing drag and drop from it.
 */
public class Gallery extends SwingOwner {
    
    // The viewer for showing an individual gallery document
    GViewer         _viewer;
    
    // The Button group for selecting individual gallery documents
    ButtonGroup     _bgroup = new ButtonGroup();
    
    // The shape being dragged
    static RMShape  _dragShape;

/**
 * Returns the name to be used in the inspector window.
 */
public String getWindowTitle()  { return "Gallery"; }

/**
 * Returns the shape being dragged.
 */
public static RMShape getDragShape()  { return _dragShape; }

/**
 * Creates UI panel.
 */
protected JComponent createUI()
{
    // Create panel
    SpringsPane galleryPanel = new SpringsPane();
    
    // Panel should be 275x260
    galleryPanel.setSize(275, 260);
    galleryPanel.setPreferredSize(new Dimension(275, 260));
    
    // Add new inner panel for selection buttons
    SpringsPane panel = new SpringsPane();
    panel.setBounds(1, 2, 271, 46);
    panel.setBorder(BorderFactory.createLoweredBevelBorder());
    
    // Add panel with stretch horizontal autosizing
    galleryPanel.add(panel, "-~-,--~");
     
    // Create new viewer (with first document)
    _viewer = new GViewer();
    _viewer.setContent(getDocument(0));
    _viewer.setBounds(0, 50, 275, 210);
    _viewer.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    
    // Add panel with stretch horizontal and vertical autosizing
    galleryPanel.add(_viewer, "-~-,-~-");
    
    // Iterate over gallery documents and create button for each
    for(int i=0, iMax=getDocumentCount(); i<iMax; i++) { Icon icon = getDocumentIcon(i);
        
        // Create button for icon, set bounds and add
        JToggleButton button = new JToggleButton(icon);
        button.setBounds(i*46 + (i==0? 3 : 5), 3, 40, 40);
        panel.add(button);
        
        // Add button to button group and if first button, select
        _bgroup.add(button);
        if(i==0)
            button.setSelected(true);
    }
    
    // Return panel
    return galleryPanel;
}

/**
 * Responds to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Find index of gallery button by iterating over button group buttons, incrementing index
    int index = 0;
    for(Enumeration e=_bgroup.getElements(); e.hasMoreElements(); index++)
        if(e.nextElement()==anEvent.getTarget())
            break;
    
    // If button for existing document was pressed, install that document
    if(index<getDocumentCount())
        _viewer.setContent(getDocument(index));
}

/**
 * Returns the number of document shown by the gallery.
 */
public int getDocumentCount()  { return 0; }

/**
 * Returns the specifc document at the given index.
 */
public RMParentShape getDocument(int anIndex)  { return null; }

/**
 * Returns the icon for the specific document at the given index.
 */
public Icon getDocumentIcon(int anIndex)  { return null; }

/**
 * Handle drop of gallery shape onto editor.
 */
public static void dropGalleryShape(RMParentShape aShape, DropTargetDropEvent anEvent)
{
    // Get editor
    RMEditor editor = (RMEditor)anEvent.getDropTargetContext().getComponent();
    
    // Get gallery shape and have it's tool get a gallery clone (a hook to allow drop to be different than gallery)
    RMShape galleryShape = Gallery.getDragShape();
    RMShape shape = RMTool.getTool(galleryShape).getGalleryClone(galleryShape);
    
    // Get/set shape location to drop point in destination shape coords
    RMPoint dpoint = editor.convertPointToShape(anEvent.getLocation(), aShape);
    if(DragSource.isDragImageSupported()) { dpoint.x -= shape.getWidth()/2; dpoint.y -= shape.getHeight()/2; }
    shape.setXY(dpoint.x, dpoint.y);

    // Add imageShape, select it and set SelectTool
    editor.undoerSetUndoTitle("Add Ribs Element");
    aShape.addChild(shape);
    
    // Select drag shape
    editor.setSelectedShape(shape);
    
    // Select SelectTool
    editor.setCurrentToolToSelectTool();
    
    // Snap it to the proximity guides, if available
    shape.setXY(RMEditorProxGuide.pointSnappedToProximityGuides(editor, shape.getFrameXY(), RMSelectTool.DragMode.Move));
    
    // Erase the guidelines
    RMEditorProxGuide.clearGuidelines(editor);
    
    // Take focus back from the gallery
    editor.requestFocus();
}

/**
 * An inner class and RMViewer subclass for showing and dragging shapes in an RMDocument.
 */
public static class GViewer extends RMViewer implements DragGestureListener
{
    /** Creates new gallery viewer. */
    public GViewer()
    {
        setZoomFactor(1);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    /** Create an no-op input adapter instead of default one. */
    protected RMViewerInputAdapter createInputAdapter()  { return new RMViewerInputAdapter(this); }
    
    /**
     * Called when drag should be initiated due to mouse drag. 
     */
    public void dragGestureRecognized(DragGestureEvent dge)
    {
        // Get drag shape at event point
        _dragShape = getShapeAtPoint(dge.getDragOrigin(), false); if(_dragShape==null) return;
        
        // Get image for drag shape
        float opacity = _dragShape.getOpacity(); _dragShape.setOpacity(.5f);
        Image image = new RMShapeImager().createImage(_dragShape);
        _dragShape.setOpacity(opacity);
        
        // Get Dragger and drag shape with image (with DragSourceListener to clear DragShape)
        DragSourceListener dsl = new DragSourceAdapter() {
            public void dragDropEnd(DragSourceDropEvent dsde) { _dragShape = null; }};
        SwingDragger dragger = new SwingDragger(); dragger.setDragGestureEvent(dge);
        dragger.setDragItem("Gallery:" + _dragShape.getClass().getSimpleName()); dragger.setDragImage(image);
        dragger.setDragSourceListener(dsl);
        dragger.startDrag();
    }
}

}