package com.reportmill.app;
import com.reportmill.base.RMAWTUtils;
import com.reportmill.graphics.RMPath;
import com.reportmill.shape.*;
import com.reportmill.text.RMTextEditor;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * A Java2D painter subclass for editor.
 */
public class RMEditorShapePainter extends RMShapePainterJ2D {

    // The editor
    RMEditor      _editor;

/**
 * Creates an editor java2d painter for the given editor.
 */    
public RMEditorShapePainter(Graphics2D aGr, RMEditor anEditor)  { super(aGr); _editor = anEditor; }

/**
 * Returns whether painting is for editor.
 */
public boolean isEditing()  { return _editor.isEditing(); }

/**
 * Returns whether given shape is selected.
 */
public boolean isSelected(RMShape aShape)  { return _editor.isSelected(aShape); }

/**
 * Returns whether given shape is super selected.
 */
public boolean isSuperSelected(RMShape aShape)  { return _editor.isSuperSelected(aShape); }

/**
 * Returns whether given shape is THE super selected shape.
 */
public boolean isSuperSelectedShape(RMShape aShape)  { return _editor.getSuperSelectedShape()==aShape; }

/**
 * Override to handle text.
 */
public void sendPaintShape(RMShape aShape)
{
    // If TextShape, paintText
    if(aShape instanceof RMTextShape) { RMTextShape tshape = (RMTextShape)aShape;
        tshape.paintShapeBack(this);
        paintText((RMTextShape)aShape);
        tshape.paintShapeText(this);
    }

    // Otherwise, do normal version
    else super.sendPaintShape(aShape);
}

/**
 * Paints text - special case because uses editor.
 */
protected void paintText(RMTextShape aText)
{
    // Draw a bounding rect, if needed
    if(drawBoundsRect(_editor, aText)) {
        
        // Set color (red if selected, light gray otherwise) and stroke
        setColor(_editor.isSuperSelected(aText)? new Color(.9f, .4f, .4f) : Color.lightGray);
        setStroke(new BasicStroke(1f, 0, 0, 1, new float[] { 3, 2 }, 0));
        
        // Get path for text and draw (no antialiasing)
        RMPath path = aText.getPath().getPathInRect(aText.getBoundsInside());
        setAntialiasing(false); draw(path);
        setAntialiasing(true);
    }

    // If text editor is editing text, have it paint (clipped to text bounds)
    if(_editor._textEditor._textShape==aText) {
        Shape clip = getClip(); clip(aText.getBoundsInside());
        paintTextEditor(_editor._textEditor);
        setClip(clip);
    }
}

/**
 * Paints a given layout in a given graphics.
 */
protected void paintTextEditor(RMTextEditor aTE)
{
    // Get selection path
    GeneralPath path = aTE.getSelPath();

    // If empty selection, draw caret
    if(aTE.isSelEmpty() && path!=null) {
        setColor(Color.black); setStroke(RMAWTUtils.Stroke1); // Set color and stroke of cursor
        setAntialiasing(false); draw(path); // Draw cursor (antialias off for sharpness)
        setAntialiasing(true);
    }

    // If selection, get selection path and fill
    else {
        setColor(new Color(128, 128, 128, 128));
        fill(path);
    }

    // If spell checking, get path for misspelled words and draw
    if(aTE.isSpellChecking() && aTE.length()>0) {

        // Get path for misspelled words and draw
        GeneralPath spellPath = aTE.getSpellingPath();
        if(spellPath!=null) {
            setColor(Color.red); setStroke(RMAWTUtils.StrokeDash1);
            draw(spellPath);
        }
    }
}

/**
 * Returns whether to draw bounds rect.
 */
static boolean drawBoundsRect(RMEditor anEditor, RMTextShape aText)
{
    if(aText.getStroke()!=null) return false; // If text draws it's own stroke, return false
    if(anEditor.isPreview()) return false; // If editor is previewing, return false
    if(aText.isStructured()) return false; // If structured text, return false
    if(anEditor.isSelected(aText) || anEditor.isSuperSelected(aText)) return true; // If selected, return true
    if(aText.length()==0) return true; // If text is zero length, return true
    if(aText.getDrawsSelectionRect()) return true; // If text explicitly draws selection rect, return true
    return false; // Otherwise, return false
}

}