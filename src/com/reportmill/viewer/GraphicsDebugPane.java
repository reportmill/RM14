package com.reportmill.viewer;
import com.reportmill.base.RMListUtils;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;
import java.util.*;

/**
 * A JComponent subclass that can be installed as the glass pane of another component, 
 * to help show the regions that get repainted.
 */
public class GraphicsDebugPane extends JComponent {

    //
    Rectangle _flashRect;
    
    // The original glass pane
    static Component _originalGlassPane;
    
/**
 * Protected constructor.  To debug a component, call GraphicsDebugPane.installDebugPane() instead
 */
protected GraphicsDebugPane() { }

/**
 * Call this to start debugging a component and all its children.
 */
public static void installDebugPane(JComponent aComponent)
{
    // If the component isn't showing yet, install a hierarchy listener to install the pane once it is showing.
    if(!aComponent.isDisplayable()) {
        final JComponent watchedComponent = aComponent;
        aComponent.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e)
            {
               if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                    Component comp = e.getChanged();
                    if (comp==watchedComponent) {
                        comp.removeHierarchyListener(this);
                        //This better be true or we'll get some infinite recursion
                        assert comp.isDisplayable();
                        GraphicsDebugPane.installDebugPane((JComponent)comp);
                    }
                    watchedComponent.removeHierarchyListener(this);
                }
            }
        });
        return;
    }
    
    // Get original glass pane
    _originalGlassPane = aComponent.getRootPane().getGlassPane();

    // Create the glass pane
    GraphicsDebugPane graphicsDebugPane = new GraphicsDebugPane();
    graphicsDebugPane.setBounds(aComponent.getBounds());
    aComponent.getRootPane().setGlassPane(graphicsDebugPane);
    graphicsDebugPane.setOpaque(false);
    graphicsDebugPane.setVisible(true);
    
    // If repaint manager is already there, just add the new pane to it
    RepaintManager currentManager = RepaintManager.currentManager(aComponent);
    if(!(currentManager instanceof DebugRepaintManager))
        RepaintManager.setCurrentManager(new DebugRepaintManager());
    
    // Add pane
    ((DebugRepaintManager)RepaintManager.currentManager(aComponent)).addPane(graphicsDebugPane);
}

public static void removeDebugPane(JComponent aComponent)
{
    GraphicsDebugPane graphicsDebugPane = (GraphicsDebugPane)aComponent.getRootPane().getGlassPane();
    aComponent.getRootPane().setGlassPane(_originalGlassPane);
    ((DebugRepaintManager)RepaintManager.currentManager(aComponent)).removePane(graphicsDebugPane);
}

public static boolean isDebugPaneInstalled(JComponent aComponent)
{
    return aComponent.getRootPane().getGlassPane() instanceof GraphicsDebugPane;
}

/**
 * Called by the repaintManager with the rect that is about to be redrawn.
 */
public void displayDirty(Rectangle r)
{
    _flashRect = r;
    paintImmediately(r);
}

protected void paintComponent(Graphics g)
{
    // paint the dirtyrect yellow
    if(_flashRect != null) {
        g.setColor(Color.YELLOW);
        g.fillRect(_flashRect.x,_flashRect.y,_flashRect.width,_flashRect.height);
        // reset the dirtyrect so the yellow rect will immediately get erased
        _flashRect = null;
    }

    // Do nothing here so the yellow rect will get erased by the component being watched.
    else { }
}

/**
 * This RepaintManager subclass records the dirty rect and forwards it on to the glasspane
 * before doing the real drawing.
 */
static class DebugRepaintManager extends RepaintManager {

    // The dirty rects (one for each top-level component being debugged)
    List <Rectangle> _dirts =  new ArrayList<Rectangle>(2);
    
    // the glasspanes that will do the debug painting
    List <GraphicsDebugPane> _panes = new ArrayList<GraphicsDebugPane>(2);
    
    
    public DebugRepaintManager() { }
    
    public void addPane(GraphicsDebugPane pane)
    {
        // Just return if already there
        if(RMListUtils.containsId(_panes, pane)) return;
        
        // Add pane
        _panes.add(pane);
        _dirts.add(null); //ArrayLists let you do this
    }
    
    public void removePane(GraphicsDebugPane pane)
    {
        int index = RMListUtils.indexOfId(_panes, pane);
        if(index>=0) {
            _panes.remove(index);
            _dirts.remove(index);
        }
    }
    
    public void addDirtyRegion(JComponent c, int x, int y, final int w, final int h)
    {
        // Record the dirty rect for everything that has the same glasspane as the owner, except for the owner itself
        if ((!(c instanceof GraphicsDebugPane)) && (c.getRootPane()!=null)) {
            int i = _panes.indexOf(c.getRootPane().getGlassPane());
            if (i>=0) {
                GraphicsDebugPane owner = _panes.get(i);
                Rectangle dirt = _dirts.get(i);
                
                // convert from component coords to glasspane coords
                Rectangle r = SwingUtilities.convertRectangle(c, new Rectangle(x,y,w,h), owner);
        
                // this check probably isn't necessary
                if (r.intersects(owner.getBounds())) {
                    if (dirt == null)
                       dirt = r;
                    else
                       dirt = dirt.union(r);
                    _dirts.set(i, dirt);
                }
            }
        }
        super.addDirtyRegion(c, x, y, w, h);
    }
    
    @Override
    public void markCompletelyDirty(JComponent aComponent)
    {
        System.err.println("markCompletelyDirty");
        super.markCompletelyDirty(aComponent);
    }
    
    public void paintDirtyRegions()
    {
        for(int i=0; i<_dirts.size(); ++i) {
            Rectangle r = _dirts.get(i);
            if (r != null) {
                _panes.get(i).displayDirty(r);
                _dirts.set(i,null);
            }
        }
        
        super.paintDirtyRegions();
    }
}

}