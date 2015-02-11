package com.reportmill.swing.tool;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * Provides base inspector UI for Swing Buttons and Labels.
 */
public class LabeledTool <T extends LabeledShape> extends JComponentTool <T> {

    // Whether shape is being dragged
    boolean     _inDrag;

    // The horizontal/vertical alignment prior to drag
    int         _oldHorizontalAlign, _oldVerticalAlign;
    
    // The icon prior to drag
    Icon        _oldIcon;
    
    // The border prior to drag
    RMBorder    _oldBorder;

/**
 * Updates the UI panel controls from the currently selected shape.
 */
public void resetUI()
{
    // Get the currently selected button or label shape and component (just return if null)
    LabeledShape labeled = getSelectedShape(); if(labeled==null) return;
  
    // Set ImageNameText
    setNodeValue("ImageNameText", labeled.getImageName());
     
    // select appropriate alignment button from the 3x3 matrix
    int ha = labeled.getHorizontalAlignment();
    int va = labeled.getVerticalAlignment();
    int p = (ha==SwingConstants.LEFT || ha==SwingConstants.LEADING)? 0 : (ha==SwingConstants.CENTER? 1 : 2);
    p += va==SwingConstants.CENTER? 3 : (va==SwingConstants.BOTTOM? 6 : 0);
    setNodeValue("align" + p, true);
    
    // do the same for the text alignment matrix
    int htp = labeled.getHorizontalTextPosition();
    int vtp = labeled.getVerticalTextPosition();
    
    // LEADING & TRAILING are handled like LEFT & RIGHT, although that's only valid in a left-right language.
    // Some other special UI would be needed to add LEADING & TRAILING separately.
    p = (htp==SwingConstants.LEFT || htp==SwingConstants.LEADING)? 0 : (htp==SwingConstants.CENTER? 1 : 2);
    p += vtp==SwingConstants.CENTER? 3 : (vtp==SwingConstants.BOTTOM? 6 : 0);
    setNodeValue("tpos" + p, true);
}

/**
 * Updates the current shape from shape UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // If no selected shapes, just return
    if(getSelectedShape()==null) return;

    // Get object (component) name (just return if null)
    String name = anEvent.getName();
  
    // Iterate over selected shapes
    for(LabeledShape shape : (List<LabeledShape>)getSelectedShapes()) {
    
        // Handle ImageNameText
        if(name.equals("ImageNameText"))
            shape.setImageName(anEvent.getStringValue());
      
        // Handle alignN
        else if(name.startsWith("align")) {
            int which = name.charAt(5)-'0'; // The content alignment buttons.  "align0" - "align8"
            switch(which/3) {
                case 0: shape.setVerticalAlignment(SwingConstants.TOP); break;
                case 1: shape.setVerticalAlignment(SwingConstants.CENTER); break;
                case 2: shape.setVerticalAlignment(SwingConstants.BOTTOM); break;
            }
            switch(which%3) {
                case 0 : shape.setHorizontalAlignment(SwingConstants.LEFT); break;
                case 1 : shape.setHorizontalAlignment(SwingConstants.CENTER); break;
                case 2 : shape.setHorizontalAlignment(SwingConstants.RIGHT); break;
            }
        }
        
        // Handle tposN
        else if(name.startsWith("tpos")) {
            int which = name.charAt(4)-'0'; // Text alignment buttons.  "tpos0" - "tpos8"
            switch(which/3) {
                case 0: shape.setVerticalTextPosition(SwingConstants.TOP); break;
                case 1: shape.setVerticalTextPosition(SwingConstants.CENTER); break;
                case 2: shape.setVerticalTextPosition(SwingConstants.BOTTOM); break;
            }
            switch(which%3) {
                case 0 : shape.setHorizontalTextPosition(SwingConstants.LEFT); break;
                case 1 : shape.setHorizontalTextPosition(SwingConstants.CENTER); break;
                case 2 : shape.setHorizontalTextPosition(SwingConstants.RIGHT); break;
            }
        }
    }
}

/**
 * Editor method.
 */
public boolean acceptsDrag(T aLabeledShape, DropTargetDragEvent anEvent)
{
    // If drag is a file list, accept drag
    if(anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        
        // If not yet in drag, cache some values
        if(!_inDrag) {
            
            // Get old alignments
            _oldHorizontalAlign = aLabeledShape.getHorizontalAlignment();
            _oldVerticalAlign = aLabeledShape.getVerticalAlignment();
            
            // Get original icon and border, reset border and set InDrag
            _oldIcon = aLabeledShape.getIcon();
            _oldBorder = aLabeledShape.getBorder();
            aLabeledShape.setBorder(new RMBorder.LineBorder(RMColor.blue, 1));
            _inDrag = true;
        }
        
        // Return true for file dragging
        return true;
    }
  
    // Return false for anything but file dragging
    return super.acceptsDrag(aLabeledShape, anEvent);
}

/**
 * Editor method.
 */
public void dragOver(RMShape aShape, DropTargetDragEvent anEvent)
{
    // If drag isn't file list, do normal dragOver
    if(!anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        super.dragOver(aShape, anEvent); return; }
        
    // Get LabeledShape and component
    LabeledShape shape = (LabeledShape)aShape;
    
    // Get shape bounds, event point in shape coords and point x/y
    RMRect bounds = shape.getBounds();
    RMPoint point = getEditor().convertPointToShape(anEvent.getLocation(), aShape);
    double px = point.x/bounds.width;
    double py = point.y/bounds.height;
    
    // Center alignment is probably what you want most often, so make hit area for the center larger than the others.
    int h = px<.2 ? JLabel.LEFT : (px<.8 ? JLabel.CENTER : JLabel.RIGHT);
    int v = py<.2 ? JLabel.TOP : (py<.8 ? JLabel.CENTER : JLabel.BOTTOM);
    
    // Declare variable for whether icon changed
    boolean changed = false;
    if(shape.getHorizontalAlignment() != h) { shape.setHorizontalAlignment(h); changed = true; }
    if(shape.getVerticalAlignment() != v) { shape.setVerticalAlignment(v); changed = true; }
    if(changed || shape.getIcon()==null) {
        shape.setIcon(getDragImage(h,v));
        shape.repaint();
    }
}

public void dragExit(RMShape aShape, DropTargetDragEvent anEvent) 
{
    // If drag isn't file list, do normal dragOver
    if(!anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        super.dragExit(aShape, anEvent); return; }
        
    _inDrag = false;
    
    // Get LabeledShape and reset icon, alignment, border
    LabeledShape shape = (LabeledShape)aShape;
    shape.setIcon(_oldIcon);
    shape.setHorizontalAlignment(_oldHorizontalAlign);
    shape.setVerticalAlignment(_oldVerticalAlign);
    shape.setBorder(_oldBorder);
}

public void drop(T aShape, DropTargetDropEvent anEvent) 
{
    try { dropImpl(aShape, anEvent); }
    catch(Exception e) { e.printStackTrace(); }
}

private void dropImpl(T aLabeledShape, DropTargetDropEvent anEvent) throws UnsupportedFlavorException, IOException
{
    // If drag isn't file list, do normal dragOver
    if(!anEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        super.drop(aLabeledShape, anEvent); return; }
        
    // Get drop transferable
    Transferable tr = anEvent.getTransferable();
  
    // Only fileLists are accepted by acceptsDrag
    List <File> filesList = (List)tr.getTransferData(DataFlavor.javaFileListFlavor);
        
    // AcceptsDrag should check that drag is only for a single file, and that the extension is an image,
    // but the DragEvent doesn't give you access to the transferrable, only the drop event.
    // It's pretty stupid to show feedback when dragging a random file over button/label but then rejecting it on drop.
    // Sun added a getTransferable() to dragEvent in 1.5
    if(filesList.size()>0) {
        
        // Get first file
        File file = filesList.get(0);
        
        // If file isn't directory
        if(!file.isDirectory() && file.canRead()) {
            
            // Set the button or label image name
            aLabeledShape.setImageName(file.getName());
            
            // Set the button or label icon
            aLabeledShape.setIcon(new ImageIcon(file.getPath()));
            
            // If document is unsaved, run warning message dialog that unsaved document can't copy image file
            if(aLabeledShape.getDocument().getFilename()==null) {
                String msg = "Image can't be copied to file directory until document is saved";
                DialogBox dbox = new DialogBox("Unsaved document can't copy image file"); dbox.setWarningMessage(msg);
                dbox.showMessageDialog(getEditor());
            }
            
            // Otherwise, move image to document directory or peer ribs directory
            else {
                
                // Get file for document and parent directory
                File docFile = new File(aLabeledShape.getDocument().getFilename());
                File parent = docFile.getParentFile();
                
                // Get out file: If peer ribs directory exists, copy file, otherwise copy to parent
                File peerRibsDir = new File(docFile.getPath() + 's');
                File outFile = peerRibsDir.exists() && peerRibsDir.isDirectory()? peerRibsDir : parent;
                
                // If out file candidate was found copy file over and inform user
                if(outFile!=null) {
                    File destination = new File(outFile, file.getName());
                    
                    // Don't do anything if the file is already in the right place.
                    if(!destination.equals(file)) {
                        boolean docopy = true;
                        
                        // If a file with the same name is already there, ask for confirmation.
                        if(destination.exists()) {
                            String msg = "The file " + file.getName() + " already exists in the directory " +
                                outFile.getPath() + ".\nReplace exisiting file?";
                            DialogBox dbox = new DialogBox("Copy Image File"); dbox.setWarningMessage(msg);
                            docopy = dbox.showConfirmDialog(getEditor());
                        }

                        // Copy file and run warning message dialog informing user of copy
                        if(docopy) {
                            RMFileUtils.copyFileSafe(file, outFile);
                            String msg = "The image file was copied to the rib directory:\n" + outFile.getPath();
                            DialogBox dbox = new DialogBox("Image file was copied to document directory");
                            dbox.setWarningMessage(msg); dbox.showMessageDialog(getEditor());
                        }
                    }
                }
            }
        }
    }
    
    // Reset border, turn off drag
    aLabeledShape.setBorder(_oldBorder);
    _inDrag = false;
}

/**
 * Returns a drag alignment image based on given x & y.
 */
private static Icon getDragImage(int h, int v) 
{
    // Get corrected x & y, get image name and return
    int x = h==JLabel.LEFT ? 0 : (h==JLabel.CENTER ? 1 : 2);
    int y = v==JLabel.TOP ? 0 : (v==JLabel.CENTER ? 1 : 2);
    String imageName = "align" + (y*3+x) + ".png";
    return Swing.getIcon(imageName, LabeledTool.class);
}

/**
 * Declare LabeledShapes to be SuperSelectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overrides RMTool implementation to...
 */
public void didBecomeSuperSelectedShapeInEditor(RMShape aShape, RMEditor anEditor)
{
    // Do normal didBecomeSuperSelected
    super.didBecomeSuperSelectedShapeInEditor(aShape, anEditor);
    
    // Get LabeledShape and text shape
    LabeledShape lshape = (LabeledShape)aShape;
    RMTextShape tshape = new RMTextShape(); tshape.setBounds(lshape.getBoundsInside());
    tshape.setText(lshape.getText());
    tshape.setFont(lshape.getFont());
    tshape.setName(lshape.getName());
    tshape.setAlignmentX(lshape.getAlignmentX());
    tshape.setAlignmentY(lshape.getAlignmentY());
    
    // Set TextShape as child of TextComponentShape and super-select
    lshape.undoerDisable();
    lshape.addChild(tshape);
    lshape.undoerEnable();
    anEditor.setSuperSelectedShape(tshape);
    
    // Clear labeled text
    lshape.setText("");
}

/**
 * Overrides RMTool implementation to...
 */
public void willLoseSuperSelectionInEditor(RMShape aShape, RMEditor anEditor)
{
    // Do normal willLoseSuperSelection
    super.willLoseSuperSelectionInEditor(aShape, anEditor);
    
    // Get LabeledShape and text shape
    LabeledShape lshape = (LabeledShape)aShape;
    RMTextShape tshape = (RMTextShape)lshape.getChild(0);
    
    // Set text, font and alignment in TextComponentShape
    lshape.setText(tshape.getText());
    lshape.setFont(tshape.getFont());
    lshape.setName(tshape.getName());
    lshape.setAlignmentX(tshape.getAlignmentX());
    lshape.setAlignmentY(tshape.getAlignmentY());

    // Remove TextShape as child of TextComponentShape
    lshape.undoerDisable();
    lshape.removeChildren();
    lshape.undoerEnable();
}

}