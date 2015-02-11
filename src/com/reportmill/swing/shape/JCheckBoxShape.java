package com.reportmill.swing.shape;
import javax.swing.*;

/**
 * An RMShape subclass for JCheckBox.
 */
public class JCheckBoxShape extends JToggleButtonShape {

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JCheckBox.class; }

}