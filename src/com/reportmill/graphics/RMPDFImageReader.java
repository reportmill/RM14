package com.reportmill.graphics;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.reportmill.base.RMRect;
import com.reportmill.pdf.reader.*;

/**
 * Provides info for an encapsulated PDF (a PDF used as an image).
 */
public class RMPDFImageReader implements RMImageData.ImageReader {
    
    // The PDF file
    PDFFile          _pdfFile;

/**
 * Creates a new PDF image reader.
 */
public RMPDFImageReader(RMImageData anImageData)  { }

/**
 * Returns whether PDF image reader can read files with given extension.
 */    
public static boolean canRead(String anExt)  { return anExt!=null && anExt.toLowerCase().equals("pdf"); }

/**
 * Returns whether PDF image reader can read the file provided in the byte array.
 */
public static boolean canRead(byte data[])
{
    // Return true if first 5 bytes are "%PDF-"
    if(data==null || data.length<10) return false;
    return data[0]==(byte)'%' && data[1]==(byte)'P' && data[2]==(byte)'D' && data[3]==(byte)'F' && data[4]==(byte)'-';
}

/**
 * Returns the PDF file for the PDF image data (creating if necessary).
 */
public PDFFile getPDFFile()  { return _pdfFile; }

/**
 * Reads the basic info from PDF data.
 */
public void readBasicInfo(RMImageData anImageData)
{
    // Set type
    anImageData._type = "pdf";
    
    // Create PDF file from image data bytes and set
    if(_pdfFile==null)
        _pdfFile = new PDFFile(anImageData.getBytes());
    
    // Get pdf page
    PDFPage page = _pdfFile.getPage(anImageData.getPageIndex());
    
    // Set image data width and height
    anImageData._width = (int)Math.ceil(page.getCropBox().getWidth());
    anImageData._height = (int)Math.ceil(page.getCropBox().getHeight());
    
    // Set image data page count
    anImageData._pageCount = _pdfFile.getPageCount();
}

/**
 * Reads the image.
 */
public BufferedImage readImage(RMImageData anImageData)
{
    return getPDFFile().getPage(anImageData.getPageIndex()).getImage();
}

/**
 * ImageReader method - just a stub, since PDF images don't have image bytes.
 */
public void readBytesDecoded()  { }

/**
 * Draw at maximum resolution.  Page is scaled & translated to fit exactly in r.
 */
public void paint(RMImageData anImageData, Graphics2D g, double x, double y, double w, double h)
{
    // Get rect
    RMRect rect = new RMRect(x, y, w, h);
    
    // Have pdf file draw
    getPDFFile().getPage(anImageData.getPageIndex()).paint(g, rect);
}

}