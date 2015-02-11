package snap.util;
import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * This class represents a single undo but holds a list of PropertyChangeEvents.
 */
public class UndoSet {
    
    // The title of the undo
    String                      _undoTitle;
    
    // The list of change events
    List <PropertyChangeEvent>  _changes = new ArrayList();
    
    // The objects to be selected if undo is executed
    Object                      _undoSelection;
    
    // The objects to be selected if redo is selected
    Object                      _redoSelection;
    
    // Whether this event has been coalesced
    boolean                     _coalesced;

/**
 * Creates a new empty undo set.
 */
public UndoSet() { }

/**
 * Returns the undo title.
 */
public String getUndoTitle()  { return _undoTitle; }

/**
 * Returns the full title of this undo when used for undo.
 */
public String getFullUndoTitle()  { return _undoTitle==null? "Undo" : "Undo " + _undoTitle; }

/**
 * Returns the full title of this undo when used for redo.
 */
public String getFullRedoTitle()  { return _undoTitle==null? "Redo" : "Redo " + _undoTitle; }

/**
 * Returns the number of changes.
 */
public int getChangeCount()  { return _changes.size(); }

/**
 * Returns the individual change at given index.
 */
public PropertyChangeEvent getChange(int anIndex)  { return _changes.get(anIndex); }

/**
 * Returns the selection to be set if undo is executed.
 */
public List <PropertyChangeEvent> getChanges()  { return _changes; }

/**
 * Returns the last change.
 */
public PropertyChangeEvent getChangeLast()  { int cc = getChangeCount(); return cc>0? getChange(cc-1) : null; }

/**
 * Returns the selection to be set if undo is executed.
 */
public Object getUndoSelection()  { return _undoSelection; }

/**
 * Sets the selection to be set if undo is executed.
 */
public void setUndoSelection(Object aSelection)  { _undoSelection = aSelection; }

/**
 * Returns the selection to be set if redo is executed.
 */
public Object getRedoSelection()  { return _redoSelection==null? _undoSelection : _redoSelection; }

/**
 * Sets the selection to be set if redo is executed.
 */
public void setRedoSelection(Object aSelection)  { _redoSelection = aSelection; }

/**
 * Adds a property change.
 */
public void addPropertyChange(PropertyChangeEvent anEvent)
{
    // Iterate over changes and if duplicate exists, coalesce (go backward so we only check last same prop name event)
    for(int i=_changes.size()-1; i>=0; i--) { PropertyChangeEvent e = _changes.get(i);
        if(e.getSource()==anEvent.getSource() && e.getPropertyName().equals(anEvent.getPropertyName())) {
            PropertyChangeEvent event = UndoPropChangeUtils.merge(e, anEvent);
            if(event!=null) {
                anEvent = event;
                _changes.remove(i);
            }
            break;
        }
    }
    
    // If values are equal, just return
    if(SnapUtils.equals(anEvent.getOldValue(), anEvent.getNewValue()))
        return;
    
    // Add change
    _changes.add(anEvent);
}

/**
 * Executes undo for this event.
 */
public void undo()
{
    // Iterate over changes and execute
    for(int i=_changes.size()-1; i>=0; i--) { PropertyChangeEvent pce = _changes.get(i);
        System.out.println("Undoing " + toString(pce, true));
        UndoPropChangeUtils.undoChange(pce);
    }
    
    // If undo selection is set, trigger it
    if(_undoSelection instanceof Undoer.Selection)
        ((Undoer.Selection)_undoSelection).setSelection();
}

/**
 * Executes redo for this event.
 */
public void redo()
{
    // Iterate over changes and execute
    for(PropertyChangeEvent pce : _changes) {
        System.out.println("Redoing " + toString(pce, false));
        UndoPropChangeUtils.redoChange(pce);
    }
    
    // If redo selection is set, trigger it
    if(_redoSelection instanceof Undoer.Selection)
        ((Undoer.Selection)_redoSelection).setSelection();
}

/**
 * Resets this undo event for reuse.
 */
public void reset()
{
    _undoTitle = null;
    _undoSelection = _redoSelection = null;
    _changes.clear();
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer("UndoSet(" + getUndoTitle() + "): ");
    for(PropertyChangeEvent pce : getChanges())
        sb.append(toString(pce, true)).append(", ");
    if(getChangeCount()>0) sb.delete(sb.length()-2, sb.length());
    return sb.toString();
}

/**
 * Returns a string for a property change event.
 */
public String toString(PropertyChangeEvent anEvent, boolean doUndo)
{
    String source = anEvent.getSource().getClass().getSimpleName(), pname = anEvent.getPropertyName(); 
    Object oV = anEvent.getOldValue(); String oS = oV!=null? oV.toString().replace("\n", "\\n") : null;
    Object nV = anEvent.getNewValue(); String nS = nV!=null? nV.toString().replace("\n", "\\n") : null;
    return String.format("%s %s (set %s to %s)", source, pname, doUndo? nS : oS, doUndo? oS : nS);
}

}