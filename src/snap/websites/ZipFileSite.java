/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.websites;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import snap.util.SnapUtils;
import snap.web.*;

/**
 * A WebSite subclass for Zip files.
 */
public class ZipFileSite extends WebSite {

    // The ZipFile
    ZipFile      _zipFile;
    
    // Whether data source is loaded
    boolean      _loaded;
    
/**
 * Loads WebSite from Archive (zip) file.
 */
protected void loadArchive()
{
    ZipFile zipFile; try { zipFile = getZipFile(); } catch(Exception e) { throw new RuntimeException(e); }
    if(zipFile==null) return;
    List <ZipEntry> zipEntries = (List)Collections.list(zipFile.entries());
    for(ZipEntry ze : zipEntries) addZipEntry(ze);
}

/**
 * Returns the ZipFile.
 */
protected ZipFile getZipFile() throws Exception { return _zipFile!=null? _zipFile : (_zipFile=createZipFile()); }

/**
 * Creates the ZipFile.
 */
protected ZipFile createZipFile() throws Exception
{
    File sfile = getStandardFile(); if(sfile==null) return null; // Get local file
    return new ZipFile(sfile); // Create/return ZipFile
}

/**
 * Returns a Java file for the zip file URL (copied to Sandbox if remote).
 */
protected File getStandardFile() throws Exception
{
    WebURL url = getURL();
    WebFile file = url.getFile(); if(file==null) return null;
    WebSite site = url.getSite();
    if(site instanceof FileSite)
        return file.getStandardFile();
    
    // Get Sandbox file - create/update if needed
    WebSite sbox = site.getSandbox();
    WebFile dfile = sbox.getFile(url.getPath());
    if(dfile==null) { // || dfile.getModifiedTime()<file.getModifiedTime()) {
        if(dfile==null) dfile = sbox.createFile(file.getPath(), false);
        dfile.setBytes(file.getBytes());
        dfile.save();
    }
    
    // Return local java file
    return dfile.getStandardFile();
}

/**
 * Adds a ZipEntry to WebSite.
 */
protected void addZipEntry(ZipEntry anEntry)
{
    String path = "/" + anEntry.getName();
    if(path.endsWith("/") && path.length()>1) path = path.substring(0, path.length()-1);
    ZipDataFile zfile = (ZipDataFile)createFile(path, anEntry.isDirectory());
    zfile.setZipEntry(anEntry);
    try { zfile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Override to return ZipDataFile.
 */
protected WebFile createFileImpl(String aPath, boolean isDirectory)  { return new ZipDataFile(); }

/**
 * Override to load files when first file is requested.
 */
public synchronized Response getResponse(Request aRequest)
{
    if(!_loaded) { _loaded = true; loadArchive(); }
    return super.getResponse(aRequest);
}

/**
 * Returns a data source file for given path (if file exists).
 */
protected WebFile getFileImpl(String aPath)  { return null; }

/**
 * Returns a list of files at path.
 */
protected List getFilesImpl(WebFile aFile) throws Exception  { return null; }

/**
 * Returns file bytes.
 */
protected byte[] getFileBytesImpl(WebFile aFile) throws Exception
{
   ZipDataFile zfile = (ZipDataFile)aFile;
   InputStream istream = _zipFile.getInputStream(zfile.getZipEntry());
   return SnapUtils.getBytes2(istream);
}

/**
 * Saves a file.
 */
protected void saveFileImpl(WebFile aFile) throws Exception  { }

/**
 * Deletes a file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception  { }

/**
 * A ZipFile.
 */
public static class ZipDataFile extends WebFile {

    // The ZipEntry
    ZipEntry   _zipEntry;
    
    /** Create ZipDataFile. */
    public ZipDataFile()  { setExists(false); }

    /** Returns the ZipEntry. */
    public ZipEntry getZipEntry()  { return _zipEntry; }
    
    /** Sets the ZipEntry. */
    protected void setZipEntry(ZipEntry aZE)
    {
        _zipEntry = aZE;
        setModifiedTime(Math.max(_zipEntry.getTime(), 0));
    }
}

protected List<Row> getRowsImpl(Entity anEntity, Query aQuery)  { return null; }
protected void saveRowImpl(Row aRow)  { }
protected void deleteRowImpl(Row aRow)  { }

}