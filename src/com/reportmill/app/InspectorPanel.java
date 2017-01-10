package com.reportmill.app;
import com.reportmill.apptools.*;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import snap.swing.*;

/**
 * This class is responsible for the Swing UI associated with the inspector window.
 */
public class InspectorPanel extends SwingOwner {
    
    // The selection path panel
    SpringsPane          _selectionPathPanel;
    
    // The selection path button group
    ButtonGroup          _selectionPathButtonGroup = new ButtonGroup();
    
    // The JPanel used to swap in different inspectors
    SpringsPane          _inspectorPanel;
    
    // The child inspector current installed in inspector panel
    SwingOwner           _childInspector;
    
    // The inspector for shape placement attributes (location, size, roll, scale, skew, autosizing)
    ShapePlacement       _shapePlacement = new ShapePlacement();
    
    // The inspector for shape general attributes (name, url, text wrap around)
    ShapeGeneral         _shapeGeneral = new ShapeGeneral();
    
    // The inspector for shape animation
    Animation            _animation = new Animation();
    
    // The inspector for Undo
    UndoInspector        _undoInspector;
    
    // The inspector for XML datasource
    DataSourcePanel      _dataSource;
    
    // Used for managing selection path
    RMShape              _deepestShape;
    
    // Used for managing selection path
    RMShape              _selectedShape;

/**
 * Initializes UI panel for the inspector.
 */
public void initUI()
{
    // Get SelectionPathPanel and reset layout
    _selectionPathPanel = getNode("SelectionPathPanel", SpringsPane.class);
    _selectionPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
    // Get InspectorPanel and reset layout
    _inspectorPanel = getNode("InspectorPanel", SpringsPane.class);
    _inspectorPanel.setLayout(new BorderLayout());
    
    // Create the Action that redispatches the event and add the action to the action map
    addKeyActionEvent("UndoAction", "meta Z");
    
    // Configure Window
    getWindow().setAlwaysOnTop(true);
    getWindow().setHideOnDeactivate(true);
    getWindow().setResizable(false);
    getWindow().setStyle(SwingWindow.Style.Small);
}

/**
 * Refreshes the inspector for the current editor selection.
 */
public void resetUI()
{
    // Get editor (and just return if null) and tool for selected shapes
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    RMTool tool = RMTool.getTool(editor.getSelectedOrSuperSelectedShapes());
    
    // If ShapeSpecific inspector is selected, make sure we've installed inspector for the current selection
    if(getNodeBoolValue("ShapeSpecificButton"))
        setInspector(tool);
    
    // If fills inspector is showing, check to see if we need to install RibsGeneral (or re-install normal ShapeFills)
    if(getNodeBoolValue("ShapeFillsButton"))
        setInspector(tool.getShapeFillInspector());

    // Get the inspector (owner)
    SwingOwner owner = getInspector();
    
    // Get window title from owner and set
    String windowTitle = RMKey.getStringValue(owner, "getWindowTitle");
    getWindow().setTitle(windowTitle);

    // If owner non-null, tell it to reset
    if(owner!=null)
        owner.resetLater();
    
    // Reset the selection path matrix
    resetSelectionPathMatrix();
    
    // Get image for current tool and set in ShapeSpecificButton
    Icon toolIcon = tool.getIcon();
    getNode("ShapeSpecificButton", AbstractButton.class).setIcon(toolIcon);
}

/**
 * Handles changes to the inspector Swing UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle ShapePlacementButton
    if(anEvent.equals("ShapePlacementButton"))
        setInspector(_shapePlacement);
    
    // Handle ShapeGeneralButton
    if(anEvent.equals("ShapeGeneralButton"))
        setInspector(_shapeGeneral);
    
    // Handle AnimationButton
    if(anEvent.equals("AnimationButton"))
        setInspector(_animation);
    
    // Handle UndoAction
    if(anEvent.equals("UndoAction")) { RMEditor ed = RMEditor.getMainEditor(); if(ed==null) return;
        ed.undo(); }
    
    // Reset ui
    resetUI();
}

/**
 * Returns whether the inspector is visible.
 */
public boolean isVisible()  { return isUISet() && getUI().isShowing(); }

/**
 * Sets whether the inspector is visible.
 */
public void setVisible(boolean aValue)
{
    // If requested visible and inspector is not visible, make visible
    if(aValue && !isVisible())
        setVisible(-1);
    
    // If requested invisible and inspector is visible, set window not visible
    else if(!aValue && isVisible())
        setWindowVisible(false);
}

/**
 * Sets the inspector to be visible, showing the specific sub-inspector at the given index.
 */
public void setVisible(final int anIndex)
{
    // If RMEditor is null, delay this call
    if(RMEditor.getMainEditor()==null) { runLater(new Runnable() { public void run() {
        setVisible(anIndex); }}); return; }
    
    // If index 0, 1 or 3, set appropriate toggle button true
    if(anIndex==0) setNodeValue("ShapeSpecificButton", true);
    if(anIndex==1) setNodeValue("ShapeFillsButton", true);
    if(anIndex==3) setNodeValue("ShapeGeneralButton", true);
    
    // If index is 6, show _undoInspector
    if(anIndex==6) {
        setInspector(_undoInspector!=null? _undoInspector : (_undoInspector = new UndoInspector()));
        setNodeValue("OffscreenButton", true);
    }
    
    // If index is 7, show DataSource Inspector
    if(anIndex==7) {
        setInspector(_dataSource!=null? _dataSource : (_dataSource = new DataSourcePanel()));
        setNodeValue("OffscreenButton", true);
    }
    
    // If index is 8, show _animation
    if(anIndex==8) {
        setInspector(_animation);
        setNodeValue("OffscreenButton", true);
    }
    
    // If inspector panel isn't visible, set window visible
    if(!isVisible())
        getWindow().setVisible(SwingWindow.Pos.BOTTOM_RIGHT, -10, -5, "InspectorPanel", false);
}

/**
 * Returns whether the inspector is showing the datasource inspector.
 */
public boolean isShowingDataSource()  { return isUISet() && getNodeBoolValue("OffscreenButton"); }

/**
 * Returns the inspector (owner) of the inspector pane.
 */
protected SwingOwner getInspector()  { return _childInspector; }

/**
 * Sets the inspector in the inspector pane.
 */
protected void setInspector(SwingOwner anOwner)
{
    _childInspector = anOwner;
    setNodeChildren(_inspectorPanel, anOwner.getUI());
}

/**
 * Updates the selection path UI.
 */
public void resetSelectionPathMatrix() 
{
    // Get main editor, Selected/SuperSelected shape and shape that should be selected in selection path
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null) return;
    RMShape selectedShape = editor.getSelectedOrSuperSelectedShape();
    RMShape shape = _deepestShape!=null && _deepestShape.isAncestor(selectedShape)? _deepestShape : selectedShape;
    
    // If the selectedShape has changed because of external forces, reset selectionPath to point to it
    if(selectedShape != _selectedShape)
        shape = selectedShape;
    
    // Set new DeepestShape to be shape
    _deepestShape = shape; _selectedShape = selectedShape;

    // Remove extra buttons after deepest shape from panel and button group
    for(int i=_selectionPathPanel.getComponentCount()-1; i>_deepestShape.getAncestorCount()-1; i--) {
        PathSelectionButton button = (PathSelectionButton)_selectionPathPanel.getComponent(i);
        _selectionPathPanel.remove(button);
        _selectionPathButtonGroup.remove(button);
    }
    
    // Add missing buttons
    for(int i=_selectionPathPanel.getComponentCount(), iMax=selectedShape.getAncestorCount(); i<iMax; i++) {
        
        // Create new button and configure action
        PathSelectionButton button = new PathSelectionButton();
        button.setActionCommand("" + i);
        button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            popSelection(e);
            resetLater();
        }});
        
        // Add button to selection path panel and button group
        _selectionPathPanel.add(button);
        _selectionPathButtonGroup.add(button);
    }
    
    // Iterate up parent chain to configure selection path panel buttons
    for(RMShape shp=selectedShape; shp!=null && shp.getParent()!=null; shp=shp.getParent()) {
        int index = shp.getAncestorCount()-1;  // Get button index for shape and get button
        PathSelectionButton button = (PathSelectionButton)_selectionPathPanel.getComponent(index);
        Icon icon = RMTool.getTool(shp).getIcon(); button.setIcon(icon); // Icon
        button.setToolTipText(RMClassUtils.getClassSimpleName(shp)); // Tooltip
        button._paintArrow = (shp!=_deepestShape); // Whether to paint arrow
        button.setSelected(shp==selectedShape);  // Whether selected
    }
    
    // Have selection path panel perform layout and repaint
    _selectionPathPanel.revalidate(); _selectionPathPanel.repaint();
}

/**
 * Changes the selection path selection to the level of the string index in the action event.
 */
public void popSelection(ActionEvent anEvent) 
{
    // Get main editor (just return if editor or deepest shape is null)
    RMEditor editor = RMEditor.getMainEditor(); if(editor==null || _deepestShape==null) return;
    
    // Get selected item
    int selectedIndex = Integer.parseInt(anEvent.getActionCommand());
    
    // If user selected descendant of current selected shape, select on down to it
    if(selectedIndex > editor.getSelectedOrSuperSelectedShape().getAncestorCount()-1) {
        
        // Get current deepest shape
        RMShape shape = _deepestShape;

        // Find shape that was clicked on
        while(selectedIndex != shape.getAncestorCount()-1)
            shape = shape.getParent();

        // If shape parent's childrenSuperSelectImmediately, superSelect shape
        if(shape.getParent().childrenSuperSelectImmediately())
            editor.setSuperSelectedShape(shape);

        // If shape shouldn't superSelect, just select it
        else editor.setSelectedShape(shape);
    }

    // If user selected ancestor of current shape, pop selection up to it
    else while(selectedIndex != editor.getSelectedOrSuperSelectedShape().getAncestorCount()-1)
        editor.popSelection();

    // Set selected shape to new editor selected shape
    _selectedShape = editor.getSelectedOrSuperSelectedShape();
    
    // Make sure shape specific inspector is selected
    if(!getNodeBoolValue("ShapeSpecificButton"))
        getNode("ShapeSpecificButton", JToggleButton.class).doClick();
}

/**
 * Makes the inspector panel show the document inspector.
 */
public void showDocumentInspector()
{
    setVisible(0); // Select the shape specific inspector
    resetSelectionPathMatrix(); // Reset selection path matrix
    popSelection(new ActionEvent(this, 0, "0")); // Pop selection
}

/**
 * This class is used to render buttons in the path selection matrix (separated by arrows).
 */
private static class PathSelectionButton extends JToggleButton {
    
    // Whether to paint arrow after button and the shape for the arrow drawn between path selection buttons
    boolean _paintArrow = true;
    static Polygon _arrow = new Polygon(new int[] {39, 44, 39}, new int[] {15, 20, 25}, 3);

    /** Creates a new button. */
    public PathSelectionButton()
    {
        setBorderPainted(false); setContentAreaFilled(false); setPreferredSize(new Dimension(44, 40));
    }
    
    /** Paint button: white background rect (if selected), icon and arrow. */
    protected void paintComponent(Graphics g)
    {
        if(isSelected()) { g.setColor(Color.white); g.fillRect(0, 0, 39, 40); }
        Icon i = getIcon(); if(i!=null) i.paintIcon(this, g, (39-i.getIconWidth())/2, (40-i.getIconHeight())/2);
        if(_paintArrow) { g.setColor(Color.darkGray); g.fillPolygon(_arrow); }
    }
}

}