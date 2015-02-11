package com.reportmill.swing.shape;
import com.reportmill.shape.*;
import java.util.*;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JTabbedPane.
 */
public class JTabbedPaneShape extends JComponentShape {

    // A list of tab names
    List <String>     _tabNames = new ArrayList();
    
    // The selected index
    int               _selIndex;

/**
 * Returns the number of tabs in pane.
 */
public int getTabCount()  { return getChildCount(); }

/**
 * Adds the given tab shape to tabbed pane shape.
 */
public void addTab(String aTitle, RMShape aTabShape)
{
    _tabNames.add(aTitle);
    addChild(aTabShape);
}

/**
 * Returns the title at given index.
 */
public String getTitleAt(int anIndex)  { return _tabNames.get(anIndex); }

/**
 * Returns the tap pane's selected index.
 */
public int getSelectedIndex()  { return _selIndex; }

/**
 * Sets the tab pane's selected index.
 */
public void setSelectedIndex(int anIndex)
{
    firePropertyChange("SelectedIndex", _selIndex, _selIndex = anIndex, -1);
}

/**
 * Returns the selected child.
 */
public RMShape getSelectedChild()  { return _selIndex<getChildCount()? getChild(_selIndex) : null; }

/**
 * Editor method - overrides default implementation to indicate shape is super selectable.
 */
public boolean superSelectable()  { return true; }

/**
 * Editor method - overrides default implementation to indicate children should super select immediately.
 */
public boolean childrenSuperSelectImmediately()  { return true; }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JTabbedPane.class; }

/**
 * Override to add tabs for app rendering.
 */
protected JComponent createComponent()
{
    // Add tabs for app rendering
    final JTabbedPane tpane = (JTabbedPane)super.createComponent();
    for(int i=0, iMax=getChildCount(); i<iMax; i++) tpane.addTab(getTitleAt(i), new JLabel());
    tpane.validate();
    
    // Reset child bounds (after delay)
    SwingUtilities.invokeLater(new Runnable() { public void run() {
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
            child.setBounds(tpane.getComponent(i).getBounds());
            if(child!=getSelectedChild()) child.setX(getWidth());
        }
    }});
    
    // Return tabpane
    return tpane;
}

/**
 * Returns the component deep.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JTabbedPane tpane = (JTabbedPane)aComp;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JComponent ccomp = aBldr.createComponentDeep(child);
        tpane.addTab(getTitleAt(i), ccomp);
    }
    if(getSelectedIndex()<tpane.getTabCount()) tpane.setSelectedIndex(getSelectedIndex());
}

/**
 * Override to make children paint in tabpane.
 */
public void paintShapeChildren(RMShapePainter aPntr)
{
    RMShape child = getSelectedChild();
    if(child!=null) child.paint(aPntr);
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jtabbedpane");
    e.add("selected-index", getSelectedIndex()); // Archive the index of the currently selected tab
    return e;
}

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i); String title = getTitleAt(i);
        XMLElement cxml = anArchiver.toXML(child, this); cxml.add("title", title);
        cxml.removeAttribute("x"); cxml.removeAttribute("y"); cxml.removeAttribute("asize");
        anElement.add(cxml);
    }    
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLShape(anArchiver, anElement);
    if(anElement.hasAttribute("selected-index")) setSelectedIndex(anElement.getAttributeIntValue("selected-index"));
}

/**
 * XML unarchival for children. Only panels do anything here so far.
 */
public void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive shapes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement cxml = anElement.get(i);
        
        // Get child class - if RMShape, unarchive and add
        Class childClass = anArchiver.getClass(cxml.getName());
        if(childClass!=null && JComponentShape.class.isAssignableFrom(childClass)) {
            JComponentShape shape = (JComponentShape)anArchiver.fromXML(cxml, this);
            String title = cxml.getAttributeValue("title"); if(title==null) title = "";
            addTab(title, shape);
        }
    }
}

}