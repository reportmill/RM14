package com.reportmill.swing.shape;
import javax.swing.*;

/**
 * An RMShape subclass for JPasswordField.
 */
public class JPasswordFieldShape extends JTextFieldShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JPasswordField.class; }

}