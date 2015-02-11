package snap.swing;
import com.reportmill.base.RMKeyChain;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.*;
import snap.util.*;

/**
 * A class to hold SwingHelpers for standard Swing components.
 */
public class SwingHelpers {

/**
 * A Helper class for Object.
 */
public static class ObjectHpr <T extends Object> extends SwingHelper <T> {

    /** UIHelper method. */
    public String getName(T anObj)  { return ""; }
    
    /** UIHelper method. */
    public UIOwner getOwner(T anObj)  { return null; }
    
    /** UIHelper method. */
    public void setOwner(T anObj, UIOwner anOwner)  { }
    
    /** UIHelper method. */
    public Object getParent(T anObj)  { return null; }
    
    /** UIHelper method. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** UIHelper method. */
    public Object getChild(T anObj, int anIndex)  { return null; }
    
    /** UIHelper method. */
    public boolean isEnabled(T anObj, UIEvent.Type aType)  { return false; }
    
    /** UIHelper method. */
    public void setEnabled(T anObj, UIEvent.Type aType, boolean aValue)  { }
}

/**
 * This class is used to provide Snap UI functionality to Swing JComponents.
 */
public static class JComponentHpr <T extends JComponent> extends SwingHelper <T> {
    
    /** Returns the name of the given component. */
    public String getName(T anObj)  { return anObj.getName(); }
    
    /** Initializes newly created object. */
    public void initUI(T anObj, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(anObj, anOwner);
        
        // If SendActionOnFocusLost, set SendActionOnFocusLostEnabled
        if(getSendActionOnFocusLost(anObj))
            getEventAdapter(anObj, true).setInputVerifier(true);
    }
    
    /** Returns the parent object for given object. */
    public Object getParent(T anObj)  { return anObj.getParent(); }
    
    /** Returns the number of children for given object. */
    public int getChildCount(T aComp)  { return aComp.getComponentCount(); }
    
    /** Returns the individual child object for given object and index. */
    public Object getChild(T aComp, int anIndex)  { return aComp.getComponent(anIndex); }
    
    /** Returns given component's owner. */
    public UIOwner getOwner(T anObj)  { return (UIOwner)anObj.getClientProperty("RibsOwner"); }
    
    /** Sets given component's owner. */
    public void setOwner(T anObj, UIOwner anOwner)  { anObj.putClientProperty("RibsOwner", anOwner); }
    
    /** Returns the constraints for component. */
    public Object getConstraints(T aComponent)  { return aComponent.getClientProperty("Constraints"); }
    
    /** Sets the constraints for component. */
    public void setConstraints(T aComponent, Object theConstraints)
    {
        aComponent.putClientProperty("Constraints", theConstraints);
    }
    
    /** Returns the autosizing for the given component. */
    public String getAutosizing(T aComponent)
    {
        String asize = (String)getConstraints(aComponent);
        return asize!=null? asize : "--~,--~";
    }
    
    /** Returns a change listener suitable for some controls. */
    protected ChangeListener getChangeListener()  { return _changeListener; }
    
    /** A private shared ChangeListener implementation for controls to send action on change. */
    static ChangeListener _changeListener = new ChangeListener() { public void stateChanged(ChangeEvent e) {
        Swing.sendEvent(e);
    }};
    
    /** Returns whether this component should send action when focus lost. */
    public boolean getSendActionOnFocusLost(T aComponent)
    {
        Object value = aComponent.getClientProperty("SendActionOnFocusLost");
        if(value==null) value = getSendActionOnFocusLostDefault(aComponent);
        return value==Boolean.TRUE;
    }
    
    /** Sets whether this component should sends action when it loses focus. */
    public void setSendActionOnFocusLost(T aComponent, Boolean aValue)
    {
        aComponent.putClientProperty("SendActionOnFocusLost", aValue);
    }
    
    /** Returns whether given component defaults to send action when it loses focus. */
    public boolean getSendActionOnFocusLostDefault(T aComponent)  { return aComponent instanceof JTextComponent; }
    
    /** Returns the number of bindings associated with given component. */
    public int getBindingCount(T anObj)  { List list = getBindings(anObj, false); return list!=null? list.size() : 0; }
    
    /** Returns the individual binding at the given index for given component. */
    public Binding getBinding(T anObj, int anIndex)  { return getBindings(anObj, true).get(anIndex); }
    
    /** Returns the list of RibsBindings, with option to create, if missing. */
    public List <Binding> getBindings(T anObj, boolean doCreate)
    {
        // Get bindings list
        List <Binding> bindings = (List)anObj.getClientProperty("RibsBindings");
        if(bindings==null && doCreate)
            anObj.putClientProperty("RibsBindings", bindings = new ArrayList());
        
        // Return bindings list
        return bindings;
    }
    
    /** Adds the individual binding at the given index to given component. */
    public void addBinding(T anObj, Binding aBinding)
    {
        // Remove current binding for property (if it exists)
        removeBinding(anObj, aBinding.getPropertyName());
        
        // Get bindings list  and add binding
        List <Binding> bindings = getBindings(anObj, true);
        bindings.add(aBinding);
        
        // Set binding component and helper
        aBinding.setNode(anObj);
        aBinding.setHelper(this);
    }
    
    /** Removes the binding at the given index from given component. */
    public Binding removeBinding(T anObj, int anIndex)  { return getBindings(anObj, true).remove(anIndex); }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Enabled", "Visible"); super.addPropNames(); }
    
    /** Returns whether given event is enabled. */
    public boolean isEnabled(T anObj, UIEvent.Type aType)  { return getEventAdapter(anObj, true).isEnabled(aType); }
    
    /** Sets whether given event is enabled. */
    public void setEnabled(T anObj, UIEvent.Type aType, boolean aValue)
    {
        getEventAdapter(anObj, true).setEnabled(aType, aValue);
    }
    
    /** Returns the SwingEventAdapter for component. */
    public SwingEventAdapter getEventAdapter(T aComponent, boolean doCreate)
    {
        SwingEventAdapter sea = (SwingEventAdapter)aComponent.getClientProperty("SwingEventAdapter");
        if(sea==null && doCreate)
            aComponent.putClientProperty("SwingEventAdapter", sea = createEventAdapter(aComponent));
        return sea;
    }
    
    /** Creates the SwingEventAdapter for component. */
    protected SwingEventAdapter createEventAdapter(T aComponent)  { return new SwingEventAdapter(aComponent); }
}

/**
 * This class is the superclass for labels and buttons which handles attributes common to both classes.
 */
public static abstract class LabeledHpr <T extends JComponent> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Returns the button or label icon. */
    public abstract Icon getIcon(T aLabeled);
    
    /** Sets the button or label icon. */
    public abstract void setIcon(T aLabeled, Icon anIcon);
    
    /** Standard button or label property. */
    public abstract int getHorizontalAlignment(T aLabeled);
    
    /** Standard button or label property. */
    public abstract void setHorizontalAlignment(T aLabeled, int aValue);
    
    /** Standard button or label property. */
    public abstract int getVerticalAlignment(T aLabeled);
    
    /** Standard button or label property. */
    public abstract void setVerticalAlignment(T aLabeled, int aValue);
    
    /** Standard button or label property. */
    public abstract int getHorizontalTextPosition(T aLabeled);
    
    /** Standard button or label property. */
    public abstract void setHorizontalTextPosition(T aLabeled, int aValue);
    
    /** Standard button or label property. */
    public abstract int getVerticalTextPosition(T aLabeled);
    
    /** Standard button or label property. */
    public abstract void setVerticalTextPosition(T aLabeled, int aValue);
    
    /** Standard button or label property. */
    public abstract int getIconTextGap(T aLabeled);
    
    /** Standard button or label property. */
    public abstract void setIconTextGap(T aLabeled, int aValue);
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Text"); super.addPropNames(); }
}

/**
 * This helper implementation for JLabel.
 */
public static class JLabelHpr <T extends JLabel> extends LabeledHpr <T> {
    
    /** Returns the button or label icon. */
    public Icon getIcon(T aLabeled)  { return aLabeled.getIcon(); }
    
    /** Sets the button or label icon. */
    public void setIcon(T aLabeled, Icon anIcon)  { aLabeled.setIcon(anIcon); }
    
    /** Standard button or label property. */
    public int getHorizontalAlignment(T aLabeled)  { return aLabeled.getHorizontalAlignment(); }
    
    /** Standard button or label property. */
    public void setHorizontalAlignment(T aLabeled, int aValue)  { aLabeled.setHorizontalAlignment(aValue); }
    
    /** Standard button or label property. */
    public int getVerticalAlignment(T aLabeled)  { return aLabeled.getVerticalAlignment(); }
    
    /** Standard button or label property. */
    public void setVerticalAlignment(T aLabeled, int aValue)  { aLabeled.setVerticalAlignment(aValue); }
    
    /** Standard button or label property. */
    public int getHorizontalTextPosition(T aLabeled)  { return aLabeled.getHorizontalTextPosition(); }
    
    /** Standard button or label property. */
    public void setHorizontalTextPosition(T aLabeled, int aValue)  { aLabeled.setHorizontalTextPosition(aValue); }
    
    /** Standard button or label property. */
    public int getVerticalTextPosition(T aLabeled)  { return aLabeled.getVerticalTextPosition(); }
    
    /** Standard button or label property. */
    public void setVerticalTextPosition(T aLabeled, int aValue)  { aLabeled.setVerticalTextPosition(aValue); }
    
    /** Standard button or label property. */
    public int getIconTextGap(T aLabeled)  { return aLabeled.getIconTextGap(); }
    
    /** Standard button or label property. */
    public void setIconTextGap(T aLabeled, int aValue)  { aLabeled.setIconTextGap(aValue); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return "Text"; // Remap Value to Text
        return super.getPropertyNameMapped(anObj, aName);
    }
}

/**
 * JComponentHpr subclass for AbstractButtons.
 */
public static class AbstractButtonHpr <T extends AbstractButton> extends LabeledHpr<T> {
    
    /** Initializes newly created object. */
    public void initUI(T aButton, UIOwner anOwner)
    {
        super.initUI(aButton, anOwner);  // Do normal init
        getEventAdapter(aButton, true).setEnabled(UIEvent.Type.Action, true);  // Enable action listener
        aButton.setRequestFocusEnabled(false);  // Turn off request focus enabled, so mouse clicks don't grab focus
    }
    
    /** Returns the button or label icon. */
    public Icon getIcon(T aLabeled)  { return aLabeled.getIcon(); }
    
    /** Sets the button or label icon.     */
    public void setIcon(T aLabeled, Icon anIcon)  { aLabeled.setIcon(anIcon); }
    
    /** Standard button or label property. */
    public int getHorizontalAlignment(T aLabeled)  { return aLabeled.getHorizontalAlignment(); }
    
    /** Standard button or label property. */
    public void setHorizontalAlignment(T aLabeled, int aValue)  { aLabeled.setHorizontalAlignment(aValue); }
    
    /** Standard button or label property. */
    public int getVerticalAlignment(T aLabeled)  { return aLabeled.getVerticalAlignment(); }
    
    /** Standard button or label property. */
    public void setVerticalAlignment(T aLabeled, int aValue)  { aLabeled.setVerticalAlignment(aValue); }
    
    /** Standard button or label property. */
    public int getHorizontalTextPosition(T aLabeled)  { return aLabeled.getHorizontalTextPosition(); }
    
    /** Standard button or label property. */
    public void setHorizontalTextPosition(T aLabeled, int aValue)  { aLabeled.setHorizontalTextPosition(aValue); }
    
    /** Standard button or label property. */
    public int getVerticalTextPosition(T aLabeled)  { return aLabeled.getVerticalTextPosition(); }
    
    /** Standard button or label property. */
    public void setVerticalTextPosition(T aLabeled, int aValue)  { aLabeled.setVerticalTextPosition(aValue); }
    
    /** Standard button or label property. */
    public int getIconTextGap(T aLabeled)  { return aLabeled.getIconTextGap(); }
    
    /** Standard button or label property. */
    public void setIconTextGap(T aLabeled, int aValue)  { aLabeled.setIconTextGap(aValue); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aPropertyName)
    {
        if(aPropertyName.equals("Value")) return "Selected";
        return super.getPropertyNameMapped(anObj, aPropertyName);
    }
    
    /** Returns the text property of given object. */
    public String getText(T aButton)
    {
        // If button icon is bogus RMIconUtils.LabelIcon, return text for it
        if(aButton.getIcon() instanceof IconUtils.LabelIcon)
            return ((IconUtils.LabelIcon)aButton.getIcon()).getLabel().getText();
        
        // Do normal version
        return aButton.getText();
    }
}

/**
 * JButtonHpr.
 */
public static class JButtonHpr <T extends JButton> extends AbstractButtonHpr<T> {

    /** Returns the action for a node. */
    public String getAction(T aButton)  { return (String)aButton.getClientProperty("SnapAction"); }
    
    /** Sets the action for a node. */
    public void setAction(T aButton, String anAction)  { aButton.putClientProperty("SnapAction", anAction); }
}

/**
 * JToggleButtonHpr.
 */
public static class JToggleButtonHpr <T extends JToggleButton> extends AbstractButtonHpr<T> {

    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Selected"); super.addPropNames(); }
}

/**
 * Ribs Helper for JProgressBar.
 */
public static class JProgressBarHpr <T extends JProgressBar> extends JComponentHpr <T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Value"); super.addPropNames(); }
}

/**
 * This class is a RibsHelper implementation for JSlider.
 */
public static class JSliderHpr <T extends JSlider> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Override to add ChangeListener to slider. */
    public void initUI(T aSlider, UIOwner anOwner)
    {
        super.initUI(aSlider, anOwner);
        aSlider.addChangeListener(getChangeListener());
    }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Value"); super.addPropNames(); }
}

/**
 * Implements Ribs bindings for JSpinner.
 */
public static class JSpinnerHpr <T extends JSpinner> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Initializes newly created object. */
    public void initUI(T aSpinner, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(aSpinner, anOwner);
        
        // Add action to Spinner
        aSpinner.addChangeListener(getChangeListener());
        
        // Get spinner text field and add some focus-lost behavior
        JFormattedTextField textField = getEditorTextField(aSpinner);
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    }
    
    /** Returns the spinner editor textfield. */
    public JFormattedTextField getEditorTextField(JSpinner aSpinner)
    {
        // If Spinner.Editor is JSpinner.DefaultEditor, return its TextField
        JComponent editor = aSpinner.getEditor();
        if(editor instanceof JSpinner.DefaultEditor) { JSpinner.DefaultEditor deditor = (JSpinner.DefaultEditor)editor;
            return deditor.getTextField(); }
        return null;
    }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Value", "Minimum", "Maximum", "StepSize"); super.addPropNames(); }
    
    /** Sets an object's value for given property name. */
    public void setValue(T aSpinner, String aPropertyName, Object aValue)
    {
        // If property name is Value, do some pre-emptive conversions based on model
        if(aPropertyName.equals("Value")) {
            if(aSpinner.getModel() instanceof SpinnerNumberModel) aValue = SnapUtils.doubleValue(aValue);
            else if(aSpinner.getModel() instanceof SpinnerDateModel) aValue = SnapUtils.getDate(aValue);
        }
        
        // Do normal version
        super.setValue(aSpinner, aPropertyName, aValue);
    }
}

/**
 * This class is a JComponentHpr subclass for JTextComponent.
 */
public static class JTextComponentHpr <T extends JTextComponent> extends JComponentHpr<T> {
    
    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Text"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return "Text"; // Remap Value to Text
        return super.getPropertyNameMapped(anObj, aName);
    }
}

/**
 * A helper for JTextArea.
 */
public static class JTextAreaHpr <T extends JTextArea> extends JTextComponentHpr<T> {

    /** Initializes newly created object. */
    public void initUI(T aTextArea, UIOwner anOwner)
    {
        super.initUI(aTextArea, anOwner);  // Do normal init
        aTextArea.setLineWrap(true); aTextArea.setWrapStyleWord(true); // Set some JTextArea, defaults
    }
}

/**
 * Helper for JTextField.
 */
public static class JTextFieldHpr <T extends JTextField> extends JTextComponentHpr<T> {
    
    /** Override to enable action event. */
    public void initUI(T aTextField, UIOwner anOwner)
    {
        super.initUI(aTextField, anOwner);
        getEventAdapter(aTextField, true).setEnabled(UIEvent.Type.Action, true);
    }
}

/**
 * Ribs Helper for formatted text field.
 */
public static class JFormattedTextFieldHpr <T extends JFormattedTextField> extends JTextComponentHpr<T> {

    /** Initializes newly created object. */
    public void initUI(T aTextField, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(aTextField, anOwner);
        
        // Add focus listener?
        // Implemented to make sure formatter validates the data whenever the focus is lost (temporarily or permanently).
        // Note that if the vale in the text field is invalid, focus will be allowed to changed, but the value in the
        // text field will revert to what it was before the invalid edit. You would think the 
        // setFocusLostBehavior(COMMIT_OR_REVERT) would do this, but it doesn't. Plus it ignores temporary focus events.
        aTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent anEvent) {
                JFormattedTextField textField = (JFormattedTextField)anEvent.getComponent();
                try { textField.commitEdit(); }
                catch(ParseException pe) { }
            }
        });
    }
}

/**
 * A helper implementation for JMenuItem (with specific support for subclasses: JCheckBoxMenuItem,JRadioButtonMenuItem).
 */
public static class JMenuItemHpr <T extends JMenuItem> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Override to enable Action event. */
    public void initUI(T aMenuItem, UIOwner anOwner)
    {
        super.initUI(aMenuItem, anOwner);
        getEventAdapter(aMenuItem, true).setEnabled(UIEvent.Type.Action, true);
    }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        // Map Value to Selected or State (JCheckBoxMenuItem)
        if(aName.equals("Value")) return anObj instanceof JCheckBoxMenuItem? "State" : "Selected";
        return super.getPropertyNameMapped(anObj, aName);
    }
}

/**
 * A helper class for JMenu.
 */
public static class JMenuHpr <T extends JMenu> extends JMenuItemHpr<T> {

    // A stand in for Separator menu items (Swing uses null)
    static JMenuItem _standInMenuItem = new JMenuItem();

    /** Override to return JMenu item count if menu item is a menu. */
    public int getChildCount(T aMenu)  { return aMenu.getItemCount(); }
    
    /** Returns the Ribs-relevant child component at the given index. */
    public JComponent getChild(T aMenu, int anIndex)
    { JMenuItem c = aMenu.getItem(anIndex); return c!=null? c : _standInMenuItem; }
}

/**
 * A helper implementation for JScrollPane.
 */
public static class JScrollPaneHpr <T extends JScrollPane> extends JComponentHpr<T> {

    /** Override to return 1 if Viewport is present. */
    public int getChildCount(T aScrollPane)  { return aScrollPane.getViewport().getView()==null? 0 : 1; }
    
    /** Returns the child component at the given index. */
    public JComponent getChild(T aScrollPane, int anIndex)  { return (JComponent)aScrollPane.getViewport().getView(); }
}

/**
 * A helper for JSplitPane
 */
public static class JSplitPaneHpr <T extends JSplitPane> extends JComponentHpr<T> {

    /** Initializes newly created object. */
    public void initUI(T aSplitPane, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(aSplitPane, anOwner);
        
        // This seems silly - insert empty JComponents for top/bottom
        if(aSplitPane.getTopComponent()==null) aSplitPane.setTopComponent(new JPanel());
        if(aSplitPane.getBottomComponent()==null) aSplitPane.setBottomComponent(new JPanel());
    }
    
    /** Returns the child component count. */
    public int getChildCount(T aSplitPane)
    { return (aSplitPane.getTopComponent()==null? 0 : 1) + (aSplitPane.getBottomComponent()==null? 0 : 1); }
    
    /** Returns the Ribs-relevant child component at the given index. */
    public JComponent getChild(T aSplitPane, int anIndex)
    { return anIndex==0? (JComponent)aSplitPane.getTopComponent() : (JComponent)aSplitPane.getBottomComponent(); }
    
    /** Sets the splitpane location by name (saved to preferences). */
    public static void setDividerLocation(final JSplitPane aSplitPane, final String aName, float aDefault)
    {
        // Get DividerLocation from preferences and set
        float divLoc = PrefsUtils.getPrefs().getFloat(aName, aDefault); if(divLoc<.1 || divLoc>.9) divLoc = aDefault;
        aSplitPane.setDividerLocation(divLoc);
        
        // Add PropertyChangeListener to save DividerLocation to prefs when it changes
        aSplitPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                    if(aSplitPane.getRightComponent()==null || !aSplitPane.getRightComponent().isVisible()) return;
                    boolean horizontal = aSplitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT;
                    float max = horizontal? aSplitPane.getWidth() : aSplitPane.getHeight();
                    float divLoc = aSplitPane.getDividerLocation()/max; if(divLoc<.1 || divLoc>.9) return;
                    PrefsUtils.prefsPut(aName, divLoc); PrefsUtils.flush();
                }
             }
        });
    }
}

/**
 * A Helper for JTabbedPane.
 */
public static class JTabbedPaneHpr <T extends JTabbedPane> extends JComponentHpr<T> {

    /** Override to add ChangeListener. */
    public void initUI(T aTabPane, UIOwner anOwner)
    {
        super.initUI(aTabPane, anOwner);  // Do normal init
        aTabPane.addChangeListener(getChangeListener());
    }
    
    /** Returns the child component count. */
    public int getChildCount(T aTabPane)  { return aTabPane.getTabCount(); }
    
    /** Returns the child component at the given index. */
    public JComponent getChild(T aTabPane, int anIndex)  { return (JComponent)aTabPane.getComponentAt(anIndex); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T aTabPane, String aName)
    {
        if(aName.equals("Value")) return "SelectedIndex";  // Map Value to SelectedIndex
        return super.getPropertyNameMapped(aTabPane, aName);
    }
}

/**
 * A helper class for SpringsPane.
 */
public static class SpringsPaneHpr <T extends SpringsPane> extends JComponentHpr<T> {

    /** Override to include WindowMenuBar (if present but not child). */
    public int getChildCount(T aPanel)
    {
        int count = aPanel.getComponentCount();
        if(aPanel.getWindowMenuBar()!=null && aPanel.getWindowMenuBar().getParent()!=aPanel) count++;
        return count;
    }
    
    /** Override to include WindowMenuBar (if present but not child). */
    public JComponent getChild(T aPanel, int anIndex)
    {
        if(anIndex<aPanel.getComponentCount())
            return (JComponent)aPanel.getComponent(anIndex);
        return aPanel.getWindowMenuBar();
    }
}

/**
 * Swing Helper for SwitchPane.
 */
public static class SwitchPaneHpr <T extends SwitchPane> extends JComponentHpr <T> {

    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("SelectedIndex", "SelectedName"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        // Map Value to SelectedIndex
        if(aName.equals("Value")) return "SelectedIndex";
        return super.getPropertyNameMapped(anObj, aName);
    }
    
    /** Sets the switch pane's selected index. */
    public void setValue(T aSwitchPane, String aPropertyName, Object aValue)
    {
        // Hack for old support of setting Value to pane name
        if(aPropertyName.equals("SelectedIndex") && (aValue instanceof String) && aSwitchPane.getPane((String)aValue)!=null)
            aPropertyName = "SelectedName";
        
        // Handle other property names
        else super.setValue(aSwitchPane, aPropertyName, aValue);
    }
}

/**
 * This class provides helper functionality for JComboBox.
 */
public static class JComboBoxHpr <T extends JComboBox> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Initializes newly created object. */
    public void initUI(T anObj, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(anObj, anOwner);
        
        // Add action listener
        getEventAdapter(anObj, true).setEnabled(UIEvent.Type.Action, true);
        
        // If editable and editor component is textfield, add FocusLostVerifier input verifier to editor component
        if(anObj.isEditable() && anObj.getEditor().getEditorComponent() instanceof JTextField)
            getEventAdapter(anObj, true).setInputVerifier(true);
    }
    
    /** Creates a SwingEventAdapter subclass. */
    protected SwingEventAdapter createEventAdapter(T aComboBox)  { return new JComboBoxEventAdapter(aComboBox); }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Items", "SelectedItem", "SelectedIndex"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return getBinding(anObj, "SelectedIndex")!=null? "SelectedIndex" : "SelectedItem";
        return super.getPropertyNameMapped(anObj, aName);
    }
    
    /** Override to convert SelectedItem enums to String if needed. */
    public void setValue(T aCBox, String aPropertyName, Object aValue)
    {
        // Get the combo box
        String pname = getPropertyNameMapped(aCBox, aPropertyName);
    
        // Handle SelectedItem property
        if(pname.equals("SelectedItem")) {
            if(aValue instanceof Enum && aCBox.getItemCount()>0 && aCBox.getItemAt(0) instanceof String)
                aValue = ((Enum)aValue).toString();
        }
    
        // Do normal version
        super.setValue(aCBox, aPropertyName, aValue);
    }
    
    /** Override to repaint. */
    public void setSelectedIndex(T aCBox, int anIndex)  { aCBox.setSelectedIndex(anIndex); }
    
    /** Returns the items for an object. */
    public List getItems(T anObj)
    {
        ComboBoxModel model = anObj.getModel(); if(model==null) return Collections.emptyList();
        Object array[] = new Object[model.getSize()];
        for(int i=0,iMax=model.getSize(); i<iMax; i++) array[i] = model.getElementAt(i);
        return Arrays.asList(array);
    }
    
    /** Sets the items for an object. */
    public void setItems(T anObj, List theItems)
    {
        CBoxItemsModel model = getItemsModel(anObj, true);
        model.setItems(theItems);
    }
    
    /** Returns the render key chain. */
    public String getItemDisplayKey(T aComboBox)
    {
        if(aComboBox.getRenderer() instanceof JListKeyChainCellRenderer)
            return ((JListKeyChainCellRenderer)aComboBox.getRenderer()).getKeyChain();
        return null;
    }
    
    /** Sets the display key for ComboBox items. */
    public void setItemDisplayKey(T aComboBox, String aKey)
    {
        aComboBox.setRenderer(aKey!=null && aKey.length()>0? new JListKeyChainCellRenderer(aKey) : null);
    }
    
    /** Returns the ItemsModel (with option to create/set). */
    private CBoxItemsModel getItemsModel(JComboBox aCB, boolean doCreate)
    {
        CBoxItemsModel model = aCB.getModel() instanceof CBoxItemsModel? (CBoxItemsModel)aCB.getModel() : null;
        if(model==null && doCreate) aCB.setModel(model = new CBoxItemsModel());
        return model;
    }
}

/**
 * A SwingEventListener for JComboBox.
 */
public static class JComboBoxEventAdapter extends SwingEventAdapter {

    /** Create new JComboBoxEventAdapter. */
    public JComboBoxEventAdapter(JComponent aComp)  { super(aComp); }

    /** Override. */
    public JComboBox getComponent()  { return (JComboBox)super.getComponent(); }
    
    /** Returns combobox TextComponent. */
    public JTextComponent getTextComponent() { return (JTextComponent)getComponent().getEditor().getEditorComponent(); }
    
    /** Override to add to JComboBox Editor EditorComponent. */
    protected void setKeyListenerSet(boolean aValue)
    {
        if(aValue==isKeyListenerSet()) return;
        super.setKeyListenerSet(aValue);
        if(!getComponent().isEditable()) return;
        if(aValue) getTextComponent().addKeyListener(this);
        else getTextComponent().removeKeyListener(this);
    }
    
    /** Override to add to JComboBox Editor EditorComponent. */
    protected void setFocusListenerSet(boolean aValue)
    {
        if(aValue==isFocusListenerSet()) return;
        super.setFocusListenerSet(aValue);
        if(!getComponent().isEditable()) return;
        if(aValue) getTextComponent().addFocusListener(this);
        else getTextComponent().removeFocusListener(this);
    }
    
    /** Override to verify Editor instead. */
    public boolean verify(JComponent aComponent)
    {
        JComboBox comboBox = getComponent(); if(!comboBox.isEditable()) return super.verify(aComponent);
        JTextComponent textField = getTextComponent();
        if(!SnapUtils.equals(textField.getText(), getFocusGainedValue()))
            comboBox.setSelectedItem(comboBox.getEditor().getItem());
        return true;
    }
}

/**
 * A combobox model implementation to handle binding.
 */
private static class CBoxItemsModel extends AbstractListModel implements ComboBoxModel {

    // The items and selected item
    List          _items = new ArrayList();
    Object        _selectedItem;
    
    /** Sets the items. */
    public void setItems(List theItems)
    {
        _items.clear(); if(theItems!=null) _items.addAll(theItems);
        fireContentsChanged(this, 0, getSize());
    }
    
    /** List model method - returns item count. */
    public int getSize()  { return _items.size(); }
    
    /** List model method - returns item at given index. */
    public Object getElementAt(int anIndex)  { return _items.get(anIndex); }
    
    /** Returns the selected item. */
    public Object getSelectedItem()  { return _selectedItem; }
    
    /** Sets the selected item. */
    public void setSelectedItem(Object anObj)  { _selectedItem = anObj; fireContentsChanged(this, -1, -1); }
}

/**
 * A helper implementation for JList.
 */
public static class JListHpr <T extends JList> extends JComponentHpr<T> {
    
    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Initializes newly created object. */
    public void initUI(T aList, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(aList, anOwner);
        
        // Get list and install list selection listener
        aList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Swing.sendEvent(e); }});
    }
    
    /** Scrolls a JList to make given row visible (-1 for current selection). */
    public static void scrollCellToVisible(JList aList, int aRow)
    {
        int row = aRow>=0? aRow : aList.getSelectedIndex();
        Rectangle rect = aList.getCellBounds(row, row);
        aList.scrollRectToVisible(rect);
    }
    
    /** Adds the property names. */
    protected void addPropNames()  { addPropNames("Items", "SelectedItem", "SelectedIndex"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return getBinding(anObj, "SelectedIndex")!=null? "SelectedIndex" : "SelectedItem";
        return super.getPropertyNameMapped(anObj, aName);
    }
    
    /** Returns the items for an object. */
    public List getItems(T aList)
    {
        ListItemsModel itemsModel = getItemsModel(aList, false);
        if(itemsModel!=null) return Collections.unmodifiableList(itemsModel._items);
        ListModel model = aList.getModel(); if(model==null) return Collections.emptyList();
        Object array[] = new Object[model.getSize()];
        for(int i=0,iMax=model.getSize(); i<iMax; i++) array[i] = model.getElementAt(i);
        return Arrays.asList(array);
    }
    
    /** Sets the items for an object. */
    public void setItems(T aList, List theItems)
    {
        ListItemsModel model = getItemsModel(aList, true);
        model.setItems(theItems);
        aList.repaint();
        if(aList.getSelectedIndex()>=model._items.size())
            aList.clearSelection();
    }
    
    /** Returns the render key chain. */
    public String getItemDisplayKey(T aList)
    {
        if(aList.getCellRenderer() instanceof JListKeyChainCellRenderer)
            return ((JListKeyChainCellRenderer)aList.getCellRenderer()).getKeyChain();
        return null;
    }
    
    public void setItemDisplayKey(T aList, String aKey)
    {
        aList.setCellRenderer(aKey!=null && aKey.length()>0? new JListKeyChainCellRenderer(aKey) : null);
    }
    
    /** Returns the selected object property of given object. */
    public Object getSelectedItem(T aList)  { return aList.getSelectedValue(); }
    
    /** Sets the selected object property of given object to given value. */
    public void setSelectedItem(T aList, Object aValue)
    {
        aList.setSelectedValue(aValue, true);
        if(aValue!=null && aValue.equals(aList.getSelectedValue()))
            aList.ensureIndexIsVisible(aList.getSelectedIndex());
        else aList.clearSelection();
    }
    
    /** Returns the ItemsModel (with option to create/set). */
    private ListItemsModel getItemsModel(JList aList, boolean doCreate)
    {
        ListItemsModel model = aList.getModel() instanceof ListItemsModel? (ListItemsModel)aList.getModel() : null;
        if(model==null && doCreate) aList.setModel(model = new ListItemsModel());
        return model;
    }
}

/**
 * List Model implementation for lists of items.
 */
private static class ListItemsModel extends AbstractListModel {

    // The items
    List          _items = new ArrayList();
    
    /** Sets the items. */
    public void setItems(List theItems)
    {
        _items.clear(); if(theItems!=null) _items.addAll(theItems);
        fireContentsChanged(this, 0, getSize());
    }
    
    /** List model method - returns item count. */
    public int getSize()  { return _items.size(); }
    
    /** List model method - returns item at given index. */
    public Object getElementAt(int anIndex)  { return _items.get(anIndex); }
}

/**
 * Inner class to dereference the display value from list value using given key (from binding).
 */
public static class JListKeyChainCellRenderer extends DefaultListCellRenderer {

    // The keychain
    String _keyChain;
    
    // Creates a new key chain cell renderer for key chain
    public JListKeyChainCellRenderer(String aKeyChain)  { _keyChain = aKeyChain; }
    
    // Returns key chain
    public String getKeyChain()  { return _keyChain; }
    public void setKeyChain(String aKeyChain)  { _keyChain = aKeyChain; }
    
    // Overrides to swap in value for key
    public Component getListCellRendererComponent(JList aList, Object aVal, int anIndex, boolean isSel, boolean hasFoc)
    {
        Object value = RMKeyChain.getValue(aVal, _keyChain);
        return super.getListCellRendererComponent(aList, value, anIndex, isSel, hasFoc);
    }
}

/**
 * A Helper subclass for JTable.
 */
public static class JTableHpr <T extends JTable> extends JComponentHpr<T> {

    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Initializes newly created object. */
    public void initUI(final T aTable, UIOwner anOwner)
    {
        // Do normal init
        super.initUI(aTable, anOwner);
        
        // Set the default selection mode
        aTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection listener to selection model
        aTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent anEvent)  { Swing.sendEvent(anEvent, aTable); }});
    }
    
    /** Removes columns. */
    public void removeColumns(JTable aTable)
    {
        for(int i=aTable.getColumnCount()-1; i>=0; i--)
            aTable.removeColumn(aTable.getColumnModel().getColumn(i));
    }
    
    /** Adds the property names. */
    protected void addPropNames()
    { addPropNames("Items", "SelectedItem", "SelectedIndex", "ColumnKeys"); super.addPropNames(); }
    
    /** Returns a mapped property name. */
    public String getPropertyNameMapped(T anObj, String aName)
    {
        if(aName.equals("Value")) return getBinding(anObj, "SelectedIndex")!=null? "SelectedIndex" : "SelectedItem";
        return super.getPropertyNameMapped(anObj, aName);
    }
    
    /** Sets the selected row index. */
    public void setValue(T aTable, String aPropertyName, Object aValue)
    {
        // Handle ColumnKeys
        if(aPropertyName.equals("ColumnKeys")) {
            removeColumns(aTable);
            if(aValue instanceof List)
                SwingUtils.addTableColumns(aTable, (List<String>)aValue);
        }
        
        // Do normal version
        else super.setValue(aTable, aPropertyName, aValue);
    }
    
    /** Returns the items for an object. */
    public List getItems(T aTable)
    {
        TableItemsModel itemsModel = getItemsModel(aTable, false);
        if(itemsModel!=null) return Collections.unmodifiableList(itemsModel._items);
        return Collections.emptyList();
    }
    
    /** Sets the items for an object. */
    public void setItems(T aTable, List theItems)
    {
        TableItemsModel model = getItemsModel(aTable, true);
        model.setItems(theItems);
    }
    
    /** Returns the selected index property of given object. */
    public int getSelectedIndex(T aTable)  { return aTable.getSelectedRow(); }
    
    /** Sets the selected index property of given object to given value. */
    public void setSelectedIndex(T aTable, int anIndex)
    {
        if(anIndex>=0) {
            aTable.setRowSelectionInterval(anIndex, anIndex);
            SwingUtils.scrollTableCellToVisible(aTable, anIndex, 0);
        }
        else aTable.clearSelection();
    }
    
    /** Returns the selected object property of given object. */
    public Object getSelectedItem(T aTable)
    {
        TableItemsModel model = getItemsModel(aTable, false); if(model==null) return null;
        int index = getSelectedIndex(aTable); if(index<0 || index>=model.getRowCount()) return null;
        RowSorter rs = aTable.getRowSorter(); if(rs!=null) index = rs.convertRowIndexToModel(index);
        return model._items.get(index);
    }
    
    /** Sets the selected object property of given object to given value. */
    public void setSelectedItem(T aTable, Object aValue)
    {
        TableItemsModel model = getItemsModel(aTable, false); if(model==null) return;
        for(int i=0, iMax=model.getRowCount(); i<iMax; i++)
            if(SnapUtils.equals(aValue, model._items.get(i))) {
                RowSorter rs = aTable.getRowSorter(); if(rs!=null) i = rs.convertRowIndexToView(i);
                setSelectedIndex(aTable, i); return;
            }
        setSelectedIndex(aTable, -1);
    }
    
    /** Returns whether table value is adjusting. */
    public boolean isValueAdjusting(T aTable)  { return aTable.getSelectionModel().getValueIsAdjusting(); }
    
    /** Returns the ItemsModel (with option to create/set). */
    private TableItemsModel getItemsModel(JTable aTable, boolean doCreate)
    {
        TableItemsModel model = aTable.getModel() instanceof TableItemsModel? (TableItemsModel)aTable.getModel() : null;
        if(model==null && doCreate) aTable.setModel(model = new TableItemsModel(aTable));
        return model;
    }
}

/**
 * Table Model implementation for tables using bind key.
 */
private static class TableItemsModel extends AbstractTableModel {

    // The table and items
    JTable       _table;
    List         _items = new ArrayList();

    /** Creates a new table model for given table. */ 
    public TableItemsModel(JTable aTable)  { _table = aTable; }
    
    /** Sets the items. */
    public void setItems(List theItems)
    {
        if(theItems==null) theItems = Collections.EMPTY_LIST;
        int count1 = _table.getRowCount(), count2 = theItems.size(), count3 = Math.min(count1, count2);
        _items.clear(); _items.addAll(theItems);
        if(count3>0) fireTableRowsUpdated(0, count3-1);
        if(count2>count1) fireTableRowsInserted(count1, count2-1);
        else if(count2<count1) fireTableRowsDeleted(count2, count1-1);
    }
    
    /** Table model method. */
    public int getColumnCount()  { return _table.getColumnCount(); }
    
    /** Table model method. */
    public int getRowCount()  { return _items.size(); }
    
    /** Table model method. */
    public Object getValueAt(int aRow, int aColumn)
    {
        Object object = _items.get(aRow);
        TableColumn column = _table.getColumnModel().getColumn(aColumn);
        String key = (String)column.getIdentifier();
        Object value = RMKeyChain.getValue(object, key);
        return value;
    }
}

/**
 * A helper implementation for JTree.
 */
public static class JTreeHpr <T extends JTree> extends JComponentHpr<T> {
    
    /** Override to suppress children. */
    public int getChildCount(T anObj)  { return 0; }
    
    /** Override to install TreeSelectionListener. */
    public void initUI(T aTree, UIOwner anOwner)
    {
        super.initUI(aTree, anOwner);
        aTree.addTreeSelectionListener(_treeSelectionListener);
    }
    
    /** Returns a component's value as a string. */
    public Object getValue(T aTree, String aPropertyName)
    {
        // Handle Value property: Get selected tree path and return selected node
        if(aPropertyName.equals("Value")) {
            TreePath treePath = aTree.getSelectionPath();
            return treePath!=null? treePath.getLastPathComponent() : null;
        }
        
        // Handle other property names
        return super.getValue(aTree, aPropertyName);
    }
    
    /** A tree selection listener to send action when node is selected. */
    private static TreeSelectionListener _treeSelectionListener = new TreeSelectionListener()
    {
        public void valueChanged(TreeSelectionEvent anEvent)  { Swing.sendEvent(anEvent); }
    };
    
    /** Returns the selected index property of given object. */
    public int getSelectedIndex(T aTree)
    {
        // Get selected rows and return first one
        int rows[] = aTree.getSelectionRows();
        return rows.length>0? rows[0] : -1;
    }
    
    /** Sets the selected index property of given object to given value. */
    public void setSelectedIndex(T aTree, int aValue)  { aTree.setSelectionRow(aValue); }
    
    /** Returns the selected object property of given object. */
    public Object getSelectedItem(T aTree)
    {
        // Get SelectionPath and SelectedObject, convert to UserObject and return
        TreePath selectedPath = aTree.getSelectionPath();
        Object selectedObject = selectedPath!=null? selectedPath.getLastPathComponent() : null;
        if(selectedObject instanceof DefaultMutableTreeNode)
            selectedObject = ((DefaultMutableTreeNode)selectedObject).getUserObject();
        return selectedObject;
    }
    
    /** Sets the selected object property of given object to given value. */
    public void setSelectedItem(T aTree, Object aValue)
    {
        // Find tree object, create TreePath, and setSelectionPath
        List selectedPath = findTreeObjectPath(aTree.getModel(), aTree.getModel().getRoot(), aValue);
        TreePath treePath = new TreePath(selectedPath.toArray());
        aTree.setSelectionPath(treePath);
    }
    
    /** Finds a tree object. */
    protected static List findTreeObjectPath(TreeModel aModel, Object aNode, Object aValue)
    {
        // If value is found, return new list with value
        if(getTreeNodeObject(aNode)==aValue)
            return ListUtils.newList(aValue);
        
        // Iterate over children
        for(int i=0, iMax=aModel.getChildCount(aNode); i<iMax; i++) {
            Object child = aModel.getChild(aNode, i);
            List list = findTreeObjectPath(aModel, child, aValue);
            if(list!=null) {
                list.add(0, getTreeNodeObject(aNode));
                return list;
            }
        }
        
        // Return null since value not found
        return null;
    }
    
    /** Returns a value for a tree node. */
    protected static Object getTreeNodeObject(Object aNode)
    {
        return aNode instanceof DefaultMutableTreeNode? ((DefaultMutableTreeNode)aNode).getUserObject() : aNode;
    }
}

}