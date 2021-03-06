package com.reportmill.app;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import java.awt.*;
import javax.swing.JList;
import snap.swing.*;

/**
 * This class just runs a simple modal panel to register user name and email on first run.
 */
public class SpellCheckPanel extends SwingOwner {
    
    // The current text being checked
    RMTextShape             _workingText;
    
    // The current suspected misspelled word
    RMSpellCheck.Word       _word;
    
    // The shared instance of the spell check panel
    static SpellCheckPanel  _shared;
    
/**
 * Returns the shared instance.
 */
public static SpellCheckPanel getShared()  { return _shared!=null? _shared : (_shared=new SpellCheckPanel()); }

/**
 * Runs the spell check panel.
 */
public void show()
{
    // Make window visible and find next misspelling
    getWindow().setVisible(SwingWindow.Pos.CENTER, 0, 0, "SpellCheck", false);
    findNextMisspelling();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure SuggestionList to watch for MouseClicks
    enableEvents("SuggestionList", MouseClicked);
}

/**
 * Reset the UI.
 */
public void resetUI()
{
    // Reset WordText
    setNodeValue("WordText", _word==null? "" : _word.getString());
    
    // Reset SuggestionList
    if(_word!=null)
        getNode("SuggestionList", JList.class).setListData(_word.getSuggestions().toArray());
    else getNode("SuggestionList", JList.class).setListData(new Object[] { });
    
    // Select first item
    setNodeSelectedIndex("SuggestionList", 0);
}

/**
 * Handles okay button.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle CloseButton: Just close the window and return
    if(anEvent.equals("CloseButton")) {
        setWindowVisible(false); return; }
    
    // Handle IgnoreButton: find and hilight next misspelled word
    if(anEvent.equals("FindNextButton"))
        findNextMisspelling();
    
    // Handle CorrectButton
    else if(anEvent.equals("CorrectButton"))
        doCorrection();
    
    // Handle SuggestionList
    else if(anEvent.equals("SuggestionList") && anEvent.getClickCount()>1)
        doCorrection();
}

/**
 * Find next misspelling.
 */
public void findNextMisspelling()
{
    // Get main editor and text editor and text
    RMEditor editor = RMEditor.getMainEditor();
    RMTextEditor textEditor = editor.getTextEditor();
    RMTextShape text = getText(); if(text==null) return;
    
    // Get next misspelled word
    _word = RMSpellCheck.getMisspelledWord(text.getText(), textEditor.getSelEnd());
    
    // If not null, select word
    if(_word!=null)
        textEditor.setSel(_word.getStart(), _word.getEnd());
    
    // Otherwise, set selection to end and beep
    else {
        textEditor.setSel(textEditor.length());
        Toolkit.getDefaultToolkit().beep();
    }

    // Reset panel
    resetLater();
}

/**
 * Do correction.
 */
public void doCorrection()
{
    // Get main editor and text editor and text
    RMEditor editor = RMEditor.getMainEditor();
    RMTextEditor textEditor = editor.getTextEditor();
    
    // Get suggested word from list
    String correctWord = getNodeStringValue("SuggestionList");
    
    // Replace in text editor
    if(_word!=null && correctWord!=null) {
        textEditor.setSel(_word.getStart(), _word.getEnd());
        textEditor.replace(correctWord);
    }
    
    // Find next misspelling
    findNextMisspelling();
}

/**
 * Returns the text that this spell check panel should be checking.
 */
private RMTextShape getText()
{
    // Get main editor and selected shape
    RMEditor editor = RMEditor.getMainEditor();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();

    // If shape has changed do the right thing
    if(shape!=_workingText) {
        
        // If new shape is text, make it the working text
        if(shape instanceof RMTextShape)
            _workingText = (RMTextShape)shape;
        
        // If new shape isn't text, but is on same page as previous workingText, select previous working text
        else if(_workingText!=null && shape.getPageShape()==_workingText.getPageShape()) {
        }
        
        // Otherwise, set workingText to null
        else _workingText = null;
    }
    
    // Make sure working text is superselected
    if(_workingText!=null && _workingText!=editor.getSuperSelectedShape()) {
        editor.setSuperSelectedShape(_workingText);
        editor.getTextEditor().setSel(0);
    }
    
    // Return working text
    return _workingText;
}

}