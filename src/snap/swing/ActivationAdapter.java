package snap.swing;
import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;

/**
 * An AWTEventListener to listen for app activate/deactivate and modal dialog activate/deactivate to hide or show
 * hide-on-deactivate windows and disable always-on-top for always-on-top windows.
 */
public class ActivationAdapter implements AWTEventListener {

    // The window
    Window      _window;
    
    // Whether window is AlwaysOnTop
    boolean     _windowAlwaysOnTop;
    
    // Whether window hides on deactivate
    boolean     _windowHidesOnDeactivate;

    // Whether adapter closed the window
    boolean     _adapterClosedWindow;
    
    // Whether app is active
    boolean     _activeApp = true;
    
    // Whether app has active window
    boolean     _activeWin = true;
    
/**
 * Creates a new activation adaptor for given window.
 */
public ActivationAdapter(Window aWindow, boolean hideOnDeactivate)
{
    _window = aWindow;
    _windowHidesOnDeactivate = hideOnDeactivate;
}

/**
 * Sets the adapter to be installed or removed from toolkit.
 */
public void setEnabled(boolean aValue)
{
    if(aValue)
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
    else Toolkit.getDefaultToolkit().removeAWTEventListener(this);
}

/**
 * AWTEventListener method to catch window activated/deactivated and interpret as app/modal activate/deactivate.
 */
public void eventDispatched(AWTEvent anEvent)
{
    // Get window event, window, eventID and isModal
    WindowEvent windowEvent = (WindowEvent)anEvent;
    final Window window = windowEvent.getWindow();
    boolean isModal = window instanceof Dialog && ((Dialog)window).isModal();
    
    // Handle different event types
    switch(anEvent.getID()) {

        // If no active window and event is window activated, activate app
        case WindowEvent.WINDOW_ACTIVATED:
            
            // If no active window
            if(!_activeWin) {
    
                // Set flag
                _activeWin = true;
                
                // If app was deactivated, set activated, notify AppActivated and re-assert window front
                if(!_activeApp) {
                    _activeApp = true;
                    appActivated();
                    SwingUtilities.invokeLater(new Runnable() { public void run() {
                        window.toFront(); }});
                }
            }
            break;
    
        // If window deactivate event, set app active to false an register for deactivate notify check
        case WindowEvent.WINDOW_DEACTIVATED:
    
            // Mark ActiveWin false and trigger callback to check if another win gets activated after this deactivate 
            // Run this after delay - if still no active window, window is still deactivated, deactivate app
            if(windowEvent.getOppositeWindow()==null) {
                _activeWin = false;
                Runnable runnable = new Runnable() { public void run() {
                    if(!_activeWin) { _activeApp = false; appDeactivated(); }}};
                SwingUtils.invokeLater(runnable, 3);
            }
            break;
    
        // Handle Modal Dialog Window opened
        case WindowEvent.WINDOW_OPENED: if(isModal) modalActivated(); break;
    
        // Handle Modal Dialog Window closed
        case WindowEvent.WINDOW_CLOSED: if(isModal) modalDeactivated(); break;
    }
}

/**
 * Called when app activates.
 */
public void appActivated()
{
    // Handle hide on deactivate
    if(_windowHidesOnDeactivate) {
        
        // If adapter previously closed window, re-open it
        if(_adapterClosedWindow) {
            SwingWindow.setVisible(_window, null, _window.getX(), _window.getY(), null, false);
            _adapterClosedWindow = false;
        }
    }
    
    // Handle always on top
    else _window.setAlwaysOnTop(true);
}

/**
 * Called when app deactivates.
 */
public void appDeactivated()
{
    // Handle hide on deactivate
    if(_windowHidesOnDeactivate) {
        
        // If window is visible, hide it and mark that we hid it
        if(_window!=null && _window.isVisible()) {
            _window.setVisible(false);
            _adapterClosedWindow = true;
        }
    }
    
    // Handle always on top
    else _window.setAlwaysOnTop(false);
}

/**
 * Called when a modal window is activated.
 */
public void modalActivated()
{
    if(_window.isAlwaysOnTop() && _window.isVisible()) {
        _window.setAlwaysOnTop(false); _windowAlwaysOnTop = true; }
}

/**
 * Called when a modal window is deactivated.
 */
public void modalDeactivated()
{
    if(_windowAlwaysOnTop) {
        _window.setAlwaysOnTop(true); _windowAlwaysOnTop = false; }
}

}