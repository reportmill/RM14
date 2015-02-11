package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import java.awt.event.*;
import java.util.*;
import snap.swing.Swing;
import snap.util.*;

/**
 * This class manages a list of children and transforms them to 3D representations for display based on
 * X and Y axis rotations.
 * 
 * ReportMill 3D conventions:
 * 
 *   Coordinate system: Right handed (not left handed)
 *   Polygon front: Right hand rule (counter-clockwise defined polygons face forward)
 *   Transforms: Row major notation (as opposed to column major, points are assumed row vectors) 
 */
public class RMScene3D extends RMParentShape {
    
    // Depth
    double     _depth = 40;
    
    // Rotation around y axis
    double     _yaw = 0;
    
    // Rotation around x axis
    double     _pitch = 0;
    
    // Rotation around z axis
    double     _roll = 0;
    
    // Offset from z axis
    double     _offsetZ = 0;
    
    // Perspective
    double     _focalLength = 60*72;
    
    // Whether to do simple 3d rendering effect by skewing geometry a little bit
    boolean    _pseudo3D;
    
    // The skew in radians along x axis when doing pseudo 3d
    double     _pseudoSkewX;
    
    // The skew in radians along y axis when doing pseudo 3d
    double     _pseudoSkewY;
    
    // Coefficient of ambient reflection for shading
    double     _ka = .6f;
    
    // Coefficient of diffuse reflection for shading
    double     _kd = .5f;
    
    // Camera
    RMVector3D _camera = new RMVector3D(0, 0, 1);
    
    // Lights
    RMVector3D _light = new RMVector3D(0, 0, -1).normalize();
    
    // List of real child shapes
    List <RMShape> _shapes = new ArrayList();
    
    // Mouse drag variable - mouse drag last point
    RMPoint    _pointLast;
    
    // Mouse drag variable - whether in mouse drag loop
    boolean    _valueIsAdjusting = false;

    // used for shift-drag to indicate which axis to constrain rotation to
    int        _dragConstraint;
    
    // Constants for mouse drag constraints
    public final int CONSTRAIN_NONE = 0;
    public final int CONSTRAIN_PITCH = 1;
    public final int CONSTRAIN_YAW = 2;
    
/**
 * Returns the depth of the scene.
 */
public double getDepth()  { return _depth; }

/**
 * Sets the depth of the scene.
 */
public void setDepth(double aValue)
{
    if(aValue==_depth) return;
    repaint();
    firePropertyChange("Depth", _depth, _depth = aValue, -1);
    relayout();
}

/**
 * Returns the scene's origin.
 */
public RMPoint3D getOrigin()  { return new RMPoint3D(getWidth()/2, getHeight()/2, getDepth()/2); }

/**
 * Returns the rotation about the Y axis in degrees.
 */
public double getYaw()  { return _yaw; }

/**
 * Sets the rotation about the Y axis in degrees.
 */
public void setYaw(double aValue)
{
    if(aValue==_yaw) return;
    repaint();
    firePropertyChange("Yaw", _yaw, _yaw = aValue, -1);
    relayout();
}

/**
 * Returns the rotation about the X axis in degrees.
 */
public double getPitch()  { return _pitch; }

/**
 * Sets the rotation about the X axis in degrees.
 */
public void setPitch(double aValue)
{
    if(aValue==_pitch) return;
    repaint();
    firePropertyChange("Pitch", _pitch, _pitch = aValue, -1);
    relayout();
}

/**
 * Returns the rotation about the Z axis in degrees.
 */
public double getRoll3D()  { return _roll; }

/**
 * Sets the rotation about the Z axis in degrees.
 */
public void setRoll3D(double aValue)
{
    if(aValue==_roll) return;
    repaint();
    firePropertyChange("Roll3D", _roll, _roll = aValue, -1);
    relayout();
}

/**
 * Returns the focal length of the camera (derived from the field of view and with view size).
 */
public double getFocalLength()  { return _focalLength; }

/**
 * Sets the focal length of the camera. Two feet is normal (1728 points).
 */
public void setFocalLength(double aValue)
{
    if(aValue==_focalLength) return;
    repaint();
    firePropertyChange("FocalLength", _focalLength, _focalLength = aValue, -1);
    relayout();
}

/**
 * Returns the Z offset of the scene (for zooming).
 */
public double getOffsetZ()  { return _offsetZ; }

/**
 * Sets the Z offset of the scene (for zooming).
 */
public void setOffsetZ(double aValue)
{
    if(aValue==_offsetZ) return;
    repaint();
    firePropertyChange("OffsetZ", _offsetZ, _offsetZ = aValue, -1);
    relayout();
}

/**
 * Returns whether scene is rendered in pseudo 3d.
 */
public boolean isPseudo3D()  { return _pseudo3D; }

/**
 * Sets whether scene is rendered in pseudo 3d.
 */
public void setPseudo3D(boolean aFlag)
{
    if(_pseudo3D==aFlag) return;
    repaint();
    firePropertyChange("Pseudo3D", _pseudo3D, _pseudo3D = aFlag, -1);
    relayout();
}

/**
 * Returns the skew angle for X by Z.
 */
public double getPseudoSkewX()  { return _pseudoSkewX; }

/**
 * Sets the skew angle for X by Z.
 */
public void setPseudoSkewX(double anAngle)
{
    if(anAngle==_pseudoSkewX) return;
    repaint();
    firePropertyChange("PseudoSkewX", _pseudoSkewX, _pseudoSkewX = anAngle, -1);
    relayout();
}

/**
 * Returns the skew angle for Y by Z.
 */
public double getPseudoSkewY()  { return _pseudoSkewY; }

/**
 * Sets the skew angle for Y by Z.
 */
public void setPseudoSkewY(double anAngle)
{
    if(anAngle==_pseudoSkewY) return;
    repaint();
    firePropertyChange("PseudoSkewY", _pseudoSkewY, _pseudoSkewY = anAngle, -1);
    relayout();
}

/**
 * Returns the field of view of the camera (derived from focalLength).
 */
public double getFieldOfView()
{
    double height = Math.max(getWidth(), getHeight());
    double fieldOfView = Math.toDegrees(Math.atan(height/(2*_focalLength)));
    return fieldOfView*2;
}

/**
 * Sets the field of view of the camera.
 */
public void setFieldOfView(double aValue)
{
    double height = Math.max(getWidth(), getHeight());
    double tanTheta = Math.tan(Math.toRadians(aValue/2));
    double focalLength = height/(2*tanTheta);
    setFocalLength(focalLength);
}

/**
 * Sets some reasonable default view settings.
 */
public void setDefaultViewSettings()
{
    // If pseudo 3d, set good defaults
    if(isPseudo3D()) {
        setPseudoSkewX(.3f);
        setPseudoSkewY(-.25f);
        setDepth(20);
        setFocalLength(60*72);
    }
    
    // If true 3d, set good 3d defaults
    else {
        setYaw(23);
        setPitch(12);
        setDepth(100);
        setFocalLength(8*72);
    }    
}

/**
 * Returns the camera as a vector.
 */
public RMVector3D getCamera()  { return _camera; }

/**
 * Returns the scene light as a vector.
 */
public RMVector3D getLight()  { return _light; }

/**
 * Returns the number of shapes in the shape list.
 */
public int getShapeCount()  { return _shapes.size(); }

/**
 * Returns the specific shape at the given index from the shape list.
 */
public RMShape getShape(int anIndex)  { return _shapes.get(anIndex); }

/**
 * Adds a shape to the end of the shape list.
 */
public void addShape(RMShape aShape)  { addShape(aShape, _shapes.size()); }

/**
 * Adds a shape to the shape list at the given index.
 */
public void addShape(RMShape aShape, int anIndex)
{
    // Add shape to list and reset autosizing so shape will stretch
    _shapes.add(anIndex, aShape);
    aShape.setLayoutInfo("-~-,-~-");

    // Fire property change and revalidate
    firePropertyChange("Shape", null, aShape, anIndex);
    relayout();
}

/**
 * Removes the shape at the given index from the shape list.
 */
public void removeShape(int anIndex)
{
    // Remove shape
    RMShape shape = _shapes.remove(anIndex);
    
    // Fire property change and revalidate
    firePropertyChange("Shape", shape, null, anIndex);
    relayout();
}

/**
 * Returns the transform 3d for the scene's camera.
 */
public RMTransform3D getTransform3D()
{
    // Create base transform
    RMTransform3D t = new RMTransform3D();
    
    // If pseudo 3d, just return skewed transform
    if(isPseudo3D()) {
        t.skew(_pseudoSkewX, _pseudoSkewY);
        t.perspective(getFocalLength());
        return t;
    }
    
    // Normal transform: translate about center, rotate X & Y, translate by Z, perspective, translate back
    RMPoint3D origin = getOrigin();
    t.translate(-origin.x, -origin.y, -origin.z);
    t.rotate(_pitch, _yaw, _roll); //t.rotateY(_yaw).rotateX(_pitch).rotateZ(_roll);
    t.translate(0, 0, getOffsetZ());
    t.perspective(getFocalLength());
    t.translate(origin.x, origin.y, origin.z);
    
    // Return the transform
    return t;
}

/**
 * Rebuilds 3D representation of shapes from shapes list.
 */
protected void layoutChildren()
{
    // Remove all existing children
    removeChildren();
    
    // Iterate over shapes and add them as 3D
    for(int i=0, iMax=getShapeCount(); i<iMax; i++) { RMShape child = getShape(i);
        addChild3D(child, 0, getDepth(), false); }
    
    // Resort shapes
    resort();
}

/**
 * Adds a given shape in 3D.
 * FixEdges flag indicates wheter to stroke polygons created during extrusion, to try to make them mesh better.
 * The axisAlign flag can be set to true to make sure the shape's coordinate system is aligned with the screen. 
 */
protected RMShape3D addChild3D(RMShape aShape, double z1, double z2, boolean fixEdges)
{
    // Get the camera transform & optionally align it to the screen
    RMTransform3D xform = getTransform3D();
        
    // If the shape is already a 3D shape, just apply new camera transform
    if(aShape instanceof RMShape3D) { RMShape3D shape3d = (RMShape3D)aShape;
        
        // Get path clone
        RMPath3D path3d = (RMPath3D)shape3d.getPath3D().clone();
        
        // Transform clone by scene transform
        path3d.transform(xform);
        
        // Backface culling : Only add paths that face the camera
        if(!path3d.getNormal().isAligned(getCamera(), true)) {
            RMShape3D shape3d2 = new RMShape3D(path3d);
            shape3d2.setColor(shape3d.getColor());
            addChild(shape3d2);
            return shape3d2;
        }
        
        // Return null
        return null;
    }
    
    // Store current child index so we can return first newly added child
    int childCount = getChildCount();
    
    // Get shape path
    RMPath shapePath = aShape.getPathInBounds();
    
    // Flatten path to remove curve tos
    shapePath = RMPathUtils.getPathFlattened(shapePath);
    
    // Transform path to shape parent coords
    shapePath.transformBy(aShape.getTransform());
    
    // If aShape is text, add shape3d for background and add shape3d for char path shape
    if(aShape instanceof RMTextShape) { RMTextShape text = (RMTextShape)aShape;
        
        // If text draws fill or stroke, add child for background
        if(text.getFill()!=null || text.getStroke()!=null) {
            RMShape background = new RMPolygonShape(aShape.getPath()); // Create background shape from text
            background.copyShape(aShape);
            addChild3D(background, z1+.1f, z2, fixEdges); // Add background shape
        }
        
        // Get shape for char paths
        RMShape chars = RMTextShapeUtils.getTextPathShape(text);
        
        // If not null, add shape3d for char path shape
        if(chars!=null)
            addChild3D(chars, z1, z1, fixEdges);
    }
    
    // If only one face is requested do simple 3D conversion
    else if(z1>=z2) {
        
        // Get path3d for shape path
        RMPath3D path3d = new RMPath3D(shapePath, z1);

        // Transform path for camera
        path3d.transform(xform);

        // If path faces in same direction as camera (as opposed to facing it), reverse it
        if(!aShape.getColor().equals(RMColor.black))
            if(path3d.getNormal().isAligned(getCamera(), true))
                path3d.reverse();

        // Create 3D shape from path, set fill, stroke, opacity and add
        RMShape3D shape3d = new RMShape3D(path3d);
        setFillAndStroke(shape3d, aShape.getFill(), aShape.getStroke(), aShape.getEffect());
        shape3d.setOpacity(aShape.getOpacity());
        addChild(shape3d);
    }
    
    // If full extrusion is requested, do it
    else {
        
	    // Get front, back and side faces for extruded path
        List <RMPath3D> paths = RMPath3DUtils.getPaths(shapePath, z1, z2, fixEdges ? .001f : 0f);
	
        // Save away original size, to identify the front face
        int frontface = paths.size()-1;
        
	    // Transform paths by scene's rotations and remove those facing away from camera vector
	    for(int j=paths.size()-1; j>=0; j--) { RMPath3D path = paths.get(j);
            
            // Transform path by camera transform
            path.transform(xform);
	        
	        // If path faces in same direction as camera (as opposed to facing it), remove it
            // This is an extra, unnecessary step, since paths is a temporary list that isn't referenced
            // anywhere else.  Could just negate the condition and use the else clause.
            if(shapePath.isClosed() && path.getNormal().isAligned(getCamera(), true))
                paths.remove(j);
	        
	        // If polygon surface normal points toward camera, create shape3d, set fill/stroke and add shape
	        else {
                
                // Create new 3d shape for path3d
	            RMShape3D shape3d = new RMShape3D(path);
                
                // Set fill and stroke colors
                setFillAndStroke(shape3d, aShape.getFill(), aShape.getStroke(), null);
                shape3d.setOpacity(aShape.getOpacity());
                
	            // Set the stroke for side faces to a special stroke to stitch up the abutting polys
                if(fixEdges && (j!=0) && (j!=frontface)) {
                    RMStroke stroke = new RMStroke(shape3d.getColor(),1.5f);
                    shape3d.setStroke(stroke);
                }
                
                // Add 3d shape
	            addChild(shape3d);
	        }
	    }
    }
    
    // Returns the newly added shape3d
    return childCount<getChildCount()? (RMShape3D)getChild(childCount) : null;
}

/**
 * Sets the fill and stroke of a 3D shape from a 2D shape.
 */
public void setFillAndStroke(RMShape3D aShape3D, RMFill aFill, RMStroke aStroke, RMEffect anEffect)
{
    if(aFill!=null) setColor(aShape3D, aFill.getColor()); // Set shape3D fill to fill from shape
    else aShape3D.setFill(null);
    if(aStroke!=null) aShape3D.setStroke(aStroke.clone()); // Set shape3D stroke to stroke from shape
    if(anEffect!=null) aShape3D.setEffect(anEffect.clone()); // Set shape3D effect to effect from shape
}

/**
 * Sets the color for a 3d shape from a base color.
 */
public void setColor(RMShape3D aShape3D, RMColor aColor)
{
    // Get shape3d path3d
    RMPath3D path = aShape3D.getPath3D();
    
    // Get shape3d path normal
    RMVector3D normal = path.getNormal();
    
    // If shape is facing away from camera, negate normal
    if(normal.isAligned(getCamera(), true))
        normal.negate();
        
    // Get dot product of shape3d surface normal and light source vector
    double normalDotLight = normal.getDotProduct(getLight());
    
    // Calculate color components based on original color, surface normal, reflection constants and light source
    double r = aColor.getRed()*_ka + aColor.getRed()*_kd*normalDotLight; r = Math.min(r,1);
    double g = aColor.getGreen()*_ka + aColor.getGreen()*_kd*normalDotLight; g = Math.min(g,1);
    double b = aColor.getBlue()*_ka + aColor.getBlue()*_kd*normalDotLight; b = Math.min(b,1);
    
    // Set new color
    aShape3D.setColor(new RMColor(r, g, b, aColor.getAlpha()));    
}

/**
 * Resorts child shapes from back to front.
 */
public void resort()
{
    // Get list of children (just return if null)    
    List <RMShape3D> list = (List)_children; if(list==null) return;
    
    // Sort children from front to back with simple Z based sort
    RMSort.sort(list, new RMSort("getPath3D.getZMin"));

    // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
    for(int i=list.size()-1; i>=0; i--) { RMShape3D poly0 = list.get(i), poly1 = poly0;
        
        // Iterate over remaining shapes
        for(int j=0, jMax=i; j<jMax; j++) { RMShape3D poly2 = list.get(j); if(poly2==poly1) continue;
        
            // Get poly paths
            RMPath3D path1 = poly1.getPath3D();
            RMPath3D path2 = poly2.getPath3D();

            if(path1.getZMin()>=path2.getZMax()) continue;
            if(path1.getXMax()<=path2.getXMin() || path1.getXMin()>=path2.getXMax()) continue;
            if(path1.getYMax()<=path2.getYMin() || path1.getYMin()>=path2.getYMax()) continue;
            
            // Test poly against poly2
            int comp1 = path1.comparePlane(path2);
            
            // If the polygons are on the same plane, they don't overlap.
            if(comp1==RMSort.ORDER_SAME || comp1==RMSort.ORDER_DESCEND) continue;
            
            int comp2 = path2.comparePlane(path1);
            if(comp2==RMSort.ORDER_ASCEND) continue;
            
            if(!path1.getPath().intersects(path2.getPath(),0)) continue;
            
            // If all five tests fail, try next polygon up from poly1
            int index = RMListUtils.indexOfId(list, poly1);
            
            if(index==0) { //System.out.println("i is " + i); // There is still a bug - this shouldn't happen
                poly1 = list.get(i); j = jMax; continue; }
            
            poly1 = list.get(RMListUtils.indexOfId(list, poly1)-1);
            
            j = -1;
        }
        
        // Move poly
        if(poly1!=poly0) {
            RMListUtils.removeId(list, poly1); list.add(i, poly1); }
    }

    // Reverse child list so it is back to front (so front most shape will be drawn last)
    Collections.reverse(list);
}

/**
 * Override to make all 3D children show.
 */
protected boolean isShowing(RMShape aChild)  { return true; }

/**
 * Override to indicate that scene children are unhittable.
 */
public boolean isHittable(RMShape aChild)  { return false; }

/** Viewer method. */
public boolean acceptsMouse()  { return true; }

/**
 * Viewer method.
 */
public void mousePressed(RMShapeMouseEvent anEvent)
{
    _pointLast = anEvent.getPoint2D(); // Set last point to event location in scene coords
    _valueIsAdjusting = true; // Set value adjusting
    _dragConstraint = CONSTRAIN_NONE; // Set drag constraint
}

/**
 * Viewer method.
 */
public void mouseDragged(RMShapeMouseEvent anEvent)
{
    // Register for repaint
    repaint();
    
    // Get event location in this scene shape coords
    RMPoint point = anEvent.getPoint2D();

    // If pseudo3d, set skew using event offset
    if(isPseudo3D()) {
        setPseudoSkewX(getPseudoSkewX() + (point.x - _pointLast.x)/100);
        setPseudoSkewY(getPseudoSkewY() + (point.y - _pointLast.y)/100);
    }
    
    // If right-mouse, muck with perspective
    else if(Swing.isMetaDown())
        setOffsetZ(getOffsetZ() + _pointLast.y - point.y);
    
    // Otherwise, just do pitch and roll
    else {
        
        // Shift-drag constrains to just one axis at a time
        if((anEvent.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
            
            // If no constraint
            if(_dragConstraint==CONSTRAIN_NONE) {
                if(Math.abs(point.y-_pointLast.y)>Math.abs(point.x-_pointLast.x)) 
                    _dragConstraint = CONSTRAIN_PITCH;
                else _dragConstraint = CONSTRAIN_YAW;
            }
            
            // If Pitch constrained
            if(_dragConstraint==CONSTRAIN_PITCH)
                point.x = _pointLast.x;
            
            // If Yaw constrained
            else point.y = _pointLast.y;
        }
        
        // Set pitch & yaw
        setPitch(getPitch() + (point.y - _pointLast.y)/1.5f);
        setYaw(getYaw() - (point.x - _pointLast.x)/1.5f);
    }
    
    // Set last point
    _pointLast = point;
}

/**
 * Viewer method.
 */
public void mouseReleased(RMShapeMouseEvent anEvent)  { _valueIsAdjusting = false; repaint(); relayout(); }

/**
 * Returns whether scene3d is being re-oriented.
 */
public boolean getValueIsAdjusting()  { return _valueIsAdjusting; }

/**
 * Copy 3D attributes only.
 */
public void copy3D(RMScene3D aScene3D)
{
    setDepth(aScene3D.getDepth());
    setYaw(aScene3D.getYaw());
    setPitch(aScene3D.getPitch());
    setRoll(aScene3D.getRoll());
    setFocalLength(aScene3D.getFocalLength());
    setOffsetZ(aScene3D.getOffsetZ());
    setPseudo3D(aScene3D.isPseudo3D());
    setPseudoSkewX(aScene3D.getPseudoSkewX());
    setPseudoSkewY(aScene3D.getPseudoSkewY());
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("scene3d");
    
    // Archive the 2d children: create element for shape, iterate over shapes and add
    if(getShapeCount()>0) {
        XMLElement shapesXML = new XMLElement("shapes");
        for(int i=0, iMax=getShapeCount(); i<iMax; i++)
            shapesXML.add(anArchiver.toXML(getShape(i)));
        e.add(shapesXML);
    }
    
    // Archive Depth, Yaw, Pitch, Roll, FocalLength, Offset3D
    if(_depth!=0) e.add("depth", _depth);
    if(_yaw!=0) e.add("yaw", _yaw);
    if(_pitch!=0) e.add("pitch", _pitch);
    if(_roll!=0) e.add("zroll", _roll);
    if(_focalLength!=60*72) e.add("focal-length", _focalLength);
    if(_offsetZ!=0) e.add("offset-z", _offsetZ);
    
    // Archive Pseudo3D
    if(isPseudo3D()) {
        e.add("pseudo", true);
        e.add("pseudo-skew-x", getPseudoSkewX());
        e.add("pseudo-skew-y", getPseudoSkewY());
    }
        
    // Return xml element
    return e;
}

/**
 * XML archival of children - overrides shape implementation to suppress archival of generated 3D shapes.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement) { }

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive Depth, Yaw, Pitch, Roll, FocalLength, OffsetZ
    setDepth(anElement.getAttributeFloatValue("depth"));
    setYaw(anElement.getAttributeFloatValue("yaw"));
    setPitch(anElement.getAttributeFloatValue("pitch"));
    setRoll3D(anElement.getAttributeFloatValue("zroll"));
    setFocalLength(anElement.getAttributeFloatValue("focal-length", 60*72));
    setOffsetZ(anElement.getAttributeFloatValue("offset-z"));
    
    // Unarchive Pseudo3D
    _pseudo3D = anElement.getAttributeBoolValue("pseudo", false);
    setPseudoSkewX(anElement.getAttributeFloatValue("pseudo-skew-x"));
    setPseudoSkewY(anElement.getAttributeFloatValue("pseudo-skew-y"));
    
    // Unarchive the 2d children
    XMLElement shapesXML = anElement.get("shapes");
    if(shapesXML!=null)
        for(int i=0, iMax=shapesXML.size(); i<iMax; i++)
            addShape((RMShape)anArchiver.fromXML(shapesXML.get(i), this));
}

/**
 * RMPolygon subclass that encapsulates a Path3D.
 */
public static class RMShape3D extends RMPolygonShape {

    // The path 3d used to describe this shape 3d
    RMPath3D _path3d;
    
    /** Creates a new shape 3d from the given path3d. */
    public RMShape3D(RMPath3D aPath3D) { setPath3D(aPath3D); }
    
    /** Returns the path3D for this shape. */
    public RMPath3D getPath3D()  { return _path3d; }
    
    /** Sets the path3d for this shape. */
    public void setPath3D(RMPath3D aPath3D)
    {
        _path3d = aPath3D;
        RMPath path = aPath3D.getPath();
        setBounds(path.getBounds2D());
        setPath(path.getPathInRect(new RMRect(0, 0, path.getWidth(), path.getHeight())));
    }
}

}