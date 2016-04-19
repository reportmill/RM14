package com.reportmill.app;
import com.reportmill.base.*;
import javax.swing.*;
import javax.swing.table.*;
import snap.swing.*;
import java.util.*;

/**
 * This class provides UI for showing the formatter from the currently selected shape and editing or changing it.
 */
public class FormatPanel extends SwingOwner {
    
    // A list of standard number formats
    static List <RMNumberFormat>  _numberFormats = new Vector();
    
    // A list of standard date formats
    static List <RMDateFormat>    _dateFormats = new Vector();

    // Sample date object to be used to display date formats
    Date                          _sampleDate = new Date();
    
    // Sample positive number to be used to display number formats
    Float                         _sampleNumberPos = 1234.567f;
    
    // Sample negative number to be used to display number formats
    Float                         _sampleNumberNeg = 1234.567f;

/** Initializes _numberFormats and _dateFormats lists. */
static
{
    // Load standard number formats from preferences
    String nums = RMPrefsUtils.prefs().get("NumberFormats", getDefaultNumberFormatsString());
    setNumberFormatsString(nums);
    
    // Load standard date formats from preferences
    String dates = RMPrefsUtils.prefs().get("DateFormats2", getDefaultDateFormatsString());
    setDateFormatsString(dates);
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set table models
    getNode("NumberFormatTable", JTable.class).setModel(new NumberFormatTableModel());
    getNode("DateFormatTable", JTable.class).setModel(new DateFormatTableModel());
}

/**
 * Reset UI panel.
 */
public void resetUI()
{
    // Get main editor and currently selected format (just return if null)
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    RMFormat format = RMEditorShapes.getFormat(editor);
            
    // Update NumberPanel
    if(format instanceof RMNumberFormat) {
        
        // Get currently selected format as number format
        RMNumberFormat nformat = (RMNumberFormat)format;
        
        // Install number panel if absent
        setNodeSelectedIndex("PickerPanel", 1);
        setNodeValue("NumberFormatButton", true);
        
        // Have the table select current format if present
        setNodeSelectedIndex("NumberFormatTable", _numberFormats.indexOf(format));
        
        // Update NumberFormatText
        setNodeValue("NumberFormatText", nformat.getFormatString());
        
        // Update NegativeInRedCheckBox
        setNodeValue("NegativeInRedCheckBox", nformat.isNegativeInRed());
        
        // Update NumberNullStringText
        setNodeValue("NumberNullStringText", nformat.getNullString());
    }
    
    // Update _datePanel text fields
    else if(format instanceof RMDateFormat) {
        
        // Get currently selected format as date format
        RMDateFormat dformat = (RMDateFormat)format;
        
        // Install date panel if absent
        setNodeSelectedIndex("PickerPanel", 2);
        setNodeValue("DateFormatButton", true);

        // Have the table select current format if present
        setNodeSelectedIndex("DateFormatTable", getDateFormatIndex(dformat.toPattern()));
        
        // Update DateFormatText
        setNodeValue("DateFormatText", dformat.toPattern());
        
        // Update DateNullStringText
        setNodeValue("DateNullStringText", dformat.getNullString());
    }
    
    // Handle NoFormatPanel
    else {
        setNodeSelectedIndex("PickerPanel", 0);
        setNodeValue("NoFormatButton", true);
    }
}

/** Responds to changes from format panel UI controls. */
public void respondUI(SwingEvent anEvent)
{
    // Get main editor (just return if null)
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    
    // Get currently selected format
    RMFormat format = RMEditorShapes.getFormat(editor);
    
    // Get currently selected number format (if appropriate)
    RMNumberFormat numFormat = format instanceof RMNumberFormat? (RMNumberFormat)format : null;
    
    // Get currently selected date format (if appropriate)
    RMDateFormat dateFormat = format instanceof RMDateFormat? (RMDateFormat)format : null;
    
    // Handle NoFormatButton
    if(anEvent.equals("NoFormatButton"))
        RMEditorShapes.setFormat(editor, null);
    
    // Handle NumberFormatButton
    if(anEvent.equals("NumberFormatButton") && !(format instanceof RMNumberFormat))
        RMEditorShapes.setFormat(editor, new RMNumberFormat(getNumberFormat(0)));
    
    // Handle DateFormatButton
    if(anEvent.equals("DateFormatButton") && !(format instanceof RMDateFormat))
        RMEditorShapes.setFormat(editor, new RMDateFormat(getDateFormat(0).toPattern()));
        
    // Handle NumberFormatTable
    if(anEvent.equals("NumberFormatTable")) {
        int row = anEvent.getSelectedIndex();
        if(row > -1) 
            RMEditorShapes.setFormat(editor, new RMNumberFormat(getNumberFormat(row)));
    }
    
    // Handle NumberFormatText
    if(anEvent.equals("NumberFormatText") && anEvent.getStringValue().length()>0) {
        try { format.setFormatString(anEvent.getStringValue()); }
        catch(Exception e) {
            String msg = "Invalid number format (see DecimalFormat javadoc for info).";
            DialogBox dbox = new DialogBox("Invalid Number Format String"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
        }
    }

    // Handle NegativeInRedCheckBox
    if(anEvent.equals("NegativeInRedCheckBox"))
        numFormat.setNegativeInRed(anEvent.getBoolValue());
    
    // Handle NumberNullStringText
    if(anEvent.equals("NumberNullStringText"))
        numFormat.setNullString(anEvent.getStringValue());

    // Handle DateFormatTable
    if(anEvent.equals("DateFormatTable") && anEvent.getSelectedIndex()>=0) {
        int row = anEvent.getSelectedIndex();
        RMDateFormat df = (RMDateFormat)getDateFormat(row).clone();
        RMEditorShapes.setFormat(editor, df);
    }
    
    // Handle DateFormatText
    if(anEvent.equals("DateFormatText") && anEvent.getStringValue().length()>0) {
        try { format.setFormatString(anEvent.getStringValue()); }
        catch(Exception e) {
            String msg = "Invalid date format (see SimpleDateFormat javadoc for info).";
            DialogBox dbox = new DialogBox("Invalid Date Format String"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
        }
    }
    
    // Handle DateNullStringText
    if(anEvent.equals("DateNullStringText"))
        dateFormat.setNullString(anEvent.getStringValue());
}

// An inner-class to fill NumberFormatTable
private class NumberFormatTableModel extends AbstractTableModel {
    Class columnClasses[] = { String.class, String.class, String.class };
    String headers[] = { "Positive", "Negative", "Format" };
    public int getRowCount()  { return getNumberFormatCount(); }
    public int getColumnCount()  { return 3; }
    public Class getColumnClass(int c)  { return columnClasses[c]; }
    public String getColumnName(int c)  { return headers[c]; }
    public boolean isCellEditable(int r, int c)  { return false; }
    public Object getValueAt(int r, int c) { 
        RMNumberFormat f = getNumberFormat(r);
        if(c==0)
            return f.formatRM(_sampleNumberPos).toString();
        if(c==1)
            return f.formatRM(_sampleNumberNeg).toString();
        return f.getFormatString();
    }
}

// An inner-class to fill DateFormatTable
private class DateFormatTableModel extends AbstractTableModel {
    Class columnClasses[] = { String.class, String.class };
    String headers[] = { "Date", "Format" };
    public int getRowCount()  { return getDateFormatCount(); }
    public int getColumnCount()  { return 2; }
    public Class getColumnClass(int c)  { return columnClasses[c]; }
    public String getColumnName(int c)  { return headers[c]; }
    public boolean isCellEditable(int r, int c)  { return false; }
    public Object getValueAt(int r, int c) { 
        RMDateFormat f = getDateFormat(r);
        return c==0? f.formatRM(_sampleDate) : f.toPattern();
    }
}

/** Returns the number of preset number formats available to the format panel. */
public static int getNumberFormatCount()  { return _numberFormats.size(); }

/** Returns the preset number format at the given index. */
public static RMNumberFormat getNumberFormat(int anIndex)  { return _numberFormats.get(anIndex); }

/** Returns the Format panel's current number format strings as a single newline separated string. */
public static String getNumberFormatsString()
{
    StringBuffer sb = new StringBuffer();
    for(int i=0, iMax=getNumberFormatCount(); i<iMax; i++)
        sb.append(getNumberFormat(i).getFormatString()).append('\n');
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
}

/** Sets the FormatPanel's current number formats from a single newline separated string. */
public static void setNumberFormatsString(String aString)
{
    _numberFormats.clear();
    List fstrings = RMStringUtils.separate(aString, "\n");
    for(int i=0, iMax=fstrings.size(); i<iMax; i++)
        _numberFormats.add(new RMNumberFormat(fstrings.get(i).toString()));
}

/** Returns the number of preset date formats available to the format panel. */
public static int getDateFormatCount()  { return _dateFormats.size(); }

/** Returns the preset date format at the given index. */
public static RMDateFormat getDateFormat(int anIndex)  { return _dateFormats.get(anIndex); }

/** Returns the index of the preset date format for a given date format pattern. */
public static int getDateFormatIndex(String aPattern)
{
    // Iterate over preset date formats to see if any matches the given pattern
    for(int i=0, iMax=getDateFormatCount(); i<iMax; i++)
        if(getDateFormat(i).toPattern().equals(aPattern))
            return i;
    
    // If pattern not found, return -1
    return -1;
}

/** Returns the Format panel's current date format strings as a single newline separated string. */
public static String getDateFormatsString()
{
    // Create new string buffer for date formats string
    StringBuffer sb = new StringBuffer();
    
    // Iterate over formats and add format pattern + newline
    for(int i=0, iMax=getDateFormatCount(); i<iMax; i++)
        sb.append(getDateFormat(i).toPattern()).append('\n');
    
    // Delete last newline
    sb.deleteCharAt(sb.length() - 1);
    
    // Return string
    return sb.toString();
}

/** Sets the FormatPanel's current date formats from a single newline separated string. */
public static void setDateFormatsString(String aString)
{
    _dateFormats.clear();
    List fstrings = RMStringUtils.separate(aString, "\n");
    for(int i=0, iMax=fstrings.size(); i<iMax; i++)
        _dateFormats.add(new RMDateFormat(fstrings.get(i).toString()));
}

/** Returns the name for the attributes panel window. */
public String getWindowTitle()  { return "Format Panel"; }

/** Returns ReportMill's default number format strings as a single newline delimited string. */
public static String getDefaultNumberFormatsString()
{
    return
        "$ #,##0.00\n" +
        "$ #,##0\n" +
        "0.00\n" +
        "0\n" +
        "#,##0\n" +
        "000000\n" +
        "0%\n" +
        "0.00%";
}

/** Returns ReportMill's default date format strings as a single newline delimited String. */
public static String getDefaultDateFormatsString()
{
    return
        "EEEE, MMMM d, yyyy\n" +
        "MMMM d, yyyy\n" +
        "d MMMM yyyy\n" +
        "MM/dd/yy\n" +
        "MM/dd/yyyy\n" +
        "MMM dd, yyyy\n" +
        "dd MMM yyyy\n" +
        "dd-MMM-yyyy\n" +
        "HH:mm:ss a zzzz\n" +
        "hh:mm a";
}

}