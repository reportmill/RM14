package snap.util;
import com.reportmill.base.RMKeyChain;
import java.text.DateFormat;
import java.util.*;

/**
 * A base controller class class that manages a SwingPanel usually loaded from a rib file.
 */
public abstract class UIOwner <EVENT extends UIEvent, UITYPE> {

    // The UI panel
    UITYPE                  _ui;  boolean  _uiSet;
    
    // A runnable to reset later
    Runnable                _resetLaterRunnable;
    
    // The first UI node to focus when UI is made visible in window or dialog
    Object                  _firstFocus;
    
    // A map of binding values not explicitly defined in model
    Map                     _modelValues = new HashMap();
    
    // A map of maps that perform value conversions
    Map <String, Map>       _conversionMaps = new HashMap();
    
    // Map of RunOne runnables
    Map <String,Runnable>   _runOnceMap = new HashMap();
    
    // Convenience for common events
    public static final UIEvent.Type KeyPressed = UIEvent.Type.KeyPressed;
    public static final UIEvent.Type KeyReleased = UIEvent.Type.KeyReleased;
    public static final UIEvent.Type KeyTyped = UIEvent.Type.KeyTyped;
    public static final UIEvent.Type KeyFinished = UIEvent.Type.KeyFinished;
    public static final UIEvent.Type MousePressed = UIEvent.Type.MousePressed;
    public static final UIEvent.Type MouseDragged = UIEvent.Type.MouseDragged;
    public static final UIEvent.Type MouseReleased = UIEvent.Type.MouseReleased;
    public static final UIEvent.Type MouseClicked = UIEvent.Type.MouseClicked;
    public static final UIEvent.Type MouseFinished = UIEvent.Type.MouseFinished;
    public static final UIEvent.Type MouseEntered = UIEvent.Type.MouseEntered;
    public static final UIEvent.Type MouseMoved = UIEvent.Type.MouseMoved;
    public static final UIEvent.Type MouseExited = UIEvent.Type.MouseExited;
    public static final UIEvent.Type DragEnter = UIEvent.Type.DragEnter;
    public static final UIEvent.Type DragOver = UIEvent.Type.DragOver;
    public static final UIEvent.Type DragExit = UIEvent.Type.DragExit;
    public static final UIEvent.Type DragDrop = UIEvent.Type.DragDrop;
    public UIEvent.Type KeyEvents[] = { KeyPressed, KeyReleased, KeyTyped };
    public UIEvent.Type MouseEvents[] = { MousePressed, MouseDragged, MouseReleased,
        MouseClicked, MouseEntered, MouseMoved, MouseExited };
    public UIEvent.Type DragEvents[] = { DragEnter, DragExit, DragOver, DragDrop };

/**
 * Returns whether UI has been set.
 */
public boolean isUISet()  { return _uiSet; }

/**
 * Returns the main UI node.
 */
public synchronized UITYPE getUI()
{
    // If UI not present, create, init and set
    if(_ui==null) {
        _ui = createUI();
        initUI();
        initUI(_ui);
        resetLater(); _uiSet = true;
    }
    
    // Return UI
    return _ui;
}

/**
 * Returns the main UI node with the given name as the given class.
 */
public <T extends UITYPE> T getUI(Class <T> aClass)  { return ClassUtils.getInstance(getUI(), aClass); }

/**
 * Creates the UI panel.
 */
protected abstract UITYPE createUI();

/**
 * Initializes the UI panel.
 */
protected void initUI()  { }

/**
 * Initialize UI.
 */
protected void initUI(Object anObj)  { getNodeHelper(anObj).initUIDeep(anObj, this); }

/**
 * Reset UI controls.
 */
protected void resetUI()  { }

/**
 * Respond to UI controls.
 */
protected void respondUI(EVENT anEvent)  { }

/**
 * Resets UI later.
 */
public synchronized void resetLater()
{
    if(_resetLaterRunnable==null) {
        _resetLaterRunnable = new Runnable() { public void run() {
            _resetLaterRunnable = null;
            processResetUI();
        }};
        runLater(_resetLaterRunnable);
    }
}

/**
 * Called to reset bindings and resetUI().
 */
protected void processResetUI()
{
    boolean old = setSendEventDisabled(true);
    try {
        resetNodeBindings(getUI()); // Reset bindings
        this.resetUI();
    }
    finally { setSendEventDisabled(old); }
}

/**
 * Called to invoke respondUI().
 */
protected void processRespondUI(EVENT anEvent)
{
    // Get binding for property name and have it retrieve value
    Object node = anEvent.getTarget();
    UIHelper helper = getNodeHelper(node);
    Binding binding = helper.getBinding(node, anEvent.getPropertyName());
    if(binding!=null)
        setBindingModelValue(binding);
    if(anEvent.isActionEvent() && helper.getAction(node)!=null)
        sendNodeAction(node, helper.getAction(node));

    // Call main Owner.respondUI method
    this.respondUI(anEvent);
}

/**
 * Returns the specific UI child node with the given name.
 */
public UITYPE getNode(String aName)
{
    Object obj = getUIHpr().getChild(getUI(), aName); //if(obj==null) getUIMissing(aName);
    return (UITYPE)obj;
}

/**
 * Returns the specific UI child node for given object (name, event or node).
 */
protected UITYPE getNode(Object anObj)
{
    if(anObj instanceof String) return getNode((String)anObj);
    if(anObj instanceof UIEvent) return (UITYPE)((UIEvent)anObj).getTarget();
    return (UITYPE)anObj;
}

/**
 * Returns the specific UI child node with the given name as the given class.
 */
public <T extends UITYPE> T getNode(String aName, Class <T> aClass)
{
    return ClassUtils.getInstance(getNode(aName), aClass);
}

/**
 * Returns the object value for a given name or UI node.
 */
public Object getNodeValue(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getValue(obj, "Value");
}

/**
 * Sets the object value for a given name or UI node.
 */
public void setNodeValue(Object anObj, Object aValue)
{
    Object obj = getNode(anObj);
    boolean old = setSendEventDisabled(true);
    getNodeHelper(obj).setValue(obj, "Value", aValue);
    setSendEventDisabled(old);
}

/**
 * Returns the string value for a given name or UI node.
 */
public String getNodeStringValue(Object anObj)  { return SnapUtils.stringValue(getNodeValue(anObj)); }

/**
 * Returns the boolean value for a given name or UI node.
 */
public boolean getNodeBoolValue(Object anObj)  { return SnapUtils.boolValue(getNodeValue(anObj)); }

/**
 * Returns the int value for a given name or UI node.
 */
public int getNodeIntValue(Object anObj)  { return SnapUtils.intValue(getNodeValue(anObj)); }

/**
 * Returns the float value for a given name or UI node.
 */
public float getNodeFloatValue(Object anObj)  { return SnapUtils.floatValue(getNodeValue(anObj)); }

/**
 * Returns the text value for a given name or UI node.
 */
public String getNodeText(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getText(obj);
}

/**
 * Sets the object value for a given name or UI node.
 */
public void setNodeText(Object anObj, String aValue)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).setText(obj, aValue);
}

/**
 * Returns the items for a given name or UI node.
 */
public List getNodeItems(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getItems(obj);
}

/**
 * Sets the items for a given name or UI node.
 */
public void setNodeItems(Object anObj, List theItems)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).setItems(obj, theItems);
}

/**
 * Sets the items for a given name or UI node.
 */
public void setNodeItems(Object anObj, Object theItems[])
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).setItems(obj, theItems);
}

/**
 * Returns the display key for given name or UI node.
 */
public String getNodeItemDisplayKey(Object anObj, String aKey)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getItemDisplayKey(obj);
}

/**
 * Sets the display key for given name or UI node.
 */
public void setNodeItemDisplayKey(Object anObj, String aKey)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).setItemDisplayKey(obj, aKey);
}

/**
 * Returns the selected index for given name or UI node.
 */
public int getNodeSelectedIndex(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getSelectedIndex(obj);
}

/**
 * Sets the selected index for given name or UI node.
 */
public void setNodeSelectedIndex(Object anObj, int aValue)
{
    Object obj = getNode(anObj);
    boolean old = setSendEventDisabled(true);
    getNodeHelper(obj).setSelectedIndex(obj, aValue);
    setSendEventDisabled(old);
}

/**
 * Returns the selected item for given name or UI node.
 */
public Object getNodeSelectedItem(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).getSelectedItem(obj);
}

/**
 * Sets the selected item for given name or UI node.
 */
public void setNodeSelectedItem(Object anObj, Object anItem)
{
    Object obj = getNode(anObj);
    boolean old = setSendEventDisabled(true);
    getNodeHelper(obj).setSelectedItem(obj, anItem);
    setSendEventDisabled(old);
}

/**
 * Returns whether given name or UI node is enabled.
 */
public void isNodeEnabled(Object anObj)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).isEnabled(obj);
}

/**
 * Sets whether given name or UI node is enabled.
 */
public void setNodeEnabled(Object anObj, boolean aValue)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).setEnabled(obj, aValue);
}

/**
 * Returns whether given name or UI node is currently being modified.
 */
public boolean isNodeValueAdjusting(Object anObj)
{
    Object obj = getNode(anObj);
    return getNodeHelper(obj).isValueAdjusting(obj);
}

/**
 * Sets the node children.
 */
public void setNodeChildren(Object anObj, UITYPE ... theChildren)  { }

/**
 * Returns the helper for a given UI node.
 */
protected abstract UIHelper getNodeHelper(Object anObj); //  { return Swing.getHelper(anObj); }

/**
 * Returns a node action.
 */
public String getNodeAction(Object anObj)
{
    Object obj = getNode(anObj);
    UIHelper helper = getNodeHelper(obj);
    return helper.getAction(obj);
}

/**
 * Sets a node action.
 */
public void setNodeAction(Object anObj, String anAction)
{
    Object obj = getNode(anObj);
    UIHelper helper = getNodeHelper(obj);
    helper.setAction(obj, anAction);
}

/**
 * Sends a node action.
 */
protected void sendNodeAction(Object anObj, String anAction)  { RMKeyChain.getValue(this, anAction); }

/**
 * Adds a binding to a UI node.
 */
public void addNodeBinding(Object anObj, String aPropertyName, String aKeyPath)
{
    Object obj = getNode(anObj);
    UIHelper helper = getNodeHelper(obj);
    String pname = helper.getPropertyNameMapped(obj, aPropertyName);
    helper.addBinding(obj, new Binding(pname, aKeyPath));
}

/**
 * Reset bindings for UI node (recurses for children).
 */
protected void resetNodeBindings(Object anObj)
{
    // If Owner of node doesn't match, just return
    UIHelper helper = getNodeHelper(anObj); if(helper.isValueAdjusting(anObj)) return;
    Object owner = helper.getOwner(anObj); if(owner!=this) return;
    
    // Iterate over node bindings and reset
    for(int i=0, iMax=helper.getBindingCount(anObj); i<iMax; i++) { Binding binding = helper.getBinding(anObj, i);
        setBindingNodeValue(binding); }
    
    // Iterate over node children and recurse
    for(int i=0, iMax=helper.getChildCount(anObj); i<iMax; i++) { Object child = helper.getChild(anObj, i);
        resetNodeBindings(child); }
}

/**
 * Returns the UI node value for the given binding.
 */
protected Object getBindingNodeValue(Binding aBinding)
{
    // Get value from UI node
    UIHelper helper = aBinding.getHelper(); Object node = aBinding.getNode();
    Object value = helper.getValue(node, aBinding.getPropertyName());
    
    // If conversion key is present, do conversion
    String convKey = aBinding.getConversionKey();
    if(convKey!=null)
        value = getConversionMapKey(convKey, value);
    
    // If binding format is available, try to parse
    if(aBinding.getFormat()!=null && value instanceof String)
        try { value = aBinding.getFormat().parseObject((String)value); }
        catch(Exception e) { }
    
    // Return value
    return value;
}

/**
 * Sets the UI node value for the given binding from the key value.
 */
protected void setBindingNodeValue(Binding aBinding)
{
    // Get UI node, Helper, PropertyName and Key
    Object node = aBinding.getNode(); UIHelper helper = aBinding.getHelper(); 
    String pname = aBinding.getPropertyName();
    
    // Get value for property name and have helper setValue
    Object value = getBindingModelValue(aBinding);
    helper.setValue(node, pname, value);
}

/**
 * Returns the key value for a given binding.
 */
protected Object getBindingModelValue(Binding aBinding)
{
    // Get binding key and value
    String key = aBinding.getKey();
    Object value = getModelValue(key);
    
    // If conversion key is present, do conversion
    String convKey = aBinding.getConversionKey();
    if(convKey!=null)
        value = getConversionMapValue(convKey, value);
    
    // If format is present, format value
    if(aBinding.getFormat()!=null && value!=null)
        try { value = aBinding.getFormat().format(value); }
        catch(Exception e) { System.err.println("UIOwner.getBindingKeyValue: " + e); }
    
    // This is probably the wrong thing to do - maybe should be in JTextComponentHpr somehow
    if(value instanceof Date)
        value = DateFormat.getDateInstance(DateFormat.MEDIUM).format(value);
    
    // Return value
    return value;
}

/**
 * Sets the key value for the given binding from the UI node.
 */
protected void setBindingModelValue(Binding aBinding)
{
    Object value = getBindingNodeValue(aBinding); // Get value from node
    setModelValue(aBinding.getKey(), value); // Set value in model
}

/**
 * Returns the first focus UI node for when window/dialog is made visible.
 */
public Object getFirstFocus()  { return _firstFocus; }

/**
 * Sets the first focus UI node.
 */
public void setFirstFocus(Object anObj)  { _firstFocus = anObj; }

/**
 * Focuses given UI node (name or node).
 */
public abstract void requestFocus(Object anObj);

/**
 * Returns the map of maps, each of which is used to perform value conversions.
 */
public Map <String,Map> getConversionMaps()  { return _conversionMaps; }

/**
 * Returns a named map to perform value conversions.
 */
public Map <String,String> getConversionMap(String aName)
{
    Map map = _conversionMaps.get(aName);
    if(map==null)
        _conversionMaps.put(aName, map = new HashMap());
    return map;
}

/**
 * Converts a UI node value to binder object value using conversion key map.
 */
protected Object getConversionMapKey(String aConversionMapName, Object aValue)
{
    // Get conversion map (just return original object if null)
    Map <String, String> map = getConversionMap(aConversionMapName); if(map==null) return aValue;
    for(Map.Entry entry : map.entrySet()) // Return key for value (just return original object if null)
        if(entry.getValue().equals(aValue.toString()))
            return entry.getKey();
    return aValue.toString(); // Return original object, since value not found in conversion map
}

/**
 * Converts a binder object value to UI node using conversion key map.
 */
public Object getConversionMapValue(String aConversionMapName, Object aKey)
{
    Map map = getConversionMap(aConversionMapName); if(map==null) return aKey;
    String value = (String)map.get(aKey.toString());
    return value!=null? value : aKey.toString();
}

/**
 * Enables events on given object.
 */
public void enableEvents(Object anObj, UIEvent.Type ... theTypes)
{
    Object obj = getNode(anObj);
    getNodeHelper(obj).enableEvents(obj, theTypes);
}

/**
 * Enables events on given object.
 */
public void disableEvents(Object anObj, UIEvent.Type ... theTypes)
{
    Object obj = getNode(anObj); UIHelper helper = getNodeHelper(obj);
    for(UIEvent.Type type : theTypes) helper.setEnabled(anObj, type, false);
}

/**
 * Sends an event for a UI node (name or node).
 */
public abstract void sendEvent(Object anObj);

/**
 * Sends an event for a UI node.
 */
public void sendEvent(final EVENT anEvent)
{
    // If send event is disabled, just return
    if(isSendEventDisabled()) return;
    
    // If no special callback, just call respondUI
    setSendEventDisabled(true);
    try { processRespondUI(anEvent); }
    finally { setSendEventDisabled(false); }
    
    // Trigger UI reset
    if(anEvent.getTriggersReset())
        resetLater();
}

/**
 * Returns whether Ribs' send event facility is disabled (so controls can be updated without triggering response).
 */
public boolean isSendEventDisabled()  { return _sendEventDisabled; } boolean _sendEventDisabled;

/**
 * Sets whether Ribs' send event facility is disabled (so controls can be updated without triggering response).
 */
public boolean setSendEventDisabled(boolean aFlag)
{
    boolean old = _sendEventDisabled; _sendEventDisabled = aFlag; return old;
}

/**
 * Configures an ActionEvent to be sent to owner for given name and key description (in KeyStroke string format).
 * @see javax.swing.KeyStroke
 */
public abstract void addKeyActionEvent(String aName, String aKey);

/**
 * Runs the given runnable in the next event.
 */
public abstract void runLater(Runnable aRunnable);

/**
 * Runs the runnable after the given delay in milliseconds.
 */
public void runLaterDelayed(int aDelay, final Runnable aRunnable)
{
    TimerTask task = new TimerTask() { public void run()  { runLater(aRunnable); }};
    new Timer().schedule(task, aDelay);
}

/**
 * Invokes the given runnable for name once (cancels unexecuted previous runLater registered with same name).
 */
public void runLaterOnce(String aName, Runnable aRunnable)
{
    synchronized (_runOnceMap) {
        RunLaterRunnable runnable = (RunLaterRunnable)_runOnceMap.get(aName);
        if(runnable==null) {
            _runOnceMap.put(aName, runnable = new RunLaterRunnable(aName, aRunnable));
            runLater(runnable);
        }
        else runnable._runnable = aRunnable;
    }
}

/**
 * A wrapper Runnable for RunLaterOnce. 
 */
private class RunLaterRunnable implements Runnable {
    String _name; Runnable _runnable;
    RunLaterRunnable(String aName, Runnable aRunnable)  { _name = aName; _runnable = aRunnable; }
    public void run()
    {
        Runnable runnable;
        synchronized (_runOnceMap) { _runOnceMap.remove(_name); runnable = _runnable; }
        if(runnable!=null) runnable.run();
    }
}

/**
 * Returns whether current thread is event thread.
 */
protected abstract boolean isEventThread();

/** Returns the helper for a given object. */
protected UIHelper getUIHpr() { return _uih!=null? _uih : (_uih=getNodeHelper(getUI())); } UIHelper _uih;

/**
 * Returns the model value for given key expression from this UIOwner.
 */
public Object getModelValue(String aKey)
{
    Object value = RMKeyChain.getValue(this, aKey);
    if(value==null) value = RMKeyChain.getValue(_modelValues, aKey);
    return value;
}

/**
 * Sets the model value for given key expression and value for this UIOwner.
 */
public void setModelValue(String aKey, Object aValue)
{
    try { RMKeyChain.setValue(this, aKey, aValue); }
    catch(Exception e) { RMKeyChain.setValue(_modelValues, aKey, aValue); }
}

}