package com.reportmill.app;
import com.reportmill.base.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import snap.swing.*;
import snap.util.*;

/**
 * Swing UI editing for undos.
 */
public class UndoInspector extends SwingOwner {
    
    // The Undos, Redos and Changes JLists
    JList        _undosList, _redosList, _changesList;
    
    // A list of sources we've seen, so we can generate small ids
    List         _sources = new ArrayList();

/**
 * Initialize UI panel (loading it the first time).
 */
protected void initUI()
{
    _undosList = getNode("UndosList", JList.class);
    _redosList = getNode("RedosList", JList.class);
    _changesList = getNode("ChangesList", JList.class);
}

/**
 * Updates the UI controls from the current undoer.
 */
public void resetUI()
{
    // Get undoer undos and redos
    Undoer undoer = RMEditor.getMainEditor().getUndoer();
    List <UndoSet> undos = undoer.getUndoSets();
    List <UndoSet> redos = undoer.getRedoSets();
    
    // Get titles (reversed)
    String titles[] = new String[undos.size()];
    for(int i=0, iMax=undos.size(); i<iMax; i++) titles[i] = undos.get(iMax-1-i).getFullUndoTitle();
    
    // Reload data, preserving selection
    int index = _undosList.getSelectedIndex();
    _undosList.setListData(titles);
    if(index<undos.size()) _undosList.setSelectedIndex(index);

    // Replace with titles
    titles = new String[redos.size()];
    for(int i=0, iMax=redos.size(); i<iMax; i++) titles[i] = redos.get(iMax-1-i).getFullRedoTitle();
    
    // Reload data, preserving selection
    index = _redosList.getSelectedIndex();
    _redosList.setListData(titles);
    if(index<redos.size()) _redosList.setSelectedIndex(index);
}

/**
 * Responds to UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Handle UndoList
    if(anEvent.getTarget()==_undosList) {
        
        // Get selected index (just return if null)
        int index = _undosList.getModel().getSize() - 1 - _undosList.getSelectedIndex(); if(index<0) return;
        
        // Get undoer and undo event
        Undoer undoer = RMEditor.getMainEditor().getUndoer();
        UndoSet undoEvent = undoer.getUndoSets().get(index);
        
        // Add to changes jlist
        _changesList.setListData(undoEvent.getChanges().toArray());

        // Clear redo selection
        _redosList.setSelectedIndex(-1);
        setNodeValue("ChangeText", "");
    }

    // Handle RedoList
    if(anEvent.getTarget()==_redosList) {

        // Get selected index (just return if null)
        int index = _redosList.getModel().getSize() - 1 - _redosList.getSelectedIndex(); if(index<0) return;
        
        // Get all redos
        List <UndoSet> redos = RMEditor.getMainEditor().getUndoer().getRedoSets();
        
        // Get selected redo
        UndoSet undoEvent = redos.get(index);
        
        // Add to object jlist
        _changesList.setListData(undoEvent.getChanges().toArray());
        
        // Clear undo selection
        _undosList.setSelectedIndex(-1);
        setNodeValue("ChangeText", "");
    }
    
    // Handle ChangeList
    if(anEvent.getTarget()==_changesList) {
        PropertyChangeEvent e = (PropertyChangeEvent)_changesList.getSelectedValue();
        int index = e instanceof IndexedPropertyChangeEvent? ((IndexedPropertyChangeEvent)e).getIndex() : -1;
        setNodeValue("ChangeText", e.getSource().getClass().getSimpleName() + "(" + getId(e.getSource()) + ") " +
            e.getPropertyName() + " " + e.getOldValue() + " " + e.getNewValue() + " " +
            (index>=0? index : "") + "\n\n" + e.getSource());
    }
}

/**
 * Returns a unique id for given source.
 */
public int getId(Object anObj)
{
    int id = RMListUtils.indexOfId(_sources, anObj);
    if(id<0) { id = _sources.size(); _sources.add(anObj); }
    return id;
}

/**
 * Returns the string used in the inspector window title.
 */
public String getWindowTitle()  { return "Undo Inspector"; }

}