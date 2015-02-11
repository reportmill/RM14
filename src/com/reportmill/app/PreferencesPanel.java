package com.reportmill.app;
import com.reportmill.base.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides a Preferences panel UI window.
 */
public class PreferencesPanel extends SwingOwner {

/**
 * Runs the panel.
 */
public void showPanel(JComponent aComponent)
{
    DialogBox dbox = new DialogBox("Preferences Panel"); dbox.setContent(getUI());
    while(dbox.showConfirmDialog(aComponent) && !apply());
}

/**
 * Initialize UI panel.
 */
public void initUI()
{
    // Set LicenseText & EnableExceptionsCheckBox
    setNodeValue("LicenseText", RMPrefsUtils.prefs().get("HostProperties1", null));
    setNodeValue("EnableExceptionsCheckBox", RMPrefsUtils.prefs().getBoolean("ExceptionReportingEnabled", true));
    
    // Set NumberFormatsText & DateFormatsText
    setNodeValue("NumberFormatsText", FormatPanel.getNumberFormatsString());
    setNodeValue("DateFormatsText", FormatPanel.getDateFormatsString());
}

/**
 * Updates user preferences settings from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle ResetNumbersButton
    if(anEvent.equals("ResetNumbersButton"))
        setNodeValue("NumberFormatsText", FormatPanel.getDefaultNumberFormatsString());
    
    // Handle ResetDatesButton
    if(anEvent.equals("ResetDatesButton"))
        setNodeValue("DateFormatsText", FormatPanel.getDefaultDateFormatsString());
}

/**
 * Handles the preferences panel apply button.
 */
public boolean apply()
{
    // Get License key
    String licenseKey = RMStringUtils.min(getNodeStringValue("LicenseText"));
    
    // If license is provided but invalid, complain and return
    if(licenseKey!=null && !RMUtils.checkString(licenseKey, true)) {
        String msg = "The license key entered is invalid - please recheck and try again.";
        DialogBox dbox = new DialogBox("Invalid License"); dbox.setErrorMessage(msg);
        dbox.showMessageDialog(getUI());
        return false;
    }
    
    // Set license
    ReportMill.setLicense(licenseKey, true, true);

    // Save the exception reporting pref
    RMPrefsUtils.prefsPut("ExceptionReportingEnabled", getNodeBoolValue("EnableExceptionsCheckBox"));
    
    // Get pref panel number formats and the original number formats
    String nums = getNodeStringValue("NumberFormatsText");
    String oldNums = FormatPanel.getNumberFormatsString();
    
    // If number formats have changed, try to commit them
    if(!nums.equals(oldNums)) {
    
        // If setting the format throws exception, reset old format string, show error dialog and return false
        try { FormatPanel.setNumberFormatsString(nums); }
        catch(Exception e) {
            FormatPanel.setNumberFormatsString(oldNums);
            String msg = "Invalid number format (see format panel for examples).";
            DialogBox dbox = new DialogBox("Invalid Number Format String"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
            return false;
        }
        
        // Add new format string to default (clear it if it's the default)
        if(nums.equals(FormatPanel.getDefaultNumberFormatsString()))
            nums = null;
        RMPrefsUtils.prefsPut("NumberFormats", nums);
    }
    
    // Get pref panel date formats and original date formats
    String dates = getNodeStringValue("DateFormatsText");
    String oldDates = FormatPanel.getDateFormatsString();
    
    // If date formats have changed, commit them
    if(!dates.equals(oldDates)) {

        // If setting the format throws exception, reset old format string, show error dialog and return false
        try { FormatPanel.setDateFormatsString(dates); }
        catch(Exception e) {
            FormatPanel.setDateFormatsString(oldDates);
            String msg = "Invalid date format (see format panel for examples).";
            DialogBox dbox = new DialogBox("Invalid Date Format String"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
            return false;
        }

        // Add new format string to default (clear it if it's the default)
        if(dates.equals(FormatPanel.getDefaultDateFormatsString()))
            dates = null;
        RMPrefsUtils.prefsPut("DateFormats2", dates);
    }
    
    // Flush properties to registry
    try { RMPrefsUtils.prefs().flush(); }
    catch(Exception e) { e.printStackTrace(); }
    
    // Return true if everything went as planned
    return true;
}

}