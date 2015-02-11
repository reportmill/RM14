package com.reportmill.text;
import com.reportmill.graphics.RMColor;
import java.awt.Color;
import java.io.*;
import java.util.Enumeration;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * Parses rtf data from a string and returns it as an xstring.
 */
public class RMRTFParser {

/**
 * Returns an xstring from the given rtf string and default font.
 */
public static RMXString parse(String rtf, RMFont baseFont)
{
    // Create new xstring
    RMXString result = new RMXString();
    
    // Catch excpetions
    try {
        
        // We'll use RTFEditorKit to do the real parsing work
        EditorKit kit = new RTFEditorKit();
        Document doc = kit.createDefaultDocument();
        Reader reader = new StringReader(rtf);
        kit.read(reader, doc, 0);
        
        // Now we'll walk through the document and piece together our XString
        ElementIterator elemIterator = new ElementIterator(doc);
        AbstractDocument.AbstractElement elem;
        
        // Declare loop attribute variables
        RMFont font = baseFont;
        RMColor color = null;
        boolean underline = false;
        
        // Iterate over rtf elements
        while((elem = (AbstractDocument.AbstractElement)elemIterator.next()) != null) {
            
            // Handle content element
            if(elem.getName().equals("content")) {
                
                // Get content string
                String content = doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());

                // Iterate over attribute names
                for(Enumeration e=elem.getAttributeNames(); e.hasMoreElements();) {
                
                    // Get attribute and attribute name
                    Object attr = e.nextElement();
                    String attrName = attr.toString();
                    
                    // Handle bold
                    if(attrName=="bold" && ((Boolean)elem.getAttribute(attr)).booleanValue() && font.getBold()!=null)
                        font = font.getBold();
                    
                    // Handle italic
                    if(attrName=="italic" && ((Boolean)elem.getAttribute(attr)).booleanValue() && font.getItalic()!=null)
                        font = font.getItalic();

                    // Handle underline
                    if(attrName=="underline")
                        underline = ((Boolean)elem.getAttribute(attr)).booleanValue();
                    
                    // Handle foreground
                    if(attrName=="foreground") {
                        Color c = (Color)elem.getAttribute(attr);
                        color = new RMColor(c);
                    }
                    
                    // Handle size
                    if(attrName=="size") {
                        int size = ((Integer)elem.getAttribute(attr)).intValue();
                        font = font.deriveFont(size);
                    }
                    
                    // Handle font family
                    if(attrName=="family") {
                        String fontName = (String)elem.getAttribute(attr);
                        RMFont f = new RMFont(fontName, font.getSize());
                        if(!f.isSubstitute())
                            font = f;
                    }
                }
                
                // Create new xstring for rtf run (string, font, color, underline) and add
                RMXString xstring = new RMXString(content, font, color);
                xstring.setUnderlined(underline);
                result.addString(xstring, result.length());
                
                // Reset font, color & underline
                font = baseFont;
                color = null;
                underline = false;
            }
        }
    }
    
    // Catch exceptions
    catch (Exception e) { e.printStackTrace(); }
    
    // Return rtf xstring
    return result;
}

}