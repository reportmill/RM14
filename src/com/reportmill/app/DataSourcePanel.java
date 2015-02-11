package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.Toolkit;
import java.util.Map;
import javax.swing.*;
import snap.swing.*;
import snap.web.*;

/**
 * This class manages UI display and editing of editor's datasource.
 */
public class DataSourcePanel extends SwingOwner {
    
    // The index of the selected entity in the current datasource
    int        _selectedEntityIndex = -1;
    
    // The index of the selected property in the current datasource
    int        _selectedPropertyIndex = -1;
    
    // Data types
    String     _types[] = { "String", "Number", "Boolean", "Date", "DateTime", "Binary",
                                "To-One Relation", "To-Many Relation" };

/**
 * Initialize UI panel for the datasource inspector.
 */
protected void initUI()
{
    // Set TypeComboBox model
    setNodeItems("TypeComboBox", _types);
        
    // Set SubtypeComboBox model
    getNode("SubtypeComboBox", JComboBox.class).setModel(new SubtypeComboBoxModel());
}

/**
 * Resets UI from the current editor's datasource.
 */
public void resetUI()
{
    // Get selected datasource (just return if null)
    RMDataSource ds = getDataSource(); if(ds==null) return;
    
    // Update DSLabel
    setNodeValue("DSLabel", ds.getName());
    
    // Get selected property
    Property property = getSelectedProperty();
    
    // Reset EntitiesList model
    getNode("EntitiesList", JList.class).setModel(new EntitiesListModel());
    
    // Update EntitiesList selection
    setNodeSelectedIndex("EntitiesList", _selectedEntityIndex);
    
    // Reset PropertiesList model
    getNode("PropertiesList", JList.class).setModel(new PropertiesListModel());
    
    // Update PropertiesList selection
    setNodeSelectedIndex("PropertiesList", _selectedPropertyIndex);
    
    // Make property panel invisible/visible
    setNodeSelectedIndex("CardPanel", property==null? 0 : 1);

    // Update property UI controls
    if(property!=null) {
        setNodeValue("TypeComboBox", prettyType(property.getType()));
        setNodeValue("SubtypeComboBox", property.getRelationEntityName());
        setNodeValue("PrimaryCheckBox", property.isPrimary());
        setNodeValue("PrivateCheckBox", property.isPrivate());
    }
}

/**
 * Responds to changes from the datasource inspector panel's controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get selected shape and data source
    RMShape shape = getSelectedShape(); if(shape==null) return;
    RMDataSource ds = getDataSource();

    // Handle Save As xml dropdown
    if(anEvent.equals("SaveDatasetMenuItem"))
        saveAsXML(ds);

    // Handle EntitiesList: Reset SelectedEntityIndex and SelectedPropertyIndex
    if(anEvent.equals("EntitiesList")) {
        _selectedEntityIndex = anEvent.getSelectedIndex();
        _selectedPropertyIndex = -1;
    }
    
    // Handle PropertiesList
    if(anEvent.equals("PropertiesList"))
        _selectedPropertyIndex = anEvent.getSelectedIndex();
    
    // Handle TypeComboBox
    if(anEvent.equals("TypeComboBox")) {
        getSelectedProperty().setType(unprettyType(anEvent.getStringValue()));
        ds.setCustomSchema(true);
    }
    
    // Handle SubtypeComboBox
    if(anEvent.equals("SubtypeComboBox")) {
        getSelectedProperty().setRelationEntityName(anEvent.getStringValue());
        ds.setCustomSchema(true);
    }
    
    // Handle PrimaryCheckBox
    if(anEvent.equals("PrimaryCheckBox")) {
        getSelectedProperty().setPrimary(anEvent.getBooleanValue());
        ds.setCustomSchema(true);
    }
    
    // Handle PrivateCheckBox
    if(anEvent.equals("PrivateCheckBox")) {
        getSelectedProperty().setPrivate(anEvent.getBooleanValue());
        ds.setCustomSchema(true);
    }
}

/** 
 * Saves the current datasource's dataset as an xml dataset.
 */
public void saveAsXML(RMDataSource source)
{
    // Get dataset (if null, beep and return)
    Map dataset = source.getDataset();
    if(dataset==null) {
        Toolkit.getDefaultToolkit().beep(); return; }
       
    // Run the save panel (just return if null)
    String path = FileChooserUtils.showChooser(true, getUI(), "xml dataset file", ".xml");
    if(path==null)
        return;
    
    // Use the XMLWriter to write out the dataset
    RMXMLWriter xmlWriter = new RMXMLWriter();
    xmlWriter.writeObject(dataset, path);
}

/**
 * Returns the currently selected shape.
 */
public RMShape getSelectedShape()  { return RMEditor.getMainEditor().getDocument(); }

/**
 * Returns the datasource for the editor's document.
 */
private RMDataSource getDataSource()
{
    RMDocument document = RMEditor.getMainEditor().getDocument();
    return document!=null? document.getDataSource() : null;
}

/**
 * Returns the schema for the datasource.
 */
private Schema getSchema()
{
    RMDataSource dataSource = getDataSource();
    return dataSource!=null? dataSource.getSchema() : null;
}

/**
 * Returns the selected entity of the current datasource.
 */
private Entity getSelectedEntity()
{
    // Get schema (if null, return null)
    Schema schema = getSchema(); if(schema==null) return null;
    
    // If selected entity index is greater than schema entity count, reset
    if(_selectedEntityIndex>=schema.getEntityCount())
        _selectedEntityIndex = schema.getEntityCount() - 1;
    
    // If selected entity index less than zero, return null
    if(_selectedEntityIndex<0)
        return null;
    
    // Return selected entity
    return schema.getEntity(_selectedEntityIndex);
}

/**
 * Returns the selected property as indicated by the PropertiesList.
 */
private Property getSelectedProperty()
{
    // Get entity (if null, return null)
    Entity entity = getSelectedEntity(); if(entity==null) return null;
    
    // If selected property index is greater than entity property count, reset
    if(_selectedPropertyIndex>=entity.getPropertyCount())
        _selectedPropertyIndex = entity.getPropertyCount() - 1;
    
    // If selected property index is less than zero, return null
    if(_selectedPropertyIndex<0)
        return null;
    
    // Return entity property
    return entity.getProperty(_selectedPropertyIndex);
}

/**
 * Returns the name to be used in the inspector panel window title.
 */
public String getWindowTitle()  { return "Datasource Inspector"; }

/**
 * Returns the "user-friendly" name for the given property type name.
 */
private String prettyType(Property.Type aType)
{
    if(aType==null) return "None";
    if(aType==Property.Type.Relation) return "To-One Relation";
    if(aType==Property.Type.RelationList) return "To-Many Relation";
    return aType.toString();
}

/**
 * Returns the internal name for the user-friendly property name.
 */
private Property.Type unprettyType(String aType)
{
    if(aType==null || aType.length()==0) return null;
    if(aType.equals("To-One Relation")) return Property.Type.Relation;
    if(aType.equals("To-Many Relation")) return Property.Type.RelationList;
    return Property.Type.valueOf(aType);
}

/**
 * An inner class to track entities JList.
 */
private class EntitiesListModel extends AbstractListModel {

    // Returns schema entity count
    public int getSize()
    {
        Schema schema = getSchema(); if(schema==null) return 0;
        return schema.getEntityCount();
    }
    
    // Returns specific entity name
    public Object getElementAt(int anIndex)  { return getSchema().getEntity(anIndex).getName(); }
}

/**
 * An inner class to track properties JList.
 */
private class PropertiesListModel extends AbstractListModel {

    // Returns the selected entity property count
    public int getSize()
    {
        Entity entity = getSelectedEntity(); if(entity==null) return 0;
        return entity.getPropertyCount();
    }
    
    // Returns the specific property name
    public Object getElementAt(int anIndex)  { return getSelectedEntity().getProperty(anIndex).getName(); }
}

/**
 * An inner class to track type JComboBox.
 */
private class SubtypeComboBoxModel extends AbstractListModel implements ComboBoxModel {

    // Returns the schema entity count plus one ("None")
    public int getSize()  { Schema schema = getSchema(); return schema==null? 1 : schema.getEntityCount() + 1; }
    
    // Returns the specific schema entity name (or "None")
    public Object getElementAt(int index)  { return index==0? "None" : getSchema().getEntity(index-1).getName(); }
    
    // Returns the selected item
    public Object getSelectedItem()
    {
        Property p = getSelectedProperty();
        return p==null? null : p.getRelationEntityName();
    }
    
    // ???
    public void setSelectedItem(Object anItem) { }
}

}