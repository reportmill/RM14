package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.DialogBox;
import snap.swing.SwingEvent;

/**
 * This class provides Swing UI editing for table rows.
 */
public class RMTableRowTool <T extends RMTableRow> extends RMParentShapeTool <T> {

/**
 * Update UI controls.
 */
public void resetUI()
{
    // Get selected table row (just return if null)
    RMTableRow trow = getSelectedShape(); if(trow==null) return;
    
    // Update StructuredCheckBox
    setNodeValue("StructuredCheckBox", trow.isStructured());
    
    // Update NumOfColumnsText
    setNodeValue("NumOfColumnsText", trow.getNumberOfColumns());
    setNodeEnabled("NumOfColumnsText", trow.isStructured());
    
    // Update SyncParentCheckBox
    setNodeValue("SyncParentCheckBox", trow.getSyncStructureWithRowAbove());
    setNodeEnabled("SyncParentCheckBox", trow.isStructured());
    
    // Update SyncAlternatesCheckBox
    setNodeValue("SyncAlternatesCheckBox", trow.getSyncStructureWithAlternates());
    setNodeEnabled("SyncAlternatesCheckBox", trow.isStructured());
    
    // Update StayWithChildrenCheckBox, ReprintCheckBox
    setNodeValue("StayWithChildrenCheckBox", trow.getNumberOfChildrenToStayWith()>0);
    setNodeValue("ReprintCheckBox", trow.getReprintWhenWrapped());
    
    // Update NumOfChildrenText
    setNodeValue("NumOfChildrenText", trow.getNumberOfChildrenToStayWith());
    setNodeEnabled("NumOfChildrenText", trow.getNumberOfChildrenToStayWith() > 0);
    
    // Update PrintIfNoObjectsCheckBox, MoveToBottomCheckBox
    setNodeValue("PrintIfNoObjectsCheckBox", trow.getPrintEvenIfGroupIsEmpty());
    setNodeValue("MoveToBottomCheckBox", trow.getMoveToBottom());
    
    // Update MinSplitHeightText, MinRemainderHeightText
    setNodeValue("MinSplitHeightText", getUnitsFromPoints(trow.getMinSplitHeight()));
    setNodeValue("MinRemainderHeightText", getUnitsFromPoints(trow.getMinSplitRemainderHeight()));
    
    // Update VersionKeyText
    setNodeValue("VersionKeyText", trow.getVersionKey());
}

/**
 * Handle UI changes.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected table row (just return if null)
    RMTableRow trow = getSelectedShape(); if(trow==null) return;
    trow.repaint();

    // Handle StructuredCheckBox
    if(anEvent.equals("StructuredCheckBox")) trow.setStructured(anEvent.getBoolValue());
    
    // Handle NumOfColumnsText    
    if(anEvent.equals("NumOfColumnsText")) trow.setNumberOfColumns(anEvent.getIntValue());

    // Handle SyncParentCheckBox, SyncAlternatesCheckBox
    if(anEvent.equals("SyncParentCheckBox")) trow.setSyncStructureWithRowAbove(anEvent.getBoolValue());
    if(anEvent.equals("SyncAlternatesCheckBox")) trow.setSyncStructureWithAlternates(anEvent.getBoolValue());

    // Handle StayWithChildrenCheckBox, ReprintCheckBox
    if(anEvent.equals("StayWithChildrenCheckBox"))
        trow.setNumberOfChildrenToStayWith(anEvent.getBoolValue()? 1 : 0);
    if(anEvent.equals("ReprintCheckBox"))
        trow.setReprintWhenWrapped(anEvent.getBooleanValue());
    
    // Handle NumOfChildrenText
    if(anEvent.equals("NumOfChildrenText"))
        trow.setNumberOfChildrenToStayWith(anEvent.getIntValue());

    // Handle PrintIfNoObjectsCheckBox, MoveToBottomCheckBox
    if(anEvent.equals("PrintIfNoObjectsCheckBox")) trow.setPrintEvenIfGroupIsEmpty(anEvent.getBoolValue());
    if(anEvent.equals("MoveToBottomCheckBox")) trow.setMoveToBottom(anEvent.getBoolValue());

    // Handle MinSplitHeightText, MinRemainderHeightText
    if(anEvent.equals("MinSplitHeightText")) trow.setMinSplitHeight(getPointsFromUnits(anEvent.getFloatValue()));
    if(anEvent.equals("MinRemainderHeightText"))
        trow.setMinSplitRemainderHeight(getPointsFromUnits(anEvent.getFloatValue()));

    // Handle VersionKeyText
    if(anEvent.equals("VersionKeyText"))
        trow.setVersionKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
}

/**
 * Loads a popup menu with menus specific for currently selected table row.
 */
public JPopupMenu getPopupMenu(RMTableRow aTableRow)
{
    // Create pop up menu
    JPopupMenu menu = new JPopupMenu();

    // Get list of alternates names. Make sure it has current mode
    List alternates = aTableRow.getAlternates()==null? new Vector() : new Vector(aTableRow.getAlternates().keySet());

    // Make sure alternates array exists and has current mode
    if(!alternates.contains(aTableRow.getVersion()))
        alternates.add(aTableRow.getVersion());

    // Sort Alternates and make sure Standard band is listed first
    Collections.sort(alternates);
    alternates.remove("Standard");
    alternates.add(0, "Standard");

    // Add menu items for each version
    for(int i=0, iMax=alternates.size(); i<iMax; i++) {
        final String title = (String)alternates.get(i);
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setVersionFromMenu(title); } });
        menu.add(item);
    }

    // Add a menu divider
    menu.addSeparator();

    // Add 'Remove' menu item
    JMenuItem item = new JMenuItem("Remove");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { removeVersionCurrent(); }});
    menu.add(item);

    // Add 'Add First Only' if it isn't present
    if(!alternates.contains("First Only")) {
        item = new JMenuItem("Add First Only");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("First Only"); }});
        menu.add(item);
    }

    // Add 'Add Reprint' if it isn't present
    if(!alternates.contains("Reprint")) {
        item = new JMenuItem("Add Reprint");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("Reprint"); }});
        menu.add(item);
    }

    // Add 'Add Alternate' if it isn't present
    if(!alternates.contains("Alternate")) {
        item = new JMenuItem("Add Alternate");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("Alternate"); }});
        menu.add(item);
    }

    // Add 'Add Running' if it isn't present and aTableRow is summary
    if(!alternates.contains("Running") && aTableRow.getTitle()!=null && aTableRow.getTitle().endsWith("Summary")) {
        item = new JMenuItem("Add Running");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("Running"); }});
        menu.add(item);
    }

    // Add 'TopN Others' if it isn't present
    if(!alternates.contains("TopN Others")) {
        item = new JMenuItem("Add TopN Others");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("TopN Others"); }});
        menu.add(item);
    }

    // Add 'Split Header' if it isn't present
    if(!alternates.contains("Split Header")) {
        item = new JMenuItem("Add Split Header");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addVersionFromMenu("Split Header"); }});
        menu.add(item);
    }

    // Add 'Add Custom...'
    item = new JMenuItem("Add Custom...");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { addVersionFromMenu("Custom..."); }});
    menu.add(item);
    
    // Return menu
    return menu;
}

/**
 * Sets the version of the currently selected table row.
 */
public void setVersionFromMenu(String aVersion)
{
    // Get editor and selected TableRow
    RMEditor editor = RMEditor.getMainEditor();
    RMTableRow tableRow = (RMTableRow)editor.getSelectedOrSuperSelectedShape();
    
    // Set version
    tableRow.repaint(); // Register table row for repaint
    tableRow.undoerSetUndoTitle("Version Change"); // Set undo title
    tableRow.setVersion(aVersion); // Set table row version
    tableRow.getParent().repaint(); // Register table for repaint
    editor.setSuperSelectedShape(tableRow); // Super select table row
}

/**
 * Adds a new version to the currently selected table row.
 */
public void addVersionFromMenu(String aVersion)
{
    // Get main editor and selected TableRow
    RMEditor editor = RMEditor.getMainEditor();
    RMTableRow tableRow = (RMTableRow)editor.getSelectedOrSuperSelectedShape();

    // Get name of Custom Version if requested
    if(aVersion.equals("Custom...")) {
        DialogBox dbox = new DialogBox("Custom Alternate");
        dbox.setQuestionMessage("Input label for custom table row version");
        aVersion = dbox.showInputDialog(editor, "");
    }
    
    // If version string is invalid, just return
    if(RMStringUtils.length(aVersion)==0) return;
    
    // If version already exists, set version instead
    if(tableRow.hasVersion(aVersion)) {
        setVersionFromMenu(aVersion); return; }
    
    // Set version
    tableRow.repaint(); // Register table row for repaint
    tableRow.undoerSetUndoTitle("Add New Version"); // Set undo title
    tableRow.setVersion(aVersion); // Set version
    editor.setSelectedShapes(tableRow.getChildren()); // Select new table row children
}

/**
 * Removes the currently selected version from the currently selected table row.
 */
public void removeVersionCurrent()
{
    // Get main editor and selected TableRow
    RMEditor editor = RMEditor.getMainEditor();
    RMTableRow tableRow = (RMTableRow)editor.getSelectedOrSuperSelectedShape();
    
    // Register table row for repaint (thus undo)
    tableRow.repaint();
    
    // Get current table row version
    String version = tableRow.getVersion();

    // Complain and return if user tries to remove Standard version
    if(version.equals("Standard"))
        Toolkit.getDefaultToolkit().beep();

    // Remove version (with undo grouping)
    else {
        tableRow.undoerSetUndoTitle("Remove Version");
        tableRow.removeVersion(version);
    }
    
    // Register table for repaint
    tableRow.getParent().repaint();
}

/**
 * Adds a column to the currently selected table row.
 */
public static void addColumn()
{
    // Get currently selected editor and selected shape
    RMEditor editor = RMEditor.getMainEditor();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    
    // Get currently selected table row (by iterating up selected shape's ancestor list)
    while(shape!=null && !(shape instanceof RMTableRow))
        shape = shape.getParent();
    
    // If no currently selected table row, just return
    if(shape==null) return;
    
    // Add column
    RMTableRow tableRow = (RMTableRow)shape; // Get the table row
    tableRow.setNumberOfColumns(tableRow.getNumberOfColumns()+1); // Increment ColumnCount
    editor.setSuperSelectedShape(tableRow.getChildLast()); // Super-Select last child
}

/**
 * Returns the class that this tool is responsible for (RMTableRow).
 */
public Class getShapeClass()  { return RMTableRow.class; }

/**
 * Returns the name that should be used in the inspector window.
 */
public String getWindowTitle()  { return "Table Row Inspector"; }

/**
 * Overridden to make table row not ungroupable.
 */
public boolean isUngroupable(RMShape aShape)  { return false; }

/**
 * MouseMoved implementation to update cursor for resize bars.
 */
public void mouseMoved(T aTableRow, MouseEvent anEvent)
{
    // If structured
    if(!aTableRow.isStructured()) { super.mouseMoved(aTableRow, anEvent); return; }
        
    // Get handle shape
    RMShapeHandle shapeHandle = getShapeHandleAtPoint(anEvent.getPoint());
    
    // If shape handle is non-null, see if it's a structured text that needs special cursor
    if(shapeHandle!=null) {

        // If shape handle shape is structured text, set cursor, consume event and return
        if(shapeHandle.getShape() instanceof RMTextShape && ((RMTextShape)shapeHandle.getShape()).isStructured()) {
            
            // Set cursor
            if(shapeHandle.getHandle()==HandleNW)
                getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            else getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            
            // Consume event and return
            anEvent.consume(); return;
        }
    }
    
    // Do normal mouse moved
    super.mouseMoved(aTableRow, anEvent);
}

/**
 * Mouse pressed implementation to make sure structured table row columns get selected.
 */
public void mousePressed(T aTableRow, MouseEvent anEvent)
{
    // If selected and structured, select child
    if(aTableRow.isStructured() && aTableRow!=getEditor().getSuperSelectedShape().getParent()) {
        
        // Get the point and child at point
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aTableRow);
        RMShape child = aTableRow.getChildContaining(point);
        
        // If child was hit, super select it and resend event
        if(child!=null) {
            getEditor().setSuperSelectedShape(child); // Select child
            RMTool.getSelectTool().setRedoMousePressed(true); // Have SelectTool resend event
        }
    }
}

/**
 * Overrides tool method to declare that table rows have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}