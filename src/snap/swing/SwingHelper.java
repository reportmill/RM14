package snap.swing;
import java.util.*;
import javax.swing.JComponent;
import snap.util.*;

/**
 * This class provides Ribs functionality for abstract widgets (display objects).
 */
public abstract class SwingHelper <T> extends UIHelper <T> {

    // The map of SwingHelper for Swing classes
    static Map            _helpers = new Hashtable();

/**
 * Returns the SwingHelper object for a given object.
 */
public SwingHelper getHelper(Object anObj)  { return getSwingHelper(anObj); } 

/**
 * Returns the SwingHelper object for a given object.
 */
public static SwingHelper getSwingHelper(Object anObj)
{
    // Add bogus mapping for null to Object
    if(anObj==null)
        anObj = Object.class;
    
    // Get the class
    Class objClass = anObj instanceof Class? (Class)anObj : anObj.getClass();;
    
    // Get helper from helpers map and return if present
    SwingHelper helper = (SwingHelper)_helpers.get(objClass);
    if(helper!=null)
        return helper;
    
    // Otherwise do real getHelper and add helper to helpers map
    helper = getSwingHelperImpl(objClass);
    if(helper!=null)
        _helpers.put(objClass, helper);
    
    // Return helper
    return helper;
}
    
/**
 * Returns the SwingHelper object for a given object.
 */
public static SwingHelpers.JComponentHpr getSwingHelper(JComponent aComponent)
{
    return (SwingHelpers.JComponentHpr)getSwingHelper(aComponent!=null? aComponent : JComponent.class);
}

/**
 * Returns the Ribs Helper object for a given object.
 */
private static SwingHelper getSwingHelperImpl(Class aClass)
{
    // Get simple name of object class
    String simpleName = aClass.getSimpleName();
        
    // Get helper class from helpers package
    Class helperClass = null;
    
    // If not found, look in swing helpers package
    if(helperClass==null)
        helperClass = ClassUtils.getClassForName("snap.swing.SwingHelpers$" + simpleName + "Hpr");
    
    // If not found, look for helper in same package
    if(helperClass==null)
        helperClass = ClassUtils.getClassForName(aClass.getName() + "Hpr", aClass);
        
    // If not found, try inner class
    if(helperClass==null)
        helperClass = ClassUtils.getClassForName(aClass.getName() + "$" + "Helper", aClass);
        
    // If helper class found, instantiate helper class
    SwingHelper helper = null;
    if(helperClass!=null)
        try { helper = (SwingHelper)helperClass.newInstance(); }
        catch(Exception ie) { ie.printStackTrace(); }
            
    // Otherwise, get helper for superclass
    else helper = getSwingHelper(aClass.getSuperclass());
        
    // Return helper
    return helper;
}

}