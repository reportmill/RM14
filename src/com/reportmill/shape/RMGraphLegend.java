package com.reportmill.shape;
import com.reportmill.text.*;
import snap.util.*;

/**
 * An inner class for Legend.
 */
public class RMGraphLegend extends RMFlowShape {

    // The legend text
    String          _legendText;
    
    // The number of columns in legend
    int             _colCount = 1;
    
    // The text shape
    RMTextShape     _textShape = new RMTextShape(createXString());
    
/**
 * Returns the graph that owns this legend.
 */
public RMGraph getGraph()  { return getParent()!=null? getParent().getChildWithClass(RMGraph.class) : null; }

/**
 * Overrides RMText method to create an xstring that defaults to Arial 10.
 */
private RMXString createXString()
{
    return new RMXString() { public RMFont getDefaultFont() { return RMFont.Helvetica10; } };
}

/**
 * Returns the legend text.
 */
public String getLegendText()  { return _legendText; }

/**
 * Sets the legend text.
 */
public void setLegendText(String aString)  { _legendText = aString; relayout(); }

/**
 * Returns the number of columns in legend.
 */
public int getColumnCount()  { return _colCount; }

/**
 * Sets the number of columns in legend.
 */
public void setColumnCount(int aValue)
{
    firePropertyChange("ColumnCount", _colCount, _colCount = aValue, -1);
    relayout(); repaint();
}

/**
 * Override to forward to TextShape.
 */
public RMFont getFont()  { return _textShape.getFont(); }

/**
 * Override to forward to TextShape.
 */
public void setFont(RMFont aFont)  { _textShape.setFont(aFont); }

/**
 * Override to layout legend.
 */
protected void layoutChildren()
{
    // Remove children
    removeChildren(); if(getParent()==null) return;
    
    // Get Graph/GraphRPG
    RMGraph graph = getParent().getChildWithClass(RMGraph.class);
    RMGraphRPG graphRPG = graph==null? getGraphRPG() : null;
    
    // Get graph and iterate over series
    if(graph!=null) for(int i=0, iMax=Math.max(graph.getKeyCount(), 1); i<iMax; i++) {
        RMRectShape box = new RMRectShape(); box.setSize(16, 12); box.setColor(graph.getColor(i));
        addChild(box);
        RMTextShape label = _textShape.clone(); label.setText("Series " + (i+1));
        addChild(label, (i+1)%getColumnCount()==0? "N" : null);
    }
    
    // If no graph, do GraphRPG
    else if(graphRPG!=null) { graph = graphRPG._graph;
        for(int i=0, iMax=graphRPG.getSeriesCount(); i<iMax; i++) { RMGraphSeries series = graphRPG.getSeries(i);
            RMRectShape box = new RMRectShape(); box.setSize(16, 12); box.setColor(graph.getColor(i));
            addChild(box);
            RMTextShape label = _textShape.clone(); label.setText(graph.getSeries(i).getTitle());
            label.getXString().rpgClone(graphRPG._rptOwner, series._group, null, false);
            addChild(label, (i+1)%getColumnCount()==0? "N" : null);
        }
    }
    
    // Do normal version
    super.layoutChildren();
    
    // Reszie
    if(getBestWidth()>getWidth()) setWidth(getBestWidth());
    if(getBestHeight()>getHeight()) setHeight(getBestHeight());
}

/**
 * Returns RMGraphRPG, if parent has RMGraphRPG.GraphShape child.
 */
private RMGraphRPG getGraphRPG()
{
    RMGraphRPG.GraphShape gshp = getParent()!=null? getParent().getChildWithClass(RMGraphRPG.GraphShape.class) : null;
    return gshp!=null? gshp.getGraphRPG() : null;
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes
    XMLElement e = super.toXMLShape(anArchiver); e.setName("graph-legend");
    
    // Archive LegendText, ColumnCount
    if(getLegendText()!=null && getLegendText().length()>0) e.add("text", getLegendText());
    if(getColumnCount()>1) e.add("ColumnCount", getColumnCount());
    
    // Archive TextShape
    XMLElement tsxml = _textShape.toXML(anArchiver); tsxml.setName("TextShape");
    if(tsxml.getAttributeCount()>0) e.addElement(tsxml);
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive LegendText, ColumnCount
    setLegendText(anElement.getAttributeValue("text"));
    if(anElement.hasAttribute("ColumnCount")) setColumnCount(anElement.getAttributeIntValue("ColumnCount"));
    
    // Unarchive TextShape
    XMLElement tsxml = anElement.getElement("TextShape");
    if(tsxml!=null) _textShape.fromXML(anArchiver, tsxml);
}

}