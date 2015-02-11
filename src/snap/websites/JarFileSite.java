/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.websites;
import java.io.*;
import java.net.*;
import java.util.jar.JarFile;
import java.util.zip.*;
import snap.util.FileUtils;
import snap.web.*;

/**
 * A WebSite subclass for Jar files.
 */
public class JarFileSite extends ZipFileSite {

    // Whether to trim entries via isInterestingPath
    boolean       _trim;

/**
 * Override to turn on file trimming from system jars. 
 */
protected void setURL(WebURL aURL)
{
    // Do normal version
    super.setURL(aURL);
    
    // Turn on file trimming if system jar
    String urls = aURL.getString();
    _trim = urls.contains("/rt.jar") || urls.contains("/jfxrt.jar") || urls.contains("/JavaInvetor1.jar") ||
        urls.contains("/RMStudio14.jar");
}

/**
 * Override to LoadFiles if Zip URL is really just a local (bin) directory.
 */
protected void loadArchive()
{
    if(getURL().getScheme().equals("file") && getURL().getPathName().indexOf('.')<0) {
        String path = getURL().getPath(); loadFiles("/", new File(path)); }
    else super.loadArchive();
}

/**
 * Override to do weird (Jar)URLConnection thing if URL not local.
 */
protected ZipFile createZipFile() throws Exception
{
    // If HTTP or .pack.gz, use "jar:" url
    if(getURL().getScheme().equals("http") || getURLString().endsWith(".pack.gz")) try {
        URL url = new URL("jar:" + getURLString() + "!/");
        JarURLConnection conn = (JarURLConnection)url.openConnection();
        return conn.getJarFile();
    }
    catch(Exception e) { System.err.println(e); }
    
    // Otherwise, get local file and create JarFile
    File sfile = getStandardFile(); if(sfile==null) return null; // Get local file
    return new JarFile(sfile); // Create/return ZipFile
}

/**
 * Override to ignore certain Jar paths.
 */
protected void addZipEntry(ZipEntry anEntry)
{
    if(!anEntry.isDirectory() && _trim && !isInterestingPath(anEntry.getName())) return;
    super.addZipEntry(anEntry);
}

/**
 * Adds an entry (override to ignore).
 */
protected boolean isInterestingPath(String aPath)
{
    // Bogus excludes
    if(aPath.startsWith("sun")) return false;
    if(aPath.startsWith("com/sun")) return false;
    if(aPath.startsWith("com/apple")) return false;
    if(aPath.startsWith("javax/swing/plaf")) return false;
    if(aPath.startsWith("org/omg")) return false;
    int dollar = aPath.endsWith(".class")? aPath.lastIndexOf('$') : -1;
    if(dollar>0 && Character.isDigit(aPath.charAt(dollar+1))) return false;
    return true;
}

/**
 * Loads this JarFileSite from files (starting with root and recursing).
 */
protected void loadFiles(String aPath, File aFile)
{
    // Create JarDataFile for file and save
    JarDataFile dfile = (JarDataFile)createFile(aPath, aFile.isDirectory());
    dfile.setStandardFile(aFile);
    try { dfile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }

    // If directory, recurse over directory files
    if(aFile.isDirectory()) {
        String dirPath = aPath.equals("/")? "/" : aPath + "/";
        File files[] = aFile.listFiles(); if(files==null) return;
        for(File file : files) { String name = file.getName();
            if(name.equalsIgnoreCase(".DS_Store")) continue; // Skip funky apple files
            loadFiles(dirPath + file.getName(), new File(aFile, file.getName()));
        }
    }
}

/**
 * Override to return ZipDataFile.
 */
protected JarDataFile createFileImpl(String aPath, boolean isDirectory)  { return new JarDataFile(); }

/**
 * Returns file bytes.
 */
protected byte[] getFileBytesImpl(WebFile aFile) throws Exception
{
    JarDataFile file = (JarDataFile)aFile;
    if(file.getStandardFile()!=null) return FileUtils.getBytes(file.getStandardFile());
    return super.getFileBytesImpl(aFile);
}

/**
 * A ZipFile.
 */
public static class JarDataFile extends ZipFileSite.ZipDataFile {

    // The Java File
    File   _file;

    /** Returns the standard file. */
    public File getStandardFile()  { return _file; }
    
    /** Sets the standard file. */
    protected void setStandardFile(File aFile)  { _file = aFile; }
}

}