package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import snap.swing.*;

/**
 * This class provides Swing UI editing for Tables.
 */
public class RMTableTool <T extends RMTable> extends RMParentShapeTool <T> implements RMSortPanel.Owner {
    
    // The grouping table
    JTable               _groupingTable;
    
    // The sort panel
    RMSortPanel          _sortPanel;
    
    // Used for splitshape editing in shape editing mouse loop
    RMPoint              _lastMousePoint;

    // Used for splitshape editing in shape editing mouse loop
    int                  _resizeBarIndex;

    // Constants for images used by inspector
    static Icon PageBreakIcon   = Swing.getIcon("group-pagebreak.png", RMTableTool.class);
    static Icon NoPageBreakIcon = Swing.getIcon("group-nobreak.png", RMTableTool.class);

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get grouping and configure
    _groupingTable = getNode("GroupingTable", JTable.class);
    _groupingTable.setRowHeight(20);
    _groupingTable.setTableHeader(null);
    _groupingTable.setFillsViewportHeight(true);
    _groupingTable.setDefaultRenderer(Object.class, new ToolTipRenderer());
    _groupingTable.setModel(new GroupingTableModel());
    _groupingTable.getColumnModel().getColumn(1).setMaxWidth(20);
    enableEvents(_groupingTable, MouseClicked); // So we get called for click on PageBreakIcon
    enableEvents(_groupingTable, DragDrop);
    
    // Add GroupingTable PopupMenu Listener
    _groupingTable.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            if(e.isPopupTrigger())
                getGroupingTablePopupMenu().show(e.getComponent(), e.getX(), e.getY()); }});
    
    // Get SortPanel, configure and add
    _sortPanel = new RMSortPanel(this);
    _sortPanel.getUI().setBounds(4, 170, 267, 100);
    getUI().add(_sortPanel.getUI());
}

/**
 * Updates UI panel from currently  table
 */
public void resetUI()
{
    // Get currently selected table, grouper and grouping (just return if null)
    RMTable table = getTable(); if(table==null) return;
    RMGrouper grouper = table.getGrouper();
    RMGrouping grouping = getGrouping();
    
    // Update ListKeyText, FilterKeyText, NumColumnsText, ColumnSpacingText
    setNodeValue("ListKeyText", table.getDatasetKey());
    setNodeValue("FilterKeyText", table.getFilterKey());
    setNodeValue("NumColumnsText", table.getColumnCount());
    setNodeValue("ColumnSpacingText", getUnitsFromPoints(table.getColumnSpacing()));
    
    // Update HeaderCheckBox, DetailsCheckBox, SummaryCheckBox
    setNodeValue("HeaderCheckBox", grouping.getHasHeader());
    setNodeValue("DetailsCheckBox", grouping.getHasDetails());
    setNodeValue("SummaryCheckBox", grouping.getHasSummary());
    
    // Update GroupingTable
    setNodeSelectedIndex(_groupingTable, grouper.getSelectedGroupingIndex());
    _groupingTable.repaint();
    
    // Update TableGroupButton text
    String buttonText = table.getParent() instanceof RMTableGroup? "Ungroup TableGroup" : "Make TableGroup";
    setNodeText("TableGroupButton", buttonText);
    
    // Update SortPanel
    _sortPanel.resetUI();
}

/**
 * Updates currently selected table from UI panel.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected table, grouper and grouping (just return if null)
    RMTable table = getTable(); if(table==null) return;
    RMGrouper grouper = table.getGrouper();
    RMGrouping grouping = getGrouping();
    
    // Handle ListKeyText, FilterKeyText
    if(anEvent.equals("ListKeyText")) table.setDatasetKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
    if(anEvent.equals("FilterKeyText")) table.setFilterKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));

    // Handle AddGroupMenuItem
    if(anEvent.equals("AddGroupMenuItem")) {
        
        // Run input dialog to get new group key
        DialogBox dbox = new DialogBox("Add Grouping Key"); dbox.setQuestionMessage("Enter Grouping Key:");
        String key = dbox.showInputDialog(getUI(), null);
        
        // If key was returned, add grouping
        if(key!=null)
            addGroupingKey(key);
    }
    
    // Handle RemoveGroupMenuItem
    if(anEvent.equals("RemoveGroupMenuItem")) {
        
        // If grouping isn't last grouping, remove grouping
        if(grouping!=grouper.getGroupingLast())
            table.removeGrouping(grouping);
        
        // Otherwise beep
        else Toolkit.getDefaultToolkit().beep();
    }
    
    // Handle KeysMenuItem
    if(anEvent.equals("KeysMenuItem"))
        getEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);
    
    // Handle GroupingTable
    if(anEvent.equals("GroupingTable")) {
        
        // Handle DropEvent
        if(anEvent.isDragDropEvent()) {
            
            // Get drop string and drop point
            String string = anEvent.getDropString().replace("@", "");
            Point point = anEvent.getLocation();
            
            // Get toRow from down point
            int toRow = _groupingTable.rowAtPoint(point);
            
            // Get fromRow by iterating over table rows
            int fromRow = -1;
            for(int i=0, iMax=_groupingTable.getRowCount(); i<iMax; i++)
                if(string.equals(_groupingTable.getValueAt(i, 0)))
                    fromRow = i;
                
            // If drag is from group table, move grouping, otherwise add grouping
            if(fromRow>=0)
                table.moveGrouping(fromRow, toRow);
            else addGroupingKey(string);
        }
        
        // Handle SelectionEvent and MouseClicked
        else {
            
            // Update grouper SelectedGroupingIndex
            int row = _groupingTable.getSelectedRow();
            int col = _groupingTable.getSelectedColumn();
            grouper.setSelectedGroupingIndex(row);
            
            // If MouseClicked, set or reset PageBreakGroupIndex
            if(anEvent.isMouseClicked() && col==1)
                table.setPageBreakGroupIndex(row==table.getPageBreakGroupIndex()? -1 : row);
        }
    }
    
    // Handle MoveGroupUpMenuItem
    if(anEvent.equals("MoveGroupUpMenuItem")) {
        int loc = _groupingTable.getSelectedRow();
        if(loc>0)
            table.moveGrouping(loc, loc - 1);
    }
    
    // Handle MoveGroupDownMenuItem
    if(anEvent.equals("MoveGroupDownMenuItem")) {
        int loc = _groupingTable.getSelectedRow();
        if(loc<_groupingTable.getRowCount() - 1)
            table.moveGrouping(loc, loc + 1);
    }
    
    // Handle HeaderCheckBox, DetailsCheckBox, SummaryCheckBox
    if(anEvent.equals("HeaderCheckBox")) grouping.setHasHeader(anEvent.getBoolValue());
    if(anEvent.equals("DetailsCheckBox")) grouping.setHasDetails(anEvent.getBoolValue());
    if(anEvent.equals("SummaryCheckBox")) grouping.setHasSummary(anEvent.getBoolValue());
    
    // Handle NumColumnsText, ColumnSpacingText
    if(anEvent.equals("NumColumnsText")) table.setColumnCount(anEvent.getIntValue());
    if(anEvent.equals("ColumnSpacingText")) table.setColumnSpacing(getPointsFromUnits(anEvent.getFloatValue()));
    
    // Handle TableGroupButton
    if(anEvent.equals("TableGroupButton")) {
        
        // Get table parent
        RMParentShape parent = table.getParent();
        
        // If in TableGroup, get out of it
        if(parent instanceof RMTableGroup) {
            RMTableGroup tableGroup = (RMTableGroup)parent; tableGroup.repaint();
            tableGroup.removeTable(table);
            tableGroup.getParent().addChild(table);
            table.setFrame(tableGroup.getFrame());
            if(tableGroup.getChildTableCount()==0)
                tableGroup.getParent().removeChild(tableGroup);
            RMEditor.getMainEditor().setSelectedShape(table);
        }
        
        // If not in TableGroup, create one and add
        else {
            
            // Create new table group
            RMTableGroup tableGroup = new RMTableGroup();

            // Configure tableGroup
            tableGroup.copyShape(table);
            tableGroup.addPeerTable(table);

            // Add TableGroup to table's parent and select tableGroup
            tableGroup.undoerSetUndoTitle("Make TableGroup");
            parent.removeChild(table);
            parent.addChild(tableGroup);
            RMEditor.getMainEditor().setSelectedShape(tableGroup);
        }
    }
}

/**
 * Returns the selected table.
 */
public RMTable getTable()
{
    // Get editor selected or super selected shape - if shape isn't table, go up hierarchy until table is found
    RMShape shape = getEditor()!=null? getEditor().getSelectedOrSuperSelectedShape() : null;
    while(shape!=null && !(shape instanceof RMTable))
        shape = shape.getParent();
    
    // Return shape as table
    return (RMTable)shape;
}

/**
 * Returns the selected grouping for this table.
 */
public RMGrouping getGrouping()
{
    RMTable table = getTable();
    return table!=null? table.getGrouper().getSelectedGrouping() : null;
}

/**
 * Returns the shape class this tool edits (RMTable).
 */
public Class getShapeClass()  { return RMTable.class; }

/**
 * Returns the display name for this tool ("Table Inspector").
 */
public String getWindowTitle()  { return "Table Inspector"; }

/**
 * Overridden to make table super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overridden to make table not ungroupable.
 */
public boolean isUngroupable(RMShape aShape)  { return false; }

/**
 * Returns the popup menu for the grouping table.
 */
public JPopupMenu getGroupingTablePopupMenu()
{
    JPopupMenu pmenu = new JPopupMenu();
    JMenuItem mitem = new JMenuItem("Move Up"); mitem.setName("MoveGroupUpMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Move Down"); mitem.setName("MoveGroupDownMenuItem"); pmenu.add(mitem);
    initUI(pmenu);
    return pmenu;
}

/**
 * Adds a grouping key to the currently selected table.
 */
public void addGroupingKey(String aKey)
{
    getTable().undoerSetUndoTitle("Add Grouping");
    getTable().addGroupingKey(aKey, 0);
}

/**
 * Adds a new table to the given editor with the given dataset key.
 */
public static void addTable(RMEditor anEditor, String aKeyPath)
{
    // Create new default table for key path
    RMTable table = new RMTable(aKeyPath==null? "Objects" : aKeyPath);

    // Set table location in middle of selected shape
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    table.setXY(parent.getWidth()/2 - table.getWidth()/2, parent.getHeight()/2 - table.getHeight()/2);

    // Add table
    anEditor.undoerSetUndoTitle("Add Table");
    parent.addChild(table);

    // Select table, select selectTool and redisplay
    anEditor.setCurrentToolToSelectTool();
    anEditor.setSelectedShape(table);
}

/**
 * Support for managing Groups JTable.
 */
private class GroupingTableModel extends AbstractTableModel {

    /** Returns the selected table row group count. */
    public int getRowCount()  { RMTable t = getTable(); return t==null? 0 : t.getGroupingCount(); }
    
    /** Returns 2 columns. */
    public int getColumnCount()  { return 2; }
    
    /** Returns column classes. */
    public Class getColumnClass(int c)  { return c==0? String.class : Icon.class; }
    
    /** Returns column names. */
    public String getColumnName(int c)  { return c==0? "Grouping" : "Break"; }
    
    /** Makes everything not editable. */
    public boolean isCellEditable(int r, int c)  { return false; }
    
    /** Returns value for given row & column. */
    public Object getValueAt(int r, int c)
    { 
        if(c==0) return getTable().getGrouper().getGroupingKey(r);
        return getTable().getPageBreakGroupIndex()==r? PageBreakIcon : NoPageBreakIcon;
    }
}

/**
 * MouseMoved implementation to update cursor for resize bars.
 */
public void mouseMoved(T aTable, MouseEvent anEvent)
{
    // Get event point in table coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aTable);
    
    // Get resize bar for point
    int resizeBarIndex = aTable.getResizeBarAtPoint(point);

    // If resize bar is under point, set cursor
    if(resizeBarIndex>=0) {
        
        // Get the table row above resize bar
        RMTableRow tableRow = (RMTableRow)aTable.getChild(resizeBarIndex);
        
        // If point is before resize bar controls, set cursor to N resize
        if(point.x<getResizeBarPopupX(tableRow) - 20)
            getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            
        // Otherwise, set it to null
        else getEditor().setCursor(null);
        
        // Consume event
        anEvent.consume();
    }
    
    // Otherwise, do normal mouse moved
    else super.mouseMoved(aTable, anEvent);
}

/**
 * Event handling for table editing.
 */
public void mousePressed(T aTable, MouseEvent anEvent)
{
    // Initialize resize bar index
    _resizeBarIndex = -1;
    
    // Get event point in table coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aTable);
    
    // If table isn't super selected, forward to TableRow and return
    if(!isSuperSelected(aTable)) {
        RMTableRow tableRow = (RMTableRow)aTable.getChildContaining(point); // Get hit table row
        if(tableRow!=null && tableRow.isStructured()) // If table row is structured
            RMTool.getTool(tableRow).mousePressed(tableRow, anEvent);
        return;
    }
    
    // If we're not editor super selected shape, just return
    if(getEditor().getSuperSelectedShape()!=aTable) return;
    
    // If point hit's table row, just return
    if(aTable.getChildContaining(point)!=null) return;
    
    // Since we are the editor super selected shape, consume event to indicate we'll handle events
    anEvent.consume();
    
    // If point is inside table group button, super select table group 
    if(aTable.getParent() instanceof RMTableGroup && point.x<100 && point.y>aTable.getHeight()-18)
        getEditor().setSuperSelectedShape(aTable.getParent());
    
    // Get resize bar index for point
    _resizeBarIndex = aTable.getResizeBarAtPoint(point);
    
    // If no selected resize bar, just return
    if(_resizeBarIndex == -1) return;
    
    // Get the table row above resize bar
    RMTableRow tableRow = (RMTableRow)aTable.getChild(_resizeBarIndex);
    
    // Get the x location of resize bar popup menu
    double resizeBarPopupX = getResizeBarPopupX(tableRow);

    // If downPoint is on version, run its context menu
    if(point.x > resizeBarPopupX) {
        getEditor().setSuperSelectedShape(tableRow); // Super select resize bar table row and run popup menu
        runMenuForShape(aTable, (int)resizeBarPopupX, (int)aTable.getResizeBarBounds(_resizeBarIndex).getMaxY());
        _resizeBarIndex = -1; // Reset resize bar index
    }

    // If downPoint is on structuredButton, change structured state of child
    else if(point.x > resizeBarPopupX - 20) {
        aTable.undoerSetUndoTitle("Turn Table Row Structuring " + (tableRow.isStructured()? "Off" : "On"));
        tableRow.repaint(); // Register table row for repaint
        tableRow.setStructured(!tableRow.isStructured()); // Toggle structured setting
        _resizeBarIndex = -1; // Reset resize bar index
    }

    // Set last mouse point to down point
    _lastMousePoint = point;
    
    // Set undo title
    aTable.undoerSetUndoTitle("Resize Table Row");
}

/**
 * Returns the x location of the given resize bar popup.
 */
public double getResizeBarPopupX(RMTableRow aTableRow)
{
    String version = aTableRow.getVersion(); // Get table row version string 
    double versionWidth = RMFont.Helvetica12.stringAdvance(version); // Get width of version
    return aTableRow.getWidth() - versionWidth - 13; // Get start of version
}

/**
 * Event handling for table editing.
 */
public void mouseDragged(T aTable, MouseEvent anEvent)
{
    // If no resize bar selected, just return
    if(_resizeBarIndex<0) return;
    
    // Get event point in table coords
    RMPoint downPoint = getEditor().convertPointToShape(anEvent.getPoint(), aTable);
    
    // Get change in Height and child for current _resizeBarIndex
    double dh = downPoint.y - _lastMousePoint.y;
    
    // Get table row for resize bar
    RMShape tableRow = aTable.getChild(_resizeBarIndex);

    // Make sure dh doesn't cause row to be smaller than zero or cause last row to go below bottom of table
    dh = RMMath.clamp(dh, -Math.abs(tableRow.height()), Math.abs(aTable.height()) -
        aTable.getResizeBarBounds(aTable.getChildCount()-1).getMaxY());
    
    // Update last mouse point, rese table row height and repaint table
    _lastMousePoint.y += dh;
    tableRow.setHeight(tableRow.height() + dh);
    aTable.repaint();
}

/**
 * Event handling for table editing.
 */
public void mouseReleased(T aTable, MouseEvent anEvent)
{
    // If no resize bar selected, just return
    if(_resizeBarIndex<0) return;
    
    // Super select the child above the selected resize bar
    getEditor().setSuperSelectedShape(aTable.getChild(_resizeBarIndex));
}

/**
 * Opens a popup menu specific for table row divider under mouse.
 */
public void runMenuForShape(RMShape aShape, int x, int y)
{
    RMEditor editor = RMEditor.getMainEditor(); // Get editor
    RMTableRow tableRow = (RMTableRow)editor.getSuperSelectedShape(); // Get table row
    RMTableRowTool tableRowTool = (RMTableRowTool)RMTool.getTool(tableRow); // Get table row tool
    JPopupMenu popupMenu = tableRowTool.getPopupMenu(tableRow); // Fill menu
    RMPoint point = editor.convertPointFromShape(new RMPoint(x, y), aShape); // Get point in editor
    popupMenu.show(editor, (int)point.x, (int)point.y); // Show popup menu
}

/**
 * Overrides shape implementation to declare no handles when the child of a table group.
 */
public int getHandleCount(T aShape)
{
    return aShape.getParent() instanceof RMTableGroup? 0 : super.getHandleCount(aShape);
}

/**
 * A renderer to install a tooltip.
 */
static class ToolTipRenderer extends DefaultTableCellRenderer {

    /** Override to install tooltip. */
    public Component getTableCellRendererComponent(JTable aTb, Object aVl, boolean isSl, boolean isFc,int aRw,int aCl)
    {
        JComponent c = (JComponent)super.getTableCellRendererComponent(aTb, aVl, isSl, isFc, aRw, aCl);
        if(aCl==0 && aTb.getValueAt(aRw, aCl)!=null) c.setToolTipText(aTb.getValueAt(aRw, aCl).toString());
        return c;
    }
}

}