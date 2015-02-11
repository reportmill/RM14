package snap.swing;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A popup window that has a JList instead of menu items.
 */
public class PopupList<T> extends JPopupMenu implements KeyListener, MouseListener {

    // The component this list works for
    JComponent    _component;

    // The JList
    JList         _jlist;

/**
 * Creates a new popup list.
 */
public PopupList(JComponent aComponent)  { this(aComponent, null); }

/**
 * Creates a new popup list with given items.
 */
public PopupList(JComponent aComponent, T theItems[])
{
    // Set component
    _component = aComponent;
    
    // Reset layout to border layout and set preferred size
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(400,160));
    
    // If items not null, set items
    if(theItems!=null)
        setItems(theItems);
    
    // Create ScrollPane and add to this popup
    JList jlist = getJList();
    JScrollPane scrollPane = new JScrollPane(jlist);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    add(scrollPane);
}

/**
 * Returns the JList.
 */
public JList getJList()  { if(_jlist==null) initJList(_jlist=createJList()); return _jlist; }

/**
 * Creates the JList.
 */
protected JList createJList()  { return new JList(); }

/**
 * Initializes the JList.
 */
protected void initJList(JList aList)
{
    aList.setSelectionForeground(Color.black);
    aList.setSelectionBackground(Color.lightGray);
    aList.setCellRenderer(new PopupListCellRenderer());
    aList.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
    aList.addMouseListener(this);
}

/**
 * Sets items.
 */
public void setItems(T theItems[])
{
    // Set new model for items
    final Object items[] = theItems;
    getJList().setModel(new AbstractListModel() {
        public int getSize() { return items.length; }
        public Object getElementAt(int i) { return items[i]; }
    });
    
    // Reset selection
    if(theItems.length>0)
        getJList().setSelectedIndex(0);
}

/**
 * Returns the currently selected JList item.
 */
public T getSelectedItem()  { return (T)getJList().getSelectedValue(); }

/**
 * Select up.
 */
public void selectUp()
{
    int index = getJList().getSelectedIndex();
    if(index>0) index--;
    getJList().setSelectedIndex(index);
}

/**
 * Select down.
 */
public void selectDown()
{
    int index = getJList().getSelectedIndex();
    if(index<getJList().getModel().getSize()-1) index++;
    getJList().setSelectedIndex(index);
}

/**
 * Returns the text to use for list item.
 */
protected String getItemText(T anItem)  { return anItem.toString(); }

/**
 * Returns the icon to use list item.
 */
protected Icon getItemIcon(T anItem)  { return null; }

/**
 * Called when user hits return or double-clicks.
 */
protected void fireAction(InputEvent anEvent)
{
    setVisible(false);
}

/**
 * A ListCellRenderer for PopupList.
 */
public class PopupListCellRenderer extends DefaultListCellRenderer {

    /** Override to set icon. */
    public Component getListCellRendererComponent(JList list, Object aValue, int index, boolean isSel, boolean isFoc)
    {
        // Get text, do normal version, set icon and return
        T item = (T)aValue;
        String text = getItemText(item);
        super.getListCellRendererComponent(list, text, index, isSel, isFoc);
        Icon icon = getItemIcon(item); if(icon!=null) setIcon(icon);
        return this;
    }
}

/**
 * Override to store requested preferred size.
 */
public void setPreferredSize(Dimension aSize)  { _psize = aSize; super.setPreferredSize(aSize); }  Dimension _psize;

/**
 * Override to resize PopupMenu window if it won't fit on screen.
 */
public void show(Component invoker, int x, int y)
{
    // Adjust size of PopupMenu window to fit on screen
    Dimension size = getScreenSizeAvailable(invoker, x, y);
    size.width = Math.min(size.width, _psize.width); size.height = Math.min(size.height, _psize.height);
    super.setPreferredSize(size);
    
    // Do normal version
    super.show(invoker, x, y);
}

/**
 * Override to add/remove Component KeyListener.
 */
public void setVisible(boolean b)
{
    // Do normal version
    super.setVisible(b);
    
    // Add/Remove KeyListener
    if(b) _component.addKeyListener(this);
    else _component.removeKeyListener(this);
}

/**
 * Sets the size of PopupMenu Window to fit on screen for screen location.
 */
private Dimension getScreenSizeAvailable(Component aComponent, int anX, int aY)
{
    Rectangle rect = getScreenBounds(aComponent, anX, aY, true);
    Point sp = getScreenLocation(aComponent, anX, aY);
    return new Dimension(rect.x + rect.width - sp.x, rect.y + rect.height - sp.y);
}

/**
 * Returns the screen bounds for a component location (or screen location if component null).
 */
private Rectangle getScreenBounds(Component aComponent, int anX, int aY, boolean doInset)
{
    GraphicsConfiguration gc = getGraphicsConfiguration(aComponent, anX, aY);
    Rectangle rect = gc!=null? gc.getBounds() : new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    Insets insets = doInset && gc!=null? Toolkit.getDefaultToolkit().getScreenInsets(gc) : null;
    if(insets!=null)
        rect.setBounds(rect.x + insets.left, rect.y + insets.top,
            rect.width - insets.left - insets.right, rect.height - insets.top - insets.bottom);
    return rect;
}

/**
 * Returns the GraphicsConfiguration for a point.
 */
private GraphicsConfiguration getGraphicsConfiguration(Component aComponent, int anX, int aY)
{
    // Get initial GC from component (if available) and point on screen
    GraphicsConfiguration gc = aComponent!=null? aComponent.getGraphicsConfiguration() : null;
    Point screenPoint = getScreenLocation(aComponent, anX, aY);
    
    // Replace with alternate GraphicsConfiguration if point on another screen
    for(GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        if(gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
            GraphicsConfiguration dgc = gd.getDefaultConfiguration();
            if(dgc.getBounds().contains(screenPoint.x, screenPoint.y)) {
                gc = dgc; break; }
        }
    }
    
    // Return GraphicsConfiguration
    return gc;
}

/**
 * Returns the screen location for a component and X and Y.
 */
private Point getScreenLocation(Component aComponent, int anX, int aY)
{
    Point point = aComponent!=null && aComponent.isShowing()? aComponent.getLocationOnScreen() : new Point();
    point.x += anX; point.y += aY;
    return point;
}

/**
 * KeyListener Method to consume Arrow Up/Down, Enter and Escape.
 */
public void keyTyped(KeyEvent e)
{
    int kc = e.getKeyCode();
    if(kc==KeyEvent.VK_UP || kc==KeyEvent.VK_DOWN || kc==KeyEvent.VK_ENTER || kc==KeyEvent.VK_ESCAPE) e.consume();
}

/**
 * KeyListener Method to consume Arrow Up/Down, Enter and Escape.
 */
public void keyPressed(KeyEvent e)
{
    int kc = e.getKeyCode();
    if(kc==KeyEvent.VK_UP || kc==KeyEvent.VK_DOWN || kc==KeyEvent.VK_ENTER || kc==KeyEvent.VK_ESCAPE) e.consume();
}

/**
 * KeyListener Method to consume Arrow Up/Down, Enter and Escape.
 */
public void keyReleased(KeyEvent e)
{
    if(e.getKeyCode()==KeyEvent.VK_UP) { selectUp(); e.consume(); }
    if(e.getKeyCode()==KeyEvent.VK_DOWN) { selectDown(); e.consume(); }
    if(e.getKeyCode()==KeyEvent.VK_ENTER) { fireAction(e); e.consume(); }
    if(e.getKeyCode()==KeyEvent.VK_ESCAPE) { setVisible(false); e.consume(); }    
}

/**
 * MouseListener methods.
 */
public void mousePressed(MouseEvent e)  { }
public void mouseReleased(MouseEvent e)  { }
public void mouseEntered(MouseEvent e)  { }
public void mouseExited(MouseEvent e)  { }
public void mouseClicked(MouseEvent e)  { if(e.getClickCount()==2) fireAction(e); }

}