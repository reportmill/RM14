package com.reportmill.pdf.reader;
import java.io.*;

/*
 * Implementations of pdf decode filters
 */
public class RBCodec {

/**  ASCII85 decoder */
public static byte[] bytesForASCII85(byte bytes[], int offset, int length)
{
    int end=offset+length;
    int decode=0, matchlen=0;
    byte zzzz[] = {0,0,0,0};
    ByteArrayOutputStream out = new ByteArrayOutputStream(length*4/5);
    
    for(int i=offset; i<end; ++i) {
        byte c=bytes[i];
        //skip whitespace
        if ((c==' ') || (c=='\t') || (c=='\n') || (c=='\r') || (c=='\f'))
            continue;
        //valid ascii85 char
        if ((c>='!') && (c<='u')) {
            // ascii85 is 5 bytes interpreted as 5 base-85 digits
            decode = decode*85+c-'!';
            if (++matchlen==5) {
                // convert to 4 base-256 digits and reset
                out.write((decode>>24) & 0xff);
                out.write((decode>>16) & 0xff);
                out.write((decode>>8) & 0xff);
                out.write(decode & 0xff);
                decode=0;
                matchlen=0;
            }
        }
        else if ((c=='z') && (matchlen==0)) {
            // z is shorthand for 4 zero bytes
            out.write(zzzz,0,4);
        }
        else if ((c=='~') && (i<end-1) && (bytes[i+1]=='>')) {
            // ~> is the EOD marker
            break;
        }
        else throw new PDFException("Illegal character in ASCII85 stream");
    }
    
    //take care of odd bytes at the end
    if (matchlen==1)
        throw new PDFException("wrong number of characters in ASCII85 stream");
    // oh my god how freaky
    // instead of leaving the last bytes as the low digits of the base-85 number,
    // they are actually the high digits, with a 1 added to the least significant digit
    // thrown in for good measure.  Don't ask me.
    ++decode;
    for(int i=matchlen; i<5; ++i)
        decode*=85;
    
    for(int i=0; i<matchlen-1; ++i)
        out.write((decode>>((3-i)*8)) & 0xff);
   
    return out.toByteArray();
}

/** ASCIIHex decoder. */
public static byte[] bytesForASCIIHex(byte bytes[], int offset, int length)
{
    // Due to whitespace & odd chars at end, outlength not necessarily inlength/2
    ByteArrayOutputStream out = new ByteArrayOutputStream((length+1)/2);
    int end = offset+length;
    int matchlen=0;
    int decode = 0;
    int nibble;
    
    for(int i=offset; i<end; i++) {
        byte c = bytes[i];
        
        //skip whitespace
        if ((c==' ') || (c=='\t') || (c=='\n') || (c=='\r') || (c=='\f'))
            continue;
        
        if ((c>='a') && (c<='f'))
            nibble = c-'a'+10;
        else if ((c>='A') && (c<='F'))
            nibble = c-'A'+10;
        else if ((c>='0') && (c<='9'))
            nibble = c-'0';
        else if (c=='>')
            break;
        else
            throw new PDFException("Illegal character in ASCIIHex stream");
        decode = (decode<<4) | nibble;
        if (++matchlen==2) {
            out.write(decode);
            decode=0;
            matchlen=0;
        }
    }
    if (matchlen==1)
        out.write(0);
    
    return out.toByteArray();
}

/** ASCIIHex decoder for hex strings */
public static byte[] bytesForASCIIHex(String hstring)
{
    byte ascii[];
    try {
        ascii = hstring.getBytes("US-ASCII");
        if((ascii.length>1) && (ascii[0]=='<'))
            return bytesForASCIIHex(ascii,1,ascii.length-1);
        throw new PDFException("String is not a hexadecimal string");
    }
    // this should never happen
    catch(UnsupportedEncodingException uee) { throw new PDFException("Couldn't convert string to ascii"); }
}

}