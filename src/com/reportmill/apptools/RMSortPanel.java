package com.reportmill.apptools;
import com.reportmill.app.AttributesPanel;
import com.reportmill.app.RMEditorPane;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import snap.swing.*;

/**
 * Provides UI for configuring a grouping for a tool.
 */
public class RMSortPanel extends SwingOwner {

    // The owner of this sort panel
    Owner         _owner;

    // The switch pane
    SwitchPane    _switchPane;
    
    // The sorts table
    JTable        _sortsTable;
    
    // Images used for panel
    static Icon SortAscIcon     = Swing.getIcon("SortAscending.png", RMSortPanel.class);
    static Icon SortDescIcon    = Swing.getIcon("SortDescending.png", RMSortPanel.class);
    
/**
 * Creates a new sort panel instance.
 */
public RMSortPanel(Owner anOwner)  { _owner = anOwner; }

/**
 * An interface for SortPanelOwner
 */
public interface Owner {

    // SortPanel calls this as first line of SortPanel respondUI
    public void respondUI(SwingEvent anEvent);
    
    // Returns the selected shape that is being edited
    public RMShape getSelectedShape();
    
    // Returns the grouping that we modify
    public RMGrouping getGrouping();
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Cache the switch pane
    _switchPane = getNode("SortPanel", SwitchPane.class);
        
    // Get sorts table and configure
    _sortsTable = getNode("SortsTable", JTable.class);
    _sortsTable.setRowHeight(20);
    _sortsTable.setTableHeader(null);
    _sortsTable.setFillsViewportHeight(true);
    _sortsTable.setDefaultRenderer(Object.class, new ToolTipRenderer());
    _sortsTable.setModel(new SortingTableModel());
    _sortsTable.getColumnModel().getColumn(1).setMaxWidth(20);
    enableEvents(_sortsTable, MouseReleased); // So we get called for click on sort order
    enableEvents(_sortsTable, DragDrop);
    
    // Add SortingTable PopupMenu Listener
    _sortsTable.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            if(e.isPopupTrigger())
                getSortsTablePopupMenu().show(e.getComponent(), e.getX(), e.getY()); }});
    
    // Configure TopNSortButton
    getNode("TopNSortButton", JToggleButton.class).setBorder(null);
}

/**
 * Resets the UI controls.
 */
public void resetUI()
{
    // Get grouping
    RMGrouping grouping = _owner.getGrouping();
    
    // If grouping is null, disable everything and return
    if(grouping==null) {
        getUI().setEnabled(false); return; }
    else getUI().setEnabled(true);
    
    // Update SortButton, TopNButton, ValuesButton
    setNodeValue("SortButton", _switchPane.getSelectedIndex()==0);
    setNodeValue("TopNButton", _switchPane.getSelectedIndex()==1);
    setNodeValue("ValuesButton", _switchPane.getSelectedIndex()==2);
    
    // Update SortingTable
    setNodeSelectedIndex(_sortsTable, grouping.getSelectedSortIndex());
    _sortsTable.repaint();
    
    // Update TopNKeyText, TopNCountText, TopNSortButton, TopNInclCheckBox, TopNPadCheckBox
    setNodeValue("TopNKeyText", grouping.getTopNSort().getKey());
    setNodeValue("TopNCountText", grouping.getTopNSort().getCount());
    setNodeValue("TopNSortButton", grouping.getTopNSort().getOrder()==RMSort.ORDER_DESCEND);
    setNodeValue("TopNInclCheckBox", grouping.getTopNSort().getIncludeOthers());
    setNodeValue("TopNPadCheckBox", grouping.getTopNSort().getPad());
    
    // Update ValuesText, SortOnValuesCheckBox, IncludeValuesCheckBox
    setNodeValue("ValuesText", grouping.getValuesString());
    setNodeValue("SortOnValuesCheckBox", grouping.getSortOnValues());
    setNodeValue("IncludeValuesCheckBox", grouping.getIncludeValues());
}

/**
 * Responds to changes to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Forward on to tool respondUI
    _owner.respondUI(anEvent);
    
    // Get the selected shape
    RMShape shape = _owner.getSelectedShape();
    
    // Get current grouping
    RMGrouping grouping = _owner.getGrouping(); if(grouping==null) return;
    
    // Handle SortButton, TopNButton, ValuesButton
    if(anEvent.equals("SortButton")) setNodeSelectedIndex("SortPanel", 0);
    if(anEvent.equals("TopNButton")) setNodeSelectedIndex("SortPanel", 1);
    if(anEvent.equals("ValuesButton")) setNodeSelectedIndex("SortPanel", 2);
    
    // Handle SortingTable
    if(anEvent.equals("SortsTable")) {
        
        // Handle drop
        if(anEvent.isDragDropEvent()) {
            
            // Get drop string
            String string = anEvent.getDropString();
            string = RMStringUtils.delete(string, "@");
            
            // Get drop point
            Point point = anEvent.getLocation();
            
            // Get toRow from down point
            int toRow = _sortsTable.rowAtPoint(point);
            
            // Declare fromRow
            int fromRow = -1;
            
            // Iterate over table rows to find from row
            for(int i=0, iMax=_sortsTable.getRowCount(); i<iMax; i++)
                if(string.equals(_sortsTable.getValueAt(i, 0)))
                    fromRow = i;
            
            // If drag is from sort table, just move sort
            if(fromRow>=0) {
                shape.undoerSetUndoTitle("Rearrange Sort Orderings");
                grouping.moveSort(fromRow, toRow);
            }
                
            // If drag is from outside TableTool, add sort
            else {
                shape.undoerSetUndoTitle("Add Sort Order");
                grouping.addSort(new RMSort(string));
            }
        }
        
        // Handle selection
        else {
        
            // Get row and column of SortingTable selection and set grouping SelectedSortIndex
            int row = _sortsTable.getSelectedRow();
            int col = _sortsTable.getSelectedColumn();
            grouping.setSelectedSortIndex(row);
        
            // If selected sort order column, flip selected sort
            if(anEvent.isMouseReleased() && col==1) {
                shape.undoerSetUndoTitle("Flip Sort Ordering");
                grouping.getSelectedSort().toggleOrder();
            }
        }
    }
    
    // Handle AddSortMenuItem
    if(anEvent.equals("AddSortMenuItem")) {
        
        // Get key from input dialog
        DialogBox dbox = new DialogBox("Add Sorting Key"); dbox.setQuestionMessage("Sorting Key:");
        String key = dbox.showInputDialog(getUI(), null);
        
        // If key was entered, add it to grouping
        if(key!=null && key.length()>0) {
            shape.undoerSetUndoTitle("Add Sort Order");
            grouping.addSort(new RMSort(key));
        }
    }
    
    // Handle RemoveSortMenuItem
    if(anEvent.equals("RemoveSortMenuItem") && grouping.getSelectedSort()!=null) {
        shape.undoerSetUndoTitle("Remove Sort Order");
        grouping.removeSort(grouping.getSelectedSort());
    }
    
    // Handle KeysMenuItem
    if(anEvent.equals("KeysMenuItem"))
        RMEditorPane.getMainEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);
    
    // Handle MoveSortUpMenuItem
    if(anEvent.equals("MoveSortUpMenuItem")) {
        int loc = _sortsTable.getSelectedRow();
        if(loc>0)
            grouping.moveSort(loc, loc - 1);        
    }
    
    // Handle MoveSortDownMenuItem
    if(anEvent.equals("MoveSortDownMenuItem")) {
        int loc = _sortsTable.getSelectedRow();
        if(loc<_sortsTable.getRowCount() - 1)
            grouping.moveSort(loc, loc + 1);        
    }
    
    // Handle TopNKeyText
    if(anEvent.equals("TopNKeyText")) {
        shape.undoerSetUndoTitle("TopN Sort Change");
        grouping.getTopNSort().setKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
        if(grouping.getTopNSort().getCount()==0)
            grouping.getTopNSort().setCount(5);
    }
    
    // Handle TopNCountText
    if(anEvent.equals("TopNCountText")) {
        shape.undoerSetUndoTitle("TopN Count Change");
        grouping.getTopNSort().setCount(anEvent.getIntValue());
    }
    
    // Handle TopNSortButton, TopNInclCheckBox, TopNPadCheckBox
    if(anEvent.equals("TopNSortButton")) grouping.getTopNSort().toggleOrder();
    if(anEvent.equals("TopNInclCheckBox")) grouping.getTopNSort().setIncludeOthers(anEvent.getBoolValue());
    if(anEvent.equals("TopNPadCheckBox")) grouping.getTopNSort().setPad(anEvent.getBoolValue());
    
    // Handle ValuesText, SortOnValuesCheckBox, IncludeValuesCheckBox
    if(anEvent.equals("ValuesText")) grouping.setValuesString(anEvent.getStringValue());
    if(anEvent.equals("SortOnValuesCheckBox")) grouping.setSortOnValues(anEvent.getBoolValue());
    if(anEvent.equals("IncludeValuesCheckBox")) grouping.setIncludeValues(anEvent.getBoolValue());
}

/**
 * Returns a popup menu for the sorts table.
 */
public JPopupMenu getSortsTablePopupMenu()
{
    JPopupMenu pmenu = new JPopupMenu();
    JMenuItem mitem = new JMenuItem("Move Up"); mitem.setName("MoveSortUpMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Move Down"); mitem.setName("MoveSortDownMenuItem"); pmenu.add(mitem);
    initUI(pmenu);
    return pmenu;
}

/**
 * Sets the selected pane.
 */
public void setSelectedPane(int anIndex)  { setNodeValue("SortPanel", anIndex); }

/**
 * Support for managing Sorts JTable.
 */
private class SortingTableModel extends AbstractTableModel {
    
    /** Returns number of sorts for selected grouping. */
    public int getRowCount()  { RMGrouping g = _owner.getGrouping(); return g==null? 0 : g.getSortCount(); }
    
    /** Returns 2 columns. */
    public int getColumnCount()  { return 2; }
    
    /** Returnsw column classes. */
    public Class getColumnClass(int c)  { return c==0? String.class : Icon.class; }
    
    /** Returns column names. */
    public String getColumnName(int c)  { return c==0? "Sort" : "Order"; }
    
    /** Makes columns not editable. */
    public boolean isCellEditable(int r, int c)  { return false; }
    
    /** Returns value for given row & column. */
    public Object getValueAt(int r, int c)
    { 
        RMSort sort = _owner.getGrouping().getSort(r);
        if(c==0) return sort.getKey();
        return sort.getOrder()==RMSort.ORDER_ASCEND? SortAscIcon : SortDescIcon;
    }
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