/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * Represents a UI event sent to a UIOwner.
 */
public abstract class UIEvent {

    // The name of the event (or the name of the Event Target)
    String              _name = "";
    
    // The platform specific event, if available
    Object              _event;

    // The UI control that the current event was directed at
    Object              _target;
    
    // The UI event type
    Type                _type;
    
    // Whether event triggers a UI reset
    boolean             _triggersReset = true;

/**
 * Creates a new UIEvent.
 */
public UIEvent()  { }

/**
 * Creates a new UIEvent with given event, event target and property name.
 */
public UIEvent(Object anEvent, Object aTarget, Type aType)
{
    setEvent(anEvent);
    setTarget(aTarget);
    setType(aType);
}

/**
 * Returns the name of the event (or the name of the Event Target).
 */
public String getName()  { return _name; }

/**
 * Returns the name of the event (or the name of the Event Target).
 */
public void setName(String aName)  { _name = aName!=null? aName : ""; }

/**
 * Returns the platform specific event, if available.
 */
public Object getEvent()  { return _event; }

/**
 * Sets the platform specific event, if available.
 */
protected void setEvent(Object anEvent)  { _event = anEvent; }

/**
 * Returns the platform specific event as given class.
 */
public <T> T getEvent(Class<T> aClass)  { return ClassUtils.getInstance(getEvent(), aClass); }

/**
 * Returns the event type.
 */
public Type getType()  { return _type!=null? _type : (_type=getType(getEvent())); }

/**
 * Sets the event type.
 */
public void setType(Type aType)  { _type = aType; }

/**
 * Returns the UIEventType from given Event object.
 */
protected abstract Type getType(Object anEvent);

/**
 * Returns the event target.
 */
public Object getTarget()  { return _target; }

/**
 * Sets the event target.
 */
protected void setTarget(Object aTarget)  { _target = aTarget; }

/**
 * Returns the target of the event as given class.
 */
public <T> T getTarget(Class<T> aClass)  { return ClassUtils.getInstance(getTarget(), aClass); }

/**
 * Returns whether event is value change event.
 */
public boolean isValueChangeEvent()  { return getType()==Type.ValueChange; }

/**
 * Returns whether event is property change.
 */
public boolean isPropertyChangeEvent()  { return getType()==Type.PropertyChange; }

/**
 * Returns whether event is property change.
 */
protected abstract String getPropertyChangePropertyName();

/**
 * Returns the property name.
 */
public String getPropertyName()
{
    if(isPropertyChangeEvent()) return getPropertyChangePropertyName();
    if(isValueChangeEvent() || isSelectionEvent() || isActionEvent()) return "Value";
    return "";
}

/**
 * Returns whether event is action event.
 */
public boolean isActionEvent()  { return getType()==Type.Action; }

/**
 * Returns whether event is selection event.
 */
public boolean isSelectionEvent()  { return getType()==Type.Selection; }

/**
 * Returns whether event is timer event.
 */
public boolean isTimerEvent()  { return getType()==Type.Timer; }

/**
 * Returns whether event is mouse event.
 */
public boolean isMouseEvent()  //{ return getType()!=null && getType().toString().startsWith("Mouse"); }
{
    Type t = getType();
    return t==Type.MouseEntered || t==Type.MouseMoved || t==Type.MouseExited || t==Type.MousePressed ||
        t==Type.MouseDragged || t==Type.MouseReleased || t==Type.MouseClicked || t==Type.MouseFinished ||
        t==Type.DragGesture;
}

/**
 * Returns whether event is mouse pressed.
 */
public boolean isMousePressed()  { return getType()==Type.MousePressed; }

/**
 * Returns whether event is mouse dragged.
 */
public boolean isMouseDragged()  { return getType()==Type.MouseDragged; }

/**
 * Returns whether event is mouse released.
 */
public boolean isMouseReleased()  { return getType()==Type.MouseReleased; }

/**
 * Returns whether event is mouse clicked.
 */
public boolean isMouseClicked()  { return getType()==Type.MouseClicked; }

/**
 * Returns whether event is mouse finished.
 */
public boolean isMouseFinished()  { return getType()==Type.MouseFinished; }

/**
 * Returns whether event is mouse entered.
 */
public boolean isMouseEntered()  { return getType()==Type.MouseEntered; }

/**
 * Returns whether event is mouse moved.
 */
public boolean isMouseMoved()  { return getType()==Type.MouseMoved; }

/**
 * Returns whether event is mouse exited.
 */
public boolean isMouseExited()  { return getType()==Type.MouseExited; }

/**
 * Returns whether event is key event.
 */
public boolean isKeyEvent()  { return getType()!=null && getType().toString().startsWith("Key"); }

/**
 * Returns whether event is key pressed.
 */
public boolean isKeyPressed()  { return getType()==Type.KeyPressed; }

/**
 * Returns whether event is key released.
 */
public boolean isKeyReleased()  { return getType()==Type.KeyReleased; }

/**
 * Returns whether event is key typed.
 */
public boolean isKeyTyped()  { return getType()==Type.KeyTyped; }

/**
 * Returns whether event is key finished.
 */
public boolean isKeyFinished()  { return getType()==Type.KeyFinished; }

/**
 * Returns whether event is any drag event.
 */
public boolean isDragEvent()
{
    Type t = getType(); return t==Type.DragEnter || t==Type.DragOver || t==Type.DragExit || t==Type.DragDrop;
}

/**
 * Returns whether event is drag enter.
 */
public boolean isDragEnter()  { return getType()==Type.DragEnter; }

/**
 * Returns whether event is drag over.
 */
public boolean isDragOver()  { return getType()==Type.DragOver; }

/**
 * Returns whether event is drag exit.
 */
public boolean isDragExit()  { return getType()==Type.DragExit; }

/**
 * Returns whether event is drop event.
 */
public boolean isDragDropEvent()  { return getType()==Type.DragDrop; }

/**
 * Returns whether event is DragGesture event.
 */
public boolean isDragGesture()  { return getType()==Type.DragGesture; }

/**
 * Returns whether event is DragSourceEnter event.
 */
public boolean isDragSourceEnter()  { return getType()==Type.DragSourceEnter; }

/**
 * Returns whether event is DragSourceExit event.
 */
public boolean isDragSourceExit()  { return getType()==Type.DragSourceExit; }

/**
 * Returns whether event is DragSourceEnd event.
 */
public boolean isDragSourceEnd()  { return getType()==Type.DragSourceEnd; }

/**
 * Returns whether event is FocusGained.
 */
public boolean isFocusGained()  { return getType()==Type.FocusGained; }

/**
 * Returns whether event is FocusLost.
 */
public boolean isFocusLost()  { return getType()==Type.FocusLost; }

/**
 * Returns whether event represents component with given name.
 */
public boolean is(String aName)  { return getName().equals(aName); }

/**
 * Returns whether widget is equal to given name.
 */
public boolean equals(String aName)  { return getName().equals(aName); }

/**
 * Returns the value encapsulated by the event widget.
 */
public Object getValue()  { Object t = getTarget(); return t!=null? getHelper().getValue(t, "Value") : null; }

/**
 * Sets the value encapsulated by the event widget.
 */
public void setValue(Object aValue)  { getHelper().setValue(getTarget(), "Value", aValue); }

/**
 * Returns the String value encapsulated by the event widget.
 */
public String getStringValue()  { return SnapUtils.stringValue(getValue()); }

/**
 * Returns the Boolean value encapsulated by the event widget.
 */
public boolean getBoolValue()  { return SnapUtils.booleanValue(getValue()); }

/**
 * Returns the Boolean value encapsulated by the event widget.
 */
public Boolean getBooleanValue()  { return SnapUtils.booleanValue(getValue()); }

/**
 * Returns the Integer value encapsulated by the event widget.
 */
public Integer getIntValue()  { return SnapUtils.getInteger(getValue()); }

/**
 * Returns the Float value encapsulated by the event widget.
 */
public Float getFloatValue()  { return SnapUtils.getFloat(getValue()); }

/**
 * Returns text for encapsulated widget.
 */
public String getText()  { return getHelper().getText(getTarget()); }

/**
 * Returns the selected index for encapsulated widget.
 */
public int getSelectedIndex()  { return getHelper().getSelectedIndex(getTarget()); }

/**
 * Returns the selected item for encapsulated widget.
 */
public Object getSelectedItem()  { return getHelper().getSelectedItem(getTarget()); }

/**
 * Returns the selected item for encapsulated widget.
 */
public <T> T getSelectedItem(Class<T> aClass)  { return ClassUtils.getInstance(getSelectedItem(), aClass); }

/**
 * Returns the UI Helper for event target.
 */
public abstract UIHelper getHelper();

/**
 * Returns whether event widget or internal event is currently in a continuous state of change.
 */
public boolean isValueAdjusting()
{
    // Check Target
    Object value1 = Key.getValue(getTarget(), "ValueIsAdjusting");
    if(SnapUtils.boolValue(value1))
        return true;
    
    // Otherwise, check Event
    Object value2 = Key.getValue(getEvent(), "ValueIsAdjusting");
    return SnapUtils.boolValue(value2);
}

/**
 * Returns whether this event triggers a UI reset.
 */
public boolean getTriggersReset()  { return _triggersReset; }

/**
 * Sets whether this event triggers a UI reset.
 */
public void setTriggersReset(boolean aValue)  { _triggersReset = aValue; }

/**
 * Consumes the event.
 */
public void consume()  { }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + " " + getName() + " " + getType(); }

/**
 * Types for events.
 */
public enum Type {

    /** Key events. */
    KeyPressed, KeyReleased, KeyTyped, KeyFinished,
    
    /** Mouse events.*/
    MousePressed, MouseDragged, MouseReleased, MouseClicked, MouseFinished,
    
    /** Mouse move events. */
    MouseEntered, MouseMoved, MouseExited,
    
    /** Drag events. */
    DragEnter, DragOver, DragExit, DragDrop,
    
    /** DragSource events. */
    DragGesture, DragSourceEnter, DragSourceOver, DragSourceExit, DragSourceEnd,
    
    /** Focus events. */
    FocusGained, FocusLost,
    
    /** Others. */
    Action, ValueChange, PropertyChange, Selection, Timer
}

}