package snap.util;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.*;
import snap.parse.*;
import snap.web.*;

/**
 * A class for reading file of comma separated values, or really any separated values.
 */
public class CSVReader {

    // The field delimiter
    String                      _fieldDelimiter = "\t";
    
    // The record delimiter
    String                      _recordDelimiter = "\n";
    
    // Whether first row contains field names
    boolean                     _hasHeaderRow = true;
    
    // Whether fields are quoted
    boolean                     _hasQuotedFields;
    
    // The source string
    String                      _sourceString;
    
    // The number of field delimiters of most recent readFormat
    int                         _fieldDelimiterCount;
    
    // The number of record delimiters of most recent readFormat
    int                         _recordDelimiterCount;
    
    // The entity generated by the most previous read
    Entity                      _entity;

/**
 * Returns the field delimiter.
 */
public String getFieldDelimiter()  { return _fieldDelimiter; }

/**
 * Sets the field delimiter.
 */
public void setFieldDelimiter(String aDelimiter)  { _fieldDelimiter = aDelimiter; }

/**
 * Returns the record delimiter.
 */
public String getRecordDelimiter()  { return _recordDelimiter; }

/**
 * Sets the record delimiter.
 */
public void setRecordDelimiter(String aDelimiter)  { _recordDelimiter = aDelimiter; }

/**
 * Returns whether first row has field names.
 */
public boolean getHasHeaderRow()  { return _hasHeaderRow; }

/**
 * Sets whether first row has field names.
 */
public void setHasHeaderRow(boolean aFlag)  { _hasHeaderRow = aFlag; }

/**
 * Returns whether fields are quoted.
 */
public boolean getHasQuotedFields()  { return _hasQuotedFields; }

/**
 * Sets whether records are quoted.
 */
public void setHasQuotedFields(boolean aFlag)  { _hasQuotedFields = aFlag; }

/**
 * Reads given source and returns list of maps.
 */
public List <Map> readObject(Object aSource)  { return readObject(aSource, null, true); }

/**
 * Reads given source and returns list of maps.
 */
public List <Map> readObject(Object aSource, Entity anEntity)
{
    _entity = anEntity;
    return readObject(aSource, null, true);
}

/**
 * Reads records from given string (and creates entity).
 */
public List <Map> readObject(Object aSource, String aName, boolean doReadFormat)
{
    // Get source string
    String string = getSourceString(aSource);
    if(string==null)
        return null;
    
    // If we should try to divine format, try to read format
    if(doReadFormat)
        readFormat(string);
    
    // Get records
    String records[] = getRecords(string);
    
    // Create list of lists
    List <String[]> dataset = new ArrayList();
    
    // Iterate over records, get fields and add to dataset
    for(int i=0; i<records.length; i++) { String record = records[i];
        String fields[] = getFields(record);
        dataset.add(fields);
    }
    
    // If entity not set, create from first record
    boolean createEntity = _entity==null;
    if(createEntity) {
        _entity = new Entity(aName!=null? aName : getSourceName(aSource));
    
        // Get first record
        String record0[] = dataset.get(0);
        
        // Iterate over first record
        for(int i=0; i<record0.length; i++) {
            
            // Get property name - trim quotes if needed
            String propertyName = getHasHeaderRow()? record0[i].trim() : "Field" + i;
            if(getHasQuotedFields() && propertyName.startsWith("\"") && propertyName.endsWith("\""))
                propertyName = propertyName.substring(1, propertyName.length()-1);
            
            // Create and add new property (start with type Date, so setTypeFromSample can try to refine)
            _entity.addProperty(new Property(propertyName, Property.Type.Date));
        }
    }
    
    // Create maps list
    List <Map> maps = new ArrayList();

    // Iterate over records to determine property types and add rows
    for(int i=getHasHeaderRow()? 1 : 0, iMax=dataset.size(); i<iMax; i++) { String record[] = dataset.get(i);
        
        // Create map
        Map map = new HashMap();
        
        // Iterate over entity properties
        for(int j=0, jMax=_entity.getPropertyCount(), k=0; j<jMax; j++) { Property property = _entity.getProperty(j);
        
            // If entity was provided, skip properties that are private, autogenerated or relations
            if(!createEntity && (property.isPrivate() || property.isAutoGenerated() || property.isAutoGenerated()))
                continue;
            
            // Get field
            String field = k<record.length? record[k++].trim() : null;
            if(field==null)
                continue;
            
            // Trim quotes if needed
            if(getHasQuotedFields() && field.startsWith("\"") && field.endsWith("\""))
                field = field.substring(1, field.length()-1);
            
            // Set/update type from sample
            if(createEntity) property.setTypeFromSample(field);
        
            // Add attributes
            map.put(property.getName(), field);
        }
        
        // Add map
        maps.add(map);
    }
    
    // Return maps
    return maps;
}

/**
 * Analyzes the given string and sets reader format attributes.
 */
public void readFormat(String aString)
{
    // Declare variable for field delimiter with the largest occurrence count and that count
    String fieldDelimeter = "";
    _fieldDelimiterCount = -1;
    
    // Iterate over common field delimiters to find most common delimiter
    for(String del : new String[] { "\t", ",", ";"} ) {
        int count = 0;
        for(int i=aString.indexOf(del); i>=0; i=aString.indexOf(del, i+1)) count++;
        if(count>_fieldDelimiterCount) {
            fieldDelimeter = del;
            _fieldDelimiterCount = count;
        }
    }
    
    // Set most likely field delimiter
    setFieldDelimiter(fieldDelimeter);
    
    // Declare variable for record delimiter with the largest occurrence count and that count
    String recordDelimeter = "";
    _recordDelimiterCount = -1;
    
    // Iterate over common record delimiters to find most common delimiter
    for(String del : new String[] { "\r\n", "\r", "\n"} ) {
        int count = 0;
        for(int i=aString.indexOf(del); i>=0; i=aString.indexOf(del, i+1)) count++;
        if(count>_recordDelimiterCount) {
            recordDelimeter = del;
            _recordDelimiterCount = count;
        }
    }
    
    // Set most likely record delimiter
    setRecordDelimiter(recordDelimeter);
    
    // Get quote count
    int quoteCount = -1; for(int i=0; i>=0; i=aString.indexOf('"', i+1)) quoteCount++;
    
    // If quote count is greater than number of fields x records * 2, than we can probably assume data is quoted
    setHasQuotedFields(quoteCount>_recordDelimiterCount*1.9);
}

/**
 * Returns the records.
 */
private String[] getRecords(String aString)  { return aString.split(getRecordDelimiter()); }

/**
 * Returns the fields for a record.
 */
private String[] getFields(String aRecord)  // { return aRecord.split(getFieldDelimiter()); }
{
    // Get tokenizer
    Tokenizer tokenizer = getTokenizer();
    tokenizer.setInput(aRecord);
    
    // Get StringBuffer and Fields list
    _sb.setLength(0); _fields.clear();

    // Parse tokens
    for(Token token=tokenizer.getNextToken(); token!=null; token=tokenizer.getNextToken()) {
        
        // If Separator token, add cumulative string and clear String Buffer
        if(token.getName()=="Separator") {
            _fields.add(_sb.toString().trim()); _sb.setLength(0); }
        
        // Otherwise, add to StringBuffer
        else _sb.append(token.getString());
    }
    
    // If remainder, add to fields
    String string = _sb.toString().trim();
    if(string.length()>0) _fields.add(string);
    
    // Return String
    return _fields.toArray(new String[_fields.size()]);
    
} StringBuffer _sb = new StringBuffer(); List <String> _fields = new ArrayList();

/**
 * Returns the tokenizer.
 */
private Tokenizer getTokenizer()
{
    // If tokenizer not set, set
    if(_tokenizer==null) {
        String separator = getFieldDelimiter(); if(separator.equals("\t")) separator = "\\t";
        _tokenizer = new Tokenizer();
        if(getHasQuotedFields())
            _tokenizer.addPattern("QuotedString",
                "\"(([^\"\\\\\\n\\\r])|(\\\\([ntbrf\\\\'\"]|[0-7][0-7]?|[0-3][0-7][0-7]|u[\\da-fA-F]{4})))*\"", false);
        _tokenizer.addPattern("NonSeparator", "[^" + separator + "]+", false);
        _tokenizer.addPattern("Separator", separator, true);
    }
    
    // Return Tokenizer
    return _tokenizer;
    
} Tokenizer _tokenizer;

/**
 * Returns the number of field delimiters found in last readFormat.
 */
public int getFieldDelimiterCount()  { return _fieldDelimiterCount; }

/**
 * Returns the entity generated from last readRecords.
 */
public Entity getEntity()  { return _entity; }

/**
 * Returns the number of record delimeters found in last readFormat.
 */
public int getRecordDelimiterCount()  { return _recordDelimiterCount; }

/**
 * Returns the source name.
 */
public String getSourceName(Object aSource)
{
    if(aSource instanceof File)
        return FileUtils.getFileNameSimple((File)aSource);
    return "Untitled";
}

/**
 * Creates the source string.
 */
public String getSourceString(Object aSource)
{
    // Handle String
    if(aSource instanceof String)
        return (String)aSource;
    
    // Handle Clipboard
    if(aSource instanceof Clipboard) {
        
        // Get clipboard and transferable 
        Clipboard clipboard = (Clipboard)aSource; //Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);
        
        // If contents contains a string, get string and have it replace current selection
        if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
            try { return (String)transferable.getTransferData(DataFlavor.stringFlavor); }
            catch(Exception e) { e.printStackTrace(); }
    }
    
    // Handle anything we can get bytes from
    byte bytes[] = SnapUtils.getBytes(aSource);
    if(bytes!=null)
        return StringUtils.getISOLatinString(bytes);
    
    // Return null since string not found
    return null;
}

}