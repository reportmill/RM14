package com.reportmill.swing.shape;
import javax.swing.JComponent;
import snap.swing.SwitchPane;
import snap.util.*;

/**
 * An RMShape subclass for SwitchPane.
 */
public class SwitchPaneShape extends JComponentShape {

    // The selected index
    int        _sindex;

/**
 * Returns the switch pane's selected index.
 */
public int getSelectedIndex()  { return _sindex; }

/**
 * Sets the switch pane's selected index.
 */
public void setSelectedIndex(int anIndex)
{
    firePropertyChange("SelectedIndex", _sindex, _sindex=anIndex, -1);
    relayout();
}

/**
 * Returns the selected name.
 */
public String getSelectedPaneName()
{
    int index = getSelectedIndex();
    return index<0? null : getChild(index).getName();
}

/**
 * Sets the selected pane to the first with the given name.
 */
public void setSelectedPaneName(String aName)
{
    int index = -1;
    for(int i=0, iMax=getChildCount(); i<iMax && index<0; i++)
        if(aName.equals(getChild(i).getName()))
            index = i;
    setSelectedIndex(index);
}

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
public Class <? extends JComponent> getComponentClass()  { return SwitchPane.class; }

/**
 * Override to set SelectedIndex.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JComponent ccomp = aBldr.createComponentDeep(child); aComp.add(ccomp); }
    SwitchPane spane = (SwitchPane)aComp;
    spane.setSelectedIndex(getSelectedIndex());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("switchpane");
    if(getSelectedIndex()>0) e.add("selected-index", getSelectedIndex());
    return e;
}

/**
 * XML unarchival for children.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic attributes
    super.fromXMLShape(anArchiver, anElement);
    setSelectedIndex(anElement.getAttributeIntValue("selected-index", 0));
}

}