package com.reportmill.app;
import javax.swing.JComponent;
import snap.swing.*;

/**
 * Runs a simple panel letting the user choose a dataset key element, like table, graph, crosstab or labels.
 */
public class DatasetKeyPanel extends SwingOwner {
    
    // The selected dataset key element type
    byte        _selectedType = TABLE;
    
    // The DialogBox
    DialogBox   _dbox = new DialogBox("Dataset Key Element");

    // Constants for Dataset Element Types
    public static final byte TABLE = 1;
    public static final byte LABELS = 2;
    public static final byte GRAPH = 3;
    public static final byte CROSSTAB = 4;

/**
 * Runs the dataset key panel.
 */
public int showDatasetKeyPanel(JComponent aComponent)
{
    _dbox.setContent(getUI());
    return _dbox.showConfirmDialog(aComponent)? _selectedType : 0;
}

/**
 * Initialize UI.
 */
public void initUI()
{
    // Configure buttons to accept clicks so we can watch for double click
    enableEvents("TableButton", MouseClicked);
    enableEvents("LabelsButton", MouseClicked);
    enableEvents("GraphButton", MouseClicked);
    enableEvents("CrossTabButton", MouseClicked);
}

/**
 * Handles input from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle TableButton, LabelsButton, GraphButton, CrossTabButton
    if(anEvent.equals("TableButton")) _selectedType = TABLE;
    if(anEvent.equals("LabelsButton")) _selectedType = LABELS;
    if(anEvent.equals("GraphButton")) _selectedType = GRAPH;
    if(anEvent.equals("CrossTabButton")) _selectedType = CROSSTAB;

    // Handle any double-click
    if(anEvent.getClickCount()>1)
        _dbox.confirm();
}

}