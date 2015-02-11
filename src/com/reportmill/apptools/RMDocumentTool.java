package com.reportmill.apptools;
import com.reportmill.app.RMEditor;
import com.reportmill.app.RMEditorProxGuide;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides UI for configuring an RMDocument.
 */
public class RMDocumentTool <T extends RMDocument> extends RMTool <T> {
    
    // Whether document tool should show normal inspector or advanced
    boolean          _advanced;

    // The array of supported paper sizes
    static RMSize    _paperSizes[];
    
    // The array of supported paper size names
    static String    _paperSizeNames[];

/**
 * Returns the class that tool edits.
 */
public Class getShapeClass()  { return RMDocument.class; }

/**
 * Returns the name to be show in inspector window.
 */
public String getWindowTitle()  { return "Document Inspector"; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set PaperSizeComboBox paper size names and UnitComboBox Unit values
    setNodeItems("PaperSizeComboBox", _paperSizeNames);
    setNodeItems("UnitComboBox", RMDocument.Unit.values());
}

/**
 * Resets the UI controls from the current document.
 */
public void resetUI()
{
    // Get currently selected document and its page size
    RMDocument doc = getSelectedShape(); if(doc==null) return;
    RMPage page = doc.getSelectedPage();
    
    // Make sure appropriate panel is set
    getUI(SwitchPane.class).setSelectedIndex(_advanced? 1 : 0);
    
    // Handle Advanced controls
    if(_advanced) {
        setNodeValue("PaginateCheckBox", doc.isPaginate());
        setNodeValue("CompressCheckBox", doc.getCompress());
        setNodeValue("ProximityGuideCheckBox", RMEditorProxGuide.isEnabled());
        return;
    }

    // Set PageWidthText and PageHeightText
    setNodeValue("PageWidthText", RMStringUtils.toString(getUnitsFromPoints(page.getWidth())));
    setNodeValue("PageHeightText", RMStringUtils.toString(getUnitsFromPoints(page.getHeight())));
    
    // Update PaperSizeComboBox: Get index of PaperName for Page.Size and set SelectedIndex
    int sindex = 0; for(int i=1; i<_paperSizeNames.length && sindex==0; i++) { RMSize size = _paperSizes[i];
        if(size.equals(page.getSize()) || size.equals(page.getHeight(), page.getWidth())) sindex = i; }
    getNode("PaperSizeComboBox", JComboBox.class).setSelectedIndex(sindex); // default to "custom"
    
    // Reset Units and orientation controls
    setNodeValue("UnitComboBox", doc.getUnit());
    setNodeValue("PortraitRadioButton", page.getHeight()>=page.getWidth());
    setNodeValue("LandscapeRadioButton", page.getWidth()>page.getHeight());
    
    // Reset Margin controls
    setNodeValue("LeftMarginText", RMStringUtils.toString(getUnitsFromPoints(doc.getMarginLeft())));
    setNodeValue("RightMarginText", RMStringUtils.toString(getUnitsFromPoints(doc.getMarginRight())));
    setNodeValue("TopMarginText", RMStringUtils.toString(getUnitsFromPoints(doc.getMarginTop())));
    setNodeValue("BottomMarginText", RMStringUtils.toString(getUnitsFromPoints(doc.getMarginBottom())));
    setNodeValue("DrawMarginCheckBox", doc.getShowMargin());
    setNodeValue("SnapMarginCheckBox", doc.getSnapMargin());
    
    // Reset Grid controls
    setNodeValue("ShowGridCheckBox", doc.getShowGrid());
    setNodeValue("SnapGridCheckBox", doc.getSnapGrid());
    setNodeValue("GridSpacingText", RMStringUtils.toString(getUnitsFromPoints(doc.getGridSpacing())));
    
    // Reset Page Layout controls and null string text
    setNodeValue("SingleRadio", doc.getPageLayout()==RMDocument.PageLayout.Single);
    setNodeValue("DoubleRadio", doc.getPageLayout()==RMDocument.PageLayout.Double);
    setNodeValue("FacingRadio", doc.getPageLayout()==RMDocument.PageLayout.Facing);
    setNodeValue("ContinuousRadio", doc.getPageLayout()==RMDocument.PageLayout.Continuous);
    setNodeValue("NullStringText", doc.getNullString());
    
    // Repaint PageSizeView
    getNode("PageSizeView").repaint();
}

/**
 * Responds to controls in UI to update current document.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get current document and page (just return if null)
    RMDocument doc = getSelectedShape(); if(doc==null) return;
    RMPage page = doc.getSelectedPage();
    
    // Set boolean for whether we need to resize window
    boolean resizeWindow = false;

    // Handle PageWidthText, PageHeightText
    if(anEvent.equals("PageWidthText") || anEvent.equals("PageHeightText")) {
        float w = getNodeFloatValue("PageWidthText");
        float h = getNodeFloatValue("PageHeightText");
        doc.setPageSize(getPointsFromUnits(w), getPointsFromUnits(h));
        resizeWindow = true;
    }
    
    // Handle PaperSizeComboBox
    if(anEvent.equals("PaperSizeComboBox")) {
        String sizeKey = anEvent.getStringValue();
        if(!sizeKey.equals("Custom")) {
            int index = RMArrayUtils.indexOf(_paperSizeNames, sizeKey);
            double w = _paperSizes[index].width;
            double h = _paperSizes[index].height;
            doc.setPageSize(w, h);
        }
        resizeWindow = true;
    }
    
    // Handle UnitComboBox
    if(anEvent.equals("UnitComboBox"))
        doc.setUnit((RMDocument.Unit)anEvent.getValue());
    
    // Handle PortraitRadioButton, LandscapeRadioButton
    if((anEvent.equals("PortraitRadioButton") && page.getWidth()>page.getHeight()) ||
        (anEvent.equals("LandscapeRadioButton") && page.getHeight()>page.getWidth())) {
        doc.setPageSize(page.getHeight(), page.getWidth());
        resizeWindow = true;
    }
    
    // Handle margin Texts
    if(anEvent.equals("LeftMarginText") || anEvent.equals("RightMarginText") ||
        anEvent.equals("TopMarginText") || anEvent.equals("BottomMarginText")) {
        float l = getNodeFloatValue("LeftMarginText");
        float r = getNodeFloatValue("RightMarginText");
        float t = getNodeFloatValue("TopMarginText");
        float b = getNodeFloatValue("BottomMarginText");
        doc.setMargins(getPointsFromUnits(l), getPointsFromUnits(r), getPointsFromUnits(t), getPointsFromUnits(b));
    }

    // Handle DrawMarginCheckBox, SnapMarginCheckBox
    if(anEvent.equals("DrawMarginCheckBox")) doc.setShowMargin(anEvent.getBoolValue());
    if(anEvent.equals("SnapMarginCheckBox")) doc.setSnapMargin(anEvent.getBoolValue());
    
    // Handle ShowGridCheckBox, SnapGridCheckBox, GridSpacingText
    if(anEvent.equals("ShowGridCheckBox")) doc.setShowGrid(anEvent.getBoolValue());
    if(anEvent.equals("SnapGridCheckBox")) doc.setSnapGrid(anEvent.getBoolValue());
    if(anEvent.equals("GridSpacingText")) doc.setGridSpacing(getPointsFromUnits(anEvent.getFloatValue()));
    
    // Handle Page Layout options: SingleRadio, DoubleRadio, FacingRadio and ContinuousRadio
    if(anEvent.equals("SingleRadio") || anEvent.equals("DoubleRadio") ||
        anEvent.equals("FacingRadio") || anEvent.equals("ContinuousRadio")) {
        String name = RMStringUtils.delete(anEvent.getName(), "Radio");
        doc.setPageLayout(name);
        resizeWindow = true;
    }

    // Handle NullStringText
    if(anEvent.equals("NullStringText")) doc.setNullString(anEvent.getStringValue());
    
    // Handle advanced button
    if(anEvent.equals("AdvancedButton")) _advanced = !_advanced;
        
    // Handle UIAdvanced PaginateCheckBox, CompressCheckBox
    if(anEvent.equals("PaginateCheckBox")) doc.setPaginate(anEvent.getBooleanValue());
    if(anEvent.equals("CompressCheckBox")) doc.setCompress(anEvent.getBoolValue());
    if(anEvent.equals("ProximityGuideCheckBox")) RMEditorProxGuide.setEnabled(anEvent.getBoolValue());
    if(anEvent.equals("BasicButton")) _advanced = !_advanced;
    
    // If page size changed, make sure window is right size
    if(resizeWindow) {
        if(getEditorPane().getWindow().isVisible())
            getEditorPane().getWindow().pack();
        getNode("PageSizeView").repaint();
    }
}

/**
 * Overrides tool method to declare that documents have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

// Initialize page sizes and paper size names
static {
    _paperSizeNames = new String[15]; _paperSizes = new RMSize[15];
    _paperSizeNames[0] = "Custom"; _paperSizes[0] = new RMSize(612, 792);
    _paperSizeNames[1] = "Letter"; _paperSizes[1] = new RMSize(612, 792);
    _paperSizeNames[2] = "Legal"; _paperSizes[2] = new RMSize(612, 1008);
    _paperSizeNames[3] = "Tabloid"; _paperSizes[3] = new RMSize(792, 1224);
    _paperSizeNames[4] = "Exec"; _paperSizes[4] = new RMSize(540, 720);
    _paperSizeNames[5] = "#10 Env"; _paperSizes[5] = new RMSize(684, 306);
    _paperSizeNames[6] = "Banner"; _paperSizes[6] = new RMSize(500, 100);
    _paperSizeNames[7] = "Small"; _paperSizes[7] = new RMSize(320, 240);
    _paperSizeNames[8] = "Medium"; _paperSizes[8] = new RMSize(640, 480);
    _paperSizeNames[9] = "Large"; _paperSizes[9] = new RMSize(800, 600);
    _paperSizeNames[10] = "A3"; _paperSizes[10] = new RMSize(842, 1190);
    _paperSizeNames[11] = "A4"; _paperSizes[11] = new RMSize(595, 842);
    _paperSizeNames[12] = "A5"; _paperSizes[12] = new RMSize(420, 595);
    _paperSizeNames[13] = "B4"; _paperSizes[13] = new RMSize(729, 1032);
    _paperSizeNames[14] = "B5"; _paperSizes[14] = new RMSize(516, 729);
}

/** An inner class to render Page control. */
public static class PageSizeView extends JComponent {
    public PageSizeView() { setOpaque(false); }
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
        RMSize pageSize = editor.getDocument().getPageSize();
        double maxHeight = Math.max(17*72, pageSize.height);
        double scale = (getHeight()-10)/maxHeight;
        float pageW = Math.round(pageSize.width*scale);
        float pageH = Math.round(pageSize.height*scale);
        float pageX = Math.round((getWidth() - pageW)/2f);
        float pageY = Math.round((getHeight() - pageH)/2f);
        g.setColor(Color.black); RMAWTUtils.fillRect(g2, pageX+5, pageY+5, pageW, pageH);
        g.setColor(Color.white); RMAWTUtils.fillRect(g2, pageX, pageY, pageW, pageH);
        g.setColor(Color.black); RMAWTUtils.drawRect(g2, pageX, pageY, pageW, pageH);
    }
}

}