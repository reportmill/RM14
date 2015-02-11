package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import snap.util.PrefsUtils;

/**
 * This class is a label subclass with a popup menu attached to implement popup menus.
 */
public class ColorPickerButton extends JButton {

    // The color picker button's color
    Color         _color;
    
    // The color picker button's title shown at top of popup menu
    String        _title;
    
    // Whether to save color to preferences
    boolean       _saveColor;

    // The base icon version
    Icon          _icon;
    
    // The popup menu
    JPopupMenu    _popupMenu;
    
    // The icon for the down arrow
    static Icon   _downArrowIcon;

/**
 * Creates a new menu button.
 */
public ColorPickerButton()
{
    // Configure default settings for label 
    setIcon(getDownArrowIcon());
    setVerticalTextPosition(JLabel.CENTER);
    setHorizontalTextPosition(JLabel.LEFT);
    
    // Turn off border
    setBorderPainted(false);
    setContentAreaFilled(false);
}

/**
 * Returns the color picker button's color.
 */
public Color getColor()  { return _color; }

/**
 * Sets the color picker button's color.
 */
public void setColor(Color aColor)
{
    // Set color
    _color = aColor;
    
    // Save color to prefs
    if(getSaveColor())
        PrefsUtils.prefsPut(_title, _color==null? null : AWTUtils.toStringColor(_color));
    
    // Have color picker button send event
    Swing.sendEvent(new ChangeEvent(this));
}

/**
 * Returns the title shown at top of popup menu.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title shown at top of the popup menu.
 */
public void setTitle(String aValue)  { _title = aValue; }

/**
 * Returns whether to save color to preferences.
 */
public boolean getSaveColor()  { return _saveColor; }

/**
 * Sets wether to save color to preferences.
 */
public void setSaveColor(boolean doSave)
{
    // Set save color
    _saveColor = doSave;
    
    // If do save, get color from preferences
    if(doSave && _title!=null) {
        
        // See if color string has been set in preferences
        String colorString = PrefsUtils.prefs().get(_title, null);
        
        // If color string is found, set color
        if(colorString!=null)
            _color = AWTUtils.fromStringColor(colorString);
    }
}

/**
 * Override button version to trigger popup menu if on right border.
 */
protected void processMouseEvent(MouseEvent anEvent)
{
    // If mouse press is on right border, trigger popup
    if(anEvent.getID()==MouseEvent.MOUSE_PRESSED && anEvent.getX()>=getWidth()-10) {
        setSelected(true);
        getPopupMenu().show(anEvent.getComponent(), 0, anEvent.getComponent().getHeight());
    }
    
    // Otherwise do normal version
    else super.processMouseEvent(anEvent);
}

/**
 * Returns the menu button's popup menu.
 */
public JPopupMenu getPopupMenu()
{
    // If popup menu is null, create it
    if(_popupMenu==null) {
        
        // Create popup menu for this color picker button
        _popupMenu = new CPBPopupMenu(this);
        
        // Add title menu item (and separator)
        _popupMenu.add(new JMenuItem(_title));
        _popupMenu.add(new JPopupMenu.Separator());
        
        // Add color swatch buttons (and separator)
        _popupMenu.add(getColorButtons());
        _popupMenu.add(new JPopupMenu.Separator());
        
        // Create "None" menuitem and add action listener to reset color
        JMenuItem noneMenuItem = new JMenuItem("None");
        noneMenuItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            ColorPickerButton.this.setColor(null); }});
        
        // Add None menu item
        _popupMenu.add(noneMenuItem);
        
        // Create More menu item and add action listener to make color panel visible
        JMenuItem moreMenuItem = new JMenuItem("More...");
        moreMenuItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            ColorPanel.getShared().setWindowVisible(true); }});
        
        // Add More menu item
        _popupMenu.add(moreMenuItem);
        
        // Resize to preferred size
        _popupMenu.setPopupSize(_popupMenu.getPreferredSize());
        
        // Turn off focus support
        _popupMenu.setFocusable(false);
    }
    
    // Return popup menu
    return _popupMenu;
}

/**
 * Overridden to take a base icon and set derived version with popup arrow. Also derives other icon versions.
 */
public void setIcon(Icon anIcon)
{
    // Save base icon
    _icon = anIcon;
    
    // Set CPBIcon
    super.setIcon(new CPBIcon(null, null));

    // Add rollover icon as base plus black line border
    setRolloverIcon(new CPBIcon(Color.black, null));
    
    // Add pressed icon as base plus light gray background
    setPressedIcon(new CPBIcon(Color.gray, Color.lightGray));

    // Set selected icon
    setSelectedIcon(new CPBIcon(Color.gray, Color.lightGray));
    
    // Set rollover selected
    setRolloverSelectedIcon(new CPBIcon(Color.black, Color.lightGray));
}

/**
 * Returns the real icon.
 */
public Icon getRealIcon()  { return _icon; }

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
 * Returns a panel with color buttons in it.
 */
public JPanel getColorButtons()
{
    // Create panel with 5x8 grid layout
    JPanel panel = new JPanel(new GridLayout(5, 8));
    
    // Get colors
    Color colors[] = {
            Color.black, new Color(158, 61, 12), new Color(61, 61, 12), new Color(12, 61, 12),
            new Color(12, 61, 109), new Color(12, 12, 134), new Color(58, 58, 155), new Color(61, 61, 61),
            new Color(134, 12, 12), new Color(255, 109, 12), new Color(134, 134, 12), new Color(12, 134, 12),
            new Color(12, 134, 134), new Color(9, 9, 252), new Color(105, 105, 154), new Color(134, 134, 134),
            new Color(255, 12, 12), new Color(255, 158, 12), new Color(158, 196, 12), new Color(61, 158, 109),
            new Color(61, 206, 206), new Color(61, 109, 255), new Color(129, 8, 129), new Color(155, 155, 155),
            new Color(255, 12, 255), new Color(255, 206, 12), new Color(255, 255, 12), new Color(12, 255, 12),
            new Color(4, 247, 247), new Color(12, 206, 255), new Color(158, 61, 109), new Color(195, 195, 195),
            new Color(255, 158, 206), new Color(255, 206, 158), new Color(255, 255, 158), new Color(206, 255, 206),
            new Color(205, 254, 254), new Color(158, 206, 255), new Color(206, 158, 255), new Color(255, 255, 255),
    };
    
    // Create rectangle for icons
    Rectangle rect = new Rectangle(0, 0, 12, 12);
    
    // Add buttons
    for(int i=0; i<colors.length; i++) {
        JButton button = new ColorSwatchButton(colors[i], rect);
        panel.add(button);
    }
    
    // Set panel size to preferred size
    panel.setSize(panel.getPreferredSize());
    
    // Return panel
    return panel;
}

/**
 * Returns an Icon of a down arrow.
 */
public Icon getDownArrowIcon()
{
    // If down arrow icon hasn't been created, create it
    if(_downArrowIcon==null) {
        GeneralPath p = new GeneralPath();
        p.moveTo(2.5f, 1f);
        p.lineTo(5.5f, 6f);
        p.lineTo(8.5f, 1f);
        p.closePath();
        _downArrowIcon = new IconUtils.ShapeIcon(p, Color.black, 11, 10);
    }
    
    // Return down arrow icon
    return _downArrowIcon;
}

/**
 * A custom subclass of JPopupMenu to reference the color picker button.
 */
private static class CPBPopupMenu extends JPopupMenu {
    
    // The color picker button
    ColorPickerButton _button;
    
    /** Creates a new CPBPopupMenu with given color picker button. */
    public CPBPopupMenu(ColorPickerButton aButton) { _button = aButton; }
    
    /** Returns the color picker button. */
    public ColorPickerButton getButton() { return _button; }
    
    /** De-selects color picker button. */
    public void setVisible(boolean visible)
    {
        // If turning not-visible, de-select color picker button
        if(!visible) {
            _button.setSelected(false);
            _button._popupMenu = null; // Release menu, in case menu made invisible by event other than click on swatch
        }
        
        // Do normal set visible
        super.setVisible(visible);
    }
}

/**
 * A custom JButton subclass to represent a color swatch.
 */
private static class ColorSwatchButton extends JButton {

    // The color for this button
    Color    _color;
    
    /** Creates a new color swatch button. */
    public ColorSwatchButton(Color aColor, Rectangle aRect)
    {
        // Set color
        _color = aColor;
        
        // Set icon for color
        Icon icon = new IconUtils.ShapeIcon(aRect, _color, 12, 12);
        icon = new IconUtils.BorderIcon(Color.gray, icon, 12, 12);
        icon = new IconUtils.SpacerIcon(icon, 18, 18);
        setIcon(icon);
        
        // Set rollover icon
        setRolloverIcon(new IconUtils.BorderIcon(icon, 18, 18));
        
        // Turn off border, margin and background painting
        setBorder(null);
        setBorderPainted(false);
        setMargin(new Insets(0,0,0,0));
        setContentAreaFilled(false);
        
        // Add listener
        addActionListener(_actionListener);
        
        // Resize to 20x20
        setPreferredSize(new Dimension(20, 20));
    }
}

/**
 * A private shared ActionListener implementation for controls to send action on click.
 */
private static ActionListener _actionListener = new ActionListener() {
    
    /** Custom action performed to set color picker button color and fire rib action. */
    public void actionPerformed(ActionEvent e) {
        
        // Get swatch button, and its parent popup menu
        ColorSwatchButton button = (ColorSwatchButton)e.getSource();
        CPBPopupMenu popupMenu = (CPBPopupMenu)button.getParent().getParent();
        
        // Order popup menu offscreen
        popupMenu.setVisible(false);
        
        // Get color picker button
        ColorPickerButton colorPickerButton = popupMenu.getButton();
        
        // Set color picker button
        colorPickerButton.setColor(button._color);

        // Clear color picker button's popup menu to free memory (really just a hack to clear color swatch border)
        colorPickerButton._popupMenu = null;
    }
};

/**
 * Icon implementation for color picker button, with arrow & dynamically updated swatch color.
 */
private class CPBIcon implements Icon {

    // Border color
    Color   _borderColor;
    
    // Background color
    Color   _backgroundColor;

    /** Creates new CPBIcon. */
    public CPBIcon(Color aBorder, Color aBackGround)
    {
        _borderColor = aBorder;
        _backgroundColor = aBackGround;
    }
    
    /** Returns the width of icon1. */
    public int getIconWidth() { return getWidth(); }
    
    /** Returns the height of icon1. */
    public int getIconHeight() { return getHeight(); }
    
    /** Paints the icons. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        // Get graphics 2d
        Graphics2D g2 = (Graphics2D)aGraphics;
        
        // Paint background
        if(_backgroundColor!=null) {
            g2.setColor(_backgroundColor);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Paint border
        if(_borderColor!=null) {
            g2.setColor(_borderColor);
            g2.drawRect(0, 0, getWidth()-11, getHeight()-1);
            g2.drawRect(getWidth()-11, 0, 10, getHeight()-1);
        }
        
        // Paint arrow
        getDownArrowIcon().paintIcon(aComponent, aGraphics, 21, 9);
        
        // Paint base icon
        _icon.paintIcon(aComponent, aGraphics, 2, 2);
        
        // Paint swatch for no color
        if(_color==null) {
            g2.setColor(Color.darkGray);
            g2.draw(new Rectangle(3, 15, 14, 4));
        }

        // Paint color swatch
        else {
            g2.setColor(_color);
            g2.fill(new Rectangle(3, 15, 14, 4));
            g2.setColor(_color.darker());
            g2.draw(new Rectangle(3, 15, 14, 4));
        }
    }
}

}