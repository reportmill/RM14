package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JTree.
 */
public class JTreeShape extends JComponentShape {

    // Whether root is visible
    boolean            _rootVisible = true;
    
    // Whether editable
    boolean            _editable;
    
    // Whether to show root handles
    boolean            _showRootHand = true;

/**
 * Returns whether root is visible.
 */
public boolean isRootVisible()  { return _rootVisible; }

/**
 * Sets whether root is visible.
 */
public void setRootVisible(boolean aValue)
{
    firePropertyChange("RootVisible", _rootVisible, _rootVisible = aValue, -1);
}

/**
 * Returns whether editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether editable.
 */
public void setEditable(boolean aValue)
{
    firePropertyChange("Editable", _editable, _editable = aValue, -1);
}

/**
 * Returns whether root shows handles.
 */
public boolean getShowsRootHandles()  { return _showRootHand; }

/**
 * Sets whether root shows handles.
 */
public void setShowsRootHandles(boolean aValue)
{
    firePropertyChange("ShowsRootHandles", _showRootHand, _showRootHand = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JTree.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get JTree and configure
    JTree tree = (JTree)aComp;
    tree.setRootVisible(isRootVisible());
    tree.setEditable(isEditable());
    tree.setShowsRootHandles(getShowsRootHandles());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jtree");

    // Archive RootVisible, Editable, ShowRootHandles
    if(!isRootVisible()) e.add("RootVisible", false);
    if(isEditable()) e.add("editable", true);
    if(!getShowsRootHandles()) e.add("ShowsRootHandles", false);

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);

    // Unarchive RootVisible, Editable, ShowRootHandles
    if(anElement.hasAttribute("RootVisible")) setRootVisible(anElement.getAttributeBooleanValue("RootVisible", true));
    if(anElement.hasAttribute("editable")) setEditable(anElement.getAttributeBooleanValue("editable", false));
    if(anElement.hasAttribute("ShowsRootHandles"))
        setShowsRootHandles(anElement.getAttributeBooleanValue("ShowsRootHandles", true));
}

}