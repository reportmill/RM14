package com.reportmill.graphics;
import com.reportmill.shape.RMShape;
import java.awt.Shape;
import snap.util.*;

/**
 * This stroke subclass strokes the rectangular border of a given shape, with option include/exclude
 * individual sides.
 */
public class RMBorderStroke extends RMStroke {

    // Whether to show left border
    boolean         _showLeft = true;
    
    // Whether to show right border
    boolean         _showRight = true;
    
    // Whether to show top border
    boolean         _showTop = true;
    
    // Whether to show left border
    boolean         _showBottom = true;
    
/**
 * Returns whether to show left border.
 */
public boolean getShowLeft()  { return _showLeft; }
    
/**
 * Sets whether to show left border.
 */
public void setShowLeft(boolean aValue)
{
    firePropertyChange("ShowLeft", _showLeft, _showLeft = aValue, -1);
}
    
/**
 * Returns whether to show right border.
 */
public boolean getShowRight()  { return _showRight; }
    
/**
 * Sets whether to show right border.
 */
public void setShowRight(boolean aValue)
{
    firePropertyChange("ShowRight", _showRight, _showRight = aValue, -1);
}
    
/**
 * Returns whether to show top border.
 */
public boolean getShowTop()  { return _showTop; }
    
/**
 * Sets whether to show top border.
 */
public void setShowTop(boolean aValue)
{
    firePropertyChange("ShowTop", _showTop, _showTop = aValue, -1);
}
    
/**
 * Returns whether to show bottom border.
 */
public boolean getShowBottom()  { return _showBottom; }
    
/**
 * Sets whether to show bottom border.
 */
public void setShowBottom(boolean aValue)
{
    firePropertyChange("ShowBottom", _showBottom, _showBottom = aValue, -1);
}

/**
 * Returns the path to be stroked, transformed from the input path.
 */
public Shape getStrokePath(RMShape aShape)
{
    // If showing all borders, just return bounds
    if(getShowLeft() && getShowRight() && getShowTop() && getShowBottom())
        return aShape.getBoundsInside();
    
    // Otherwise, build path
    RMPath path = new RMPath();
    
    // Add top
    if(getShowTop()) {
        path.moveTo(0,0);
        path.lineTo(aShape.getWidth(), 0);
    }
    
    // Add right
    if(getShowRight()) {
        if(!getShowTop()) path.moveTo(aShape.getWidth(), 0);
        path.lineTo(aShape.getWidth(), aShape.getHeight());
    }
    
    // Add bottom
    if(getShowBottom()) {
        if(!getShowRight()) path.moveTo(aShape.getWidth(), aShape.getHeight());
        path.lineTo(0, aShape.getHeight());
    }
    
    // Add left
    if(getShowLeft()) {
        if(!getShowBottom()) path.moveTo(0, aShape.getHeight());
        path.lineTo(0,0);
    }
    
    // Return path
    return path;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, super, class and get other
    if(anObj==this) return true;
    if(!super.equals(anObj)) return false;
    if(!(anObj instanceof RMBorderStroke)) return false;
    RMBorderStroke other = (RMBorderStroke)anObj;
    
    // Check ShowLeft, ShowRight, ShowTop, ShowBottom
    if(other._showLeft!=_showLeft) return false;
    if(other._showRight!=_showRight) return false;
    if(other._showTop!=_showTop) return false;
    if(other._showBottom!=_showBottom) return false;
    return true; // Return true since all checks passed
}
        
/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic stroke attributes
    XMLElement e = super.toXML(anArchiver); e.add("type", "border");
    
    // Archive ShowLeft, ShowRight, ShowTop, ShowBottom
    if(!getShowLeft()) e.add("show-left", false);
    if(!getShowRight()) e.add("show-right", false);
    if(!getShowTop()) e.add("show-top", false);
    if(!getShowBottom()) e.add("show-bottom", false);
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic stroke attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive ShowLeft, ShowRight, ShowTop, ShowBottom
    if(anElement.hasAttribute("show-left")) setShowLeft(anElement.getAttributeBoolValue("show-left"));
    if(anElement.hasAttribute("show-right")) setShowRight(anElement.getAttributeBoolValue("show-right"));
    if(anElement.hasAttribute("show-top")) setShowTop(anElement.getAttributeBoolValue("show-top"));
    if(anElement.hasAttribute("show-bottom")) setShowBottom(anElement.getAttributeBoolValue("show-bottom"));
        
    // Return stroke
    return this;
}

}