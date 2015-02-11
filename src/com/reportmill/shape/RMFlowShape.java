package com.reportmill.shape;
import java.awt.*;
import java.beans.*;
import snap.util.*;

/**
 * A shape to layout children with flow.
 */
public class RMFlowShape extends RMParentShape {

    // The Snap UI layout that does the real child layout work
    SnapFlowLayout           _snapLayout = new SnapFlowLayout();

/**
 * Returns the horizontal spacing between children.
 */
public double getSpacingX()  { return _snapLayout.getSpacingX(); }

/**
 * Sets the horizontal spacing between children.
 */
public void setSpacingX(double aSpacing)  { _snapLayout.setSpacingX(aSpacing); }

/**
 * Returns the vertical spacing between children.
 */
public double getSpacingY()  { return _snapLayout.getSpacingY(); }

/**
 * Sets the vertical spacing between children.
 */
public void setSpacingY(double aSpacing)  { _snapLayout.setSpacingY(aSpacing); }

/**
 * Returns whether to wrap children to bounds of parent.
 */
public boolean getWraps()  { return _snapLayout.getWraps(); }

/**
 * Sets whether to wrap children to bounds of parent.
 */
public void setWraps(boolean aValue)  { _snapLayout.setWraps(aValue); }

/**
 * Override to send to SnapLayout.
 */
protected double computePrefWidth(double aHeight)  { return _snapLayout.getPrefWidth(aHeight); }

/**
 * Override to send to SnapLayout.
 */
protected double computePrefHeight(double aWidth)  { return _snapLayout.getPrefHeight(aWidth); }

/**
 * Override to send to SnapLayout.
 */
protected void layoutChildren()  { _inLayout = true; _snapLayout.layoutChildren(this); _inLayout = false; }
boolean _inLayout;

/**
 * Override to set child MinWidth/MinHeight.
 */
public void addLayoutChild(RMShape aChild)
{
    if(!aChild.isMinWidthSet()) aChild.setMinWidth(aChild.getWidth());
    if(!aChild.isMinHeightSet()) aChild.setMinHeight(aChild.getHeight());
}

/**
 * Catch child size changes (when not doing layout) to set minimum size.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Do normal version (just return if performing child layout)
    super.propertyChange(anEvent); if(_inLayout) return;
    
    // If change in Width/Height/LayoutInfo, request layout and repaint
    String pname = anEvent.getPropertyName();
    if(pname=="Width" || pname=="Height" || pname=="LayoutInfo") { RMShape child = (RMShape)anEvent.getSource();
        if(pname.equals("Width")) child.setMinWidth(child.getWidth());
        if(pname.equals("Height")) child.setMinHeight(child.getHeight());
        relayout(); repaint();
    }
}

/**
 * Override to paint dashed box around bounds.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Do normal version
    super.paintShape(aPntr);
    
    // Paint dashed box around bounds
    if(aPntr.isEditing() && getStroke()==null && getFill()==null && getEffect()==null) {
        aPntr.setColor(Color.lightGray); aPntr.setStroke(new BasicStroke(1f, 0, 0, 1, new float[] { 3, 2 }, 0));
        aPntr.setAntialiasing(false); aPntr.draw(getBoundsInside()); aPntr.setAntialiasing(true);
    }
}

/**
 * XML Archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("flow-shape");
    
    // Archive SpacingX and SpacingY
    if(getSpacingX()!=5) e.add("SpacingX", getSpacingX());
    if(getSpacingY()!=5) e.add("SpacingY", getSpacingY());
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal layout unarchival
    super.fromXMLShape(anArchiver, anElement);
    
    // Legacy: 
    if(anElement.getElement("layout")!=null) anElement = anElement.getElement("layout");
    
    // Unarchive SpacingX and Spacing Y
    if(anElement.hasAttribute("SpacingX")) setSpacingX(anElement.getAttributeIntValue("SpacingX"));
    if(anElement.hasAttribute("SpacingY")) setSpacingY(anElement.getAttributeIntValue("SpacingY"));
}

/**
 * Standard clone implementation.
 */
public RMParentShape clone()
{
    RMFlowShape clone = (RMFlowShape)super.clone();
    clone._snapLayout = (SnapFlowLayout)_snapLayout.clone();
    return clone;
}

/**
 * A LayoutAdapter implementation for RMShape.
 */
private static class SnapFlowLayout extends snap.util.FlowLayout <RMShape> {

    /** Returns the parent width/height. */
    public double getParentWidth(RMShape aParent)  { return aParent.getWidth(); }
    public double getParentHeight(RMShape aParent)  { return aParent.getHeight(); }

    /** Returns the child count and child at index for given parent. */
    public int getChildCount(RMShape aParent)  { return ((RMParentShape)aParent).getChildCount(); }
    public RMShape getChild(RMShape aParent, int anIndex)  { return ((RMParentShape)aParent).getChild(anIndex); }
    
    /** Returns child x, y, width, height. */
    public double getX(RMShape aChild)  { return aChild.getFrameX(); }
    public double getY(RMShape aChild)  { return aChild.getFrameY(); }
    public double getWidth(RMShape aChild)  { return aChild.getFrameWidth(); }
    public double getHeight(RMShape aChild)  { return aChild.getFrameHeight(); }

    /** Set child bounds. */
    public void setBounds(RMShape aCh, double aX, double aY, double aW, double aH) { aCh.setFrame(aX, aY, aW, aH); }

    /** Returns child preferred width/height. */
    public double getPrefWidth(RMShape aChild, double aValue)  { return aChild.getPrefWidth(aValue); }
    public double getPrefHeight(RMShape aChild, double aValue)  { return aChild.getPrefHeight(aValue); }
    
    /** Returns the margin of the layout. */
    public snap.util.SPInsets getInsets(RMShape aParent)  { return new snap.util.SPInsets(0,0,0,0); }

    /** Returns/sets the layout info for a shape. */
    public Object getLayoutInfo(RMShape aChild)  { return aChild.getLayoutInfo(); }
    public void setLayoutInfo(RMShape aChild, Object aLI)  { aChild.setLayoutInfo(aLI); }
}

}