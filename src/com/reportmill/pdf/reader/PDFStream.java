package com.reportmill.pdf.reader;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/**
 * This class represents a PDF stream object.
 */
public class PDFStream implements Cloneable {
    
    // Stream bytes
    byte     _bytes[];
    
    // Stream dictionary
    Map      _dict;

/**
 * Creates a new stream from the byte array and map.
 */
public PDFStream(byte bytes[], Map aMap)
{
    _bytes = bytes; // no copy
    _dict = aMap==null? new Hashtable() : new Hashtable(aMap);
}

/**
 * Creates a new stream by copying the byte array and map.
 */
public PDFStream(byte bytes[], int offset, int len, Map aMap)
{
    _bytes = new byte[len]; // Make local copy of data
    System.arraycopy(bytes, offset, _bytes, 0, len);
    _dict = aMap==null? new Hashtable() : new Hashtable(aMap); // and hashtable
}

/**
 * Returns the stream bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Returns the stream dictionary.
 */
public Map getDictionary()  { return _dict; }

/**
 * The dict for a stream may specify multiple filters. The "Filter" property is either a single filter name or an
 * array of filter names. If it's an array, the PDF reader will apply the filters in the order they appear.
 * Call this method with the name of the filter after applying the encoding to the data.
 */
public void addFilter(String aName)
{
    Object filters = _dict.get("Filter");
    
    // If no filters yet, just add aName
    if(filters==null)
        _dict.put("Filter", aName);
    
    // If one filter, convert to list
    else if(filters instanceof String)
        _dict.put("Filter", Arrays.asList(aName, filters));
    
    // If list of filters, add new filter
    else ((List)filters).add(0, aName);
}

/**
 * Tests whether a specifc filter will be needed to decode the stream.
 */
public boolean usesFilter(String fName)  { return (indexOfFilter(fName)>=0); }

/** Returns the index of a particular filter in the filter chain, or -1 if not found. */
public int indexOfFilter(String fName)
{
    Object filters = _dict.get("Filter");
    
    if (filters != null) {
        if (filters instanceof String)
            return filters.equals(fName) ? 0 : -1;
        else {
            List allFilters = (List)filters;
            for(int i=0, n=allFilters.size(); i<n; ++i) 
                if (allFilters.get(i).equals(fName))
                    return i;
        }
    }
    return -1;
}

/** Returns the total number of filters with which this stream is currently encoded. */
public int numFilters()
{
    Object filters = _dict.get("Filter");
    if(filters==null) return 0;
    if(filters instanceof String) return 1;
    return ((List)filters).size();
}

/** Returns the filter parameters for a particular filter */
public Map getFilterParameters(String name)
{
    Object parameters = _dict.get("DecodeParms"); // Get the filter parameters
    int nfilters = numFilters();
    int index = indexOfFilter(name);
    
    // If there's only a single filter, the parameters should be a dictionary
    if((nfilters==1) && (index==0) && (parameters instanceof Map))
        return (Map)parameters;
    
    // otherwise it should be a list, with one dict per filter
    if((parameters instanceof List) && (index>=0))
        return (Map)((List)parameters).get(index);

    // otherwise return null
    return null;
}
    
    
/**
 * Returns the result of running the data through all the filters.
 */
public byte[] decodeStream()  { return decodeStream(numFilters()); }

/** Returns the result of running the data through the first n filters. */
public byte[] decodeStream(int nfilters)
{
    // Get filters for the stream (just return _data if none)
    Object filter = _dict.get("Filter");
    if((filter==null) || nfilters==0)
        return _bytes;

    // Get the filter parameters
    Object parameters = _dict.get("DecodeParms"); // parms?  what's a parm?
    
    // If list, run through all filters
    if(filter instanceof List) {
        List filters = (List)filter;
        List paramList = null;
        byte decoded[] = _bytes;
        int len = _bytes.length;
        int iMax = filters.size();
        
        if (iMax > nfilters)
            iMax = nfilters;
        if (parameters instanceof List)
            paramList = (List)parameters;
        
        for(int i=0; i<iMax; i++) {
            String fname = (String)filters.get(i);
            decoded = getBytesDecoded(decoded, 0, len, fname, paramList != null ? (Map)paramList.get(i) : null);
            len = decoded.length;
        }
        return decoded;
    }
    
    // If not list, just decode bytes and return
    return  getBytesDecoded(_bytes, 0, _bytes.length, (String)filter, (Map)parameters);
}

/** Returns the result of runnning the bytes through a particular filter (/FlateDecode, /LZW, /ASCII85Decode, etc.). */
public static byte[] getBytesDecoded(byte bytes[], int offset, int length, String aFilter, Map params)
{
    byte decoded[];
    
    // Get predictor parameters
    int predictor = 1, columns = 1, colors = 1, bits = 8;
    
    if (params != null) {
        Object obj = params.get("Predictor");
            
        if(obj instanceof Number) {
            predictor = ((Number)obj).intValue();
            
            obj = params.get("Columns");
            if(obj instanceof Number)
                columns = ((Number)obj).intValue();
            obj = params.get("Colors");
            if(obj instanceof Number)
                colors = ((Number)obj).intValue();
            obj = params.get("BitsPerComponent");
            if(obj instanceof Number)
                bits = ((Number)obj).intValue();
        }
    }

    // Handle FlateDecode
    if(aFilter.equals("/FlateDecode")) {

        // Get byte array input stream for compressed bytes
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(bytes, offset, length);

        // Get inflator input stream for bytes
        InflaterInputStream inflaterInStream = new InflaterInputStream(byteInStream, new Inflater(false));
        
        // Get byte array output stream for uncompressed bytes
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        
        // Get buffer for transfer
        byte chunk[] = new byte[1024];

        // Catch exceptions
        try {
            
        // Transfer from inflaterStream to byteOutStream
        for(int len=0; len>=0; len=inflaterInStream.read(chunk, 0, chunk.length))
            byteOutStream.write(chunk, 0, len);

        // get the buffer
        decoded = byteOutStream.toByteArray();
        
        // Catch Exceptions
        } catch(IOException e) { throw new PDFException(e); }
    }
    
    // Handle LZW
    else if(aFilter.equals("/LZWDecode") || aFilter.equals("/LZW")) {
    
        // Get extra parameters
        int early = 1;
        Object obj = params!=null ? params.get("EarlyChange") : null;
        if(obj instanceof Number)
            early = ((Number)obj).intValue();

        // LZW decode
        decoded = RBCodecLZW.decode(bytes, offset, length, early);
    }
    
    // handle CCITTFaxDecode
    else if (aFilter.equals("/CCITTFaxDecode")) {
        // create a decoder instance from the parameters dictionary
        RBCodecCCITTFaxDecode ccittDecoder = createCCITTFaxDecoder(params, bytes, offset, length);
        decoded = ccittDecoder.decodeStream();
    }
    // Handle ASCII85
    else if(aFilter.equals("/ASCII85Decode"))
        decoded = RBCodec.bytesForASCII85(bytes, offset, length);
    
    // Handle ASCIIHex
    else if(aFilter.equals("/ASCIIHexDecode"))
        decoded = RBCodec.bytesForASCIIHex(bytes, offset, length);
    
    else {
        // Complain if unsupported Decode
        System.err.println("Decompression type " + aFilter + " not yet supported.");
        return new byte[0];
    }
    
    // apply predictor
    return postprocessBytesForPredictor(decoded, predictor, colors, bits, columns);
}

/**
 * Flate & LZW predictor algorithms
 * Predictors are simple algorithms performed on samples prior to compression in hopes of getting better
 * compression ratios. This method is called after lzw or flate decompression in order to undo the predictor.
 */
private static byte[] postprocessBytesForPredictor(byte buffer[], int predictor, int colors, int bitspersample, int width)
{
    if (predictor==1) return buffer;  // No prediction
    if (predictor==2) { // TIFF Predictor 2  
        // Sample actually represents a distance from same sample of pixel to it's left.
        // This makes rows of the same color collapse into zeros.
        int bitsperpixel=colors*bitspersample;
        int bytesperrow=(bitsperpixel*width+7)/8;
        int height = buffer.length/bytesperrow;
        int row,column,src=0;
        if (bitspersample==8) {
            for(row=0; row<height; ++row) {
                for(column=colors; column<bytesperrow; ++column)
                   buffer[src+column] += buffer[src+column-colors];
                src+=bytesperrow;
            }
        }
        else {
           System.err.println("Predictor not yet implemented for this image configuration");
           System.err.println(" bitspersample="+bitspersample+", samples per pixel="+colors);
        }
    }
    else if ((predictor>=10) && (predictor<=15)) {
        /* PNG Predictors
         * In images using PNG predictors, the predictor is selected on a per row basis.
         * The first byte in the row is the predictor selector for that row.
         * Also, unlike the TIFF predictor, which works on samples, PNG predictors always
         * work on bytes, regardless of the size of the components.
         * For a given byte, the algorithms select the 'corresponding' byte in the 
         * three neighboring pixels (left, above, above-left).
         * Since PNG predictors include an extra tag byte for each row, conversion is
         * done into a new buffer instead of in place.
         */
        /* The wierdest thing of all, however, is that the predictor's bitsPerSample/samplesPerPixel
         * doesn't necessarily match that of the image.
         * An 8 bit per sample rgb image can be compressed with a 4 bits per sample,
         * 3 sample per pixel predictor.  Seems like that would be pointless, but Illustrator
         * and Acrobat are happy to generate images like that.
         */
         
        int bitsperpixel=colors*bitspersample;
        int bytesperpixel=(bitsperpixel+7)/8;
        int dest_bytesperrow=(bitsperpixel*width+7)/8;
        int src_bytesperrow = dest_bytesperrow+1;  // +1 for tag byte
        int height = buffer.length/(src_bytesperrow);
        int row,column,src=0,dest=0;
        byte x,left,above,aboveleft;
        byte newbuffer[];
        byte newbyte;
        
        // since the predictor pixelsize may not match the pixelsize 
        // of the of the image, there may be a final
        // incomplete scanline.
        if (buffer.length%src_bytesperrow != 0) {
            ++height;
            //...
        }
        // The real image buffer size is the size of the post-predicted buffer
        // minus one byte for each predicted scanline (the predictor tag byte)
        newbuffer = new byte[buffer.length - height];

        for(row=0; row<height; ++row) {
            predictor = buffer[src];
            // last scanline may not be complete
            if (row==height-1)
                src_bytesperrow=buffer.length-src;
            for(column=1; column<src_bytesperrow; ++column) {
                x = buffer[src+column];
                left = column-1<bytesperpixel ? 0 : newbuffer[dest+column-1-bytesperpixel];
                if (predictor==0) // None
                    newbyte=x;
                else if (predictor==1) // Sub
                    newbyte=(byte)(x+left);
                else {
                    above = row==0 ? 0 : newbuffer[dest+column-1-dest_bytesperrow];
                    if (predictor==2) // Up
                        newbyte=(byte)(x+above);
                    else if (predictor==3) // Average
                        newbyte=(byte)(x+(left+above)/2);
                    else if (predictor==4) { //Paeth
                        int p,pa,pb,pc,pr;
                        if ((row==0)||(column<bytesperpixel)) 
                            aboveleft = 0;
                        else 
                            aboveleft = newbuffer[dest+column-1-dest_bytesperrow-bytesperpixel];
                        //TODO : double check sign extension, since all java bytes are signed
                        p =  left+above-aboveleft;
                        pa = Math.abs(p-left) & 255;
                        pb = Math.abs(p-above) & 255;
                        pc = Math.abs(p-aboveleft) & 255;
                        if ((pa<=pb) && (pa<=pc)) 
                            pr = left;
                        else if (pb<=pc) 
                            pr = above;
                        else pr = aboveleft;
                        newbyte  = (byte)(x + pr);
                    }
                    else throw new PDFException("Illegal value for PNG predictor tag");
                }
                newbuffer[dest+column-1] = newbyte;
            }
           src+=src_bytesperrow;
           dest+=dest_bytesperrow;
        }
    buffer = newbuffer;
    }
    else System.err.println("Predictor algorithm #"+predictor+" not applied - image will look funny");
    
    return buffer;
}


/** Standard clone implementation. */
public Object clone()
{
    PDFStream copy = null;
    try { copy = (PDFStream)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
    copy._dict = new Hashtable(_dict);
    return copy;
}

/** Utility method to create a CCITTFaxDecoder from the stream's decodeParameters dictionary. */
public static RBCodecCCITTFaxDecode createCCITTFaxDecoder(Map params, byte bytes[], int offset, int len)
{
    // Every value has a default
    int K = 0;
    boolean eolRequired = false;
    boolean byteAligned = false;
    boolean eoblockRequired = true;
    boolean blackIsOne = false;
    int allowedDamagedRows = 0;
    int rows = 0, columns = 1728;
    
    // pull the parameter values out of the dictionary
    if (params != null) {
        Object pval = params.get("K");
        if (pval instanceof Number)
            K=((Number)pval).intValue();
        pval=params.get("EndOfLine");
        if (pval instanceof Boolean)
            eolRequired=((Boolean)pval).booleanValue();
        pval=params.get("EncodedByteAlign");
        if (pval instanceof Boolean)
            byteAligned=((Boolean)pval).booleanValue();
        pval = params.get("Columns");
        if (pval instanceof Number)
            columns=((Number)pval).intValue();
        pval = params.get("Rows");
        if (pval instanceof Number)
            rows=((Number)pval).intValue();
        pval=params.get("EndOfBlock");
        if (pval instanceof Boolean)
            eoblockRequired=((Boolean)pval).booleanValue();
        pval=params.get("BlackIs1");
        if (pval instanceof Boolean)
            blackIsOne=((Boolean)pval).booleanValue();
        pval = params.get("DamagedRowsBeforeError");
        if (pval instanceof Number)
            allowedDamagedRows=((Number)pval).intValue();
    }
    
    // return the new decoder
    return new RBCodecCCITTFaxDecode(bytes, offset, K, rows, columns, allowedDamagedRows, 
            byteAligned, eolRequired, eoblockRequired, blackIsOne);
}


}
