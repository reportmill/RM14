package com.reportmill.swing.shape;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import javax.swing.*;
import snap.util.*;

/**
 * A JComponentShape subclass to represent a JScrollPane.
 */
public class JScrollPaneShape extends JComponentShape {

    // The horizontal/vertical scrollbar policy
    int                 _hpolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
    int                 _vpolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Returns the horizontal scrollbar policy.
 */
public int getHorizontalScrollBarPolicy()  { return _hpolicy; }

/**
 * Sets the horizontal scrollbar policy.
 */
public void setHorizontalScrollBarPolicy(int aValue)
{
    firePropertyChange("HorizontalScrollBarPolicy", _hpolicy, _hpolicy = aValue, -1);
}

/**
 * Returns the vertical scrollbar policy.
 */
public int getVerticalScrollBarPolicy()  { return _vpolicy; }

/**
 * Sets the vertical scrollbar policy.
 */
public void setVerticalScrollBarPolicy(int aValue)
{
    firePropertyChange("VerticalScrollBarPolicy", _vpolicy, _vpolicy = aValue, -1);
}

/**
 * Returns the shape that is scrolling.
 */
public JComponentShape getViewportViewShape()  { return getChildWithClass(JComponentShape.class); }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JScrollPane.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get scroll pane and configure
    JScrollPane spane = (JScrollPane)aComp;
    spane.setHorizontalScrollBarPolicy(getHorizontalScrollBarPolicy());
    spane.setVerticalScrollBarPolicy(getVerticalScrollBarPolicy());
}

/**
 * Returns the component deep.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JScrollPane spane = (JScrollPane)aComp;
    JComponentShape jcs = getChildWithClass(JComponentShape.class);
    if(jcs!=null)
        spane.setViewportView(aBldr.createComponentDeep(jcs));
}

/**
 * Override default implementation to deal with viewport view shape.
 */
public void setWidth(double aWidth)
{
    if(aWidth==getWidth()) return;
    super.setWidth(aWidth);
    // Re-layout scroll pane and reset viewport shape bounds
    //getViewportShape().setBounds(getScrollPane().getViewportBorderBounds());
}

/**
 * Override default implementation to deal with viewport view shape.
 */
public void setHeight(double aHeight)
{
    if(aHeight==height()) return;
    super.setHeight(aHeight);
    // Re-layout scroll pane and reset viewport shape bounds
    //getViewportShape().setBounds(getScrollPane().getViewportBorderBounds());
}

/**
 * Override to account for fact that scroll pane clips viewport shape.
 */
public RMRect getBoundsMarkedDeep()  { return getBoundsMarked(); }

/**
 * Overrides default implementation to indicate shape is super selectable.
 */
public boolean superSelectable()  { return true; }

/**
 * Overrides default implementation to indicate children should super select immediately.
 */
public boolean childrenSuperSelectImmediately()  { return true; }

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jscrollpane");
    
    // Archive horizontal policy
    int hpolicy = getHorizontalScrollBarPolicy();
    if(hpolicy==ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) e.add("horizontal", "always");
    else if(hpolicy==ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) e.add("horizontal", "never");
    
    // Archive vertical policy
    int vpolicy = getVerticalScrollBarPolicy();
    if(vpolicy==ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS) e.add("vertical", "always");
    else if(vpolicy==ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER) e.add("vertical", "never");
    
    // Return element
    return e;
}

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i);
        XMLElement cxml = anArchiver.toXML(child, this);
        cxml.removeAttribute("x"); cxml.removeAttribute("y"); cxml.removeAttribute("asize");
        anElement.add(cxml);
    }    
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive Horizontal ScrollBarPolicy
    String horizontal = anElement.getAttributeValue("horizontal", "asneeded");
    if(horizontal.equals("always")) setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    else if(horizontal.equals("never")) setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    
    // Unarchive Vertical ScrollBarPolicy
    String vertical = anElement.getAttributeValue("vertical", "asneeded");
    if(vertical.equals("always")) setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    else if(vertical.equals("never")) setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
}

}