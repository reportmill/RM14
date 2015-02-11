package com.reportmill.apptools;
import com.reportmill.shape.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import snap.swing.*;

/**
 * This class provides UI editing for RMPage shapes.
 */
public class RMPageTool <T extends RMPage> extends RMParentShapeTool <T> {

    // The layers table
    JTable               _layersTable;
    
    // Icons
    Icon                 _visibleIcon = Swing.createIcon("LayerVisible.png", getClass());
    Icon                 _invisibleIcon = Swing.createIcon("LayerInvisible.png", getClass());
    Icon                 _lockedIcon = Swing.createIcon("LayerLocked.png", getClass());

/**
 * Initialize UI panel for this tool.
 */
protected void initUI()
{
    // Add DropTargetListener to layersTable
    _layersTable = getNode("LayersTable", JTable.class);
    _layersTable.setModel(new LayerTableModel());
    _layersTable.getColumnModel().getColumn(1).setMaxWidth(20);
    _layersTable.setTableHeader(null);
    enableEvents(_layersTable, MouseClicked);
}

/**
 * Updates the UI controls from currently selected page.
 */
public void resetUI()
{
    // Get currently selected page (just return if null)
    RMPage page = getSelectedShape(); if(page==null) return;
        
    // Update AddButton enabled state
    setNodeEnabled("AddButton", page.getLayerCount()>0);
    
    // Update RemoveButton enabled state
    setNodeEnabled("RemoveButton", page.getLayerCount()>1);
    
    // Update RenameButton enabled state
    setNodeEnabled("RenameButton", page.getLayerCount()>0);
    
    // Update MergeButton enabled state
    setNodeEnabled("MergeButton", page.getLayerCount()>1 && page.getSelectedLayerIndex()>0);
    
    // Update layers table selection
    int index = page.getSelectedLayerIndex();
    _layersTable.tableChanged(new TableModelEvent(_layersTable.getModel()));
    if(index>=0 && index<page.getLayerCount()) _layersTable.setRowSelectionInterval(index, index);
}

/**
 * Updates currently selected page from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected page (just return if null)
    RMPage page = getSelectedShape(); if(page==null) return;

    // Handle DatasetKeyText
    if(anEvent.equals("DatasetKeyText") && anEvent.isDragDropEvent())
        page.setDatasetKey(anEvent.getStringValue().replace("@", ""));
    
    // Handle AddButton
    if(anEvent.equals("AddButton"))
        page.addLayerNamed("Layer " + (page.getLayerCount() + 1));

    // Handle RemoveButton
    if(anEvent.equals("RemoveButton")) {
        int[] selectedRows = _layersTable.getSelectedRows();
        for(int i=0; i<selectedRows.length; i++) {
            String name = (String)_layersTable.getValueAt(selectedRows[i], 0);
            RMPageLayer layer = page.getLayer(name);
            page.removeLayer(layer);
        }
        _layersTable.getSelectionModel().clearSelection();
    }

    // Handle MergeButton
    if(anEvent.equals("MergeButton")) {
        
        // Get selected layer
        RMPageLayer layer = page.getSelectedLayer();
        
        // Get selected layer index
        int index = page.getSelectedLayerIndex();
        
        // If index is less than layer count
        if(index<page.getLayerCount()) {
            RMPageLayer resultingLayer = page.getLayer(index - 1);
            resultingLayer.addChildren(layer.getChildren());
            layer.removeChildren();
            page.removeLayer(layer);
        }
    }
    
    // Handle RenameButton
    if(anEvent.equals("RenameButton")) {
        int selectedRow = _layersTable.getSelectedRow();
        RMPageLayer layer = page.getLayer(selectedRow);
        DialogBox dbox = new DialogBox("Rename Layer"); dbox.setQuestionMessage("Layer Name:");
        String newName = dbox.showInputDialog(getUI(), layer.getName());
        if(newName!=null && newName.length()>0)
            layer.setName(newName);
    }
    
    // Handle LayersTable
    if(anEvent.equals("LayersTable")) {
        
        // Handle DropString event
        if(anEvent.getDropString()!=null) {
            String string = anEvent.getDropString();
            Point point = anEvent.getLocation();
            int toRow = _layersTable.rowAtPoint(point), fromRow = -1;
            for(int i=0, iMax=_layersTable.getRowCount(); i<iMax; i++)
                if(string.equals(_layersTable.getValueAt(i, 0)))
                    fromRow = i;
            if(fromRow>=0)
                moveLayer(fromRow, toRow);
        }
        
        // Handle MouseClicked event - have page select new table row
        else {
            int row = _layersTable.getSelectedRow(), col = _layersTable.getSelectedColumn(); if(row<0) return;
            RMPageLayer layer = page.getLayer(row);
            page.selectLayer(layer);
            
            // If column one was selected, cycle through layer states
            if(anEvent.isMouseClicked() && col==1) {
                Object obj = _layersTable.getValueAt(row, col);
                if(obj==_visibleIcon)
                    layer.setLayerState(RMPageLayer.StateInvisible);
                else if(obj==_invisibleIcon)
                    layer.setLayerState(RMPageLayer.StateLocked);
                else layer.setLayerState(RMPageLayer.StateVisible);
            }
        }
    }

    // Handle AllVisibleButton
    if(anEvent.equals("AllVisibleButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(RMPageLayer.StateVisible);
    
    // Handle AllVisibleButton
    if(anEvent.equals("AllInvisibleButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(RMPageLayer.StateInvisible);
    
    // Handle AllLockedButton
    if(anEvent.equals("AllLockedButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(RMPageLayer.StateLocked);
}

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMPage.class; }

/**
 * Returns the name to be used for this tool in the inspector window title.
 */
public String getWindowTitle()  { return "Page Inspector"; }

/**
 * Overrides tool method to declare that pages have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

/**
 * Moves the layer at the first layer index to the second given index.
 */
public void moveLayer(int from, int to)
{
    RMPage page = getSelectedShape(); page.repaint();
    page.moveLayer(from, to);
}

/**
 * An inner class for controlling the layers table.
 */
private class LayerTableModel extends AbstractTableModel {

    // Row count
    public int getRowCount()  { RMPage p = getSelectedShape(); return p!=null? p.getLayerCount() : 0; }
    
    // Column count
    public int getColumnCount()  { return 2; }
    
    // Column classes
    public Class getColumnClass(int c)  { return c==0? String.class : ImageIcon.class; }
    
    // Column names
    public String getColumnName(int c)  { return c==0? "Layer" : "Visible"; }
    
    // Editable
    public boolean isCellEditable(int r, int c)  { return false; }
    
    // Table values
    public Object getValueAt(int r, int c)
    { 
        // Get selected page and layer for row
        RMPage page = getSelectedShape();
        RMPageLayer layer = page.getLayer(r);
        
        // Handle column 0
        if(c==0) return layer.getName();
        
        // Handle column 1
        int state = layer==null? RMPageLayer.StateVisible : layer.getLayerState();
        if(state==RMPageLayer.StateVisible) return _visibleIcon;
        if(state==RMPageLayer.StateInvisible) return _invisibleIcon;
        return _lockedIcon;
    }
}

}