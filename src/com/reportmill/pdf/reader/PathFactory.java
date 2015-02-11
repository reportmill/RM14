/**
 * PathFactory.java 
 * 
 * Path & related object creation.
 * The default pathfactory returns an empty GeneralPath.
 * Developers can add other path subclasses by implementing this interface.
 */

package com.reportmill.pdf.reader;
import java.awt.geom.GeneralPath;
import java.awt.Stroke;

public interface PathFactory {

/** Initialize and return a new empty path.
 * This path may be used as either the current path or the clipping path.
 **/
public GeneralPath createEmptyPath();

/** Line style constants.
 *    The following constants are the values as they appear in a pdf file, as
 *    well as the legal values int the PDFGState
 */
public static final int PDFButtLineCap=0;
public static final int PDFRoundLineCap=1;
public static final int PDFSquareLineCap=2;
public static final int PDFMiterJoin=0;
public static final int PDFRoundJoin=1;
public static final int PDFBevelJoin=2;

/** Create a Stroke object using the linecap,linejoin,linewidth,dashpattern, etc from
 * the gstate.
 * Like all objects created by the factories, the parser just creates them and
 * passes them through to the page markup handler, which can choose to use as much or
 * as little as it wants from them.
 */
public Stroke createStroke(PDFGState g);
}
