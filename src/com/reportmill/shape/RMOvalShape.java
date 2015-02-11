package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import snap.util.*;

/**
 * This class represents a simple oval, with a setable start angle and sweep angle.
 */
public class RMOvalShape extends RMShape {
    
    // The oval start angle
    float         _startAngle = 0;
    
    // The oval sweep angle
    float         _sweep = 360;
    
    // Whether oval with sweep angle closes path by drawing lines to oval center and back
    boolean       _drawsWedge = true;
    
    // The oval path
    RMPath        _path;

/**
 * Creates a plain oval (draws stroke).
 */
public RMOvalShape() { }

/**
 * Creates an oval with the given startAngle and sweep.
 */
public RMOvalShape(float startAngle, float sweep)
{
    // Do normal initialization
    this();
    
    // Set given start angle and sweep
    _startAngle = startAngle;
    _sweep = sweep;
}

/**
 * Returns the start angle for the oval.
 */
public float getStartAngle()  { return _startAngle; }

/**
 * Sets the start angle for the oval.
 */
public void setStartAngle(float aValue)
{
    if(getStartAngle()==aValue) return;
    repaint();
    firePropertyChange("StartAngle", _startAngle, _startAngle = aValue, -1);
    _path = null;
}

/**
 * Returns the sweep angle for the oval.
 */
public float getSweepAngle()  { return _sweep; }

/**
 * Sets the sweep angle for the oval.
 */
public void setSweepAngle(float aValue)
{
    if(getSweepAngle()==aValue) return;
    repaint();
    firePropertyChange("SweepAngle", _sweep, _sweep = aValue, -1);
    _path = null;
}

/**
 * Return whether the oval draws lines from the unswept portion of the oval to the center (like a pie wedge).
 */
public boolean getDrawsWedge()  { return _drawsWedge; }

/**
 * Sets whether the oval draws lines from the unswept portion of the oval to the center (like a pie wedge).
 */
public void setDrawsWedge(boolean aFlag)  { _drawsWedge = aFlag; }

/**
 * Returns the (oval) path for this shape.
 */
public RMPath getPath()
{
    // If path hasn't been set, create path
    if(_path==null) {
    
        // Handle full sweep
        if(RMMath.equals(_sweep, 360))
            _path = RMPathUtils.appendOval(new RMPath(), RMRect.unitRect, 0, 360, false);
        
        // Handle partial sweep
        else {
            _path = RMPathUtils.appendOval(new RMPath(), RMRect.unitRect, _startAngle, _sweep, _drawsWedge);
            if(_drawsWedge)
                _path.closePath();
            _path.moveTo(0, 0);
            _path.moveTo(1, 1);
        }

        // Set bounds
        _path.setBounds(RMRect.unitRect);
    }
    
    // Return path
    return _path;
}

/**
 * Returns the property names for this shape.
 */
protected void addPropNames() { addPropNames("StartAngle", "SweepAngle"); super.addPropNames(); }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("oval");
    
    // Archive StartAngle, Sweep
    if(_startAngle!=0) e.add("start", _startAngle);
    if(_sweep!=360) e.add("sweep", _sweep);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive StartAngle, Sweep
    setStartAngle(anElement.getAttributeFloatValue("start"));
    setSweepAngle(anElement.getAttributeFloatValue("sweep", 360));
    return this;
}

}