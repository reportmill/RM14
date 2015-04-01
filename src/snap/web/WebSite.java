/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.beans.*;
import java.io.File;
import java.util.*;
import snap.util.*;

/**
 * This is an abstract class to provide data management (create, get, put, delete) and file management.
 */
public abstract class WebSite extends SnapObject implements PropertyChangeListener, JSONArchiver.GetKeys {
    
    // The URL describing this WebSite
    WebURL                    _url;
    
    // The user name for authentication purposes
    String                    _userName;
    
    // The password for authentication purposes
    String                    _password;
    
    // The map of files previously vended by this data source
    Map <String,WebFile>      _files = new HashMap();
    
    // The schema
    Schema                    _schema;
    
    // The entities
    Map <String, Entity>      _entities = new HashMap();
    
    // The DataTables
    Map <String,DataTable>    _dataTables = new HashMap();
    
    // A WebSite that can be used for writing persistent support files
    WebSite                   _sandbox;
    
    // A map of properties associated with file
    Map                       _props = new HashMap();
    
    // The class loader for this WebSite
    WebClassLoader            _classLoader;
    
    // Constants for PropertyChanges
    public static final String Refresh_Prop = "Refresh";

/**
 * Returns the URL.
 */
public WebURL getURL()  { return _url; }

/**
 * Sets the URL.
 */
protected void setURL(WebURL aURL)  { _url = aURL; _url._asSite = this; }

/**
 * Returns the URL root.
 */
public String getURLString()  { return getURL().getString(); }

/**
 * Returns the name for this data source.
 */
public String getName()  { return getURL().getPath()!=null? getURL().getPathName() : getURL().getHost(); }

/**
 * Returns the host name.
 */
public String getHostName()  { return getURL().getHost(); }

/**
 * Returns the data source name-space and name in standard path form.
 */
public String getPath()  { return getURL().getPath(); }

/**
 * Returns the user name.
 */
public String getUserName()  { return _userName; }

/**
 * Sets the user name.
 */
public void setUserName(String aName)  { firePropertyChange("UserName", _userName, _userName = aName, -1); }

/**
 * Returns the password.
 */
public String getPassword()  { return _password; }

/**
 * Sets the password.
 */
public void setPassword(String aPassword)  { firePropertyChange("Password", _password, _password = aPassword, -1); }

/**
 * Returns whether data source exists.
 */
public boolean getExists()  { WebFile f = getFile("/"); return f!=null && f.getExists(); }

/**
 * Returns the root directory.
 */
public WebFile getRootDirectory()  { WebFile f = getFile("/"); return f!=null? f : createFile("/", true); }

/**
 * Returns a request object for a URL.
 */
public Request getRequest(WebURL aURL)  { Request req = new Request(); req.setURL(aURL); return req; }

/**
 * Returns a response instance for a request.
 */
public Response getResponse(Request aRequest)
{
    switch(aRequest.getType())  {
        case HEAD: return handleHead(aRequest);
        case GET: return handleGet(aRequest);
        case PUT: return handlePut(aRequest);
        case DELETE: return handleDelete(aRequest);
    }
    return null;
}

/**
 * Handles a head request.
 */
protected synchronized Response handleHead(Request aRequest)
{
    // Create basic response
    Response resp = new Response(); resp.setRequest(aRequest);
    
    // Get URL and path
    WebURL url = aRequest.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    
    // Get file from files cache
    WebFile file = _files.get(path);
    if(file!=null && file.getExists())  {
        resp.setFile(file); resp.setCode(Response.OK); return resp; }
    
    // Get file for path from data source
    try { file = getFileImpl(path); }
    catch(AccessException e) { resp.setException(e); resp.setCode(Response.UNAUTHORIZED); }
    catch(Exception e) { resp.setException(e); resp.setCode(Response.NOT_FOUND); }
    
    // If found, set Exists to true
    if(file!=null) {
        file._exists = true;
        resp.setFile(file); resp.setCode(Response.OK);
    }
    
    // Return file
    return resp;
}

/**
 * Handle a get request.
 */
protected synchronized Response handleGet(Request aRequest)
{
    // Bogus Servlet stuff
    //WebURL url = aRequest.getURL();
    //if(url.getQuery()!=null) { Servlet servlet = getServlet(url.getFileURL());
    //    Response sresp = servlet!=null? servlet.handleGet(aRequest) : null; if(sresp!=null) return sresp;
    //    Response resp = new Response(); resp.setRequest(aRequest); resp.setCode(Response.NOT_FOUND); return resp; }
    
    // Handle file
    Response resp = handleHead(aRequest);
    WebFile file = resp.getFile(); if(file==null) return resp;
    
    // If file is plain file, get bytes
    if(file.isFile()) {
        byte bytes[] = null; try { bytes = getFileBytesImpl(file); }
        catch(Exception e) { resp.setException(e); }
        if(bytes!=null) file._exists = true;
        resp.setBytes(bytes); file._bytes = bytes;
    }
    
    // Otherwise if directory, get files
    else {
        
        // Get files
        List <WebFile> files = null;
        try { files = getFilesImpl(file); resp.setFiles(files); }
        catch(Exception e) { resp.setException(e); }
        if(files==null) files = Collections.EMPTY_LIST;
        
        // Mark all as exists and return
        for(WebFile f : files) f._exists = true;
        file.initFiles(files);
        return resp;
    }
    
    // Return response
    return resp;
}

/**
 * Handle a PUT request.
 */
protected Response handlePut(Request aRequest)  { throw new RuntimeException("handlePut"); }

/**
 * Handle a DELETE request.
 */
protected Response handleDelete(Request aRequest) { throw new RuntimeException("handleDelete"); }

/**
 * Returns a new file at the given path, regardless of whether it exists in the data source.
 */
public synchronized WebFile createFile(String aPath, boolean isDir)
{
    // Get standardized path
    String path = PathUtils.getNormalized(aPath);
    
    // Get cached file for path - if not found, create and put new file in cache and configure (synched get/put)
    WebFile file = _files.get(path);
    if(file==null) {
        _files.put(path, file = createFileImpl(path, isDir));
        file._path = path; file._site = this; file._directory = isDir;
        file.addPropertyChangeListener(this);
        file.setDataType(DataType.getPathDataType(path));
    }
    
    // Return file
    return file;
}

/**
 * Returns the individual file with the given path.
 */
public WebFile getFile(String aPath) throws ResponseException
{
    // Get URL, request and response for path
    WebURL url = getURL(aPath);
    Request req = getRequest(url); req.setType(Request.Type.HEAD);
    Response resp = getResponse(req);
    
    // If response contains exception, throw it
    if(resp.getException()!=null)
        throw new ResponseException(resp);
    
    // Return Response.File (might be null if FILE_NOT_FOUND)
    return resp.getFile();
}

/**
 * Returns the file bytes for given file.
 */
protected byte[] getFileBytes(WebFile aFile) throws ResponseException
{
    // Get URL, request and response for file
    WebURL url = aFile.getURL();
    Request req = getRequest(url); req.setType(Request.Type.GET);
    Response resp = getResponse(req);
    
    // If response hit exception, throw it
    if(resp.getException()!=null)
        throw new ResponseException(resp);
    
    // Return Response bytes
    return resp.getBytes();
}

/**
 * Returns a list of files at path.
 */
protected List <WebFile> getFiles(WebFile aFile) throws ResponseException
{
    // Get URL, request and response for file
    WebURL url = aFile.getURL();
    Request req = getRequest(url); req.setType(Request.Type.GET);
    Response resp = getResponse(req);
    
    // If response hit exception, throw it
    if(resp.getException()!=null)
        throw new ResponseException(resp);
    
    // Return Response files
    return resp.getFiles();
}

/**
 * Save file.
 */
protected void saveFile(WebFile aFile) throws ResponseException
{
    // If there is an updater, push update and clear
    WebFile.Updater updater = aFile.getUpdater();
    if(updater!=null) {
        updater.updateFile(aFile); aFile.setUpdater(null); }

    // If parent doesn't exist, save it (to make sure it exists)
    WebFile parent = aFile.getParent();
    if(parent!=null && !parent.getExists(true))
        parent.save();
    
    // Save file
    try { saveFileImpl(aFile); }
    catch(Exception e) { Response r = new Response(); r.setException(e); throw new ResponseException(r); }
    
    // If file needs to be added to parent, add and save
    if(parent!=null && !aFile.getExists()) {
        parent.addFile(aFile);
        parent.save();
    }
    
    // Set File.Exists
    aFile.setExists(true);
}

/**
 * Delete file.
 */
protected void deleteFile(WebFile aFile) throws ResponseException
{
    // If file doesn't exist, throw exception
    if(!aFile.getExists()) {
        Exception e = new Exception("WebSite.deleteFile: File doesn't exist: " + aFile.getPath());
        Response r = new Response(); r.setException(e); new ResponseException(r);
    }
    
    // If directory, delete child files
    if(aFile.isDirectory()) {
        aFile._exists = false;
        for(WebFile file : aFile.getFiles())
            file.delete();
        aFile._exists = true;
    }

    // Delete file
    try { deleteFileImpl(aFile); }
    catch(Exception e) { Response r = new Response(); r.setException(e); throw new ResponseException(r); }
    
    // If not root, remove file from parent, and if parent still exists, save
    if(!aFile.isRoot()) { WebFile parent = aFile.getParent();
        parent.removeFile(aFile);
        if(parent.getExists())
            parent.save();
    }
    
    // Set file Exists to false, remove from cache
    aFile.setExists(false);     // Need to implement WeakReference map
    aFile.removePropertyChangeListener(this);
    if(aFile.isDirectory()) aFile.setFiles(null); else aFile.setBytes(null); aFile._modifiedTime = 0;
    aFile.addPropertyChangeListener(this);
    //_files.remove(aFile.getPath()); aFile.setSource(null); aFile.removePropertyChangeListener(this);
}

/**
 * Revert file from saved version.
 */
protected void refreshFile(WebFile aFile)
{
    // If file doesn't exist, just return
    if(!aFile.getExists()) return;
    
    // Get new/current version of file (just return if missing or has same modified time)
    long mtime = getModifiedTime(aFile); if(mtime==aFile.getModifiedTime()) return;
    
    // Revert basic stuff
    if(aFile.isFile()) aFile.setBytes(null);
    else aFile.setFiles(null);
    
    // Revert info stuff
    aFile.setModifiedTime(mtime);
}

/**
 * Creates a new file.
 */
protected WebFile createFileImpl(String aPath, boolean isDirectory)  { return new WebFile(); }

/**
 * Returns a data source file for given path (if file exists).
 */
protected WebFile getFileImpl(String aPath) throws Exception  { throw notImpl("getFileImpl"); }

/**
 * Returns file bytes.
 */
protected byte[] getFileBytesImpl(WebFile aFile) throws Exception { throw notImpl("getFileBytesImpl"); }

/**
 * Returns a list of files at path.
 */
protected List <WebFile> getFilesImpl(WebFile aFile) throws Exception { throw notImpl("getFilesImpl"); }

/**
 * Saves a file.
 */
protected void saveFileImpl(WebFile aFile) throws Exception { throw notImpl("saveFileImpl"); }

/**
 * Deletes a file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception { throw notImpl("deleteFileImpl"); }

/**
 * Returns the modified time for a file to underlying file system.
 */
public long getModifiedTime(WebFile aFile)  { return aFile.getModifiedTime(); }

/**
 * Saves the modified time for a file to underlying file system.
 */
public void setModifiedTime(WebFile aFile, long aTime) throws Exception
{
    setModifiedTimeImpl(aFile, aTime);
    aFile.setModifiedTime(aTime);
}

/**
 * Saves the modified time for a file to underlying file system.
 */
protected void setModifiedTimeImpl(WebFile aFile, long aTime) throws Exception { }

/**
 * Sets whether a file exists.
 */
protected void setExists(WebFile aFile, boolean aValue)  { aFile._exists = aValue; }

/**
 * Returns a standard java.io.File, if available.
 */
protected File getStandardFile(WebFile aFile)  { return null; }

/**
 * Returns a URL for the given file path.
 */
public WebURL getURL(String aPath)
{
    if(aPath.indexOf(':')>=0) return new WebURL(aPath);
    String path = PathUtils.getNormalized(aPath);
    WebURL url = getURL();
    String urls = url.getString(); if(url.getPath()!=null) urls += '!';
    return new WebURL(urls + path);
}

/**
 * Creates the data site remote site (database, directory file, etc.).
 */
public void createSite() throws Exception  { }

/**
 * Deletes this data site, assuming it corresponds to something that can be deleted, like a database.
 */
public void deleteSite() throws Exception
{
    if(getFile("/")!=null)
        getFile("/").delete();
}

/**
 * Returns the schema of represented WebSite as a hierarchy of RMEntity and RMProperty objects.
 */
public synchronized Schema getSchema()
{
    if(_schema==null) {
        _schema = createSchema(); _schema.setName(getName()); _schema.setSite(this); }
    return _schema;
}

/**
 * Creates the schema.
 */
protected Schema createSchema()  { return new Schema(); }

/**
 * Creates an entity for given name.
 */
public synchronized Entity createEntity(String aName)
{
    // If entity already exists, just return it
    Entity entity = _entities.get(aName); if(entity!=null) return entity;
    
    // Create and add entity
    _entities.put(aName, entity = createEntityImpl(aName));
    entity.setName(aName);
    entity.setSchema(getSchema());
    return entity;
}

/**
 * Returns the entity for given name.
 */
protected Entity createEntityImpl(String aName)  { return new Entity(); }

/**
 * Returns the entity for given name.
 */
public synchronized Entity getEntity(String aName)
{
    // Get entity from files cache
    Entity entity = _entities.get(aName);
    if(entity!=null && entity.getExists())
        return entity;
    
    // Get entity for name from data source
    try { entity = getEntityImpl(aName); }
    catch(Exception e) { throw new RuntimeException(e); }
    
    // If found, set Exists to true
    if(entity!=null) {
        entity.setExists(true);
        getSchema().addEntity(entity);
    }
    
    // Return entity
    return entity;
}

/**
 * Returns the entity for given name.
 */
protected Entity getEntityImpl(String aName) throws Exception
{
    WebFile efile = getFile("/" + aName + ".table");
    if(efile!=null) {
        Entity entity = createEntity(efile.getSimpleName());
        try { return entity.fromBytes(efile.getBytes()); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    return null;
}

/**
 * Saves the given entity.
 */
public void saveEntity(Entity anEntity) throws Exception
{
    saveEntityImpl(anEntity);
    if(!anEntity.getExists()) {
        anEntity.setExists(true);
        getSchema().addEntity(anEntity);
    }
}

/**
 * Saves the given entity.
 */
protected void saveEntityImpl(Entity anEntity) throws Exception
{
    WebFile efile = anEntity.getSourceFile(); if(efile==null) return;
    efile.setBytes(anEntity.toBytes());
    efile.save();
}

/**
 * Saves the given entity.
 */
public void deleteEntity(Entity anEntity) throws Exception
{
    deleteEntityImpl(anEntity);
    anEntity.setExists(false);
    getSchema().removeEntity(anEntity);
}

/**
 * Saves the given entity.
 */
protected void deleteEntityImpl(Entity anEntity) throws Exception
{
    WebFile efile = anEntity.getSourceFile(); if(efile==null) return;
    efile.delete();
}

/**
 * Returns the list of known data tables.
 */
public synchronized List <DataTable> getDataTables()  { return new ArrayList(_dataTables.values()); }

/**
 * Returns the DataTable for given name
 */
public synchronized DataTable getDataTable(String aName)
{
    DataTable dtable = _dataTables.get(aName);
    if(dtable==null) {
        dtable = createDataTable(aName); if(dtable==null) return null;
        _dataTables.put(aName, dtable);
    }
    return dtable;
}

/**
 * Returns the DataTable for given name.
 */
protected DataTable createDataTable(String aName)
{
    Entity entity = getEntity(aName); if(entity==null) return null;
    DataTable table = createDataTableImpl(); table.setSite(this); table.setEntity(entity);
    return table;
}

/**
 * Creates an instance of DataTable.
 */
protected DataTable createDataTableImpl()  { return new DataTable(); }

/**
 * Returns a row for an entity and primary value that is guaranteed to be unique for this data source.
 */
public Row createRow(Entity anEntity, Object aPrimaryValue)  { return createRow(anEntity, aPrimaryValue, null); }

/**
 * Returns a row for an entity and primary value that is guaranteed to be unique for this data source.
 */
public synchronized Row createRow(Entity anEntity, Object aPrimaryValue, Map aMap)
{
    // If PrimaryValue provided, check/set LocalRows cache
    Row row = null;
    if(aPrimaryValue!=null) {
        DataTable dtable = getDataTable(anEntity.getName());
        row = dtable.getLocalRow(aPrimaryValue); if(row!=null) return row;
        row = createRowImpl(anEntity, aPrimaryValue); row.setSite(this); row.setEntity(anEntity);
        row.put(anEntity.getPrimary(), aPrimaryValue);
        dtable.addLocalRow(row);
    }
    
    // Otherwise just create row
    else { row = createRowImpl(anEntity, null); row.setSite(this); row.setEntity(anEntity); }
    
    // Initialize values, start listening to PropertyChanges and return
    row.initValues(aMap);
    row.addPropertyChangeListener(this);
    return row;
}

/**
 * Creates a new row for source.
 */
protected Row createRowImpl(Entity anEntity, Object aPrimaryValue)  { return new Row(); }

/**
 * Returns a row for a given entity and primary value.
 */
public synchronized Row getRow(Entity anEntity, Object aPrimaryValue)
{
    // Make sure PrimaryValue is non-null
    assert(aPrimaryValue!=null);
    
    // See if there is a local row - if so return it
    DataTable dtable = getDataTable(anEntity.getName());
    Row row = dtable.getLocalRow(aPrimaryValue);
    if(row!=null && row.getExists())
        return row;
    
    // Fetch row - if found, set exists
    row = getRowImpl(anEntity, aPrimaryValue);
    if(row!=null)
        row.setExists(true);

    // Return row
    return row;
}

/**
 * Returns a row for a given entity and primary value.
 */
protected Row getRowImpl(Entity anEntity, Object aPrimaryValue)
{
    Query query = new Query(anEntity);
    query.addCondition(anEntity.getPrimary().getName(), Condition.Operator.Equals, aPrimaryValue);
    return getRow(query);
}

/**
 * Returns a row for given query.
 */
public Row getRow(Query aQuery)  { List <Row> rows = getRows(aQuery); return rows.size()>0? rows.get(0) : null; }

/**
 * Returns a set of rows for the given properties and condition.
 */
public synchronized List <Row> getRows(Query aQuery)
{
    // Get query entity (just return if null)
    String ename = aQuery.getEntityName();
    Entity entity = getEntity(ename); if(entity==null) return null;
    
    // Fetch rows, set Exists and return
    List <Row> rows = getRowsImpl(entity, aQuery);
    for(Row row : rows) row.setExists(true);
    return rows;
}

/**
 * Returns a set of rows for the given properties and condition.
 */
protected abstract List <Row> getRowsImpl(Entity anEntity, Query aQuery);

/**
 * Inserts or updates a given row.
 */
public synchronized void saveRow(Row aRow) throws Exception
{
    // If row exists and hasn't changed, just return
    boolean exists = aRow.getExists(); if(exists && !aRow.isModified()) return;
    
    // If there are UnresolvedRelationRows, make sure they get saved
    Row urows[] = aRow.getUnresolvedRelationRows();
    if(urows!=null) {
        if(!exists) { saveRowImpl(aRow); aRow.setExists(true); } // Save this row first in case of circular reference
        for(Row urow : urows)
            urow.save();
    }

    // Save row for real
    saveRowImpl(aRow);
    
    // Set row exists and not modified and add to DataTable
    aRow.setExists(true);
    aRow.setModified(false);
    if(!exists) {
        DataTable dtable = getDataTable(aRow.getEntity().getName());
        dtable.addLocalRow(aRow);
    }
}

/**
 * Inserts or updates a given row.
 */
protected abstract void saveRowImpl(Row aRow) throws Exception;

/**
 * Deletes a given row.
 */
public synchronized void deleteRow(Row aRow) throws Exception
{
    // Delete row
    deleteRowImpl(aRow);
    
    // Set Exists to false and remove from table
    aRow.setExists(false);
    DataTable dtable = getDataTable(aRow.getEntity().getName());
    dtable.removeLocalRow(aRow);    
}

/**
 * Deletes a given row.
 */
protected abstract void deleteRowImpl(Row aRow) throws Exception;

/**
 * Returns a file property for key.
 */
public Object getProp(String aKey)  { return _props.get(aKey); }

/**
 * Sets a property for a key.
 */
public void setProp(String aKey, Object aValue)  { _props.put(aKey, aValue); }

/**
 * Returns a WebSite that can be used for storing persistent support files.
 */
public WebSite getSandbox()  { return _sandbox!=null? _sandbox : (_sandbox=createSandbox()); }

/**
 * Sets a WebSite that can be used for storing persistent support files.
 */
public void setSandbox(WebSite aSandbox)  { _sandbox = aSandbox; }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected WebSite createSandbox()  { return createSandboxURL().getAsSite(); }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected WebURL createSandboxURL()  { return new WebURL(createSandboxURLS()); }

/**
 * Creates a WebSite that can be used for storing persistent support files.
 */
protected String createSandboxURLS()
{
    // Get site URL and construct filename string from scheme/host/path
    WebURL url = getURL(); String fname = "";
    String scheme = url.getScheme(); if(!scheme.equals("local")) fname += scheme + '/';
    String host = url.getHost(); if(host!=null && host.length()>0) fname += host + '/';
    String path = url.getPath(); if(path!=null && path.length()>1) fname += path.substring(1);
    
    // If filename string ends with /bin or /, trim, then replace '/' & '.' separators with '_'
    if(fname.endsWith("/bin")) fname = fname.substring(0, fname.length()-4);
    else if(fname.endsWith("/")) fname = fname.substring(0, fname.length()-1);
    fname = fname.replace('.', '_').replace('/', '_');
    
    // Return URL string for filename in local Sandboxes directory
    return "local:/Sandboxes/" + fname;
}

/** Interface for Servlet. */
//public interface Servlet { Response handleGet(Request aRequest); }
//public Servlet getServlet(WebURL aURL)  { return (Servlet)getProp(aURL.getFileURL().getString()); }
//public void setServlet(WebURL aURL, Servlet aServlet)  { setProp(aURL.getFileURL().getString(), aServlet); }

/**
 * Returns the DataClassLoader.
 */
public WebClassLoader getClassLoader() { return _classLoader!=null? _classLoader : (_classLoader=createClassLoader());}

/**
 * Sets the DataClassLoader.
 */
public void setClassLoader(WebClassLoader aClassLoader)  { _classLoader = aClassLoader; }

/**
 * Creates the DataClassLoader.
 */
protected WebClassLoader createClassLoader()  { return new WebClassLoader(this); }

/**
 * Adds a deep (property) change listener to get notified when this WebSite sees changes (to files).
 */
public void addDeepChangeListener(DeepChangeListener aListener)
{
    addListener(DeepChangeListener.class, aListener);
}

/**
 * Removes a deep (property) change listener to get notified when this WebSite sees changes (to files).
 */
public void removeDeepChangeListener(DeepChangeListener aListener)
{
    removeListener(DeepChangeListener.class, aListener);
}

/**
 * Handle property changes on row objects by forwarding to listener.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Forward to deep change listeners
    for(int i=0, iMax=getListenerCount(DeepChangeListener.class); i<iMax; i++)
        getListener(DeepChangeListener.class, i).deepChange(this, anEvent);
}

/**
 * Clears all cached data in the data source, forcing everything to reload.
 */
public synchronized void refresh()
{
    _files.clear(); _schema = null;  // Clear files and schema
    firePropertyChange(Refresh_Prop, null, null, -1);  // Fire Refresh property change
    _classLoader = null;
}

/**
 * Flushes any unsaved changes to backing store.
 */
public void flush() throws Exception { }

/**
 * RMJSONArchiver.GetKeys method.
 */
public List <String> getJSONKeys()  { return Arrays.asList("HostName", "Path", "UserName", "Password"); }

/** Returns a "not implemented" exception for string (method name). */
private Exception notImpl(String aStr)  { return new Exception(getClass().getName() + ": Not implemented:" + aStr); }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ' ' + getURLString(); }

}