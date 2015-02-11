package snap.swing;
import java.awt.Dimension;
import java.util.*;
import javax.swing.JTextArea;
import snap.util.*;

/**
 * This class provides a Swing UI panel to inform users that an exception was hit and send info back to ReportMill.
 */
public class ExceptionReporter extends SwingOwner implements Thread.UncaughtExceptionHandler {
    
    // The cgimail template URL
    String         _url = "http://www.reportmill.com/cgi-bin/cgiemail/email/rm-exception.txt";
    
    // Tells whether this exception reporter has been run before
    boolean        _done = false;
    
/**
 * Returns the URL.
 */
public String getURL()  { return _url; }

/**
 * Sets the URL.
 */
public void setURL(String aURL)  { _url = aURL; }

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
 * Send exception via cgiemail at reportmill.com.
 */
public void sendException()
{        
    // Set keys user-email, user-name, user-comment, and exception represent (they are used in cgiemail template)
    final Map map = new HashMap();
    map.put("user-name", getNodeStringValue("UserText"));
    map.put("user-email", getNodeStringValue("EmailText"));
    map.put("user-comment", getNodeStringValue("ScenarioText"));
    map.put("exception", getNodeStringValue("BacktraceText"));
        
    // Send email in background thread
    new Thread() { public void run() {
        Exception e = URLUtils.sendCGIEmail(_url, map);
        if(e!=null) e.printStackTrace();
    }}.start();
}

}