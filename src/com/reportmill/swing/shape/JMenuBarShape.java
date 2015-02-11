package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape for JMenuBar.
 */
public class JMenuBarShape extends JComponentShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JMenuBar.class; }

/**
 * Override to replace empty child menu items with separators.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JMenuBar mbar = (JMenuBar)aComp;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JMenu menu = (JMenu)aBldr.createComponentDeep(child);
        mbar.add(menu);
    }
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jmenubar"); return e;
}

}