package com.reportmill.text;
import com.reportmill.graphics.*;
import java.awt.geom.*;

/**
 * Performs text layout in a path.
 */
public class RMTextLayoutInPath extends RMTextLayout {

    // The path associated with the text bounds
    RMPath            _path;
    
    // The amount to pad next line by
    int               _pad;

/**
 * Creates a new RMTextLayoutInPath for given path.
 */
public RMTextLayoutInPath(RMPath aPath)  { setPath(aPath); }

/**
 * Returns the path that the text should wrap to.
 */
public RMPath getPath()  { return _path; }

/**
 * Sets the path that the text should wrap to.
 */
public void setPath(RMPath aPath)  { _path = aPath; }

/**
 * Returns what this line thinks is the next line's x.
 */
public Point2D getNextLineStartPoint(RMXStringRun aRun)
{
    // If no path, do normal version
    if(getPath()==null) return super.getNextLineStartPoint(aRun);
    
    // Get last line
    RMTextLine lastLine = getLineLast();
    double nextLineLineHeight = aRun.getFont().getLineHeight();
    
    // Declare variables
    double nextX1 = 0, nextX2 = 0;
    
    // If last line is available, see if there is a region to the right of that line
    if(lastLine!=null) {
    
        // Get current x max, y min/max points
        double x = lastLine.getX() + 1;
        double y1 = _point.y = lastLine.getY();
        double y2 = y1 + nextLineLineHeight;
    
        // Get current line's max x for y min/max
        double maxX1 = getHitX(x, y1) + .1f;
        double maxX2 = getHitX(x, y2) + .1f;
    
        // Get next line's possible x for y min/max
        nextX1 = getHitX(maxX1, y1);
        nextX2 = getHitX(maxX2, y2);
    
        // If either maxX is greater than opposing nextX, reset it's corresponds nextX
        if(maxX2>nextX1 && maxX2<java.lang.Float.MAX_VALUE) nextX2 = nextX1;
        if(maxX1>nextX2 && maxX1<java.lang.Float.MAX_VALUE) nextX1 = nextX2;
    }

    // If either nextX at layout max x, try with Pad
    if(lastLine==null || nextX1>=getMaxX() || nextX2>=getMaxX()) {
        
        // Get line x and y min/max
        double x = getX() - 100, lastLineY = lastLine!=null? lastLine.getY() + lastLine.getLineAdvance() : getY();
        double y1 = _point.y = lastLineY + _pad;
        double y2 = y1 + nextLineLineHeight;
        
        // Get line x of impact of line top/bottom with path
        nextX1 = getHitX(x, y1);
        nextX2 = y2>getMaxY()? nextX1 : getHitX(x, y2);
    }
    
    // Get the greater of next line's hits and add indent (and Layout.X as a hack to get Left Margin inset)
    _point.x = Math.max(nextX1, nextX2) + getIndent(aRun) + getX();
    if(_point.x>=getMaxX()) _point.x = getX();

    // Return point
    return _point;
}

/**
 * Returns the x hit for the horizontal line from given point to positive infinity.
 */
private double getHitX(double anX, double aY)
{
    // Get line from point to layout's right edge
    RMLine line = new RMLine(anX, aY, getMaxX()+100, aY);
    
    // Get hit info for line against path and, if null, just return float's max value
    RMHitInfo hinfo = getPath().getHitInfo(line, true);
    if(hinfo==null)
        return java.lang.Float.MAX_VALUE;
    
    // Get float of hit
    double hitx = line.getPoint(hinfo._r).x;
    return hitx;
}

/**
 * Override to create text line in path.
 */
protected RMTextLine createLine()  { return new RMTextLineInPath(); }

/**
 * A text line in path.
 */
private class RMTextLineInPath extends RMTextLine {
    
    /** Returns the max x position possible for layout's path. */
    public double getHitMaxX()
    {
        // Get parent layout
        RMTextLayoutInPath layout = (RMTextLayoutInPath)getLayout();

        // If no path, do normal version
        if(layout.getPath()==null) return super.getHitMaxX();
        
        // Get line x and y min/max
        double x1 = getX(), x2 = super.getHitMaxX();
        double y1 = getY(), y2 = getMaxY();
        
        // Get line x of impact of line top/bottom with path
        double lx1 = layout.getHitX(x1, y1);
        double lx2 = layout.getHitX(x1, y2);
        
        // Return the lesser of the two values
        double hmx = Math.min(lx1, lx2); hmx = Math.min(hmx, x2);
        return hmx;
    }
    
    /** Rewind last word from line. */
    protected int deleteFromLastWord(CharSequence anInput, int aStart)
    {
        // Get parent layout
        RMTextLayoutInPath layout = (RMTextLayoutInPath)getLayout();

        // Do normal version (just return if no path or no LongWordFound)
        int value = super.deleteFromLastWord(anInput, aStart);
        if(layout.getPath()==null || !layout.getLongWordFound())
            return value;
        
        // Reset long word found
        layout._longWordFound = false;
            
        // Rewind back to line start
        int length = length();
        _layout.deleteChars(_start);
            
        // Drop line down by one
        _pad++;
        
        // Return deleted chars
        return length;
    }
    
    /** Override to reset pad. */
    public void setLocked(boolean aFlag)  { super.setLocked(aFlag); if(length()>0) _pad = 0; }
}

}