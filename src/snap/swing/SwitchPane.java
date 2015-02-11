package snap.swing;
import java.awt.*;
import javax.swing.*;

/**
 * This class encapsulates a list of child components which can be switched in and out, like a tab view without the
 * tabs.
 */
public class SwitchPane extends JPanel {
    
    // The selected index
    int      _sindex;

/**
 * Creates a new SwitchPane.
 */
public SwitchPane()  { setLayout(new SPLayout()); }

/**
 * Returns the currently visible pane's index in the panes list.
 */
public int getSelectedIndex()  { return _sindex; }

/**
 * Replaces the currently visible pane with the pane at the given index.
 */
public void setSelectedIndex(int anIndex)
{
    firePropertyChange("SelectedIndex", _sindex, _sindex = anIndex);
    revalidate();
}

/**
 * Returns the currently visible pane.
 */
public JComponent getSelectedPane()
{
    return _sindex>=0 && _sindex<getComponentCount()? (JComponent)getComponent(_sindex) : null;
}

/**
 * Sets the given component as the selected pane.
 */
public void setSelectedPane(JComponent aPane)
{
    for(int i=0, iMax=getComponentCount(); i<iMax; i++)
        if(getComponent(i)==aPane) setSelectedIndex(i);
}

/**
 * Returns the currently visible pane.
 */
public String getSelectedName()  { JComponent c = getSelectedPane(); return c!=null? c.getName() : null; }

/**
 * Replaces the currently visible pane with the pane having the given name (if found).
 */
public void setSelectedName(String aName)  { setSelectedPane(getPane(aName)); }

/**
 * Returns the pane with the given name.
 */
public JComponent getPane(String aName)
{
    for(int i=0, iMax=getComponentCount(); i<iMax; i++) { JComponent c = (JComponent)getComponent(i);
        if(aName.equals(c.getName())) return c; }
    return null; // Return null since pane not found
}


/**
 * A Layout manager.
 */
protected class SPLayout implements LayoutManager {

    /** LayoutManager method. */
    public Dimension preferredLayoutSize(Container aParent)
    { JComponent c = getSelectedPane(); return c!=null? c.getPreferredSize() : null; }
    
    /** LayoutManager method. */
    public Dimension minimumLayoutSize(Container aParent)
    { JComponent c = getSelectedPane(); return c!=null? c.getMinimumSize() : null; }
    
    /** LayoutManager method. */
    public void layoutContainer(Container aParent)
    {
        for(int i=0, iMax=getComponentCount(); i<iMax; i++) { JComponent comp = (JComponent)aParent.getComponent(i);
            if(i==_sindex) comp.setBounds(0, 0, aParent.getWidth(), aParent.getHeight());
            else comp.setBounds(getWidth(), 0, aParent.getWidth(), aParent.getHeight());
        }
    }
    
    /** LayoutManager method. */
    public void addLayoutComponent(String aName, Component aComponent)  { }
    
    /** LayoutManager method. */
    public void removeLayoutComponent(Component aComponent)  { }
}

}