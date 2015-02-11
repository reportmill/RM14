package snap.gfx;

/**
 * Represents a rectangle - a Quadrilateral with parallel sides.
 */
public class Rect extends Quad {

/**
 * Creates a new rect.
 */
public Rect(double aX, double aY, double aW, double aH)
{
    _x1 = _x4 = aX; _y1 = _y2 = aY; _x2 = _x3 = aX + aW; _y3 = _y4 = aY + aH; _width = aW; _height = aH; 
}

}
