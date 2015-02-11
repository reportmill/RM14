package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import snap.swing.ClipboardUtils;
import snap.util.XMLElement;

/**
 * Handles editor methods specific to clipboard operations (cut, copy paste).
 */
public class RMEditorClipboard implements Transferable {
    
    // Bytes for when this class is used as a transferable
    byte _bytes[];

    // A defined data flavor for RM shapes
    public static DataFlavor RMDataFlavor = new DataFlavor("application/reportmill", "ReportMill Shape Data");
    
    // DataFlavors supported by RMEditor
    public static DataFlavor SupportedFlavors[] = { RMDataFlavor, DataFlavor.stringFlavor };
    
/**
 * Creates new editor clipboard object with the given bytes.
 */
public RMEditorClipboard(byte bytes[]) { _bytes = bytes; }

/**
 * Handles editor cut operation.
 */
public static void cut(RMEditor anEditor)
{
    // If text editing, have text editor do copy instead
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().cut();
    
    // If not text editing, do copy and delete (and null anchor & smart paste shape)
    else {
        anEditor.copy();
        anEditor.delete();
        anEditor._lastCopyShape = anEditor._lastPasteShape = null;
    }
}

/**
 * Handles editor copy operation.
 */
public static void copy(RMEditor anEditor)
{
    // If text editing, have text editor do copy instead
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().copy();

    // If not text editing, add selected shapes (serialized) to pasteboard for DrawPboardType
    else if(!(anEditor.getSelectedOrSuperSelectedShape() instanceof RMDocument) &&
            !(anEditor.getSelectedOrSuperSelectedShape() instanceof RMPage)) {
        
        // Get xml for selected shapes, create and set in EditorClipboard and install in SystemClipboard
        XMLElement xml = new RMArchiver().writeObject(anEditor.getSelectedOrSuperSelectedShapes());
        RMEditorClipboard ec = new RMEditorClipboard(xml.getBytes());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ec, null);
        
        // Reset Editor.LastCopyShape/LastPasteShape
        anEditor._lastCopyShape = anEditor.getSelectedShape(0); anEditor._lastPasteShape = null;
    }
    
    // Otherwise beep
    else Toolkit.getDefaultToolkit().beep();
}

/**
 * Handles editor paste operation.
 */
public static void paste(RMEditor anEditor)
{
    // If text editing, have text editor do paste instead
    if(anEditor.getTextEditor()!=null)
        anEditor.getTextEditor().paste();
    
    // If not text editing, do paste for system clipboard transferable
    else {
        
        // Get system clipboard and its transferable
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = cb.getContents(null);
        
        // If transferable, find parent shape and paste
        if(contents!=null) {
            RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
            paste(anEditor, contents, parent, null);
        }
        
        // If no transferable, just beep
        else Toolkit.getDefaultToolkit().beep();
    }
}

/**
 * Handles editor paste operation for given transferable, parent shape and location.
 */
public static void paste(RMEditor anEditor, Transferable contents, RMParentShape aParent, Point2D aPoint)
{
    // Declare variable for pasted shape
    RMShape pastedShape = null;

    // If PasteBoard has ReportMill Data, paste it
    if(contents.isDataFlavorSupported(RMDataFlavor)) try {
        
        // Unarchive shapes from clipboard bytes
        Object object = getShapesFromClipboard(anEditor, contents);
        
        // If data is list of previously copied shapes, add them
        if(object instanceof List) {
            List shapes = (List)object;
            anEditor.undoerSetUndoTitle("Paste Shape" + (shapes.size()>1? "s" : ""));
            anEditor.addShapesToShape(shapes, aParent, true);
            anEditor.setSelectedShapes(shapes);
        }
        
        // If data is text, create text object and add it
        else if(object instanceof RMXString) {
            RMTextShape text = new RMTextShape((RMXString)object);
            double width = Math.min(text.getPrefWidth(), aParent.getWidth());
            double height = Math.min(text.getPrefHeight(), aParent.getHeight());
            text.setSize(width, height);
            anEditor.undoerSetUndoTitle("Paste Text");
            anEditor.addShapesToShape(Arrays.asList(text), aParent, true);
            anEditor.setSelectedShape(text);
        }
        
        // Promote _smartPastedShape to anchor and set new _smartPastedShape
        if(anEditor._lastPasteShape!=null)
            anEditor._lastCopyShape = anEditor._lastPasteShape;
        anEditor._lastPasteShape = anEditor.getSelectedShape(0);
        
    }
    
    // Catch paste RMData exceptions
    catch(Exception e) { e.printStackTrace(); }
    
    // Paste Image
    else if(contents.isDataFlavorSupported(DataFlavor.imageFlavor)) try {
        Image image = (Image)contents.getTransferData(DataFlavor.imageFlavor);
        byte bytes[] = RMAWTUtils.getBytesJPEG(image);
        pastedShape = new RMImageShape(bytes);
    }
    
    // Catch paste image exceptions
    catch(Exception e) { e.printStackTrace(); }
    
    // paste pdf
    else if((pastedShape=getTransferPDF(contents)) != null) { }
    
    // last one - plain text
    else if((pastedShape=getTransferText(contents)) != null) { }
        
    // Might as well log unsupported paste types
    else {
        DataFlavor flavors[] = contents.getTransferDataFlavors();
        for(int i=0; i<flavors.length; i++)
            System.err.println("Unsupported flavor: " + flavors[i].getMimeType() + " " + flavors[i].getSubType());
    }

    // Add pastedShape
    if(pastedShape!=null) {
        
        // Set undo title
        anEditor.undoerSetUndoTitle("Paste");
        
        // Resize/relocate shape (if point was provided, move pasted shape to that point)
        pastedShape.setBestSize();
        if(aPoint!=null) {
            aPoint = anEditor.convertPointToShape(aPoint, aParent);
            pastedShape.setXY(aPoint.getX() - pastedShape.getWidth()/2, aPoint.getY() - pastedShape.getHeight()/2);
        }
        
        // Add pasted shape to parent
        aParent.addChild(pastedShape);

        // Select imageShape, set selectTool and redisplay
        anEditor.setSelectedShape(pastedShape);
        anEditor.setCurrentToolToSelectTool();
        anEditor.repaint();
    }
}

/**
 * Returns the first Shape read from the system clipboard.
 */
public static RMShape getShapeFromClipboard(RMEditor anEditor)
{
    Object object = getShapesFromClipboard(anEditor, null);
    if(object instanceof List) object = RMListUtils.get((List)object, 0);
    return object instanceof RMShape? (RMShape)object : null;
}

/**
 * Returns the shape or shapes read from the given transferable (uses system clipboard if null).
 */
public static Object getShapesFromClipboard(RMEditor anEditor, Transferable contents)
{
    // If no contents, use system clipboard
    if(contents==null)
        contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);        
    
    // If PasteBoard has ReportMill Data, paste it
    if(contents.isDataFlavorSupported(RMDataFlavor)) try {
    
        // Get bytes from clipboard
        ByteArrayInputStream bis = (ByteArrayInputStream)contents.getTransferData(RMDataFlavor);
        byte bytes[] = new byte[bis.available()];
        bis.read(bytes);
        
        // Get unarchived object from clipboard bytes
        Object object = new RMArchiver().readObject(bytes);

        // A bit of a hack - remove any non-shapes (plugins for one)
        if(object instanceof List) { List list = (List)object;
            for(int i=list.size()-1; i>=0; --i)
                if(!(list.get(i) instanceof RMShape))
                    list.remove(i);
        }
        
        // Return object
        return object;
    }
    
    // Handle exceptions and return
    catch(Exception e) { e.printStackTrace(); }
    return null;
}

/**
 * Returns an RMText object with the contents if there's a plain text string on the clipboard.
 */
public static RMShape getTransferText(Transferable contents) 
{
    String string = ClipboardUtils.getString(contents);
    return string==null? null : new RMTextShape(string);
}

/**
 * Returns an RMImage with the contents if there's a pdf image on the clipboard.
 */
public static RMShape getTransferPDF(Transferable contents) 
{
    try {
        DataFlavor pdflav = new DataFlavor("application/pdf");
        if(contents.isDataFlavorSupported(pdflav)) {
            InputStream ps = (InputStream)contents.getTransferData(pdflav);
            if(ps!=null) return new RMImageShape(ps);
        }
    }
    catch(Exception e) { e.printStackTrace(); } return null;
}

/**
 * Transferable method - returns the transfer data flavors supported by this transferable.
 */
public DataFlavor[] getTransferDataFlavors()  { return SupportedFlavors; }

/**
 * Transferable method - returns whether the given flavor is supported.
 */
public boolean isDataFlavorSupported(DataFlavor aFlavor)
{
    return aFlavor.equals(RMDataFlavor) || aFlavor.equals(DataFlavor.stringFlavor);
}

/**
 * Transferable method - returns the transfer data for the specified flavor.
 */
public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
{
    if(aFlavor.equals(RMDataFlavor)) return new ByteArrayInputStream(_bytes);
    if(aFlavor.equals(DataFlavor.stringFlavor)) return new String(_bytes);
    throw new UnsupportedFlavorException(aFlavor);
}

}