package com.reportmill.swing.shape;
import com.reportmill.base.RMListUtils;
import java.util.*;
import javax.swing.*;
import snap.swing.SwingHelper;
import snap.util.*;

/**
 * An RMShape subclass for JLists.
 */
public class JListShape extends JComponentShape {

    // The SelectionMode
    int               _selectionMode;
    
    // The selected index
    int               _selectedIndex = -1;

    // The items
    List <String>     _items;

/**
 * Returns the selection mode.
 */
public int getSelectionMode()  { return _selectionMode; }

/**
 * Sets the selection mode.
 */
public void setSelectionMode(int aMode)
{
    firePropertyChange("SelectionMode", _selectionMode, _selectionMode = aMode, -1);
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
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JList.class; }

/**
 * Override to configure component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get list and configure
    JList jlist = (JList)aComp;
    jlist.setSelectionMode(getSelectionMode());
    SwingHelper helper = SwingHelper.getSwingHelper(jlist);
    if(getItems()!=null && getItems().size()>0) helper.setItems(jlist, getItems());
    if(getSelectedIndex()>=0) helper.setSelectedIndex(jlist, getSelectedIndex());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jlist");
    
    // Archive selection mode
    if(getSelectionMode()==ListSelectionModel.SINGLE_INTERVAL_SELECTION) e.add("selection", "single-interval");
    else if(getSelectionMode()==ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        e.add("selection", "multiple-interval");
    
    // Archive SelectedIndex
    if(getSelectedIndex()>=0) e.add("SelectedIndex", getSelectedIndex());
    
    // Archive Items
    if(getItems()!=null) for(int i=0, iMax=getItems().size(); i<iMax; i++) {
        XMLElement item = new XMLElement("item");
        item.add("text", getItems().get(i));
        e.add(item);
    }
    
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
    
    // Set selectionMode
    String selection = anElement.getAttributeValue("selection", "single");
    if(selection.equals("single")) setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    else if(selection.equals("single-interval")) setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    else setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    // Unarchive SelectedIndex
    if(anElement.hasAttribute("SelectedIndex")) setSelectedIndex(anElement.getAttributeIntValue("SelectedIndex"));
    
    // Unarchive items
    if(anElement.indexOf("item")>=0) {
        List <String> items = new ArrayList();
        for(int i=anElement.indexOf("item"); i>=0; i=anElement.indexOf("item", i+1))
            items.add(anElement.get(i).getAttributeValue("text"));
        setItems(items);
    }
}

/**
 * Override JComponentShape version to suppress reading of JMenuItems as JComponentShapes (JListHpr will load them).
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement) { }

}