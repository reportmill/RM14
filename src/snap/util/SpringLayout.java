/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.awt.geom.Rectangle2D;

/**
 * A class to layout children using springs/struts settings.
 * Settings are described with strings using '~' (spring) and '-' (struts), eg.: "-~-,-~-" (horizontal,vertical).
 */
public abstract class SpringLayout<T> implements Cloneable {

    // The parent that is being laid out
    T                  _parent;
    
    // The current width/height of shape
    double             _width, _height;
    
    // An object to determine preferred size 
    SpringSizer        _sizer;

/**
 * Creates a new SpringLayout for adapter.
 */
public SpringLayout(T aParent)  { setParent(aParent); }

/**
 * Returns the parent.
 */
public T getParent()  { return _parent; }

/**
 * Sets the parent.
 */
public void setParent(T aParent)  { _parent = aParent; }

/**
 * Return LayoutInfo for child.
 */
public SPLayoutInfo getLayoutInfoSP(T aChild)
{
    Object lix = getLayoutInfoX(aChild);
    if(!(lix instanceof SpringLayout.SPLayoutInfo))
        setLayoutInfoX(aChild, lix = new SPLayoutInfo(aChild));
    return (SPLayoutInfo)lix;
}

/**
 * Returns the autosizing string for child.
 */
public String getAutosizing(T aChild)
{
    Object springs = getLayoutInfo(aChild);
    String ss = springs instanceof String? (String)springs : null;
    return ss!=null && ss.length()>6 && (ss.charAt(0)=='-' || ss.charAt(0)=='~')? ss : "--~,--~";
}

/**
 * Override to force LayoutInfoX to get set.
 */
public void addChild(T aChild, Object aLayoutInfo)
{
    if(aLayoutInfo!=null) setLayoutInfo(aChild, aLayoutInfo);
    getLayoutInfoSP(aChild); _sizer = null;
}

/**
 * Override to clear LayoutInfoX.
 */
public void removeChild(T aChild)  { setLayoutInfoX(aChild, null); _sizer = null; }

/**
 * Updates a child if its layout info has changed.
 */
public void updateChild(T aChild)  { getLayoutInfoSP(aChild).update(aChild); }

/**
 * Performs layout.
 */
public void layoutChildren(T aParent)
{
    // If size hasn't changed, just return
    if(_width==getParentWidth(aParent) && _height==getParentHeight(aParent)) return;
    
    // Update Width/Height
    _width = getParentWidth(aParent); _height = getParentHeight(aParent);
    
    // Get child bounds rects and set bounds of children for new width/height
    Rectangle2D rects[] = getChildrenBoundsRects();
    for(int i=0, iMax=getChildCount(aParent); i<iMax; i++) {
        T child = getChild(aParent, i); Rectangle2D rect = rects[i];
        setBounds(child, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}

/**
 * Returns the child rects for given parent height.
 */
private Rectangle2D.Double[] getChildrenBoundsRects()
{
    // If Sizer.ChildBoxes is set, return them
    if(_sizer!=null && _sizer._cboxes!=null) return _sizer._cboxes;
    
    // Get original child rects
    int ccount = getChildCount(_parent);
    double newPW = getParentWidth(_parent), newPH = getParentHeight(_parent);
    
    // Iterate over children and calculate new bounds rect for original child bounds and new parent width/height
    Rectangle2D.Double rects[] = new Rectangle2D.Double[ccount];
    for(int i=0; i<ccount; i++) { T child = getChild(_parent, i);
        SPLayoutInfo li = getLayoutInfoSP(child);
        Rectangle2D.Double rect = rects[i] = new Rectangle2D.Double(li._x, li._y, li._w, li._h);
        double oldPW = li._pw, oldPH = li._ph;
        String asize = getAutosizing(child);
        boolean lms = asize.charAt(0)=='~', ws = asize.charAt(1)=='~', rms = asize.charAt(2)=='~';
        boolean tms = asize.charAt(4)=='~', hs = asize.charAt(5)=='~', bms = asize.charAt(6)=='~';
        double x1 = rect.getX(), y1 = rect.getY(), w1 = rect.getWidth(), h1 = rect.getHeight();
        double sw = (lms? x1 : 0) + (ws? w1 : 0) + (rms? oldPW - (x1 + w1) : 0), dw = newPW - oldPW;
        double sh = (tms? y1 : 0) + (hs? h1 : 0) + (bms? oldPH - (y1 + h1) : 0), dh = newPH - oldPH;
        double x2 = (!lms || sw==0)? x1 : (x1 + dw*x1/sw);
        double y2 = (!tms || sh==0)? y1 : (y1 + dh*y1/sh);
        double w2 = (!ws || sw==0)? w1 : (w1 + dw*w1/sw);
        double h2 = (!hs || sh==0)? h1 : (h1 + dh*h1/sh);
        rect.setRect(x2, y2, w2, h2);
    }
    
    // Return rects
    return rects;
}

/**
 * Update LayoutInfo for all children. 
 */
public void reset()
{
    _width = getParentWidth(_parent); _height = getParentHeight(_parent);
    for(int i=0, iMax=getChildCount(_parent); i<iMax; i++) { T child = getChild(_parent, i);
        SpringLayout.SPLayoutInfo layoutInfo = getLayoutInfoSP(child);
        layoutInfo.update(child);
    }
}

/**
 * The min width for layout.
 */
public double getMinWidth(double aHeight)  { return 0; }
    
/**
 * The min height for layout.
 */
public double getMinHeight(double aWidth)  { return 0; }
    
/**
 * The preferred width for layout.
 */
public double getPrefWidth(double aHeight)  { return getParentWidth(_parent); }
    
/**
 * The preferred height for layout.
 */
public double getPrefHeight(double aWidth)  { return getSizer().getPrefHeight(); }

/** Returns the sizer. */
private SpringSizer getSizer()  { return _sizer!=null? _sizer : (_sizer=new SpringSizer(this)); }

/** Returns the internal layout info for a child. */
public abstract Object getLayoutInfoX(T aChild);

/** Sets the internal layout info for a child. */
public abstract void setLayoutInfoX(T aChild, Object aLIX);

/** Returns parent width. */
public abstract double getParentWidth(T aParent);

/** Returns parent height. */
public abstract double getParentHeight(T aParent);

/** Returns the child count of given parent. */
public abstract int getChildCount(T aParent);

/** Returns the individual child in this line. */
public abstract T getChild(T aParent, int anIndex);

/** Returns the insets of the parent. */
public abstract SPInsets getInsets(T aParent);

/** Returns layout info (descriptor) for child. */
public abstract Object getLayoutInfo(T aChild);

/** Sets layout info (descriptor) for child. */
public abstract void setLayoutInfo(T aChild, Object aLI);

/** Returns child x. */
public abstract double getX(T aChild);

/** Returns child y. */
public abstract double getY(T aChild);

/** Returns child width. */
public abstract double getWidth(T aChild);

/** Returns child height. */
public abstract double getHeight(T aChild);

/** Set child bounds. */
public abstract void setBounds(T aChild, double anX, double aY, double aWidth, double aHeight);

/** Returns child min width. */
public abstract double getMinWidth(T aChild, double aValue);

/** Returns child max height. */
public abstract double getMinHeight(T aChild, double aValue);

/** Returns child preferred width. */
public abstract double getPrefWidth(T aChild, double aValue);

/** Returns child preferred height. */
public abstract double getPrefHeight(T aChild, double aValue);

/** Returns child best height. */
public double getBestHeight(T aChild, double aValue)
{
    return Math.max(getPrefHeight(aChild, aValue), getMinHeight(aChild, aValue));
}

/**
 * Standard clone implementation.
 */
public SpringLayout clone()
{
    try { SpringLayout clone = (SpringLayout)super.clone(); clone._parent = null; clone._sizer = null; return clone; }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
}

/**
 * LayoutInfo for SpringLayout.
 */
public class SPLayoutInfo {

    // The bounds of child when added and size of parent when added
    double            _x, _y, _w, _h, _pw, _ph;

    /** Creates a new LayoutInfo. */
    protected SPLayoutInfo(T aChild) { update(aChild); }
    
    /** Updates values for shape. */
    public void update(T aChild)
    {
        _x = getX(aChild); _y = getY(aChild);
        _w = getWidth(aChild); _h = getHeight(aChild);
        _pw = getParentWidth(_parent); _ph = getParentHeight(_parent);
    }
}

}