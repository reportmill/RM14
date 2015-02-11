package com.reportmill.swing.shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import snap.swing.Swing;
import snap.util.*;

/**
 * An RMShape subclass for JTextArea.
 */
public class JTextAreaShape extends JTextComponentShape {

    // Whether to send action on return
    boolean         _sendActionOnReturn;

/**
 * Returns whether text area sends action on return.
 */
public boolean getSendActionOnReturn()  { return _sendActionOnReturn; }

/**
 * Sets whether text area sends action on return.
 */
public void setSendActionOnReturn(boolean aValue)
{
    firePropertyChange("SendActionOnReturn", _sendActionOnReturn, _sendActionOnReturn = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JTextArea.class; }

/**
 * Override to configure JTextArea.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get JTextArea and configure
    JTextArea tarea = (JTextArea)aComp;
    if(getSendActionOnReturn()) {
        InputMap imap = tarea.getInputMap();
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        Object key = imap.get(enter);
        tarea.getActionMap().put(key, new AbstractAction("EnterAction") {
            public void actionPerformed(ActionEvent e) { Swing.sendEvent(e); }});
    }
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive text component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jtextarea");
    
    // Archive SendActionOnReturn
    if(getSendActionOnReturn()) e.add("send-action-on-return", true);

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive text component attributes
    super.fromXMLShape(anArchiver, anElement);

    // Unarchive send action on return
    if(anElement.hasAttribute("send-action-on-return"))
        setSendActionOnReturn(anElement.getAttributeBoolValue("send-action-on-return"));
}

}