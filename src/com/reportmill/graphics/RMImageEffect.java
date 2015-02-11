package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.image.*;

/**
 * A effect based on image filtering.
 */
public abstract class RMImageEffect extends RMEffect {

    // A weak hashmap to hold image caches for shapes
    BufferedImage         _image;

/**
 * Returns the effect image.
 */
public abstract BufferedImage getImage(RMShape aShape);
    
/**
 * Returns image of given shape inside a gutter of given inset (maybe should be insets one day).
 */
public BufferedImage getShapeImage(RMShape aShape, int anInset, boolean premultiply)
{
    // Get shape bounds marked
    setEnabled(false);
    RMRect bounds = aShape.getBoundsMarked();
    setEnabled(true);
    
    // Get shape image width and height
    int width = (int)(bounds.getWidth() + anInset*2);
    int height = (int)(bounds.getHeight() + anInset*2);
    
    // Create new image for effect image
    BufferedImage shapeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
    // Get graphics for effect image
    Graphics2D graphics = shapeImage.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    graphics.clipRect(0, 0, width, height);
    
    // Paint shape in image (centered) and dispose graphics
    graphics.translate(anInset, anInset);
    aShape.paintShapeAll(new RMShapePainterJ2D(graphics));
    graphics.dispose();
    
    // Make sure image has right pre-multiply
    shapeImage.coerceData(premultiply);
    
    // Return shape image
    return shapeImage;
}

/**
 * Returns the blur image (can be set pre-inset image for efficiency of ConvolveOp).
 */
public BufferedImage getBlurredImage(BufferedImage anImage, int imageInset, int aRadius)
{
    // If image isn't inset by same amount as radius, create copy so ConvolveOp can have same size source & filtered image
    if(imageInset!=aRadius)
        System.err.println("Need to implement image-resize for convolve");
    
     // Default blur kernel is the gaussian kernel
    Kernel kernel = new GaussianKernel(aRadius);
    return getBlurredImage(anImage, imageInset, kernel);
}

/**
 * Returns the image blurred with the particular Kernel.
 */
public BufferedImage getBlurredImage(BufferedImage anImage, int imageInset, Kernel kernel)
{
    // Get source image
    BufferedImage image = anImage;
    
    // If image isn't inset by same amount as radius, create copy so ConvolveOp can have same size source & filtered image
    if(!image.isAlphaPremultiplied())
        System.err.println("Need to implement premultiply for convolve");

    // Create colvolve op for kernel
    ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null); //ConvolveOp.EDGE_ZERO_FILL, null);
    
    // Create new image for effect
    BufferedImage blurImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    
    // Make blur image pre-multiplied
    blurImage.coerceData(true);
    
    // Apply gaussian blur convolv op from shape image to effect image
    cop.filter(image, blurImage);
    
    // Convert blur image to non-premultiplied
    blurImage.coerceData(false);
    
    // Return blur image
    return blurImage;
}

/**
 * Returns the effect image from cache (calls getImage to load image into cache).
 */
public BufferedImage getCachedImage(RMShape aShape)  { return _image!=null? _image : (_image=getImage(aShape)); }

/**
 * Override to reset image.
 */
public void reset()  { _image = null; }

/**
 * Standard clone implementation.
 */
public RMImageEffect clone()  { RMImageEffect clone = (RMImageEffect)super.clone(); clone._image = null; return clone; }
  
/**
 * Extends Kernel with constructor which takes Kernel size and automatically generates a gaussian matrix.
 */
public static class GaussianKernel extends Kernel {
    
    /** Creates Kernel with number of pixels, on each size, which are taken into account by the Kernel. */
    public GaussianKernel(int radius)  { super(2*radius+1, 2*radius+1, getGaussianKernel(radius)); }

    /** Returns kernel for a Gaussian blur convolve. */
    public static float[] getGaussianKernel(int radius)
    {
        // Calculate width/height of convolve matrix
        int w = radius*2 + 1;

        // Create new kernel float array
        float kernel[] = new float[w*w];
        
        // Declare variable for sum
        double sum = 0.;
        
        double deviation = radius/3.; // This guarantees non zero values in the kernel
        double devSqr2 = 2*Math.pow(deviation, 2);
        double piDevSqr2 = Math.PI*devSqr2;

        for(int i=0; i<w; i++) {
            for(int j=0; j<w; j++) {
                kernel[i*w+j] = (float)(Math.pow(Math.E, -((j-radius)*(j-radius) +
                    (i-radius)*(i-radius))/devSqr2)/piDevSqr2);
                sum += kernel[i*w+j];               
            }
        }

        // Make elements sum to 1
        for(int i=0; i<w; i++)
            for(int j=0; j<w; j++)
                kernel[i*w+j] /= sum;

        return(kernel);
    } 
}

/**
 * A kernel class for cone effects.
 */
public static class ConeKernel extends Kernel {

    /** Creates Kernel with number of pixels, on each size, which are taken into account by the Kernel.  */
    public ConeKernel(int radius)  { super(2*radius+1, 2*radius+1, getConeKernel(radius)); }
    
    /** Returns kernel for a cone blur convolve. */
    public static float[] getConeKernel(int radius)
    {
        // Calculate width/height of convolve matrix
        int w = radius*2 + 1;
        float kernel[] = new float[w*w];
        double sum = 0;
        
        // This offset makes the corners exactly zero probably should bump it a hair so corners contribute something
        double offset = radius*Math.sqrt(2)+.01;
    
        for(int i=0; i<w; i++) {
            for(int j=0; j<w; j++) {
                kernel[i*w+j] = (float)(offset-Math.sqrt((j-radius)*(j-radius) +
                    (i-radius)*(i-radius)));
                sum += kernel[i*w+j];               
            }
        }
    
        // Make elements sum to 1
        for(int i=0; i<w; i++)
            for(int j=0; j<w; j++)
                kernel[i*w+j] /= sum;

        return(kernel);
    } 
}

/**
 * A kernel class for box filters (really?).
 */
public static class BoxKernel extends Kernel {

    /** Creates Kernel with number of pixels, on each size, which are taken into account by the Kernel. */
    public BoxKernel(int radius) { super(2*radius+1, 2*radius+1, getBoxKernel(radius)); }
    
    /** Returns kernel for a boxy blur convolve. */
    public static float[] getBoxKernel(int radius)
    {
        int w = radius*2 + 1, wsquared = w*w;
        float avg = 1f/wsquared;
        float kernel[] = new float[wsquared]; for(int i=0; i<wsquared; i++) kernel[i] = avg;
        return kernel;
    }
}

}