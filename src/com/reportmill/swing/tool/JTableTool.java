package com.reportmill.swing.tool;
import com.reportmill.swing.shape.*;

/**
 * An RBTool implementation for JTableShape management.
 */
public class JTableTool <T extends JTableShape> extends JComponentTool <T> {

/**
 * Updates the Swing UI panel from the current selection.
 */
/*public void resetUI()
{
    // Get currently selected table shape and table (and return if null)
    JTableShape tshape = getSelectedShape(); if(tshape==null) return;
    
    // Update AutoResizeComboBox
    setNodeSelectedIndex("AutoResizeComboBox", getIndexFromAutoResize(tshape.getAutoResizeMode()));
    
    // Update RowSelectionCheckBox, ColumnSelectionCheckBox
    setNodeValue("RowSelectionCheckBox", tshape.getRowSelectionAllowed());
    setNodeValue("ColumnSelectionCheckBox", tshape.getColumnSelectionAllowed());
    
    // Update RowHeightText
    setNodeValue("RowHeightText", tshape.getRowHeight());
    
    // Update SpacingXText, SpacingYText
    Dimension intercellSpacing = tshape.getIntercellSpacing();
    setNodeValue("SpacingXText", intercellSpacing.width);
    setNodeValue("SpacingYText", intercellSpacing.height);
    
    // Update GridColorWell
    setNodeValue("GridColorWell", tshape.getGridColor());
    
    // Update GridHorizontalCheckBox, GridVerticalCheckBox
    setNodeValue("GridHorizontalCheckBox", tshape.getShowHorizontalLines());
    setNodeValue("GridVerticalCheckBox", tshape.getShowVerticalLines());
    
    // Update ColumnsText
    TableModel model = tshape.getModel();
    setNodeValue("ColumnsText", model.getColumnCount());
}*/

/**
 * Updates the current selection from the Swing UI controls.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get currently selected table shape and table (and return if null)
    JTableShape tshape = getSelectedShape(); if(tshape==null) return;
    
    // Handle AutoResizeComboBox
    if(anEvent.equals("AutoResizeComboBox"))
        tshape.setAutoResizeMode(getAutoResizeFromIndex(anEvent.getSelectedIndex()));

    // Handle RowSelectionCheckBox, ColumnsSelectionCheckBox
    if(anEvent.equals("RowSelectionCheckBox")) tshape.setRowSelectionAllowed(anEvent.getBoolValue());
    if(anEvent.equals("ColumnSelectionCheckBox")) tshape.setColumnSelectionAllowed(anEvent.getBoolValue());
    
    // Handle RowHeightText
    if(anEvent.equals("RowHeightText")) tshape.setRowHeight(anEvent.getIntValue());
    
    // Handle SpacingXText or SpacingYText
    if(anEvent.equals("SpacingXText") || anEvent.equals("SpacingYText")) {
        Dimension d = new Dimension(getNodeIntValue("SpacingXText"), getNodeIntValue("SpacingYText"));
        tshape.setIntercellSpacing(d);
    }
    
    // Handle GridColorWell
    if(anEvent.equals("GridColorWell")) tshape.setGridColor(anEvent.getColorValue());
    
    // Handle GridHorizontalCheckBox, GridVerticalCheckBox
    if(anEvent.equals("GridHorizontalCheckBox")) tshape.setShowHorizontalLines(anEvent.getBoolValue());
    if(anEvent.equals("GridVerticalCheckBox")) tshape.setShowVerticalLines(anEvent.getBoolValue());
    
    // Handle ColumnsText
    //if(anEvent.equals("ColumnsText")) { JTableHpr.SampleModel model = (JTableHpr.SampleModel)table.getModel();
    //    model.setColumnCount(anEvent.getIntValue()); model.fireTableStructureChanged(); }
}*/

/**
 * Returns an index for an AUTO_RESIZE enumeration.
 */
/*private int getIndexFromAutoResize(int anAutoResizeValue)
{
    if(anAutoResizeValue==JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS) return 0;
    if(anAutoResizeValue==JTable.AUTO_RESIZE_NEXT_COLUMN) return 1;
    if(anAutoResizeValue==JTable.AUTO_RESIZE_ALL_COLUMNS) return 2;
    if(anAutoResizeValue==JTable.AUTO_RESIZE_LAST_COLUMN) return 3;
    if(anAutoResizeValue==JTable.AUTO_RESIZE_OFF) return 4;
    return -1;
}*/

/**
 * Returns an AUTO_RESIZE enum for a given index.
 */
/*private int getAutoResizeFromIndex(int anIndex)
{
    for(int i=0; i<99; i++) if(getIndexFromAutoResize(i)==anIndex) return i;
    return 0;
}*/

/**
 * Override normal implementation to handle KeysPanel drop.
 */
/*public void drop(T aTableShape, DropTargetDropEvent anEvent)
{
    // If not a keys panel drop, do normal implementation
    if(com.reportmill.panels.KeysPanel.getDragKey()==null || com.reportmill.panels.KeysPanel.isSelectedToMany()) {
        super.drop(aTableShape, anEvent); return; }
    
    // Register for repaint
    aTableShape.repaint();
    
    // If table model is JTableHpr$SampleModel, replace it
    if(table.getAutoCreateColumnsFromModel()) {
        //table.setModel(new DefaultTableModel());
        table.setAutoCreateColumnsFromModel(false);
        table.setColumnModel(new DefaultTableColumnModel());
    }
    
    // Create new table column
    TableColumn column = new TableColumn(aTableShape.getColumnCount());
    
    // Delete @-signs from string
    String string = RMStringUtils.delete(RMClipboardUtils.getString(anEvent.getTransferable()), "@");
    
    // Get header by removing "get" from key
    String header = RMStringUtils.delete(string, "get");
    
    // Set table column header
    column.setHeaderValue(header);
    
    // Set bind key as table column identifier
    column.setIdentifier(string);
    
    // Add column to table
    aTableShape.addColumn(column);
    
    // For table layout
    aTableShape.doLayout();
    
    // Rebuild table column shapes
    aTableShape.rebuildTableColumnShapes();
}*/

/**
 * Returns the string to be used in the inspector window title.
 */
public String getWindowTitle()  { return "JTable Inspector"; }

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTableShape.class; }

}