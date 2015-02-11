package com.reportmill.base;
import java.util.*;
import snap.util.*;

/**
 * This object represents an individual grouping with attributes like key, sorts, topN sort, etc.
 */
public class RMGrouping extends SnapObject implements XMLArchiver.Archivable {
    
    // The grouping key
    String           _key;
    
    // The list of sorts
    List <RMSort>    _sorts = new Vector();
    
    // The top N sort
    RMTopNSort       _topNSort = new RMTopNSort(null, RMSort.ORDER_ASCEND, 0, false);
    
    // Values (in the form of comma separated keychain keys) explicitly defined to sort or to include
    String           _values;
    
    // Whether to sort on explicitly defined values
    boolean          _sortOnValues;
    
    // Whether to explicitly include explicitly defined values
    boolean          _includeValues;
    
    // Whether grouping includes all values for grouping key found in entire dataset in every subgroup
    boolean          _includeAllValues = false;
    
    // Whether grouping has header
    boolean          _hasHeader = false;
    
    // Whether grouping has details
    boolean          _hasDetails = false;
    
    // Whether grouping has summary
    boolean          _hasSummary = false;
    
    // Selected sort index (used in editer only)
    int              _selectedSortIndex = -1;

/**
 * Creates an empty grouping.
 */
public RMGrouping() { }

/**
 * Creates a grouping with the given key.
 */
public RMGrouping(String aKey)  { _key = aKey; }

/**
 * Returns the grouping key.
 */
public String getKey()  { return _key; }

/**
 * Sets the grouping key.
 */
public void setKey(String aValue)
{
    // If key is already set, just return
    if(RMUtils.equals(aValue, _key)) return;
    
    // Set value and fire property change
    firePropertyChange("Key", _key, _key = aValue, -1);
}

/**
 * Returns the groupings list of sorts.
 */
public List <RMSort> getSorts()  { return _sorts; }

/**
 * Returns the number of sorts in the grouping.
 */
public int getSortCount()  { return _sorts.size(); }

/**
 * Returns the individual sort at the given index.
 */
public RMSort getSort(int anIndex)  { return _sorts.get(anIndex); }

/**
 * Adds the given sort to the grouping.
 */
public RMGrouping addSort(RMSort aSort)  { addSort(aSort, getSortCount()); return this; }

/**
 * Adds the given sort to the grouping.
 */
public void addSort(RMSort aSort, int anIndex)
{
    // Add sort
    _sorts.add(anIndex, aSort);
    
    // Set selected sort index
    _selectedSortIndex = anIndex;
    
    // Fire property change
    firePropertyChange("Sort", null, aSort, anIndex);
}

/**
 * Removes the sort at the given index.
 */
public RMSort removeSort(int anIndex)
{
    // Remove sort
    RMSort sort = _sorts.remove(anIndex);
    
    // Set selected sort index
    _selectedSortIndex = Math.min(anIndex, getSortCount() - 1);
    
    // Fire property change
    firePropertyChange("Sort", sort, null, anIndex);
    
    // Return sort
    return sort;
}

/**
 * Adds the list of sorts to the grouping.
 */
public void addSorts(List<RMSort> aList)  { for(RMSort s : aList) addSort(s); }

/**
 * Adds a sort to the grouping for the given sort key.
 */
public void addSort(String aSortKey)  { addSort(new RMSort(aSortKey)); }

/**
 * Removes the given sort from the grouping.
 */
public int removeSort(RMSort aSort)
{
    int index = RMListUtils.indexOfId(getSorts(), aSort);
    if(index>=0)
        removeSort(index);
    return index;
}

/**
 * Moves a sort from the source index to the destination index.
 */
public void moveSort(int fromIndex, int toIndex)
{
    RMSort sort = removeSort(fromIndex);
    addSort(sort, toIndex);
}

/**
 * Returns the top N sort for the grouping.
 */
public RMTopNSort getTopNSort()  { return _topNSort; }

/**
 * Sets the top N sort for the grouping.
 */
public void setTopNSort(RMTopNSort aSort)
{
    // If no change, just return
    if(RMUtils.equals(aSort, _topNSort)) return;
    
    // Set value and fire property change
    firePropertyChange("TopNSort", _topNSort, _topNSort = aSort, -1);
}

/**
 * Returns whether the grouping includes all values.
 */
public boolean getIncludeAllValues()  { return _includeAllValues; }

/**
 * Sets whether the grouping includes all values.
 */
public void setIncludeAllValues(boolean aValue)
{
    // If no change, just return
    if(aValue==_includeAllValues) return;
    
    // Fire property change
    firePropertyChange("IncludeAllValues", _includeAllValues, _includeAllValues = aValue, -1);
}

/**
 * Returns the values (in the form of comma separated keychain keys) explicitly defined to sort or to include. 
 */
public String getValuesString()  { return _values; }

/**
 * Sets the values (in the form of comma separated keychain keys) explicitly defined to sort or to include. 
 */
public void setValuesString(String aString)
{
    // If no change, just return
    if(RMUtils.equals(aString, _values)) return;
    
    // Fire property change
    firePropertyChange("ValuesString", _values, _values = aString, -1);
}

/**
 * Returns a list of explicit values for this grouping.
 */
public List getValues()
{
    // Create list to return
    List valuesList = new ArrayList();
    
    // If no values string, just return
    if(_values==null)
        return valuesList;
    
    // Get values with commas replaced by newlines
    String valuesString = _values.replace(',', '\n');
    
    // Convert to string array
    String valueStrings[] = valuesString.split("\n");
    
    // Iterate over values strings
    for(int i=0; i<valueStrings.length; i++) {
        
        // Get current loop value trimmed
        String valueString = valueStrings[i].trim();
        
        // If length is non-zero, evaluate and add
        if(valueString.length()>0) {
            
            // Evaluate value string as key (maybe it would be useful to support keys on aReportMill one day?)
            Object value = RMKeyChain.getValue(new Object(), valueString);
            
            // If value is non-null, add it
            if(value!=null)
                valuesList.add(value);
        }
    }
    
    // Return valuesList
    return valuesList;
}

/**
 * Returns whether to sort on values explicitly provided. 
 */
public boolean getSortOnValues()  { return _sortOnValues; }

/**
 * Sets whether to sort on values explicitly provided. 
 */
public void setSortOnValues(boolean aFlag)
{
    // If no change, just return
    if(aFlag==_sortOnValues) return;
    
    // Set value and fire property change
    firePropertyChange("SortOnValues", _sortOnValues, _sortOnValues = aFlag, -1);
}

/**
 * Returns whether to include values explicitly provided. 
 */
public boolean getIncludeValues()  { return _includeValues; }

/**
 * Sets whether to include values explicitly provided. 
 */
public void setIncludeValues(boolean aFlag)
{
    // If no change, just return
    if(aFlag==_includeValues) return;
    
    // Set value and fire property change
    firePropertyChange("IncludeValues", _includeValues, _includeValues = aFlag, -1);
}

/**
 * Returns whether the grouping has a header.
 */
public boolean getHasHeader()  { return _hasHeader; }

/**
 * Sets whether the grouping has a header.
 */
public void setHasHeader(boolean aValue)
{
    // If no change, just return
    if(aValue==_hasHeader) return;
    
    // Set value and fire property change
    firePropertyChange("HasHeader", _hasHeader, _hasHeader = aValue, -1);
}

/**
 * Returns whether the grouping has a details.
 */
public boolean getHasDetails()  { return _hasDetails; }

/**
 * Sets whether the grouping has a details.
 */
public void setHasDetails(boolean aValue)
{
    // If no change, just return
    if(aValue==_hasDetails) return;
    
    // Set value and fire property change
    firePropertyChange("HasDetails", _hasDetails, _hasDetails = aValue, -1);
}

/**
 * Returns whether the grouping has a summary.
 */
public boolean getHasSummary()  { return _hasSummary; }

/**
 * Sets whether the grouping has a summary.
 */
public void setHasSummary(boolean aValue)
{
    // If no change, just return
    if(aValue==_hasSummary) return;
    
    // Set value and fire property change
    firePropertyChange("HasSummary", _hasSummary, _hasSummary = aValue, -1);
}

/**
 * Returns the currently selected grouping's currently selected sort (for editing, mostly).
 */
public int getSelectedSortIndex()  { return _selectedSortIndex; }

/**
 * Sets the currently selected grouping's currently selected sort (for editing, mostly).
 */
public void setSelectedSortIndex(int anIndex)  { _selectedSortIndex = anIndex; }

/**
 * Returns the currently selected grouping's sort (while editing only).
 */
public RMSort getSelectedSort()
{
    // If selected sort index is out of bounds, just return null
    if(_selectedSortIndex<0 || _selectedSortIndex>=getSortCount())
        return null;
    
    // Return selected sort
    return getSort(_selectedSortIndex);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity
    if(anObj==this) return true;
    
    // Check class
    if(!(anObj instanceof RMGrouping)) return false;
    
    // Get other grouping
    RMGrouping other = (RMGrouping)anObj;
    
    // Check key
    if(!RMUtils.equals(other._key, _key)) return false;
    
    // Check sorts
    if(!RMUtils.equals(other._sorts, _sorts)) return false;
    
    // Check top N sort
    if(!RMUtils.equals(other._topNSort, _topNSort)) return false;
    
    // Check include all values
    if(other._includeAllValues!=_includeAllValues) return false;
    
    // Check values string
    if(!RMUtils.equals(other._values, _values)) return false;
    
    // Check sort on values
    if(other._sortOnValues!=_sortOnValues) return false;
    
    // Check include values
    if(other._includeValues!=_includeValues) return false;
    
    // Check has header, details, summary
    if(other._hasHeader!=_hasHeader) return false;
    if(other._hasDetails!=_hasDetails) return false;
    if(other._hasSummary!=_hasSummary) return false;
    
    // Return true if all checks pass
    return true;
}

/**
 * Standard clone implementation.
 */
public RMGrouping clone()
{
    // Do basic clone
    RMGrouping clone = (RMGrouping)super.clone();
    
    // Clone sorts
    clone._sorts = RMListUtils.cloneDeep(_sorts);
    
    // Clone top N  sort
    clone._topNSort = RMUtils.clone(_topNSort);
    
    // Return clone
    return clone;
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named grouper
    XMLElement e = new XMLElement("grouping");
    
    // Archive key
    if(_key!=null && _key.length()>0)
        e.add("key", _key);
    
    // Archive sorts
    for(int i=0, iMax=_sorts.size(); i<iMax; i++)
        e.add(getSort(i).toXML(anArchiver));
        
    // Archive top n sort key
    if(_topNSort.getKey()!=null && _topNSort.getKey().length()>0)
        e.add("topn", _topNSort.getKey());
        
    // Archive top n sort order
    if(_topNSort.getOrder()!=RMSort.ORDER_ASCEND)
        e.add("topn-order", _topNSort.getOrderString());
    
    // Archive top n sort count
    if(_topNSort.getCount()>0)
        e.add("topn-count", _topNSort.getCount());
    
    // Archive top n sort includeOthers
    if(_topNSort.getIncludeOthers())
        e.add("topn-include", true);
    
    // Archive top n sort pad
    if(_topNSort.getPad())
        e.add("topn-pad", true);
    
    // Archive includeAllValues
    if(_includeAllValues)
        e.add("allvalues", true);
    
    // Archive values string
    if(_values!=null && _values.length()>0)
        e.add("values", _values);
    
    // Archive sort on values
    if(_sortOnValues)
        e.add("sort-on-values", _sortOnValues);
    
    // Archive include values
    if(_includeValues)
        e.add("include-values", _includeValues);
        
    // Archive hasHeader, hasDetails, hasSummary
    if(_hasHeader)
        e.add("header", true);
    if(_hasDetails)
        e.add("details", true);
    if(_hasSummary)
        e.add("summary", true);
        
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive key
    if(anElement.hasAttribute("key"))
        setKey(anElement.getAttributeValue("key"));
    
    // Unarchive sorts
    _sorts = anArchiver.fromXMLList(anElement, "sort", null, this);
        
    // Unarchive top n sort key
    if(anElement.hasAttribute("topn"))
        _topNSort.setKey(anElement.getAttributeValue("topn"));
    
    // Unarchive top n sort order
    if(anElement.hasAttribute("topn-order"))
        _topNSort.setOrderString(anElement.getAttributeValue("topn-order"));
    
    // Unarchive top n sort count
    if(anElement.hasAttribute("topn-count"))
        _topNSort.setCount(anElement.getAttributeIntValue("topn-count"));

    // Unarchive top n sort includeOthers
    if(anElement.hasAttribute("topn-include"))
        _topNSort.setIncludeOthers(anElement.getAttributeBoolValue("topn-include"));
        
    // Unarchive top n sort pad
    if(anElement.hasAttribute("topn-pad"))
        _topNSort.setPad(anElement.getAttributeBoolValue("topn-pad"));
        
    // Unarchive includeAllValues
    setIncludeAllValues(anElement.getAttributeBoolValue("allvalues"));
    
    // Unarchive values string
    _values = anElement.getAttributeValue("values");
    
    // Unarchive sort on values
    if(anElement.hasAttribute("sort-on-values"))
        _sortOnValues = anElement.getAttributeBoolValue("sort-on-values");
    
    // Unarchive include values
    if(anElement.hasAttribute("include-values"))
        _includeValues = anElement.getAttributeBoolValue("include-values");
    
    // Unarchive hasHeader, hasDetails, hasSummary
    setHasHeader(anElement.getAttributeBoolValue("header"));
    setHasDetails(anElement.getAttributeBoolValue("details"));
    setHasSummary(anElement.getAttributeBoolValue("summary"));
        
    // Return this grouping
    return this;
}

/**
 * Returns string representation of grouping. 
 */
public String toString()  { return getClass().getSimpleName() + ": " + getKey(); }

}