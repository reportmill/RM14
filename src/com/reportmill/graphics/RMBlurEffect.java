package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.image.*;
import snap.util.*;

/**
 * This class adds a blur to a given shape.
 */
public class RMBlurEffect extends RMImageEffect {
    
    // The blur radius
    int             _radius = 5;
    
/**
 * Creates a new blur effect.
 */
public RMBlurEffect()  { }

/**
 * Creates a new blur effect with the parameters.
 */
public RMBlurEffect(int aRadius)  { _radius = aRadius; }

/**
 * Returns the radius of the blur.
 */
public int getRadius()  { return _radius; }

/**
 * Returns a blur effect just like this one, but with a radius equal to the given value.
 */
public RMBlurEffect deriveFill(int aRadius)
{
    RMBlurEffect effect = (RMBlurEffect)clone();
    effect._radius = aRadius;
    return effect;
}

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)
{
    // If disabled, return pathBounds
    if(!isEnabled())
        return super.getBounds(aShape);
    
    // Get bounds for shape with effect turned off
    setEnabled(false);
    RMRect bounds = aShape.getBoundsMarkedDeep();
    setEnabled(true);
    
    // Offset and extend bounds to account for blur radius
    bounds.x = bounds.x - getRadius()*2;
    bounds.y = bounds.y - getRadius()*2;
    bounds.width += getRadius()*4;
    bounds.height += getRadius()*4;
    
    // Return rect
    return bounds;
}

/**
 * Returns the blur image.
 */
public BufferedImage getImage(RMShape aShape)
{
    // Create new image for effect image
    BufferedImage shapeImage = getShapeImage(aShape, getRadius()*2, true);
    
    // Get blurred image
    BufferedImage blurImage = getBlurredImage(shapeImage, getRadius(), getRadius());
    
    // Return blurred image
    return blurImage;
}

/**
 * Render this fill in a painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // If radius is less than 1, do default drawing and return
    if(getRadius()<1) { super.paint(aPntr, aShape); return; }
    
    // Get effect image for shape
    BufferedImage effectImage = getCachedImage(aShape);
    
    // Draw image at offset (blur effect draws image as a complete replacement for shape drawing)
    aPntr.drawImage(effectImage, -_radius*2, -_radius*2, effectImage.getWidth(), effectImage.getHeight());
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, super and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMBlurEffect other = (RMBlurEffect)anObj;
    
    // Check radius
    if(other._radius!=_radius) return false;
    return true; // Return true since all checks passed
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver); e.add("type", "blur");
    e.add("radius", _radius);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);
    _radius = anElement.getAttributeIntValue("radius");
    return this;
}

}