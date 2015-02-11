package com.reportmill.swing.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.TableColumn;
import snap.util.*;

/**
 * A RMShape subclass for JTable.
 */
public class JTableShape extends JComponentShape {

    // The auto resize mode
    int              _asizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS;

    // The selection mode
    int              _selMode = ListSelectionModel.SINGLE_SELECTION;
    
    // Whether row/col selection is allowed
    boolean          _rowSel = true, _colSel;
    
    // Whether to show table header
    boolean          _showHeader = true;
    
    // Whether to create row sorter
    boolean          _createRowSorter;
    
    // Whether to show horziontal/vertical lines
    boolean          _showLinesH, _showLinesV;
    
    // Grid color
    RMColor          _gridColor;
    
    // Row height
    int              _rowHeight;
    
    // Intercell spacing
    Dimension        _cellSpacing;

/**
 * Returns whether to show header.
 */
public boolean getShowHeader()  { return _showHeader; }

/**
 * Sets whether to show header.
 */
public void setShowHeader(boolean aValue)
{
    firePropertyChange("ShowHeader", _showHeader, _showHeader = aValue, -1);
}

/**
 * Returns whether row selection is allowed.
 */
public boolean getRowSelectionAllowed()  { return _rowSel; }

/**
 * Sets whether row selection is allowed.
 */
public void setRowSelectionAllowed(boolean aValue)
{
    firePropertyChange("RowSelectionAllowed", _rowSel, _rowSel = aValue, -1);
}

/**
 * Returns whether column selection is allowed.
 */
public boolean getColumnSelectionAllowed()  { return _colSel; }

/**
 * Sets whether row selection is allowed.
 */
public void setColumnSelectionAllowed(boolean aValue)
{
    firePropertyChange("ColumnSelectionAllowed", _colSel, _colSel = aValue, -1);
}

/**
 * Returns the auto resize mode.
 */
public int getAutoResizeMode()  { return _asizeMode; }

/**
 * Sets the auto resize mode.
 */
public void setAutoResizeMode(int aValue)
{
    firePropertyChange("AutoResizeMode", _asizeMode, _asizeMode = aValue, -1);
}

/**
 * Returns the auto resize mode for the given JTable as a simple string.
 */
public String getAutoResizeModeString()
{
    switch(getAutoResizeMode()) {
        case JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS: return "subsequent";
        case JTable.AUTO_RESIZE_NEXT_COLUMN: return "next";
        case JTable.AUTO_RESIZE_ALL_COLUMNS: return "all";
        case JTable.AUTO_RESIZE_LAST_COLUMN: return "last";
        case JTable.AUTO_RESIZE_OFF: return "off";
        default: return null;
    }
}

/**
 * Set the auto resize mode for a table from the given string.
 */
public void setAutoResizeModeString(String aString)
{
    if(aString.equals("subsequent")) setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    else if(aString.equals("next")) setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    else if(aString.equals("all")) setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    else if(aString.equals("last")) setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    else if(aString.equals("off")) setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
}

/**
 * Returns the selection mode.
 */
public int getSelectionMode()  { return _selMode; }

/**
 * Sets the selection mode.
 */
public void setSelectionMode(int aValue)
{
    firePropertyChange("SelectionMode", _selMode, _selMode = aValue, -1);
}

/**
 * Returns the selection mode string for a table.
 */
public String getSelectionModeString()
{
    // Handle selection modes
    switch(getSelectionMode()) {
        case ListSelectionModel.SINGLE_SELECTION: return "single";
        case ListSelectionModel.SINGLE_INTERVAL_SELECTION: return "single-interval";
        case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION: return "multiple-interval";
        default: return null;
    }
}

/**
 * Sets the selection mode for a table from given string.
 */
public void setSelectionModeString(String aString)
{
    if(aString.equals("single")) setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    else if(aString.equals("single-interval")) setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    else if(aString.equals("multiple-interval")) setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
}

/**
 * Returns whether to show horizontal lines.
 */
public boolean getShowHorizontalLines()  { return _showLinesH; }

/**
 * Sets whether to show horizontal lines.
 */
public void setShowHorizontalLines(boolean aValue)
{
    firePropertyChange("ShowHorizontalLines", _showLinesH, _showLinesH = aValue, -1);
}

/**
 * Returns whether to show vertical lines.
 */
public boolean getShowVerticalLines()  { return _showLinesV; }

/**
 * Sets whether to show vertical lines.
 */
public void setShowVerticalLines(boolean aValue)
{
    firePropertyChange("ShowVerticalLines", _showLinesV, _showLinesV = aValue, -1);
}

/**
 * Returns whether to auto create row sorter.
 */
public boolean getAutoCreateRowSorter()  { return _createRowSorter; }

/**
 * Sets whether to auto create row sorter.
 */
public void setAutoCreateRowSorter(boolean aValue)
{
    firePropertyChange("AutoCreateRowSorter", _createRowSorter, _createRowSorter = aValue, -1);
}

/**
 * Returns grid color.
 */
public RMColor getGridColor()  { return _gridColor; }

/**
 * Sets grid color.
 */
public void setGridColor(RMColor aValue)
{
    firePropertyChange("GridColor", _gridColor, _gridColor = aValue, -1);
}

/**
 * Returns the row height.
 */
public int getRowHeight()  { return _rowHeight; }

/**
 * Sets the row height.
 */
public void setRowHeight(int aValue)
{
    firePropertyChange("RowHeight", _rowHeight, _rowHeight = aValue, -1);
}

/**
 * Returns the intercell spacing.
 */
public Dimension getIntercellSpacing()  { return _cellSpacing; }

/**
 * Sets the intercell spacing.
 */
public void setIntercellSpacing(Dimension aValue)
{
    firePropertyChange("IntercellSpacing", _cellSpacing, _cellSpacing = aValue, -1);
}

/**
 * Returns the number of columns.
 */
public int getColumnCount()  { return getChildCount(); }

/**
 * Returns the column shape at given index.
 */
public JTableColumnShape getColumnShape(int anIndex)  { return (JTableColumnShape)getChild(anIndex); }

/**
 * Editor method - overrides default implementation to indicate shape is super selectable.
 */
public boolean superSelectable()  { return true; }

/**
 * Editor method - overrides default implementation to indicate children should super select immediately.
 */
public boolean childrenSuperSelectImmediately()  { return true; }

/**
 * Performs layout.
 */
protected void layoutChildren()
{
    // Get table shape and have JTable do layout
    JTableShape tableShape = (JTableShape)getParent();
    
    // Iterate over table columns and reset successive x values based on cumulative width of preceding columns
    float x = 0;
    for(int i=0, iMax=tableShape.getChildCount(); i<iMax; i++) {
        JTableColumnShape child = tableShape.getColumnShape(i);
        child.setBounds(x, 0, child.getWidth(), tableShape.getHeight());
        x += child.getWidth(); // Increment x by child column width
    }
}
    
/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JTable.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get JTable and configure
    JTable table = (JTable)aComp;
    table.setAutoResizeMode(getAutoResizeMode());
    table.setRowSelectionAllowed(getRowSelectionAllowed());
    table.setColumnSelectionAllowed(getColumnSelectionAllowed());
    if(!getShowHeader()) table.setTableHeader(null);
    table.setAutoCreateRowSorter(getAutoCreateRowSorter());
    if(getGridColor()!=null) table.setGridColor(getGridColor().awt());
    table.setShowHorizontalLines(getShowHorizontalLines());
    table.setShowVerticalLines(getShowVerticalLines());
    if(getRowHeight()>0) table.setRowHeight(getRowHeight());
    if(getIntercellSpacing()!=null) table.setIntercellSpacing(getIntercellSpacing());
    table.getSelectionModel().setSelectionMode(getSelectionMode());
}

/**
 * Override to add children as TableColumns.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JTable table = (JTable)aComp;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JTableColumnShape child = (JTableColumnShape)getChild(i);
        TableColumn col = new TableColumn();
        col.setHeaderValue(child.getHeaderValue());
        col.setIdentifier(child.getItemDisplayKey()!=null? child.getItemDisplayKey() : child.getHeaderValue());
        col.setModelIndex(i);
        col.setResizable(child.isResizable());
        col.setWidth((int)child.getWidth());
        col.setPreferredWidth(child.isPrefWidthSet()? (int)child.getPrefWidth() : 75);
        table.addColumn(col);
        table.setAutoCreateColumnsFromModel(false);
    }
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jtable");
    
    // Archive auto resize mode
    if(getAutoResizeMode()!=JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS)
        e.add("auto-resize-mode", getAutoResizeModeString());
    
    // Archive RowSelectionAllowed, ColumnSelectionAllowed
    if(!getRowSelectionAllowed()) e.add("row-selection-allowed", getRowSelectionAllowed());
    if(getColumnSelectionAllowed()) e.add("column-selection-allowed", getColumnSelectionAllowed());
    
    // Archive ShowHeader
    if(!getShowHeader()) e.add("show-header", false);
    
    // Archive auto create row sorter
    if(RMUtils.boolValue(RMKey.getValue(this, "AutoCreateRowSorter")))
        e.add("auto-create-row-sorter", true);

    // Archive GridColor
    if(getGridColor()!=null) e.add("grid-color", '#' + getGridColor().toHexString());
    
    // Archive ShowHorizontalLines, ShowVerticalLinses
    if(getShowHorizontalLines()) e.add("show-horizontal-lines", getShowHorizontalLines());
    if(getShowVerticalLines()) e.add("show-vertical-lines", getShowVerticalLines());
    
    // Archive RowHeight
    if(getRowHeight()!=16) e.add("row-height", getRowHeight());
    
    // Archive IntercellSpacing X & Y
    Dimension spacing = getIntercellSpacing();
    if(spacing!=null && spacing.width!=1) e.add("spacing-x", spacing.width);
    if(spacing!=null && spacing.height!=1) e.add("spacing-y", spacing.height);
    
    // Archive SelectionMode
    if(getSelectionMode()!=ListSelectionModel.SINGLE_SELECTION)
        e.add("selection", getSelectionModeString());
    
    // Return the element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive AutoResizeMode
    if(anElement.hasAttribute("auto-resize-mode"))
        setAutoResizeModeString(anElement.getAttributeValue("auto-resize-mode"));
    
    // Unarchive RowSelectionAllowed, ColumnSelectionAllowed
    setRowSelectionAllowed(anElement.getAttributeBoolValue("row-selection-allowed", true));
    setColumnSelectionAllowed(anElement.getAttributeBoolValue("column-selection-allowed", false));
    
    // Unarchive TableHeader
    if(anElement.hasAttribute("show-header"))
        setShowHeader(anElement.getAttributeBooleanValue("show-header"));
    
    // Unarchive AutoCreateRowSorter
    if(anElement.hasAttribute("auto-create-row-sorter"))
        setAutoCreateRowSorter(anElement.getAttributeBooleanValue("auto-create-row-sorter"));

    // Unarchive grid color
    if(anElement.hasAttribute("grid-color")) setGridColor(new RMColor(anElement.getAttributeValue("grid-color")));
    
    // Unarchive ShowHorizontalLines, ShowVerticalLines
    setShowHorizontalLines(anElement.getAttributeBoolValue("show-horizontal-lines", false));
    setShowVerticalLines(anElement.getAttributeBoolValue("show-vertical-lines", false));
    
    // Unarchive RowHeight
    setRowHeight(anElement.getAttributeIntValue("row-height", 16));
    
    // Unarchive IntercellSpacing X & Y
    int spacingX = anElement.getAttributeIntValue("spacing-x", 1);
    int spacingY = anElement.getAttributeIntValue("spacing-y", 1);
    if(spacingX!=1 || spacingY!=1)
        setIntercellSpacing(new Dimension(spacingX, spacingY));
    
    // Unarchive SelectionMode string
    if(anElement.hasAttribute("selection"))
        setSelectionModeString(anElement.getAttributeValue("selection"));
}

}