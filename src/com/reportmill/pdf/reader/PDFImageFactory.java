package com.reportmill.pdf.reader;
import com.reportmill.pdf.reader.PDFImageColorModel.SoftMask;
import java.awt.Image;
import java.awt.image.*;
import java.awt.color.*;
import java.util.*;

/*
 * PDFImageFactory.java 
 * 
 * Base implementation of the ImageFactory interface.
 */
public class PDFImageFactory implements ImageFactory {

// Alpha channels can be specified for images via a "SMask" image,
// which is a /DeviceGray image that gets mapped to the size of 
// the destination image.
// Samples in the original image may or may not be premultiplied by
// the smask alpha samples, along with an additional matte color
// if specified by a /Matte entry
SoftMask readSMask(PDFStream smaskStream)
{
    Object val;
    int w,h,bpc;
    float matte_components[] = null;
    Map smaskDict = smaskStream.getDictionary();
    
    if ((!"/XObject".equals(smaskDict.get("Type"))) || 
        (!"/Image".equals(smaskDict.get("Subtype"))) ||
        (!"/DeviceGray".equals(smaskDict.get("ColorSpace"))))
        throw new PDFException("Illegal soft mask declaration");
    
    val = smaskDict.get("Width");
    w = ((Number)val).intValue();
    val =smaskDict.get("Height");
    h = ((Number)val).intValue();
    val = smaskDict.get("BitsPerComponent");
    bpc = ((Number)val).intValue();
    val = smaskDict.get("Matte");
    if (val instanceof List) {
        List mlist = (List)val;
        matte_components = new float[mlist.size()];
        for(int i=0, n=mlist.size(); i<n; ++i)
            matte_components[i] = ((Number)mlist.get(i)).floatValue();
    }
    // create the softmask object to hold the alpha
    return new SoftMask(smaskStream.decodeStream(), w, h, bpc, matte_components);
}

public Image getImage(PDFStream imageStream, ColorSpace cspace, PDFFile srcfile)
{
    // First check to see if we've done this already
    Map imageDict = imageStream.getDictionary();
    Image awtImage = (Image)imageDict.get("_rbcached_awtimage_");
    if (awtImage != null)
        return awtImage;
    
    // image xobjects only, although a form xobject could be made someday
    Object val = imageDict.get("Subtype");
    if (val == null) 
        return null;
    
    if (!val.equals("/Image")) {
        System.err.println(val+" xObjects not supported yet");
        return null;
    }
    
    if (imageStream.usesFilter("/JPXDecode")) {
        System.err.println("JPeg2000 (/JPXDecode filter) not supported yet ");
        return null;
    }
    if (imageStream.usesFilter("/DCTDecode")) {
        awtImage = getDCTImage(imageStream);
        imageDict.put("_rbcached_awtimage_", awtImage);
        return awtImage;
    }

    byte streamBytes[];
    int w,h,bpc=0,cpp;
    boolean isMask, expandBitmap=false;
    float dmins[], dmaxs[];
    SoftMask alphaMask=null;
    
    // Width (required)
    val = imageDict.get("Width");
    if (val == null)
        return null;
    w = ((Number)val).intValue();
    
    // Height (required)
    val = imageDict.get("Height");
    if (val == null)
        return null;
    h = ((Number)val).intValue();
    
    // ImageMask (optional)
    val = imageDict.get("ImageMask");
    isMask = ((val!=null) && ((Boolean)val).booleanValue());
    
    // SMask (alpha) is stored as a separate image XObject
    val = srcfile.resolveObject(imageDict.get("SMask"));
    if (val != null) {
        alphaMask = readSMask((PDFStream)val);
        // the size of the mask doesn't have to match the size
        // of the image, so tell the mask how big the image
        // is so it can map values correctly.
        alphaMask.setSourceImageSize(w,h);
    }
    
    // Bits per component (required except for image masks or /JPXDecode)
    val = imageDict.get("BitsPerComponent");
    if (val!=null)
        bpc = ((Number)val).intValue();

    // An image mask will get turned into an image with 8 byte samples, with 
    // two values - 1 & 0.  To draw the image, we create an indexed Colorspace
    // with 2 colors - one the stroke color and the other transparent black. 
    //
    // A few issues :
    //   1. this routine doesn't have access to the stroke color
    //   2. image can't be cached, since it may be drawn with a different stroke color
    //   3. what happens if the colorspace is a pattern or shading?  would need a clip path.
    if (isMask || (bpc==1)) {
        //For enormous monochrome images, use an image producer that renders the image in strips
        if ((w*h>4*1024*1024) && imageStream.usesFilter("/CCITTFaxDecode")) {
            awtImage = getCCITTFaxDecodeImage(imageStream,w,h);
            // uncached - there's really no point
            return awtImage;
        }
        
        //current color and alpha for mask, black and white for image
        byte clut[] = isMask ? new byte[]{0,0,0,-1,-1,-1,-1,0} : new byte[]{0,0,0,-1,-1,-1};
        cspace = new PDFIndexedColorSpace(PDFDeviceRGB.sharedInstance(), 1, isMask, clut);
        bpc=8;
        expandBitmap=true;
    }
    
    if (bpc==0)
        throw new PDFException("Illegal image format");
        
    // Components per pixel (from the colorspace)
    cpp = cspace.getNumComponents();
    
    // Get the actual bytes from the pdf stream, running through any filters if necessary
    try {
        streamBytes = imageStream.decodeStream();
    }
    catch (Exception e) {
        System.err.println("Error decoding image stream: "+e);
        return null;
    }
    
    // For image masks, check to see if filter already expanded the data out to 8bpp.
    // If not, do it now.
    if (expandBitmap) {
        if (w*h != streamBytes.length)
            streamBytes = expandBitmapBits(streamBytes,w,h);
        // if w & h are both 1, the number of bits equals the number of bytes
        // regardless of whether we've exapanded them or not.  So do it again
        // right here just in case.
        else if (w==1 && h==1)
            streamBytes[0]=(byte)(streamBytes[0]&1);
    }
    
    // Decode array (optional)
    // The decode array tells you how to turn the bits of an individual
    // componet sample into a float which would be valid in the colorspace.
    dmins=new float[cpp];
    dmaxs=new float[cpp];
    val = imageDict.get("Decode");
    if (val != null) {
        List dList = (List)val;
        
        if (dList.size() != cpp*2) {
            System.err.println("Wrong number of components in decode array");
            return null;
        }
        for(int i=0; i<cpp; ++i) {
            dmins[i]=((Number)dList.get(2*i)).floatValue();
            dmaxs[i]=((Number)dList.get(2*i+1)).floatValue();
        }
    }
    else { 
        //default decodes are [0 1 .. 0 1] except for indexed spaces
        //in which case its [0 2^bps-1]
        if (cspace instanceof PDFIndexedColorSpace) {
            dmins[0]=0;
            dmaxs[0]=(1<<bpc)-1;
        }
        else {
            for(int i=0; i<cpp; ++i) {
                dmins[i]=0;
                dmaxs[i]=1;
            }
        }
    }
    
    // Create a Raster for the image samples.
    // The raster will use meshed samples (as they are in pdf).
    // If the image specifies a softmask, its data will get meshed
    // in with the color samples.
    WritableRaster praster;
    if (alphaMask==null)
        praster = PDFImageColorModel.createPDFRaster(streamBytes,cspace,bpc,w,h);
    else
        praster = PDFImageColorModel.createPDFRaster(streamBytes, alphaMask, cspace, bpc, w, h);
    
    // Now create a PDFColorModel for the image.
    // The model takes care of colorspace conversion of the samples and 
    // knows how to return sRGB pixels with or without alpha for the BufferedImage
    if (praster != null) {
        PDFImageColorModel pixelModel = PDFImageColorModel.createPDFModel(cspace, bpc, dmins, dmaxs, alphaMask!=null);
        pixelModel.setSoftMask(alphaMask);
        awtImage = new BufferedImage(pixelModel, praster, false, null);
    }
  
    if (awtImage != null) 
        imageDict.put("_rbcached_awtimage_", awtImage);
    return awtImage;
}

// Expand 1 bit-per-pixel data out into 8 bits per pixel so we can create an
// awt image from it.  The image code will create a color table for black & white
public byte[] expandBitmapBits(byte streamBytes[], int width, int height)
{
    byte expandedBytes[] = new byte[width*height];
    int rowbytes = width/8;
    int x,y;
    int expanded_position, src_position;
    int mask, bits;
    
    // rows are padded to byte boundaries
    if (width % 8 != 0)
        ++rowbytes;
    // (in)sanity check
    if (height * rowbytes != streamBytes.length)
        throw new PDFException("wrong amount of data for image mask");
    
    expanded_position=src_position=0;
    for(y=0; y<height; ++y) {
        mask = 128;
        src_position = y*rowbytes;
        bits = streamBytes[src_position];
        for(x=0; x<width; ++x) {
            expandedBytes[expanded_position++] = (byte)((bits & mask)==0 ? 0 : 1);
            mask>>=1;
            if (mask==0 && x<width-1) {
                mask=128;
                bits = streamBytes[++src_position];
            }
        }
    }
    return expandedBytes;
}
        
       
// For DCTDecode images, try assuming the data is a valid jpeg stream and 
// let awt read it.
//TODO:  A big problem here is color spaces.  The awt version won't work
// for cmyk images that are dct encoded, for example.  Other,  more outlandish
// colorspaces definitely wont work.
// 
Image getDCTImage(PDFStream imageStream)
{
    byte dctbytes[];
    
    // Since DCTDecode is an image-specifc encoding, it makes no sense to
    // have any filters AFTER it (at least not with the kind of filters 
    // pdf currently supports).
    // Also, since we're going to try to pass the DCT encoded bytes to awt,
    // we have to run any filters that come BEFORE the dct, but leave the 
    // stream as DCT.
    //   /Filters [/ASCII85 /DCTDecode]
    // 
    if (imageStream.indexOfFilter("/DCTDecode") != imageStream.numFilters()-1)
        throw new PDFException("Illegal image stream");
    dctbytes = imageStream.decodeStream(imageStream.numFilters()-1);
    
    // hold your breath
    Image im = java.awt.Toolkit.getDefaultToolkit().createImage(dctbytes);
    
    // You need a component to use a media tracker
    ImWaiter waiter = new ImWaiter();
    if (!waiter.waitFor(im))
        throw new PDFException("Error loading DCTDecoded image");

    return im;
}

// Memory optimization for large ccittfax images.
// Do stream processing as above, but create a special image
// using an imageProducer that decrypts one scanline at a time.
Image getCCITTFaxDecodeImage(PDFStream imageStream, int width, int height)
{
    byte ccittfaxBytes[];

    // see above for comments on stream processing 
    // 
    if (imageStream.indexOfFilter("/CCITTFaxDecode") != imageStream.numFilters()-1)
        throw new PDFException("Illegal CCITTFaxDecode image stream");
    ccittfaxBytes = imageStream.decodeStream(imageStream.numFilters()-1);

    // Get the filter parameters
    Map params = imageStream.getFilterParameters("/CCITTFaxDecode");

    // Create a decoder
    RBCodecCCITTFaxDecode decoder = PDFStream.createCCITTFaxDecoder(params, ccittfaxBytes, 0, ccittfaxBytes.length);
    // Tell the decoder what we think the dimensions are.
    //   (CCITTFaxDecode doesn't know until it's finished decompressing.)
    decoder.setDimensions(width, height);
    
    // And an image producer
    PDFCCITTFaxProducer producer = new PDFCCITTFaxProducer(decoder);

    // And a tiled image
    PDFTiledImage.TiledImageProxy im = new PDFTiledImage.TiledImageProxy(producer);
 
    return im;
}

}

class ImWaiter implements ImageObserver {
  public int imagestatus=0;
  public ImWaiter() {super();}
  public boolean waitFor(Image i) {
      int w = i.getWidth(this);
      if (w != -1)
          return true;
      while(imagestatus==0)
          Thread.yield();
      return (imagestatus==1);
  }
  public boolean imageUpdate(Image i, int flags, int x, int y, int w, int h) {
    if ((flags & ABORT) != 0) {
        imagestatus=-1;
        return false;
    }
    if ((flags & ERROR) != 0) {
        imagestatus=-1;
        return false;
    }
    if ((flags & ALLBITS) !=0) {
        imagestatus=1;
        return false;
    }
    imagestatus=0;
    return true;
}
}
