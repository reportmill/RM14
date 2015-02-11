package com.reportmill.app;
import com.reportmill.apptools.RMTableRowTool;
import com.reportmill.base.*;
import com.reportmill.text.RMTextEditor;
import java.io.File;
import javax.swing.*;
import snap.swing.*;
import snap.util.Undoer;

/**
 * Menu bar for RMEditor pane.
 */
public class RMEditorPaneMenuBar extends SwingOwner {

    // The editor pane this menu bar works for
    RMEditorPane   _editorPane;
    
/**
 * Creates a new editor pane menu bar.
 */
public RMEditorPaneMenuBar(RMEditorPane anEditorPane)  { _editorPane = anEditorPane; }

/**
 * Returns the editor pane.
 */
public RMEditorPane getEditorPane()  { return _editorPane; }

/**
 * Returns the editor pane editor.
 */
public RMEditor getEditor()  { return getEditorPane().getEditor(); }

/**
 * Returns the MenuBar.
 */
public JMenuBar getMenuBar()  { return (JMenuBar)getUI(); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure CheckSpellingAsYouTypeMenuItem and HyphenateTextMenuItem
    setNodeValue("CheckSpellingAsYouTypeMenuItem", RMTextEditor.isSpellChecking);
    setNodeValue("HyphenateTextMenuItem", RMTextEditor.isHyphenating);
        
    // If MacOSX, Remove Separator and QuitMenuItem from FileMenu, remove separator and Preferences from ToolsMenu
    if(RMUtils.isMac) {
        JMenu fileMenu = getNode("FileMenu", JMenu.class);
        fileMenu.remove(fileMenu.getItemCount()-1); fileMenu.remove(fileMenu.getItemCount()-1);
        JMenu toolsMenu = getNode("ToolsMenu", JMenu.class);
        toolsMenu.remove(toolsMenu.getItemCount()-1); toolsMenu.remove(toolsMenu.getItemCount()-1);
    }
}

/** Override to make available to package. */
protected void initUI(Object anObj)  { super.initUI(anObj); }

/**
 * Updates the editor's UI.
 */
protected void resetUI()
{
    // Get the editor undoer
    Undoer undoer = getEditor().getUndoer();

    // Update UndoMenuItem
    String uTitle = undoer==null || undoer.getUndoSetLast()==null? "Undo" : undoer.getUndoSetLast().getFullUndoTitle();
    setNodeValue("UndoMenuItem", uTitle);
    setNodeEnabled("UndoMenuItem", undoer!=null && undoer.getUndoSetLast()!=null);

    // Update RedoMenuItem
    String rTitle = undoer==null || undoer.getRedoSetLast()==null? "Redo" : undoer.getRedoSetLast().getFullRedoTitle();
    setNodeValue("RedoMenuItem", rTitle);
    setNodeEnabled("RedoMenuItem", undoer!=null && undoer.getRedoSetLast()!=null);
    
    // Update ShowRulersMenuItem
    setNodeValue("ShowRulersMenuItem", getEditorPane().getShowRulers());
}

/**
 * Handles changes to the editor's UI controls.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Handle NewMenuItem, NewButton: Get new editor pane and make visible
    if(anEvent.equals("NewMenuItem") || anEvent.equals("NewButton")) {
        RMEditorPane editorPane = RMClassUtils.newInstance(getEditorPane()).newDocument();
        editorPane.setWindowVisible(true);
    }
    
    // Handle OpenMenuItem, OpenButton: Get new editor pane from open panel and make visible (if created)
    if(anEvent.equals("OpenMenuItem") || anEvent.equals("OpenButton")) {
        RMEditorPane editorPane = RMClassUtils.newInstance(getEditorPane()).open();
        if(editorPane!=null)
            editorPane.setWindowVisible(true);
    }
    
    // Handle CloseMenuItem
    if(anEvent.equals("CloseMenuItem")) getEditorPane().close();
    
    // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, SaveAsPDFMenuItem, RevertMenuItem
    if(anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton")) getEditorPane().save();
    if(anEvent.equals("SaveAsMenuItem")) getEditorPane().saveAs();
    if(anEvent.equals("SaveAsPDFMenuItem")) RMEditorPaneUtils.saveAsPDF(getEditorPane());
    if(anEvent.equals("RevertMenuItem")) getEditorPane().revert();
    
    // Handle PrintMenuItem, QuitMenuItem
    if(anEvent.equals("PrintMenuItem") || anEvent.equals("PrintButton")) getEditor().print(null, !Swing.isAltDown());
    if(anEvent.equals("QuitMenuItem")) getEditorPane().quit();
        
    // Handle File -> Preview Reports menu items
    if(anEvent.equals("PreviewPDFMenuItem") || anEvent.equals("PreviewPDFButton"))
        RMEditorPaneUtils.previewPDF(getEditorPane());
    if(anEvent.equals("PreviewHTMLMenuItem") || anEvent.equals("PreviewHTMLButton"))
        RMEditorPaneUtils.previewHTML(getEditorPane());
    if(anEvent.equals("PreviewCSVMenuItem")) RMEditorPaneUtils.previewCSV(getEditorPane());
    if(anEvent.equals("PreviewExcelMenuItem")) RMEditorPaneUtils.previewXLS(getEditorPane());
    if(anEvent.equals("PreviewRTFMenuItem")) RMEditorPaneUtils.previewRTF(getEditorPane());
    if(anEvent.equals("PreviewJPEGMenuItem")) RMEditorPaneUtils.previewJPG(getEditorPane());
    if(anEvent.equals("PreviewPNGMenuItem")) RMEditorPaneUtils.previewPNG(getEditorPane());
        
    // Handle File -> Samples menu items
    if(anEvent.equals("MoviesMenuItem")) RMEditorPaneUtils.openSample("Movies");
    if(anEvent.equals("MoviesGraphMenuItem")) RMEditorPaneUtils.openSample("MoviesGraph");
    if(anEvent.equals("MoviesLabelsMenuItem")) RMEditorPaneUtils.openSample("MoviesLabels");
    if(anEvent.equals("HollywoodMenuItem"))RMEditorPaneUtils.openSample("Jar:/com/reportmill/examples/HollywoodDB.xml");
    if(anEvent.equals("SalesMenuItem")) RMEditorPaneUtils.openSample("Jar:/com/reportmill/examples/Sales.xml");
        
    // Handle Edit menu items
    if(anEvent.equals("UndoMenuItem") || anEvent.equals("UndoButton")) getEditor().undo();
    if(anEvent.equals("RedoMenuItem") || anEvent.equals("RedoButton")) getEditor().redo();
    if(anEvent.equals("CutMenuItem") || anEvent.equals("CutButton")) getEditor().cut();
    if(anEvent.equals("CopyMenuItem") || anEvent.equals("CopyButton")) getEditor().copy();
    if(anEvent.equals("PasteMenuItem") || anEvent.equals("PasteButton")) getEditor().paste();
    if(anEvent.equals("SelectAllMenuItem")) getEditor().selectAll();
    if(anEvent.equals("CheckSpellingMenuItem")) SpellCheckPanel.getShared().show();
    
    // Edit -> CheckSpellingAsYouTypeMenuItem
    if(anEvent.equals("CheckSpellingAsYouTypeMenuItem")) {
        RMTextEditor.isSpellChecking = anEvent.getBooleanValue();
        RMPrefsUtils.prefsPut("SpellChecking", RMTextEditor.isSpellChecking? Boolean.TRUE : Boolean.FALSE);
        getEditor().repaint();
    }
    
    // Edit -> HyphenateTextMenuItem
    if(anEvent.equals("HyphenateTextMenuItem")) {
        RMTextEditor.isHyphenating = anEvent.getBooleanValue();
        RMPrefsUtils.prefsPut("Hyphenating", RMTextEditor.isHyphenating? Boolean.TRUE : Boolean.FALSE);
        getEditor().repaint();
    }
        
    // Handle Format menu items (use name because anObj may come from popup menu)
    if(anEvent.equals("FontPanelMenuItem")) getEditorPane().getAttributesPanel().setVisible(AttributesPanel.FONT);
    if(anEvent.equals("BoldMenuItem") || anEvent.equals("BoldButton"))
        RMEditorShapes.setFontBold(getEditor(), !RMEditorShapes.getFont(getEditor()).isBold());
    if(anEvent.equals("ItalicMenuItem") || anEvent.equals("ItalicButton"))
        RMEditorShapes.setFontItalic(getEditor(), !RMEditorShapes.getFont(getEditor()).isItalic());
    if(anEvent.equals("UnderlineMenuItem") || anEvent.equals("UnderlineButton"))
        RMEditorShapes.setUnderlined(getEditor());
    if(anEvent.equals("OutlineMenuItem")) RMEditorShapes.setOutline(getEditor());
    if(anEvent.equals("AlignLeftMenuItem") || anEvent.equals("AlignLeftButton"))
        RMEditorShapes.setAlignmentX(getEditor(), RMTypes.AlignX.Left);
    if(anEvent.equals("AlignCenterMenuItem") || anEvent.equals("AlignCenterButton"))
        RMEditorShapes.setAlignmentX(getEditor(), RMTypes.AlignX.Center);
    if(anEvent.equals("AlignRightMenuItem") || anEvent.equals("AlignRightButton"))
        RMEditorShapes.setAlignmentX(getEditor(), RMTypes.AlignX.Right);
    if(anEvent.equals("AlignFullMenuItem") || anEvent.equals("AlignFullButton"))
        RMEditorShapes.setAlignmentX(getEditor(), RMTypes.AlignX.Full);
    if(anEvent.equals("SuperscriptMenuItem")) RMEditorShapes.setSuperscript(getEditor());
    if(anEvent.equals("SubscriptMenuItem")) RMEditorShapes.setSubscript(getEditor());
        
    // Handle Pages menu items
    if(anEvent.equals("AddPageMenuItem")) getEditor().addPage();
    if(anEvent.equals("AddPagePreviousMenuItem")) getEditor().addPagePrevious();
    if(anEvent.equals("RemovePageMenuItem")) getEditor().removePage();
    if(anEvent.equals("ZoomInMenuItem")) getEditor().setZoomFactor(getEditor().getZoomFactor() + .1f);
    if(anEvent.equals("ZoomOutMenuItem")) getEditor().setZoomFactor(getEditor().getZoomFactor() - .1f);
    if(anEvent.equals("Zoom100MenuItem")) getEditor().setZoomFactor(1);
    if(anEvent.equals("Zoom200MenuItem")) getEditor().setZoomFactor(2);
    if(anEvent.equals("ZoomToggleLastMenuItem")) getEditor().zoomToggleLast();
    if(anEvent.equals("ZoomToMenuItem")) getEditor().runZoomPanel();
        
    // Handle Shapes menu items (use name because anObj may come from popup menu)
    String name = anEvent.getName();
    if(name.equals("GroupMenuItem")) RMEditorShapes.groupShapes(getEditor(), null, null);
    if(name.equals("UngroupMenuItem")) RMEditorShapes.ungroupShapes(getEditor());
    if(name.equals("BringToFrontMenuItem")) RMEditorShapes.bringToFront(getEditor());
    if(name.equals("SendToBackMenuItem")) RMEditorShapes.sendToBack(getEditor());
    if(name.equals("MakeRowTopMenuItem")) RMEditorShapes.makeRowTop(getEditor());
    if(name.equals("MakeRowCenterMenuItem")) RMEditorShapes.makeRowCenter(getEditor());
    if(name.equals("MakeRowBottomMenuItem")) RMEditorShapes.makeRowBottom(getEditor());
    if(name.equals("MakeColumnLeftMenuItem")) RMEditorShapes.makeColumnLeft(getEditor());
    if(name.equals("MakeColumnCenterMenuItem")) RMEditorShapes.makeColumnCenter(getEditor());
    if(name.equals("MakeColumnRightMenuItem")) RMEditorShapes.makeColumnRight(getEditor());
    if(name.equals("MakeSameSizeMenuItem")) RMEditorShapes.makeSameSize(getEditor());
    if(name.equals("MakeSameWidthMenuItem")) RMEditorShapes.makeSameWidth(getEditor());
    if(name.equals("MakeSameHeightMenuItem")) RMEditorShapes.makeSameHeight(getEditor());
    if(name.equals("SizeToFitMenuItem")) RMEditorShapes.setSizeToFit(getEditor());
    if(name.equals("EquallySpaceRowMenuItem")) RMEditorShapes.equallySpaceRow(getEditor());
    if(name.equals("EquallySpaceColumnMenuItem")) RMEditorShapes.equallySpaceColumn(getEditor());
    if(name.equals("GroupInSwitchShapeMenuItem")) RMEditorShapes.groupInSwitchShape(getEditor());
    if(name.equals("GroupInScene3DMenuItem")) RMEditorShapes.groupInScene3D(getEditor());
    if(name.equals("GroupInMorphShapeMenuItem")) RMEditorShapes.groupInMorphShape(getEditor());
    if(name.equals("GroupInAnimPathMenuItem")) RMEditorShapes.groupInAnimationPath(getEditor());
    if(name.equals("GroupInPainterShapeMenuItem")) RMEditorShapes.groupInPainterShape(getEditor());
    if(name.equals("GroupInPanelMenuItem")) RMEditorShapes.groupInPanel(getEditor());
    if(name.equals("GroupInTabbedPaneMenuItem")) RMEditorShapes.groupInTabbedPane(getEditor());
    if(name.equals("GroupInScrollPaneMenuItem")) RMEditorShapes.groupInScrollPane(getEditor());
    if(name.equals("MoveToNewLayerMenuItem")) RMEditorShapes.moveToNewLayer(getEditor());
    if(name.equals("CombinePathsMenuItem")) RMEditorShapes.combinePaths(getEditor());
    if(name.equals("SubtractPathsMenuItem")) RMEditorShapes.subtractPaths(getEditor());
    if(name.equals("ConvertToImageMenuItem")) RMEditorShapes.convertToImage(getEditor());
    
    // Handle Tools menu items
    if(anEvent.equals("InspectorMenuItem")) getEditorPane().getInspectorPanel().setVisible(-1);
    if(anEvent.equals("ColorPanelMenuItem")) getEditorPane().getAttributesPanel().setVisible(AttributesPanel.COLOR);
    if(anEvent.equals("FormatPanelMenuItem")) getEditorPane().getAttributesPanel().setVisible(AttributesPanel.FORMAT);
    if(anEvent.equals("KeysPanelMenuItem")) getEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);
    if(anEvent.equals("GalleryPanelMenuItem")) getEditorPane().getAttributesPanel().setVisible(AttributesPanel.GALLERY);
    
    // Handle ShowRulersMenuItem, FeedbackMenuItem, PrefsMenuItem
    if(anEvent.equals("ShowRulersMenuItem")) getEditorPane().setShowRulers(!getEditorPane().getShowRulers());
    if(anEvent.equals("FeedbackMenuItem")) FeedbackPanel.runModal();
    if(anEvent.equals("PrefsMenuItem")) new PreferencesPanel().showPanel(getEditor());
    
    // Handle SupportPageMenuItem, TutorialMenuItem, BasicAPIMenuItem, TablesMenuItem
    if(anEvent.equals("SupportPageMenuItem")) RMURLUtils.openURL("http://reportmill.com/support");
    if(anEvent.equals("TutorialMenuItem")) RMURLUtils.openURL("http://reportmill.com/support/tutorial.pdf");
    if(anEvent.equals("BasicAPIMenuItem")) RMURLUtils.openURL("http://reportmill.com/support/BasicApi.pdf");
    if(anEvent.equals("TablesMenuItem")) RMURLUtils.openURL("http://reportmill.com/support/tables.pdf");
    
    // Handle Help -> JavaWebStartMenuItem
    if(anEvent.equals("JavaWebStartMenuItem")) {
        String javaws = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaws";
        try { Runtime.getRuntime().exec(new String[] { javaws, "-viewer"}); }
        catch(Exception e) { System.err.println(e.getMessage()); }
    }
    
    // Handle AddColumnMenuItem, SplitColumnMenuItem (from right mouse pop-up)
    if(anEvent.equals("AddColumnMenuItem")) RMTableRowTool.addColumn();
    if(anEvent.equals("SplitColumnMenuItem")) RMEditorShapes.splitHorizontal(getEditor());
}

}