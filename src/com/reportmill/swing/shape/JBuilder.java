package com.reportmill.swing.shape;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;

/**
 * A class to build components for JComponentShape.
 */
public class JBuilder {

    // Whether to use real classes if available
    boolean                   _useRealClasses;

    // A map to manage buttongroups for child buttons
    Map <String,ButtonGroup>  _buttonGroups;
    
    // The default builder
    static JBuilder           _default = new JBuilder();

/**
 * Returns the default builder.
 */
public static JBuilder getDefault()  { return _default; }

/**
 * Returns whether to use real classes if available.
 */
public boolean getUseRealClasses()  { return _useRealClasses; }

/**
 * Sets whether to use real classes if available.
 */
public void setUseRealClasses(boolean aValue)  { _useRealClasses = aValue; }

/**
 * Creates a component for a JComponentShape.
 */
public JComponent createComponent(JComponentShape aJCS)
{
    // Get class
    Class <? extends JComponent> clss = aJCS.getComponentClass();
    if(getUseRealClasses() && aJCS.getRealClassName()!=null) { String cname = aJCS.getRealClassName();
        try { clss = (Class)Class.forName(cname); }
        catch(Exception e) { System.err.println("JBuilder: Couldn't find class " + cname); }
    }
    
    // Create instance
    JComponent comp = null; try { comp = clss.newInstance(); } catch(Exception e) { throw new RuntimeException(e); }
    
    // Confgiure and return
    aJCS.configureComponent(this, comp);
    return comp;
}

/**
 * Creates a component with children.
 */
public JComponent createComponentDeep(JComponentShape aJCS)
{
    JComponent comp = createComponent(aJCS);
    aJCS.createComponentDeep(this, comp);
    return comp;
}

/**
 * Return the button group in this table for the given name (creating if needed).
 */
public ButtonGroup getButtonGroup(String aName)
{
    if(_buttonGroups==null) _buttonGroups = new HashMap();
    ButtonGroup bg = _buttonGroups.get(aName); if(bg==null) _buttonGroups.put(aName, bg = new ButtonGroup());
    return bg;
}

}