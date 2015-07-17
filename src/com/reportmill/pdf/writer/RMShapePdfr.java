package com.reportmill.pdf.writer;
import com.reportmill.base.RMRect;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;

/**
 * This RMObjectPdfr subclass writes PDF for RMShape.
 */
public class RMShapePdfr <T extends RMShape> extends RMObjectPdfr<T> {

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
public void writePDF(T aShape, RMPDFWriter aWriter)
{
    // Write shape
    writeShapeBefore(aShape, aWriter);
    
    // If shape has effect, forward to it
    if(aShape.getEffect()!=null)
        RMEffectPdfr.writeShapeEffect(aShape, aShape.getEffect(), aWriter);
    
    // Otherwise, do basic write shape all
    else writeShapeAll(aShape, aWriter);
    
    // Write shape after children
    writeShapeAfter(aShape, aWriter);    
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeBefore(T aShape, RMPDFWriter aWriter)
{
    // Get page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Save the graphics transform
    pdfPage.gsave();
        
    // If not rotated/scaled, write simple translation matrix
    if(!aShape.isRSS())
        pdfPage.append("1 0 0 1 ").append(aShape.getX()).append(' ').append(aShape.getY()).appendln(" cm");
    
    // If rotated/scaled, write full transform
    else pdfPage.transform(aShape.getTransform());
}
    
/**
 * Writes the shape and then the shape's children.
 */
protected void writeShapeAll(T aShape, RMPDFWriter aWriter)
{
    // Write shape fills
    writeShape(aShape, aWriter);
    
    // Write shape children
    writeShapeChildren(aShape, aWriter);
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShape(T aShape, RMPDFWriter aWriter)
{
    // Get pdf page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Set shape opacity
    pdfPage.setOpacity(aShape.getOpacityDeep(), PDFPage.OPACITY_SHAPE);
    
    // Clip to bounds???
    //pageBuffer.print(aShape.getBoundsInside()); pageBuffer.println(" re W n"));
        
    // Get fill and write pdf if not null
    RMFill fill = aShape.getFill();
    if(fill!=null)
        RMFillPdfr.writeShapeFill(aShape, fill, aWriter);
    
    // Get stroke and write pdf if not null
    RMStroke stroke = aShape.getStroke();
    if(stroke!=null)
        RMFillPdfr.writeShapeStroke(aShape, stroke, aWriter);
}

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeChildren(RMShape aShape, RMPDFWriter aWriter)
{
    // Write children
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        if(child.isVisible())
            getPdfr(child).writePDF(child, aWriter);
    }
}
    
/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShapeAfter(T aShape, RMPDFWriter aWriter)
{
    // Get pdf page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Restore graphics state (with hack since RMPagePdfr doesn't do a gsave and unbalances things)
    if(pdfPage._gstack.getStackSize()>1)
        pdfPage.grestore();

    // Add link, if it's there (What happens with rotated or skewed shapes?)
    if(aShape.getURL() != null) {
        RMRect frame = aShape.convertRectToShape(aShape.getBoundsInside(), null);
        frame.y = aShape.getPageShape().getHeight() - frame.getMaxY();
        PDFAnnotation link = new PDFAnnotation.Link(frame, aShape.getURL());
        pdfPage.addAnnotation(link);
    }
}

/**
 * Returns the shape pdfr for a shape.
 */
public static RMShapePdfr getPdfr(RMShape aShape)  { return (RMShapePdfr)RMObjectPdfr.getPdfr(aShape); }

}