package com.reportmill.app;
import com.reportmill.base.RMPoint;
import com.reportmill.shape.RMShape;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import snap.swing.*;

/**
 * Provides UI editing for shapes with SpringsLayout.
 */
public class SpringsLayoutPane extends SwingOwner {

    // The Autosizing Panel
    AutosizingPanel    _autosizingPanel;

/**
 * Override to get AutosizingPanel. 
 */
protected void initUI()  { _autosizingPanel = getNode("AutosizingPanel", AutosizingPanel.class); }

/**
 * ResetUI.
 */
public void resetUI()
{
    RMEditor editor = RMEditor.getMainEditor();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    _autosizingPanel.setAutosizing(shape.getAutosizing());
}

/**
 * Responds to UI control changes.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get currently selected editor and selected shapes
    RMEditor editor = RMEditor.getMainEditor();
    List <? extends RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Handle AutosizingPanel
    if(anEvent.equals("AutosizingPanel"))
        for(RMShape shape : shapes)
            shape.setAutosizing(_autosizingPanel.getAutosizing());
            
    // Handle ResetAutosizingButton
    if(anEvent.equals("ResetAutosizingButton"))
        for(RMShape shape : shapes)
            shape.setAutosizing("--~,--~");
}

/**
 * An inner class to provide a simple springs and struts control.
 */
public static class AutosizingPanel extends JComponent {
    
    // Autosizing string
    String    _autosizing = "-~~,-~~";
    
    // Autosizing spring/strut images
    Icon      _icons[];
    
    // Constants for images
    public static final int BACKGROUND = 0;
    public static final int OUTER_HORIZONTAL_SPRING = 1;
    public static final int OUTER_VERTICAL_SPRING = 2;
    public static final int OUTER_HORIZONTAL_STRUT = 3;
    public static final int OUTER_VERTICAL_STRUT = 4;
    public static final int INNER_HORIZONTAL_SPRING = 5;
    public static final int INNER_VERTICAL_SPRING = 6;
    public static final int INNER_HORIZONTAL_STRUT = 7;
    public static final int INNER_VERTICAL_STRUT = 8;

    /** Creates a new autosizing panel. */
    public AutosizingPanel()
    {
        // Get image names
        String imageNames[] = { "ssback.png", "outerhspring.png", "outervspring.png", "outerhstrut.png",
            "outervstrut.png", "hspring.png", "vspring.png", "hstrut.png", "vstrut.png" };

        // Create images array
        _icons = new ImageIcon[imageNames.length];
      
        // Load images
        for(int i=0; i<imageNames.length; ++i)
            _icons[i] = Swing.getIcon(imageNames[i], SpringsLayoutPane.class);
        
        // Non-opaque, so Swing knows to draw background
        setOpaque(false);
        
        // Add mouse listener to send action
        addMouseListener(new MouseAdapter() {
            
            // Mouse pressed to trigger spring/strut buttons
            public void mouseReleased(MouseEvent e)
            {
                if(!isEnabled()) return;
                StringBuffer sb = new StringBuffer(_autosizing);
                RMPoint p = new RMPoint(e.getX(), e.getY());
                float w = getWidth(), h = getHeight();
                
                if(p.distance(new RMPoint(w/8, h/2))<w/8)
                    sb.setCharAt(0, sb.charAt(0)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w*3/8, h/2))<w/8)
                    sb.setCharAt(1, sb.charAt(1)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w*5/8, h/2))<w/8)
                    sb.setCharAt(1, sb.charAt(1)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w*7/8, h/2))<w/8)
                    sb.setCharAt(2, sb.charAt(2)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w/2, h/8))<w/8)
                    sb.setCharAt(4, sb.charAt(4)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w/2, h*3/8))<w/8)
                    sb.setCharAt(5, sb.charAt(5)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w/2, h*5/8))<w/8)
                    sb.setCharAt(5, sb.charAt(5)=='-'? '~' : '-');
                else if(p.distance(new RMPoint(w/2, h*7/8))<w/8)
                    sb.setCharAt(6, sb.charAt(6)=='-'? '~' : '-');
                
                // Set new autosizing string
                _autosizing = sb.toString();
                
                // Repaint to show new spring configuration
                repaint();
                
                // Send change event
                Swing.sendEvent(new ChangeEvent(AutosizingPanel.this));
        }});
    }
    
    /** Returns autosizing string. */
    public String getAutosizing() { return _autosizing; }
    
    /** Sets autosizing string. */
    public void setAutosizing(String aString) { _autosizing = aString; repaint(); }
    
    /** Paints the component. */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        float w = getWidth(), h = getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, (int)w, (int)h);
        g.setColor(Color.black);
        g.drawRect(0, 0, (int)w, (int)h);
        
        _icons[BACKGROUND].paintIcon(this, g, 24, 24);
        
        // Draw horizontal left springs
        if(_autosizing.charAt(0)=='-')
            _icons[OUTER_HORIZONTAL_STRUT].paintIcon(this,g,0,41);
        else _icons[OUTER_HORIZONTAL_SPRING].paintIcon(this,g,0,41);
        
        // Draw horizontal middle
        if(_autosizing.charAt(1)=='-')
            _icons[INNER_HORIZONTAL_STRUT].paintIcon(this,g,25,41);
        else _icons[INNER_HORIZONTAL_SPRING].paintIcon(this,g,25,41);
        
        // Draw horizontal right
        if(_autosizing.charAt(2)=='-')
            _icons[OUTER_HORIZONTAL_STRUT].paintIcon(this,g, 73,41);
        else _icons[OUTER_HORIZONTAL_SPRING].paintIcon(this,g,73,41);
        
        // Draw vertical springs top
        if(_autosizing.charAt(4)=='-')
            _icons[OUTER_VERTICAL_STRUT].paintIcon(this,g,41,0);
        else _icons[OUTER_VERTICAL_SPRING].paintIcon(this,g,41,0);
        
        // Draw vertical middle
        if(_autosizing.charAt(5)=='-')
            _icons[INNER_VERTICAL_STRUT].paintIcon(this,g,41,25);
        else _icons[INNER_VERTICAL_SPRING].paintIcon(this,g,41,25);
        
        // Draw vertical bottom
        if(_autosizing.charAt(6)=='-')
            _icons[OUTER_VERTICAL_STRUT].paintIcon(this,g,41,73);
        else _icons[OUTER_VERTICAL_SPRING].paintIcon(this,g,41,73);
        
        // If disabled then dim everything out
        if(!isEnabled()) {
            g2.setColor(new Color(1f, 1f, 1f, .5f));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

}