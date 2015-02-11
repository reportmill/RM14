package com.reportmill.pdf.reader;

/**
 * An implementation of interface CharStream, where the stream can contain any
 * binary data.  It holds a buffer to the entire contents and keeps track of the
 * the current position within that buffer.
 */

public final class PDFCharStream implements CharStream
{
  public static final boolean staticFlag = false;
  int tokenBegin;
  public int bufpos = 0;
  private byte[] buffer;

  public PDFCharStream(byte b[])
  {
  super();
  buffer = b;
  bufpos = tokenBegin = 0;
  }
  
  public final char readChar() throws java.io.IOException
  {
     if (bufpos >= buffer.length) 
       throw new java.io.EOFException();
     
     char c = (char)((char)0xff & buffer[bufpos]);
     ++bufpos;
     return c;
  }

  /* This stream is used for binary data, so lines and columns are irrelevant. */
  public int getColumn() { return -1; }
  public int getLine() { return -1; }
  public int getEndColumn() { return -1; }
  public int getEndLine() { return -1; }
  public int getBeginColumn() { return -1; }
  public int getBeginLine() { return -1; }

  public final void backup(int amount) {
    bufpos -= amount;
    if (bufpos < 0)
       bufpos = 0;
  }

  public final char BeginToken() throws java.io.IOException
  {
     tokenBegin = bufpos;
     char c = readChar();
     return c;
  }

 public final String GetImage()
  {     
     if (bufpos > tokenBegin) {
       int len = bufpos - tokenBegin;
       char imageChars[] = new char[len];
       for(int i=0; i<len; ++i)
         imageChars[i] = (char)((char)0xff & buffer[tokenBegin+i]);
       return new String(imageChars);
       }
     else
       return null;
  }

  public char[] GetSuffix(int len)
  {
     char[] ret = new char[len];
     for(int i=0; i<len; ++i) 
       ret[i] = (char)((char)0xff & buffer[bufpos-len+i]);
     return ret;
  }

  // returns an array of the next n bytes in the data and advances the pointer
  public byte[] GetNextBytes(int n) throws java.io.IOException
  {
     if (bufpos+n>=buffer.length) throw new java.io.EOFException();
     
     byte[] ret = new byte[n];
     System.arraycopy(buffer, bufpos, ret, 0, n);
     bufpos += n;
     return ret;
  }

  public void Done()
  {
     buffer = null;
  }

  public int currentLocation() { return bufpos; }
  public byte[] buffer() { return buffer; }
  
  public void ReInit(int offset)
  {
    tokenBegin = bufpos = offset;
  }

  public void ReInit(byte newdata[], int offset)
  {
      buffer=newdata;
      ReInit(offset);
  }
  
}
