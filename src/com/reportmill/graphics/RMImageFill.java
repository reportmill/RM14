package com.reportmill.graphics;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import snap.util.*;

/**
 * This class is used to fill a shape's path with an image.
 */
public class RMImageFill extends RMFill {

    // Image data
    RMImageData        _imageData = RMImageData.EMPTY;
    
    // Whether to tile fill image
    boolean            _tiled = true;
    
    // X location (offset) of image fill
    double             _x;
    
    // Y location (offset) of image fill
    double             _y;
    
    // Rotation of image fill
    double             _roll;
    
    // Scale X of image fill
    double             _scaleX = 1;
    
    // Scale Y of image fill
    double             _scaleY = 1;

    // Constants for image fill attributes
    public static final String ATTRIBUTE_TILED = "tiled";
    public static final String ATTRIBUTE_X = "x";
    public static final String ATTRIBUTE_Y = "y";
    public static final String ATTRIBUTE_ROLL = "roll";
    public static final String ATTRIBUTE_SCALE_X = "scale-x";
    public static final String ATTRIBUTE_SCALE_Y = "scale-y";

/**
 * Creates a plain image fill.
 */
public RMImageFill()  { }

/**
 * Creates an image fill from an image source.
 */
public RMImageFill(Object aSource)  { this(); _imageData = RMImageData.getImageData(aSource); }

/**
 * Returns the image data associated with this image fill.
 */
public RMImageData getImageData()  { return _imageData; }

/**
 * Returns whether to tile fill image.
 */
public boolean isTiled()  { return _tiled; }

/**
 * Sets whether to tile fill image.
 */
public void setTiled(boolean aValue)  { _tiled = aValue; }

/**
 * Returns the X location (offset) of the image fill image.
 */
public double getX()  { return _x; }

/**
 * Returns the Y location (offset) of the image fill image.
 */
public double getY()  { return _y; }

/**
 * Returns the rotation of the image fill image.
 */
public double getRoll()  { return _roll; }

/**
 * Returns the scale x of the image fill image.
 */
public double getScaleX()  { return _scaleX; }

/**
 * Returns the scale y of the image fill image.
 */
public double getScaleY()  { return _scaleY; }

/**
 * Creates a new image fill from this fill, but with a new image.
 */
public RMImageFill deriveFill(RMImageData anImageData)
{
    RMImageFill copy = (RMImageFill)clone();
    copy._imageData = anImageData;
    return copy;
}

/**
 * Creates a new image fill identical to this image fill, but with new value for given attribute.
 */
public RMImageFill deriveFill(String aName, Number aValue)
{
    // Get copy of this image fill, reset named attribute and return
    RMImageFill copy = (RMImageFill)clone();
    if(aName.equals(ATTRIBUTE_TILED)) copy._tiled = aValue.intValue()>0;
    if(aName.equals(ATTRIBUTE_X)) copy._x = aValue.floatValue();
    if(aName.equals(ATTRIBUTE_Y)) copy._y = aValue.floatValue();
    if(aName.equals(ATTRIBUTE_ROLL)) copy._roll = aValue.floatValue();
    if(aName.equals(ATTRIBUTE_SCALE_X)) copy._scaleX = aValue.floatValue();
    if(aName.equals(ATTRIBUTE_SCALE_Y)) copy._scaleY = aValue.floatValue();
    return copy;
}

/**
 * Returns the type of the image for this image fill (gif, jpg, png, etc.).
 */
public String getType()  { return _imageData.getType(); }

/**
 * Returns the actual display width of the image in printer's points using the image DPI if available.
 */
public double getImageWidth()  { return _imageData.getImageWidth(); }

/**
 * Returns the actual display height of the image in printer's points using the image DPI if available.
 */
public double getImageHeight()  { return _imageData.getImageHeight(); }

/**
 * Render this fill in a shape painter.
 */
public void paint(RMShapePainter aPntr, RMShape aShape)
{
    // Copy graphics so we can muck with transform and clip
    aPntr = aPntr.clone();
    
    // Get shape path and apply clip
    RMPath path = aShape.getPathInBounds();
    aPntr.clip(path);
    
    // Get shape bounds
    RMRect bounds = aShape.getBoundsInside();
    
    // If rolled or scaled, translate to shape center, rotate, scale and return
    if(getRoll()!=0 || getScaleX()!=1 || getScaleY()!=1) {
        
        // Get shape width and height
        double width = aShape.getWidth();
        double height = aShape.getHeight();
        
        // Get transform width translate to shape center, rotate and scale, and translate back
        RMTransform t = new RMTransform();
        t.translate(-width/2, -height/2);
        t.rotate(getRoll()); t.scale(getScaleX(), getScaleY());
        t.translate(width/2, height/2);
        
        // Apply transform to graphics
        aPntr.transform(t.awt());
        
        // Transform bounds to enclose rotated and scaled image space
        bounds = t.invert().transform(bounds);
        
        // If not TILE, scale enclosing bounds by image fill scale
        if(!isTiled()) {
            RMTransform t2 = new RMTransform();
            t2.translate(-width/2, -height/2);
            t2.scale(getScaleX(), getScaleY());
            t2.translate(width/2, height/2);
            bounds = t2.transform(bounds);
        }
    }
    
    // If file style tile, iterate over bounds x/y by image width/height and stamp out image
    if(isTiled()) {
        
        // Get image size
        double width = getImageWidth();
        double height = getImageHeight();
        
        // Get starting x and y
        double startX = getX(); while(startX>bounds.x) startX -= width;
        double startY = getY(); while(startY>bounds.y) startY -= height;
        
        // Iterate over shape height and width
        for(double y=startY, yMax=bounds.getMaxY(); y<yMax; y+=height)
            for(double x=startX, xMax=bounds.getMaxX(); x<xMax; x+=width)
                getImageData().paint(aPntr, x, y, width, height);
    }

    // Otherwise, just paint image in bounds
    else getImageData().paint(aPntr, bounds.x + getX(), bounds.y + getY(), bounds.width, bounds.height);
    
    // Dispose of graphics
    aPntr.dispose();
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and super and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    RMImageFill other = (RMImageFill)anObj;
    
    // Check ImageData, FillStyle, X, Y, Roll, ScaleX, ScaleY, ImageMargins
    if(!RMUtils.equals(other._imageData, _imageData)) return false;
    if(other._tiled!=_tiled) return false;
    if(other._x!=_x || other._y!=_y) return false;
    if(other._roll!=_roll) return false;
    if(other._scaleX!=_scaleX || other._scaleY!=_scaleY) return false;
    return true; // Return true since all checks passed
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic fill attributes and set type
    XMLElement e = super.toXML(anArchiver); e.add("type", "image");

    // Archive ImageData
    if(_imageData.getBytes()!=null && _imageData!=RMImageData.EMPTY) {
        String resName = anArchiver.addResource(_imageData.getBytes(), _imageData.getName());
        e.add("resource", resName);
    }
    
    // Archive Tile
    if(!isTiled()) e.add("Tile", _tiled);
    
    // Archive X, Y, Roll, ScaleX, ScaleY
    if(_x!=0) e.add("x", _x);
    if(_y!=0) e.add("y", _y);
    if(_roll!=0) e.add("roll", _roll);
    if(_scaleX!=1) e.add("scale-x", _scaleX);
    if(_scaleY!=1) e.add("scale-y", _scaleY);
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic fill attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive ImageName: get resource bytes, page and set ImageData
    String iname = anElement.getAttributeValue("resource");
    if(iname!=null) {
        byte bytes[] = anArchiver.getResource(iname); // Get resource bytes
        int page = anElement.getAttributeIntValue("page"); // Unarchive page number
        _imageData = RMImageData.getImageData(bytes, page); // Create new image data
    }
    
    // Unarchive Tile, legacy FillStyle (Stretch=0, Tile=1, Fit=2, FitIfNeeded=3)
    if(anElement.hasAttribute("Tile")) setTiled(anElement.getAttributeBooleanValue("Tile"));
    else if(anElement.hasAttribute("fillstyle")) { int fs = anElement.getAttributeIntValue("fillstyle");
        if(fs!=1) setTiled(false); }
    
    // Unarchive X, Y, Roll, ScaleX, ScaleY
    _x = anElement.getAttributeFloatValue("x");
    _y = anElement.getAttributeFloatValue("y");
    _roll = anElement.getAttributeFloatValue("roll");
    _scaleX = anElement.getAttributeFloatValue("scale-x", 1);
    _scaleY = anElement.getAttributeFloatValue("scale-y", 1);
    
    // Return this image fill
    return this;
}

}