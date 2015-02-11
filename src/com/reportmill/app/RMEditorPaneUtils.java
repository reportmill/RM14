package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import snap.swing.*;
import snap.util.XMLElement;
import snap.web.WebURL;

/**
 * Some utility methods for RMEditorPane.
 */
public class RMEditorPaneUtils {

    // The most recently activated editor pane for this app
    static RMEditorPane         _mainEditorPane;
    
    // The list of all open editors
    static List <RMEditorPane>  _allEditorPanes = new Vector(1);
    
    // Whether some editor is in the process of becoming main editor
    static boolean              _mainEditorResetting = false;

/**
 * Returns the editor pane that is the most recently activated.
 */
public static RMEditorPane getMainEditorPane()  { return _mainEditorPane; }

/**
 * Sets the editor that is most recently used.
 */
public static void setMainEditorPane(RMEditorPane anEditorPane)
{
    // If given editor is already main editor or if main editor is being reset, just return
    if(anEditorPane==_mainEditorPane || _mainEditorResetting) return;
    
    // Set mainEditorResetting variable so we know to ignore redundant calls generated inside this method
    _mainEditorResetting = true;
    
    // Set given editor as new main editor
    _mainEditorPane = anEditorPane;
    
    // Move editor to front of all editors list
    RMListUtils.moveToFront(_allEditorPanes, anEditorPane);
    
    // Reset current tool and editor pane
    anEditorPane.getEditor().resetCurrentTool();
    anEditorPane.resetLater();
    
    // Give focus to editor
    anEditorPane.getEditor().requestFocus();
    
    // Reset "resetting" flag after delay
    SwingUtilities.invokeLater(new Runnable() { public void run() { _mainEditorResetting = false; }});
}

/**
 * Returns the number of active/visible editor panes.
 */
public static int getEditorPaneCount()  { return _allEditorPanes.size(); }

/**
 * Returns the specific editor pane at the given index from the list of all editors currently in the application.
 */
public static RMEditorPane getEditorPane(int anIndex)  { return _allEditorPanes.get(anIndex); }

/**
 * Remove given editor from editors list.
 */
protected static void removeEditorPane(RMEditorPane anEditorPane)
{
    // Remove editor from editor's list - if MainEditorPane, clear MainEditorPane
    _allEditorPanes.remove(anEditorPane);
    if(anEditorPane==_mainEditorPane) _mainEditorPane = null;
}

/**
 * Returns the editor that was most recently activated.
 */
public static RMEditor getMainEditor()
{
    RMEditorPane ep = RMEditorPaneUtils.getMainEditorPane(); return ep!=null? ep.getEditor() : null;
}

/**
 * Installs a sample data source.
 */
public static void connectToDataSource(RMEditorPane anEditorPane)
{
    RMDataSource ds = new RMDataSource(WebURL.getURL("Jar:/com/reportmill/examples/HollywoodDB.xml"));
    if(ds!=null) anEditorPane.setDataSource(ds);
}

/**
 * Opens the named sample file from the examples package.
 */
public static RMEditorPane openSample(String aTitle)
{
    // If file is xml resource, get temp file, get XML bytes, write to file, open file and return null
    if(aTitle.endsWith(".xml")) {
        File file = RMFileUtils.getTempFile(RMStringUtils.getPathFileName(aTitle));
        byte bytes[] = RMUtils.getBytes(aTitle);
        RMUtils.writeBytes(bytes, file);
        RMFileUtils.openFile(file);
        return null;
    }
    
    // If not url, append Jar:/com/reportmill prefix
    if(!aTitle.startsWith("http:")) aTitle = "Jar:/com/reportmill/examples/" + aTitle + ".rpt";
        
    // Create new editor pane, open document and window, and return editor pane
    RMEditorPane editorPane = new RMEditorPane();
    editorPane.open(aTitle);
    editorPane.setWindowVisible(true);
    return editorPane;
}

/**
 * Preview PDF.
 */
public static void previewPDF(RMEditorPane anEditorPane)
{
    // Get filename (if alt key is pressed, change to current doc plus .pdf)
    String filename = RMUtils.getTempDir() + "RMPDFFile.pdf";
    if(Swing.isAltDown() && anEditorPane.getDocument().getFilename()!=null)
        filename = RMStringUtils.getPathSimple(anEditorPane.getDocument().getFilename()) + ".pdf";
    
    // Get report, write report and open file
    RMDocument report = generateReport(anEditorPane, true);
    report.writePDF(filename);
    RMFileUtils.openFile(filename);
}

/**
 * Generates report from editor.
 */
public static RMDocument generateReport(RMEditorPane anEditorPane, boolean doPaginate)
{
    // Get editor - if editing, flush changes, otherwise, set Editing
    RMEditor editor = anEditorPane.getEditor();
    if(anEditorPane.isEditing())
        editor.flushEditingChanges();
    else anEditorPane.setEditing(true);
    
    // Get document and return report
    RMDocument document = anEditorPane.getDocument();
    return document.generateReport(editor.getDataSourceDataset(), doPaginate);
}

/**
 * Generate report, save as HTML in temp file and open.
 */
public static void previewHTML(RMEditorPane anEditorPane)
{
    RMDocument report = generateReport(anEditorPane, !Swing.isAltDown());
    report.write(RMUtils.getTempDir() + "RMHTMLFile.html");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMHTMLFile.html");
}

/**
 * Generate report, save as CSV in temp file and open.
 */
public static void previewCSV(RMEditorPane anEditorPane)
{
    RMDocument report = generateReport(anEditorPane, false);
    report.write(RMUtils.getTempDir() + "RMCSVFile.csv");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMCSVFile.csv");
}

/**
 * Generate report, save as JPG in temp file and open.
 */
public static void previewJPG(RMEditorPane anEditorPane)
{
    RMDocument report = generateReport(anEditorPane, false);
    report.write(RMUtils.getTempDir() + "RMJPGFile.jpg");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMJPGFile.jpg");
}

/**
 * Generate report, save as PNG in temp file and open.
 */
public static void previewPNG(RMEditorPane anEditorPane)
{
    RMDocument report = generateReport(anEditorPane, false);
    report.write(RMUtils.getTempDir() + "RMPNGFile.png");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMPNGFile.png");
}

/**
 * Preview XLS.
 */
public static void previewXLS(RMEditorPane anEditorPane)
{
    // Get report, write report and open file (in handler, in case POI jar is missing)
    try {
        RMDocument report = generateReport(anEditorPane, false);
        report.write(RMUtils.getTempDir() + "RMXLSFile.xls");
        RMFileUtils.openFile(RMUtils.getTempDir() + "RMXLSFile.xls");
    }
    
    // Catch exception - handle case where poi jar is missing    
    catch(Throwable t) {
        
        // print it out (in case it's something other than a missing jar)
        t.printStackTrace();
        
        // Run option dialog to ask user if they want to see Excel doc
        String msg = "ReportMill needs the OpenSource POI jar in order to generate Excel. Click Open to see " +
            "the support document on the subject.";
        DialogBox dbox = new DialogBox("Excel POI Jar Missing");
        dbox.setWarningMessage(RMStringUtils.wrap(msg, 50)); dbox.setOptions("Open", "Cancel");
        int answer = dbox.showOptionDialog(anEditorPane.getUI(), "Open");
        
        // If user answered "open", open poi doc url
        if(answer==0)
            RMURLUtils.openURL("http://reportmill.com/support/Excel.html");
    }
}

/**
 * Preview RTF.
 */
public static void previewRTF(RMEditorPane anEditorPane)
{
    // Get report, write report and open file
    RMDocument report = generateReport(anEditorPane, true);
    report.write(RMUtils.getTempDir() + "RMRTFFile.rtf");
    RMFileUtils.openFile(RMUtils.getTempDir() + "RMRTFFile.rtf");
}

/**
 * Preview XML.
 */
public static void previewXML(RMEditorPane anEditorPane)
{
    RMEditor editor = anEditorPane.getEditor();
    XMLElement xml = new RMArchiver().writeObject(editor.getContent());
    File file = RMFileUtils.getTempFile("RMXMLFile.xml");
    try { RMFileUtils.writeBytes(file, xml.getBytes()); }
    catch(Exception e) { throw new RuntimeException(e); }
    RMFileUtils.openFile(file);
}

/**
 * Save document as PDF to given path.
 */
public static void saveAsPDF(RMEditorPane anEditorPane)
{
    RMEditor editor = anEditorPane.getEditor();
    String path = FileChooserUtils.showChooser(true, editor, "PDF file (.pdf)", ".pdf"); if(path==null) return;
    editor.flushEditingChanges();
    editor.getDocument().writePDF(path);
}

}