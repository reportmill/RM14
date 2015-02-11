package snap.util;

/**
 * A layout that puts children one after another, wrapping when needed.
 */
public abstract class FlowLayout <T> implements Cloneable {

    // The spacing x & y
    double             _sx = 5, _sy = 5;
    
    // Whether layout wraps
    boolean            _wraps = true;

/**
 * Returns the spacing x.
 */
public double getSpacingX()  { return _sx; }

/**
 * Sets the spacing x.
 */
public void setSpacingX(double aValue)  { _sx = aValue; }

/**
 * Returns the spacing y.
 */
public double getSpacingY()  { return _sy; }

/**
 * Sets the spacing y.
 */
public void setSpacingY(double aValue)  { _sy = aValue; }

/**
 * Returns whether layout wraps.
 */
public boolean getWraps()  { return _wraps; }

/**
 * Sets whether layout wraps.
 */
public void setWraps(boolean aValue)  { _wraps = aValue; }

/**
 * Layout children.
 */
public void layoutChildren(T aParent)
{
    // Get parent specs
    double w = getParentWidth(aParent), h = getParentHeight(aParent);
    SPInsets pad = getInsets(aParent); double pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
    
    // Iterate over children and set bounds
    double x = pl, y = pt, lh = 0;
    for(int i=0, iMax=getChildCount(aParent); i<iMax; i++) { T child = getChild(aParent, i);
        double cw = getPrefWidth(child, -1), ch = getPrefHeight(child, -1);
        if(x+cw+pr>w) { x = pl; y += lh + _sy; lh = 0; }
        setBounds(child, x, y, cw, ch);
        x += _sx + cw;
        Object li = getLayoutInfo(child);
        if(li instanceof String && ((String)li).equals("N")) { x = pl; y += lh + _sy; lh = 0; }
        if(ch>lh) lh = ch;
        if(y+ch+pb>h) setBounds(child, w + 1, 0, cw, ch);
    }
}

/** The preferred width for layout. */
public double getPrefWidth(double aHeight)  { return 0; }

/** The preferred height for layout. */
public double getPrefHeight(double aWidth)  { return 0; }

/** The min width for layout. */
public double getMinWidth(double aHeight)  { return 0; }
    
/** The min height for layout. */
public double getMinHeight(double aWidth)  { return 0; }
    
/**
 * Adds a layout child.
 */
public void addChild(T aChild, Object aLayoutInfo)  { if(aLayoutInfo!=null) setLayoutInfo(aChild, aLayoutInfo); }

/**
 * Removes a child.
 */
public void removeChild(T aChild)  { }

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

/** Returns child preferred width. */
public abstract double getPrefWidth(T aChild, double aValue);

/** Returns child preferred height. */
public abstract double getPrefHeight(T aChild, double aValue);

/**
 * Standard clone implementation.
 */
public FlowLayout clone()
{
    try { FlowLayout clone = (FlowLayout)super.clone(); return clone; }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
}

}