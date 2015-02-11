package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.*;
import snap.util.*;

/**
 * This class represents a font for use in rich text. Currently this is necessary because Java fonts are missing
 * so much basic typographic information.
 */
public class RMFont implements XMLArchiver.Archivable {
    
    // This font's base font file
    RMFontFile         _fontFile;
    
    // This fonts point size
    double             _size;
    
    // Whether font was substituted
    boolean            _substitute;

    // Whether font not found error has already been printed
    static boolean     _fontNotFoundErrorPrinted = false;
    
    // Some common fonts (using Arial since it seems more reliable on Windows & Mac)
    static public RMFont Helvetica10;
    static public RMFont Helvetica12;
    static public RMFont Helvetica14;
    
// Moved class initialization here because we were getting some odd RMFont init errors with bogus stacktraces
static {
    
    // Load common fonts
    try {
	    Helvetica10 = new RMFont("Arial", 10f);
	    Helvetica12 = new RMFont("Arial", 12f);
	    Helvetica14 = new RMFont("Arial", 14f);
    }
    
    // Catch exceptions
    catch(Throwable t) { t.printStackTrace(); throw new RuntimeException(t); }
}

/**
 * Creates an empty font (really only used for unarchival).
 */
public RMFont()  { this(Helvetica12._fontFile, 12); }

/**
 * Creates a font for the given font file and point size.
 */
private RMFont(RMFontFile aFontFile, double aSize)  { _fontFile = aFontFile; _size = aSize; }

/**
 * Returns the font for the given name and size.
 */
public RMFont(String aName, double aSize)
{
    // Get fontFile for aName
    _fontFile = RMFontFile.getFontFile(aName);
    _size = aSize;

    // If fontFile not found, substitute Helvetica (try to get the style right)
    if(_fontFile==null) {
        _substitute = true; _fontFile = Helvetica10.getFontFile();
    
        if(!_fontNotFoundErrorPrinted) {
            System.err.println("RMFont Alert! See http://www.reportmill.com/support/docs/fonts.html");
            _fontNotFoundErrorPrinted = true;
        }

        // Complain about lost font (should put aName in HashSet and complain only once per font name)
        System.err.println("RMFont: No font found for " + aName + " (using " + _fontFile + ")");
    }
}

/**
 * Returns the font for the given name and size (substitutes Arial if not found).
 */
public static RMFont getFont(String aName, double aSize)  { return new RMFont(aName, aSize); }

/**
 * Returns the user's default font.
 */
public static RMFont getDefaultFont()  { return new RMFont(); }

/**
 * Returns the name of this font.
 */
public String getFontName()  { return _fontFile.getFontName(); }

/**
 * Returns the name of this font in English.
 */
public String getFontNameEnglish()  { return _fontFile.getFontNameEnglish(); }

/**
 * Returns the font size of this font.
 */
public double getSize()  { return _size; }

/**
 * Returns the family name of this font.
 */
public String getFamily()  { return _fontFile.getFamily(); }

/**
 * Returns the family name of this font in English.
 */
public String getFamilyEnglish()  { return _fontFile.getFamilyEnglish(); }

/**
 * Returns the PostScript name of this font.
 */
public String getPSName()  { return _fontFile.getPSName(); }

/**
 * Returns the font file for this font.
 */
public RMFontFile getFontFile()  { return _fontFile; }

/**
 * Returns the char advance for the given char.
 */
public double charAdvance(char aChar)  { return _fontFile.charAdvance(aChar)*_size; }

/**
 * Returns the kerning for the given pair of characters (no way to do this in Java!).
 */
public double charKern(char aChar1, char aChar2)  { return _fontFile.charKern(aChar1, aChar2)*_size; }

/**
 * Returns the path for a given character.
 */
public RMPath charPath(char c)  { return _fontFile.charPath(c); }

/**
 * Returns the bounds for a given character.
 */
public RMRect charBounds(char aChar)
{
    RMPath path = charPath(aChar);
    return path.getBounds2D().scaledRect(_size);
}

/**
 * Returns the horizontal distance spanned by the given string when rendered in this font.
 */
public double stringAdvance(String aString)
{
    double w = 0;
    for(int i=0, iMax=aString.length(); i<iMax; i++) w += charAdvance(aString.charAt(i));
    return w;
}

/**
 * Returns the max distance above the baseline that this font goes.
 */
public double getMaxAscent()  { return _fontFile.getMaxAscent()*_size; }

/**
 * Returns the max distance below the baseline that this font goes.
 */
public double getMaxDescent()  { return _fontFile.getMaxDescent()*_size; }

/**
 * Returns the default distance between lines for this font.
 */
public double getLeading()  { return _fontFile.getLeading()*_size; }

/**
 * Returns the height of this font.
 */
public double getHeight()  { return _fontFile.getHeight()*_size; }

/**
 * Returns the height for a line of text in this font.
 */
public double getLineHeight()  { return _fontFile.getLineHeight()*_size; }

/**
 * Returns the distance from the top of a line of text to the to top of a successive line of text.
 */
public double getLineAdvance()  { return _fontFile.getLineAdvance()*_size; }

/**
 * Returns the distance below the baseline that an underline should be drawn.
 */
public double getUnderlineOffset()  { return _fontFile.getUnderlineOffset()*_size; }

/**
 * Returns the default thickness that an underline should be drawn.
 */
public double getUnderlineThickness()  { return _fontFile.getUnderlineThickness()*_size; }

/**
 * Returns the distance above the baseline that a strikethrough should be drawn.
 */
public double getStrikethroughOffset()  { return _fontFile.getStrikethroughOffset()*_size; }

/**
 * Returns whether this font is considered bold.
 */
public boolean isBold()  { return _fontFile.isBold(); }

/**
 * Returns whether this font is considered italic.
 */
public boolean isItalic()  { return _fontFile.isItalic(); }

/**
 * Returns whether font had to substitute because name wasn't found.
 */
public boolean isSubstitute()  { return _substitute; }

/**
 * Returns if this font can display the given char.
 */
public boolean canDisplay(char aChar)  { return _fontFile.canDisplay(aChar); }

/**
 * Returns the bold version of this font.
 */
public RMFont getBold()
{
    RMFontFile ff = _fontFile.getBold();
    return ff!=null? new RMFont(ff, _size) : null;
}

/**
 * Returns the italic version of this font.
 */
public RMFont getItalic()
{
    RMFontFile ff = _fontFile.getItalic();
    return ff!=null? new RMFont(ff, _size) : null;
}

/**
 * Returns a font with the same family as the receiver but with the given size.
 */
public RMFont deriveFont(double aPointSize) { return aPointSize==_size? this : new RMFont(_fontFile, aPointSize); }

/**
 * Returns a font with the same family as the receiver but with size adjusted by given scale factor.
 */
public RMFont scaleFont(double aScale)  { return aScale==1? this : new RMFont(_fontFile, _size*aScale); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(!(anObj instanceof RMFont)) return false;
    RMFont font = (RMFont)anObj;
    
    // Check FontFile, Size
    if(font._fontFile!=_fontFile) return false;
    if(font._size!=_size) return false;
    return true; // Return true since all checks passed
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = new XMLElement("font");
    e.add("name", getFontNameEnglish());
    e.add("size", _size);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    String name = anElement.getAttributeValue("name");
    double size = anElement.getAttributeFloatValue("size");
    return getFont(name, size);
}

/**
 * Returns the font name, size and family for this font.
 */
public String toString()  { return getFontName() + " " + _size + " (" + getFamily() + ")"; }

/**
 * Returns the AWT font for this font.
 */
public Font awt()  { return _awt!=null? _awt : (_awt=_fontFile._awt.deriveFont((float)_size)); } Font _awt;

}