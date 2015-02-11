package com.reportmill.base;
import snap.util.XMLArchiver.Archivable;

/**
 * An interface for RM format classes (they all should get/set format strings, format objects and archive XML).
 */
public interface RMFormat extends Archivable {

    /** Returns a format string. */
    public String getFormatString();
    
    /** Sets a format string. */
    public void setFormatString(String format);

    /** Returns a string or xstring. */
    public Object formatRM(Object anObj);
}