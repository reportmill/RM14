/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import snap.util.*;

/**
 * Represents a file from a WebSite.
 */
public class WebFile extends SnapObject implements Comparable<WebFile>, JSONArchiver.GetKeys {

    // The WebSite that provided this file
    WebSite           _site;
    
    // The file path
    String            _path;
    
    // Whether file is a directory
    boolean           _directory;
    
    // The URL for this file
    WebURL            _url;
    
    // Whether file exists in data source (has been saved and, if so, not deleted)
    Boolean           _exists;

    // The file parent
    WebFile           _parent;
    
    // The file modified time
    long              _modifiedTime;
    
    // The file size
    long              _size;
    
    // The file bytes
    byte              _bytes[];
    
    // The directory files
    List <WebFile>    _files;
    
    // The content type
    DataType          _dataType;
    
    // A map of properties associated with file
    Map               _props = new HashMap();
    
    // Constants for properties
    final public static String Path_Prop = "Path";
    final public static String ModifiedTime_Prop = "ModifiedTime";
    final public static String Bytes_Prop = "Bytes";
    final public static String Size_Prop = "Size";
    final public static String File_Prop = "File";
    final public static String Files_Prop = "Files";
    final public static String Exists_Prop = "Exists";
    final public static String Updater_Prop = "Updater";
    
/**
 * Returns the WebSite.
 */
public WebSite getSite()  { return _site; }

/**
 * Returns the file path.
 */
public String getPath()  { return _path; }

/**
 * Returns the resource name.
 */
public String getName()  { return StringUtils.getPathFileName(getPath()); }

/**
 * Returns the file simple name.
 */
public String getSimpleName()  { return StringUtils.getPathFileNameSimple(getPath()); }

/**
 * Returns the file type (extension without the '.').
 */
public String getType()  { return StringUtils.getPathExtension(getPath()).toLowerCase(); }

/**
 * Returns the path as a directory (with trailing separator).
 */
public String getDirPath()  { String path = getPath(); return path.endsWith("/")? path : path + '/'; }

/**
 * Returns the URL for this file.
 */
public WebURL getURL()
{
    if(_url==null) { _url = getSite().getURL(getPath()); _url._file = this; }
    return _url;
}

/**
 * Returns whether file is a directory.
 */
public boolean isDirectory()  { return _directory; }

/**
 * Returns whether file is a plain file.
 */
public boolean isFile()  { return !isDirectory(); }

/**
 * Returns whether this file is root directory.
 */
public boolean isRoot()  { return getPath().equals("/"); }

/**
 * Returns whether file exists in data source (has been saved and, if so, not deleted).
 */
public boolean getExists()  { return _exists!=null && _exists; }

/**
 * Returns whether file exists in data source (with option to confirm if file was created instead of fetched).
 */
public boolean getExists(boolean doConfirm)
{
    if(_exists==null)
        _exists = getSite().getFile(getPath())!=null;
    return _exists;
}

/**
 * Sets whether file exists in data source (has been saved and, if so, not deleted).
 */
protected void setExists(boolean aFlag)
{
    if(_exists!=null && aFlag==_exists) return;
    firePropertyChange(Exists_Prop, _exists, _exists = aFlag, -1);
}

/**
 * Returns the file parent directory.
 */
public WebFile getParent()
{
    // If parent not set, get from data source
    if(_parent==null && !isRoot() && getSite()!=null)
        _parent = getSite().createFile(StringUtils.getPathParent(getPath()), true);

    // Return parent
    return _parent;
}

/**
 * Sets the file parent.
 */
protected void setParent(WebFile aFile)  { _parent = aFile; }

/**
 * Returns the file modification time.
 */
public long getModifiedTime()  { return _modifiedTime; }

/**
 * Sets the file modification time.
 */
public void setModifiedTime(long aTime)
{
    if(aTime==_modifiedTime) return;
    firePropertyChange(ModifiedTime_Prop, _modifiedTime, _modifiedTime = aTime, -1);
}

/**
 * Returns the modified date.
 */
public Date getModifiedDate()  { return new Date(_modifiedTime); }

/**
 * Returns the file size.
 */
public long getSize()  { return _size; }

/**
 * Sets the file size.
 */
public void setSize(long aSize)
{
    if(aSize==_size) return;
    firePropertyChange(Size_Prop, _size, _size = aSize, -1);
}

/**
 * Returns whether bytes have been set/loaded for file.
 */
public boolean isBytesSet()  { return _bytes!=null; }

/**
 * Returns the file bytes.
 */
public byte[] getBytes()  { return _bytes!=null? _bytes : (_bytes=getSite().getFileBytes(this)); }

/**
 * Sets the file bytes.
 */
public void setBytes(byte theBytes[])
{
    if(ArrayUtils.equals(theBytes, _bytes)) return;
    firePropertyChange(Bytes_Prop, _bytes, _bytes = theBytes, -1);
    setSize(theBytes!=null? theBytes.length : 0); // Update size
}

/**
 * Returns the number of files in this directory.
 */
public int getFileCount()  { return getFiles()!=null? getFiles().size() : 0; }

/**
 * Returns the individual file at given index.
 */
public WebFile getFile(int anIndex)  { return getFiles().get(anIndex); }

/**
 * Returns the directory files list.
 */
public <T extends WebFile> List <T> getFiles()  { if(_files==null && isDirectory()) initFiles(); return (List)_files; }

/**
 * Sets the directory files list.
 */
protected void setFiles(List theFiles)
{
    if(SnapUtils.equals(theFiles, _files)) return;
    firePropertyChange(Files_Prop, _files, _files = theFiles, -1);
}

/**
 * Initialize files.
 */
protected synchronized void initFiles()
{
    if(_files!=null) return;  // If files already set, return
    List <? extends WebFile> files = getSite().getFiles(this);
    if(files==null) files = Collections.EMPTY_LIST;
    if(_files==null) initFiles(files);
}

/**
 * Initialize files.
 */
protected synchronized void initFiles(List <? extends WebFile> theFiles)
{
    Collections.sort(theFiles);
    _files = new CopyOnWriteArrayList(theFiles);   // Create files array from files
    for(WebFile file : theFiles) file.setParent(this);  // Set each file parent to this file
}

/**
 * Adds a file.
 */
protected void addFile(WebFile aFile)
{
    if(!getFiles().contains(aFile))
        addFile(aFile, getInsertIndex(aFile));
}

/**
 * Adds a file at given index.
 */
protected void addFile(WebFile aFile, int anIndex)
{
    // Add file and set file parent
    getFiles().add(anIndex, aFile);
    aFile.setParent(this);
    
    // Fire property change
    firePropertyChange(File_Prop, null, aFile, anIndex);
}

/**
 * Removes a file at given index.
 */
protected WebFile removeFile(int anIndex)
{
    // Remove file and clear file parent
    WebFile file = _files.remove(anIndex);
    file.setParent(null);
    
    // Fire property change and return file
    firePropertyChange(File_Prop, file, null, anIndex);
    return file;
}

/**
 * Removes given file.
 */
protected int removeFile(WebFile aFile)
{
    int index = getFileIndex(aFile);
    if(index>=0) removeFile(index);
    return index;
}

/**
 * Saves the file.
 */
public void save() throws ResponseException  { getSite().saveFile(this); }

/**
 * Deletes the file.
 */
public void delete() throws ResponseException  { getSite().deleteFile(this); }

/**
 * Revert the file from saved version.
 */
public void refresh()  { getSite().refreshFile(this); }

/**
 * Returns the file with the given name.
 */
public WebFile getFile(String aName)
{
    String path = aName.startsWith("/")? aName : getDirPath() + aName;
    return getSite().getFile(path);
}

/**
 * Returns the list of files that match given regex.
 */
public List <WebFile> getFiles(String aRegex)
{
    List files = new ArrayList();
    for(WebFile file : getFiles())
        if(file.getName().matches(aRegex))
            files.add(file);
    return files;
}

/**
 * Returns the index of a file.
 */
public int getFileIndex(WebFile aFile)  { return ListUtils.indexOfId(getFiles(), aFile); }

/**
 * Returns the insert index to insert a file in this directory.
 */
public int getInsertIndex(WebFile aFile)  { return ListUtils.binarySearch(getFiles(), aFile); }

/**
 * Returns the file keys.
 */
public List <String> getFileNames()
{
    List <String> names = new ArrayList<String>();
    for(WebFile file : getFiles()) names.add(file.getName());
    return names;
}

/**
 * Returns the data type of the file.
 */
public DataType getDataType()  { return _dataType; }

/**
 * Sets the data type for the file.
 */
protected void setDataType(DataType aDataType)  { _dataType = aDataType; }

/**
 * Returns a file property for key.
 */
public Object getProp(String aKey)  { return _props.get(aKey); }

/**
 * Sets a property for a key.
 */
public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

/**
 * Returns whether update is set and has update.
 */
public boolean isUpdateSet()  { return getUpdater()!=null; }

/**
 * Returns the updater.
 */
public Updater getUpdater()  { return _updater; } Updater _updater;

/**
 * Sets the Updater.
 */
public void setUpdater(Updater anUpdater)
{
    if(anUpdater==_updater) return;
    firePropertyChange(Updater_Prop, _updater, _updater = anUpdater, -1);
}

/**
 * An interface for classes that want to post modifications to files.
 */
public interface Updater {

    /** Saves the file. */
    public void updateFile(WebFile aFile);
}

/**
 * Returns the URL string for this file.
 */
public String getURLString()  { return getURL().getString(); }

/**
 * Returns the file bytes as a string.
 */
public String getText()  { return StringUtils.getString(getBytes()); }

/**
 * Sets the file bytes as a string.
 */
public void setText(String aString)  { setBytes(StringUtils.getBytes(aString)); }

/**
 * Returns an input stream for file.
 */
public InputStream getInputStream()  { return new ByteArrayInputStream(getBytes()); }

/**
 * Returns a standard java.io.File, if available.
 */
public File getStandardFile()  { return getSite().getStandardFile(this); }

/**
 * Returns a relative URL for the given file path.
 */
public WebURL getURL(String aPath)
{
    WebFile dir = isFile()? getParent() : this;
    return WebURL.getURL(dir.getURL(), aPath);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    WebFile other = (WebFile)anObj; if(other==null) return false;
    return other.getURL().equals(getURL());
}

/**
 * Standard hashCode implementation.
 */
public int hashCode()  { return getURL().hashCode(); }

/**
 * Standard compareTo implementation.
 */
public int compareTo(WebFile aFile)
{
    int c = aFile.getParent()!=getParent()? getPath().compareToIgnoreCase(aFile.getPath()) :
        getSimpleName().compareToIgnoreCase(aFile.getSimpleName());
    if(c==0) c = getName().compareToIgnoreCase(aFile.getName());
    return c;
}

/**
 * Standard clone implementation.
 */
public WebFile clone()
{
    WebFile clone = (WebFile)super.clone();
    clone._props = new HashMap();
    return clone;
}

/** RMJSONArchiver.GetKeys implementation. */
public List <String> getJSONKeys()  { return Arrays.asList(Path_Prop, Size_Prop, ModifiedTime_Prop, "Directory"); }

/**
 * Returns a string representation of file.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getURLString() + (isDirectory()? "/" : ""); }

}