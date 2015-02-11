package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import snap.util.ClassUtils;

/**
 * This class is a label subclass with a popup menu attached to implement popup menus.
 * 
 * Copyright (c) 2004 ReportMill Software, Inc. All Rights Reserved. Contact ReportMill <info@reportmill.com>.
 */
public class MenuButton extends JLabel {
    
    // The popup menu
    JPopupMenu    _popupMenu;
    
    // The menu popup point
    Point         _popupPoint;
    
    // The menu popup size
    Dimension     _popupSize;
    
    // The icon for the down arrow
    static Icon   _downArrowIcon;

/**
 * Creates a new menu button.
 */
public MenuButton()
{
    // Menu button has a menu
    _popupMenu = new JPopupMenu();
    
    // Configure default settings for label 
    setIcon(getDownArrowIcon());
    setVerticalTextPosition(JLabel.CENTER);
    setHorizontalTextPosition(JLabel.LEFT);
    
    // Add raised border
    setBorder(BorderFactory.createRaisedBevelBorder());

    // Configure label to show popup
    addMouseListener(new MouseAdapter()
    {
        // Mouse pressed
        public void mousePressed(MouseEvent e)
        {
            // Get location
            int x = _popupPoint==null? 0 : _popupPoint.x;
            int y = _popupPoint==null? e.getComponent().getHeight() : _popupPoint.y;
            
            // Set popup size
            if(_popupSize!=null)
                _popupMenu.setPopupSize(_popupSize);
            
            // Popu menu
            _popupMenu.show(e.getComponent(), x, y);
        }
    });
}

/**
 * Returns the menu button's popup menu.
 */
public JPopupMenu getPopupMenu()  { return _popupMenu; }

/**
 * Sets the contents of the pop-up menu to be the same as the given jmenu
 */
public void setPopupMenu(JMenu aMenu)  { _popupMenu = aMenu.getPopupMenu(); }

/**
 * Returns the point that popup menu pops up at.
 */
public Point getPopupPoint()  { return _popupPoint!=null? new Point(_popupPoint) : null; }

/**
 * Sets the point that popup menu pops up at.
 */
public void setPopupPoint(Point aPoint)  { _popupPoint = new Point(aPoint); }

/**
 * Returns the size that popup menu pops up with.
 */
public Dimension getPopupSize()  { return _popupSize; }

/**
 * Sets the size that popup menu pops up with.
 */
public void setPopupSize(Dimension aSize)  { _popupSize = aSize; }

/**
 * Returns the number of menu items in this button.
 */
public int getItemCount()  { return _popupMenu.getComponentCount(); }

/**
 * Returns the specifc menu item at the given index.
 */
public JMenuItem getItemAt(int anIndex)
{
    return ClassUtils.getInstance(_popupMenu.getComponent(anIndex), JMenuItem.class);
}

/**
 * Adds a menu item for the given name.
 */
public void addItem(String aString, String aName)
{
    // If string is separator, just add a separator
    if(aString.equals("separator"))
        _popupMenu.addSeparator();
        
    // If string is not separator, add new menu item for given name
    else {
        JMenuItem item = new JMenuItem(aString);
        item.setName(aName);
        _popupMenu.add(item);
    }
}

/**
 * Sets the font for the menu button.
 */
public void setFont(Font aFont)
{
    // Do normal set font
    super.setFont(aFont);
    
    // Set font on popup menu
    if(_popupMenu!=null)
        _popupMenu.setFont(aFont);
}

/**
 * Returns an Icon of a down arrow.
 */
public Icon getDownArrowIcon()
{
    // If down arrow icon hasn't been created, create it
    if(_downArrowIcon==null) {
        GeneralPath p = new GeneralPath();
        p.moveTo(1.5f, 1.5f); p.lineTo(5.5f, 8.5f); p.lineTo(9.5f, 1.5f); p.closePath();
        _downArrowIcon = IconUtils.getIcon(p, Color.black, 11, 10);
    }
    
    // Return down arrow icon
    return _downArrowIcon;
}

/**
 * Implements Ribs helpers methods for MenuButton.
 */
public static class Helper <T extends MenuButton> extends SwingHelpers.JLabelHpr <T> {

    /** Returns the MenuButton child component count.  */
    public int getChildCount(T aMenuButton)  { return 1; }
    
    /** Returns the MenuButton child component at the given index. */
    public JComponent getChild(T aMenuButton, int anIndex)  { return aMenuButton.getPopupMenu(); }
}

}