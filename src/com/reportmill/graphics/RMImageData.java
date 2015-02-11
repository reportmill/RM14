package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.RMShapePainter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import snap.web.WebURL;

/**
 * This class manages image data. Each instance holds the raw image data and provides methods to return
 * attributes of the decoded image.
 */
public class RMImageData implements Cloneable {

    // The object that provided image bytes
    Object              _source;
    
    // The time the source was last modified (in milliseconds since 1970)
    long                _modTime;

    // The AWT version of this image
    BufferedImage       _image;

    // The original file bytes
    byte                _bytes[];
    
    // The image bytes uncompressed
    byte                _bytesDecoded[];
    
    // The image page index (if from multi-page image type like PDF)
    int                 _pageIndex;
    
    // The image page count (if from multi-page image type like PDF)
    int                 _pageCount = 1;
    
    // The image type ("gif", "jpg", "png", etc.)
    String              _type = "";
    
    // The image pixels wide
    int                 _width;
    
    // The image pixels high
    int                 _height;
    
    // The preferred image width
    float               _dpiX;
    
    // The preferred image height
    float               _dpiY;
    
    // The image samples per pixel
    int                 _spp;
    
    // The image bits per sample
    int                 _bps;
    
    // Whether image has alpha
    boolean             _hasAlpha;
    
    // The image color map (if indexed color)
    byte                _colorMap[];
    
    // The image transparent color index (if indexed color with alpha index)
    int                 _transparentColorIndex = -1;
    
    // Whether this image is valid
    boolean             _valid = true;
    
    // The image reader used to load this image
    ImageReader         _reader = null;
    
    // The cache used to hold application instances
    static List <WeakReference<RMImageData>>  _cache = new ArrayList();
    
    // A shared empty ImageData
    public static RMImageData EMPTY = getImageData(RMImageData.class.getResourceAsStream("DefaultImage.png"));
    
/**
 * Returns an image data for a given source and page index.
 * Source can be an image or something that can provide bytes.
 * Page index only applicable for multi-page image types, like PDF.
 */
protected RMImageData(Object aSource, int aPageIndex)  { setSource(aSource, aPageIndex); }

/**
 * Returns an image data loaded from aSource.
 */
public static RMImageData getImageData(Object aSource)
{
    return aSource instanceof RMImageData? (RMImageData)aSource : getImageData(aSource, 0);
}

/**
 * Returns an image data loaded from aSource. If image type supports multiple pages, page index can be specified.
 */
public static synchronized RMImageData getImageData(Object aSource, int aPageIndex)
{
    // If source is null, return EMPTY, if image data, return it dereferencing given page
    if(aSource==null) return EMPTY;
    if(aSource instanceof RMImageData) return ((RMImageData)aSource).getPage(aPageIndex);
    
    // Get source url
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    
    // Iterate over image list and see if any match source
    for(int i=_cache.size()-1; i>0; i--) { RMImageData idata = _cache.get(i).get();
        
        // If null, remove weak reference and continue)
        if(idata==null) { _cache.remove(i); continue; }
        
        // If source matches cached source, return
        if(url!=null && url.equals(idata.getSourceURL()) || aSource==idata.getSource()) {
            idata.refresh();
            return idata.getPage(aPageIndex);
        }
    }

    // Create new ImageData, add to cache (as WeakReference) and return
    RMImageData idata = new RMImageData(aSource, aPageIndex);
    _cache.add(new WeakReference(idata));
    return idata;
}

/**
 * Returns the original source for the image (byte[], File, InputStream or whatever).
 */
public Object getSource()  { return _source; }

/**
 * Sets the source.
 */
protected void setSource(Object aSource, int aPageIndex)
{
    // Get URL, source, modified time
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    _source = url!=null? url : aSource;
    _modTime = url!=null && url.getFile()!=null? url.getFile().getModifiedTime() : System.currentTimeMillis();

    // If source is image, load up image
    if(aSource instanceof Image) {
        
        // Get BufferedImage and basic image info
        _image = RMAWTUtils.getBufferedImage((Image)aSource);
        _type = _image.getColorModel().hasAlpha()? "png" : "jpg";
        _width = _image.getWidth(); _height = _image.getHeight();
        _hasAlpha = _image.getColorModel().hasAlpha();
        _spp = _hasAlpha? 4 : 3;
        _bps = 8;
        
        // Set default reader and clear bytes
        _reader = new RMImageReader(this); _bytes = null;
    }
    
    // Otherwise, assume source can provide bytes
    else if(aSource!=null) {
        _bytes = url!=null && url.getFile()!=null? url.getFile().getBytes() : RMUtils.getBytes(aSource); // Get bytes
        _pageIndex = aPageIndex;  // Set PageIndex
        getReader(); _image = null; // Get reader and clear image
    }
}

/**
 * Returns the source URL, if loaded from URL.
 */
public WebURL getSourceURL()  { return _source instanceof WebURL? (WebURL)_source : null; }

/**
 * Refreshes data from source.
 */
protected void refresh()
{
    WebURL url = getSourceURL(); if(url==null || url.getFile()==null) return;
    url.getFile().refresh();
    if(url.getFile().getModifiedTime()>_modTime)
        setSource(url, _pageIndex);
}

/**
 * Returns the buffered image for image data.
 */
public BufferedImage getImage()  { return _image!=null? _image : (_image=createImage()); }

/**
 * Creates a buffered image for image data.
 */
protected BufferedImage createImage()  { ImageReader rdr = getReader(); return rdr!=null? rdr.readImage(this) : null; }

/**
 * Returns the original bytes for the image (loaded from the source).
 */
public byte[] getBytes()  { return _bytes!=null? _bytes : (_bytes=createBytes()); }

/**
 * Creates bytes from the image (loaded from the source).
 */
protected byte[] createBytes()  { return getSource() instanceof Image? RMAWTUtils.getBytesPNG(getImage()) : null; }

/**
 * Returns the decoded image bytes for the image.
 */
public byte[] getBytesDecoded()  { if(_bytesDecoded==null) createBytesDecoded(); return _bytesDecoded; }

/**
 * Creates decoded image bytes for the image.
 */
protected void createBytesDecoded()  { if(getReader()!=null) getReader().readBytesDecoded(); }

/**
 * Returns the name for the image (assigned from our hashCode).
 */
public String getName()  { return "" + System.identityHashCode(this); }

/**
 * Returns the type for the image (one of gif, jpg, png, pdf, etc.).
 */
public String getType()  { return _type; }

/**
 * Returns the page index for the image.
 */
public int getPageIndex()  { return _pageIndex; }

/**
 * Returns the total number of pages for the image.
 */
public int getPageCount()  { return _pageCount; }

/**
 * Returns the number of pixels horizontally.
 */
public int getWidth()  { return _width; }

/**
 * Returns the number of pixels vertically.
 */
public int getHeight()  { return _height; }

/**
 * Returns the actual display width of the image in printer's points using the image DPI if available.
 */
public double getImageWidth()  { return _dpiX>0? _width*(72f/_dpiX) : _width; }

/**
 * Returns the actual display height of the image in printer's points using the image DPI if available.
 */
public double getImageHeight()  { return _dpiY>0? _height*(72f/_dpiY) : _height; }

/**
 * Returns whether the image is non-grayscale.
 */
public boolean isColor()  { return hasColorMap() || _spp>2; }

/**
 * Returns the number of samples per pixel (RGB=3, RGBA=4, GrayScale=1, etc.).
 */
public int getSamplesPerPixel()  { return _spp; }

/**
 * Returns the number of bits per sample (eg, 24 bit RGB image is 8 bits per sample).
 */
public int getBitsPerSample()  { return _bps; }

/**
 * Returns the number of bits per pixel (derived from bits per sample and samples per pixel).
 */
public int getBitsPerPixel()  { return getBitsPerSample()*getSamplesPerPixel(); }

/**
 * Returns the number of bytes per row (derived from width and bits per pixel).
 */
public int getBytesPerRow()  { return (getWidth()*getBitsPerPixel()+7)/8; }

/**
 * Color map support: returns whether color map image has a transparent color.
 */
public boolean hasAlpha()  { return _hasAlpha; }

/**
 * Color map support: returns the index of the transparent color in a color map image.
 */
public int getAlphaColorIndex()  { return _transparentColorIndex; }

/**
 * Returns whether image uses a color map.
 */
public boolean hasColorMap()  { return _colorMap!=null; }

/**
 * Color map support: returns the bytes of color map from a color map image.
 */
public byte[] getColorMap()  { return _colorMap; }

/**
 * Returns the image data for a successive page.
 */
public RMImageData getPage(int aPage)
{
    // Clamp page number to legal values (just return this if page equal)
    int page = RMMath.clamp(aPage, 0, _pageCount-1); if(page==_pageIndex) return this;
    
    // Return new image data for new page number
    RMImageData clone = clone();
    clone._pageIndex = page; clone._image = null;
    clone.getReader().readBasicInfo(clone);
    return clone;
}

/**
 * Returns the reader used to load the image.
 */
public ImageReader getReader()
{
    // If reader has already been set, just return it
    if(_reader!=null || !_valid)
        return _reader;

    // If standard RMImageReader can read bytes, create and set
    if(RMImageReader.canRead(getBytes()))
        _reader = new RMImageReader(this);
    
    // If RMPDFImageReader can read bytes, create and set
    else if(RMPDFImageReader.canRead(getBytes()))
        _reader = new RMPDFImageReader(this);
        
    // If reader was found, have it read basic info
    if(_reader!=null)
        _reader.readBasicInfo(this);
    
    // If reader wasn't found, set valid to false
    else _valid = false;
    
    // Return reader
    return _reader;
}

/**
 * Returns whether the image was loaded successfully.
 */
public boolean isValid()  { return _valid; }

/**
 * An interface for classes that can handle image reading for RMImageData.
 */
public interface ImageReader {

    /** Reads basic image info (width, height, etc.) without parsing the whole image file, if possible. */
    public void readBasicInfo(RMImageData anImageData);
    
    /** Reads the image file as a buffered image. */
    public BufferedImage readImage(RMImageData anImageData);
    
    /** Reads the image bytes decoded. */
    public void readBytesDecoded();
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and class and get other
    if(anObj==this) return true;
    if(!(anObj instanceof RMImageData)) return false;
    RMImageData other = (RMImageData)anObj;
    
    // Check bytes (use method in case images source was java Image and bytes need to be generated), PageIndex
    if(!RMArrayUtils.equals(other.getBytes(), getBytes())) return false;
    if(other._pageIndex!=_pageIndex) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public RMImageData clone()
{
    try { return (RMImageData)super.clone(); }
    catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns whether given extension is supported.
 */
public static boolean canRead(String anExt)  { return RMImageReader.canRead(anExt) || RMPDFImageReader.canRead(anExt); }

/**
 * Draws image data in given rect.
 */
public void paint(RMShapePainter aPntr, double x, double y, double w, double h)
{
    // If reader is pdf reader, have it paint and return
    if(aPntr.isPrinting() && getReader() instanceof RMPDFImageReader) {
        RMPDFImageReader pdfReader = (RMPDFImageReader)getReader();
        pdfReader.paint(this, aPntr.getGraphics(), x, y, w, h);
        return;
    }
    
    // Get image
    Image image = getImage();
    if(image==null) image = EMPTY.getImage();
    
    // If image is non-null, draw it
    if(image!=null) {
        double sx = w/image.getWidth(null), sy = h/image.getHeight(null);
        AffineTransform transform = new AffineTransform(sx, 0, 0, sy, x, y);
        aPntr.drawImage(image, transform);
    }
}

}