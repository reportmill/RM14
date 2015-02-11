/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.io.IOException;
import java.net.*;

/**
 * A class to load URLs.
 */
public class WebURLHandler {

/**
 * Returns a URL for given string.
 */
static URL createURL(String aSpec) throws MalformedURLException
{
    // Get scheme
    String spec = aSpec; if(spec.startsWith("/")) spec = "file:" + spec;
    int ind = spec.indexOf(':'); if(ind<0) return new URL(aSpec);
    String scheme = spec.substring(0, ind).toLowerCase();
    
    // Handle unsupported schemes
    if(scheme.equals("class")) return new URL(null, spec, new BogusURLStreamHandler());
    if(scheme.equals("local")) return new URL(null, spec, new BogusURLStreamHandler());
    if(scheme.equals("eclipse")) return new URL(null, spec, new BogusURLStreamHandler());
    if(scheme.equals("sandbox")) return new URL(null, spec, new BogusURLStreamHandler());
    return new URL(spec);
}

/**
 * Returns a URL for given string.
 */
public static WebURL getURL(Object anObj)
{
    // Handle null, WebURL, WebFile
    if(anObj==null || anObj instanceof WebURL) return (WebURL)anObj;
    if(anObj instanceof WebFile) return ((WebFile)anObj).getURL();
    
    // Handle String; See if it's our silly "Jar:/com/rm" format, or URL or File
    if(anObj instanceof String) { String string = (String)anObj;
        if(string.startsWith("Jar:/com/reportmill")) anObj = WebURL.class.getResource(string.substring(4));
        else {
            URL url = null; try { anObj = url = createURL(string); } catch(Exception e) { }
            if(url==null) {
                if(string.indexOf(":\\")>0) string = '/' + string.replace('\\', '/');
                else if(string.startsWith("\\")) string = string.replace('\\', '/');
                anObj = new File(string);
            }
        }
    }
    
    // Handle File: Convert to Canonical URL to normalize path
    if(anObj instanceof File) { File file = (File)anObj;
        try { anObj = file.getCanonicalFile().toURI().toURL(); } catch(Exception e) { } }
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that)
    if(anObj instanceof URL) { URL url = (URL)anObj;
        try {
            String urls = url.toExternalForm(); urls = URLDecoder.decode(urls, "UTF-8");
            if(url.getProtocol().equals("jar")) urls = urls.substring(4);
            else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
            return new WebURL(urls);
        }
        catch(Exception e) { }
    }
    
    // Handle Class
    if(anObj instanceof Class) return getURL((Class)anObj, null);
    throw new RuntimeException("No URL found for: " + anObj);
}

/**
 * Returns a URL for given class and resource name.
 */
public static WebURL getURL(Class aClass, String aName)
{
    // Get absolute path to class/resource
    String path = '/' + aClass.getName().replace('.', '/') + ".class";
    if(aName!=null) { int sep = path.lastIndexOf('/'); path = path.substring(0, sep+1) + aName; }
    
    // If class loader is DataClassLoader, have it return URL
    ClassLoader cldr = aClass.getClassLoader();
    if(cldr instanceof WebClassLoader)
        return ((WebClassLoader)cldr).getURL(path);
    
    // Get URL string for class and resource (decoded)
    URL url = aClass.getResource(path); if(url==null) return null;
    String urls = url.toExternalForm();
    try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
    
    // If Jar resource, just strip jar protocol, otherwise, install "!/" separator between URL root and resource path
    if(url.getProtocol().equals("jar")) urls = urls.substring(4);
    else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
    else urls = urls.replace(path, '!' + path);
    
    // Set URL string
    return new WebURL(urls);
}

/**
 * Returns a URL for given base URL and relative path or URL string.
 */
public static WebURL getURL(WebURL aURL, String aPath)
{
    if(aURL==null || aPath.indexOf(':')>=0) return getURL(aPath);
    if(aPath.startsWith("/")) aURL.getSite().getURL(aPath);
    String urls = aURL.getString(); urls = PathUtils.getChild(urls, aPath);
    return getURL(urls);
}

/**
 * A URLStreamHandlerFactory.
 */
private static class BogusURLStreamHandler extends URLStreamHandler
{
    /** OpenConnection. */
    protected URLConnection openConnection(URL u) throws IOException  { return null; }
}

}