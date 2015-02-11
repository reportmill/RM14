package com.reportmill.shape;
import com.reportmill.base.RMRect;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A class to produce an image for an RMShape.
 */
public class RMShapeImager {

    // The scale
    double         _scale = 1;
    
    // The background color
    Color          _color;
    
/**
 * Returns the background color.
 */
public Color getColor()  { return _color; }

/**
 * Sets the background color.
 */
public RMShapeImager setColor(Color aColor)  { _color = aColor; return this; }

/**
 * Returns the scale.
 */
public double getScale()  { return _scale; }

/**
 * Sets the scale.
 */
public RMShapeImager setScale(double aValue)  { _scale = aValue; return this; }

/**
 * Returns an image for the given shape, with given background color (null for clear) and scale.
 */
public BufferedImage createImage(RMShape aShape)
{
    // Get marked bounds for shape
    RMRect bounds = aShape instanceof RMPage? aShape.getBounds() : aShape.getBoundsMarkedDeep();
    
    // Calculate image size from shape bounds and scale (rounded up to integral size)
    int width = (int)Math.ceil(bounds.getWidth()*getScale());
    int height = (int)Math.ceil(bounds.getHeight()*getScale());
    
    // If shape has no area, return empty image
    if(width==0 || height==0)
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    
    // Create new image
    BufferedImage image = null;
    if(getColor()!=null && getColor().getAlpha()==255)
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    else image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
    // Get graphics and apply rendering hints
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    //rhints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    
    // Fill background
    if(getColor()!=null) {
        g2.setColor(getColor()); g2.fillRect(0, 0, width, height); }

    // Create shape painter and configure
    RMShapePainterJ2D painter = new RMShapePainterJ2D(g2);
    painter.setBounds(0, 0, width, height);
    painter.setScale((float)getScale());
    painter.setPrinting(true);
    
    // Paint shape
    painter.paintShape(aShape);
    
    // Return image
    return image;
}

}