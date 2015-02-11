package com.reportmill.base;
import java.awt.Dimension;

/**
 * This class represents a simple geometric size (width, height).
 */
public class RMSize implements Cloneable {
    
    // The width of size
    public double width;
    
    // The height of size
    public double height;

/**
 * Creates a size initialized to 0,0.
 */
public RMSize() { }

/**
 * Creates a size initialized to the given width and height.
 */
public RMSize(double w, double h)  { width = w; height = h; }

/**
 * Creates a size initialized to the given size.
 */
public RMSize(RMSize aSize)  { if(aSize!=null) { width = aSize.width; height = aSize.height; } }

/**
 * Creates a size from a java2D dimension
 */
public RMSize(Dimension d)  { width = d.getWidth(); height = d.getHeight(); }

/**
 * Creates a size from a string (assumes comma separated).
 */
public RMSize(String aString)
{
    width = RMStringUtils.floatValue(aString);
    height = RMStringUtils.doubleValue(aString, aString.indexOf(",") + 1);
}

/**
 * Normalizes the receiver to positive values.
 */
public RMSize abs()
{
    if(width<0) width = -width;
    if(height<0) height = -height;
    return this;
}

/**
 * Returns the square root of the sum of the squares of the width and height.
 */
public double magnitude()  { return Math.sqrt(width*width + height*height); }

/**
 * Normalizes the receiver by scaling its width and height such that its magnitude will be 1.
 */
public RMSize normalize()  { double hyp = magnitude(); width /= hyp; height /= hyp; return this; }

/**
 * Simply sets the width and height to their negatives.
 */
public void negate()  { width = -width; height = -height; }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(!(anObj instanceof RMSize)) return false;
    RMSize size = (RMSize)anObj;
    return size.width==width && size.height==height;
}

/**
 * Returns whether size is equal to given width and height.
 */
public boolean equals(double w, double h)  { return w==width && h==height; }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    try { return super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
}

/**
 * Returns a string representation of size.
 */
public String toString()  { return "{" + width + "," + height + "}"; }

}