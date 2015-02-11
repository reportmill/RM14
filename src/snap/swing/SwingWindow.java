package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.Callable;
import javax.swing.*;
import snap.util.*;

/**
 * A class to manage the Window of a SwingOwner.
 */
public class SwingWindow {

    // The panel's window
    Window                    _window;
    
    // The method to get the window
    Callable <Window>         _windowCall;
    
    // The ContentPane
    Container                 _contentPane;
    
    // The method to get the window content pane
    Callable <JComponent>     _contentPaneCall;
    
    // The panel's window class
    Class <? extends Window>  _wclass = JDialog.class;
    
    // The panel's window title
    String                    _title;
    
    // Whether the panel's window is always on top
    boolean                   _alwaysOnTop;
    
    // The document file
    File                      _docFile;
    
    // Whether the panel's window hides on deativate
    boolean                   _hideOnDeactivate;
    
    // The icon image
    Image                     _image;
    
    // A Ribs EventListener to deactivate AlwaysOnTop when app deactivates
    ActivationAdapter       _activationAdapter;
    
    // Whether the panel's window is modal
    boolean                   _modal = false;
    
    // Whether the panel's window is resizable
    boolean                   _resizable = true;
    
    // The window style
    Style                     _style;
    
    // The panel's window menu bar
    JMenuBar                  _menuBar;
    
    // The panel's window's default button
    JButton                   _defaultButton;
    
    // Whether the panel's window is visible
    boolean                   _visible;
    
    // Whether the window has a title bar and other decorations
    boolean                   _undecorated = false;
    
    // The window listener
    WindowListener            _windowListener;
    
    // Constants for style
    public static enum Style { Small }
    
    // Constants for setVisible position
    public static enum Pos {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

/**
 * Returns whether window has been created.
 */
public boolean isWindowSet()  { return _window!=null; }

/**
 * Returns the window associated with this panel, creating it if necessary.
 */
public Window getWindow()
{
    if(_window==null) {
        getContentPane(); if(_window==null) setWindow(createWindow()); }
    return _window;
}

/**
 * Sets a new window for this panel.
 */
protected void setWindow(Window aWindow)
{
    // Set window
    _window = aWindow;
        
    // If window is undecorated - set that
    if(_window instanceof JFrame) ((JFrame)_window).setUndecorated(isUndecorated());
    
    // Install ContentPane
    setContentPane(getContentPane());
        
    // Set window attributes: Title, AlwaysOnTop, Modal and Resizable
    setTitle(getTitle());
    setAlwaysOnTop(isAlwaysOnTop());
    setIconImage(getIconImage());
    setModal(isModal());
    setResizable(isResizable());
    setStyle(getStyle());
    if(_windowListener!=null) addWindowListener(_windowListener);
    
    // Reset window default button
    if(getDefaultButton()!=null) {
        JRootPane rootPane = getRootPane();
        rootPane.setDefaultButton(getDefaultButton());
    }
    
    // Set the window menu bar
    setMenuBar(getMenuBar());    
}

/**
 * Creates a window for this panel from window class.
 */
protected Window createWindow()
{
    try {
        if(_windowCall!=null)
            return _windowCall.call();
        return getWindowClass().newInstance();
    }
    catch(Exception e) { throw new RuntimeException(e); }    
}

/**
 * Returns the call to create the window.
 */
public Callable <Window> getWindowCall()  { return _windowCall; }

/**
 * Sets the call to create the window.
 */
public void setWindowCall(Callable <Window> aCall)  { _windowCall = aCall; }

/**
 * Returns the Callable that provides the ContentPane.
 */
public Callable <JComponent> getContentPaneCall()  { return _contentPaneCall; }

/**
 * Sets the Callable that provides the ContentPane.
 */
public void setContentPaneCall(Callable <JComponent> aCall)  { _contentPaneCall = aCall; }

/**
 * Returns the class of the window associated with this panel.
 */
public Class <? extends Window> getWindowClass()  { return _wclass; }

/**
 * Sets the window class.
 */
public void setWindowClass(Class <? extends Window> aClass)  { _wclass = aClass; }

/**
 * Returns the title of the window.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title of the window.
 */
public void setTitle(String aValue)
{
    _title = aValue; // Set window title
    if(_window instanceof Frame) ((Frame)_window).setTitle(_title!=null? _title : "");
    else if(_window instanceof Dialog) ((Dialog)_window).setTitle(_title!=null? _title : "");
}

/**
 * Returns whether the window is resizable.
 */
public boolean isResizable()  { return _resizable; }

/**
 * Sets whether the window is resizable (default to true).
 */
public void setResizable(boolean aValue)
{
    _resizable = aValue; // Set value
    if(_window instanceof Frame) ((Frame)_window).setResizable(aValue);
    else if(_window!=null) ((Dialog)_window).setResizable(aValue);
}

/**
 * Returns the window style.
 */
public Style getStyle()  { return _style; }

/**
 * Sets the window style.
 */
public void setStyle(Style aStyle)
{
    _style = aStyle; String style = aStyle==Style.Small? "small" : null;
    JRootPane rootPane = getRootPane(); if(rootPane==null) return;
    rootPane.putClientProperty("Window.style", style);
}

/**
 * Returns the menu bar associated with this window.
 */
public JMenuBar getMenuBar()  { return _menuBar; }

/**
 * Sets the menu bar associated with this window.
 */
public void setMenuBar(JMenuBar aMenuBar)
{
    _menuBar = aMenuBar;
    if(_window instanceof JFrame) { JFrame frame = (JFrame)_window; JRootPane rpane = frame.getRootPane();
        frame.setJMenuBar(aMenuBar);
        rpane.revalidate(); rpane.repaint();
    }
}

/**
 * Returns whether the window is always on top.
 */
public boolean isAlwaysOnTop()  { return _alwaysOnTop; }

/**
 * Sets whether the window is always on top.
 */
public void setAlwaysOnTop(boolean aValue)  { _alwaysOnTop = aValue; }

/**
 * Returns the document file for the window title bar proxy icon.
 */
public File getDocumentFile()  { return _docFile; }

/**
 * Returns the document file for the window title bar proxy icon.
 */
public void setDocumentFile(File aFile)
{
    if(SnapUtils.equals(aFile, _docFile)) return; _docFile = aFile;
    JRootPane rootPane = getRootPane(); if(rootPane==null) return;
    rootPane.putClientProperty("Window.documentFile", aFile);
}

/**
 * Returns whether the window will hide on deactivate.
 */
public boolean isHideOnDeactivate()  { return _hideOnDeactivate; }

/**
 * Sets whether the window will hide on deacativate.
 */
public void setHideOnDeactivate(boolean aValue)  { _hideOnDeactivate = aValue; }

/**
 * Returns the icon image for the window.
 */
public Image getIconImage() { return _image; }
    
/**
 * Sets the icon image for the window.
 */
public void setIconImage(Image anImage)
{
    _image = anImage;
    if(_window instanceof JFrame) ((JFrame)_window).setIconImage(_image);
}
    
/**
 * Returns the modal mode of the window.
 */
public boolean isModal()  { return _modal; }

/**
 * Sets the modal mode of the window (defaults to false).
 */
public void setModal(boolean aValue)
{
    _modal = aValue; // Set flag
    if(_window instanceof Dialog) ((Dialog)_window).setModal(_modal);
}

/**
 * Returns the name of the default button associated with this window.
 */
public JButton getDefaultButton()  { return _defaultButton; }

/**
 * Sets the name of the default button associated with this window.
 */
public void setDefaultButton(JButton aButton)  { _defaultButton = aButton; }

/**
 * Returns true if the window has no title bar or other decorations.
 */
public boolean isUndecorated()  { return _undecorated; }

/**
 * Sets whether the window has a title bar and decorations or not.
 * This must be done before the first call to getWindow() or an IllegalComponentStateException will be thrown.
 */
public void setUndecorated(boolean flag)  { _undecorated = flag; }

/**
 * Add window listener.
 */
public void addWindowListener(WindowListener aListener)
{
    _windowListener = aListener;
    if(_window!=null && aListener!=null) _window.addWindowListener(aListener);
}

/**
 * Returns the root pane for the window.
 */
public JRootPane getRootPane()  { return _window!=null? ((RootPaneContainer)_window).getRootPane() : null; }

/**
 * Returns the content pane.
 */
public Container getContentPane()
{
    if(_contentPane==null && _contentPaneCall!=null)
        try { _contentPane = _contentPaneCall.call(); }
        catch(Exception e)  { throw new RuntimeException(e); }
    return _contentPane;
}

/**
 * Sets the content pane.
 */
public void setContentPane(Container aContainer)
{
    _contentPane = aContainer;
    if(_window instanceof RootPaneContainer) ((RootPaneContainer)_window).setContentPane(aContainer);
}

/**
 * Returns the window x.
 */
public int getX()  { return getWindow().getX(); }

/**
 * Returns the window y.
 */
public int getY()  { return getWindow().getY(); }

/**
 * Returns the window width.
 */
public int getWidth()  { return getWindow().getWidth(); }

/**
 * Returns the window x.
 */
public int getHeight()  { return getWindow().getHeight(); }

/**
 * Returns the window size.
 */
public Dimension getSize()  { return getWindow().getSize(); }

/**
 * Returns the window size.
 */
public void setSize(Dimension aSize)  { getWindow().setSize(aSize); }

/**
 * Returns the window PreferredSize.
 */
public Dimension getPreferredSize()  { return getWindow().getPreferredSize(); }

/**
 * Returns the window MinimumSize.
 */
public Dimension getMinimumSize()  { return getWindow().getMinimumSize(); }

/**
 * Packs the window.
 */
public void pack()  { getWindow().pack(); }

/**
 * Requests focus on the window.
 */
public void requestFocus()  { getWindow().requestFocus(); }

/**
 * Order window to front.
 */
public void toFront()  { getWindow().toFront(); }

/**
 * Set Maximized bounds.
 */
public void setMaximizedBounds(Rectangle aRect)
{
    if(_window instanceof Frame)  ((Frame)_window).setMaximizedBounds(aRect);
    else if(_window!=null) throw new RuntimeException("SwingWindow: Can't setMaximizedBounds on " + getWindowClass());
}

/**
 * This method disposes the window associated with this panel.
 */
public void windowDispose()
{
    // Remove window listeners in case there are still events in the queue, dispose window and clear variable
    if(_window==null) return;
    for(WindowListener l : _window.getWindowListeners()) _window.removeWindowListener(l);
    _window.dispose(); _window = null;
}

/**
 * Returns whether window is visible.
 */
public boolean isVisible()  { return _window!=null && _window.isVisible(); }

/**
 * This method sets the window to be visible or invisible.
 */
public void setVisible(boolean aValue)
{
    // If setting window visible, call real setWindowVisible
    if(aValue) {
        
        // If window is already visible, set visible where it already is
        if(_visible && _window!=null)
            setVisible(null, _window.getX(), _window.getY(), null, false);
        
        // Otherwsie, set window visible centered
        else setVisible(Pos.CENTER, 0, 0, null, false);
    }
    
    // If ordering window offscreen, have window setVisible(false)
    else if(_window!=null) {
        
        // If window always-on-top, explicitly turn this off (seems unnecessary, but fixes a bug on Windows)
        if(isAlwaysOnTop())
            _window.setAlwaysOnTop(false);
        
        // If there is a window activation listener, remove it
        if(_activationAdapter!=null)
            _activationAdapter.setEnabled(false);
        
        // Set window not visible
        _window.setVisible(false);
    }
    
    // Set window visible flag
    _visible = aValue;
}

/**
 * This method set the window associated with this panel to be visible at some given x and y away from a given corner.
 * It also allows you to provide a "frame save name" which causes the frame's location and size to be saved between
 * sessions (in which case it will override the default position provided).
 */
public void setVisible(Pos aPos, int x, int y, String fsaveName, boolean doSize)
{
    // If always-on-top, turn this on (since this is can be turned off in setWindowVisible(false))
    if(isAlwaysOnTop())
        getWindow().setAlwaysOnTop(true);

    // Set panel's window visible using Ribs utility method
    setVisible(getWindow(), aPos, x, y, fsaveName, doSize);
    
    // If window is modal, just return
    if(isModal())
        return;
    
    // If window is always-on-top or does hide-on-deactivate, add listener to handle app deactivate stuff
    if(isAlwaysOnTop() || isHideOnDeactivate()) {
        
        // Create listener to handle app deactivate stuff
        if(_activationAdapter==null)
            _activationAdapter = new ActivationAdapter(getWindow(), isHideOnDeactivate());
        
        // Add listener
        _activationAdapter.setEnabled(true);
    }
        
    // Mark window as visible
    _visible = true;
}

/**
 * Makes a window visible with the given corner at the given x & y.
 */
public static void setVisible(Component aWin, Pos aPos, int dx, int dy, String aFrmSvName, boolean doSize)
{
    // Get actual window for component (just return if null)
    Window window = SwingUtils.getWindow(aWin); if(window==null) return;
    
    // Declare variables for a width and height
    int w = 0, h = 0;
    
    // If FrameSaveName provided, set Location from defaults and register to store future window moves
    if(aFrmSvName!=null) {
        
        // Get location string from preferences
        String locString = PrefsUtils.prefs().get(aFrmSvName + "Loc", null);
        
        // If location string is non-null, extract previous x and y and change relative corner to upper left
        if(locString!=null) {
            String strings[] = locString.split(" ");
            dx = StringUtils.intValue(strings[0]);
            dy = StringUtils.intValue(strings[1]);
            w = doSize && strings.length>2? StringUtils.intValue(strings[2]) : 0;
            h = doSize && strings.length>3? StringUtils.intValue(strings[3]) : 0;
            aPos = null;
        }

        // Check component listeners to see if FrameSaveComponentListener has been added yet
        ComponentListener listeners[] = window.getComponentListeners();
        for(int i=0; listeners!=null && i<listeners.length; i++)
            if(listeners[i].getClass()==FrameSaveComponentListener.class)
                listeners = null;
        
        // If window doesn't have ComponentListener for componentMoved, add it
        if(listeners!=null)
            window.addComponentListener(new FrameSaveComponentListener(aFrmSvName, doSize));
    }
    
    // Resize window to appropriate size
    if(w!=0 && h!=0) window.setSize(w, h);
    else window.pack();
    
    // Get screen size and insets from Java Toolkit (adjust screen size for screen insets)
    Dimension ssize = Toolkit.getDefaultToolkit().getScreenSize();
    Insets sins = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
    
    // Get screen bounds and initial window bounds (as x, y, w, h)
    int sx = sins.left, sy = sins.top, sw = ssize.width - sx - sins.right, sh = ssize.height - sy - sins.bottom;
    int wx = dx, wy = dy, ww = window.getWidth(), wh = window.getHeight();
    
    // Trim window bounds if wider or taller than screen
    if(ww>sw) ww = sw - 4;
    if(wh>sh) wh = sh - 4;
    
    // Adjust X for Position
    if(aPos!=null) switch(aPos) {
        case TOP_LEFT: case CENTER_LEFT: case BOTTOM_LEFT: wx += sx; break;
        case TOP_CENTER: case CENTER: case BOTTOM_CENTER: wx += sx + (sw - ww)/2; break;
        case TOP_RIGHT: case CENTER_RIGHT: case BOTTOM_RIGHT: wx += sx + sw - ww; break;
    }
    
    // Adjust Y for Position
    if(aPos!=null) switch(aPos) {
        case TOP_LEFT:case TOP_CENTER:case TOP_RIGHT: wy += sy; break;
        case CENTER_LEFT:case CENTER:case CENTER_RIGHT: wy += sy + (sh - wh)/2; break;
        case BOTTOM_LEFT:case BOTTOM_CENTER:case BOTTOM_RIGHT: wy += sy + sh - wh; break;
    }
    
    // Offset window bounds if it overruns any screen border
    if(wx+ww>sx+sw) wx -= ((wx+ww) - (sx+sw));
    if(wy+wh>sy+sh) wy -= ((wy+wh) - (sy+sh));
    if(wx<sx) wx += (sx - wx);
    if(wy<sy) wy += (sy - wy);

    // Set new window bounds and make window visible
    window.setBounds(wx, wy, ww, wh);
    window.setVisible(true);
}

/**
 * Returns the root pane for a window, if available.
 */
public static JRootPane getRootPane(Window aWin)
{
    return aWin instanceof RootPaneContainer? ((RootPaneContainer)aWin).getRootPane() : null;
}

/**
 * Returns the document file for the window title bar proxy icon.
 */
public static File getDocumentFile(Window aWindow)
{
    JRootPane rootPane = getRootPane(aWindow); if(rootPane==null) return null;
    return (File)rootPane.getClientProperty("Window.documentFile");
}

/**
 * Returns the document file for the window title bar proxy icon.
 */
public static void setDocumentFile(Window aWindow, File aFile)
{
    if(SnapUtils.equals(aFile, getDocumentFile(aWindow))) return;
    JRootPane rootPane = getRootPane(aWindow); if(rootPane==null) return;
    rootPane.putClientProperty("Window.documentFile", aFile);
}

/**
 * A component listener to save frame to preferences on move.
 */
private static class FrameSaveComponentListener extends ComponentAdapter {
    String  _frameName; boolean  _doSize;
    public FrameSaveComponentListener(String aName, boolean doSize)  { _frameName = aName; _doSize = doSize; }
    public void componentMoved(ComponentEvent e)  { setFrameString(e); }
    public void componentResized(ComponentEvent e)  { setFrameString(e); }
    private void setFrameString(ComponentEvent e)
    {
        Component win = e.getComponent();
        StringBuffer sb = new StringBuffer().append(win.getX()).append(' ').append(win.getY());
        if(_doSize) sb.append(' ').append(win.getWidth()).append(' ').append(win.getHeight());
        PrefsUtils.prefsPut(_frameName + "Loc", sb.toString());
    }
}

}