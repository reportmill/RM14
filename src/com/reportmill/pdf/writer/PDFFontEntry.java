package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import com.reportmill.graphics.RMPath;
import com.reportmill.text.*;
import java.util.*;

/**
 * An inner class to store font info.
 * There are individual entries when a font has chars beyond 255, in blocks of 256.
 */
public class PDFFontEntry {

    // The pdf file this entry is associated with
    PDFFile           _pFile;
    
    // Original Font
    RMFont            _font;
    
    // Reference string for XREF table
    String            _refString;
    
    // Name of font for PDF file
    String            _pdfName;
    
    // Chars present in base range (0-255), represented as boolean array
    boolean           _chars[];
    
    // Chars present in extended range (anything above 255), represented as char array
    List <Character>  _uchars;
    
    // The font entry
    int               _fontCharSet;
    
/**
 * Creates a new font entry for a given pdf file and font.
 */
public PDFFontEntry(PDFFile pdfFile, RMFont aFont, int fontCharSet)
{
    // Set pdf file this font entry is associated with
    _pFile = pdfFile;
    
    // Set font
    _font = aFont;
    
    // Add to PDF file and get reference string
    _refString = pdfFile._xref.addObject(this);
    
    // Set name from font PostScript name (so it will be a legal PDF name too)
    _pdfName = aFont.getPSName(); // Used to be RMStringUtils.replace(aFont.getFontNameEnglish(), " ", "-");
    
    // Set font char set
    _fontCharSet = fontCharSet;
    
    // If root char set, do stuff
    if(fontCharSet==0) {
    
        // Initialize present chars for base font (this is bogus, shouldn't write base font if not used)
        _chars = new boolean[256];
        _chars[32] = true;
        
        // Initialize present chars for extended fonts
        _uchars = new ArrayList();
    }
    
    // Otherwise do other stuff
    else {
        _pdfName += "." + fontCharSet;
        PDFFontEntry fontEntry = pdfFile.getFontEntry(aFont, 0);
        _chars = fontEntry._chars;
        _uchars = fontEntry._uchars;
    }
}

/**
 * Returns the font for this font entry.
 */
public RMFont getFont()  { return _font; }

/**
 * Returns the char set for this font entry. Font entries represent blocks of 256 chars for a given font, and the
 * char set is the index of the current font entry's block of 256 chars.
 */
public int getCharSet()  { return _fontCharSet; }

/**
 * Returns the char count for this font entry.
 */
public int getCharCount()  { return Math.min(_uchars.size() - (_fontCharSet-1)*256, 256); }

/**
 * Returns the char at the given index for this font entry.
 */
public char getChar(int anIndex)  { return _uchars.get((_fontCharSet-1)*256 + anIndex); }

/**
 * Returns the pdf name of the font entry font.
 */
public String getPDFName()  { return _pdfName; }

/**
 * Writes the font entry to the pdf buffer.
 */
public void writePDF(RMPDFWriter aWriter)
{
    if(getCharSet()==0)
        writeFont(aWriter);
    else writeExtendedFont(aWriter);
}

/**
 * Writes the given font to the given pdf file.
 */
public void writeFont(RMPDFWriter aWriter)
{
    // Get pdf xref table and pdf buffer
    PDFXTable xref = aWriter.getXRefTable();
    PDFBuffer buffer = aWriter.getBuffer();

    // Get font file and font entry
    RMFontFile fontFile = getFont().getFontFile();
    
    // If font is standard font, just declare BaseFont and return
    if(isStandardFont(fontFile)) {
        buffer.appendln("<< /Type /Font /Subtype /TrueType /Name /" + getPDFName());
        buffer.appendln("/BaseFont /" + getStandardFontName(fontFile));
        buffer.appendln("/Encoding /WinAnsiEncoding");
        buffer.append(">>");
        return;
    }

    // Write font dictionary basics
    buffer.appendln("<< /Type /Font /Subtype /Type3 /Name /" + getPDFName());

    // Write FontBBox and FontMatrix
    int y = (int)(-fontFile.getMaxDescent()*1000);
    int w = (int)(fontFile.getMaxAdvance()*1000);
    int h = (int)(fontFile.getHeight()*1000);
    buffer.appendln(" /FontBBox [0 " + y + " " + w + " " + h + "]");
    buffer.appendln(" /FontMatrix [0.001 0 0 0.001 0 0]");
    
    // Write "CharProcs" operator and dictionary opener
    buffer.append(" /CharProcs << ");
    
    // Create string buffer for char proc references
    StringBuffer encoding = new StringBuffer();
    
    // Declare variables for first and last chars present in font
    int firstChar = -1, lastChar = -1;
    
    // Declare variable for whether last char was missing
    boolean gap = true;
    
    // Iterate over char array
    for(int i=0; i<256; i++) {
        
        // If char at index is present, write glyph for it and add to encoding string
        if(_chars[i]==true) {
            
            // Write char proc for char
            String charProcRef = xref.addObject(charProcStreamForChar(fontFile, (char)i));
            
            // Write reference for char
            buffer.append('/').append(i).append(' ').append(charProcRef).append(' ');
            
            // If coming off a gap, write current index
            if(gap) {
                encoding.append(i);
                encoding.append(' ');
                gap = false;
            }
            
            // Write char index
            encoding.append('/'); encoding.append(i); encoding.append(' ');
            
            // Set first char/last char (first char only if uninitialized)
            if(firstChar<0) firstChar = i;
            lastChar = i;
        }
        
        // If char at index not present, set gap variable
        else gap = true;
    }
    
    // Remove trailing space from encoding
    if(encoding.length()>0) encoding.deleteCharAt(encoding.length()-1);
    
    // Close char procs dictionary
    buffer.appendln(">>");

    // Write Encoding
    buffer.appendln(" /Encoding << /Type /Encoding /Differences [" + encoding + "] >>");
    
    // Write FirstChar/LastChar/Widths
    buffer.appendln(" /FirstChar " + firstChar);
    buffer.appendln(" /LastChar " + lastChar);
    
    // Write widths
    buffer.append(" /Widths [");
    for(int i=firstChar; i<=lastChar; i++) {
        if(_chars[i]==true)
            buffer.append(fontFile.charAdvance((char)i)*1000);
        else buffer.append('0');
        buffer.append(' ');
    }
    buffer.appendln("]");
    
    // Close encoding
    buffer.append(">>");
}

/**
 * Writes the given font char set to the given pdf file.
 */
public void writeExtendedFont(RMPDFWriter aWriter)
{
    // Get pdf xref table and pdf buffer
    PDFXTable xref = aWriter.getXRefTable();
    PDFBuffer buffer = aWriter.getBuffer();

    // Get font file and font entry
    RMFontFile fontFile = getFont().getFontFile();
    
    // Write font dictionary basics
    buffer.appendln("<< /Type /Font /Subtype /Type3 /Name /" + getPDFName());

    // Write FontBBox and FontMatrix
    int y = (int)(-fontFile.getMaxDescent()*1000);
    int w = (int)(fontFile.getMaxAdvance()*1000);
    int h = (int)(fontFile.getHeight()*1000);
    buffer.appendln(" /FontBBox [0 " + y + " " + w + " " + h + "]");
    buffer.appendln(" /FontMatrix [0.001 0 0 0.001 0 0]");
    
    // Create CharProcs (and encoding and width) and print
    buffer.append(" /CharProcs << ");
    StringBuffer encoding = new StringBuffer("0 ");
    int lastChar = -1;
    for(int i=0, iMax=getCharCount(); i<iMax; i++) {
        char c = getChar(i);
        if(c==0)
            break;

        String charProcRef = xref.addObject(charProcStreamForChar(fontFile, c));
        buffer.append('/').append(i).append(' ').append(charProcRef).append(' ');
        encoding.append('/').append(i).append(' ');
        lastChar = i;
    }
    if(encoding.length()>0) encoding.deleteCharAt(encoding.length()-1);
    buffer.appendln(">>");
    
    // Write Encoding
    buffer.appendln(" /Encoding << /Type /Encoding /Differences [" + encoding + "] >>");
    
    // Write FirstChar & LastChar
    buffer.appendln(" /FirstChar 0");
    buffer.appendln(" /LastChar " + lastChar);
    
    // Write widths
    buffer.append(" /Widths [");
    for(int i=0, iMax=getCharCount(); i<iMax; i++) {
        char c = getChar(i);
        if(c==0)
            break;
        buffer.append(fontFile.charAdvance(c)*1000);
        buffer.append(' ');
    }
    buffer.appendln("]");

    // Close encoding
    buffer.append(">>");
}

/**
 * Returns a pdf stream buffer with given char written as a char proc.
 */
public static PDFStream charProcStreamForChar(RMFontFile fontFile, char aChar)
{
    // Get outline of given char
    RMPath path = fontFile.charPath(aChar);
    
    // Create buffer for char proc
    PDFBuffer buffer = new PDFBuffer();
    
    // Write glyph width, height, bbox
    RMRect bounds = path.getBounds2D().scaledRect(1000);
    buffer.append(bounds.width).append(' ').append(bounds.height).append(' ');
    buffer.append(bounds.x).append(' ').append(bounds.y).append(' ');
    buffer.append(bounds.getMaxX()).append(' ').append(bounds.getMaxY()).appendln(" d1");

    // Create point array for iterating over path segments
    RMPoint p[] = new RMPoint[3];
    RMPoint lastPoint = RMPoint.zeroPoint;
    
    // Iterate over path segments
    for(int i=0, iMax=path.getElementCount(); i<iMax; i++) {
        
        // Get current path segment type and points
        byte type = path.getElement(i, p);
        
        // Handle path segment types independently
        switch(type) {
        
            // Handle MOVE_TO
            case RMPath.MOVE_TO: {
                buffer.moveTo(Math.round(p[0].x*1000), Math.round(p[0].y*1000));
                lastPoint = p[0];
                break;
            }
            
            // Handle LINE_TO
            case RMPath.LINE_TO: {
                buffer.lineTo(Math.round(p[0].x*1000), Math.round(p[0].y*1000));
                lastPoint = p[0];
                break;
            }
            
            // Handle QUAD_TO
            case RMPath.QUAD_TO: {
                
                // Convert quad control point (in conjunction with path last point) to cubic bezier control points
                double cp1x = (lastPoint.x + 2*p[0].x)/3.0;
                double cp1y = (lastPoint.y + 2*p[0].y)/3.0;
                double cp2x = (2*p[0].x + p[1].x)/3.0;
                double cp2y = (2*p[0].y + p[1].y)/3.0;
                    
                buffer.curveTo(Math.round(cp1x*1000), Math.round(cp1y*1000), Math.round(cp2x*1000),
                    Math.round(cp2y*1000), Math.round(p[1].x*1000), Math.round(p[1].y*1000));
                    
                // Update last point and break
                lastPoint = p[1];
                break;
            }
                
            // Handle CURVE_TO
            case RMPath.CURVE_TO: {
                buffer.curveTo(Math.round(p[0].x*1000), Math.round(p[0].y*1000),
                    Math.round(p[1].x*1000), Math.round(p[1].y*1000),
                    Math.round(p[2].x*1000), Math.round(p[2].y*1000));
                lastPoint = p[2];
                break;
            }
                
            // Handle CLOSE
            case RMPath.CLOSE: {
                buffer.appendln("h");
                break;
            }
        }
    }
    
    // Append font char
    buffer.append('f');
    
    // Return char proc stream
    return new PDFStream(buffer.toByteArray(), null);
}

/**
 * Returns whether the given font is one of the standard PDF fonts.
 * All PDF readers are guaranteed to have the following standard fonts.
 */
static boolean isStandardFont(RMFontFile fontFile)
{
    String name = getStandardFontNameSanitized(fontFile.getPSName());
    return RMArrayUtils.contains(_pdfBuiltIns, name);
}

/**
 * Returns the standard font name for any variant of a standard font name.
 */
static String getStandardFontName(RMFontFile fontFile)
{
    String name = getStandardFontNameSanitized(fontFile.getPSName());
    int index = Math.max(0, RMArrayUtils.indexOf(_pdfBuiltIns, name));
    return _pdfBuiltIns2[index];
}

/**
 * Strips any bogus stuff from a standard font name.
 */
static String getStandardFontNameSanitized(String aName)
{
    aName = RMStringUtils.replace(aName, "New", "");
    aName = RMStringUtils.replace(aName, "Neue", "");
    aName = RMStringUtils.replace(aName, "Plain", "");
    aName = RMStringUtils.replace(aName, "Roman", "");
    aName = RMStringUtils.replace(aName, "MT", "");
    aName = RMStringUtils.replace(aName, "PS", "");
    aName = RMStringUtils.replace(aName, "Oblique", "Italic");
    aName = RMStringUtils.replace(aName, " ", "");
    aName = RMStringUtils.replace(aName, "-", "");
    return aName;
}

/**
 * Holds a list of all the PDF built in font name variants.
 */
protected static final String _pdfBuiltIns[] =
{
    "Arial", "ArialBold", "ArialItalic", "ArialBoldItalic",
    "Helvetica", "HelveticaBold", "HelveticaItalic", "HelveticaBoldItalic",
    "Times", "TimesBold", "TimesItalic", "TimesBoldItalic",
    "Courier", "CourierBold", "CourierItalic", "CourierBoldItalic",
    "Symbol", "ZapfDingbats"
};

/**
 * Holds a list of all the PDF build font names.
 */
protected static final String _pdfBuiltIns2[] =
{
    "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
    "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
    "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic",
    "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
    "Symbol", "ZapfDingbats"
};

}