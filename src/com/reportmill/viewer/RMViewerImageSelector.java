package com.reportmill.viewer;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * A viewer input adapter to perform selection of a rectangle and copy as image.
 */
public class RMViewerImageSelector extends RMViewerInputAdapter {

    // The selection rect
    RMRect         _rect = new RMRect();
    
    // The down point for the last mouse loop
    Point          _downPoint;
    
    // The selected sides (a mask of sides)
    int            _selectedSides;
    
    // Whether to drag rect or not
    Point2D         _dragPoint;
    
/**
 * Creates a new image selector.
 */
public RMViewerImageSelector(RMViewer aViewer)  { super(aViewer); }

/**
 * Handle mouse pressed event.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Get down point
    _downPoint = anEvent.getPoint();
    
    // If rect isn't empty, repaint
    if(!_rect.isEmpty())
        getViewer().repaint();
    
    // Get rect in viewer coords
    RMRect rect = getViewer().convertRectFromShape(_rect, getViewer().getSelectedPage());
    
    // Reset selected sides to edges hit by point
    _selectedSides = rect.isEmpty()? 0 : rect.getHitEdges(anEvent.getPoint(), 5);
    
    // Set drag rect
    _dragPoint = _selectedSides>0 || !rect.contains(anEvent.getPoint())? null : anEvent.getPoint();
    
    // If no selected sides, reset rect
    if(_selectedSides==0 && _dragPoint==null)
        _rect = new RMRect();
}

/**
 * Handle mouse dragged event.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get rect in viewer coords
    RMRect rect = getViewer().convertRectFromShape(_rect, getViewer().getSelectedPage());
        
    // Repaint rect
    getViewer().repaint(rect.isEmpty()? getViewer().getBounds() : rect.insetRect(-5));
    
    // If there are selected sides, move them
    if(_selectedSides>0)
        rect.setHitEdges(anEvent.getPoint(), _selectedSides);
    
    // Otherwise, if point is in rect, move rect
    else if(_dragPoint!=null) {
        rect.offset(anEvent.getX() - _dragPoint.getX(), anEvent.getY() - _dragPoint.getY());
        _dragPoint = anEvent.getPoint();
    }
    
    // Otherwise, reset rect from down point and event event point
    else {

        // Get rect parts for down point and current event point
        double x = Math.min(_downPoint.getX(), anEvent.getX());
        double y = Math.min(_downPoint.getY(), anEvent.getY());
        double w = Math.max(_downPoint.getX(), anEvent.getX()) - x;
        double h = Math.max(_downPoint.getY(), anEvent.getY()) - y;
    
        // Get rect as rect
        rect = new RMRect(x, y, w, h);
    }
    
    // Set rect ivar in viewer coords
    _rect = getViewer().convertRectToShape(rect, getViewer().getSelectedPage());
    
    // Repaint rect
    getViewer().repaint(rect.isEmpty()? getViewer().getBounds() : rect.insetRect(-5));
}

/**
 * Handle mouse moved event.
 */
public void mouseMoved(MouseEvent anEvent)
{
    // Get point in selected page coords
    Point2D point = getViewer().convertPointToShape(anEvent.getPoint(), getViewer().getSelectedPage());
    
    // Get hit edges
    int hitEdges = _rect.isEmpty()? 0 : _rect.getHitEdges(point, 5);
    
    // If selected edge, set cursor
    if(hitEdges!=0)
        getViewer().setCursor(getResizeCursor(hitEdges));
    
    // If point in rect, set move cursor
    else if(_rect.contains(point))
        getViewer().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    
    // Otherwise, reset cursor
    else getViewer().setCursor(Cursor.getDefaultCursor());
}

/**
 * Handle paint.
 */
public void paint(Graphics2D g)
{
    // If selection rect is empty, just return
    if(_rect.isEmpty())
        return;
    
    // Get selection rect in viewer coords
    RMRect rect = getViewer().convertRectFromShape(_rect, getViewer().getSelectedPage());
    
    // Set color
    g.setColor(new Color(0, 0, 0, .667f));
    
    // Create area for bounds
    Area area = new Area(getViewer().getBounds());
    
    // Subtract rect
    area.subtract(new Area(rect));
    
    // Paint area
    g.fill(area);
    
    // Paint corners
    drawCircle(new Point2D.Double(rect.getX(), rect.getY()), g);
    drawCircle(new Point2D.Double(rect.getMidX(), rect.getY()), g);
    drawCircle(new Point2D.Double(rect.getMaxX(), rect.getY()), g);
    drawCircle(new Point2D.Double(rect.getX(), rect.getMidY()), g);
    drawCircle(new Point2D.Double(rect.getMaxX(), rect.getMidY()), g);
    drawCircle(new Point2D.Double(rect.getX(), rect.getMaxY()), g);
    drawCircle(new Point2D.Double(rect.getMidX(), rect.getMaxY()), g);
    drawCircle(new Point2D.Double(rect.getMaxX(), rect.getMaxY()), g);
}

/**
 * Handle copy.
 */
public void copy()
{
    // Get an image of the current page
    RMShape page = getViewer().getDocument().getSelectedPage();
    BufferedImage image = new RMShapeImager().setColor(Color.white).createImage(page);
    
    // Get sub image
    BufferedImage image2 = image.getSubimage((int)_rect.getX(), (int)_rect.getY(), (int)_rect.getWidth(), (int)_rect.getHeight());
    
    // Get transferable
    Transferable tr = new ImageSelectorTransferrable(image2);

    // Add to clipboard 
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
}

/**
 * Draws a circle.
 */
public void drawCircle(Point2D aPoint, Graphics2D g2)
{
    g2.setColor(Color.WHITE);
    g2.fill(new Ellipse2D.Double(aPoint.getX()-4, aPoint.getY()-4, 8, 8));
    g2.setColor(Color.GRAY);
    g2.fill(new Ellipse2D.Double(aPoint.getX()-3, aPoint.getY()-3, 6, 6));
}

/**
 * Returns a resize cursor for a rect edge mask.
 */
private static Cursor getResizeCursor(int anEdgeMask)
{
    // Handle W_RESIZE_CURSOR, E_RESIZE_CURSOR, N_RESIZE_CURSOR, S_RESIZE_CURSOR
    if(anEdgeMask==RMRect.MinXEdge) return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    if(anEdgeMask==RMRect.MaxXEdge) return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    if(anEdgeMask==RMRect.MinYEdge) return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    if(anEdgeMask==RMRect.MaxYEdge) return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
    
    // Handle NW_RESIZE_CURSOR, NE_RESIZE_CURSOR, SW_RESIZE_CURSOR, SE_RESIZE_CURSOR
    if(anEdgeMask==(RMRect.MinXEdge | RMRect.MinYEdge)) return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
    if(anEdgeMask==(RMRect.MaxXEdge | RMRect.MinYEdge)) return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
    if(anEdgeMask==(RMRect.MinXEdge | RMRect.MaxYEdge)) return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
    if(anEdgeMask==(RMRect.MaxXEdge | RMRect.MaxYEdge)) return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
    return null; // Return null since not found
}

/**
 * A transferable for copy.
 */
public static class ImageSelectorTransferrable implements Transferable {

    // The image
    BufferedImage  _image;
    
    /** Creates a new ImageSelectorTransferrable. */
    public ImageSelectorTransferrable(BufferedImage anImage) { _image = anImage; }

    /** Transferable method - returns the transfer dataflavors supported by this transferable. */
    public DataFlavor[] getTransferDataFlavors()
    { return new DataFlavor[] { DataFlavor.imageFlavor, new DataFlavor("image/jpeg", "JPEG Image Data") }; }

    /** Transferable method - returns whether the given flavor is supported. */
    public boolean isDataFlavorSupported(DataFlavor aFlavor)
    { return aFlavor.equals(DataFlavor.imageFlavor) || aFlavor.equals(new DataFlavor("image/jpeg", "JPEG Image Data"));}

    /** Transferable method - returns the transfer data for the specified flavor. */
    public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
    {
        if(aFlavor.equals(DataFlavor.imageFlavor)) return _image;
        if(aFlavor.equals(new DataFlavor("image/jpeg", "JPEG Image Data")))
            return new ByteArrayInputStream(RMAWTUtils.getBytesJPEG(_image));
        throw new UnsupportedFlavorException(aFlavor);
    }
}

}