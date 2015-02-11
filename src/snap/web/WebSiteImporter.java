/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import snap.util.ListUtils;

/**
 * This class provides functionality to import a list of connected maps.
 */
public class WebSiteImporter {

    // The data site to work with
    WebSite           _site;

    // List of provided maps
    List <Map>        _providedMaps = new ArrayList();
    
    // List of created rows
    List <Row>        _createdRows = new ArrayList();
    
/**
 * Creates a new WebSiteImporter with given data site.
 */
public WebSiteImporter(WebSite aSite)  { _site = aSite; }

/**
 * Returns importer data site.
 */
public WebSite getSite()  { return _site; }

/**
 * Creates a new row for given entity name and map.
 */
public Row createRow(String anEntityName, Map aMap)
{
    Entity entity = getSite().getEntity(anEntityName);
    return createRow(entity, aMap);
}

/**
 * Imports a map.
 */
public Row createRow(Entity anEntity, Map aMap)
{
    // Look for previously created row for provided object
    int index = ListUtils.indexOfId(_providedMaps, aMap);
    Row row = index>=0? _createdRows.get(index) : null;
    if(row!=null)
        return row;
    
    // Create row, add to lists and return
    row = getSite().createRow(anEntity, null);
    _providedMaps.add(aMap);
    _createdRows.add(row);
    return row;
}

/**
 * Creates a row deep.
 */
public void createRowDeep(Map aMap, Row aRow)
{
    // Get entity
    Entity entity = aRow.getEntity();
    
    // Add properties
    for(Property property : entity.getProperties()) {
        
        // If property is primary or derived, just skip
        if(property.isPrimary()) continue;
        
        // Get provided object property value (just skip if null)
        String pname = property.getName();
        Object value = aMap.get(pname); if(value==null) continue;
        
        // If property isn't relation, just add
        if(!property.isRelation() || property.getRelationEntity()==null)
            aRow.put(property, value);
        
        // If to-one, create row and add
        else if(value instanceof Map) { Map map = (Map)value;
            value = createRow(property.getRelationEntity(), map);
            aRow.put(property, value);
        }
        
        // If to-many, get list of rows for maps and add
        else if(value instanceof List) { List list = (List)value, list2 = new ArrayList();
            for(Object item : list) { Map map = (Map)item;
                Row createdRow = createRow(property.getRelationEntity(), map);
                list2.add(createdRow); }
            aRow.put(property, list2);
        }
        
        // Otherwise complain
        else System.err.println("WebSiteImporter.createRowDeep: Import failure " + entity.getName() + "." + pname);
    }
}

/**
 * Save rows.
 */
public void saveRows()
{
    // Make sure all rows have been created deep
    for(int i=0; i<_providedMaps.size(); i++)
        createRowDeep(_providedMaps.get(i), _createdRows.get(i));
    
    // Save row for all created rows
    for(Row row : _createdRows)
        row.save();
    
    // Clear map/row lists
    _providedMaps.clear();
    _createdRows.clear();
}

}