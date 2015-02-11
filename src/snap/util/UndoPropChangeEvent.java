package snap.util;
import java.beans.IndexedPropertyChangeEvent;

/**
 * An (Indexed) PropertyChangeEvent subclass to describe shape property changes.
 * Index should be less than zero if not really an indexed property change.
 */
public class UndoPropChangeEvent extends IndexedPropertyChangeEvent {

/**
 * Creates a property change event.
 */
public UndoPropChangeEvent(Object aSource, String aProperty, Object oldValue, Object newValue, int anIndex)
{
    super(aSource, aProperty, oldValue, newValue, anIndex);
}

/**
 * Undoes this change.
 */
public void undoChange()  { doChange(getNewValue(), getOldValue()); }

/**
 * Redoes this change.
 */
public void redoChange()  { doChange(getOldValue(), getNewValue()); }

/**
 * Does this change with given new/old values.
 */
protected void doChange(Object oldValue, Object newValue)
{
    UndoPropChangeUtils.doChange(getSource(), getPropertyName(), oldValue, newValue, getIndex());
}

/**
 * Attempts to merge the given property change into this property change.
 */
public UndoPropChangeEvent merge(UndoPropChangeEvent anEvent)
{
    return getClass()==UndoPropChangeEvent.class? UndoPropChangeUtils.mergeImpl(this, anEvent) : null;
}

/**
 * Simple to string.
 */
public String toString()
{
    String cname = getSource().getClass().getSimpleName(), pname = getPropertyName();
    Object oldV = getOldValue(); String oldS = oldV!=null? oldV.toString().replace("\n", "\\n") : null;
    Object newV = getNewValue(); String newS = newV!=null? newV.toString().replace("\n", "\\n") : null;
    int index = getIndex(); String istring = index>0? (" at " + index) : "";
    return cname + " " + pname + " (set " + oldS + " to " + newS + ")" + istring;
}

}