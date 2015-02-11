package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.RMPath;
import snap.util.*;

/**
 * An n pointed star.
 * 
 * A Star is defined as a shape with n equal triangles rotated about a circle
 * such that the startpoint of one triangle is equivalent to the endpoint
 * of the triangle before it.
 * 5 and six pointed triangles give what you'd expect.  Other values give interesting,
 * although undoubtedly less useful shapes.
 */
public class RMStarShape extends RMShape {

    // The number of points (start points, not RMPoints)
    int         _numPoints = 5;
    
    // adjustment to the angles of the sides of the star's points 
    double      _bloat = getOptimalBloat(_numPoints);
    
    // starting angle for first star or vertex 
    double      _startAngle = Math.PI/2;
    
    // The star type
    int         _starType = STAR;
    
    // the path
    RMPath      _path;
    
    // this shape can draw either stars or regular polygons
    public static int STAR=0;
    public static int POLY=1;
    public static int MAGIC=2;
    
/**
 * Return the point count.
 */
public int getNumPoints()  { return _numPoints; }

/**
 * Sets the point count.
 */
public void setNumPoints(int aCount)
{
    if(aCount==_numPoints) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("NumPoints", _numPoints, _numPoints = aCount, -1); // Set value and fire PropertyChange
    _bloat = getOptimalBloat(_numPoints); _path = null;
}

/**
 * Return the bloat.
 */
public double getBloat()  { return _bloat; }

/**
 * Sets the bloat.
 */
public void setBloat(double aBloat)
{
    if(aBloat==_bloat) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("Bloat", _bloat, _bloat = aBloat, -1); // Set value and fire PropertyChange
    _path = null;
}

/** 
 * Returns a value for the inner radius so the stars look reasonable by default.
 */
public double getOptimalBloat(int npts)
{
    // 5 & 6 sided stars get set so their sides are straight
    if ((npts==5) || (npts==6)) {
        double a = 1.0/(Math.tan(2*Math.PI/_numPoints) * Math.tan(Math.PI/_numPoints) + 1);
        return a / Math.cos(Math.PI/_numPoints);
    }
    // other stars look better somewhat pinched in ( I think )
    return .33;
}

/**
 * Returns whether star/polygon is polygon.
 */
public boolean isPolygon()  { return _starType==POLY; }

/**
 * Sets whether star/polygon is polygon.
 */
public void setIsPolygon(boolean pflag)  { setStarType(pflag? POLY : STAR); }

/**
 * Sets star/polygon type.
 */
public void setStarType(int aType)
{
    if(aType==_starType) return; // If value already set, just return
    repaint(); // Register repaint
    firePropertyChange("Bloat", _starType, _starType = aType, -1); // Set value and fire PropertyChange
    if(_starType==MAGIC) { _numPoints=4; _bloat=.5; } _path=null;
}

public void setStartAngle(double anAngle)  { _startAngle = anAngle; _path = null; }

/**
 * Returns the star path.
 */
public RMPath getPath()  { return _path!=null? _path : (_path=getPathImpl()); }

/**
 * Returns the star path.
 */
private RMPath getPathImpl()
{
    // Create new path
    RMPath path = new RMPath();
    
    // Get Radius for i
    double radii[] = {.5, isPolygon()? .5 : _bloat/2};
    
    // Iterate over points
    for(int i=0, iMax=isPolygon()? _numPoints : _numPoints*2; i<iMax; i++) {
        int ai = i; if(_starType==MAGIC) ai &= ~1;
        double theta = 2*Math.PI*ai/iMax+_startAngle;
        double x = .5+radii[i%2]*Math.cos(theta);
        double y = .5+radii[i%2]*Math.sin(theta);
        if(i==0) path.moveTo(x,y);
        else path.lineTo(x,y);
   }
    
    // force bounds of path to the unit rect
    path.closePath();
    path.moveTo(0,0); path.moveTo(1,1);
    return path;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("star");
    
    // Archive star attributes
    if(_numPoints!=5) e.add("points", _numPoints);
    if(_bloat!=getOptimalBloat(_numPoints)) e.add("bloat", _bloat);
    if(_starType != STAR) e.add("type", _starType);
    if(!RMMath.equals(_startAngle, Math.PI/2)) e.add("startangle", _startAngle);
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive PointCount, Bloat, Type, StartAngle
    setNumPoints(anElement.getAttributeIntValue("points",5));
    if(anElement.hasAttribute("bloat")) setBloat(anElement.getAttributeFloatValue("bloat"));
    if(anElement.hasAttribute("type")) _starType = anElement.getAttributeIntValue("type");
    else setIsPolygon(anElement.getAttributeBoolValue("polygon", false));
    setStartAngle(anElement.getAttributeFloatValue("startangle", (float)Math.PI/2));
    return this;
}

}