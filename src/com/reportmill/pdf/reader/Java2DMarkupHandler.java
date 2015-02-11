/**
 * Java2DMarkupHandler.java 
 * Created Oct 11, 2005
 *
 * This is a concrete subclass of PDFMarkupHandler that knows how to 
 * render into a Graphics2D environment.
 * It can be used to render directly into a Component or into an 
 * offscreen buffer.
 * 
 */

package com.reportmill.pdf.reader;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;

public class Java2DMarkupHandler extends PDFMarkupHandler {
  Graphics2D destination;
  Rectangle2D destinationRect;
  AffineTransform awtspace_transform;
  AffineTransform saved_transform;
  Shape initialClip;
  
public Java2DMarkupHandler(Graphics2D g)
{
    super();
    setGraphics(g);
}

AffineTransform establishTransform(PDFGState g)
{
    AffineTransform old = destination.getTransform();
    destination.setTransform(awtspace_transform);
    if (g != null)
        destination.transform(g.trans);
    return old;
}

/** Set the Graphics2D object that will do all the rendering. */
public void setGraphics(Graphics2D g) { destination = g; }

public Graphics getGraphics() { return destination; }

/** Set the bounds in the Graphics2D's coordinate system where the page will be drawn. */
public void setDestinationRect(Rectangle2D r) { destinationRect = r; }

/** Set the bounds of the page.  This will be called before any marking operations. */
public void beginPage(float width, float height)
{
    // If no destination rect has been set, draw unscaled & untranslated
    if (destinationRect==null)
        destinationRect = new Rectangle2D.Float(0f, 0f, width, height);
    
    // Save away the initial user clip
    initialClip = destination.getClip();
    
    // Sets the clip for the destination to the page size
    destination.clip(destinationRect);
    
    // The PDF space has (0,0) at the top, awt has it at the bottom
    awtspace_transform = destination.getTransform();
    awtspace_transform.concatenate(
            new AffineTransform(destinationRect.getWidth() / width, 0, 
                                0, -destinationRect.getHeight()/height, 
                                destinationRect.getX(), destinationRect.getY()+destinationRect.getHeight()));
}

/** Restore Graphics2D to the state it was in before we started. */
public void endPage() 
{
    destination.setClip(initialClip);
}

/** reset the clip */
public void clipChanged(PDFGState g)
{
    // apply original clip, if any.
    // A null clip in the gstate resets the clip to whatever it was originally
    if ((initialClip != null) || (g.clip==null))
        destination.setClip(initialClip);
    
     if (g.clip != null) {
        // Clip is defined in page space, so apply only the page->awtspace transform
        AffineTransform old = establishTransform(null);
        
        if (initialClip == null)
            destination.setClip(g.clip);
        else  
            destination.clip(g.clip);
        destination.setTransform(old);
    }
}

/** Stroke the current path with the current miter limit, color, etc. */
public void strokePath(PDFGState g, GeneralPath p)
{
    AffineTransform old = establishTransform(g);
    
    if (g.scomposite != null)
        destination.setComposite(g.scomposite);
    destination.setColor(g.scolor);
    destination.setStroke(g.lineStroke);
    destination.draw(p);
    destination.setTransform(old);
}

/** Fill the current path using the fill params in the gstate */
public void fillPath(PDFGState g, GeneralPath p)
{
    AffineTransform old = establishTransform(g);

    if (g.composite != null)
         destination.setComposite(g.composite);
    destination.setPaint(g.color);
    destination.fill(p);
    destination.setTransform(old);
}    

/** Draw an image */
public void drawImage(PDFGState g, Image i, AffineTransform ixform) 
{
    AffineTransform old = establishTransform(g);
    if (g.composite != null)
        destination.setComposite(g.composite);
    
    // special case for huge images
    if (i instanceof PDFTiledImage.TiledImageProxy)
        drawTiledImage((PDFTiledImage.TiledImageProxy)i, ixform);
    
    // normal image case
    else try {
        destination.drawImage(i, ixform, null);
    }
    catch (java.awt.image.ImagingOpException exc) {
    	// If the image drawing throws an exception, try the workaround
        sun_bug_4723021_workaround(i,ixform);
    }
    
    // restore transform
    destination.setTransform(old);
}

public void drawTiledImage(PDFTiledImage.TiledImageProxy i, AffineTransform ixform)
{
    // save old antialiasing value and turn it off
    Object oldaa = destination.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    destination.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    
    destination.transform(ixform);

    try {
        i.drawImage(destination);
    }
    catch (OutOfMemoryError e) {
        System.gc();
        throw new PDFException("Out of memory error ",e);
    }
    
    destination.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldaa);
}

/** 
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4723021
 * 
 * Applying a transform to an image with interesting SampleModels
 * fails.  To work around this, we try creating an untransformed
 * RGBA image first and then redrawing that with the desired 
 * transform.
 */
public void sun_bug_4723021_workaround(Image i, AffineTransform ixform)
{
	int width = i.getWidth(null);
	int height = i.getHeight(null);
	// create a new image and draw the original image to it with the identity transform
	BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g = img.createGraphics();
	g.drawImage(i, new AffineTransform(), null);
	// now draw the new image as we would have drawn the original
	destination.drawImage(img, ixform, null);	
}

/** Draw some text at the current text position.  
 * The glyphVector will have been created by the parser using the current
 * font and its character encodings.
 */
public void showText(PDFGState g, GlyphVector v)
{
    AffineTransform old = establishTransform(g);
    
    /*TODO: eventually need check the font render mode in the gstate */
    if (g.composite != null)
        destination.setComposite(g.composite);
    
    destination.setPaint(g.color);
    destination.drawGlyphVector(v,0,0);
    destination.setTransform(old);
}

public FontRenderContext getFontRenderContext()
{
    return destination.getFontRenderContext();
}


}
