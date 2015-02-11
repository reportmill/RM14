package com.reportmill.apptools;
import com.reportmill.graphics.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.event.*;
import snap.swing.*;

/**
 * UI editing for RMShadowEffect.
 */
public class RMShadowEffectTool extends RMEffectTool {
    
/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get shadow effect from shape (or default, if not available)
    RMShadowEffect shadow = effect instanceof RMShadowEffect? (RMShadowEffect)effect : new RMShadowEffect();
    
    // Update ShadowColor
    setNodeValue("ShadowColorWell", shadow.getColor().awt());
    
    // Set SoftnessSlider and SoftnessText
    setNodeValue("SoftnessSlider", shadow.getRadius());
    setNodeValue("SoftnessText", shadow.getRadius());
        
    // Update ShadowDXSpinner and ShadowDYSpinner
    setNodeValue("ShadowDXSpinner", shadow.getDX());
    setNodeValue("ShadowDYSpinner", shadow.getDY());
}

/**
 * Responds to changes from the UI panel controls and updates currently selected shape.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected effect
    RMEffect effect = getSelectedEffect();
    
    // Get shadow effect from shape (or default, if not available)
    RMShadowEffect shadow = effect instanceof RMShadowEffect? (RMShadowEffect)effect : new RMShadowEffect();
    
    // Handle ShadowColorWell
    if(anEvent.equals("ShadowColorWell")) {
        
        // If value is adjusting, just return
        if(anEvent.getValueIsAdjusting()) return;
        
        // Create new fill from old shadow fill with new softness
        shadow = shadow.deriveFill(new RMColor(anEvent.getColorValue()));
    }        
    
    // Handle SoftnessText and SoftnessSlider
    if(anEvent.equals("SoftnessText") || anEvent.equals("SoftnessSlider"))
        shadow = shadow.deriveFill(anEvent.getIntValue());
    
    // Handle ShadowDXSpinner
    if(anEvent.equals("ShadowDXSpinner"))
        shadow = shadow.deriveFill(anEvent.getIntValue(), shadow.getDY());
    
    // Handle ShadowDYSpinner
    if(anEvent.equals("ShadowDYSpinner"))
        shadow = shadow.deriveFill(shadow.getDX(), anEvent.getIntValue());
    
    // Handle OffsetPanel - get the offset panel and create new fill from old shadow fill with new dx
    if(anEvent.equals("ShadowOffsetPanel")) {
        OffsetPanel op = anEvent.getTarget(OffsetPanel.class);
        shadow = shadow.deriveFill(shadow.getDX() + op.getDX(), shadow.getDY() + op.getDY());
    }
    
    // Set new shadow effect
    setSelectedEffect(shadow);
}

/**
 * Implements a simple control to edit shadow position.
 */
public static class OffsetPanel extends JComponent {

    // Previous offsets
    int       _x1, _y1;
    
    // Current offsets
    int       _x2, _y2;
    
    /** Creates offset panel. */
    public OffsetPanel()
    {
        // Turn off opaque
        setOpaque(false);
        
        // Create mouse input adaptor to send ribs actions
        MouseInputAdapter mia = new MouseInputAdapter() {
            
            // Handle mouse pressed
            public void mousePressed(MouseEvent e)  { _x1 = _x2 = e.getX(); _y1 = _y2 = e.getY(); }
            
            // Handle mouse dragged
            public void mouseDragged(MouseEvent e)
            {
                if(!isEnabled()) return;
                _x1 = _x2; _y1 = _y2;
                _x2 = e.getX(); _y2 = e.getY();
                repaint();
                
                // Send event
                Swing.sendEvent(new ChangeEvent(OffsetPanel.this));
            }
            
            // Handle mouse released
            public void mouseReleased(MouseEvent e) { mouseDragged(e); }
        };
        
        // Add mouse input adapter
        addMouseListener(mia);
        addMouseMotionListener(mia);
    }
    
    /** Return offset X. */
    public int getDX()  { return _x2 - _x1; }
    
    /** Return offset Y. */
    public int getDY()  { return _y2 - _y1; }
    
    /** Paint component. */
    public void paintComponent(Graphics g)
    {
        float w = getWidth(), h = getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, (int)w, (int)h);
        g.setColor(Color.black);
        g.drawRect(0, 0, (int)w, (int)h);
        g.setColor(Color.lightGray);
        g.fill3DRect((int)(w/4), (int)(h/4), (int)(w/2), (int)(h/2), true);
    }
}

}