package com.reportmill.apptools;
import com.reportmill.shape.*;
import javax.swing.JLabel;

public class RMLabelTool <T extends RMLabel> extends RMTool <T> {

public Class getShapeClass()  { return RMLabel.class; }

public String getWindowTitle()  { return "Label Inspector"; }

public JLabel createUI()  { return new JLabel(); }

/**
 * Overrides tool method to declare that labels have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}