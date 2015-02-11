package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape for JMenu.
 */
public class JMenuShape extends JMenuItemShape {

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JMenu.class; }

/**
 * Override to replace empty child menu items with separators.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JMenu menu = (JMenu)aComp;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JMenuItem mi = (JMenuItem)aBldr.createComponentDeep(child);
        if((mi.getText()==null || mi.getText().length()==0) && mi.getIcon()==null) menu.addSeparator();
        else menu.add(mi);
    }
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jmenu"); return e;
}

}