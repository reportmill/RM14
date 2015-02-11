package com.reportmill.pdf.writer;
import com.reportmill.base.RMRect;
import com.reportmill.graphics.*;
import com.reportmill.shape.RMShape;
import java.awt.image.BufferedImage;

/**
 * A PDF helper/writer class for RMEffect.
 */
public class RMEffectPdfr {

/**
 * Writes an effect.
 */
public static void writeShapeEffect(RMShape aShape, RMEffect anEffect, RMPDFWriter aWriter)
{
    if(anEffect instanceof RMBlurEffect) writeBlurEffect(aShape, (RMBlurEffect)anEffect, aWriter);
    else if(anEffect instanceof RMShadowEffect) writeShadowEffect(aShape, (RMShadowEffect)anEffect, aWriter);
    else if(anEffect instanceof RMReflectionEffect) writeRefectionEffect(aShape, (RMReflectionEffect)anEffect, aWriter);
    else if(anEffect instanceof RMEmbossEffect) writeEmbossEffect(aShape, (RMEmbossEffect)anEffect, aWriter);
}
    
/**
 * Writes an effect.
 */
public static void writeEffect(RMShape aShape, RMEffect anEffect, RMPDFWriter aWriter)
{
    RMShapePdfr.getPdfr(aShape).writeShapeAll(aShape, aWriter);
}

/**
 * Writes pdf for given blur effect and shape.
 */
public static void writeBlurEffect(RMShape aShape, RMBlurEffect aBlurEffect, RMPDFWriter aWriter)
{
    // If radius is less than 1, do default drawing and return
    if(aBlurEffect.getRadius()<1) {
        writeEffect(aShape, aBlurEffect, aWriter); return; }
    
    // Get effect image and image fill
    BufferedImage effectImage = aBlurEffect.getCachedImage(aShape);
    RMImageFill ifill = new RMImageFill(effectImage); ifill.setTiled(false);
    
    // Get bounds for image fill
    RMRect bounds = new RMRect(-aBlurEffect.getRadius()*2, -aBlurEffect.getRadius()*2,
        effectImage.getWidth(), effectImage.getHeight());
    
    // Write pdf for image fill
    RMImageFillPdfr.writeImageFill(ifill, null, bounds, aWriter);
}

/**
 * Writes pdf for given shadow effect and shape.
 */
public static void writeShadowEffect(RMShape aShape, RMShadowEffect aShadow, RMPDFWriter aWriter)
{
    // Get effect image and image fill
    BufferedImage effectImage = aShadow.getCachedImage(aShape);
    RMImageFill ifill = new RMImageFill(effectImage); ifill.setTiled(false);
    
    // Get bounds for image fill
    RMRect bounds = new RMRect(-aShadow.getRadius()*2 + aShadow.getDX(), -aShadow.getRadius()*2 + aShadow.getDY(),
        effectImage.getWidth(), effectImage.getHeight());
    
    // Write pdf for image fill
    RMImageFillPdfr.writeImageFill(ifill, null, bounds, aWriter);
    
    // Do normal pdf write
    writeEffect(aShape, aShadow, aWriter);
}
    
/**
 * Writes pdf for given reflection effect and shape.
 */
public static void writeRefectionEffect(RMShape aShape, RMReflectionEffect aReflection, RMPDFWriter aWriter)
{
    // If valid reflection and fade heights, do reflection
    if(aReflection.getReflectionHeight()>0 && aReflection.getFadeHeight()>0) {
    
        // Get reflection image for shape
        BufferedImage reflectImage = aReflection.getCachedImage(aShape);
    
        // Get shape bounds marked
        aReflection.setEnabled(false);
        RMRect bounds = aShape.getBoundsMarked();
        aReflection.setEnabled(true);
    
        // Create new image fill for image
        RMImageFill ifill = new RMImageFill(reflectImage); ifill.setTiled(false);
        
        // Get bounds of reflected image
        bounds = new RMRect(bounds.getX(), bounds.getMaxY() + aReflection.getGapHeight(),
            reflectImage.getWidth(), reflectImage.getHeight());
        
        // Write pdf for image fill
        RMImageFillPdfr.writeImageFill(ifill, null, bounds, aWriter);
    }
    
    // Do normal write pdf
    writeEffect(aShape, aReflection, aWriter);
}
    
/**
 * Writes pdf for given emboss effect and shape.
 */
public static void writeEmbossEffect(RMShape aShape, RMEmbossEffect anEmbossEffect, RMPDFWriter aWriter)
{
    // Get effect image and image fill
    BufferedImage effectImage = anEmbossEffect.getCachedImage(aShape);
    RMImageFill ifill = new RMImageFill(effectImage); ifill.setTiled(false);
    
    // Get bounds for image fill
    RMRect bounds = new RMRect(0, 0, effectImage.getWidth(), effectImage.getHeight());
    
    // Write pdf for image fill
    RMImageFillPdfr.writeImageFill(ifill, null, bounds, aWriter);
}
    
}