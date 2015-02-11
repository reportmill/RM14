package com.reportmill.text;
import com.reportmill.base.RMStringUtils;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Provides some utility methods for Fonts.
 */
public class RMFontUtils {

    // Fonts - caches requested font names for fast successive lookups
    static Map            _fontCache = new Hashtable();
    
    // The array of system fonts
    static Font[]         _fonts;

    // Cached list of all AWT font names
    static String[]       _fontNames;
    
    // Cached list of all AWT font family names
    static String[]       _familyNames;
    
    // A list of all fonts
    static List <RMFont>  _altFonts;
    
/**
 * Returns a Font for a given name and size.
 */
public static Font getFont(String aName, float aSize)
{
    // Get font from fonts map, if not in map, guess font for name and put in map
    Font font = (Font)_fontCache.get(aName);
    if(font==null)
        _fontCache.put(aName, font = guessFont(aName));
    return font.deriveFont(aSize); // Return font adjusted for requested size
}

/**
 * Returns the array of system fonts.
 */
public static Font[] getFonts()
{
    return _fonts!=null? _fonts : (_fonts=GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
}

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public static synchronized String[] getFontNames()
{
    // If fontNames has been loaded already, just return it
    if(_fontNames!=null) return _fontNames;
    
    // Get system fonts and create list for font names and family names
    Font fonts[] = RMFontUtils.getFonts();
    List fontNames = new ArrayList(fonts.length);
    List familyNames = new Vector(fonts.length/2);
    
    // Iterate over fonts
    for(int i=0; i<fonts.length; i++) {
        
        // Get current loop font name and family name
        String name = fonts[i].getFontName();
        String family = fonts[i].getFamily();
        
        // skip fonts with bad names
        if(RMStringUtils.isEmpty(name) || RMStringUtils.isEmpty(family))
            continue;
        
        // If font name or family name doesn't start with a letter, skip this font
        if(!Character.isLetter(name.charAt(0)) || !Character.isLetter(family.charAt(0)))
            continue;
        
        // If font name hasn't been encountered yet, add it to list
        if(!fontNames.contains(name))
            fontNames.add(name);
            
        // If family name hasn't been encountered yet, add it to list
        if(!familyNames.contains(family))
            familyNames.add(family);
    }
    
    // Get String array for font names and sort
    _fontNames = (String[])fontNames.toArray(new String[fontNames.size()]);
    Arrays.sort(_fontNames);
    
    // Get String array for family names and sort
    _familyNames = (String[])familyNames.toArray(new String[familyNames.size()]);
    Arrays.sort(_familyNames);

    // Return font names
    return _fontNames;
}

/**
 * Returns a list of all system family names.
 */
public static String[] getFamilyNames()
{
    if(_familyNames==null) getFontNames();
    return _familyNames;
}

/**
 * Returns a list of all font names for a given family name.
 */
public static String[] getFontNames(String aFamilyName)
{
    // Get system fonts and create new list for font family
    Font fonts[] = RMFontUtils.getFonts();
    List family = new Vector();
    
    // Iterate over fonts
    for(int i=0; i<fonts.length; i++) {
        
        // Get current loop font name and family name
        String name = fonts[i].getFontName();
        String fam = fonts[i].getFamily();
        
        // If family name is equal to given family name, add font name
        if(fam.equals(aFamilyName) && !family.contains(name))
            family.add(name);
    }
    
    // Get font names as array and sort
    String familyArray[] = (String[])family.toArray(new String[family.size()]);
    Arrays.sort(familyArray);
    
    // Return list for font family
    return familyArray;
}

/**
 * Returns the system font with the most similar name to the given name.
 */
public static Font guessFont(String aName)
{
    // Get normalized font name and array of system fonts
    String name = getFontNameNormalized(aName);
    Font fonts[] = getFonts();

    // Iterate over system fonts and if one has same name, return it
    for(int i=0; i<fonts.length; i++) {
        String fontName = getFontNameNormalized(fonts[i].getFontName(Locale.ENGLISH));
        if(name.equals(fontName))
            return fonts[i];
    }

    // Declare variable for guess font, maximum matches found and minNameLengthDelta
    Font guessFont = null;
    int maxMatches = 0, minNameLengthDelta = 999;
    
    // Iterate over all fonts to see if one has similar name
    for(int i=0, iMax=fonts.length; i<iMax; i++) {
        
        // Get font name normalized and break it into pieces
        String fName = fonts[i].getFontName(Locale.ENGLISH);
        String fontName = getFontNameNormalized(fName);
        String pieces[] = fontName.split(" ");
        
        // Declare vars for determining relevance
        boolean substantialMatch = false;
        int matches = 0;
        
        // Iterate over pieces of current font name
        for(int j=0, jMax=pieces.length; j<jMax; j++) {
            
            // Get current piece (just skip it if zero length)
            String piece = pieces[j];
            if(piece.length()==0)
                continue;

            // If piece is found in font name, increment pieces count and possibly declare substantial match
            if(aName.indexOf(piece)>=0) {
                
                // Just skip out if piece is part of another piece
                if((piece.equalsIgnoreCase("Bold") ||
                    piece.equalsIgnoreCase("Italic") ||
                    piece.equalsIgnoreCase("Oblique")) &&
                    (aName.indexOf("BoldItalic")>0 || aName.indexOf("BoldOblique")>0))
                    break;

                // If piece isn't common descriptor, mark substantial match true
                if(!(piece.equalsIgnoreCase("Regular") ||
                    piece.equalsIgnoreCase("Medium") ||
                    piece.equalsIgnoreCase("Bold") || 
                    piece.equalsIgnoreCase("Italic") ||
                    piece.equalsIgnoreCase("Oblique") ||
                    piece.equalsIgnoreCase("BoldItalic") ||
                    piece.equalsIgnoreCase("BoldOblique") ||
                    piece.equalsIgnoreCase("Rounded")))
                    substantialMatch = true;
                
                // Increment found pieces counter
                matches++;
            }
        }
        
        // Calculate name length difference
        int nameLengthDelta = Math.abs(aName.length() - fontName.length());

        // If we found a substantial piece and this is highest pieces/missingPieces count, cache ttfName et. al.
        if(substantialMatch && (matches>maxMatches || (matches==maxMatches && nameLengthDelta<minNameLengthDelta))) {
            guessFont = fonts[i];
            maxMatches = matches;
            minNameLengthDelta = nameLengthDelta;
        }
    }
    
    // If guessFont is still null, return something
    if(guessFont==null) {
        guessFont = new Font(aName, Font.PLAIN, 1000);
        if(!name.equals(getFontNameNormalized(guessFont.getFontName())) &&
           !name.equals(getFontNameNormalized(guessFont.getFontName(Locale.ENGLISH))) &&
           !name.equals(getFontNameNormalized(guessFont.getFamily())) &&
           !name.equals(getFontNameNormalized(guessFont.getFamily(Locale.ENGLISH))) &&
           !name.equals(getFontNameNormalized(guessFont.getPSName())))
           System.err.println("RMFontUtils: Couldn't find font for " + aName + " (using " + guessFont.getFontName() + ")");
    }
    
    // Return guess font
    return guessFont;
}

/**
 * Returns a "cleaned up" or standardized version of the given font name:
 *   1. Remove MT or MS or PS
 *   2. Convert all non alpha numeric characters (essentially just dashes?) to spaces
 *   3. Add space between any adjacent pair of lower-case:upper-case chars
 */
public static String getFontNameNormalized(String aName)
{
    // 1. Remove MS, MT and PS
    aName = RMStringUtils.delete(aName, "MS");
    aName = RMStringUtils.delete(aName, "MT");
    aName = RMStringUtils.delete(aName, "PS");
    
    // Get string buffer for name
    StringBuffer name = new StringBuffer(aName);
    
    // Iterate over chars
    for(int i=0; i<name.length()-1; i++) {
        
        // 2. Convert any non alpha-numeric characters to space
        if(!Character.isLetterOrDigit(name.charAt(i)))
            name.setCharAt(i, ' ');
        
        // 3. Add space between any adjacent pair of camel case chars
        if(Character.isLetterOrDigit(name.charAt(i)) && Character.isLowerCase(name.charAt(i)) &&
            Character.isLetterOrDigit(name.charAt(i+1)) && Character.isUpperCase(name.charAt(i+1)))
            name.insert(i+1, ' ');
        
        // Coalesce adjacent space
        if(name.charAt(i)==' ' && name.charAt(i+1)==' ') {
            name.deleteCharAt(i+1);
            i--;
        }
    }
    
    // Return name
    return name.toString().trim();
}

/**
 * Returns an alternate font for given char, if one is found that can display it
 */
public static RMFont getAltFont(char aChar)
{
    // Iterate over alternate fonts and return first that can display
    for(int i=0, iMax=getAltFonts().size(); i<iMax; i++) { RMFont font = getAltFonts().get(i);
        if(font.canDisplay(aChar))
            return font; }
    return null; // Return null since alternate font not found
}

/**
 * Returns the list of suggested alternate fonts.
 */
public static List <RMFont> getAltFonts()  { return _altFonts!=null? _altFonts : (_altFonts=createAltFonts()); }

/**
 * Returns the list of suggested alternate fonts.
 */
static List <RMFont> createAltFonts()
{
    // Yuichi recommended:
    // Japanese, Simplified Chinese, Traditional Chinese, Korean
    // Windows: "MS Gothic", "MS Hei", "MingLiU", "Gulimche"
    // OSX: "Hiragino Kaku Go-W3", "Hei", "Apple LiGothic", "Apple Gothic"
    //String names[] = { "Symbol", "Song", "MS Gothic", "Hiragino Kaku Gothic Pro", "MS Hei", "Hei", "MingLiU",
    //    "Apple LiGothic", "Gulimche", "Apple Gothic", "Song", "Lucida Grande" };
    //for(int i=0; i<names.length; i++) { RMFont f = RMFont.getFont(names[i], 12, false);
    //    if(f!=null && !_altFonts.contains(f)) _altFonts.add(f); }
    
    // Get all system fonts and create alt fonts list with RMFont for each system font
    Font fonts[] = RMFontUtils.getFonts();
    List <RMFont> altFonts = new Vector(fonts.length);
    for(int i=0; i<fonts.length; i++)
        altFonts.add(RMFont.getFont(fonts[i].getFontName(), 12));
    
    // Make sure Symbol is the first font and return
    altFonts.add(0, RMFont.getFont("Symbol", 12));
    return altFonts;
}

/**
 * Returns whether two fonts are equal.
 */
public static boolean equals(Font f1, Font f2)
{
    // Check identity, nulls
    if(f1==f2) return true;
    if(f1==null || f2==null) return false;
    
    // Check normalized names
    String f1name = getFontNameNormalized(f1.getFontName(Locale.ENGLISH));
    String f2name = getFontNameNormalized(f2.getFontName(Locale.ENGLISH));
    if(!f1name.equals(f2name)) return false;
    
    // Check Size, Style
    if(f1.getSize2D()!=f2.getSize2D()) return false;
    if(f1.getStyle()!=f2.getStyle()) return false;
    return true; // Return true since all checks passed
}

/**
 * Starts loading fonts in the background and returns.
 */
public static void startFontLoadingInBackground()
{
    // Start font loading in background (hopefully this will finish before any text operations)
    try {
        Thread fontLoader = new Thread() { public void run() { getFonts(); }};
        fontLoader.setPriority(Thread.MIN_PRIORITY);
        fontLoader.start();
    }
    
    // Catch exceptions
    catch(Exception e) { System.err.println("RMFontUtils:InitFonts: Error loading system fonts"); }
}

}