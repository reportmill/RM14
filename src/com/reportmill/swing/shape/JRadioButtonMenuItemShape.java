package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape for JRadioButtonMenuItem.
 */
public class JRadioButtonMenuItemShape extends JMenuItemShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JRadioButtonMenuItem.class; }

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jradiobuttonmenuitem"); return e;
}

}