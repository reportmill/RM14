package com.reportmill.pdf.writer;
import com.reportmill.shape.*;

/**
 * This RMShapePdfr subclass writes PDF for RMPage.
 */
public class RMPagePdfr <T extends RMPage> extends RMShapePdfr <T> {

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeBefore(T aPageShape, RMPDFWriter aWriter)
{
    // Get pdf page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Write page header comment
    pdfPage.appendln("\n% ------ page " + (aPageShape.page() - 1) + " -----");
        
    // legacy defaults different from pdf defaults
    pdfPage.setLineCap(1);
    pdfPage.setLineJoin(1);
    
    // Flip coords to match java2d model
    pdfPage.append("1 0 0 -1 0 ").append(aPageShape.getHeight()).appendln(" cm");    
}

}