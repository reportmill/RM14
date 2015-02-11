package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JSpinner.
 */
public class JSpinnerShape extends JComponentShape {

    // Number spinner attributes
    double     _number, _min, _max = 100, _stepSize = 1;
  
/**
 * Returns the spinner value.
 */
public double getNumber()  { return _number; }

/**
 * Sets the spinner value.
 */
public void setNumber(double aValue)
{
    firePropertyChange("Number", _number, _number = aValue, -1);
}

/**
 * Returns the spinner min.
 */
public double getMinimum()  { return _min; }

/**
 * Sets the spinner min.
 */
public void setMinimum(double aValue)
{
    firePropertyChange("Minimum", _min, _min = aValue, -1);
}

/**
 * Returns the spinner max.
 */
public double getMaximum()  { return _max; }

/**
 * Sets the spinner min.
 */
public void setMaximum(double aValue)
{
    firePropertyChange("Maximum", _max, _max = aValue, -1);
}

/**
 * Returns the spinner step size.
 */
public double getStepSize()  { return _stepSize; }

/**
 * Sets the spinner step size.
 */
public void setStepSize(double aValue)
{
    firePropertyChange("StepSize", _stepSize, _stepSize = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JSpinner.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get spinner and configure
    JSpinner spinner = (JSpinner)aComp;
    spinner.setModel(new SpinnerNumberModel(getNumber(), getMinimum(), getMaximum(), getStepSize()));
    
    // If editor is DefaultEditor, set font in text field
    if(spinner.getEditor() instanceof JSpinner.DefaultEditor)
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setFont(aComp.getFont());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jspinner");
    
    // Archive Value, Minimum, Maximum, StepSize
    if(getNumber()!=0) e.add("value", getNumber());
    if(getMinimum()!=0) e.add("min", getMinimum());
    if(getMaximum()!=100) e.add("max", getMaximum());
    if(getStepSize()!=1) e.add("step", getStepSize());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive Value, Minimum, Maximum, StepSize
    if(anElement.hasAttribute("value")) setNumber(anElement.getAttributeFloatValue("value"));
    if(anElement.hasAttribute("min")) setMinimum(anElement.getAttributeFloatValue("min"));
    if(anElement.hasAttribute("max")) setMaximum(anElement.getAttributeFloatValue("max"));
    if(anElement.hasAttribute("step")) setStepSize(anElement.getAttributeFloatValue("step"));
}

}