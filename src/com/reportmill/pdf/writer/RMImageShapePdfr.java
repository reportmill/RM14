package com.reportmill.pdf.writer;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.geom.Rectangle2D;

/**
 * PDF writer for RMImageShape.
 */
public class RMImageShapePdfr <T extends RMImageShape> extends RMShapePdfr <T> {

/**
 * Override to write ImageData.
 */
protected void writeShape(T anImageShape, RMPDFWriter aWriter)
{
    // Do normal version
    super.writeShape(anImageShape, aWriter);
    
    // Get image fill and image data (just return if missing or invalid)
    RMImageData idata = anImageShape.getImageData(); if(idata==null || !idata.isValid()) return;
    Rectangle2D bounds = anImageShape.getImageBounds();
    
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
    
    // Get image bounds width and height
    double width = bounds.getWidth(), height = bounds.getHeight();

    // pdfImage writes out scale of imageBounds/imageSize
    if(pdfImage) {
        width /= idata.getImageWidth();
        height /= idata.getImageHeight();
    }

    // Apply CTM - image coords are flipped from page coords ( (0,0) at upper-left )
    pdfPage.transform(width, 0, 0, -height, bounds.getX(), bounds.getMaxY());
    
    // Do image
    pdfPage.appendln("/" + idata.getName() + " Do");
        
    // Grestore
    pdfPage.grestore();
    
    // If image has alpha, declare output to be PDF-1.4
    if(idata.hasAlpha() && idata.getSamplesPerPixel()==4)
        aWriter.setVersion(1.4f);
}

}