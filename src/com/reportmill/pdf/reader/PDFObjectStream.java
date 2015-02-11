/**
 * PDFObjectStream
 * 
 * A PDFObjectStream object is used to read pdf 1.5 compressed objects
 * Compressed objects are stored in a stream with the following structure:
 *   << 
 *     /Type /ObjStm
 *     /N     <number of compressed objects>
 *     /First <byte offset of first object>
 *     /Extends <ptr to another objstm>
 *   >>
 *   stream
 *   <objnum1> <byteoffset1> <objnum2> <byteoffset2> ...
 *   object1 object2 object3
 *   endstream
 *   
 *   The PDFObjectStream object reads the offset table at the 
 *   beginning of the stream and stores away the objects & byte numbers at initialization.
 *   To read a specific object, the PDFParser's stream data gets reset to the
 *   object stream's data and the position is set to the position in the table.
 *     
 *   
 */

package com.reportmill.pdf.reader;
import java.util.*;

public class PDFObjectStream {
     int firstOffset;
     Offsets offTable[];
     byte streamdata[];
     
public PDFObjectStream(PDFStream s, PDFFile srcFile, PDFParser parser)
{
    super();
    Map dict = s.getDictionary();
    int numObjs = PDFDictUtils.getInt(dict, srcFile, "N");
    
    firstOffset = PDFDictUtils.getInt(dict,srcFile,"First");
    
    // allocate space for the offsets
    offTable = new Offsets[numObjs];
    
    // save away the decompressed stream data
    streamdata = s.decodeStream();
    readOffsets(parser);
}

public void readOffsets(PDFParser p)
{
    Integer ival;
    int onum,off;
    int i,n=offTable.length;
    byte oldData[] = p.pdfdata.buffer(); // save away the parser's old data (the main pdf file)
    p.resetLexingData(streamdata,0);  // point the parser at the objstream
    
    try {
        for(i=0; i<n; ++i) {
            // read 2 ints from the stream (object number, relative offset)
            ival = p.integer();
            onum=ival.intValue();
            ival = p.integer();
            off=ival.intValue();
            offTable[i]=new Offsets(onum,off);
        }
    }
    catch (ParseException pe) { throw new PDFException("Error reading object stream"); }
    // reset the parser data
    p.resetLexingData(oldData,0);  // old location should be irrelevant at this point
        
}

public Object get(int objnum, PDFParser p)
{
    //int relativeOffset = Offsets.findOffset(offTable, objnum);
    int relativeOffset = offTable[objnum].offset;
    if (relativeOffset<0)
        return null;
    
    byte oldData[] = p.pdfdata.buffer();
    Object pdfobj;
    
    p.resetLexingData(streamdata, relativeOffset+firstOffset);
    try {
        pdfobj = p.pdf_object();
    }
    catch(ParseException pe) {
        p.resetLexingData(oldData,0);
        throw new PDFException("Error reading pdf object from object stream");
    }
    p.resetLexingData(oldData,0);
    return pdfobj;
}

}

/** A private class that represents a single element in the object stream.  Implements 
 * the Comparable interface so a table of Offsets objects can be sorted and bsearched.
 */
class Offsets implements Comparable {
  int objnum;
  int offset;
  
public Offsets(int num, int off)
{
    super();
    objnum=num;
    offset=off;
}

public int compareTo(Object other)
{
    Offsets otheroff=(Offsets)other;
    return objnum-otheroff.objnum;
}

/** Binary search the array for a particular object number and return its relative
 * file offset, or -1 if not found.
 */
public static int findOffset(Offsets offArray[], int objNum)
{
    Offsets key = new Offsets(objNum,0);
    int where = Arrays.binarySearch(offArray, key);
    if (where<0)
        return -1;
    else
        return offArray[where].offset;
}
}


