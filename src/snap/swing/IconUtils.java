package snap.swing;
import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Some useful Icon utilities and subclasses. 
 */
public class IconUtils {

/**
 * Returns an icon for the given shape, color and size.
 */
public static Icon getIcon(Shape aShape, Color aColor, int aWidth, int aHeight)
{
    return new ShapeIcon(aShape, aColor, aWidth, aHeight);
}

/**
 * Returns an image icon for the given shape, color and size.
 */
public static ImageIcon getImageIcon(Shape aShape, Color aColor, int aWidth, int aHeight)
{
    return getImageIcon(getIcon(aShape, aColor, aWidth, aHeight));
}

/**
 * Returns an image icon for given icon.
 */
public static ImageIcon getImageIcon(Icon anIcon)
{
    return getImageIcon(anIcon, anIcon.getIconWidth(), anIcon.getIconHeight());
}

/**
 * Returns an image icon for given icon.
 */
public static ImageIcon getImageIcon(Icon anIcon, int aWidth, int aHeight)
{
    // If icon is already image icon, just return it
    if(anIcon instanceof ImageIcon && anIcon.getIconWidth()==aWidth && anIcon.getIconHeight()==aHeight)
        return (ImageIcon)anIcon;
    
    // Get image for icon
    Image image = getImage(anIcon, aWidth, aHeight);
    
    // Return image icon
    return new ImageIcon(image);
}

/**
 * Returns an image for the given shape, image size and color.
 */
public static BufferedImage getImage(Icon anIcon, int aWidth, int aHeight)
{
    // Create image and graphics object
    BufferedImage image = new BufferedImage(aWidth, aHeight, BufferedImage.TYPE_INT_ARGB);
    
    // Create graphics from image
    Graphics2D graphics = image.createGraphics();
    
    // Get x and y of centered icon
    int x = (aWidth - anIcon.getIconWidth())/2;
    int y = (aHeight - anIcon.getIconHeight())/2;
    
    // Paint icon
    anIcon.paintIcon(null, graphics, x, y);
    
    // Return image
    return image;
}

/**
 * A convenience method for setting roll-over, pressed and selected icons for a tool bar button with just an icon.
 */
public static void setRolloverIcons(AbstractButton aButton, boolean doSelected)
{
    setRolloverIcons(aButton, doSelected, aButton.getWidth(), aButton.getHeight());
}

/**
 * A convenience method for setting roll-over, pressed and selected icons for a tool bar button with just an icon.
 */
public static void setRolloverIcons(AbstractButton aButton, boolean doSelected, int w, int h)
{
    // Get icon
    if(aButton==null)
        System.currentTimeMillis();
    Icon icon = aButton.getIcon();
        
    // If original icon isn't Image icon or right size, reset  to image icon (so we get automatic disabled rendering)
    if(!(icon instanceof ImageIcon) || icon.getIconWidth()!=w || icon.getIconHeight()!=h) {
        icon = IconUtils.getImageIcon(icon, w, h);
        aButton.setIcon(icon);
    }
        
    // Add rollover icon as base plus black line border
    aButton.setRolloverIcon(new IconUtils.BorderIcon(icon, w, h));
        
    // Make base icon for a pressed toolbar button (light gray background)
    Icon bgIcon = new IconUtils.ShapeIcon(new Rectangle(0,0,w,h), Color.lightGray, w, h);

    // Make base pressed icon from background icon with border
    Icon pressedIcon = new IconUtils.BorderIcon(Color.gray, bgIcon, w, h);

    // Add pressed icon as base plus light gray background
    aButton.setPressedIcon(new IconUtils.CompositeIcon(aButton.getIcon(), pressedIcon));
    
    // If doing selected & selected rollover, proceed...
    if(doSelected) {
    
        // Set selected icon
        aButton.setSelectedIcon(new IconUtils.CompositeIcon(icon, pressedIcon));
        
        // Make base icon for selected rollover, black border light gray background (pressed state)
        Icon selectedRollOver = new IconUtils.BorderIcon(Color.black, bgIcon, w, h);

        // Set rollover selected
        aButton.setRolloverSelectedIcon(new IconUtils.CompositeIcon(icon, selectedRollOver));
    }        
}

/**
 * A simple icon implementation to draw a given shape in a given color in a given size.
 */
public static class ShapeIcon implements Icon {

    // The shape
    Shape     _shape;
    
    // The color
    Color     _color;
    
    // The width, height
    int       _width, _height;
    
    /** Creates a new ShapeIcon. */
    public ShapeIcon(Shape aShape, Color aColor, int aWidth, int aHeight)
    {
        _shape = aShape; _color = aColor; _width = aWidth; _height = aHeight;
    }
    
    /** Returns width. */
    public int getIconWidth() { return _width; }
    
    /** Returns height. */
    public int getIconHeight() { return _height; }
    
    /** Paints the icon. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        // If shape is null, just return
        if(_shape==null) return;
        
        // Set color
        Graphics2D g2 = (Graphics2D)aGraphics;
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(_color);
        
        // Translate to point, fill shape, translate back, reset hints
        g2.translate(x, y);
        g2.fill(_shape);
        g2.translate(-x, -y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
    }
}

/**
 * An icon implementation that renders a border on another icon.
 */
public static class SpacerIcon implements Icon {

    // The icon
    Icon      _icon;
    
    // The composite icon width, height
    int       _width, _height;
    
    /** Creates a new SpacerIcon with given width & height. */
    public SpacerIcon(Icon anIcon, int aWidth, int aHeight)  { _icon = anIcon; _width = aWidth; _height = aHeight; }
    
    /** Returns width. */
    public int getIconWidth() { return _width; }
    
    /** Returns height. */
    public int getIconHeight() { return _height; }
    
    /** Paints the icon. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        // Render icon
        int ix = x + (_width - _icon.getIconWidth())/2;
        int iy = y + (_height - _icon.getIconHeight())/2;
        _icon.paintIcon(aComponent, aGraphics, ix, iy);
    }
}

/**
 * An icon implementation that stretches another icon to specified with.
 */
public static class StretcherIcon implements Icon {

    // The real icon
    Icon         _icon;
    
    // The new width/height
    int          _width, _height;
    
    /**
     * Creates a new StretcherIcon with given width and height.
     */
     public StretcherIcon(Icon anIcon, int aWidth, int aHeight)  { _icon = anIcon; _width = aWidth; _height = aHeight; }
     
     /** Returns the encapsulated icon. */
     public Icon getIcon() { return _icon; }
     
     /** Returns width. */
     public int getIconWidth() { return _width; }
     
     /** Returns height. */
     public int getIconHeight() { return _height; }
     
     /** Paints the icon. */
     public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
     {
         // Copy graphics, translate to x & y, scale and render
         Graphics2D g = (Graphics2D)aGraphics.create();
         g.translate(x, y);
         float sx = getIconWidth()*1f/getIcon().getIconWidth();
         float sy = getIconHeight()*1f/getIcon().getIconHeight();
         g.scale(sx, sy);
         _icon.paintIcon(aComponent, g, 0, 0);
     }
}

/**
 * An icon implementation that renders a border on another icon.
 */
public static class BorderIcon implements Icon {

    // The border
    Border    _border;
    
    // The other icon
    Icon      _icon;
    
    // The composite icon width & height
    int       _width, _height;
    
    /** Creates a new BorderIcon with black line border. */
    public BorderIcon(Icon anIcon, int aWidth, int aHeight)
    {
        this(BorderFactory.createLineBorder(Color.black), anIcon, aWidth, aHeight);
    }
    
    /** Creates a new BorderIcon with line border in given color. */
    public BorderIcon(Color aColor, Icon anIcon, int aWidth, int aHeight)
    {
        this(BorderFactory.createLineBorder(aColor), anIcon, aWidth, aHeight);
    }
    
    /** Creates a new BorderIcon. */
    public BorderIcon(Border aBorder, Icon anIcon, int aWidth, int aHeight)
    {
        _border = aBorder; if(_border==null) _border = BorderFactory.createLineBorder(Color.black);
        _icon = anIcon; _width = aWidth; _height = aHeight;
    }
    
    /** Returns width. */
    public int getIconWidth()  { return _width; }
    
    /** Returns height. */
    public int getIconHeight()  { return _height; }
    
    /** Paints the icon. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        int ix = x + (_width - _icon.getIconWidth())/2, iy = y + (_height - _icon.getIconHeight())/2;
        _icon.paintIcon(aComponent, aGraphics, ix, iy);  // Paint inner icon centered in BorderIcon
        _border.paintBorder(aComponent, aGraphics, x, y+1, _width, _height-1); // Paint Boder
    }
}

/**
 * An icon implementation that draws two icons.
 */
public static class CompositeIcon implements Icon {

    // The first icon, second icon
    Icon    _icon1, _icon2;
    
    /** Creates a new CompositeIcon. */
    public CompositeIcon(Icon anIcon1, Icon anIcon2)  { _icon1 = anIcon1; _icon2 = anIcon2; }
    
    /** Returns the width of icon1. */
    public int getIconWidth() { return _icon1.getIconWidth(); }
    
    /** Returns the height of icon1. */
    public int getIconHeight() { return _icon1.getIconHeight(); }
    
    /** Paints the icons. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        _icon2.paintIcon(aComponent, aGraphics, x, x); // Render icon2 (in back)
        _icon1.paintIcon(aComponent, aGraphics, x, y); // Render icon1 (in front))
    }
}

/**
 * Returns a composite icon for original icon and new icon at x,y.
 */
public static CompositeIcon2 getCompositeIcon(Icon anIcon1, Icon anIcon2, int anX, int aY)
{
    CompositeIcon2 ci = anIcon1 instanceof CompositeIcon2? (CompositeIcon2)anIcon1 : new CompositeIcon2(anIcon1, 0,0);
    ci.addIcon(anIcon2, anX, aY);
    return ci;
}

/**
 * An icon implementation that draws two icons.
 */
public static class CompositeIcon2 implements Icon {

    // The icons
    List <CIEntry>  _icons = new ArrayList();
    
    // The width and height
    int             _x, _y, _width, _height;

    /** Creates a new CompositeIcon. */
    public CompositeIcon2(Icon anIcon, int anX, int aY)  { addIcon(anIcon, anX, aY); }
    
    /** Adds an icon at x y. */
    public void addIcon(Icon anIcon, int anX, int aY)
    {
        _icons.add(new CIEntry(anIcon, anX, aY));
        _x = Math.min(_x, anX); _y = Math.min(_y, aY);
        _width = Math.max(_x + _width, anIcon.getIconWidth() + anX) - _x;
        _height = Math.max(_y + _height, anIcon.getIconHeight() + aY) - _y;
    }
    
    /** Returns the width of icon1. */
    public int getIconWidth() { return _width; }
    
    /** Returns the height of icon1. */
    public int getIconHeight() { return _height; }
    
    /** Paints the icons. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        if(_x!=0 || _y!=0) { aGraphics = aGraphics.create(); aGraphics.translate(-_x, -_y); }
        for(CIEntry entry : _icons)
            entry.icon.paintIcon(aComponent, aGraphics, x + entry.x, y + entry.y);
    }
}

private static class CIEntry {
    Icon     icon;
    int      x, y;
    public CIEntry(Icon anIcon, int anX, int aY)  { icon = anIcon; x = anX; y = aY; }
}

/**
 * An icon implementation that uses a JLabel.
 */
public static class LabelIcon implements Icon {

    // The label
    JLabel     _label = new JLabel();

    /**
     * Creates a new label icon.
     */
    public LabelIcon(String aString, Font aFont, Icon anIcon)
    {
        _label.setText(aString);
        _label.setFont(aFont);
        _label.setIcon(anIcon);
        _label.setHorizontalAlignment(SwingConstants.CENTER);
        _label.setVerticalAlignment(SwingConstants.CENTER);
        _label.setSize(_label.getPreferredSize());
    }
    
    /**
     * Creates a label icon for a button.
     */
    public LabelIcon(AbstractButton aButton)
    {
        _label.setText(aButton.getText());
        _label.setFont(aButton.getFont());
        _label.setIcon(aButton.getIcon());
        _label.setHorizontalAlignment(aButton.getHorizontalAlignment());
        _label.setVerticalAlignment(aButton.getVerticalAlignment());
        _label.setHorizontalTextPosition(aButton.getHorizontalTextPosition());
        _label.setVerticalTextPosition(aButton.getVerticalTextPosition());
        _label.setIconTextGap(aButton.getIconTextGap());
        _label.setSize(_label.getPreferredSize());
    }
    
    /** Returns the JLabel. */
    public JLabel getLabel() { return _label; }
    
    /** Returns the width of icon1. */
    public int getIconWidth() { return _label.getWidth(); }
    
    /** Returns the height of icon1. */
    public int getIconHeight() { return _label.getHeight(); }
    
    /** Paints the icons. */
    public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
    {
        // Get x & y to align label in component
        float dx = (aComponent.getWidth() - _label.getWidth());
        float dy = (aComponent.getHeight() - _label.getHeight());
        
        // Adjust alignments
        switch(_label.getHorizontalAlignment()) {
            case SwingConstants.LEFT: dx *= 0; break;
            case SwingConstants.LEADING: dx *= 0; break;
            case SwingConstants.CENTER: dx *= .5f; break;
            case SwingConstants.RIGHT: dx *= 1; break;
            case SwingConstants.TRAILING: dx *= 1; break;
        }
        switch(_label.getVerticalAlignment()) {
            case SwingConstants.TOP: dy *= 0; break;
            case SwingConstants.CENTER: dy *= .5f; break;
            case SwingConstants.BOTTOM: dy *= 1; break;
        }
        
        // Do translate
        ((Graphics2D)aGraphics).translate(dx, dy);
        
        // Paint label
        _label.paint(aGraphics);
    }
}

/**
 * Set button icon.
 */
public static void setButtonIconAsLabelIcon(AbstractButton aButton)
{
    aButton.setIcon(new LabelIcon(aButton));
    aButton.setText(null);
}

}