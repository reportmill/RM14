package com.reportmill.apptools;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import com.reportmill.viewer.*;
import java.util.*;
import javax.swing.*;
import snap.swing.*;
import snap.util.Binding;
import snap.web.*;

/**
 * A tool to set formatting options on a simple table from a list of templates.
 */
public class RMCrossTabAutoFormatPanel extends SwingOwner {

    // The sample report viewer
    RMViewer            _viewer;
    
    // The dataset
    List                _dataset;
    
    // The available templates
    List <WebFile>     _templateFiles = new ArrayList();
    
    // The cached RMDocuments
    RMDocument          _autoDocs[];
    
/**
 * Run the panel for the given CrossTab.
 */
public void showPanel(JComponent aComp, RMCrossTab aTable)
{
    DialogBox dbox = new DialogBox("Table Autoformat"); dbox.setContent(getUI());
    if(dbox.showConfirmDialog(aComp))
        applyFormatting(aTable);
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Load viewer
    _viewer = getNode("AutoTableViewer", RMViewer.class);
    
    // Get all the auto-format templates
    WebURL turl = WebURL.getURL(getClass(), getClass().getSimpleName().replace('.', '/') + ".ribs");
    WebFile tdir = turl.getFile();
    for(WebFile tfile : tdir.getFiles())
        if(tfile.getPath().endsWith(".rpt"))
            _templateFiles.add(tfile);
    
    // If none.rpt is available, move it to the front
    WebFile none = tdir.getFile("none.rpt");
    if(none!=null) {
        _templateFiles.remove(none); _templateFiles.add(0, none); }
    
    // If Default.rpt exists, make it the selected index
    WebFile defaultTemplate = tdir.getFile("Default.rpt");
    int defaultSelection = defaultTemplate!=null? _templateFiles.indexOf(defaultTemplate) : 0;
    
    // Get template names array
    String tnames[] = new String[_templateFiles.size()];
    for(int i=0, iMax=_templateFiles.size(); i<iMax; i++) { WebFile tfile = _templateFiles.get(i);
        tnames[i] = tfile.getSimpleName().replace('_', ' '); }
    
    // Allocate the array of RMDocuments
    _autoDocs = new RMDocument[_templateFiles.size()];
    
    // Get list and set data, Set the default selection.  This will also fire an action and reset the viewer
    JList list = getNode("AutoTableList", JList.class);
    list.setListData(tnames);
    list.setSelectedIndex(defaultSelection);
    showSelectedTemplate();
}

/**
 * Respond to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle AutoTableList
    if(anEvent.equals("AutoTableList"))
        showSelectedTemplate();
}

/**
 * Returns the selected template resource.
 */
public RMDocument getSelectedTemplate()
{
    JList list = getNode("AutoTableList", JList.class);
    int index = list.getSelectedIndex();
    
    // load the template
    if(_autoDocs[index]==null)
        _autoDocs[index] = new RMDocument(_templateFiles.get(index));
    return _autoDocs[index];
}

/** 
 * Resets the RMViewer 
 */
public void showSelectedTemplate()
{
    RMDocument doc = getSelectedTemplate();
    RMDocument filled = doc.generateReport(getDataset());
    _viewer.setContent(filled);
}

/** 
 * Fills the destination table with all relevant formatting options from the selected table.
 */
public void applyFormatting(RMCrossTab aTable)
{
    // Get template
    RMDocument template = getSelectedTemplate();
    
    // Get the table inside the template
    RMCrossTab source = template.getPage(0).getChildrenWithClass(RMCrossTab.class).get(0);
    int nheaderCols = aTable.getHeaderColCount();
    int nheaderRows = aTable.getHeaderRowCount();
    int nRows = aTable.getRowCount();
    int nCols = aTable.getColCount();
    
    // Set undo title
    aTable.undoerSetUndoTitle("Table Autoformatting");

    // all templates consist of a 2x2 table: 1 header row, 1 header column, and a data cell
    for(int r=0; r<nRows; ++r) {
        for(int c=0; c<nCols; ++c) {
            RMCrossTabCell destCell = aTable.getCell(r, c);
            RMCrossTabCell sourceCell = source.getCell(r<nheaderRows ? 0 : 1, c<nheaderCols ? 0 : 1);
            applyCellFormatting(sourceCell, destCell);
        }
    }
    
    // Copy over table formatting
    aTable.setFill(source.getFill());
    aTable.setStroke(source.getStroke());
    
    // Register for table layout
    aTable.relayout();
}

/**
 * Copy all relevant cell formatting options from source to destination.
 */
public void applyCellFormatting(RMCrossTabCell aSourceCell, RMCrossTabCell aDestCell) 
{
    // Probably could bail here if the cell isn't visible, but might as well do the rest anyway
    aDestCell.setVisible(aSourceCell.isVisible());
    
    // Set borders
    aDestCell.setShowLeftBorder(aSourceCell.getShowLeftBorder());
    aDestCell.setShowRightBorder(aSourceCell.getShowRightBorder());
    aDestCell.setShowTopBorder(aSourceCell.getShowTopBorder());
    aDestCell.setShowBottomBorder(aSourceCell.getShowBottomBorder());
    
    // Set fill
    aDestCell.setFill(aSourceCell.getFill());
    
    // Set text attributes
    //Map attrs = aSourceCell.getXString().getAttributes(0);
    RMTextStyle sourceStyle = aSourceCell.getXString().getRun().getStyle();
    RMXString destString = aDestCell.getXString();
    destString.setStyle(sourceStyle, 0, destString.length());
    
    // check for cases of removal of attributes (to set a default)
    //if(attrs.get(RMTextTypes.TEXT_COLOR)==null) destString.setAttribute(RMTextTypes.TEXT_COLOR, null);

    // Copy FillColor binding
    Binding binding = aSourceCell.getBinding("FillColor");
    if(binding!=null)
        aDestCell.addBinding(new Binding("FillColor", binding.getKey()));
    else aDestCell.removeBinding("FillColor");
}

/**
 * Returns the sample dataset.
 */
List getDataset()
{
    if(_dataset!=null) return _dataset;
    Map m1 = new HashMap(); m1.put("state", "Ohio"); m1.put("month", "Jan"); m1.put("value", 4);
    Map m2 = new HashMap(); m2.put("state", "Ohio"); m2.put("month", "Feb"); m2.put("value", 3);
    Map m3 = new HashMap(); m3.put("state", "Ohio"); m3.put("month", "Mar"); m3.put("value", 7);
    Map m4 = new HashMap(); m4.put("state", "Idaho"); m4.put("month", "Jan"); m4.put("value", 1);
    Map m5 = new HashMap(); m5.put("state", "Idaho"); m5.put("month", "Feb"); m5.put("value", 3);
    Map m6 = new HashMap(); m6.put("state", "Idaho"); m6.put("month", "Mar"); m6.put("value", 6);
    Map m7 = new HashMap(); m7.put("state", "Texas"); m7.put("month", "Jan"); m7.put("value", 1);
    Map m8 = new HashMap(); m8.put("state", "Texas"); m8.put("month", "Feb"); m8.put("value", 2);
    Map m9 = new HashMap(); m9.put("state", "Texas"); m9.put("month", "Mar"); m9.put("value", 7);
    return _dataset=Arrays.asList(m1,m2,m3,m4,m5,m6,m7,m8,m9);
}

}