package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import java.util.*;

/**
 * This class represents a cross reference table in a PDF file.
 */
public class PDFXTable {
    
    // List of entries
    List   _entries = new Vector(16);
    
    // Map of entry names
    Map    _names = new Hashtable();
    
/**
 * Creates an empty xref table.
 */
public PDFXTable() { }

/**
 * Returns the number of entries in xref table.
 */
public int getEntryCount()  { return _entries.size(); }

/**
 * Returns the specific entry at the given index.
 */
public Object getEntry(int anIndex)  { return _entries.get(anIndex); }

/**
 * Adds an object and returns the string by which the object can be referenced inside the pdf file.
 */
public String addObject(Object anObj)  { return addObject(anObj, false); }

/**
 * Adds an object and returns the string by which the object can be referenced inside the pdf file.
 */
public String addObject(Object anObj, boolean definitelyIsNew)
{
    // Check to see if it's there already
    int index = definitelyIsNew? -1 : RMListUtils.indexOfId(_entries, anObj);

    if(index == -1) {
        _entries.add(anObj);
        index = _entries.size();
    }
    else index++;
    
    // Return
    return getRefString(index);
}

/**
 * Returns the index of a given entry object.
 */
public int indexOfEntry(Object obj)
{
    int index = RMListUtils.indexOfId(_entries, obj);
    if (index != -1) ++index;
    return index;
}

/**
 * Sets the name for a given entry object.
 */
public void setNameForObject(String aName, Object anObj)
{
    if(indexOfEntry(anObj)>0)
        _names.put(aName, anObj);
    else throw new RuntimeException("object not present in xref table");
}

/**
 * Returns a reference string for the entry object at the given index.
 */
public String getRefString(int index)
{
    if(index<1 || index>_entries.size())
        throw new RuntimeException("Entry #" + index + " not in xref table");
    return index + " 0 R";
}

/**
 * Returns a reference string for the given entry object.
 */
public String getRefString(Object anObj)
{
    // If there are a million objects, we may want to change entries from array to dictionary for performance
    int index = indexOfEntry(anObj);
    if(index==-1)
        throw new RuntimeException("object not present in xref table");
    return getRefString(index);
}

/**
 * Returns a reference string for the given entry object name.
 */
public String getRefStringForName(String aName)
{
    Object obj = _names.get(aName);
    return obj==null? null : getRefString(obj);
}

}