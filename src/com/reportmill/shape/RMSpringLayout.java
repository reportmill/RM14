package com.reportmill.shape;
import java.beans.*;

/**
 * Handles code for laying out shape children with springs and struts.
 */
public class RMSpringLayout extends RMShapeLayout implements PropertyChangeListener {

    // The SnapLayout UI layout that does the real work
    SnapSpringLayout       _snapLayout = new SnapSpringLayout(null);
    
    // Whether we are in layout
    boolean                _inLayout;

/** Override to send to SnapLayout. */
public void setParent(RMParentShape aParent)  { super.setParent(aParent); _snapLayout.setParent(aParent); }

/** Override to send to SnapLayout. */
protected void layoutChildren()  { _inLayout = true; _snapLayout.layoutChildren(getParent()); _inLayout = false; }

/** Override to send to SnapLayout. */
protected double computePrefWidth(double aHeight)  { return _snapLayout.getPrefWidth(aHeight); }
protected double computePrefHeight(double aWidth)  { return _snapLayout.getPrefHeight(aWidth); }

/** Standard clone implementation. */
public RMSpringLayout clone()
{
    RMSpringLayout clone = (RMSpringLayout)super.clone();
    clone._snapLayout = (SnapSpringLayout)_snapLayout.clone();
    return clone;
}

/**
 * Override to start listening to property changes.
 */
public void addLayoutChild(RMShape aChild)
{
    aChild.addPropertyChangeListener(this); // Start listening to shape property changes
    _snapLayout.addChild(aChild, null);
}

/**
 * Override to stop listening to property changes.
 */
public void removeLayoutChild(RMShape aChild)
{
    aChild.removePropertyChangeListener(this);
    _snapLayout.removeChild(aChild);
}

/**
 * Update LayoutInfo for all children. 
 */
public void reset()  { _snapLayout.reset(); getParent().setNeedsLayout(false); }

/**
 * Called to revalidate when shape bounds change.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // If InLayout (we caused property change), just return
    if(_inLayout) return;

    // Get property name - if frame changer, do something
    String pname = anEvent.getPropertyName();
    if(pname=="X" || pname=="Y" || pname=="Width" || pname=="Height" || pname=="Roll" || pname=="LayoutInfo") {
        RMShape child = (RMShape)anEvent.getSource();
        _snapLayout.updateChild(child);
    }
}

/**
 * A LayoutAdapter implementation for RMShape.
 */
private static class SnapSpringLayout extends snap.util.SpringLayout <RMShape> {

    /** Creates SnapSpringLayout. */
    SnapSpringLayout(RMShape aParent)  { super(aParent); }
    
    /** Returns the parent width/height. */
    public double getParentWidth(RMShape aParent)  { return aParent.getWidth(); }
    public double getParentHeight(RMShape aParent)  { return aParent.getHeight(); }

    /** Returns the child count and child at index for given parent. */
    public int getChildCount(RMShape aParent)  { return ((RMParentShape)aParent).getChildCount(); }
    public RMShape getChild(RMShape aParent, int anIndex)  { return ((RMParentShape)aParent).getChild(anIndex); }
    
    /** Returns the margin of the layout. */
    public snap.util.SPInsets getInsets(RMShape aParent)  { return new snap.util.SPInsets(0,0,0,0); }

    /** Returns child x, y, width, height. */
    public double getX(RMShape aChild)  { return aChild.getFrameX(); }
    public double getY(RMShape aChild)  { return aChild.getFrameY(); }
    public double getWidth(RMShape aChild)  { return aChild.getFrameWidth(); }
    public double getHeight(RMShape aChild)  { return aChild.getFrameHeight(); }

    /** Set child bounds. */
    public void setBounds(RMShape aCh, double aX, double aY, double aW, double aH) { aCh.setFrame(aX, aY, aW, aH); }

    /** Returns child minimum width/height. */
    public double getMinWidth(RMShape aChild, double aValue)  { return aChild.getMinWidth(aValue); }
    public double getMinHeight(RMShape aChild, double aValue)  { return aChild.getMinHeight(aValue); }

    /** Returns child preferred width/height. */
    public double getPrefWidth(RMShape aChild, double aValue)  { return aChild.getPrefWidth(aValue); }
    public double getPrefHeight(RMShape aChild, double aValue)  { return aChild.getPrefHeight(aValue); }
    
    /** Returns/sets the layout info for a shape. */
    public Object getLayoutInfo(RMShape aChild)  { return aChild.getLayoutInfo(); }
    public void setLayoutInfo(RMShape aChild, Object aLI)  { aChild.setLayoutInfo(aLI); }

    /** Returns/sets the layout info for a shape. */
    public Object getLayoutInfoX(RMShape aChild)  { return aChild._layoutInfoX; }
    public void setLayoutInfoX(RMShape aChild, Object aLI)  { aChild._layoutInfoX = aLI; }
}

}