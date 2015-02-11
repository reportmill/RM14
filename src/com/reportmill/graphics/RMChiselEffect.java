package com.reportmill.graphics;
import com.reportmill.base.RMClassUtils;
import com.reportmill.shape.*;
import java.awt.image.BufferedImage;
import snap.util.*;

/**
 * An emboss effect that uses contour fill as its bump map. 
 */
public class RMChiselEffect extends RMEmbossEffect {

    // Percentage of the shape to carve up
    float         _bevelDepth = 1f;

    // I don't have a good name for this yet.
    boolean       _directionIsUp = true;
  
/**
 * Creates effect image
 */
 public BufferedImage getImage(RMShape aShape)
 {
     // get the image to be embossed
     BufferedImage embossImage = getShapeImage(aShape, 0, false);
     
     // make an RMPolygon with the shape's outline and a shapeburst fill for generating the bumpmap
     RMPolygonShape pathShape = new RMPolygonShape(aShape.getMaskPath());
     pathShape.setSize(aShape.getWidth(), aShape.getHeight());
     if(aShape.getStroke()!=null) pathShape.setStroke(aShape.getStroke().clone());
     pathShape.setFill(new RMContourFill(_bevelDepth)); // the fill used temporarily by shape to draw its bump map
     BufferedImage bumpImage = getShapeImage(pathShape, _radius, true);

     // run the emboss
     emboss(embossImage, bumpImage);
     return embossImage;
}
 
/** 
 * Overridden from RMEmbossEffect to use the blue sample as the height
 */
public void isolateHeightSample(int bumpPixels[], int w, int h)
{
    // Bump map is drawn with RMContourFill, with gradient from black to white so we could use any of either r, g, or b
    if (_directionIsUp) {
        for(int i=0, iMax=w*h; i<iMax; i++) 
            bumpPixels[i] = bumpPixels[i]&0xff;
    }
    
    //
    else {
        for(int i=0, iMax=w*h; i<iMax; i++) 
            bumpPixels[i] = 255-(bumpPixels[i]&0xff);
    }
}

public RMChiselEffect deriveEffect(float newDepth)
{
    RMChiselEffect other = (RMChiselEffect)clone();
    other._bevelDepth = newDepth;
    return other;
}

public RMChiselEffect deriveEffect(boolean direction)
{
    RMChiselEffect other = (RMChiselEffect)clone();
    other._directionIsUp = direction;
    return other;
}

/**
 * Returns whether direction is up.
 */
public boolean isDirectionUp()  { return _directionIsUp; }

/**
 * Returns bevel depth.
 */
public float getBevelDepth()  { return _bevelDepth; }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, super and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMChiselEffect other = RMClassUtils.getInstance(anObj, RMChiselEffect.class); if(other==null) return false;
    
    // Check BevelDepth and Direction
    if(other.getBevelDepth()!=getBevelDepth()) return false;
    if(other.isDirectionUp()!=isDirectionUp()) return false;
    return true;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver); e.add("type", "chisel");
    if(getBevelDepth()!=1) e.add("depth", getBevelDepth());
    if(!isDirectionUp()) e.add("up", false);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);
    _bevelDepth = anElement.getAttributeFloatValue("depth", 1);
    _directionIsUp = anElement.getAttributeBoolValue("up",true);
    return this;
}

}