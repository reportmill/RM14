package com.reportmill.swing.tool;
import com.reportmill.swing.shape.*;
import javax.swing.*;

/**
 * Provides Ribs UI editing for JTabbedPaneShape.
 */
public class JTabbedPaneTool extends JComponentTool {

    // The ItemsList JList
    JList         _itemsList;
    
/**
 * Initialize UI panel for this inspector.
 */
protected void initUI()
{
    // Add list model for items
    _itemsList = getNode("ItemsList", JList.class);
    _itemsList.setModel(new ItemsListModel());
}
    
/**
 * Resets UI from currently selected tab pane shape.
 */
public void resetUI()
{
    // Get tabbed pane shape and tabbed pane (just return if null)
    JTabbedPaneShape tabbedPaneShape = getSelectedShape(); if(tabbedPaneShape==null) return;
    
    // Tell the list to reload itself from the model and reset selection
    ((ItemsListModel)_itemsList.getModel()).didChange();
    int selection = tabbedPaneShape.getSelectedIndex();
    _itemsList.setSelectedIndex(selection);
    
    // Enable/Disable Delete & Rename buttons
    boolean validSelection = (selection >= 0);
    setNodeEnabled("RemoveItemButton", validSelection);
    setNodeEnabled("RenameItemButton", validSelection);
}

/**
 * Responds to UI.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get tabbed pane shape and tabbed pane (just return if null)
    JTabbedPaneShape tabbedPane = getSelectedShape(); if(tabbedPane==null) return;
    
    // Get ItemsList vars
    String newItemName = getNodeStringValue("ItemText");
    int selectedIndex = _itemsList.getModel().getSize()>0? _itemsList.getSelectedIndex() : -1;
    int numItems = tabbedPane.getTabCount();
    
    // Handle ItemsList: Notify parent that selection was (potentially) changed
    if(anEvent.is("ItemsList"))
        itemWasSelected(selectedIndex);
    
    // Handle AddItemButton and ItemText
    else if(anEvent.is("AddItemButton") || anEvent.is("ItemText")) {
        
        // Parent adds the item to whatever - then notify the list that the model changed
        tabbedPane.addTab(newItemName, new SwingPanelShape());
        
        // Change the list selection to the newly added string and forward the message
        _itemsList.setSelectedIndex(numItems);
        itemWasSelected(numItems);
        
        // Select the text in the textfield
        requestFocus("ItemText");
    }
    
    // Handle RemoveItemButton
    else if(anEvent.is("RemoveItemButton")) {
        if(selectedIndex>=0 && selectedIndex<numItems) {
            tabbedPane.remove(selectedIndex);
            if (--numItems >= 0) {
                if (selectedIndex >= numItems)
                    selectedIndex=numItems - 1;
                _itemsList.setSelectedIndex(selectedIndex);
                itemWasSelected(selectedIndex);
            }
        }
    }
    
    // Handle RenameItemButton
    else if(anEvent.is("RenameItemButton"))
        tabbedPane.setTitleAt(selectedIndex, newItemName);
}*/

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTabbedPaneShape.class; }
    
/**
 * Returns the name to be used for this tool in inspector window title.
 */
public String getWindowTitle()  { return "Tabbed Pane Inspector"; }
   
/**
 * If super-selected, allow mousedown on a tab to switch the pane.
 */
/*public void mousePressed(T aShape, MouseEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aShape))
        return;
    
    // Get the tabpane shape
    JTabbedPaneShape tabPaneShape = (JTabbedPaneShape)aShape;
    
    // Get event point in tab pane coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), tabPaneShape);
    
    // Get child shape
    RMShape hit = tabPaneShape.getChildContaining(point);

    // If no child hit, check to see if we hit a tab
    if(hit==null) {
        
        // Get hit tab index
        int whichTab = tabPaneShape.indexAtLocation((int)point.x, (int)point.y);
        
        // If valid tab index (and not current index), reset tab shape's selected index
        if(whichTab>=0 && whichTab!=tabPaneShape.getSelectedIndex()) {
            tabPaneShape.repaint();
            tabPaneShape.setSelectedIndex(whichTab);
        }
    }
    
    // If child was hit, forward on to normal mouse pressed
    else super.mousePressed(aShape, anEvent);
    
    // Consume event
    anEvent.consume();
}*/

/**
 * Returns the tab pane shape.
 */
public JTabbedPaneShape getSelectedShape()  { return (JTabbedPaneShape)super.getSelectedShape(); }

/** RJListController interface - returns tab count. */
public int getSize() { return getSelectedShape().getTabCount(); }

/** RJListController interface - returns tab title at index. */
public String getItemAt(int anIndex) { return getSelectedShape().getTitleAt(anIndex);}

/** RJListController interface - notifies that item was selected. */
public void itemWasSelected(int i)  { if(i>=-1) getSelectedShape().setSelectedIndex(i); }

/**
 * An inner class to forward list queries to the controller
 */
private class ItemsListModel extends AbstractListModel {
     
    /** Returns the number of items. */
    public int getSize()  { return getSelectedShape()!=null? JTabbedPaneTool.this.getSize() : 0; }
    
    /** Returns the element at the given index. */
    public Object getElementAt(int index)  { return getSelectedShape()!=null? getItemAt(index) : null; }
    
    /** Called to indicate that something changed. */
    public void didChange() { fireContentsChanged(this, 0, 999); }
}

}