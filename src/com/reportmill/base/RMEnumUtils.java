package com.reportmill.base;

/**
 * Utilities for enums.
 */
public class RMEnumUtils {

/**
 * Returns an enum.
 */
public static <T extends Enum<T>> T valueOfIC(Class<T> enumType, String aName)
{
    for(T value : enumType.getEnumConstants())
        if(value.toString().equalsIgnoreCase(aName))
            return value;
    if(aName==null)
        throw new NullPointerException("Name is null");
    throw new IllegalArgumentException("No enum const " + enumType +"." + aName);
}

}