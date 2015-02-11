package com.reportmill.app;
import com.reportmill.shape.*;
import java.util.List;
import snap.swing.*;

/**
 * This class provides UI editing for shapes with FlowLayout.
 */
public class FlowLayoutPane extends SwingOwner {

/**
 * ResetUI.
 */
public void resetUI()
{
    // Get layout info
    RMEditor editor = RMEditor.getMainEditor();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    Object li = shape.getLayoutInfo();
    String lis = li instanceof String? (String)li : "";
    
    // Set UI
    setNodeValue("NewlineCheckBox", lis.equals("N"));
}

/**
 * RespondUI.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected editor and selected shapes
    RMEditor editor = RMEditor.getMainEditor();
    List <? extends RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Handle NewlineCheckBox
    if(anEvent.equals("NewlineCheckBox"))
         for(RMShape shape : shapes) shape.setLayoutInfo(anEvent.getBooleanValue()? "N" : null);
}

}