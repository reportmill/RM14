package snap.swing;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.util.SnapUtils;

/**
 * A class to build a form.
 */
public class FormBuilder extends SwingOwner {

    // The padding
    Insets                    _padding = new Insets(8,8,8,8);
    
    // The spacing
    int                       _spacing = 5;
    
    // The font
    Font                      _font;

    // A map to manage buttongroups for child buttons
    Map <String,ButtonGroup>  _buttonGroups = new HashMap();

/**
 * Returns the padding.
 */
public Insets getPadding()  { return _padding; }

/**
 * Sets the padding.
 */
public void setPadding(Insets theInsets)  { _padding = (Insets)theInsets.clone(); }

/**
 * Sets the padding.
 */
public void setPadding(int aTp, int aLt, int aBt, int aRt)  { setPadding(new Insets(aTp, aLt, aBt, aRt)); }

/**
 * Returns the spacing between components.
 */
public int getSpacing()  { return _spacing; }

/**
 * Sets the spacing between components.
 */
public void setSpacing(int aValue)  { _spacing = aValue; }

/**
 * Returns the font.
 */
public Font getFont()  { return _font; }

/**
 * Sets the font.
 */
public void setFont(Font aFont)  { _font = aFont; }

/**
 * Adds a label.
 */
public JLabel addLabel(String aTitle)
{
    JLabel label = new JLabel(aTitle); label.setAlignmentX(0); if(_font!=null) label.setFont(_font);
    label.setHorizontalAlignment(SwingConstants.LEFT); label.setHorizontalTextPosition(SwingConstants.LEFT);
    return addNode(label);
}

/**
 * Adds a separator.
 */
public JSeparator addSeparator()
{
    JSeparator sep = new JSeparator(); sep.setAlignmentX(0);
    return addNode(sep);
}

/**
 * Adds a text field.
 */
public JTextField addTextField(String aTitle, String aDefault)  { return addTextField(aTitle, aDefault, 160); }

/**
 * Adds a text field.
 */
public JTextField addTextField(String aTitle, String aDefault, int aWidth)
{
    // Create TextField and panel and add
    JLabel label = new JLabel(aTitle + ":"); label.setAlignmentX(0); if(_font!=null) label.setFont(_font);
    JTextField tfield = new JTextField(); tfield.setName(aTitle); if(_font!=null) tfield.setFont(_font);
    tfield.setPreferredSize(new Dimension(aWidth, tfield.getPreferredSize().height));
    Box panel = new Box(BoxLayout.X_AXIS); panel.setAlignmentX(0);
    panel.add(label); panel.add(tfield); addNode(panel);
    
    // Add binding
    addNodeBinding(tfield, "Text", aTitle.replace(" ", ""));
    if(aDefault!=null) setValue(aTitle, aDefault);
    
    // Set FirstFocus
    if(getFirstFocus()==null) setFirstFocus(tfield);
    
    // Return text field
    return tfield;
}

/**
 * Adds an option field.
 */
public JComboBox addComboBox(String aTitle, String options[], String aDefault)
{
    // Create ComboBox and panel and add
    JLabel label = new JLabel(aTitle + ":"); label.setAlignmentX(0);
    JComboBox cbox = new JComboBox(options); cbox.setName(aTitle); if(_font!=null) cbox.setFont(_font);
    Box panel = new Box(BoxLayout.X_AXIS); panel.setAlignmentX(0);
    panel.add(label); panel.add(cbox); addNode(panel);
    
    // Add binding
    addNodeBinding(cbox, "SelectedItem", aTitle.replace(" ", ""));
    setValue(aTitle, aDefault);
    
    // Return combobox
    return cbox;
}

/**
 * Adds radio buttons.
 */
public List <JRadioButton> addRadioButtons(String aTitle, String options[], String aDefault)
{
    List <JRadioButton> rbuttons = new ArrayList();
    for(String option : options) rbuttons.add(addRadioButton(aTitle, option, option.equals(aDefault)));
    return rbuttons;
}

/**
 * Adds a radio button.
 */
public JRadioButton addRadioButton(String aTitle, String theText, boolean isSelected)
{
    // Create radio button, add to button group and add to panel
    JRadioButton rb = new JRadioButton(theText); rb.setName(aTitle); rb.setAlignmentX(0);
    if(_font!=null) rb.setFont(_font);
    if(isSelected) { rb.setSelected(true); setValue(aTitle, theText); }
    getButtonGroup(aTitle).add(rb);
    addNode(rb);
    return rb;
}

/**
 * Adds a component.
 */
public <T extends JComponent> T addNode(T aComp)
{
    if(getUI().getComponentCount()>0) getUI().add(Box.createRigidArea(new Dimension(0,getSpacing())));
    getUI().add(aComp);
    initUI(aComp);
    return aComp;
}

/**
 * Runs the option panel and returns a map.
 */
public boolean showPanel(JComponent aComp, String aTitle, Icon anIcon)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setIcon(anIcon); dbox.setContent(getUI());
    return dbox.showConfirmDialog(aComp);
}

/**
 * Returns the specified value.
 */
public Object getValue(String aKey)  { String key = aKey.replace(" ", ""); return getModelValue(key); }

/**
 * Sets the specified value.
 */
public void setValue(String aKey, Object aValue)  { String key = aKey.replace(" ", ""); setModelValue(key, aValue); }

/**
 * Returns the specified value.
 */
public String getStringValue(String aKey)  { return SnapUtils.stringValue(getValue(aKey)); }

/**
 * Creates the UI.
 */
protected JComponent createUI()
{
    SpringsPane ui = new SpringsPane(); ui.setLayout(new BoxLayout(ui, BoxLayout.Y_AXIS));
    ui.setBorder(BorderFactory.createEmptyBorder(_padding.top, _padding.left, _padding.bottom, _padding.right));
    return ui;
}

/**
 * Responds to UI.
 */
protected void respondUI(SwingEvent anEvent)
{
    // Handle RadioButtons
    if(anEvent.getComponent() instanceof JRadioButton) { JRadioButton rb = (JRadioButton)anEvent.getComponent();
        setValue(rb.getName(), rb.getText());
    }
}

/**
 * Return the button group in this table for the given name (creating if needed).
 */
protected ButtonGroup getButtonGroup(String aName)
{
    ButtonGroup bg = _buttonGroups.get(aName);
    if(bg==null) _buttonGroups.put(aName, bg = new ButtonGroup());
    return bg;
}

}