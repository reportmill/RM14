package com.reportmill.text;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.util.*;
import snap.util.XMLElement;

/**
 * This class provides a method to perform @key@ substitution on an xstring.
 */
public class RMXStringUtils implements RMTextTypes {

/**
 * Returns a List of XString substrings of this XString that are separated by the given separator String.
 */
public static RMXString[] separate(RMXString aString, String aSeparator)
{
    // Create list for separated strings
    List <RMXString> list = new Vector();

    // While instances of aSeparator are found, add preceding characters
    int start = 0;
    for(int index=aString.indexOf(aSeparator); index>=0; index=aString.indexOf(aSeparator, start)) {
        if(index>start)
            list.add(aString.substring(start, index));
        start = index + aSeparator.length();
    }

    // If remainder, add it
    if(start<aString.length())
        list.add(aString.substring(start, aString.length()));
    
    // Return list
    return list.toArray(new RMXString[list.size()]);
}

/**
 * Replaces any occurrence of consecutive newlines with a single newline.
 */
public static void coalesceNewlines(RMXString aString)
{
    // Get string
    String string = aString.toString();
    
    // Iterate over occurrences of adjacent newlines (from back of string to font) and replace with single newline
    for(int start=string.lastIndexOf("\n\n"); start>=0; start=string.lastIndexOf("\n\n", start)) {
        int end = start + 2;
        while(start>0 && string.charAt(start-1)=='\n') start--;
        aString.replaceChars("\n", start, end);
        string = aString.toString();
    }
    
    // Also remove leading newline if present
    if(aString.length()>0 && aString.charAt(0)=='\n')
        aString.removeChars(0, 1);
}

/**
 * Sets a value to that should be multiplied times all font sizes in this string.
 */
public static void scaleFonts(RMXString aString, float aScale)
{
    // If scale is 1, just return
    if(aScale==1) return;
    
    // Iterate over runs and change fonts
    for(int i=0, iMax=aString.getRunCount(); i<iMax; i++) { RMXStringRun run = aString.getRun(i);
        aString.setAttribute(run.getFont().scaleFont(aScale), run.start(), run.end()); }
}

/**
 * Returns a blended version of the receiver and the given string (0 is receiver, 1 is given string). The current
 * implementation actually only blends text color.
 */
public static RMXString blend(RMXString aString1, RMXString aString2, float fraction)
{
    // Handle end cases 0 & 1
    if(fraction==0) return aString1; if(fraction==1) return aString2;
    
    // Real blending
    RMColor c1 = aString1.getRunAt(0).getColor(), c2 = aString2.getRunAt(0).getColor(), c3 = c1.blend(c2, fraction);
    RMXString copy = aString1.clone();
    copy.setAttribute(c3);
    return copy;
}

/**
 * Returns an XML style string representation of the attributes and string runs in this XString.
 */
public static String toStringXML(RMXString aString)
{
    // Get XML element and add child elements to string buffer
    XMLElement e = aString.toXML(null);
    StringBuffer sb = new StringBuffer();
    for(int i=0, iMax=e.size(); i<iMax; i++) e.get(i).write(sb, 0, "");
    return sb.toString();
}

/** Characters are copied from this sequence into the destination character array dst. */
/*public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
    // Handle common case
    if(srcBegin==0 && srcEnd==length())
        for(int i=0, iMax=getRunCount(), start=0; i<iMax; i++) { RMXStringRun run = getRun(i);
            run.getCharBuffer().getChars(0, run.length(), dst, dstBegin + start); start += run.length(); }
    // Handle everything else
    else toString().getChars(srcBegin, srcEnd, dst, dstBegin); }*/

/**
 * Returns a version of this string that substitutes alternate fonts for any characters that cannot be displayed in 
 * their associated fonts (simply returns the receiver if all characters are valid).
 */
public static RMXString getRepresentableString(RMXString aString)
{
    // Set representable string to this
    RMXString rstring = aString;
    
    // Iterate over runs
    for(int i=0; i<rstring.getRunCount(); i++) { RMXStringRun run = rstring.getRun(i); RMFont font = run.getFont();
    
        // Iterate over run chars
        for(int j=0, jMax=run.length(); j<jMax; j++) { char c = run.charAt(j);
             
            // If font can't display char, fix it
            if(!font.canDisplay(c)) {
                 
                // If first invalid char, clone string
                if(rstring==aString) {
                    rstring = aString.clone();
                    run = rstring.getRun(i);
                }
                 
                // If character is control character, replace with space (increment start)
                if(Character.isISOControl(c))
                    rstring._sb.setCharAt(run.start() + j, ' ');
                 
                // Otherwise, search for alt font
                else {

                    // Get alternate font for char
                    RMFont altFont = RMFontUtils.getAltFont(c);
                     
                    // If alternate font found, reset similar chars in run to font
                    if(altFont!=null) {
                        int j2 = j+1, runStart = run.start();
                        while(j2<jMax && !font.canDisplay(run.charAt(j2)) && altFont.canDisplay(run.charAt(j2))) j2++;
                        rstring.setAttribute(TEXT_FONT, altFont, runStart+j, runStart+j2);
                        j = jMax;
                    }
                     
                    // Otherwise complain and replace
                    else {
                        System.err.println("RMXString: Couldn't find font for char " + c + "(" + ((int)c) + ")");
                        rstring._sb.setCharAt(run.start() + j, ' ');
                    }
                }
            }
        }
    }
    
    // Return representable string
    return rstring;
}
         
/**
 * Performs @key@ substitution on an xstring.
 */
public static RMXString rpgClone(RMXString aString, ReportOwner anRptOwner, Object userInfo, RMShape aShape,
        boolean doCopy)
{
    // Declare local variable for resulting out-xstring and for whether something requested a recursive RPG run
    RMXString outString = aString;
    boolean redo = false;

    // If userInfo provided, plug it into ReportMill
    if(userInfo!=null && anRptOwner!=null)
        anRptOwner.pushDataStack(userInfo);

    // Get range for first key found in string
    Range totalKeyRange = nextKeyRangeAfterIndex(outString, 0, new Range());
    
    // While the inString still contains @key@ constructs, do substitution
    while(totalKeyRange.length() > 0) {
        
        // Get key start location (after @-sign) and length
        int keyLocation = totalKeyRange.start + 1;
        int keyLength = totalKeyRange.length() - 2;
        Object valString = null;
        
        // Get the run at the given location
        RMXStringRun keyRun = outString.getRunAt(keyLocation, false);

        // If there is a key between the @-signs, evaluate it for substitution string
        if(keyLength > 0) {
            
            // Get actual key string
            String keyString = outString.subSequence(keyLocation, keyLocation + keyLength).toString();
            
            // Get key string as key chain
            RMKeyChain keyChain = RMKeyChain.getKeyChain(keyString);

            // If keyChain hasPageReference, tell reportMill and skip this key
            if(aShape!=null && keyChain.hasPageReference()) {
                anRptOwner.addPageReferenceShape(aShape);
                nextKeyRangeAfterIndex(outString, totalKeyRange.end, totalKeyRange);
                continue;
            }
            
            // Get keyChain value
            Object val = RMKeyChain.getValue(anRptOwner, keyChain);
            
            // If val is list, replace with first value (or null)
            if(val instanceof List) { List list = (List)val;
                val = list.size()>0? list.get(0) : null; }
                
            // If we found a String, then we'll just use it for key sub (although we to see if it's a KeyChain literal)
            if(val instanceof String) {
                
                // Set string value to be substitution string
                valString = val;
    
                // If keyChain has a string literal, check to see if val is that string literal
                if(keyChain.hasOp(RMKeyChain.Op.Literal) && !RMStringUtils.startsWithIC((String)val, "<html")) {
                    String string = val.toString();
                    int index = keyString.indexOf(string);
                    
                    // If val is that string literal, get original xstring substring (with attributes)
                    if(index>0 && keyString.charAt(index-1)=='"' && keyString.charAt(index+string.length())=='"') {
                        int start = index + keyLocation;
                        valString = outString.substring(start, start + string.length());
                        redo = redo || string.indexOf("@")>=0;
                    }
                }
            }

            // If we found an xstring, then we'll just use it for key substitution
            else if(val instanceof RMXString)
                valString = val;
                
            // If we found a keyChain, add @ signs and redo (this feature lets developers return an RMKeyChain)
            else if(val instanceof RMKeyChain) {
                valString = "@" + val.toString() + "@";
                redo = true;
            }

            // If val is Number, get format and change val to string (verify format type)
            else if(val instanceof Number) {
                RMFormat format = keyRun.getFormat();
                if(!(format instanceof RMNumberFormat)) format = RMNumberFormat.PLAIN;
                valString = format.formatRM(val);
            }

            // If val is Date, get format and change val to string (verify format type)
            else if(val instanceof Date) {
                RMFormat format = keyRun.getFormat();
                if(!(format instanceof RMDateFormat)) format = RMDateFormat.defaultFormat;
                valString = format.formatRM(val);
            }

            // If val is byte array, turn it into string (maybe an image shape)
            else if(val instanceof byte[]) {
                if(RMImageReader.canRead((byte[])val))
                    valString = RMKeyChainFuncs.RMImage(val);
                else new String((byte[])val);
            }

            // If value is null, either use current format's or DocumentInfo's _nullString
            else if(val==null) {

                // If there is format in XString, get string for val
                RMFormat fmt = keyRun.getFormat();
                if(fmt != null)
                    valString = fmt.formatRM(val);
            }
            
            // If object is none of standard types (Str, Num, Date, XStr or null), see if it will provide bytes
            else {
                
                // Ask object for "bytes" method or attribute
                Object bytes = RMKey.getValue(val, "bytes");
                
                // If bytes is byte array, just set it
                if(bytes instanceof byte[])
                    valString = new String((byte[])bytes);
                
                // If value is List, reset it so we don't get potential hang in toString
                else if(val instanceof List)
                    valString = "<List>";
                
                // If value is Map, reset to "Map" so we don't get potential hang in toString
                else if(val instanceof Map)
                    valString = "<Map>";
                
                // Set substitution value to string representation of provided object
                else valString = val.toString();
            }

            // If substitution string is still null, replace it with document null-string
            if(valString == null) 
                valString = anRptOwner.getNullString()!=null? anRptOwner.getNullString() : "";
        }

        // If there wasn't a key between '@' signs, assume they wanted '@'
        else valString = "@";

        // If substitution string was found, perform substitution
        if(valString != null) {

            // If this is the first substitution, get a copy of outString
            if(outString==aString && doCopy)
                outString = aString.clone();

            // If substitution string was raw string, perform replace (and possible rtf/html evaluation)
            if(valString instanceof String) { String string = (String)valString;
                
                // If string is HTML formatted text, parse into RMXString
                if(RMStringUtils.startsWithIC(string, "<html"))
                    valString = RMHTMLParser.parse(string, keyRun.getFont());
                
                // If string is RTF formatted text, parse into RMXString
                else if(string.startsWith("{\\rtf"))
                    valString = RMRTFParser.parse(string, keyRun.getFont());
                
                // If string is normal string, just perform replace and update key range
                else {
                    outString.replaceChars(string, totalKeyRange.start, totalKeyRange.end);
                    totalKeyRange.setLength(((String)valString).length());
                }
            }
            
            // If substitution string is xstring, just do xstring replace
            if(valString instanceof RMXString) { RMXString xstring = (RMXString)valString;
                outString.replaceString(xstring, totalKeyRange.start, totalKeyRange.end);
                totalKeyRange.setLength(xstring.length());
            }
        }

        // Get next totalKeyRange
        nextKeyRangeAfterIndex(outString, totalKeyRange.end, totalKeyRange);
    }
    
    // If userInfo was provided, remove it from ReportMill
    if(userInfo!=null)
        anRptOwner.popDataStack();

    // If something requested a recursive RPG run, do it
    if(redo)
        outString = rpgClone(outString, anRptOwner, userInfo, aShape, false);

    // Return RPG string
    return outString;
}

/**
 * Returns the range of the next occurrence of @delimited@ text.
 */
public static Range nextKeyRangeAfterIndex(RMXString aString, int anIndex, Range aRange)
{
    // Get length of string (return bogus range if null)
    int length = aString!=null? aString.length() : 0;
    if(length<2)
        return aRange.set(-1, -1);

    // Get start of key (return if it is the last char)
    int startIndex = aString.indexOf("@", anIndex);
    if(startIndex==length-1) return aRange.set(startIndex, startIndex+1);

    // If startRange of key was found, look for end
    if(startIndex>=0) {
        int nextIndex = startIndex;
        while(++nextIndex < length) {
            char c = aString.charAt(nextIndex);
            if(c=='"')
                while((++nextIndex<length) && (aString.charAt(nextIndex)!='"'));
            else if(c=='@')
                return aRange.set(startIndex, nextIndex+1);
        }
    }
    
    // Set bogus range and return
    return aRange.set(-1, -1);
}

/**
 * A range class.
 */
private static class Range {
    int start, end;
    public int length()  { return end - start; }
    public void setLength(int aLength)  { end = start + aLength; } 
    public Range set(int aStart, int anEnd)  { start = aStart; end = anEnd; return this; }
}

}