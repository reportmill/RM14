package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import javax.swing.SwingUtilities;
import snap.swing.*;
import snap.web.Entity;

/**
 * This class shows the current set of keys relative to the current editor selection in a browser and lets users
 * drag and drop them to the editor.
 */
public class KeysPanel extends SwingOwner {
    
    // The KeysPanel browser
    KeysBrowser              _keysBrowser;
    
    // Whether to show built in keys
    boolean                  _showBuiltIn = false;
    
    // Shared
    static KeysPanel         _shared;

/**
 * Creates a new keys panel.
 */
public KeysPanel()  { if(_shared==null) _shared = this; }

/**
 * Initialize UI panel.
 */
protected void initUI()  { _keysBrowser = getNode("KeysBrowser", KeysBrowser.class); }

/**
 * Updates the UI from the current selection.
 */
public void resetUI()
{
    // Get selected shape and shape tool
    RMShape selectedShape = getSelectedShape();
    RMTool tool = RMTool.getTool(selectedShape);
    
    // Get entity from tool/shape and set in browser
    Entity entity = tool.getDatasetEntity(selectedShape);
    _keysBrowser.setEntity(_showBuiltIn? null : entity);
    
    // Update BuiltInKeysButton
    setNodeValue("BuiltInKeysButton", _showBuiltIn);
}

/**
 * Updates the current selection from the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle KeysBrowser (double-click - used to check anEvent.getClickCount()==2)
    if(anEvent.equals("KeysBrowser") && anEvent.isMouseClicked() && anEvent.getClickCount()==2) {

        // If double-click on RMTable, add grouping
        if(getSelectedShape() instanceof RMTable) {
            RMTableTool tool = (RMTableTool)RMTool.getTool(getSelectedShape());
            tool.addGroupingKey(_keysBrowser.getPath());
        }

        // If leaf click for RMText, add key
        else if(_keysBrowser.isSelectedLeaf() && RMEditor.getMainEditor().getTextEditor()!=null)
            RMEditor.getMainEditor().getTextEditor().replace(_keysBrowser.getKeyPath());
    }
    
    // Handle BuiltInKeysButton
    if(anEvent.equals("BuiltInKeysButton"))
        _showBuiltIn = anEvent.getBoolValue();
}

/**
 * Returns the current editor's selected shape.
 */
public RMShape getSelectedShape()  { return RMEditor.getMainEditor().getSelectedOrSuperSelectedShape(); }

/**
 * Returns the window title for this panel.
 */
public String getWindowTitle()  { return "Keys Panel"; }

/**
 * Returns the current drag key.
 */
public static String getDragKey()
{
    return _shared!=null && _shared._keysBrowser!=null? _shared._keysBrowser.getDragKey() : null;
}

/**
 * Sets the current drag key.
 */
public static void setDragKey(String aKey)  { _shared._keysBrowser._dragKey = aKey; }

/**
 * Returns whether selected key path is to-many.
 */
public static boolean isSelectedToMany()  { return _shared!=null && _shared._keysBrowser.isSelectedToMany(); }

/**
 * Drops a drag key.
 */
public static void dropDragKey(RMShape aShape, DropTargetDropEvent anEvent)
{
    // Get editor
    final RMEditor editor = (RMEditor)anEvent.getDropTargetContext().getComponent();
    
    // Get transferable
    Transferable transferable = anEvent.getTransferable();
    
    // Handle KeysPanel to-many drop - run dataset key panel (after delay)
    if(KeysPanel.isSelectedToMany()) {
        final String datasetKey = RMStringUtils.delete(KeysPanel.getDragKey(), "@");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { RMEditorShapes.runDatasetKeyPanel(editor, datasetKey); }});
    }
    
    // Otherwise, just drop string as text shape
    else {
        aShape.repaint();
        editor.undoerSetUndoTitle("Drag and Drop Key");
        RMEditorClipboard.paste(editor, transferable, (RMParentShape)aShape, anEvent.getLocation());            
    }
}

}