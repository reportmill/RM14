package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape class for JPopupMenu.
 */
public class JPopupMenuShape extends JComponentShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JPopupMenu.class; }

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)  { return new XMLElement("jpopupmenu"); }

}