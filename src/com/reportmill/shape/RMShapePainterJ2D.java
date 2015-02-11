package com.reportmill.shape;
import com.reportmill.base.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * This class manages the painting of shapes to a Java2D graphics object.
 */
public class RMShapePainterJ2D implements RMShapePainter {

    // The painting bounds
    Rectangle2D            _bounds;
    
    // The component horizontal alignment
    float                  _alignmentX = .5f;
    
    // The component vertical alignment
    float                  _alignmentY = .5f;
    
    // The default scale
    float                  _scale = 1;
    
    // Whether painter should scale painting of shape up to fit bounds
    boolean                _growToFit;
    
    // Whether shape should scale painting of shape down to fit bounds
    boolean                _shrinkToFit;
    
    // The opacity
    double                 _opacity = 1;
    
    // Whether shape painting is really printing
    boolean                _printing;

    // The graphics
    Graphics2D             _g2;

/**
 * Creates a new RMShapePainterJ2D.
 */
public RMShapePainterJ2D(Graphics2D aGr)  { _g2 = aGr; }

/**
 * Returns the painting bounds (can be null).
 */
public Rectangle2D getBounds()  { return _bounds; }

/**
 * Sets the painting bounds (can be null).
 */
public void setBounds(Rectangle2D aRect)  { _bounds = aRect; }

/**
 * Sets the painting bounds.
 */
public void setBounds(double x, double y, double w, double h)  { setBounds(new Rectangle2D.Double(x, y, w, h)); }

/**
 * Returns the horizontal alignment.
 */
public float getAlignmentX()  { return _alignmentX; }

/**
 * Sets the horizontal alignment.
 */
public void setAlignmentX(float aValue)  { _alignmentX = aValue; }

/**
 * Returns the vertical alignment.
 */
public float getAlignmentY()  { return _alignmentY; }

/**
 * Sets the vertical alignment.
 */
public void setAlignmentY(float aValue)  { _alignmentY = aValue; }

/**
 * Returns the default scale.
 */
public float getScale()  { return _scale; }

/**
 * Sets the default scale.
 */
public void setScale(float aValue)  { _scale = aValue; }

/**
 * Returns whether painter should scale painting of shape up to fit bounds
 */
public boolean isGrowToFit()  { return _growToFit; }

/**
 * Returns whether painter should scale painting of shape up to fit bounds
 */
public void setGrowToFit(boolean aValue)  { _growToFit = aValue; }

/**
 * Returns whether painter should scale painting of shape down to fit bounds
 */
public boolean isShrinkToFit()  { return _shrinkToFit; }

/**
 * Returns whether painter should scale painting of shape down to fit bounds
 */
public void setShrinkToFit(boolean aValue)  { _shrinkToFit = aValue; }

/**
 * Returns whether shape painting is really printing.
 */
public boolean isPrinting()  { return _printing; }

/**
 * Sets whether shape painting is really printing.
 */
public void setPrinting(boolean aValue)  { _printing = aValue; }

/**
 * Paints a simple shape.
 */
public void paintShape(RMShape aShape)
{
    // Validate shape
    if(aShape instanceof RMParentShape) ((RMParentShape)aShape).layout();
    
    // Get shape marked bounds
    RMRect shapeBounds = aShape.getBoundsMarked();
    double shapeWidth = shapeBounds.getWidth();
    double shapeHeight = shapeBounds.getHeight();
    
    // Copy graphics
    Graphics2D graphics = _g2; _g2 = (Graphics2D)_g2.create();
    
    // Get shape bounds
    Rectangle2D bounds = getBounds();
    
    // Get default scale
    double scale = getScale();
    
    // If bounds are present, see if scale needs to be adjusted and get translation to bounds
    if(bounds!=null) {
        
        // If size to fit always or as needed (and it's needed), reset scale so that shape exactly fits in inset bounds
        if(isGrowToFit() ||
            (isShrinkToFit() && (shapeWidth*scale>bounds.getWidth() || shapeHeight*scale>bounds.getHeight())))
            scale = Math.min(bounds.getWidth()/shapeWidth, bounds.getHeight()/shapeHeight);        

        // Get the discrepancy of bounds size and shape scaled size
        double dw = bounds.getWidth() - shapeWidth*scale;
        double dh = bounds.getHeight() - shapeHeight*scale;
        
        // Constrain alignment to bounds (maybe this should be an option)
        if(dw<0) dw = 0; if(dh<0) dh = 0;
        
        // Get the translations to bounds with specified alignments (don't allow alignment outside)
        double tx = bounds.getX() + dw*getAlignmentX();
        double ty = bounds.getY() + dh*getAlignmentY();
        tx = Math.round(tx - .01); ty = Math.round(ty - .01); // Round down?
        translate(tx, ty);
    }
    
    // Do scale
    if(scale!=1)
        scale(scale, scale);
    
    // Apply inverse shape transform to negate effects of shape paint applying transform
    transform(aShape.getTransformInverse().awt());

    // Paint shape
    aShape.paint(this);
    
    // Dispose graphics
    _g2.dispose(); _g2 = graphics;
}

/**
 * Paints a child shape.
 */
public void sendPaintShape(RMShape aShape)  { aShape.paintShape(this); }

/**
 * Returns whether painting is for editor.
 */
public boolean isEditing()  { return false; }

/**
 * Returns whether given shape is selected.
 */
public boolean isSelected(RMShape aShape)  { return false; }

/**
 * Returns whether given shape is super selected.
 */
public boolean isSuperSelected(RMShape aShape)  { return false; }

/**
 * Returns whether given shape is THE super selected shape.
 */
public boolean isSuperSelectedShape(RMShape aShape)  { return false; }

/**
 * 
 */
public Color getColor()  { return _g2.getColor(); }

/**
 * 
 */
public void setColor(Color c)  { _g2.setColor(c); }

/**
 * 
 */
public Font getFont()  { return _g2.getFont(); }

/**
 * 
 */
public void setFont(Font font)  { _g2.setFont(font); }

/**
 * 
 */
public Paint getPaint()  { return _g2.getPaint(); }

/**
 * 
 */
public void setPaint(Paint paint)  { _g2.setPaint(paint); }

/**
 * 
 */
public Stroke getStroke()  { return _g2.getStroke(); }

/**
 * 
 */
public void setStroke(Stroke s)  { _g2.setStroke(s); }

/**
 * Returns the opacity.
 */
public double getOpacity()  { return _opacity; }

/**
 * Sets the opacity.
 */
public void setOpacity(double aValue)
{
    if(aValue==_opacity) return;
    _g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)aValue));
}

/**
 * 
 */
public void draw(Shape s)  { _g2.draw(s); }

/**
 * 
 */
public void fill(Shape s)  { _g2.fill(s); }

/**
 * 
 */
public void drawLine(double x1, double y1, double x2, double y2)  { _g2.drawLine(rnd(x1), rnd(y1), rnd(x2), rnd(y2)); }

/**
 * 
 */
public void fillRect(double x, double y, double w, double h)  { _g2.fillRect(rnd(x), rnd(y), rnd(w), rnd(h)); }

/**
 * 
 */
public void drawRect(double x, double y, double w, double h)  { _g2.drawRect(rnd(x), rnd(y), rnd(w), rnd(h)); }

/**
 * 
 */
public void fill3DRect(double x, double y, double w, double h, boolean raised)
{
    _g2.fill3DRect(rnd(x), rnd(y), rnd(w), rnd(h), raised);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(RMRect aRect, boolean isPressed)
{
    drawButton(aRect.x, aRect.y, aRect.width, aRect.height, isPressed);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(double x, double y, double w, double h, boolean isPressed)
{
    setColor(Color.black); fillRect(x, y, w, h);
    setColor(_white); fillRect(x, y, --w, --h);
    setColor(_darkGray); fillRect(++x, ++y, --w, --h);
    setColor(_lightGray); fillRect(x, y, --w, --h);
    setColor(isPressed? _darkerGray : _gray); fillRect(++x, ++y, --w, --h);
}

// DrawButton colors
static Color _white = new Color(.9f, .95f, 1), _lightGray = new Color(.9f, .9f, .9f);
static Color _darkGray = new Color(.58f, .58f, .58f), _darkerGray = new Color(.5f, .5f, .5f);
static Color _gray = new Color(.7f, .7f, .7f);

/**
 * 
 */
public boolean drawImage(Image img, AffineTransform xform) { return _g2.drawImage(img, xform, null); }

/**
 * 
 */
public boolean drawImage(Image img, int x, int y, int w, int h)
{
    return _g2.drawImage(img, x, y, w, h, null);
}

/**
 * 
 */
public void drawString(String str, double x, double y)  { _g2.drawString(str, (float)x, (float)y); }

/**
 * 
 */
public void drawGlyphVector(GlyphVector g, float x, float y)  { _g2.drawGlyphVector(g,x,y); }

/**
 * 
 */
public void translate(double tx, double ty)  { _g2.translate(tx, ty); }

/**
 * 
 */
public void rotate(double theta)  { _g2.rotate(theta); }

/**
 * 
 */
public void scale(double sx, double sy)  { _g2.scale(sx,sy); }

/**
 * 
 */
public void transform(AffineTransform Tx)  { _g2.transform(Tx); }

/**
 * Returns the string bounds for current font.
 */
public Rectangle2D getStringBounds(String aString)
{
    return getFont().getStringBounds(aString, _g2.getFontRenderContext());
}

/**
 * Returns the ascender for the current font.
 */
public double getFontAscent()  { return _g2.getFontMetrics().getAscent(); }

/**
 * 
 */
public Rectangle getClipBounds()  { return _g2.getClipBounds(); }

/**
 * 
 */
public Shape getClip()  { return _g2.getClip(); }

/**
 * 
 */
public void setClip(Shape clip)  { _g2.setClip(clip); }

/**
 * 
 */
public void clip(Shape s)  { _g2.clip(s); }

/**
 * 
 */
public void dispose()  { _g2.dispose(); _g2 = null; }

/**
 * Sets whether antialiasing.
 */
public boolean setAntialiasing(boolean aValue)  { return RMAWTUtils.setAntialiasing(_g2, aValue); }

/**
 * Returns the graphics.
 */
public Graphics2D getGraphics()  { return _g2; }

/**
 * Standard clone implementation.
 */
public RMShapePainter clone()
{
    RMShapePainterJ2D clone = null; try { clone = (RMShapePainterJ2D)super.clone(); } catch(Exception e) { }
    clone._g2 = (Graphics2D)_g2.create();
    return clone;
}

/**
 * Standard toString implementation.
 */
public String toString() { return getClass().getName() + "[font=" + getFont() + ",color=" + getColor() + "]"; }

/** Round. */
int rnd(double aValue)  { return (int)Math.round(aValue); }

}