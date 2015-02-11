package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape for JCheckBoxMenuItem.
 */
public class JCheckBoxMenuItemShape extends JMenuItemShape {

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JCheckBoxMenuItem.class; }

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jcheckboxmenuitem"); return e;
}

}