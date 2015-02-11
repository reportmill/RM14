package snap.swing;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import snap.util.PrefsUtils;

/**
 * A ColorWell subclass that handle a whole grid of color swatches, including drag and drop support.
 */
public class ColorDock extends ColorWell {

    // Whether changes to the dock cause the colors to be saved to the preferences database
    boolean    _persistent;
    
    // The size of the individual swatches
    Dimension  _swatchSize = new Dimension(13, 13);
    
    // A hashtable to map row,col coordinates to colors in the dock (which is a sparse array of unlimited size)
    Map        _colors = new Hashtable();
    
    // The selected point (swatch) in color dock
    Point      _selectedPoint = null;

/**
 * Creates a new color dock.
 */
public ColorDock()  { }

/**
 * Returns whether this doc writes itself out to preferences.
 */
public boolean isPersistent()  { return _persistent; }

/**
 * Sets whether this dock writes itself out to preferences.
 */
public void setPersistent(boolean aFlag)
{
    // Set persistent flag
    _persistent = aFlag;
    
    // If persistent, read colors from prefs
    if(_persistent)
        readFromPreferences(getName());
}

/**
 * Returns the size of individual color swatches.
 */
public Dimension getSwatchSize()  { return _swatchSize; }

/**
 * Sets the size of the individual color swatches.
 */
public void setSwatchSize(Dimension aSize)  { _swatchSize.setSize(aSize); repaint(); }

/**
 * Overrides ColorWell version to return color of selected swatch.
 */
public Color getColor()  { return _selectedPoint!=null? getColor(_selectedPoint) : super.getColor(); }

/**
 * Overrides color well version to set color of selected swatch.
 */
public void setColor(Color aColor)
{
    // If there is a selected point, set color
    if(_selectedPoint!=null)
        setColor(aColor, _selectedPoint);
    
    // Do normal version
    super.setColor(aColor);
}

/**
 * Returns the color at the given row & column.
 */
public Color getColor(int aRow, int aCol)
{
    // Get color from color map
    Color color = (Color)_colors.get(aRow + "," + aCol);
    
    // Return color (or just white if null)
    return color==null? Color.white : color;
}

/**
 * Sets the color at the given row & column.
 */
public void setColor(Color aColor, int aRow, int aCol)
{
    // Get key
    String key = aRow + "," + aCol;
    
    // If color isn't null, add to map
    if(aColor!=null)
        _colors.put(key, aColor);
    
    // If color is null, remove map key
    else _colors.remove(key);
}

/**
 * Returns the color at the given swatch index.
 */
public Color getColor(int anIndex)
{
    int row = anIndex/getColumnCount();
    int col = anIndex%getColumnCount();
    return getColor(row, col);    
}

/**
 * Sets the color at the given swatch index.
 */
public void setColor(Color aColor, int anIndex)
{
    int row = anIndex/getColumnCount();
    int col = anIndex%getColumnCount();
    setColor(aColor, row, col);
}

/**
 * Returns the color at the mouse location within the component.
 */
public Color getColor(Point aPoint)
{
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    return getColor(row, col);
}

/**
 * Returns the color at the mouse location within the component.
 */
public void setColor(Color aColor, Point aPoint)
{
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    setColor(aColor, row, col);
}

/**
 * Resets the colors in colordock to white.
 */
public void resetColors()  { _colors.clear(); }

/**
 * Returns the row for the given y coordinate.
 */
public int getRow(Point aPoint)
{
    Insets insets = getInsets();
    return (int)((aPoint.getY() - insets.top)/_swatchSize.height);
}

/**
 * Returns the column for the given x coordinate.
 */
public int getColumn(Point aPoint)
{
    Insets insets = getInsets();
    return (int)((aPoint.getX() - insets.left)/_swatchSize.width);
}

/**
 * Returns the number of rows in this color dock.
 */
public int getRowCount()
{
    Insets ins = getInsets();
    int height = getHeight() - (ins.top+ins.bottom);
    int swatchH = _swatchSize.height;
    return height/swatchH + (height%swatchH !=0 ? 1 : 0);    
}

/**
 * Returns the number of columns in this color dock.
 */
public int getColumnCount()
{
    Insets ins = getInsets();
    int width = getWidth() - (ins.left+ins.right);
    int swatchW = _swatchSize.width;
    return width/swatchW;// + (width%swatchW != 0 ? 1 : 0);
}

/**
 * Returns the total number of visible swatches.
 */
public int getSwatchCount()  { return getRowCount()*getColumnCount(); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return getSwatchIndex(_selectedPoint); }

/**
 * Returns the swatch index for given point.
 */
public int getSwatchIndex(Point aPoint)
{
    // If point is null, return -1
    if(aPoint==null)
        return -1;
    
    // Get row and column for selected point
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    
    // Return selected index
    return row*getColumnCount() + col;
}

/**
 * Returns the selected point.
 */
public Point getSelectedPoint()  { return _selectedPoint; }

/**
 * Sets the selected point.
 */
public void setSelectedPoint(Point aPoint)
{
    // Set selected point
    _selectedPoint = aPoint;
    
    // Repaint
    repaint();
}

/**
 * Overrides colorwell to reset selected point.
 */
public void setSelected(boolean select)
{
    // Do normal set selected
    super.setSelected(select);
    
    // Reset selected point if needed
    if(!isSelected())
        setSelectedPoint(null);
}

/**
 * Overridden from colorwell to use a lowered-bevel border.
 */
public void resetBorder()  { setBorder(BorderFactory.createLoweredBevelBorder()); }

/**
 * Paints this color dock component.
 */
public void paintComponent(Graphics g) 
{
    // Get bounds and insets
    Rectangle bounds = getBounds();
    Insets ins = getInsets();
    
    // Get content size
    int width = bounds.width-(ins.left+ins.right);
    int height = bounds.height-(ins.top+ins.bottom);
    
    // Get swatch size
    int swatchW = _swatchSize.width;
    int swatchH = _swatchSize.height;
    
    // Get row & column count
    int ncols = getColumnCount();
    int nrows = getRowCount();
    
    // Set color to white and fill background
    g.setColor(Color.white);
    g.fillRect(ins.left, ins.top, width, height);
    
    // Make as many rows & columns as will fit, and fill any that are present in the sparse array.
    for(int row=0; row<nrows; ++row) {
        for(int col=0; col<ncols; ++col) {
            Color color = getColor(row, col);
            ColorWell.paintSwatch(g, color, ins.left+col*swatchW+2, ins.top+row*swatchH+2, swatchW-3, swatchH-3);
        }
    }
    
    // Draw borders
    g.setColor(Color.lightGray);
    for(int row=0; row<=nrows; ++row)
        g.drawLine(ins.left, ins.top+row*swatchH, ins.left+width, ins.top+row*swatchH);
    for(int col=0; col<=ncols; ++col)
        g.drawLine(ins.left+col*swatchW, ins.top, ins.left+col*swatchW, ins.top+height);
    
    // If dragging, hilight the drag destination
    if(_selectedPoint != null) {
        
        // Get select row & column
        int row = getRow(_selectedPoint);
        int col = getColumn(_selectedPoint);
        
        // Get select x y
        int x = ins.left + col*swatchW;
        int y = ins.top + row*swatchH;
        
        // Draw red rect
        g.setColor(Color.red);
        g.drawRect(x,y,swatchW,swatchH);
        g.drawRect(x+1,y+1,swatchW-2,swatchH-2);
        g.setColor(Color.white);
        g.drawRect(x+2,y+2,swatchW-4,swatchH-4);
    }
}

/**
 * Implement mouse pressed to order color panel onscreen.
 */
protected void mousePressed(MouseEvent e)
{
    // If disabled, just return
    if(!isEnabled())
        return;
    
    // last point
    int lastPointIndex = getSwatchIndex(getSelectedPoint());
    
    // Update selected point
    setSelectedPoint(e.getPoint());
    
    // If colorDock is selectable, toggle selected state
    if(isSelectable()) {
        
        // If colordock isn't selected, select it
        if(!isSelected())
            setSelected(true);
        
        // If clicking the same swatch, deselect it
        else if(getSwatchIndex(e.getPoint())==lastPointIndex)
            setSelected(false);
    }
}
    
/**
 * Implement mouse pressed to order color panel onscreen.
 */
protected void mouseClicked(MouseEvent e)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // If colorwell isn't selectable, make the color panel display this color
    if(!isSelectable() || isSelected()) {
        ColorPanel panel = ColorPanel.getShared();
        panel.setColor(getColor());
        panel.resetLater();
    }
    
    // If not selectable, clear selection point
    if(!isSelectable())
        setSelectedPoint(null);
}

/** 
 * Update an individual color at {row,column} in the preferences
 */
public void saveToPreferences(String aName, int aRow, int aColumn) 
{
    // Get the app's preferences node
    Preferences root = PrefsUtils.prefs();
    
    // Create a sub-node for the list of colors
    Preferences dockNode = root.node(aName);
    
    // Catch exceptions
    try {
        
        // Get color
        Color c = getColor(aRow, aColumn);
        
        // Get rgb value
        int rgb = c.getRGB();
        
        // Get key
        String key = aRow + "," + aColumn;
        
        // If not white put value
        if(rgb!=0xFFFFFFFF)
            dockNode.putInt(key, rgb);

        // If color is white, remove value
        else dockNode.remove(key);
    }
    
    // Catch exceptions
    catch(Throwable t) { System.err.println("Error writing colors to preferences"); }
}

/**
 * Read color well color from preferences.
 */
public void readFromPreferences(String aName)
{
    // Get the app's preferences node
    Preferences root = PrefsUtils.prefs();

    // Reset colors map
    resetColors();
    
    // Handle exceptions
    try {
        
    // If named node doesn't exist, just return
    if(!root.nodeExists(aName))
        return;
    
    // Get named node
    Preferences dockNode = root.node(aName);
    
    // Get node keys
    String keys[] = dockNode.keys();

    // Iterate over keys
    for(int i=0; i<keys.length; i++) {
        
        // Get color rgb for current loop key
        int rgb = dockNode.getInt(keys[i],0xFFFFFFFF);
        
        // Get color from rgb
        Color color = new Color(rgb, true);
        
        // Add color to map
        _colors.put(keys[i], color);
    }
    
    // Catch exceptions
    } catch(Throwable t) { System.err.println("Error reading color dock from preferences"); }
}

/**
 * DropTargetListener method.
 */
public void dragEnter(DropTargetDragEvent dtde)
{
    if (dtde.isDataFlavorSupported(ColorWell.getColorDataFlavor())) {
        _selectedPoint = dtde.getLocation();
    
        dtde.acceptDrag(dtde.getDropAction());
        repaint();
    }
    else dtde.rejectDrag();
}

/**
 * DropTargetListener method.
 */
public void dragOver(DropTargetDragEvent dtde)
{
    // Get drag point
    Point newPoint = dtde.getLocation();
    
    // If drag point is over new swatch, reset drag point and repaint
    if(getRow(newPoint)!=getRow(_selectedPoint) || getColumn(newPoint)!=getColumn(_selectedPoint)) {
        _selectedPoint = newPoint;
        repaint();
    }
}

/**
 * DropTargetListener method.
 */
public void dragExit(DropTargetEvent dte)
{
    // Deselect color dock, since we used selected point for highlighting target
    setSelected(false);
}

/**
 * DropTargetListener method.
 */
public void drop(DropTargetDropEvent dtde)
{
    // Do normal drop
    super.drop(dtde);
    
    // Deselect color dock, since we used selected point for highlighting target
    setSelected(false);
}

/**
 * DropTargetListener method.
 */
public void dropColor(Color aColor, Point aPoint)
{ 
    // Get row and column for last drag point
    int row = getRow(aPoint);
    int col = getColumn(aPoint);
    
    // Set color
    setColor(aColor);
    setColor(aColor, row, col);
    
    // If color dock is persistent, save new color to preferences
    if(_persistent)
        saveToPreferences(getName(), row, col);
}

}