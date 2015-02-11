package com.reportmill.swing.tool;
import com.reportmill.app.RMEditor;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * A Ribs app tool for JSplitPanes.
 */
public class JSplitPaneTool <T extends JSplitPaneShape> extends JComponentTool <T> {

    // For split pane divider dragging during tool mouse loop
    RMPoint      _hitPoint;
    
    // For split pane divider dragging during tool mouse loop
    int          _dividerOrigin;
  
/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return JSplitPaneShape.class; }
  
/**
 * Returns the name to be used for this tool in inspector window title.
 */
public String getWindowTitle()  { return "SplitPane Inspector"; }
 
/**
 * Updates the UI panel controls from the currently selected button shape.
 */
/*public void resetUI()
{
    // Get current split pane shape (and return if null)
    JSplitPaneShape spane = getSelectedShape(); if(spane == null) return;

    // Update VerticalRadioButton, ContinuousCheckBox, ExpandableCheckBox
    setNodeValue("VerticalRadioButton", spane.getOrientation()==JSplitPane.VERTICAL_SPLIT);
    setNodeValue("ContinuousCheckBox", spane.isContinuousLayout());
    setNodeValue("ExpandableCheckBox", spane.isOneTouchExpandable());
}*/

/**
 * Updates the currently selected button shape from the UI panel controls.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get current split pane shape (and return if null)
    JSplitPaneShape spane = getSelectedShape(); if(spane == null) return;
    
    // Handle HorizontalRadioButton, VerticalRadioButton, ContinuousCheckBox, ExpandableCheckBox
    if(anEvent.is("HorizontalRadioButton")) spane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    if(anEvent.is("VerticalRadioButton")) spane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    if(anEvent.is("ContinuousCheckBox")) spane.setContinuousLayout(anEvent.getBooleanValue());
    if(anEvent.is("ExpandableCheckBox")) spane.setOneTouchExpandable(anEvent.getBooleanValue());
}*/

/**
 * Event handling for shape editing.
 */
/*public void mousePressed(T aShape, MouseEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aShape)) return;
    
    // Get the split pane shape
    JSplitPaneShape spane = (JSplitPaneShape)aShape;
    
    // Get event point in split pane shape coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), spane);
    
    // Get hit child
    RMShape hit = spane.getChildContaining(point);

    // If no child was hit, find which divider was hit
    if(hit==null) {
        _hitPoint = point;
        _dividerOrigin = spane.getDividerLocation();
    }
    
    // Otherwise reset hit point
    else _hitPoint = null;
    
    // Consume event
    anEvent.consume();
}*/

/**
 * Event handling for shape editing.
 */
/*public void mouseDragged(T aShape, MouseEvent anEvent)
{
    // Just return if hit point is null
    if(_hitPoint==null) return;
        
    // Get the split pane shape
    JSplitPaneShape splitPaneShape = (JSplitPaneShape)aShape;
    
    // Get event point in split pane shape coords
    RMPoint point = getEditor().convertPointToShape(anEvent.getPoint(), splitPaneShape);
    
    // Handle horizontal split drag
    if(splitPaneShape.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
        int d = (int) (point.x - _hitPoint.x);
        splitPaneShape.setHorizontalDividerLocation(d + _dividerOrigin);
    }
    
    // Handle vertical split drag
    else {
        int d =(int) (point.y - _hitPoint.y);
        splitPaneShape.setVerticalDividerLocation(d + _dividerOrigin);
    }
}*/

/**
 * Group multiple shapes into splitpanes.  Splitpanes have only two children, so this routine recursively groups the
 * list of shapes into a tree of splitpanes. 98% of the time it will probably be called with just 2 shapes, but we 
 * might as well cover all the cases. Also, every shape will go into its own pane.  Selecting 10 random shapes &
 * choosing "group in splitpane" is awfully ambiguous, so you get what you get - quit your bitching.
 */
public static void groupInSplitpanes(RMEditor anEditor) 
{
    // Get selected shapes (just return if empty)
    List <RMShape> shapes = anEditor.getSelectedShapes(); if(shapes.size()==0) return;
    
    // Get parent
    RMParentShape parent = shapes.get(0).getParent();
    
    /*JSplitPane parentSplitPane;
  
    // Grouping just one shape makes the second pane empty. Also, it makes it a vertical splitpane.
    if(shapes.size()==1) {
        SwingPanelShape empty = new SwingPanelShape();
        JComponentShape aShape = (JComponentShape)shapes.get(0);
        RMRect bounds = aShape.getBounds();
        bounds.y += bounds.height;
        empty.setBounds(bounds);
        parentSplitPane = buildSplitpaneWithChildren(aShape.getComponent(), empty.getComponent(),
            JSplitPane.VERTICAL_SPLIT);
        aShape.removeFromParent();
    }
    
    else {
        List componentList = new ArrayList(shapes.size());
        
        // Multiple shapes get grouped recursively. pull out just the components 
        for(int i=0; i<shapes.size(); i++) { RMShape ashape = shapes.get(i);
            if (ashape instanceof JComponentShape) {
                JComponentShape rj = (JComponentShape)ashape;
                rj.removeFromParent();
                componentList.add(rj.getComponent());
            }
        }
      
        parentSplitPane = (JSplitPane)_groupComponentsInSplitpanes(componentList);
    }*/
    
    // validate the whole shebang
    //JSplitPane splitterCopy = (JSplitPane)getComponentPacked(parentSplitPane);
    
    // Create splitpane shape
    JSplitPaneShape splitPaneShape = new JSplitPaneShape();
    // copy attributes
    
    // Add split pane shape
    parent.addChild(splitPaneShape);
    
    // Select shape
    anEditor.setSelectedShape(splitPaneShape);
}

/**
 * Returns a copy of the given component appropriately laid out.
 */
public static JComponent getComponentPacked(JComponent aComponent)
{
    // Get copy of component shape ??? Ribs.setLoadingRibsDocument(true);
    //JComponent componentCopy = new RibsArchiver().copy(aComponent); Ribs.setLoadingRibsDocument(false);
    JComponent componentCopy = aComponent;
    
    // Reset location to zero
    componentCopy.setBounds(0, 0, componentCopy.getWidth(), componentCopy.getHeight());
    
    // Create offscreen window
    JWindow win = new JWindow();
    win.setLocation(9999,9999);
    win.setSize(componentCopy.getWidth(), componentCopy.getHeight());
    
    // Add component pane copy
    win.getContentPane().add(componentCopy);
    
    // Make window visible and then dispose
    win.setVisible(true);
    win.dispose();
    
    // Return component copy
    return componentCopy;
}

/**
 * Recursive body of above method.
 */
/*private static JComponent _groupComponentsInSplitpanes(List <JComponent> theComponents)
{
    int numComponents = theComponents.size();
    int orientation;

    if(numComponents==1)
        return theComponents.get(0);

    // The end of the road for the recursion. One shape for each pane.
    if(numComponents==2) {
        
        // Decide whether the split should be as a column or a row. We do this by calculating the slope of the line 
        // joining the midpoints of the two shapes and looking at which octent it falls in.
        JComponent s1 = theComponents.get(0), s2 = theComponents.get(1);
        Rectangle r1 = s1.getBounds(), r2 = s2.getBounds();
        float dy = (r1.y + r1.height/2f) - (r2.y + r2.height/2f);
        float dx = (r1.x + r1.width/2f) - (r2.x + r2.width/2f);
        float m = dx==0? 1000 : Math.abs(dy/dx);
        
        // If a row (+-45 degrees from the vertical axis) make a column
        if(m>=1) {
            orientation = JSplitPane.VERTICAL_SPLIT;
            if(r1.y>r2.y) { JComponent swap = s1; s1 = s2; s2 = swap; }
        }
        
        // If it ain't a column, it's a row
        else {
            orientation = JSplitPane.HORIZONTAL_SPLIT;
            if(r2.x<r1.x) { JComponent swap = s1; s1 = s2; s2 = swap; } // Make sure first comp is left-most
        }

        // Build split pane
        return buildSplitpaneWithChildren(s1, s2, orientation);
    }

    // More than 2 shapes - split them up and recurse
    // Basic strategy is to sort the shapes with a quicksort-like algorithm that picks a pivot point and splits shapes
    // into those on either side of pivot and then recurses on those subarrays, putting result of each recursion into
    // one or the other pane. The main tricks are deciding whether the split should be horizontal or vertical,
    // and where the pivot point should be. To do this, we use the OrderedRangeList class.
    // Essentially, what the OrderedRangeList is doing is projecting every shape rectangle onto either the x or y axis,
    // unioning the projections, and then sorting. What this gives you is a list of x (or y) ranges in which
    // it is guaranteed that if you shot a ray orthogonal to the axis, you would eventually hit a shape.
    // If, however, you picked two ranges in a row and shot a ray in between the two, you would never hit a shape.
    // Also you are guaranteed that there are shapes on either side of this ray.
    // This point is, therefore, an excellent pivot point.
    // The recursive algorithm that creates the panes is an O(N logN) algorithm, but the choosing of the pivot point
    // using the OrderedRangeList is also O(N logN), so the whole thing winds up being something like O(N log^2N)
    List left = new ArrayList(numComponents / 2);
    List right = new ArrayList(numComponents / 2);
    List above = left, below = right; // aliases, just for clarity

    OrderedRangeList ranges = createComponentRanges(theComponents, false);
    if(ranges.getRangeCount() > 1) {
        float pivoty = ranges.getFreeValue();
        for(int i=0; i<numComponents; ++i) { JComponent aComponent = theComponents.get(i);
            if(aComponent.getY() < pivoty)
                above.add(aComponent);
            else below.add(aComponent);
        }
        orientation = JSplitPane.VERTICAL_SPLIT;
    }
    else {
        ranges = createComponentRanges(theComponents, true);
        if(ranges.getRangeCount() > 1) {
            orientation = JSplitPane.HORIZONTAL_SPLIT;
            float pivotx = ranges.getFreeValue();
            for(int i=0; i<numComponents; ++i) { JComponent aComponent = theComponents.get(i);
                if (aComponent.getX() < pivotx)
                    left.add(aComponent);
                else right.add(aComponent);
            }
        }
        
        // More than 2 components, but they overlap. Just pick one at random to be the pivot
        else {
            orientation = JSplitPane.VERTICAL_SPLIT;
            JComponent aComponent = theComponents.get(0);
            float pivoty = aComponent.getY() + aComponent.getHeight()/2f;
            above.add(aComponent);
            for(int i=1; i<numComponents; ++i) { aComponent = theComponents.get(i);
                if(aComponent.getY() + aComponent.getHeight() / 2f <= pivoty)
                    above.add(aComponent);
                else below.add(aComponent);
            }
            
            // Degenerate case - all lined up along the center and overlapping
            if(below.size() == 0)
                below.add(above.remove(above.size() - 1));
        }
    }
    
    // Recurse
    JComponent cleft = _groupComponentsInSplitpanes(left);
    JComponent cright = _groupComponentsInSplitpanes(right);
    return buildSplitpaneWithChildren(cleft, cright, orientation);
}*/

/**
 * 
 */
public static JSplitPane buildSplitpaneWithChildren(JComponent c1, JComponent c2, int splitways)
{
    // We assume they both come from the same parent. Note that there exists a bug whereby selectedShapes
    // can contain shapes with different parents in them. (shift-click can get you in trouble) That one should be fixed.
    int barlocation;
     
    // Origin of new split pane will be old origin of first component. The size will try to be big enough for everyone.
    Rectangle pbounds = c1.getBounds();
    Rectangle otherbounds = c2.getBounds();
    if(splitways==JSplitPane.HORIZONTAL_SPLIT) {
        if(otherbounds.height > pbounds.height)
            pbounds.height = otherbounds.height;
        barlocation = pbounds.width;
        pbounds.width += otherbounds.width;
    }
    else {
        if(otherbounds.width > pbounds.width)
            pbounds.width = otherbounds.width;
        barlocation = pbounds.height;
        pbounds.height += otherbounds.height;
    }
    
    // Probably unnecessary 
    c1.setLocation(0,0);
    c2.setLocation(0,0);
    
    // Make the splitpane (with continuous layout turned on)
    JSplitPane pane = new JSplitPane(splitways, true, c1, c2);
    pane.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
    
    // Adjust the bbox to include the border & the divider
    
    int bar = pane.getDividerSize();
    if(splitways==JSplitPane.HORIZONTAL_SPLIT)
        pbounds.width += bar;
    else pbounds.height += bar;
    if(pane.getInsets()!=null) { Insets in = pane.getInsets();
        pbounds.x -= in.left; pbounds.y -= in.top;
        pbounds.width += in.left+in.right; pbounds.height += in.top + in.bottom;
    }
    pane.setBounds(pbounds);
    pane.setDividerLocation(barlocation); // set the divider to show all of each pane
    
    // Nothing is laid out at this point.  The top level will do it when everything's done.
    return pane;
}

/**
 * Draw some sort of indicator for super-selected splitpanes.
 */
public void paintShapeHandles(T aShape, Graphics2D g, boolean isSuperSelected)
{
    // Do normal tool paintShapeHandles
    super.paintShapeHandles(aShape, g, isSuperSelected);
    
    // If not super-selected, just return
    if(!isSuperSelected) return;
    
    // Get bounds in editor coords
    RMRect bounds = getEditor().convertRectFromShape(aShape.getBoundsInside(), aShape);

    // Turn off antialiasing, draw blue bounds rect, restore antialiasing
    Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setColor(Color.blue); g.draw(bounds);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
}

/**
 * Given a list of Components, create a sorted list of ranges which represents occupied regions along given axis.
 */
/*private static OrderedRangeList createComponentRanges(List <JComponent> theComponents, boolean doHorizontal)
{
    // Create new list
    List ranges = new ArrayList();
    if(doHorizontal)
        for(int i=0, iMax=theComponents.size(); i<iMax; i++) { JComponent component = theComponents.get(i);
            ranges.add(new FloatRange(component.getX(), component.getWidth())); }
    else for(int i=0, iMax=theComponents.size(); i<iMax; i++) { JComponent component = theComponents.get(i);
        ranges.add(new FloatRange(component.getY(), component.getHeight())); }
    
    // Return ordered range list
    return new OrderedRangeList(ranges);
}*/

/**
 * This class is a utility class used to creating split panes. It represents a sorted, non-contiguous list of ranges.
 */
public static class OrderedRangeList {

    // The list of ranges
    List <FloatRange>   _ranges;

    /**
     * Create OrderedRangeList from a list of float ranges.
     */
    private OrderedRangeList(List theRanges)
    {
        // Set ranges
        _ranges = theRanges;
        
        // Sort ranges
        Collections.sort(_ranges);
        
        // Coalesce ranges
        for(int i=_ranges.size()-1; i>=1; i--) {
            FloatRange r1 = _ranges.get(i-1), r2 = _ranges.get(i);
            if(r1.end()>=r2.start) {
                r1.length = r2.end() - r1.start; _ranges.remove(i); }
        }
    }
    
    /** Returns the total number of non-overlapping ranges */
    public int getRangeCount()  { return _ranges.size(); }
    
    /** Returns a value within an unoccupied region */
    public float getFreeValue()
    {
        int n = getRangeCount();
        if(n>1) {
            n/=2;
            return (_ranges.get(n-1).end() + _ranges.get(n).start)/2;
        }
        
        // the infinite-length region to left of first range or right of last are certainly unoccupied, but this 
        // method assumes you want something in the middle, and so raises exception if there are no gaps in ranges
        throw new ArrayIndexOutOfBoundsException("no free ranges");
    }
}

/**
 * A simple float-based range class.
 */
private static class FloatRange implements Cloneable, Comparable<FloatRange> {
    public float start, length;
    public FloatRange(float s, float l) { super(); start = s; length = l; }
    public float end() { return start+length; }
    public Object clone() { return new FloatRange(start, length); }
    public String toString() { return "{"+start+" -> "+end()+"}"; }
    public int compareTo(FloatRange o)  { return start<o.start? -1 : start>o.start? 1 : 0; }
}

}