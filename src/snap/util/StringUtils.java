package snap.util;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.*;

/**
 * This class is a collection of convenient static String utils.
 */
public class StringUtils {

    // A regex pattern to find integer/long numbers
    static Pattern    _intLongPattern = Pattern.compile("[-+]?[0-9]+");

    // A regex pattern to find numbers in strings (supports positive/negative, floating point & exponents)
    static Pattern    _numberPattern = Pattern.compile("[-+]?(([0-9]+\\.?[0-9]*)|([0-9]*\\.[0-9]+))([eE][-+]?[0-9]+)?");

/**
 * Returns whether string is null or empty.
 */
public static boolean isEmpty(String aString)  { return aString==null || aString.trim().length()==0; }

/**
 * Returns the length of given string (supports null).
 */
public static int length(CharSequence aString)  { return aString==null? 0 : aString.length(); }

/**
 * Adds the two strings together (treats nulls as empty strings).
 */
public static String add(String s1, String s2)  { return add(s1, s2, null); }

/**
 * Adds the two strings together with separator (treats nulls as empty strings, omitting sep if either is null).
 */
public static String add(String s1, String s2, String aSeparator)
{
    if(s1==null) return s2;
    if(s2==null) return s1;
    return aSeparator==null? (s1 + s2) : (s1 + aSeparator + s2);
}

/**
 * String demotion - returns either the given string or null (if given string length is zero).
 */
public static String min(String s)  { return s!=null && s.length()==0? null : s; }

/**
 * String promotion - returns either the given string or empty string (if given string is null).
 */
public static String max(String s)  { return s==null? "" : s; }

/**
 * Returns the first non-null string of the two given strings (or null).
 */
public static String or(String s1, String s2)  { return s1!=null? s1 : s2; }

/**
 * Returns a string representation of the given float to (at most) 3 significant digits.
 */
public static String toString(double aValue)
{
    long lval = (long)aValue; if(lval==aValue) return Long.toString(lval);
    return toStrFmtSynch(aValue);
}

// A toString(double) with formatter
private synchronized static String toStrFmtSynch(double aValue)  { return _fmt.format(aValue); }
static DecimalFormat _fmt = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ENGLISH));

/**
 * Returns the file name component of the given string path (everything after last file separator).
 */
public static String getPathFileName(String aPath)
{
    String path = getPathStandardized(aPath);
    int index = getPathFileNameIndex(path);
    return index==0? path : path.substring(index);
}

/**
 * Returns the simple file name for a given path (just the file name without extension).
 */
public static String getPathFileNameSimple(String aPath)
{
    String fileName = getPathFileName(getPathStandardized(aPath));
    int index = fileName.lastIndexOf('.');
    return index<0? fileName : fileName.substring(0, index);
}

/**
 * Returns the index to the first character of the filename component of the given path.
 */
public static int getPathFileNameIndex(String aPath)
{
    String path = getPathStandardized(aPath);
    int index = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    if(index<0)
        index = path.lastIndexOf(File.separator);
    return Math.max(index+1, 0);
}

/**
 * Returns the extension of the given string path (everything after last '.').
 */
public static String getPathExtension(String aPath)
{
    if(aPath==null) return null;
    String filename = getPathFileName(aPath);
    int dot = filename.lastIndexOf('.');
    return dot>=0 && dot<filename.length()-1? filename.substring(dot+1) : "";
}

/**
 * Returns the given string path minus any extension.
 */
public static String getPathSimple(String aPath)
{
    String ext = getPathExtension(aPath);
    return ext!=null && ext.length()>0? aPath.substring(0, aPath.length()-ext.length()-1) : aPath;
}

/**
 * Returns the given string path minus the file name component (everything after last file separator).
 */
public static String getPathParent(String aPath)
{
    int index = getPathFileNameIndex(aPath);
    return index>0 && aPath.length()>1? getPathStandardized(aPath.substring(0, index)) : "";
}

/**
 * Returns a path with a filename or relative path added.
 */
public static String getPathChild(String aPath, String aChildPath)
{
    String path = getPathStandardized(aPath);
    if(path.endsWith("/") ^ aChildPath.startsWith("/"))
        return path + aChildPath;
    if(aChildPath.startsWith("/"))
        return path + aChildPath.substring(1);
    return path + '/' + aChildPath;
}

/**
 * Returns a peer path for given path and new filename by stripping filename from given path and using given name.
 */
public static String getPathPeer(String aPath, String aName)
{
    String parent = getPathParent(aPath); if(parent.length()==0) parent = "/";
    return getPathChild(parent, aName);
}

/**
 * Returns a sister path for given path and new extension by stripping extension from path and using given extension.
 */
public static String getPathSister(String aPath, String anExtension)
{
    String path = getPathSimple(aPath); if(!anExtension.startsWith(".")) path += ".";
    return path + anExtension;
}

/**
 * Returns the path in a standard, normal format (strips any trailing file separators).
 */
public static String getPathStandardized(String aPath)
{
    // Get path stripped of trailing file separator
    String path = aPath;
    if((path.endsWith("/") || path.endsWith("\\")) && path.length()>1)
        path = path.substring(0, path.length()-1);
    else if(path.endsWith(File.separator) && path.length()>File.separator.length())
        path = path.substring(0, path.length()-File.separator.length());
    
    // Return path
    return path;
}

/**
 * Trims the end of a string.
 */
public static String trimEnd(String aString)
{
    int length = aString.length(), index = length;
    while(index>0 && Character.isWhitespace(aString.charAt(index-1))) index--;
    return index<length? aString.substring(0, index) : aString;
}

/**
 * Returns the result of deleting from the given string any occurrence of the search string.
 */
public static String delete(String aString, String aSearch)  { return replace(aString, aSearch, ""); }

/**
 * Returns the result of deleting from the given string any occurrence of the search string (ignores case).
 */
public static String deleteIC(String aString, String aSearch)  { return replaceIC(aString, aSearch, ""); }

/**
 * Returns the result of replacing in the given string the char range with the with-string.
 */
public static String replace(String s, int start, int end, String withString)
{
    String first = start>0? s.substring(0, start) : "";
    String last = end<s.length()? s.substring(end, s.length()) : "";
    return first + withString + last;
}

/**
 * Returns the result of replacing in the given string any occurrence of the search string with the replace-char.
 */
public static String replace(String aString, String search, char replace)
{
    int start = aString.indexOf(search);
    
    if(start>=0) {
        StringBuffer sb = new StringBuffer(aString);
        
        do {
            int sbStart = start + sb.length() - aString.length();
            sb.setCharAt(sbStart, replace);
            sb.delete(sbStart + 1, sbStart + search.length());
            start = aString.indexOf(search, start + search.length());
        } while(start>=0);
        aString = sb.toString();
    }
    
    return aString;
}

/**
 * Returns the result of replacing in the given string any occurrence of the search string with the replace-string.
 */
public static String replace(String aString, String search, String replace)
{
    // If string or search are null, return
    if(aString==null || search==null) return aString;
    
    // If replace is null, do delete
    if(replace==null) return delete(aString, search);
    
    // Get first occurrence of search string (just return if not found)
    int start = aString.indexOf(search);
    if(start<0)
        return aString;

    // Create string buffer for string
    StringBuffer sb = new StringBuffer(aString);
        
    // Iterate while there are occurrences of search string
    do {
        int sbStart = start + sb.length() - aString.length();
        sb.replace(sbStart, sbStart + search.length(), replace);
        start = aString.indexOf(search, start + search.length());
    } while(start>=0);
    
    // Return modified string buffer string
    return sb.toString();
}

/**
 * Returns the result of replacing in given string any occurrence of search string with replace-string (ignore case).
 */
public static String replaceIC(String aString, String search, String replace)
{
    int start = indexOfIC(aString, search);
    
    if(start>=0) {
        StringBuffer sb = new StringBuffer(aString);
        
        do {
            int sbStart = start + sb.length() - aString.length();
            sb.replace(sbStart, sbStart + search.length(), replace);
            start = indexOfIC(aString, search, start + search.length());
        } while(start>=0);
        aString = sb.toString();
    }
    
    return aString;
}

/**
 * Returns a list of parts of given string separated by the given delimiter.
 */
public static List <String> separate(String aString, String aSeparator)
{
    return separate(aString, aSeparator, false);
}

/**
 * Returns a list of parts of given string separated by the given delimiter, with option to trim space.
 */
public static List <String> separate(String aString, String aSeparator, boolean doTrim)
{
    List list = new ArrayList(); if(aString==null || aSeparator==null) return list;
    int start = 0, length = aSeparator.length();

    // While instances of aSeparator are found, add preceding characters
    for(int index=aString.indexOf(aSeparator); index>=0; index=aString.indexOf(aSeparator, start)) {
        if(index>start)
            list.add(doTrim? aString.substring(start, index).trim() : aString.substring(start, index));
        start = index + length;
    }

    // If remainder, add it
    if(start<aString.length())
        list.add(doTrim? aString.substring(start, aString.length()).trim() : aString.substring(start, aString.length()));
    
    // Return list
    return list;
}

/**
 * Returns an int value by parsing the given string.
 */
public static int intValue(String aString)  { return (int)longValue(aString, 0); }

/**
 * Returns an int value by parsing the given string.
 */
public static long longValue(String aString)  { return longValue(aString, 0); }

/**
 * Returns an double value by parsing the given string starting at the given index.
 */
public static long longValue(String aString, int aStart)
{
    // Bail if string is null or start index beyond bounds
    if(aString==null || aStart>aString.length()) return 0;
    
    // Get number matcher for string
    Matcher matcher = _intLongPattern.matcher(aString);
    
    // If number found, have Double parse it
    if(matcher.find(aStart)) {
        String string = matcher.group();
        try { return Long.parseLong(string); }
        catch(Exception e) { }
    }
    
    // Return zero since number not found
    return 0;
}

/**
 * Returns an float value by parsing the given string.
 */
public static float floatValue(String aString)  { return (float)doubleValue(aString, 0); }

/**
 * Returns an double value by parsing the given string.
 */
public static double doubleValue(String aString)  { return doubleValue(aString, 0); }

/**
 * Returns an double value by parsing the given string starting at the given index.
 */
public static double doubleValue(String aString, int aStart)
{
    // Bail if string is null or start index beyond bounds
    if(aString==null || aStart>aString.length()) return 0;
    
    // Get number matcher for string
    Matcher matcher = _numberPattern.matcher(aString);
    
    // If number found, have Double parse it
    if(matcher.find(aStart)) {
        String string = matcher.group();
        try { return Double.parseDouble(string); }
        catch(Exception e) { }
    }
    
    // Return zero since number not found
    return 0;
}

/**
 * Returns the ASCII bytes of the given string (ISO-Latin).
 */
public static byte[] getBytes(String aString)  { return getBytes(aString, "ISO-8859-1"); }

/**
 * Returns the bytes of the given string in the requested char encoding.
 */
public static byte[] getBytes(String aString, String enc)
{
    try { return aString!=null? aString.getBytes(enc) : null; }
    catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns a string for given bytes.
 */
public static String getString(byte theBytes[])  { return getString(theBytes, "ISO-8859-1"); }

/**
 * Returns a string for given bytes.
 */
public static String getString(byte theBytes[], String anEncoding)
{
    try { return theBytes!=null? new String(theBytes, 0, theBytes.length, anEncoding) : null; }
    catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns a string from the given ASCII bytes.
 */
public static String getISOLatinString(byte bytes[])  { return getISOLatinString(bytes, 0, bytes.length); }

/**
 * Returns a string from the given ASCII bytes (from offset to offset+length).
 */
public static String getISOLatinString(byte bytes[], int offset, int length)
{
    try { return new String(bytes, offset, length, "ISO-8859-1"); }
    catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns the index of search string s2 in given string s1, ignores case.
 */
public static int indexOfIC(CharSequence s1, CharSequence s2)  { return indexOfIC(s1, s2, 0); }

/**
 * Returns the index of search string s2 in given string s1, ignores case and starts at start char index.
 */
public static int indexOfIC(CharSequence s1, CharSequence s2, int start)
{
    int l1 = length(s1), l2 = length(s2); if(l1==0 || l2==0) return -1;
    for(int i=start, iMax=l1-l2; i<=iMax; i++) {
        for(int j=0; j<l2; j++) {
            char c1 = s1.charAt(i+j), c2 = s2.charAt(j);
            if(c1!=c2 && Character.toUpperCase(c1)!=Character.toUpperCase(c2))
                break;
            if(j==l2-1)
                return i;
        }
    }

    // Return -1 since string not found
    return -1;
}

/**
 * Returns whether a given string contains a given string, ignoring case.
 */
public static boolean containsIC(CharSequence aString1, CharSequence aString2)
{
    return indexOfIC(aString1, aString2)>=0;
}

/**
 * Returns whether s1 equals s2, ignoring case.
 */
public static boolean equalsIC(String s1, String s2)
{
    if(s1==null || s2==null) return false;
    if(s1.length()!=s2.length()) return false;
    return startsWithIC(s1, s2);
}

/**
 * Returns whether s1 equals any of the given strings, ignoring case.
 */
public static boolean equalsIC(String s1, String... strings)
{
    for(String string : strings) if(equalsIC(s1, string)) return true; return false;
}

/**
 * Returns whether s1 ends with s2, ignoring case.
 */
public static boolean endsWithIC(String s1, String s2)
{
    return s1!=null && s2!=null && s1.regionMatches(true, s1.length() - s2.length(), s2, 0, s2.length());
}

/**
 * Returns whether s1 ends with any of the given strings, ignoring case.
 */
public static boolean endsWithIC(String s1, String... strings)
{
    for(String string : strings) if(endsWithIC(s1, string)) return true; return false;
}

/**
 * Returns whether s1 starts with s2, ignoring case.
 */
public static boolean startsWithIC(String s1, String s2)
{
    return s1!=null && s2!=null && s1.regionMatches(true, 0, s2, 0, s2.length());
}

/**
 * Returns whether s1 starts with any of the given strings, ignoring case.
 */
public static boolean startsWithIC(String s1, String... strings)
{
    for(String string : strings) if(startsWithIC(s1, string)) return true; return false;
}

/**
 * Returns the given string with the first char promoted to uppercase.
 */
public static String firstCharUpperCase(String aString)
{
    // If first char already upper case (or if string empty) return string
    if(aString==null | aString.length()==0 || Character.isUpperCase(aString.charAt(0))) return aString;
    
    // Fix string
    StringBuffer sb = new StringBuffer(aString);
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    return sb.toString();
}

/**
 * Returns the given string with the first char demoted to lowercase.
 */
public static String firstCharLowerCase(String aString)
{
    StringBuffer sb = new StringBuffer(aString);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    return sb.toString();
}

/**
 * Returns a spaced string from a camel case string.
 */
public static String fromCamelCase(String aString)
{
    // Get string buffer for string
    StringBuffer string = new StringBuffer(aString);
    
    // Make sure first char is upper case
    if(string.length()>0 && Character.isLowerCase(string.charAt(0)))
            string.setCharAt(0, Character.toUpperCase(string.charAt(0)));
    
    // Iterate over chars
    for(int i=0; i<string.length()-1; i++) {
        
        // Add space between any adjacent pair of camel case chars
        if(Character.isLowerCase(string.charAt(i)) && Character.isUpperCase(string.charAt(i+1)))
            string.insert(i+1, ' ');
    }
    
    // Return spaced string
    return string.toString();
}

/**
 * Returns a camel cased string from a string with non-alphanumeric chars.
 */
public static String toCamelCase(String aString)
{
    // Get string buffer for string
    StringBuffer sb = new StringBuffer(aString);
    
    // Iterate over chars
    for(int i=0; i<sb.length()-1; i++) { char c = sb.charAt(i);
        
        // If whitespace char, delete (and see if next char needs to be promoted)
        if(!Character.isJavaIdentifierPart(c)) {
            sb.deleteCharAt(i);
            if(i<sb.length()) { c = sb.charAt(i);
                if(Character.isLetter(c) && Character.isLowerCase(c))
                    sb.setCharAt(i, Character.toUpperCase(c));
            }
        }
    }
    
    // Return string
    return sb.toString();
}

/**
 * Returns a string wrapped on word boundaries.
 */
public static String wrap(String aString, int aLimit)  { return wrap(aString, aLimit, "\\s*", "\n"); }

/**
 * Returns a string wrapped on word boundaries as defined by regex string (eg., whitespace="\\s*", dot="\\.", etc.).
 */
public static String wrap(String aString, int aLimit, String aSeperator, String aJoiner)
{
    // Create new string buffer
    StringBuffer buffer = new StringBuffer();
    
    // Get matcher
    Pattern pattern = Pattern.compile(aSeperator);
    Matcher matcher = pattern.matcher(aString);
    
    // Add words
    int start = 0;
    while(start<aString.length()) {
        if(start+aLimit>aString.length()) {
            buffer.append(aString, start, aString.length());
            break;
        }
        else {
            start = addWords(aString, start, start+aLimit, buffer, matcher);
            buffer.append(aJoiner);
        }
    }
    
    // Return string
    return buffer.toString();
}

/**
 * Adds words from given string and string start/end to given string buffer.
 */
public static int addWords(String aString, int aStart, int anEnd, StringBuffer aBuffer, Matcher aMatcher)
{
    // If empty string, return
    if(aString==null) return anEnd;
    
    // Configure matcher
    aMatcher.region(aStart, anEnd);
    
    // If starts with matcher, adjust start
    int start = aMatcher.lookingAt()? aMatcher.end() : aStart;
    
    // Find end of last "word" within bounds
    int end = anEnd;
    while(aMatcher.find() && aMatcher.start()<=anEnd)
        end = aMatcher.start();
    
    // Add chars to buffer
    aBuffer.append(aString, start, end);
    
    // Return last char
    return aMatcher.find(end)? aMatcher.end() : end;
}
    
/** 
 * Allows you to take an array of strings and concatenate them together with a separator
 * @param parts an array of Strings
 * @param separator a string to place between elements
 * @return
 */
public static String join(Object[] parts, String separator)  { return join(parts, separator, 0, parts.length); }

/** 
 * Allows you to take an array of strings and concatenate them together with a separator
 * @param parts an array of Strings
 * @param separator a string to place between elements
 * @return
 */
public static String join(Object[] parts, String separator, int aStart, int anEnd)
{
    // Create string buffer
    StringBuffer buf = new StringBuffer();
    
    // Iterate over parts array
    for(int i=aStart; i<anEnd; i++) {
	    buf.append(parts[i]);
	    if(i<anEnd-1 && separator!=null)
            buf.append(separator);
    }
    
    // Return string buffer string
    return buf.toString();	
}

/**
 * Turns a string like "myFile" into "myFile-1", and a string like "myFile-2" to "myFile-3", given suffix like "-".
 */
public static String nextInSequence(String source, String suffix)
{
    int sindex = source.lastIndexOf(suffix);
    
    if (sindex != -1) {    
        String remainder = source.substring(sindex+suffix.length());
    
        // Get the integer value after the suffix, if it's there.
        if (remainder.length()>0) {
            
            // The StringUtils.intValue() above is a little too forgiving for this purpose, so use the Integer version.
            try {
                int sequence = Integer.parseInt(remainder)+1;
                return source.substring(0,sindex)+suffix+sequence;
            }
            catch (NumberFormatException e) { }
        }
    }
    return source+suffix+"1";
}

/**
 * Returns the index of a given string in the given array, ignoring case.
 */
public static int indexOfIC(String theStrings[], String aString)
{
    for(int i=0; i<theStrings.length; i++)
        if(equalsIC(aString, theStrings[i]))
            return i;
    return -1;
}

/**
 * Returns whether the given string array contains the given string, ignoring case.
 */
public static boolean containsIC(String theStrings[], String aString)  { return indexOfIC(theStrings, aString)>=0; }

/**
 * Returns a quoted string.
 */
public static String getStringQuoted(String aString)  { return getStringSurrounded(aString, "\"", "\\\""); }

/**
 * Returns a single quoted string.
 */
public static String getStringSingleQuoted(String aString)  { return getStringSurrounded(aString, "\'", "\\\'"); }

/**
 * Returns a string surrounded by given string.
 */
public static String getStringSurrounded(String aString, String aSurroundString, String anEscapeString)
{
    // Get trimmed string and just return if it starts and ends with surround string
    String string = aString.trim();
    if(string.startsWith(aSurroundString) && string.endsWith(aSurroundString))
        return string;
    
    // Replace any inner quotes with escaped version
    string = replace(string, aSurroundString, anEscapeString);
    
    // Return quoted string
    return aSurroundString + string + aSurroundString;
}

/**
 * Returns a stack trace string for given exception.
 */
public static String getStackTraceString(Throwable aThrowable)
{
    // Get root exception
    while(aThrowable.getCause()!=null) aThrowable = aThrowable.getCause();
    
    // Get string
    try {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        aThrowable.printStackTrace(pw); pw.flush();
        return sw.toString();
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns a stack trace string for given exception.
 */
public static String getStackTraceString(Throwable aThrowable, int aDepth)
{
    String str = getStackTraceString(aThrowable);
    int index = str.indexOf('\n'); for(int i=1; i<aDepth && index>0; i++) index = str.indexOf('\n', index+1);
    String str2 = index>0? str.substring(0, index) : str;
    return str2;
}

}