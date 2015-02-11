package com.reportmill.pdf.writer;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/**
 * This class represents a PDF stream object.
 */
public class PDFStream implements Cloneable {
    
    // Stream bytes
    byte          _bytes[];
    
    // Stream dictionary
    Map           _dict;

/**
 * Creates a new stream from the byte array and map.
 */
public PDFStream(byte theBytes[], Map aMap)
{
    _bytes = theBytes;
    _dict = aMap==null? new Hashtable() : new Hashtable(aMap);
}

/**
 * Returns the stream dictionary.
 */
public Map getDictionary()  { return _dict; }

/**
 * The dict for a stream may specify multiple filters. The "Filter" property is either a single filter name or
 * on array of filter names. If it's an array, the PDF reader will apply the filters in the order they appear.
 * Call this method with the name of the filter after applying the encoding to the data. 
 */
public void addFilter(String aName)
{
    Object filters = _dict.get("Filter");
    
    // If no filters yet, just add aName
    if(filters==null)
        _dict.put("Filter", aName);
    
    // If one filter, convert to list
    else if(filters instanceof String) {
        List list = new Vector(2);
        list.add(aName);
        list.add(filters);
        _dict.put("Filter", list);
    }
    
    // If list of filters, add new filter
    else ((List)filters).add(0, aName);
}

/**
 * Returns Flate encoded bytes from the given raw bytes.
 */
public static byte[] getBytesEncoded(RMPDFWriter aWriter, byte bytes[], int offset, int length)
{
    // Get byte array output stream for bytes
    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(bytes.length);
    
    // Get deflator output stream for bytes (use shared deflater)
    DeflaterOutputStream deflaterOutStream = new DeflaterOutputStream(byteOutStream, aWriter.getDeflater());

    // Catch exceptions
    try {

    // Write bytes to deflator output stream
    deflaterOutStream.write(bytes);
    deflaterOutStream.close();
    
    // Reset shared deflater
    aWriter.getDeflater().reset();
    
    // Catch exceptions
    } catch(Exception e) { e.printStackTrace(); return null; }
    
    // Return bytes
    return byteOutStream.toByteArray();
}

/**
 * Standard clone implementation.
 */
public PDFStream clone()
{
    PDFStream copy; try { copy = (PDFStream)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
    copy._dict = new Hashtable(_dict);
    return copy;
}

/**
 * Writes a stream to a pdf buffer.
 */
public void writePDF(RMPDFWriter aWriter)
{
    // Get bytes and length
    byte bytes[] = _bytes;
    int length = _bytes.length;
  
    // Compress the data if it hasn't already been filtered
    Object filter = _dict.get("Filter");
    if(filter==null && length>64 && aWriter.getCompress()) {
        
        // Get flate encoded bytes and swap them in if smaller
        byte bytes2[] = getBytesEncoded(aWriter, bytes, 0, length);
        if(bytes2.length<length) {
            bytes = bytes2;
            length = bytes2.length;
            addFilter("/FlateDecode");
        }
    }
    
    // If encryption is enabled, encrypt the stream data
    if(aWriter.getEncryptor() != null)
        bytes = aWriter.getEncryptor().encryptBytes(bytes);

    // Now set the length key to represent the real length
    _dict.put("Length", new Integer(length));
    
    // Stick dict description in stream, followed by "stream" keyword, data & "endstream" keyword
    aWriter.writeXRefEntry(_dict);
    
    // Get pdf file buffer and write bytes
    PDFBuffer buffer = aWriter.getBuffer();
    buffer.appendln();
    buffer.appendln("stream");
    buffer.append(bytes, 0, length);
    buffer.appendln();
    buffer.appendln("endstream");
}

}