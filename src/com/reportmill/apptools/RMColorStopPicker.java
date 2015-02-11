package com.reportmill.apptools;
import com.reportmill.graphics.*;
import com.reportmill.base.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import snap.swing.*;

/**
 * A multi-stop gradient inspector.
 */
public class RMColorStopPicker extends JPanel implements DropTargetListener {

    // The gradient
    RMGradientFill      _gradient;
    
    // The knobs
    List <Rectangle>    _knobs = new ArrayList();
    
    // The wells
    List <ColorWell>  _wells = new ArrayList();
    
    // The gradient rect
    Rectangle2D         _gradientRect;
    
    // The selected knob
    int                 _selectedKnob;
    
    // The knob images
    Image               _knobImages[];
    
    // The texture paint
    TexturePaint        _background;
    
    // The cursor
    Cursor              _addStopCursor;
    
    // The dropping point
    Point               _droppingPoint;
    
    // Constants
    public static final int WELL_SIZE = 24;
    public static final int KNOB_WIDTH = 19;
    public static final int KNOB_HEIGHT = 22;
    public static final int KNOB_BASELINE = 11;

/**
 * Creates new RMColorStopPicker.
 */
public RMColorStopPicker()
{
    // Clear layout
    setLayout(null);
    
    // load knob images
    _knobImages = new Image[2];
    _knobImages[0] = Swing.getImage("knob.png", RMColorStopPicker.class);
    _knobImages[1] = Swing.getImage("knob_hilighted.png", RMColorStopPicker.class);
    
    // add mouse listeners
    addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e)
        {
            if(_gradient==null) return;
            
            // find the part clicked in
            Point pt = e.getPoint();
            int sindex = findKnobIndex(pt);
            
            // Clicking inside gradient creates a new stop whose color is color just clicked on.
            // A drag after this moves the new stop.
            if ((sindex<0) && _gradientRect.contains(pt)) {
                sindex = addColorStop(pt);
                sendAction();
            }
            
            // Create and run the pop-up menu
            else if(e.isPopupTrigger() && _gradient.getColorStopCount()>2) {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem("Delete Color Stop");
                menuItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                    deleteColorStop(); }});
                popupMenu.add(menuItem);
                popupMenu.show(e.getComponent(), pt.x, pt.y);
                e.consume(); // Consume event.
            }
            
            selectStop(sindex);
        }
        
    });
    
    addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e)
        {
            if (_selectedKnob>=0) {
                adjustColorStop(_selectedKnob, e.getPoint());
                sendAction();
            }
        }
        // cursor (if the gradient were its own component, it could do this automatically
        boolean _inGradient;
        public void mouseMoved(MouseEvent e)
        {
            boolean in = _gradientRect.contains(e.getPoint()) && (findKnobIndex(e.getPoint())==-1);
            
            if (in != _inGradient) {
                _inGradient = !_inGradient;
                Cursor c = getCustomCursor(_inGradient);
                setCursor(c);
            }
        }
    });    

    // Add a drop target so colors can be dropped into gradient to create new stops
    setDropTarget(new DropTarget(this, this));
}

/**
 * Return picker gradient.
 */
public RMGradientFill getGradient()  { return _gradient; }

/**
 * Set picker gradient.
 */
public void setGradient(RMGradientFill aGradient)
{
    // Just return if already set
    if(RMUtils.equals(aGradient, _gradient)) return;
    
    // Get/set clone of given gradient
    _gradient = aGradient!=null? aGradient.clone() : null;
    
    // Deselect knob
    _selectedKnob = -1;
    
    // call revalidate to add/remove components
    resetComponents();
}

public Image getKnobImage(boolean isHilighted)  { return _knobImages[isHilighted ? 1 : 0]; }

public int findKnobIndex(Point pt)
{
    for(int i=0, iMax=_knobs.size(); i<iMax; i++) { Rectangle r = _knobs.get(i);
        if(r.contains(pt))
            return i; }
    return -1;
}

public void selectStop(int index)
{
    if (index != _selectedKnob) {
        _selectedKnob = index;
        repaint();
    }
}

/**
 * Given a mouse point within gradient rectangle, returns corresponding position in gradient
 * (0 at left of gradient, 1 at right).
 */
public float getStopPosition(Point pt)
{
    float position = (float)((pt.x -_gradientRect.getX()) / _gradientRect.getWidth());
    return position<0 ? 0 : (position>1 ? 1 : position);
}

/**
 * Given a stop index, returns the corresponding x coordinate in the gradient rectangle.
 */
public float getGradientCoordinate(int stopIndex)
{
    float position = _gradient.getStopPosition(stopIndex);
    return (float)(_gradientRect.getX() + position*_gradientRect.getWidth());
}

/**
 * create an explicit stop at the corresponding gradient position.
 */
public int addColorStop(Point pt)
{
    float position = getStopPosition(pt);
    
    // null for the color tells the gradient to use whatever color is currently at position
    int sindex = _gradient.insertColorStop(null, position);
    
    // call revalidate to create new components
    resetComponents();
    
    return sindex;
}

/**
 * Move the selected stop to the new position.
 */
public void adjustColorStop(int index, Point pt)
{
    // turn mouse point into a number in the range {0,1}
    float newposition = getStopPosition(pt);
        
    if (_gradient.getStopPosition(index) != newposition) {
        // move the stop
        _gradient.setColorStop(index, _gradient.getStopColor(index), newposition);
        // reset the control bounds so they're centered over the new position
        float x = getGradientCoordinate(index);
        ColorWell well = _wells.get(index);
        well.setBounds((int)(x-well.getWidth()/2), well.getY(), well.getWidth(), well.getHeight());
        Rectangle krect = _knobs.get(index);
        krect.x = (int)(x-krect.getWidth()/2);
        repaint();
    }
}

/**
 * Called whenever the gradient is reset or a stop is added or removed.  Lays out 
 * all the color wells, the knob images, and the gradient rect.
 */
public void resetComponents()
{
    // Reset wells, knobs and remove all children    
    _wells.clear();
    _knobs.clear();
    removeAll();
    
    // Reset gradient rect
    float w = getWidth(), h = getHeight();
    _gradientRect = new Rectangle2D.Float(WELL_SIZE/2, KNOB_BASELINE, w - WELL_SIZE, h-KNOB_BASELINE-WELL_SIZE-1);
    
    // If no gradient, just return
    if(_gradient==null) return;
    
    // Create bounds rects for knobs & color wells
    Rectangle wrect = new Rectangle(0, (int)_gradientRect.getMaxY()+1, WELL_SIZE, WELL_SIZE);
            
    // add wells & knobs for each stop
    for(int i=0, iMax=_gradient.getColorStopCount(); i<iMax; i++) {

        // Calc x of this stop in gradient rect - controls are placed above/below gradient, centered about this position
        float position = getGradientCoordinate(i);
        
        // Create, configure and add color well for this stop
        ColorWell well = new ColorWell();
        wrect.x = (int)(position - WELL_SIZE/2);
        well.setColor(_gradient.getStopColor(i).awt());
        well.setBounds(wrect.x, wrect.y, wrect.width, wrect.height);
        _wells.add(well);
        add(well);
        
        // Add action listener to ColorWell to update gradient
        well.addChangeListener(new ChangeListener() { public void stateChanged(ChangeEvent e) {
            colorWellChanged((ColorWell)e.getSource()); }});
        
        // Set the knob image rectangle for this stop
        _knobs.add(new Rectangle((int)(position - KNOB_WIDTH/2), 0, KNOB_WIDTH, KNOB_HEIGHT));
    }
    
    repaint();
}

/**
 * Called when a color well changes.
 */
protected void colorWellChanged(ColorWell aWell)
{
    int which = _wells.indexOf(aWell);
    RMColor color = new RMColor(aWell.getColor());
    _gradient.setColorStop(which, color, _gradient.getStopPosition(which));
    repaint();
    SwingUtilities.invokeLater(new Runnable() { public void run() { 
        sendAction(); }});
}

/**
 * Called when a ColorStop is deleted.
 */
public void deleteColorStop()
{
    _gradient.removeColorStop(_selectedKnob);
    _selectedKnob = -1; // deselect
    resetComponents();
    SwingUtilities.invokeLater(new Runnable() { public void run() { 
        sendAction(); }});
}

/**
 * Sends a Ribs action to anybody who's interested.
 */
private final void sendAction()  { Swing.sendEvent(new ChangeEvent(this)); }

/**
 * Paint Component.
 */
public void paintComponent(Graphics g)
{
    g.clearRect(0, 0, getWidth(), getHeight());

    if(_gradient != null) {
        Graphics2D g2 = (Graphics2D)g;
        RMRect r = new RMRect(_gradientRect);
         
        // create an awt Paint class to draw the gradient
        RMMultipleStopGradient gp = new RMMultipleStopGradient((float)r.getX(), 0, (float)r.getMaxX(), 0, _gradient);
         
        // Draw a background under gradients with alpha
        if (_gradient.hasAlpha()) {
            g2.setPaint(getBackgroundTexture());
            g2.fill(r);
        }
        
        // draw the gradient
        g2.setPaint(gp);
        g2.fill(r);
        
        // draw indicator for drag & drop
        if(_droppingPoint != null) {
            g2.setPaint(Color.blue);
            g2.drawLine(_droppingPoint.x, (int)_gradientRect.getY(), _droppingPoint.x, (int)_gradientRect.getMaxY());
            g2.draw(r);
            r.inset(1);
        }
 
        // draw the gradient rect
        g2.setPaint(Color.black);
        g2.draw(r);  
        
        // draw the knobs (back to front, so stacking order matches the wells)
        for(int i=_knobs.size()-1; i>=0; --i) { Rectangle k = _knobs.get(i);
            g2.drawImage(getKnobImage(i==_selectedKnob), (int)k.getX(), (int)k.getY(), this); }
     }
}

/**
 * Creates & returns a texture to be used for the background of transparent gradients
 */
public TexturePaint getBackgroundTexture()
{
    int cellsize=4;
    int w = 2*cellsize;
    
    if (_background==null) {
        BufferedImage im = new BufferedImage(w,w,BufferedImage.TYPE_INT_ARGB);
        Graphics g = im.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, w);
        g.setColor(new Color(168,193,255));
        g.fillRect(0,0,cellsize,cellsize);
        g.fillRect(cellsize,cellsize,cellsize,cellsize);
        _background = new TexturePaint(im, new Rectangle(0,0,w,w));
    }
    return _background;
}

public Cursor getCustomCursor(boolean inGradientRect)
{
    if (!inGradientRect) return null;
    if (_addStopCursor==null) {
        Image im = Swing.getImage("addStopCursor.png", getClass());
        _addStopCursor = Toolkit.getDefaultToolkit().createCustomCursor(im, new Point(6,16), "add hotspot");
    }
    return _addStopCursor;
}

/**
 * DropTargetListener method.
 */
public void dragEnter(DropTargetDragEvent dtde)
{
    if (dtde.isDataFlavorSupported(ColorWell.getColorDataFlavor())) {
        Point pt = dtde.getLocation();
        checkDrag(pt);
        dtde.acceptDrag(dtde.getDropAction());
    }
    else dtde.rejectDrag();
}

public void checkDrag(Point pt)
{
    Point newPt = (pt==null || !_gradientRect.contains(pt)) ? null : pt;
    if (!RMUtils.equals(_droppingPoint, newPt)) {
        _droppingPoint = newPt;
        repaint();
    }
}

/**
 * DropTargetListener method.
 */
public void dragExit(DropTargetEvent dte)  { checkDrag(null); }

/**
 * DropTargetListener method.
 */
public void dragOver(DropTargetDragEvent dtde)  { checkDrag(dtde.getLocation()); }

/**
 * DropTargetListener method.
 */
public void dropActionChanged(DropTargetDragEvent dtde)  { }

/**
 * DropTargetListener method.
 */
public void drop(DropTargetDropEvent dtde)
{
    if (_droppingPoint != null) {
        // Accept drop
        dtde.acceptDrop(dtde.getDropAction());
        
        // Get transferable
        Transferable t = dtde.getTransferable();
        
        // Try to accept dropped color
        try {
            Color color = (Color)t.getTransferData(ColorWell.getColorDataFlavor());
            _gradient.insertColorStop(new RMColor(color), getStopPosition(_droppingPoint));
            _droppingPoint=null;
            resetComponents();
            sendAction();
            dtde.dropComplete(true);
        }
        catch(UnsupportedFlavorException ufe) { System.err.println(ufe); dtde.dropComplete(false); }
        catch(IOException ioe) { System.err.println(ioe); dtde.dropComplete(false);}
    }
    else dtde.rejectDrop();
}

}