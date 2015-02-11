package com.reportmill.apptools;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * Tool for Swing shape.
 */
public class RMSwitchShapeTool <T extends RMSwitchShape> extends RMParentShapeTool <T> {

/**
 * Returns the shape class this tool edits.
 */
public Class getShapeClass() { return RMSwitchShape.class; }

/**
 * Returns the window title for this tool
 */
public String getWindowTitle() { return "SwitchShape Inspector"; }

/**
 * Initialize UI panel for this tool.
 */
protected void initUI()
{
    // Configure SwitchList
    getNode("SwitchList", JList.class).setCellRenderer(new SwitchVersionCellRenderer());
}

/**
 * Reset Swing UI panel controls
 */
public void resetUI()
{
    // Get currently selected switch shape and versions (just return if null)
    RMSwitchShape shape = getSelectedShape(); if(shape==null) return;
    List <String> versions = getVersionNames(); if(versions==null) return;
    
    // Update SwitchList Items and SelectedItem
    setNodeItems("SwitchList", versions);
    setNodeSelectedItem("SwitchList", shape.getVersion());
    
    // Update VersionKeyText
    setNodeValue("VersionKeyText", shape.getVersionKey());
}

public void respondUI(SwingEvent anEvent)
{
    // Get currently selected switch shape (just return if null)
    RMSwitchShape shape = getSelectedShape(); if(shape==null) return;
    
    // Register for repaint (and thus undo)
    shape.repaint();

    // Handle SwitchList
    if(anEvent.equals("SwitchList")) {
        shape.undoerSetUndoTitle("Change Version");
        shape.setVersion(anEvent.getStringValue());
    }
    
    // Handle ClearButton (either remove version or beep if they try to remove standard)
    if(anEvent.equals("ClearButton")) {
        String version = shape.getVersion();
        if(version.equals(shape.getDefaultVersionName()))
            Toolkit.getDefaultToolkit().beep();
        else {
            shape.undoerSetUndoTitle("Remove Version");
            shape.removeVersion(version);
        }
    }
    
    // Handle _addButton
    if(anEvent.equals("AddButton")) {
        String msg = "Enter label for custom version:", title = "Custom Version";
        DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg);
        String version = dbox.showInputDialog(getUI(), null);
        if(version != null) {
            shape.repaint(); // Because last one is eaten by OptionPane event loop
            shape.undoerSetUndoTitle("Change Version");
            shape.setVersion(version);
        }
    }
    
    // Handle VersionKeyText
    if(anEvent.equals("VersionKeyText"))
        shape.setVersionKey(anEvent.getStringValue());
}

/**
 * Get list of shape versions, plus default versions.
 */
public List <String> getVersionNames()
{
    RMSwitchShape s = getSelectedShape(); if(s==null) return Collections.emptyList();
    List names = s.getVersionNames();
    RMListUtils.moveToFront(names, "Standard");
    return names;
}

/**
 * An inner class to draw versions JList.
 */
protected class SwitchVersionCellRenderer extends JLabel implements ListCellRenderer {
    public SwitchVersionCellRenderer() { setOpaque(true); }
    public Component getListCellRendererComponent(JList list, Object val, int index, boolean isSel, boolean hasFoc) {
        RMSwitchShape shape = getSelectedShape(); if(shape==null) return this;
        String version = (String)val;
        setText(version);
        setFont(shape.hasVersion(version)? RMAWTUtils.HelveticaBold12 : RMAWTUtils.Helvetica12);
        setForeground(isSel? list.getSelectionForeground() : list.getForeground());
        setBackground(isSel? list.getSelectionBackground() : list.getBackground());
        setEnabled(list.isEnabled());
        return this;
    }
}

}