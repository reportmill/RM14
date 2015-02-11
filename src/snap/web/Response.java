/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.List;

/**
 * The response.
 */
public class Response {

    // The request that generated this response
    Request          _request;
    
    // The response code
    int              _code;
    
    // The response time
    long             _time;
    
    // The response content/data type
    DataType         _dataType;
    
    // The response bytes
    byte             _bytes[];
    
    // The response text
    String           _text;
    
    // The response file
    WebFile          _file;
    
    // The response files (if directory get)
    List <WebFile>   _files;
    
    // An exception if response represents an exception
    Throwable        _exception;
    
    // Constants for response codes (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
    public static final int OK = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int EXCEPTION_THROWN = 420;

/**
 * Returns the request.
 */
public Request getRequest()  { return _request; }

/**
 * Sets the request.
 */
public void setRequest(Request aRequest)  { _request = aRequest; }

/**
 * Returns the request URL.
 */
public WebURL getRequestURL()  { return _request.getURL(); }

/**
 * Returns the site for the request/response.
 */
public WebSite getSite()  { return _request.getSite(); }

/**
 * Returns the response code.
 */
public int getCode()  { return _code; }

/**
 * Sets the response code.
 */
public void setCode(int aCode)  { _code = aCode; }

/**
 * Returns the code message.
 */
public String getCodeString()
{
    switch(_code) {
        case OK: return "OK";
        case UNAUTHORIZED: return "Unauthorized";
        case NOT_FOUND: return "Not Found";
        case METHOD_NOT_ALLOWED: return "Method Not Allowed";
        default: return "Unknown";
    }
}

/**
 * Returns the response time.
 */
public long getTime()  { return _time; }

/**
 * Returns the response type.
 */
public DataType getDataType()  { return _dataType!=null? _dataType : (_dataType=getDataTypeImpl()); }

/**
 * Returns a data type for this response based on the file.
 */
protected DataType getDataTypeImpl()  { return _file!=null? _file.getDataType() : DataType.Unknown; }

/**
 * Sets the data type.
 */
protected void setDataType(DataType aType)  { _dataType = aType; }

/**
 * Returns the file.
 */
public WebFile getFile()  { return _file; }

/**
 * Sets the file.
 */
public void setFile(WebFile aFile)  { _file = aFile; }

/**
 * Returns the files (for directory request).
 */
public List <WebFile> getFiles()  { return _files; }

/**
 * Sets the files (for directory request).
 */
public void setFiles(List <WebFile> theFile)  { _files = theFile; }

/**
 * Returns the bytes.
 */
public byte[] getBytes()  { return _bytes!=null? _bytes : (_bytes=getBytesImpl()); }

/**
 * Returns the bytes.
 */
protected byte[] getBytesImpl()
{
    if(_file!=null && _file.isBytesSet()) return _file.getBytes();
    if(_text!=null) return _text.getBytes();
    return null;
}

/**
 * Sets the response bytes.
 */
public void setBytes(byte theBytes[])  { _bytes = theBytes; }

/**
 * Returns the text of the response.
 */
public String getText()  { return _text!=null? _text : (_text=getTextImpl()); }

/**
 * Returns the text.
 */
protected String getTextImpl()
{
    if(_file!=null) return _file.getText();
    if(_bytes!=null) return new String(_bytes);
    return null;
}

/**
 * Sets the text.
 */
public void setText(String aString)  { _text = aString; }

/**
 * Returns the exception.
 */
public Throwable getException()  { return _exception; }

/**
 * Sets the exception.
 */
public void setException(Throwable aThrowable)  { _exception = aThrowable; }

/**
 * Standard toString implementation.
 */
public String toString() { return "Response " + getCode() + ' ' + getCodeString() + ' ' + getRequestURL().getString(); }

}