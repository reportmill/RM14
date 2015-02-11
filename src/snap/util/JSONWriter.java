/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.Iterator;

/**
 * Writes a JSON to string.
 */
public class JSONWriter {

    // The current indent level
    int         _indentLevel = 0;
    
    // The indent string
    String      _indent = "\t";
    
    // Whether writer compacts JSON (no indent or newline)
    boolean     _compacted = false;
    
/**
 * Returns the current indent.
 */
public String getIndent()  { return _indent; }

/**
 * Sets the current indent string.
 */
public JSONWriter setIndent(String anIndent)  { _indent = anIndent; return this; }

/**
 * Returns whether writer compacts JSON (no indent or newline).
 */
public boolean isCompacted()  { return _compacted; }

/**
 * Sets whether writer compacts JSON (no indent or newline).
 */
public JSONWriter setCompacted(boolean aValue)  { _compacted = aValue; return this; } 

/**
 * Returns a string for given JSON node.
 */
public String getString(JSONNode aNode)  { return getStringBuffer(aNode).toString(); }

/**
 * Returns a string buffer for given JSON node.
 */
public StringBuffer getStringBuffer(JSONNode aNode)  { return append(new StringBuffer(1024), aNode); }

/**
 * Returns a string buffer for given JSON node.
 */
protected StringBuffer append(StringBuffer aSB, JSONNode aNode)
{
    // Handle node types
    switch(aNode.getType()) {
    
        // Handle Map
        case Map: {
            
            // Get whether map is deep (not leaf)
            boolean deep = _indentLevel==0 || isDeep(aNode);
            
            // Append map opening
            aSB.append('{');
            if(deep)
                appendNewlineIndent(aSB, ++_indentLevel);
            else aSB.append(' ');
            
            // Append keys, values and separators
            for(Iterator i=aNode.getKeys().iterator(); i.hasNext();) { String key = (String)i.next();
            
                // Append key and value
                aSB.append(key).append(':');
                JSONNode valueNode = aNode.get(key);
                if(valueNode.getType()==JSONNode.Type.Map || valueNode.getType()==JSONNode.Type.List)
                    aSB.append(' ');
                append(aSB, valueNode);
                
                // If has next, append separator and whitespace
                if(i.hasNext()) {
                    if(deep)
                        appendNewlineIndent(aSB.append(','));
                    else aSB.append(", ");
                }
            }
            
            // Append trailing whitespace and close
            if(deep)
                appendNewlineIndent(aSB, --_indentLevel).append('}');
            else aSB.append(" }");
            
        } break;
        
        // Handle List
        case List: {
            
            // Get whether list is deep (not leaf)
            boolean deep = isDeep(aNode);
            
            // Append list opening
            aSB.append('[');
            if(deep)
                appendNewlineIndent(aSB, ++_indentLevel);
            else aSB.append(' ');
            
            // Iterate over items to append items and separators
            for(int i=0, iMax=aNode.size(); i<iMax; i++) { boolean hasNext = i+1<iMax;
            
                // Append item
                append(aSB, aNode.get(i));
                
                // If has next, append separator
                if(hasNext) {
                    if(deep)
                        appendNewlineIndent(aSB.append(','));
                    else aSB.append(", ");
                }
            }
            
            // Append trailing whitespace and close
            if(deep)
                appendNewlineIndent(aSB, --_indentLevel).append(']');
            else aSB.append(" ]");
            
        } break;
        
        // Handle String
        case String: {
            aSB.append('"');
            for(int i=0, iMax=aNode.getString().length(); i<iMax; i++) { char c = aNode.getString().charAt(i);
                if(c=='"' || c=='\\' || c=='/')
                    aSB.append('\\').append(c);
                else if(c=='\b') aSB.append("\\b");
                else if(c=='\f') aSB.append("\\f");
                else if(c=='\n') aSB.append("\\n");
                else if(c=='\r') aSB.append("\\r");
                else if(c=='\t') aSB.append("\\t");
                else if(Character.isISOControl(c))
                    System.err.println("RMJSONWriter.append: Tried to print control char in string: " + aNode.getString());
                else aSB.append(c);
            }
            aSB.append('"');
        } break;
        
        // Handle Number
        case Number: aSB.append(aNode.getNumber()); break;
        
        // Handle Boolean
        case Boolean: aSB.append(aNode.getBoolean()? "true" : "false"); break;
        
        // Handle Null
        case Null: aSB.append("null"); break;
    }
    
    // Return string buffer
    return aSB;
}

/**
 * Appends newline and indent.
 */
protected StringBuffer appendNewlineIndent(StringBuffer aSB)  { return appendNewlineIndent(aSB, _indentLevel); }

/**
 * Appends newline and indent.
 */
protected StringBuffer appendNewlineIndent(StringBuffer aSB, int aLevel)
{
    // If tiny mode enabled, just append space and return
    if(isCompacted())
        return aSB.append(' ');
    
    // Otherwise, append newline
    aSB.append('\n');
    
    // And indent
    for(int i=0; i<aLevel; i++)
        aSB.append(_indent);
    
    // Return string buffer
    return aSB;
}

/**
 * Writes the given JSON object to given file path.
 */
public void writeJSON(JSONNode aNode, String aPath)
{
    String json = getString(aNode);
    SnapUtils.writeBytes(StringUtils.getBytes(json), aPath);
}

/**
 * Returns whether given node has child Map or List of Map/List.
 */
protected boolean isDeep(JSONNode aNode)
{
    // Handle type Map - is deep if any child is Map or List of Map
    if(aNode.getType()==JSONNode.Type.Map)
        for(String key: aNode.getKeys()) { JSONNode node = aNode.get(key);
            if(node.getType()==JSONNode.Type.Map || isDeep(node))
                return true;
        }
    
    // Handle type List - is deep if any child is Map or List
    if(aNode.getType()==JSONNode.Type.List)
        for(int i=0, iMax=aNode.size(); i<iMax; i++) { JSONNode node = aNode.get(i);
            if(node.getType()==JSONNode.Type.Map || node.getType()==JSONNode.Type.List)
                return true;
        }
    
    // Handle anything else
    return false;
}

}