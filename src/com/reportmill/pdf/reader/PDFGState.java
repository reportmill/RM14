package com.reportmill.pdf.reader;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.color.*;

public class PDFGState implements Cloneable {
    
    // The current point
    Point2D.Float  cp = new Point2D.Float();
    
    // The current transform
    AffineTransform      trans = new AffineTransform();
    
    // The current color
//    Color          color = Color.black;
    Paint          color = Color.black;
    
    // The current color space
    ColorSpace     colorSpace;
    
    // The current color rendering intent
    int            renderingIntent = ColorFactory.RelativeColorimetricIntent;
    
    // The current stroke color
    Color          scolor = Color.black;
    
    // The current stroke color space
    ColorSpace     scolorSpace;
    
    // The transparency parameters
    int            blendMode = ColorFactory.NormalBlendMode;
    boolean        alphaIsShape = false;
    float          salpha=1; // stroke alpha
    float          alpha=1;  // non-stroke alpha
    Object         softMask = null;
    // Composites that performs the operation described above
    Composite      composite = AlphaComposite.SrcOver;
    Composite      scomposite = AlphaComposite.SrcOver;
    
    // The current stroke parameters
    float          lineWidth = 1;
    int            lineCap = 0;
    int            lineJoin = 0;
    float          miterLimit = 10;
    float          lineDash[] = null;
    float          dashPhase = 0;
    float          flatness = 0;
    // A Stroke representation of the above
    Stroke         lineStroke = new BasicStroke(1f,
                                       BasicStroke.CAP_SQUARE,
                                       BasicStroke.JOIN_MITER,
                                       10f);
    
    // The clipping path
    GeneralPath    clip = null;
        
    // The current font dictionary
    Map            font;
    
    // The current font size
    float          fontSize = 12;
    
    // The current text character spacing
    float          tcs = 0;
    
    // The current text word spacing
    float          tws = 0;
    
    // The current text leading
    float          tleading = 0;
    
    // The curent text rise
    float          trise = 0;
    
    // Text horizontal scale factor (in PDF "Tz 100" means scale=1)
    float          thscale = 1;
    
    // The text rendering mode
    int            trendermode=0;
    
    // The text knockout
    //   false indicates individual glyphs in a text object get composited on top
    //   of each other.  true indicates the entire text object gets composited as one group.
    boolean        tknockout = true;
    
    // Values for the text rendering mode
    // I wonder if there's a better place to collect all these constants (like a PDFConstants...)
    // rather then having them scattered hither and yon.
    public final int PDFFillTextMode = 0;
    public final int PDFStrokeTextMode = 1;
    public final int PDFFillStrokeMode = 2;
    public final int PDFInvisibleTextMode = 3;
    public final int PDFFillClipTextMode = 4;
    public final int PDFStrokeClipTextTextMode = 5;
    public final int PDFFillStrokeClipTextMode = 6;
    public final int PDFClipTextMode = 7;
    
/** Creates a new PDF gstate. */
public PDFGState()
{
        super();
        colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        scolorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
}

/** Standard clone implementation. */
public Object clone()
{
    PDFGState copy = null;

    try { copy = (PDFGState)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }

    copy.trans = (AffineTransform)trans.clone();
    copy.cp = (Point2D.Float)cp.clone();
    if (clip != null)
        copy.clip = (GeneralPath)clip.clone();
    return copy;
}


}
