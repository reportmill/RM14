package com.reportmill.app;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import snap.swing.SwingUtils;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * This component displays a hierarchy of data like a JTree, however, it does it with a number of distinct JTables
 * layed out horizontally.
 */
public class KeysBrowserBase extends JScrollPane {
    
    // The browser model
    BrowserModel            _model;
    
    // The box that the browser columns live in
    ScrollableBox           _columnsBox = new ScrollableBox();
    
    // The minimum number of columns
    int                     _visibleColumnCount = 2;
    
    // The currently selected column index
    int                     _selectedColumnIndex;
    
    // The table cell renderer
    TableCellRenderer       _tcr;
    
    // The icon for a standard branch
    static Icon             _branchIcon;
    
/**
 * An interface for providing data to the browser.
 */
public interface BrowserModel <T> {

    /** Returns the browser root object. */
    public Object getRoot();
    
    /** Returns whether browser object has children. */
    public boolean isLeaf(T anObj);
    
    /** Returns the number of children for the given parent. */
    public int getChildCount(T anObj);
    
    /** Returns browser object children. */
    public Object getChild(T anObj, int anIndex);
}

/**
 * Creates a new browser.
 */
public KeysBrowserBase()  { setViewportView(_columnsBox); }

/**
 * Returns the current browser model.
 */
public BrowserModel getModel()  { return _model; }

/**
 * Reconfigures browser for given model.
 */
public void setModel(BrowserModel aModel)
{
    // Set new model (if null, just return)
    _model = aModel; if(_model==null) return;
    
    // Reset active column and column count
    setColumnCount(1);
    reloadColumns(0);
    setSelectedColumnIndex(0);
}

/**
 * Returns the path constructed by appending the selected row in each column by a dot.
 */
public String getPath()  { return getPath("."); }

/**
 * Returns the path constructed by appending the selected row in each column by a dot.
 */ 
public String getPath(String aSeparator)
{
    // Create string buffer for path
    StringBuffer buf = new StringBuffer();
    
    // Iterate over browser columns to add selected row items
    for(int i=0, iMax=getColumnCount(); i<iMax; i++) {
        JTable col = getColumn(i);
        int row = col.getSelectedRow();
        if(row<0) break;
        Object item = col.getValueAt(row, 0);
        if(i>0) buf.append(aSeparator);
        buf.append(item.toString());
    }
    
    // Return path string
    return buf.toString();
}

/**
 * Returns the selected item.
 */
public Object getSelectedItem()
{
    JTable table = getSelectedColumnIndex()<getColumnCount()? getColumn(getSelectedColumnIndex()) : null;
    int row = table!=null? table.getSelectedRow() : -1;
    return row>=0? table.getValueAt(row, 0) : null;
}

/**
 * Returns whether selected item is leaf.
 */
public boolean isSelectedLeaf()  { Object item = getSelectedItem(); return item==null || _model.isLeaf(item); }

/**
 * Returns the number of columns currently in the browser.
 */
public int getColumnCount()  { return _columnsBox.getComponentCount(); }

/**
 * Sets the number of columns.
 */
protected void setColumnCount(int aValue)
{
    // If value already set, just return
    if(aValue==getColumnCount()) return;
    
    // Remove columns after value
    while(aValue<getColumnCount())
        removeColumn(getColumnCount()-1);
    
    // Add columns up to value
    while(aValue>getColumnCount())
        addColumn(createColumn(), getColumnCount());

    // Make sure last column is visible
    _columnsBox.setSize(_columnsBox.getPreferredSize());
    _columnsBox.scrollRectToVisible(new Rectangle(_columnsBox.getWidth() - 1, 0, 1, 1));
    
    // Reload all tables
    revalidate();
}

/**
 * Returns the number of desired columns, based on the currently selected column.
 */
public int getColumnCountPreferred()
{
    return Math.max(getSelectedColumnIndex() + (isSelectedLeaf()? 1 : 2), getVisibleColumnCount());
}

/**
 * Returns the specific column table at the given index.
 */
public JTable getColumn(int anIndex)
{
    JScrollPane column = (JScrollPane)_columnsBox.getComponent(anIndex);
    return (JTable)column.getViewport().getView();
}

/**
 * Internal method - called to add a browser column table.
 */
private void addColumn(JScrollPane aColumn, int index)  { _columnsBox.add(aColumn, index); }

/**
 * Removes the browser column table at the given index.
 */
private void removeColumn(int index)  { _columnsBox.remove(index); }

/**
 * Reloads columns from given index.
 */
public void reloadColumns(int aStartIndex)
{
    for(int i=aStartIndex; i<getColumnCount(); i++)
        ((AbstractTableModel)getColumn(i).getModel()).fireTableDataChanged();
}

/**
 * Returns the index of a column.
 */
private int indexOfColumn(JTable aColumn)
{
    for(int i=0, iMax=getColumnCount(); i<iMax; i++)
        if(getColumn(i)==aColumn)
            return i;
    return -1;
}

/**
 * Creates a new browser column.
 */
protected JScrollPane createColumn()
{
    // Create new table and add to table list
    JTable table = createColumnTable();

    // Put the new table in a scroll pane and add the scroll pane to the browser box
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getViewport().setBackground(table.getBackground());
    
    // Set scroll pane preferred size to column size
    Rectangle viewportBounds = getViewportBorderBounds();
    Dimension columnSize = new Dimension(viewportBounds.width/getVisibleColumnCount(), viewportBounds.height);
    scrollPane.setPreferredSize(columnSize);
    
    // Return column
    return scrollPane;
}

/**
 * Creates a new browser column table.
 */
protected JTable createColumnTable()
{
    // Create new table from table class
    final JTable table = new JTable();
    
    // Configure table
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    table.setTableHeader(null);
    table.setIntercellSpacing(new Dimension(0,0));
    
    // Create new model and add it to the model list
    BrowserColumnTableModel model = new BrowserColumnTableModel(table);
    
    // Set model and format it
    table.setModel(model);
    table.getColumnModel().getColumn(0).setCellRenderer(getTableCellRenderer());
    table.getColumnModel().getColumn(1).setMinWidth(10);
    table.getColumnModel().getColumn(1).setMaxWidth(20);
    
    // Add list selection listener to update columns when selected value changes
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(final ListSelectionEvent e) {
            SwingUtilities.invokeLater(new Runnable() { public void run() {
                setColumnCount(getColumnCountPreferred());
                reloadColumns(getSelectedColumnIndex()+1);
            }});
        }});
    
    // Add mouse listener to set selected column index
    table.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) { setSelectedColumnIndex(indexOfColumn(table)); } });
    
    // Return table
    return table;
}

/**
 * Returns the number of visible columns in the browser.
 */
public int getVisibleColumnCount()  { return _visibleColumnCount; }

/**
 * Sets the number of visible columns in the browser.
 */
public void setVisibleColumnCount(int aValue)  { _visibleColumnCount = aValue; }

/**
 * Returns the selected column index.
 */
public int getSelectedColumnIndex()  { return _selectedColumnIndex; }

/**
 * Sets the selected column index.
 */
protected void setSelectedColumnIndex(int anIndex)
{
    // If value already set, just return
    if(anIndex==getSelectedColumnIndex()) return;
    
    // Set value
    _selectedColumnIndex = anIndex;
    
    // Clear selection in columns after this one
    for(int i=_selectedColumnIndex+1, iMax=getColumnCount(); i<iMax; i++)
        getColumn(i).clearSelection();
}

/**
 * Returns the browser table cell renderer.
 */
public TableCellRenderer getTableCellRenderer()  { return _tcr!=null? _tcr : (_tcr=createTableCellRenderer()); }

/**
 * Returns the browser table cell renderer.
 */
protected TableCellRenderer createTableCellRenderer()
{
    // Create new table cell renderer class
    return new DefaultTableCellRenderer() {
        
        // Override to render table cells
        public Component getTableCellRendererComponent(JTable aTable, Object anObj, boolean isSel, boolean hasFoc, int row, int col)
        {
            Object item = aTable.getValueAt(row, 0); // Get browser node
            JLabel label = (JLabel)super.getTableCellRendererComponent(aTable, item, isSel, hasFoc, row, col);
            label.setFont(KeysBrowserBase.this.getFont());
            label.setToolTipText(item.toString());
            return label;
        }
    };
}

/**
 * Sets the browser table cell renderer.
 */
public void setTableCellRenderer(TableCellRenderer aTableCellRenderer)  { _tcr = aTableCellRenderer; }

/**
 * Returns the icon to indicate branch nodes in a browser (right arrow by default).
 */
public Icon getBranchIcon(Object anObj)
{
    // If object is null or leaf, return null
    if(anObj==null || _model.isLeaf(anObj)) return null;
    
    // If branch icon hasn't been created, create it
    if(_branchIcon==null) {
        GeneralPath path = new GeneralPath();
        path.moveTo(1.5f, 1.5f); path.lineTo(8.5f, 5.5f); path.lineTo(1.5f, 9.5f); path.closePath();
        _branchIcon = SwingUtils.getImageIcon(path, Color.black, 10, 11);
    }
    
    // Return right arrow icon
    return _branchIcon;
}

/**
 * Sets the default icon to indicate branch nodes in a browser.
 */
public void setBranchIcon(Icon anIcon)  { _branchIcon = anIcon; }

/**
 * Overrides setBounds to reset model.
 */
public void setBounds(int x, int y, int width, int height)
{
    // Do normal set bounds
    super.setBounds(x, y, width, height);
    
    // Reset columns preferred sizes to column size
    Rectangle viewportBounds = getViewportBorderBounds();
    Dimension columnSize = new Dimension(viewportBounds.width/getVisibleColumnCount(), viewportBounds.height);
    for(int i=0, iMax=getColumnCount(); i<iMax; i++)
        _columnsBox.getComponent(i).setPreferredSize(columnSize);
    _columnsBox.revalidate();
}

/**
 * An inner class to represent Browser selection.
 */
public class BrowserColumnTableModel extends AbstractTableModel {

    // The table we work for
    JTable     _table;
    
    // Creates a new browser column table model
    public BrowserColumnTableModel(JTable aTable)  { _table = aTable; }

    // The browser data
    private Object getRoot()
    {
        // Get the index of this model's table - if zero, just return model root
        int index = indexOfColumn(_table);
        if(index<=0)
            return _model.getRoot();
        
        // Get the table for the previous column
        JTable table = getColumn(index - 1);
        
        // Get table's selected row value
        int row = table.getSelectedRow();
        return row<0? null : table.getValueAt(row, 0);
    }
    
    /** Returns browser column row count. */
    public int getRowCount()  { Object root = getRoot(); return _model.isLeaf(root)? 0 : _model.getChildCount(root); }
    
    /** Returns browser column count. */
    public int getColumnCount()  { return 2; }
    
    /** Returns browser column class. */
    public Class getColumnClass(int c)  { return c==0? String.class : ImageIcon.class; }
    
    /** Returns browser column name. */
    public String getColumnName(int c)  { return c==0? "Items" : "Branch Icon"; }
    
    /** Returns that browser is not editable. */
    public boolean isCellEditable(int r, int c)  { return false; }
    
    /** Returns the browser column value - column zero is data value, one is branch icon. */
    public Object getValueAt(int r, int c)
    { return c==0? _model.getChild(getRoot(), r) : getBranchIcon(getValueAt(r, 0)); }
}

/**
 * An component to hold browser columns.
 */
static class ScrollableBox extends JComponent implements Scrollable {
    public ScrollableBox() { setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); }
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    public boolean getScrollableTracksViewportHeight() { return true; }
    public boolean getScrollableTracksViewportWidth() { return false; }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return getScrollableBlockIncrement(r,o,d); }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
        Component c = getComponentCount()>0? getComponent(0) : null;
        return c==null? 1 : c.getWidth() + 8;
    }
}

}