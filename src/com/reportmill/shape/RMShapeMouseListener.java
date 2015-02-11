package com.reportmill.shape;

/**
 * A listener for shape mouse events.
 */
public interface RMShapeMouseListener {

/**
 * Handles mouse pressed events.
 */
public void mousePressed(RMShapeMouseEvent anEvent);

/**
 * Handles mouse dragged events.
 */
public void mouseDragged(RMShapeMouseEvent anEvent);

/**
 * Handles mouse released events.
 */
public void mouseReleased(RMShapeMouseEvent anEvent);

/**
 * Handles mouse clicked events.
 */
public void mouseClicked(RMShapeMouseEvent anEvent);

/**
 * Handles mouse entered events.
 */
public void mouseEntered(RMShapeMouseEvent anEvent);

/**
 * Handles mouse moved events.
 */
public void mouseMoved(RMShapeMouseEvent anEvent);

/**
 * Handles mouse exited events.
 */
public void mouseExited(RMShapeMouseEvent anEvent);

/**
 * An adapter class for RMShapeMouseListener.
 */
public static class Adapter implements RMShapeMouseListener {

    /** Handles mouse pressed events. */
    public void mousePressed(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse dragged events. */
    public void mouseDragged(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse released events. */
    public void mouseReleased(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse clicked events. */
    public void mouseClicked(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse entered events. */
    public void mouseEntered(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse moved events. */
    public void mouseMoved(RMShapeMouseEvent anEvent)  { }
    
    /** Handles mouse exited events. */
    public void mouseExited(RMShapeMouseEvent anEvent)  { }
}

}