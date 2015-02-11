package com.reportmill.apptools;
import com.reportmill.shape.*;
import java.awt.event.*;
import snap.swing.SwingEvent;

/**
 * Tool for visual editing RMScene3D.
 */
public class RMScene3DTool <T extends RMScene3D> extends RMTool <T> {
    
    // The scene3d control for rotating selected scene3d
    RMTrackballControl  _sceneControl;
    
/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get trackball control
    _sceneControl = getNode("TrackballControl", RMTrackballControl.class);
}

/**
 * Updates UI panel from currently selected scene3d.
 */
public void resetUI()
{
    // Get the selected scene
    RMScene3D scene = getSelectedShape(); if(scene==null) return;
    
    // Reset Rendering radio buttons
    setNodeSelectedIndex("RenderingComboBox", scene.isPseudo3D()? 1 : 0);
    
    // Reset YawSpinner, PitchSpinner, RollSpinner
    setNodeValue("YawSpinner", Math.round(scene.getYaw()));
    setNodeValue("PitchSpinner", Math.round(scene.getPitch()));
    setNodeValue("RollSpinner", Math.round(scene.getRoll3D()));
    
    // Reset scene control
    _sceneControl.syncFrom(scene);
    
    // Reset Depth slider/text
    setNodeValue("DepthSlider", scene.getDepth());
    setNodeValue("DepthText", scene.getDepth());
    
    // Reset Field of view slider/text
    setNodeValue("FOVSlider", scene.getFocalLength()/72);
    setNodeValue("FOVText", scene.getFocalLength()/72);
}

/**
 * Updates currently selected scene 3d from UI panel controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected scene3d
    RMScene3D scene = getSelectedShape(); if(scene==null) return;
    
    // Handle RenderingComboBox
    if(anEvent.equals("RenderingComboBox")) {
        scene.setPseudo3D(anEvent.getSelectedIndex()==1);
        scene.setDefaultViewSettings();
    }
    
    // Handle YawSpinner, PitchSpinner, RollSpinner
    if(anEvent.equals("YawSpinner"))
        scene.setYaw(anEvent.getFloatValue());
    if(anEvent.equals("PitchSpinner"))
        scene.setPitch(anEvent.getFloatValue());
    if(anEvent.equals("RollSpinner"))
        scene.setRoll3D(anEvent.getFloatValue());

    // Handle Scene3DControl
    if(anEvent.equals("TrackballControl"))
        _sceneControl.syncTo(scene);
    
    // Handle DepthSlider and DepthText
    if(anEvent.equals("DepthSlider") || anEvent.equals("DepthText"))
        scene.setDepth(anEvent.getFloatValue());

    // Handle FOVSlider or FOVText
    if(anEvent.equals("FOVSlider") || anEvent.equals("FOVText"))
        scene.setFocalLength(anEvent.getFloatValue()*72);
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RMScene3D.class; }

/**
 * Returns the name of this tool for the inspector window.
 */
public String getWindowTitle()  { return "Scene3D Inspector"; }

/**
 * Overridden to make scene3d super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return true; }

/**
 * Overridden to make scene3d not ungroupable.
 */
public boolean isUngroupable(RMShape aShape)  { return false; }

/**
 * Event handler for editing.
 */    
public void mousePressed(T aScene3D, MouseEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aScene3D)) return;
    
    // Forward mouse pressed to scene and consume event
    aScene3D.mousePressed(new RMShapeMouseEvent(aScene3D, anEvent));
    anEvent.consume();
}

/**
 * Event handler for editing.
 */
public void mouseDragged(T aScene3D, MouseEvent anEvent)
{
    // Forward mouse pressed to scene and consume event
    aScene3D.mouseDragged(new RMShapeMouseEvent(aScene3D, anEvent));
    anEvent.consume();
}

/**
 * Event handler for editing.
 */
public void mouseReleased(T aScene3D, MouseEvent anEvent)
{
    // Forward mouse pressed to scene and consume event
    aScene3D.mouseReleased(new RMShapeMouseEvent(aScene3D, anEvent));
    anEvent.consume();
}

}