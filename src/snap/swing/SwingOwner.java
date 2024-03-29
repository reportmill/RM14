package snap.swing;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import snap.util.*;

/**
 * A base controller class class that manages a JComponent usually loaded from a rib file.
 */
public abstract class SwingOwner extends UIOwner <SwingEvent, JComponent> {

    // An object to manage a Swing window
    SwingWindow              _swin;

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
    // Get resource URL for class
    URL resURL = getUISource(aClass);
    if (resURL == null) {
        System.out.println("SwingOwner.createUI: Rib file not found for " + aClass.getName());
        return null;
    }

    // Create UI and return
    return Swing.createUI(resURL);
}

/**
 * Returns the UI source.
 */
protected URL getUISource(Class<?> aClass)
{
    // Look for .rib file for class name
    String className = aClass.getSimpleName();
    String resName = className + ".rib";
    URL url = aClass.getResource(resName);
    if (url != null)
        return url;

    // Look for .ribs/.rib file for class name
    String resName2 = className + ".ribs/" + resName;
    url = aClass.getResource(resName2);
    if (url != null)
        return url;

    // Get superclass - just return if bogus
    Class<?> superClass = aClass.getSuperclass();
    if (superClass == SwingOwner.class || superClass == Object.class || superClass == null)
        return null;

    // Recurse with superClass
    return getUISource(superClass);
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