package com.reportmill.swing.shape;
import com.reportmill.base.RMStringUtils;
import javax.swing.*;
import snap.util.*;

/**
 * An RMShape subclass for JSlider.
 */
public class JSliderShape extends JComponentShape {

    // Min, max, value
    double     _min, _max, _value;
    
    // Minor/Major track tick space
    int        _minorTickSpace, _majorTickSpace;
    
    // Paint/snap booleans
    boolean   _paintLabels, _paintTicks, _paintTrack = true, _snapToTicks;

    // The orientation
    int       _orient = SwingConstants.HORIZONTAL;
/**
 * Returns the minimum.
 */
public double getMinimum()  { return _min; }

/**
 * Sets the minimum.
 */
public void setMinimum(double aValue)
{
    firePropertyChange("Minimum", _min, _min = aValue, -1);
}

/**
 * Returns the maximum.
 */
public double getMaximum()  { return _max; }

/**
 * Sets the maximum.
 */
public void setMaximum(double aValue)
{
    firePropertyChange("Maximum", _max, _max = aValue, -1);
}

/**
 * Returns the value.
 */
public double getValue()  { return _value; }

/**
 * Sets the value.
 */
public void setValue(double aValue)
{
    firePropertyChange("Value", _value, _value = aValue, -1);
}

/**
 * Returns whether paints labels.
 */
public boolean getPaintLabels()  { return _paintLabels; }

/**
 * Sets whether paints labels.
 */
public void setPaintLabels(boolean aValue)
{
    firePropertyChange("PaintLabels", _paintLabels, _paintLabels = aValue, -1);
}

/**
 * Returns whether paints ticks.
 */
public boolean getPaintTicks()  { return _paintTicks; }

/**
 * Sets whether paints ticks.
 */
public void setPaintTicks(boolean aValue)
{
    firePropertyChange("PaintTicks", _paintTicks, _paintTicks = aValue, -1);
}

/**
 * Returns whether paints track.
 */
public boolean getPaintTrack()  { return _paintTrack; }

/**
 * Sets whether paints track.
 */
public void setPaintTrack(boolean aValue)
{
    firePropertyChange("PaintTrack", _paintTrack, _paintTrack = aValue, -1);
}

/**
 * Returns whether snaps to ticks.
 */
public boolean getSnapToTicks()  { return _snapToTicks; }

/**
 * Sets whether snaps to ticks.
 */
public void setSnapToTicks(boolean aValue)
{
    firePropertyChange("SnapToTicks", _snapToTicks, _snapToTicks = aValue, -1);
}

/**
 * Returns the minor tick spacing.
 */
public int getMinorTickSpacing()  { return _minorTickSpace; }

/**
 * Sets the minor tick spacing.
 */
public void setMinorTickSpacing(int aValue)
{
    firePropertyChange("MinorTickSpacing", _minorTickSpace, _minorTickSpace = aValue, -1);
}

/**
 * Returns the major tick spacing.
 */
public int getMajorTickSpacing()  { return _majorTickSpace; }

/**
 * Sets the major tick spacing.
 */
public void setMajorTickSpacing(int aValue)
{
    firePropertyChange("MajorTickSpacing", _majorTickSpace, _majorTickSpace = aValue, -1);
}

/**
 * Returns the orientation of the JSeparator.
 */
public int getOrientation()  { return _orient; }

/**
 * Resets the orientation of the separator component and resizes the shape.
 */
public void setOrientation(int aValue)
{
    firePropertyChange("Orientation", _orient, _orient = aValue, -1);
}

/**
 * Returns the component class for this component shape.
 */
public Class <? extends JComponent> getComponentClass()  { return JSlider.class; }

/**
 * Configures the component.
 */
protected void configureComponent(JBuilder aBldr, JComponent aComp)
{
    // Do normal version
    super.configureComponent(aBldr, aComp);
    
    // Get JSlider and configure
    JSlider slider = (JSlider)aComp;
    slider.setMinimum((int)getMinimum());
    slider.setMaximum((int)getMaximum());
    slider.setValue((int)getValue());
    slider.setMinorTickSpacing(getMinorTickSpacing());
    slider.setMajorTickSpacing(getMajorTickSpacing());
    slider.setPaintLabels(getPaintLabels());
    slider.setPaintTicks(getPaintTicks());
    slider.setPaintTrack(getPaintTrack());
    slider.setSnapToTicks(getSnapToTicks());
    slider.setOrientation(getOrientation());
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic component attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("jslider");
    
    // Archive Minimum, Maximum and current value
    if(getMinimum()!=0) e.add("min", getMinimum());
    if(getMaximum()!=100) e.add("max", getMaximum());
    if(getValue()!=50) e.add("value", getValue());
        
    // Archive ticks settings
    if(getPaintTicks()) {
        StringBuffer ticks = new StringBuffer().append(getMajorTickSpacing());
        if(getMinorTickSpacing()>0) ticks.append(',').append(getMinorTickSpacing());
        e.add("ticks", ticks.toString());
    }
    
    // Archive SnapsToTicks, PaintLabels, PaintTrack
    if(getSnapToTicks()) e.add("snap", true);
    if(getPaintLabels()) e.add("labels", true);
    if(!getPaintTrack()) e.add("track", false);
        
    // Archive orientation
    if(getOrientation()==SwingConstants.VERTICAL) e.add("orient", "vertical");

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

    // Unarchive Minimum, Maximum and current value
    setMinimum(anElement.getAttributeIntValue("min", 0));
    setMaximum(anElement.getAttributeIntValue("max", 100));
    setValue(anElement.getAttributeIntValue("value", 50));
    
    // Unarchive ticks settings
    String ticks = anElement.getAttributeValue("ticks");
    if(ticks!=null) {
        int major = RMStringUtils.intValue(ticks);
        int minor = ticks.indexOf(",")>0? RMStringUtils.intValue(ticks.substring(ticks.indexOf(","))) : 0;
        setPaintTicks(true);
        setMajorTickSpacing(major);
        setMinorTickSpacing(minor);
    }
    
    // Unarchive SnapsToTicks, PaintLabels, PaintTrack
    setSnapToTicks(anElement.getAttributeBoolValue("snap"));
    setPaintLabels(anElement.getAttributeBoolValue("labels", false));
    setPaintTrack(anElement.getAttributeBoolValue("track", true));

    // Unarchive orientation
    if(anElement.getAttributeValue("orient", "horizontal").equals("vertical"))
        setOrientation(SwingConstants.VERTICAL);
}

}