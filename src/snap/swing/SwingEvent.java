package snap.swing;
import com.reportmill.shape.RMShape;
import com.reportmill.swing.shape.JComponentShape;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import javax.swing.JComponent;
import javax.swing.event.*;
import snap.util.UIEvent;

/**
 * This class encapsulates information for UI changes, like button clicks, text field entries, list selections, etc..
 */
public class SwingEvent extends UIEvent {

/**
 * Creates a new SwingEvent.
 */
public SwingEvent(EventObject anEvent, JComponent aComponent)  { this(anEvent, aComponent, null); }

/**
 * Creates a new RibsEvent.
 */
public SwingEvent(EventObject anEvent, JComponent aComponent, Type aType)
{
    // So normal version
    super(anEvent, aComponent, aType);
    
    // Set the name
    if(isComponent() && getComponent().getName()!=null) setName(getComponent().getName());
    else if(getShape()!=null && getShape().getName()!=null) setName(getShape().getName());
}

/**
 * Returns the target as shape, if shape.
 */
public RMShape getShape()
{
    if(getTarget() instanceof JComponent)
        return (JComponentShape)getComponent().getClientProperty("JComponentShape");
    return getTarget(RMShape.class);
}

/**
 * Returns whether target is a component.
 */
public boolean isComponent()  { return getTarget() instanceof JComponent || getTarget() instanceof JComponentShape; }

/**
 * Returns the target component, if event target was component shape.
 */
public JComponent getComponent()
{
    if(getTarget() instanceof JComponent) return (JComponent)getTarget();
    if(getShape() instanceof JComponentShape) return ((JComponentShape)getTarget()).getComponent();
    return null;
}

/**
 * Returns the event as an EventObject.
 */
public EventObject getEvent()  { return (EventObject)super.getEvent(); }

/**
 * Computes the event type from EventObject.
 */
protected Type getType(Object anEvent)
{
    int id = anEvent instanceof AWTEvent? ((AWTEvent)anEvent).getID() : 0;
    switch(id) {
        case ActionEvent.ACTION_PERFORMED: return Type.Action;
        case MouseEvent.MOUSE_PRESSED: return Type.MousePressed;
        case MouseEvent.MOUSE_DRAGGED: return Type.MouseDragged;
        case MouseEvent.MOUSE_RELEASED: return Type.MouseReleased;
        case MouseEvent.MOUSE_CLICKED: return Type.MouseClicked;
        case MouseEvent.MOUSE_ENTERED: return Type.MouseEntered;
        case MouseEvent.MOUSE_MOVED: return Type.MouseMoved;
        case MouseEvent.MOUSE_EXITED: return Type.MouseExited;
        case KeyEvent.KEY_PRESSED: return Type.KeyPressed;
        case KeyEvent.KEY_RELEASED: return Type.KeyReleased;
        case KeyEvent.KEY_TYPED: return Type.KeyTyped;
        case FocusEvent.FOCUS_GAINED: return Type.FocusGained;
        case FocusEvent.FOCUS_LOST: return Type.FocusLost;
    }
    if(anEvent instanceof ChangeEvent) return Type.ValueChange;
    if(anEvent instanceof PropertyChangeEvent) { PropertyChangeEvent pce = (PropertyChangeEvent)anEvent;
        return pce.getPropertyName().equals("Value")? Type.ValueChange : Type.PropertyChange; }
    if(anEvent instanceof ListSelectionEvent) return Type.Selection;
    if(anEvent instanceof TreeSelectionEvent) return Type.Selection;
    if(anEvent instanceof DropTargetDropEvent) return Type.DragDrop;
    if(anEvent instanceof DragGestureEvent) return Type.DragGesture;
    return null;
}

/**
 * Return PropertyChangeEvent property name.
 */
protected String getPropertyChangePropertyName()
{
    PropertyChangeEvent pce = getEvent(PropertyChangeEvent.class);
    return pce!=null? pce.getPropertyName() : null;
}

/**
 * Returns whether event is AWT event.
 */
public boolean isAWTEvent()  { return getEvent() instanceof AWTEvent; }

/**
 * Returns the AWT event.
 */
public AWTEvent getAWTEvent()  { return getEvent(AWTEvent.class); }

/**
 * Returns the event id.
 */
public int getEventID()  { return isAWTEvent()? getAWTEvent().getID() : 0; }

/**
 * Returns whether event is input event.
 */
public boolean isInputEvent()  { return getEvent() instanceof InputEvent; }

/**
 * Returns the input event.
 */
public InputEvent getInputEvent()  { return getEvent(InputEvent.class); }

/** Returns whether shift key is down. */
public boolean isShiftDown()  { return getInputEvent().isShiftDown(); }

/** Returns whether control key is down. */
public boolean isControlDown()  { return getInputEvent().isControlDown(); }

/** Returns whether alt key is down. */
public boolean isAltDown()  { return getInputEvent().isAltDown(); }

/** Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows). */
public boolean isMetaDown()  { return getInputEvent().isMetaDown(); }

/** Returns whether command menu shortcut key is down (command on Mac or control on other platforms). */
public boolean isCommandDown()  { return isMenuShortcutKeyPressed(); }

/** Returns whether menu shortcut key is pressed. */
public boolean isMenuShortcutKeyPressed()  { return (getInputEvent().getModifiers() & getMenuShortcutKeyMask())>0; }

/** Returns the menu shortcut key mask. */
public int getMenuShortcutKeyMask()  { return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); }

/**
 * Returns the mouse event.
 */
public MouseEvent getMouseEvent()  { return getEvent(MouseEvent.class); }

/**
 * Returns the click count for a mouse event.
 */
public int getClickCount()  { return isMouseEvent()? getMouseEvent().getClickCount() : 0; }

/**
 * Returns the location for a mouse event or drop event.
 */
public Point getLocation()
{
    if(isMouseEvent())
        return getMouseEvent().getPoint();
    if(isDragDragEvent())
        return getDragDragEvent().getLocation();
    if(isDragDropEvent())
        return getDragDropEvent().getLocation();
    return new Point();
}

/** Returns the mouse event x. */
public int getX()  { return getLocation().x; }

/** Returns the mouse event y. */
public int getY()  { return getLocation().y; }

/**
 * Returns the key event.
 */
public KeyEvent getKeyEvent()  { return getEvent(KeyEvent.class); }

/** Returns the event keycode. */
public int getKeyCode()  { return getKeyEvent()!=null? getKeyEvent().getKeyCode() : 0; }

/**
 * Returns the drop event.
 */
public boolean isDragDragEvent()  { return getDragDragEvent()!=null; }

/**
 * Returns the drop event.
 */
public DropTargetDragEvent getDragDragEvent()  { return getEvent(DropTargetDragEvent.class); }

/**
 * Returns the drop event.
 */
public DropTargetDropEvent getDragDropEvent()  { return getEvent(DropTargetDropEvent.class); }

/**
 * Returns the drop string, if drop event.
 */
public String getDropString()  { return isDragDropEvent()? getStringValue() : null; }

/**
 * Override to provide value for DragDrop event.
 */
public Object getValue()
{
    // Handle DragDropEvent: Get value from transferable
    if(isDragDropEvent())
        return ClipboardUtils.getString(getDragDropEvent().getTransferable());
    
    // Otherwise, do normal version
    return super.getValue();
}

/**
 * Returns the selection event.
 */
public ListSelectionEvent getSelectionEvent()  { return getEvent(ListSelectionEvent.class); }

/**
 * Returns whether event widget or internal event is currently in a continuous state of change.
 */
public boolean getValueIsAdjusting()  { return isValueAdjusting(); }

/**
 * Returns the Color value encapsulated by the event widget.
 */
public Color getColorValue()  { return (Color)getHelper().getValue(getTarget(), "Color"); }

/**
 * Returns the Ribs Helper for event widget.
 */
public SwingHelper getHelper()  { Object t = getTarget(); return t!=null? SwingHelper.getSwingHelper(t) : null; }

/**
 * Consume event.
 */
public void consume()  { if(isInputEvent()) getInputEvent().consume(); }
}