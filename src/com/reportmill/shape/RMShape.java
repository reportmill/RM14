package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import snap.util.*;
import snap.util.XMLArchiver.*;
import snap.web.*;

/**
 * This class is the basis for all graphic elements in a ReportMill document. You'll rarely use this class directly,
 * however, it encapsulates all the basic shape attributes and the most common methods used in template manipulation,
 * like setX(), setY(), setWidth(), setColor(), etc.
 *
 * Here's an example of programatically adding a watermark to a document:
 * <p><blockquote><pre>
 *   RMFont font = RMFont.getFont("Arial Bold", 72);
 *   RMColor color = new RMColor(.9f, .9f, .9f);
 *   RMXString string = new RMXString("REPORTMILL", font, color);
 *   RMText shape = new RMText(string);
 *   myDocument.getPage(0).addChild(shape);
 *   shape.setBounds(36, 320, 540, 140);
 *   shape.setRoll(45);
 *   shape.setOpacity(.667f);
 * </pre></blockquote>
 */
public class RMShape extends SnapObject implements PropertyChangeListener, DeepChangeListener, RMTypes, Archivable {

    // X location of shape
    double         _x = 0;
    
    // Y location of shape
    double         _y = 0;
    
    // Width of shape
    double         _width = 0;
    
    // Height of shape
    double         _height = 0;
    
    // An object to hold optional roll/scale/skew info
    RMShapeRSS     _rss;
    
    // The stroke for this shape
    RMStroke       _stroke = null;
    
    // The fill for this shape
    RMFill         _fill = null;
    
    // The effect for this shape
    RMEffect       _effect = null;
    
    // Whether this shape is visible
    boolean        _visible = true;
    
    // The parent of this shape
    RMParentShape  _parent = null;
    
    // An object describing layout parameters for this shape in parent layout (and one for parent layout internal use)
    Object         _layoutInfo, _layoutInfoX;
    
    // Map to hold less used attributes (name, url, etc.)
    RMSharedMap    _attrMap = RMSharedMap.getShared();
    
    // Property names map
    static Map <Class,List<String>>  _propertyNamesMap = new HashMap();
    
/**
 * Returns raw x location of shape. Developers should use the more common getX, which presents positive x.
 */
public double x()  { return _x; }

/**
 * Returns raw y location of shape. Developers should use the more common getY, which presents positive y.
 */
public double y()  { return _y; }

/**
 * Returns raw width of shape. Developers should use the more common getWidth, which presents positive width.
 */
public double width()  { return _width; }

/**
 * Returns raw height of shape. Developers should use the more common getHeight, which presents positive height.
 */
public double height()  { return _height; }

/**
 * Returns raw x, y, width and height of shape as rect (preserves possible negative sizes).
 */
public RMRect bounds()  { return new RMRect(x(), y(), width(), height()); }

/**
 * Returns the X location of the shape.
 */
public double getX()  { return _width<0? _x + _width : _x; }

/**
 * Sets the X location of the shape.
 */
public void setX(double aValue)
{
    if(_x==aValue) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("X", _x, _x = aValue, -1); // Set value and fire PropertyChange
    if(_parent!=null) _parent.setNeedsLayout(true); // Rather bogus
}

/**
 * Returns the Y location of the shape.
 */
public double getY()  { return _height<0? _y + _height : _y; }

/**
 * Sets the Y location of the shape.
 */
public void setY(double aValue)
{
    if(_y==aValue) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("Y", _y, _y = aValue, -1); // Set value and fire PropertyChange
    if(_parent!=null) _parent.setNeedsLayout(true); // Rather bogus
}

/**
 * Returns the width of the shape.
 */
public double getWidth()  { return _width<0? -_width : _width; }

/**
 * Sets the width of the shape.
 */
public void setWidth(double aValue)
{
    if(_width==aValue) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("Width", _width, _width = aValue, -1); // Set value and fire PropertyChange
    if(_parent!=null) _parent.setNeedsLayout(true); // Rather bogus
}

/**
 * Returns the height of the shape.
 */
public double getHeight()  { return _height<0? -_height : _height; }

/**
 * Sets the height of the shape.
 */
public void setHeight(double aValue)
{
    if(_height==aValue) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("Height", _height, _height = aValue, -1); // Set value and fire PropertyChange
    if(_parent!=null) _parent.setNeedsLayout(true); // Rather bogus
}

/**
 * Returns the max X of the shape (assumes not rotated, scaled or skewed).
 */
public double getMaxX()  { return getX() + getWidth(); }

/**
 * Returns the max Y of the shape (assumes not rotated, scaled or skewed).
 */
public double getMaxY()  { return getY() + getHeight(); }

/**
 * Returns the XY location of the shape as a point.
 */
public RMPoint getXY()  { return new RMPoint(getX(), getY()); }

/**
 * Sets the X and Y location of the shape to the given point (convenience).
 */
public void setXY(Point2D aPoint)  { setXY(aPoint.getX(), aPoint.getY()); }

/**
 * Sets the X and Y location of the shape to the given point (convenience).
 */
public void setXY(double anX, double aY)  { setX(anX); setY(aY); }

/**
 * Returns the size of the shape.
 */
public RMSize getSize()  { return new RMSize(getWidth(), getHeight()); }

/**
 * Sets the size of the shape.
 */
public void setSize(RMSize aSize)  { setSize(aSize.width, aSize.height); }

/**
 * Sets the size of the shape.
 */
public void setSize(double aWidth, double aHeight)  { setWidth(aWidth); setHeight(aHeight); }

/**
 * Returns the X, Y, width and height of the shape as a rect (use getFrame if shape has roll/scale/skew).
 */
public RMRect getBounds()  { return new RMRect(getX(), getY(), getWidth(), getHeight()); }

/**
 * Sets X, Y, width and height of shape to dimensions in given rect.
 */
public void setBounds(Rectangle2D aRect)
{
    setBounds(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight());
}

/**
 * Sets X, Y, width and height of shape to given dimensions.
 */
public void setBounds(double anX, double aY, double aW, double aH) { setX(anX); setY(aY); setWidth(aW); setHeight(aH); }

/**
 * Returns the rect in parent coords that fully encloses the shape.
 */
public RMRect getFrame()  { return isRSS()? convertRectToShape(getBoundsInside(), _parent) : getBounds(); }

/**
 * Sets the bounds of the shape such that it exactly fits in the given parent coord rect.
 */
public void setFrame(Rectangle2D aRect)  { setFrame(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight()); }

/**
 * Sets the bounds of the shape such that it exactly fits in the given parent coord rect.
 */
public void setFrame(double anX, double aY, double aWidth, double aHeight)
{
    setFrameXY(anX, aY);
    setFrameSize(aWidth, aHeight);
}

/**
 * Returns the X of the rect that fully encloses the shape in parent coords.
 */
public double getFrameX()  { return isRSS()? getFrameXY().x : getX(); }

/**
 * Sets a shape's X such that its bounds rect (in parent coords) has origin at the given X.
 */
public void setFrameX(double anX)  { double x = _x + anX - getFrameX(); setX(x); }

/**
 * Returns the Y of the rect that fully encloses the shape in parent coords.
 */
public double getFrameY()  { return isRSS()? getFrameXY().y : getY(); }

/**
 * Sets a shape's Y such that its bounds rect (in parent coords) has origin at the given Y.
 */
public void setFrameY(double aY)  { double y = _y + aY - getFrameY(); setY(y); }

/**
 * Returns the width of the rect that fully encloses the shape in parent coords.
 */
public double getFrameWidth()  { return isRSS()? getFrame().width : getWidth(); }

/**
 * Returns the height of the rect that fully encloses the shape in parent coords.
 */
public double getFrameHeight()  { return isRSS()? getFrame().height : getHeight(); }

/**
 * Returns the origin of the shape's bounds rect in parent coords.
 */ 
public RMPoint getFrameXY()  { return isRSS()? getFrame().getOrigin() : getXY(); }

/**
 * Sets a shape's origin such that its bounds rect (in parent coords) has origin at the given point.
 */
public void setFrameXY(RMPoint aPoint)  { setFrameXY(aPoint.x, aPoint.y); }

/**
 * Sets a shape's origin such that its frame (enclosing rect in parent coords) will have the given X and Y.
 */
public void setFrameXY(double anX, double aY)  { setFrameX(anX); setFrameY(aY); }

/**
 * Sets the height of the rect that fully encloses the shape in parent coords.
 */
public void setFrameSize(double aWidth, double aHeight)
{
    // If shape not rotated, scaled or skewed, just set and return
    if(!isRSS()) {
        if(_width<0) { setX(_x + (aWidth+_width)); aWidth = -aWidth; }
        if(_height<0) { setY(_y + (aHeight+_height)); aHeight = -aHeight; }
        setSize(aWidth, aHeight); return;
    }
    
    // Convert X & Y axis to parent coords
    RMSize x_axis = convertVectorToShape(new RMSize(_width, 0), _parent);
    RMSize y_axis = convertVectorToShape(new RMSize(0, _height), _parent);

    // Scale widths of X & Y axes in parent coords by ratio of NewWidth/OldWidth
    double sizeByRatio1 = Math.abs(aWidth)/(Math.abs(x_axis.width) + Math.abs(y_axis.width));
    x_axis.width *= sizeByRatio1; y_axis.width *= sizeByRatio1;
    
    // Scale heights of X & Y axes in parent coords by ratio of NewHeight/OldHeight
    double sizeByRatio2 = Math.abs(aHeight)/(Math.abs(x_axis.height) + Math.abs(y_axis.height));
    x_axis.height *= sizeByRatio2; y_axis.height *= sizeByRatio2;

    // Cache current bounds origin (this shouldn't change)
    RMPoint origin = getFrameXY();
    
    // Reset current Skew and convert X & Y axis from parent coords
    setSkewXY(0, 0);
    convertVectorFromShape(x_axis, _parent);
    convertVectorFromShape(y_axis, _parent);

    // Set the size to compensate for the skew
    setSize(x_axis.width, y_axis.height);

    // Calculate new skew angles (or roll, if width or height is zero)
    if(width()==0)
        setRoll(getRoll() - Math.toDegrees(Math.atan(y_axis.width/y_axis.height)));
    else if(height()==0)
        setRoll(getRoll() - Math.toDegrees(Math.atan(x_axis.height/x_axis.width)));
    else {
        setSkewX(Math.toDegrees(Math.atan(x_axis.height/x_axis.width)));
        setSkewY(Math.toDegrees(Math.atan(y_axis.width/y_axis.height)));
    }

    // Reset original bounds origin (it may have been effected by skew changes)
    setFrameXY(origin);
}

/**
 * Returns the max X of the shape's frame.
 */
public double getFrameMaxX()  { return isRSS()? getFrame().getMaxX() : getMaxX(); }

/**
 * Returns the max Y of the shape's frame.
 */
public double getFrameMaxY()  { return isRSS()? getFrame().getMaxY() : getMaxY(); }

/**
 * Returns the origin point of the shape in parent's coords.
 */
public RMPoint getXYP()  { return convertPointToShape(new RMPoint(), _parent); }

/**
 * Sets the origin point of the shape to the given X and Y in parent's coords.
 */
public void setXYP(double anX, double aY)
{
    // If rotated-scaled-skewd, get XY in parent coords and set XY as an offset from parent
    if(isRSS()) {
        RMPoint p = getXYP();
        setXY(_x + anX - p.x, _y + aY - p.y);
    }

    // If not rotated-scaled-skewed, just set x/y (adjusted if width/height are negative)
    else setXY(_width<0? anX-_width : anX, _height<0? aY-_height : aY);    
}

/**
 * Offsets the X and Y location of the shape by the given dx & dy amount (convenience).
 */
public void offsetXY(double dx, double dy)  { setXY(_x + dx, _y + dy); }

/**
 * Returns the bounds of the shape in the shape's own coords.
 */
public RMRect getBoundsInside()  { return new RMRect(0, 0, getWidth(), getHeight()); }

/**
 * Returns the bounds of the path associated with this shape in local coords, adjusted to account for stroke width.
 */
public RMRect getBoundsMarked()
{
    // Declare bounds marked rect
    RMRect boundsMarked = null;
    
    // If stroke, get stroke marked bounds
    if(getStroke()!=null)
        boundsMarked = getStroke().getBounds(this);
    
    // If fill, get fill marked bounds
    if(getFill()!=null) {
        RMRect b = getFill().getBounds(this);
        boundsMarked = boundsMarked==null? b : boundsMarked.unionRect(b);
    }
    
    // If effect, get effect marked bounds
    if(getEffect()!=null) {
        RMRect b = getEffect().getBounds(this);
        boundsMarked = boundsMarked==null? b : boundsMarked.unionRect(b);
    }
    
    // Return marked bounds
    return boundsMarked!=null? boundsMarked : getBoundsInside();
}

/**
 * Returns the marked bounds of this shape and it's children.
 */
public RMRect getBoundsMarkedDeep()
{
    // Get normal marked bounds
    RMRect bounds = getBoundsMarked();
    
    // Iterate over (visible) children and union with their BoundsMarkedDeep (converted to this shape coords)
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i);
        if(!child.isVisible()) continue;
        RMRect childBounds = child.getBoundsMarkedDeep();
        childBounds = child.convertRectToShape(childBounds, this);
        bounds.unionEvenIfEmpty(childBounds);
    }

    // Return marked bounds
    return bounds;
}

/**
 * Returns the roll of the shape.
 */
public double getRoll()  { return _rss==null? 0 : _rss.roll; }

/**
 * Sets the roll of the shape.
 */
public void setRoll(double aValue)
{
    if(aValue==getRoll()) return;
    repaint();
    firePropertyChange("Roll", getRSS().roll, getRSS().roll = aValue, -1);
}

/**
 * Returns the scale of the X axis of the shape.
 */
public double getScaleX()  { return _rss==null? 1 : _rss.scaleX; }

/**
 * Sets the scale of the X axis of the shape.
 */
public void setScaleX(double aValue)
{
    if(aValue==getScaleX()) return;
    repaint();
    firePropertyChange("ScaleX", getRSS().scaleX, getRSS().scaleX = aValue, -1);
}

/**
 * Returns the scale of the Y axis of the shape.
 */
public double getScaleY()  { return _rss==null? 1 : _rss.scaleY; }

/**
 * Sets the scale of the Y axis of the shape.
 */
public void setScaleY(double aValue)
{
    if(aValue==getScaleY()) return;
    repaint();
    firePropertyChange("ScaleY", getRSS().scaleY, getRSS().scaleY = aValue, -1);
}

/**
 * Sets the scale of the X and Y axis.
 */
public void setScaleXY(double sx, double sy)  { setScaleX(sx); setScaleY(sy); }

/**
 * Returns the skew of the X axis of the shape.
 */
public double getSkewX()  { return _rss==null? 0 : _rss.skewX; }

/**
 * Sets the skew of the X axis of the shape.
 */
public void setSkewX(double aValue)
{
    if(aValue==getSkewX()) return;
    repaint();
    firePropertyChange("SkewX", getRSS().skewX, getRSS().skewX = aValue, -1);
}

/**
 * Returns the skew of the Y axis of the shape.
 */
public double getSkewY()  { return _rss==null? 0 : _rss.skewY; }

/**
 * Sets the skew of the Y axis of the shape.
 */
public void setSkewY(double aValue)
{
    if(aValue==getSkewY()) return;
    repaint();
    firePropertyChange("SkewY", getRSS().skewY, getRSS().skewY = aValue, -1);
}

/**
 * Sets the skew of the X and Y axis.
 */
public void setSkewXY(double skx, double sky)  { setSkewX(skx); setSkewY(sky); }

/**
 * Returns whether the shape has been rotated, scaled or skewed (for efficiency).
 */
public boolean isRSS()  { return _rss!=null; }

/**
 * Returns the roll scale skew object.
 */
protected RMShapeRSS getRSS()  { return _rss!=null? _rss : (_rss=new RMShapeRSS()); }

/**
 * Returns the stroke for this shape.
 */
public RMStroke getStroke()  { return _stroke; }

/**
 * Sets the stroke for this shape, with an option to turn on drawsStroke.
 */
public void setStroke(RMStroke aStroke)
{
    if(RMUtils.equals(getStroke(), aStroke)) return; // If value already set, just return
    repaint(); // Register repaint
    if(_stroke!=null) _stroke.removePropertyChangeListener(this);
    firePropertyChange("Stroke", _stroke, _stroke = aStroke, -1); // Set value and fire PropertyChange
    if(_stroke!=null) _stroke.addPropertyChangeListener(this); // Set shape
}

/**
 * Returns the fill for this shape.
 */
public RMFill getFill()  { return _fill; }

/**
 * Sets the fill for this shape.
 */
public void setFill(RMFill aFill)
{
    if(RMUtils.equals(getFill(), aFill)) return; // If value already set, just return
    repaint(); // Register repaint
    if(_fill!=null) _fill.removePropertyChangeListener(this);
    firePropertyChange("Fill", _fill, _fill = aFill, -1); // Set value and fire PropertyChange
    if(_fill!=null) _fill.addPropertyChangeListener(this);
}

/**
 * Returns the effect for this shape.
 */
public RMEffect getEffect()  { return _effect; }

/**
 * Sets the effect for this shape.
 */
public void setEffect(RMEffect anEffect)
{
    if(RMUtils.equals(getEffect(), anEffect)) return; // If value already set, just return
    repaint(); // Register repaint
    if(_effect!=null) { _effect.removePropertyChangeListener(this);
        removePropertyChangeListener(_effect); removeDeepChangeListener(_effect); }
    firePropertyChange("Effect", _effect, _effect = anEffect, -1); // Set value and fire PropertyChange
    if(_effect!=null) { _effect.addPropertyChangeListener(this);
        addPropertyChangeListener(_effect); addDeepChangeListener(_effect); }
}

/**
 * Returns the color of the shape.
 */
public RMColor getColor()  { return getFill()==null? RMColor.black : getFill().getColor(); }

/**
 * Sets the color of the shape.
 */
public void setColor(RMColor aColor)
{
    // Set color
    if(aColor==null) setFill(null);
    else if(getFill()==null) setFill(new RMFill(aColor));
    else getFill().setColor(aColor);
}

/**
 * Returns the stroke color of the shape.
 */
public RMColor getStrokeColor()  { return getStroke()==null? RMColor.black : getStroke().getColor(); }

/**
 * Sets the stroke color of the shape.
 */
public void setStrokeColor(RMColor aColor)
{
    // Set stroke color
    if(aColor==null) setStroke(null);
    else if(getStroke()==null) setStroke(new RMStroke(aColor, 1));
    else getStroke().setColor(aColor);
}

/**
 * Returns the stroke width of the shape's stroke in printer points.
 */
public float getStrokeWidth()  { return getStroke()==null? 0 : getStroke().getWidth(); }

/**
 * Sets the stroke width of the shape's stroke in printer points.
 */
public void setStrokeWidth(float aValue)
{
    // Set line width
    if(getStroke()==null) setStroke(new RMStroke(RMColor.black, aValue));
    else getStroke().setWidth(aValue);
}

/**
 * Returns the opacity of the shape (1 for opaque, 0 for transparent).
 */
public float getOpacity()  { Number n = (Number)get("Opacity"); return n!=null? n.floatValue() : 1; }

/**
 * Sets the opacity of the shape (1 for opaque, 0 for transparent).
 */
public void setOpacity(float aValue)
{
    if(getOpacity()==aValue) return; // If value already set, just return
    repaint(); // Register repaint
    Object oldValue = getOpacity(); // Cache old value, set new value and fire PropertyChange
    put("Opacity", aValue==1? null : aValue);
    firePropertyChange("Opacity", oldValue, aValue, -1);
}

/**
 * Returns the combined opacity of this shape and its parent.
 */
public float getOpacityDeep()
{
    float op = getOpacity();
    for(RMShape s=_parent; s!=null; s=s._parent) op *= s.getOpacity();
    return op;
}

/**
 * Returns whether this shape is visible.
 */
public boolean isVisible()  { return _visible; }

/**
 * Sets whether this shape is visible.
 */
public void setVisible(boolean aValue)
{
    if(isVisible()==aValue) return; // If value already set, just return
    firePropertyChange("Visible", _visible, _visible = aValue, -1); // Set value and fire PropertyChange
}

/**
 * Returns an object describing layout paramaters of this shape in its parent's layout.
 */
public Object getLayoutInfo()  { return _layoutInfo; }

/**
 * Sets an object describing layout parameters of this shape in its parent's layout.
 */
public void setLayoutInfo(Object theLayoutInfo)
{
    if(RMUtils.equals(theLayoutInfo, _layoutInfo)) return; // If value already set, just return
    _layoutInfoX = null;
    firePropertyChange("LayoutInfo", _layoutInfo, _layoutInfo = theLayoutInfo, -1);
}

/**
 * Returns the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
 */
public String getAutosizing()
{
    String li = _layoutInfo instanceof String? (String)_layoutInfo : null;
    return li!=null && li.length()>6 && (li.charAt(0)=='-' || li.charAt(0)=='~')? li : getAutosizingDefault();
}

/**
 * Sets the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
 */
public void setAutosizing(String aValue)  { setLayoutInfo(aValue); }

/**
 * Returns the autosizing default.
 */
public String getAutosizingDefault()  { return "--~,--~"; }

/** Returns whether this shape is visible in its parent. */
//public boolean isShowing()  { return isVisible() && _parent!=null && _parent.isShowing()); }
//void setShowing(bool v) { if(v) shapeShown(); else shapeHidden(); for(RMShape c : getChildren()) c.setShowing(v); }
//protected void shapeShown()  { } protected void shapeHidden()  { }

/**
 * Returns whether this shape is hittable in its parent.
 */
public boolean isHittable()  { return isVisible() && (_parent==null || _parent.isHittable(this)); }

/**
 * Returns whether this shape is being viewed in a viewer.
 */
public boolean isViewing()  { RMShape p = getParent(); return p!=null && p.isViewing(); }

/**
 * Returns whether this shape is being edited in an editor.
 */
public boolean isEditing()  { RMShape p = getParent(); return p!=null && p.isEditing(); }

/**
 * Returns the text color for the shape.
 */
public RMColor getTextColor()  { return RMColor.black; }

/**
 * Sets the text color for the shape.
 */
public void setTextColor(RMColor aColor) { }

/**
 * Returns whether font has been set.
 */
public boolean isFontSet()  { return false; }

/**
 * Returns the font for the shape (defaults to parent font).
 */
public RMFont getFont()  { return getParent()!=null? getParent().getFont() : null; }

/**
 * Sets the font for the shape.
 */
public void setFont(RMFont aFont)  { }

/**
 * Returns whether the shape is underlined.
 */
public boolean isUnderlined()  { return false; }

/**
 * Sets the shape to underline.
 */
public void setUnderlined(boolean aFlag)  { }

/**
 * Returns the outline for the shape.
 */
public RMXString.Outline getOutline()  { return null; }

/**
 * Sets the outline for the shape.
 */
public void setOutline(RMXString.Outline anOutline)  { }

/**
 * Returns the alignment.
 */
public Align getAlignment()
{
    AlignX ax = getAlignmentX(); AlignY ay = getAlignmentY();
    if(ax==AlignX.Left && ay==AlignY.Top) return Align.TopLeft;
    if(ax==AlignX.Center && ay==AlignY.Top) return Align.TopCenter;
    if(ax==AlignX.Right && ay==AlignY.Top) return Align.TopRight;
    if(ax==AlignX.Left && ay==AlignY.Middle) return Align.CenterLeft;
    if(ax==AlignX.Center && ay==AlignY.Middle) return Align.Center;
    if(ax==AlignX.Right && ay==AlignY.Middle) return Align.CenterRight;
    if(ax==AlignX.Left && ay==AlignY.Bottom) return Align.BottomLeft;
    if(ax==AlignX.Center && ay==AlignY.Bottom) return Align.BottomCenter;
    return Align.BottomRight;
}

/**
 * Sets the alignment.
 */
public void setAlignment(Align anAlign)
{
    switch(anAlign) {
        case TopLeft: case CenterLeft: case BottomLeft: setAlignmentX(AlignX.Left); break; 
        case TopCenter: case Center: case BottomCenter: setAlignmentX(AlignX.Center); break;
        case TopRight: case CenterRight: case BottomRight: setAlignmentX(AlignX.Right); break; 
    }
    switch(anAlign) {
        case TopLeft: case TopCenter: case TopRight: setAlignmentY(AlignY.Top); break; 
        case CenterLeft: case Center: case CenterRight: setAlignmentY(AlignY.Middle); break;
        case BottomLeft: case BottomCenter: case BottomRight: setAlignmentY(AlignY.Bottom); break; 
    }
}

/**
 * Returns the horizontal alignment.
 */
public AlignX getAlignmentX()  { return AlignX.Left; }

/**
 * Sets the horizontal alignment.
 */
public void setAlignmentX(AlignX anAlignX)  { }

/**
 * Returns the vertical alignment.
 */
public AlignY getAlignmentY()  { return AlignY.Top; }

/**
 * Sets the vertical alignment.
 */
public void setAlignmentY(AlignY anAlignX)  { }

/**
 * Returns the format for the shape.
 */
public RMFormat getFormat()
{
    // Return format from first binding
    if(getBindingCount()>0)
        return (RMFormat)getBinding(0).getFormat();
    
    // Return null
    return null;
}

/**
 * Sets the format for the shape.
 */
public void setFormat(RMFormat aFormat)
{
    // Add format to first binding
    if((aFormat==null || aFormat instanceof java.text.Format) && getBindingCount()>0)
        getBinding(0).setFormat((java.text.Format)aFormat);
    
    // Pass down to children
    for(int i=0, iMax=getChildCount(); i<iMax; i++)
        getChild(i).setFormat(aFormat);
}
    
/**
 * Adds a deep change listener to shape to listen for shape changes and property changes received by shape.
 */
public void addDeepChangeListener(DeepChangeListener aListener)
{
    addListener(DeepChangeListener.class, aListener);  // Add listener
    if(getListenerCount(DeepChangeListener.class)==1)   // If first listener, add for children
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i);
            child.addPropertyChangeListener(this); child.addDeepChangeListener(this); }
}

/**
 * Removes a deep change listener from shape.
 */
public void removeDeepChangeListener(DeepChangeListener aLstnr)  { removeListener(DeepChangeListener.class, aLstnr); }

/**
 * Property change listener implementation.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Get DeepChangeListener count (just return if zero)
    int deepListenerCount = getListenerCount(DeepChangeListener.class); if(deepListenerCount==0) return;
    
    // If change is fill/stroke, convert property name to "Fill.xxx" or "Stroke.xxx" for the sake of animation
    if(anEvent.getSource() instanceof RMFill) {
        Object source = anEvent.getSource();
        String prefix = source==getFill()? "Fill" : source==getStroke()? "Stroke" : null;
        String propertyName = prefix + "." + anEvent.getPropertyName();
        anEvent = new PropertyChangeEvent(this, propertyName, anEvent.getOldValue(), anEvent.getNewValue());
        repaint();
    }
    
    // Propagate to this shape's DeepChangeListeners (if present)
    for(int i=0, iMax=getListenerCount(DeepChangeListener.class); i<iMax; i++)
        getListener(DeepChangeListener.class, i).deepChange(this, anEvent);
}

/**
 * Deep property change listener implementation.
 */
public void deepChange(PropertyChangeListener aListener, PropertyChangeEvent anEvent)
{
    for(int i=0, iMax=getListenerCount(DeepChangeListener.class); i<iMax; i++)
        getListener(DeepChangeListener.class, i).deepChange(aListener, anEvent);
}

/**
 * Returns the name for the shape.
 */
public String getName()  { return (String)get("Name"); }

/**
 * Sets the name for the shape.
 */
public void setName(String aName)
{
    if(RMUtils.equals(aName, getName())) return; // If value already set, just return
    Object oldValue = put("Name", RMStringUtils.min(aName)); // Cache old value
    firePropertyChange("Name", oldValue, RMStringUtils.min(aName), -1); // Set new value and fire PropertyChange
}

/**
 * Sets the URL for the shape.
 */
public String getURL()  { return (String)get("RMShapeURL"); }

/**
 * Returns the URL for the shape.
 */
public void setURL(String aURL)
{
    if(RMUtils.equals(aURL, getURL())) return; // If value already set, just return
    Object oldValue = put("RMShapeURL", RMStringUtils.min(aURL)); // Cache old value
    firePropertyChange("RMShapeURL", oldValue, aURL, -1); // Set new value and fire PropertyChange
}

/**
 * Sets the Hover string for the shape.
 */
public String getHover()  { return (String)get("Hover"); }

/**
 * Returns the URL for the shape.
 */
public void setHover(String aString)
{
    if(RMUtils.equals(aString, getHover())) return; // If value already set, just return
    Object oldValue = put("Hover", RMStringUtils.min(aString)); // Cache old value
    firePropertyChange("Hover", oldValue, aString, -1); // Set new value and fire PropertyChange
}

/**
 * Returns the locked state of the shape (really just to prevent location/size changes in the editor).
 */
public boolean isLocked()  { return RMUtils.boolValue(get("Locked")); }

/**
 * Sets the locked state of the shape (really just to prevent location/size changes in the editor).
 */
public void setLocked(boolean aValue)
{
    if(aValue==isLocked()) return; // If value already set, just return
    Object oldValue = put("Locked", aValue); // Cache old value
    firePropertyChange("Locked", oldValue, aValue, -1); // Set new value and fire PropertyChange
}

/**
 * Returns the timeline for animating shape property changes.
 */
public RMTimeline getTimeline()  { return getTimeline(false); }

/**
 * Returns the timeline for animating shape property changes, with an option to create if absent.
 */
public RMTimeline getTimeline(boolean create)
{
    // Get timeline from attributes map
    RMTimeline timeline = (RMTimeline)get("Anim");
    
    // If null and create is requested, create
    if(timeline==null && create)
        setTimeline(timeline = new RMTimeline(this));

    // Return timeline
    return timeline;
}

/**
 * Sets the shape timeline.
 */
protected void setTimeline(RMTimeline aTimeline)
{
    // Stop listening to old timeline property changes
    if(getTimeline()!=null) getTimeline().removePropertyChangeListener(this);
    
    // Set anim
    put("Anim", aTimeline);
    
    // Set owner to this shape and start listening for property changes
    if(aTimeline!=null) {
        aTimeline.setOwner(this);
        aTimeline.addPropertyChangeListener(this);
    }
}

/**
 * Tells the shape's timeline to update the shape to the given time in milliseconds. Recurses to shape children.
 */
public void setTime(int aTime)
{
    // Get shape timeline and set time (if non-null)
    RMTimeline timeline = getTimeline();
    if(timeline!=null) {
        undoerDisable();
        timeline.setTime(aTime);
        undoerEnable();
    }
    
    // If children have same animator as this shape, recurse setTime to children
    if(getAnimator()==getChildAnimator())
        for(int i=0, iMax=getChildCount(); i<iMax; i++)
            getChild(i).setTime(aTime);
}

/**
 * Returns the Object associated with the given name for the shape.
 * This is a general purpose property facility to allow shapes to hold many less common properties without the overhead
 * of explicitly including ivars for them. The map that holds these properties is shared so that there is only ever one
 * instance of the map for each unique permutation of attributes.
 */
public Object get(String aName)  { return _attrMap.get(aName); }

/**
 * Sets an Object to be associated with the given name for the shape.
 */
public Object put(String aName, Object anObj)
{
    // If map shared, clone it for real
    if(_attrMap.isShared())
        _attrMap = _attrMap.cloneX();
    
    // Put value (or remove if null)
    return anObj!=null? _attrMap.put(aName, anObj) : _attrMap.remove(aName);
}

/**
 * Returns the object associated with given key, using the given default if not found.
 */
public Object get(String aName, Object aDefault)
{
    Object value = get(aName);
    return value!=null? value : aDefault;
}

/**
 * Returns the shape's path.
 */
public RMPath getPath()  { return RMPath.unitRectPath; }

/**
 * Returns the shape's path scaled to the shape's current bounds.
 */
public RMPath getPathInBounds()  { return getPath().getPathInRect(getBoundsInside()); }

/**
 * Returns the outline of the shape as a path.  For simple shape this is the same as getPath(),
 * and for group shapes it's the union of all the children paths.
 */
public RMPath getMaskPath()
{
    RMPath path = null;
    
    // If this shape draws anything, start with its path
    if(getStroke()!= null || getFill()!=null)
        path = getPathInBounds();
    
    // If this shape has children, add paths of children
    if(getChildCount()>0) {
        
        // Create list for paths
        List paths = new ArrayList();
        if(path!=null)
            paths.add(path);
        
        // Iterate over children and add their paths
        for(int i=0, iMax=getChildCount(); i<iMax; i++) {
            RMPath childPath = getChild(i).getMaskPath();
            if(childPath != null) {
                RMPath p2 = childPath.clone();
                p2.transformBy(getChild(i).getTransform());
                paths.add(p2);
            }
        }
        
        // Join paths
        if(paths.size() > 0)
            path = RMPathUtils.join(paths, RMPathUtils.JOIN_OP_ADD);
    }
    
    // Return path
    return path;
}            
    
/**
 * Returns the parent of this shape.
 */
public RMParentShape getParent()  { return _parent; }

/**
 * Sets the parent of this shape (called automatically by addChild()).
 */
public void setParent(RMParentShape aShape)  { _parent = aShape; }

/**
 * Returns the first parent with given class by iterating up parent hierarchy.
 */
public <T extends RMShape> T getParent(Class<T> aClass)
{
    for(RMShape s=getParent(); s!=null; s=s.getParent()) if(aClass.isInstance(s)) return (T)s;
    return null; // Return null since parent of class wasn't found
}

/**
 * Removes this shape from it's parent.
 */
public void removeFromParent()  { if(_parent!=null) _parent.removeChild(this); }

/**
 * Returns the index of this child in its parent.
 */
public int indexOf()  { return _parent!=null? _parent.indexOfChild(this) : -1; }

/**
 * Returns the child count.
 */
public int getChildCount()  { return 0; }

/**
 * Returns the child at given index.
 */
public RMShape getChild(int anIndex)  { return null; }

/**
 * Returns the children list.
 */
public List <RMShape> getChildren()  { return Collections.emptyList(); }

/**
 * Returns the top level shape (usually an RMDocument).
 */
public RMShape getRootShape()  { return _parent!=null? _parent.getRootShape() : this; }

/**
 * Returns the RMDocument ancestor of this shape (or null if not there).
 */
public RMDocument getDocument()  { return _parent!=null? _parent.getDocument() : null; }

/**
 * Returns the RMPage ancestor of this shape (or null if not there).
 */
public RMParentShape getPageShape()  { return _parent!=null? _parent.getPageShape() : (RMParentShape)this; }

/**
 * Returns the undoer for this shape (or null if not there).
 */
public Undoer getUndoer()  { return _parent!=null? _parent.getUndoer() : null; }

/**
 * Undoer convenience - sets title of next registered undo.
 */
public void undoerSetUndoTitle(String aTitle) { Undoer u = getUndoer(); if(u!=null) u.setUndoTitle(aTitle); }

/**
 * Undoer convenience - disable the undoer.
 */
public void undoerDisable()  { Undoer u = getUndoer(); if(u!=null) u.disable(); }

/**
 * Undoer convenience - enables the undoer.
 */
public void undoerEnable()  { Undoer u = getUndoer(); if(u!=null) u.enable(); }

/**
 * Editor method - returns whether this shape is at the top level (usually RMPage).
 */
public boolean isRoot()  { return getAncestorCount()<2; }

/**
 * Returns the number of ancestors (from this shape's parent up to the document).
 */
public int getAncestorCount()  { return _parent!=null? getParent().getAncestorCount() + 1 : 0; }

/**
 * Returns the ancestor at the given index (parent is ancestor 0).
 */
public RMShape getAncestor(int anIndex)  { return anIndex==0? getParent() : getParent().getAncestor(anIndex-1); }

/**
 * Returns true if given shape is one of this shape's ancestors.
 */
public boolean isAncestor(RMShape aShape)  { return aShape==_parent || (_parent!=null && _parent.isAncestor(aShape)); }

/**
 * Returns true if given shape is one of this shape's descendants.
 */
public boolean isDescendant(RMShape aShape)  { return aShape!=null && aShape.isAncestor(this); }

/**
 * Returns first ancestor that the given shape and this shape have in common.
 */
public RMShape getAncestorInCommon(RMShape aShape)
{
    // If shape is our descendant, return this shape
    if(isDescendant(aShape))
        return this;
    
    // Iterate up shape's ancestors until one has this shape as descendant
    for(RMShape shape=aShape; shape!=null; shape=shape.getParent())
        if(shape.isDescendant(this))
            return shape;
    
    // Return null since common ancestor not found
    return null;
}

/**
 * Returns a list of shapes from this shape to a given ancestor, inclusive.
 */
public List <RMShape> getShapesToAncestor(RMShape aShape)
{
    // Iterate and add up this shape and parents until given ancestor is added (or we run out)
    List ancestors = new ArrayList();
    for(RMShape shape=this; shape!=null; shape=shape.getParent()) {
        ancestors.add(shape);
        if(shape==aShape)
            break;
    }
    
    // Return ancestors
    return ancestors;
}

/**
 * Returns a list of shape's from this shape to given descendant, inclusive.
 */
public List <RMShape> getShapesToDescendant(RMShape aShape)
{
    List list = aShape.getShapesToAncestor(this); Collections.reverse(list); return list;
}

/**
 * Returns a list of shapes from this shape to given shape.
 */
public List <RMShape> getShapesToShape(RMShape aShape)
{
    // If shape is null or ancestor, return shapes to ancestor
    if(aShape==null || isAncestor(aShape))
        return getShapesToAncestor(aShape);
    
    // If shape is a descendant, return shapes to descendant
    if(isDescendant(aShape))
        return getShapesToDescendant(aShape);

    // Get common ancestor (if none, just return null)
    RMShape commonAncestor = getAncestorInCommon(aShape);
    if(commonAncestor==null)
        return null;
    
    // Get shapes to common ancestor, without ancestor, and add shapes from common ancestor to given shape
    List shapes = getShapesToAncestor(commonAncestor);
    shapes.remove(shapes.size()-1);
    shapes.addAll(commonAncestor.getShapesToDescendant(aShape));

    // Return shapes
    return shapes;
}

/**
 * Returns the transform to this shape from its parent.
 */
public RMTransform getTransform()
{
    return new RMTransform(getX(), getY(), getRoll(), getWidth()/2, getHeight()/2,
         getScaleX(), getScaleY(), getSkewX(), getSkewY());
}

/**
 * Returns the transform from this shape to it's parent.
 */
public RMTransform getTransformInverse()  { return getTransform().invert(); }

/**
 * Returns the transform from this shape to the given shape.
 */
public RMTransform getTransformToShape(RMShape aShape)
{
    // If transforming out of shape hierarchy, concat recursive transformToShape call to parents
    if(aShape==null)
        return getParent()==null? getTransform() : getTransform().multiply(getParent().getTransformToShape(null));

    // The transform to parent is just our transform, transform to child is just child's inverse transform
    if(aShape==getParent())
        return getTransform();
    if(this==aShape.getParent())
        return aShape.getTransformInverse();
    if(aShape==this)
        return new RMTransform();

    // Start with identity transform
    RMTransform transform = RMTransform.identity;
    
    // If not one of simple cases above, concat successive transforms from last shape to self
    List <RMShape> shapes = getShapesToShape(aShape);
    if(shapes!=null)
        for(int i=shapes.size()-1; i>0; i--) {
            RMShape cs = shapes.get(i), ns = shapes.get(i-1);
            RMTransform t2 = ns==cs.getParent()? cs.getTransformInverse() : ns.getTransform(); // Inv if going up, else normal
            transform = t2.multiply(transform);
        }
    
    // Return transform
    return transform;
}

/**
 * Returns the transform from the given shape to this shape.
 */
public RMTransform getTransformFromShape(RMShape aShape)
{
    // The transform from parent is just our inverse transform, transform from child is just child's transform
    if(aShape==getParent())
        return getTransformInverse();
    if(aShape!=null && this==aShape.getParent())
        return aShape.getTransform();

    // Return the inverse of transform to shape
    return getTransformToShape(aShape).invert();
}

/**
 * Converts the given point to the given shape's coords (returns it for convenience).
 */
public RMPoint convertPointToShape(RMPoint point, RMShape shape)
{
    if(shape==_parent && !isRSS()) { point.offset(getX(), getY()); return point; }
    return getTransformToShape(shape).transform(point);
}

/**
 * Converts the given point to the given shape's coords (returns it for convenience).
 */
public RMPoint convertPointFromShape(RMPoint point, RMShape shape)
{
    if(shape==_parent && !isRSS()) { point.offset(-getX(), -getY()); return point; }
    return getTransformFromShape(shape).transform(point);
}

/**
 * Converts the given size (as a vector) to the given shape's coords (returns it for convenience).
 */
public RMSize convertVectorToShape(RMSize size, RMShape shape)
{
    if(shape==_parent && !isRSS()) return size;
    return getTransformToShape(shape).transformVector(size);
}

/**
 * Converts the given size (as a vector) from the given shape's coords (returns it for convenience).
 */
public RMSize convertVectorFromShape(RMSize size, RMShape shape)
{
    if(shape==_parent && !isRSS()) return size;
    return getTransformFromShape(shape).transformVector(size);
}

/**
 * Converts the given rect to the given shape's coords (returns it for convenience).
 */
public RMRect convertRectToShape(RMRect rect, RMShape shape)
{
    if(shape==_parent && !isRSS()) { rect.offset(getX(), getY()); return rect; }
    return getTransformToShape(shape).transform(rect);
}

/**
 * Converts the given rect from the given shape's coords (returns it for convenience).
 */
public RMRect convertRectFromShape(RMRect rect, RMShape shape)
{
    if(shape==_parent && !isRSS()) { rect.offset(-getX(), -getY()); return rect; }
    return getTransformFromShape(shape).transform(rect);
}

/**
 * Returns the given point converted to the given shape's coords.
 */
public RMPoint convertedPointToShape(RMPoint aPoint, RMShape aShape)
{
    return convertPointToShape(new RMPoint(aPoint), aShape);
}

/**
 * Returns the given point converted from the given shape's coords.
 */
public RMPoint convertedPointFromShape(RMPoint aPoint, RMShape aShape)
{
    return convertPointFromShape(new RMPoint(aPoint), aShape);
}

/**
 * Returns the rect encompassing the given rect converted to the given shape's coords.
 */
public RMRect convertedRectToShape(RMRect aRect, RMShape aShape)
{
    return convertRectToShape(new RMRect(aRect), aShape);
}

/**
 * Returns the rect encompassing the given rect converted from the given shape's coords.
 */
public RMRect convertedRectFromShape(RMRect aRect, RMShape aShape)
{
    return convertRectFromShape(new RMRect(aRect), aShape);
}

/**
 * Returns the given path converted to the given shape's coords.
 */
public RMPath convertPathToShape(RMPath aPath, RMShape aShape)
{
    RMTransform transform = getTransformToShape(aShape);
    if(!transform.isIdentity()) { aPath = aPath.clone(); aPath.transformBy(transform); }
    return aPath;
}

/**
 * Returns the given path converted from the given shape's coords.
 */
public RMPath convertPathFromShape(RMPath aPath, RMShape aShape)
{
    RMTransform transform = getTransformFromShape(aShape);
    if(!transform.isIdentity()) { aPath = aPath.clone(); aPath.transformBy(transform); }
    return aPath;
}

/**
 * Transforms the given shape to this shape's coords.
 */
public void convertToShape(RMShape aShape)
{
    // Get center point in shape coords
    RMPoint cp = convertPointToShape(new RMPoint(getWidth()/2, getHeight()/2), aShape);
    
    // Coalesce transforms up the parent chain
    for(RMShape s=_parent; s!=aShape; s=s._parent) {
        setRoll(getRoll() + s.getRoll());
        setScaleX(getScaleX() * s.getScaleX()); setScaleY(getScaleY() * s.getScaleY());
        setSkewX(getSkewX() + s.getSkewX()); setSkewY(getSkewY() + s.getSkewY());
    }
    
    // Convert center point back from _parent, calc vector to old center from new center (in parent coords) & translate
    convertPointFromShape(cp, _parent);
    RMSize v = convertVectorToShape(new RMSize(cp.x - getWidth()/2, cp.y - getHeight()/2), _parent);
    offsetXY(v.width, v.height);
}

/**
 * Transforms the given shape from this shape's coords.
 */
public void convertFromShape(RMShape aShape)
{
    // Get center point in parent coords
    RMPoint cp = convertPointToShape(new RMPoint(getWidth()/2, getHeight()/2), _parent);

    // Coalesce transforms down the shape chain
    for(RMShape s=_parent; s!=aShape; s=s._parent) {
        setRoll(getRoll() - s.getRoll());
        setScaleX(getScaleX()/s.getScaleX()); setScaleY(getScaleY()/s.getScaleY());
        setSkewX(getSkewX() - s.getSkewX()); setSkewY(getSkewY() - s.getSkewY());
    }

    // Convert center point back from aShape, calc vector to old center from new center (in parent coords) & translate
    convertPointFromShape(cp, aShape);
    RMSize v = convertVectorToShape(new RMSize(cp.x - getWidth()/2, cp.y - getHeight()/2), _parent);
    offsetXY(v.width, v.height);
}

/**
 * Returns whether shape minimum width is set.
 */
public boolean isMinWidthSet()  { return get("MinWidth")!=null; }

/**
 * Returns the shape minimum width.
 */
public double getMinWidth()  { return getMinWidth(-1); }

/**
 * Returns the shape minimum width.
 */
public double getMinWidth(double aValue)  { Double w = (Double)get("MinWidth"); return w!=null? w : 0; }

/**
 * Sets the shape minimum width.
 */
public void setMinWidth(double aWidth)
{
    double w = aWidth<=0? 0 : aWidth; if(w==getMinWidth()) return;
    firePropertyChange("MinWidth", put("MinWidth", w), w, -1);
}

/**
 * Returns whether shape minimum height is set.
 */
public boolean isMinHeightSet()  { return get("MinHeight")!=null; }

/**
 * Returns the shape minimum height.
 */
public double getMinHeight()  { Double h = (Double)get("MinHeight"); return h!=null? h : 0; }

/**
 * Returns the shape minimum height.
 */
public double getMinHeight(double aValue)  { Double h = (Double)get("MinHeight"); return h!=null? h : 0; }

/**
 * Sets the shape minimum height.
 */
public void setMinHeight(double aHeight)
{
    double h = aHeight<=0? 0 : aHeight; if(h==getMinHeight()) return;
    firePropertyChange("MinHeight", put("MinHeight", h), h, -1);
}

/**
 * Sets the shape minimum size.
 */
public void setMinSize(double aWidth, double aHeight)  { setMinWidth(aWidth); setMinHeight(aHeight); }

/**
 * Returns whether shape preferred width is set.
 */
public boolean isPrefWidthSet()  { return get("PrefWidth")!=null; }

/**
 * Returns the shape preferred width.
 */
public double getPrefWidth()  { return getPrefWidth(-1); }

/**
 * Returns the shape preferred width.
 */
public double getPrefWidth(double aValue)
{
    Double v = (Double)get("PrefWidth"); if(v!=null) return v;
    return computePrefWidth(-1);
}

/**
 * Sets the shape preferred width.
 */
public void setPrefWidth(double aWidth)
{
    double w = aWidth<=0? 0 : aWidth; if(w==getPrefWidth()) return;
    firePropertyChange("PrefWidth", put("PrefWidth", w), w, -1);
}

/**
 * Computes the preferred width for given height.
 */
protected double computePrefWidth(double aHeight)  { return getWidth(); }

/**
 * Returns whether shape preferred height is set.
 */
public boolean isPrefHeightSet()  { return get("PrefHeight")!=null; }

/**
 * Returns the shape preferred height.
 */
public double getPrefHeight()  { return getPrefHeight(-1); }

/**
 * Returns the shape preferred height.
 */
public double getPrefHeight(double aValue)
{
    Double v = (Double)get("PrefHeight"); if(v!=null) return v;
    return computePrefHeight(-1);
}

/**
 * Sets the shape preferred height.
 */
public void setPrefHeight(double aHeight)
{
    double h = aHeight<=0? 0 : aHeight; if(h==getPrefHeight()) return;
    firePropertyChange("PrefHeight", put("PrefHeight", h), h, -1);
}

/**
 * Computes the preferred height for given width.
 */
protected double computePrefHeight(double aWidth)  { return getHeight(); }

/**
 * Returns the best width for current height.
 */
public double getBestWidth()  { return getBestWidth(-1); }

/**
 * Returns the best width for current height.
 */
public double getBestWidth(double aValue)  { return Math.max(getMinWidth(aValue), getPrefWidth(aValue)); }

/**
 * Returns the best height for current width.
 */
public double getBestHeight()  { return getBestHeight(-1); }

/**
 * Returns the best height for current width.
 */
public double getBestHeight(double aValue)  { return Math.max(getMinHeight(aValue), getPrefHeight(aValue)); }

/**
 * Sets the shape to its best height (which is just the current height for most shapes).
 */
public void setBestHeight()  { setHeight(getBestHeight()); }

/**
 * Sets the shape to its best size.
 */
public void setBestSize()  { setSize(getBestWidth(), getBestHeight()); }

/**
 * Divides the shape by a given amount from the top. Returns a clone of the given shape with bounds 
 * set to the remainder. Divies children among the two shapes (recursively calling divide shape for those stradling).
 */
public RMShape divideShapeFromTop(double anAmount)  { return divideShapeFromEdge(anAmount, RMRect.MinYEdge, null); }

/**
 * Divides the shape by a given amount from the given edge. Returns newShape (or, if null, a clone)
 * whose bounds have been set to the remainder.
 */
public RMShape divideShapeFromEdge(double anAmount, byte anEdge, RMShape aNewShape)
{
    // Get NewShape (if aNewShape is null, create one)
    RMShape newShape = aNewShape!=null? aNewShape : createDivideShapeRemainder(anEdge);

    // Get bounds for this shape and remainder bounds (divide bounds by amount from edge)
    RMRect bounds = getFrame();
    RMRect remainder = bounds.divideRect(anAmount, anEdge);
    
    // Set this shape's new bounds and NewShape bounds as remainder
    setFrame(bounds);
    newShape.setFrame(remainder);
    return newShape;
}

/**
 * Creates a shape suitable for the "remainder" portion of a divideShape call (just a clone by default).
 */
protected RMShape createDivideShapeRemainder(byte anEdge)  { return clone(); }

/**
 * Returns the animator that this shape registers changes with.
 */
public RMAnimator getAnimator()  { return getAnimator(false); }

/**
 * Returns the animator that this shape registers changes with (creating, if requested and currently null).
 */
public RMAnimator getAnimator(boolean create)
{
    return getParent()!=null? getParent().getChildAnimator(create) : null;
}

/**
 * Returns the animator that this shape's children use.
 */
public RMAnimator getChildAnimator()  { return getChildAnimator(false); }

/**
 * Returns the animator that this shape's children use (creating, if requested and currently null). The base
 * implementation passes request onto ancestors, but some subclasses create and manage one (RMPage, RMSwitchShape).
 */
public RMAnimator getChildAnimator(boolean create)
{
    return getParent()!=null? getParent().getChildAnimator(create) : null;
}

/**
 * Add mouse listener.
 */
public void addMouseListener(RMShapeMouseListener aListener)  { addListener(RMShapeMouseListener.class, aListener); }

/**
 * Remove mouse listener.
 */
public void removeMouseListener(RMShapeMouseListener aLsnr)  { removeListener(RMShapeMouseListener.class, aLsnr); }

/**
 * Returns whether shape accepts mouse events.
 */
public boolean acceptsMouse() 
{
    // If hover shape, return false
    if(RMShapeUtils.getHoverShape()==this) return false;
    
    // Return true if there is a URL, Hover or MouseListener
    return getURL()!=null || getHover()!=null || getListenerCount(RMShapeMouseListener.class)>0;
}

/**
 * Handles mouse pressed events.
 */
public void mousePressed(RMShapeMouseEvent anEvent)
{
    // Iterate over mouse listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mousePressed(anEvent);
}

/**
 * Handles mouse dragged events.
 */
public void mouseDragged(RMShapeMouseEvent anEvent)
{
    // Iterate over mouse motion listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseDragged(anEvent);
}

/**
 * Handles mouse released events.
 */
public void mouseReleased(RMShapeMouseEvent anEvent)
{
    // Iterate over mouse listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseReleased(anEvent);
    
    // If URL is present and mouse released in shape, fire url
    if(getURL()!=null && contains(anEvent.getPoint2D()))
        anEvent.getViewer().performActionURLClick(getURL());
}

/**
 * Handles mouse clicked events.
 */
public void mouseClicked(RMShapeMouseEvent anEvent)
{
    // Iterate over mouse listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseClicked(anEvent);
}

/**
 * Handles mouse entered events.
 */
public void mouseEntered(RMShapeMouseEvent anEvent)
{
    // Set cursor
    anEvent.getViewer().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
    // Iterate over mouse listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseEntered(anEvent);
    
    // If alt is down and name starts with hover, get hover shape
    if(getHover()!=null)
        RMShapeUtils.getHoverTimer(anEvent);
}

/**
 * Handles mouse moved events.
 */
public void mouseMoved(RMShapeMouseEvent anEvent)
{
    // If timer, restart
    if(RMShapeUtils.getHoverTimer()!=null && RMShapeUtils.getHoverTimer().getShape()==this)
        RMShapeUtils.getHoverTimer(anEvent);
    
    // Iterate over mouse motion listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseMoved(anEvent);
}

/**
 * Handles mouse exited events.
 */
public void mouseExited(RMShapeMouseEvent anEvent)
{
    // Clear hover timer and if hover shape present, remove it
    RMShapeUtils.setHoverTimer(null);
    if(RMShapeUtils.getHoverShape()!=null)
        getPageShape().removeChild(RMShapeUtils.getHoverShape());
    
    // Iterate over mouse listeners and forward
    for(int i=0, iMax=getListenerCount(RMShapeMouseListener.class); i<iMax; i++)
        getListener(RMShapeMouseListener.class, i).mouseExited(anEvent);
}

/**
 * Returns whether this shape is hit by the point, given in this shape's parent's coords.
 */
public boolean contains(RMPoint aPoint)
{
    // Get line width to be used in contain test
    float lineWidth = getStrokeWidth();
    
    // If polygon or line, make line width effectively at least 8, so users will have a better shot of selecting it
    if(this instanceof RMPolygonShape || this instanceof RMLineShape)
        lineWidth = Math.max(8, getStrokeWidth());
    
    // Get bounds, adjusted for line width
    RMRect bounds = getBoundsInside();
    bounds.inset(-lineWidth/2, -lineWidth/2);

    // If point isn't even in bounds rect, just return false
    if(!aPoint.inRect(bounds))
        return false;
    
    // Get shape path in bounds rect
    RMPath path = getPathInBounds();
    
    // Return whether path intersects point
    return path.intersects(aPoint, lineWidth);
}

/**
 * Returns whether this shape is hit by the path, given in this shape's parent's coords.
 */
public boolean intersects(RMPath aPath)
{
    // Get line width to be used in intersects test
    float lineWidth = getStrokeWidth();
    
    // Get bounds, adjusted for line width
    RMRect bounds = getBoundsInside();
    bounds.inset(-lineWidth/2, -lineWidth/2);

    // If paths don't even intersect bounds, just return false
    if(!aPath.getBounds2D().intersectsRectEvenIfEmpty(bounds))
        return false;
    
    // Get path in bounds
    RMPath path = getPathInBounds();
    
    // Return whether path intersects given path
    return path.intersects(aPath, lineWidth);
}

/**
 * Returns the dataset key associated with this shape.
 */
public String getDatasetKey()  { return null; }

/**
 * Returns the entity this shape should show in keys browser.
 */
public Entity getDatasetEntity()
{
    // Get parent and parent entity (just return null, if null)
    RMShape parent = getParent(); if(parent==null) return null;
    Entity parentEntity = parent.getDatasetEntity(); if(parentEntity==null) return null;
    
    // Get Property/RelationEntity for Shape.DatasetKey
    Property prop = getDatasetKey()!=null? parentEntity.getKeyPathProperty(getDatasetKey()) : null;
    Entity entity = prop!=null && prop.isRelation()? prop.getRelationEntity() : null;
    return entity!=null? entity : parentEntity;
}

/**
 * Returns the property names for helper's instance class.
 */
public List <String> getPropertyNames()
{
    // Get list from PropertyNamesMap - load if not found
    List <String> list = _propertyNamesMap.get(getClass());
    if(list==null) {
        _propertyNamesMap.put(getClass(), list = new ArrayList()); addPropNames(); }
    return list;
}

/**
 * Adds the property names for this shape.
 */
protected void addPropNames()
{
    addPropNames("Visible", "X", "Y", "Width", "Height", "Roll", "ScaleX", "ScaleY",
        "Font", "TextColor", "FillColor", "StrokeColor", "URL");
}

/**
 * Adds given property names to the front of property names list.
 */
protected final void addPropNames(String ... theNames)  { Collections.addAll(getPropertyNames(), theNames); }

/**
 * Returns a mapped property name.
 */
public String getPropertyNameMapped(String aName)  { return aName; }

/**
 * Returns the number of bindings associated with shape.
 */
public int getBindingCount()  { List bindings = getBindings(false); return bindings!=null? bindings.size() : 0; }

/**
 * Returns the individual binding at the given index.
 */
public Binding getBinding(int anIndex)  { return getBindings(true).get(anIndex); }

/**
 * Returns the list of bindings, with an option to create if missing.
 */
protected List <Binding> getBindings(boolean doCreate)
{
    List <Binding> bindings = (List)get("RibsBindings");
    if(bindings==null && doCreate)
        put("RibsBindings", bindings = new ArrayList());
    return bindings;
}

/**
 * Adds the individual binding to the shape's bindings list.
 */
public void addBinding(Binding aBinding)
{
    removeBinding(aBinding.getPropertyName()); // Remove current binding for property name (if it exists)
    List <Binding> bindings = getBindings(true); // Add binding
    bindings.add(aBinding);
    aBinding.setNode(this); // Set binding width to this shape
}

/**
 * Removes the binding at the given index from shape's bindings list.
 */
public Binding removeBinding(int anIndex)  { return getBindings(true).remove(anIndex); }

/**
 * Returns the individual binding with the given property name.
 */
public Binding getBinding(String aPropertyName)
{
    // Iterate over bindings and return the first that matches given property name
    for(int i=0, iMax=getBindingCount(); i<iMax; i++)
        if(getBinding(i).getPropertyName().equals(aPropertyName))
            return getBinding(i);
    return null; // Return null since binding not found
}

/**
 * Removes the binding with given property name.
 */
public boolean removeBinding(String aPropertyName)
{
    // Iterate over binding and remove given binding
    for(int i=0, iMax=getBindingCount(); i<iMax; i++)
        if(getBinding(i).getPropertyName().equals(aPropertyName)) {
            removeBinding(i); return true; }
    return false; // Return false since binding not found
}

/**
 * Adds a binding for given name and key.
 */
public void addBinding(String aPropName, String aKey)  { addBinding(new Binding(aPropName, aKey)); }

/**
 * Returns whether given event is enabled.
 */
public boolean isEnabled(UIEvent.Type aType)  { return getEventAdapter(true).isEnabled(aType); }

/**
 * Sets whether given event is enabled.
 */
public void setEnabled(UIEvent.Type aType, boolean aValue)  { getEventAdapter(true).setEnabled(aType, aValue); }

/**
 * Returns the ShapeEventAdapter for this shape.
 */
public RMShapeEventAdapter getEventAdapter(boolean doCreate)
{
    RMShapeEventAdapter sea = (RMShapeEventAdapter)get("EventAdapter");
    if(sea==null && doCreate)
        put("EventAdapter", sea = new RMShapeEventAdapter(this));
    return sea;
}

/**
 * Standard implementation of Object clone. Null's out shape's parent and children.
 */
public RMShape clone()
{
    // Do normal version, clear parent, LayoutInfoX, clone RSS
    RMShape clone = (RMShape)super.clone();
    clone._parent = null; clone._layoutInfoX = null;
    clone._rss = RMUtils.clone(_rss);
    
    // Clone stroke, fill, effect
    clone._stroke = null; clone._fill = null; clone._effect = null;
    if(getStroke()!=null) clone.setStroke(getStroke().clone());
    if(getFill()!=null) clone.setFill(getFill().clone());
    if(getEffect()!=null) clone.setEffect(getEffect().clone());
    
    // Copy attributes map
    clone._attrMap = _attrMap.clone();
    
    // If shape has timeline, clone it
    if(getTimeline()!=null)
        clone.setTimeline(getTimeline().clone(clone));
    
    // Clone bindings and add to clone (with hack to make sure clone has it's own, non-shared, attr map)
    for(int i=0, iMax=getBindingCount(); i<iMax; i++) {
        if(i==0) clone.put("RibsBindings", null);
        clone.addBinding(getBinding(i).clone());
    }
    
    // Clone event adapter
    if(getEventAdapter(false)!=null) {
        clone.put("EventAdapter", null);
        clone.getEventAdapter(true).setEnabledEvents(getEventAdapter(true).getEnabledEvents());
    }
    
    // Return clone
    return clone;
}

/**
 * Clones all attributes of this shape with complete clones of its children as well.
 */
public RMShape cloneDeep()  { return clone(); }

/**
 * Copies basic shape attributes from given RMShape (location, size, fill, stroke, roll, scale, name, url, etc.).
 */
public void copyShape(RMShape aShape)
{
    // Copy bounds
    setBounds(aShape._x, aShape._y, aShape._width, aShape._height);
    
    // Copy roll, scale, skew
    if(aShape.isRSS()) {
        setRoll(aShape.getRoll());
        setScaleXY(aShape.getScaleX(), aShape.getScaleY());
        setSkewXY(aShape.getSkewX(), aShape.getSkewY());
    }
    
    // Copy Stroke, Fill, Effect
    if(!RMUtils.equals(getStroke(), aShape.getStroke())) setStroke(RMUtils.clone(aShape.getStroke()));
    if(!RMUtils.equals(getFill(), aShape.getFill())) setFill(RMUtils.clone(aShape.getFill()));
    if(!RMUtils.equals(getEffect(), aShape.getEffect())) setEffect(RMUtils.clone(aShape.getEffect()));
    
    // Copy Opacity and Visible
    setOpacity(aShape.getOpacity());
    setVisible(aShape.isVisible());
    
    // Copy Name, Url, Hover, Locked
    setName(aShape.getName());
    setURL(aShape.getURL());
    setHover(aShape.getHover());
    setLocked(aShape.isLocked());
    
    // Copy LayoutInfo
    setLayoutInfo(aShape.getLayoutInfo());
    
    // Copy timeline (if this goes back, then RMAnimPathShape(shape) needs to go back to clearing it
    //if(aShape.getTimeline()!=null) setTimeline(aShape.getTimeline().clone(this));
    
    // Copy bindings
    while(getBindingCount()>0) removeBinding(0);
    for(int i=0, iMax=aShape.getBindingCount(); i<iMax; i++)
        addBinding(aShape.getBinding(i).clone());
}

/**
 * Generate report with report owner.
 */
public RMShape rpgAll(ReportOwner anRptOwner, RMShape aParent)
{
    RMShape clone = rpgShape(anRptOwner, aParent);
    rpgBindings(anRptOwner, clone);
    return clone;
}

/**
 * Generate report with report owner.
 */
protected RMShape rpgShape(ReportOwner anRptOwner, RMShape aParent)  { return clone(); }

/**
 * Report generation for URL and bindings.
 */
public void rpgBindings(ReportOwner anRptOwner, RMShape aShapeRPG)
{
    // Clone URL
    if(getURL()!=null && getURL().length()>0 && getURL().indexOf('@')>=0) {
        RMXString url = new RMXString(getURL()).rpgClone(anRptOwner, null, aShapeRPG, false);
        aShapeRPG.setURL(url.getText());
    }
    
    // Clone Hover
    if(getHover()!=null && getHover().length()>0 && getHover().indexOf('@')>=0) {
        RMXString hover = new RMXString(getHover()).rpgClone(anRptOwner, null, aShapeRPG, false);
        aShapeRPG.setHover(hover.getText());
    }
    
    // Iterate over bindings and evaluate
    for(int i=0; i<getBindingCount(); i++) { Binding binding = getBinding(i);
        
        // Get PropertyName, Key and Value (just continue if empty key)
        String pname = binding.getPropertyName(); if(pname==null) continue;
        String key = binding.getKey(); if(key==null || key.length()==0) continue;
        Object value = RMKeyChain.getValue(anRptOwner, key);
        
        // Handle Font
        if(pname.equals("Font")) {
            
            // Get value as string (if zero length, just continue)
            String fs = value instanceof String? (String)value : null; if(fs==null || fs.length()==0) continue;
            
            // If string has underline in it, underline and delete
            if(RMStringUtils.indexOfIC(fs, "Underline")>=0) {
                aShapeRPG.setUnderlined(true); fs = RMStringUtils.deleteIC(fs, "Underline").trim(); }
            
            // Get size from string (if found, strip size from string)
            int sizeIndex = fs.lastIndexOf(" ");
            double size = sizeIndex<0 ? 0 : RMUtils.floatValue(fs.substring(sizeIndex+1));
            if(size>0) fs = fs.substring(0, Math.max(sizeIndex, 0)).trim();
            else size = getFont()==null? 12 : getFont().getSize();
            
            // Get root font (use default font if not found), and modified font
            RMFont font = getFont(); if(font==null) font = RMFont.getDefaultFont();
            if(fs.equalsIgnoreCase("Bold")) font = font.getBold();
            else if(fs.equalsIgnoreCase("Italic")) font = font.getItalic();
            else if(fs.length()>0) // If there is anything in string, try to parse font name
                font = new RMFont(fs, size);
            
            // Get font at right size and apply it
            font = font.deriveFont(size);
            aShapeRPG.setFont(font);
        }

        // Handle FillColor, StrokeColor, TextColor
        else if(pname.equals("FillColor")) { RMColor color = RMColor.colorValue(value); 
            if(color!=null) aShapeRPG.setColor(color); }
        else if(pname.equals("StrokeColor")) { RMColor color = RMColor.colorValue(value); 
            if(color!=null) aShapeRPG.setStrokeColor(color); }
        else if(pname.equals("TextColor")) { RMColor color = RMColor.colorValue(value); 
            if(color!=null) aShapeRPG.setTextColor(color); }
        
        // Handle others: X, Y, Width, Height, Visible, URL
        else RMKey.setValueSafe(aShapeRPG, pname, value);
    }
}

/**
 * Replaces all @Page@ style keys with their actual values for this shape and it's children.
 */
protected void resolvePageReferences(ReportOwner aRptOwner, Object userInfo)
{
    // If URL has @-sign, do rpg clone in case it is page reference
    if(getURL()!=null && getURL().length()>0 && getURL().indexOf('@')>=0) {
        RMXString url = new RMXString(getURL()).rpgClone(aRptOwner, userInfo, null, false);
        setURL(url.getText());
    }
}

/**
 * Visual change notification - call before making changes that will require repaint.
 */
public void repaint()  { if(_parent!=null) _parent.repaint(this); }

/**
 * Visual change notification - call before making changes that will require repaint.
 */
protected void repaint(RMShape aShape)  { if(_parent!=null) _parent.repaint(aShape); }

/** Editor method - indicates whether this shape can be super selected. */
public boolean superSelectable()  { return getClass()==RMParentShape.class; }

/** Editor method. */
public boolean acceptsChildren()  { return getClass()==RMParentShape.class; }

/** Editor method. */
public boolean childrenSuperSelectImmediately()  { return _parent==null; }

/**
 * Page number resolution.
 */
public int page()  { return _parent!=null? _parent.page() : 0; }

/**
 * Page number resolution.
 */
public int pageMax()  { return _parent!=null? _parent.pageMax() : 0; }

/**
 * Returns the "PageBreak" for this shape as defined by shapes that define a page break (currently only RMTable).
 */
public int getPageBreak()  { return _parent!=null? _parent.getPageBreak() : 0; }

/**
 * Returns the "PageBreakMax" for this shape as defined by shapes that define a page break (currently only RMTable).
 */
public int getPageBreakMax()  { return _parent!=null? _parent.getPageBreakMax() : 0; }

/**
 * Returns the "PageBreakPage" for this shape, or the page number relative to the last page break,
 * as defined by shapes that define explicit page breaks (currently only RMTable).
 */
public int getPageBreakPage()  { return _parent!=null? _parent.getPageBreakPage() : 0; }

/**
 * Returns the "PageBreakPageMax" for this shape, or the max page number relative to the last and next page breaks,
 * as defined by shapes that define explicit page breaks (currently only RMTable).
 */
public int getPageBreakPageMax()  { return _parent!=null? _parent.getPageBreakPageMax() : 0; }

/**
 * Top-level generic shape painting - sets transform and opacity then does a paintAll.
 * If a effect is present, has it paint instead of doing paintAll.
 */
public void paint(RMShapePainter aPntr)
{
    // Clone graphics
    RMShapePainter pntr = aPntr.clone();
    
    // Apply transform for shape
    if(isRSS()) pntr.transform(getTransform().awt());
    else pntr.translate(getX(), getY());
    
    // If shape bounds don't intersect clip bounds, just return
    Rectangle cbounds = pntr.getClip()!=null? pntr.getClipBounds() : null;
    if(cbounds!=null && !getBoundsMarkedDeep().intersects(cbounds))
        return;
    
    // If shape is semi-transparent, apply composite
    if(getOpacityDeep()!=1) {
        float op = pntr.isEditing()? Math.max(.15f, getOpacityDeep()) : getOpacityDeep();
        pntr.setOpacity(op);
    }
    
    // If shape has a effect, have it paint
    if(getEffect()!=null)
        getEffect().paint(pntr, this);
    
    // Otherwise paintShapeAll
    else paintShapeAll(pntr);
    
    // Dispose of graphics
    pntr.dispose();
}

/**
 * Calls paintShape, paintShapeChildren and paintShapeOver.
 */
public void paintShapeAll(RMShapePainter aPntr)
{
    // Get graphics
    RMShapePainter pntr = aPntr;
    
    // If clipping, clip to shape
    if(getClipShape()!=null) {
        pntr = pntr.clone();
        pntr.clip(getClipShape());
    }
        
    // Have shape paint only itself
    pntr.sendPaintShape(this);
    
    // Have shape paint children
    paintShapeChildren(pntr);
        
    // Have shape paint over
    paintShapeOver(aPntr);
    
    // If graphics copied, dispose
    if(pntr!=aPntr) pntr.dispose();
}

/**
 * Basic shape painting - paints shape fill and stroke.
 */
public void paintShape(RMShapePainter aPntr)
{
    // If fill/stroke present, have them paint
    if(getFill()!=null)
        getFill().paint(aPntr, this);
    if(getStroke()!=null && !getStrokeOnTop())
        getStroke().paint(aPntr, this);
}

/**
 * Paints shape children.
 */
public void paintShapeChildren(RMShapePainter aPntr)
{
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i);
        if(child.isVisible())
            child.paint(aPntr); }
}

/**
 * Paints after (on top) of children.
 */
public void paintShapeOver(RMShapePainter aPntr)
{
    if(getStrokeOnTop() && getStroke()!=null)
        getStroke().paint(aPntr, this);
}

/**
 * Returns whether to stroke on top.
 */
public boolean getStrokeOnTop()  { return false; }

/**
 * Returns clip shape for shape.
 */
public Shape getClipShape()  { return null; }

/**
 * Called to update shape anim.
 */
public void animUpdate(PropertyChangeEvent anEvent)
{
    // Return if shape is a new-born
    if(getAnimator()==null || getAnimator().isNewborn(this) || !getAnimator().isEnabled()) return;
    
    // If change is anim property, add record
    if(isAnimProperty(anEvent.getPropertyName()))
        addTimelineEntry(anEvent.getPropertyName(), anEvent.getNewValue(), anEvent.getOldValue());
    
    // Add anim records for Fill
    else if(anEvent.getPropertyName().equals("Fill")) {
        RMFill f1 = (RMFill)anEvent.getNewValue();
        RMFill f2 = (RMFill)anEvent.getOldValue();
        RMColor c1 = f1!=null? f1.getColor() : RMColor.clearWhite;
        RMColor c2 = f2!=null? f2.getColor() : RMColor.clearWhite;
        addTimelineEntry("Color", c1, c2);
    }
    
    // Add anim records for Fill.Color
    else if(anEvent.getPropertyName().equals("Fill.Color"))
        addTimelineEntry("Color", anEvent.getNewValue(), anEvent.getOldValue());
    
    // Add anim records for Stroke
    else if(anEvent.getPropertyName().equals("Stroke")) {
        RMStroke s1 = (RMStroke)anEvent.getNewValue();
        RMStroke s2 = (RMStroke)anEvent.getOldValue();
        RMColor c1 = s1!=null? s1.getColor() : RMColor.clearWhite;
        RMColor c2 = s2!=null? s2.getColor() : RMColor.clearWhite;
        addTimelineEntry("StrokeColor", c1, c2);
        float lw1 = s1!=null? s1.getWidth() : 0;
        float lw2 = s2!=null? s2.getWidth() : 0;
        addTimelineEntry("StrokeWidth", lw1, lw2);
    }
    
    // Add anim records for Stroke.Color
    else if(anEvent.getPropertyName().equals("Stroke.Color"))
        addTimelineEntry("StrokeColor", anEvent.getNewValue(), anEvent.getOldValue());
    
    // Add anim records for Stroke.Width
    else if(anEvent.getPropertyName().equals("Stroke.Width"))
        addTimelineEntry("StrokeWidth", anEvent.getNewValue(), anEvent.getOldValue());
}

/**
 * Adds a record to the timeline.
 */
private void addTimelineEntry(String aKey, Object aValue, Object anOldValue)
{
    // Just return if values equal
    if(RMUtils.equals(aValue, anOldValue)) return;
    
    // Get animator and current time
    RMAnimator animator = getAnimator();
    int time = animator.getTime();
    int oldTime = animator.getScopeTime();
    
    // Get timeline (just return if no timeline or time zero and no key/values for this property) 
    RMTimeline timeline = getTimeline(time!=0);
    if(timeline!=null)
        timeline.addKeyFrameKeyValue(this, aKey, aValue, time, anOldValue, oldTime);
}

/**
 * Returns whether given property name is anim property.
 */
public boolean isAnimProperty(String aPropertyName)
{
    // Declare anim properties
    String animProps[] = { "X", "Y", "Width", "Height", "Roll", "ScaleX", "ScaleY", "SkewX", "SkewY", "Opacity",
        "Radius", // Bogus - for RMRectangle
        "StartAngle", "SweepAngle", // Bogus - for RMOval
        "Depth", "Yaw", "Pitch", "Roll3D", "FocalLenth", "OffsetZ", // for RMScene3D
        "Playing", // Bogus - for RMSound
        "Morphing", // Bogus - for RMMorphShape
        "Distance", "PreservesOrientation" // Bogus - for RMAnimPath
    };
    
    // Return true if is anim property
    return RMArrayUtils.contains(animProps, aPropertyName);
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element called shape
    XMLElement e = new XMLElement("shape");
    
    // Archive name
    if(getName()!=null && getName().length()>0) e.add("name", getName());
    
    // Archive X, Y, Width, Height
    if(_x!=0) e.add("x", _x);
    if(_y!=0) e.add("y", _y);
    if(_width!=0) e.add("width", _width);
    if(_height!=0) e.add("height", _height);
    
    // Archive Roll, ScaleX, ScaleY, SkewX, SkewY
    if(getRoll()!=0) e.add("roll", getRoll());
    if(getScaleX()!=1) e.add("scalex", getScaleX());
    if(getScaleY()!=1) e.add("scaley", getScaleY());
    if(getSkewX()!=0) e.add("skewx", getSkewX());
    if(getSkewY()!=0) e.add("skewy", getSkewY());

    // Archive Stroke, Fill, Effect
    if(getStroke()!=null) e.add(anArchiver.toXML(getStroke(), this));
    if(getFill()!=null) e.add(anArchiver.toXML(getFill(), this));
    if(getEffect()!=null) e.add(anArchiver.toXML(getEffect(), this));
    
    // Archive font
    if(isFontSet()) e.add(getFont().toXML(anArchiver));
    
    // Archive Opacity, Visible
    if(getOpacity()<1) e.add("opacity", getOpacity());
    if(!isVisible()) e.add("visible", false);
    
    // Archive URL, Hover
    if(getURL()!=null && getURL().length()>0) e.add("url", getURL());
    if(getHover()!=null && getHover().length()>0) e.add("hover", getHover());
    
    // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(isMinWidthSet()) e.add("MinWidth", getMinWidth());
    if(isMinHeightSet()) e.add("MinHeight", getMinHeight());
    if(isPrefWidthSet()) e.add("PrefWidth", getPrefWidth());
    if(isPrefHeightSet()) e.add("PrefHeight", getPrefHeight());
    
    // Archive LayoutInfo, Autosizing
    if(getParent()!=null && getParent().getLayout() instanceof RMSpringLayout) {
        if(!getAutosizing().equals(getAutosizingDefault())) e.add("asize", getAutosizing()); }
    else if(getLayoutInfo()!=null) e.add("LayoutInfo", getLayoutInfo());
    
    // Archive Locked
    if(isLocked()) e.add("locked", true);
    
    // Archive shape timeline
    if(getTimeline()!=null) getTimeline().toXML(anArchiver, e);

    // Archive bindings
    for(int i=0, iMax=getBindingCount(); i<iMax; i++)
        e.add(getBinding(i).toXML(anArchiver));
    
    // Archive EnabledEvents
    if(getEventAdapter(false)!=null && getEventAdapter(true).getEnabledEvents().length>0)
        e.add("EnabledEvents", getEventAdapter(true).getEnabledEventsString());

    // Return the element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive name
    setName(anElement.getAttributeValue("name"));
    
    // Unarchive X, Y, Width, Height
    _x = anElement.getAttributeFloatValue("x", 0);
    _y = anElement.getAttributeFloatValue("y", 0);
    _width = anElement.getAttributeFloatValue("width", 0);
    _height = anElement.getAttributeFloatValue("height", 0);
    
    // Unarchive Roll, ScaleX, ScaleY, SkewX, SkewY
    setRoll(anElement.getAttributeFloatValue("roll"));
    setScaleX(anElement.getAttributeFloatValue("scalex", 1));
    setScaleY(anElement.getAttributeFloatValue("scaley", 1));
    setSkewX(anElement.getAttributeFloatValue("skewx", 0));
    setSkewY(anElement.getAttributeFloatValue("skewy", 0));

    // Unarchive Stroke, Fill 
    for(int i=anArchiver.indexOf(anElement, RMFill.class); i>=0; i=anArchiver.indexOf(anElement, RMFill.class, i+1)) {
        RMFill fill = (RMFill)anArchiver.fromXML(anElement.get(i), this);
        if(fill instanceof RMStroke) setStroke((RMStroke)fill);
        else setFill(fill);
    }
    
    // Unarchive Effect
    for(int i=anArchiver.indexOf(anElement, RMEffect.class); i>=0; i=-1) {
        RMEffect fill = (RMEffect)anArchiver.fromXML(anElement.get(i), this);
        setEffect(fill);
    }
    
    // Unarchive font
    XMLElement fontXML = anElement.getElement("font");
    if(fontXML!=null) setFont((RMFont)anArchiver.fromXML(fontXML, this));
    
    // Unarchive Opacity, Visible
    setOpacity(anElement.getAttributeFloatValue("opacity", 1));
    if(anElement.hasAttribute("visible")) _visible = anElement.getAttributeBoolValue("visible");
    
    // Unarchive URL, Hover
    setURL(anElement.getAttributeValue("url"));
    setHover(anElement.getAttributeValue("hover"));
    
    // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(anElement.hasAttribute("MinWidth")) setMinWidth(anElement.getAttributeFloatValue("MinWidth"));
    if(anElement.hasAttribute("MinHeight")) setMinHeight(anElement.getAttributeFloatValue("MinHeight"));
    if(anElement.hasAttribute("PrefWidth")) setPrefWidth(anElement.getAttributeFloatValue("PrefWidth"));
    if(anElement.hasAttribute("PrefHeight")) setPrefHeight(anElement.getAttributeFloatValue("PrefHeight"));
    
    // Unarchive LayoutInfo, Autosizing
    if(anElement.hasAttribute("LayoutInfo")) setLayoutInfo(anElement.getAttributeValue("LayoutInfo"));
    else if(anElement.hasAttribute("asize")) setLayoutInfo(anElement.getAttributeValue("asize"));
    
    // Unarchive Locked
    setLocked(anElement.getAttributeBoolValue("locked"));
    
    // Unarchive animation
    if(anElement.getElement("KeyFrame")!=null || anElement.getElement("anim")!=null) {
        getTimeline(true).fromXML(this, anArchiver, anElement); getAnimator(true); }
    
    // Unarchive bindings
    for(int i=anElement.indexOf("binding"); i>=0; i=anElement.indexOf("binding",i+1)) { XMLElement bxml=anElement.get(i);
        addBinding(new Binding().fromXML(anArchiver, bxml)); }

    // Unarchive property keys (legacy)
    for(int i=anElement.indexOf("property-key"); i>=0; i=anElement.indexOf("property-key", i+1)) {
        XMLElement prop = anElement.get(i); String name = prop.getAttributeValue("name");
        if(name.equals("FontColor")) name = "TextColor"; if(name.equals("IsVisible")) name = "Visible";
        String key = prop.getAttributeValue("key"); addBinding(new Binding(name, key));
    }
    
    // Unarchive EnabledEvents
    if(anElement.hasAttribute("EnabledEvents"))
        getEventAdapter(true).setEnabledEventsString(anElement.getAttributeValue("EnabledEvents"));

    // Return this shape
    return this;
}

/**
 * Standard to string implementation (prints class name and shape bounds).
 */
public String toString()
{
    StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append(' ');
    if(getName()!=null) sb.append(getName()).append(' ');
    sb.append(getFrame().toString());
    return sb.toString();
}

}