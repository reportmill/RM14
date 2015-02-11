package snap.util;
import java.beans.*;

/**
 * A listener to get property changes and nested property changes.
 */
public interface DeepChangeListener extends java.util.EventListener {

    /**
     * Deep property changes (as well as normal property changes).
     */
    void deepChange(PropertyChangeListener aSource, PropertyChangeEvent anEvent);
}
