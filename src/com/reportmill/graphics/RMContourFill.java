package com.reportmill.graphics;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import snap.util.*;

/**
 * RMFill implementation of a shape-burst gradient.
 * Implements Paint & PaintContext methods for scan converting the gradient into an ARGB raster.
 * Can be used on its own for a vaguely-interesting fill, or to create a bump map for the emboss effect.
 * Currently the edge profile is a 45 degree straight line, but other edge profiles (rounded, cove, whatever)
 * can easily be added.
 */
public class RMContourFill extends RMFill implements PaintContext, Paint  {

    // The original shape path
    RMPath          _path;
    
    // The path scaled to device coords
    RMPath          _scaledPath;
    
    // cached segment list
    RMLine          _segments[];
    
    // Raster to hold the bits of the filled path
    WritableRaster  _maskRaster;
    
    // percentage (in the range [0-1]) of the gradient to show
    float           _maxheight;

/**
 * Creates a contour fill.
 */
public RMContourFill()  { this(1); }

/**
 * Creates a contour fill with given height.
 */
public RMContourFill(float height)  { _maxheight = height; }

/**
 * Paint & PaintContext interface methods.
 */
public Paint getPaint()  { return this; }

/** PaintContext method. */
public int getTransparency()  { return TRANSLUCENT; }

/** PaintContext method. */
public ColorModel getColorModel()  { return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000); }

/** PaintContext method. */
public void dispose()  { }

/**
 * Paint & PaintContext interfaces.
 */
public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
        AffineTransform xform, RenderingHints hints)
{
    // transform the path into device space
    int w = deviceBounds.width;
    int h = deviceBounds.height;
    
    // The RMPathIterator now skips stray moveTos, so they aren't included in userBounds
    _scaledPath = _path.getPathInRect(userBounds);
    
    // translate so origin is at 0,0
    xform.preConcatenate(AffineTransform.getTranslateInstance(-deviceBounds.x, -deviceBounds.y));
    _scaledPath = _scaledPath.createTransformedPath(xform);
    
    // paint the path into an argb image to use as a mask
    BufferedImage img = getMaskImage(w,h);
    
    // create a raster view of the mask that matches the device bounds
    _maskRaster = img.getRaster().createWritableTranslatedChild(deviceBounds.x, deviceBounds.y);
    
    // The path-point distance calculations just flatten path, so do it once here instead of for every pixel
    _scaledPath = RMPathUtils.getPathFlattened(_scaledPath);
    
    // Get scaled path segments array
    _segments = _scaledPath.getSegments().toArray(new RMLine[0]);
    
    // The contour-fill algorithm isn't conducive to tiling, since it has to run through everything twice - once to
    // calculate all the distances, and then a second time to scale the distances by the maximum.
    // Unless there's some good way to pre-compute the maximum distance that will be encountered,
    // we may as well do the whole thing once and then parcel out the tiles as they're asked for.
    doShading(((DataBufferInt)_maskRaster.getDataBuffer()).getData(), 0, 0, 0, w, h, w);
    
    // Return this paint context
    return this;
}

/**
 * Creates a new raster.
 */
public Raster getRaster(int x, int y, int w, int h)
{
    // Return empty rasters for bogus dimensions
    if(w<=0 || h<=0)
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getRaster();
    
    // Return a child raster for this tile
    int rw = Math.min(w, _maskRaster.getWidth());
    int rh = Math.min(h, _maskRaster.getHeight());
    return _maskRaster.createChild(x, y, rw, rh, 0, 0, null);
}

/**
 * Replaces image samples with distance from pixel scaled to the range [0-1]. We scale every distance by the maximum.
 * This means that the color of each edge will change with the same slope, and thicker sections will therefore be
 * taller (if edge color is thought of as height).
 * Another possibility would be to scale each edge by its local max, so each section ultimately reaches same height.
 */
public void doShading(int samples[], int offset, int devicex, int devicey, int w, int h, int rowbytes)
{
    // If no segments, just return
    if(_segments==null || _segments.length==0) return;
    
    // Declare array for each pixel/sample holding its distance to the closest segment
    float distances[] = new float[w*h];
    
    // Declare variable for max distance from any pixel/sample to path
    double maxDistance = 0;
        
    // Iterate over pixels/samples and get distances from each to path
    for(int y=devicey, j=offset-devicex, k=0, maxX=devicex+w, maxY=devicey+h; y<maxY; y++, j+=rowbytes) {
        for(int x=devicex; x<maxX; x++, k++) {
            
            // Get sample alpha - if zero, just continue (it's outside the path)
            int alpha = samples[j+x] & 0xff000000;
            if(alpha==0)
                continue;
           
            // Find the smallest distance to any path segment
            double distanceSqrd = _segments[0].getDistanceLineSquared(x, y);
            for(int i=1, iMax=_segments.length; i<iMax; i++)
                distanceSqrd = Math.min(distanceSqrd, _segments[i].getDistanceLineSquared(x, y));
                
            // Save min distance for each pixel until we know what to scale it by
            distances[k] = (float)Math.sqrt(distanceSqrd);
            if(distances[k]>maxDistance)
                maxDistance = distances[k];
        }
    }
    
    // This shouldn't happen, except maybe with a degenerate path
    if(maxDistance<=0) return;
    
    // no need to do these multiplies a million times
    int maxP = ((int)(255*_maxheight))*0x010101;
    
    // Iterate over pixels/samples and turn all the distances into pixel values by scaling by the max distance
    for(int y=0, j=offset, k=0; y<h; y++, j+=rowbytes) {
        for(int x=0; x<w; x++, k++) { int alpha = samples[j+x] & 0xff000000; if(alpha==0) continue;
        
            // At this point, normalizedDistance changes linearly from 0-1 as the edge moves from out to inside.
            // Here is where you could put normalizedDistance through some map if you wanted different edge profiles.
            double normalizedDistance = distances[k]/maxDistance;
            int p = normalizedDistance>=_maxheight? maxP : (int)(255*normalizedDistance)*0x010101;
            samples[j+x] = alpha | p; // Keep alpha value to preserve anti-aliasing
        }
    }
}

/**
 * Paint method.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    _path = aShape.getPathInBounds();
    aPntr.setPaint(this); // Set paint
    aPntr.fill(_path); // Fill path
}

/**
 * Draws the path into a buffered image to use as a mask
 */
public BufferedImage getMaskImage(int width, int height)
{
    // Create argb image
    BufferedImage shapeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
    // Get graphics for image
    Graphics2D g = shapeImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.clipRect(0, 0, width, height);
    
    // Clear background (is this the default?)
    g.setComposite(AlphaComposite.Clear);
    g.fillRect(0, 0, width, height);
    
    // Paint path in image
    g.setComposite(AlphaComposite.SrcOver);
    g.setColor(Color.black);
    
    // Fill path
    g.fill(_scaledPath);
    
    // Dispose graphics
    g.dispose();

    // Return shape image
    return shapeImage;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver); e.add("type", "contour");
    return e;
}

}