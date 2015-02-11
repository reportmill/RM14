package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import snap.swing.*;
import snap.web.*;

/**
 * This class shows the current set of keys relative to the current editor selection in a browser and lets users
 * drag and drop them to the editor.
 */
public class KeysBrowser extends KeysBrowserBase {
    
    // The entity the keys browser should show
    Entity                   _entity;
    
    // The icon for a "to-many" branch
    static Icon              _doubleArrowIcon;
    
    // The table cell renderer
    TableCellRenderer        _tableCellRenderer;

    // The Drag and Drop key, since Java 1.4 doesn't support getting transferable in dragEnter/dragOver.
    String                   _dragKey;

    // Contants aggregate keys
    static final String      _aggregateKeys[] = { "total", "average", "count", "countDeep", "max", "min" };
    
    // Constants for heritage keys
    static final String      _heritageKeys[] = { "Running", "Remaining", "Up" };
    
    // Constants for built-in keys
    static final String      _builtInKeys[] = { "Date", "Page", "PageMax", "Page of PageMax",
        "PageBreak", "PageBreakMax", "PageBreakPage", "PageBreakPageMax", "Row" };

/**
 * Returns the entity.
 */
public Entity getEntity()  { return _entity; }

/**
 * Sets the entity.
 */
public void setEntity(Entity anEntity)
{
    if(RMUtils.equals(anEntity, _entity) && getModel()!=null) return; // Return if already set
    _entity = anEntity; // Set entity
    setModel(new KeysBrowserModel()); // Reset model
}

/**
 * Returns the current drag key.
 */
public String getDragKey()  { return _dragKey; }

/**
 * Returns the current key path selected by the browser.
 */
public String getKeyPath()
{
    String key = getPath(); // Get normal path
    if(key.equals("Page of PageMax")) return "@Page@ of @PageMax@";  // Special case for Page of PageMax
    return "@" + key + "@"; // Return path with @ signs
}
  
/**
 * Returns whether selected item is to-many.
 */
public boolean isSelectedToMany()
{
    KeysBrowserNode node = (KeysBrowserNode)getSelectedItem();
    return node!=null && node._isToMany;
}

/**
 * Override to use node font.
 */
protected TableCellRenderer createTableCellRenderer()
{
    // Create new table cell renderer class
    return new DefaultTableCellRenderer() {
        
        // Override to render table cells
        public Component getTableCellRendererComponent(JTable aTable, Object anObj, boolean isSel, boolean hasFoc, int row, int col)
        {
            KeysBrowserNode node = (KeysBrowserNode)aTable.getValueAt(row, 0);
            JLabel label = (JLabel)super.getTableCellRendererComponent(aTable, node, isSel, hasFoc, row, col);
            label.setFont(node._font);
            label.setToolTipText(node.toString());
            return label;
        }
    };
}

/**
 * Override to return DoubleArrowIcon for Node.IsToMany.
 */
public Icon getBranchIcon(Object anObj)
{
    KeysBrowserNode node = (KeysBrowserNode)anObj; if(node==null || node._isLeaf) return null;
    if(node._isToMany) return getDoubleArrowIcon();
    return super.getBranchIcon(anObj);    
}

/**
 * Returns the icon of a double right arrow to indicate branch nodes of a "to-many" relationship in a browser.
 */
public Icon getDoubleArrowIcon()
{
    // If double arrow icon hasn't been created, create it
    if(_doubleArrowIcon==null) {
        GeneralPath path = new GeneralPath();
        path.moveTo(1.5f, 1.5f); path.lineTo(8.5f, 5.5f); path.lineTo(1.5f, 9.5f); path.closePath();
        path.moveTo(9.5f, 1.5f); path.lineTo(16.5f, 5.5f); path.lineTo(9.5f, 9.5f); path.closePath();
        _doubleArrowIcon = SwingUtils.getImageIcon(path, Color.black, 18, 11);
    }
    
    // Return double arrow icon
    return _doubleArrowIcon;
}

/**
 * Override to add DragSource to column.
 */
public JTable createColumnTable()
{
    JTable table = super.createColumnTable();
    DragSource dds = DragSource.getDefaultDragSource();
    dds.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_COPY_OR_MOVE, new GListener());
    return table;
}

/**
 * An inner class to provide data for keys browser.
 */
class KeysBrowserModel implements BrowserModel <KeysBrowserNode> {
    
    // The root list of browser nodes
    KeysBrowserNode     _root;
    
    /** Creates a new browser model. */
    public KeysBrowserModel()
    {
        // Create new root
        _root = new KeysBrowserNode(null, "Root", null); _root._isLeaf = false;
        
        // If datasource is null, get built-in keys
        if(getEntity()==null) {
            _root._children = new ArrayList(_builtInKeys.length);
            for(int i=0; i<_builtInKeys.length; ++i) {
                KeysBrowserNode node = new KeysBrowserNode(_root, _builtInKeys[i], null);
                node._isLeaf = true;
                _root._children.add(node);
            }
        }
    }
    
    /** Returns the root of the browser model. */
    public Object getRoot()  { return _root; }
    
    /** Returns whether given browser node is a leaf. */
    public boolean isLeaf(KeysBrowserNode aNode)  { return aNode==null || aNode._isLeaf; }

    /** Returns the child count for the given browser node.  */
    public int getChildCount(KeysBrowserNode aNode)  { return aNode.getChildCount(); }
    
    /** Returns the specific child of the given browser node at the given index.  */
    public Object getChild(KeysBrowserNode aNode, int anIndex)  { return aNode.getChildren().get(anIndex); }
}

/**
 * An inner class for Node of KeysBrowser.
 */
private class KeysBrowserNode extends Object {

    // Node parent
    KeysBrowserNode   _parent;

    // Node name
    String            _name;
    
    // The Property (if based on one)
    Property          _prop;
    
    // Node children
    List              _children;
    
    // Whether node is leaf
    boolean           _isLeaf;
    
    // Whether node is "to-many"
    boolean           _isToMany;
    
    // Node font
    Font              _font = RMAWTUtils.Helvetica11;
    
    /** Creates a new KeysBrowserNode for given parent, name and optional property. */
    public KeysBrowserNode(KeysBrowserNode aParent, String aName, Property aProperty)
    {
        _name = aName; _parent = aParent; _prop = aProperty;
    }
    
    /** Returns the node parent. */ //public KeysBrowserNode getParent()  { return _parent; }
    /** Returns the node property (if based on one). */ //public Property getProperty()  { return _prop; }
    
    /** Returns the node name. */
    public String getName()  { return _name; }
    
    /** Returns the number of children. */
    public int getChildCount()  { return _isLeaf? 0 : getChildren().size(); }
    
    /** Returns the list of children for this node. */
    public List <KeysBrowserNode> getChildren()  { return _children!=null? _children : (_children=createChildren()); }
    
    /** Creates the list of children for this node. */
    protected List <KeysBrowserNode> createChildren()
    {
        // Create children list and get entity for node
        List children = new ArrayList();
        Entity entity = getEntity(); if(entity==null) return children;
        
        // Add attributes
        for(int i=0, iMax=entity.getAttributeCount(); i<iMax; i++) {
            Property attr = entity.getAttributeSorted(i); if(attr.isPrivate()) continue;
            KeysBrowserNode child = new KeysBrowserNode(this, attr.getName(), attr); child._isLeaf = true;
            children.add(child);
        }
        
        // Add relations
        for(int i=0, iMax=entity.getRelationCount(); i<iMax; i++) {
            Property rel = entity.getRelationSorted(i); if(rel.isPrivate()) continue;
            KeysBrowserNode child = new KeysBrowserNode(this, rel.getName(), rel);
            child._isToMany = rel.isToMany();
            children.add(child);
        }
        
        // Add aggregate keys
        if(getShowAggregates())
            for(int i=0; i<_aggregateKeys.length; i++) {
                KeysBrowserNode child = new KeysBrowserNode(this, _aggregateKeys[i], null);
                child._font = RMAWTUtils.HelveticaBold11;
                children.add(child);
            }
        
        // If Root, add heritage keys
        if(_parent==null)
            for(int i=0; i<_heritageKeys.length; i++) {
                KeysBrowserNode child = new KeysBrowserNode(this, _heritageKeys[i], null);
                child._font = RMAWTUtils.HelveticaBold11;
                children.add(child);
            }
        
        // Return list of child nodes
        return children;
    }
    
    /** Returns whether node should have aggregates. */
    protected boolean getShowAggregates()
    {
        if(_parent==null) return true;
        if(RMArrayUtils.contains(_heritageKeys, getName())) return true;
        if(_prop!=null && _prop.isRelation() && _prop.isToMany()) return true;
        return false;
    }
    
    /** Returns the entity. */
    protected Entity getEntity()
    {
        if(_parent==null) return KeysBrowser.this.getEntity();
        if(_prop!=null) return _prop.getRelationEntity();
        return _parent.getEntity();
    }
    
    // Returns node as string
    public String toString()  { return _name; }
}

/**
 * A DragGestureListener to start dragging on KeysBrowser column.
 */
private class GListener implements DragGestureListener {

    /** Creates new GListener. */
    public void dragGestureRecognized(DragGestureEvent dge)
    {
        // Set the drag key
        _dragKey = getPath();
        
        // Get drag key with @-signs
        String dragKeyFull = getKeyPath();
        
        // Create a drag source listener to clear drag key
        DragSourceListener dsl = new DragSourceAdapter() {
            public void dragDropEnd(DragSourceDropEvent dsde) { _dragKey = null; }};
            
        // Get Dragger, set key and start drag
        SwingDragger dragger = new SwingDragger(); dragger.setDragGestureEvent(dge);
        dragger.setDragItem(dragKeyFull); dragger.setDragImageFromString(dragKeyFull, RMFont.getDefaultFont().awt());
        dragger.setDragSourceListener(dsl);
        dragger.startDrag();
    }
}

}