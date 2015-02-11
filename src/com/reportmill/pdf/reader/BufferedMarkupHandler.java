package com.reportmill.pdf.reader;
import java.awt.*;
import java.awt.image.*;

/**
 * BufferedMarkupHandler - a PDFMarkupHandler (and Java2DMarkupHandler) that renders PDF into an image.
 */
public class BufferedMarkupHandler extends Java2DMarkupHandler {

    // The image to hold the rendered pdf
    BufferedImage _image;
    
    // The rendering hints
    RenderingHints _hints;
    
/**
 * Creates a new BufferedMarkupHandler.
 */
public BufferedMarkupHandler()
{
    // Start out with an empty Graphics object.  We'll create it once the parser tells us how big the page is.
    super(null);
}

/**
 * Set all the rendering hints for pdf rendering.
 */
public void setRenderingHints(RenderingHints h)
{
    _hints = (h==null) ? h : (RenderingHints)h.clone();
}

/**
 * Set an individual rendering hint
 */
public void setRenderingHint(RenderingHints.Key key, Object value)
{
    // If hints isnull, create hints
    if (_hints == null)
        _hints = new RenderingHints(key, value);
    
    // Otherwise, just add
    else _hints.put(key, value);
}

/**
 * This is the first callback.
 */
public void beginPage(float width, float height)
{
    // Get integer width & height
    int w = (int)Math.ceil(width);
    int h = (int)Math.ceil(height);
    
    //TODO:  Some image formats are undoubtedly faster to draw in than others.
    //  We may want to let the deviceConfiguration get the image for us, 
    //  although that would seem to return an image that's fast to blit 
    //  to the screen, but how fast is it to draw into? <see above routine>
    _image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    // Create graphics object from image
    Graphics2D g = _image.createGraphics();
    
    // Clear the background
    g.setComposite(AlphaComposite.Clear);
    g.fillRect(0,0,w,h);
    g.setComposite(AlphaComposite.Src);

    // Work around bug that drawing to a transparent background screws with antialiasing on a mac?
    // See the description about this at the bottom.
    //g.setColor(Color.WHITE); g.fillRect(0,0,w,h);
   
    // Merge any rendering hints, if any
    if(_hints != null) {
        
        // Get default hints
        RenderingHints defaultHints = g.getRenderingHints();
        
        // If default hints is non-null, add to hints and reset
        if (defaultHints != null) {
            defaultHints.putAll(_hints);
            g.setRenderingHints(defaultHints);
        }
        
        // If no default hints, just set hints
        else g.setRenderingHints(_hints);
    }
         
    // Set image graphics object
    setGraphics(g);   
    
    // Sets the clip and establishes a pdfspace->awtspace transform
    super.beginPage(width, height);
}

/**
 * Returns pdf image.
 */
public BufferedImage getImage()
{ 
    return _image; 
}

}

//    A note about the Mac text antialiasing problems.  
//    When you draw antialiased text on a Mac into a buffer with a transparent background, 
//    the text looks ugly when composited to the screen.
//    If the text is drawn onto an opaque backrgound, it looks great.
//    
//    This is due to the Mac's use of cooltype (or whatever they call it) antialiasing.
//    The cooltype antialising works by using the individual r, g, & b samples as 
//    separate 'subpixels' to antialias.  This gives them effectively 3x the horizontal 
//    resolution while antialiasing.
//    The normal (non-cooltype) method for antialising with alpha is to draw the pixel 
//    the original color, and set the alpha to match the pixel coverage. When that image is
//    later composited, the alpha will cause the correct blending with whatever color pixel
//    the image is being composited on top of.
//    With the cooltype antialiasing, however, this same strategy can't be used.
//    That's because there's only a single alpha channel for each pixel.  The coolfont
//    antialiasing depends on the fact that the individual r, g, & b samples will
//    have different coverage.  It can't do the alpha antialiasing since setting a pixel's
//    alpha would apply to all 3 subpixels.
//    
//    This explains why they would have to turn off cooltype when drawing to alpha,
//    although it doesn't really explain why it would look so ugly.  Seems like they
//    could have used a good non-cooltype antialiasing and the text would still look decent,
//    if not super-cool.
//    
