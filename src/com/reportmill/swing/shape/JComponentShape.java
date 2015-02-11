package com.reportmill.swing.shape;
import com.reportmill.graphics.RMColor;
import com.reportmill.shape.*;
import com.reportmill.text.*;
import com.reportmill.viewer.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import snap.swing.*;
import snap.util.*;

/**
 * A shape subclass for JComponent.
 */
public class JComponentShape extends RMParentShape {

    // Whether shape autoscrolls
    Boolean            _ascroll;
    
    // The foreground/background colors
    RMColor            _fg, _bg;
    
    // The font
    RMFont             _font;
    
    // The border
    RMBorder           _border;
    
    // Whether shape is opaque
    Boolean            _opaque;
    
    // The Tooltip text
    String             _ttip;
    
    // Whether shape component is enabled
    boolean            _enabled;
    
    // The action for shape component
    String             _action;
    
    // Whether shape component sends action of FocusLost
    Boolean            _sendActionFocusLost;
    
    // The key used for component shapes that show lists of items
    String             _itemKey;
    
    // Client properties
    Map                _clientProps = new HashMap();
    
    // The real class name, if shape component is really a custom subclass
    String             _realClassName;
    
    // The component
    JComponent         _comp;
    
/**
 * Returns whether shape autoscrolls.
 */
public Boolean getAutoscrolls()  { return _ascroll; }

/**
 * Sets whether shape autoscrolls.
 */
public void setAutoscrolls(Boolean aValue)  { firePropertyChange("Autoscrolls", _ascroll, _ascroll = aValue, -1); }

/**
 * Returns foreground color.
 */
public RMColor getForeground()  { return _fg; }

/**
 * Sets the foreground color.
 */
public void setForeground(RMColor aColor)  { firePropertyChange("Foreground", _fg, _fg = aColor, -1); }

/**
 * Returns background color.
 */
public RMColor getBackground()  { return _bg; }

/**
 * Sets the background color.
 */
public void setBackground(RMColor aColor)  { firePropertyChange("Background", _bg, _bg = aColor, -1); }

/**
 * Returns wether font has been set.
 */
public boolean isFontSet()  { return _font!=null; }

/**
 * Returns the font.
 */
public RMFont getFont()  { return _font!=null? _font : super.getFont(); }

/**
 * Sets the font.
 */
public void setFont(RMFont aFont)  { firePropertyChange("Font", _font, _font = aFont, -1); }

/**
 * Returns the border.
 */
public RMBorder getBorder()  { return _border; }

/**
 * Sets the border.
 */
public void setBorder(RMBorder aBorder)  { firePropertyChange("Border", _border, _border = aBorder, -1); }

/**
 * Returns whether shape is opaque.
 */
public Boolean isOpaque()  { return _opaque; }

/**
 * Sets whether shape is opaque.
 */
public void setOpaque(Boolean aValue)  { firePropertyChange("Opaque", _opaque, _opaque = aValue, -1); }

/**
 * Returns the tool tip text.
 */
public String getToolTipText()  { return _ttip; }

/**
 * Sets the tool tip text.
 */
public void setToolTipText(String aString)  { firePropertyChange("ToolTipText", _ttip, _ttip = aString, -1); }

/**
 * Whether shape is enabled.
 */
public boolean isEnabled()  { return _enabled; }

/**
 * Sets whether shape is enabled.
 */
public void setEnabled(boolean aValue)  { firePropertyChange("Enabled", _enabled, _enabled = aValue, -1); }

/**
 * Returns the action for this shape.
 */
public String getAction()  { return _action; }

/**
 * Sets the action for this shape.
 */
public void setAction(String anAction)  { firePropertyChange("Action", _action, _action = anAction, -1); }

/**
 * Returns whether shape SendActionOnFocusLost.
 */
public Boolean getSendActionOnFocusLost()  { return _sendActionFocusLost; }

/**
 * Sets whether shape Sends action on FocusLost.
 */
public void setSendActionOnFocusLost(Boolean aValue)
{
    firePropertyChange("SendActionOnFocusLost", _sendActionFocusLost, _sendActionFocusLost = aValue, -1);
}

/**
 * Returns the ItemDisplayKey.
 */
public String getItemDisplayKey()  { return _itemKey; }

/**
 * Sets the ItemDisplayKey.
 */
public void setItemDisplayKey(String aKey)  { firePropertyChange("ItemDisplayKey", _itemKey, _itemKey = aKey, -1); }

/**
 * Returns a named client property.
 */
public Object getClientProperty(String aName)  { return _clientProps.get(aName); }

/**
 * Puts a named client property.
 */
public Object putClientProperty(String aName, Object aValue)
{
    Object val = _clientProps.put(aName, aValue);
    firePropertyChange(aName, val, aValue, -1);
    return val;
}

/**
 * Returns the substitution class name.
 */
public String getRealClassName()  { return _realClassName; }

/**
 * Sets the substitution class string.
 */
public void setRealClassName(String aString)
{
    firePropertyChange("RealClassString", _realClassName, _realClassName = aString, -1);
}

/**
 * Add property names for shape.
 */
protected void addPropNames()
{
    java.util.List <String> names = SwingHelper.getSwingHelper(getComponent()).getPropertyNames(getComponent());
    addPropNames(names.toArray(new String[0])); super.addPropNames();
}

/**
 * Override to clear component.
 */
protected void firePropertyChange(PropertyChangeEvent anEvent)
{ super.firePropertyChange(anEvent); firedPropertyChange(anEvent.getPropertyName()); }

/**
 * Override to clear component.
 */
protected void firePropertyChange(String aName, Object oldVal, Object newVal, int anIndex)
{ super.firePropertyChange(aName, oldVal, newVal, anIndex); firedPropertyChange(aName); }

/**
 * Override to clear component.
 */
private void firedPropertyChange(String aName)
{
    if(aName.equals("X") || aName.equals("Y")) return;
    if(_comp!=null && _comp.getParent()!=null) _comp.getParent().remove(_comp);
    _comp = null;
    repaint();
}

/**
 * Overrides shape method to say we want events (to pass on to component).
 */
public boolean acceptsMouse()  { return true; }

/**
 * Overrides shape method to pass events on to component.
 */
public void mousePressed(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, anEvent.getID()); }

/**
 * Overrides shape method to pass events on to component.
 */
public void mouseDragged(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, anEvent.getID()); }

/**
 * Overrides shape method to pass events on to component.
 */
public void mouseReleased(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, anEvent.getID()); }

/**
 * Handles mouse clicked events.
 */
public void mouseClicked(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, anEvent.getID()); }

/**
 * Overrides shape method to pass events on to component.
 */
public void mouseEntered(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, MouseEvent.MOUSE_ENTERED); }

/**
 * Overrides shape method to pass events on to component.
 */
public void mouseMoved(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, anEvent.getID()); }

/**
 * Overrides shape method to pass events on to component.
 */
public void mouseExited(RMShapeMouseEvent anEvent)  { dispatchEventToComponent(anEvent, MouseEvent.MOUSE_EXITED); }

/**
 * Sends given event to shape component.
 */
protected void dispatchEventToComponent(RMShapeMouseEvent anEvent, int anId)
{
    // Get deepest component at point
    Component component = getComponent(); //SwingUtilities.getDeepestComponentAt(getComponent(), anEvent.getX(), anEvent.getY());

    // If starting mouse drag loop, cache mouse pressed component, otherwise if in loop, reset component
    if(anId==MouseEvent.MOUSE_PRESSED) _mousePressComp = component;
    else if(_mousePressComp!=null) component = _mousePressComp;
    
    // Create event for component and dispatch
    Point p2 = anEvent.getPoint();//SwingUtilities.convertPoint(getComponent(), anEvent.getX(), anEvent.getY(), component);
    int mods = anEvent.getModifiers() | anEvent.getModifiersEx(), cc = anEvent.getClickCount();
    MouseEvent event = new MouseEvent(component, anId, anEvent.getWhen(),mods,p2.x,p2.y,cc,anEvent.isPopupTrigger());
    component.dispatchEvent(event);
    
    // Shouldn't have to repaint shape - should come from component I think
    repaint();
    
    // Reset cursor
    anEvent.getViewer().setCursor(component.getCursor());
    
    // Consume original event
    anEvent.consume();
}

// The component hit by mouse pressed event, if in mouse drag loop
Component _mousePressComp;

/**
 * Returns the component.
 */
public JComponent getComponent()  { return _comp!=null? _comp : (_comp=createComponent()); }

/**
 * Creates the component.
 */
protected JComponent createComponent()
{
    JComponent comp = JBuilder.getDefault().createComponent(this);
    comp.setLocation(0,0); return comp;
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JComponent.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Handle basic attributes
    aComp.setName(getName());
    int x = (int)Math.round(getX()), y = (int)Math.round(getY());
    int w = (int)Math.round(getWidth()), h = (int)Math.round(getHeight());
    aComp.setBounds(x, y, w, h);
    if(getAutoscrolls()!=null) aComp.setAutoscrolls(getAutoscrolls());
    if(getForeground()!=null) aComp.setForeground(getForeground().awt());
    if(getBackground()!=null) aComp.setBackground(getBackground().awt());
    if(getFont()!=null) aComp.setFont(getFont().awt());
    if(isOpaque()!=null) aComp.setOpaque(isOpaque());
    if(!isVisible()) aComp.setVisible(false);
    if(getToolTipText()!=null) aComp.setToolTipText(getToolTipText());
    if(!isEnabled()) aComp.setEnabled(false);
    
    // Handle border
    if(getBorder()!=null) { Border border = getBorder().getBorder();
        if(border instanceof TitledBorder) ((TitledBorder)border).setTitleFont(aComp.getFont());
        aComp.setBorder(border);
    }

    // Copy over ClientProperties
    for(Object key : _clientProps.keySet()) aComp.putClientProperty(key, _clientProps.get(key));
    
    // Handle helper attributes
    SwingHelpers.JComponentHpr helper = SwingHelper.getSwingHelper(aComp);
    if(getSendActionOnFocusLost()!=null) helper.setSendActionOnFocusLost(aComp, getSendActionOnFocusLost());
    if(getAction()!=null) helper.setAction(aComp, getAction());
    if(getItemDisplayKey()!=null) helper.setItemDisplayKey(aComp, getItemDisplayKey());
    
    // Add bindings
    for(int i=0, iMax=getBindingCount(); i<iMax; i++) { Binding binding = getBinding(i); 
        helper.addBinding(aComp, binding); }
    
    // Enable events
    if(getEventAdapter(false)!=null && getEventAdapter(true).getEnabledEvents().length>0)
        helper.enableEvents(aComp, getEventAdapter(true).getEnabledEvents());
}

/**
 * Returns the component deep.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    for(int i=getChildCount()-1; i>=0; i--) { JComponentShape child = (JComponentShape)getChild(i);
        JComponent ccomp = aBldr.createComponentDeep(child);
        if(aComp instanceof SpringsPane) aComp.add(ccomp, child.getAutosizing());
        else aComp.add(ccomp);
    }
}

/**
 * Overrides paintShape to paint JComponentShapes with image from component.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Do normal paint shape
    super.paintShape(aPntr);

    // Get component (if not visible, just return)
    JComponent component = getComponent(); if(!component.isVisible()) return;
    
    // Make sure component is child of RMViewer.PhantomComponent
    if(component.getParent()==null) {
        RMShape root = getRootShape();
        RMViewerShape vshape = root instanceof RMViewerShape? (RMViewerShape)root : null; if(vshape==null) return;
        RMViewer vwr = vshape.getViewer();
        vwr.getPhantomPane().add(component);
        vwr.getPhantomPane().revalidate(); vwr.getPhantomPane().repaint(); // JComboBox needs this
        SwingUtilities.invokeLater(new Runnable() { public void run() { repaint(); }}); // ... and this
    }
        
    // Paint component (seems print works better with weird hierarchy)
    component.print(aPntr.getGraphics());
}

/**
 * Override to clear component.
 */
public RMParentShape clone()
{
    JComponentShape clone = (JComponentShape)super.clone();
    clone._comp = null; clone._clientProps = new HashMap(_clientProps);
    return clone;
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("component");
    
    // Archive autoscrolls
    if(getAutoscrolls()!=null) e.add("autoscrolls", getAutoscrolls());
    
    // Archive Foreground, Background
    if(getForeground()!=null) e.add("foreground", '#' + getForeground().toHexString());
    if(getBackground()!=null) e.add("background", '#' + getBackground().toHexString());
    
    // Archive border
    if(getBorder()!=null)
        e.add(anArchiver.toXML(getBorder(), this));
    
    // Archive Opaque, ToolTipText
    if(isOpaque()!=null) e.add("opaque", isOpaque());
    if(getToolTipText()!=null) e.add("ttip", getToolTipText());
    
    // Archive "enabled" flag, (defaults to true)
    if(!isEnabled()) e.add("enabled", false);
    
    // Archive SizeVariant
    if(getClientProperty("JComponent.sizeVariant")!=null)
        e.add("size-variant", getClientProperty("JComponent.sizeVariant"));
    
    // Archive SendActionOnFocusLost
    if(getSendActionOnFocusLost()!=null) e.add("SendActionOnFocusLost", getSendActionOnFocusLost());
    
    // Archive Action
    if(getAction()!=null && getAction().length()>0) e.add("Action", getAction());
    
    // Archive ItemDisplayKey
    if(getItemDisplayKey()!=null) e.add("ItemDisplayKey", getItemDisplayKey());
    
    // Return element
    return e;
}

/**
 * Override to archive RealClassName last.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    super.toXMLChildren(anArchiver, anElement);
    String cname = getRealClassName(); if(cname!=null && cname.length()>0) anElement.add("class", cname);
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal version
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive autoscrolls
    if(anElement.hasAttribute("autoscrolls"))
        setAutoscrolls(anElement.getAttributeBoolValue("autoscrolls"));

    // Unarchive Foreground, Background
    String fg = anElement.getAttributeValue("foreground", anElement.getAttributeValue("color"));
    if(fg!=null) setForeground(RMColor.colorValue(fg));
    String bg = anElement.getAttributeValue("background");
    if(bg!=null) setBackground(RMColor.colorValue(bg));
    
    // Unarchive Border
    XMLElement bxml = anElement.getElement("border");
    if(bxml!=null) { RMBorder border = RMBorder.fromXMLBorder(anArchiver, bxml);
        setBorder(border); }
    
    // Unarchive Opaque, ToolTipText
    if(anElement.hasAttribute("opaque")) setOpaque(anElement.getAttributeBooleanValue("opaque"));
    if(anElement.hasAttribute("ttip")) setToolTipText(anElement.getAttributeValue("ttip"));
    
    // Unarchive Enabled
    setEnabled(anElement.getAttributeBoolValue("enabled", true));
    
    // Unarchive SizeVariant
    if(anElement.hasAttribute("size-variant"))
        putClientProperty("JComponent.sizeVariant", anElement.getAttributeValue("size-variant"));
    
    // Unarchive SendActionOnFocusLost
    if(anElement.hasAttribute("SendActionOnFocusLost"))
        setSendActionOnFocusLost(anElement.getAttributeBoolValue("SendActionOnFocusLost"));
    
    // Unarchive Action
    if(anElement.hasAttribute("Action"))
        setAction(anElement.getAttributeValue("Action"));

    // Unarchive ItemDisplayKey
    if(anElement.hasAttribute("ItemDisplayKey")) setItemDisplayKey(anElement.getAttributeValue("ItemDisplayKey"));
    
    // Unarchive class property for subclass substitution, if available
    if(anElement.hasAttribute("class"))
        setRealClassName(anElement.getAttributeValue("class"));
}

}