package snap.util;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import snap.web.WebFile;

/**
 * Utility methods for Class.
 */
public class ClassUtils {

    // An array of primitive type classes and sister array of it's non-primitive matches
    static Class _primitives[] = { boolean.class, char.class, byte.class, short.class, int.class, long.class,
        float.class, double.class, void.class };
    static Class _primMappings[] = { Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, Void.class };

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
 * Returns a field for a parent class and a name.
 */
public static Field getField(Class aClass, String aName)
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Field field = getDeclaredField(cls, aName);
    if(field==null && (cls=cls.getSuperclass())!=null)
        return getField(cls, aName);
    return field;
}

/**
 * Returns a field for a parent class and a name.
 */
public static Field getDeclaredField(Class aClass, String aName)
{
    Field fields[] = aClass.getDeclaredFields();
    for(Field field : fields)
        if(field.getName().equals(aName))
            return field;
    return null;
}

/**
 * Returns all methods for given class and subclasses that start with given prefix.
 */
public static Method[] getMethods(Class aClass, String aPrefix)
{
    List meths = new ArrayList();
    getMethods(aClass, aPrefix, meths, true);
    return (Method[])meths.toArray(new Method[0]);
}

/**
 * Returns the method for given class, name and parameter types.
 */
private static void getMethods(Class aClass, String aPrefix, List theMethods, boolean doPrivate)
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Method methods[] = cls.getDeclaredMethods();
    for(Method m : methods)
        if(m.getName().startsWith(aPrefix) && (doPrivate || !Modifier.isPrivate(m.getModifiers())))
            theMethods.add(m);
    
    // Recurse for interfaces and super classes
    if(cls.isInterface()) {
        for(Class cl : cls.getInterfaces())
            getMethods(cl, aPrefix, theMethods, false); }
    else if((cls=cls.getSuperclass())!=null)
        getMethods(cls, aPrefix, theMethods, false);
}

/**
 * Returns the method for given class, name and parameter types.
 */
public static Method getMethod(Class aClass, String aName, Class theClasses[])
{
    // Get methods that match name/args (just return if null, no args or singleton)
    Method methods[] = getMethods(aClass, aName, theClasses, null);
    if(methods==null) return null;
    if(theClasses.length==0 || methods.length==1) return methods[0];
    
    Method method = null; int rating = 0;
    for(Method meth : methods) {
        int rtg = getRating(meth, theClasses);
        if(rtg>rating) { method = meth; rating = rtg; }
    }
    return method;
}

/**
 * Returns the method for given class, name and parameter types.
 */
public static Method[] getMethods(Class aClass, String aName, Class theClasses[], Method theResult[])
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Method methods[] = getDeclaredMethods(cls, aName, theClasses, theResult);
    if(cls.isInterface()) {
        for(Class cl : cls.getInterfaces())
            methods = getMethods(cl, aName, theClasses, methods); }
    else if((cls=cls.getSuperclass())!=null)
        methods = getMethods(cls, aName, theClasses, methods);
    return methods;
}

/**
 * Returns the declared method for a given class, name and parameter types array.
 */
private static Method[] getDeclaredMethods(Class aClass, String aName, Class theClasses[], Method theResult[])
{
    // Get class methods and intern name
    Method methods[] = aClass.getDeclaredMethods();
    String name = aName.intern();
    
    // Iterate over methods and if any have same name and given classes are assignable to method types, return method
    for(Method method : methods)
        if(method.getName()==name && isCompatible(method, theClasses)) {
            theResult = theResult!=null? Arrays.copyOf(theResult, theResult.length+1) : new Method[1];
            theResult[theResult.length-1] = method;
        }
    return theResult;
}

/**
 * Returns whether arg classes are compatible.
 */
public static boolean isCompatible(Method aMethod, Class theClasses[])
{
    // Get method parameter classes
    Class params[] = aMethod.getParameterTypes();
    
    // Handle Var args
    if(aMethod.isVarArgs()) {
        
        // If standard args don't match return false
        if(theClasses.length<params.length-1 || !isAssignable(params, theClasses, params.length-1))
            return false;
        
        // Get VarArgClass
        Class varArgArrayClass = params[params.length-1];
        Class varArgClass = varArgArrayClass.getComponentType();
        
        // If only one arg and it is of array class, return true
        Class argClass = theClasses.length==params.length? theClasses[params.length-1] : null;
        if(argClass!=null && argClass.isArray() && varArgArrayClass.isAssignableFrom(argClass))
            return true;

        // If any var args don't match, return false
        for(int i=params.length-1; i<theClasses.length; i++)
            if(theClasses[i]!=null && !isAssignable(varArgClass, theClasses[i]))
                return false;
        return true;
    }
    
    // Handle normal method
    return params.length==theClasses.length &&
        isAssignable(params, theClasses, params.length);
}

/**
 * Returns whether second batch of classes is assignable to first batch of classes (accounting for auto-boxing).
 */
public static boolean isAssignable(Class theClasses1[], Class theClasses2[], int aCount)
{
    if(theClasses1==null) return theClasses2==null || theClasses2.length==0;
    if(theClasses2==null) return theClasses1.length==0;
    for(int i=0; i<aCount; i++)
        if(theClasses2[i]!=null && !isAssignable(theClasses1[i], theClasses2[i]))
            return false;
    return true;
}

/**
 * Returns whether a given class could be assigned a value from the second given class (accounting for auto-boxing).
 */
public static boolean isAssignable(Class aClass1, Class aClass2)
{
    if(aClass2==null) return !aClass1.isPrimitive();
    if(aClass1.isPrimitive() || aClass2.isPrimitive())
        return isAssignablePrimitive(aClass1, aClass2);
    return aClass1.isAssignableFrom(aClass2);
}

/**
 * Returns whether a given primitive class could be assigned a value from the second given class.
 */
public static boolean isAssignablePrimitive(Class aClass1, Class aClass2)
{
    Class c1 = toPrimitive(aClass1), c2 = toPrimitive(aClass2);
    if(c1==Object.class) return true;
    if(c1==float.class || c1==double.class || c1==Number.class)
        return c2==c1||c2==byte.class||c2==char.class||c2==short.class||c2==int.class||c2==long.class||c2==float.class;
    if(c1==byte.class || c1==char.class || c1==short.class || c1==int.class || c1==long.class)
        return c2==c1 || c2==byte.class || c2==char.class || c2==short.class || c2==int.class;
    return c1.isAssignableFrom(c2);
}

/**
 * Returns non primitive type for primitive.
 */
public static Class toPrimitive(Class aClass)
{
    for(int i=0; i<_primitives.length; i++) if(aClass==_primMappings[i]) return _primitives[i];
    return aClass;
}

/**
 * Returns primitive type for non-primitive.
 */
public static Class fromPrimitive(Class aClass)
{
    for(int i=0; i<_primitives.length; i++) if(aClass==_primitives[i]) return _primMappings[i];
    return aClass;
}

/**
 * Returns a rating of a method for given possible arg classes.
 */
private static int getRating(Method aMethod, Class theClasses[])
{
    // If VarArgs and theClasses are VarArgs, return 1000 - this is a bit bogus
    if(aMethod.isVarArgs() && theClasses.length!=aMethod.getParameterTypes().length) return 1000;
    
    // Iterate over classes and add score based on matching classes
    // This is a punt - need to groc the docs on this: https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html
    Class classes[] = aMethod.getParameterTypes(); int rating = 0;
    for(int i=0, iMax=classes.length; i<iMax; i++) {
        Class cls1 = classes[i], cls2 = theClasses[i];
        if(cls1==cls2) rating += 1000;
        else if(cls2!=null && cls1.isAssignableFrom(cls2)) rating += 100;
        else if(isAssignable(cls1, cls2)) rating += 10;
        else if(cls2==null);
        else System.err.println("ClassUtils: Method doesn't match??? " + aMethod);
    }
    return rating;
}

/**
 * Returns a declared inner class for a given class and a name, checking super classes as well.
 */
public static Class getClass(Class aClass, String aName)
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Class cls2 = getDeclaredClass(cls, aName);
    if(cls2==null && (cls=cls.getSuperclass())!=null)
        return getClass(cls, aName);
    return cls2;
}

/**
 * Returns a class for a parent class and a name.
 */
public static Class getDeclaredClass(Class aClass, String aName)
{
    for(Class cls : aClass.getDeclaredClasses())
        if(cls.getSimpleName().equals(aName))
            return cls;
    return null;
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
public static Class getCommonClass(Object ... theObjects)
{
    // Get class for first object
    Class commonClass = theObjects.length>0? getClass(theObjects[0]) : null;
    
    // Iterate over remaining objects and get common class
    for(int i=1, iMax=theObjects.length; i<iMax; i++)
        commonClass = getCommonClass(commonClass, theObjects[i]);
    
    // Return common class
    return commonClass;
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
    System.err.println("RMClassUtils.getTypeParameterClass: Type Parameter Not Found for " + aClass.getName());
    return null;
}

/**
 * Returns the classes in a package name.
 * Busted for JNLP Web Start.
 */
public static Class[] getClasses(String aPackageName)
{
    // Create new list
    List <Class> classes = new ArrayList();
    
    // Translate the package name into an absolute path
    String name = new String(aPackageName);
    if(!name.startsWith("/"))
        name = "/" + name;
    name = name.replace('.', '/');
    
    // Get a File object for the package
    URL url = ClassUtils.class.getResource(name); //sun.misc.Launcher.class.getResource(name);
    File directory = new File(url.getFile());
    
    // New code
    if(directory.exists()) {
        
        // Get the list of the files contained in the package
        String files[] = directory.list();
        
        // Iterate over files
        for (int i=0; i<files.length; i++) {
             
            // we are only interested in .class files
            if(files[i].endsWith(".class")) {
                
                // Get class name minus '.class' extension
                String className = aPackageName + "." + files[i].substring(0, files[i].length()-6);
                
                // Try to create an instance of the object                
                try {
                    Class clazz = Class.forName(className);
                    if(Modifier.isPublic(clazz.getModifiers()))
                        classes.add(clazz);
                }
                
                // Exceptions
                catch(Exception e) { System.out.println(e.getMessage()); }
            }
        }
    }
    
    // Return classes
    return classes.toArray(new Class[classes.size()]);
}

/**
 * Returns the plain URL (http: or file:) to the jar for given class.
 */
public static URL getRootURL(Class aClass)
{
    String urls = getJarURLS(aClass); if(urls.startsWith("jar:")) urls = urls.substring(4, urls.length()-2);
    try { return new URL(urls); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the Jar URL for given class.
 */
public static URL getJarURL(Class aClass)
{
    String urls = getJarURLS(aClass);
    try { return new URL(urls); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the root URL for given class (could be a "file:" (directory) or "jar:").
 */
public static String getJarURLS(Class aClass)
{
    // Get ClassLaoder, ClassPath and URL to ClassPath
    ClassLoader cldr = aClass.getClassLoader(); if(cldr==null) cldr = Thread.currentThread().getContextClassLoader();
    String classPath = aClass.getName().replace(".", "/") + ".class";
    URL url = cldr.getResource(classPath); if(url==null) return null;

    // Get URL string minus ClassPath (if jar URL also strip '!' separator)
    String urls = url.toExternalForm();
    int end = urls.length() - classPath.length() - 1; if(url.getProtocol().equals("jar")) end++;
    urls = urls.substring(0, end); 
    return urls;
}

/**
 * Returns the class name.
 */
public static String getClassName(WebFile aFile)
{
    String fpath = aFile.getPath();
    return fpath.substring(1, fpath.length() - ".class".length()).replace('/', '.');
}

/**
 * Returns the class compiled from this java file.
 */
public static Class getClass(WebFile aFile)
{
    String cname = getClassName(aFile);
    try { return aFile.getSite().getClassLoader().loadClass(cname); }
    catch(Throwable e) { System.err.println("ClassUtils.getClass(" + aFile.getName() + "): " + e); return null; }
}

/**
 * Returns whether class file has main method.
 */
public static boolean getHasMain(Class aClass)
{
    try { return aClass.getMethod("main", String[].class)!=null; }
    catch(Exception e) { return false; }
}

/**
 * Runs the main method of compiled class.
 */
public static void runMain(Class aClass)
{
    try { aClass.getMethod("main", String[].class).invoke(null, (Object)new String[0]); }
    catch(Exception e) { throw new RuntimeException(e); }
}

}