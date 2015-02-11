package com.reportmill.viewer;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import javax.print.*;
import javax.swing.*;
import snap.swing.*;
import snap.util.Undoer;
import snap.web.WebURL;

/**
 * The RMViewer class is a JComponent subclass that can be used in Swing applications to display and/or print an
 * RMDocument.
 *
 * You might use it like this to simply print a document:
 * <p><blockquote><pre>
 *   new RMViewer(aDocument).print();
 * </pre></blockquote><p>
 * Or you might want to allocate one and add it to a Swing component hierarchy:
 * <p><blockquote><pre>
 *   RMViewer viewer = new RMViewer(); viewer.setContent(new RMDocument(aSource));
 *   myFrame.getContentPane().add(new JScrollPane(viewer));
 * </pre></blockquote>
 */
public class RMViewer extends JComponent implements PropertyChangeListener, Scrollable {

    // The shape viewer uses to manage real root of shapes
    RMViewerShape            _vshape = createViewerShape();
    
    // The Zoom mode
    ZoomMode                 _zoomMode = ZoomMode.ZoomAsNeeded;
    
    // Zoom factor
    float                    _zoomFactor = 1;
    
    // The previous zoom factor (for toggle zoom)
    float                    _lastZoomFactor = 1;

    // The input adapter that handles input for the viewer
    RMViewerInputAdapter     _inputAdapter;

    // The current set of shapes that need to be redrawn after the current event
    List <RMShape>           _dirtyShapes = new Vector(32);
    
    // The area of the viewer marked for redraw after the current event
    RMRect                   _dirtyRect;
    
    // A "Phantom" component that JComponentShapes can attach to
    PhantomPane              _phantomPane;
    
    // An array of dates of last 10 frames
    List <Long>              _frameTimes;
    
    // Zoom modes
    public enum ZoomMode { ZoomToFit, ZoomAsNeeded, ZoomToFactor };
    
    // Constants for PropertyChanges
    public static final String Content_Prop = "Content";
        
/**
 * Creates a new RMViewer with an empty document in it.
 */
public RMViewer()
{    
    // Enable events
    enableEvents(MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
            MouseEvent.MOUSE_WHEEL_EVENT_MASK);
    
    // Set RequestFocusEnabled, Focusable so viewer can get key events
    setRequestFocusEnabled(true);
    setFocusable(true);
    
    // Install hierarchy listener to set Viewer Shown state
    addHierarchyListener(new HierarchyListener() {
        public void hierarchyChanged(HierarchyEvent e) { setShown(isShowing()); }});

    // Set background to light gray
    setBackground(Color.lightGray);
}

/**
 * Returns the viewer shape.
 */
public RMViewerShape getViewerShape()  { return _vshape; }

/**
 * Creates the viewer shape.
 */
protected RMViewerShape createViewerShape()  { return new RMViewerShape(this); }

/**
 * Returns the root shape that is the content of this viewer.
 */
public RMParentShape getContent()  { return _vshape.getContent(); }

/**
 * Sets the root shape that is the content of this viewer.
 */
public void setContent(RMParentShape aShape)
{
    // If old shape, stop animation
    if(getContent()!=null) stop();
    
    // Set new document and fire property change
    RMShape shape = getContent(); _vshape.setContent(aShape);
    firePropertyChange(Content_Prop, shape, aShape);
    
    // If Showing, start playing
    if(isShowing()) play();
    
    // Set ZoomToFitFactor and revalidate/repaint (for possible size change)
    setZoomToFitFactor();
    revalidate(); repaint();
}

/**
 * Sets the content from any source.
 */
public void setContent(Object aSource)  { setContent(new RMArchiver().getParentShape(aSource)); }

/**
 * Returns the RMDocument associated with this viewer.
 */
public RMDocument getDocument()  { return _vshape.getContent(RMDocument.class); }

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return getViewerShape().getSourceURL(); }

/**
 * Returns whether viewer is really doing editing.
 */
public boolean isEditing()  { return false; }

/**
 * Returns whether editor is preview (or viewer) mode.
 */
public boolean isPreview()  { return !isEditing(); }

/**
 * Returns the page count.
 */
public int getPageCount()  { return _vshape.getPageCount(); }

/**
 * Returns the currently selected page shape.
 */
public RMParentShape getSelectedPage()  { return _vshape.getSelectedPage(); }

/**
 * Returns the index of the current visible document page.
 */
public int getSelectedPageIndex()  { return _vshape.getSelectedPageIndex(); }

/**
 * Sets the page of viewer's document that is visible (by index).
 */
public void setSelectedPageIndex(int anIndex)  { _vshape.setSelectedPageIndex(anIndex); }

/**
 * Selects the next page.
 */
public void pageForward()  { setSelectedPageIndex(getSelectedPageIndex()+1); }

/**
 * Selects the previous page.
 */
public void pageBack()  { setSelectedPageIndex(getSelectedPageIndex()-1); }

/**
 * Starts any animation viewer might have.
 */
public void play()
{
    // If animated, start playing doc
    if(isEditing()) return;
    RMAnimator animator = _vshape.getChildAnimator(); if(animator==null) return;
    animator.setTime(0);
    animator.setResetTimeOnStop(false);
    animator.play();
    animator.addAnimatorListener(new RMAnimator.Listener() {
        public void animatorStarted(RMAnimator anAnimator) { }
        public void animatorUpdated(RMAnimator anAnimator) { }
        public void animatorStopped(RMAnimator anAnimator) {
            
            // If end action is Page:, perform action
            if(anAnimator.getEndAction().startsWith("Page:")) {
                String action = anAnimator.getEndAction().substring("Page:".length());
                if(action.equals("Next"))
                    pageForward();
            }
            
            // Remove this listener
            anAnimator.removeAnimatorListener(this);
        }
    });
}

/**
 * Stops any animation viewer might have.
 */
public void stop()
{
    RMAnimator animator = _vshape.getChildAnimator(); if(animator==null) return;
    animator.stop();
}

/**
 * Returns whether viewer was shown.
 */
public boolean getShown()  { return _shown; } boolean _shown;

/**
 * Sets whether viewer was shown.
 */
protected void setShown(boolean aValue)
{
    // Set value and call viewerShown()/viewerHidden() (just return if already set)
    if(aValue==_shown) return;
    _shown = aValue;
    if(_shown) viewerShown(); else viewerHidden();
}

/**
 * Called when viewer is shown.
 */
protected void viewerShown()  { _vshape.setShowing(true); play(); }

/**
 * Called when viewer is hidden.
 */
protected void viewerHidden()  { stop(); _vshape.setShowing(false); }

/**
 * Returns the bounds of the viewer document.
 */
public RMRect getDocumentBounds()  { return convertRectFromShape(getContent().getBoundsInside(), null); }

/**
 * Returns the bounds of the viewer document's selected page.
 */
public RMRect getPageBounds()  { return convertRectFromShape(getSelectedPage().getBoundsInside(), getSelectedPage()); }

/**
 * Returns the first shape hit by the given point.
 */
public RMShape getShapeAtPoint(Point2D aPoint, boolean goDeep)
{
    // Convert point from viewer to selected page
    RMParentShape parent = getSelectedPage();
    RMPoint point = convertPointToShape(aPoint, parent);
    
    // Iterate over children to find shape hit by point
    RMShape shape = null; RMPoint point2 = null;
    for(int i=parent.getChildCount(); i>0 && shape==null; i--) { RMShape child = parent.getChild(i-1);
        point2 = child.convertedPointFromShape(point, parent);
        if(child.contains(point2))
            shape = child;
    }
    
    // If we need to goDeep (and there was a top level hit shape), recurse until shape is found
    while(goDeep && shape instanceof RMParentShape) { parent = (RMParentShape)shape;
        RMShape shp = parent.getChildContaining(point2);
        if(shp!=null) { shape = shp; point2 = shape.convertPointFromShape(point2, parent); }
        else break;
    }
    
    // Return hit shape
    return shape;
}

/**
 * Returns the viewer's zoom factor (1 by default).
 */
public float getZoomFactor()  { return _zoomFactor; }

/**
 * Sets the viewer's zoom factor (1 for 100%).
 */
public void setZoomFactor(float aFactor)
{
    setZoomMode(ZoomMode.ZoomToFactor);
    setZoomFactorImpl(aFactor);
}

/**
 * Sets the viewer's zoom factor (1 for 100%) and mode.
 */
protected void setZoomFactorImpl(float aFactor)
{    
    // Constrain zoom factor to valid range (ZoomToFactor: 20%...10000%, ZoomAsNeed: Max of 1)
    ZoomMode zmode = getZoomMode();
    if(zmode==ZoomMode.ZoomToFactor) aFactor = Math.min(Math.max(.2f, aFactor), 100);
    else if(zmode==ZoomMode.ZoomAsNeeded) aFactor = Math.min(aFactor, 1);

    // If already at given factor, just return
    if(aFactor==_zoomFactor) return;

    // Set last zoom factor and new zoom factor and fire property change
    firePropertyChange("ZoomFactor", _lastZoomFactor = _zoomFactor, _zoomFactor = aFactor);
    
    // If ZoomToFactor and parent is viewport, resize and scroll to center of previous zoom
    if(isZoomToFactor() && getParent() instanceof JViewport) {
        Rectangle vr = getZoomFocusRect();
        setSize(getPreferredSize());
        RMRect vr2 = new RMRect(vr).scale(_zoomFactor/_lastZoomFactor);
        vr2.inset((vr2.width - vr.width)/2, (vr2.height - vr.height)/2);
        scrollRectToVisible(vr2.getBounds());
    }
    
    // Revalidate and repaint
    revalidate(); repaint();
}

/**
 * Returns the ZoomMode (ZoomToFit, ZoomIfNeeded, ZoomToFactor).
 */
public ZoomMode getZoomMode()  { return _zoomMode; }

/**
 * Sets the ZoomMode.
 */
public void setZoomMode(ZoomMode aZoomMode)
{
    if(aZoomMode==getZoomMode()) return;
    firePropertyChange("ZoomMode", _zoomMode, _zoomMode = aZoomMode);
    setZoomToFitFactor(); // Reset ZoomFactor
}

/**
 * Returns whether viewer is set to ZoomToFactor.
 */
public boolean isZoomToFactor()  { return getZoomMode()==ZoomMode.ZoomToFactor; }

/**
 * Returns the zoom factor for the given mode at the current viewer size.
 */
public float getZoomFactor(ZoomMode aMode)
{
    // If ZoomToFactor, just return ZoomFactor
    if(aMode==ZoomMode.ZoomToFactor) return getZoomFactor();
    
    // Get ideal size and current size (if size is zero, return 1)
    Dimension isize = getPrefSize();
    int width = getWidth(), height = getHeight(); if(width==0 || height==0) return 1;
    
    // If ZoomAsNeeded and IdealSize is less than size, return
    if(aMode==ZoomMode.ZoomAsNeeded && isize.width<=width && isize.height<=height) return 1;
    if(aMode==ZoomMode.ZoomToFit && isize.width==width && isize.height==height) return 1;
    
    // Otherwise get ratio of parent size to ideal size (with some gutter added in) and return smaller axis
    float zw = width/(isize.width + 8f), zh = height/(isize.height + 8f);
    return Math.min(zw, zh);
}

/**
 * Sets the zoom to fit factor, based on the current zoom mode.
 */
public void setZoomToFitFactor()  { setZoomFactorImpl(getZoomFactor(getZoomMode())); }

/**
 * Returns zoom focus rect (just the visible rect by default, but overriden by editor to return selected shapes rect).
 */
public Rectangle getZoomFocusRect()  { return getVisibleRect(); }

/**
 * Returns the zoom factor to view the document at actual size taking into account the current screen resolution.
 */
public float getZoomToActualSizeFactor()
{
    // Return screen resolution
    try { return Toolkit.getDefaultToolkit().getScreenResolution()/72f; }
    catch(HeadlessException he) { return 1; }
}

/**
 * Sets the viewer's zoom to its previous value.
 */
public void zoomToggleLast()  { setZoomFactor(_lastZoomFactor); }

/**
 * Runs a dialog panel to request a percentage zoom (which is then set with setZoomFactor).
 */
public void runZoomPanel()
{
    // Run input dialog to get zoom factor string
    DialogBox dbox = new DialogBox("Zoom Panel"); dbox.setQuestionMessage("Enter Percentage to Zoom to:");
    String string = dbox.showInputDialog(this, "120");
    
    // If string is valid, set zoom factor to float value
    if(string!=null) {
        float factor = RMStringUtils.floatValue(string)/100;
        if(factor>0)
            setZoomFactor(factor);
    }
    
    // Request focus
    SwingUtils.getWindow(this).requestFocus();
}

/**
 * Overrides to update ZoomFactor if dynamic.
 */
public void setBounds(int x, int y, int w, int h)  { super.setBounds(x, y, w, h); setZoomToFitFactor(); }

/**
 * Returns the content shape's X location in viewer.
 */
public int getContentX()
{
    float align = .5f; //RMShapeUtils.getAutosizeAlignmentX(getContent());
    return (int)Math.round(Math.max((getWidth()-getContent().getWidth()*getZoomFactor())*align, 0));
}

/**
 * Returns the content shape's Y location in viewer.
 */
public int getContentY()
{
    float align = .5f; //RMShapeUtils.getAutosizeAlignmentY(getContent());
    return (int)Math.round(Math.max((getHeight()-getContent().getHeight()*getZoomFactor())*align, 0));
}

/**
 * Returns a point converted from the coordinate space of the given shape to viewer coords.
 */
public RMPoint convertPointFromShape(RMPoint aPoint, RMShape aShape)
{
    // If given shape, transform point from shape to doc
    if(aShape!=null)
        aPoint = aShape.convertedPointToShape(aPoint, null);
    
    // Transform point for zoom
    aPoint.multiply(getZoomFactor(), getZoomFactor());
    
    // Transform point for document centering
    aPoint.x += getContentX();
    aPoint.y += getContentY();
    
    // Return transformed point
    return aPoint;
}

/**
 * Returns a point converted from viewer coords to the coordinate space of the given shape.
 */
public RMPoint convertPointToShape(Point2D aPoint, RMShape aShape)
{
    // Correct for zoom
    double x = aPoint.getX() - getContentX();
    double y = aPoint.getY() - getContentY();
    
    // Get point unzoomed
    RMPoint point = new RMPoint(x/getZoomFactor(), y/getZoomFactor());
    
    // Conver point to shape
    if(aShape!=null)
        aShape.convertPointFromShape(point, null);
    
    // Return point
    return point;
}

/**
 * Returns a rect converted from the coordinate space of the given shape to viewer coords.
 */
public RMRect convertRectFromShape(RMRect aRect, RMShape aShape)
{
    RMPoint points[] = aRect.getPoints();
    for(int i=0; i<4; i++)
        points[i] = convertPointFromShape(points[i], aShape);
    return new RMRect(points);
}

/**
 * Returns a rect converted from viewer coords to the coordinate space of the given shape.
 */
public RMRect convertRectToShape(RMRect aRect, RMShape aShape)
{
    RMPoint points[] = aRect.getPoints();
    for(int i=0; i<4; i++)
        points[i] = convertPointToShape(points[i], aShape);
    return new RMRect(points);
}

/**
 * Returns the transform from given shape to viewer.
 */
public AffineTransform getTransformFromShape(RMShape aShape)
{
    // Get transform for translation
    AffineTransform trans = AffineTransform.getTranslateInstance(getContentX(), getContentY());
    
    // Scale transform for document zoom
    if(getZoomFactor()!=1)
        trans.scale(getZoomFactor(), getZoomFactor());

    // If given shape, get transform from shape to doc
    if(aShape!=null)
        trans.concatenate(aShape.getTransformToShape(null).awt());
    
    // Return the transform
    return trans;
}

/**
 * Requests a repaint for the area represented by the given rect.
 */
public void repaint(Rectangle2D aRect)
{
    int x = (int)Math.floor(aRect.getX());
    int y = (int)Math.floor(aRect.getY());
    int w = (int)Math.ceil(aRect.getMaxX() - x);
    int h = (int)Math.ceil(aRect.getMaxY() - y);
    repaint(0, x, y, w, h);
}

/**
 * Returns the object that is actually responsible for paining shapes in the viewer.
 */
public RMShapePainter getShapePainter(Graphics2D aGr)
{
    // Create, configure and return shape painter
    RMShapePainterJ2D pntr = createShapePainter(aGr);
    pntr.setScale(getZoomFactor());
    pntr.setBounds(0, 0, getWidth(), getHeight());
    //_shapePainter.setAlignmentX(RMShapeUtils.getAutosizeAlignmentX(getShape()));
    //_shapePainter.setAlignmentY(RMShapeUtils.getAutosizeAlignmentY(getShape()));
    return pntr;
}

/**
 * Creates the object that is actually responsible for paining shapes in the viewer.
 */
protected RMShapePainterJ2D createShapePainter(Graphics2D aGr)  { return new RMShapePainterJ2D(aGr); }

/**
 * Returns whether or not the gutter, page background & drop shadow get drawn.
 */
public boolean getDrawsBackground()  { return true; }

/**
 * Overrides JComponent implementation to paint viewer shapes and page, margin, grid, etc.
 */
public void paintComponent(Graphics aGraphics)
{
    // Get graphics and add rendering hints
    Graphics2D g2 = (Graphics2D)aGraphics; // Get graphics 2D
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    // Paint background
    paintBackground(g2);
    
    // Get shape painter and paint shape
    RMShapePainter pntr = getShapePainter(g2);
    pntr.paintShape(_vshape); // Paint document
    getInputAdapter().paint(g2); // Have input adapter paint above
    if(_frameTimes!=null) paintFrameRate(g2); // If frame rate is requested, paint it
}

/**
 * Paints the background.
 */
protected void paintBackground(Graphics2D aGrfx)
{
    if(!getDrawsBackground() || getBackground()==null) return;
    aGrfx.setColor(getBackground());
    aGrfx.fillRect(0, 0, getWidth(), getHeight());
}

/**
 * Returns the input adapter for the viewer (handles mouse and keyboard input).
 */
public RMViewerInputAdapter getInputAdapter()
{
    if(_inputAdapter==null) setInputAdapter(createInputAdapter());
    return _inputAdapter;
}

/**
 * Sets the input adapter for the viewer (handles mouse and keyboard input).
 */
public void setInputAdapter(RMViewerInputAdapter anInputAdapter)  { _inputAdapter = anInputAdapter; repaint(); }

/**
 * Creates a default input adapter.
 */
protected RMViewerInputAdapter createInputAdapter()  { return new RMViewerInputAdapterImpl(this); }

/**
 * Handle mouse events.
 */
protected void processMouseEvent(MouseEvent anEvent)
{
    super.processMouseEvent(anEvent); // Do normal version
    getInputAdapter().processMouseEvent(anEvent); // Forward to input adapter
}

/**
 * Handle mouse motion events
 */
protected void processMouseMotionEvent(MouseEvent anEvent)
{
    super.processMouseMotionEvent(anEvent); // Do normal version
    getInputAdapter().processMouseMotionEvent(anEvent); // Forward to input adapter
}

/**
 * Handle mouse wheel events.
 */
protected void processMouseWheelEvent(MouseWheelEvent anEvent)
{
    // If alt is down, zoom
    if(anEvent.isAltDown()) {

        // Get current zoom factor and new zoom factor
        float zoomFactor = getZoomFactor();
        float zoomFactor2 = zoomFactor - anEvent.getWheelRotation()/10f;
        
        // If zooming down and factor less than or equal 1, setZoomMode ZoomAsNeeded
        if(zoomFactor2<zoomFactor && zoomFactor2<=1)
            setZoomMode(ZoomMode.ZoomAsNeeded);

        // Otherwise, set zoom factor to new zoom factor and scroll rect under mouse to visible
        else {
            Rectangle rect = getVisibleRect();
            setZoomFactor(zoomFactor2);
            Rectangle rect2 = getVisibleRect();
            rect.x = Math.round(anEvent.getX()*zoomFactor2/zoomFactor) - (anEvent.getX() - rect.x);
            rect.y = Math.round(anEvent.getY()*zoomFactor2/zoomFactor) - (anEvent.getY() - rect.y);
            rect.width = rect2.width;
            rect.height = rect2.height;
            scrollRectToVisible(rect);
        }
    }
        
    // If no-alt, do normal mouse wheel event scrolling
    else  {
        Rectangle rect = getVisibleRect();
        rect.y += anEvent.getWheelRotation()*10f;
        scrollRectToVisible(rect);
    }
    
    // Consume event
    anEvent.consume();
}

/**
 * Handle key events.
 */
protected void processKeyEvent(KeyEvent anEvent)
{
    getInputAdapter().processKeyEvent(anEvent); // Forward to input adapter
    super.processKeyEvent(anEvent); // Do normal version
}

/**
 * Handle mouse pressed event.
 */
protected void mousePressed(MouseEvent anEvent)  { getInputAdapter().mousePressed(anEvent); }

/**
 * Handle mouse dragged event.
 */
protected void mouseDragged(MouseEvent anEvent)  { getInputAdapter().mouseDragged(anEvent); }

/**
 * Handle mouse released event.
 */
protected void mouseReleased(MouseEvent anEvent)  { getInputAdapter().mouseReleased(anEvent); }

/**
 * Handle mouse moved event.
 */
protected void mouseMoved(MouseEvent anEvent)  { getInputAdapter().mouseMoved(anEvent); }

/**
 * Viewer callback - called when shape with URL is clicked (opens URL).
 */
public void performActionURLClick(String aURL)
{
    // Handle Page Links
    if(aURL.startsWith("Page:")) {
        if(aURL.equals("Page:Next")) setSelectedPageIndex(getSelectedPageIndex()+1);
        else if(aURL.equals("Page:Back")) setSelectedPageIndex(getSelectedPageIndex()-1);
        else if(aURL.equals("Page:First")) setSelectedPageIndex(0);
        else if(aURL.equals("Page:Last")) setSelectedPageIndex(_vshape.getPageCount()-1);
        else setSelectedPageIndex(RMUtils.intValue(aURL)-1);
    }
    
    // Handle anything else
    else RMURLUtils.openURL(aURL);
}

/**
 * Returns a hover shape for given something.
 */
public RMShape getHoverShape(String aString)
{
    RMTextShape text = new RMTextShape(aString); text.setBestSize();
    text.setColor(new RMColor("#FEFED8")); text.setStrokeColor(RMColor.black);
    return text;
}

/**
 * Returns the optimal size of the viewer.
 */
public Dimension getPreferredSize()  { return isZoomToFactor()? getPrefSize(getZoomFactor()) : getPrefSize(); }

/**
 * Returns the preferred size of the viewer ignoring ZoomFactor.
 */
protected Dimension getPrefSize()
{
    // Get document width and height, add gutter if not PageLayout.Single and return Dimension
    double w = _vshape.getPrefWidth(), h = _vshape.getPrefHeight();
    if(getDocument()==null || getDocument().getPageLayout()!=RMDocument.PageLayout.Single) { w += 8; h += 8; }
    return new Dimension((int)w, (int)h);
}

/**
 * Returns the preferred size of the viewer scaled to a given zoom factor.
 */
protected Dimension getPrefSize(float aScale)
{
    Dimension d = getPrefSize();
    d.width = (int)Math.ceil(d.width*aScale);
    d.height = (int)Math.ceil(d.height*aScale);
    return d;
}

/** Scrollable methods. */
public boolean getScrollableTracksViewportWidth()
{ return !isZoomToFactor() || getParent().getWidth()>getPreferredSize().width; }
public boolean getScrollableTracksViewportHeight()
{ return !isZoomToFactor() || getParent().getHeight()>getPreferredSize().height; }
public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 72; }
public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 12; }
public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

/**
 * Returns the undoer associated with the viewer's document.
 */
public Undoer getUndoer()  { return _vshape.getUndoer(); }

/**
 * Sets the title of the next registered undo in the viewer's documents's undoer (convenience).
 */
public void undoerSetUndoTitle(String aTitle)
{
    if(getUndoer()!=null)
        getUndoer().setUndoTitle(aTitle);
}

/**
 * Returns whether undos exist in the viewer's documents's undoer (convenience).
 */
public boolean undoerHasUndos()  { return getUndoer()!=null && getUndoer().hasUndos(); }

/**
 * Returns whether changes to shapes cause repaints.
 */
public boolean getShapeRepaintEnabled()  { return _dirtyShapes!=null; }

/**
 * Sets whether changes to shapes cause repaints.
 */
public void setShapeRepaintEnabled(boolean aFlag)  { _dirtyShapes = aFlag? new Vector() : null; }

/**
 * Doc listener method - called before a shape makes a visual change.
 * Provides a mechanism to efficiently repaint the portion of the viewer that currently displays a shape. Registers
 * the area covered by the shape now and at event end, to efficiently repaint shapes in transition as well.
 */
public void docShapeRepaint(RMShape aShape)
{
    // If given shape hasn't been registered yet, post repaint and squirrel shape away for flushGraphics call
    if(isShowing() && _dirtyShapes!=null && !RMListUtils.containsId(_dirtyShapes, aShape)) {
        
        // Add shape to dirty shapes set
        _dirtyShapes.add(aShape);
        
        // Get shape dirty rect
        RMRect dirtyRect = getRepaintBoundsForShape(aShape);
        
        // If this is the first dirty shape, register for flushGraphics call
        if(_dirtyRect==null) {
            _dirtyRect = dirtyRect; // Init dirty rect
            SwingUtilities.invokeLater(new Runnable() { // Flush graphics after delay
                public void run() { flushShapeRepaints(); } });
        }
        
        // Otherwise, add shape bounds to dirty rect
        else _dirtyRect.union(dirtyRect);
    }
    
    // Iterate over shape siblings to notify them of peer change
    RMParentShape parent = aShape.getParent();
    for(int i=0, iMax=parent!=null? parent.getChildCount() : 0; i<iMax; i++) { RMShape child = parent.getChild(i);
        if(child instanceof RMTextShape && child !=aShape)
            ((RMTextShape)child).peerDidChange(aShape);
    }
}

/**
 * This method repaints the total bounds of shapes that have previously been registered by shapeNeedsRepaint. This 
 * should only be used internally.
 */
protected void flushShapeRepaints()
{
    getContent().layout();
    // If no dirty shapes, just return
    if(_dirtyShapes==null || _dirtyShapes.size()==0) return;
    
    // Get local dirty shapes and clear ivar so nothing will re-register while we're building
    List <RMShape> dirtyShapes = _dirtyShapes; _dirtyShapes = null;
    
    // Declare variable for dirty rect
    RMRect dirtyRect = _dirtyRect;
    
    // Iterate over dirty shapes and get total marked bounds in viewer coords
    for(RMShape shape : dirtyShapes) {
        RMRect bounds = getRepaintBoundsForShape(shape); // Get shape marked bounds in viewer coords
        if(dirtyRect==null) dirtyRect = bounds;  // Either set or union dirty bounds
        else dirtyRect.union(bounds);
    }

    // Repaint dirty rect
    repaint(dirtyRect);
    
    // Reset dirty shapes and rect
    _dirtyShapes = dirtyShapes; _dirtyShapes.clear(); _dirtyRect = null;
}

/**
 * Returns the bounds for a given shape in the viewer.
 * Subclasses can override this to account for things like different bounds for selected shapes.
 */
public RMRect getRepaintBoundsForShape(RMShape aShape)
{
    RMRect bounds = aShape.getBoundsMarkedDeep();  // Get shape marked bounds
    bounds = convertRectFromShape(bounds, aShape);  // Convert to viewer coords
    bounds.inset(-4, -4); // Outset for handles
    return bounds;
}

/**
 * PropertyChangeListener method - called by document.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Get property name
    String name = anEvent.getPropertyName();
    
    // Handle SelectedPageIndex, PageSize, PageLayout
    if(name.equals("SelectedPage") || name.equals("PageSize") || name.equals("PageLayout")) {
        revalidate(); setZoomToFitFactor(); repaint();
        firePropertyChange("ContentChange" + name, anEvent.getOldValue(), anEvent.getNewValue());
    }
    
    // On SelectedPage change, clear PhantomPane
    if(name.equals("SelectedPage") && _phantomPane!=null) getPhantomPane().setSelectedPageNotify();
}

/**
 * Returns the document shape for given name.
 */
public RMShape getShape(String aName)  { return _vshape.getChildWithName(aName); }

/**
 * Returns the phantom pane (creating, if necessary).
 */
public PhantomPane getPhantomPane()
{
    if(_phantomPane==null) { removeAll(); add(_phantomPane = new PhantomPane()); }
    return _phantomPane;
}

/**
 * Returns whether we paint frame rate.
 */
public boolean getPaintFrameRate()  { return _frameTimes!=null; }

/**
 * Sets whether we paint frame rate.
 */
public void setPaintFrameRate(boolean aValue)
{
    _frameTimes = aValue? new ArrayList() : null;
    repaint();
}

/**
 * Paints the frame rate.
 */
private void paintFrameRate(Graphics2D aGraphics)
{
    // Add new time (and make sure it doesn't grow bigger than 10)
    _frameTimes.add(System.nanoTime());
    while(_frameTimes.size()>10) _frameTimes.remove(0);

    // Get elapsed time of last 10 frames and frames per second (reset to zero if bogus)
    long elapsedTime = _frameTimes.get(_frameTimes.size()-1) - _frameTimes.get(0);
    double fps = (_frameTimes.size()-1)/(elapsedTime/1000000000f);
    
    // Reset clip and draw string
    float x = getWidth() - 60, y = getHeight() - 10;
    Rectangle2D clip = aGraphics.getClipBounds(), draw = new Rectangle2D.Float(x, y - 15, 60, 20);
    Rectangle2D.union(clip, draw, clip); aGraphics.setClip(clip);
    aGraphics.setColor(Color.white); aGraphics.fill(draw);
    aGraphics.setColor(Color.black); aGraphics.setFont(RMAWTUtils.Helvetica12);
    aGraphics.drawString(Math.round(fps) + " fps", getWidth() - 60, getHeight() - 15);
}

/**
 * This method tells the RMViewer to print by running the print dialog (configured to the default printer).
 */
public void print()  { print(null, true); }

/**
 * This method tells the RMViewer to print to the printer with the given printer name (use null for default printer). It
 * also offers an option to run the printer dialog.
 */
public void print(String aPrinterName, boolean runPanel)
{
    // Get first page, book and printer job for book
    RMShape page = _vshape.getPage(0);
    Book book = getBook();
    PrinterJob job = PrinterJob.getPrinterJob(); job.setPageable(book);
    
    // If a printerName was provided, try to find service with that name
    if(aPrinterName!=null) {
        PrintService services[] = PrinterJob.lookupPrintServices(), service = null;
        for(int i=0; i<services.length; i++)
            if(aPrinterName.equals(services[i].getName()))
                service = services[i];
        if(service!=null)
            try { job.setPrintService(service); }
            catch(Exception e) { e.printStackTrace(); service = null; }
        if(service==null) {
            System.err.println("RMViewer:Print: Couldn't find printer named " + aPrinterName);
            System.err.println("Available Services:");
            for(int i=0; i<services.length; i++)
                System.err.println("\t- " + services[i].getName());
            return;
        }
    }
    
    // Flip the orientation if printer has funny definition of portrait/landscape
    int orient = page.getHeight()>=page.getWidth()? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
    PageFormat pf = job.defaultPage();
    if(pf.getOrientation()==PageFormat.PORTRAIT && pf.getWidth()>pf.getHeight() ||
        pf.getOrientation()==PageFormat.LANDSCAPE && pf.getHeight()>pf.getWidth()) {
        orient = orient==PageFormat.PORTRAIT? PageFormat.LANDSCAPE : PageFormat.PORTRAIT;
        book.getPageFormat(0).setOrientation(orient);
    }
    
    // Run printDialog, and if successful, execute print
    boolean shouldPrint = !runPanel || job.printDialog();
    try { if(shouldPrint) job.print(); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Returns a java.awt.print.Book, suitable for AWT printing.
 */
public Book getBook()
{
    // Get document, generic viewer printable and book
    Printable printable = new RMVPrintable();
    Book book = new Book();
    
    // Iterate over pages and add to book
    for(int i=0, iMax=_vshape.getPageCount(); i<iMax; i++) { RMShape page = _vshape.getPage(i);
    
	    // Get doc width, height and orientation
	    double width = page.getWidth(), height = page.getHeight();
	    int orientation = PageFormat.PORTRAIT;
	    if(width>height) {
	        orientation = PageFormat.LANDSCAPE; width = height; height = page.getWidth(); }
	    
	    // Get paper and configure with appropriate paper size and imageable area
	    Paper paper = new Paper();
	    paper.setSize(width, height);
	    paper.setImageableArea(0, 0, width, height);
	
	    // Get pageFormat and configure with appropriate orientation and paper
	    PageFormat pageFormat = new PageFormat();
	    pageFormat.setOrientation(orientation);
	    pageFormat.setPaper(paper);
	    
	    // Appends page to book
	    book.append(printable, pageFormat);
    }

    // Return book
    return book;
}

/**
 * This inner class simply paints a reqested page to a given Graphics object.
 */
protected class RMVPrintable implements Printable {

    /** Print method. */
    public int print(Graphics aGrfx, PageFormat pageFormat, int pageIndex)
    {
        // If bogus range, bail
        if(pageIndex>=_vshape.getPageCount()) return Printable.NO_SUCH_PAGE;
        
        // Get page at index, get/configure shape painter, paint shape, return success
        RMShape page = _vshape.getPage(pageIndex);
        RMShapePainterJ2D painter = new RMShapePainterJ2D((Graphics2D)aGrfx); painter.setPrinting(true);
        painter.paintShape(page);
        return Printable.PAGE_EXISTS; // Return success
    }
}

/**
 * A phantom component that resides in the viewer, that Swing JComponentShapes can attach to, to draw.
 */
public static class PhantomPane extends JPanel {

    /** Creates a new PhantomPane. */
    public PhantomPane()
    { setRequestFocusEnabled(true); setFocusable(true); setLayout(null); setBounds(_pb); setOpaque(false); }
    
    /** Override to suppress painting. */
    public void paint(Graphics g) { }
    public void paintAll(Graphics g)  { }
    public void print(Graphics g) { }
    public boolean isShowing()  { return true; }
    public boolean contains(int x, int y) { return false; }
    public Rectangle getBounds() { return _pb; } Rectangle _pb = new Rectangle(0, 0, 10, 10);
    
    /** Called when viewer changes selected page. */
    public void setSelectedPageNotify()  { removeAll(); }
}

}