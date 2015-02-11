package com.reportmill.swing.shape;
import com.reportmill.base.*;
import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import snap.swing.IconUtils;
import snap.util.*;

/**
 * A JComponentShape for AbstractButton.
 */
public class AbstractButtonShape extends LabeledShape {

    // Whether button is selected
    boolean           _selected;
    
    // The selected image name
    String            _siname;
    
    // The pressed image name
    String            _piname;

    // Whether border is painted
    Boolean           _borderPainted;
    
    // Whether content area filled
    Boolean           _cafilled;
    
    // Whether focus is painted
    Boolean           _focusPainted;
    
    // The margins
    Insets            _margin;
    
    // The button group name
    String            _bgname;

/**
 * Returns whether button is selected.
 */
public boolean isSelected()  { return _selected; }

/**
 * Sets whether button is selected.
 */
public void setSelected(boolean aValue)
{
    firePropertyChange("Selected", _selected, _selected = aValue, -1);
}

/**
 * Returns the selected image name.
 */
public String getSelectedImageName()  { return _siname; }

/**
 * Sets the selected image name.
 */
public void setSelectedImageName(String aName)
{
    firePropertyChange("SelectedImageName", _siname, _siname = aName, -1);
}

/**
 * Returns the pressed image name.
 */
public String getPressedImageName()  { return _piname; }

/**
 * Sets the pressed image name.
 */
public void setPressedImageName(String aName)
{
    firePropertyChange("PressedImageName", _piname, _piname = aName, -1);
}

/**
 * Returns whether button border is painted.
 */
public boolean isBorderPainted()  { return _borderPainted!=null && _borderPainted; }

/**
 * Returns whether button border is painted.
 */
public Boolean getBorderPainted()  { return _borderPainted; }

/**
 * Sets whether button border is painted.
 */
public void setBorderPainted(Boolean aValue)
{
    firePropertyChange("BorderPainted", _borderPainted, _borderPainted = aValue, -1);
}

/**
 * Returns whether button content area filled.
 */
public boolean isContentAreaFilled()  { return _cafilled!=null && _cafilled; }

/**
 * Returns whether button content area filled.
 */
public Boolean getContentAreaFilled()  { return _cafilled; }

/**
 * Sets whether button content area filled.
 */
public void setContentAreaFilled(Boolean aValue)
{
    firePropertyChange("ContentAreaFilled", _cafilled, _cafilled = aValue, -1);
}

/**
 * Returns whether focus is painted.
 */
public boolean isFocusPainted()  { return _focusPainted!=null && _focusPainted; }

/**
 * Returns whether focus is painted.
 */
public Boolean getFocusPainted()  { return _focusPainted; }

/**
 * Sets whether focus is painted.
 */
public void setFocusPainted(Boolean aValue)
{
    firePropertyChange("FocusPainted", _focusPainted, _focusPainted = aValue, -1);
}

/**
 * Returns the button margins.
 */
public Insets getMargin()  { return _margin; }

/**
 * Sets the button margins.
 */
public void setMargin(Insets theInsets)
{
    firePropertyChange("Margin", _margin, _margin = theInsets, -1);
}

/**
 * Returns the margin string.
 */
public String getMarginString()  { return _margin!=null? RMAWTUtils.toStringInsets(_margin) : null; }

/**
 * Sets the button margin string.
 */
public void setMarginString(String aString)
{
    Insets insets = aString!=null && aString.length()>0? RMAWTUtils.fromStringInsets(aString) : null;
    setMargin(insets);
}

/**
 * Returns the button group name.
 */
public String getButtonGroupName()  { return _bgname; }

/**
 * Sets the button group name.
 */
public void setButtonGroupName(String aName)
{
    firePropertyChange("ButtonGroupName", _bgname, _bgname = aName, -1);
}

/**
 * Returns a mapped property name.
 */
public String getPropertyNameMapped(String aPropertyName)
{
    if(aPropertyName.equals("Value")) return "Selected";
    return super.getPropertyNameMapped(aPropertyName);
}

/**
 * Override to apply subclass attributes.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get button
    AbstractButton button = (AbstractButton)aComp;
    
    // Get icons
    Icon sicon = getIcon(getSelectedImageName());
    Icon picon = getIcon(getPressedImageName());
    
    // If no pressed icon, create one: Create icon for circle and icon for composite of icon on circle
    if(picon==null && button.getIcon()!=null && getBorderPainted()!=null && !isBorderPainted()) {
        Icon icon = button.getIcon();
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        Icon circle = new IconUtils.ShapeIcon(new RoundRectangle2D.Float(0, 0, w-1, h-1, 16, 16), Color.white, w, h);
        sicon = picon = new IconUtils.CompositeIcon(icon, circle);
    }
    
    // Set icons
    if(sicon!=null) button.setSelectedIcon(sicon);
    if(picon!=null) button.setPressedIcon(picon);
    
    // Set other stuff
    if(isSelected()) button.setSelected(true);
    if(getBorderPainted()!=null) button.setBorderPainted(isBorderPainted());
    if(getContentAreaFilled()!=null) button.setContentAreaFilled(isContentAreaFilled());
    if(getFocusPainted()!=null) button.setFocusPainted(isFocusPainted());
    if(getMargin()!=null) button.setMargin(getMargin());
    else if(getHeight()<20) button.setMargin(new Insets(0,0,0,0));
    if(getButtonGroupName()!=null) aBldr.getButtonGroup(getButtonGroupName()).add(button);
    
    // If Mac OS X, have JButton, JToggleButton use JButton.buttonType "text"
    if((button instanceof JButton || button.getClass()==JToggleButton.class) && RMUtils.isMac)
        button.putClientProperty("JButton.buttonType", "text");
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes
    XMLElement e = super.toXMLShape(anArchiver);
    
    // Set the element name (one of: checkbox, radio-button, toggle, button)
    if(this instanceof JCheckBoxShape) e.setName("jcheckbox");
    else if(this instanceof JRadioButtonShape) e.setName("jradiobutton");
    else if(this instanceof JToggleButtonShape) e.setName("jtogglebutton");
    else e.setName("jbutton");
    
    // Archive SelectedImageName, PressedImageName
    if(getSelectedImageName()!=null) e.add("selected-image", getSelectedImageName());
    if(getPressedImageName()!=null) e.add("pressed-image", getPressedImageName());
    
    // Archive selected state
    if(isSelected()) e.add("selected", true);
    
    // Archive BorderPainted, ContentAreaFilled, FocusPainted
    if(getBorderPainted()!=null) e.add("border", isBorderPainted());
    if(getContentAreaFilled()!=null) e.add("content", isContentAreaFilled());
    if(getFocusPainted()!=null) e.add("focus-painted", isFocusPainted());
        
    // Archive ButtonGroupName
    if(getButtonGroupName()!=null) e.add("bgroup", getButtonGroupName());
    
    // Archive margin
    if(getMargin()!=null) e.add("margin", RMAWTUtils.toStringInsets(getMargin()));
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive SelectedImageName, PressedImageName
    setSelectedImageName(anElement.getAttributeValue("selected-image"));
    setPressedImageName(anElement.getAttributeValue("pressed-image"));
    
    // Unarchive selected state
    setSelected(anElement.getAttributeBoolValue("selected"));
    
    // Unarchive BorderPainted, ContentAreaFilled, FocusPainted
    if(anElement.hasAttribute("border")) setBorderPainted(anElement.getAttributeBoolValue("border"));
    if(anElement.hasAttribute("content")) setContentAreaFilled(anElement.getAttributeBoolValue("content"));
    if(anElement.hasAttribute("focus-painted")) setFocusPainted(anElement.getAttributeBoolValue("focus-painted"));
        
    // Unarchive Margin
    if(anElement.hasAttribute("margin")) {
        Insets insets = RMAWTUtils.fromStringInsets(anElement.getAttributeValue("margin"));
        setMargin(insets);
    }
    
    // Unarchive ButtonGroupName
    if(anElement.hasAttribute("bgroup"))
        setButtonGroupName(anElement.getAttributeValue("bgroup"));
}

}