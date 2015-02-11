package snap.util;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array; 

/**
 * A class that holds a list of instances for given class.
 */
public class ListenerList implements Cloneable {

    /* The list of ListenerType - Listener pairs */
    Object[]     _listeners = NULL_ARRAY;

    /* A null array to be shared by all empty listener lists*/
    private final static Object[] NULL_ARRAY = new Object[0];
    
/**
 * Returns the given listener at index.
 */
public <T> T get(Class<T> aClass, int anIndex)
{
    Object[] lList = _listeners; 
    for(int i=lList.length-2, j=0; i>=0; i-=2) {
        if(lList[i]==aClass && (anIndex==j++))
            return (T)lList[i+1]; }
    throw new IndexOutOfBoundsException("Listener index " + anIndex + " beyond " + getCount(aClass));
}

/**
 * Return an array of all the listeners of the given type. 
 */
public <T> T[] getAll(Class<T> aClass)
{
    Object[] lList = _listeners; 
    int count = getCount(lList, aClass); 
    T[] result = (T[])Array.newInstance(aClass, count); 
    for(int i=lList.length-2, j=0; i>=0; i-=2) {
        if(lList[i]==aClass)
            result[j++] = (T)lList[i+1];
    }
    return result;   
}

/**
 * Returns whether this obejct has listeners.
 */
public boolean isEmpty()  { return getCount()==0; }

/**
 * Returns the total number of listeners for this listener list.
 */
public int getCount()  { return _listeners.length/2; }

/**
 * Returns the total number of listeners of the supplied type for this listener list.
 */
public int getCount(Class<?> aClass)  { return getCount(_listeners, aClass); }

/**
 * Returns the total number of listeners of the supplied type for this listener list.
 */
private int getCount(Object[] theListeners, Class aClass)
{
    int count = 0; for(int i=0; i<theListeners.length; i+=2) if(aClass==theListeners[i]) count++;
    return count;
}

/**
 * Adds the listener as a listener of the specified type.
 */
public synchronized <T> void add(Class<T> aClass, T aListener)
{
    // Sanity check
    if(!aClass.isInstance(aListener))
        throw new IllegalArgumentException("Listener " + aListener + " is not of type " + aClass);

    // if this is the first listener added, initialize the lists
    if(_listeners==NULL_ARRAY) _listeners = new Object[] { aClass, aListener };
    
    // Otherwise copy the array and add the new listener
    else {
        int i = _listeners.length;
        Object[] tmp = new Object[i+2];
        System.arraycopy(_listeners, 0, tmp, 0, i);
        tmp[i] = aClass; tmp[i+1] = aListener;
        _listeners = tmp;
    }
}

/**
 * Removes the listener as a listener of the specified type.
 * @param t the type of the listener to be removed
 * @param l the listener to be removed
 */
public synchronized <T> void remove(Class<T> aClass, T aListener)
{
    // Get index of listener for class
    int index = -1;
    for(int i=_listeners.length-2; i>=0; i-=2)
        if(_listeners[i]==aClass && _listeners[i+1]==aListener) {
            index = i; break; }

    // If so,  remove it
    if(index>=0) {
        Object[] tmp = new Object[_listeners.length-2];
        System.arraycopy(_listeners, 0, tmp, 0, index);
        if(index<tmp.length) System.arraycopy(_listeners, index+2, tmp, index, tmp.length - index);
        _listeners = tmp.length==0? NULL_ARRAY : tmp;
    }
}

/**
 * Add PropertyChangeListener (convenience).
 */
public void addPropertyChangeListener(PropertyChangeListener aPCL)  { add(PropertyChangeListener.class,aPCL); }

/**
 * Remove PropertyChangeListener (convenience).
 */
public void removePropertyChangeListener(PropertyChangeListener aPCL)  { remove(PropertyChangeListener.class,aPCL); }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    ListenerList clone = null; try { clone = (ListenerList)super.clone(); }
    catch(CloneNotSupportedException e) { }
    clone._listeners = NULL_ARRAY;  // Clear listeners and return clone
    return clone;
}
  
/**
 * Standard toString implementation.
 */
public String toString()
{
    String s = "ListenerList: " + _listeners.length/2 + " listeners: ";
    for(int i=0; i<=_listeners.length-2; i+=2)
        s += " type " + ((Class)_listeners[i]).getName() + " listener " + _listeners[i+1];
    return s;
}

}