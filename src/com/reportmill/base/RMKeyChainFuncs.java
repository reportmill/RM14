package com.reportmill.base;
import com.reportmill.shape.RMImageShape;
import com.reportmill.text.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import snap.util.*;

/**
 * This class represents an RM function call (method plus args) and defines a bunch of built-in functions.
 */
public class RMKeyChainFuncs extends Object {

    // The function method
    Method _method;

    // The function args list
    Object _args[];
    
    // A list of classes to search for global functions
    static Class _funcClasses[] = { };

/**
 * Find the Method to invoke for evaluating the given key chain (assumed to be a function) on the given object.
 * fills args (if non-null) with the actual arguments to the function.
 */
public static RMKeyChainFuncs getFunctionCall(Object aRoot, Object anObj, RMKeyChain aKeyChain)
{
    // Get function name and arguments list
    String name = aKeyChain.getChildString(0);
    RMKeyChain argList = aKeyChain.getChildKeyChain(1);
    
    // Create list for args and evaluate
    Object args[] = new Object[argList.getChildCount()];
    for(int i=0, iMax=argList.getChildCount(); i<iMax; i++)
        args[i] = RMKeyChain.getValue(aRoot, argList.getChild(i)); 

    // Try to find method on anObj that takes a single keyChain 
    // (if found, set args to first argument for one arg funcs, or argument list for multiple arg funcs)
    Method method = SnapUtils.getMethod(anObj, name, RMKeyChain.class);
    if(method!=null)
        args = ArrayUtils.add(args, argList.getChildCount()==1? argList.getChild(0) : argList, 0);
    
    // Try to find method on anObj that takes args
    if(method==null) {
        
        // Create a Class array of the same size as the argument list loaded with Object.class
        Class argTypes[] = new Class[argList.getChildCount()]; Arrays.fill(argTypes, Object.class);
        
        // Look for method with given args
        method = SnapUtils.getMethod(anObj, name, argTypes);
        
        // If object doesn't implement the method, see if we have a Category implementation.
        // A category takes the target object as the first argument.
        if(method==null) {
            method = getMethod(name, ArrayUtils.add(argTypes, ClassUtils.getClass(anObj), 0));
            if(method!=null)
                args = ArrayUtils.add(args, anObj, 0);
        }
        
        // If object doesn't implement method, try to find method for registered functions that takes args
        if(method==null)
            method = getMethod(name, argTypes);
        
        // If method not found, try again with var-arg Object array
        if(method==null) {
            method = getMethod(name, Object[].class);
            if(method!=null)
                args = new Object[] { args };
        }
    }
    
    // Return method
    return method!=null? new RMKeyChainFuncs(method, args) : null;
}

/**
 * Creates a function call.
 */
public RMKeyChainFuncs(Method aMethod, Object theArgs[])  { _method = aMethod; _args = theArgs; }

/**
 * Invoke method.
 */
public Object invoke(Object anObj) throws InvocationTargetException, IllegalAccessException
{
    return _method.invoke(anObj, _args);
}

/**
 * Returns a method for a method name and the given argument classes.
 */
private static Method getMethod(String aName, Class ... argClasses)
{
    try { return RMKeyChainFuncs.class.getMethod(aName, argClasses); } catch(Exception e) { }
    for(Class cls : _funcClasses) try { return cls.getMethod(aName, argClasses); } catch(Exception e) { }
    return null;
}

/**
 * Adds a class to the list of classes that RM queries for functions.
 */
public static void addFunctionClass(Class aClass)  { _funcClasses = ArrayUtils.add(_funcClasses, aClass); }

/**
 * Returns whether given string is empty (or null).
 */
public boolean isEmpty(Object anObject)  { return anObject==null || anObject.toString().length()==0; }

/**
 * Returns the given value as a double rounded up to the nest largest integer.
 */
public static long ceil(Object val)  { return (long)Math.ceil(SnapUtils.doubleValue(val)); }

/**
 * Returns the given value as a double truncated down to the nest smallest integer.
 */
public static long floor(Object val)  { return (long)Math.floor(SnapUtils.doubleValue(val)); }

/**
 * Returns the given value as a double rounded to the nearest integer.
 */
public static long round(Object val)  { return Math.round(SnapUtils.doubleValue(val)); }

/**
 * Returns the absolute value of the given value.
 */
public static double abs(Object val)  { return Math.abs(SnapUtils.doubleValue(val)); }

/**
 * Returns the maximum value of the two given values.
 */
public static Object max(Object arg1, Object arg2)
{
    return RMSort.Compare(arg1, arg2)==RMSort.ORDER_ASCEND? arg2 : arg1;
}

/**
 * Returns the minimum value of the two given values.
 */
public static Object min(Object arg1, Object arg2)
{
    return RMSort.Compare(arg1, arg2)==RMSort.ORDER_DESCEND? arg2 : arg1;
}

/**
 * Returns the first value raised to the power of the second value.
 */
public static Double pow(Object arg1, Object arg2)
{
    double f1 = SnapUtils.doubleValue(arg1), f2 = SnapUtils.doubleValue(arg2);
    return Math.pow(f1, f2);
}

/**
 * Returns an xstring by interpreting html commands in the given string.
 */
public static Object html(Object aValue) { return RMHTML(aValue); }

/**
 * Returns an xstring by interpreting rtf commands in the given string.
 */
public static Object rtf(Object aValue) { return RMRTF(aValue); }

/**
 * Returns an xstring by interpreting html commands in the given string.
 */
public static Object RMHTML(Object aValue)
{
    // Get default font (or if val is xstring, get its first font)
    RMFont font = RMFont.getDefaultFont();
    if(aValue instanceof RMXString)
        font = ((RMXString)aValue).getFontAt(0);
    
    // Return result of parsing html from val string
    return RMHTMLParser.parse(aValue.toString(), font);
}

/**
 * Returns an xstring by interpreting rtf commands in the given string.
 */
public static Object RMRTF(Object aValue)
{
    // Get default font (or if val is xstring, get its first font)
    RMFont font = RMFont.getDefaultFont();
    if(aValue instanceof RMXString)
        font = ((RMXString)aValue).getFontAt(0);
    
    // Return result of parsing rtf from val string
    return RMRTFParser.parse(aValue.toString(), font);
}

/**
 * Returns an xstring with an embedded image shape for given image source.
 */
public static RMXString RMImage(Object aSource)
{
    // Get image shape
    RMImageShape imageShape = new RMImageShape(aSource);
    
    // If source couldn't resolve, return empty string
    if(imageShape.getWidth()==0)
        return null;
    
    // Create xstring
    RMXString xstring = new RMXString("a");
    xstring.setAttribute(RMTextTypes.TEXT_EMBEDDED_SHAPE, imageShape);
    return xstring;
}

/** Returns the trueVal if condition is true, otherwise null. */
public static Object RMConditional(Object v, Object t) { return SnapUtils.boolValue(v)? t : null; }
public static Object RMConditional(Object v, Object t, Object f) { return SnapUtils.boolValue(v)? t : f; }

/**
 * Returns true if given object string starts with given string.
 */
public static boolean startsWith(Object anObj, Object aString)
{
    return anObj instanceof String && aString instanceof String && anObj.toString().startsWith(aString.toString());
}

/**
 * Returns true if given object string ends with given string.
 */
public static boolean endsWith(Object anObj, Object aString)
{
    return anObj instanceof String && aString instanceof String && anObj.toString().endsWith(aString.toString());
}

/**
 * Returns the first index of given pattern in given string.
 */
public static int indexOf(String aStr, Object aPtrn)  { return indexOf(aStr, aPtrn, 0); }

/**
 * Returns the first index of given pattern in given string.
 */
public static int indexOf(String aStr, Object aPtrn, Object aStart)
{
    String ptrn = aPtrn instanceof String? (String)aPtrn : null; if(ptrn==null) return -1;
    int start = SnapUtils.intValue(aStart);
    return aStr.indexOf(ptrn, start);
}

/**
 * Returns the last index of given pattern in given string.
 */
public static int lastIndexOf(String aStr, Object aPtrn)
{
    return aPtrn instanceof String? aStr.lastIndexOf((String)aPtrn) : -1;
}

/**
 * Returns the substring of the given string from the given index onward.
 */
public static String substring(Object aString, Object start)
{
    return aString.toString().substring(SnapUtils.intValue(start));
}

/**
 * Returns the substring of the given string in the given start/end range.
 */
public static String substring(Object aString, Object start, Object end)
{
    String string = aString.toString();
    int s = SnapUtils.intValue(start);
    int e = Math.min(SnapUtils.intValue(end), string.length());
    return string.substring(s, e);
}

/**
 * Returns an array of strings by splitting given string with given regex separator.
 */
public static String[] split(Object aString, Object aRegex)
{
    String string = aString.toString();
    return string!=null? string.split(aRegex.toString()) : null;
}

/**
 * Returns the result of joining results of evaluating keychain on given list objects, separated by delimiter.
 */
public static String join(Object aList, Object aKeyChain, Object aDelimiter)
{
    // If object is list, do join
    if(aList instanceof List) {
        List list = (List)aList, joinParts = new ArrayList(list.size());
        for(int i=0, iMax=list.size(); i<iMax; i++)
            joinParts.add(RMKeyChain.getValue(list.get(i), aKeyChain));
        return ListUtils.joinStrings(joinParts, aDelimiter.toString());
    }
    
    // Return null if object isn't list
    return null;
}

/**
 * Returns the unicode character string for the given unicode value.
 */
public static Object RMUnicode(Object num) { char c[] = { (char)SnapUtils.intValue(num) }; return new String(c); }

/**
 * Returns the unicode string for the given range of unicode values.
 */
public static Object RMUnicodeRange(Object c1, Object c2)
{
    int i1 = SnapUtils.intValue(c1), i2 = SnapUtils.intValue(c2), charCount = Math.max(i2 - i1 + 1, 0);
    char chars[] = new char[charCount];
    for(int i=0; i<charCount; i++) chars[i] = (char)(i1+i);
    return new String(chars);
}

/**
 * Returns all of the printable characters for the given font name.
 */
public static Object RMAllFontGlyphs(Object fontName)
{
    String name = fontName.toString();
    RMFont font = RMFont.getFont(name, 12); if(font==null) return "Font not found";
    StringBuffer sb = new StringBuffer();
    
    for(char c=1; c<0xffff; c++) {
        if(font.canDisplay(c)) {
            if(c==1 || c%256==0)
                sb.append("\n0x").append(Integer.toHexString(c)).append(":\n");
            sb.append(c);
        }
    }
    
    return sb.toString();
}

/**
 * Returns a string with all fonts names rendered as the fonts themselves (at the given size).
 */
public static Object RMAllFonts(Object aSize)
{
    int size = SnapMath.clamp(SnapUtils.intValue(aSize), 8, 80);
    RMXString string = new RMXString();
    for(String fontName : RMFontUtils.getFontNames()) { RMFont font = RMFont.getFont(fontName, size);
        string.addChars(fontName + "\n", font); }
    return string;
}

/**
 * Returns string format of given date, using given date format string (category method).
 */
public static String format(Date aDate, Object stringObj)
{
    String formatString = stringObj.toString();
    RMDateFormat fmt = new RMDateFormat(formatString);
    return (String)fmt.formatRM(aDate);
}

/**
 * Returns string format of given number using given decimal format string (category method).
 */
public static String format(Number aNumber, Object aFormatString)
{
    String formatString = aFormatString.toString();
    RMNumberFormat fmt = new RMNumberFormat(formatString);
    return (String)fmt.formatRM(aNumber);
}

/**
 * Returns string format of given Double using given decimal format string (category method).
 */
public static String format(Double aDouble, Object aFormatString)  { return format((Number)aDouble, aFormatString); }

/**
 * Returns string format of given Float using given decimal format string (category method).
 */
public static String format(Float aFloat, Object aFormatString)  { return format((Number)aFloat, aFormatString); }

/**
 * Returns string format of given Integer using given decimal format string (category method).
 */
public static String format(Integer anInt, Object aFormatString)  { return format((Number)anInt, aFormatString); }

/**
 * Returns string format of given BigDecimal using given decimal format string (category method).
 */
public static String format(BigDecimal aDecimal, Object aFormatString)
{
    return format((Number)aDecimal, aFormatString);
}

/** 
 * Returns substring of given string from given start index (category method).
 */
public static String substring(String aString, Object start)  { return substring((Object)aString, start); }

/**
 * Returns substring of given string from given start index to given end index (category method).
 */
public static String substring(String aString, Object start, Object end)
{
    return substring((Object)aString, start, end);
}

/**
 * Returns whether given string starts with other given string (category method).
 */
public static boolean startsWith(String aString, Object pre)  { return startsWith((Object)aString, pre); }

/**
 * Returns whether given string ends with other given string (category method).
 */
public static boolean endsWith(String aString, Object post)  { return endsWith((Object)aString, post); }

/**
 * Returns a number for a given string.
 */
public static Number number(String aString)  { return SnapUtils.numberValue(aString); }

/**
 * Returns the given string padded by the given string to be the given length (category method).
 */
public static String pad(String aString, Object aLength)  { return pad(aString, " ", aLength); }

/**
 * Returns the given string padded by the given string to be the given length (category method).
 */
public static String pad(String aString, Object aPad, Object aLength)
{
    // Get length as int (just return if string already given length)
    int len = SnapUtils.intValue(aLength);
    if(aString.length()>=len)
        return aString;
    
    // Create string buffer, add pad and return
    StringBuffer sb = new StringBuffer(aString);
    while(sb.length()<len) sb.append(aPad);
    return sb.toString();
}

/**
 * Returns the given string padded by the given string to be the given length (category method).
 */
public static String padLeft(String aString, Object aPad, Object aLength)
{
    // Get length as int (if string is already given length or greater, just return it)
    int length = SnapUtils.intValue(aLength);
    if(aString.length()>=length)
        return aString;
    
    // Create string buffer, add pad, return string
    StringBuffer sb = new StringBuffer(aString);
    while(sb.length()<length) sb.insert(0, aPad);
    return sb.toString();
}

/**
 * Fixes the given string to the given length, padding by space. 
 */
public static String fix(String aString, Object aLength)  { return fix(aString, aLength, " "); }

/**
 * Fixes the given string to the given length with the given pad string.
 */
public static String fix(String aString, Object aLength, Object aPad)
{
    return pad(substring(aString, 0, aLength), aPad, aLength);
}

/**
 * Wraps the given string to a max of given length by adding newlines.
 */
public static String wrap(String aString, Object aLength)
{
    return StringUtils.wrap(aString, SnapUtils.intValue(aLength));
}

/**
 * Returns a list of the given args.
 */
public static List list(Object ... theObjects)
{
    // If a single object passed in and it is a List or Array, return it as a List
    if(theObjects!=null && theObjects.length==1) {
        if(theObjects[0] instanceof List)
            return (List)theObjects[0];
        if(theObjects[0] instanceof Object[])
            return Arrays.asList((Object[])theObjects[0]);
    }
    
    // Return 
    return Arrays.asList(theObjects);
}

/**
 * Formats the given object in roman numerals. If anObj is a date object, the year is formatted.
 */
public static String roman(Object anObj)
{
    if(anObj instanceof Date) {
        GregorianCalendar c = new GregorianCalendar(); c.setTime((Date)anObj); anObj = c.get(Calendar.YEAR); }
    return anObj instanceof Number? romanNumeralFormat(((Number)anObj).intValue()) : null;
}

/**
 * Formats the given object in roman numerals. If anObj is a date object, the year is formatted.
 */
public static String roman(Number aNumber)  { return romanNumeralFormat(aNumber.intValue()); }

/**
 * Return a roman numeral representation of the number as a string.
 * Returned characters are all uppercase. Valid range is 1-4999
 */
private static String romanNumeralFormat(int aValue)
{
    // If not in valid range, return
    if(aValue>4999 || aValue<=0) return "" + aValue;
 
    // Declare parts and string buffer
    char dparts[][] = { {'X','I','V'}, {'C','X','L'}, {'M','C','D'} };
    StringBuffer digs[] = new StringBuffer[4];
    
    // Iterate over something
    for(int i=0; i<3;i++) {
        int d = aValue%10;
        if(d>0) {
            digs[i] = new StringBuffer();
            if(d==9) {
                fill(digs[i],1,dparts[i][1]);
                fill(digs[i],1,dparts[i][0]);
            }
            else if(d>=4) {
                if(d==4) fill(digs[i],1,dparts[i][1]);
                fill(digs[i],1,dparts[i][2]);
                if(d>5) fill(digs[i], d-5, dparts[i][1]);
            }
            else fill(digs[i], d, dparts[i][1]);
        }
        aValue/=10;
    }
    
    // Do something else
    if(aValue>=0) {
        digs[3]=new StringBuffer();
        fill(digs[3], aValue, 'M');
    }
    
    // Get something
    StringBuffer rstring = null;
    for(int i=3; i>=0; --i) {
        if(rstring==null) rstring = digs[i];
        else if(digs[i]!=null) rstring.append(digs[i]);
    }
    
    // Return string
    return rstring!=null? rstring.toString() : "Error";
}

/** Utility method to append n occurrences of a character to a string buffer */
private static void fill(StringBuffer s, int n, char c)  { while(n-->0) s.append(c); }

}