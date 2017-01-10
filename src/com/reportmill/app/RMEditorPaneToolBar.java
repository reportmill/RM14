package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.graphics.RMColor;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.JComponentShape;
import com.reportmill.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import snap.util.Undoer;
import snap.swing.*;

/**
 * Tool bar for RMEditorPane.
 */
public class RMEditorPaneToolBar extends SwingOwner {

    // The editor pane that this tool bar works for
    RMEditorPane          _editorPane;
    
    // The font face combobox
    JComboBox             _fontFaceComboBox;
    
    // The font face combobox text
    JTextComponent        _fontFaceText;
    
    // The font size combobox
    JComboBox             _fontSizeComboBox;
    
    // The toolbar tools
    static RMTool         _toolBarTools[] = createToolBarTools();

/**
 * Creates a new editor pane tool bar.
 */
public RMEditorPaneToolBar(RMEditorPane anEditorPane)  { _editorPane = anEditorPane; }

/**
 * Returns the editor pane.
 */
public RMEditorPane getEditorPane()  { return _editorPane; }

/**
 * Returns the editor pane editor.
 */
public RMEditor getEditor()  { return getEditorPane().getEditor(); }

/**
 * Initializes UI panel.
 */
protected void initUI()
{
    // Reset layout to box layout
    getUI().setPreferredSize(new Dimension(500, 95));
        
    // Get names of tool bar buttons and tool bar buttons that can be set
    String names1[] = { "NewButton", "OpenButton", "SaveButton", "PreviewPDFButton", "PreviewHTMLButton", "PrintButton",
        "CutButton", "CopyButton", "PasteButton", "DeleteButton", "UndoButton", "RedoButton",
        "MoneyButton", "PercentButton", "CommaButton", "DecimalAddButton", "DecimalRemoveButton",
        "BoldButton", "ItalicButton", "UnderlineButton", // Start of TextToolBar buttons
        "AlignLeftButton", "AlignCenterButton", "AlignRightButton", "AlignFullButton",
        "FontSizeUpButton", "FontSizeDownButton" };
    String names2[] = { "MoneyButton", "PercentButton", "CommaButton",
        "BoldButton", "ItalicButton", "UnderlineButton" }; // Start of TextToolBar buttons

    // Iterate over tool bar buttons, promote base 18x18 to 22x22 and add roll-over as black line border
    for(int i=0; i<names1.length; i++) { JButton button = getNode(names1[i], JButton.class);
        if(button==null)
            button = getNode(names1[i], JButton.class);
        IconUtils.setRolloverIcons(button, RMArrayUtils.contains(names2, names1[i]), 22, 22); }
    
    // Configure ColorPicker buttons
    ColorPickerButton fcp = getNode("FillColorPickerButton", ColorPickerButton.class);
    fcp.setTitle("Fill Color"); fcp.setSaveColor(true);
    ColorPickerButton scp = getNode("StrokeColorPickerButton", ColorPickerButton.class);
    scp.setTitle("Stroke Color"); scp.setSaveColor(true);
    ColorPickerButton tcp = getNode("TextColorPickerButton", ColorPickerButton.class);
    tcp.setTitle("Text Color"); tcp.setSaveColor(true);

    // Get FontFaceComboBox, FontFaceText and FontFaceComboBox
    _fontFaceComboBox = getNode("FontFaceComboBox", JComboBox.class);
    _fontFaceText = (JTextComponent)_fontFaceComboBox.getEditor().getEditorComponent();
    setNodeItems(_fontFaceComboBox, RMFontUtils.getFamilyNames());
    
    // Set FontFaceComboBox cell renderer to render font names in font
    _fontFaceComboBox.setPrototypeDisplayValue("Prototype Display Value");
    _fontFaceComboBox.setRenderer(new DefaultListCellRenderer() {
        { setPreferredSize(new Dimension(200, 22)); setVerticalAlignment(SwingConstants.CENTER); }
        public Component getListCellRendererComponent(JList aList, Object aVal, int index, boolean isSel, boolean isFoc)
        {
            // Do normal version
            super.getListCellRendererComponent(aList, aVal, index, isSel, isFoc);
            
            // If not visible or prototype value, short circuit so it doesn't run through all fonts unnecessarily 
            if(!_fontFaceComboBox.isPopupVisible() || aVal.equals("Prototype Display Value")) return this;
            
            // Get font
            String fontName = aVal instanceof String? (String)aVal : null;
            Font font = fontName!=null? RMFontUtils.getFont(fontName, 18) : null;
            if(font!=null) { setFont(font); setToolTipText(fontName); }
            return this;
        }
    });
    
    // Get the font size combobox and configure with data/model and max row count
    _fontSizeComboBox = getNode("FontSizeComboBox", JComboBox.class);
    Object sizes[] = { 6, 8, 9, 10, 11, 12, 14, 16, 18, 22, 24, 36, 48, 64, 72, 96, 128, 144 };
    setNodeItems(_fontSizeComboBox, sizes);

    // Build popup menu for preview button and add mouseListener to PreviewDropDown button to bring up the menu
    final JPopupMenu previewMenu = createPreviewButtonPopup();
    getNode("PreviewDropDown").addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            previewMenu.show(e.getComponent(), 3, e.getComponent().getHeight()); } });
}

/**
 * Updates the Swing UI panel controls.
 */
protected void resetUI()
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // Update UndoButton, RedoButton
    Undoer undoer = editor.getUndoer();
    setNodeEnabled("UndoButton", undoer!=null && undoer.getUndoSetLast()!=null);
    setNodeEnabled("RedoButton", undoer!=null && undoer.getRedoSetLast()!=null);
    
    // Update BoldButton, PercentButton, CommaButton, DecimalAddButton, DecimalRemoveButton
    RMFormat format = RMEditorShapes.getFormat(editor);
    setNodeValue("MoneyButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isLocalCurrencySymbolUsed());
    setNodeValue("PercentButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isPercentSymbolUsed());
    setNodeValue("CommaButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isGroupingUsed());
    setNodeEnabled("DecimalAddButton", format instanceof RMNumberFormat);
    setNodeEnabled("DecimalRemoveButton", format instanceof RMNumberFormat);    

    // Update FontFaceComboBox, FontSizeComboBox, BoldButton, ItalicButton, UnderlineButton, FontSizeDownButton.Enabled
    RMFont font = RMEditorShapes.getFont(editor); double size = font.getSize(); Number snum = size;
    setNodeValue("FontFaceComboBox", font.getFamily()); if(size==(int)size) snum = snum.intValue();
    setNodeValue("FontSizeComboBox", snum); // if is whole number, reset to integer
    setNodeValue("BoldButton", font.isBold());
    setNodeValue("ItalicButton", font.isItalic());
    setNodeValue("UnderlineButton", RMEditorShapes.isUnderlined(editor));
    setNodeEnabled("FontSizeDownButton", font.getSize()>6);

    // Reset PreviewEditButton state if out of sync
    if(getNodeBoolValue("PreviewEditButton")==getEditorPane().isEditing())
        setNodeValue("PreviewEditButton", !getEditorPane().isEditing());

    // Get selected tool button name and button - if found and not selected, select it
    String toolButtonName = editor.getCurrentTool().getClass().getSimpleName() + "Button";
    JToggleButton toolButton = getNode(toolButtonName, JToggleButton.class);
    if(toolButton!=null && !toolButton.isSelected())
        toolButton.setSelected(true);
}

/**
 * Responds to Swing UI panel control changes.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Get the editor
    RMEditor editor = getEditor();
    
    // Handle File NewButton, OpenButton, SaveButton, PreviewPDFButton, PreviewHTMLButton, PrintButton
    if(anEvent.equals("NewButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("OpenButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("SaveButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PreviewPDFButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PreviewHTMLButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PrintButton")) _editorPane.respondUI(anEvent);
        
    // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
    if(anEvent.equals("CutButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("CopyButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PasteButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("DeleteButton")) editor.delete();
        
    // Handle Edit UndoButton, RedoButton
    if(anEvent.equals("UndoButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("RedoButton")) _editorPane.respondUI(anEvent);
    
    // Handle MoneyButton: If currently selected format is number format, add or remove dollars
    if(anEvent.equals("MoneyButton")) {
        if(RMEditorShapes.getFormat(editor) instanceof RMNumberFormat) {
            RMNumberFormat format = (RMNumberFormat)RMEditorShapes.getFormat(editor); // Get number format
            format = (RMNumberFormat)format.clone(); // Clone it
            format.setLocalCurrencySymbolUsed(!format.isLocalCurrencySymbolUsed()); // If no currency, add default
            RMEditorShapes.setFormat(editor, format);
        }
        
        // If no current format, set basic dollar format
        else RMEditorShapes.setFormat(editor, RMNumberFormat.CURRENCY);
    }
    
    // Handle PercentButton: If currently selected format is number format, add or remove percent symbol
    if(anEvent.equals("PercentButton")) {
        if(RMEditorShapes.getFormat(editor) instanceof RMNumberFormat) {
            RMNumberFormat format = (RMNumberFormat)RMEditorShapes.getFormat(editor); // Get number format
            format = (RMNumberFormat)format.clone(); // Clone it
            format.setPercentSymbolUsed(!format.isPercentSymbolUsed()); // Toggle whether percent symbol is used
            RMEditorShapes.setFormat(editor, format); // Set new format
        }
        
        // If no current format, set basic dollar format
        else RMEditorShapes.setFormat(editor, new RMNumberFormat("#,##0.00 %"));
    }
    
    // Handle CommaButton: If currently selected format is number format, add or remove grouping
    if(anEvent.equals("CommaButton")) {
        if(RMEditorShapes.getFormat(editor) instanceof RMNumberFormat) {
            RMNumberFormat format = (RMNumberFormat)RMEditorShapes.getFormat(editor); // Get number format
            format = (RMNumberFormat)format.clone(); // Clone it
            format.setGroupingUsed(!format.isGroupingUsed()); // Toggle whether grouping is used
            RMEditorShapes.setFormat(editor, format); // Set new format
        }
        
        // If no current format, set basic dollar format
        else RMEditorShapes.setFormat(editor, new RMNumberFormat("#,##0.00"));
    }
    
    // Handle DecimalAddButton: If currently selected format is number format, add decimal
    if(anEvent.equals("DecimalAddButton")) {
        if(RMEditorShapes.getFormat(editor) instanceof RMNumberFormat) {
            RMNumberFormat format = (RMNumberFormat)RMEditorShapes.getFormat(editor); // Get number format
            format = (RMNumberFormat)format.clone(); // Clone it
            format.setMinimumFractionDigits(format.getMinimumFractionDigits()+1); // Add decimal digits
            format.setMaximumFractionDigits(format.getMinimumFractionDigits());
            RMEditorShapes.setFormat(editor, format); // Set new format
        }
    }
    
    // Handle DecimalRemoveButton: If currently selected format is number format, remove decimal digits
    if(anEvent.equals("DecimalRemoveButton")) {
        if(RMEditorShapes.getFormat(editor) instanceof RMNumberFormat) {
            RMNumberFormat format = (RMNumberFormat)RMEditorShapes.getFormat(editor); // Get number format
            format = (RMNumberFormat)format.clone(); // Clone it
            format.setMinimumFractionDigits(format.getMinimumFractionDigits()-1); // Remove decimal digits
            format.setMaximumFractionDigits(format.getMinimumFractionDigits());
            RMEditorShapes.setFormat(editor, format); // Set new format
        }
    }
    
    // Handle FillColorPickerButton, StrokeColorPickerButton
    if(anEvent.equals("FillColorPickerButton")) { Color color = anEvent.getColorValue();
        RMEditorShapes.setColor(editor, color==null? null : new RMColor(color)); }
    if(anEvent.equals("StrokeColorPickerButton")) { Color color = anEvent.getColorValue();
        RMEditorShapes.setStrokeColor(editor, color==null? null : new RMColor(color)); }

    // Handle FontFaceComboBox: 
    if(anEvent.equals("FontFaceComboBox")) {
        
        // Handle KeyPressed: Need to update selection when delete/backspace pressed
        if(anEvent.isKeyPressed()) {
            int kCode = anEvent.getKeyCode();
            if((kCode==KeyEvent.VK_BACK_SPACE || kCode==KeyEvent.VK_DELETE) && _fontFaceText.getSelectionStart()>0 &&
                _fontFaceText.getSelectionStart()!=_fontFaceText.getSelectionEnd())
                _fontFaceText.setSelectionStart(_fontFaceText.getSelectionStart()-1);
            anEvent.setTriggersReset(false);
        }
        
        // Handle KeyFinished: Update selected font family name
        else if(anEvent.isKeyFinished()) {
            fontFaceTextChanged(); anEvent.setTriggersReset(false); }
        
        // Handle FocusGained: Select all text and show popup
        else if(anEvent.isFocusGained()) {
            _fontFaceText.selectAll();
            _fontFaceComboBox.showPopup(); anEvent.setTriggersReset(false); 
        }
        
        // Handle Action: Get selected index, family name, font name and font and set
        else {
            int index = anEvent.getSelectedIndex();
            String familyName = index>=0? RMFontUtils.getFamilyNames()[index] : "Arial";
            String fontName = RMFontUtils.getFontNames(familyName)[0];
            RMFont newFont = RMFont.getFont(fontName, 12);
            RMEditorShapes.setFontFamily(editor, newFont);
            requestFocus(editor);
        }
    }
    
    // Handle FontSizeComboBox
    if(anEvent.equals("FontSizeComboBox")) {
        
        // Handle FocusGained: SelectAll text and show PopUp
        if(anEvent.isFocusGained()) {
            JTextComponent fontSizeText = (JTextComponent)_fontSizeComboBox.getEditor().getEditorComponent();
            fontSizeText.selectAll();
            _fontSizeComboBox.showPopup(); anEvent.setTriggersReset(false);
        }
        
        // Handle ActionEvent
        else {
            RMEditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
            requestFocus(editor);
        }
    }

    // Handle Format BoldButton, ItalicButton, UnderlineButton
    if(anEvent.equals("BoldButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("ItalicButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("UnderlineButton")) _editorPane.respondUI(anEvent);
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    if(anEvent.equals("AlignLeftButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignCenterButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignRightButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignFullButton")) _editorPane.respondUI(anEvent);
    
    // Handle FontSizeUpButton, FontSizeDownButton, TextColorPickerButton
    if(anEvent.equals("FontSizeUpButton")) { RMFont font = RMEditorShapes.getFont(editor);
        RMEditorShapes.setFontSize(editor, font.getSize()<16? 1 : 2, true); }
    if(anEvent.equals("FontSizeDownButton")) { RMFont font = RMEditorShapes.getFont(editor);
        RMEditorShapes.setFontSize(editor, font.getSize()<16? -1 : -2, true); }
    if(anEvent.equals("TextColorPickerButton")) { Color color = anEvent.getColorValue();
        RMEditorShapes.setTextColor(editor, color==null? null : new RMColor(color)); }

    // Handle Preview/Edit button and PreviewMenuItem
    if(anEvent.equals("PreviewEditButton") || anEvent.equals("PreviewMenuItem")) {
        
        // Hack to open edited file: Get filename (create file if missing) and open file in TextEdit
        if(Swing.isAltDown()) {
            String fname = getEditor().getDocument().getFilename();
            if(fname==null) { fname = RMUtils.getTempDir() + "RMDocument.rpt"; editor.getDocument().write(fname); }
            String commands[] = { "open",  "-e", fname };
            try { Runtime.getRuntime().exec(commands); }
            catch(Exception e) { e.printStackTrace(); }
        }
        
        // Normal preview
        else getEditorPane().setEditing(!getEditorPane().isEditing());
    }
    
    // Handle PreviewXMLMenuItem
    if(anEvent.equals("PreviewXMLMenuItem"))
        RMEditorPaneUtils.previewXML(getEditorPane());

    // Handle ConvertToSwingMenuItem
    if(anEvent.equals("ConvertToSwingMenuItem"))
        convertToSwing();
    
    // Handle ToolButton(s)
    if(anEvent.getName().endsWith("ToolButton")) {
        for(RMTool tool : _toolBarTools)
            if(anEvent.getName().startsWith(tool.getClass().getSimpleName())) {
                getEditor().setCurrentTool(tool); break; }
    }
    
    // Handle AddTableButton, AddGraphButton, AddLabelsButton, AddCrossTabFrameButton
    if(anEvent.equals("AddTableButton")) RMTableTool.addTable(getEditor(), null);;
    if(anEvent.equals("AddGraphButton")) RMGraphTool.addGraph(getEditor(), null);;
    if(anEvent.equals("AddLabelsButton")) RMLabelsTool.addLabels(getEditor(), null);;
    if(anEvent.equals("AddCrossTabFrameButton")) RMCrossTabTool.addCrossTab(getEditor(), null);
    
    // Handle AddCrossTabButton, AddImagePlaceHolderMenuItem, AddSubreportMenuItem
    if(anEvent.equals("AddCrossTabButton")) RMCrossTabTool.addCrossTab(getEditor());
    if(anEvent.equals("AddImagePlaceHolderMenuItem")) RMEditorShapes.addImagePlaceholder(getEditor());
    if(anEvent.equals("AddSubreportMenuItem")) RMEditorShapes.addSubreport(getEditor());
    
    // Handle ConnectToDataSourceMenuItem
    if(anEvent.equals("ConnectToDataSourceMenuItem") || anEvent.equals("ConnectToDataSourceButton"))
        RMEditorPaneUtils.connectToDataSource(getEditorPane());

    // Handle InspectorPanelButton, ColorPanelButton, FontPanelButton
    if(anEvent.equals("InspectorPanelButton"))
        getEditorPane().getInspectorPanel().setVisible(!getEditorPane().getInspectorPanel().isVisible());
    if(anEvent.equals("KeysPanelButton")) {
        if(getEditorPane().getAttributesPanel().getVisible()!=AttributesPanel.KEYS)
            getEditorPane().getAttributesPanel().setVisible(AttributesPanel.KEYS);
        else getEditorPane().getAttributesPanel().setVisible(false); }
    if(anEvent.equals("ColorPanelButton")) {
        if(getEditorPane().getAttributesPanel().getVisible()!=AttributesPanel.COLOR)
            getEditorPane().getAttributesPanel().setVisible(AttributesPanel.COLOR);
        else getEditorPane().getAttributesPanel().setVisible(false); }
    if(anEvent.equals("FontPanelButton")) {
        if(getEditorPane().getAttributesPanel().getVisible()!=AttributesPanel.FONT)
            getEditorPane().getAttributesPanel().setVisible(AttributesPanel.FONT);
        else getEditorPane().getAttributesPanel().setVisible(false); }
}

/**
 * Called when text added or removed from font face text.
 */
public void fontFaceTextChanged()
{
    // Get text string and text string as completed font family name (if it is a prefix of an existing font family name)
    String text = _fontFaceText.getText(), textCompleted = null;
    for(int i=0; i<RMFontUtils.getFamilyNames().length && textCompleted==null; i++)
        if(RMStringUtils.startsWithIC(RMFontUtils.getFamilyNames()[i], text))
            textCompleted = RMFontUtils.getFamilyNames()[i];
    
    // If text completed is non-null, install in combobox
    if(textCompleted!=null && !text.equals(textCompleted)) {
        _fontFaceText.setText(textCompleted); // Set text completed value in combobox text
        setNodeValue(_fontFaceComboBox, textCompleted);  // Set text completed value in combobox
        _fontFaceText.setSelectionStart(text.length()); // Reset selection to part between original text and
        _fontFaceText.setSelectionEnd(textCompleted.length()); //  end of completed text
    }    
}

/**
 * Creates the list of tool instances for tool bar.
 */
protected static RMTool[] createToolBarTools()
{
    List <RMTool> tools = new ArrayList();
    tools.add(RMTool.getSelectTool());
    tools.add(RMTool.getTool(RMLineShape.class));
    tools.add(RMTool.getTool(RMRectShape.class));
    tools.add(RMTool.getTool(RMOvalShape.class));
    tools.add(RMTool.getTool(RMTextShape.class));
    tools.add(RMTool.getTool(RMPolygonShape.class));
    //tools.add(RMTool.getTool(RMStarShape.class));    
    tools.add(new RMPolygonShapeTool.PencilTool());
    //tools.add(RMTool.getTool(RMFlowShape.class));
    return tools.toArray(new RMTool[0]);
}

/**
 * Creates a pop-up menu for preview edit button (currently with look and feel options).
 */
private JPopupMenu createPreviewButtonPopup()
{
    JPopupMenu pmenu = new JPopupMenu();
    pmenu.add(SwingUtils.createMenuItem("PreviewXMLMenuItem", "Preview XML", null));
    pmenu.add(SwingUtils.createMenuItem("ConvertToSwingMenuItem", "Convert to Swing", null));
    initUI(pmenu);
    return pmenu;
}

/**
 * Convert an editor doc to swing.
 */
private void convertToSwing()
{
    RMEditor editor = getEditor();
    RMParentShape shape = editor.getContent();
    JComponentShape jcs = shape.getChildWithClass(JComponentShape.class);
    if(jcs!=null)
        editor.setContent(jcs);
}

}