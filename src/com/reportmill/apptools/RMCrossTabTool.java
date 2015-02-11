package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides support for UI editing of RMCrossTab.
 */
public class RMCrossTabTool <T extends RMCrossTab> extends RMTool <T> {

    // The currently selected divider
    RMCrossTabDivider  _divider;
    
    // Whether popup trigger has been encountered during mouse loop
    boolean            _popupTriggered;
    
/**
 * Resets the Swing UI from current selected crosstab.
 */
public void resetUI()
{
    // Get currently selected crosstab (just return if null)
    RMCrossTab table = getTable(); if(table==null) return;
    
    // Update DatasetKeyText, FilterKeyText, RowCountSpinner, ColCountSpinner, HeaderRow/HeaderColCountSpinner
    setNodeValue("DatasetKeyText", table.getDatasetKey());
    setNodeValue("FilterKeyText", table.getFilterKey());
    setNodeValue("RowCountSpinner", table.getRowCount());
    setNodeValue("ColCountSpinner", table.getColCount());
    setNodeValue("HeaderRowCountSpinner", table.getHeaderRowCount());
    setNodeValue("HeaderColCountSpinner", table.getHeaderColCount());
}

/**
 * Updates currently selected crosstab from Swing UI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected CrossTab and cell (just return if null)
    RMCrossTab ctab = getTable(); if(ctab==null) return;
    RMCrossTabCell cell = getCell();
    
    // Handle DatasetKeyText, FilterKeyText
    if(anEvent.equals("DatasetKeyText")) ctab.setDatasetKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));
    if(anEvent.equals("FilterKeyText")) ctab.setFilterKey(RMStringUtils.delete(anEvent.getStringValue(), "@"));

    // Handle RowsSpinner: Get count, make sure it's at least 1 and at least the number of header rows, and set
    if(anEvent.equals("RowCountSpinner")) {
        int count = anEvent.getIntValue();
        count = Math.max(count, 1); count = Math.max(count, ctab.getHeaderRowCount());
        ctab.setRowCount(count);
    }
    
    // Handle ColumnsSpinner: Get count, make sure it's at least 1 and at least the number of header columns, and set
    if(anEvent.equals("ColCountSpinner")) {
        int count = anEvent.getIntValue();
        count = Math.max(count, 1); count = Math.max(count, ctab.getHeaderColCount());
        ctab.setColumnCount(count);
    }
    
    // Handle HeaderRowCountSpinner, HeaderColCountSpinner
    if(anEvent.equals("HeaderRowCountSpinner"))
        if(ctab.getRowCount()-ctab.getHeaderRowCount()+anEvent.getIntValue()>0)
            ctab.setHeaderRowCount(anEvent.getIntValue());
    if(anEvent.equals("HeaderColCountSpinner"))
        if(ctab.getColCount()-ctab.getHeaderColCount()+anEvent.getIntValue()>0)
            ctab.setHeaderColCount(anEvent.getIntValue());
    
    // Handle ClearContentsMenuItem
    if(anEvent.equals("ClearContentsMenuItem"))
        for(int i=0, iMax=getEditor().getSelectedOrSuperSelectedShapeCount(); i<iMax; i++)
            if(getEditor().getSelectedOrSuperSelectedShape(i) instanceof RMCrossTabCell) {
                getEditor().getSelectedOrSuperSelectedShape(i).repaint();
                ((RMCrossTabCell)getEditor().getSelectedOrSuperSelectedShape(i)).clearContents();
            }
    
    // Handle AddRowAboveMenuItem, AddRowBelowMenuItem, AddColBeforeMenuItem, AddColAfterMenuItem
    if(anEvent.equals("AddRowAboveMenuItem") && cell!=null) ctab.addRow(cell.getRow());
    if(anEvent.equals("AddRowBelowMenuItem") && cell!=null) ctab.addRow(cell.getRowEnd() + 1);
    if(anEvent.equals("AddColBeforeMenuItem") && cell!=null) ctab.addCol(cell.getCol());
    if(anEvent.equals("AddColAfterMenuItem") && cell!=null) ctab.addCol(cell.getColEnd() + 1);
    
    // Handle RemoveRowMenuItem, RemoveColMenuItem
    if(anEvent.equals("RemoveRowMenuItem") && cell!=null) {
        ctab.removeRow(cell.getRow()); getEditor().setSuperSelectedShape(ctab); }
    if(anEvent.equals("RemoveColMenuItem") && cell!=null) {
        ctab.removeCol(cell.getCol()); getEditor().setSuperSelectedShape(ctab); }
    
    // Handle MergeCellsMenuItem
    if(anEvent.equals("MergeCellsMenuItem") && cell!=null) {
        
        // Get selected cell row/col min/min and expand to total row & col min/max
        int rowMin = cell.getRow(), rowMax = cell.getRow();
        int colMin = cell.getCol(), colMax = cell.getCol();
        for(int i=1; i<getEditor().getSelectedShapeCount(); i++) { 
            cell = (RMCrossTabCell)getEditor().getSelectedShape(i);
            rowMin = Math.min(rowMin, cell.getRow()); rowMax = Math.max(rowMax, cell.getRow());
            colMin = Math.min(colMin, cell.getCol()); colMax = Math.max(colMax, cell.getCol());
        }
            
        // Have table merge cells and super-select cell
        ctab.mergeCells(rowMin, colMin, rowMax, colMax);
        getEditor().setSuperSelectedShape(ctab.getCell(rowMin, colMin));
    }
    
    // Handle SplitCellMenuItem
    if(anEvent.equals("SplitCellMenuItem") && cell!=null)
        ctab.splitCell((RMCrossTabCell)getEditor().getSelectedOrSuperSelectedShape());

    // Handle AutoFormatButton and AutoFormatButton
    if(anEvent.equals("AutoFormatButton") || anEvent.equals("AutoFormatButton"))
        new RMCrossTabAutoFormatPanel().showPanel(getEditor(), ctab);
}

/**
 * Event handling - overridden to set a custom cursor.
 */
public void mouseMoved(T aCTab, MouseEvent anEvent)
{
    // Get shape under point
    RMShape shape = getEditor().getShapeAtPoint(anEvent.getPoint());
    
    // If shape is a divider, set RESIZE_CURSOR
    if(shape instanceof RMCrossTabDivider) { RMCrossTabDivider divider = (RMCrossTabDivider)shape;
        if(divider.isRowDivider()) getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        else getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        anEvent.consume(); // Consume event
    }
    
    // If shape is cell, set TEXT_CURSOR
    else if(shape instanceof RMCrossTabCell) {
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        anEvent.consume();
    }
        
    // Otherwise, do default behavior
    else super.mouseMoved(aCTab, anEvent);
}

/**
 * Handles Shape MousePressed.
 */
public void mousePressed(T aCTab, MouseEvent anEvent)
{
    // If event is popup trigger, run crosstab popup
    if(anEvent.isPopupTrigger()) { _popupTriggered = true; runContextMenu(anEvent); return; }
    
    // If super selected, ensure that cell under event point is super selected
    if(getEditor().getSuperSelectedShape()==aCTab) {
        
        // Get the event point in crosstab coords and cell under point
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCTab);
        RMShape piece = aCTab.getChildContaining(point);
        
        // Clear divider
        _divider = null;
        
        // If hit cell: Record DownPoint and consume
        if(piece instanceof RMCrossTabCell) {
            _downPoint = getEditor().convertPointToShape(anEvent.getPoint(), aCTab);
            anEvent.consume();
        }
        
        // If hit divider, set it
        else if(piece instanceof RMCrossTabDivider) {
            _divider = (RMCrossTabDivider)piece; // Set divider
            getEditor().setSelectedShape(_divider); // Select divider
            anEvent.consume(); // Consume event
        }
    }
}

/**
 * Handle CrossTab mouse dragged.
 */
public void mouseDragged(T aCTab, MouseEvent anEvent)
{
    // If popup trigger, consume event and return
    if(anEvent.isPopupTrigger() || _popupTriggered) { _popupTriggered = true; anEvent.consume(); return; }
    
    // If no divider, select cells in selection rect
    if(_divider==null) {
        
        // Get event point int table coords and cell rect for DownPoint and EventPoint
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCTab);
        Rectangle cellRect = getCellRect(aCTab, new RMRect(_downPoint, point));
        
        // Create/fill list with selected cells (unique)
        List cells = new ArrayList();
        for(int i=cellRect.y; i<=cellRect.y + cellRect.height; i++)
            for(int j=cellRect.x; j<=cellRect.x + cellRect.width; j++)
                if(!cells.contains(aCTab.getCell(i, j)))
                    cells.add(aCTab.getCell(i, j));
        
        // Select cells
        getEditor().setSelectedShapes(cells);
    }
    
    // If divider is row divider, resize rows
    else if(_divider.isRowDivider()) {
        
        // Get event point in table coords and divider move delta
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCTab);
        double delta = point.y - _divider.getY();
        
        // Get divider row and resize
        RMCrossTabRow row = _divider.getRow();
        if(row.getIndex()>=0) row.setHeight(row.getHeight() + delta);
    }
    
    // If divider is column divider, resize columns
    else {
        
        // Get event point in table coords and divider move delta
        RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), aCTab);
        double delta = point.x - _divider.getX();
        
        // Get divider column and resize
        RMCrossTabCol column = _divider.getColumn();
        if(column.getIndex()>=0) column.setWidth(column.getWidth() + delta);
    }
    
    // Register for layout/repaint
    aCTab.relayout(); aCTab.repaint();
}

/**
 * Handle crosstab mouse released.
 */
public void mouseReleased(T aCTab, MouseEvent anEvent)
{
    // If popup trigger, consume event and return
    if(anEvent.isPopupTrigger() || _popupTriggered) {
        if(!_popupTriggered) runContextMenu(anEvent); _popupTriggered = false; anEvent.consume(); return; }
}

/**
 * Key event handler for crosstab editing.
 */
public void processKeyEvent(T aCTab, KeyEvent anEvent)
{
    // If event isn't typed or pressed, just return
    if(anEvent.getID()!=KeyEvent.KEY_PRESSED && anEvent.getID()!=KeyEvent.KEY_TYPED) return;
    
    // Have table register for repaint (and thus undo)
    aCTab.repaint();
    
    // Get key code
    int keyCode = anEvent.getKeyCode();
    
    // If backspace or delete key is pressed, remove selected divider
    if(getEditor().getSelectedShape() instanceof RMCrossTabDivider) {
        
        // If key was backspace or delete, remove selected grouping
        if(keyCode==KeyEvent.VK_BACK_SPACE || keyCode==KeyEvent.VK_DELETE) {
            
            // Get the selected divider
            RMCrossTabDivider divider = (RMCrossTabDivider)getEditor().getSelectedShape();
            
            // If divider is last column or row divider, just beep
            if(divider.isColumnDivider()? divider.getNextColumn()==null : divider.getNextRow()==null)
                Toolkit.getDefaultToolkit().beep();
            
            // If column divider, merge cells around divider
            else if(divider.isColumnDivider()) {
                
                // Get column index and iterate over divider rows and merge cells on either side of divider
                int col = divider.getColumn().getIndex();
                for(int i=divider.getStart(), iMax=divider.getEnd(); i<iMax; i++)
                    aCTab.mergeCells(i, col, i, col+1);
            }
            
            // If row divider, merge cells around divider
            else {
                
                // Get divider row index and iterate over divider columns and merge cells on either side of divider
                int row = divider.getRow().getIndex();
                for(int i=divider.getStart(), iMax=divider.getEnd(); i<iMax; i++)
                    aCTab.mergeCells(row, i, row+1, i);
            }
            
            // Selected crosstab
            getEditor().setSuperSelectedShape(aCTab);
        }
    }
    
    // If selected shape is cell, either change selection or super select
    if(getEditor().getSelectedShape() instanceof RMCrossTabCell) {
        
        // Get selected cell
        RMCrossTabCell cell = (RMCrossTabCell)getEditor().getSelectedShape();
        
        // If key is right arrow or tab, move forward
        if(keyCode==KeyEvent.VK_RIGHT || (keyCode==KeyEvent.VK_TAB && !anEvent.isShiftDown()))
            getEditor().setSelectedShape(cell.getCellAfter());
        
        // If key is left arrow or shift tab, move backward
        else if(keyCode==KeyEvent.VK_LEFT || (keyCode==KeyEvent.VK_TAB && anEvent.isShiftDown()))
            getEditor().setSelectedShape(cell.getCellBefore());

        // If key is down arrow or enter, move down
        else if(keyCode==KeyEvent.VK_DOWN || (keyCode==KeyEvent.VK_ENTER && !anEvent.isShiftDown()))
            getEditor().setSelectedShape(cell.getCellBelow());
        
        // If key is up arrow or shift-enter, move up
        else if(keyCode==KeyEvent.VK_UP || (keyCode==KeyEvent.VK_ENTER && anEvent.isShiftDown()))
            getEditor().setSelectedShape(cell.getCellAbove());
        
        // If key has meta-down or control-down, just return
        else if(anEvent.isMetaDown() || anEvent.isControlDown())
            return;
        
        // If key char is control character or undefined, just return
        else if(anEvent.getKeyChar()==KeyEvent.CHAR_UNDEFINED || Character.isISOControl(anEvent.getKeyChar()))
            return;
        
        // If key is anything else, superselect cell and forward key press
        else {
            getEditor().setSuperSelectedShape(cell); // Super-select cell
            RMTool.getTool(cell).processKeyEvent(cell, anEvent); // Forward to cell tool
        }
    }
    
    // Consume event
    anEvent.consume();
}

/**
 * Runs a context menu for the given event.
 */
public void runContextMenu(MouseEvent anEvent)
{
    // Create PopupMenu and configure
    JPopupMenu pmenu = new JPopupMenu();
    JMenuItem mitem = new JMenuItem("Clear Contents"); mitem.setName("ClearContentsMenuItem"); pmenu.add(mitem);
    pmenu.addSeparator();
    mitem = new JMenuItem("Add Row Above"); mitem.setName("AddRowAboveMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Add Row Below"); mitem.setName("AddRowBelowMenuItem"); pmenu.add(mitem);
    pmenu.addSeparator();
    mitem = new JMenuItem("Add Column Before"); mitem.setName("AddColBeforeMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Add Column After"); mitem.setName("AddColAfterMenuItem"); pmenu.add(mitem);
    pmenu.addSeparator();
    mitem = new JMenuItem("Remove Row"); mitem.setName("RemoveRowMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Remove Column"); mitem.setName("RemoveColMenuItem"); pmenu.add(mitem);
    pmenu.addSeparator();
    mitem = new JMenuItem("Merge Cells"); mitem.setName("MergeCellsMenuItem"); pmenu.add(mitem);
    mitem = new JMenuItem("Split Cell"); mitem.setName("SplitCellMenuItem"); pmenu.add(mitem);
    pmenu.addSeparator();
    mitem = new JMenuItem("Auto Format..."); mitem.setName("AutoFormatMenuItem"); pmenu.add(mitem);
    initUI(pmenu);
    
    // Run menu and consume event
    pmenu.show(anEvent.getComponent(), anEvent.getX(), anEvent.getY());
    anEvent.consume();
}

/**
 * Highlights the selected cells or dividers.
 */
public void paintShapeHandles(T aShape, Graphics2D g, boolean isSuperSelected)
{
    // If not super-selected just do normal paintShapeHandles and return
    if(!isSuperSelected) { super.paintShapeHandles(aShape, g, isSuperSelected); return; }
    
    // Get the table and declare rect to highlight
    RMCrossTab table = getTable();
    Shape drawShape = null;
    
    // If super selected shape is RMCell, get path for ouset bounds
    if(getEditor().getSuperSelectedShape() instanceof RMCrossTabCell) {
        
        // Get super selected shape
        RMShape cell = getEditor().getSuperSelectedShape();
        
        // Get rect, outset by 2
        RMRect rect = cell.getBoundsInside(); rect.inset(-2.5f, -2.5f);
        RMPath path = new RMPath();
        RMPathUtils.appendShape(path, rect);
        drawShape = getEditor().getTransformFromShape(cell).createTransformedShape(path);
    }
    
    // If selected shape is RMCell, get path for outset bounds
    else if(getEditor().getSelectedShape() instanceof RMCrossTabCell) {
        
        // Create path: Iterate over selected shapes, get bounds of selected shapes ouset by 2
        RMPath path = new RMPath();
        for(int i=0, iMax=getEditor().getSelectedShapeCount(); i<iMax; i++) {
            RMRect bounds = getEditor().getSelectedShape(i).getBounds(); bounds.inset(-2.5f, -2.5f);
            RMPathUtils.appendShape(path, bounds);
        }
        
        // Create area from path to combine subpaths
        Area area = new Area(path);
        
        // Get shape of bounds transformed to editor
        drawShape = getEditor().getTransformFromShape(table).createTransformedShape(area);
    }
    
    // If selected shape is divider, get path for all selected dividers
    else if(getEditor().getSelectedShape() instanceof RMCrossTabDivider) {
        
        // Iterate over editor selected shapes
        RMPath path = new RMPath();
        for(int i=0, iMax=getEditor().getSelectedShapeCount(); i<iMax; i++) {
            RMShape shape = getEditor().getSelectedShape(i); // Get current loop selected shape
            RMRect bounds = shape.getBounds(); bounds.inset(-2.5f, -2.5f); // Get bounds outset by 3
            RMPathUtils.appendShape(path, bounds); // Append bounds
        }
        
        // Get path in editor coords
        drawShape = getEditor().getTransformFromShape(table).createTransformedShape(path);
    }
    
    // If draw shape is non-null, stroke it
    if(drawShape!=null) {
        g.setStroke(new BasicStroke(4*getEditor().getZoomFactor()));
        g.setColor(new Color(253, 219, 19));
        g.draw(drawShape);
    }
    
    // Do normal paintShapeHandles
    super.paintShapeHandles(aShape, g, isSuperSelected);
}

/**
 * Returns the selected table.
 */
public RMCrossTab getTable()
{
    // Get editor and selected shape
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return null;
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    
    // Iterate up chain until table is found and return
    while(shape!=null && !(shape instanceof RMCrossTab)) shape = shape.getParent();
    return (RMCrossTab)shape;
}

/**
 * Returns the selected cell.
 */
public RMCrossTabCell getCell()
{
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); // Get editor selected or super selected shape
    return shape instanceof RMCrossTabCell? (RMCrossTabCell)shape : null;
}

/**
 * Returns the cell rect for the given rectangle2d.
 */
public Rectangle getCellRect(RMCrossTab aCTab, Rectangle2D aRect)
{
    // Get row min & max and col min & max and return rect
    int rowMin = RMMath.clamp(aCTab.getRow(aRect.getY()), 0, aCTab.getRowCount() - 1);
    int rowMax = RMMath.clamp(aCTab.getRow(aRect.getMaxY()), 0, aCTab.getRowCount() - 1);
    int colMin = RMMath.clamp(aCTab.getCol(aRect.getX()), 0, aCTab.getColCount() - 1);
    int colMax = RMMath.clamp(aCTab.getCol(aRect.getMaxX()), 0, aCTab.getColCount() - 1);
    return new Rectangle(colMin, rowMin, colMax - colMin, rowMax - rowMin);
}

/**
 * Returns the shape class this tool edits (RMTable).
 */
public Class getShapeClass()  { return RMCrossTab.class; }

/**
 * Returns the display name for this tool ("Table Inspector").
 */
public String getWindowTitle()  { return "CrossTab Inspector"; }

/**
 * Overridden to make crosstab super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overridden to make crosstab ungroupable.
 */
public boolean isUngroupable(RMShape aShape)  { return false; }

/**
 * Returns the number of handles for this shape.
 */
public int getHandleCount(T aShape)
{
    // If crosstab parent isn't crosstab frame, return normal tool implementation
    if(!(aShape.getParent() instanceof RMCrossTabFrame)) return super.getHandleCount(aShape);
    
    // crosstabs in crosstab frame have 3 handles
    return 3;
}

/**
 * Editor method.
 */
public RMPoint getHandlePoint(T aShape, int aHandle, boolean isSuperSelected)
{
    // If crosstab parent isn't crosstab frame, return normal tool implementation
    if(!(aShape.getParent() instanceof RMCrossTabFrame))
        return super.getHandlePoint(aShape, aHandle, isSuperSelected);
    
    // Call base tool implementation with base tool handle
    return super.getHandlePoint(aShape, getBaseHandle(aHandle), isSuperSelected);
}

/**
 * Returns the cursor for given handle.
 */
public Cursor getHandleCursor(T aShape, int aHandle)
{
    // If crosstab parent isn't crosstab frame, return normal tool implementation
    if(!(aShape.getParent() instanceof RMCrossTabFrame))
        return super.getHandleCursor(aShape, aHandle);
    
    // Call base tool implementation with base tool handle
    return super.getHandleCursor(aShape, getBaseHandle(aHandle));    
}

/**
 * Editor method.
 */
public void moveShapeHandle(T aShape, int aHandle, RMPoint aPoint)
{
    // If crosstab parent isn't crosstab frame, return normal tool implementation
    if(!(aShape.getParent() instanceof RMCrossTabFrame))
        super.moveShapeHandle(aShape, aHandle, aPoint);
    
    // Call base tool implementation with base tool handle
    else RMTool.getTool(RMShape.class).moveShapeHandle(aShape, getBaseHandle(aHandle), aPoint);
}

/**
 * Returns the base handle for the crosstab child handle.
 */
private int getBaseHandle(int aHandle)
{
    if(aHandle==0) return HandleSE; // Remap handle 0
    if(aHandle==1) return HandleE; // Remap handle 1
    return HandleS; // Remap handle 2
}

/**
 * Adds a crosstab to the given editor with the given list key.
 */
public static void addCrossTab(RMEditor anEditor, String aKeyPath)
{
    // Create CrossTab frame and set DatasetKey
    RMCrossTabFrame ctab = new RMCrossTabFrame();
    ctab.getTable().setDatasetKey(aKeyPath);

    // Get parent for shape add and set ctab shape location in middle of parent
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    ctab.setXY((parent.getWidth() - ctab.getWidth())/2, (parent.getHeight() - ctab.getHeight())/2);
    
    // Add table, select table, select selectTool and redisplay
    anEditor.undoerSetUndoTitle("Add CrossTab");
    parent.addChild(ctab);
    anEditor.setCurrentToolToSelectTool();
    anEditor.setSelectedShape(ctab);
}

/**
 * Adds a crosstab to the given editor with the given list key.
 */
public static void addCrossTab(RMEditor anEditor)
{
    // Create and configure default table
    RMCrossTab ctab = new RMCrossTab();
    ctab.setRowCount(3); ctab.setColumnCount(3); ctab.setHeaderRowCount(1);
    
    // Get parent for shape add and set ctab shape location in middle of parent
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    ctab.setXY((parent.getWidth() - ctab.getWidth())/2, (parent.getHeight() - ctab.getHeight())/2);
    
    // Add table, select table, select selectTool and redisplay
    anEditor.undoerSetUndoTitle("Add Simple Table");
    parent.addChild(ctab);
    anEditor.setCurrentToolToSelectTool();
    anEditor.setSelectedShape(ctab);
}

}