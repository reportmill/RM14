package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JPasswordFieldShape;

/**
 * A tool subclass for JPasswordFieldShape.
 */
public class JPasswordFieldTool extends JTextFieldTool {

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JPasswordFieldShape.class; }

/**
 * Returns the string for the inspector window title.
 */
public String getWindowTitle()  { return "Password Field Inspector"; }

}