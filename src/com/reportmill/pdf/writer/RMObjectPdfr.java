package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class and its subclasses write PDF for their corresponding base class.
 */
public class RMObjectPdfr<T> {

    // A map of pdfr objects for distinct classes
    static Map      _pdfrs = new HashMap();

/**
 * Writes a given object to a PDF file.
 */
public void writePDF(T anObj, RMPDFWriter aWriter) { }

/**
 * Returns the pdfr object for a given object (or class).
 */
public static RMObjectPdfr getPdfr(Object anObj)
{
    // Get pdfr from pdfr map - just return if present
    Class clss = RMClassUtils.getClass(anObj);
    RMObjectPdfr pdfr = (RMObjectPdfr)_pdfrs.get(clss);
    if(pdfr==null)
        _pdfrs.put(clss, pdfr = createPdfr(clss));
    return pdfr;
}

/**
 * Returns the pdfr object for a given object (or class).
 */
private static RMObjectPdfr createPdfr(Class aClass)
{
    // Get pdfr class from pdf package
    String cname = aClass.getSimpleName();
    Class pdfrClass = RMClassUtils.getClassForName("com.reportmill.pdf.writer." + cname + "Pdfr");
    
    // If not found, try inner class
    if(pdfrClass==null)
        pdfrClass = RMClassUtils.getClassForName(aClass.getName() + "$" + "Pdfr");
    
    // If not found, try same package
    if(pdfrClass==null)
        pdfrClass = RMClassUtils.getClassForName(aClass.getName() + "Pdfr");
        
    // If pdfr class found, instatiate pdfr class
    if(pdfrClass!=null)
        try { return (RMObjectPdfr)pdfrClass.newInstance(); }
        catch(Exception ie) { ie.printStackTrace(); }
        
    // Otherwise, get pdfr class for superclass
    return getPdfr(aClass.getSuperclass());
}

}