package snap.swing;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * A class to run a dialog box.
 */
public class DialogBox {

    // The main message to display
    String            _message;
    
    // The title of the dialog box
    String            _title;
    
    // The type of box
    Type              _type = Type.Message;
    
    // The type of message
    MessageType       _messageType = MessageType.Plain;
    
    // The options for buttons
    String            _options[];
    
    // The content for dialog box
    JComponent        _content;
    
    // The icon
    Icon              _icon;
    
    // Constants for DialogBox type
    public enum Type { Message, Confirm, Option, Input }
    
    // Constants for tone of dialog box
    public enum MessageType { Plain, Question, Information, Warning, Error }
    
    // Standard Options
    public static final String[] OPTIONS_OK = { "OK" };
    public static final String[] OPTIONS_OK_CANCEL = { "OK", "Cancel" };
    public static final String[] OPTIONS_YES_NO_CANCEL = { "Yes", "No", "Cancel" };
    
    // Return values
    public static final int OK_OPTION = 0; /** Return value form class method if OK is chosen. */
    public static final int YES_OPTION = 0; /** Return value form class method if YES is chosen. */
    public static final int NO_OPTION = 1;  /** Return value from class method if NO is chosen. */
    public static final int CANCEL_OPTION = 2;   /** Return value from class method if CANCEL is chosen. */

/**
 * Creates a new SwingDialogBox.
 */
public DialogBox()  { }

/**
 * Creates a new SwingDialogBox with given title.
 */
public DialogBox(String aTitle)  { setTitle(aTitle); }

/**
 * Returns the message to display.
 */
public String getMessage()  { return _message; }

/**
 * Sets the message to display.
 */
public void setMessage(String aMessage)  { _message = aMessage; }

/**
 * Sets the message to display.
 */
public void setErrorMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Error); }

/**
 * Sets the message to display.
 */
public void setWarningMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Warning); }

/**
 * Sets the message to display.
 */
public void setQuestionMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Question); }

/**
 * Returns the title of the dialog box.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title of the dialog box.
 */
public void setTitle(String aTitle)  { _title = aTitle; }

/**
 * Returns the type of the box.
 */
public Type getType()  { return _type; }

/**
 * Sets the type of the box.
 */
public void setType(Type aType)  { _type = aType; }

/**
 * Returns the message type of the box.
 */
public MessageType getMessageType()  { return _messageType; }

/**
 * Sets the message type of the box.
 */
public void setMessageType(MessageType aMessageType)  { _messageType = aMessageType; }

/**
 * Returns the options strings.
 */
public String[] getOptions()
{
    if(_options!=null) return _options;
    if(getType()==Type.Message) return OPTIONS_OK;
    return OPTIONS_OK_CANCEL;
}

/**
 * Sets the option strings.
 */
public void setOptions(String ... theOptions)  { _options = theOptions; }

/**
 * Returns the content for dialog box.
 */
public JComponent getContent()  { return _content; }

/**
 * Sets the content for dialog box.
 */
public void setContent(JComponent aComp)  { _content = aComp; }

/**
 * Returns the Swing message type.
 */
private int getSwingMessageType()
{
    switch(getMessageType()) {
        case Question: return JOptionPane.QUESTION_MESSAGE;
        case Information: return JOptionPane.INFORMATION_MESSAGE;
        case Warning: return JOptionPane.WARNING_MESSAGE;
        case Error: return JOptionPane.ERROR_MESSAGE;
        default: return JOptionPane.PLAIN_MESSAGE;
    }
}

/**
 * Returns the icon.
 */
public Icon getIcon()  { return _icon!=null || getContent()!=null? _icon : getIconDefault(); }

/**
 * Sets the icon.
 */
public void setIcon(Icon anIcon)  { _icon = anIcon; }

/**
 * Returns the icon.
 */
private Icon getIconDefault()
{
    switch(getMessageType()) {
        case Question: return UIManager.getIcon("OptionPane.questionIcon");
        case Information: return UIManager.getIcon("OptionPane.informationIcon");
        case Warning: return UIManager.getIcon("OptionPane.warningIcon");
        case Error: return UIManager.getIcon("OptionPane.errorIcon");
        default: return UIManager.getIcon("OptionPane.informationIcon");
    }
}

/**
 * Runs the panel.
 */
public void showMessageDialog(JComponent aComp)
{
    setType(Type.Message);
    Object content = getContent()!=null? getContent() : getMessage();
    if(getContent()!=null) SwingUtilities.invokeLater(new Runnable() { public void run() { initDialogPanel(); }});
    JOptionPane.showMessageDialog(aComp, content, getTitle(), getSwingMessageType());
}

/**
 * Shows an option dialog.
 */
public boolean showConfirmDialog(JComponent aComp)
{
    setType(Type.Confirm);
    Object content = getContent()!=null? getContent() : getMessage();
    if(getContent()!=null) SwingUtilities.invokeLater(new Runnable() { public void run() { initDialogPanel(); }});
    int option = JOptionPane.showOptionDialog(aComp, content, getTitle(), JOptionPane.DEFAULT_OPTION, 
        getSwingMessageType(), getIcon(), getOptions(), getOptions()[0]);
    return option==0;
}

/**
 * Shows an option panel.
 */
public int showOptionDialog(JComponent aComp, String aDefault)
{
    setType(Type.Option);
    Object content = getContent()!=null? getContent() : getMessage();
    if(getContent()!=null) SwingUtilities.invokeLater(new Runnable() { public void run() { initDialogPanel(); }});
    int option = JOptionPane.showOptionDialog(aComp, content, getTitle(), JOptionPane.DEFAULT_OPTION, 
        getSwingMessageType(), getIcon(), getOptions(), aDefault);
    return option;
}

/**
 * Shows an input panel.
 */
public String showInputDialog(JComponent aComp, String aDefault)
{
    setType(Type.Input);
    Object content = getContent()!=null? getContent() : getMessage();
    if(getContent()!=null) SwingUtilities.invokeLater(new Runnable() { public void run() { initDialogPanel(); }});
    String input = (String)JOptionPane.showInputDialog(aComp, content, getTitle(), getSwingMessageType(),
        getIcon(), null, aDefault);
    return input;
}

/**
 * Closes the running dialog panel, with option to click the okay button.
 */
public void confirm()
{
    JDialog dialog = SwingUtils.getParent(getContent(), JDialog.class); if(dialog==null) return;
    if(dialog!=null && dialog.getRootPane().getDefaultButton()!=null) {
        final JButton button = dialog.getRootPane().getDefaultButton();
        button.requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() { public void run() { button.doClick(); }});
    }
    else { System.err.println("DialogBox: Couldn't find window or default button"); dialog.setVisible(false); }
}

/**
 * Closes the running dialog panel, with option to click the okay button.
 */
public void cancel()
{
    JDialog dialog = SwingUtils.getParent(getContent(), JDialog.class);
    if(dialog==null) { System.err.println("DialogBox: Couldn't find window"); return; }
    dialog.setVisible(false);
}

/**
 * Registers EnterAction for dialog.
 */
protected void initDialogPanel()
{
    // If there is an owner and it has first focus component, set it to focus
    SwingOwner sowner = (SwingOwner)SwingHelper.getSwingHelper(getContent()).getOwner(getContent());
    if(sowner!=null && sowner.getFirstFocus()!=null)
        sowner.requestFocus(sowner.getFirstFocus());
        
    // Make ENTER dismiss dialog
    JDialog win = (JDialog)SwingUtilities.getWindowAncestor(getContent()); if(win==null) return;
    JRootPane rootPane = win.getRootPane();
    KeyStroke keyStroke = KeyStroke.getKeyStroke("released ENTER"); String name = "ENTERAction";
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
    rootPane.getActionMap().put(name, new AbstractAction(name) { public void actionPerformed(ActionEvent e) {
        confirm(); }});
}

}