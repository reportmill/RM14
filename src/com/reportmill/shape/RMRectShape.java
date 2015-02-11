package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import snap.util.*;

/**
 * This class represents a simple rectangle shape with a rounding radius.
 */
public class RMRectShape extends RMShape {
    
    // Rounding radius
    float      _radius = 0;

/**
 * Returns the rounding radius for the rectangle.
 */
public float getRadius()  { return _radius; }

/**
 * Sets the rounding radius for the rectangle.
 */
public void setRadius(float aValue)
{
    if(getRadius()==aValue) return;
    repaint();
    firePropertyChange("Radius", _radius, _radius = aValue, -1);
}

/**
 * Returns the path for the rectangle (building path with rounded corners if needed).
 */
public RMPath getPath()
{
    // Declare variable for path
    RMPath path = null;

    // If rounding radius, cache path
    if(getRadius() > 0.0001) {
        RMRect bounds = getBoundsInside();
        double hw = bounds.width/2, hh = bounds.height/2;
        double rw = getRadius() > hw? hw : getRadius();
        double rh = getRadius() > hh? hh : getRadius();
        double of = .5523f; // I calculated this in mathematica one time

        path = new RMPath();

        // Start point and left edge
        path.moveTo(new RMPoint(-hw, rh - hh));
        if(rh != hh)
            path.relativeLineTo(new RMPoint(0, 2*(hh - rh)));

        // Upper Left corner
        path.relativeCurveTo(new RMPoint(0,of*rh), new RMPoint(rw*(1-of),rh), new RMPoint(rw,rh));

        // Upper edge
        if(rw != hw)
            path.relativeLineTo(new RMPoint(2*(hw-rw),0));

        // Upper right corner
        path.relativeCurveTo(new RMPoint(of*rw,0), new RMPoint(rw,-rh*(1-of)), new RMPoint(rw,-rh));

        // Right edge
        if(rh != hh)
            path.relativeLineTo(new RMPoint(0,-2*(hh-rh)));

        // Lower right corner
        path.relativeCurveTo(new RMPoint(0,-of*rh), new RMPoint(-rw*(1-of),-rh), new RMPoint(-rw,-rh));

        // Lower edge
        if(rw != hw)
            path.relativeLineTo(new RMPoint(-2*(hw-rw),0));

        // Lower left corner
        path.relativeCurveTo(new RMPoint(-of*rw,0), new RMPoint(-rw,rh*(1-of)), new RMPoint(-rw,rh));

        // Close
        path.closePath();
    }
    
    // Return path
    return path==null? super.getPath() : path;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("rect");
    
    // Archive Radius
    if(_radius!=0) e.add("radius", _radius);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive Radius
    if(anElement.hasAttribute("radius")) setRadius(anElement.getAttributeFloatValue("radius"));
    return this;
}

}