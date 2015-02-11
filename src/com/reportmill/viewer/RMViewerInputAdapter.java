package com.reportmill.viewer;
import java.awt.*;
import java.awt.event.*;

/**
 * This class handles functionality related to mouse and keyboard input on a viewer, so that different behavior
 * can easily be swapped in beyond the standard interactive behavior, like text selection or area-selection.
 */
public class RMViewerInputAdapter {

    // The viewer this input adapter works for
    RMViewer            _viewer;

/**
 * Creates a new viewer input adapter.
 */
public RMViewerInputAdapter(RMViewer aViewer)  { _viewer = aViewer; }

/**
 * Returns the viewer we work for.
 */
public RMViewer getViewer()  { return _viewer; }

/**
 * Handle mouse pressed event.
 */
public void mousePressed(MouseEvent anEvent)  { }

/**
 * Handle mouse dragged event.
 */
public void mouseDragged(MouseEvent anEvent)  { }

/**
 * Handle mouse released event.
 */
public void mouseReleased(MouseEvent anEvent)  { }

/**
 * Handle mouse clicked event.
 */
public void mouseClicked(MouseEvent anEvent)  { }

/**
 * Handle mouse entered.
 */
public void mouseEntered(MouseEvent anEvent)  { }

/**
 * Handle mouse moved event.
 */
public void mouseMoved(MouseEvent anEvent)  { }

/**
 * Handle mouse exited.
 */
public void mouseExited(MouseEvent anEvent)  { }

/**
 * Handle key pressed.
 */
public void keyPressed(KeyEvent anEvent)  { }

/**
 * Handle key released.
 */
public void keyReleased(KeyEvent anEvent)  { }

/**
 * Handle key typed.
 */
public void keyTyped(KeyEvent anEvent)  { }

/**
 * Handle paint.
 */
public void paint(Graphics2D g)  { }

/**
 * Handle copy.
 */
public void copy()  { }

/**
 * Handle mouse events.
 */
protected void processMouseEvent(MouseEvent anEvent)
{
    // Forward mouse pressed and released to official methods
    switch(anEvent.getID()) {
        case MouseEvent.MOUSE_PRESSED: getViewer().mousePressed(anEvent); break;
        case MouseEvent.MOUSE_RELEASED: getViewer().mouseReleased(anEvent); break;
        case MouseEvent.MOUSE_CLICKED: mouseClicked(anEvent); break;
        case MouseEvent.MOUSE_ENTERED: mouseEntered(anEvent); break;
        case MouseEvent.MOUSE_EXITED: mouseExited(anEvent); break;
    }
}

/**
 * Handle mouse motion events
 */
protected void processMouseMotionEvent(MouseEvent anEvent)
{
    // Forward mouse moved and dragged to official methods
    switch(anEvent.getID()) {
        case MouseEvent.MOUSE_MOVED: getViewer().mouseMoved(anEvent); break;
        case MouseEvent.MOUSE_DRAGGED: getViewer().mouseDragged(anEvent); break;
    }
}

/**
 * Handle key events.
 */
protected void processKeyEvent(KeyEvent anEvent)
{
    // Forward to input adapter methods
    switch(anEvent.getID()) {
        case KeyEvent.KEY_PRESSED: keyPressed(anEvent);
        case KeyEvent.KEY_RELEASED: keyReleased(anEvent);
        case KeyEvent.KEY_TYPED: keyTyped(anEvent);
    }
}

}