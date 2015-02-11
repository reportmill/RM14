package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;
import snap.swing.SwingEvent;
import snap.web.Entity;

/**
 * This class handles UI editing of table groups.
 */
public class RMTableGroupTool <T extends RMTableGroup> extends RMParentShapeTool <T> {

    // The tables tree
    TGTree         _tablesTree;

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get TableTree
    _tablesTree = getNode("TableTree", TGTree.class);
    _tablesTree._tableGroupTool = this;
}

/**
 * Updates UI panel.
 */
public void resetUI()
{
    // Get currently selected table group (just return if null)
    RMTableGroup tableGroup = getSelectedShape(); if(tableGroup==null) return;
    
    // Create root node for table group and add child tables to it
    TableNode rootNode = new TableNode("");
    addChildNodesForTables(rootNode, tableGroup.getChildTables());    

    // Reset the table group tree
    DefaultTreeModel tm = (DefaultTreeModel)_tablesTree.getModel();
    tm.setRoot(rootNode);
    expandTree((TableNode)_tablesTree.getModel().getRoot());
}

/**
 * Respond to UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected table group and main table (just return if null)
    RMTableGroup tableGroup = getSelectedShape(); if(tableGroup==null) return;
    RMTable mainTable = tableGroup.getMainTable();
    
    // Handle DatasetKeyText
    if(anEvent.equals("DatasetKeyText") && anEvent.isDragDropEvent())
        mainTable.setDatasetKey(anEvent.getStringValue().replace("@", ""));
    
    // Handle MainTableNameText
    if(anEvent.equals("MainTableNameText") && anEvent.isDragDropEvent())
        mainTable.setName(anEvent.getStringValue().replace("@", ""));

    // Handle AddPeerMenuItem
    if(anEvent.equals("AddPeerMenuItem")) {

        // If asked to add peer to table that is already a level or more down, call addChildTableToTable on it's parent
        if(tableGroup.getParentTable(mainTable) != null) {
            addChildTable(tableGroup.getParentTable(mainTable), mainTable, null); return; }
        
        addPeerTable(mainTable);
    }
    
    // Handle AddChildMenuItem
    if(anEvent.equals("AddChildMenuItem"))
    	addChildTable(mainTable, null, null);
    
    // Handle RemoveTableMenuItem (short-circuit and beep if main table is only top level table)
    if(anEvent.equals("RemoveTableMenuItem")) {
        if(tableGroup.getParentTable(mainTable)==null && tableGroup.getPeerTables(mainTable).size()==1)
            Toolkit.getDefaultToolkit().beep();
        else tableGroup.removeTable(mainTable);
    }
    
    // Handle PasteTableMenuItem
    if(anEvent.equals("PasteTableMenuItem")) {
        Object pasteShape = RMEditorClipboard.getShapeFromClipboard(RMEditor.getMainEditor());
        if(pasteShape instanceof RMTable) {
            tableGroup.undoerSetUndoTitle("Paste Table");
            tableGroup.addPeerTable((RMTable)pasteShape);
        }
    }
    
    // Handle MoveUpMenuItem
    if(anEvent.equals("MoveUpMenuItem"))
        tableGroup.moveTable(mainTable, -1);
    
    // Handle MoveDownMenuItem
    if(anEvent.equals("MoveDownMenuItem"))
        tableGroup.moveTable(mainTable, 1);
        
    // Handle MoveInMenuItem
    if(anEvent.equals("MoveInMenuItem")) {
        RMTable tableBefore = tableGroup.getPeerTablePrevious(mainTable); if(tableBefore==null) return;
        tableGroup.makeTableChildOfTable(mainTable, tableBefore);
    }
    
    // Handle MoveOutMenuItem
    if(anEvent.equals("MoveOutMenuItem")) {
        RMTable parentTable = tableGroup.getParentTable(mainTable);
        RMTable parentTableParentTable = tableGroup.getParentTable(parentTable);
        tableGroup.makeTableChildOfTable(mainTable, parentTableParentTable);
    }
    
    // Handle KeysButton
    if(anEvent.equals("KeysButton"))
        getEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);
    
    // Handle TableTree
    if(anEvent.equals("TableTree")) {
        
        // Get selected table node
        TableNode o = (TableNode)_tablesTree.getLastSelectedPathComponent();
        
        // If non-null, set table for table node
        if(o!=null) {
            RMTable t = (RMTable)o.getUserObject();
            tableGroup.setMainTable(t);
        }
    }
}

/**
 * Super selects the current table in the table tree.
 */
public void superSelectTable()
{
    TableNode o = (TableNode)_tablesTree.getLastSelectedPathComponent(); if(o==null) return;
    RMTable t = (RMTable)o.getUserObject();
    RMEditor.getMainEditor().setSuperSelectedShape(t);
}

/**
 * Adds a new child table to given table after the other given child table with given dataset key.
 */
private void addChildTable(RMTable toTable, RMTable afterTable, String aKey)
{
    RMTableGroup tgroup = getSelectedShape();

    // Get new table, set default size and add grouping key for root entity (with structured tablerow)
    RMTable table = new RMTable(aKey==null? "Objects" : aKey);
    table.setSize(tgroup.getWidth(), tgroup.getHeight());
    
    // Add table (recording it for Undo)
    tgroup.undoerSetUndoTitle("Add Table to Table Group");
    tgroup.addChildTable(table, toTable, afterTable);
}

/**
 * Adds a new peer table after given table.
 */
private void addPeerTable(RMTable afterThisTable)
{
    RMTableGroup tgroup = getSelectedShape();

    // Get table and set defalt size and add grouping key for root entity (with structured tablerow)
    RMTable table = new RMTable("Objects");
    table.setSize(tgroup.getWidth(), tgroup.getHeight());

    // Add table (recording it for Undo)
    tgroup.undoerSetUndoTitle("Add Table to Table Group");
    tgroup.addPeerTable(table, afterThisTable);
}

private void addChildNodesForTables(TableNode aNode, List <RMTable> theTables)
{
    // Get table group (just return if table group or tables are null)
    RMTableGroup tgroup = getSelectedShape(); if(tgroup==null || theTables==null) return;
    
    // Iterate over tables, adding node for each one (run recursively, too)
    for(RMTable table : theTables) {
        TableNode childNode = new TableNode(table);
        aNode.add(childNode);
        addChildNodesForTables(childNode, tgroup.getChildTables(table));
    }
}

private void expandTree(TableNode aNode)
{
    // expand the path
    TreePath tp = new TreePath(((DefaultMutableTreeNode)aNode).getPath());
    _tablesTree.expandPath(tp);
    
    // if the node represents the selected table, make it the selected node
    if(aNode.getUserObject()==getMainTable())
        _tablesTree.setSelectionPath(tp);
    
    // now recurse to expand all children
    for(int i=0, iMax = aNode.getChildCount(); i<iMax; i++)
        expandTree((TableNode)aNode.getChildAt(i));
}

/**
 * Returns the shape class for this tool (table group).
 */
public Class getShapeClass()  { return RMTableGroup.class; }

/**
 * Returns the display name for this inspector.
 */
public String getWindowTitle()  { return "Table Group Inspector"; }

/**
 * Overridden to make graph super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overridden to make graph not ungroupable.
 */
public boolean isUngroupable(RMShape aShape)  { return false; }

/**
 * Returns the given shape's dataset entity.
 */
public Entity getDatasetEntity(RMShape aShape)
{
    // Get the table group
    RMTableGroup tableGroup = (RMTableGroup)aShape;
    
    // Get the table group's main table
    RMTable table = tableGroup.getMainTable();
    if(table==null)
        return tableGroup.getDatasetEntity();
    
    // Return the main table's dataset entity
    return table.getDatasetEntity();
}

/**
 * Returns the main table for the current table group.
 */
public RMTable getMainTable()
{
    RMTableGroup tgroup = getSelectedShape();
    return tgroup!=null? tgroup.getMainTable() : null;
}

/**
 * MousePressed.
 */
public void mousePressed(T aTableGroup, MouseEvent anEvent)
{
    // If selected, forward on to main table, to potentially super select structured table row
    if(getEditor().getSelectedOrSuperSelectedShape()==aTableGroup) {
        
        // Get main table
        RMTable mainTable = aTableGroup.getMainTable();
        
        // Forward on
        RMTool.getTool(mainTable).mousePressed(mainTable, anEvent);
        
        // If event was consumed, just return
        if(anEvent.isConsumed())
            return;
    }
}

// An Inner class for tree nodes for table tree
private class TableNode extends DefaultMutableTreeNode {
    public TableNode(Object o) { super(o); }
    public String toString() {
        Object o = getUserObject(); if(!(o instanceof RMTable)) return o.toString();
        RMTable t = (RMTable)o;
        return RMStringUtils.length(t.getName())>0? t.getName() :
             RMStringUtils.length(t.getDatasetKey())>0? t.getDatasetKey() : "Table";
    }
}

// A JTree subclass to accept Drag & Drop keys
public static class TGTree extends JTree implements DropTargetListener {

    // The table group tool that owns this tree
    RMTableGroupTool    _tableGroupTool;
    
    /** Creates a new TGTree. */
    public TGTree()
    {
        // Create new drop target
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        
        // Set root visible
        setRootVisible(false);
        
        // Add mouse listener to fire when double clicked
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getClickCount()==2)
                    _tableGroupTool.superSelectTable(); } });
                    
        // Renderer
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        renderer.setLeafIcon(null);
        renderer.setFont(RMAWTUtils.Helvetica11);
        setCellRenderer(renderer);
    }
    
    /** Called when drag enters tree bounds. */
    public void dragEnter(DropTargetDragEvent dtde) { dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE); }
    
    /** Called when drag is dropped inside tree bounds. */
    public void drop(DropTargetDropEvent dtde)
    {
        // Catch exceptions
        try {
            
        // If drag is StringFlavor, accept text
        if(dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            
            // Accept the drop
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            // Get the transferable and the drop string
            Transferable tr = dtde.getTransferable();
            String dropString = (String)tr.getTransferData(DataFlavor.stringFlavor);
            
            // If drop string not null append it as the text
            if(dropString != null) {
                
                // Bogus - delete @ signs
                dropString = RMStringUtils.delete(dropString, "@");
                
                // Accept string
                _tableGroupTool.addChildTable(_tableGroupTool.getMainTable(), null, dropString);
            }

            // Register drop complete
            dtde.dropComplete(true);
        }
        
        // If not StringFlavor, reject drop
        else dtde.rejectDrop();
            
        // Catch exceptions
        } catch (Exception e) { e.printStackTrace(); dtde.rejectDrop(); }
    }
    
    /** Called when drag exits tree bounds. */
    public void dragExit(DropTargetEvent dte) { }
    
    /** Called when drag is moved inside tree bounds. */
    public void dragOver(DropTargetDragEvent dtde) { }
    
    /** Called when drop action changed. */
    public void dropActionChanged(DropTargetDragEvent dtde) { }
}

}