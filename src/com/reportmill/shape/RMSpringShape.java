package com.reportmill.shape;
import java.awt.*;
import snap.util.*;

/**
 * A parent shape that does child layout with RMSpringLayout.
 */
public class RMSpringShape extends RMParentShape {
    
    // Whether to GrowHeight
    boolean       _growHeight = true;

/**
 * Creates a new RMSpringShape.
 */
public RMSpringShape()  { setLayout(new RMSpringLayout()); }

/**
 * Returns the shape preferred height.
 */
protected double computePrefHeight(double aWidth)
{
    if(!_growHeight)
        return getHeight();
    return super.computePrefHeight(aWidth);
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLShape(anArchiver,anElement);
    if(anElement.hasAttribute("GrowHeight")) _growHeight = anElement.getAttributeBoolValue("GrowHeight");
    if(anArchiver.getVersion()<=12)
        _growHeight = false;
}

/**
 * XML Archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("spring-shape");
    e.add("GrowHeight", false);
    return e;
}
    
/**
 * Override to paint dashed box around bounds.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Do normal version
    super.paintShape(aPntr);
    
    // Paint dashed box around bounds
    if(aPntr.isEditing() && getStroke()==null && getFill()==null && getEffect()==null &&
        (aPntr.isSelected(this) || aPntr.isSuperSelected(this))) {
        aPntr.setColor(Color.lightGray); aPntr.setStroke(new BasicStroke(1f, 0, 0, 1, new float[] { 3, 2 }, 0));
        aPntr.setAntialiasing(false); aPntr.draw(getBoundsInside()); aPntr.setAntialiasing(true);
    }
}

}