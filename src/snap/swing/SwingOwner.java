package snap.swing;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import snap.util.*;
import snap.web.*;

/**
 * A base controller class class that manages a JComponent usually loaded from a rib file.
 */
public abstract class SwingOwner extends UIOwner <SwingEvent, JComponent> {

    // An object to manage a Swing window
    SwingWindow              _swin;

    // Map of known timers
    Map <String,SwingTimer>  _timers = new HashMap();
    
/**
 * Returns the main UI node.
 */
public JComponent getUI()  { return super.getUI(); }

/**
 * Creates the UI panel.
 */
protected JComponent createUI()  { return createUI(getClass()); }

/**
 * Creates the UI panel.
 */
protected JComponent createUI(Class aClass)
{
    // Get ResourceURL for given class along with the class able to resolved ResourceURL
    WebFile file = null; Class rclass = aClass; 
    while(rclass!=Object.class) { String sname = rclass.getSimpleName();
        WebURL durl = WebURL.getURL(rclass, null); WebFile dfile = durl.getFile().getParent();
        file = dfile.getFile(sname + ".rib"); if(file!=null) break;
        file = dfile.getFile(sname + ".ribs/" + sname + ".rib"); if(file!=null) break;
        rclass = rclass.getSuperclass();
    }

    // Create UI from WebFile
    return Swing.createUI(file);
}

/**
 * Override to include Window MenuBar.
 */
public JComponent getNode(String aName)
{
    JComponent ui = super.getNode(aName);
    if(ui==null && getWindow().getMenuBar()!=null) { JMenuBar mb = getWindow().getMenuBar();
        ui = (JComponent)getNodeHelper(mb).getChild(mb, aName); }
    return ui;
}

/**
 * Returns the specific UI child node with the given name as the given class.
 */
public <T extends JComponent> T getNode(String aName, Class <T> aClass)  { return super.getNode(aName, aClass); }

/**
 * Calls RespondUI.
 */
protected void processRespondUI(SwingEvent anEvent)  { super.processRespondUI(anEvent); }

/**
 * Respond to UI controls.
 */
protected void respondUI(SwingEvent anEvent)  { super.respondUI(anEvent); }

/**
 * Returns the helper for a given object.
 */
public SwingHelper getNodeHelper(Object anObj)  { return SwingHelper.getSwingHelper(anObj); }

/**
 * Sets the node children.
 */
public void setNodeChildren(Object anObj, JComponent ... theChildren)
{
    // If children already set, just return
    JComponent node = getNode(anObj); int ccount = node.getComponentCount(); boolean same = ccount==theChildren.length;
    if(same) for(int i=0; i<ccount; i++) if(node.getComponent(i)!=theChildren[i]) same = false; if(same) return;
    
    // Remove current children, set new children, revalidate, repaint and return
    node.removeAll();
    for(JComponent child : theChildren) node.add(child);
    node.revalidate(); node.repaint();
}

/**
 * Focuses given component.
 */
public void requestFocus(final Object anObj)
{
    runLater(new Runnable() { public void run() { runLater(new Runnable() { public void run() {
    runLater(new Runnable() { public void run() { runLater(new Runnable() { public void run() {
        requestFocusImpl(anObj);
    }});}});}});}});
}

/**
 * Actual request focus implementation.
 */
protected void requestFocusImpl(Object anObj)
{
    JComponent comp = getNode(anObj); if(comp==null) return;
    comp.requestFocusInWindow();
    if(comp instanceof JTextField) { JTextField textField = (JTextField)comp;
        textField.selectAll(); }
}

/**
 * Configures an ActionEvent to be sent to owner for given name and key description (in KeyStroke string format).
 * @see javax.swing.KeyStroke
 */
public void addKeyActionEvent(String aName, String aKey)
{
    // Get (converted) key, InputMap and KeyStroke
    String key = SnapUtils.isWindows? aKey.replace("meta ", "control ").replace("META", "CONTROL") : aKey;
    InputMap inputMap = getUI().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
    if(keyStroke==null) throw new RuntimeException("SwingOwner.addKeyActionEvent: Can't parse key: " + aKey);
    
    // Add KeyStroke to input map and Action to ActionMap
    inputMap.put(keyStroke, aName);
    getUI().getActionMap().put(aName, new AbstractAction(aName) { public void actionPerformed(ActionEvent e) {
        SwingEvent event = new SwingEvent(e, getUI());
        event.setName((String)getValue(Action.NAME));
        sendEvent(event);
    }});
}

/**
 * Returns the SwingWindow to manage this SwingOwner's window.
 */
public SwingWindow getWindow()  { return _swin!=null? _swin : (_swin=createWindow()); }

/**
 * Creates a window for this panel from window class.
 */
protected SwingWindow createWindow()
{
    SwingWindow swin = new SwingWindow();
    swin.setContentPaneCall(new Callable() { public JComponent call() { return getUI(); }});
    return swin;
}

/**
 * Returns whether window is visible.
 */
public boolean isWindowVisible()  { return getWindow().isVisible(); }

/**
 * Sets whether window is visible.
 */
public void setWindowVisible(boolean aValue)  { getWindow().setVisible(aValue); }

/**
 * Returns a timer for given name.
 */
public synchronized SwingTimer getTimer(String aName)
{
    SwingTimer timer = _timers.get(aName);
    if(timer==null)
        _timers.put(aName, timer = createTimer().init(this, aName, 50));
    return timer;
}

/**
 * Returns a timer for given name and interval (in milliseconds).
 */
public SwingTimer getTimer(String aName, int aPeriod)
{
    SwingTimer timer = getTimer(aName); timer.setPeriod(aPeriod); return timer;
}

/**
 * Override to return SwingTimer.
 */
protected SwingTimer createTimer()  { return new SwingTimer(); }

/**
 * Runs the given runnable in the next event.
 */
public void runLater(Runnable aRunnable)  { SwingUtilities.invokeLater(aRunnable); }

/**
 * Returns whether current thread is event thread.
 */
protected boolean isEventThread()  { return SwingUtilities.isEventDispatchThread(); }

/**
 * Sends an event for a component.
 */
public void sendEvent(Object anObj)
{
    final JComponent comp = getNode(anObj);
    runLater(new Runnable() { public void run() {
        Swing.sendEvent(new ChangeEvent(comp)); } });
}

}