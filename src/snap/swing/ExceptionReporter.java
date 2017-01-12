package snap.swing;
import java.awt.Dimension;
import javax.swing.JTextArea;
import snap.util.*;

/**
 * This class provides a Swing UI panel to inform users that an exception was hit and send info back to ReportMill.
 */
public class ExceptionReporter extends SwingOwner implements Thread.UncaughtExceptionHandler {
    
    // Tells whether this exception reporter has been run before
    boolean        _done = false;
    
/**
 * Creates a new exception reporter for given throwable.
 */
public void uncaughtException(Thread t, Throwable aThrowable)
{
    // Get root exception
    while(aThrowable.getCause()!=null)
        aThrowable = aThrowable.getCause();
    
    // Go ahead and print stack trace
    aThrowable.printStackTrace();
    
    // If exception reporting not enabled, just return (otherwise mark done, because we only offer this once)
    if(_done || !PrefsUtils.prefs().getBoolean("ExceptionReportingEnabled", true))
        return;
    else _done = true;

    // Set preferred size
    getUI().setPreferredSize(new Dimension(585, 560));
    
    // Default user/email values in UI
    setNodeValue("UserText", PrefsUtils.prefs().get("ExceptionUserName", ""));
    setNodeValue("EmailText", PrefsUtils.prefs().get("ExceptionEmail", ""));
    
    // Start the exception text with environment info
    StringBuffer eBuffer = new StringBuffer();
    eBuffer.append("ReportMill Version " + SnapUtils.getVersion() + ", Build Date: " + SnapUtils.getBuildInfo() + "\n");
    eBuffer.append("Java VM: " + System.getProperty("java.version"));
    eBuffer.append(" (" + System.getProperty("java.vendor") + ")\n");
    eBuffer.append("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")\n\n");
    
    // Get the backtrace of the throwable into a string and append it to our exception text
    eBuffer.append("Backtrace:\n");
    eBuffer.append(StringUtils.getStackTraceString(aThrowable));
    
    // Finally, set the exception text in the UI
    setNodeValue("BacktraceText", eBuffer.toString());
    getNode("BacktraceText", JTextArea.class).setSelectionStart(0);
    getNode("BacktraceText", JTextArea.class).setSelectionEnd(0);
    
    // Run panel (just return if cancelled)
    DialogBox dbox = new DialogBox("ReportMill Exception Reporter");
    dbox.setContent(getUI()); dbox.setOptions("Submit", "Cancel");
    if(!dbox.showConfirmDialog(null)) return;

    // Update preferences and send exception
    PrefsUtils.prefsPut("ExceptionUserName", getNodeStringValue("UserText"));
    PrefsUtils.prefsPut("ExceptionEmail", getNodeStringValue("EmailText"));
    sendException();
}

/**
 * Send exception via SendMail.py at reportmill.com.
 */
public void sendException()
{        
    // Get to address
    String toAddr = "support@reportmill.com";
    
    // Get from address
    String name = getNodeStringValue("UserText"); int nlen = name!=null? name.length() : 0;
    String email = getNodeStringValue("EmailText"); int elen = email!=null? email.length() : 0;
    if(nlen>0 && elen>0) email = name + " <" + email + '>';
    else if(nlen>0) email = name; else if(elen==0) email = "Anonymous";
    String fromAddr = email;
    
    // Get subject
    String subject = "ReportMill Exception Report";
    
    // Get body
    String scenario = getNodeStringValue("ScenarioText");
    if(scenario==null || scenario.length()==0) scenario = "<Not provided>";
    String btrace = getNodeStringValue("BacktraceText");
    String body = String.format("%s\n\nFrom:\n%s\n\nUser Scenario:\n%s\n\n%s", subject, fromAddr, scenario, btrace);
    
    // Get url
    String url = "http://reportmill.com/cgi-bin/SendMail.py";
        
    // Send email in background thread
    new Thread() { public void run() {
        String str = sendMail(toAddr, fromAddr, subject, body, url);
        if(str!=null) System.out.println("ExceptionReporter Response: " + str);
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