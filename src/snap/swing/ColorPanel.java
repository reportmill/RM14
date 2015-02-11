package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import snap.util.ClassUtils;

/**
 * This class provides Swing UI for selecting a color.
 */
public class ColorPanel extends SwingOwner {
    
    // The currently selected color
    Color                _color = Color.black;
    
    // The currently selected color well
    ColorWell          _colorWell;
    
    // The Panel's local, no-selectable well which shows the current color
    ColorWell         _displayWell;
    
    // The type of color being set (Stroke, Fill, Text)
    byte                 _colorAttribute;
    
    // The list of previously selected colors
    List <Color>         _recentColors = new ArrayList();
    
    // The shared instance of the color panel
    static ColorPanel  _shared;
    
    // The class for the shared instance
    static Class         _sharedClass = ColorPanel.class;

    // Image names
    static String _iNames[] = { "Spectrum", "Paradise", "Floral", "Foilage", "Fall Leaves", "Metal", "Wood" };
    
    // Image file names
    static Object _images[] = { "spectrum.jpg", "paradise.jpg", "tulips.jpg",
        "green_foilage.jpg","fall_foilage.jpg","metal.jpg","wood.jpg" };
    
    // Could be a settable parameter, but who really cares?
    static final int MAXIMUM_RECENT_COLORS = 8;

/**
 * Creates a new color panel.
 */
public ColorPanel()  { _shared = this; }

/**
 * Returns the shared instance of the color panel.
 */
public static ColorPanel getShared()
{
    // If shared is null, create new instance
    if(_shared==null)
        ClassUtils.newInstance(_sharedClass);
    
    // Return shared instance
    return _shared;
}

/**
 * Returns the class for the shared instance of color panel.
 */
public static Class getSharedClass()  { return _sharedClass; }

/**
 * Sets the class for the shared instanceof color panel.
 */
public static void setSharedClass(Class aClass)  { _sharedClass = aClass; }

/**
 * Returns the current color of the color panel.
 */
public Color getColor()  { return _colorWell!=null? (_color = _colorWell.getColor()) : _color; }

/**
 * Sets the current color of the color panel.
 */
public void setColor(final Color aColor)
{
    // Set new color
    _color = aColor;

    // If not interactively setting color (like from a mouse or slider drag), add color to _recentColorList
    if(!Swing.isMouseDown() && _color!=null) {
        addRecentColor(aColor);
        resetLater();
    }
   
    // If color well is present, set new color in color well and fireActionPerformed
    if(_colorWell!=null)
        runLater(new Runnable() { public void run() {
            _colorWell.setColor(aColor); }});
}

/**
 * Add to the list of Recent Colors shown on the Color Panel
 */
public void addRecentColor(Color aColor)
{
    // If the color is already in the list, delete it from its old position so it gets moved to the head of the list.
    int oldPosition = _recentColors.indexOf(aColor);
    if(oldPosition != -1) 
        _recentColors.remove(oldPosition);
    else if(oldPosition==0)
        return;
    
    // Add it at the beginning
    _recentColors.add(0, aColor);
    
    // old ones fall off the end of the list.  If you want to keep a color forever, stick it in the dock.
    while(_recentColors.size()>MAXIMUM_RECENT_COLORS) _recentColors.remove(_recentColors.size()-1);
    
    // Get HistoryMenuButton and its popup menu
    MenuButton menuButton = getNode("HistoryMenuButton", MenuButton.class);
    JPopupMenu popupMenu = menuButton.getPopupMenu();
    
    // Remove all items and add them back from updated list
    popupMenu.removeAll();
    for(int i=0, iMax=_recentColors.size(); i<iMax; i++)
        popupMenu.add(new ColorMenuItem(_recentColors.get(i)));
    popupMenu.pack();
    
    // Arm popup menu items to send RibsEvents to this color panel
    for(Object menuItem : popupMenu.getComponents()) initUI(menuItem);
}

/**
 * Returns the currently selected color well.
 */
public ColorWell getColorWell()  { return _colorWell; }

/**
 * Sets the currently selected color well.
 */
public void setColorWell(ColorWell aColorWell)
{
    // If old color well is preset, unselect it
    if(_colorWell!=null && _colorWell.isSelected())
        _colorWell.setSelected(false);
    
    // Set new color well
    _colorWell = aColorWell;
    
    // If new color well is present, select it
    if(_colorWell!=null) {
        if (!_colorWell.isSelected()) _colorWell.setSelected(true);
        setColor(aColorWell.getColor()); 
    }
    
    // Reset color panel
    resetLater();
}

/**
 * Returns the selected picker (0=Image, 1=RGB Sliders, 2=Gray Sliders, 3=SwatchPicker).
 */
public int getSelectedPicker()  { return getNodeIntValue("PickerPanel"); }

/**
 * Sets the selected picker (0=Image, 1=RGB Sliders, 2=Gray Sliders, 3=SwatchPicker).
 */
public void setSelectedPicker(int aPicker)
{
    setNodeSelectedIndex("PickerPanel", aPicker);
    resetLater();
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure ImagePickerLabel
    getNode("ImagePickerLabel", JLabel.class).setIcon(new ImageIcon(getImage(0)));
    getNode("ImagePickerLabel", JLabel.class).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    
    // Configure ImageComboBox
    getNode("ImageComboBox", JComboBox.class).setModel(new DefaultComboBoxModel(_iNames));

    // Start with black in the list of recent colors, more will be added as they are inspected or set
    addRecentColor(Color.black);
            
    // Load Dock's colors from preferences
    getNode("ColorDock", ColorDock.class).setPersistent(true);
    getNode("ColorDock", ColorDock.class).setSelectable(false);

    // Configure SwatchPicker
    getNode("SwatchPicker").setLayout(new JPanel().getLayout());
    getNode("SwatchPicker").add(getSwatchPanel());
    
    // Install funky slider renderers
    installGradientUI(getNode("RedSlider", JSlider.class));
    installGradientUI(getNode("GreenSlider", JSlider.class)); 
    installGradientUI(getNode("BlueSlider", JSlider.class)); 
    installGradientUI(getNode("AlphaSlider", JSlider.class)); 
    installGradientUI(getNode("GraySlider", JSlider.class)); 
    installGradientUI(getNode("GrayAlphaSlider", JSlider.class)); 
}

/**
 * Resets UI controls.
 */
public void resetUI()
{
    // Get color (if null, replace with color clear)
    Color color = getColor();
    if(color==null)
        color = new Color(0,0,0,0);
    
    // Reset color sliders if needed
    switch(getNodeIntValue("PickerPanel")) {
    
        // Update RGB picker
        case 1:
            
            // Update RGB sliders/text
            setNodeValue("RedSlider", color.getRed());
            setNodeValue("RedText", color.getRed());
            setNodeValue("GreenSlider", color.getGreen());
            setNodeValue("GreenText", color.getGreen());
            setNodeValue("BlueSlider", color.getBlue());
            setNodeValue("BlueText", color.getBlue());
            setNodeValue("AlphaSlider", color.getAlpha());
            setNodeValue("AlphaText", color.getAlpha());
            
            // Update HextText
            setNodeValue("HexText", toHexString(color));
            
            // Update the slider color gradients
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
            JSlider slider = getNode("RedSlider", JSlider.class); slider.repaint();
            GradientSliderUI grade = (GradientSliderUI)slider.getUI();
            grade.setColors(new Color(0,g,b), new Color(255,g,b));
            slider = getNode("GreenSlider", JSlider.class); slider.repaint();
            grade = (GradientSliderUI)slider.getUI();
            grade.setColors(new Color(r,0,b), new Color(r,255,b));
            slider = getNode("BlueSlider", JSlider.class); slider.repaint();
            grade = (GradientSliderUI)slider.getUI();
            grade.setColors(new Color(r,g,0), new Color(r,g,255));
            slider = getNode("AlphaSlider", JSlider.class); slider.repaint();
            grade = (GradientSliderUI)slider.getUI();
            grade.setColors(new Color(r,g,b,0), new Color(r,g,b,255));
            
            // Break
            break;
            
        // Update gray scale picker
        case 2:
            
            // Update gray/alpha siders/text
            setNodeValue("GraySlider", color.getRed());
            setNodeValue("GrayText", color.getRed());
            setNodeValue("GrayAlphaSlider", color.getAlpha());
            setNodeValue("GrayAlphaText", color.getAlpha());
            
            // Update the slider alpha gradient
            r = color.getRed();
            slider = getNode("GrayAlphaSlider", JSlider.class); slider.repaint();
            grade = (GradientSliderUI)(slider.getUI());
            grade.setColors(new Color(r,r,r,0), new Color(r,r,r,255));
    }
    
    // Update the display well
    setNodeValue("DisplayColorWell", color);
}

/**
 * Responds to changes to the UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle ImagePickerButton
    if(anEvent.equals("ImagePickerButton"))
        setSelectedPicker(0);
    
    // Handle RGBPickerButton
    if(anEvent.equals("RGBPickerButton"))
        setSelectedPicker(1);
    
    // Handle GrayPickerButton
    if(anEvent.equals("GrayPickerButton"))
        setSelectedPicker(2);
    
    // Handle SwatchPickerButton
    if(anEvent.equals("SwatchPickerButton"))
        setSelectedPicker(3);
        
    // Handle ImageLabel
    if(anEvent.equals("ImagePickerLabel"))
        setColor(anEvent.getTarget(ImagePickerLabel.class).getForeground());
    
    // Handle ImageComboBox
    if(anEvent.equals("ImageComboBox"))
        getNode("ImagePickerLabel", ImagePickerLabel.class).setIcon(new ImageIcon(getImage(anEvent.getSelectedIndex())));
    
    // Have any JSlider's automatically update their Text counterparts and substitute that text
    if(anEvent.getTarget() instanceof JSlider) {
        String name = anEvent.getName().replace("Slider", "Text");
        setNodeValue(name, anEvent.getIntValue());
        sendEvent(name);
    }

    // Handle Gray or GrayAlpha Text 
    if(anEvent.equals("GrayText") || anEvent.equals("GrayAlphaText")) {
        int g = getNodeIntValue("GrayText"), a = getNodeIntValue("GrayAlphaText");
        g = g<0 ? 0 : g>255 ? 255 : g;
        a = a<0 ? 0 : a>255 ? 255 : a;
        Color gray = new Color(g, g, g, a);
        setColor(gray);
    }
    
    // Handle Red/Green/Blue/Alpha Text
    if(anEvent.equals("RedText") || anEvent.equals("GreenText") || anEvent.equals("BlueText")
        || anEvent.equals("AlphaText")) {
        int r = getNodeIntValue("RedText"), g = getNodeIntValue("GreenText");
        int b = getNodeIntValue("BlueText"), a = getNodeIntValue("AlphaText");
        r = r<0 ? 0 : r>255 ? 255 : r;
        g = g<0 ? 0 : g>255 ? 255 : g;
        b = b<0 ? 0 : b>255 ? 255 : b;
        a = a<0 ? 0 : a>255 ? 255 : a;
        Color c = new Color(r, g, b, a);
        setColor(c);
    }
    
    // Handle Recent Colors Dropdown menu
    if(anEvent.getTarget() instanceof ColorMenuItem)
        setColor(anEvent.getTarget(ColorMenuItem.class).getColor());
        
    // Handle HexText
    if(anEvent.equals("HexText"))
        setColor(fromHexString(anEvent.getStringValue()));
}

/**
 * Returns a hex string representation of this color.
 */
private static String toHexString(Color aColor)
{
    // Allocate string buffer and get integer rgba components
    StringBuffer sb = new StringBuffer("#");
    int r = aColor.getRed(), g = aColor.getGreen(), b = aColor.getBlue(), a = aColor.getAlpha();
    
    // Add r, g, b components
    if(r<16) sb.append('0'); sb.append(Integer.toHexString(r));
    if(g<16) sb.append('0'); sb.append(Integer.toHexString(g));
    if(b<16) sb.append('0'); sb.append(Integer.toHexString(b));
    
    // If alpha is not max, add it too
    if(a<255) {
        if(a<16) sb.append('0'); sb.append(Integer.toHexString(a));
    }

    // Return string
    return sb.toString().toUpperCase();
}

/**
 * Returns a color from a hex string.
 */
private static Color fromHexString(String aHexString)
{
    try {
    int start = aHexString.charAt(0)=='#'? 1 : 0;
    float r = Integer.decode("0x" + aHexString.substring(start, start + 2)).intValue()/255f;
    float g = Integer.decode("0x" + aHexString.substring(start + 2, start + 4)).intValue()/255f;
    float b = Integer.decode("0x" + aHexString.substring(start + 4, start + 6)).intValue()/255f;
    float a = 1;
    if(aHexString.length() >= start + 8)
        a = Integer.decode("0x" + aHexString.substring(start + 6, start + 8)).intValue()/255f;
    return new Color(r, g, b, a);
    
    // Just return black on any error
    } catch(Exception e) { return Color.black; }
}

/**
 * Returns the buffered image from the list of images.
 */
private BufferedImage getImage(int anIndex)
{
    // If image at given index is still a String, convert to image
    if(_images[anIndex] instanceof String) try {
        
        // Get input stream for image and read buffered image
        InputStream fis = ColorPanel.class.getResourceAsStream("ColorPanel.ribs/" + _images[anIndex]);
        _images[anIndex] = ImageIO.read(fis);
    }
    
    // Catch exceptions
    catch (Exception e) { e.printStackTrace(); }

    // Return buffered image
    return (BufferedImage)_images[anIndex];
}

/**
 * An inner class to act as an image color picker.
 */
public static class ImagePickerLabel extends JLabel {
    
    // Whether image picker label is engaged in interactive color picking (mouse loop)
    boolean _valueIsAdjusting;
    
    /** Creates a new image picker label. */
    public ImagePickerLabel() {
        
        // Create a new mouse input adaptor
        MouseInputAdapter mia = new MouseInputAdapter() {
            
            /** Mouse Pressed. */
            public void mousePressed(MouseEvent e)  { _valueIsAdjusting = true; doIt(e); }
            
            /** Mouse Dragged. */
            public void mouseDragged(MouseEvent e)  { doIt(e); }
            
            /** Mouse Released. */
            public void mouseReleased(MouseEvent e)  { _valueIsAdjusting = false; doIt(e); }
            
            /** Fires action for color change. */
            public void doIt(MouseEvent me) {
                
                // Catch exceptions - don't remember why
                try {
                    
                // Get the image
                ImageIcon icon = (ImageIcon)getIcon();
                BufferedImage im = (BufferedImage)icon.getImage();
                    
                // Get mouse x & y 
                int x = me.getX(); if(getWidth()>im.getWidth()) x -= (getWidth() - im.getWidth())/2;
                int y = me.getY(); if(getHeight()>im.getHeight()) y -= (getHeight() - im.getHeight())/2;
                x = Math.min(Math.max(0, x), im.getWidth()-1);
                y = Math.min(Math.max(0, y), im.getHeight()-1);
                
                // Get color at mouse point and install
                Color color = new Color(im.getRGB(x, y));
                setForeground(color);
                
                // Send action for image picker label
                Swing.sendEvent(new ChangeEvent(ImagePickerLabel.this));
                
                // Catch exceptions
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        
        // Add mouse listener and mouse motion listener
        addMouseListener(mia);
        addMouseMotionListener(mia);
        
        // Set horizontal alignment to center
        setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /** Returns whether image picker label is engaged in interactive color picking (mouse loop). */
    public boolean getValueIsAdjusting()  { return _valueIsAdjusting; }
}

/**
 * An inner JMenuItem subclass that draws a solid color and can highlight the selected item non-destructively.
 */
private static class ColorMenuItem extends JMenuItem {
    Color _color;
    public ColorMenuItem(Color c) { super(); _color=c; }
    public Color getColor() { return _color; }
    public boolean isHighlighted() { // What nonsense.  There's got to be an easier way
        MenuElement path[] = MenuSelectionManager.defaultManager().getSelectedPath();
        return path!=null && path.length==2 && this.equals(path[1]);
    }
    protected void paintComponent(Graphics g) {
        Rectangle r = getBounds();
        g.setColor(Color.white);
        g.fillRect(0,0,r.width,r.height);
        ColorWell.paintSwatch(g, _color, 0, 0, r.width, r.height);
        if (isHighlighted()) {
            g.setColor(Color.white);
            g.drawRect(0,0,r.width-1,r.height-1);
            g.setColor(Color.black);
            g.drawRect(1,1,r.width-3,r.height-3);
        }
    }
}
    
/**
 * Returns a panel with a matrix of hard-coded colors to pick from.
 */
private JPanel getSwatchPanel()
{
    JPanel swatchPanel = new JPanel();
    swatchPanel.setLayout(new GridLayout(16,17));
    Dimension swatchDim = new Dimension(10,10);
    
    // Create color swatch buttons for each web color basing bounds on position in array for desired 16x17 matrix
    for(int i=0; i<_webColors.length; i++) {
    
        // Create Button for colors
        JToggleButton swatch = new JToggleButton() {
                public boolean isSelected() { return false; }
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(getBackground());
                    Rectangle clip = g.getClipBounds();
                    g.fillRect(1, 1, (int)clip.getWidth() - 2, (int)clip.getHeight() - 2); }};
        
        swatch.setBorder(BorderFactory.createLineBorder(Color.gray,1));
        swatch.setBackground(Color.decode(_webColors[i]));
        swatch.setToolTipText("#" + _webColors[i].substring(2));
        swatch.setPreferredSize(swatchDim);
        swatch.setMinimumSize(swatchDim);
        swatch.setMaximumSize(swatchDim);
        swatch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setColor(((JComponent)e.getSource()).getBackground()); }});
        swatchPanel.add(swatch);
    }

    return swatchPanel;
}

/**
 * A list of hard-coded colors for the swatch panel.
 */
private static String _webColors[] = {
    "0x990033", "0xFF3366", "0xCC0033", "0xFF0033", "0xFF9999", "0xCC3366", "0xFFCCFF", "0xCC6699",
    "0x993366", "0x660033", "0xCC3399", "0xFF99CC", "0xFF66CC", "0xFF99FF", "0xFF6699", "0xCC0066",
    "0xFF0066", "0xFF3399", "0xFF0099", "0xFF33CC", "0xFF00CC", "0xFF66FF", "0xFF33FF", "0xFF00FF",
    "0xCC0099", "0x990066", "0xCC66CC", "0xCC33CC", "0xCC99FF", "0xCC66FF", "0xCC33FF", "0x993399",
    "0xCC00CC", "0xCC00FF", "0x9900CC", "0x990099", "0xCC99CC", "0x996699", "0x663366", "0x660099",
    "0x9933CC", "0x660066", "0x9900FF", "0x9933FF", "0x9966CC", "0x330033", "0x663399", "0x6633CC",
    "0x6600CC", "0x9966FF", "0x330066", "0x6600FF", "0x6633FF", "0xCCCCFF", "0x9999FF", "0x9999CC",
    "0x6666CC", "0x6666FF", "0x666699", "0x333366", "0x333399", "0x330099", "0x3300CC", "0x3300FF",
    "0x3333FF", "0x3333CC", "0x0066FF", "0x0033FF", "0x3366FF", "0x3366CC", "0x000066", "0x000033",
    "0x0000FF", "0x000099", "0x0033CC", "0x0000CC", "0x336699", "0x0066CC", "0x99CCFF", "0x6699FF",
    "0x003366", "0x6699CC", "0x006699", "0x3399CC", "0x0099CC", "0x66CCFF", "0x3399FF", "0x003399",
    "0x0099FF", "0x33CCFF", "0x00CCFF", "0x99FFFF", "0x66FFFF", "0x33FFFF", "0x00FFFF", "0x00CCCC",
    "0x009999", "0x669999", "0x99CCCC", "0xCCFFFF", "0x33CCCC", "0x66CCCC", "0x339999", "0x336666",
    "0x006666", "0x003333", "0x00FFCC", "0x33FFCC", "0x33CC99", "0x00CC99", "0x66FFCC", "0x99FFCC",
    "0x00FF99", "0x339966", "0x006633", "0x336633", "0x669966", "0x66CC66", "0x99FF99", "0x66FF66",
    "0x339933", "0x99CC99", "0x66FF99", "0x33FF99", "0x33CC66", "0x00CC66", "0x66CC99", "0x009966",
    "0x009933", "0x33FF66", "0x00FF66", "0xCCFFCC", "0xCCFF99", "0x99FF66", "0x99FF33", "0x00FF33",
    "0x33FF33", "0x00CC33", "0x33CC33", "0x66FF33", "0x00FF00", "0x66CC33", "0x006600", "0x003300",
    "0x009900", "0x33FF00", "0x66FF00", "0x99FF00", "0x66CC00", "0x00CC00", "0x33CC00", "0x339900",
    "0x99CC66", "0x669933", "0x99CC33", "0x336600", "0x669900", "0x99CC00", "0xCCFF66", "0xCCFF33",
    "0xCCFF00", "0x999900", "0xCCCC00", "0xCCCC33", "0x333300", "0x666600", "0x999933", "0xCCCC66",
    "0x666633", "0x999966", "0xCCCC99", "0xFFFFCC", "0xFFFF99", "0xFFFF66", "0xFFFF33", "0xFFFF00",
    "0xFFCC00", "0xFFCC66", "0xFFCC33", "0xCC9933", "0x996600", "0xCC9900", "0xFF9900", "0xCC6600",
    "0x993300", "0xCC6633", "0x663300", "0xFF9966", "0xFF6633", "0xFF9933", "0xFF6600", "0xCC3300",
    "0x996633", "0x330000", "0x663333", "0x996666", "0xCC9999", "0x993333", "0xCC6666", "0xFFCCCC",
    "0xFF3333", "0xCC3333", "0xFF6666", "0x660000", "0x990000", "0xCC0000", "0xFF0000", "0xFF3300",
    "0xCC9966", "0xFFCC99", "0xFFFFFF", "0xCCCCCC", "0x999999", "0x666666", "0x333333", "0x000000",
    "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000", "0x000000"
};

/**
 * A Slider UI subclass to show colors in slider groove.
 */
private static class GradientSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
    Color startColor = Color.black, endColor = Color.white;
    Image _thumbImage = Swing.getImage("thumb.png", ColorPanel.class);
    
    public GradientSliderUI(JSlider s)  { super(s); }
    public void setColors(Color start, Color end) { startColor = start; endColor = end; }
    
    public void paintFocus(Graphics g)  { }
    public void paintTrack(Graphics g)
    {        
        Rectangle trackBounds = trackRect;
        int cy = (trackBounds.height / 2) - 4;
        int cw = trackBounds.width;

        g.translate(trackBounds.x, trackBounds.y + cy);

        g.setColor(getShadowColor());
        g.drawLine(0, 0, cw - 1, 0);
        g.drawLine(0, 1, 0, 7);
        g.setColor(getHighlightColor());
        g.drawLine(0, 8, cw, 8);
        g.drawLine(cw, 0, cw, 7);
        g.setColor(Color.white);
        g.drawLine(1,7,cw-1,7);
        g.drawLine(cw-1,1,cw-1,6);
        
        if(startColor==null || endColor==null)
            g.setColor(Color.black);
        else ((Graphics2D)g).setPaint(new GradientPaint(1, 1, startColor, cw-2, 1, endColor));
        for(int y=1;y<7;++y)
            g.drawLine(1, y, cw-2, y);

        g.translate(-trackBounds.x, -(trackBounds.y + cy));
    }
    
    protected Dimension getThumbSize()  { return new Dimension(16,14); }
    
    public void paintThumb(Graphics g)
    {        
        if(!slider.isEnabled()) return;
        Rectangle bounds = thumbRect;
        ((Graphics2D)g).drawImage(_thumbImage, AffineTransform.getTranslateInstance(bounds.x, bounds.y), null);
    }
    
}

/** Install Gradient UI in slider. */
public static void installGradientUI(JSlider aSlider)  { aSlider.setUI(new GradientSliderUI(aSlider)); }

}