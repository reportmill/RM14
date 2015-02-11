package snap.swing;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A browser class.
 */
public class BrowserView <T> extends Box {

    // The browser model
    BrowserModel        _model;
    
    // The minimum column width
    int                 _minColWidth = 200;

/**
 * The browser model interface.
 */
public interface BrowserModel <T> {

    // The browser root object
    public T getRoot();
    
    // Whether given object is a parent (can have children)
    public boolean isParent(T anItem);
    
    // The number of children in given parent
    public int getChildCount(T aParent);
    
    // The child at given index in given parent
    public T getChild(T aParent, int anIndex);
    
    // The name of given item
    public String getName(T anItem);
    
    // The icon of given item
    public Icon getIcon(T anItem);
}

/**
 * Creates a new BrowserView.
 */
public BrowserView()  { super(BoxLayout.X_AXIS); }

/**
 * Creates a new BrowserView with given root.
 */
public BrowserView(BrowserModel aModel)  { this(); setModel(aModel); }

/**
 * Returns the browser model.
 */
public BrowserModel getModel()  { return _model; }

/**
 * Sets the browser model.
 */
public void setModel(BrowserModel aModel)
{
    // Set model
    _model = aModel;
    
    // Remove browser columns and reset
    removeAll();
    if(aModel!=null && aModel.getRoot()!=null)
        add(createBrowserColumn((T)aModel.getRoot()));
    revalidate(); repaint();
}

/**
 * Return the root object.
 */
public T getRoot()  { return (T)getModel().getRoot(); }

/**
 * Returns the browser column list at given index.
 */
public BrowserColumn getColumn(int anIndex)
{
    return anIndex<getComponentCount()? (BrowserColumn)getComponent(anIndex) : null;
}

/**
 * Returns the currently selected column.
 */
public BrowserColumn getSelectedColumn()
{
    int columnIndex = getComponentCount() - 1;
    while(columnIndex>=0) {
        BrowserColumn column = getColumn(columnIndex--);
        if(column.getList().getSelectedItem()!=null)
            return column;
    }
    return null;
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()
{
    List <T> items = getSelectedItems();
    return items.size()>0? items.get(0) : null;
}

/**
 * Returns selected items.
 */
public List <T> getSelectedItems()
{
    BrowserColumn column = getSelectedColumn();
    return column!=null? column.getSelectedItems() : (List)Collections.emptyList();
}

/**
 * Returns the minimum column width.
 */
public int getMinColumnWidth()  { return _minColWidth; }

/**
 * Sets the minimum column width.
 */
public void setMinColumnWidth(int aWidth)  { _minColWidth = aWidth; }

/**
 * Creates a browser column for given item.
 */
protected BrowserColumn createBrowserColumn(T anItem)  { return new BrowserColumn(anItem); }

/**
 * A browser column.
 */
public class BrowserColumn extends JScrollPane implements ListSelectionListener {

    // The root item for this column
    T                  _item;

    // The index of this browser column
    int                _index;
    
    // The browser column list for this column
    BrowserColumnList  _columnList;

    /** Creates a new BrowserColumn. */
    public BrowserColumn(T anItem)
    {
        _item = anItem;
        setViewportView(getList());
        setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setInheritsPopupMenu(true);
    }
    
    /** Returns the root item for browser column. */
    public T getRoot()  { return _item; }
    
    /** Returns the column list. */
    public BrowserColumnList getList()
    {
        if(_columnList==null) {
            _columnList = new BrowserColumnList(_item);
            _columnList.addListSelectionListener(this);
        }
        return _columnList;
    }
    
    /** Returns the selected item. */
    public T getSelectedItem()  { return getList().getSelectedItem(); }
    
    /** Returns the selected items. */
    public List <T> getSelectedItems()  { return getList().getSelectedItems(); }
    
    /** Respond to selection. */
    public void valueChanged(ListSelectionEvent anEvent)
    {
        if(anEvent.getValueIsAdjusting()) return;
        while(getColumn(_index+1)!=null) {
            getParent().remove(_index+1);
            ((JComponent)getParent()).revalidate(); getParent().repaint();
        }
        T item = getSelectedItem(); if(item==null) return;
        if(getModel().isParent(item)) {
            BrowserColumn bcol = createBrowserColumn(item); bcol._index = getParent().getComponentCount();
            getParent().add(bcol);
            ((JComponent)getParent()).revalidate(); getParent().repaint();
        }
    }
    
    /** Implement to reflect size of list. */
    public Dimension getMinimumSize()
    {
        Dimension dim = super.getMinimumSize(); dim.width = Math.max(getMinColumnWidth(), dim.width+20); return dim;
    }
    
    /** Implement to reflect size of list. */
    public Dimension getPreferredSize()
    {
        Dimension dim = super.getPreferredSize(); dim.width = Math.max(getMinColumnWidth(), dim.width+20); return dim;
    }
}

/**
 * A JList for browser column.
 */
protected class BrowserColumnList extends JList {

    // The root item for this column
    T         _item;
    
    /** Creates a new BrowserColumnList for given item. */
    public BrowserColumnList(T anItem)
    {
        _item = anItem;
        setModel(new Model());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellRenderer(new BrowserListCellRenderer());
        setInheritsPopupMenu(true);
    }
    
    /** Returns the currently selected item. */
    public T getSelectedItem()  { return (T)getSelectedValue(); }
    
    /** Returns the currently selected items. */
    public List <T> getSelectedItems()
    {
        List items = new ArrayList(); T item = getSelectedItem(); if(item!=null) items.add(item); return items;
    }    
    
    /** Override to forward mouse events to browser so anything listening to MouseEvents will see internal events. */
    protected void processEvent(AWTEvent e)
    {
        // Do normal version
        super.processEvent(e);
        
        // If mouse event, send to browser so anyone listening to MouseEvents will see internal events
        if(e instanceof MouseEvent) {
            MouseEvent me = SwingUtilities.convertMouseEvent(this, (MouseEvent)e, BrowserView.this);
            BrowserView.this.processEvent(me);
        }
    }
    
    /** Reloads List. */
    public void reload()
    {
        int indexes[] = getSelectedIndices();
        Model model = (Model)getModel(); model.fireContentsChanged(this, 0, Integer.MAX_VALUE);
        clearSelection();
        setSelectedIndices(indexes);
    }

    /** BrowserColumnListModel. */
    protected class Model extends AbstractListModel {
        BrowserModel bm = BrowserView.this.getModel();
        public int getSize()  { return bm.isParent(_item)? bm.getChildCount(_item) : 0; }
        public Object getElementAt(int anIndex)  { return bm.getChild(_item, anIndex); }
        public void fireContentsChanged(Object src, int ind0, int ind1) { super.fireContentsChanged(src, ind0, ind1); }
    }
}

/**
 * A Cell renderer for browser column list featuring two labels to show name, item icon and branch icon (if parent).
 */
protected class BrowserListCellRenderer extends JPanel implements ListCellRenderer {

    DefaultListCellRenderer  _lcr1 = new DefaultListCellRenderer(), _lcr2 = new DefaultListCellRenderer();

    /** Create new BrowserListCellRenderer. */
    public BrowserListCellRenderer()
    {
        super(new BorderLayout(0,0));
        setOpaque(false);
        _lcr2.setHorizontalTextPosition(SwingConstants.RIGHT);
        add(_lcr1);
        add(_lcr2, BorderLayout.EAST);
    }

    /** Configure renderer. */
    public Component getListCellRendererComponent(JList aList, Object aVal, int anIndex, boolean isSel, boolean isFoc)
    {
        String name = getModel().getName(aVal);
        Icon icon = getModel().getIcon(aVal);
        boolean isDirectory = getModel().isParent(aVal);
        _lcr1.getListCellRendererComponent(aList, name, anIndex, isSel, isFoc);
        _lcr1.setIcon(icon);
        _lcr2.getListCellRendererComponent(aList, isDirectory? getBranchIcon() : null, anIndex, isSel, isFoc);
        revalidate();
        return this;
    }
    
    /** Returns the icon to indicate branch nodes in a browser (right arrow by default). */
    public Icon getBranchIcon()
    {
        // If branch icon hasn't been created, create it
        if(_branchIcon==null) {
            GeneralPath path = new GeneralPath();
            path.moveTo(1.5f, 1.5f); path.lineTo(8.5f, 5.5f); path.lineTo(1.5f, 9.5f); path.closePath();
            _branchIcon = SwingUtils.getImageIcon(path, Color.black, 14, 11);
        }
        
        // Return right arrow icon
        return _branchIcon;
    } Icon _branchIcon;
}

}