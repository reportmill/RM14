package com.reportmill.shape;
import com.reportmill.base.RMRect;
import com.reportmill.graphics.*;
import com.reportmill.text.*;

/**
 * Utility methods for some esoteric text functionality.
 */
public class RMTextShapeUtils {

/**
 * Returns a path for all text chars.
 */
public static RMPath getTextPath(RMTextShape aText)
{
    // Get text layout
    RMTextLayout layout = aText.getTextLayout();
    
    // Create path and establish bounds of text
    RMPath path = new RMPath();
    path.moveTo(0, 0);
    path.moveTo(aText.getWidth(), aText.getHeight());
    
    // Iterate over text runs
    for(RMTextRun run=layout.getRun(); run!=null; run=run.getNext()) {
        if(run.length()==0 || run.isTab()) continue;
        RMPathUtils.appendShape(path, run.glyphVector(null).getOutline((float)run.getX(), (float)run.getYBaseline()));
    }
    
    // Return path
    return path;
}

/**
 * Returns a path for given run.
 */
public static RMPath getTextRunPath(RMTextShape aText, RMTextRun aRun)
{
    // Create path and establish bounds of text
    RMPath path = new RMPath();
    path.moveTo(0, 0);
    path.moveTo(aText.getWidth(), aText.getHeight());
    
    // Append glyph vector outline path
    RMPathUtils.appendShape(path, aRun.glyphVector(null).getOutline((float)aRun.getX(), (float)aRun.getYBaseline()));
    
    // Return path
    return path;
}

/**
 * Returns an RMPolygon shape with the glyph path for the chars in this text. Assumes all runs have same visual attrs.
 */
public static RMPolygonShape getTextPathShape(RMTextShape aText)
{
    // Create polygon for text path
    RMPolygonShape polygon = new RMPolygonShape(getTextPath(aText));
    
    // Set polygon shape attributes from text shape attributes
    polygon.copyShape(aText);
    
    // Set polygon color to run or outline color and stroke
    polygon.setColor(aText.getOutline()==null? aText.getTextColor() : aText.getOutline().getFillColor());
    polygon.setStroke(aText.getOutline()==null? null : new RMStroke(aText.getTextColor(), aText.getOutline().getStrokeWidth()));
    
    // Return polygon shape
    return polygon;
}

/**
 * Returns a polygon shape for the glyphs in a given text run.
 */
public static RMPolygonShape getTextRunPathShape(RMTextShape aText, RMTextRun aRun)
{
    // Create polygon shape for run path
    RMPolygonShape polygon = new RMPolygonShape(getTextRunPath(aText, aRun));
    
    // Set polygon shape attributes from text shape attributes
    polygon.copyShape(aText);
    
    // Set polygon color to run outline color and stroke
    polygon.setColor(aRun.getOutline()==null? aRun.getColor() : aRun.getOutline().getFillColor());
    polygon.setStroke(aRun.getOutline()==null? null : new RMStroke(aRun.getColor(), aRun.getOutline().getStrokeWidth()));
    
    // Return polygon shape
    return polygon;
}

/**
 * Returns a group shape with a text shape for each individual character in this text shape.
 */
public static RMShape getTextCharsShape(RMTextShape aText)
{
    // Get shape for chars
    RMParentShape charsShape = new RMSpringShape(); charsShape.copyShape(aText);
    
    // Get text layout
    RMTextLayout layout = aText.getTextLayout();
    
    // Iterate over runs
    for(RMTextRun run=layout.getRun(); run!=null; run=run.getNext()) {
        
        // If run is empty, just skip
        if(run.length()==0 || run.isTab()) continue;
    
        // Get run font and run bounds
        RMFont font = run.getFont();
        RMRect runBounds = new RMRect(run.getX(), run.getY(), 0, run.getHeight());
        
        // Iterate over run chars
        for(int i=0, iMax=run.length(); i<iMax; i++) { char c = run.charAt(i);
            
            // Get char advance (just continue if zero)
            double advance = font.charAdvance(c);
            if(advance<=0)
                continue;
            
            // If non-space character, create glyph shape
            if(c != ' ') {
                RMRect glyphBounds = font.charBounds(c);
                RMXString gstring = aText.getXString().substring(run.getStart() + i, run.getStart() + i + 1);
                RMTextShape glyph = new RMTextShape(gstring);

                charsShape.addChild(glyph, "~-~,~-~");
                runBounds.width = (float)Math.ceil(Math.max(advance, glyphBounds.getMaxX()));
                glyph.setFrame(getBoundsFromTextBounds(aText, runBounds));
            }

            // Increase bounds by advance
            runBounds.x += advance;
        }
    }

    // Return chars shape
    return charsShape;
}

/**
 * Returns bounds from given text bounds, adjusted to account for text margins.
 */
private static RMRect getBoundsFromTextBounds(RMTextShape aText, RMRect aRect)
{
    RMRect r = new RMRect(aRect);
    r.x -= aText.getMarginLeft(); r.width += (aText.getMarginLeft() + aText.getMarginRight());
    r.y -= aText.getMarginTop(); r.height += (aText.getMarginTop() + aText.getMarginBottom());
    return r;
}

}