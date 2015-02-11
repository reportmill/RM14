/**
 * PDFMarkupHandler.java 
 * 
 * The MarkupHandler implements all the callbacks for displaying or processing
 * the page marking operations on a pdf page.
 * 
 * The basic architecture is as such:
 * 
 * The PDFFile object is used to open a pdf.  
 * It creates a PDFParser, which understands the general syntax for pulling all 
 * the PDF objects out of the file.
 * As the parser reads the file, it can create objects for dealing with
 * specific pdf objects, like streams or pages.  It also creates graphic objects,
 * as needed, like fonts or paths.  The default implementation
 * just returns the basic awt objects, but users can supply various Factory
 * objects if they want to create custom subclasses, or if they want more control
 * over how the awt objects are created.
 * See the FontFactory & PathFactory interfaces for more details.
 *
 * The actual page marking operations are parsed by the PDFPageParser.
 * The PageParser uses a PDFMarkupHandler subclass to draw the pdf.
 * The PDFEngine supplies all the callbacks to do whatever it likes with the
 * parsed pdf.
 * 
 * The simplest PDFMarkupHandler would just use awt to draw to a Graphics2D,
 * but additional engines might cache a playlist for drawing over & over,
 * or create object representations (like RMShape subclasses) for editing.
 * 
 * The marking operation will need to reference the current PDFGState to
 * get any information not passed as one of the parameters.
 * For example:
 *
 * void fillPath(PDFGState g, GeneralPath p) {
 *   Color c = g.getCurrentColor();
 *   ...// set the color, fill the path
 *   }
 */

package com.reportmill.pdf.reader;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.*;

public abstract class PDFMarkupHandler extends Object {
  
  /** Set the bounds of the page.  This will be called before any marking operations. */
  public abstract void beginPage(float width, float height);
  
  /** Called when all drawing is done. (optional) */
  public void endPage()  {};
  
  /** Stroke the current path with the current miter limit, color, etc. */
  public abstract void strokePath(PDFGState g, GeneralPath p);
  
  /** Fill the current path using the fill params in the gstate */
  public abstract void fillPath(PDFGState g, GeneralPath p);
  
  /** Notifies that a change has been made to the current clipping path */
  public abstract void clipChanged(PDFGState g);
  
  /** Draw an image */
  public abstract void drawImage(PDFGState g, Image i, AffineTransform ixform);
  
  /** Draw some text at the current text position.  
   * The glyphVector will have been created by the parser using the current
   * font and its character encodings.
   */
  public abstract void showText(PDFGState g, GlyphVector v);

  /** Return an awt FontRenderContext object which will be used to render the fonts.
   *  The same rendercontext is used for the entire page.
   */
  public abstract FontRenderContext getFontRenderContext();

  /** Return an awt Graphics object which will be used for suspect purposes.
   *  TODO: this is really to help get the FontMetrics for a given font
   *  
   */
  public abstract Graphics getGraphics();
}
