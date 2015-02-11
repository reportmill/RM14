package snap.util;
import com.reportmill.base.RMKeyChain;
import java.beans.*;
import java.util.*;

/**
 * Utilities for property changes.
 */
public class UndoPropChangeUtils {

/**
 * Returns the index for a property change event (assuming it's indexed).
 */
public static int getIndex(PropertyChangeEvent anEvent)
{
    return anEvent instanceof IndexedPropertyChangeEvent? ((IndexedPropertyChangeEvent)anEvent).getIndex() : -1;
}

/**
 * Reverses the given change.
 */
public static void undoChange(PropertyChangeEvent anEvent)
{
    if(anEvent instanceof UndoPropChangeEvent)
        ((UndoPropChangeEvent)anEvent).undoChange();
    else undoChange(anEvent.getSource(), anEvent);
}

/**
 * Reverses the given change.
 */
public static void undoChange(Object aSource, PropertyChangeEvent anEvent)
{
    doChange(aSource, anEvent.getPropertyName(), anEvent.getNewValue(), anEvent.getOldValue(), getIndex(anEvent));
}

/**
 * Re-performs the given change.
 */
public static void redoChange(PropertyChangeEvent anEvent)
{
    if(anEvent instanceof UndoPropChangeEvent)
        ((UndoPropChangeEvent)anEvent).redoChange();
    else redoChange(anEvent.getSource(), anEvent);
}

/**
 * Re-performs the given change on the given object.
 */
public static void redoChange(Object aSource, PropertyChangeEvent anEvent)
{
    doChange(aSource, anEvent.getPropertyName(), anEvent.getOldValue(), anEvent.getNewValue(), getIndex(anEvent));
}

/**
 * Performs a list of changes.
 */
public static void doChanges(List <PropertyChangeEvent> theChanges)
{
    for(PropertyChangeEvent event : theChanges)
        redoChange(event);
}

/**
 * Performs a list of changes.
 */
public static void doChanges(Object aSource, List <PropertyChangeEvent> theChanges)
{
    for(PropertyChangeEvent event : theChanges)
        redoChange(aSource, event);
}

/**
 * Reverts a list of changes.
 */
public static void undoChanges(List <PropertyChangeEvent> theChanges)
{
    // Copy and reverse list
    List <PropertyChangeEvent> changes = new ArrayList(theChanges); Collections.reverse(changes);
    
    // Iterate over changes and apply the inverse
    for(PropertyChangeEvent event : changes)
        undoChange(event);
}

/**
 * Reverts a list of changes.
 */
public static void undoChanges(Object aSource, List <PropertyChangeEvent> theChanges)
{
    // Copy and reverse list
    List <PropertyChangeEvent> changes = new ArrayList(theChanges); Collections.reverse(changes);
    
    // Iterate over changes and apply the inverse
    for(PropertyChangeEvent event : changes)
        undoChange(aSource, event);
}

/**
 * Performs the given change by using RMKey.setValue or RMKeyList add/remove.
 */
public static void doChange(Object aSource, String aProperty, Object oldValue, Object newValue, int anIndex)
{
    // If indexed change, create RMKeyList and add/remove
    if(anIndex>=0) {
        
        // Create list
        KeyList list = new KeyList(aSource, aProperty);
        
        // Other value
        Object otherValue = list.size()>anIndex? list.get(anIndex) : null;
        
        // If new value was provided (added), add new object
        if(newValue!=null && newValue!=otherValue)
            list.add(anIndex, newValue);
        
        // Otherwise, remove object at index
        else if(oldValue!=null && oldValue==otherValue)
            list.remove(anIndex);
    }
    
    // If plain change, do RMKey.setValue on new value
    else RMKeyChain.setValue(aSource, aProperty, newValue);
}

/**
 * Attempts to merge the second property change into the first property change.
 */
public static PropertyChangeEvent merge(PropertyChangeEvent anEvent1, PropertyChangeEvent anEvent2)
{
    if(anEvent1 instanceof UndoPropChangeEvent && anEvent2 instanceof UndoPropChangeEvent)
        return ((UndoPropChangeEvent)anEvent1).merge((UndoPropChangeEvent)anEvent2);
    return mergeImpl(anEvent1, anEvent2);
}

/**
 * Attempts to merge the second property change into the first property change.
 */
public static UndoPropChangeEvent mergeImpl(PropertyChangeEvent anEvent1, PropertyChangeEvent anEvent2)
{
    // If index, return false
    if(getIndex(anEvent1)>=0 || getIndex(anEvent2)>=0)
        return null;
    
    // Create new merged event and return it 
    UndoPropChangeEvent event = new UndoPropChangeEvent(anEvent1.getSource(), anEvent1.getPropertyName(),
        anEvent1.getOldValue(), anEvent2.getNewValue(), -1);
    return event;
}

}