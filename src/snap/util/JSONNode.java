/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.util.*;

/**
 * Represents a node in a JSON tree.
 */
public class JSONNode {

    // The type of node
    Type           _type = Type.Null;
    
    // The value
    Object         _value;
    
    // The keys list (if map)
    List <String>  _keys;
    
    // Constants for JSON types
    public enum Type { Map, List, String, Number, Boolean, Null };
    
/**
 * Creates a new node.    
 */
public JSONNode()  { }

/**
 * Creates a new node.    
 */
public JSONNode(Object aSource)
{
    try {
        JSONNode node = readSource(aSource);
        _type = node._type;
        _value = node._value;
        _keys = node._keys;
    }
    catch(Exception e) { System.err.println(e); setValue(e.toString()); }
}

/**
 * Creates a JSON node tree from a JSON string or JSON String byte source.
 */
public static JSONNode readSource(Object aSource) throws JSONReader.RMJSONException
{
    // Get JSON string from source (either a String or a byte source)
    String string = aSource instanceof String? (String)aSource : null;
    if(string==null) {
        byte bytes[] = SnapUtils.getBytes(aSource);
        string = StringUtils.getString(bytes);
    }
    
    // Read string
    return JSONReader.nextNode(new JSONReader(string));
}

/**
 * Returns the the node type.
 */
public Type getType()  { return _type; }

/**
 * Sets the node type.
 */
protected void setType(Type aType)  { _type = aType; }

/**
 * Returns the value.
 */
public Object getValue()  { return _value; }

/**
 * Sets the value.
 */
protected JSONNode setValue(Object anObj)
{
    // Handle enum special
    if(anObj instanceof Enum)
        anObj = anObj.toString();
    
    // Set value
    _value = anObj;
    
    // Set type
    if(_value instanceof Map)
        setType(Type.Map);
    else if(_value instanceof List)
        setType(Type.List);
    else if(_value instanceof String)
        setType(Type.String);
    else if(_value instanceof Number)
        setType(Type.Number);
    else if(_value instanceof Boolean)
        setType(Type.Boolean);
    else if(_value==null)
        setType(Type.Null);
    else throw new RuntimeException("RMJSONNode: Unsupported core type (" + _value.getClass().getName() + ")");
    
    // Return this node
    return this;
}

/**
 * Returns value for key if node type Map.
 */
public JSONNode get(String aKey)  { return getJSON(getMap().get(aKey)); }

/**
 * Sets value for key.
 */
public JSONNode put(String aKey, Object aValue)
{
    ListUtils.addUnique(getKeys(), aKey);
    Object o = getMap().put(aKey, getJSON(aValue));
    return o==null? null : getJSON(aValue);
}

/**
 * Returns keys list.
 */
public List <String> getKeys()  { return _keys!=null? _keys : (_keys = new ArrayList()); }

/**
 * Returns the number of JSON nodes if node type List.
 */
public int size()  { return getType()==Type.List? getList().size() : getType()==Type.Map? getMap().size() : 0; }

/**
 * Returns the individual node at given index.
 */
public JSONNode get(int anIndex)  { return getJSON(getList().get(anIndex)); }

/**
 * Returns a JSON node of given value.
 */
protected JSONNode getJSON(Object anObj)
{
    return anObj instanceof JSONNode? (JSONNode)anObj : new JSONNode().setValue(anObj);
}

/**
 * Returns the value as Map if type is Map.
 */
public Map<String,Object> getMap()  { return (Map)_value; }

/**
 * Returns the value as List if type is List.
 */
public List getList()  { return (List)_value; }

/**
 * Returns the value as String if type is String.
 */
public String getString()  { return (String)_value; }

/**
 * Returns the value as Number if type is Number.
 */
public Number getNumber()  { return (Number)_value; }

/**
 * Returns the value as Map if type is Map.
 */
public Boolean getBoolean()  { return (Boolean)_value; }

/**
 * Converts a JSON object to Map.
 */
public Object getNonJSON()
{
    // Handle Node Types
    switch(getType()) {
    
        // Handle Type Map
        case Map: {
            Map map = new HashMap();
            for(String key : getMap().keySet()) { JSONNode node = get(key);
                Object value = node.getNonJSON();
                if(value!=null)
                    map.put(key, value);
            }
            return map;
        }
        
        // Handle Type List
        case List: {
            List list = new ArrayList();
            for(int i=0, iMax=size(); i<iMax; i++)
                list.add(get(i).getNonJSON());
            return list;
        }
        
        // Handle String
        case String: return getString();
        case Number: return getNumber();
        case Boolean: return getBoolean();
        default: return null;
    }
}

/**
 * Returns a string representation of node (as JSON, of course).
 */
public String toString()  { return new JSONWriter().getString(this); }

/**
 * Returns a string representation of node (as JSON, of course).
 */
public String toStringCompacted()  { return new JSONWriter().setCompacted(true).getString(this); }

/**
 * Simple main implementation to read standard in and re-print JSON.
 */
public static void main(String args[])
{
    // Declare string
    String string = null;
    
    // Read string from standard in
    try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuffer sb = new StringBuffer();
        for(String line=reader.readLine(); line!=null; line=reader.readLine())
            sb.append(line);
        string = sb.toString();
    }
    catch(Exception e) { e.printStackTrace(); return; }

    // Skip to first map or list char 
    int index1 = string.indexOf("{");
    int index2 = string.indexOf("[");
    int index3 = index1<0? index2 : (index2<0? index1 : Math.min(index1, index2));
    if(index3>0)
        string = string.substring(index3);
    
    // Print output
    try { System.out.println(JSONNode.readSource(string)); }
    catch(Exception e) { e.printStackTrace(); }
}

}