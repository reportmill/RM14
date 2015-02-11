package com.reportmill.apptools;
import com.reportmill.app.RMEditorClipboard;
import com.reportmill.shape.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;

/**
 * A tool class for RMParentShape.
 */
public class RMParentShapeTool <T extends RMParentShape> extends RMTool <T> {

/**
 * Override to return shape class.
 */
public Class<T> getShapeClass()  { return (Class<T>)RMParentShape.class; }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Group Shape Inspector"; }

/**
 * Called to handle dropping a string.
 */
public void dropString(T aShape, DropTargetDropEvent anEvent)
{
    Transferable transferable = anEvent.getTransferable();
    getEditor().undoerSetUndoTitle("Drag and Drop Key");
    RMEditorClipboard.paste(getEditor(), transferable, aShape, anEvent.getLocation());    
}

}