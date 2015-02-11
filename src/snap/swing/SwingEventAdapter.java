package snap.swing;
import java.awt.Color;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import snap.swing.SwingHelpers.JComponentHpr;
import snap.util.*;
import snap.util.UIEvent.Type;

/**
 * A class to listen for Swing events and send them on.
 */
public class SwingEventAdapter extends InputVerifier implements ActionListener, MouseListener, MouseMotionListener,
                             KeyListener, FocusListener, DropTargetListener, DragGestureListener, DragSourceListener {
    // The component
    JComponent        _component;
    
    // The component helper
    JComponentHpr     _helper;
    
    // Bit set of enabled events
    BitSet            _bitset = new BitSet();
    
    // Whether MouseListener is set
    boolean           _mouseListenerSet;

    // Whether MouseMotionListener is set
    boolean           _mouseMotionListenerSet;
    
    // Whether KeyListener is set
    boolean           _keyListenerSet;
    
    // Whether FocusListener is set
    boolean           _focusListenerSet;
    
    // Whether InputVerifier is set
    boolean           _inputVerifierSet;
    
    // The focus gained value
    Object            _focusGainedValue;
    
    // The DropTarget, if set
    DropTarget        _dropTarget;
    
    // The original border of the shape
    Border            _border;
    
    // The border used during drag
    static Border     _dragBorder = BorderFactory.createLineBorder(Color.blue, 2);

/**
 * Creates a new SwingEventAdapter for given component.
 */
public SwingEventAdapter(JComponent aComponent)  { _component = aComponent; }

/**
 * Returns the component.
 */
public JComponent getComponent()  { return _component; }

/**
 * Returns the component helper.
 */
public JComponentHpr getHelper()  { return _helper!=null? _helper : (_helper=Swing.getHelper(_component)); }

/**
 * Returns the component border if it is currently being substituted with a temporary one.
 */
public Border getBorder()  { return _border; }

/**
 * Returns whether given type is enabled.
 */
public boolean isEnabled(UIEvent.Type aType)  { return _bitset.get(aType.ordinal()); }

/**
 * Returns whether any of given types are enabled.
 */
public boolean isEnabled(UIEvent.Type ... theTypes)
{
    boolean enabled = false; for(UIEvent.Type type : theTypes) enabled |= isEnabled(type); return enabled;
}

/**
 * Sets whether a given type is enabled.
 */
public void setEnabled(UIEvent.Type aType, boolean aValue)
{
    // Set bit
    _bitset.set(aType.ordinal(), aValue);
    
    // Check ActionListener
    if(aType==UIEvent.Type.Action) {
        if(aValue) {
            if(_component instanceof AbstractButton) ((AbstractButton)_component).addActionListener(this);
            else if(_component instanceof JComboBox) ((JComboBox)_component).addActionListener(this);
            else if(_component instanceof JTextField) ((JTextField)_component).addActionListener(this);
        }
        else {
            if(_component instanceof AbstractButton) ((AbstractButton)_component).removeActionListener(this);
            else if(_component instanceof JComboBox) ((JComboBox)_component).removeActionListener(this);
            else if(_component instanceof JTextField) ((JTextField)_component).removeActionListener(this);
        }
    }
    
    // Check KeyListener
    if(isKeyListenerNeeded()!=isKeyListenerSet())
        setKeyListenerSet(!isKeyListenerSet());
    
    // Check MouseListener
    if(isMouseListenerNeeded()!=isMouseListenerSet())
        setMouseListenerSet(!isMouseListenerSet());
    
    // Check MouseMotionListener
    if(isMouseMotionListenerNeeded()!=isMouseMotionListenerSet())
        setMouseMotionListenerSet(!isMouseMotionListenerSet());

    // Check FocusListener
    if(isFocusListenerNeeded()!=isFocusListenerSet())
        setFocusListenerSet(!isFocusListenerSet());
    
    // Check DropTargetListener
    boolean dropTargetListenerSet = _dropTarget!=null;
    if(isDropTargetListenerNeeded()!=dropTargetListenerSet) {
        if(dropTargetListenerSet)
            _component.setDropTarget(_dropTarget=null);
        else _dropTarget = new DropTarget(_component, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    // Check DragSourceDrag
    if(aType==Type.DragGesture) {
        DragSource ds = DragSource.getDefaultDragSource();
        ds.createDefaultDragGestureRecognizer(_component, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
}

/**
 * Returns an array of enabled events.
 */
public Type[] getEnabledEvents()
{
    List <Type> types = new ArrayList();
    for(Type type : Type.values()) if(isEnabled(type)) types.add(type);
    return types.toArray(new Type[types.size()]);
}

/**
 * Sets an array of enabled events.
 */
public void setEnabledEvents(Type ... theEvents)
{
    for(Type type : theEvents) setEnabled(type, true);
}

/**
 * Returns whether KeyListener is needed.
 */
private boolean isKeyListenerNeeded()
{
    return isEnabled(Type.KeyPressed, Type.KeyTyped, Type.KeyReleased, Type.KeyFinished);
}

/**
 * Returns whether KeyListener is set.
 */
protected boolean isKeyListenerSet()  { return _keyListenerSet; }

/**
 * Sets whether KeyListener is set.
 */
protected void setKeyListenerSet(boolean aValue)
{
    if(aValue==_keyListenerSet) return;
    if(aValue) _component.addKeyListener(this);
    else _component.removeKeyListener(this);
    _keyListenerSet = aValue;
}

/**
 * Returns whether MouseListener is needed.
 */
private boolean isMouseListenerNeeded()
{
    return isEnabled(Type.MousePressed, Type.MouseReleased, Type.MouseClicked,
            Type.MouseEntered, Type.MouseExited, Type.MouseFinished);
}

/**
 * Returns whether MouseListener is set.
 */
protected boolean isMouseListenerSet()  { return _mouseListenerSet; }

/**
 * Sets whether MouseListener is set.
 */
protected void setMouseListenerSet(boolean aValue)
{
    if(aValue==_mouseListenerSet) return;
    if(aValue) _component.addMouseListener(this);
    else _component.removeMouseListener(this);
    _mouseListenerSet = aValue;
}

/**
 * Returns whether MouseMotionListener is needed.
 */
private boolean isMouseMotionListenerNeeded()  { return isEnabled(Type.MouseDragged, Type.MouseMoved); }

/**
 * Returns whether MouseMotionListener is set.
 */
protected boolean isMouseMotionListenerSet()  { return _mouseMotionListenerSet; }

/**
 * Sets whether MouseMotionListener is set.
 */
protected void setMouseMotionListenerSet(boolean aValue)
{
    if(aValue==_mouseMotionListenerSet) return;
    if(aValue) _component.addMouseMotionListener(this);
    else _component.removeMouseMotionListener(this);
    _mouseMotionListenerSet = aValue;
}

/**
 * Returns whether FocusListener is needed.
 */
private boolean isFocusListenerNeeded()
{
    return isInputVerifierSet() || isEnabled(Type.FocusGained, Type.FocusLost);
}

/**
 * Returns whether FocusListener is set.
 */
protected boolean isFocusListenerSet()  { return _focusListenerSet; }

/**
 * Sets whether FocusListener is set.
 */
protected void setFocusListenerSet(boolean aValue)
{
    if(aValue==_focusListenerSet) return;
    if(!_focusListenerSet) _component.addFocusListener(this);
    else _component.removeFocusListener(this);
    _focusListenerSet = aValue;
}

/**
 * Returns whether DropTargetListener is needed.
 */
private boolean isDropTargetListenerNeeded()
{
    return isEnabled(Type.DragEnter, Type.DragOver, Type.DragExit, Type.DragDrop);
}

/**
 * ActionListener method.
 */
public void actionPerformed(ActionEvent e)
{
    sendEvent(e, null);
    _focusGainedValue = isFocusListenerNeeded()? getHelper().getValue(_component, "Value") : null;
}

/**
 * KeyListener method.
 */
public void keyPressed(KeyEvent e)  { if(isEnabled(Type.KeyPressed)) sendEvent(e, null); }

/**
 * KeyListener method.
 */
public void keyTyped(KeyEvent e)  { if(isEnabled(Type.KeyTyped)) sendEvent(e, null); }

/**
 * KeyListener method.
 */
public void keyReleased(final KeyEvent e)
{
    if(isEnabled(Type.KeyReleased)) sendEvent(e, null);
    if(isEnabled(Type.KeyFinished))
        SwingUtilities.invokeLater(new Runnable() { public void run() { sendEvent(e, Type.KeyFinished); }});
}

/**
 * MouseListener method.
 */
public void mousePressed(MouseEvent e)  { if(isEnabled(Type.MousePressed)) sendEvent(e, null); }

/**
 * MouseMotionListener method.
 */
public void mouseDragged(MouseEvent e)  { if(isEnabled(Type.MouseDragged)) sendEvent(e, null); }

/**
 * MouseListener method.
 */
public void mouseReleased(final MouseEvent e)
{
    if(isEnabled(Type.MouseReleased)) sendEvent(e, null);
    if(isEnabled(Type.MouseFinished))
        SwingUtilities.invokeLater(new Runnable() { public void run() { sendEvent(e, Type.MouseFinished); }});
}

/**
 * MouseListener method.
 */
public void mouseClicked(MouseEvent e)  { if(isEnabled(Type.MouseClicked)) sendEvent(e, null); }

/**
 * MouseListener method.
 */
public void mouseEntered(MouseEvent e)  { if(isEnabled(Type.MouseEntered)) sendEvent(e, null); }

/**
 * MouseMotionListener method.
 */
public void mouseMoved(MouseEvent e)  { if(isEnabled(Type.MouseMoved)) sendEvent(e, null); }

/**
 * MouseListener method.
 */
public void mouseExited(MouseEvent e)  { if(isEnabled(Type.MouseExited)) sendEvent(e, null); }

/**
 * Implements focusGained to cache value prior to editing, so we know whether to fire action on focusLost/verify.
 */
public void focusGained(FocusEvent e)
{
    // Set focus gained value
    _focusGainedValue = getHelper().getValue(_component, "Value");
    
    // Send event
    if(isEnabled(Type.FocusGained)) sendEvent(e, null);
}

/**
 * Implements focus lost to verify (almost certainly not needed, since verify is called prior to focus lost).
 */
public void focusLost(FocusEvent e)
{
    // Send event
    if(isEnabled(Type.FocusLost)) sendEvent(e, null);
    
    // Do verify
    verify((JComponent)e.getComponent());
    _focusGainedValue = null;
}

/**
 * Returns the value of the component when focus gained.
 */
public Object getFocusGainedValue()  { return _focusGainedValue; }

/**
 * Returns whether input verifier is set.
 */
public boolean isInputVerifierSet()  { return _inputVerifierSet; }

/**
 * Sets Input verifier.
 */
public void setInputVerifier(boolean aValue)
{
    // If already set, just return
    if(aValue==_inputVerifierSet) return;
    
    // Set InputVerifierSet and or remove InputVerifier
    _inputVerifierSet = aValue;
    _component.setInputVerifier(_inputVerifierSet? this : null);
    
    // Check FocusListener
    if(isFocusListenerNeeded()!=_focusListenerSet) {
        if(!_focusListenerSet)
            _component.addFocusListener(this);
        else _component.removeFocusListener(this);
        _focusListenerSet = !_focusListenerSet;
    }
}

/**
 * Verify.
 */
public boolean verify(JComponent aComponent)
{
    // Get component value
    Object value = getHelper().getValue(aComponent, "Value");
    if(value instanceof String && _focusGainedValue==null) _focusGainedValue = "";

    // If value has changed, send action
    if(!SnapUtils.equals(value, _focusGainedValue)) {
        PropertyChangeEvent pce = new PropertyChangeEvent(aComponent, "Value", _focusGainedValue, value);
        sendEvent(pce, null);
        _focusGainedValue = value;
    }
    
    // Return true (bogus little test value there - wonder if this'll annoy someone someday)
    return !"rmverify!".equals(value);
}

/**
 * Drop target listener method.
 */
public void dragEnter(DropTargetDragEvent anEvent)
{
    // Accept Event
    anEvent.acceptDrag(DnDConstants.ACTION_COPY);
    
    // Cache & replace border (with blue 2 pixel border)
    Border border = _component.getBorder(); if(border!=_dragBorder) _border = border;
    _component.setBorder(_dragBorder);

    // Send Event
    if(isEnabled(Type.DragEnter))
        sendEvent(anEvent, Type.DragEnter);
}

/**
 * Drop target listener method.
 */
public void dragOver(DropTargetDragEvent anEvent)
{
    // Accept Event
    anEvent.acceptDrag(DnDConstants.ACTION_COPY);

    // Send Event
    if(isEnabled(Type.DragOver))
        sendEvent(anEvent, Type.DragOver);
}

/**
 * Drop target listener method.
 */
public void dropActionChanged(DropTargetDragEvent anEvent)  { }

/**
 * Drop target listener method.
 */
public void dragExit(DropTargetEvent anEvent)
{
    // Reset border
    _component.setBorder(_border); _border = null;
    
    // Send Event
    if(isEnabled(Type.DragExit))
        sendEvent(anEvent, Type.DragExit);
}

/**
 * Drop target listener method.
 */
public void drop(final DropTargetDropEvent anEvent)
{
    // Reset border
    _component.setBorder(_border); _border = null;
    
    // Accept Event
    anEvent.acceptDrop(DnDConstants.ACTION_COPY);
    
    // Send Event
    if(isEnabled(Type.DragDrop)) sendEvent(anEvent, null);

    // Formally complete drop
    anEvent.dropComplete(true);
}

/**
 * Called when drag should be initiated due to mouse drag. 
 */
public void dragGestureRecognized(DragGestureEvent anEvent)
{
    // Send Event
    if(isEnabled(Type.DragGesture))
        sendEvent(anEvent, Type.DragGesture);
}

/**
 * DragSourceListener method.
 */
public void dragEnter(DragSourceDragEvent anEvent)
{
    if(isEnabled(Type.DragSourceEnter))
        sendEvent(anEvent, Type.DragSourceEnter);
}

/**
 * DragSourceListener method.
 */
public void dragOver(DragSourceDragEvent anEvent)
{
    if(isEnabled(Type.DragSourceEnter))
        sendEvent(anEvent, Type.DragSourceEnter);
}

/**
 * DragSourceListener method.
 */
public void dropActionChanged(DragSourceDragEvent anEvent)  { }

/**
 * DragSourceListener method.
 */
public void dragExit(DragSourceEvent anEvent)
{
    if(isEnabled(Type.DragSourceExit))
        sendEvent(anEvent, Type.DragSourceExit);
}

/**
 * DragSourceListener method.
 */
public void dragDropEnd(DragSourceDropEvent anEvent)
{
    if(isEnabled(Type.DragSourceEnd))
        sendEvent(anEvent, Type.DragSourceEnd);
}

/**
 * Sends the given event.
 */
private void sendEvent(EventObject anEvent, Type aType)
{
    if(anEvent instanceof InputEvent) { InputEvent ie = (InputEvent)anEvent; if(ie.isConsumed()) return; }
    SwingEvent event = new SwingEvent(anEvent, _component, aType);
    UIOwner owner = getHelper().getOwner(_component);
    owner.sendEvent(event);
}

}