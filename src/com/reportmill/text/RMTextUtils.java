package com.reportmill.text;

/**
 * Some utility methods for text processing.
 */
public class RMTextUtils {

/**
 * Returns the index of the next newline (or carriage-return/newline) in given char sequence starting at given
 * char index. 
 */
public static int indexOfNewline(CharSequence aSequence, int aStart)
{
    for(int i=aStart, iMax=aSequence.length(); i<iMax; i++)
        if(isNewlineChar(aSequence, i))
            return i;
    return -1;
}

/**
 * Returns the index just beyond next newline (or carriage-return/newline) in given char sequence starting at given
 * char index. 
 */
public static int indexAfterNewline(CharSequence aSequence, int aStart)
{
    for(int i=aStart, iMax=aSequence.length(); i<iMax; i++) { char c = aSequence.charAt(i);
        if(c=='\r')
            return i+1<iMax && aSequence.charAt(i+1)=='\n'? (i+2) : (i+1);
        if(c=='\n')
            return i+1;
    }
    return -1;
}

/**
 * Returns the index of the previous newline (or carriage-return/newline) in given char sequence starting at given
 * char index. 
 */
public static int lastIndexOfNewline(CharSequence aSequence, int aStart)
{
    for(int i=aStart-1; i>=0; i--) { char c = aSequence.charAt(i);
        if(c=='\n')
            return i-1>=0 && aSequence.charAt(i-1)=='\r'? (i-1) : i;
        if(c=='\r')
            return i;
    }
    return -1;
}

/**
 * Returns the index just beyond previous newline (or carriage-return/newline) in given char sequence starting at given
 * char index. 
 */
public static int lastIndexAfterNewline(CharSequence aSequence, int aStart)
{
    for(int i=aStart-1; i>=0; i--)
        if(isNewlineChar(aSequence, i))
            return i+1;
    return -1;
}

/**
 * Returns whether the index in the given char sequence is at a line end.
 */
public static boolean isLineEnd(CharSequence aSequence, int anIndex)
{
    return anIndex<aSequence.length() && isNewlineChar(aSequence, anIndex);
}

/**
 * Returns whether the index in the given char sequence is at just after a line end.
 */
public static boolean isAfterLineEnd(CharSequence aSequence, int anIndex)
{
    return anIndex-1>=0 && isNewlineChar(aSequence, anIndex-1);
}

/**
 * Returns whether a char is a newline char.
 */
public static boolean isNewlineChar(CharSequence aSequence, int anIndex)
{
    char c = aSequence.charAt(anIndex); return c=='\r' || c=='\n';
}

}