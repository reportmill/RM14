package com.reportmill.graphics;
import com.reportmill.base.RMRect;
import java.awt.*;
import java.util.*;
import java.util.List;
import snap.swing.AWTUtils;
import snap.util.*;

/**
 * An object to process painter shape operations.
 */
public class RMPaintbot extends SnapObject {

    // The list of painter shape operations
    List <Op>       _ops = new ArrayList();
    
    // Last path x, y and angle
    double                   _lastX, _lastY, _angle;
    
    // Last MoveTo/LineTo
    double                   _lastMTX, _lastMTY;

/**
 * Creates a new painter shape painter.
 */
public RMPaintbot()  { }

/**
 * Returns the number of painter shape operations.
 */
public int getOpCount()  { return _ops.size(); }

/**
 * Returns the individual operation at given index.
 */
public Op getOp(int anIndex)  { return _ops.get(anIndex); }

/**
 * Returns the list of ops.
 */
public List <Op> getOps()  { return _ops; } 

/**
 * Adds a new operation.
 */
public void addOp(Op anOp)  { addOp(anOp, getOpCount()); }

/**
 * Adds a new operation at the given index.
 */
private void addOp(Op anOp, int anIndex)
{
    // Add operation
    _ops.add(anIndex, anOp);
    
    // Get last op and init op
    anOp.initOp(_lastX, _lastY, _angle);
    _lastX = anOp.endX; _lastY = anOp.endY; _angle = anOp.endAngle;
    
    // If MoveTo, update LastMoveTo
    if(anOp.getClass()==MoveTo.class) {
        _lastMTX = anOp.endX; _lastMTY = anOp.endY; }
    
    // Fire property change
    firePropertyChange("Op", null, anOp, anIndex);
}

/**
 * Returns the first operation of given class.
 */
public Op getOpOfClass(Class aClass)
{
    for(int i=0, iMax=getOpCount(); i<iMax; i++)
        if(aClass.isInstance(getOp(i)))
            return getOp(i);
    return null;
}

/**
 * Returns the first operation of given class.
 */
public <T extends Op> T getLastOpOfClass(Class <T> aClass)
{
    for(int i=getOpCount()-1; i>=0; i--)
        if(aClass.isInstance(getOp(i)))
            return (T)getOp(i);
    return null;
}

/**
 * Sets the paint color for path operations.
 */
public void setPaintColor(Color aColor)  { addOp(new SetPaintColor(aColor)); }

/**
 * Sets the paint stroke width for path operations.
 */
public void setPaintStrokeWidth(double aWidth)  { addOp(new SetStrokeWidth(aWidth)); }

/**
 * Sets the paint speed for path operations.
 */
public void setPaintSpeed(double aSpeed)  { addOp(new SetPaintSpeed(aSpeed)); }

/**
 * Adds a forward instruction to cpu.
 */
public void forward(double aDistance)  { addOp(new Forward(aDistance)); }

/**
 * Adds a Jump instruction to cpu.
 */
public void jump(double aDistance)  { addOp(new Jump(aDistance)); }

/**
 * Adds a turn instruction to cpu.
 */
public void turn(double anAngle)  { addOp(new Turn(anAngle)); }

/**
 * Move to.
 */
public void moveTo(double anX, double aY)  { addOp(new MoveTo(anX, aY)); }

/**
 * Paint a line to given coordinates.
 */
public void lineTo(double anX, double aY)  { addOp(new LineTo(anX, aY)); }

/**
 * Paint a parabolic curve with the given control point and end point.
 */
public void quadTo(double anX1, double aY1, double anX2, double aY2)  { addOp(new QuadTo(anX1, aY1, anX2, aY2)); }

/**
 * Paint a bezier curve with the given control points and end point.
 */
public void curveTo(double anX1, double aY1, double anX2, double aY2, double anX3, double aY3)
{
    addOp(new CurveTo(anX1, aY1, anX2, aY2, anX3, aY3));
}

/**
 * Closes a path.
 */
public void close()  { addOp(new LineTo(_lastMTX, _lastMTY)); }

/**
 * Returns the default paint speed.
 */
public float getPaintSpeedDefault()  { return 288; }

/**
 * Returns this CPU's current x position.
 */
public double getX()  { return _lastX; }

/**
 * Returns this CPU's current y position.
 */
public double getY()  { return _lastY; }

/**
 * Returns this CPU's current roll.
 */
public float getRoll()  { return (float)_angle; }

/**
 * Paint.
 */
public RMLine paint(Graphics2D aGraphics, double aTime)
{
    // Reset graphics defaults
    aGraphics.setColor(Color.BLUE);
    aGraphics.setStroke(AWTUtils.getStroke(4));
    
    // Declare loop variables
    double time = 0;
    double paintSpeed = getPaintSpeedDefault();
    RMPath path = null;
    RMLine segment = null;
    
    // Process operations
    for(int i=0, iMax=getOpCount(); i<iMax && time<aTime; i++) { Op op = getOp(i);
    
        // If not path op and path is present, draw path and clear
        if(!(op instanceof PathOperation) && path!=null) {
            aGraphics.draw(path); path = null; }
        
        // If real segment add segment (or partial segment) and update time
        if(op.segment!=null) { segment = op.segment;
         
            // Make sure path exists
            if(path==null) {
                Op lop = i>0? getOp(i-1) : null; 
                path = new RMPath(); path.moveTo(lop!=null? lop.endX: 0, lop!=null? lop.endY : 0);
            }
            
            // Get execution time
            double executionTime = segment.getArcLength()/paintSpeed;
                
            // If max time doesn't exceed execution time, shave segment
            if(aTime-time<executionTime) {
                segment = segment.getHead((aTime-time)/executionTime);
                time = aTime;
            }
                
            // Otherwise increment time
            time += executionTime;
                
            // Add path segment
            path.addSegment(segment);
        }
        
        // Handle MoveTo or Jump
        else if(op instanceof MoveTo || op instanceof Jump) {
            if(path!=null) path.moveTo(op.endX, op.endY); }
        
        // Handle SetPaintColor op
        else if(op instanceof SetPaintColor)
            aGraphics.setColor(((SetPaintColor)op).paintColor);
        
        // Handle SetStrokeWidth op
        else if(op instanceof SetStrokeWidth)
            aGraphics.setStroke(AWTUtils.getStroke(((SetStrokeWidth)op).strokeWidth));
    
        // Handle SetPaintSpeed
        else if(op instanceof SetPaintSpeed)
            paintSpeed = ((SetPaintSpeed)op).paintSpeed;
    }
    
    // Draw any residual path
    if(path!=null)
        aGraphics.draw(path);
    
    // Return segment
    return segment;
}

/**
 * Returns the execution time for painter shape operations.
 */
public float getExecutionTime()
{
    // Declare time variable
    float time = 0;
    double paintSpeed = getPaintSpeedDefault();

    // Iterate over operations
    for(int i=0, iMax=getOpCount(); i<iMax; i++) { Op op = getOp(i);
    
        // Handle path operations
        if(op.segment!=null)
            time += op.segment.getArcLength()/paintSpeed;
        
        // Handle SetPaintSpeed
        else if(op instanceof SetPaintSpeed)
            paintSpeed = ((SetPaintSpeed)op).paintSpeed;
    }
    
    // Return time
    return time;
}

/**
 * Returns the bounds of the painter shape operations.
 */
public RMRect getBounds()
{
    // Declare time variable
    RMRect bounds = null, scratchRect = new RMRect();
    float strokeWidth = 4;

    // Iterate over operations
    for(int i=0, iMax=getOpCount(); i<iMax; i++) { Op op = getOp(i);
    
        // Handle path operations
        if(op.segment!=null) {
            if(bounds==null) bounds = op.segment.getBounds().inset(-strokeWidth);
            else {
                op.segment.getBounds(scratchRect);
                bounds.union(scratchRect.inset(-strokeWidth));
            }
        }
        
        // Handle stroke width
        else if(op instanceof SetStrokeWidth) { SetStrokeWidth ssw = (SetStrokeWidth)op;
            strokeWidth = ssw.strokeWidth; }
    }
    
    // Return bounds
    return bounds!=null? bounds : scratchRect;
}

/**
 * Standard clone implementation.
 */
public RMPaintbot clone()
{
    RMPaintbot clone = (RMPaintbot)super.clone();
    clone._ops = ListUtils.clone(_ops);
    return clone;
}

/**
 * A class representing an operation to an painter shape, like MoveTo or LineTo.
 */
public static class Op {

    // The ending x, y, angle
    double endX, endY, endAngle;
    
    // The path segment
    RMLine       segment;
    
    // Initialize
    void initOp(double anX, double aY, double anAngle)  { endX = anX; endY = aY; endAngle = anAngle; }
}

/**
 * An operation to set paint color.
 */
public static class SetPaintColor extends Op {

    // The paint color
    public Color         paintColor;
    
    /** Creates a new SetPaintColor operation. */
    public SetPaintColor(Color aColor)  { paintColor = aColor; }
}

/**
 * An operation to set stroke width.
 */
public static class SetStrokeWidth extends Op {

    // The stroke width
    public float      strokeWidth;
    
    /** Creates a new SetStrokeWidth operation. */
    public SetStrokeWidth(double aWidth)  { strokeWidth = (float)aWidth; }
}

/**
 * An operation to set painting speed.
 */
public static class SetPaintSpeed extends Op {

    // The painting speed
    public float    paintSpeed = 72;
    
    /** Create new set painting speed operation. */
    public SetPaintSpeed(double aSpeed)  { paintSpeed = (float)aSpeed; }
}

/**
 * An inner class for path construction operations.
 */
public static class PathOperation extends Op { }

/**
 * An op class for moving a shape forward along the path of its current roll.
 */
public static class Forward extends PathOperation {

    // The distance of the move
    public double     distance;
    
    /** Creates a new Forward operation. */
    public Forward(double aDistance)  { distance = aDistance; }
    
    /** Override to set segment. */
    void initOp(double anX, double aY, double anAngle)
    {
        double x = anX + distance*Math.cos(Math.toRadians(anAngle));
        double y = aY + distance*Math.sin(Math.toRadians(anAngle));
        segment = new RMLine(anX, aY, x, y);
        endX = x; endY = y; endAngle = segment.getAngle(1);
    }
}

/**
 * An op class for moving a shape forward along the path of its current roll.
 */
public static class Jump extends PathOperation {

    // The distance of the move
    public double     distance;
    
    /** Creates a new Forward operation. */
    public Jump(double aDistance)  { distance = aDistance; }
    
    /** Override to set segment. */
    void initOp(double anX, double aY, double anAngle)
    {
        double x = anX + distance*Math.cos(Math.toRadians(anAngle));
        double y = aY + distance*Math.sin(Math.toRadians(anAngle));
        endX = x; endY = y; endAngle = anAngle;
    }
}

/**
 * An op class for turning a shape by a given number of degrees.
 */
public static class Turn extends PathOperation {

    // The angle to turn in degrees
    public double     angle;
    
    /** Creates a new Turn operation. */
    public Turn(double anAngle)  { angle = anAngle; }
    
    /** Initialize. */
    void initOp(double anX, double aY, double anAngle)  { endX = anX; endY = aY; endAngle = anAngle + angle; }
}

/**
 * An inner class for path move to.
 */
public static class MoveTo extends PathOperation {

    // The end point
    public double     x, y;

    /** Create new MoveTo operation. */
    public MoveTo(double anX, double aY)  { x = anX; y = aY; }
    
    /** Initialize. */
    void initOp(double anX, double aY, double anAngle)  { endX = x; endY = y; endAngle = anAngle; }
}

/**
 * A path construction operation for painting a line.
 */
public static class LineTo extends MoveTo {

    /** Creates a new LineTo operation. */
    public LineTo(double anX, double aY)  { super(anX, aY); }
    
    /** Override to set segment. */
    void initOp(double anX, double aY, double anAngle)
    {
        segment = new RMLine(anX, aY, x, y);
        endX = x; endY = y; endAngle = segment.getAngle(1);
    }
}

/**
 * A path construction operation for painting a quadratic curve.
 */
public static class QuadTo extends LineTo {

    // The (first) control point
    public double cp1x, cp1y;

    /** Creates a new QuadTo operation. */
    public QuadTo(double anX1, double aY1, double anX2, double aY2)
    {
        super(anX2, aY2);
        cp1x = anX1;
        cp1y = aY1;
    }
    
    /** Override to set segment. */
    void initOp(double anX, double aY, double anAngle)
    {
        segment = new RMQuadratic(anX, aY, cp1x, cp1y, x, y);
        endX = x; endY = y; endAngle = segment.getAngle(1);
    }
}

/**
 * A path construction operation for painting a bezier curve.
 */
public static class CurveTo extends QuadTo {

    // The second control point
    public double cp2x, cp2y;

    /** Creates a new CurveTo operation. */
    public CurveTo(double anX1, double aY1, double anX2, double aY2, double anX3, double aY3)
    {
        super(anX1, aY1, anX3, aY3);
        cp2x = anX2;
        cp2y = aY2;
    }
    
    /** Override to set segment. */
    void initOp(double anX, double aY, double anAngle)
    {
        segment = new RMBezier(anX, aY, cp1x, cp1y, cp2x, cp2y, x, y);
        endX = x; endY = y; endAngle = segment.getAngle(1);
    }
}

}