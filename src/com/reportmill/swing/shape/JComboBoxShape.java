package com.reportmill.swing.shape;
import com.reportmill.base.RMListUtils;
import java.util.*;
import javax.swing.*;
import snap.swing.SwingHelper;
import snap.util.*;

/**
 * An RMShape subclass for JComboBox.
 */
public class JComboBoxShape extends JComponentShape {

    // Whether combo box is editable
    boolean           _editable;
    
    // The maximum row count
    int               _maxRowCount = 8;

    // The items
    List <String>     _items;
    
    // The selected index
    int               _selectedIndex = -1;

/**
 * Returns whether combo box is editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether combo box is editable.
 */
public void setEditable(boolean aValue)
{
    firePropertyChange("Editable", _editable, _editable = aValue, -1);
}

/**
 * Returns the maximum row count.
 */
public int getMaximumRowCount()  { return _maxRowCount; }

/**
 * Sets the maximum row count.
 */
public void setMaximumRowCount(int aValue)
{
    firePropertyChange("MaximumRowCount", _maxRowCount, _maxRowCount = aValue, -1);
}

/**
 * Returns list items.
 */
public List <String> getItems()  { return _items; }

/**
 * Sets list items.
 */
public void setItems(List <String> theItems)
{
    firePropertyChange("Items", _items, _items = theItems, -1);
}

/**
 * Adds an item.
 */
public void addItem(String anItem)  { addItem(anItem, _items!=null? _items.size() : 0); }

/**
 * Adds an item.
 */
public void addItem(String anItem, int anIndex)
{
    if(_items==null) _items = new ArrayList();
    _items.add(anIndex, anItem);
    firePropertyChange("Items", null, anItem, anIndex);
}

/**
 * Returns the list items as a single string with items separated by newlines.
 */
public String getItemsString()
{
    List <String> items = getItems(); if(items==null) return null;
    return RMListUtils.joinStrings(items, "\n");
}

/**
 * Sets the list items as a single string with items separated by newlines.
 */
public void setItemsString(String aString)
{
    String items[] = aString!=null? aString.split("\n") : new String[0];
    for(int i=0; i<items.length; i++) items[i] = items[i].trim();
    setItems(Arrays.asList(items));
}

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _selectedIndex; }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    firePropertyChange("SelectedIndex", _selectedIndex, _selectedIndex = anIndex, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JComboBox.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get ComboBox and configure
    JComboBox cbox = (JComboBox)aComp;
    cbox.setEditable(isEditable());
    cbox.setMaximumRowCount(getMaximumRowCount());
    if(getItems()!=null) SwingHelper.getSwingHelper(cbox).setItems(cbox, getItems());
    if(getSelectedIndex()>=0) cbox.setSelectedIndex(getSelectedIndex());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jcombobox");
    
    // Archive Editable
    if(isEditable()) e.add("editable", true);
    
    // Archive MaximumRowCount
    if(getMaximumRowCount()!=8) e.add("MaximumRowCount", getMaximumRowCount());
    
    // Archive Items
    if(getItems()!=null) for(int i=0, iMax=getItems().size(); i<iMax; i++) {
        XMLElement item = new XMLElement("item");
        item.add("text", getItems().get(i).toString());
        e.add(item);
    }
    
    // Archive SelectedIndex 
    if(getSelectedIndex()>=0)
        e.add("SelectedIndex", getSelectedIndex());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive whether combobox is editable
    if(anElement.getAttributeBoolValue("editable")) setEditable(true);
        
    // Unarchive MaximumRowCount (and old maxvisible)
    if(anElement.hasAttribute("MaximumRowCount")) 
        setMaximumRowCount(anElement.getAttributeIntValue("MaximumRowCount"));
    if(anElement.hasAttribute("maxvisible")) setMaximumRowCount(anElement.getAttributeIntValue("maxvisible"));
    
    // Unarchive items
    for(int i=anElement.indexOf("item"); i>=0; i=anElement.indexOf("item", i+1))
        addItem(anElement.get(i).getAttributeValue("text"));
    
    // Unarchive SelectedIndex, ItemDisplayKey
    if(anElement.hasAttribute("SelectedIndex"))
        setSelectedIndex(anElement.getAttributeIntValue("SelectedIndex"));
}

}