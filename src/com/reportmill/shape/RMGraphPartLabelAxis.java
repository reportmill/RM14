package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.text.*;
import snap.util.*;

/**
 * This shape is used by graph area to hold attributes of the value axis.
 */
public class RMGraphPartLabelAxis extends RMTextShape {

    // Whether to show axis labels
    boolean       _showAxisLabels = true;

    // Whether to show grid lines between label axis sections
    boolean       _showGridLines = true;

    // The label axis item key
    String        _itemKey = "@Row@";
    
    // Default cell paragraph (aligned center)
    static RMParagraph _defaultParagraph = RMParagraph.DEFAULT.deriveAligned(RMTypes.AlignX.Center);

/**
 * Returns whether the graph shows axis labels.
 */
public boolean getShowAxisLabels()  { return _showAxisLabels; }

/**
 * Sets whether the graph shows axis labels.
 */
public void setShowAxisLabels(boolean aFlag)  { _showAxisLabels = aFlag; }

/**
 * Returns whether the graph shows grid lines between label axis sections.
 */
public boolean getShowGridLines()  { return _showGridLines; }

/**
 * Sets whether the graph shows grid lines between label axis sections.
 */
public void setShowGridLines(boolean aFlag)  { _showGridLines = aFlag; }

/**
 * Returns the item key.
 */
public String getItemKey()  { return _itemKey; }

/**
 * Sets the item key.
 */
public void setItemKey(String aKey)  { _itemKey = aKey; }

/**
 * Overrides RMText method to create an xstring that is aligned center by default.
 */
public RMXString createXString()
{
    return new RMXString() {
        public RMParagraph getDefaultParagraph() { return _defaultParagraph; }
    };
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("label-axis");
    
    // Archive ShowAxisLabels, ShowGridLines, ItemKey
    if(!_showAxisLabels) e.add("show-labels", false);
    if(!_showGridLines) e.add("show-grid", false);
    if(_itemKey!=null && _itemKey.length()>0) e.add("item-key", _itemKey);
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive ShowAxisLabels, ShowGridLines, ItemKey
    setShowAxisLabels(anElement.getAttributeBoolValue("show-labels", true));
    setShowGridLines(anElement.getAttributeBoolValue("show-grid", true));
    setItemKey(anElement.getAttributeValue("item-key"));
    
    // Return this graph
    return this;
}

}