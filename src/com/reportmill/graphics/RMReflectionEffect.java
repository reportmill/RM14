package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import snap.util.*;

/**
 * This class adds a reflection to a given shape.
 */
public class RMReflectionEffect extends RMImageEffect {

    // The height of the reflected image as fraction of shape height (defaults to 1)
    float           _reflectionHeight = 1;

    // The height of the faded region as a fraction of reflection height (defaults to .5)
    float           _fadeHeight = .5f;
    
    // The height of the gap between the shape and the reflection in points (defaults to 0)
    float           _gapHeight = 0;
    
/**
 * Returns the height of the reflected image as fraction of shape height (defaults to 1).
 */
public float getReflectionHeight()  { return _reflectionHeight; }

/**
 * Sets the height of the reflected image as fraction of shape height.
 */
protected void setReflectionHeight(float aValue)  { _reflectionHeight = aValue; }

/**
 * Returns the height of the faded region as a fraction of reflection height (defaults to .5).
 */
public float getFadeHeight()  { return _fadeHeight; }

/**
 * Sets the height of the faded region as a fraction of reflection height.
 */
protected void setFadeHeight(float aValue)  { _fadeHeight = aValue; }

/**
 * Returns the height of the gap between the shape and the reflection in points (defaults to 0).
 */
public float getGapHeight()  { return _gapHeight; }

/**
 * Sets the height of the gap between the shape and the reflection in points.
 */
protected void setGapHeight(float aValue)  { _gapHeight = aValue; }

/**
 * Creates a new reflection effect by cloning this one and substituting given reflection, fade and gap heights,
 * if greater than zero.
 */
public RMReflectionEffect deriveEffect(float aReflectionHeight, float aFadeHeight, float aGapHeight)
{
    // Clone this effect
    RMReflectionEffect clone = (RMReflectionEffect)clone();
    
    // If valid reflection height, set it
    if(aReflectionHeight>=0)
        clone.setReflectionHeight(aReflectionHeight);
    
    // If valid fade height, set it
    if(aFadeHeight>=0)
        clone.setFadeHeight(aFadeHeight);
    
    // If valid gap height, set it
    if(aGapHeight>=0)
        clone.setGapHeight(aGapHeight);
    
    // Return clone
    return clone;
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
    RMRect bounds = aShape.getBoundsMarked();
    setEnabled(true);
    
    // Extend height by gap and reflection fade height
    bounds.height = bounds.height + getGapHeight() + bounds.height*getReflectionHeight()*getFadeHeight();
    
    // Return bounds rect
    return bounds;
}

/**
 * Returns the effect image.
 */
public BufferedImage getImage(RMShape aShape)
{
    // Get shape bounds marked
    setEnabled(false);
    RMRect boundsM = aShape.getBoundsMarked();
    setEnabled(true);
    
    // Get shape image width and height
    int width = (int)Math.ceil(boundsM.getWidth());
    int height = (int)Math.ceil(boundsM.getHeight()*getReflectionHeight()*getFadeHeight());
    
    // Create new image for shape
    BufferedImage effectImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
    // Get graphics for effect image
    Graphics2D graphics = effectImage.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    graphics.clipRect(0, 0, width, height);
    
    // Paint shape in image (upside down)
    graphics.scale(1,-getReflectionHeight());
    graphics.translate(-boundsM.getX(), -boundsM.getMaxY());
    aShape.paintShapeAll(new RMShapePainterJ2D(graphics));
    graphics.translate(boundsM.getX(), boundsM.getMaxY());
    graphics.scale(1,-1/getReflectionHeight());
    
    // Create gradient paint to fade image out
    GradientPaint mask = new GradientPaint(0, 0, new Color(1f, 1f, 1f, .5f), 0, height, new Color(1f, 1f, 1f, 0f));
    
    // Set composite to change mask colors to gradient
    graphics.setComposite(AlphaComposite.DstIn);
    graphics.setPaint(mask);
    graphics.fillRect(0, 0, width, height);
   
    // Return shape image
    return effectImage;
}

/**
 * Render this fill in a Java2D Graphics2D.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // If valid reflection and fade heights, do reflection
    if(getReflectionHeight()>0 && getFadeHeight()>0) {
    
        // Get reflection image for shape
        BufferedImage reflectImage = getCachedImage(aShape);
    
        // Get shape bounds marked
        setEnabled(false);
        RMRect bounds = aShape.getBoundsMarked();
        setEnabled(true);
    
        // Draw image at offset
        AffineTransform trans = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getMaxY() + getGapHeight());
        aPntr.drawImage(reflectImage, trans);
    }
    
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
    RMReflectionEffect other = (RMReflectionEffect)anObj;
    
    // Check ReflectionHeight, FadeHeight, GapHeight
    if(other._reflectionHeight!=_reflectionHeight) return false;
    if(other._fadeHeight!=_fadeHeight) return false;
    if(other._gapHeight!=_gapHeight) return false;
    return true;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic effect attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "reflection");

    // Archive ReflectionHeight, FadeHeight, GapHeight
    if(getReflectionHeight()!=.5) e.add("reflection-height", getReflectionHeight());
    if(getFadeHeight()!=1) e.add("fade-height", getFadeHeight());
    if(getGapHeight()!=0) e.add("gap-height", getGapHeight());
    
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
    
    // Unarchive ReflectionHeight, FadeHeight, GapHeight
    if(anElement.hasAttribute("reflection-height"))
        setReflectionHeight(anElement.getAttributeFloatValue("reflection-height"));
    if(anElement.hasAttribute("fade-height")) setFadeHeight(anElement.getAttributeFloatValue("fade-height"));
    if(anElement.hasAttribute("gap-height")) setGapHeight(anElement.getAttributeFloatValue("gap-height"));
    
    // Return this effect
    return this;
}

}