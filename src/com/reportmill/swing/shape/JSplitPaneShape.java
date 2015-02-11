package com.reportmill.swing.shape;
import com.reportmill.shape.*;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JSplitPane.
 */
public class JSplitPaneShape extends JComponentShape {

    // Whether split pane is continous
    boolean            _continuous;
    
    // Whether split pane is one touch expandable
    boolean            _oneTouch;
    
    // The divider size
    int                _dividerSize;
    
    // The divider location
    int                _dividerLoc;
    
    // The resize weight
    double             _resizeWeight;

    // The orientation
    int                _orient = JSplitPane.HORIZONTAL_SPLIT;
    
    // Whether split pane is borderless
    boolean            _borderless;

/**
 * Returns whether split pane is continuous.
 */
public boolean isContinuousLayout()  { return _continuous; }

/**
 * Sets whether split pane is continuous.
 */
public void setContinuousLayout(boolean aValue)
{
    firePropertyChange("ContinousLayout", _continuous, _continuous = aValue, -1);
}

/**
 * Returns whether split pane is one touch expandable.
 */
public boolean isOneTouchExpandable()  { return _oneTouch; }

/**
 * Sets whether split pane is one touch expandable.
 */
public void setOneTouchExpandable(boolean aValue)
{
    firePropertyChange("OneTouchExpandable", _oneTouch, _oneTouch = aValue, -1);
}

/**
 * Returns split pane divider size.
 */
public int getDividerSize()  { return _dividerSize; }

/**
 * Sets split pane divider size.
 */
public void setDividerSize(int aValue)
{
    firePropertyChange("DividerSize", _dividerSize, _dividerSize = aValue, -1);
}

/**
 * Returns split pane divider location.
 */
public int getDividerLocation()  { return _dividerLoc; }

/**
 * Sets split pane divider location.
 */
public void setDividerLocation(int aValue)
{
    firePropertyChange("DividerLocation", _dividerLoc, _dividerLoc = aValue, -1);
}

/**
 * Returns split pane resize weight.
 */
public double getResizeWeight()  { return _resizeWeight; }

/**
 * Sets split pane resize weight.
 */
public void setResizeWeight(double aValue)
{
    firePropertyChange("ResizeWeight", _resizeWeight, _resizeWeight = aValue, -1);
}

/**
 * Returns one of the JSplitPane constants HORIZONTAL_SPLIT or VERTICAL_SPLIT.
 */
public int getOrientation()  { return _orient; }

/**
 * Sets one of the JSplitPane constants HORIZONTAL_SPLIT or VERTICAL_SPLIT.
 */
public void setOrientation(int aValue)
{
    firePropertyChange("Orientation", _orient, _orient = aValue, -1);
}

/**
 * Returns whether split pane has no border.
 */
public boolean isBorderless()  { return _borderless; }

/**
 * Sets whether split pane has no border.
 */
public void setBorderless(boolean aValue)
{
    firePropertyChange("Borderless", _borderless, _borderless = aValue, -1);
}

/**
 * Returns the shape on this split pane shape's left side.
 */
public RMShape getLeftShape()  { return getChildCount()>0? getChild(0) : null; }

/**
 * Sets the shape on this split pane shape's left side.
 */
public void setLeftShape(RMShape aShape)
{
    if(getChildCount()>0) removeChild(0);
    addChild(aShape, 0);
}

/**
 * Returns the shape on this split pane shape's right side.
 */
public RMShape getRightShape()  { return getChildCount()>1? getChild(1) : null; }

/**
 * Sets the shape on this split pane shape's right side.
 */
public void setRightShape(RMShape aShape)
{
    if(getChildCount()>1) removeChild(1);
    if(getChildCount()==0) addChild(new JLabelShape());
    addChild(aShape, 1);
}

/**
 * Returns the shape on this split pane shape's top side.
 */
public RMShape getTopShape()  { return getLeftShape(); }

/**
 * Sets the shape on this split pane shape's top side.
 */
public void setTopShape(RMShape aShape)  { setLeftShape(aShape); }

/**
 * Returns the shape on this split pane shape's bottom side.
 */
public RMShape getBottomShape()  { return getRightShape(); }

/**
 * Sets the shape on this split pane shape's bottom side.
 */
public void setBottomShape(RMShape aShape)  { setRightShape(aShape); }

/**
 * Set the dividerLocation of a horizontal splitpane and resize children.
 */
/*public void setHorizontalDividerLocation(int loc)
{
    // Get the splitpane, insets, divider size
    JSplitPane splitter = getSplitPane();
    Insets insets = splitter.getInsets();
    float dividerSize = splitter.getDividerSize();
    
    // Get splitpane left and right insets
    float left = insets==null? 0 : insets.left;
    float right = insets==null? 0 : insets.right;
    
    // Calculate width of splitpane components
    double availableWidth = getWidth() - left - right - dividerSize;

    // Clamp location to real values
    if(loc<0) loc = (int)availableWidth/2;
    if(loc>availableWidth) loc = (int)availableWidth;

    // Pass the new location to the component
    splitter.setDividerLocation(loc);
    
    // Resize left shape (deep)
    RMShape child = getLeftShape(); if(child != null) child.setWidth(loc);

    // Skip to the divider
    loc += left;

    // Move right shape and resize
    child = getRightShape();
    if(child != null) { child.setX(loc+dividerSize); child.setWidth(availableWidth - loc); }
}*/

/**
 * Set the dividerLocation of a horizontal splitpane and resize children.
 */
/*public void setVerticalDividerLocation(int loc)
{
    // Get splitpane, insets, DividerSize
    JSplitPane splitter = getSplitPane();
    Insets insets = splitter.getInsets();
    float dividerSize = splitter.getDividerSize();
    
    // Get splitpane top and right insets
    float top = insets==null? 0 : insets.top;
    float bottom = insets==null? 0 : insets.bottom;
    
    // Calculate height of splitpane components
    double availableHeight = getHeight() - top - bottom - dividerSize;

    // Clamp location to real values
    if(loc<0) loc = (int)availableHeight/2;
    if(loc>availableHeight) loc = (int)availableHeight;

    // Pass the new location to the component
    splitter.setDividerLocation(loc);
    
    // Resize top shape
    RMShape child = getTopShape(); if(child != null) child.setHeight(loc);

    // Skip to the divider
    loc += top;

    // Move right shape and resize
    child = getBottomShape();
    if(child != null) { child.setY(loc+dividerSize); child.setHeight(availableHeight - loc); }
}*/

/** Overrides default implementation to indicate that this shape can be super selected. */
public boolean superSelectable() { return true; }

/** Overrides default implementation to indicate children should super select immediately. */
public boolean childrenSuperSelectImmediately() { return true; }

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JSplitPane.class; }

/**
 * Override to add tabs for app rendering.
 */
protected JComponent createComponent()
{
    // Add tabs for app rendering
    final JSplitPane spane = (JSplitPane)super.createComponent();
    for(int i=0, iMax=getChildCount(); i<iMax; i++)
        if(i==0) spane.setLeftComponent(new JLabel()); else spane.setRightComponent(new JLabel());
    spane.validate();
    
    // Reset child bounds (after delay)
    SwingUtilities.invokeLater(new Runnable() { public void run() {
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
            child.setBounds(i==0? spane.getLeftComponent().getBounds() : spane.getRightComponent().getBounds()); }
    }});
    
    // Return tabpane
    return spane;
}

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get JSplitPane and configure
    JSplitPane spane = (JSplitPane)aComp;
    spane.setContinuousLayout(isContinuousLayout());
    if(getDividerSize()>0) spane.setDividerSize(getDividerSize());
    if(getDividerLocation()>0) spane.setDividerLocation(getDividerLocation());
    spane.setResizeWeight(getResizeWeight());
    spane.setOneTouchExpandable(isOneTouchExpandable());
    spane.setOrientation(getOrientation());
    if(isBorderless()) spane.setBorder(null);
}

/**
 * Returns the component deep.
 */
public void createComponentDeep(JBuilder aBldr, JComponent aComp)
{
    JSplitPane spane = (JSplitPane)aComp;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { JComponentShape child = (JComponentShape)getChild(i);
        JComponent ccomp = aBldr.createComponentDeep(child);
        if(i==0) spane.setLeftComponent(ccomp);
        else spane.setRightComponent(ccomp);
    }
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jsplitpane");
    
    // Archive orientation if vertical (horizontal is the default)
    if(getOrientation()==JSplitPane.VERTICAL_SPLIT) e.add("orientation", "vertical");
    
    // Archive ContinuousLayout
    if(!isContinuousLayout()) e.add("continuous-layout", false);
    
    // Archive DividerSize, DividerLocation
    if(getDividerSize()>0) e.add("DividerSize", getDividerLocation());
    if(getDividerLocation()>0) e.add("divider-location", getDividerLocation());
    
    // Archive ResizeWeight
    if(getResizeWeight()!=0) e.add("resize-weight", (float)getResizeWeight());
    
    // Archive OneTouchExpandable, Borderless
    if(isOneTouchExpandable()) e.add("one-touch-expandable", true);
    if(isBorderless()) e.add("borderless", true);

    // Return the XML element
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
    
    // Unarchive Orientation
    String orientation = anElement.getAttributeValue("orientation", "horizontal");
    setOrientation(orientation.startsWith("v")? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
    
    // Unarchive ContinuousLayout
    setContinuousLayout(anElement.getAttributeBoolValue("continuous-layout", true));
    
    // Unarchive DividerSize, DividerLocation
    if(anElement.hasAttribute("DividerSize"))
        setDividerSize(anElement.getAttributeIntValue("DividerSize"));
    if(anElement.hasAttribute("divider-location"))
        setDividerLocation(anElement.getAttributeIntValue("divider-location"));

    // Unarchive ResizeWeight
    if(anElement.hasAttribute("resize-weight"))
        setResizeWeight(anElement.getAttributeFloatValue("resize-weight"));
    
    // Unarchive OneTouchExpandable, Borderless
    if(anElement.hasAttribute("one-touch-expandable"))
        setOneTouchExpandable(anElement.getAttributeBoolValue("one-touch-expandable"));
    if(anElement.hasAttribute("borderless"))
        setBorderless(anElement.getAttributeBooleanValue("borderless"));
}

}