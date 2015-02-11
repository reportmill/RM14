package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import snap.util.*;

/**
 * This class represents a fill that draws a linear gradient between an arbitrary list of colors.
 */
public class RMGradientFill extends RMFill {
    
    // Gradient colors
    List<ColorStop>    _colorStops = new ArrayList<ColorStop>(2);
    
    // Gradient rotation angle
    float              _roll = 0;
    
/**
 * Creates an uninitialized gradient fill.
 */
public RMGradientFill()  { this(RMColor.black, RMColor.white, 0f); }

/**
 * Creates a gradient fill from the given start color to the given end color with the given roll.
 */
public RMGradientFill(RMColor aColor1, RMColor aColor2, float aRotation)
{
    _colorStops.add(new ColorStop(aColor1, 0.0f));
    _colorStops.add(new ColorStop(aColor2, 1.0f));
    setRoll(aRotation);
}

/** 
 * Returns the number of color stops in the gradient
 */
public int getColorStopCount()  { return _colorStops.size(); }

/**
 * Returns the individual color stop at given index.
 */
public ColorStop getColorStop(int anIndex)  { return _colorStops.get(anIndex); }

/**
 * Returns the list of color stops.
 */
public List <ColorStop> getColorStops()  { return _colorStops; }

/**
 * Sets the color & position of the stop at the given index.
 */
public void setColorStop(int index, RMColor aColor, float position)
{
    int nstops = getColorStopCount();
    
    if(index<0 || index>=nstops)
        throw new IndexOutOfBoundsException("Invalid color index ("+index+")");
    
    float pmin = index==0 ? 0 : (float)(getColorStop(index-1).getPosition()+1e-4);
    float pmax = index==nstops-1 ? 1 : (float)(getColorStop(index+1).getPosition()-1e-4);
    
    if (position<pmin) position = pmin;
    else if (position>pmax) position = pmax;
    
    _colorStops.set(index, new ColorStop(aColor, position));
}

/**
 * Removes the stop at the given index.
 */
public void removeColorStop(int index)
{
    int nstops = getColorStopCount();

    // Complain if only one stop would be left
    if (nstops==2)
        throw new IndexOutOfBoundsException("Gradient Fill cannot have fewer than 2 stops");

    // Remove stop
    _colorStops.remove(index);
    
    // TODO: the behavior of a gradient without a stop at 0.0 or 1.0 needs to be defined.
    // awt or pdf allow the gradient to either cycle or extend the endpoints.
}

/**
 * Resets all the stops from the new list.
 */
public void setStops(List<ColorStop> newStops) 
{
    _colorStops.clear();
    if (newStops != null)
        _colorStops.addAll(newStops);
}

/**
 * Adds a new color stop at the given position. Returns the index of the new stop.
 */
public int insertColorStop(RMColor aColor, float position)
{
    // Get loop counter and max
    int i, nstops = getColorStopCount();
    
    if (position<0) position = 0;
    else if (position>1) position = 1;
    
    // Find location within sorted list for new stop
    for(i=0; i<nstops; ++i) {
        float listPos = getColorStop(i).getPosition();
        
        // inserting one exactly where one already exists just replaces the old one
        if (RMMath.equals(listPos, position)) {
            if (aColor != null)
                _colorStops.set(i, new ColorStop(aColor, position));
            return i;
        }
        // break if location of new stop is found.
        if (listPos > position) 
          break;
    }
    
    // a null color defaults to whatever value the old gradient has at the new position.
    // The new gradient will be visually identical to the old one, therefore, but with an explicit stop at 'position'
    if(aColor == null) {
        if(i==0 || i==nstops)
            aColor = RMColor.black;
        else {
            float distBefore = getColorStop(i-1).getPosition();
            float distAfter = getColorStop(i).getPosition();
            float stopDistance = (position - distBefore) / (distAfter-distBefore);
            RMColor colorBefore = getColorStop(i-1).getColor();
            RMColor colorAfter = getColorStop(i).getColor();
            aColor = colorBefore.blend(colorAfter, stopDistance);
        }
    }
 
    // insert stop at index
    _colorStops.add(i, new ColorStop(aColor, position));
 
    return i;
}

/**
 * Reverse the order of the color stops
 */
public void reverseColors()
{
    int nstops = getColorStopCount();
    ArrayList<ColorStop> newstops = new ArrayList<ColorStop>(nstops);
    
    // create a new list in of the stops, with the positions inverted and in the reverse order
    for(int i=0; i<nstops; ++i) {
        ColorStop oldStop = getColorStop(i);
        ColorStop newStop = new ColorStop(oldStop.getColor(), 1f - oldStop.getPosition());
        newstops.add(0, newStop);
    }
    
    // reset the stops
    _colorStops = newstops;
}

/**
 * Returns the gradient's rotation.
 */
public float getRoll()  { return _roll; }

/**
 * Sets the gradient's rotation.
 */
protected void setRoll(float aRoll)  { _roll = aRoll; }

/**
 * Returns whether gradient is radial.
 */
public boolean isRadial()  { return this instanceof RMRadialGradientFill; }

/**
 * Returns a new gradient which is a copy of this gradient but with a different roll value.
 */
public RMGradientFill deriveGradient(float aRoll)
{
    RMGradientFill newFill = clone();
    newFill.setRoll(aRoll);
    return newFill;
}

/**
 * Returns a new gradient which is a copy of this gradient but of a different type.
 */
public RMGradientFill deriveGradient(boolean isRadial)
{
    // don't make copy if this gradient is already the correct type
    if (!isRadial) return this;
    
    RMRadialGradientFill newFill = new RMRadialGradientFill();
    newFill.setStops(getColorStops());
    return newFill;
}

/**
 * Returns the gradient bounds for a given shape which is the bounds required to encompass the entire shape in the
 * coordinates of the gradient rotation (effectively the bounds of the shape rotated by opposite gradient rotation).
 * The gradient should be defined in these bounds so that rendered shapes completely utilizes the color range. 
 */
public Rectangle2D getGradientBounds(Shape aShape)
{
    // Get bounds of shape (just return if rotation is zero)
    Rectangle2D bounds = aShape.getBounds2D();
    if(_roll==0)
        return bounds;
    
    // Get transform for opposite of gradient rotation
    AffineTransform t1 = AffineTransform.getTranslateInstance(bounds.getCenterX(), bounds.getCenterY());
    t1.rotate(Math.toRadians(-_roll));
    t1.translate(-bounds.getCenterX(), -bounds.getCenterY());

    // Get shape transformed by opposite rotation
    Shape shapeRotated = t1.createTransformedShape(aShape);
    
    // Return bounds around rotated shape
    return shapeRotated.getBounds2D();
}

/** 
 * Returns the 2 points, in the shape's coordinate system, which define the gradient.
 */
public void getGradientAxis(RMShape aShape, RMPath aPath, Point2D p1, Point2D p2)
{
    // Get shape bounds
    RMRect bounds = aShape.getBoundsInside();
    
    // Get bounds needed to encompass shape with gradient roll
    Rectangle2D pathShapeRotatedBounds = getGradientBounds(aPath);
    
    // Get points for horizontal spar for bounds of rotated path shape
    p1.setLocation(pathShapeRotatedBounds.getX(), pathShapeRotatedBounds.getCenterY());
    p2.setLocation(pathShapeRotatedBounds.getMaxX(), pathShapeRotatedBounds.getCenterY());
    
    // Get transform for rotation
    AffineTransform t2 = AffineTransform.getTranslateInstance(bounds.getCenterX(), bounds.getCenterY());
    t2.rotate(Math.toRadians(_roll));
    t2.translate(-bounds.getCenterX(), -bounds.getCenterY());
    
    // Rotate points
    t2.transform(p1, p1);
    t2.transform(p2, p2);
}

/** 
 * Returns the 2 points, in the shape's coordinate system, which define the gradient.
 */
public void getGradientAxis(RMRect aRect, Point2D p1, Point2D p2)
{
    // Get points for horizontal spar for bounds of rotated path shape
    p1.setLocation(aRect.getX(), aRect.getCenterY());
    p2.setLocation(aRect.getMaxX(), aRect.getCenterY());
    
    // Get transform for rotation
    AffineTransform t2 = AffineTransform.getTranslateInstance(aRect.getCenterX(), aRect.getCenterY());
    t2.rotate(Math.toRadians(_roll));
    t2.translate(-aRect.getCenterX(), -aRect.getCenterY());
    
    // Rotate points
    t2.transform(p1, p1);
    t2.transform(p2, p2);
}

/**
 * Returns the color associated with this fill.
 */
public RMColor getColor()  { return getStopColor(0); }

/**
 * Sets color of first stop.
 */
public void setColor(RMColor aColor)  { super.setColor(aColor); setColorStop(0, aColor, getStopPosition(0)); }

/**
 * Returns the color of the stop at the given index.
 */
public RMColor getStopColor(int index)  { return _colorStops.get(index).getColor(); }

/**
 * Returns the position (in the range {0-1}) for the given stop index.
 */
public float getStopPosition(int index)  { return _colorStops.get(index).getPosition(); }

/**
 * Returns true if any of the colors in the gradient have alpha
 */
public boolean hasAlpha()
{
    for(int i=0, iMax=getColorStopCount(); i<iMax; i++)
        if(getStopColor(i).getAlphaInt() != 255)
            return true;
    return false;
}

/**
 * Returns a java.awt.Paint instance to draw this gradient.
 */
public Paint getPaint(float startx, float starty, float endx, float endy)
{
    return new RMMultipleStopGradient(startx, starty, endx, endy, this); 
}

/**
 * Render this fill in a shape painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // Get shape path
    RMPath path = aShape.getPathInBounds();
        
    // Get points for line which defines the gradient
    Point2D p1 = new Point2D.Double();
    Point2D p2 = new Point2D.Double();
    getGradientAxis(aShape, path, p1, p2);
    
    // Fill path using specific Paint subclass for this gradient
    Paint gp = getPaint((float)p1.getX(), (float)p1.getY(), (float)p2.getX(), (float)p2.getY());
    aPntr.setPaint(gp);
    aPntr.fill(path);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other
    if(anObj==this) return true;
    RMGradientFill other = RMClassUtils.getInstance(anObj, RMGradientFill.class); if(other==null) return false;
    
    // Check Radial, ColorStops, Roll
    if(other.isRadial()!=isRadial()) return false;
    if(!other._colorStops.equals(_colorStops)) return false;
    if(other._roll!=_roll) return false;
    return true; // Return true since checks passed
}

/**
 * Standard clone implementation.
 */
public RMGradientFill clone()
{
    RMGradientFill clone = (RMGradientFill)super.clone(); // Do normal clone
    clone._colorStops = RMListUtils.clone(_colorStops); // Clone stops
    return clone;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic fill attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "gradient");
        
    // Archive all colors beyond the first one as color2,color3 (for compatibility)
    for(int i=1, iMax=getColorStopCount(); i<iMax; ++i) {
        RMColor c = _colorStops.get(i).getColor();
        if(!c.equals(RMColor.black))
            e.add("color"+(i+1), "#" + c.toHexString());
    }
    
    // Archive stop positions (stop 0 defaults to 0.0, and last stop defaults to 1.0)
    for(int i=0, iMax=getColorStopCount(); i<iMax; ++i) {
        float position = _colorStops.get(i).getPosition();
        if((i==0 && RMMath.equalsZero(position)) || (i==iMax-1 && RMMath.equals(position, 1.0))) continue;
        e.add("stop"+(i==0 ? "" : (i+1)), position);
    }
    
    // Archive the number of stops, since the defaults in the above lists make it possibly indeterminate
    if(getColorStopCount()!= 2)
        e.add("nstops", getColorStopCount());
        
    // Archive roll
    if(_roll!=0)
        e.add("roll", _roll);
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic fill attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive stops
    int numstops = anElement.getAttributeIntValue("nstops", 2);
    
    // blow away old list (color # 0 will be read twice)
    _colorStops.clear();

    for(int i=0; i<numstops; ++i) {
        // unarchive color,color2,color3...
        String colorString = anElement.getAttributeValue("color"+(i==0 ? "" : (i+1)));
        RMColor c = colorString==null? RMColor.black : new RMColor(colorString);
        // unarchive stop,stop2,stop3...
        float position;
        XMLAttribute stopAttr = anElement.getAttribute("stop"+(i==0 ? "" : (i+1)));
        if (stopAttr==null) {
            if (i==0) position = 0;
              else if (i==numstops-1) position = 1;
                else continue;
        }
        else position = stopAttr.getFloatValue();
        _colorStops.add(new ColorStop(c, position));
    }
    
    // Unarchive roll
    _roll = anElement.getAttributeFloatValue("roll");
    
    // Return this gradient fill
    return this;
}

/**
 * Standard to string implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer("Gradient { ");
    for(ColorStop cs : getColorStops()) sb.append(cs).append(", ");
    return sb.append(" }").toString();
}

/**
 * Simple static class to represent a stop (they're immutable).
 */
public static class ColorStop {

    // The stop color
    RMColor   _color;
    
    // The stop position
    float     _position;
    
    /** Creates a new color stop. */
    public ColorStop(RMColor aColor, float aPosition)  { _color = aColor; _position = aPosition; }
    
    /** Returns stop color. */
    public RMColor getColor()  { return _color; }

    /** Returns stop position. */
    public float getPosition()  { return _position; }

    /** Standard equals implementation. */
    public boolean equals(Object obj)
    {
        if(this==obj) return true;
        ColorStop other = obj instanceof ColorStop? (ColorStop)obj : null; if(other==null) return false;
        return RMUtils.equals(_color, other._color) && RMMath.equals(_position, other._position);
    }
    
    /** Standard to string implementation. */
    public String toString() { return "ColorStop { Color=" + getColor().toHexString() + ", Pos=" + getPosition() +"}"; }
}

}