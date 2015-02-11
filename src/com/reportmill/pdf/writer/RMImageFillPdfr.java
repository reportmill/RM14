package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.RMShape;
import java.util.*;

/**
 * This RMFillPdfr subclass writes PDF for RMImageFill.
 */
public class RMImageFillPdfr {

/**
 * Writes given RMImageFill to a PDF file.
 */
public static void writeImageFill(RMShape aShape, RMImageFill anImageFill, RMPDFWriter aWriter)
{
    RMImageFillPdfr.writeImageFill(anImageFill, aShape.getPathInBounds(), aShape.getBoundsInside(), aWriter);
}

/**
 * Writes given RMImageFill to a PDF file.
 */
public static void writeImageFill(RMImageFill anImageFill, RMPath aPath, RMRect bounds, RMPDFWriter aWriter)
{
    // Get image fill and image data (just return if missing or invalid)
    RMImageData idata = anImageFill.getImageData(); if(idata==null || !idata.isValid()) return;
    
    // Get whether image fill is for pdf image (and just return if no page contents - which is apparently legal)
    boolean pdfImage = idata.getReader() instanceof RMPDFImageReader;
    if(pdfImage) {
        RMPDFImageReader pdfImageReader = (RMPDFImageReader)idata.getReader();
        if(pdfImageReader.getPDFFile().getPage(idata.getPageIndex()).getPageContentsStream()==null)
            return;
    }

    // Add image data
    aWriter.addImageData(idata);

    // Get PDF page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Gsave
    pdfPage.gsave();
    
    // If pdf image, reset gstate defaults
    if(pdfImage) {
        pdfPage.setLineCap(0);
        pdfPage.setLineJoin(0);
    }
    
    // If path was provided, clip to it
    if(aPath!=null) {
        pdfPage.writePath(aPath);
        pdfPage.appendln(" W n");
    }

    // If rolled or scaled, translate to shape center, rotate, scale and return
    if(anImageFill.getRoll()!=0 || anImageFill.getScaleX()!=1 || anImageFill.getScaleY()!=1) {
        
        // Get shape width and height
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        // Get transform with translate to shape center, rotate and scale, and translate back
        RMTransform t = new RMTransform();
        t.translate(-width/2, -height/2);
        t.rotate(anImageFill.getRoll());
        t.scale(anImageFill.getScaleX(), anImageFill.getScaleY());
        t.translate(width/2, height/2);
        
        // Apply transform
        pdfPage.transform(t);
                
        // Transform bounds to enclose rotated and scaled image space
        bounds = t.invert().transform(bounds);
        
        // If not STYLE_TILE, scale enclosing bounds by image fill scale
        if(!anImageFill.isTiled()) {
            RMTransform t2 = new RMTransform();
            t2.translate(-width/2, -height/2);
            t2.scale(anImageFill.getScaleX(), anImageFill.getScaleY());
            t2.translate(width/2, height/2);
            bounds = t2.transform(bounds);
        }
    }

    // If fill style tile, stamp image edge 2 edge, left 2 right, top 2 bottom (could use PDF tiling patterns instead)
    if(anImageFill.isTiled()) {
        
        // Get image width/height, which becomes scale of image coords (except PDF images write out scale of 1x1)
        double width = anImageFill.getImageWidth();
        double height = anImageFill.getImageHeight();
        
        // Get starting x and y
        double startX = anImageFill.getX(); while(startX>bounds.x) startX -= width;
        double startY = anImageFill.getY(); while(startY>bounds.y) startY -= height;

        // Iterate left to right over shape width and top to bottom over shape height
        for(double x=startX, xMax=bounds.getMaxX(); x<xMax; x+=width) {
            for(double y=startY, yMax=bounds.getMaxY(); y<yMax; y+=height) {
                
                // Gsave, scale CTM, Do image and Grestore
                pdfPage.gsave();
                if(pdfImage)
                    pdfPage.transform(1, 0, 0, -1, x, height + y);
                else pdfPage.transform(width, 0, 0, -height, x, height + y);
                pdfPage.appendln("/" + idata.getName() + " Do");
                pdfPage.grestore();
            }
        }
    }

    // All other fillStyles just smack image in imageBounds
    else {
        
        // Get image bounds width and height
        double width = bounds.width, height = bounds.height;

        // pdfImage writes out scale of imageBounds/imageSize
        if(pdfImage) {
            width /= anImageFill.getImageWidth();
            height /= anImageFill.getImageHeight();
        }
    
        // Apply CTM - image coords are flipped from page coords ( (0,0) at upper-left )
        pdfPage.transform(width, 0, 0, -height, anImageFill.getX() + bounds.x, anImageFill.getY() + bounds.getMaxY());
        
        // Do image
        pdfPage.appendln("/" + idata.getName() + " Do");
    }
        
    // Grestore
    pdfPage.grestore();
    
    // If image has alpha, declare output to be PDF-1.4
    if(idata.hasAlpha() && idata.getSamplesPerPixel()==4) aWriter.setVersion(1.4f);
}

/**
 * Writes the PDF to embed the actual image bytes.
 */
public static void writeImageData(RMImageData anImageData, RMPDFWriter aWriter)
{
    // If invalid image, return
    if(!anImageData.isValid()) return;
    
    // If PDF image, run special case code
    if(anImageData.getReader() instanceof RMPDFImageReader) {
        writeImageDataPDF(anImageData, aWriter); return; }
    
    // Get image bits per sample and samples per pixel
    int bps = anImageData.getBitsPerSample();
    int spp = anImageData.getSamplesPerPixel();
    
    // Get image color space and whether image is jpg
    String colorspace = getColorSpace(anImageData, aWriter);
    boolean isJPG = anImageData.getType().equals("jpg");

    // Declare variable for image bytes to be encoded in PDF stream
    byte bytes[];

    // Get bytes - if JPG, just original file bytes
    if(isJPG)
        bytes = anImageData.getBytes();
    
    // Get bytes - just raw decoded bytes if valid format (RGB24 or Gray8)
    else if(isValidPDFImageFormat(anImageData))
        bytes = anImageData.getBytesDecoded();
    
    // Get bytes - covert all else to RGB24
    else {
        bytes = getBytesRGB24(anImageData);
        bps = 8;
        colorspace = "/DeviceRGB";
    }
    
    // Create image dictionary
    Map imageDict = new Hashtable(8);
    imageDict.put("Type", "/XObject");
    imageDict.put("Subtype", "/Image");
    imageDict.put("Name", "/" + anImageData.getName());
    imageDict.put("Width", anImageData.getWidth());
    imageDict.put("Height", anImageData.getHeight());
    imageDict.put("BitsPerComponent", bps);
    imageDict.put("ColorSpace", colorspace);
    
    // If JPG CMYK, put in bogus decode entry
    if(isJPG && spp==4)
        imageDict.put("Decode", "[1 0 1 0 1 0 1 0]");

    // If indexed image with transparency, define mask color index (chromakey masking)
    if(anImageData.hasColorMap() && anImageData.getAlphaColorIndex()>=0) {
        int tindex = anImageData.getAlphaColorIndex();
        String tarray = "[" + tindex + " " + tindex + "]";
        imageDict.put("Mask", tarray);
    }
    
    // If image has alpha channel, create soft-mask dictionary
    else if(anImageData.hasAlpha()) {
        
        // Get alpha bytes (should really do this with getBytesRGB24 above so we don't go through image bytes twice).
        byte alpha[] = getBytesAlpha8(anImageData);
        if(alpha!=null) {
            
            // Create soft-mask dict with basic attributes
            Map softMask = new Hashtable();
            softMask.put("Type", "/XObject");
            softMask.put("Subtype", "/Image");
            softMask.put("Width", anImageData.getWidth());
            softMask.put("Height", anImageData.getHeight());
            softMask.put("BitsPerComponent", 8);
            softMask.put("ColorSpace", "/DeviceGray");
            
            // create alpha bytes stream, xref and add to parent image dict
            PDFStream smask = new PDFStream(alpha, softMask);
            String smaskXRef = aWriter._xref.addObject(smask);
            imageDict.put("SMask", smaskXRef);
        }
    }
    
    // Create stream for image with image bytes and image dict
    PDFStream imagestream = new PDFStream(bytes, imageDict);
    
    // If JPG, add DCTDecode filter
    if(isJPG)
        imagestream.addFilter("/DCTDecode");
    
    // Write stream
    imagestream.writePDF(aWriter);
}

/**
 * Returns whether given image data image bytes are natively in supported PDF form.
 * PDF supports 1,2,4,8 bit gray or 1,2,4,8 bit indexed or 3,6,12,24 bit rgb or 4,8,16,32 bit cmyk images.
 * Also, data should be packed - no slop bytes at row end, although bits should be padded to byte boundary.
 */
static boolean isValidPDFImageFormat(RMImageData anImageData)
{
    // Get samples per pixel
    int spp = anImageData.getSamplesPerPixel();
    
    // Get bits per sample 
    int bps = anImageData.getBitsPerSample();
    
    // Get whether image is color
    boolean isColor = anImageData.isColor();
    
    // Get raw uncompressed image bytes
    byte bytes[] = anImageData.getBytesDecoded();

    // If bytes aren't packed, return false
    if(((bps*spp*anImageData.getWidth()+7)/8)*anImageData.getHeight() != bytes.length)
        return false;

    // If image is indexed with one 8-bit sample, return true
    if(anImageData.hasColorMap() && (bps==8) && (spp==1))
        return true;

    // If image is color with 3 samples, return true
    if((isColor && spp==3) && (bps==1 || bps==2 || bps==4 || bps==8))
        return true;
    
    // If image is grayscale with 1 sample, return true
    if((!isColor && spp==1) && (bps==1 || bps==2 || bps==4 || bps==8))
        return true;
    
    // All else is invalid
    return false;
}

/** Returns the image data's raw image bytes as RGB24. */
private static byte[] getBytesRGB24(RMImageData anImageData)
{
    // Get raw image bytes
    byte imagebits[] = anImageData.getBytesDecoded();
    
    // Only conversion we do is to blow off alpha in 32bit rgba images.
    if(anImageData.isColor() && anImageData.getSamplesPerPixel()==4 && anImageData.getBitsPerSample()==8) {
        
        // Get image rows
        int rows = anImageData.getHeight();
        
        // Get image width
        int width = anImageData.getWidth();
        
        // Get image bytes per row
        int bpr = anImageData.getBytesPerRow(), dstoff = 0;
        
        // Create byte array for RGB bytes
        byte rgb[] = new byte[rows*3*width];
        
        // Iterate over image rows
        for(int i=0; i<rows; i++) {
            
            // Calculate offset to this row
            int srcoff = i*bpr;
            
            // Iterate over image row bytes
            for(int j=0; j<width; j++) {
                rgb[dstoff++] = imagebits[srcoff++];
                rgb[dstoff++] = imagebits[srcoff++];        
                rgb[dstoff++] = imagebits[srcoff++];
                srcoff++;
            }
        }
        
        // Return RGB bytes
        return rgb;
    }
    
    // If image conversion not supported, complain
    System.err.println("Unknown image format :\n\tBitsPerSample = " + anImageData.getBitsPerSample() + 
        "\n\tSamplesPerPixel = " + anImageData.getSamplesPerPixel() + "\n\tisColor = " + anImageData.isColor() + 
        "\n\tWidth = " + anImageData.getWidth() + "\n\tHeight = " + anImageData.getHeight() +
        "\n\tLength = " + imagebits.length);
    
    // If image conversion not supported, return null
    return null;
}

/**
 * Returns the image data's raw alpha bytes as byte array.
 */
private static byte[] getBytesAlpha8(RMImageData anImageData)
{
    // Get raw image bytes
    byte imagebits[] = anImageData.getBytesDecoded();
    
    // Only conversion we do is to extract alpha from 32bit rgba images.
    if(anImageData.isColor() && anImageData.getSamplesPerPixel()==4 && anImageData.getBitsPerSample()==8) {
        
        // Get image rows
        int rows = anImageData.getHeight();
        
        // Get image width
        int width = anImageData.getWidth();
        
        // Get image bytes per row
        int bpr = anImageData.getBytesPerRow(), dstoff = 0;
        
        // Create byte array for alpha bytes
        byte alpha[] = new byte[rows*width];
        
        // Keep track of whether alpha is actually used.
        boolean allOpaque = true;
        
        // Iterate over image rows
        // Note that assumption is that pixel format is rgba.
        for(int i=0; i<rows; i++) {
            
            // Calculate offset to this row
            int srcoff = i*bpr;
            
            // Iterate over image row bytes
            for(int j=0; j<width; j++) {
                srcoff += 3;
                alpha[dstoff] = imagebits[srcoff++];
                if (alpha[dstoff] != -1) 
                    allOpaque=false;
                ++dstoff;
            }
        }
        
        // Return alpha bytes, but only if there's useful information in them
        return allOpaque ? null : alpha;
    }
    
    // If image conversion not supported, complain
    System.err.println("Unknown image format :\n\tbitsPerSample = " + anImageData.getBitsPerSample() + 
        "\n\tsamplesPerPixel = " + anImageData.getSamplesPerPixel() + "\n\tisColor = " + anImageData.isColor() + 
        "\n\tpixelsWide = " + anImageData.getWidth() + "\n\tpixelsHigh = " + anImageData.getHeight() +
        "\n\tdataLength = " + imagebits.length);
    
    // If image conversion not supported, return null
    return null;
}

/**
 * Returns the pdf colorspace string for a given image data.
 */
static public String getColorSpace(RMImageData anImageData, RMPDFWriter aWriter)
{
    // If image is gray scale, return /DeviceGray
    if(!anImageData.isColor())
        return "/DeviceGray";

    // If image has color map, return map
    if(anImageData.hasColorMap()) {
        
        // Get color map bytes
        byte map[] = anImageData.getColorMap();
        
        // Get color map entries
        int entries = 1<<anImageData.getBitsPerPixel();
        
        // Error check for bad color map size
        if(entries*3 != map.length) {
            System.err.println("Image has bad color map size");
            return null;
        }
      
        // Create color map bytes stream
        PDFStream colorMapBytesStream = new PDFStream(map, null);
        
        // Add color map bytes stream to PDF file
        String mapref = aWriter.getXRefTable().addObject(colorMapBytesStream);
        
        // Return color space string including reference to color map bytes stream
        return "[/Indexed /DeviceRGB" + " " + (entries-1) + " " + mapref +"]";
    }
    
    // If image has 4 sample per pixel, return CMYK
    if(anImageData.getSamplesPerPixel()==4)
        return "/DeviceCMYK";

    // Return RGB
    return "/DeviceRGB";
}

/* ----------- PDF Image Reader ------------
 * The whole idea here is to be able to extract a page from a pdf file
 * and import it into another by turning it into a pdf form xobject.  This allows
 * us to independently control its ctm and use it over & over.
 * We have to extract any resources that it uses and add them to the new pdf
 * file.
 */
public static void writeImageDataPDF(RMImageData anImageData, RMPDFWriter aWriter)
{
    // Get the pdf image reader
    RMPDFImageReader reader = (RMPDFImageReader)anImageData.getReader();

    // Trap exceptions for PDF parsing (reader.getPDFFile())
    try {

    // Get reader PDF file and page
    com.reportmill.pdf.reader.PDFFile readerFile = reader.getPDFFile();
    com.reportmill.pdf.reader.PDFPage readerPage = readerFile.getPage(anImageData.getPageIndex());
    
    // Bump the pdf version number, if necessary
    aWriter.setVersion(readerFile.getVersion());
    
    // Get the crop box of page for use in BBox and Matrix strings
    RMRect crop = new RMRect(readerPage.getCropBox());
    
    // Now create the form dictionary
    Map dict = new Hashtable(8);
    dict.put("Type", "/XObject");
    dict.put("Subtype", "/Form");
    dict.put("FormType", "1");
    dict.put("BBox", "[" + crop.x + " " + crop.y + " " + crop.getMaxX() + " " + crop.getMaxY() + "]");
    dict.put("Matrix", "[1 0 0 1 " + (-crop.x) + " " + (-crop.y) + "]");
    dict.put("Name", "/" + anImageData.getName());
    
    // Add page resources from reader page to writer and form dictionary
    Object readerPageResources = readerPage.getPageResources();
    Object writerPageResources = addObjectToWriter(readerFile, readerPageResources, aWriter);
    dict.put("Resources", writerPageResources);
  
    // Get reader page contents stream and add its dictionary to writer
    com.reportmill.pdf.reader.PDFStream readerPageContentsStream = readerPage.getPageContentsStream();
    Map readerPageContentsDictionary = readerPageContentsStream.getDictionary();
    Map writerPageContentsDictionary = (Map)addObjectToWriter(readerFile, readerPageContentsDictionary, aWriter);
    dict.putAll(writerPageContentsDictionary);
    
    // Now create the form stream object and dump it to writer
    PDFStream formStream = new PDFStream(readerPageContentsStream.getBytes(), dict);
    formStream.writePDF(aWriter);
    
    // Everything is wrapped in parse exception handler, complain if thrown
    } catch(Exception e) { System.err.println("Error parsing pdf file : " + e); e.printStackTrace(); }    
}

/**
 * This function recurses through a tree of PDF objects and make sure
 * that any objects that are referenced within the tree get added to the xref table.
 * Starting at the root node, we traverse every object in the tree.  If any of the nodes
 * are indirect references (ie. the pdf "n 0 R") we add the object that is referenced to the xref table.
 * Returns a fully resolved object suitable for inclusion in the file.
 */
public static Object addObjectToWriter(com.reportmill.pdf.reader.PDFFile pFile, Object anObj, RMPDFWriter aWriter)
{
    // Check for local version, and if found, return
    Object local = aWriter._readerWriterXRefMap.get(anObj);
    if(local!=null)
        return local;
        
    // Handle reader entry: add entry object and return object xref entry
    if(anObj instanceof com.reportmill.pdf.reader.PDFXEntry) {
        Object resolved = pFile.resolveObject(anObj);
        Object resolveLocal = addObjectToWriter(pFile, resolved, aWriter);
        local = aWriter.getXRefTable().addObject(resolveLocal);
    }
    
    // Handle reader stream: add stream dictionary and return new stream with new dictionary
    else if(anObj instanceof com.reportmill.pdf.reader.PDFStream) {
        com.reportmill.pdf.reader.PDFStream readerStream = (com.reportmill.pdf.reader.PDFStream)anObj;
        Map writerDictionary = (Map)addObjectToWriter(pFile, readerStream.getDictionary(), aWriter);
        local = new PDFStream(readerStream.getBytes(), writerDictionary);
    }

    // Handle map: add map values and return new map with new values
    else if(anObj instanceof Map) { Map map = (Map)anObj, map2 = new HashMap(); local = map2;
        for(Map.Entry entry : (Set <Map.Entry>)map.entrySet()) {
            Object value = addObjectToWriter(pFile, entry.getValue(), aWriter);
            map2.put(entry.getKey(), value);
        }
    }
    
    // Handle list: add list values and return new list with new values
    else if(anObj instanceof List) { List list = (List)anObj, list2 = new ArrayList(list.size()); local = list2;
        for(int i=0, iMax=list.size(); i<iMax; i++) {
            Object value = addObjectToWriter(pFile, list.get(i), aWriter);
            list2.add(value);
        }
    }
    
    // Handle everything else
    else local = anObj;

    // Add local object if it has changed
    if(local!=anObj)
        aWriter._readerWriterXRefMap.put(anObj, local);

    // Return object reference
    return local;
}

}