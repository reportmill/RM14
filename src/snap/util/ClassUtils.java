package snap.util;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility methods for Class.
 */
public class ClassUtils {

/**
 * Returns the class for an object.
 */
public static Class getClass(Object anObj)
{
    return anObj instanceof Class? (Class)anObj : anObj!=null? anObj.getClass() : null;
}

/**
 * Returns the given object as instance of given class, if it is.
 */
public static <T> T getInstance(Object anObj, Class <T> aClass)  { return aClass.isInstance(anObj)? (T)anObj : null; }

/**
 * Returns simple class name of an object.
 */
public static String getClassSimpleName(Object anObj)  { return getClass(anObj).getSimpleName(); }

/**
 * Returns a standard class name of an object, minus any weird notation, like inner-class $, etc.
 */
public static String getClassStandardName(Object anObj, boolean doSimple)
{
    // Get class
    Class classe = getClass(anObj);
    
    // Handle Maps special
    if(anObj instanceof Map || Map.class.isAssignableFrom(classe))
        return "Map";
    
    // Get class name
    String className = classe.getName();

    // Change weird inner class $ syntax for plain dot syntax
    className = className.replace('$', '.');
        
    // Remove anything after a semi-colon (For class names like java.lang.Object; Serialize)
    if(className.indexOf(";")>0)
        className = className.substring(0, className.indexOf(";"));
        
    // Get simple class name
    if(doSimple) {
        int dot = className.lastIndexOf(".");
        className = className.substring(dot + 1);
    }
    
    // Return class name string
    return className;
}

/**
 * Returns a class for a given name.
 */
public static Class getClassForName(String aName)
{
    Class pcls = getPrimitiveClassForName(aName); if(pcls!=null) return pcls;
    try { return Class.forName(aName); }
    catch(ClassNotFoundException e) { return null; }
    catch(Throwable t) { return null; } // This can be thrown - I've seen it!
}

/**
 * Returns a class for a given name, using the class loader of the given class.
 */
public static Class getClassForName(String aName, Object anObj)
{
    Class pcls = getPrimitiveClassForName(aName); if(pcls!=null) return pcls;
    ClassLoader cl = anObj instanceof ClassLoader? (ClassLoader)anObj : getClass(anObj).getClassLoader();
    if(cl==null)
        return getClassForName(aName);
    try { return Class.forName(aName, false, cl); } //try { return cl.loadClass(aName); } 
    catch(ClassNotFoundException e) { return null; }
    catch(NoClassDefFoundError t) { return null; } // Can sometimes be thrown (I've seen it!)
}

/**
 * Returns whether name is a primitive class name.
 */
public static boolean isPrimitiveClassName(String aName)
{
    if(!Character.isLowerCase(aName.charAt(0)) || aName.indexOf('.')>0) return false; String tp = aName.intern();
    return tp=="boolean" || tp=="char" || tp=="void" ||  tp=="byte" || tp=="short" || tp=="int" ||
        tp=="long" || tp=="float" || tp=="double";
}

/**
 * Returns a primitive class for name.
 */
public static Class getPrimitiveClassForName(String aName)
{
    if(!Character.isLowerCase(aName.charAt(0)) || aName.indexOf('.')>0) return null; String tp = aName.intern();
    return tp=="boolean"? boolean.class : tp=="char"? char.class : tp=="void"? void.class :
        tp=="byte"? byte.class : tp=="short"? short.class : tp=="int"? int.class :
        tp=="long"? long.class : tp=="float"? float.class : tp=="double"? double.class : null;
}

/**
 * Returns a new instance of a given object.
 */
public static <T> T newInstance(T anObject)
{
    try { return (T)getClass(anObject).newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns a new instance of a given class.
 */
public static <T> T newInstance(Class <T> aClass)
{
    try { return aClass.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the common ancestor class for two objects.
 */
public static Class getCommonClass(Object anObj1, Object anObj2)
{
    // Bail if either object is null
    if(anObj1==null || anObj2==null) return null;
    
    // Get the classes for the objects
    Class c1 = getClass(anObj1);
    Class c2 = getClass(anObj2);
    
    // If either is assignable from the other, return that class
    if(c1.isAssignableFrom(c2)) return c1;
    if(c2.isAssignableFrom(c1)) return c2;
    
    // Recurse by swapping args and using superclass of second
    return getCommonClass(c2.getSuperclass(), c1);
}

/**
 * Returns the common ancestor class for a list of objects.
 */
public static Class getCommonClass(List aList)
{
    // Get class for first object
    Class commonClass = aList.size()>0? getClass(aList.get(0)) : null;
    
    // Iterate over remaining objects and get common class
    for(int i=1, iMax=aList.size(); i<iMax; i++)
        commonClass = getCommonClass(commonClass, aList.get(i));
    
    // Return common class
    return commonClass;
}

/**
 * Returns a type parameter class.
 */
public static Class getTypeParameterClass(Class aClass)
{
    Type type = aClass.getGenericSuperclass();
    if(type instanceof ParameterizedType) { ParameterizedType ptype = (ParameterizedType)type;
        Type type2 = ptype.getActualTypeArguments()[0];
        if(type2 instanceof Class)
            return (Class)type2;
        if(type2 instanceof ParameterizedType) { ParameterizedType ptype2 = (ParameterizedType)type2;
            if(ptype2.getRawType() instanceof Class)
                return (Class)ptype2.getRawType();
        }
    }

    // Complain and return null
    System.err.println("ClassUtils.getTypeParameterClass: Type Parameter Not Found for " + aClass.getName());
    return null;
}

}