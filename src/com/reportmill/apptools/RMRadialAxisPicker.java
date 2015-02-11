package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.graphics.RMRadialGradientFill;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.*;
import snap.swing.Swing;

/**
 * RadialAxis Picker.
 */
public class RMRadialAxisPicker extends JComponent {

    public RMRadialGradientFill _gradient;
    public Point2D _startPoint;
    public Point2D _endPoint;
    public boolean _dragging;
    public TexturePaint _background;
    
public RMRadialAxisPicker()
{
    setBorder(BorderFactory.createLoweredBevelBorder());
    _startPoint = new Point2D.Float(0.5f, 0.5f);
    _endPoint = new Point2D.Float(1.0f, 0.5f);
    
    // create a mouse adapter to forward the interesting events to this component.
    MouseInputAdapter m = new MouseInputAdapter() {
        public void mousePressed(MouseEvent e)  { RMRadialAxisPicker.this.mousePressed(e); }
        public void mouseReleased(MouseEvent e)  { RMRadialAxisPicker.this.mouseReleased(e); }
        public void mouseDragged(MouseEvent e)  { RMRadialAxisPicker.this.mouseDragged(e); }
    };
    
    addMouseListener(m);
    addMouseMotionListener(m);
}

public void setGradient(RMRadialGradientFill g)
{
    if (RMUtils.equals(g, _gradient))
        return;
    
    _gradient = g;
    _startPoint.setLocation(g._beginPoint);
    _endPoint.setLocation(g._endPoint);
    repaint();
}

public RMRadialGradientFill getGradient()  { return _gradient; }

/**
 * Returns the starting point for gradient, defined as point in unit square where the first color stop is drawn.
 */
public Point2D getStartPoint()  { return _startPoint; }

/**
 * Returns the ending point for the gradient, defined as a point in the unit square where a circle centered at 
 * _startPoint and with the color of the last color stop, passes through.
 */
public Point2D getEndPoint()  { return _endPoint; }

/**
 * Surely this exists somewhere else?
 */
public Rectangle getBoundsInside()
{
    Insets ins = getInsets();
    return new Rectangle(ins.left, ins.top, getWidth() - (ins.left+ins.right), getHeight()-(ins.top+ins.bottom));
}

public void paintComponent(Graphics g)
{
    // If no gradient, just return
    if(_gradient==null) return;
    
    // Get graphics and rect
    Graphics2D g2 = (Graphics2D)g;
    Rectangle r = getBoundsInside();
     
    // Scale gradient points from unit square into this component's drawing area
    float sx = (float)(r.getX() + _startPoint.getX()*r.getWidth());
    float sy = (float)(r.getY() + _startPoint.getY()*r.getHeight());
    float ex = (float)(r.getX() + _endPoint.getX()*r.getWidth());
    float ey = (float)(r.getY() + _endPoint.getY()*r.getHeight());
     
    // Create an awt Paint class to draw the gradient
    Paint gp = _gradient.getPaint(sx, sy, ex, ey);
     
    // Draw a background under gradients with alpha
    if(_gradient.hasAlpha()) {
        g2.setPaint(getBackgroundTexture());
        g2.fill(r);
    }
    
    // draw the gradient
    g2.setPaint(gp);
    g2.fill(r);
    
    // draw axis if dragging
    if(_dragging) {
        int xsize = 3;
        g2.setPaint(Color.black);
        g2.drawLine((int)sx, (int)sy, (int)ex, (int)ey);
        g2.setPaint(Color.green); // green for start, red for stop.  how original
        g2.drawLine((int)(sx-xsize),(int)sy, (int)(sx+xsize), (int)sy);
        g2.drawLine((int)sx, (int)(sy-xsize), (int)sx, (int)(sy+xsize));
        g2.setPaint(Color.red);
        g2.drawLine((int)(ex-xsize),(int)ey, (int)(ex+xsize), (int)ey);
        g2.drawLine((int)ex, (int)(ey-xsize), (int)ex, (int)(ey+xsize));
    }
}

// Converts a point in the component's coordinate system into a point in the unit and stores the result in dstPoint
void convertPoint(Point2D srcPoint, Point2D dstPoint)
{
    Rectangle r = getBoundsInside();
    dstPoint.setLocation((srcPoint.getX()-r.getMinX()) / r.getWidth(), (srcPoint.getY()-r.getMinY()) / r.getHeight());
}

public void mousePressed(MouseEvent e)
{
    Point pt = e.getPoint();
    convertPoint(pt, _startPoint);
    convertPoint(pt, _endPoint);
    _dragging = true;
    repaint();
}

public void mouseDragged(MouseEvent e)
{
    Point pt = e.getPoint();
    convertPoint(pt, _endPoint);
    repaint();
}

public void mouseReleased(MouseEvent e)
{
    // Reset dragging flag and register for repaint
    _dragging = false;
    repaint();
    
    // Send ChangeEvent
    Swing.sendEvent(new ChangeEvent(this));
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

}