package com.reportmill.viewer;
import com.reportmill.base.*;
import com.reportmill.graphics.RMPath;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * A viewer input adapter to perform text selection and copy (as RTF and CSV).
 */
public class RMViewerTextSelector extends RMViewerInputAdapter {

    // The list of text shapes selected
    List <RMTextShape>    _selectedTexts = new ArrayList();
    
    // The down point for the last mouse loop
    Point            _downPoint;

    // The drag point for the last mouse loop
    Point            _dragPoint;

    // The paint area
    Area             _paintArea = new Area();

/**
 * Creates a new text selector.
 */
public RMViewerTextSelector(RMViewer aViewer)  { super(aViewer); }

/**
 * Handle mouse pressed event.
 */
public void mousePressed(MouseEvent anEvent)
{
    // Get down point
    _downPoint = anEvent.getPoint();
    
    // Repaint paint area
    getViewer().repaint(_paintArea.getBounds());
    
    // Reset area
    _paintArea = new Area();
}

/**
 * Handle mouse dragged event.
 */
public void mouseDragged(MouseEvent anEvent)
{
    // Get drag point
    _dragPoint = anEvent.getPoint();
    
    // Repaint paint area
    getViewer().repaint(_paintArea.getBounds());
    
    // Get rectangle for down point and current event point
    double x = Math.min(_downPoint.getX(), anEvent.getX());
    double y = Math.min(_downPoint.getY(), anEvent.getY());
    double w = Math.max(_downPoint.getX(), anEvent.getX()) - x;
    double h = Math.max(_downPoint.getY(), anEvent.getY()) - y;
    RMRect rect = new RMRect(x, y, w, h);
    
    // Convert rect to page
    rect = getViewer().convertRectToShape(rect, getViewer().getSelectedPage());
    
    // Get path for rect
    RMPath path = new RMPath(rect);
    
    // Clear selected texts rect
    _selectedTexts = new ArrayList();
    
    // Find text shapes
    findTextShapes(getViewer().getSelectedPage(), path, _selectedTexts);
    
    // Get selection paint area
    _paintArea = getTextSelectionArea();
    
    // Repaint paint area
    getViewer().repaint(_paintArea.getBounds());
}

/**
 * Handle mouse released event.
 */
public void mouseReleased(MouseEvent anEvent) { }

/**
 * Handle paint.
 */
public void paint(Graphics2D g)
{
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .33f));
    g.setColor(Color.BLUE); g.fill(_paintArea);
}

/**
 * Handle copy.
 */
public void copy()
{
    // Get first selected text (just return if none)
    RMTextShape stext = _selectedTexts.size()>0? _selectedTexts.get(0) : null; if(stext==null) return;
    RMDocument sdoc = stext.getDocument();
    
    // Create new document and add clone of SelectedTexts to new document
    RMDocument doc = new RMDocument(sdoc.getPageSize().width, sdoc.getPageSize().height);
    for(RMTextShape text : _selectedTexts) { RMTextShape clone = text.clone();
        doc.getPage(0).addChild(clone); }
    
    // Get transferable
    Transferable tr = new TextSelectorTransferrable(doc);

    // Add to clipboard 
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
}

/**
 * Finds the text shape children of the given shape in the given rect. Recurses into child shapes.
 */
public void findTextShapes(RMParentShape aParent, RMPath aPath, List aList)
{
    // Get list of hit shapes
    List <RMShape> shapes = aParent.getChildrenIntersecting(aPath);
    
    // Iterate over shapes
    for(int i=0, iMax=shapes.size(); i<iMax; i++) { RMShape shape = shapes.get(i);
        
        // If shape is text, just add it
        if(shape instanceof RMTextShape)
            aList.add(shape);
        
        // Otherwise if shape has children, recurse (with path converted to shape coords)
        else if(shape instanceof RMParentShape) { RMParentShape parent = (RMParentShape)shape;
            RMPath path = parent.convertPathFromShape(aPath, aParent);
            findTextShapes(parent, path, aList);
        }
    }
}

/**
 * Returns the text selection shape.
 */
public Area getTextSelectionArea()
{
    // Declare an area
    Area area = new Area();
    
    // Create a text editor
    RMTextEditor textEditor = new RMTextEditor();
    
    // Iterate over texts
    for(int i=0, iMax=_selectedTexts.size(); i<iMax; i++) { RMTextShape text = _selectedTexts.get(i);
        
        // Convert points to text
        RMPoint p1 = getViewer().convertPointToShape(_downPoint, text);
        RMPoint p2 = getViewer().convertPointToShape(_dragPoint, text);
        
        // Configure text editor for text
        textEditor.setXString(text.getXString());
        textEditor.setBounds(0, 0, text.getWidth(), text.getHeight());
        
        // Get text selection for point
        RMTextSel sel = textEditor.getSel(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        
        // Get path for selection
        Shape path = textEditor.getLayout().getPathForChars(sel.getStart(), sel.getEnd());
        
        // Create transformed shape
        path = getViewer().getTransformFromShape(text).createTransformedShape(path);

        // Add shape
        area.add(new Area(path));
    }
    
    // Return area
    return area;
}

/**
 * A transferable for copy.
 */
public static class TextSelectorTransferrable implements Transferable {

    // The doc
    RMDocument _doc;
    
    /** Creates a new TextSelectorTransferrable. */
    public TextSelectorTransferrable(RMDocument aDoc) { _doc = aDoc; }

    /** Transferable method - returns the transfer dataflavors supported by this transferable. */
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[] { DataFlavor.stringFlavor, new DataFlavor("text/rtf", "Rich Formatted Text") };
    }

    /** Transferable method - returns whether the given flavor is supported. */
    public boolean isDataFlavorSupported(DataFlavor aFlavor)
    {
        return aFlavor.equals(DataFlavor.stringFlavor) || aFlavor.equals(new DataFlavor("text/rtf", "Rich Formatted Text"));
    }

    /** Transferable method - returns the transfer data for the specified flavor. */
    public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
    {
        if(aFlavor.equals(DataFlavor.stringFlavor))
            return new String(_doc.getBytesCSV());
        if(aFlavor.equals(new DataFlavor("text/rtf", "Rich Formatted Text")))
            return new ByteArrayInputStream(_doc.getBytesRTF());
        else throw new UnsupportedFlavorException(aFlavor);
    }
}

}