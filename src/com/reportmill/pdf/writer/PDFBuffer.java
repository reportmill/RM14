package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.*;

/**
 * This class is like a StringBuffer, but for creating PDF files instead of strings.
 */
public class PDFBuffer {
    
    // This is the actual byte buffer
    ByteArrayOutputStream     _source = new ByteArrayOutputStream();
    
/**
 * Returns the current length of the buffer.
 */
public int length()  { return _source.size(); }

/**
 * Adds a character to the pdf buffer.
 */
public PDFBuffer append(char aChar)
{
    // Assert that char is within ascii range
    if(aChar>255) {
        System.err.println("PDFBuffer: Attempt to write non-ASCII char: " + aChar); aChar = 'X'; }
    
    // Write char and return
    _source.write(aChar); return this;
}

/**
 * Adds a string to the pdf buffer.
 */
public PDFBuffer append(String aString)
{
    for(int i=0, iMax=aString.length(); i<iMax; i++)
        append(aString.charAt(i));
    return this;
}

/**
 * Adds an int to the pdf buffer.
 */
public PDFBuffer append(int anInt)  { return anInt<0? append('-').append(-anInt) : appendDigits(anInt, 0); }

/**
 * Adds a double and newline to the pdf buffer.
 */
public PDFBuffer append(double aDouble)
{
    // If value less than zero, add negative sign and negate
    if(aDouble<0)
        return append('-').append(-aDouble);

    // Get integer portion and fraction portion of number
    int whole = (int)aDouble;
    int fraction = (int)((aDouble - whole)*1000);
    
    // Append integer portion and, if fraction is non-zero, append decimal point and fraction
    return fraction==0? append(whole) : append(whole).append('.').appendDigits(fraction, 3);
}

/**
 * Appends digits of a whole number (recursively), padded by zeros on the left to the given number of pad digits.
 */
private PDFBuffer appendDigits(int anInt, int aPad)
{
    char digit = (char)('0' + anInt%10);
    if(anInt>9 || aPad>1)
        appendDigits(anInt/10, aPad-1);
    return append(digit);
}

/**
 * Writes a color.
 */
public PDFBuffer append(RMColor aColor)
{
    append('[');
    append(aColor.getRed()).append(' ');
    append(aColor.getGreen()).append(' ');
    append(aColor.getBlue());
    return append(']');
}

/**
 * Writes a rect to the pdf buffer.
 */
public PDFBuffer append(RMRect aRect)
{
    append('[');
    append((int)aRect.x); append(' '); append((int)aRect.y); append(' ');
    append((int)aRect.getMaxX()); append(' '); append((int)aRect.getMaxY());
    return append(']');
}

/**
 * Appends an arbitrary byte array.
 */    
public PDFBuffer append(byte theBytes[])  { return append(theBytes, 0, theBytes.length); }

/**
 * Appends an arbitrary byte array with the given offset and length.
 */ 
public PDFBuffer append(byte theBytes[], int anOffset, int aLength)
{
    _source.write(theBytes, anOffset, aLength); return this;
}

/**
 * Appends another buffer.
 */
public PDFBuffer append(PDFBuffer aBuffer)  { return append(aBuffer.toByteArray()); }

/**
 * Adds a newline to the pdf buffer.
 */
public PDFBuffer appendln()  { return append('\n'); }

/**
 * Adds a string and newline to the pdf buffer.
 */
public PDFBuffer appendln(String aString)  { return append(aString).appendln(); }

/**
 * Adds a string object ( a string enclosed in parentheses ) to the buffer.
 * All chars above the seven bit range are represented by an octal version of the form '\ddd'.
 * The characters '(', ')' and '\' are escaped with backslash.
 */
public void printPDFString(String aString)
{
    // Assert that we were given PDF string
    if(!aString.startsWith("(") || !aString.endsWith(")"))
        throw new RuntimeException("Internal error - printPDFString called with non-string object");
    
    // Write string start char
    append('(');
    
    // Iterate over inside string chars
    for(int i=1, iMax=aString.length()-1; i<iMax; i++) {
        char c = aString.charAt(i);
        
        // If char outside seven bit ascii range, have to octal escape
        if(c>127) {
            _source.write('\\');
            char c3 = (char)('0' + c%8);  c/=8;
            char c2 = (char)('0' + c%8);  c/=8;
            char c1 = (char)('0' + c%8);
            _source.write(c1); _source.write(c2); _source.write(c3);
        }
        
        // Handle special chars
        else if(c=='(' || c==')' || c=='\\')
            append('\\').append(c);
        
        // Handle everything else
        else _source.write(c);
    }
    
    // Write string end char
    append(')');
}

/**
 * Writes a transform to pdf buffer.
 */
public void transform(RMTransform aTransform)
{
    transform(aTransform.a(), aTransform.b(), aTransform.c(), aTransform.d(), aTransform.tx(), aTransform.ty());
}

/**
 * Writes a transform to pdf buffer.
 */
public void transform(double a, double b, double c, double d, double tx, double ty)
{
    append(a); append(' ');
    append(b); append(' ');
    append(c); append(' ');
    append(d); append(' ');
    append(tx); append(' ');
    append(ty);
    appendln(" cm");
}

/**
 * Writes a given path to PDF file.
 */
public void writePath(Shape aShape)
{
    // Get path iterator
    PathIterator pathIterator = aShape.getPathIterator(null);
    
    // Create point array for path iteration
    float points[] = new float[6];
    
    // Declare variable to keep track of path start point and path last point for subpaths
    float pathStartX = 0, pathStartY = 0;
    float pathEndX = 0, pathEndY = 0;
    
    // Iterate until done
    while(!pathIterator.isDone()) {
        
        // Get path segment type and points
        int segment = pathIterator.currentSegment(points);

        // Handle path segment types independently
        switch(segment) {
        
            // Handle MOVE_TO
            case PathIterator.SEG_MOVETO:
                moveTo(points[0], points[1]);
                pathStartX = pathEndX = points[0];
                pathStartY = pathEndY = points[1];
                break;
            
            // Handle LINE_TO
            case PathIterator.SEG_LINETO:
                lineTo(points[0], points[1]);
                pathEndX = points[0];
                pathEndY = points[1];
                break;
            
            // Handle QUAD_TO
            case PathIterator.SEG_QUADTO: {
                quadTo(pathEndX, pathEndY, points[0], points[1], points[2], points[3]);
                pathEndX = points[2];
                pathEndY = points[3];
                break;
            }
            
            // Handle CURVE_TO
            case PathIterator.SEG_CUBICTO: {
                curveTo(points[0], points[1], points[2], points[3], points[4], points[5]);
                pathEndX = points[4];
                pathEndY = points[5];
                break;
            }
            
            // Handle CLOSE
            case PathIterator.SEG_CLOSE: {
                appendln("h");
                pathEndX = pathStartX;
                pathEndY = pathStartY;
                break;
            }
        }
        
        // Advance path iterator
        pathIterator.next();
    }
}

/**
 * Writes a moveto operator.
 */
public void moveTo(float x, float y)  { append(x); append(' '); append(y); appendln(" m"); }

/**
 * Writes a lineto operator.
 */
public void lineTo(float x, float y)  { append(x); append(' '); append(y); appendln(" l"); }

/**
 * Writes a quadto operator.
 */
public void quadTo(float lastX, float lastY, float x1, float y1, float x2, float y2)
{
    // Convert single control point and last point to cubic bezier control points
    double cp1x = lastX + 2.0/3*(x1 - lastX);
    double cp1y = lastY + 2.0/3*(y1 - lastY);
    double cp2x = cp1x + 1.0/3.0*(x2 - lastX);
    double cp2y = cp1y + 1.0/3.0*(y2 - lastY);
    
    // Call curve to
    curveTo((float)cp1x, (float)cp1y, (float)cp2x, (float)cp2y, x2, y2);
}

/**
 * Writes a curveto operator.
 */
public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
{
    append(x1); append(' '); append(y1); append(' ');
    append(x2); append(' '); append(y2); append(' ');
    append(x3); append(' '); append(y3);
    appendln(" c");
}

/**
 * Returns the buffer as a byte array.
 */
public byte[] toByteArray()  { return _source.toByteArray(); }

}