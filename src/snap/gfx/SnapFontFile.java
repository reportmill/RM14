package snap.gfx;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.util.*;
import snap.util.SnapUtils;

/**
 * This class represents all the information about a font that is independent of size. This allows SnapFont to be 
 * lighter weight (essentially just a font file at a given size).
 */
public class SnapFontFile {
    
    // Cached font name
    String              _fontName;
    
    // Cached font name in English
    String              _fontNameEnglish;
    
    // Cached "canDisplay" bitset
    BitSet              _canDisplay = new BitSet(128);
    
    // Cached glyph paths
    Map                 _glyphPaths = new Hashtable();
    
    // Cached reference to bold version of font
    SnapFontFile        _boldVersion = null;
    
    // Cached reference to italic version of font
    SnapFontFile        _italicVersion = null;
    
    // Cache of char widths
    float               _charWidths[] = new float[0];
    
    // Cached map of all previously encountered font files
    static Map          _allFontFiles = new Hashtable();
    
    // This font files AWT representation
    Font                _awt;
    
    // Cached font metrics
    FontMetrics         _fontMetrics;

    // Cached graphics object
    static Graphics2D   _graphics2D;

/**
 * Returns a font file for a given font name.
 */
public static synchronized SnapFontFile getFontFile(String aName)
{
    SnapFontFile ffile = (SnapFontFile)_allFontFiles.get(aName);
    if(ffile==null) _allFontFiles.put(aName, ffile = new SnapFontFile(aName));
    return ffile;
}

/**
 * Creates a font file for a given font name.
 */
private SnapFontFile(String aName)
{
    // Get AWT font for given name and FontMetrics for font
    _awt = SnapFontUtils.getFont(aName, 1000f);
    _fontMetrics = getGraphics2D().getFontMetrics(_awt);

    // Cache font name and English name, so we have normalized versions
    _fontName = SnapFontUtils.getFontNameNormalized(_awt.getFontName());
    _fontNameEnglish = SnapFontUtils.getFontNameNormalized(_awt.getFontName(Locale.ENGLISH));
}

/**
 * Returns the name of this font.
 */
public String getFontName()  { return _fontName; }

/**
 * Returns the name of this font in English.
 */
public String getFontNameEnglish()  { return _fontNameEnglish; }

/**
 * Returns the family name of this font.
 */
public String getFamily()  { return _awt.getFamily(); }

/**
 * Returns the family name of this font in English.
 */
public String getFamilyEnglish()  { return _awt.getFamily(Locale.ENGLISH); }

/**
 * Returns the PostScript name of this font.
 */
public String getPSName()  { return _awt.getPSName(); }

/**
 * Returns the char advance for the given char.
 */
public double charAdvance(char aChar)
{
    // If char in cache range, load from cache (might have to load cache too)
    if(aChar<_charWidths.length) {
        double charWidth = _charWidths[aChar];
        if(charWidth<0)
            charWidth = _charWidths[aChar] = _fontMetrics.charWidth(aChar)/1000f;
        return charWidth;
    }
    
    // Extend cache if less than CharWidthsLength (1200 + 256, 1456)
    if(aChar<1456) {
        int newLen = _charWidths.length==0 && aChar<256? 256 : 1456;
        float newWidths[] = new float[newLen];
        System.arraycopy(_charWidths, 0, newWidths, 0, _charWidths.length);
        Arrays.fill(newWidths, _charWidths.length, newWidths.length, -1); _charWidths = newWidths;
        return charAdvance(aChar);
    }
    
    // Get value straight from FontMetrics
    return _fontMetrics.charWidth(aChar)/1000d;
}

/**
 * Returns the kerning for the given pair of characters (no way to do this in Java!).
 */
public double charKern(char aChar1, char aChar2)  { return 0; }

/**
 * Returns the max distance above the baseline that this font goes.
 */
public double getMaxAscent()  { return _fontMetrics.getMaxAscent()/1000f; }

/**
 * Returns the max distance below the baseline that this font goes.
 */
public double getMaxDescent()  { return _fontMetrics.getMaxDescent()/1000f; }

/**
 * Returns the default distance between lines for this font.
 */
public double getLeading()  { return _fontMetrics.getLeading()/1000f; }

/**
 * Returns the height of this font file.
 */
public double getHeight()  { return getMaxAscent() + getMaxDescent(); }

/**
 * Returns the height of a line of text in this font.
 */
public double getLineHeight()  { return getMaxAscent() + getMaxDescent(); }

/**
 * Returns the distance from the top of a line of text to the to top of a successive line of text.
 */
public double getLineAdvance()  { return getMaxAscent() + getMaxDescent() + getLeading(); }

/**
 * Returns the max advance of characters in this font.
 */
public double getMaxAdvance()  { return _fontMetrics.getMaxAdvance()/1000f; }

/**
 * Returns the distance below the baseline that an underline should be drawn.
 */
public double getUnderlineOffset()  { return -getMaxDescent()/2; }

/**
 * Returns the default thickness that an underline should be drawn.
 */
public double getUnderlineThickness()
{
    // In Java 5 , Font LineMetrics getUnderlineThickness returns bogusly small value
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6216010
    if(SnapUtils.isJava5)
        return 1/16f;
    
    // Get the AWT Font's LineMetrics for X
    LineMetrics lm = _awt.getLineMetrics("X", getGraphics2D().getFontRenderContext());
    
    // Return underline thickness for line metrics
    return lm.getUnderlineThickness()/1000f;
}

/**
 * Returns the distance above the baseline that a strikethrough should be drawn.
 */
public double getStrikethroughOffset()  { return getMaxAscent()/2; }

/**
 * Returns whether this font is considered bold.
 */
public boolean isBold()  { return getFontNameEnglish().indexOf("Bold")>0; }

/**
 * Returns whether this font is considered italic.
 */
public boolean isItalic()
{
    return getFontNameEnglish().indexOf("Italic")>0 || getFontNameEnglish().indexOf("Oblique")>0;
}

/**
 * Returns if this font can display the given char.
 */
public boolean canDisplay(char aChar)
{
    // If aChar is set in _canDisplay bitset, return true
    if(_canDisplay.get(aChar))
        return true;
    
    // Get AWT Font canDisplay (not sure I need all the extra checks)
    boolean canDisplay = _awt.canDisplay(aChar) ||
        aChar=='\n' || aChar=='\r' || aChar=='\t' || aChar==' ' ||
        (aChar<256 && (getFontNameEnglish().startsWith("Wingdings") || getFontNameEnglish().startsWith("Webdings"))); 
        
    // If true and less than 128, set in bitset
    if(canDisplay && aChar<128)
        _canDisplay.set(aChar);
    
    // Return can display
    return canDisplay;
}

/**
 * Returns the bold version of this font.
 */
public SnapFontFile getBold()
{
    // If bold version hasn't been loaded yet, load it
    if(_boldVersion==null) {
        
        // Get list of font names in this font's family
        String familyNames[] = SnapFontUtils.getFontNames(getFamily());
        
        // Declare variable for "match factor"
        int matchFactor = 0;
    
        // Iterate over font names
        for(int i=0, iMax=familyNames.length; i<iMax; i++) { String fontName = familyNames[i];
            
            // Get font file for font name
            SnapFontFile fontFile = getFontFile(fontName);
            
            // If this font differs in boldness...
            if(isBold()!=fontFile.isBold()) {
                
                // Really weight matchFactor for versions that match italic condition
                int newMF = isItalic()==fontFile.isItalic()? 1000 : 0;
                
                // Weight matchFactor for matching words (+10 for matching words, -1 for missing words)
                newMF += matchingWords(getFontName(), fontFile.getFontName());

                if(newMF>matchFactor) {
                    matchFactor = newMF;
                    _boldVersion = fontFile;
                }
            }
        }
        
        // If bold version wasn't found, set bold version to this font (so we'll know we looked)
        if(_boldVersion==null)
            _boldVersion = this;
    }
    
    // Return bold version (or null if version is this font)
    return _boldVersion==this? null : _boldVersion;
}

/**
 * Returns the italic version of this font.
 */
public SnapFontFile getItalic()
{
    // If italic version hasn't been loaded yet, load it
    if(_italicVersion==null) {
        
        // Get list of font names in this font's family
        String list[] = SnapFontUtils.getFontNames(getFamily());
        
        // Declare variable for "match factor
        int matchFactor = 0;
    
        // Iterate over font names
        for(int i=0, iMax=list.length; i<iMax; i++) { String fn = list[i];
            
            // Get font file for font name
            SnapFontFile fontFile = getFontFile(fn);
            
            // If this font differs in italicness...
            if(isItalic()!=fontFile.isItalic()) {
                
                // Definitely weight matchFactor for versions that match bold condition
                int newMF = isBold()==fontFile.isBold()? 1000 : 0;
                
                // Weight matchFactor for matching words (+10 for matching words, -1 for missing words)
                newMF += matchingWords(getFontName(), fontFile.getFontName());

                if(newMF>matchFactor) {
                    matchFactor = newMF;
                    _italicVersion = fontFile;
                }
            }
        }
        
        // If italic version wasn't found, set italic version to this font (so we'll know we looked)
        if(_italicVersion==null)
            _italicVersion = this;
    }
    
    // Return italic version (or null if version is this font)
    return _italicVersion==this? null : _italicVersion;
}

/**
 * Returns the font name of this font file.
 */
public String toString()  { return getFontName(); }

/**
 * Returns a shared graphics objects that can be used to get a font render context.
 */
static synchronized Graphics2D getGraphics2D()
{
    if(_graphics2D==null) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.addRenderingHints(rh);
        _graphics2D = g2;
    }
    return _graphics2D;
}

/**
 * Utility method to determine the number of matching words in two phrases.
 */
private int matchingWords(String s1, String s2)
{
    String ls = s1.length()>s2.length()? s1 : s2;
    String ss = ls==s1? s2 : s1;
    int sc = 0, ec = 1, mwc = 0;
    
    while(ec<=ls.length()) {
        char ecc = ec<ls.length()? ls.charAt(ec) : ' ';
        if(Character.isUpperCase(ecc) || (ecc==' ') || (ecc=='-')) {
            String word = ls.substring(sc, ec);
            if(ss.indexOf(word)>=0) mwc += 10;
            else mwc--;
            sc = Character.isUpperCase(ecc)? ec : ec + 1;
            ec = sc;
        }
        ec++;
    }
    
    return mwc;
}

}