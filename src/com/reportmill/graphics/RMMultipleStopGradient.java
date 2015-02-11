package com.reportmill.graphics;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * An implementation of the java.awt.Paint interface for RMGradientFills.
 */
public class RMMultipleStopGradient implements PaintContext, Paint {

    // Cached rgba values of the stops
    int         _stop_colors[][];
    float       _stop_positions[];
    
    // Shading axis
    float       _x0,_y0,_x1,_y1;
    
    // Cached values for shading loop
    float       Ax, Ay, BAx, BAy, denom;
    
public RMMultipleStopGradient(float x0, float y0, float x1, float y1, RMGradientFill fill)
{
    int nstops = fill.getColorStopCount();
    
    // pull out the rgba components from the stop list
    _stop_colors = new int[nstops][4];
    _stop_positions = new float[nstops];
    for(int i=0; i<nstops; ++i) {
        RMColor c = fill.getStopColor(i);
        _stop_colors[i][0] = c.getAlphaInt();
        _stop_colors[i][1] = c.getRedInt();
        _stop_colors[i][2] = c.getGreenInt();
        _stop_colors[i][3] = c.getBlueInt();
        _stop_positions[i] = fill.getStopPosition(i);
    }
    // cache gradient axis values for axial shading
    Ax = _x0 = x0;
    Ay = _y0 = y0;
    _x1 = x1;
    _y1 = y1;
    BAx = x1-x0;
    BAy = y1-y0;
    denom = (BAx*BAx + BAy*BAy);
}


public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
        AffineTransform xform, RenderingHints hints)
{
    setDeviceTransform(xform,deviceBounds);
    return this;
}

public void dispose()  { }

public Raster getRaster(int x, int y, int w, int h)
{
    // Allocate an ARGB raster and pass the sample buffer to the shading implementation
    DataBufferInt dbuf = new DataBufferInt(w*h);
    WritableRaster r = Raster.createPackedRaster(dbuf, w, h, w,
        new int[]{0xff0000,0xff00,0xff,0xff000000}, new Point());
    int samples[] = dbuf.getData();

    doShading(samples,x,y,w,h);    
    return r;
}

/** Alpha & color definitions */
public int getTransparency()  { return TRANSLUCENT; }

/**
 * ARGB.
 */
public ColorModel getColorModel()  { return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000); }

/**
 * Shading loop. Put in some meaningful comment here some day.
 */
public void doShading(int argb_samples[], int x, int y, int w, int h)
{
    // For every point P in the raster, find point t along AB where dotProduct(A-B, P-AB(t)) = 0
    for(int j=0, sindex=0; j<h; ++j) {
        double PAy = (y+j-Ay)*BAy;
        double PAx = x-Ax;
        for(int i=0; i<w; ++i) {
            float t = (float)(BAx*PAx+PAy)/denom; // t is the distance (0-1) along the gradient axis   
            argb_samples[sindex] = getShadePixel(t);
            ++sindex;
            ++PAx;
        }
    }
}

/**
 * Returns the argb pixel value for the distance along the shading axis.
 */
public int getShadePixel(float t)
{
    int nstops = _stop_positions.length;
    int pixel = 0;

    // Pixels beyond the endpoints of gradient axis use colors at endpoints (pdf calls this behavior 'extend')
    if(t<_stop_positions[0])
        t = _stop_positions[0];
    else if(t>_stop_positions[nstops-1])
        t = _stop_positions[nstops-1];
    
    // find the right stop color
    for(int k=1; k<nstops; ++k) {
        if (_stop_positions[k] >= t) {
            // scale t to stop range
            t = (t-_stop_positions[k-1])/(_stop_positions[k]-_stop_positions[k-1]);
            float ti = 1f - t;
            // calculate colors
            for(int csi = 0; csi<4; ++csi) {
                // Linear interpolation between stops.
                int sample = (int)(ti*_stop_colors[k-1][csi]+t*_stop_colors[k][csi]);
                // sample is now an int in range 0-255, so no sign extension to worry about
                pixel = (pixel<<8) | sample;
            }
            break;
        }
    }
    
    // Return the pixel value
    return pixel;
}

/**
 * Sets the transform from user space to device space.
 */
public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
{
    // transform original line into device coords and recalculate values
    float pts[] = {_x0, _y0, _x1, _y1};
    x.transform(pts, 0, pts, 0, 2);
    Ax=pts[0];
    Ay=pts[1];
    BAx=pts[2]-pts[0];
    BAy=pts[3]-pts[1];
    denom = BAx*BAx+BAy*BAy;
}

/**
 * Multiple stop angle gradient with angles.
 */
public static class AngleGradient extends RMMultipleStopGradient {
    
    // The angle
    double _thstart;
    
    /** Creates a new angle gradient. */
    public AngleGradient(float x0, float y0, float x1, float y1, RMGradientFill fill)  { super(x0,y0,x1,y1,fill); }
    
    /** Sets the transform from user space to device space. */
    public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
    {
        super.setDeviceTransform(x, devRect);
        _thstart = Math.atan2(BAy, BAx);
    }
    
    /** Override. */
    public void doShading(int argb_samples[], int x, int y, int w, int h)
    {
        double twopi = Math.PI+Math.PI;
        
        // Iterate over height
        for(int j=0, sindex=0; j<h; j++) {
            double My = y+j-Ay, Mx = x-Ax;
            
            // Iterate over width
            for(int i=0; i<w; i++) {
                double t = Math.atan2(My,Mx) - _thstart;
                if(t<0) t += twopi;
                t /= twopi;
                argb_samples[sindex] = getShadePixel((float)t);
                sindex++;
                Mx++;
            }
        }
    }
}

/**
 * Multiple stop radial gradient.
 */
public static class RadialGradient extends RMMultipleStopGradient {

    // The ...
    double _maxRadius;

    /** Create MultipleStopRadialGradient. */
    public RadialGradient(float x0, float y0, float x1, float y1, RMGradientFill fill)  { super(x0,y0,x1,y1,fill); }
    
    /** Sets the transform from user space to device space. */
    public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
    {
      super.setDeviceTransform(x, devRect);
      _maxRadius = (float)Math.sqrt(BAx*BAx+BAy*BAy); 
    }
    
    /** Shading loop. */
    public void doShading(int argb_samples[], int x, int y, int w, int h)
    {
        // For every point P in the raster, find distance to start of gradient axis, scaled by length of the axis.
        for(int j=0, sindex=0; j<h; ++j) {
            double PAy2 = (y+j-Ay)*(y+j-Ay);
            double PAx = x-Ax;
            
            // Iterate over width - t is the distance (0-1) along the gradient axis
            for(int i=0; i<w; ++i) {
                float t = (float)(Math.sqrt(PAx*PAx+PAy2)/_maxRadius);
                argb_samples[sindex] = getShadePixel(t);
                sindex++;
                PAx++;
            }
        }
    }
}

/** A multiple stop diamond gradient. */
public static class DiamondGradient extends RMMultipleStopGradient {

    //
    double _fx[] = new double[2];
    double _fy[] = new double[2];
    double _d[] = new double[2];
    double _n[] = new double[2];

    /** Creates a new multiple stop diamond gradient. */
    public DiamondGradient(float x0, float y0, float x1, float y1, RMGradientFill fill)  { super(x0,y0,x1,y1,fill); }
    
    /** Sets the transform from user space to device space. */
    public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
    {
        super.setDeviceTransform(x, devRect);
    
        // get slope defined by gradient axis AB
        double m = BAy/BAx;
        
        // vector 45 degrees relative to AB
        _fx[0] = 1-m; _fy[0] = m+1;
        // vector -45 degrees relative to AB
        _fx[1] = m+1; _fy[1] = m-1;
        // cache numerator & denominator parts which are constant for all pixels
        for(int i=0; i<2; ++i) {
           _d[i] = _fx[i]*BAy - _fy[i]*BAx;
           _n[i] = _fy[i]*Ax - _fx[i]*Ay;
        }
    }
    
    /**
     * Override.
     */
    public void doShading(int argb_samples[], int x, int y, int w, int h)
    {
        // from each pixel, find the point t along AB of intersection with +45 degree vector and -45 degree vector.
        // Correct distance is the maximum of these two.
        for(int j=0, sindex=0; j<h; ++j) {
            double My = y+j, Mx = x;
            
            for(int i=0; i<w; ++i) {
                double t = Math.abs((_n[0]-_fy[0]*Mx+_fx[0]*My)/_d[0]);
                double t1 = Math.abs((_n[1]-_fy[1]*Mx+_fx[1]*My)/_d[1]);
                if(t1>t) t = t1;
                if(t>1) t = 1;
                argb_samples[sindex] = getShadePixel((float)t);
                sindex++;
                Mx++;
            }
        }
    }
}
    
}