package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * A base class for painting RMShapes.
 */
public interface RMPainter extends Cloneable {

/**
 * Returns the graphics.
 */
public Graphics2D getGraphics();

/**
 * 
 */
public Color getColor();

/**
 * 
 */
public void setColor(Color c);

/**
 * 
 */
public Font getFont();

/**
 * 
 */
public void setFont(Font font);

/**
 * 
 */
public Paint getPaint();

/**
 * 
 */
public void setPaint(Paint paint);

/**
 * 
 */
public Stroke getStroke();

/**
 * 
 */
public void setStroke(Stroke s);

/**
 * Returns the opacity.
 */
public double getOpacity();

/**
 * Sets the opacity.
 */
public void setOpacity(double aValue);

/**
 * 
 */
public void draw(Shape s);

/**
 * 
 */
public void fill(Shape s);

/**
 * 
 */
public void drawLine(double x1, double y1, double x2, double y2);

/**
 * 
 */
public void fillRect(double x, double y, double w, double h);

/**
 * 
 */
public void drawRect(double x, double y, double w, double h);

/**
 * 
 */
public void fill3DRect(double x, double y, double w, double h, boolean raised);

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(RMRect aRect, boolean isPressed);

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(double x, double y, double w, double h, boolean isPressed);

/**
 * 
 */
public boolean drawImage(Image img, AffineTransform xform);

/**
 * 
 */
public boolean drawImage(Image img, int x, int y, int width, int height);

/**
 * 
 */
public void drawString(String str, double x, double y);

/**
 * 
 */
public void drawGlyphVector(GlyphVector g, float x, float y);

/**
 * 
 */
public void translate(double tx, double ty);

/**
 * 
 */
public void rotate(double theta);

/**
 * 
 */
public void scale(double sx, double sy);

/**
 * 
 */
public void transform(AffineTransform Tx);

/**
 * Returns the string bounds for current font.
 */
public Rectangle2D getStringBounds(String aString);

/**
 * Returns the ascender for the current font.
 */
public double getFontAscent();

/**
 * 
 */
public Rectangle getClipBounds();

/**
 * 
 */
public Shape getClip();

/**
 * 
 */
public void setClip(Shape clip);

/**
 * 
 */
public void clip(Shape s);

/**
 * Sets whether antialiasing.
 */
public boolean setAntialiasing(boolean aValue);

/**
 * Returns whether shape painting is really printing.
 */
public boolean isPrinting();

/**
 * Standard clone implementation.
 */
public RMPainter clone();

/**
 * 
 */
public void dispose();

//public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs);
//public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer);
//public boolean drawImage(Image img, int x, int y, ImageObserver observer);
//public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer);
//public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer);
//public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
//                  ImageObserver obs);
//public abstract boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
//                  Color bgc, ImageObserver obs);
//public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y);
//public void drawRenderedImage(RenderedImage img, AffineTransform xform);
//public void drawRenderableImage(RenderableImage img, AffineTransform xform);
//public void drawString(String str, int x, int y)  { _g2.drawString(str, x, y); }
//public void drawString(String str, float x, float y)  { _g2.drawString(str, x, y); }
//public void drawString(AttributedCharacterIterator iterator, int x, int y)  { }
//public void drawString(AttributedCharacterIterator iterator, float x, float y);
//public void drawString(AttributedCharacterIterator iterator, int x, int y);
//public void drawChars(char data[], int offset, int length, int x, int y);
//public void drawBytes(byte data[], int offset, int length, int x, int y);
//public boolean hit(Rectangle rect, Shape s, boolean onStroke);
//public GraphicsConfiguration getDeviceConfiguration();
//public void translate(int x, int y);
//public void rotate(double theta, double x, double y);
//public void shear(double shx, double shy);
//public void setTransform(AffineTransform Tx);
//public AffineTransform getTransform();
//public void setBackground(Color color);
//public Color getBackground();
//public Graphics create(int x, int y, int width, int height) { }
//public void setPaintMode();
//public void setXORMode(Color c1);
//public FontMetrics getFontMetrics() { return _g2.getFontMetrics(); }
//public FontRenderContext getFontRenderContext();
//public void copyArea(int x, int y, int width, int height, int dx, int dy);
//public void drawLine(int x1, int y1, int x2, int y2);
//public void fillRect(int x, int y, int width, int height);
//public void drawRect(int x, int y, int w, int h)
//public void clearRect(int x, int y, int width, int height);
//public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
//public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
//public void drawOval(int x, int y, int width, int height);
//public void fillOval(int x, int y, int width, int height);
//public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);
//public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
//public void drawPolyline(int xPoints[], int yPoints[], int nPoints);
//public void drawPolygon(int xPoints[], int yPoints[], int nPoints);
//public void drawPolygon(Polygon p);
//public void fillPolygon(int xPoints[], int yPoints[], int nPoints);
//public void fillPolygon(Polygon p);
//public void draw3DRect(int x, int y, int w, int h, boolean raised);
//public void finalize() { dispose(); }
//public void clipRect(int x, int y, int width, int height);
//public void setClip(int x, int y, int width, int height);
//public Rectangle getClipBounds(Rectangle r);
//@Deprecated public Rectangle getClipRect() { return getClipBounds(); }
//public boolean hitClip(int x, int y, int width, int height);
//public RenderingHints getRenderingHints();
//public void addRenderingHints(Map<?,?> hints);
//public Object getRenderingHint(Key hintKey);
//public void setRenderingHint(Key hintKey, Object hintValue);
//public void setRenderingHints(Map<?,?> hints);
//public Composite getComposite();
//public void setComposite(Composite comp);

}