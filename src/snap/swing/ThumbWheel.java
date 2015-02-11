package snap.swing;
import com.reportmill.base.RMRect;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import snap.util.*;

/**
 * This class has the behavior of a slider, without boundaries (so you can set values beyond the visible min and max).
 * Furthermore, it has a 3D thumbwheel look, that is particularly nice for radial values. It also has an optional linear
 * look, which is good for unbounded linear values.
 */
public class ThumbWheel extends JComponent {

    // The type of thumbwheel (radial or linear)
    byte             _type = TYPE_RADIAL;

    // Value
    double           _value = 0;
    
    // Amount to round value to
    float            _round = 0;
    
    // Visible min/max
    float            _visibleMin = 0;
    float            _visibleMax = 100;
    
    // Absolute min/max
    float            _absoluteMin = -Float.MAX_VALUE;
    float            _absoluteMax = Float.MAX_VALUE;
    
    // Absolute mode
    byte             _absoluteMode = ABSOLUTE_BOUNDED;
    
    // Orientation (horizontal, vertical)
    int              _orientation = SwingConstants.HORIZONTAL;
    
    // Wether value is in the process of changing interactively
    boolean          _valueIsAdjusting = false;

    // Whether value snaps back after current change sequence
    boolean          _snapsBack = false;
    
    // Value to snap back to if snaps-back
    float            _snapBackValue = 0f;

    // How often to draw a dash (in points or degs)
    int              _dashInterval = 10;
    
    // Set this to NO if you want relative vals
    boolean          _showMainDash = true;

    // Mouse location at last press
    Point            _pressedMousePoint;
    
    // Value at last press
    double           _pressedValue;

    // Background of Thumbwheel in radial mode
    BufferedImage    _image;
    
    // Shared map of images
    static Hashtable _images = new Hashtable();
    
    // Constants for type
    public static final byte TYPE_RADIAL = 0;
    public static final byte TYPE_LINEAR = 1;
    
    // Constants for absolute behavior
    public static final byte ABSOLUTE_BOUNDED = 0;
    public static final byte ABSOLUTE_WRAPPED = 1;

/**
 * Creates a new thumbwheel.
 */
public ThumbWheel()
{
    enableEvents(MouseEvent.MOUSE_EVENT_MASK|MouseEvent.MOUSE_MOTION_EVENT_MASK); // Turn on mouse events
    setBackground(Color.lightGray); // Set default color
    setOpaque(true); // Set opaque  //setFocusable(true);
}

/**
 * Returns the type (radial or linear).
 */
public byte getType()  { return _type; }

/**
 * Sets the type (radial or linear).
 */
public void setType(byte aType)  { _type = aType; }

/**
 * Returns the value.
 */
public double getValue()  { return _round==0? _value : SnapMath.round(_value, _round); }

/**
 * Sets the value.
 */
public void setValue(double aValue)
{
    // Clamp or Wrap aValue wrt the absoluteMode
    if(aValue<getAbsoluteMin() || aValue>getAbsoluteMax()) {
        if(isBounded()) aValue = SnapMath.clamp(aValue, _absoluteMin, _absoluteMax);
        else if(isWrapped()) aValue = SnapMath.clamp_wrap(aValue, _absoluteMin, _absoluteMax);
    }

    // Set value
    _value = aValue;
    
    // Notify listeners
    for(ChangeListener l : listenerList.getListeners(ChangeListener.class))
        l.stateChanged(new ChangeEvent(this));
    
    // Repaint
    repaint();
}

/** Returns the value that thumbwheel values are rounded to. */
public float getRound() { return _round; }

/** Sets the value that thumbwheel values are rounded to. */
public void setRound(float aValue) { _round = aValue; }

/** Returns the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
public float getVisibleMin() { return _visibleMin; }

/** Sets the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
public void setVisibleMin(float aValue) { _visibleMin = aValue; }

/** Returns the largest value in the visible range (ie, on the right side) of the thumbhweel. */
public float getVisibleMax() { return _visibleMax; }

/** Sets the largest value in the visible range (ie, on the right side) of the thumbhweel. */
public void setVisibleMax(float aValue) { _visibleMax = aValue; }

/** Returns the smallest value permitted by the thumbwheel (even when outside visible range). */
public float getAbsoluteMin() { return _absoluteMin; }

/** Sets the smallest value permitted by the thumbwheel (even when outside visible range). */
public void setAbsoluteMin(float aValue) { _absoluteMin = aValue; }

/** Returns the largest value permitted by the thumbwheel (even when outside visible range). */
public float getAbsoluteMax() { return _absoluteMax; }

/** Sets the largest value permitted by the thumbwheel (even when outside visible range). */
public void setAbsoluteMax(float aValue) { _absoluteMax = aValue; }

/** Returns the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
public byte getAbsoluteMode() { return _absoluteMode; }

/** Sets the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
public void setAbsoluteMode(byte aValue) { _absoluteMode = aValue; }

/** Returns the orientation (SwingConstants.VERTICAL or SwingConstants.HORIZONTAL). */
public int getOrientation() { return _orientation; }

/** Returns the orientation (SwingConstants.VERTICAL or SwingConstants.HORIZONTAL). */
public void setOrientation(int aValue) { _orientation = aValue; }

/** Returns whether the thumbweel is in a state of rapid interactive use (from mouse loop). */
public boolean getValueIsAdjusting() { return _valueIsAdjusting; }

/**
 * Forwards mouse events to mouse methods.
 */
protected void processMouseEvent(MouseEvent e)
{
    if(!isEnabled()) return; // If disabled, just return
    super.processMouseEvent(e); // Do normal version
    if(e.getID()==MouseEvent.MOUSE_PRESSED) mousePressed(e); // Forward to mouse methods
    else if(e.getID()==MouseEvent.MOUSE_RELEASED) mouseReleased(e);
}

/**
 * Forwards mouse events to mouse methods.
 */
protected void processMouseMotionEvent(MouseEvent e)
{
    if(!isEnabled()) return; // If disabled, just return
    super.processMouseMotionEvent(e); // Do normal version
    if(e.getID()==MouseEvent.MOUSE_DRAGGED) mouseDragged(e); // Forward to mouse methods
}

/**
 * Mouse Pressed.
 */
protected void mousePressed(MouseEvent anEvent)
{
    //requestFocusInWindow(); // Request focus
    _pressedMousePoint = anEvent.getPoint(); // Record pressed mouse point
    _pressedValue = getValue(); // Record pressed pressed value
    _valueIsAdjusting = true; // Set value is adjusting
}

/**
 * Mouse Pressed.
 */
protected void mouseDragged(MouseEvent anEvent)
{
    // Get values for last point and current point
    double lastPointVal = getValueAtPoint(_pressedMousePoint);
    double currPointVal = getValueAtPoint(anEvent.getPoint());
    
    // If bounded and we are already at absoluteMax and currentPoint is greater, return 
    if(isBounded() && SnapMath.equals(getValue(), getAbsoluteMax()) && currPointVal>getAbsoluteMax()) return;

    // If bounded and we are already at absoluteMin and currPoint is less, return
    if(isBounded() && SnapMath.equals(getValue(), getAbsoluteMin()) && currPointVal<getAbsoluteMin()) return;
    
    // Set the float value relative to last point
    setValue(_pressedValue + (currPointVal - lastPointVal));
}

/**
 * Mouse Released.
 */
public void mouseReleased(MouseEvent anEvent)
{
    // Turn off value is adjusting
    _valueIsAdjusting = false;
    
    // Get values for last point and current point
    double lastPointVal = getValueAtPoint(_pressedMousePoint);
    double currPointVal = getValueAtPoint(anEvent.getPoint());
    
    // If bounded and we are already at absoluteMax and currentPoint is greater, return 
    if(isBounded() && SnapMath.equals(_value, _absoluteMax) && currPointVal>_absoluteMax) return;

    // If bounded and we are already at absoluteMin and currPoint is less, return
    if(isBounded() && SnapMath.equals(_value, _absoluteMin) && currPointVal<_absoluteMin) return;
    
    // Set the float value relative to last point
    setValue(_pressedValue + (currPointVal - lastPointVal));
}

/**
 * This method gives the value that corresponds to a point with respect to the given frame and the visible range.
 * When in radial mode, the point on the thumbwheel is approximated with a power series for arcCos to get legal values
 * for points outside of the frame.
 */
public double getValueAtPoint(Point aPoint)
{
    // Get stuff
    double pos = isVertical()? getHeight() - aPoint.y : aPoint.x;
    double width = isVertical()? getHeight() : getWidth();
    
    // If linear, just return linear extrapolation of point
    if(isLinear()) return getVisibleMin() + pos*getVisibleRange()/width;
        
    // Get radius
    double radius = width/2;
    double x = (radius - pos)/radius;
        
    // Get degrees by pwr series approximation of ArcCos (Pi/2 - x - x^3/6)
    double angle = Math.PI/2 - x - x*x*x/6;
    
    // Convert angle to thumbwheel coords
    return getVisibleMin() + angle*getVisibleRange()/Math.PI;
}

/**
 * Returns whether thumbwheel is vertical.
 */
public boolean isVertical() { return getOrientation()==SwingConstants.VERTICAL; }

/**
 * Returns whether thumbwheel is horizontal.
 */
public boolean isHorizontal() { return getOrientation()==SwingConstants.HORIZONTAL; }

/**
 * Returns whether thumbwheel is radial.
 */
public boolean isRadial() { return getType()==TYPE_RADIAL; }

/**
 * Returns whether thumbwheel is linear.
 */
public boolean isLinear() { return getType()==TYPE_LINEAR; }

/**
 * Returns whether thumbwheel is absolute bounded.
 */
public boolean isBounded() { return getAbsoluteMode()==ABSOLUTE_BOUNDED; }

/**
 * Returns whether thumbwheel does absolute wrapping.
 */
public boolean isWrapped() { return getAbsoluteMode()==ABSOLUTE_WRAPPED; }

/**
 * Returns the extent of the thumbwheel's visible range.
 */
public float getVisibleRange() { return getVisibleMax() - getVisibleMin(); }

/**
 * Paints the component.
 */
public void paintComponent(Graphics g)
{
    // Get the graphics2D
    Graphics2D g2 = (Graphics2D)g;
    
    // Get thumbwheel color
    Color color = getBackground();
    
    // Draw linear background
    if(isLinear()) {
        g2.setColor(color);
        g2.fill3DRect(0, 0, getWidth(), getHeight(), false);
        g2.fillRect(2, 2, getWidth()-4, getHeight()-4);
    }
    
    // Otherwise draw radial background
    else {
        if(_image==null) _image = getThumbWheelBackgroundImage();
        g.drawImage(_image, 0, 0, getWidth(), getHeight(), null);
    }
    
    // Get the userpath for the dashes
    GeneralPath path = getThumbWheelDashes();

    // Draw dashes once for white part of groove
    if(isHorizontal()) g2.translate(1, 0);
    else g2.translate(0, 1);
    g2.setStroke(new BasicStroke(1));

    // Get inset thumbwheel width/height
    int width = getWidth()-4;
    int height = getHeight()-4;
    
    // Draw linear white dashes
    if(isLinear()) { g2.setColor(color.brighter()); g2.draw(path); }
    
    // Break up radial white dashes to fade a little bit at ends
    else {
        g.setColor(color); g2.draw(path);
        
        Rectangle r = g.getClipBounds();
        if(isHorizontal()) g.clipRect(width/4, 0, width/2, height);
        else g.clipRect(0, height/4, width, height/2);
        g.setColor(color.brighter());
        g2.draw(path);
        g.setClip(r.x, r.y, r.width, r.height);
    }
    
    if(isHorizontal()) g2.translate(-1, 0);
    else g2.translate(0, -1);

    // Draw again for dark part of groove
    if(isLinear()) g.setColor(color.darker());
    else g.setColor(Color.black);
    g2.draw(path);
    
    // If disabled then dim ThumbWheel out
    if(!isEnabled()) {
        g2.setColor(new Color(1f, 1f, 1f, .5f));
        g2.fillRect(2, 2, width, height);
    }
}

/**
 * Returns a Java2D shape for painting thumbwheel dashes.
 */
private GeneralPath getThumbWheelDashes()
{
    float minX = 2, minY = 2, maxX = getWidth(), maxY = getHeight(), fwidth = maxX-4, height = maxY-4;
    
    // Get dashInterval (in pnts or degs depending on display mode) and shift
    GeneralPath path = new GeneralPath();
    float length = isVertical()? height - 1f : fwidth;
    int dashInt = isLinear()? _dashInterval : (int)Math.round(360/(Math.PI*length/_dashInterval));
    int shift = getShift();

    // Calculate dash sizes
    float dashBase = isVertical()? minX : minY;
    float dashHeight = isVertical()? fwidth : height;
    float dashMinTop = dashBase + dashHeight*.25f, dashMajTop = dashBase + dashHeight*.5f, dashTop = dashBase + dashHeight;

    float base = isVertical()? minY : minX;
    float width = length, halfWidth = width/2;
    float mid = base + halfWidth, top = base + width;
    
    // Calculate whether first dash is a major one
    boolean isMajor = (shift>=0)? isEven(shift/dashInt) : !isEven(shift/dashInt);
        
    // Calculate Linear dashes
    if(isLinear()) {

        // Set Main dash
        float mainDash = base + shift;

        // Calculate starting point and set the dashes
        double x = SnapMath.clamp_wrap(shift,0,dashInt);
        x = SnapMath.mod(x, ((shift>=0)? dashInt : 999999));
        x += base;
        
        if(isVertical()) while(x<top) {
            path.moveTo(dashBase, maxY - x);
            float value = isMajor? dashMajTop : dashMinTop;
            if(SnapMath.equals(x, mainDash) && _showMainDash) value = dashTop;
            path.lineTo(value, maxY - x);
            x += dashInt;
            isMajor = !isMajor;
        }
        
        else while(x<top) {
            path.moveTo(x, maxY - dashBase);
            float value = isMajor? dashMajTop : dashMinTop;
            if(SnapMath.equals(x, mainDash) && _showMainDash) value = dashTop;
            path.lineTo(x, maxY - value);
            x += dashInt;
            isMajor = !isMajor;
        }
    }

    // Calculate Radial Dashes
    else {

        // Inset dash size for beveled edges
        dashBase++;
        dashTop--;
        
        // Calc Main dash if we show it and it is in sight
        double mainDash = mid - Math.cos(shift*Math.PI/180f)*halfWidth;

        // Calculate the starting point and set the dashes
        double x = SnapMath.clamp_wrap(shift, 0, dashInt);
        x = SnapMath.mod(x, ((shift>=0)? dashInt : 999999));
        
        if(isVertical()) while(x<180) {
            double linDash = mid - Math.cos(x*Math.PI/180f)*halfWidth;
            path.moveTo(dashBase, linDash);

            // Check to see if this is a valid main dash
            double value = isMajor? dashMajTop : dashMinTop;
            if(isMajor && SnapMath.equals(linDash,mainDash) && _showMainDash && SnapMath.between(shift, 0, 180))
                value = dashTop;
                
            path.lineTo(value, linDash);
            x += dashInt;
            isMajor = !isMajor;
        }

        else while(x<180) {
            double linDash = mid - Math.cos(x*Math.PI/180f)*halfWidth;
            path.moveTo(linDash, maxY - dashBase);
            
            // Check to see if this is a valid main dash
            double value = isMajor? dashMajTop : dashMinTop;
            if(isMajor && SnapMath.equals(linDash, mainDash) && _showMainDash && SnapMath.between(shift, 0, 180))
                value = dashTop;
            
            path.lineTo(linDash, maxY - value);
            x += dashInt;
            isMajor = !isMajor;
        }
    }

    return path;
}

/**
 * Returns shift.
 */
private int getShift()
{
    // Handle linear
    if(isLinear()) {
        if(isHorizontal()) return (int)Math.round((_value - _visibleMin)/getVisibleRange()*getWidth() + .5f);
        return (int)Math.round((_value - _visibleMin)/getVisibleRange()*getHeight() +.5f);
    }
    
    // Handle radial
    return (int)Math.round((_value - _visibleMin)/getVisibleRange()*180f + .5f);
}

/**
 * Returns the background image if radial thumbwheel (keeps a cache based on orientation, size & color).
 */
private BufferedImage getThumbWheelBackgroundImage()
{
    // Get the thumbwheel color
    Color color = getBackground();
    
    // Generate imageName
    String imageName = (isVertical()? "V" : "H") + getWidth() + "x" + getHeight() + "_" + color.getRGB();
    
    // Try to find new image (return if already created/cached)
    BufferedImage image = (BufferedImage)_images.get(imageName);
    if(image!=null)
        return image;
    
    // Get new image and put in cache, then draw button background
    image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    
    // Draw button background
    g.setColor(color);
    g.fill3DRect(0, 0, getWidth(), getHeight(), false);

    // Get rects for top 2 pixels, middle and bottom 2 pixels
    RMRect topRect = new RMRect(2, 2, getWidth()-4, getHeight()-4);
    RMRect middleRect = topRect.divideRect(2, isVertical()? RMRect.MinXEdge : RMRect.MinYEdge);
    RMRect bottomRect = middleRect.divideRect(middleRect.height - 2, isVertical()? RMRect.MinXEdge : RMRect.MinYEdge);
    
    // Draw topRect brighter, middle normal and bottom darker
    drawThumbWheelGradation(g, image, topRect, color.brighter());
    drawThumbWheelGradation(g, image, middleRect, color);
    drawThumbWheelGradation(g, image, bottomRect, color.darker());
    
    // Add image to map
    _images.put(imageName, image);
    
    // Return image
    return image;
}

/**
 * Draws radial image.
 */
private void drawThumbWheelGradation(Graphics2D g2, BufferedImage image, Rectangle2D.Double rect, Color color)
{
    int thumbWheelLength = isHorizontal()? image.getWidth() : image.getHeight();
    float r = color.getRed(), g = color.getGreen(), b = color.getBlue();
    float radius = (thumbWheelLength-1)/2f, radiusSquared = radius*radius;

    // Fill strip image with color components for each point along the thumbWheelLength
    BufferedImage strip = new BufferedImage(getWidth(), 1, BufferedImage.TYPE_INT_RGB);
    for(int i=0; i<thumbWheelLength; i++) {

        // Calculate the height of the thumbwheel at current point
        float h = (float)Math.sqrt(radiusSquared - (radius-i)*(radius-i))/radius;
        
        // Get red, green and blue component of color (scaled for the height)
        int ri = Math.round(r*h), gi = Math.round(g*h), bi = Math.round(b*h);
        int val = (ri<<16) + (gi<<8) + bi;
        strip.setRGB(i, 0, val);
    }
    
    // Draw strip into image
    g2.drawImage(strip, 0, (int)rect.y, (int)rect.width, (int)rect.height, null);
}

/** Adds a change listener. */
public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

/** Remove a change listener. */
public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

/**
 * Override to reset image and set orientation.
 */
public void setBounds(int x, int y, int width, int height)
{
    if(width!=getWidth() || height!=getHeight()) _image = null; // If size changed, reset image
    super.setBounds(x, y, width, height); // Do normal version
    _orientation = width>height? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL; // Reset orientation
}

/**
 * Overrides to reset image.
 */
public void setForeground(Color aColor)  { super.setForeground(aColor); _image = null; }

/**
 * Overrides to reset image.
 */
public void setBackground(Color aColor)  { super.setBackground(aColor); _image = null; }

/**
 * Returns whether a number is even (not odd).
 */
private boolean isEven(float aValue)  { return Math.round(aValue)%2==0; }

/**
 * SwingHelper for ThumbWheel.
 */
public static class Helper <T extends ThumbWheel> extends snap.swing.SwingHelpers.JComponentHpr <T> {

    /** Override to add ChangeListener. */
    public void initUI(T aThumbWheel, UIOwner anOwner)
    {
        super.initUI(aThumbWheel, anOwner);
        aThumbWheel.addChangeListener(getChangeListener());
    }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Value"); super.addPropNames(); }
}

}