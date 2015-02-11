package com.reportmill.app;
import com.reportmill.text.RMFontUtils;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides Swing UI editing for the currently selected shapes location and size.
 */
public class ShapePlacement extends SwingOwner {
    
    // Tab pane for each inspector
    JTabbedPane         _tabPane;
    
    // ShapeLocationSize inspector
    ShapeLocationSize   _locationSize = new ShapeLocationSize();
    
    // ShapeRollScaleSkew inspector
    ShapeRollScaleSkew  _rollScaleSkew = new ShapeRollScaleSkew();

    // ShapeLayout inspector
    ShapeLayout         _layout = new ShapeLayout();

/**
 * Returns the name to be used in the inspector's window title.
 */
public String getWindowTitle()  { return "Placement Inspector"; }

/**
 * Create UI panel for this inspector.
 */
protected JComponent createUI()
{
    _tabPane = new JTabbedPane();
    _tabPane.setFont(RMFontUtils.getFont("Arial", 11));
    return _tabPane;
}

/**
 * Initialize UI panel for this inspector.
 */
protected void initUI()
{
    _tabPane.addTab("Location/Size", _locationSize.getUI());
    _tabPane.addTab("Roll/Scale", _rollScaleSkew.getUI());
    _tabPane.addTab("Layout", _layout.getUI());
}

/**
 * Updates Swing UI controls from current selection.
 */
public void resetUI()
{
    switch(_tabPane.getSelectedIndex()) {
        case 0: _locationSize.resetLater(); break;
        case 1: _rollScaleSkew.resetLater(); break;
        case 2: _layout.resetLater(); break;
    }
}

}