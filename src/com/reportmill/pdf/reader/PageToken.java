package com.reportmill.pdf.reader;

/**
 * The PageToken class represents an individual token in the page content stream.  It covers just the subset of
 * pdf token types that are legal within a content stream. Simple tokens (strings, operators, names) don't have
 * their own storage, but are piggybacked on top of the content stream using a PageToken.Range object.
 * Other possible contents of the value are Numbers and Lists.
 */
class PageToken {
    public int type;
    public Object value;

    // Constants
    public static final int PDFOperatorToken = 0;
    public static final int PDFStringToken = 1;  // either from '(' or '<'
    public static final int PDFNumberToken = 2;  // int or float
    public static final int PDFArrayToken = 3;  // '['
    public static final int PDFNameToken = 4;   //  '/'
    public static final int PDFDictOpenToken = 5; // '<<'
    public static final int PDFDictCloseToken = 6; // '>>'
    public static final int PDFBooleanToken = 7; // "true" or "false"
    public static final int PDFInlineImageData = 8; // binary image data inside BI & EI pair
    
/**
 * Creates a new PageToken.
 */
public PageToken(int tokenType, Object objectValue)  { type = tokenType; value = objectValue; }

/**
 * Construct a token of a given type with an uninitialized value.
 */
public PageToken(int tokenType)  { this(tokenType,null); }

/**
 * Specific accessors for the token's value
 * These throw ClassCastExceptions if called for the wrong token type (assumes you have already have checked type)
 */
public int intValue() { return ((Number)value).intValue();  }
public float floatValue() { return ((Number)value).floatValue(); }
public boolean boolValue() { return ((Boolean)value).booleanValue(); }
public int tokenLocation() { return ((Range)value).location; }
public int tokenLength() { return ((Range)value).length; }
public Range tokenRange() { return (Range)value;}
public String nameValue(byte pageBytes[])
{
    //TODO:  Name is preceded by a '/'.  Check that this is used consistently with the rest of the code.  I think the
    // general rule is that names used as dictionary keys never have '/' but names appearing elsewhere do.
    Range r = (Range)value;
    return new String(pageBytes, r.location, r.length);
}
/**
 * Returns the PDF name object stripped of the leading '/'.
 * NB: In PDF, "/" is a valid name, and this routine will return an empty string for that.
 * TODO: Not sure how I feel about the names of these two routines.
 */
public String nameString(byte pageBytes[]) 
{
    Range r = (Range)value;
    return new String(pageBytes, r.location+1, r.length-1);
}

/**
 * returns the token as an array of bytes.
 */
public byte[] byteArrayValue(byte pageBytes[]) 
{
    Range r = (Range)value;
    byte sub[] = new byte[r.length];
    System.arraycopy(pageBytes, r.location, sub, 0, r.length);
    return sub;
}

/**
 * Standard toString implementation.
 */
public String toString() { return toString(null); }
public String toString(byte pageBytes[])
{
   Object tok = value;
   if(value instanceof Range && pageBytes!=null) {
       Range r = (Range)value;
       tok = new String(pageBytes, r.location, r.length);
   }
   return "[" + type + " \"" + tok + "\"]";
}

}

/**
 * A simple range object.
 */
class Range implements Cloneable {
    public int location = 0;
    public int length = 0;
    public Range() { }
    public Range(int start, int len)  { location = start; length = len; }
    public Object clone()  { try { return super.clone(); } catch(CloneNotSupportedException cnse) { return null; } }
}