package com.reportmill.pdf.writer;
import com.reportmill.base.RMRect;
import java.util.*;
import snap.util.StringUtils;

/**
 * This class represents a PDF annotation (like a hyperlink).
 */
public class PDFAnnotation {
    
    // Annotation map
    protected Map _map = new Hashtable();
    
    // Highlight modes
    public static final char HighlightNone = 'N';
    public static final char HighlightInvert = 'I';
    public static final char HighlightOutline = 'O';
    public static final char HighlightPush = 'P';

/**
 * Creates a new annotation for the given rect.
 */
public PDFAnnotation(RMRect aRect)
{
    _map.put("Type", "/Annot");
    String r = "[" + (int)aRect.x + " " + (int)aRect.y + " " + (int)aRect.getMaxX() + " " + (int)aRect.getMaxY() + "]";
    _map.put("Rect", r);
}

/**
 * Sets the type of the annotation.
 */
public void setType(String s)  { _map.put("Subtype", s); }

/**
 * Sets the highlight mode of the annotaiton.
 */
public void setHighlightMode(char h)  { _map.put("H", "/" + h); }

/**
 * Sets whether the annotation has a border.
 */
public void setHasBorder(boolean b)
{
    if(b)
        _map.remove("Border");
    else _map.put("Border", "[0 0 0]");
}

/**
 * Tells the annotation to resolve page references.
 */
public void resolvePageReferences(PDFPagesTree pages, PDFXTable xref, PDFPage thisPage) { }

/**
 * Returns the annotation map.
 */
public Map getAnnotationMap()  { return _map; }

/**
 * An inner class (and annotation subclass) to support hyperlinks.
 */
public static class Link extends PDFAnnotation {
    int _page;

    public Link(RMRect aRect, String aUrl) {
        super(aRect);
        setType("/Link");
        if(aUrl.startsWith("Page:")) {
            if(aUrl.startsWith("Page:Next"))
                _page = 99999;
            else if(aUrl.startsWith("Page:Back"))
                _page = -99999;
            else _page = StringUtils.intValue(aUrl) - 1;
        }
        else {
            _page = -1;
            // add url action to annotation dictionary
            Hashtable urlAction = new Hashtable();
            urlAction.put("Type", "/Action");
            urlAction.put("S", "/URI");
            urlAction.put("URI", '(' + aUrl + ')');
            _map.put("A", urlAction);
        }
        setHasBorder(false);
    }
    
    public void resolvePageReferences(PDFPagesTree pages, PDFXTable xref, PDFPage thisPage) {
        if(_page==99999) _page = pages.indexOf(thisPage) + 1;
        if(_page==-99999) _page = pages.indexOf(thisPage) - 1;
        if(_page>=0) {
            PDFPage page = pages.getPage(_page);
            String ref = xref.getRefString(page);
            _map.put("Dest", "[" + ref + " /XYZ null null null]");
        }
    }
}

}