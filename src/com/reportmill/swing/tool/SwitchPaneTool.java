package com.reportmill.swing.tool;
import com.reportmill.swing.shape.SwitchPaneShape;

/**
 * A class to act as switch pane's inspector.
 */
public class SwitchPaneTool extends JComponentTool {
    
/**
 * Returns the shape class this tool is responsible for.
 */
public Class getShapeClass()  { return SwitchPaneShape.class; }

/**
 * Returns the inspector window title string.
 */
public String getWindowTitle()  { return "SwitchPane Inspector"; }

}