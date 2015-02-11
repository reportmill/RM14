package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.awt.*;
import snap.util.*;

/**
 * Strokes a path with 2 lines of different widths, with an arbitrary separation (d) between them.  
 * 
 * This works by taking the input path and creating 'offset curves'.  The offset
 * curve is defined as being parallel to the original curve at every point.
 *
 * For a given parametric curve C(t)={x(t),y(t)}, the slope of the curve at t
 * is given by mC(t)=(dy/dt) / (dx/dt) = y'(t)/x'(t) and
 * the the normal is therefore NC(t)=Normalize({-x'(t), y'(t)})
 * 
 * An offset curve for a cubic bezier can not be represented by a single bezier.
 * Therefore, we need to find a set of n curves OffC0,OffC1,...OffCn that fits,
 * as closely as possible, the following points:
 *   OffsetPoint(t)=C(t)+d*NC(t)
 * 
 * One possibility is to calculate a million OffsetPoints for the range t->{0,1}
 * and run the curve fit code.  
 * 
 * In order to preserve continuity of the different bezier segments, it should
 * also be required that
 *   mOffC0(0) = mC(0)
 *   and
 *   mOffCn(1) = mC(1)
 * That is, the slopes at the beginning and end of the curve should the same in
 * the offset curve as it is in the original curve.
 * Also, since the offset points are obtained by walking along the normal vector
 * for the original curve, the slope of the offset curve at offsetpoint(t) should
 * be the same as mC(t).  The curve fit code does not currently take into account
 * the slope of the curve at a given input point, but it's something to look
 * into.  Knowing this may provide enough additional information to speed up the
 * curve fit and/or reduce the number of input points needed for an accurate fit.
 */
 public class RMDoubleStroke extends RMStroke {
 
   // 'outer' and 'inner' rules are defined by the direction of the path.  For a closed path, such as a rectangle
   // or a circle, the outer rule will be on the outside if the path is declared clockwise.  For a counter-
   // clockwise declared path, the sense will be reversed.
   float       _outerRuleWidth;
   
   // The stroke width of the inner rule
   float       _innerRuleWidth;
   
   // The distance between paths
   float       _ruleSeparation;
   
   // One of the constants below
   int         _rulePositions;
   
   // Inner stroke
   RMStroke    _innerStroke = new RMStroke();
   
   // Outer stroke
   RMStroke    _outerStroke = new RMStroke();
   
   // Constants for rule positions
   public static final int OUTER_RULE_ON_PATH = 0;
   public static final int INNER_RULE_ON_PATH = 1;
   public static final int RULES_CENTERED_ABOUT_PATH = 2;
   public static final int RULE_GAP_ON_PATH = 3;

/**
 * Creates a new plain double stroke.
 */
public RMDoubleStroke()  { this(RMColor.black, 1, 2, 1, OUTER_RULE_ON_PATH); }

/**
 * Creates a new double stroke.
 */
public RMDoubleStroke(RMColor col, float w)  { this(col, 1, w+1, 2, INNER_RULE_ON_PATH); }

/**
 * Creates a new double stroke.
 */
public RMDoubleStroke(RMColor col, float outerW, float innerW, float separation, int positions)
{
    // Set given values
    setColor(col);
    _outerRuleWidth = outerW;
    _innerRuleWidth = innerW;
    _ruleSeparation = separation;
    _rulePositions = positions;
    
    // Create the offsets
    makeChildStrokes();
}

/**
 * Returns the double stroke rule positions.
 */
public int getRulePositions()  { return _rulePositions; }

/**
 * Returns the inner rule width.
 */
public float getInnerRuleWidth()  { return _innerRuleWidth; }

/**
 * Returns the outer rule width.
 */
public float getOuterRuleWidth()  { return _outerRuleWidth; }

/**
 * Return the stroke used on the outside of a clockwise path
 */
public RMStroke getOuterStroke()  { return _outerStroke; }

/**
 * Return the stroke used on the inside of a clockwise path
 */
public RMStroke getInnerStroke()  { return _innerStroke; }

/**
 * Creates the offset child strokes.
 */
private void makeChildStrokes()
{
    float d = (_outerRuleWidth+_innerRuleWidth)/2 + _ruleSeparation;
    
    // Declare variables for inner and outer offsets
    float outer_offset = 0;
    float inner_offset = 0;
    
    // Calculate where each of the two paths should be positioned relative
    // to the input path, based on the widths of the individual strokes.
    switch (_rulePositions) {
        case OUTER_RULE_ON_PATH: inner_offset = d; break;
        case INNER_RULE_ON_PATH: outer_offset = -d; break;
        case RULES_CENTERED_ABOUT_PATH:
            outer_offset = -d/2;
            inner_offset = d/2;
            break;
        case RULE_GAP_ON_PATH:
            outer_offset = -(_outerRuleWidth+_ruleSeparation)/2;
            inner_offset = (_innerRuleWidth+_ruleSeparation)/2;
            break;
    }
    
    // Add new outer & inner strokes
    _outerStroke = new RMOffsetStroke(getColor(), _outerRuleWidth, outer_offset);
    _innerStroke = new RMOffsetStroke(getColor(), _innerRuleWidth, inner_offset);
}

/**
 * Returns the bounds required to render this fill for this shape.
 */
public RMRect getBounds(RMShape aShape)  { return _outerStroke.getBounds(aShape); }

/**
 * Override to paint inner and outer strokes.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    _innerStroke.paint(aPntr, aShape);
    _outerStroke.paint(aPntr, aShape);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(!(anObj instanceof RMDoubleStroke)) return false;
    RMDoubleStroke other = (RMDoubleStroke)anObj;
    
    // Check InnerStroke, OuterStroke, RulePositions, InnerRuleWidth, OuterRuleWidth, RuleSeparation
    if(!RMUtils.equals(_innerStroke, other._innerStroke)) return false;
    if(!RMUtils.equals(_outerStroke, other._outerStroke)) return false;
    if(_rulePositions != other._rulePositions) return false;
    if(_innerRuleWidth != other._innerRuleWidth) return false;
    if(_outerRuleWidth != other._outerRuleWidth) return false;
    if(_ruleSeparation != other._ruleSeparation) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public RMDoubleStroke clone()
{
    RMDoubleStroke clone = (RMDoubleStroke)super.clone(); // Do normal version
    clone.makeChildStrokes(); // Create new inner/outer strokes
    return clone; // Return clone
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic stroke attributes
    XMLElement e = super.toXML(anArchiver); e.add("type", "double");
 
    // Archive OuterRuleWidth, InnerRuleWidth, RuleSeparation, RulePositions
    if(_outerRuleWidth != 1) e.add("outerwidth", _outerRuleWidth);
    if(_innerRuleWidth != 1) e.add("innerwidth", _innerRuleWidth);
    if(_ruleSeparation != 2) e.add("separation", _ruleSeparation);
    if(_rulePositions != OUTER_RULE_ON_PATH) e.add("positions", _rulePositions);
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic stroke attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive OuterRuleWidth, InnerRuleWidth, RuleSeparation, RulePositions
    _outerRuleWidth = anElement.getAttributeFloatValue("outerwidth", 1);
    _innerRuleWidth = anElement.getAttributeFloatValue("innerwidth", 1);
    _ruleSeparation = anElement.getAttributeFloatValue("separation", 2);
    _rulePositions = anElement.getAttributeIntValue("positions", OUTER_RULE_ON_PATH);
    
    // Create the offsets
    makeChildStrokes();
    
    // Return this stroke
    return this;
}

/**
 * RMOffsetStroke is a simple stroke class that performs the usual stroking
 * operations, but inset or outset a certain distance from its path.
 */
private class RMOffsetStroke extends RMStroke {

    // The offset from the path
    float     _offset;
    
    // Creates a new offset stroke for given color, width and offset.
    public RMOffsetStroke(RMColor aColor, float aStrokeWidth, float anOffset)
    {
        super(aColor, aStrokeWidth); // Do normal RMStroke init with color and stroke width
        _offset = anOffset; // Set given offset
    }
    
    // Returns parent color
    public RMColor getColor()  { return RMDoubleStroke.this.getColor(); }
    
    // Returns the offset path
    public Shape getStrokePath(RMShape aShape)
    {
        // Get shape path (just return if offset is zero)
        RMPath path = aShape.getPathInBounds(); if(_offset==0) return path;
        return RMPathOffsetter.createOffsetPath(path, _offset); // Return the offset path
    }
}
    
}