package com.reportmill.graphics;
import com.reportmill.shape.*;
import java.awt.image.*;
import snap.util.*;

/**
 * Renders a given shape with an emboss effect.
 */
public class RMEmbossEffect extends RMImageEffect {

    // Light source position
    float     _altitude;
    
    // Light source position
    float     _azimuth;
    
    // Radius to use when bluring the bump map mask
    int       _radius;

/**
 * Creates a new emboss effect.
 */
public RMEmbossEffect()  { this(60f, 120f, 10); }

/**
 * Creates a new emboss effect with given altitude, azimuth and radius.
 */
public RMEmbossEffect(float altitude, float azimuth, int radius)
{
    _altitude = altitude; _azimuth = azimuth; _radius = Math.max(1, radius);
}

/**
 * Returns altitude of light source.
 */
public float getAltitude()  { return _altitude; }

/**
 * Returns angle of light source.
 */
public float getAzimuth()  { return _azimuth; }

/**
 * Returns radius of edge rounding.
 */
public int getRadius()  { return _radius; }

/**
 * Derive with new altitude and azimuth.
 */
public RMEmbossEffect deriveEffect(float newAltitude, float newAzimuth)
{
    RMEmbossEffect other = (RMEmbossEffect)clone();
    other._altitude = newAltitude;
    other._azimuth = newAzimuth;
    return other;
}

/**
 * Derive with new radius.
 */
public RMEmbossEffect deriveEffect(int newRadius)
{
    RMEmbossEffect other = (RMEmbossEffect)clone();
    other._radius = newRadius;
    return other;
}

/**
 * Creates effect image
 */
public BufferedImage getImage(RMShape aShape)
{
    // Get the image to be embossed
    BufferedImage embossImage = getShapeImage(aShape, 0, false);
     
    // Draw the into bump map, offset by radius. colors are irrelevant since we're only using alpha channel as bump map
    BufferedImage bumpImage = getShapeImage(aShape, Math.abs(getRadius()), true);
     
    // Make a blurred copy of the mask
    BufferedImage blur = getBlurredImage(bumpImage, Math.abs(getRadius()), Math.abs(getRadius()));

    // Do emboss
    emboss(embossImage, blur);
     
    // Return embossed image
    return embossImage;
}

/**
 * Emboss a 24 bit source image according to a bump map.
 * Bump map is assumed to to be (2*_radius x 2*_radius) pixels larger than the source
 * to compensate for edge conditions of both the blur and the emboss convolutions.
 */
public void emboss(BufferedImage source, BufferedImage bump)
{
    // Code adapted from Graphics Gems IV - Fast Embossing Effects on Raster Image Data (by John Schlag)
    WritableRaster srcRaster = source.getRaster();
    Raster bumpRaster = bump.getRaster();
    int w = source.getWidth();
    int h = source.getHeight();
    double pixelScale = 255.9;
    
    // Normalized light source vector
    double az = getAzimuth()*Math.PI/180;
    double alt = getAltitude()*Math.PI/180;
    int Lx = (int)(Math.cos(az) * Math.cos(alt) * pixelScale);
    int Ly = (int)(Math.sin(az) * Math.cos(alt) * pixelScale);
    int Lz = (int)(Math.sin(alt) * pixelScale);
    
    // Constant z component of surface normal
    int Nz = 3*255/Math.abs(getRadius());
    int Nz2 = Nz*Nz;
    int NzLz = Nz*Lz;
    int background = Lz;
    
    DataBuffer buf = srcRaster.getDataBuffer();
    
    // Assertion.  We created the BufferedImage, so this is what we expect the data to look like.
    if(buf.getDataType() != DataBuffer.TYPE_INT || buf.getNumBanks() != 1)
        throw new RuntimeException("unknown data format");
    
    int srcPixels[] = ((DataBufferInt)buf).getData();
    int bumpPixels[] = ((DataBufferInt)bumpRaster.getDataBuffer()).getData();
    int offset = 0;
    int brow = w + Math.abs(getRadius()) + Math.abs(getRadius());
    int boff = brow*Math.abs(getRadius()) + Math.abs(getRadius());
    
    // Bump map is an ARGB image - Turn it into an array of signed ints
    isolateHeightSample(bumpPixels, brow, h + Math.abs(getRadius()) + Math.abs(getRadius()));
    
    // Shade the pixels based on bump height & light source location
    for(int y=0; y<h; ++y) {
        for(int x=0; x<w; ++x) {
            
            // Normal calculation from alpha sample of bump map
            int Nx = (bumpPixels[boff-brow-1]+bumpPixels[boff-1]+bumpPixels[boff-1+brow]
                 -bumpPixels[boff-brow+1]-bumpPixels[boff+1]-bumpPixels[boff+brow+1]);
            int Ny = (bumpPixels[boff+brow-1]+bumpPixels[boff+brow]+bumpPixels[boff+brow+1]
                 -bumpPixels[boff-brow-1]-bumpPixels[boff-brow]-bumpPixels[boff-brow+1]);
            
            // If negative, negate everything
            if(getRadius()<0) { Nx = -Nx; Ny = -Ny; }
            
            // Declare variable for shade, initialized to background
            int shade = background;
            
            // If normal isn't normal, calculate shade 
            if(Nx!=0 || Ny!=0) {
                int NdotL = Nx*Lx + Ny*Ly + NzLz;
                if(NdotL<0)
                    shade = 0;
                else shade = (int)(NdotL / Math.sqrt(Nx*Nx + Ny*Ny + Nz2));
            }            
            
            // scale each sample by shade
            int p = srcPixels[offset];
            
            //if(shade < Lz) { p=0xff202020; } else {p=0xff808080;}; 
            //srcPixels[offset]= (0x010101*shade) | alpha;
            
            // Recalculate components
            int red = (((p&0xff0000)*shade)>>8) & 0xff0000;
            int green = (((p&0xff00)*shade)>>8) & 0xff00;
            int blue = (((p&0xff)*shade)>>8);
            int alpha = p & 0xff000000; //((p>>8)*shade) & 0xff000000;
            
            // Reconstruct pixel
            srcPixels[offset] = alpha|red|green|blue;
           
            // assumption is that rowbytes==width
            ++offset;
            ++boff;
        }
        
        boff += 2*Math.abs(getRadius());
    }
}

/** 
 * Converts the argb bumpSamples into an array of signed ints representing the height.
 * Height values should be in the range 0-255
 */
public void isolateHeightSample(int bumpPixels[], int w, int h)
{
    // The emboss effect uses the alpha channel as the height.
    for(int i=0, n=w*h; i<n; ++i)
        bumpPixels[i] = (bumpPixels[i]>>24)&0xff;
}

/**
 * Render this fill in a shape painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // If disabled, just return
    if(!isEnabled()) return;
    
    // Get effect image and draw effect image instead of shape
    BufferedImage effectImage = getCachedImage(aShape);
    aPntr.drawImage(effectImage, 0, 0, effectImage.getWidth(), effectImage.getHeight());
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, super and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMEmbossEffect other = (RMEmbossEffect)anObj;
    
    // Check Radius, Altitude, Azimuth
    if(other._radius!=_radius) return false; 
    if(other._altitude!=_altitude) return false;
    if(other._azimuth!=_azimuth) return false;
    return true; // Return true since all checks passed
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic effect attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "emboss");

    // Archive Radius, Altitude, Azimuth
    if(getRadius()!=10) e.add("radius", getRadius());
    if(getAzimuth()!=120) e.add("azimuth", getAzimuth());
    if(getAltitude()!=60) e.add("altitude", getAltitude());
    
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
    
    // Uanrchive Radius, Altitude, Azimuth
    _radius = anElement.getAttributeIntValue("radius", 10);
    _azimuth = anElement.getAttributeFloatValue("azimuth", 120);
    _altitude = anElement.getAttributeFloatValue("altitude", 60);
    
    // Return this effect
    return this;
}

}