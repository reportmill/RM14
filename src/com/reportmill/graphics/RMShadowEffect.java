package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.image.*;
import snap.util.*;

/**
 * This class adds a shadow to a given shape.
 */
public class RMShadowEffect extends RMImageEffect {
    
    // The shadow radius
    int             _radius = 10;
    
    // The shadow offset
    int             _dx = 5, _dy = 5;
    
    // Fill color
    RMColor         _color = RMColor.black;

/**
 * Creates a new shadow effect.
 */
public RMShadowEffect()  { setColor(new RMColor(0, 0, 0, .65f)); }

/**
 * Creates a new shadow effect with the parameters.
 */
public RMShadowEffect(RMColor aColor, int aRadius, int dx, int dy)
{
    setColor(aColor); _radius = aRadius; _dx = dx; _dy = dy;
}

/**
 * Returns the radius of the blur.
 */
public int getRadius()  { return _radius; }

/**
 * Returns the X offset of the shadow.
 */
public int getDX()  { return _dx; }

/**
 * Returns the Y offset of the shadow.
 */
public int getDY()  { return _dy; }

/**
 * Returns the color associated with this fill.
 */
public RMColor getColor()  { return _color; }

/**
 * Sets the color associated with this fill.
 */
public void setColor(RMColor aColor)
{
    if(RMUtils.equals(aColor, _color)) return; // If value already set, just return
    firePropertyChange("Color", _color, _color = aColor, -1); // Set value and fire property change
}

/**
 * Returns a shadow effect just like this one, but with a radius equal to the given value.
 */
public RMShadowEffect deriveFill(int aRadius)
{
    RMShadowEffect clone = (RMShadowEffect)clone();
    clone._radius = aRadius; return clone;
}

/**
 * Returns a shadow effect just like this one, but with a radius equal to the given value.
 */
public RMShadowEffect deriveFill(RMColor aColor)
{
    RMShadowEffect clone = (RMShadowEffect)clone();
    clone.setColor(aColor); return clone;
}

/**
 * Returns a shadow effect just like this one, but with new offsets.
 */
public RMShadowEffect deriveFill(int dx, int dy)
{
    RMShadowEffect clone = (RMShadowEffect)clone();
    clone._dx = dx; clone._dy = dy; return clone;
}

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)
{
    // If disabled, return pathBounds
    if(!isEnabled()) return super.getBounds(aShape);
    
    // Get bounds for shape with effect turned off
    setEnabled(false);
    RMRect bounds = aShape.getBoundsMarkedDeep();
    setEnabled(true);
    
    // Offset and extend bounds to account for blur radius and shadow offset
    bounds.x = bounds.x - getRadius()/2f + _dx;
    bounds.y = bounds.y - getRadius()/2f + _dy;
    bounds.width += getRadius();
    bounds.height += getRadius();
    
    // Return shadow rect
    return bounds;
}

/**
 * Returns the effect image.
 */
public BufferedImage getImage(RMShape aShape)
{
    // Create new image for shape
    BufferedImage shapeImage = getShapeImage(aShape, getRadius()*2, false);
    
    // Set composite to change mask colors to shadow color (black) and multiply mask alpha by shadow alpha (.65f)
    Graphics2D g = shapeImage.createGraphics();
    g.setComposite(AlphaComposite.SrcIn);
    g.setColor(getColor().awt());
    g.fillRect(0, 0, shapeImage.getWidth(), shapeImage.getHeight());
    g.dispose();
    
    // Make shape image pre-multiplied
    shapeImage.coerceData(true);
   
    // Create new image for effect
    BufferedImage effectImage = getBlurredImage(shapeImage, getRadius(), getRadius());
    
    // Return effect image
    return effectImage;
}

/**
 * Render this fill in a painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // If we need to recache during mouse loop, just return without painting (takes too long to regen interactively)
    //if(shadowFill.getNeedsRecache() && com.ribs.Ribs.isMouseDown()) return;
    
    // Get effect image for shape
    BufferedImage effectImage = getCachedImage(aShape);
    
    // Draw image at offset
    aPntr.drawImage(effectImage, -getRadius()*2 + _dx, -getRadius()*2 + _dy,
        effectImage.getWidth(), effectImage.getHeight());
    
    // Do normal effect paint (just does paintShapeAll)
    super.paint(aPntr, aShape);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, super and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMShadowEffect other = (RMShadowEffect)anObj;
    
    // Check Radius, DX & DY
    if(other._radius!=_radius) return false;
    if(other._dx!=_dx) return false;
    if(other._dy!=_dy) return false;
    return true;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic effect attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "shadow");

    // Archive Radius, DX & DY
    e.add("radius", _radius);
    e.add("dx", _dx);
    e.add("dy", _dy);
    if(!getColor().equals(RMColor.black)) e.add("color", "#" + getColor().toHexString());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic effect attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive Radius, DX & DY
    _radius = anElement.getAttributeIntValue("radius");
    _dx = anElement.getAttributeIntValue("dx");
    _dy = anElement.getAttributeIntValue("dy");
    String color = anElement.getAttributeValue("color");
    if(color!=null) _color = new RMColor(color);
    
    // Return this effect
    return this;
}

}