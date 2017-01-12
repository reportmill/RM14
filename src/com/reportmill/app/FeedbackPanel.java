package com.reportmill.app;
import com.reportmill.base.*;
import snap.swing.*;
import snap.util.URLUtils;

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
 * Send feedback via SendMail.py at reportmill.com.
 */
public void sendFeedback()
{        
    // Configure environment string
    StringBuffer env = new StringBuffer();
    String lic = ReportMill.getLicense(); if(lic==null) lic = "Unlicensed Copy";
    env.append("License: " + lic + "\n");
    env.append("Build Date: " + RMUtils.getBuildInfo() + "\n");
    env.append("Java VM: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")\n");
    env.append("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
    
    // Get to address
    String toAddr = "support@reportmill.com";
    
    // Get from address
    String name = getNodeStringValue("UserText"); int nlen = name!=null? name.length() : 0;
    String email = getNodeStringValue("EmailText"); int elen = email!=null? email.length() : 0;
    if(nlen>0 && elen>0) email = name + " <" + email + '>';
    else if(nlen>0) email = name; else if(elen==0) email = "Anonymous";
    String fromAddr = email;
    
    // Get subject
    String subject = "ReportMill Feedback";
    
    // Get body
    StringBuffer sb = new StringBuffer();
    sb.append(subject).append('\n').append('\n');
    sb.append("From: ").append(fromAddr).append('\n');
    sb.append("Type: ").append(getNodeStringValue("TypeComboBox")).append('\n');
    sb.append("Severity: ").append(getNodeStringValue("SeverityComboBox")).append('\n');
    sb.append("Module: ").append(getNodeStringValue("ModuleComboBox")).append('\n').append('\n');
    sb.append("Title: ").append(getNodeStringValue("TitleText")).append('\n').append('\n');
    sb.append(getNodeStringValue("DescriptionText")).append('\n').append('\n').append(env);
    String body = sb.toString();
    
    // Get URL
    String url = "http://reportmill.com/cgi-bin/SendMail.py";

    // Send email in background thread
    new Thread() { public void run() {
        String str = sendMail(toAddr, fromAddr, subject, body, url);
        if(str!=null) System.out.println("FeedbackPanel Response: " + str);
    }}.start();
}

/**
 * Sends an email with given from, to, subject, body and SendMail url.
 */
public static String sendMail(String toAddr, String fromAddr, String aSubj, String aBody, String aURL)
{
    String text = String.format("To=%s\nFrom=%s\nSubject=%s\n%s", toAddr, fromAddr, aSubj, aBody);
    Exception e = URLUtils.postText(aURL, text);
    return e!=null? e.getMessage() : null;
}

}