package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import snap.util.*;
import java.io.*;

/**
 * This Swing control class displays a color value and kicks off the ColorPanel when clicked.
 */
public class ColorWell extends JPanel implements DropTargetListener {
    
    // The current color
    Color                  _color = Color.black;
    
    // Whether the well can be selected 
    boolean                _selectable = true;
    
    // Whether color well is selected
    boolean                _selected;
    
    // Indicates that this well is the current drag source
    boolean                _dragging;
    
    // The listeners for this color well
    List <ChangeListener>  _changeListeners = new Vector();
    
    // Border for unselected state
    static Border          _border;
    
    // Shared border for selected state
    static Border          _borderSelected;
    
    // The shared color data flavor
    static DataFlavor      _colorDataFlavor = null;

/**
 * Creates a new color well for editing a specific color attribute (fill, stroke, text).
 */
public ColorWell()
{
    // Set border for unselected state
    resetBorder();
    
    // Enable mouse clicked
    enableEvents(MouseEvent.MOUSE_EVENT_MASK);
    
    // Add hierarchy listener to de-select color well when hidden
    addHierarchyListener(_hierarchyListener);
    
    // Add a drop target so colors can be dropped onto a well
    setDropTarget(new DropTarget(this, this));
    
    // Create a drag source so colorwell can be the source of color drags
    DragSource dsrc = new DragSource();
    
    // Create a drag gesture listener
    DragGestureListener dragGestureListener = new DragGestureListener()
    {
        public void dragGestureRecognized(DragGestureEvent dge)
        {
            Dimension d = new Dimension(14,14);
            Transferable t = getColorTransfer();
            Image swatchImage = getDragImage(d);
            Cursor dragCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            DragSourceListener listener = new DragSourceAdapter() {
                public void dragDropEnd(DragSourceDropEvent e) { _dragging=false; }
            };
                 
            _dragging = true;
            dge.startDrag(dragCursor,swatchImage, new Point(-d.width/2,-d.height/2), t, listener);
        }
    };
    
    // Add drag source
    dsrc.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
}

/**
 * Returns the color represented by this color well.
 */
public Color getColor()  { return _color; }

/**
 * Sets the color represented by this color well.
 */
public void setColor(Color aColor)
{
    // If color already set, just return
    if(SnapUtils.equals(aColor, _color)) return;
    
    // Set color
    _color = aColor;
    
    // Repaint
    repaint();
    
    // Fire ChangeListeners
    for(ChangeListener cl : _changeListeners)
        cl.stateChanged(new ChangeEvent(this));
}

/**
 * Returns whether color well is selected.
 */
public boolean isSelected()  { return _selected; }

/**
 * Sets whether color well is selected.
 */
public void setSelected(boolean aValue)
{
    // If disabled, just return
    if(!isEnabled())
        return;
    
    // Set new value
    _selected = aValue;
    
    // Set appropriate border
    resetBorder();

    // If color well is selected, set in color panel and make color panel visible
    if(_selected && ColorPanel.getShared().getColorWell()!=this) {
        
        // Set color well in shared color panel and make color panel visible
        ColorPanel.getShared().setColorWell(this);
        ColorPanel.getShared().setWindowVisible(true);
    }
    
    // If color well is de-selected, set color panel's color well to null
    else if(!_selected && ColorPanel.getShared().getColorWell()==this)
        ColorPanel.getShared().setColorWell(null);
}

/**
 * Returns whether or not the well can be selected.
 */
public boolean isSelectable()  { return _selectable; }

/**
 * Sets whether or not the well can be selected.
 */
public void setSelectable(boolean flag) 
{
    // Set flag
    _selectable = flag;
    
    // Reset border
    resetBorder();
}

/**
 * This just makes sure that any colorwell that is disabled is also deselected
 */
public void setEnabled(boolean enableIt)
{
    // If disabled, make sure color well is not selected
    if(!enableIt)
        setSelected(false);
    
    // Do normal set enable
    super.setEnabled(enableIt);
    
    // Reset border
    resetBorder();
}

/**
 * Set the border given the current selection state.
 */
public void resetBorder()
{
    // If not enabled, set border to simple gray line
    if(!isEnabled())
        setBorder(BorderFactory.createLineBorder(Color.lightGray, 4));
    
    // If selectable, set border based on whether selected
    else if(isSelectable())
        setBorder(_selected? getSharedBorderSelected() : getSharedBorder());
    
    // If not selectable, just set simple line border
    else setBorder(BorderFactory.createLineBorder(Color.black.equals(getColor()) ? Color.gray : Color.black, 1));
}

/**
 * Paints the color well.
 */
public void paintComponent(Graphics g) 
{
    // Do normal paint??
    super.paintComponent(g);
    
    // Get insets for this color well's border
    Insets insets = getBorder().getBorderInsets(this);
    
    // Calculate width/height of color well sans border
    int w = getWidth() - insets.left - insets.right;
    int h = getHeight() - insets.top - insets.bottom;
    
    // Paint color well area not covered by border
    if(getColor()!=null)
    ColorWell.paintSwatch(g, getColor(), insets.left, insets.top, w, h);
}

/**
 * Paints a color swatch in a standard way.  Used to paint color wells, drag images, and color docks.
 */
public static void paintSwatch(Graphics g, Color c, int x, int y, int w, int h)
{
    // Draw given color in given rect
    g.setColor(c);
    g.fillRect(x, y, w, h);
    
    // If color has an alpha component, fill half the well with a fully-opaque version of the color
    if(c.getAlpha() != 255) {
        
        // Get graphics 2d
        Graphics2D g2 = (Graphics2D)g;

        // Set color
        g.setColor(new Color(c.getRGB(), false));
        
        // Create path for triangle
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y+h);
        p.lineTo(x+w, y+h);
        p.lineTo(x+w, y);
        p.closePath();
        
        // Get old anti-alias setting and turn on
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill path
        g2.fill(p);
        
        // Restore anti-alias setting
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
    }        
}

/**
 * Calls mouse methods.
 */
protected void processMouseEvent(MouseEvent e)
{
    // Do normal version
    super.processMouseEvent(e);

    // Forward to mouse methods
    switch(e.getID()) {
        case MouseEvent.MOUSE_PRESSED: mousePressed(e); break;
        case MouseEvent.MOUSE_RELEASED: mouseReleased(e); break;
        case MouseEvent.MOUSE_CLICKED: mouseClicked(e); break;
    }
}

/**
 * Mouse pressed.
 */
protected void mousePressed(MouseEvent e)  { }

/**
 * Mouse released.
 */
protected void mouseReleased(MouseEvent e)  { }

/**
 * Mouse clicked.
 */
protected void mouseClicked(MouseEvent e)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // If is selectable, toggle selected state
    if(isSelectable())
        setSelected(!isSelected());
    
    // If isn't selectable, make the color panel display this color
    else {
        ColorPanel panel = ColorPanel.getShared();
        panel.setColor(getColor());
        panel.resetLater();
    }
}

/**
 * Returns the border for the unselected state.
 */
private Border getSharedBorder()
{
    // If border hasn't been created, create border
    if(_border==null) {
        Border b1 = BorderFactory.createLineBorder(Color.gray, 1);
        Border b2 = BorderFactory.createRaisedBevelBorder();
        Border b3 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        Border b4 = BorderFactory.createLoweredBevelBorder();
        _border = BorderFactory.createCompoundBorder(b1, BorderFactory.createCompoundBorder(b2,
            BorderFactory.createCompoundBorder(b3, b4)));
    }
    
    // Return shared border
    return _border;
}

/**
 * Returns the border for the selected state.
 */
private Border getSharedBorderSelected()
{
    // If border hasn't been created, create border
    if(_borderSelected==null) {
        Border b1 = BorderFactory.createLineBorder(Color.red, 1);
        Border b2 = BorderFactory.createLineBorder(Color.white, 4);
        _borderSelected = BorderFactory.createCompoundBorder(b1, b2);
    }
    
    // Return shared border
    return _borderSelected;
}

/**
 * Returns a transferable with this well's color.
 */
public Transferable getColorTransfer() 
{
    return new Transferable() {
        Color _c = getColor();
        public Object getTransferData(DataFlavor flav) { return _c; }
        public boolean isDataFlavorSupported(DataFlavor flav) { return flav==ColorWell.getColorDataFlavor(); }
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{ColorWell.getColorDataFlavor()};}
        };
}

/**
 * Returns an image with a swatch of this well's color.
 */
public Image getDragImage(Dimension d)
{
    BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    
    // paint the swatch
    ColorWell.paintSwatch(g, getColor(), 0, 0, d.width, d.height);
    // paint a black border around it
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setColor(Color.black);
    g.drawRect(0,0,d.width-1,d.height-1);
    return image;
}


/**
 * Shared hierarchy listener for detecting when component is hidden.
 */
private static HierarchyListener _hierarchyListener = new HierarchyListener() {
    
    /** Catch any change in the hierarchy to see if color well is no longer in visible window. */
    public void hierarchyChanged(HierarchyEvent e)
    {
        // Get the color well
        ColorWell colorWell = (ColorWell)e.getComponent();
        
        // If color well is selected but not showing, de-select it
        if(colorWell.isSelected() && !colorWell.isShowing())
            colorWell.setSelected(false);
    }
};

/**
 * Adds an action listener to the color well.
 */
public void addChangeListener(ChangeListener l)  { _changeListeners.add(l); }

/**
 * Removes given action listener from the color well.
 */
public void removeActionListener(ActionListener l)  { _changeListeners.remove(l); }

/**
 * Returns a DataFlavor object which represents colors for Drag & Drop.
 */
public static DataFlavor getColorDataFlavor()
{ 
    // Create flavor (first time only)
    if(_colorDataFlavor==null)
        _colorDataFlavor = new DataFlavor(java.awt.Color.class, "Color");
    
    // Return flavor
    return _colorDataFlavor;
}

/**
 * DropTargetListener method.
 */
public void dragEnter(DropTargetDragEvent dtde)
{
    // don't drag onto ourselves
    if ((!_dragging) && dtde.isDataFlavorSupported(ColorWell.getColorDataFlavor())) {
        dtde.acceptDrag(dtde.getDropAction());
        
        Border b1 = BorderFactory.createLineBorder(Color.blue, 1);
        Border b2 = getBorder();
        Border b3 = BorderFactory.createCompoundBorder(b1, b2);
        setBorder(b3);
    }
    else dtde.rejectDrag();
}

/**
 * DropTargetListener method.
 */
public void dragExit(DropTargetEvent dte)  { resetBorder(); }

/**
 * DropTargetListener method.
 */
public void dragOver(DropTargetDragEvent dtde)  { }

/**
 * DropTargetListener method.
 */
public void dropActionChanged(DropTargetDragEvent dtde)  { }

/**
 * DropTargetListener method.
 */
public void drop(DropTargetDropEvent dtde)
{
    // Probably redundant.  We already did this check in dragEnter, and you can't have a drop without an enter first.
    if(dtde.isDataFlavorSupported(ColorWell.getColorDataFlavor())) {

        // Accept drop
        dtde.acceptDrop(dtde.getDropAction());
        
        // Get transferable
        Transferable t = dtde.getTransferable();
        
        // Try to accept dropped color
        try {
            Color color = (Color)t.getTransferData(ColorWell.getColorDataFlavor());
            dropColor(color, dtde.getLocation());
            dtde.dropComplete(true);
        }
        
        // Catch exceptions
        catch(UnsupportedFlavorException ufe) { System.err.println(ufe); dtde.dropComplete(false); }
        catch(IOException ioe) { System.err.println(ioe); dtde.dropComplete(false);}
    }
    
    // If not color flavor, reject drop
    else dtde.rejectDrop();
    
    // Reset border
    resetBorder();
}

/**
 * Called when a valid color has been dropped.
 */
public void dropColor(Color aColor, Point aPoint)  { setColor(aColor); }

/**
 * Provides Ribs specific support for color well.
 */
public static class Helper <T extends ColorWell> extends snap.swing.SwingHelpers.JComponentHpr <T> {

    /** Override to install ChangeListener. */
    public void initUI(T aColorWell, UIOwner anOwner)
    {
        super.initUI(aColorWell, anOwner);
        aColorWell.addChangeListener(getChangeListener());
    }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Color"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return "Color"; // Map Value to Color
        return super.getPropertyNameMapped(anObj, aName);
    }
}

}