/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.net.*;

/**
 * A class loader for a WebSite.
 */
public class WebClassLoader extends ClassLoader {

    // A WebSite
    WebSite             _site;
    
/**
 * Creates a new DataClassLoader.
 */
public WebClassLoader(WebSite aSite)  { this(WebSite.class.getClassLoader(), aSite); }
    
/**
 * Creates a new DataClassLoader.
 */
public WebClassLoader(ClassLoader aParent, WebSite aSite)  { super(aParent); _site = aSite; }

/**
 * Returns the WebSite.
 */
public WebSite getSite()  { return _site; }

/**
 * Returns resource as string.
 */
public WebURL getURL(String aPath)
{
    // Check for build file
    WebFile file = getBuildFile(aPath);
    if(file!=null)
        return file.getURL();
    
    // Get URL string for class and resource (decoded)
    String path = aPath.startsWith("/")? aPath.substring(1) : aPath;
    URL url = getResource(path); if(url==null) return null;
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
 * Returns resource as string.
 */
public InputStream getResourceAsStream(String aPath)
{
    // If build file found, return input stream for bytes
    WebFile file = getBuildFile(aPath);
    if(file!=null)
        return file.getInputStream();

    // Do normal version
    return super.getResourceAsStream(aPath);
}

/**
 * Override to find class.
 */
protected Class<?> findClass(String aName) throws ClassNotFoundException
{
    // Try normal version
    try { return super.findClass(aName); }
    catch(ClassNotFoundException e) { }

    // If class is build file, define class
    String path = '/' + aName.replace('.', '/').concat(".class");
    WebFile cfile = getBuildFile(path);
    if(cfile!=null) {
        byte bytes[] = cfile.getBytes();
        return defineClass(aName, bytes, 0, bytes.length);
    }
    
    // Do normal version
    return super.findClass(aName);
}

/**
 * Returns a class file for given class file path.
 */
protected WebFile getBuildFile(String aPath)
{
    try { return _site.getFile(aPath); }
    catch(ResponseException e)  { return null; }
}

}