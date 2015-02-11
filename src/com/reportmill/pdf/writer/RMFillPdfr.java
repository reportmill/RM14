package com.reportmill.pdf.writer;
import com.reportmill.base.RMPoint;
import com.reportmill.graphics.*;
import com.reportmill.shape.RMShape;
import java.util.*;

/**
 * This RMObjectPdfr subclass writes PDF for RMFill.
 */
public class RMFillPdfr {

/**
 * Writes a given shape fill.
 */
public static void writeShapeFill(RMShape aShape, RMFill aFill, RMPDFWriter aWriter)
{
    if(aFill instanceof RMGradientFill) writeGradientFill(aShape, (RMGradientFill)aFill, aWriter);
    else if(aFill instanceof RMImageFill) RMImageFillPdfr.writeImageFill(aShape, (RMImageFill)aFill, aWriter);
    else writeBasicFill(aShape, aFill, aWriter);    
}

/**
 * Writes a given shape stroke.
 */
public static void writeShapeStroke(RMShape aShape, RMStroke aStroke, RMPDFWriter aWriter)
{
    if(aStroke instanceof RMDoubleStroke) writeDoubleStroke(aShape, (RMDoubleStroke)aStroke, aWriter);
    else writeStroke(aShape, aStroke, aWriter);
}

/**
 * Writes PDF for a plain RMFill.
 */
public static void writeBasicFill(RMShape aShape, RMFill aFill, RMPDFWriter aWriter)
{
    // Get shape path and PDF page and write path
    RMPath path = aShape.getPathInBounds();
    PDFPage pdfPage = aWriter.getPDFPage();
    pdfPage.writePath(path);
    
    // Set fill color and write fill operator
    pdfPage.setFillColor(aFill.getColor());
    pdfPage.append('f');
    
    // If path winding rule odd, write odd fill operator
    if(path.getWindingRule()==RMPath.WIND_EVEN_ODD)
        pdfPage.append('*');
    
    // End line
    pdfPage.appendln();
}

/**
 * Writes PDF for a plain RMStroke.
 */
public static void writeStroke(RMShape aShape, RMStroke aStroke, RMPDFWriter aWriter)
{    
    // Get PDF page and write stroke path
    PDFPage pdfPage = aWriter.getPDFPage();
    pdfPage.writePath(aStroke.getStrokePath(aShape));
    
    // Set stroke color and width
    pdfPage.setStrokeColor(aStroke.getColor());
    pdfPage.setStrokeWidth(aStroke.getWidth());
    
    // Write dash array
    if(aStroke.getDashArray()!=null && aStroke.getDashArray().length>1)
        pdfPage.append('[').append(RMStroke.getDashArrayString(aStroke.getDashArray(), " ")).append("] ")
            .append(aStroke.getDashPhase()).appendln(" d");
    
    // Write stroke operator
    pdfPage.appendln("S");
}

public static void writeDoubleStroke(RMShape aShape, RMDoubleStroke aDoubleStroke, RMPDFWriter aWriter)
{
    writeStroke(aShape, aDoubleStroke.getOuterStroke(), aWriter);
    writeStroke(aShape, aDoubleStroke.getInnerStroke(), aWriter);
}

/** 
 * Writes pdf for the path filled with a shading pattern defined by the RMGradientFill
 */
public static void writeGradientFill(RMShape aShape, RMGradientFill aFill, RMPDFWriter aWriter)
{
    // Get shape path and PDF page and write path
    RMPath path = aShape.getPathInBounds();
    PDFPage pdfPage = aWriter.getPDFPage();
    pdfPage.writePath(path);
    
    // Get the xref table so we can add objects to it
    PDFXTable xref = aWriter.getXRefTable();
    
    // Set the fill colorspace to the pattern colorspace (subspace is always rgb)
    pdfPage.appendln("/Pattern cs");

    // Create pdf functions that interpolate linearly between color stops
    int colorStopCount = aFill.getColorStopCount();
    ArrayList fns = new ArrayList(colorStopCount);
    Map function = null;
    String outerBounds = "", outerDomain = "", encode = "";
    for(int i=0; i<colorStopCount-1; ++i) {
        function = new Hashtable(5);
        RMColor c0 = aFill.getStopColor(i);
        RMColor c1 = aFill.getStopColor(i+1);
        float d0 = aFill.getStopPosition(i);
        float d1 = aFill.getStopPosition(i+1);
        function.put("FunctionType", "2");
        function.put("Domain", "[0 1]");
        function.put("N", "1");
        function.put("C0", c0);
        function.put("C1", c1);
        fns.add(function);
        
        // add endpoints to Domain & Bounds arrays of stitching function
        if(i==0)
            outerDomain += d0;
        else outerBounds += " " + d0;
        if(i==colorStopCount-2)
            outerDomain += " " + d1;

        // all input to sub-functions mapped to range 0-1
        encode += "0 1 ";
    }
    
    // If there are multiple stops, create a stitching function to combine all the functions
    if(colorStopCount>2) {
        function = new Hashtable(5);
        function.put("FunctionType", "3");
        function.put("Functions", fns);
        function.put("Domain", "[" + outerDomain + "]");
        function.put("Bounds", "[" + outerBounds + "]");
        function.put("Encode", "[" + encode + "]");
    }
    
    // Create a shading dictionary for the gradient
    Map shading = new Hashtable(4);
    boolean isRadial = aFill instanceof RMRadialGradientFill;
    
    shading.put("ShadingType", isRadial? "3" : "2");  // radial or axial shading
    shading.put("ColorSpace", "/DeviceRGB");  // rgb colorspace
    shading.put("AntiAlias", "true");
    shading.put("Function", xref.addObject(function));
            
    RMPoint startPt = new RMPoint(), endPt = new RMPoint();
    aFill.getGradientAxis(aShape, path, startPt, endPt);
    
    // In pdf, coordinates of the gradient axis are defined in pattern space.  Pattern space is the same as the
    // page's coordinate system, and doesn't get affected by changes to the ctm. Since the RMGradient returns
    // points in the shape's coordinate system, we have to transform them into pattern space (page space).
    RMShape page = aShape.getPageShape();
    RMTransform patternSpaceTransform = aShape.getTransformToShape(page);
    patternSpaceTransform.transform(startPt);
    patternSpaceTransform.transform(endPt);
    
    // add in flip
    startPt.y = page.getFrameMaxY() - startPt.y;
    endPt.y = page.getFrameMaxY() - endPt.y;
    
    // Add the newly calculated endpoints to the shading dictionary
    List coords = new ArrayList(4);
    coords.add(new Double(startPt.getX()));
    coords.add(new Double(startPt.getY()));
    if(isRadial) {
        coords.add(new Double(0.0)); // start radius = 0
        coords.add(coords.get(0)); // end point is same as start point
        coords.add(coords.get(1));
        coords.add(new Double(endPt.distance(startPt))); // end radius is the distance between the start & end points
        shading.put("Extend", "[false true]"); // set radial shading to extend beyond end circle
    }
    else {
        coords.add(new Double(endPt.getX()));
        coords.add(new Double(endPt.getY()));
    }
    shading.put("Coords", coords);

    // Create a new pattern dictionary for the gradient
    Map pat = new Hashtable(10);
    pat.put("Type", "/Pattern");
    pat.put("PatternType", "2");
    pat.put("Shading", xref.addObject(shading)); // pat.put("Matrix", patternSpaceTransform);
    
    // Set the pattern for fills
    pdfPage.append('/').append(pdfPage.addPattern(pat)).appendln(" scn");
    
    // Write fill operator
    pdfPage.append('f');
    
    // If path winding rule odd, write odd fill operator
    if(path.getWindingRule()==RMPath.WIND_EVEN_ODD)
        pdfPage.append('*');
    
    // Write trailing newline
    pdfPage.appendln();
}
    
}