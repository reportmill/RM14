package com.reportmill.apptools;
import com.reportmill.app.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * Provides Swing UI editing for RMCell shapes.
 */
public class RMCrossTabCellTool <T extends RMCrossTabCell> extends RMTextTool <T> implements RMSortPanel.Owner {

    // The sort panel
    RMSortPanel     _sortPanel;
    
    // Cached mouse pressed event in case cell tool needs to start forwarding events to table tool
    MouseEvent      _mousePressedEvent;

/**
 * Creates UI panel - base panel is RMTextTool version.
 */
protected JComponent createUI()  { return createUI(RMTextTool.class); }

/**
 * Create UI panel.
 */
protected void initUI()
{
    // Do normal version
    super.initUI();
        
    // Get text tool tab pane
    JTabbedPane tabPane = getNode("TabPane", JTabbedPane.class);
        
    // Get cell panel rib
    JComponent cellPanel = createUI(getClass());
    
    // Add tab for cell panel
    tabPane.insertTab("Cell", null, cellPanel, null, 0);
    
    // Create SortPanel, set bounds and install
    _sortPanel = new RMSortPanel(this);
    _sortPanel.getUI().setBounds(4, 45, 267, 100);
    cellPanel.add(_sortPanel.getUI());
}

/**
 * Updates UI from currently selected cell.
 */
public void resetUI()
{
    // Do normal reset ui
    super.resetUI();
    
    // Get currently selected cell (just return if null)
    RMCrossTabCell cell = getSelectedShape(); if(cell==null) return;
    
    // Get whether cell is in header row or column
    boolean isHeaderCell = cell.isColumnHeader() || cell.isRowHeader();
    
    // Update GroupingKeyText
    if(isHeaderCell)
        setNodeValue("GroupingKeyText", cell.getGrouping()==null? null : cell.getGrouping().getKey());
    
    // Update GroupingKeyText for invalid case
    else setNodeValue("GroupingKeyText", "(header cells only)");
    
    // Set GroupingKeyText enabled
    setNodeEnabled("GroupingKeyText", isHeaderCell);
    
    // Set GroupingKeyLabel enabled
    setNodeEnabled("GroupingKeyLabel", isHeaderCell);
    
    // Update VisibleCheckBox
    setNodeValue("VisibleCheckBox", cell.isVisible());
    
    // Update sortpanel
    _sortPanel.resetUI();
    
    // Update border checkboxes
    setNodeValue("ShowLeftBorderCheckBox", cell.getShowLeftBorder());
    setNodeValue("ShowRightBorderCheckBox", cell.getShowRightBorder());
    setNodeValue("ShowTopBorderCheckBox", cell.getShowTopBorder());
    setNodeValue("ShowBottomBorderCheckBox", cell.getShowBottomBorder());
}

/**
 * Updates currently selected cell from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected cell and table (just return if null)
    RMCrossTabCell cell = getSelectedShape(); if(cell==null) return;
    RMCrossTab table = (RMCrossTab)cell.getParent();
    
    // Get selected cross tab cell shapes
    List <RMCrossTabCell> cells = (List)getEditor().getSelectedOrSuperSelectedShapes();
    
    // Handle GroupingKeyText
    if(anEvent.equals("GroupingKeyText")) {
        
        // Get grouping key
        String key = RMStringUtils.delete(anEvent.getStringValue(), "@");
        
        // If no key, reset grouping
        if(key==null || key.length()==0)
            cell.setGrouping(null);
        
        // If cell grouping is null, create grouping
        else if(cell.getGrouping()==null)
            cell.setGrouping(new RMGrouping(key));
        
        // Otherwise, just set cell grouping
        else cell.getGrouping().setKey(key);
    }
    
    // Handle VisibleCheckBox - Set visible on selected cells
    if(anEvent.equals("VisibleCheckBox"))
        for(RMCrossTabCell cll : cells)
            cll.setVisible(anEvent.getBoolValue());
    
    // Handle ShowLeftBorderCheckBox - Set visible on selected cells
    if(anEvent.equals("ShowLeftBorderCheckBox"))
        for(RMCrossTabCell cll : cells)
            cll.setShowLeftBorder(anEvent.getBoolValue());
    
    // Handle ShowRightBorderCheckBox - Set visible on selected cells
    if(anEvent.equals("ShowRightBorderCheckBox"))
        for(RMCrossTabCell cll : cells)
            cll.setShowRightBorder(anEvent.getBoolValue());
    
    // Handle ShowTopBorderCheckBox - Set visible on selected cells
    if(anEvent.equals("ShowTopBorderCheckBox"))
        for(RMCrossTabCell cll : cells)
            cll.setShowTopBorder(anEvent.getBoolValue());
    
    // Handle ShowBottomBorderCheckBox - Set visible on selected cells
    if(anEvent.equals("ShowBottomBorderCheckBox"))
        for(RMCrossTabCell cll : cells)
            cll.setShowBottomBorder(anEvent.getBoolValue());
    
    // Register for layout
    table.relayout();
    
    // Do text tool respondUI
    super.respondUI(anEvent);    
}

/**
 * Returns the currently selected cell.
 */
public RMCrossTabCell getCell()  { return RMClassUtils.getInstance(getSelectedShape(), RMCrossTabCell.class); }

/**
 * Returns the grouping of the selected cell.
 */
public RMGrouping getGrouping()
{
    RMCrossTabCell cell = getCell(); if(cell==null) return null;
    return cell.getGrouping();
}

/**
 * Event handling - overrides text tool to pass handling to table tool if user really wants to select cells.
 */
public void mousePressed(T aShape, MouseEvent anEvent)
{
    _mousePressedEvent = anEvent; // Cache mouse pressed event
    super.mousePressed(aShape, anEvent); // Do normal version
}

/**
 * Event handling - overrides text tool to pass handling to table tool if user really wants to select cells.
 */
public void mouseDragged(T aCell, MouseEvent anEvent)
{
    // Get cell table
    RMCrossTab table = aCell.getTable();
    
    // If mouse pressed event is null, forward events to table
    if(_mousePressedEvent==null) {
        RMTool.getTool(table).mouseDragged(table, anEvent); return; }
    
    // Get event point in cell coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCell);
    
    // If point is outside cell, start sending to table tool
    if(point.getX()<-20 || point.getY()<-20 || point.getX()>aCell.getWidth()+20 || point.getY()>aCell.getHeight()+20) {
        
        // Make cell selected instead of super selected
        getEditor().setSelectedShape(aCell);
        
        // Send table table mouse pressed
        RMTool.getTool(table).mousePressed(table, _mousePressedEvent);
        
        // Clear mouse pressed event so we'll know that events should be forwarded
        _mousePressedEvent = null;
        
        // Send current mouse dragged event to table and return
        RMTool.getTool(table).mouseDragged(table, anEvent);
        return;
    }
    
    // Do normal text tool mouse dragged
    super.mouseDragged(aCell, anEvent);
}

/**
 * Event handling - overrides text to in case cell tool needs to forward events to table tool.
 */
public void mouseReleased(T aCell, MouseEvent anEvent)
{
    // Get cell table
    RMCrossTab table = aCell.getTable();
    
    // If mouse pressed event is null, forward on to table tool
    if(_mousePressedEvent==null) {
        RMTool.getTool(table).mouseReleased(table, anEvent); _mousePressedEvent = null; return; }
    
    // Clear mouse pressed event
    _mousePressedEvent = null;
    
    // Call normal text tool mouse released
    super.mouseReleased(aCell, anEvent);
}

/**
 * Key event handler for super selected cell.
 */
public void processKeyEvent(T aCell, KeyEvent anEvent)
{
    // Get key code
    int keyCode = anEvent.getKeyCode();
        
    // If key is tab press (non-alt), move forward or backward (based on shift modifier)
    if(keyCode==KeyEvent.VK_TAB && !anEvent.isAltDown() && anEvent.getID()==KeyEvent.KEY_PRESSED)
        getEditor().setSelectedShape(anEvent.isShiftDown()? aCell.getCellBefore() : aCell.getCellAfter());
            
    // If key is enter press (non-alt), move down or up
    else if(keyCode==KeyEvent.VK_ENTER && !anEvent.isAltDown() && anEvent.getID()==KeyEvent.KEY_PRESSED)
        getEditor().setSelectedShape(anEvent.isShiftDown()? aCell.getCellAbove() : aCell.getCellBelow());
        
    // Anything else goes to text tool
    else { super.processKeyEvent(aCell, anEvent); return; }

    // Consume event
    anEvent.consume();
}

/**
 * Overrides tool method to indicate that cells have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

/**
 * Returns the icon for bar labels (uses SwitchShapeTool icon).
 */
public Icon getIcon()  { return getIcon(RMTextTool.class); }

/**
 * Returns whether to draw bounds rect.
 */
public boolean drawBoundsRect(RMTextShape aText)  { return false; }

/**
 * Override normal implementation to handle KeysPanel drop.
 */
public void drop(T aCell, DropTargetDropEvent anEvent)
{
    // If KeysPanel is dragging, add key to text
    if(KeysPanel.getDragKey()!=null) {
    
        // Do normal text version to add drop string to text
        super.drop(aCell, anEvent);
    
        // Get the string
        String string = ClipboardUtils.getString(anEvent.getTransferable());
    
        // If this cell is header row or header column and there is no grouping, set grouping
        if((aCell.isColumnHeader() || aCell.isRowHeader()) && aCell.getGrouping()==null) {
            String key = RMStringUtils.delete(string, "@"); // Get key (drop string without @-signs)
            aCell.setGrouping(new RMGrouping(key)); // Set new grouping
        }
    }
    
    // Otherwise do normal version
    else super.drop(aCell, anEvent);
}

}