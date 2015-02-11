package com.reportmill.swing.shape;
import javax.swing.*;

/**
 * An RMShape subclass for JRadioButton.
 */
public class JRadioButtonShape extends JToggleButtonShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JRadioButton.class; }

}