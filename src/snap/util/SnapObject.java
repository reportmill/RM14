package snap.util;
import java.beans.*;

/**
 * This class forms the basis of many objects to add global functionality, like archiving.
 */
public class SnapObject extends ListenerBase {

/**
 * Add listener.
 */
public void addPropertyChangeListener(PropertyChangeListener aListener)
{
    addListener(PropertyChangeListener.class, aListener);
}

/**
 * Remove listener.
 */
public void removePropertyChangeListener(PropertyChangeListener aListener)
{
    removeListener(PropertyChangeListener.class, aListener);
}

/**
 * Fires a given property change.
 */
protected void firePropertyChange(PropertyChangeEvent anEvent)  { sendPropertyChange(anEvent); }

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropertyChange(String aProperty, Object oldValue, Object newValue, int anIndex)
{
    if(!hasListeners()) return;
    sendPropertyChange(new IndexedPropertyChangeEvent(this, aProperty, oldValue, newValue, anIndex));
}

/**
 * Sends the property change.
 */
protected void sendPropertyChange(PropertyChangeEvent anEvent)
{
    for(PropertyChangeListener l : getListeners(PropertyChangeListener.class))
        l.propertyChange(anEvent);
}

/**
 * Called to update shape anim.
 */
public void animUpdate(PropertyChangeEvent anEvent)  { }

/**
 * Returns a string representation.
 */
public String toString()  { return getClass().getSimpleName()+"@"+Integer.toHexString(System.identityHashCode(this)); }

}