package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class manages the attributes panel which holds the color panel, font panel, formatter panel and keys panel.
 */
public class AttributesPanel extends SwingOwner {
    
    // Swing UI tab pane
    JTabbedPane       _tabPane;
    
    // Keys panel
    KeysPanel         _keysPanel = new KeysPanel();
    
    // Color panel
    APColorPanel      _colorPanel = new APColorPanel();
    
    // Font panel
    FontPanel         _fontPanel = new FontPanel();
    
    // Format panel
    FormatPanel       _formatPanel = new FormatPanel();
    
    // Gallery paenl
    SwingGallery      _galleryPanel = new SwingGallery();
    
    // Inspectors
    SwingOwner        _inspectors[] = { _keysPanel, _colorPanel, _fontPanel, _formatPanel, _galleryPanel };

    // Constants for tab selection
    public static final int KEYS = 0;
    public static final int COLOR = 1;
    public static final int FONT = 2;
    public static final int FORMAT = 3;
    public static final int GALLERY = 4;

/**
 * Returns the UI panel for the attributes panel.
 */
protected JComponent createUI()
{
    _tabPane = new JTabbedPane(SwingConstants.TOP);
    _tabPane.setFont(RMAWTUtils.Helvetica11);
    _tabPane.addTab("Keys", new JLabel());
    _tabPane.addTab("Color", new JLabel());
    _tabPane.addTab("Font", new JLabel());
    _tabPane.addTab("Format", new JLabel());
    //_tabPane.addTab("Gallery", new JLabel());
    _tabPane.setPreferredSize(new Dimension(275, 290));
    return _tabPane;
}

/**
 * Initializes the UI panel.
 */
protected void initUI()
{
    getWindow().setAlwaysOnTop(true);
    getWindow().setHideOnDeactivate(true);
    getWindow().setStyle(SwingWindow.Style.Small);
}

/**
 * Updates the attributes panel UI (forwards on to inspector at selected tab).
 */
public void resetUI()
{
    // Get inspector component from tab pane
    SwingOwner inspector = _inspectors[_tabPane.getSelectedIndex()];

    // If inspector panel is JLabel, swap in real inspector UI
    if(_tabPane.getSelectedComponent() instanceof JLabel)
        _tabPane.setComponentAt(_tabPane.getSelectedIndex(), inspector.getUI());
    
    // Set window title and reset inspector
    getWindow().setTitle(RMKey.getStringValue(inspector, "getWindowTitle"));
    inspector.resetLater();
}

/**
 * Returns whether the attributes panel is visible.
 */
public boolean isVisible()  { return isUISet() && getUI().isShowing(); }

/**
 * Sets the attributes panel visible.
 */
public void setVisible(boolean aValue)
{
    // If requested visible and inspector is not visible, make visible
    if(aValue && !isVisible())
        setVisible(0);
    
    // If setting not visible, propagate on to window
    if(!aValue && isVisible())
        setWindowVisible(false);
}

/**
 * Returns the index of the currently visible tab (or -1 if attributes panel not visible).
 */
public int getVisible()  { return isVisible()? _tabPane.getSelectedIndex() : -1; }

/**
 * Sets the attributes panel visible, specifying a specific tab by the given index.
 */
public void setVisible(final int anIndex)
{
    if(anIndex==4 && _tabPane.getTabCount()==4)
        _tabPane.addTab("Gal", new JLabel());
        
    // If RMEditor is null, delay this call
    if(RMEditor.getMainEditor()==null) { runLater(new Runnable() { public void run() {
        setVisible(anIndex); }}); return; }
    
    // Get the UI
    getUI();
    
    // Set tab pane to tab at given index
    _tabPane.setSelectedIndex(anIndex);
    
    // ResetUI
    resetLater();

    // If window isn't visible, set window visible
    if(!isVisible())
        getWindow().setVisible(SwingWindow.Pos.TOP_RIGHT, -10, 10, "AttributesPanel", false);
}

/**
 * This inner class is a ColorPanel suitable for manipulating colors in current RMEditor.
 */
public class APColorPanel extends ColorPanel {
    
    /** Overrides color panel behavior to order attributes panel visible instead. */
    public void setWindowVisible(boolean aValue)
    {
        // If requested visible, have attributes panel order it front
        if(aValue && AttributesPanel.this.getVisible()!=AttributesPanel.COLOR)
            AttributesPanel.this.setVisible(AttributesPanel.COLOR);
        
        // If requested offscreen, have attributes panel requested offscreen
        else if(!aValue && AttributesPanel.this.getVisible()==AttributesPanel.COLOR)
            AttributesPanel.this.setVisible(false);
    }

    /** Overrides normal implementation to get color from editor if no color well selected. */
    public Color getColor()
    {
        // If color panel has color well, just return normal
        if(getColorWell()!=null)
            return super.getColor();
        
        // Get main editor (just return black if editor is null)
        RMEditor editor = RMEditor.getMainEditor();
        if(editor==null)
            return Color.black;
        
        // Get color from editor
        return RMEditorShapes.getSelectedColor(editor).awt();
    }

    /** Override to forward to editor. */
    public void setColor(Color aColor)
    {
        super.setColor(aColor);
        if(getColorWell()==null)
            RMEditorShapes.setSelectedColor(RMEditor.getMainEditor(), new RMColor(aColor));
    }
    
    /** Returns the name for this panel. */
    public String getWindowTitle() { return "Color Panel"; }
}

}