package snap.swing;
import com.reportmill.shape.*;
import java.awt.*;
import javax.swing.Icon;

/**
 * An icon to paint a shape.
 */
public class RMShapeIcon implements Icon {

    // The shape
    RMShape          _shape;
    
    // The size
    double           _width, _height;

/**
 * Creates a new ShapeIcon.
 */
public RMShapeIcon(RMShape aShape)  { this(aShape, 0, 0); }

/**
 * Creates a new ShapeIcon.
 */
public RMShapeIcon(RMShape aShape, double aWidth, double aHeight)
{
    // Set shape and size
    setShape(aShape);
    setSize(aWidth, aHeight);
}

/**
 * Creates a shape painter from any objects (assumed to be a document source).
 */
public RMShapeIcon(Object aSource, double aWidth, double aHeight)
{
    // Handle document
    if(aSource instanceof RMDocument)
        setShape(((RMDocument)aSource).getPage(0));
    
    // Handle shape
    else if(aSource instanceof RMShape)
        setShape((RMShape)aSource);
    
    // Handle anything else
    else try {
        RMDocument document = new RMDocument(aSource);
        document.getPage(0).setPaintBackground(false);
        setShape(document.getPage(0));
    }
    
    // Catch exceptions
    catch(Exception e) { System.err.println("RMShapeIcon: Can't set source"); }
    
    // Set size
    setSize(aWidth, aHeight);
}

/**
 * Returns the shape.
 */
public RMShape getShape()  { return _shape; }

/**
 * Sets the shape.
 */
public void setShape(RMShape aShape)  { _shape = aShape; }

/**
 * Returns width.
 */
public int getIconWidth()  { return (int)Math.ceil(_width); }

/**
 * Returns height.
 */
public int getIconHeight()  { return (int)Math.ceil(_height); }

/**
 * Sets the size.
 */
public void setSize(double aWidth, double aHeight)
{
    _width = aWidth>0? aWidth : getShape().getBoundsMarked().getWidth();
    _height = aHeight>0? aHeight : getShape().getBoundsMarked().getHeight();
}

/**
 * Paints the icon.
 */
public void paintIcon(Component aComponent, Graphics aGraphics, int x, int y)
{
    // If shape is null, just return
    if(_shape==null) return;
    
    // Paint shape
    RMShapePainterJ2D pntr = new RMShapePainterJ2D((Graphics2D)aGraphics);
    pntr.setShrinkToFit(true);
    pntr.setBounds(0, 0, _width, _height);
    aGraphics.translate(x, y);
    pntr.paintShape(getShape());
    aGraphics.translate(-x, -y);
}

}