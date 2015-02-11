package snap.swing;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import snap.util.*;

/**
 * This class offers a number of useful general purpose utilities used by ribs classes.
 */
public class SwingUtils {

/**
 * Returns the first parent of given component which is window.
 */
public static Window getWindow(Component aComponent)  { return getParent(aComponent, Window.class); }

/**
 * Returns the first parent of given component which is an instance of given class.
 */
public static <T> T getParent(Component aComponent, Class <T> aClass)
{
    while(aComponent!=null && !aClass.isInstance(aComponent))
        aComponent = aComponent.getParent();
    return (T)aComponent;
}

/**
 * InvokesLater for a given count before running given runnable, in order to skip past events yet to be posted.
 */
public static void invokeLater(final Runnable aRunnable, final int aCount)
{
    if(aCount<=1)
        SwingUtilities.invokeLater(aRunnable);
    else SwingUtilities.invokeLater(new Runnable() { public void run() {
        invokeLater(aRunnable, aCount-1); }});
}

/**
 * Invokes the given runnable for name once (cancels unexecuted previous invokeLater registered with same name).
 */
public static synchronized void invokeLaterOnce(String aName, Runnable aRunnable)
{
    RunLaterRunnable runnable = (RunLaterRunnable)_runOnceMap.get(aName);
    if(runnable==null) {
        _runOnceMap.put(aName, runnable = new RunLaterRunnable(aName, aRunnable));
        SwingUtilities.invokeLater(runnable);
    }
    else runnable._runnable = aRunnable;
}

/**
 * A wrapper Runnable for RunLaterOnce. 
 */
private static class RunLaterRunnable implements Runnable {
    String _name; Runnable _runnable;
    RunLaterRunnable(String aName, Runnable aRunnable)  { _name = aName; _runnable = aRunnable; }
    public void run()
    {
        Runnable runnable;
        synchronized (this) { _runOnceMap.remove(_name); runnable = _runnable; }
        if(runnable!=null) runnable.run();
    }
}

// The RunOnce runnables map
private static Map <String,Runnable> _runOnceMap = new HashMap();

/**
 * Returns an icon for the given shape, color and size.
 */
public static Icon getIcon(Shape aShape, Color aColor, int aWidth, int aHeight)
{
    return IconUtils.getIcon(aShape, aColor, aWidth, aHeight);
}

/**
 * Returns an image icon for the given shape, color and size.
 */
public static ImageIcon getImageIcon(Shape aShape, Color aColor, int aWidth, int aHeight)
{
    return IconUtils.getImageIcon(aShape, aColor, aWidth, aHeight);
}

/**
 * Returns the default focus component.
 */
public static JComponent getDefaultFocusComponent(Component aComponent)
{
    // Get JComponent
    JComponent component = ClassUtils.getInstance(aComponent, JComponent.class);
    if(component instanceof JScrollPane)
        component = ClassUtils.getInstance(((JScrollPane)component).getViewport().getView(), JComponent.class);
    if(component==null)
        return null;
    
    // If child components, iterate over them and give them first shot
    for(int i=0, iMax=component.getComponentCount(); i<iMax; i++) {
        JComponent comp = getDefaultFocusComponent(component.getComponent(i));
        if(comp!=null)
            return comp;
    }
    
    // If component has focus traversal policy, return default component
    if(component.getFocusTraversalPolicy()!=null)
        return ClassUtils.getInstance(component.getFocusTraversalPolicy().getDefaultComponent(component),
            JComponent.class);
    
    // If component has request focus enabled, return it
    if(component.isRequestFocusEnabled() && component.isFocusable())
        return component;
    
    // Return null
    return null;
}

/**
 * Creates a JMenuItem for name and text and key accelerator description.
 */
public static JMenuItem createMenuItem(String aName, String theText, String aKey)
{
    JMenuItem mi = new JMenuItem(theText); mi.setName(aName);
    if(aKey!=null) mi.setAccelerator(getKeyStroke(aKey));
    return mi;
}


/**
 * Creates a JMenu for name and text.
 */
public static JMenu createMenu(String aName, String theText)
{
    JMenu menu = new JMenu(theText); menu.setName(aName); return menu;
}

/**
 * This utility method returns key text for a key stroke and tries to make it more conforming.
 */
public static String getKeyText(KeyStroke aKeyStroke)
{
    // Get normal key text
    String keyText = KeyEvent.getKeyText(aKeyStroke.getKeyCode());
    
    // Do some substitutions
    keyText = StringUtils.replace(keyText, "Semicolon", ";");
    keyText = StringUtils.replace(keyText, "Back Slash", "\\");
    keyText = StringUtils.replace(keyText, "Open Bracket", "[");
    keyText = StringUtils.replace(keyText, "Close Bracket", "]");
    
    // Return key text
    return keyText;
}

/**
 * This utility method tries to get a keystroke from a string and tries to be more forgiving than
 * KeyStroke.getKeyStroke().
 */
public static KeyStroke getKeyStroke(String aString)
{
    // If Windows, convert "meta" to "control"
    String string = aString;
    if(SnapUtils.isWindows) string = StringUtils.replace(string, "meta", "control");
    
    // Try normal KeyStroke method
    KeyStroke kstroke = KeyStroke.getKeyStroke(string);
    if(kstroke!=null)
        return kstroke;
    
    // Do some common substitutions
    string = StringUtils.replace(string, ";", "SEMICOLON");
    string = StringUtils.replace(string, "\\", "BACK_SLASH");
    string = StringUtils.replace(string, "[", "OPEN_BRACKET");
    string = StringUtils.replace(string, "]", "CLOSE_BRACKET");
    
    // Get last component and make sure its in upper case
    int index = string.lastIndexOf(" ") + 1;
    string = string.substring(0, index) + string.substring(index).toUpperCase();

    // Try again
    kstroke = KeyStroke.getKeyStroke(string);
    if(kstroke==null) System.err.println("JMenuItemHpr:fromXML: Invalid key accelerator format: " + aString);
    return kstroke;
}

/**
 * Adds a column for header, bind key, width.
 */
public static TableColumn addTableColumn(JTable aTable, String aHeader, String aBindKey, int anIndex, int aWidth)
{
    // Create table column and configure Header, BindKey (Identifier), ModelIndex, Width
    TableColumn column = new TableColumn();
    column.setHeaderValue(aHeader);
    column.setIdentifier(aBindKey);
    column.setModelIndex(anIndex);
    if(aWidth>0) column.setWidth(aWidth);
    
    // Add table column, turn off auto create columns from model (for good measure) and return
    aTable.addColumn(column);
    aTable.setAutoCreateColumnsFromModel(false);
    return column;
}

/**
 * Adds columns for a list of keys.
 */
public static void addTableColumns(JTable aTable, List <String> theKeys)
{
    // Iterate over properties and add columns (If column is relation or private, continue)
    for(String key : theKeys)
        addTableColumn(aTable, key, key, aTable.getColumnCount(), 75);
}

/**
 * Scrolls a table so that given row and column are visible.
 */
public static void scrollTableCellToVisible(JTable aTable, int aRow, int aColumn)
{
    int row = aRow>=0? aRow : aTable.getSelectedRow();
    int column = aColumn>=0? aColumn : aTable.getSelectedColumn();
    Rectangle rect = aTable.getCellRect(row, column, false); //Rectangle rect2 = aTable.getVisibleRect();
    aTable.scrollRectToVisible(rect);
}

/**
 * Sets table column widths to preferred size.
 */
public static void setTableColumnWidths(JTable aTable)
{
    TableModel model = aTable.getModel();
    
    for(int i=0; i<aTable.getColumnCount(); i++) {
        TableColumn column = aTable.getColumnModel().getColumn(i);
        Component comp = aTable.getTableHeader().getDefaultRenderer().
            getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
        int headerWidth = comp.getPreferredSize().width;
        
        int cellWidth = 0;
        int sampleRows = Math.min(model.getRowCount(),50);
        for(int j=0; j<sampleRows; j++) {
            comp = aTable.getDefaultRenderer(model.getColumnClass(i)).
                getTableCellRendererComponent(aTable, model.getValueAt(j,i), false, false, 0, i);
            cellWidth = (Math.max(cellWidth, comp.getPreferredSize().width));
        }
        
        //The following set width statements were required to make the table 
        //repaint columns properly after rows are added. Do not know why!
        //int prefWidth = column.getPreferredWidth();
        column.setPreferredWidth(Math.max(headerWidth, cellWidth + 10)+6);
    }
} 

/**
 * If expand is true, expands all nodes in the tree. Otherwise, collapses all nodes in the tree.
 */
public static void expandTreeAll(JTree aTree, boolean expand)
{
    // Get root and traverse tree from there
    Object root = aTree.getModel().getRoot();
    expandTreeAll(aTree, new TreePath(root), expand);
}

/**
 * Recursively expands or collapses tree nodes.
 */
private static void expandTreeAll(JTree aTree, TreePath aTreePath, boolean expand)
{
    // Traverse children
    Object node = aTreePath.getLastPathComponent();
    
    // Iterate over node children 
    for(int i=0; i<aTree.getModel().getChildCount(node); i++) {
        Object n = aTree.getModel().getChild(node, i);
        TreePath path = aTreePath.pathByAddingChild(n);
        expandTreeAll(aTree, path, expand);
    }
    
    // Expansion or collapse must be done bottom-up
    if(expand)
        aTree.expandPath(aTreePath);
    else aTree.collapsePath(aTreePath);
}

/**
 * Reloads tree.
 */
public static void reloadTree(JTree aTree, boolean doPreserveSelection, boolean doPreserveExpanded)
{
    // Get tree model and remove tree listener
    TreeModel model = aTree.getModel(); //aTree.removeTreeSelectionListener(_treeSelectionListener);
    
    // Get expanded paths
    Enumeration ep = doPreserveExpanded? aTree.getExpandedDescendants(new TreePath(model.getRoot())) : null;
    List <TreePath> expandedPaths = ep!=null? Collections.list(ep) : null;
    
    // Get selected paths
    TreePath selectedPaths[] = doPreserveSelection? aTree.getSelectionPaths() : null;
    
    // Clear and reset model
    aTree.setModel(null); aTree.setModel(model);
    
    // Restore expanded paths
    if(expandedPaths!=null)
        for(TreePath treePath : expandedPaths)
            aTree.expandPath(treePath);
    
    // Restore selected paths and add tree listener
    if(selectedPaths!=null) aTree.setSelectionPaths(selectedPaths); //aTree.addTreeSelectionListener(_treeSelLsnr);
}

}