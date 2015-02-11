/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.websites;
import java.io.File;
import java.util.*;
import snap.util.*;
import snap.web.*;

/**
 * A data source to read/write data and files to a file system.
 */
public class FileSite extends WebSite {

    // The map of row-lists generated from import
    Map <Entity, List <Row>>      _entityRows = new HashMap();
    
    // Dirty entity set
    Set <Entity>                  _dirtyEntities = new HashSet();
    
    // Table data file extension
    static final String           TableEntityFileExt = ".entity"; 
    static final String           TableDataFileExt = ".csv"; 

/**
 * Returns a file at
 */
protected WebFile getFileImpl(String aPath)
{
    // Get java file for path
    File file = getStandardFile(aPath);
    if(!file.exists())
        return null;
    
    // Declare and initialize file
    WebFile dfile = createFile(aPath, file.isDirectory());
    dfile.setModifiedTime(file.lastModified());
    dfile.setSize(file.length());
    
    // Return file
    return dfile;
}

/**
 * Returns files at path.
 */
protected List <WebFile> getFilesImpl(WebFile aFile) throws Exception
{
    // Create list for files
    List <WebFile> dfiles = new ArrayList();
    
    // Get java file for path
    File file = getStandardFile(aFile.getPath());
    if(!file.exists() || !file.isDirectory())
        return dfiles;
    
    // Get java file children (if null, just return)
    File cfiles[] = file.listFiles();
    if(cfiles==null)
        return dfiles;
    
    // Create files from child java files
    for(File cfile : cfiles) { String name = cfile.getName();
        if(name.equalsIgnoreCase(".DS_Store")) continue; // Skip funky apple files
        if(name.equalsIgnoreCase("CVS") && cfile.isDirectory()) continue; // Skip CVS directories
        WebFile dfile = getFileImpl(StringUtils.getPathChild(aFile.getPath(), name));
        if(dfile!=null) dfiles.add(dfile); // Happens with links
    }
    
    // Return data files
    return dfiles;
}

/**
 * Gets file bytes.
 */
protected byte[] getFileBytesImpl(WebFile aFile) throws Exception
{
    return FileUtils.getBytes(aFile.getStandardFile());
}

/**
 * Writes file bytes.
 */
protected void saveFileImpl(WebFile aFile) throws Exception
{
    // Get standard file
    File file = aFile.getStandardFile();
    
    // Make sure parent directories exist
    file.getParentFile().mkdirs();
    
    // If directory, create
    if(aFile.isDirectory())
        file.mkdir();
    
    // Otherwise, write bytes
    else if(aFile.getBytes()!=null)
        FileUtils.writeBytesSafely(file, aFile.getBytes());
    
    // Update modified time
    aFile.setModifiedTime(file.lastModified());
}

/**
 * Deletes file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception  { aFile.getStandardFile().delete(); }

/**
 * Override to get modified time from Java file.
 */
public long getModifiedTime(WebFile aFile)  { File f = aFile.getStandardFile(); return f!=null? f.lastModified() : 0; }

/**
 * Saves the modified time for a file to underlying file system.
 */
protected void setModifiedTimeImpl(WebFile aFile, long aTime) throws Exception
{
    aFile.getStandardFile().setLastModified(aTime);
}

/**
 * Get entity by loading from entity file.
 */
protected Entity getEntityImpl(String aName) throws Exception
{
    Entity entity = super.getEntityImpl(aName); if(entity!=null) return entity;
    WebFile entityFile = getTableEntityFile(aName, false); if(entityFile==null) return null;
    entity = createEntity(aName);
    entity.fromBytes(entityFile.getBytes());
    return entity;
}

/**
 * Save entity by saving entity bytes to entity file.
 */
protected void saveEntityImpl(Entity anEntity) throws Exception
{
    super.saveEntityImpl(anEntity);
    WebFile entityFile = getTableEntityFile(anEntity.getName(), true);
    entityFile.setBytes(anEntity.toBytes());
    entityFile.save();
}

/**
 * Delete entity file and entity table data file.
 */
protected void deleteEntityImpl(Entity anEntity) throws Exception
{
    super.deleteEntityImpl(anEntity);
    WebFile efile = getTableEntityFile(anEntity.getName(), false);
    if(efile!=null)
        efile.delete();
    WebFile tfile = getTableDataFile(anEntity.getName(), false);
    if(tfile!=null)
        tfile.delete();
}

/**
 * Returns a set of rows for the given properties and condition.
 */
protected List <Row> getRowsImpl(Entity anEntity, Query aQuery)
{
    // Get entity rows
    Condition condition = aQuery.getCondition();
    Row entityRows[] = getEntityRows(anEntity).toArray(new Row[0]);
    
    // Create fetch list and add rows that satisfy condition
    List <Row> rows = new ArrayList();
    for(Row row : entityRows)
        if(condition==null || condition.getValue(anEntity, row))
            rows.add(row);
    
    // Return rows
    return rows;
}

/**
 * Inserts or updates a given row.
 */
protected void saveRowImpl(Row aRow)
{
    // Get row entity
    Entity entity = aRow.getEntity();
    
    // If row hasn't been saved yet, insert into entity rows and update any auto generated properties
    if(!aRow.getExists()) {
        
        // Add to entity rows
        List <Row> entityRows = getEntityRows(entity);
        entityRows.add(aRow);
    
        // Set auto-generated properties
        for(Property property : entity.getProperties())
            if(property.isAutoGenerated()) {
                int maxID = 0; for(Row row : entityRows) maxID = Math.max(maxID,SnapUtils.intValue(row.get(property)));
                aRow.put(property, maxID + 1);
            }
    }
    
    // Add dirty entity
    synchronized (this) { _dirtyEntities.add(entity); }
}

/**
 * Deletes a given row.
 */
protected void deleteRowImpl(Row aRow)
{
    // Get EntityRows list for Entity and row for PrimaryValue (just return if no row for PrimaryValue)
    Entity entity = aRow.getEntity();
    List <Row> entityRows = getEntityRows(entity);
    
    // Remove row and add row entity to DirtyEntities set
    ListUtils.removeId(entityRows, aRow);
    synchronized (this) { _dirtyEntities.add(entity); }
}

/**
 * Save entity files for changed entities.
 */
protected void saveEntityFiles() throws Exception
{
    // Copy and clear DirtyEntities
    Entity entities[];
    synchronized (this) {
        entities = _dirtyEntities.toArray(new Entity[_dirtyEntities.size()]);
        _dirtyEntities.clear();        
    }

    // Save files
    for(Entity entity : entities) saveEntityFile(entity);
}

/**
 * Save entity files for changed entities.
 */
protected void saveEntityFile(Entity anEntity) throws Exception
{
    // Get entity rows and StringBuffer
    Row entityRows[] = getEntityRows(anEntity).toArray(new Row[0]);
    StringBuffer sbuffer = new StringBuffer();
    
    // Iterate over properties and add header row
    for(Property property : anEntity.getProperties())
        if(!property.isDerived())
            sbuffer.append(StringUtils.getStringQuoted(property.getName())).append(", ");
    
    // Replace trailing field delimiter with record delimiter
    sbuffer.delete(sbuffer.length()-2, sbuffer.length()).append("\n");

    // Iterate over rows
    for(Row row : entityRows) {
    
        // Iterate over properties
        for(Property property : anEntity.getProperties()) {
            
            // Skip derived properties
            if(property.isDerived()) continue;
            
            // Get value and string
            Object value = row.getValue(property);
            String string = (String)DataUtils.convertValue(value, Property.Type.String);
            if(string==null)
                string = "";
            
            // Write string
            sbuffer.append(StringUtils.getStringQuoted(string)).append(", ");
        }
        
        // Replace trailing field delimiter with record delimiter
        sbuffer.delete(sbuffer.length()-2, sbuffer.length()).append("\n");
    }
    
    // Get entity file, set bytes and save
    WebFile entityFile = getTableDataFile(anEntity.getName(), true);
    byte bytes[] = StringUtils.getBytes(sbuffer.toString()); entityFile.setBytes(bytes);
    entityFile.save();
}

/**
 * Override to Saves changes if any made.
 */
public void flush() throws Exception
{
    super.flush();
    if(_dirtyEntities.size()>0)
        saveEntityFiles();
}

/**
 * Returns the file for the given entity.
 */
protected WebFile getTableEntityFile(String aName, boolean doCreate)
{
    String path = "/FileDB/" + aName + TableEntityFileExt;
    WebFile tfile = getSandbox().getFile(path);
    if(tfile==null && doCreate) tfile = getSandbox().createFile(path, false);
    return tfile;
}

/**
 * Returns the file for the given entity.
 */
protected WebFile getTableDataFile(String aName, boolean doCreate)
{
    String path = "/FileDB/" + aName + TableDataFileExt;
    WebFile tfile = getSandbox().getFile(path);
    if(tfile==null && doCreate) tfile = getSandbox().createFile(path, false);
    return tfile;
}

/**
 * Returns the list of rows for a given entity.
 */
protected synchronized List <Row> getEntityRows(Entity anEntity)
{
    // Get entity rows
    List <Row> entityRows = _entityRows.get(anEntity);
    if(entityRows!=null)
        return entityRows;
    
    // Create and set entity rows list
    _entityRows.put(anEntity, entityRows = Collections.synchronizedList(new ArrayList()));
    
    // Get entity file
    WebFile entityFile = getTableDataFile(anEntity.getName(), false);
    
    // If file exists, read rows
    if(entityFile!=null) {
        
        // Create CSVReader
        CSVReader csvReader = new CSVReader();
        csvReader.setFieldDelimiter(",");
        csvReader.setHasHeaderRow(true);
        csvReader.setHasQuotedFields(true);
        
        // Read maps
        List <Map> maps = csvReader.readObject(entityFile.getBytes(), anEntity.getName(), false);
        
        // Create rows for maps and add to entityRows list
        Property primaryProperty = anEntity.getPrimary();
        for(Map map : maps) {
            Object pvalue = primaryProperty.convertValue(map.get(primaryProperty.getName()));
            Row row = createRow(anEntity, pvalue, map);
            entityRows.add(row);
        }
    }
    
    // Return entity rows
    return entityRows;
}

/**
 * Deletes this data source, assuming it corresponds to something that can be deleted, like a database.
 */
public void deleteSite() throws Exception { WebFile root = getFile("/"); if(root!=null) root.delete(); }

/**
 * Returns the Java file for a WebFile.
 */
protected File getStandardFile(WebFile aFile)  { return getStandardFile(aFile.getPath()); }

/**
 * Returns the Java file for RMFile.
 */
protected File getStandardFile(String aPath)
{
    String path = getPath()!=null? getPath() + aPath : aPath;
    return new File(path);
}

}