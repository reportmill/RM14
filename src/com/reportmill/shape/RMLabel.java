package com.reportmill.shape;
import com.reportmill.base.*;
import java.awt.*;
import snap.util.*;

/**
 * This class represents an individual label inside an RMLabels template.
 */
public class RMLabel extends RMParentShape {

/**
 * Editor method - indicates that individual label accepts children.
 */
public boolean acceptsChildren()  { return true; }

/**
 * Paints label.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Do normal paint shape
    super.paintShape(aPntr);
    
    // Table bands should draw a red band around thier perimeter when it is selected
    if(aPntr.isSelected(this) || aPntr.isSuperSelected(this)) {
        RMRect bounds = getBoundsInside(); bounds.inset(2, 2);
        aPntr.setColor(Color.red); aPntr.setStroke(RMAWTUtils.Stroke1);
        aPntr.draw(bounds);
    }
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("label");
    return e;
}

}

