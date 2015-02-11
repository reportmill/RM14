package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JFormattedTextFieldShape;

/**
 * Provides an inspector for JTextFieldShape.
 */
public class JFormattedTextFieldTool extends JTextFieldTool {

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JFormattedTextFieldShape.class; }

/**
 * Returns the string for the inspector window title.
 */
public String getWindowTitle() { return "Formatted Field Inspector"; }

}
