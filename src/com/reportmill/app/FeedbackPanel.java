package com.reportmill.app;
import com.reportmill.base.*;
import java.util.*;
import snap.swing.*;

/**
 * This class provides a Swing UI panel to send feedback back to ReportMill.
 */
public class FeedbackPanel extends SwingOwner {

    // The cgimail template URL
    public static String _url = "http://www.reportmill.com/cgi-bin/cgiemail/email/rm-feedback.txt";

public static void runModal()  { new FeedbackPanel().runWindow(); }

public void runWindow()
{
    // Show panel (just return if cancelled)
    DialogBox dbox = new DialogBox("ReportMill Feedback");
    dbox.setContent(getUI()); dbox.setOptions("Submit", "Cancel");
    if(!dbox.showConfirmDialog(null)) return;
    
    // Update preferences and send feedback
    RMPrefsUtils.prefsPut("ExceptionUserName", getNodeStringValue("UserText"));
    RMPrefsUtils.prefsPut("ExceptionEmail", getNodeStringValue("EmailText"));
    sendFeedback();
}

/**
 * Initialize UI.
 */
public void initUI()
{
    setNodeValue("UserText", RMPrefsUtils.prefs().get("ExceptionUserName", ""));
    setNodeValue("EmailText", RMPrefsUtils.prefs().get("ExceptionEmail", ""));
}

/**
 * Send feedback via cgiemail at reportmill.com.
 */
public void sendFeedback()
{        
    // Configure environment string
    StringBuffer environment = new StringBuffer();
    String license = ReportMill.getLicense();
    if(license==null) license = "Unlicensed Copy";
    environment.append("License: " + license + "\n");
    environment.append("Build Date: " + RMUtils.getBuildInfo() + "\n");
    environment.append("Java VM: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")\n");
    environment.append("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
    
    // Configure keys
    final Map map = new HashMap();
    map.put("user-name", getNodeStringValue("UserText"));
    map.put("user-email", getNodeStringValue("EmailText"));
    map.put("environment", environment.toString());
    map.put("type", getNodeStringValue("TypeComboBox"));
    map.put("severity", getNodeStringValue("SeverityComboBox"));
    map.put("module", getNodeStringValue("ModuleComboBox"));
    map.put("title", getNodeStringValue("TitleText"));
    map.put("description", getNodeStringValue("DescriptionText"));
    
    // Send email in background thread
    new Thread() { public void run() {
        Exception e = RMURLUtils.sendCGIEmail(_url, map);
        if(e!=null) e.printStackTrace();
    }}.start();
}

}