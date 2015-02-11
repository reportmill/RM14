package com.reportmill.viewer;
import com.reportmill.swing.shape.JComponentShape;
import javax.swing.*;
import snap.swing.*;
import snap.util.*;

/**
 * A RibsHelper implementation for JComponentShape.
 */
public class RMViewerOwnerJHpr <T extends JComponentShape> extends RMViewerOwnerHpr<T> {

/**
 * Override to forward to component helper.
 */
public void initUI(T aShape, UIOwner anOwner)
{
    super.initUI(aShape, anOwner);
    JComponent comp = aShape.getComponent();
    Swing.getHelper(comp).initUI(comp, anOwner);
}

/**
 * Returns the text property of given object.
 */
public String getText(T aShape)
{
    JComponent comp = aShape.getComponent();
    return Swing.getHelper(comp).getText(comp);
}

/** 
 * Sets the text property of given object to given string.
 */
public void setText(T aShape, String aString)
{
    JComponent comp = aShape.getComponent();
    Swing.getHelper(comp).setText(comp, aString);
}

/**
 * Returns the selected index property of given object.
 */
public int getSelectedIndex(T aShape)
{
    JComponent comp = aShape.getComponent();
    return Swing.getHelper(comp).getSelectedIndex(comp);
}

/**
 * Sets the selected index property of given object to given value.
 */
public void setSelectedIndex(T aShape, int aValue)
{
    JComponent comp = aShape.getComponent();
    Swing.getHelper(comp).setSelectedIndex(comp, aValue);
}

/**
 * Returns the selected object property of given object.
 */
public Object getSelectedItem(T aShape)
{
    JComponent comp = aShape.getComponent();
    return Swing.getHelper(comp).getSelectedItem(comp);
}

/**
 * Sets the selected object property of given object to given value.
 */
public void setSelectedItem(T aShape, Object aValue)
{
    JComponent comp = aShape.getComponent();
    Swing.getHelper(comp).setSelectedItem(comp, aValue);
}

/**
 * Returns whether given event is enabled.
 */
public boolean isEnabled(T aShape, UIEvent.Type aType)
{
    JComponent comp = aShape.getComponent();
    return Swing.getHelper(comp).isEnabled(comp, aType);
}

/**
 * Sets whether given event is enabled.
 */
public void setEnabled(T aShape, UIEvent.Type aType, boolean aValue)
{
    JComponent comp = aShape.getComponent();
    Swing.getHelper(comp).setEnabled(comp, aType, aValue);
}

}