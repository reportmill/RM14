package com.reportmill.swing.shape;
import javax.swing.*;

/**
 * An RMShape subclass for JToggleButton.
 */
public class JToggleButtonShape extends AbstractButtonShape {

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JToggleButton.class; }

}