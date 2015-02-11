package com.reportmill.text;
import com.reportmill.base.RMPrefsUtils;
import java.awt.*;
import javax.swing.*;
import snap.swing.*;

/**
 * A simple text pane.
 */
public class RMTextPane extends SwingOwner {

    // The text area
    RMTextArea              _textArea;

/**
 * Returns the text area.
 */
public RMTextArea getTextArea()  { return _textArea; }

/**
 * Returns the text editor.
 */
public RMTextEditor getTextEditor()  { return getTextArea().getTextEditor(); }

/**
 * Returns the text string.
 */
public String getText()  { getUI(); return getTextArea().getText(); }

/**
 * Sets the text string.
 */
public void setText(String aString)  { getUI(); getTextArea().setText(aString); }

/**
 * Override to add SaveAction KeyActionEvent.
 */
protected void initUI()
{
    // Get text area and start listening to its text editor
    _textArea = getNode("TextArea", RMTextArea.class);
    
    // Set border
    getUI().setBorder(BorderFactory.createLineBorder(Color.lightGray));

    // Register command-s to send action for "SaveButton"
    addKeyActionEvent("SaveButton", "meta S");    
}

/**
 * Reset ui.
 */
public void resetUI()
{
    // Reset FontSizeText
    setNodeValue("FontSizeText", getTextArea().getFont().getSize2D());
}

/**
 * Respond to Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle SaveButton
    if(anEvent.equals("SaveButton"))
        saveFile();
    
    // Handle FontSizeText
    if(anEvent.equals("FontSizeText")) {
        Font font = getTextArea().getFont(), font2 = font.deriveFont(anEvent.getFloatValue());
        getTextArea().setFont(font2);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            SwingUtilities.invokeLater(new Runnable() { public void run() {
                getNode("FontSizeText", JTextField.class).selectAll(); }}); }});
        RMPrefsUtils.prefsPut("TextPaneFontSize", font2.getSize2D()); 
    }
    
    // Handle IncreaseFontButton
    if(anEvent.equals("IncreaseFontButton")) {
        Font font = getTextArea().getFont(), font2 = font.deriveFont(font.getSize2D()+1);
        getTextArea().setFont(font2);
        RMPrefsUtils.prefsPut("TextPaneFontSize", font2.getSize2D()); 
    }
    
    // Handle DecreaseFontButton
    if(anEvent.equals("DecreaseFontButton")) {
        Font font = getTextArea().getFont(), font2 = font.deriveFont(font.getSize2D()-1);
        getTextArea().setFont(font2);
        RMPrefsUtils.prefsPut("TextPaneFontSize", font2.getSize2D()); 
    }
    
    // Handle UndoButton
    if(anEvent.equals("UndoButton")) {
        if(getTextEditor().getUndoer().hasUndos())
            getTextEditor().undo();
        else Toolkit.getDefaultToolkit().beep();
    }
    
    // Handle RedoButton
    if(anEvent.equals("RedoButton")) {
        if(getTextEditor().getUndoer().getRedoSetLast()!=null)
            getTextEditor().redo();
        else Toolkit.getDefaultToolkit().beep();
    }
}

/**
 * Save file.
 */
public void saveFile()  { }

}