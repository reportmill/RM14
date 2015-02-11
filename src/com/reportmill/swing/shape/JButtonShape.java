package com.reportmill.swing.shape;
import javax.swing.*;

/**
 * An RMShape subclass for JButton.
 */
public class JButtonShape extends AbstractButtonShape {

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JButton.class; }

}