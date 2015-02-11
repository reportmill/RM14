package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import snap.swing.*;
import snap.util.*;

/**
 * An inspector for general shape attributes, like property keys, name, text wrap around, etc.
 */
public class ShapeGeneral extends RMTool {
    
    // The bindings table
    JTable                 _bindingsTable;
    
    // The bindings table model
    BindingsTableModel     _bindingsTableModel;
    
/**
 * Initialize UI panel for this inspector.
 */
protected void initUI()
{
    // Get bindings table
    _bindingsTable = getNode("BindingsTable", JTable.class);

    // Install table model into bindings table
    _bindingsTable.setModel(_bindingsTableModel = new BindingsTableModel());
    _bindingsTable.getColumnModel().getColumn(0).setMaxWidth(80);
    
    // Add support for dropping keys panel keys
    _bindingsTable.setDragEnabled(true);
    _bindingsTable.setTransferHandler(new TransferHandler() {
        
        // Enable import if keys panel is dragging
        public boolean canImport(JComponent aComponent, DataFlavor[] transferFlavors) {
            return KeysPanel.getDragKey()!=null; }
        
        // Add binding to selected shape
        public boolean importData(JComponent aComponent, Transferable aTransferable) {
            
            // Get drop row (just return if out of range)
            Point point = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(point, _bindingsTable);
            int row = _bindingsTable.rowAtPoint(point);
            if(row<0)
                return false;
            
            // Get property name, create and add binding
            String pname = (String)_bindingsTable.getValueAt(row, 0);
            String bkey = KeysPanel.getDragKey();
            getEditor().getSelectedShape().addBinding(pname, bkey);
            
            // Reset UI and return true
            ShapeGeneral.this.resetLater();
            return true;
        }
    });

    // Install a custom editor in the value column
    _bindingsTable.getColumnModel().getColumn(1).setCellEditor(new ValueEditor());
}

/**
 * Updates Swing UI controsl from current selection.
 */
public void resetUI()
{
    // Do normal version
    super.resetUI();
    
    // Get currently selected shape
    RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();

    // Reset table model shape
    if(shape!=_bindingsTableModel._shape) {
        
        // Set new shape
        _bindingsTableModel._shape = shape;
        _bindingsTableModel.fireTableDataChanged();
        
        // Declare variable for row to select and scroll to
        int index = 0;
        
        // If there are properties, iterate over property names to find index of first one (and break)
        if(shape.getBindingCount()>0)
            for(int i=0; i<_bindingsTableModel.getRowCount(); i++)
                if(shape.getBinding((String)_bindingsTable.getValueAt(i, 0))!=null) {
                    index = i;
                    break;
                }
        
        // Select and scroll to property table row at index
        _bindingsTable.setRowSelectionInterval(index, index);
        _bindingsTable.scrollRectToVisible(_bindingsTable.getCellRect(index, 0, false));            
    }
    
    // Get selected binding index (table row) and binding key
    int row = _bindingsTable.getSelectedRow();
    String pname = row<0? null : (String)_bindingsTableModel.getValueAt(row, 0);
    
    // Reset BindingsText
    Binding binding = shape.getBinding(pname);
    setNodeValue("BindingsText", binding!=null? binding.getKey() : null);
    
    
    // Reset Event checkboxes
    setNodeValue("KeyPressedCheckBox", shape.isEnabled(KeyPressed));
    setNodeValue("KeyReleasedCheckBox", shape.isEnabled(KeyReleased));
    setNodeValue("KeyTypedCheckBox", shape.isEnabled(KeyTyped));
    setNodeValue("KeyFinishedCheckBox", shape.isEnabled(KeyFinished));
    setNodeValue("MousePressedCheckBox", shape.isEnabled(MousePressed));
    setNodeValue("MouseDraggedCheckBox", shape.isEnabled(MouseDragged));
    setNodeValue("MouseReleasedCheckBox", shape.isEnabled(MouseReleased));
    setNodeValue("MouseClickedCheckBox", shape.isEnabled(MouseClicked));
    setNodeValue("MouseFinishedCheckBox", shape.isEnabled(MouseFinished));
    setNodeValue("MouseEnteredCheckBox", shape.isEnabled(MouseEntered));
    setNodeValue("MouseMovedCheckBox", shape.isEnabled(MouseMoved));
    setNodeValue("MouseExitedCheckBox", shape.isEnabled(MouseExited));
    setNodeValue("DragEnterCheckBox", shape.isEnabled(DragEnter));
    setNodeValue("DragOverCheckBox", shape.isEnabled(DragOver));
    setNodeValue("DragExitCheckBox", shape.isEnabled(DragExit));
    setNodeValue("DragDropCheckBox", shape.isEnabled(DragDrop));
    setNodeValue("FocusGainedCheckBox", shape.isEnabled(UIEvent.Type.FocusGained));
    setNodeValue("FocusLostCheckBox", shape.isEnabled(UIEvent.Type.FocusLost));
}

/**
 * Updates current selection from Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Do normal version
    super.respondUI(anEvent);
    
    // Get the current editor and selected shape (just return if null)
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    RMShape shape = editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
    
    // Get currently selected shapes list
    List <? extends RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Handle BindingsText
    if(anEvent.equals("BindingsText")) {
        
        // Get selected binding table index (just return if less than zero)
        int row = _bindingsTable.getSelectedRow(); if(row<0) return;
        
        // Get selected PropertyName and Key
        String pname = (String)_bindingsTableModel.getValueAt(row, 0);
        String key = getNodeStringValue("BindingsText"); if(key!=null && key.length()==0) key = null;
        
        // Remove previous binding and add new one (if valid)
        for(RMShape shp : shapes)
            if(key!=null)
                shp.addBinding(pname, key);
            else shp.removeBinding(pname);
        
        // Reload table and reset selection
        _bindingsTableModel.fireTableDataChanged();
        _bindingsTable.setRowSelectionInterval(row, row);
    }
    
    // Handle Event CheckBoxes
    if(anEvent.equals("KeyPressedCheckBox") || anEvent.equals("KeyReleasedCheckBox") ||
        anEvent.equals("KeyTypedCheckBox") || anEvent.equals("KeyFinishedCheckBox") ||
        anEvent.equals("MousePressedCheckBox") || anEvent.equals("MouseDraggedCheckBox") ||
        anEvent.equals("MouseReleasedCheckBox") || anEvent.equals("MouseClickedCheckBox") ||
        anEvent.equals("MouseFinishedCheckBox") || anEvent.equals("MouseEnteredCheckBox") ||
        anEvent.equals("MouseMovedCheckBox") || anEvent.equals("MouseExitedCheckBox") ||
        anEvent.equals("DragEnterCheckBox") || anEvent.equals("DragOverCheckBox") ||
        anEvent.equals("DragExitCheckBox") || anEvent.equals("DragDropCheckBox") ||
        anEvent.equals("FocusGainedCheckBox") || anEvent.equals("FocusLostCheckBox")) {
        UIEvent.Type eventType = UIEvent.Type.valueOf(anEvent.getName().replace("CheckBox", ""));
        for(RMShape s : shapes)
            s.setEnabled(eventType, anEvent.getBoolValue());
    }
}

/**
 * Returns the name to be used in the inspector's window title.
 */
public String getWindowTitle()  { return "General Inspector"; }

/**
 * This TableModel is used to manage the inspector's BindingsTable.
 */
private class BindingsTableModel extends DefaultTableModel {

    // The shape that is providing data
    RMShape  _shape;

    /** Returns the number of shape properties. */
    public int getRowCount()  { return _shape!=null? _shape.getPropertyNames().size() : 0; }
    
    /** Returns the number of columns (2). */
    public int getColumnCount()  { return 2; }
    
    /** Returns the value for the given row & column. */
    public Object getValueAt(int row, int col)
    {
        String pname = _shape.getPropertyNames().get(row); if(col==0) return pname; // Get prop name (return if col 0)
        Binding binding = _shape.getBinding(pname); // Get binding for property name
        return binding==null? null : binding.getKey(); // Return binding key
    }
    
    /** Sets the value for the given row & column. */
    /*public void setValueAt(Object aValue, int row, int col)
    {
        RMEditor editor = RMEditor.getMainEditor(); // Get editor
        String key = (String)getValueAt(row, 0); // Get the name of the binding property name
        //for(RMShape s : editor.getSelectedShapes()) s.addBinding(key, (String)aValue); // Set SelectedShapes bindings
     }*/
    
    /** Returns whether cell is editable. */
    public boolean isCellEditable(int row, int col)  { return col==1; }
}

/**
 * A class to handle editing in the binding value column.
 * The default editor is a little strange, in that it uses different fonts from that used in the cell,
 * so this one makes sure to use same ones. It also updates the inspector when the cell changes, so changes
 * are reflected in the text area.
 */
private class ValueEditor extends DefaultCellEditor {

    /** Creates a new value editor. */
    public ValueEditor()
    {
        super(new JTextField()); // Do normal DefaultCellEditor init with a textfield
        
        // Add a focus listener to do UI reset and stop cell editing.
        getComponent().addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent arg0) {
            ShapeGeneral.this.resetLater();
            stopCellEditing();
        }});
    }

    /** Overrides DefaultCellEditor method to set same font in cell editor as properties table.  */
    public Component getTableCellEditorComponent(JTable aTable, Object aVal, boolean isSelected, int aRow, int aCol)
    {
        // Get normal table cell editor component
        JTextField comp = (JTextField)super.getTableCellEditorComponent(aTable, aVal, isSelected, aRow, aCol);
    
        // Get the properties table cell renderer (just return normal component if null)
        TableCellRenderer crenderer = _bindingsTable.getDefaultRenderer(String.class); if(crenderer==null) return comp;
    
        // Get table cell renderer component, set font to properties table cell renderer component font and return
        Component c = crenderer.getTableCellRendererComponent(aTable, aVal, false, false, aRow, aCol);
        comp.setFont(c.getFont());
        return comp;
    }  
}

}