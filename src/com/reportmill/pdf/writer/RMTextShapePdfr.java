package com.reportmill.pdf.writer;
import com.reportmill.base.RMUtils;
import com.reportmill.shape.*;
import com.reportmill.text.*;

/**
 * This class generates PDF for an RMText. This would be a simple matter of using the PDF set-font and show-text
 *  operators, except that we need to embed PDF type3 fonts (really char paths) for all chars printed in non-standard
 *  fonts. We do this by tracking used chars in the PDFText.FontEntry class. Used chars in the ASCII range (0-255)
 *  make up a base font "Font0", while chars beyond 255 get written out as separate PDF fonts for each block of 256
 *  ("Font0.1", "Font0.2", etc.).
 */
public class RMTextShapePdfr <T extends RMTextShape> extends RMShapePdfr<T> {

/**
 * Writes a given RMShape hierarchy to a PDF file (recursively).
 */
protected void writeShape(T aTextShape, RMPDFWriter aWriter)
{
    // Do normal version
    super.writeShape(aTextShape, aWriter);
    
    // Get text shape and textLayout for text and start it
    RMTextLayout layout = aTextShape.getTextLayout(); if(layout==null) return;

    // If we couldn't render all the text in bounds, perform clip
    //if(layout.endIndex()<layout.length()) { aWriter.print("0 0 "); aWriter.print(aText.getWidth());
    //    aWriter.print(' '); aWriter.print(aText.getHeight()); aWriter.println(" re W n"); }
    
    // Get pdf page
    PDFPage pdfPage = aWriter.getPDFPage();
    
    // Flip coordinate system since pdf font transforms are flipped
    pdfPage.gsave();
    pdfPage.append("1 0 0 -1 0 ");
    pdfPage.append(aTextShape.getHeight());
    pdfPage.appendln(" cm");
    
    // Output PDF begin text operator
    pdfPage.appendln("BT");

    // Run iteration variables
    PDFPage underliner = null;
    
    // Iterate over runs
    for(RMTextRun run=layout.getRun(), lastRun=null; run!=null; lastRun=run, run=run.getNext()) {
        
        // If below text, bail
        if(run.getY()>aTextShape.getHeight()) break;
        
        // Write standard run
        writeRun(aTextShape, run, lastRun, aWriter);
        
        // If underlining, get PDFPageBuffer for underlining ops (to be added after End Text)
        if(run.isUnderlined()) {
            if(underliner == null) underliner = new PDFPage(null);
            underliner.setStrokeColor(run.getColor());
            underliner.setStrokeWidth((float)run.getUnderlineStroke());
            underliner.moveTo((float)run.getX(), (float)run.getYBaseline() - (float)run.getUnderlineY());
            underliner.lineTo((float)run.getMaxX(), (float)run.getYBaseline() - (float)run.getUnderlineY());
            underliner.appendln("S");
        }
    }
    
    // End Text
    pdfPage.appendln("ET");
    
    // Restore unflipped transform
    pdfPage.grestore();
    
    // If there was any underlining, add underlining ops
    if(underliner != null)
        pdfPage.append(underliner);
}

/**
 * Writes the given text run.
 */
public void writeRun(RMTextShape aText, RMTextRun aRun, RMTextRun aLastRun, RMPDFWriter aWriter)
{
    // Get pdf page
    PDFPage pPage = aWriter.getPDFPage();
    
    // If colorChanged, have writer setFillColor
    if(aRun.getColorChanged())
        pPage.setFillColor(aRun.getColor());
    
    // Get last x & y
    double lastX = aLastRun==null? 0 : aLastRun.getX();
    double lastY = aLastRun==null? aText.getHeight() : aLastRun.getYBaseline();
        
    // Set the current text point
    double runX = aRun.getX() - lastX;
    double runY = lastY - aRun.getYBaseline(); // Flip y coordinate
    pPage.append(runX).append(' ').append(runY).appendln(" Td");
    
    // Get current run font, whether FontChanged and current font entry (base font entry for font, if font has changed)
    RMFont font = aRun.getFont();
    boolean fontChanged = aRun.getFontChanged();
    PDFFontEntry fontEntry = fontChanged? aWriter.getFontEntry(font, 0) : aWriter.getFontEntry();
    
    // If char spacing has changed, set charSpace
    if(aRun.getCharSpacing() != (aLastRun==null? 0 : aLastRun.getCharSpacing())) {
        pPage.append(aRun.getCharSpacing());
        pPage.appendln(" Tc");
    }
    
    // If run outline has changed, configure text rendering mode
    if(!RMUtils.equals(aRun.getOutline(), aLastRun==null? null : aLastRun.getOutline())) {
        RMXString.Outline outline = aRun.getOutline();
        if(outline==null)
            pPage.appendln("0 Tr");
        else {
            pPage.setStrokeColor(aRun.getColor());
            pPage.setStrokeWidth(outline.getStrokeWidth());
            if(outline.getFillColor()!=null) {
                pPage.setFillColor(outline.getFillColor());
                pPage.appendln("2 Tr");
            }
            else pPage.appendln("1 Tr");
        }
    }
    
    // Get length - just return if zero
    int length = aRun.length(); if(length==0) return;
    
    // Iterate over run chars
    for(int i=0; i<length; i++) { char c = aRun.charAt(i);
        
        // If char is less than 256, just mark it present in fontEntry chars
        if(c<256) {
            fontEntry._chars[c] = true;
            if(fontEntry.getCharSet()!=0) {
                fontChanged = true;
                fontEntry = aWriter.getFontEntry(font, 0);
            }
        }
        
        // If char beyond 255, replace c with its index in fontEntry uchars array (add it if needed)
        else {
            
            // Get index of chars
            int index = fontEntry._uchars.indexOf(c);
            
            // If char not found, add it
            if(index<0) {
                index = fontEntry._uchars.size();
                fontEntry._uchars.add(c);
            }
            
            // If char set changed, reset font entry
            if(fontEntry.getCharSet() != index/256 + 1) {
                fontChanged = true;
                fontEntry = aWriter.getFontEntry(font, index/256 + 1);
            }
            
            // Replace char with index
            c = (char)(index%256);
        }
        
        // If font changed, end current text show block, set new font, and start new text show block
        if(fontChanged) {
            if(i>0) pPage.appendln(") Tj");
            pPage.append('/'); pPage.append(fontEntry.getPDFName());
            pPage.append(' '); pPage.append(font.getSize()); pPage.appendln(" Tf");
            pPage.append('(');
            aWriter.setFontEntry(fontEntry);
            fontChanged = false;
        }
        
        // If first char, open paren
        else if(i==0)
            pPage.append('(');
        
        // Handle special chars for PDF string (might need to do backspace (\b) and form-feed (\f), too)
        if(c=='\t') { if(aWriter.getIncludeNewlines()) pPage.append("\\t"); continue; }
        if(c=='\n') { if(aWriter.getIncludeNewlines()) pPage.append("\\n"); continue; }
        if(c=='\r') { if(aWriter.getIncludeNewlines()) pPage.append("\\r"); continue; }
        if(c=='(' || c==')' || c=='\\')
            pPage.append('\\');
            
        // Write the char
        pPage.append(c);
    }
    
    // If run is hyphenated, add hyphen
    if(aRun.isHyphenated()) pPage.append('-');
    
    // End last text show block
    pPage.appendln(") Tj");
}

}