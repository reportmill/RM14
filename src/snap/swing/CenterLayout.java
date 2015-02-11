package snap.swing;
import java.awt.*;

/**
 * A layout manager to simply center a component.
 */
public class CenterLayout implements LayoutManager2 {

/**
 * layoutContainer
 */
public void layoutContainer(Container aParent)
{
    for(int i=0, iMax=aParent.getComponentCount(); i<iMax; i++)
        layoutChild(aParent, aParent.getComponent(i));
}

/**
 * Layout child.
 */
protected void layoutChild(Container aParent, Component aChild)
{
    Insets pad = aParent.getInsets();
    int pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
    Dimension psize = aChild.getPreferredSize();
    double w = psize.getWidth();
    double h = psize.getHeight();
    double x = pl + (aParent.getWidth() - pl - pr - w)/2;
    double y = pt + (aParent.getHeight() - pt - pb - h)/2;
    aChild.setBounds((int)Math.round(x), (int)Math.round(y), (int)Math.round(w), (int)Math.round(h));
}

/**
 * Returns the content.
 */
public Component getContent(Container aParent) { return aParent.getComponentCount()>0? aParent.getComponent(0) : null; } 

/** LayoutManager method. */
public void addLayoutComponent(String name, Component comp)  { }

/** LayoutManager method. */
public void removeLayoutComponent(Component comp)  { }

/** LayoutManager method. */
public Dimension preferredLayoutSize(Container aParent)
{
    Insets pad = aParent.getInsets(); int pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
    Component c = getContent(aParent);
    Dimension d = c!=null? c.getPreferredSize() : null; if(d==null) d = new Dimension();
    d.width += pl + pr; d.height += pt + pb;
    return d;
}

/** LayoutManager method. */
public void addLayoutComponent(Component comp, Object constraints)  { }

/**
 * minimumLayoutSize
 */
public Dimension minimumLayoutSize(Container aParent)
{
    Insets pad = aParent.getInsets(); int pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
    Component c = getContent(aParent);
    Dimension d = c!=null? c.getMinimumSize() : null; if(d==null) d = new Dimension();
    return new Dimension(d.width + pl + pr, d.height + pt + pb);
}

/** LayoutManager method. */
public Dimension maximumLayoutSize(Container aParent)
{
    Insets pad = aParent.getInsets();
    if(aParent.getComponentCount()==0) return null;
    Dimension d = aParent.getComponent(0).getMaximumSize(); if(d==null) return null;
    return new Dimension(d.width + pad.left + pad.right, d.height + pad.top + pad.bottom);
}

/** LayoutManager method. */
public float getLayoutAlignmentX(Container target)  { return 0; }

/** LayoutManager method. */
public float getLayoutAlignmentY(Container target)  { return 0; }

/** LayoutManager method. */
public void invalidateLayout(Container target)  { }

}