package snap.swing;
import java.awt.*;
import javax.swing.*;

/**
 * This class is the root of all Rib files.
 */
public class SpringsPane extends JPanel {

    // The panel's window menu bar
    JMenuBar                  _menuBar;

/**
 * Creates a new SpringsPane.
 */
public SpringsPane()  { setLayout(new SnapSpringLayout(this)); setRequestFocusEnabled(false); }

/**
 * Overrides JComponent method to propagate to all children (which is a quick way to disable an entire panel).
 */
public void setEnabled(boolean aFlag)
{
    super.setEnabled(aFlag);
    for(int i=0, iMax=getComponentCount(); i<iMax; i++)
        getComponent(i).setEnabled(aFlag);
}

/**
 * Returns the menu bar associated with this window.
 */
public JMenuBar getWindowMenuBar()  { return _menuBar; }

/**
 * Sets the menu bar associated with this window.
 */
public void setWindowMenuBar(JMenuBar aMenuBar)  { _menuBar = aMenuBar; }

/**
 * The SnapSpringLayout.
 */
private static class SnapSpringLayout extends snap.util.SpringLayout <Component> implements LayoutManager2 {

    /** Creates SnapSpringLayout. */
    SnapSpringLayout(Component aParent)  { super(aParent); }
    
    /** Returns parent width/height. */
    public double getParentWidth(Component aParent)  { return aParent.getWidth(); }
    public double getParentHeight(Component aParent)  { return aParent.getHeight(); }

    /** Returns the child count and child at index given parent. */
    public int getChildCount(Component aParent)  { return ((Container)aParent).getComponentCount(); }
    public Component getChild(Component aParent, int anIndex)  { return ((Container)aParent).getComponent(anIndex); }
    
    /** Returns child x, y, width, height. */
    public double getX(Component aChild)  { return aChild.getX(); }
    public double getY(Component aChild)  { return aChild.getY(); }
    public double getWidth(Component aChild)  { return aChild.getWidth(); }
    public double getHeight(Component aChild)  { return aChild.getHeight(); }
    
    /** Set child bounds. */
    public void setBounds(Component aChild, double anX, double aY, double aWidth, double aHeight)
    {
        // Get rounded components
        int x = (int)Math.round(anX), y = (int)Math.round(aY);
        int w = (int)Math.round(anX+aWidth) - x, h = (int)Math.round(aY+aHeight) - y;
        
        // Constrain new width and height to min size
        if(aChild.isMinimumSizeSet()) {
            Dimension min = aChild.getMinimumSize();
            if(w<min.width) w = min.width;
            if(h<min.height) h = min.height;
        }
    
        // Constrain new width and height to max size
        if(aChild.isMaximumSizeSet()) {
            Dimension max = aChild.getMaximumSize();
            if(w>max.width) w = max.width;
            if(h>max.height) h = max.height;
        }
        
        // Set bounds
        aChild.setBounds(x, y, w, h);
    }
    
    /** Returns the margin of the layout. */
    public snap.util.SPInsets getInsets(Component aParent)
    { Insets i = ((Container)aParent).getInsets(); return new snap.util.SPInsets(i.top, i.left, i.bottom, i.right); }
    
    /** Returns child minimum width/height. */
    public double getMinWidth(Component aChild, double aValue)  { return aChild.getMinimumSize().width; }
    public double getMinHeight(Component aChild, double aValue)  { return aChild.getMinimumSize().height; }
    
    /** Returns child preferred width/height. */
    public double getPrefWidth(Component aChild, double aValue)  { return aChild.getPreferredSize().width; }
    public double getPrefHeight(Component aChild, double aValue)  { return aChild.getPreferredSize().height; }
    
    /** Returns/sets the layout info (descriptor) for a shape. */
    public Object getLayoutInfo(Component c)  { return ((JComponent)c).getClientProperty("Constraints"); }
    public void setLayoutInfo(Component c, Object aLI)  { ((JComponent)c).putClientProperty("Constraints", aLI); }
    
    /** Returns/sets the layout info for a shape. */
    public Object getLayoutInfoX(Component c)  { return ((JComponent)c).getClientProperty("ConstraintsX"); }
    public void setLayoutInfoX(Component c, Object aLI)  { ((JComponent)c).putClientProperty("ConstraintsX", aLI); }

    /** AWT LayoutManager methods. */
    public void addLayoutComponent(String aName, Component aComponent)  { }
    public void addLayoutComponent(Component aChild, Object theConstr) { addChild(aChild, theConstr); }
    public void removeLayoutComponent(Component aChild)  { removeChild(aChild); }
    public void layoutContainer(Container aParent)  { layoutChildren(aParent); }
    public Dimension maximumLayoutSize(Container aTrgt)  { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
    public float getLayoutAlignmentX(Container aTarget)  { return 0; }
    public float getLayoutAlignmentY(Container aTarget)  { return 0; }
    public void invalidateLayout(Container aTarget)  { }
    public Dimension preferredLayoutSize(Container aParent)
    { int w = (int)Math.round(getPrefWidth(-1)), h = (int)Math.round(getPrefHeight(-1)); return new Dimension(w,h); }
    public Dimension minimumLayoutSize(Container aParent)
    { int w = (int)Math.round(getMinWidth(-1)), h = (int)Math.round(getMinHeight(-1)); return new Dimension(w,h); }
}

}