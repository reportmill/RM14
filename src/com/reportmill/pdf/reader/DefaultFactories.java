package com.reportmill.pdf.reader;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.color.*;
import java.util.*;
import java.io.InputStream;

public class DefaultFactories implements ColorFactory, PathFactory {

  ColorSpace cmykspace = null;
  Map _colorCache = null;
 
public DefaultFactories() {
    super();
    _colorCache = new Hashtable();
}

// Get a cached instance of the generic deviceCMYK space
private ColorSpace getDeviceCMYK()
{
    // should probably cache all the colorspaces, but since this is
    // such a monster, we'll start here
    if (cmykspace == null) 
       cmykspace = findICCSpace("CMYK.icc"); 
//        cmykspace = new PDFDeviceCMYK();
    return cmykspace;
}

// utility routine to load .pf file resources and initialize color spaces from them
private ColorSpace findICCSpace(String name)
{
    try {
        InputStream s=DefaultFactories.class.getResourceAsStream(name);
        ICC_Profile prof = ICC_Profile.getInstance(s);
        return new ICC_ColorSpace(prof);
    }
    catch (Exception e) {
        System.err.println("Error loading color space resource");
        return null;
    }
}

    
public ColorSpace createColorSpace(int type, Object params)
{
    //TODO: recheck this mapping
    switch (type) {
        // The device spaces 
        case DeviceGrayColorspace : return ColorSpace.getInstance(ColorSpace.CS_GRAY);
        case DeviceRGBColorspace : return PDFDeviceRGB.sharedInstance();
        case DeviceCMYKColorspace : return getDeviceCMYK();
        
        // The CIE spaces
        // TODO: Get appropriate .pf files for these to match the spec
        case CalibratedGrayColorspace : return createColorSpace(DeviceGrayColorspace,null);
        case CalibratedRGBColorspace : return createColorSpace(DeviceRGBColorspace, null);
       
        // ICC Based space - a CIE space that is specified in the pdf stream
        case ICCBasedColorspace : 
            PDFStream s = (PDFStream)params;
            Map iccinfo = s.getDictionary();
            ICC_Profile profile = null;
            try {
                byte iccdata[] = s.decodeStream();
                profile = ICC_Profile.getInstance(iccdata);
            } catch (Exception e) {System.err.println("Error reading colorspace");}
            if (profile != null) {
                Object ncomps = iccinfo.get("N");
                if (ncomps != null) {
                  if (profile.getNumComponents() != ((Number)ncomps).intValue()) 
                      System.err.println("Error reading embedded colorspace.  Wrong number of components.");
                  }
                return new ICC_ColorSpace(profile);
             }
            else {
                Object alternate = iccinfo.get("Alternate");
                //TODO:real implementation
                System.err.println("Couldn't load ICC color space .  Need to use alternate "+
                                    alternate);
            }
            break;
        case IndexedColorspace :
            Map indexdict = (Map)params;
            return new PDFIndexedColorSpace((ColorSpace)indexdict.get("Base"),
                                            ((Number)indexdict.get("HiVal")).intValue(),
                                            (byte[])indexdict.get("Lookup"));
        case SeparationColorspace :
            Map sepdict = (Map)params;
            return new PDFSeparationColorSpace((String)sepdict.get("Colorant"),
                                               (ColorSpace)sepdict.get("Base"),
                                               (PDFFunction)sepdict.get("TintTransform"));
        case DeviceNColorspace :
            Map devndict = (Map)params;
            return new PDFDeviceNColorSpace((List)devndict.get("Colorants"),
                    (ColorSpace)devndict.get("Base"),
                    (PDFFunction)devndict.get("TintTransform"),
                    (Map)devndict.get("Attributes"));            
        case PatternColorspace :
            if (params instanceof ColorSpace)
                return new PDFPatternSpace((ColorSpace)params);
            else 
                return new PDFPatternSpace();
        default :
            System.err.println("This is getting boring.  Need to implement colorspace id="+type);
    }
    
    // Return a default.  The parser's going to barf if the number of parameters passed to a 
    // sc operation doesn't match the number of components in this space.   Don't say
    // you weren't warned.
    return ColorSpace.getInstance(ColorSpace.CS_sRGB);
}

// According to JProfiler, 50% of the time parsing a page is spent in java.awt.Color.<init>
// God only know what they could possibly be doing in there, but it certainly
// warrants a color cache.

public Color createColor(ColorSpace space, float values[])
{
    Color c;
    ArrayList key = new ArrayList(4);
    
    // make a hashtable key from the params
    key.add(space);
    for(int i=0, n=values.length; i<n; ++i)
        key.add(new Float(values[i]));
    
    // see if it's in the cache
    c = (Color)_colorCache.get(key);
    
    if (c==null) {
        // alpha = 1
        c = new Color(space, values, 1f);
        _colorCache.put(key,c);
    }
    return c;
}

public GeneralPath createEmptyPath()
{
    return new GeneralPath();
}

public Stroke createStroke(PDFGState gs)
{
    int cap, join;

    /* Convert from pdf constants to awt constants */
    switch (gs.lineCap) {
    case PDFButtLineCap:   cap=BasicStroke.CAP_BUTT;   break;
    case PDFRoundLineCap:  cap=BasicStroke.CAP_ROUND;  break;
    case PDFSquareLineCap: cap=BasicStroke.CAP_SQUARE; break;
    default:
        cap=BasicStroke.CAP_SQUARE;
    }

    switch (gs.lineJoin) {
    case PDFMiterJoin: join=BasicStroke.JOIN_MITER; break;
    case PDFRoundJoin: join=BasicStroke.JOIN_ROUND; break;
    case PDFBevelJoin: join=BasicStroke.JOIN_BEVEL; break;
    default:
        join=BasicStroke.JOIN_ROUND;
    }
    
    if ((gs.lineDash == null) || (gs.lineDash.length==0))
        return new BasicStroke(gs.lineWidth,
                cap,
                join,
                gs.miterLimit);
    else
        return new BasicStroke(gs.lineWidth,
                           cap,
                           join,
                           gs.miterLimit,
                           gs.lineDash,
                           gs.dashPhase);
}

public Composite createComposite(ColorSpace sourcespace, int blendMode, boolean alphaIsShape, float alpha)
{
    /*TODO: implement some blend modes */
    switch (blendMode) {
    case NormalBlendMode:
    case MultiplyBlendMode:
    case ScreenBlendMode:
    case OverlayBlendMode:
    case DarkenBlendMode:
    case LightenBlendMode:
    case ColorDodgeBlendMode:
    case ColorBurnBlendMode:
    case HardLightBlendMode:
    case SoftLightBlendMode:
    case DifferenceBlendMode:
    case ExclusionBlendMode:
    case HueBlendMode:
    case SaturationBlendMode:
    case ColorBlendMode:
    case LuminosityBlendMode:
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }
    return null;
}


}
