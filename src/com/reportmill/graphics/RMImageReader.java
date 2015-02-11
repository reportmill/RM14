package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import javax.imageio.ImageIO;
import snap.util.ByteArray;

/**
 * Uses ImageIO or JAI to read an image, its basic info and its decoded bytes.
 * The reader tries to provide basic info without loading the entire image file into memory.
 * Decoded bytes are only needed by things like PDF and Flash writers.
 */
public class RMImageReader implements RMImageData.ImageReader {
    
    // The image data that this reader loads into
    RMImageData      _imageData;
    
    // Supported image type strings
    static String    _types[] = { "gif", "jpg", "jpeg", "png", "tif", "tiff", "bmp" };

/**
 * Returns whether image reader can read files with given extension.
 */    
public static boolean canRead(String anExt)  { return RMStringUtils.containsIC(_types, anExt); }

/**
 * Returns whether image reader can read the file provided in the byte array.
 */
public static boolean canRead(byte bytes[])  { return getType(bytes)!=null; }

/**
 * Returns the type of the image bytes provided.
 */
public static String getType(byte bytes[])
{
    // Get number of bytes
    int size = bytes!=null? bytes.length : 0;
    
    // If GIF file, return "gif"
    if(size>3 && bytes[0]==(byte)'G' && bytes[1]==(byte)'I' && bytes[2]==(byte)'F')
        return "gif";
    
    // If JPEG file, return "jpg"
    if(size>2 && bytes[0]==(byte)0xff && bytes[1] == (byte)0xd8)
        return "jpg";
    
    // If PNG file, return "png"
    if(size>4 && bytes[0]==-119 && bytes[1]==(byte)'P' && bytes[2]==(byte)'N' && bytes[3]==(byte)'G')
        return "png";
    
    // If TIFF file (little endian), return "tiff"
    if(size>4 && bytes[0]==0x49 && bytes[1]==0x49 && bytes[2]==0x2a && bytes[3] == 0x00)
        return "tiff";
    
    // If TIFF file (big endian), return "tiff"
    if(size>4 && bytes[0]==0x4d && bytes[1]==0x4d && bytes[2]==0x00 && bytes[3]==0x2a)
        return "tiff";
    
    // If BMP file, return "bmp"
    if(size>2 && bytes[0]==0x42 && bytes[1]==0x4d)
        return "bmp";
    
    // If file type not recognized, return null
    return null;
}

/**
 * Creates a new image reader for given image data.
 */
public RMImageReader(RMImageData anImageData)  { _imageData = anImageData; }

/**
 * Returns the image data.
 */
public RMImageData getImageData()  { return _imageData; }

/**
 * Reads basic image info.
 */
public void readBasicInfo(RMImageData anImageData)
{
    // Set image type
    anImageData._type = getType(anImageData.getBytes());
    
    // Special case jpg, since PDF & Flash can make do with raw file data and _pw, _ph & _bps
    if(anImageData._type.equals("jpg")) {
        readBasicInfoJPG(); return; }
    
    // Read image
    BufferedImage image = getImageData().getImage();
    
    // Get basic info from image (width, height, samples per pixel, bits per sample)
    anImageData._width = image.getWidth();
    anImageData._height = image.getHeight();
    
    // Get samples per pixel and bits per sample from color model
    ColorModel cm = getColorModel();
    anImageData._spp = cm.getNumComponents();
    anImageData._bps = 8; // Only support 8 bit samples (expand everything)
    if(anImageData._spp==2) // Grayscale/alpha unsupported, expand to RGB
        anImageData._spp = 4;
    anImageData._hasAlpha = cm.hasAlpha();

    // Specail handling of IndexColorModel
    if(cm instanceof IndexColorModel)
        loadIndexColorModel();
}

/**
 * Reads basic image info specifically optimized for JPEG images (without having to create Java RenderedImage).
 */
public void readBasicInfoJPG()
{
    // Get reader for image bytes
    ByteArray reader = new ByteArray(_imageData.getBytes());
    
    // Declare variable for current read index
    int index = 2;
    
    // Iterate over JPEG markers
    while(true) {
    
        // Get marker from first 4 bytes (just return if not valid marker)
        int marker = reader.bigUShortAtIndex(index); index += 2;
        if((marker & 0xff00) != 0xff00)
            return;
        
        // Get size from next 4 bytes
        int size = reader.bigUShortAtIndex(index); index += 2;
        
        // Decode spp, bps, width & height
        if(marker >= 0xffc0 && marker <= 0xffcf && marker != 0xffc4 && marker != 0xffc8) {
            _imageData._spp = reader.getByte(index+5) & 0xff;
            _imageData._bps = reader.getByte(index) & 0xff;
            _imageData._width = reader.bigShortAtIndex(index + 3);
            _imageData._height = reader.bigShortAtIndex(index + 1);
            return;
        }
        
        // Decode DPI (APPx)
        else if(marker==0xffe0) { 

            // APPx header must be larger than 14 bytes
            if(size<14)
                return;
            
            // Declare variable for 12 bytes
            byte data[] = new byte[12];

            // Read next 12 bytes
            reader.getBytes(index, index+12, data);

            // Declare APP0_ID
            final byte[] APP0_ID = { 0x4a, 0x46, 0x49, 0x46, 0x00 };

            // If arrays are equal, read dpi values
            if(RMArrayUtils.equals(APP0_ID, data, 5)) {

                if(data[7]==1) {
                    float x = reader.bigShortAtIndex(index + 8);
                    float y = reader.bigShortAtIndex(index + 10);
                    if(x>50) _imageData._dpiX = x;
                    if(y>50) _imageData._dpiY = y;
                }

                else if(data[7]==2) {
                    int x = reader.bigShortAtIndex(index + 8);
                    int y = reader.bigShortAtIndex(index + 10);
                    _imageData._dpiX = (int)(x * 2.54f);
                    _imageData._dpiY = (int)(y * 2.54f);
                }    
            }
        }
        
        // Any other marker should just be skipped
        index += (size - 2);
    }
}

/**
 * Reads the buffered image.
 */
public BufferedImage readImage(RMImageData anImageData)
{
    // Try javax.imageio
    try {
        
        // Use image IO to read image
        BufferedImage image = null;
        try { image = ImageIO.read(RMUtils.getInputStream(_imageData.getBytes())); }
        catch(SecurityException e) {
            if(ImageIO.getUseCache()) {
                System.out.println("ImageIO Security Exception - turning off image cache");
                ImageIO.setUseCache(false);
                image = ImageIO.read(RMUtils.getInputStream(_imageData.getBytes()));
            }
        }
        
        // If is app, convert image to compatible to speed it up
        if(RMUtils.isApp) {
            
            // Get graphics configuration
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
            // If color models not equal create compatible image, so we'll have a "ManagedImage"
            if(image!=null && !image.getColorModel().equals(gc.getColorModel())) {
                BufferedImage i = gc.createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT /*image.getTransparency()*/);
                Graphics2D g = i.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                return i;
            }
        }
        
        // If image is non-null, return
        if(image!=null)
            return image;
    }
    
    // Catch exceptions
    catch(Exception e) { e.printStackTrace(); }
        
    // Try Java advanced imaging (standard install), which seems better than javax.imageio
    try {
        
        // Get image type (coerce jpg to "jpeg")
        String type = _imageData.getType().equals("jpg")? "jpeg" : _imageData.getType();
        
        // Get ImagegCodec class and ImageDecodeParam class
        Class c1 = Class.forName("com.sun.media.jai.codec.ImageCodec");
        Class c2 = Class.forName("com.sun.media.jai.codec.ImageDecodeParam");
        
        // Get createImageDecoder method and invoke
        Method createDecoderMethod = c1.getMethod("createImageDecoder", new Class[] { String.class, InputStream.class, c2 });
        
        // Gread image decoder
        Object d = createDecoderMethod.invoke(c1, new Object[] { type, RMUtils.getInputStream(_imageData.getBytes()), null });
        
        // Get decodeAsRenderedImage method and invoke
        Method decodeImageMethod = d.getClass().getMethod("decodeAsRenderedImage", (Class[])null);
        
        // Get rendered image from decodeAsRenderedImage method
        RenderedImage image = (RenderedImage)decodeImageMethod.invoke(d, (Object[])null);
        
        // If image is buffered image, just return it
        if(image instanceof BufferedImage)
            return (BufferedImage)image;

        // Otherwise create buffered image and render rendered image into it
        else {
            BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.drawRenderedImage(image, new java.awt.geom.AffineTransform());
            return bi;
        }
    }
    
    // Catch exceptions
    catch(Exception e) { return null; }    
}

/**
 * Returns the color model for the image data image.
 */
private ColorModel getColorModel()  { return _imageData.getImage().getColorModel(); }

/**
 * Loads and sets the decoded bytes for the image data.
 */
public void readBytesDecoded()
{
    // Catch exceptions
    try {
        
    // Load image
    getImageData().getImage();
    
    // Get color model
    ColorModel cm = getColorModel();
    
    // If component color model, load...
    if(cm instanceof ComponentColorModel)
        loadComponentColorModelBytes();
    
    // If direct color model, load...
    else if(cm instanceof DirectColorModel)
        loadDirectColorModelBytes();
    
    // If index color model, load...
    else if(cm instanceof IndexColorModel)
        loadIndexColorModelBytes();
    
    // If anything else, complain
    else {
        System.err.println("Color model not supported: " + cm.getClass().getName());
        _imageData._bytesDecoded = new byte[_imageData._height*_imageData.getBytesPerRow()];
        _imageData._valid = false;
    }

    // Catch exceptions (mark image data invalid)
    } catch(Exception e) {
        _imageData._valid = false;
        _imageData._bytesDecoded = new byte[_imageData._height*_imageData.getBytesPerRow()];
    }
}

/**
 * Load index color model.
 */
private void loadIndexColorModel()
{
    _imageData._spp = 1;
    
    // Load _imageData._colorMap
    IndexColorModel icm = (IndexColorModel)getColorModel();
    int tableColors = icm.getMapSize();
    int numColors = 1<<_imageData.getBitsPerPixel();
    byte reds[] = new byte[numColors];
    byte greens[] = new byte[numColors];
    byte blues[] = new byte[numColors];
    
    icm.getReds(reds);
    icm.getGreens(greens);
    icm.getBlues(blues);

    _imageData._colorMap = new byte[3*numColors];
    for(int i=0, j=0; i<tableColors; i++) {
        _imageData._colorMap[j++] = reds[i];
        _imageData._colorMap[j++] = greens[i];
        _imageData._colorMap[j++] = blues[i];
    } // Unused colors from tableColors to numColors are left uninitialized
    
    // Get _imageData._transparentColorIndex (JAI doesn't seem to set transparentPixel, but does set alpha entry)
    _imageData._transparentColorIndex = icm.getTransparentPixel();
    if(_imageData._transparentColorIndex<0 && icm.getTransparency()==IndexColorModel.BITMASK) {
        icm.getAlphas(reds);
        for(int i=0; i<tableColors; i++)
            if(reds[i]==(byte)0) {
                _imageData._transparentColorIndex = i;
                break;
            }
    }
}

/**
 * Load index color model bytes
 */
private void loadIndexColorModelBytes()
{
    // Load _imageData._bytes (I guess we ignore the pixel size and expand to 8 bit, oh-well)
    Raster raster = _imageData._image.getData();
    
    // Get bytes decoded
    _imageData._bytesDecoded = new byte[_imageData._width*_imageData._height];
    
    // Get data elements
    raster.getDataElements(0, 0, _imageData._width, _imageData._height, _imageData._bytesDecoded);
}

/**
 * Load component color model bytes.
 */
private void loadComponentColorModelBytes()
{
    // Get storage for pixel according to ComponentColorModel's transfer type
    ComponentColorModel ccm = (ComponentColorModel)getColorModel();
    Object out = null;
    switch(ccm.getTransferType()) {
        case DataBuffer.TYPE_BYTE: out = new byte[ccm.getNumComponents()]; break;
        case DataBuffer.TYPE_USHORT: out = new short[ccm.getNumComponents()]; break;
        case DataBuffer.TYPE_INT: out = new int[ccm.getNumComponents()]; break;
    }

    // Get raster to load pixels and allocate _byte array
    Raster raster = _imageData._image.getData();
    byte bytes[] = _imageData._bytesDecoded = new byte[_imageData._height*_imageData.getBytesPerRow()];
    
    for(int y=0; y<_imageData._height; y++) {
        for(int x=0; x<_imageData._width; x++) {
            out = raster.getDataElements(x, y, out);
            int sample = (y*_imageData._width + x)*_imageData._spp;

            switch(_imageData._spp) {
                case 4: bytes[sample+3] = (byte)ccm.getAlpha(out);
                case 3: bytes[sample+2] = (byte)ccm.getBlue(out);
                        bytes[sample+1] = (byte)ccm.getGreen(out);
                case 1: bytes[sample] = (byte)ccm.getRed(out); break;
            }
        }
    }
}

/**
 * Load direct color model bytes.
 */
private void loadDirectColorModelBytes()
{
    DirectColorModel dcm = (DirectColorModel)getColorModel();
    int psize = dcm.getPixelSize();
    
    if(psize!=24 && psize!=32) {
        System.err.println("Can't read direct color images with pixel size " + psize);
        _imageData._valid = false; return;
    }

    // Load _imageData._bytesDecoded from raster
    Raster raster = _imageData._image.getData();
    int pixels[] = new int[_imageData._width*_imageData._spp];
    byte bytes[] = _imageData._bytesDecoded = new byte[_imageData._height*_imageData.getBytesPerRow()];

    for(int y=0; y<_imageData._height; y++) {
        raster.getPixels(0, y, _imageData._width, 1, pixels);
        for(int x=0; x<_imageData._width; x++) {
            int sample = (y*_imageData._width + x)*_imageData._spp;
            bytes[sample] = (byte)pixels[x*_imageData._spp];
            bytes[sample + 1] = (byte)pixels[x*_imageData._spp+1];
            bytes[sample + 2] = (byte)pixels[x*_imageData._spp+2];
            if(_imageData._spp==4)
                bytes[sample + 3] = (byte)pixels[x*_imageData._spp+3];
        }
    }
}

}