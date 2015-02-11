package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.text.*;
import snap.util.*;

/**
 * This shape is used by graph area to hold attributes of the value axis.
 */
public class RMGraphPartValueAxis extends RMTextShape {

    // Whether to draw axis labels
    boolean       _showAxisLabels = true;

    // Whether to show major grid lines
    boolean       _showMajorGrid = true;

    // Whether to show minor grid lines
    boolean       _showMinorGrid = true;

    // The axis minimum
    float         _axisMin = Float.MIN_VALUE;
    
    // The axis maximum
    float         _axisMax = Float.MIN_VALUE;
    
    // The number of axis steps
    int           _axisCount = 0;
    
    // Default cell paragraph (aligned center)
    static RMParagraph _defaultParagraph = RMParagraph.DEFAULT.deriveAligned(RMTypes.AlignX.Center);

/**
 * Overrides RMText method to create an xstring that defaults to Arial 10.
 */
public RMXString createXString()
{
    return new RMXString() {
        public RMFont getDefaultFont() { return RMFont.Helvetica10; }
        public RMParagraph getDefaultParagraph() { return _defaultParagraph; }
        public RMFormat getDefaultFormat() { return RMNumberFormat.BASIC; }
    };
}

/**
 * Returns whether the graph draws axis labels.
 */
public boolean getShowAxisLabels()  { return _showAxisLabels; }

/**
 * Sets whether the graph draws axis labels.
 */
public void setShowAxisLabels(boolean aFlag)  { _showAxisLabels = aFlag; }

/**
 * Returns whether the graph draws the major axis.
 */
public boolean getShowMajorGrid()  { return _showMajorGrid; }

/**
 * Sets whether the graph draws the major axis.
 */
public void setShowMajorGrid(boolean aFlag)  { _showMajorGrid = aFlag; }

/**
 * Returns whether the graph draws the minor axis.
 */
public boolean getShowMinorGrid()  { return _showMinorGrid; }

/**
 * Sets whether the graph draws the minor axis.
 */
public void setShowMinorGrid(boolean aFlag)  { _showMinorGrid = aFlag; }

/**
 * Returns the axis minimum.
 */
public float getAxisMin()  { return _axisMin; }

/**
 * Sets the axis minimum.
 */
public void setAxisMin(float aValue)  { _axisMin = aValue; }

/**
 * Returns the axis maximum.
 */
public float getAxisMax()  { return _axisMax; }

/**
 * Sets the axis maximum.
 */
public void setAxisMax(float aValue)  { _axisMax = aValue; }

/**
 * Returns the number of increments in the axis.
 */
public int getAxisCount()  { return _axisCount; }

/**
 * Sets the number of increments in the axis.
 */
public void setAxisCount(int aValue)  { _axisCount = aValue; }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("value-axis");
        
    // Archive ShowAxisLabels, ShowMajorGrid, ShowMinorGrid, AxisMin, AxisMax, AxisCount
    if(!_showAxisLabels) e.add("show-labels", false);
    if(!_showMajorGrid) e.add("show-major-grid", false);
    if(!_showMinorGrid) e.add("show-minor-grid", false);
    if(_axisMin!=Float.MIN_VALUE) e.add("axis-min", _axisMin);
    if(_axisMax!=Float.MIN_VALUE) e.add("axis-max", _axisMax);
    if(_axisCount>0) e.add("axis-count", _axisCount);
    
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
    
    // Unarchive ShowAxisLabels, ShowMajorGrid, ShowMinorGrid, AxisMin, AxisMax, AxisCount
    setShowAxisLabels(anElement.getAttributeBoolValue("show-labels", true));
    setShowMajorGrid(anElement.getAttributeBoolValue("show-major-grid", true));
    setShowMinorGrid(anElement.getAttributeBoolValue("show-minor-grid", true));
    if(anElement.hasAttribute("axis-min")) setAxisMin(anElement.getAttributeFloatValue("axis-min"));
    if(anElement.hasAttribute("axis-max")) setAxisMax(anElement.getAttributeFloatValue("axis-max"));
    if(anElement.hasAttribute("axis-count")) setAxisCount(anElement.getAttributeIntValue("axis-count"));

    // Return this graph
    return this;
}

}