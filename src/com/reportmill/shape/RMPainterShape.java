package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.graphics.RMPaintbot.*;
import java.awt.*;
import java.awt.geom.PathIterator;
import java.util.*;
import java.util.List;
import snap.swing.*;
import snap.util.*;

/**
 * A shape that references another shape and acts.
 */
public class RMPainterShape extends RMShape {

    // The current time
    float                 _time;
    
    // The list of painters
    List <RMPaintbot>      _painters = new ArrayList();
    
    // The shape drawn at the end of painting path
    RMShape               _paintShape;

/**
 * Returns the time.
 */
public float getTime()  { return _time; }

/**
 * Override to trigger act. 
 */
public void setTime(int aTime)
{
    super.setTime(aTime); // Do normal version
    _time = aTime/1000f; // Set time
    repaint(); // Repaint
}

/**
 * Returns the paint shape.
 */
public RMShape getPaintShape()
{
    // If paint shape not set, look for it
    if(_paintShape==null) {
        
        // Set paint shape to this in case everything fails
        _paintShape = this;
        
        // Try for rpt
        RMShapeIcon shapeIcon = (RMShapeIcon)Swing.getIcon(getClass().getSimpleName() + ".rpt", getClass());
        if(shapeIcon!=null) _paintShape = shapeIcon.getShape();
        
        // Otherwise, try for image
        else {
            Image image = Swing.getImage(getClass().getSimpleName() + ".png", getClass());
            if(image==null) image = Swing.getImage(getClass().getSimpleName() + ".jpg", getClass());
            if(image==null) image = Swing.getImage(getClass().getSimpleName() + ".gif", getClass());
            if(image!=null) _paintShape = new RMImageShape(image);
        }
    }
    
    // Return paint shape (or null if set to this shape)
    return _paintShape==this? null : _paintShape;
}

/**
 * Sets the paint color for path operations.
 */
public void setPaintColor(Color aColor)  { getPainter().setPaintColor(aColor); }

/**
 * Sets the paint stroke width for path operations.
 */
public void setPaintStrokeWidth(float aWidth)  { getPainter().setPaintStrokeWidth(aWidth); }

/**
 * Sets the paint speed for path operations.
 */
public void setPaintSpeed(float aSpeed)  { getPainter().setPaintSpeed(aSpeed); }

/**
 * Adds a forward instruction to painter.
 */
public void forward(float aDistance)  { getPainter().forward(aDistance); }

/**
 * Adds a turn instruction to painter.
 */
public void turn(float anAngle)  { getPainter().turn(anAngle); }

/**
 * Move to.
 */
public void moveTo(float anX, float aY)  { getPainter().moveTo(anX, aY); }

/**
 * Paint a line to given coordinates.
 */
public void lineTo(float anX, float aY)  { getPainter().lineTo(anX, aY); }

/**
 * Paint a parabolic curve with the given control point and end point.
 */
public void quadTo(float anX1, float aY1, float anX2, float aY2)  { getPainter().quadTo(anX1, aY1, anX2, aY2); }

/**
 * Paint a bezier curve with the given control points and end point.
 */
public void curveTo(float anX1, float aY1, float anX2, float aY2, float anX3, float aY3)
{
    getPainter().curveTo(anX1, aY1, anX2, aY2, anX3, aY3);
}

/**
 * Paint shape.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Paint painters
    for(int i=0, iMax=getPainterCount(); i<iMax; i++)
        getPainter(i).paint(aPntr.getGraphics(), getTime());
    
    // Paint painter shapes
    RMShape paintShape = getPaintShape();
    if(paintShape!=null) {
        for(int i=0, iMax=getPainterCount(); i<iMax; i++) { RMPaintbot painter = getPainter(i);
            paintShape.setXY(painter.getX() - paintShape.getWidth()/2, painter.getY() - paintShape.getHeight()/2);
            paintShape.setRoll(painter.getRoll());
            paintShape.paint(aPntr);
        }
    }
}

/**
 * Returns the first painter.
 */
public RMPaintbot getPainter()  { return getPainter(0); }

/**
 * Returns the number of painters.
 */
public int getPainterCount()  { return _painters.size(); }

/**
 * Returns the specific painter at given index.
 */
public RMPaintbot getPainter(int anIndex)
{
    while(anIndex>=getPainterCount())
        addPainter(new RMPaintbot());
    return _painters.get(anIndex);
}

/**
 * Adds a given painter to painters list.
 */
public void addPainter(RMPaintbot aPainter)  { addPainter(aPainter, getPainterCount()); }

/**
 * Adds a given painter to painter list at given index.
 */
public void addPainter(RMPaintbot aPainter, int anIndex)  { _painters.add(anIndex, aPainter); }

/**
 * Adds operations for path.
 */
public void addOpsForPath(PathIterator aPathIterator)
{
    // Declare loop variables
    float moveToX = 0, moveToY = 0;
    float coords[] = new float[6];
    
    // Iterate over path iterator segments
    while(!aPathIterator.isDone()) { int segment = aPathIterator.currentSegment(coords);
        switch(segment) {
            case PathIterator.SEG_MOVETO: moveToX = coords[0]; moveToY = coords[1]; moveTo(coords[0], coords[1]); break;
            case PathIterator.SEG_LINETO: lineTo(coords[0], coords[1]); break;
            case PathIterator.SEG_QUADTO: quadTo(coords[0], coords[1], coords[2], coords[3]); break;
            case PathIterator.SEG_CUBICTO: curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]); break;
            case PathIterator.SEG_CLOSE: lineTo(moveToX, moveToY); break;
        }
        aPathIterator.next();
    }
}

/**
 * Returns the total execution time of all painter shape operations.
 */
public float getExecutionTime()
{
    float time = 0;
    for(int i=0, iMax=getPainterCount(); i<iMax; i++) time = Math.max(time, getPainter(i).getExecutionTime());
    return time;
}

/**
 * Returns the total bounds from execution.
 */
public RMRect getExecutionBounds()
{
    RMRect bounds = getPainter().getBounds();
    for(int i=1, iMax=getPainterCount(); i<iMax; i++) bounds.union(getPainter(i).getBounds());
    if(getPaintShape()!=null)
        bounds.inset(-Math.max(getPaintShape().getWidth(), getPaintShape().getHeight())/2);
    return bounds;
}

/**
 * Returns the marked bounds.
 */
public RMRect getBoundsMarked()  { return getExecutionBounds(); }

/**
 * Override to get preferred width from instructions.
 */
public double computePrefWidth(float aHeight)  { return getExecutionBounds().getMaxX(); }

/**
 * Override to get preferred height from instructions.
 */
public double computePrefHeight(float aWidth)  { return getExecutionBounds().getMaxY(); }

/**
 * Override to make sure there is a child animator.
 */
public void setParent(RMParentShape aShape)  { super.setParent(aShape); getAnimator(true); }

/**
 * Standard clone implementation.
 */
public RMPainterShape clone()
{
    RMPainterShape clone = (RMPainterShape)super.clone(); // Do normal version
    clone._painters = new ArrayList(); // Clone painters
    for(int i=0, iMax=getPainterCount(); i<iMax; i++) clone.addPainter(getPainter(i).clone());
    clone._paintShape = null; // Clear paint shape
    return clone; // Return clone
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Do normal version and reset name
    XMLElement e = super.toXML(anArchiver); e.setName("painter-shape");
    
    // Create and add opsXML
    XMLElement opsXML = new XMLElement("Ops");
    e.add(opsXML);
    
    // Archive operations
    for(int i=0, iMax=getPainter().getOpCount(); i<iMax; i++) { RMPaintbot.Op op = getPainter().getOp(i);
    
        // Declare opXML
        XMLElement opXML = null;
    
        // Handle CurveTo
        if(op instanceof CurveTo) { CurveTo ct = (CurveTo)op;
            opXML = new XMLElement("CurveTo");
            opXML.add("cp1x", ct.cp1x); opXML.add("cp1y", ct.cp1y);
            opXML.add("cp2x", ct.cp2x); opXML.add("cp2y", ct.cp2y);
            opXML.add("x", ct.x); opXML.add("y", ct.y);
        }
        
        // Handle LineTo
        else if(op instanceof LineTo) { LineTo lt = (LineTo)op;
            opXML = new XMLElement("LineTo"); opXML.add("x", lt.x); opXML.add("y", lt.y); }

        // Handle MoveTo
        else if(op instanceof MoveTo) { MoveTo mt = (MoveTo)op;
            opXML = new XMLElement("MoveTo"); opXML.add("x", mt.x); opXML.add("y", mt.y); }
        
        // Handle SetPaintColor
        else if(op instanceof SetPaintColor) { SetPaintColor spc = (SetPaintColor)op;
            opXML = new XMLElement("SetPaintColor");
            opXML.add("paintColor", RMAWTUtils.toStringColor(spc.paintColor));
        }
        
        // Handle SetPaintStrokeWidth
        else if(op instanceof SetStrokeWidth) { SetStrokeWidth spsw = (SetStrokeWidth)op;
            opXML = new XMLElement("SetStrokeWidth");
            opXML.add("strokeWidth", spsw.strokeWidth);
        }
        
        // Add op xml
        if(opXML!=null) opsXML.add(opXML);
    }
    
    // Return xml element
    return e;
}

/**
 * XML Unarchival.
 */
public RMPainterShape fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal version
    super.fromXML(anArchiver, anElement);
    
    // Get Ops XML
    XMLElement opsXML = anElement.get("Ops");
    
    // Iterate over ops xml
    for(int i=0, iMax=opsXML.size(); i<iMax; i++) { XMLElement opXML = opsXML.get(i);
    
        // Handle CurveTo
        if(opXML.getName().equals("CurveTo"))
            curveTo(opXML.getAttributeFloatValue("cp1x"), opXML.getAttributeFloatValue("cp1y"),
                    opXML.getAttributeFloatValue("cp2x"), opXML.getAttributeFloatValue("cp2y"),
                    opXML.getAttributeFloatValue("x"), opXML.getAttributeFloatValue("y"));
            
        // Handle LineTo
        else if(opXML.getName().equals("LineTo"))
            lineTo(opXML.getAttributeFloatValue("x"), opXML.getAttributeFloatValue("y"));
    
        // Handle MoveTo
        else if(opXML.getName().equals("MoveTo"))
            moveTo(opXML.getAttributeFloatValue("x"), opXML.getAttributeFloatValue("y"));
    
        // Handle SetPaintColor
        else if(opXML.getName().equals("SetPaintColor"))
            setPaintColor(RMAWTUtils.fromStringColor(opXML.getAttributeValue("paintColor")));
    
        // Handle SetPaintStrokeWidth
        else if(opXML.getName().equals("SetStrokeWidth"))
            setPaintStrokeWidth(opXML.getAttributeFloatValue("strokeWidth"));
    }
    
    // Bogus
    int maxTime = Math.max(getParent().getChildAnimator(true).getMaxTime(), Math.round(getExecutionTime()*1000));
    getParent().getChildAnimator(true).setMaxTime(maxTime);
    
    // Return this painter shape
    return this;
}

}