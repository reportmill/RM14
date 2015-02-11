package com.reportmill.swing.shape;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import snap.swing.MenuButton;
import snap.util.*;

/**
 * RMShape subclass for MenuButton.
 */
public class MenuButtonShape extends JLabelShape {

    // Whether button has border
    boolean          _showBorder = true;

    // The popup point
    Point            _popPoint;
    
    // The popup size
    Dimension        _popSize;

/**
 * Returns whether button shows border.
 */
public boolean getShowBorder()  { return _showBorder; }

/**
 * Sets whether button shows border.
 */
public void setShowBorder(boolean aValue)  { firePropertyChange("ShowBorder", _showBorder, _showBorder = aValue, -1); }

/**
 * Returns the popup point.
 */
public Point getPopupPoint()  { return _popPoint; }

/**
 * Sets the popup point.
 */
public void setPopupPoint(Point aValue)  { firePropertyChange("PopupPoint", _popPoint, _popPoint = aValue, -1); }

/**
 * Returns the popup size.
 */
public Dimension getPopupSize()  { return _popSize; }

/**
 * Sets the popup size.
 */
public void setPopupSize(Dimension aValue)  { firePropertyChange("PopupSize", _popSize, _popSize = aValue, -1); }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return MenuButton.class; }

/**
 * Override to fix popup point in RMShape space.
 */
protected JComponent createComponent()
{
    MenuButton mb = (MenuButton)super.createComponent();
    Point p = mb.getPopupPoint(); if(p==null) p = new Point();
    p.x += (int)getX(); p.y += (int)getY(); mb.setPopupPoint(p);
    return mb;
}

/**
 * Override to apply subclass attributes.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get MenuButton and configure
    MenuButton mb = (MenuButton)aComp;
    if(!getShowBorder()) mb.setBorder(null);
    if(getPopupPoint()!=null) mb.setPopupPoint(getPopupPoint());
    if(getPopupSize()!=null) mb.setPopupSize(getPopupSize());
}

/**
 * Override to add popup menu items
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    MenuButton mb = (MenuButton)aComp; JPopupMenu pmenu = mb.getPopupMenu();
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JMenuItem mi = (JMenuItem)aBldr.createComponentDeep(child);
        if((mi.getText()==null || mi.getText().length()==0) && mi.getIcon()==null) pmenu.addSeparator();
        else pmenu.add(mi);
    }
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("menubutton");
    
    // Archive ShowBorder, PopupPoint, PopupSize
    if(!getShowBorder()) e.add("border", false);
    if(getPopupPoint()!=null) { e.add("popup-x", getPopupPoint().x); e.add("popup-y", getPopupPoint().y); }
    if(getPopupSize()!=null) {
        e.add("popup-width", getPopupSize().width); e.add("popup-height", getPopupSize().height); }
        
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
    
    // Unarchive ShowBorder
    if(anElement.hasAttribute("border")) setShowBorder(anElement.getAttributeBooleanValue("border"));
    
    // Unarchive PopupPoint
    if(anElement.hasAttribute("popup-x") || anElement.hasAttribute("popup-y")) {
        int x = anElement.getAttributeIntValue("popup-x");
        int y = anElement.getAttributeIntValue("popup-y");
        setPopupPoint(new Point(x, y));
    }
    
    // Unarchive PopupSize
    if(anElement.hasAttribute("popup-width") || anElement.hasAttribute("popup-height")) {
        int w = anElement.getAttributeIntValue("popup-width");
        int h = anElement.getAttributeIntValue("popup-height");
        setPopupSize(new Dimension(w, h));
    }
}

}