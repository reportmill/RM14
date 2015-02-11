package com.reportmill.swing.tool;
import com.reportmill.swing.shape.SpringsPaneShape;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Provides inspector for SpringsPaneShape.
 */
public class SpringsPaneTool <T extends SpringsPaneShape> extends JComponentTool <T> {
    
    // A default list of borders
    protected static Border[] _borders = {
        new EmptyBorder(0, 0, 0, 0),
        new EtchedBorder(EtchedBorder.LOWERED),
        new EtchedBorder(EtchedBorder.RAISED),
        new BevelBorder(BevelBorder.RAISED),
        new BevelBorder(BevelBorder.LOWERED),
        new LineBorder(Color.black) };
    
/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get BorderList and configure it
    JList list = getNode("BorderList", JList.class);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setCellRenderer(new BorderListCellRenderer());
    list.setVisibleRowCount(-1);
    list.setListData(_borders);
}

/**
 * Updates the Swing UI from the currently selected panel shape.
 */
/*public void resetUI()
{
    // Get the currently selected panel shape and panel (just return if null)
    SpringsPaneShape panelShape = getSelectedShape(); if(panelShape==null) return;
    
    // Get panel's border
    Border border = panel.getBorder();
    
    // Update BorderList
    if(RMBorderUtils.isEmpty(RMBorderUtils.getBorder(border))) setNodeSelectedIndex("BorderList", 0);
    else setNodeValue("BorderList", RMBorderUtils.getBorder(border));
    
    // Update TitleText
    setNodeValue("TitleText", RMBorderUtils.getTitle(border));
}*/

/**
 * Updates the currently selected panel shape from the Swing UI controls.
 */
/*public void respondUI(SwingEvent anEvent)
{
    // Get the currently selected panel shape and panel (just return if null)
    SpringsPaneShape panelShape = getSelectedShape(); if(panelShape==null) return;
    
    // Handle TitleText
    if(anEvent.equals("TitleText")) {
        Border border = panel.getBorder();
        border = RMBorderUtils.setTitle(border, anEvent.getStringValue());
        panelShape.setBorder(border);
    }
    
    // Handle BorderList
    if(anEvent.equals("BorderList")) {
        
        // Get old panel border
        Border border = panel.getBorder();
        
        // Get old border title & font
        String title = RMBorderUtils.getTitle(border);
        Font font = RMBorderUtils.getTitleFont(border);
        
        // Get new border, set title & font, set new border
        border = (Border)anEvent.getValue();
        border = RMBorderUtils.setTitle(border, title);
        RMBorderUtils.setTitleFont(border, font);
        panelShape.setBorder(border);
    }
}*/

/**
 * Returns the class this tool is responsible for.
 */
public Class getShapeClass()  { return SpringsPaneShape.class; }

/**
 * Returns the name to be used for this tool in inspector window title.
 */
public String getWindowTitle()  { return "Panel Inspector"; }

/**
 * Override to suppress handles if at top.
 */
public int getHandleCount(T aShape)
{
    return aShape.getAncestorCount()>1? super.getHandleCount(aShape) : 0;
}
   
/**
 * This class draws the buttons in the border chooser, plus holds utility methods for choosing borders
 */
public static class BorderListCellRenderer extends JToggleButton implements ListCellRenderer {

    // Label used to preview border
    JLabel borderPreview;

    /** Configures a button to draw BorderList cells. */
    public Component getListCellRendererComponent(JList aList, Object aValue, int index, boolean isSelected, boolean hasFocus)
    {
        // Configure size
        setSize(27,27);
        setPreferredSize(getSize());
        
        // Configure selected & enabled
        setSelected(isSelected);
        setEnabled(aList.isEnabled());
        
        // Configure font
        setFont(aList.getFont());
        
        // Configure border preview
        if(borderPreview==null) {
            borderPreview = new JLabel(" ");
            borderPreview.setBounds(5, 5, 17, 17);
            borderPreview.setMaximumSize(borderPreview.getSize());
            borderPreview.setMinimumSize(borderPreview.getSize());
            this.setLayout(null);
            this.add(borderPreview);
        }
        
        // Set border (used DashBorder if empty)
        Border labelBorder = (Border)aValue; if(labelBorder instanceof EmptyBorder) labelBorder = new DashedBorder();
        borderPreview.setBorder(labelBorder);
        return this; // Return component
    }
}

/** A simple dashed-line border class used to show empty borders. */
private static class DashedBorder extends LineBorder {

    public DashedBorder()  { super(Color.DARK_GRAY); }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Graphics2D g2 = (Graphics2D)g; Color oldColor = g2.getColor(); Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[] {1,1}, 0));
        g2.drawLine(x, y, x+width, y); g2.drawLine(x, y+height-1, x+width, y+height-1);
        // Second stroke same as first except for phase of dash prevents vertical dash from colliding with horizontal
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[] {1,1}, 1));
        g2.drawLine(x, y, x, y+height-1); g2.drawLine(x+width-1, y, x+width-1, y+height-1);
        g2.setColor(oldColor); g2.setStroke(oldStroke);
    }

}

}