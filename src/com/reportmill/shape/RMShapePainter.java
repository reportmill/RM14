package com.reportmill.shape;
import com.reportmill.graphics.*;

/**
 * This class manages the painting of shapes to a Java2D graphics object.
 */
public interface RMShapePainter extends RMPainter {

/**
 * Paints a simple shape.
 */
public void paintShape(RMShape aShape);

/**
 * Paints a child shape.
 */
public void sendPaintShape(RMShape aShape);

/**
 * Returns whether painting is for editor.
 */
public boolean isEditing();

/**
 * Returns whether given shape is selected.
 */
public boolean isSelected(RMShape aShape);

/**
 * Returns whether given shape is super selected.
 */
public boolean isSuperSelected(RMShape aShape);

/**
 * Returns whether given shape is THE super selected shape.
 */
public boolean isSuperSelectedShape(RMShape aShape);

/**
 * Standard clone implementation.
 */
public RMShapePainter clone();

}