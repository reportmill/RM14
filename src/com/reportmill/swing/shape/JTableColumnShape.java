package com.reportmill.swing.shape;
import snap.util.*;

/**
 * Represents a JTable TableColumn.
 */
public class JTableColumnShape extends JComponentShape {

    // The header value
    String             _headerVal;
    
    // Whether is resizable
    boolean            _resizable;

/**
 * Returns the header value.
 */
public String getHeaderValue()  { return _headerVal; }

/**
 * Sets the header value.
 */
public void setHeaderValue(String aValue)
{
    firePropertyChange("HeaderValue", _headerVal, _headerVal = aValue, -1);
}

/**
 * Returns whether resizable.
 */
public boolean isResizable()  { return _resizable; }

/**
 * Sets the resizable.
 */
public void setResizable(boolean aValue)
{
    firePropertyChange("Resizable", _resizable, _resizable = aValue, -1);
}

/**
 * Overrides shape method to say we want events (to pass on to component).
 */
public boolean acceptsMouse() { return false; }

/**
 * XML archival - table columns.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Create xml for column
    XMLElement cxml = new XMLElement("column");
    
    // Archive HeaderValue
    if(getHeaderValue()!=null) cxml.add("Header", getHeaderValue());
    
    // Archive identifier
    if(getItemDisplayKey()!=null && getItemDisplayKey().length()>0 && !getItemDisplayKey().equals(getHeaderValue()))
        cxml.add("ItemDisplayKey", getItemDisplayKey());
    
    // Archive model index
    //if(getModelIndex()!=anIndex) cxml.add("index", getModelIndex());
    
    // Archive Width, MinWidth, MaxWith, PrefWidth, Resizable
    if(getWidth()!=75) cxml.add("width", getWidth());
    if(getMinWidth()!=15) cxml.add("min-width", getMinWidth());
    //if(getMaxWidth()!=Integer.MAX_VALUE) cxml.add("max-width", getMaxWidth());
    if(getPrefWidth()!=75) cxml.add("pref-width", getPrefWidth());
    if(!isResizable()) cxml.add("resizable", false);
    
    // Return column xml
    return cxml;
}
    
/**
 * XML unarchival - table columns.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive ColumnHeader
    if(anElement.hasAttribute("Header")) setHeaderValue(anElement.getAttributeValue("Header"));
    
    // Unarchive identifier
    if(anElement.hasAttribute("ItemDisplayKey")) setItemDisplayKey(anElement.getAttributeValue("ItemDisplayKey"));
    
    // Unarchive model index
    //if(anElement.hasAttribute("index")) setModelIndex(anElement.getAttributeIntValue("index"));
    //else setModelIndex(getColumnCount());
    
    // Unarchive Width, MinWidth, MaxWidth, PrefWidth and Resizable
    if(anElement.hasAttribute("width")) setWidth(anElement.getAttributeIntValue("width"));
    if(anElement.hasAttribute("min-width")) setMinWidth(anElement.getAttributeIntValue("min-width"));
    //if(anElement.hasAttribute("max-width")) setMaxWidth(anElement.getAttributeIntValue("max-width"));
    if(anElement.hasAttribute("pref-width")) setPrefWidth(anElement.getAttributeIntValue("pref-width"));
    if(anElement.hasAttribute("resizable")) setResizable(anElement.getAttributeBoolValue("resizable"));
}

}