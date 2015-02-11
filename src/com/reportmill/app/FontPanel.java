package com.reportmill.app;
import com.reportmill.text.*;
import java.awt.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class is a Swing/Ribs Font panel for selecting fonts. It lets the user easily select a font family,
 * font size and font style (bold, italic, underline, outline). It also has a convenient slider for interactively
 * changing the size and a text field for specifically setting a size. In addition, there is a pick list that
 * shows all the individual fonts available for a given family.
 */
public class FontPanel extends SwingOwner {
    
/**
 * Initialize Swing UI panel.
 */
protected void initUI()
{
    getNode("FamilyList", JList.class).setListData(RMFontUtils.getFamilyNames());
    getNode("SizesList", JList.class).setListData(new Object[] { 6,8,9,10,11,12,14,16,18,22,24,36,48,64,72,96,128,144 }); 
    getNode("BoldButton", JToggleButton.class).setMargin(new Insets(0,0,0,0));
    getNode("ItalicButton", JToggleButton.class).setMargin(new Insets(0,0,0,0));
    getNode("UnderlineButton", JToggleButton.class).setMargin(new Insets(0,0,0,0));
    getNode("OutlineButton", JToggleButton.class).setMargin(new Insets(0,0,0,0));
}

/**
 * Reset Swing UI from the current selection.
 */
public void resetUI()
{
    // Get current font
    RMEditor editor = RMEditor.getMainEditor();
    RMFont font = RMEditorShapes.getFont(editor);
    
    // Get family name and size
    String familyName = font.getFamily();
    double size = font.getSize();
    
    // Reset FamilyList, SizesList, SizeText, SizeThumb, and Bold, Italic, Underline and Outline buttons
    setNodeValue("FamilyList", familyName);
    setNodeValue("SizesList", (int)size);
    setNodeValue("SizeText", "" + size + " pt");
    setNodeValue("SizeThumb", size);
    setNodeValue("BoldButton", font.isBold());
    setNodeEnabled("BoldButton", font.getBold()!=null);
    setNodeValue("ItalicButton", font.isItalic());
    setNodeEnabled("ItalicButton", font.getItalic()!=null);
    setNodeValue("UnderlineButton", RMEditorShapes.isUnderlined(editor));
    setNodeValue("OutlineButton", RMEditorShapes.getOutline(editor)!=null);
    
    // Get font names in currently selected font's family
    String familyNames[] = RMFontUtils.getFontNames(font.getFamily());
    
    // Reset FontNameComboBox data
    JComboBox fontNameComboBox = getNode("FontNameComboBox", JComboBox.class);
    fontNameComboBox.removeAllItems();
    for(int i=0, iMax=familyNames.length; i<iMax; i++)
        fontNameComboBox.addItem(familyNames[i]);
    
    // Reset FontNameComboBox selection
    String fn = font.awt().getFontName();
    fontNameComboBox.setSelectedItem(fn);
    
    // Reset FontNameComboBox enabled
    fontNameComboBox.setEnabled(familyNames.length>1);
}

/**
 * Respond to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get current editor
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    
    // Handle BoldButton
    if(anEvent.equals("BoldButton"))
        RMEditorShapes.setFontBold(editor, anEvent.getBoolValue());
    
    // Handle ItalicButton
    if(anEvent.equals("ItalicButton"))
        RMEditorShapes.setFontItalic(editor, anEvent.getBoolValue());
    
    // Handle UnderlineButton
    if(anEvent.equals("UnderlineButton"))
        RMEditorShapes.setUnderlined(editor);
    
    // Handle OutlineButton
    if(anEvent.equals("OutlineButton"))
        RMEditorShapes.setOutline(editor);
    
    // Handle SizeThumbwheel
    if(anEvent.equals("SizeThumb"))
        RMEditorShapes.setFontSize(editor, anEvent.getIntValue(), false);
    
    // Handle SizesList
    if(anEvent.equals("SizesList") && anEvent.getValue()!=null)
        RMEditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
    
    // Handle SizeText
    if(anEvent.equals("SizeText")) {
        RMEditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
        editor.requestFocus();
    }

    // Handle FamilyList
    if(anEvent.equals("FamilyList")) {
        String familyName = getNodeStringValue("FamilyList");
        String fontName = RMFontUtils.getFontNames(familyName)[0];
        RMFont font = RMFont.getFont(fontName, 12);
        RMEditorShapes.setFontFamily(editor, font);
    }
    
    // Handle FontNameComboBox
    if(anEvent.equals("FontNameComboBox")) {
        RMFont font = RMFont.getFont(anEvent.getStringValue(), 12);
        RMEditorShapes.setFontName(editor, font);
    }
}
    
/** Returns the name for the inspector window. */
public String getWindowTitle()  { return "Font Panel"; }

}