package com.reportmill.swing.shape;
import com.reportmill.shape.RMShapePainter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import snap.util.*;

/**
 * An RMShape subclass for CustomView, that stands in for any referenced JComponent subclass.
 */
public class CustomViewShape extends JComponentShape {

/**
 * This method implements painting for the customview, which really should only appear in the design app.
 */
public void paintShape(RMShapePainter aPntr)
{
    aPntr.setColor(Color.lightGray); aPntr.fillRect(0, 0, (int)getWidth(), (int)getHeight());
    aPntr.setColor(Color.darkGray); aPntr.setStroke(new BasicStroke(1));
    aPntr.drawRect(0, 0, (int)getWidth() - 1, (int)getHeight() - 1);
    
    // Draw "Custom View" text
    Font font = new Font("Arial", Font.PLAIN, 14);
    String text = "Custom View", text2 = getRealClassName();
    double ascender = aPntr.getFontAscent();

    aPntr.setFont(font); aPntr.setColor(Color.white);
    Rectangle2D r = aPntr.getStringBounds(text);
    double w = (getWidth() - r.getWidth())/2f;
    double h = (getHeight() - r.getHeight())/2f + ascender;
    
    if(text2!=null && getHeight()>30) {
        int dotIndex = text2.lastIndexOf(".");
        text2 = "(" + text2.substring(dotIndex+1) + ")";
        Rectangle2D r2 = aPntr.getStringBounds(text2);
        if(r2.getWidth()+8<getWidth()) {
            double w2 = (getWidth() - r2.getWidth())/2f;
            double h2 = (getHeight() - r2.getHeight())/2f + ascender;
            aPntr.drawString(text2, (int)w2, (int)(h2 + r2.getHeight()/2));
            h -= r.getHeight()/2;
        }
    }
        
    aPntr.drawString(text, (int)w, (int)h);
}

/**
 * XML archival.
 */
public XMLElement toXMLShape(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLShape(anArchiver); e.setName("customview"); return e;
}

}