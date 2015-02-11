package com.reportmill.swing.shape;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JProgressBar.
 */
public class JProgressBarShape extends JComponentShape {

    // The min, max and current values
    double        _min, _max, _value;

    // Whether progress bar is indeterminate
    Boolean       _indeterminate;
    
    // The orientation
    int           _orientation = SwingConstants.HORIZONTAL;
    
/**
 * Returns the minimum value of progress bar.
 */
public double getMinimum()  { return _min; }

/**
 * Sets the minimum value of the progress bar.
 */
public void setMinimum(double aValue)
{
    firePropertyChange("Minimum", _min, _min = aValue, -1);
}

/**
 * Returns the maximum value of progress bar.
 */
public double getMaximum()  { return _max; }

/**
 * Sets the maximum value of the progress bar.
 */
public void setMaximum(double aValue)
{
    firePropertyChange("Maximum", _max, _max = aValue, -1);
}

/**
 * Returns the value of progress bar.
 */
public double getValue()  { return _value; }

/**
 * Sets the value of the progress bar.
 */
public void setValue(double aValue)
{
    firePropertyChange("Value", _value, _value = aValue, -1);
}

/**
 * Returns whether progress bar is indeterminate.
 */
public boolean isIndeterminate()  { return _indeterminate!=null && _indeterminate; }

/**
 * Returns whether progress bar is indetermiante.
 */
public Boolean getIndeterminate()  { return _indeterminate; }

/**
 * Sets whether progress bar is indetermiante.
 */
public void setIndeterminate(Boolean aValue)
{
    firePropertyChange("Indeterminate", _indeterminate, _indeterminate = aValue, -1);
}

/**
 * Returns the orientation of this progress bar.
 */
public int getOrientation()  { return _orientation; }

/**
 * Sets the orientation of this progress bar.
 */
public void setOrientation(int aValue)
{
    firePropertyChange("Orientation", _orientation, _orientation = aValue, -1);
}

/**
 * Creates the component for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JProgressBar.class; }

/**
 * Configure component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get progress bar and configure
    JProgressBar pbar = (JProgressBar)aComp;
    pbar.setMinimum((int)getMinimum());
    pbar.setMaximum((int)getMaximum());
    pbar.setValue((int)getValue());
    pbar.setOrientation(getOrientation());
    if(getIndeterminate()!=null) pbar.setIndeterminate(isIndeterminate());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Do normal component archival and reset name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jprogressbar");
    
    // Archive isIndeterminate
    if(getIndeterminate()!=null) e.add("indeterminate", true);
    
    // Archive Minimum, Maximum, Value
    if(getMinimum()!=0) e.add("minimum", getMinimum());
    if(getMaximum()!=100) e.add("maximum", getMaximum());
    if(getValue()!=0) e.add("value", getValue());

    // Archive orientation
    if(getOrientation()==SwingConstants.VERTICAL) e.add("orientation", "vertical");

    // Return the element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic component attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive is indeterminate
    if(anElement.getAttributeBoolValue("indeterminate", false)) setIndeterminate(true);
    
    // Unarchive Minimum, Maximum, Value
    setMinimum(anElement.getAttributeIntValue("minimum", 0));
    setMaximum(anElement.getAttributeIntValue("maximum", 100));
    setValue(anElement.getAttributeIntValue("value", 0));

    // Unarchive orientation
    if(anElement.getAttributeValue("orientation", "horizontal").equals("vertical"))
        setOrientation(SwingConstants.VERTICAL);
}

/** Override to suppress. */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

/** Override to suppress. */
public void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)  { }

}