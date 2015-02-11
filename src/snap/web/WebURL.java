/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.net.*;
import snap.util.StringUtils;

/**
 * A class to represent a URL for a WebSite and WebFile (it can be both for nested sources).
 * Has the form: [Scheme:][//Authority][/Path[!/Path]][?Query][#HashTag].
 * Authority has the form: [UserInfo@]Host[:Port].
 * 
 * WebURL is a thin wrapper around standard URL, but provides easy access to the WebSite and WebFile.
 */
public class WebURL {

    // The standard URL
    URL             _url;
    
    // The path string
    String          _path;
    
    // The site URL string
    String          _siteURLS;

    // The URL of WebSite this WebURL belongs to (just this WebURL if no path)
    WebURL          _siteURL;
    
    // The WebSite for the URL
    WebSite         _asSite;
    
    // The WebFile for the URL
    WebFile         _file;

/**
 * Creates a new WebURL for given URL string.
 */
protected WebURL(String aSpec)
{
    try { setURL(WebURLHandler.createURL(aSpec)); }
    catch(Exception e)  { throw new RuntimeException(e); }
}

/**
 * Returns a URL for given object.
 */
public static WebURL getURL(Object anObj)  { return WebURLHandler.getURL(anObj); }

/**
 * Returns a URL for given class and resource name.
 */
public static WebURL getURL(Class aClass, String aName)  { return WebURLHandler.getURL(aClass, aName); }

/**
 * Returns a URL for given URL and relative URL string.
 */
public static WebURL getURL(WebURL aURL, String aName)  { return WebURLHandler.getURL(aURL, aName); }

/**
 * Returns the standard URL.
 */
public URL getURL()  { return _url; }

/**
 * Sets the standard URL.
 */
void setURL(URL aURL)
{
    // Set URL and path
    _url = aURL; _path = _url.getPath();
    
    // If there is no path, set SiteURL to this WebURL and return
    if(_path==null || _path.length()==0) { _siteURL = this; _path = null; return; }
    
    // Get site URL String
    StringBuffer sb = new StringBuffer().append(_url.getProtocol()).append(':');
    if(_url.getAuthority()!=null) sb.append("//").append(_url.getAuthority());
    int ind = _path.lastIndexOf('!');
    if(ind>0) { sb.append(_path, 0, ind); _path = _path.substring(ind+1); }
    _siteURLS = sb.toString();
}

/**
 * Returns the URL string.
 */
public String getString()  { return _url.toExternalForm(); }

/**
 * Returns the URL Scheme (lower case).
 */
public String getScheme()  { return _url.getProtocol(); }

/**
 * Returns the Host part of the URL (the Authority minus the optional UserInfo and Port).
 */
public String getHost()  { return _url.getHost(); }

/**
 * Returns the port of the URL.
 */
public int getPort()  { return _url.getPort(); }

/**
 * Returns the part of the URL string that describes the file path.
 */
public String getPath()  { return _path; }

/**
 * Returns the last component of the file path.
 */
public String getPathName()  { return StringUtils.getPathFileName(getPath()); }

/**
 * Returns the last component of the file path minus any '.' extension suffix.
 */
public String getPathNameSimple()  { return StringUtils.getPathFileNameSimple(getPath()); }

/**
 * Returns the part of the URL string that describes the query.
 */
public String getQuery()  { return _url.getQuery(); }

/**
 * Returns the value for given Query key in URL, if available.
 */
public String getQueryValue(String aKey)  { return new MapString(getQuery()).getValue(aKey); }

/**
 * Returns the hash tag reference from the URL as a simple string.
 */
public String getRef()  { return _url.getRef(); }

/**
 * Returns the value for given HashTag key in URL, if available.
 */
public String getRefValue(String aKey)  { return getRefMap().getValue(aKey); }

// Returns the hash tag reference of the URL as a MapString.
private MapString getRefMap()  { return _rm!=null? _rm : (_rm=new MapString(getRef())); } MapString _rm;

/**
 * Returns the source of this URL.
 */
public WebSite getSite()  { return getSiteURL().getAsSite(); }

/**
 * Returns the URL for the source of this URL.
 */
public WebURL getSiteURL()  { return _siteURL!=null? _siteURL : (_siteURL=new WebURL(_siteURLS)); }

/**
 * Returns whether file has been set/loaded for this URL.
 */
public boolean isFileSet()  { return _file!=null; }

/**
 * Returns the file for the URL.
 */
public WebFile getFile()  { return _file!=null? _file : (_file=getFileImpl()); }

/**
 * Returns the file for the URL.
 */
protected WebFile getFileImpl()
{
    String path = getPath(); WebSite site = getSite();
    WebFile file = path!=null? site.getFile(path) : site.getRootDirectory();
    if(file!=null && file._url==null) file._url = this;
    return file;
}

/**
 * Creates a file for the URL.
 */
public WebFile createFile(boolean isDir)
{
    String path = getPath(); WebSite site = getSite();
    WebFile file = path!=null? site.createFile(path, isDir) : site.getRootDirectory();
    if(file!=null && file._url==null) file._url = this;
    if(_file==null) _file = file;
    return file;
}

/**
 * Returns whether URL specifies only the file (no query/hashtags).
 */
public boolean isFileURL()  { return getQuery()==null && getRef()==null; }

/**
 * Returns the URL for the file only (no query/hashtags).
 */
public WebURL getFileURL()  { return isFileURL()? this : new WebURL(getFileURLString()); }

/**
 * Returns the URL string for the file only (no query/hashtags).
 */
public String getFileURLString()
{
    String str = getString(); int ind = str.indexOf('?'); if(ind<0) ind = str.indexOf('#');
    if(ind>=0) str = str.substring(0, ind);
    return str;
}

/**
 * Returns whether URL specifies only file and query (no hashtag references).
 */
public boolean isQueryURL()  { return getRef()==null; }

/**
 * Returns the URL for the file and query only (no hashtag references).
 */
public WebURL getQueryURL()  { return isQueryURL()? this : new WebURL(getQueryURLString()); }

/**
 * Returns the URL string for the file and query only (no hashtag references).
 */
public String getQueryURLString()
{
    String str = getString(); int ind = str.indexOf('#'); if(ind>=0) str = str.substring(0, ind);
    return str;
}

/**
 * Returns the site for the URL.
 */
public WebSite getAsSite()  { return _asSite!=null? _asSite : (_asSite=Web.getWeb().getSite(this)); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    WebURL other = anObj instanceof WebURL? (WebURL)anObj : null; if(other==null) return false;
    return _url.equals(other._url);
}

/**
 * Standard HashCode implementation.
 */
public int hashCode()  { return _url.hashCode(); }

/**
 * Standard toString implementation.
 */
public String toString()  { return "WebURL: " + getString(); }

}