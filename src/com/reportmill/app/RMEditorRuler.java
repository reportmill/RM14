package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.text.*;
import com.reportmill.shape.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.SwingUtils;

/**
 * Handles editor methods specific to rulers.
 */
public class RMEditorRuler extends JComponent {

    // The ruler editor
    RMEditor                  _editor;

    // The ruler orientation
    byte                      _orientation;
    
    // The Mouse Position Marker component
    MousePositionMarker       _mouseMarker = new MousePositionMarker();
    
    // The Shape Position Marker component
    ShapePositionMarker       _shapeMarker = new ShapePositionMarker();
    
    // The corner component
    CornerComponent           _corner = new CornerComponent();
    
    // Units of measure
    RMDocument.Unit           _unitOfMeasure;
    
    // Zoom Factor
    float                     _zoomFactor;
    
    // Ruler units
    Vector                    _rulerUnits = new Vector();
    
    // Min ruler increment
    int                       _minIncrement = -36;
    
    // Max ruler increment
    int                       _maxIncrement = 72;    
    
    // Ruler width
    protected static final int _rulerWidth = 20;
    
    // Constants for ruler orientation
    public static final byte HORIZONTAL = 0;
    public static final byte VERTICAL = 1;
    
    // Tab Stop Icons
    static Icon _tabLeft;
    static Icon _tabRight;
    static Icon _tabCenter;
    static Icon _tabDecimal;
    
/**
 * Creates a new editor ruler.
 */
public RMEditorRuler(RMEditor owner, byte orientation)
{
    // Set editor
    _editor = owner;
    
    // Set orientation
    _orientation = orientation;
    
    // Set unit of measure
    _unitOfMeasure = _editor.getDocument().getUnit();
    
    // Set zoom factor
    _zoomFactor = _editor.getZoomFactor();
    
    // Add mouse location marker
    _mouseMarker.setBounds(0, 0, _mouseMarker.getWidth(), _mouseMarker.getHeight());
    add(_mouseMarker);
    
    // Add shape location marker
    _shapeMarker.setBounds(0,0,0,0);
    add(_shapeMarker);

    // Add ruler units
    for(int i = _minIncrement; i < _maxIncrement; i++) {
        RulerUnit aUnit = new RulerUnit(i);
        _rulerUnits.add(aUnit);
        add(aUnit);
    }

    // register for mouse motion events on editor
    _editor.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) { _mouseMarker.moveTo(e.getPoint()); }
        public void mouseDragged(MouseEvent e) { _mouseMarker.moveTo(e.getPoint()); }
    });
    
    // If images are null, create them
    if(_tabLeft==null) {
        
        // Create tab left image
        Polygon p1 = new Polygon(new int[] { 2, 7, 2}, new int[] { 0, 5, 9 }, 3);
        _tabLeft = SwingUtils.getIcon(p1, Color.black, 9, 9);
        
        // Create tab right image
        Polygon p2 = new Polygon(new int[] { 7, 2, 7}, new int[] { 0, 5, 9 }, 3);
        _tabRight = SwingUtils.getIcon(p2, Color.black, 9, 9);
        
        // Create tab center image
        Polygon p3 = new Polygon(new int[] { 4, 5, 8, 8, 5, 4, 1, 1 }, new int[] { 0, 0, 4, 5, 9, 9, 5, 4 }, 8);
        _tabCenter = SwingUtils.getIcon(p3, Color.black, 9, 9);
        
        // Create tab decimal image
        RoundRectangle2D r1 = new RoundRectangle2D.Float(0, 0, 9, 9, 9, 9);
        RoundRectangle2D r2 = new RoundRectangle2D.Float(3, 3, 3, 3, 3, 3);
        Area a1 = new Area(r1);
        a1.subtract(new Area(r2));
        _tabDecimal = SwingUtils.getIcon(a1, Color.black, 9, 9);
    }
}
    
public int getUnitWidth()
{
    float unitWidth = _unitOfMeasure==RMDocument.Unit.CM || _unitOfMeasure==RMDocument.Unit.MM? 57 : 72;
    return Math.round(unitWidth*_zoomFactor);
}
        
public int getVisibleEdgeX()
{
    double gap = (_editor.getWidth() - _editor.getSelectedPage().getWidth()*_zoomFactor)/2;
    return (int)Math.round(Math.abs(_minIncrement*getUnitWidth()) - gap);
}

public int getVisibleEdgeY()
{
    double gap = (_editor.getHeight() - _editor.getSelectedPage().getHeight()*_zoomFactor)/2;
    return (int)Math.round(Math.abs(_minIncrement*getUnitWidth()) - gap);
}

public int getMinorTickStart()  { return _zoomFactor>0.5? _rulerWidth - 5 : _rulerWidth; }

public int getMajorTickStart()  { return _zoomFactor>0.3? _rulerWidth - 10 : _rulerWidth; }

public void update()
{
    // Update shape marker
    _shapeMarker.update();
    
    // if unit changed, update the labels & resize the unit
    if(_unitOfMeasure != _editor.getDocument().getUnit()) {
        
        // Something
        _unitOfMeasure = _editor.getDocument().getUnit();
        
        // Something
        for(int i=0, iMax=_rulerUnits.size(); i<iMax; i++) {
            RulerUnit u = (RulerUnit)_rulerUnits.get(i);
            u.updateLabel();
            u.sizeUnit();
        }
    }
    
    // if zoom factor changed, resize ruler
    if(_zoomFactor != _editor.getZoomFactor()) {
        
        // Something
        _zoomFactor = _editor.getZoomFactor();
        
        // Something
        for(int i=0, iMax=_rulerUnits.size(); i<iMax; i++) {
            RulerUnit u = (RulerUnit)_rulerUnits.get(i);
            u.sizeUnit();
        }
    }

    // If upper left changed, reposition
    RMRect bounds = _editor.getSelectedPage().getFrame();
    RMPoint upperLeft = _editor.convertPointFromShape(bounds.getOrigin(), _editor.getSelectedPage());
    Rectangle visibleEditorRect = _editor.getVisibleRect();
    
    // Reset Bounds (remove from ScrollPane to avoid flashing?)
    if(_orientation==VERTICAL)
        setBounds(0, _minIncrement*getUnitWidth() + (int)upperLeft.y - visibleEditorRect.y, _rulerWidth, getHeight());
    else setBounds(_minIncrement*getUnitWidth() + (int)upperLeft.x - visibleEditorRect.x, 0, getWidth(), _rulerWidth);
}

public void setNeedsUpdate()
{
    SwingUtilities.invokeLater(new Runnable() {
        public void run() { update(); }});
}

/** Override getPreferredSize to control minimum width/height. */
public Dimension getPreferredSize()
{
    return _orientation==HORIZONTAL?
        new Dimension(Math.abs(_minIncrement*getUnitWidth()) + _maxIncrement*getUnitWidth(), _rulerWidth) : 
        new Dimension(_rulerWidth, Math.abs(_minIncrement*getUnitWidth()) + _maxIncrement*getUnitWidth());
}

protected class RulerUnit extends JComponent
{
    // Increment
    int _increment;
    
    // Increment label
    JLabel _incrementLabel;
    
    public RulerUnit(int anIncrement)
    {
        _increment = anIncrement;

        // add the increment label
        _incrementLabel = new JLabel();
        _incrementLabel.setFont(RMAWTUtils.Arial8);
        _incrementLabel.setBounds(1, 1, 20, 10);
        add(_incrementLabel);
        updateLabel();
        
        // size the unit
        sizeUnit();
        
    }
    
    public Dimension getPreferredSize()
    {
        return _orientation==VERTICAL? new Dimension(_rulerWidth, getUnitWidth()) :
            new Dimension(getUnitWidth(), _rulerWidth);
    }
    
    public void sizeUnit()
    {
        // set the bounds of the unit
        if(_orientation==VERTICAL)
            setBounds(0, Math.abs(_minIncrement*getUnitWidth()) + _increment*getUnitWidth(), _rulerWidth, getUnitWidth());
        else setBounds(Math.abs(_minIncrement*getUnitWidth()) + _increment*getUnitWidth(), 0, getUnitWidth(), _rulerWidth);
    }
    
    public void updateLabel()
    {
	    int factor = 1;

	    switch(_unitOfMeasure) {
    	    case Inch: factor = 1; break;
    	    case Point: factor = 72; break;
    	    case CM: factor = 2; break;
    	    case MM: factor = 20; break;
    	    case Pica: factor = 6; break;
	    }
	
        //int factor = _unitOfMeasure==RMDocument.UNIT_CM? 2 : _unitOfMeasure==RMDocument.UNIT_POINTS? 72 : 1;
        _incrementLabel.setText("" + _increment*factor);
    }
    
    public void paintComponent(Graphics g)
    {
        // Paint background
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Paint ticks
        if(_unitOfMeasure==RMDocument.Unit.CM || _unitOfMeasure==RMDocument.Unit.MM)
	        paintCentTicks(g);
        else paintInchTicks(g);
    }
    
    protected void paintInchTicks(Graphics g)
    {
        if(_orientation==VERTICAL) {
            // stroke long edges
            g.setColor(Color.black);
            g.drawLine(0,0,0,getHeight());
            g.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
            
            // draw the ticks
            g.setColor(Color.darkGray);
            g.drawLine(0,0,_rulerWidth,0);
            g.drawLine(getMinorTickStart(), Math.round(9*_zoomFactor),_rulerWidth, Math.round(9*_zoomFactor));
            g.drawLine(getMajorTickStart(), Math.round(18*_zoomFactor),_rulerWidth, Math.round(18*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(27*_zoomFactor),_rulerWidth, Math.round(27*_zoomFactor));
            g.drawLine(getMajorTickStart(), Math.round(36*_zoomFactor),_rulerWidth, Math.round(36*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(45*_zoomFactor),_rulerWidth, Math.round(45*_zoomFactor));
            g.drawLine(getMajorTickStart(), Math.round(54*_zoomFactor),_rulerWidth, Math.round(54*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(63*_zoomFactor),_rulerWidth, Math.round(63*_zoomFactor));
        }
        
        else {
            // stroke long edges
            g.setColor(Color.black);
            g.drawLine(0,0,getWidth(),0);
            g.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
        
            // draw the ticks
            g.setColor(Color.darkGray);
            g.drawLine(0,0,0,_rulerWidth);
            g.drawLine( Math.round(9*_zoomFactor),getMinorTickStart(), Math.round(9*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(18*_zoomFactor),getMajorTickStart(), Math.round(18*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(27*_zoomFactor),getMinorTickStart(), Math.round(27*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(36*_zoomFactor),getMajorTickStart(), Math.round(36*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(45*_zoomFactor),getMinorTickStart(), Math.round(45*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(54*_zoomFactor),getMajorTickStart(), Math.round(54*_zoomFactor),_rulerWidth);
            g.drawLine( Math.round(63*_zoomFactor),getMinorTickStart(), Math.round(63*_zoomFactor),_rulerWidth);
        }
    }
    
    protected void paintCentTicks(Graphics g)
    {
        // Draw vertical ticks
        if(_orientation==VERTICAL) {
            
            // Stroke long edges
            g.setColor(Color.black);
            g.drawLine(0,0,0,getHeight());
            g.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
            
            // Draw the ticks
            g.setColor(Color.darkGray);
            g.drawLine(0,0,_rulerWidth,0);
            g.drawLine(getMinorTickStart(), Math.round(6*_zoomFactor),_rulerWidth, Math.round(6*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(12*_zoomFactor),_rulerWidth, Math.round(12*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(18*_zoomFactor),_rulerWidth, Math.round(18*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(24*_zoomFactor),_rulerWidth, Math.round(24*_zoomFactor));
            g.drawLine(getMajorTickStart(), Math.round(29*_zoomFactor),_rulerWidth, Math.round(29*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(34*_zoomFactor),_rulerWidth, Math.round(34*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(40*_zoomFactor),_rulerWidth, Math.round(40*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(46*_zoomFactor),_rulerWidth, Math.round(46*_zoomFactor));
            g.drawLine(getMinorTickStart(), Math.round(52*_zoomFactor),_rulerWidth, Math.round(52*_zoomFactor));
        }
        
        // Draw horizontal ticks
        else {
            
            // Stroke long edges
            g.setColor(Color.black);
            g.drawLine(0,0,getWidth(),0);
            g.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
        
            // Draw the ticks
            g.setColor(Color.darkGray);
            g.drawLine(0,0,0,_rulerWidth);
            g.drawLine(Math.round(6*_zoomFactor),getMinorTickStart(), Math.round(6*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(12*_zoomFactor),getMinorTickStart(), Math.round(12*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(18*_zoomFactor),getMinorTickStart(), Math.round(18*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(24*_zoomFactor),getMinorTickStart(), Math.round(24*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(29*_zoomFactor),getMajorTickStart(), Math.round(29*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(34*_zoomFactor),getMinorTickStart(), Math.round(34*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(40*_zoomFactor),getMinorTickStart(), Math.round(40*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(46*_zoomFactor),getMinorTickStart(), Math.round(46*_zoomFactor),_rulerWidth);
            g.drawLine(Math.round(52*_zoomFactor),getMinorTickStart(), Math.round(52*_zoomFactor),_rulerWidth);
        }
    }
}

/**
 * An inner class to draw mouse position indicator.
 */
protected class MousePositionMarker extends JComponent {

    public Dimension getPreferredSize()
    {
        return _orientation==HORIZONTAL? new Dimension(1,_rulerWidth) : new Dimension(_rulerWidth,1);
    }
    
    public void paintComponent(Graphics g)
    {
        if(_orientation==VERTICAL)
            for(int i=0; i<_rulerWidth; i++) {
                g.setColor(i%2==0? Color.black : Color.white); g.drawLine(i, 0, i+1, 0); }
        else for(int i=0; i<_rulerWidth; i++) {
            g.setColor(i%2==0? Color.black : Color.white); g.drawLine(0, i, 0, i+1); }
    }
    
    public int getPosition()  { return _orientation==VERTICAL? getY() : getX(); }
    
    public void moveTo(Point p)
    {
        if(_orientation == VERTICAL)
            setBounds(0, p.y + getVisibleEdgeY(), _rulerWidth, 1);
        else setBounds(p.x + getVisibleEdgeX(), 0, 1, _rulerWidth);
    }
}

/**
 * An inner class to draw shape position indicator.
 */
protected class ShapePositionMarker extends JComponent {

    // Paragraph object
    RMParagraph     _paragraph;
    
    // Tab stops
    List <TabStop>  _tabStops = new Vector();
    
    // The mouse pressed x
    int             _mousePressedX;
    
    /** Creates new shape position marker. */
    public ShapePositionMarker()
    {
        // Set background color
        setBackground(new Color(255,255,255,125));
        
        // If orientation is vertical, just return
        if(_orientation==VERTICAL) return;
        
        // Track mouse pressed x
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { _mousePressedX = e.getX(); }});
        
        // Create "Add Tab" menu item
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addTabItem = new JMenuItem("Add Tab");
        addTabItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addTab(_mousePressedX); }});
        popupMenu.add(addTabItem);
        setComponentPopupMenu(popupMenu);
    }
    
    /** Inserts a tab. */
    public void addTab(float aLocation)
    {
        // Create and add new tab stop
        _tabStops.add(new TabStop(aLocation, RMParagraph.TAB_LEFT));
        
        // Update currently selected RMText's paragraph
        updateParagraph();
        
        // Register for repaint
        repaint();
    }
    
    /** Paints shape marker component. */
    public void paintComponent(Graphics g)
    {
        // Set color
        g.setColor(getBackground());
        
        // Declare variable for marker
        Polygon marker;
        
        // If horizontal, create marker
        if(_orientation == HORIZONTAL) {
            int w = getWidth(), h = getHeight() - 2;
            marker = new Polygon(new int[] {0, w/2-3, w/2, w/2+3, w, w, 0}, new int[] {1, 1, 7, 1, 1, h, h}, 7);
        }
        
        // If vertical ruler, create marker
        else {
            int w = getWidth() - 2, h = getHeight();
            marker = new Polygon(new int[] {1, w, w, 1, 1, 7, 1}, new int[] {0, 0, h, h, h/2+3, h/2, h/2-3}, 7);
        }
        
        // Fill polygon
        g.fillPolygon(marker);
    }
    
    public void update()
    {
        // Get currently selected shape (just return if null)
        RMShape shape = _editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
        
        // If shape is RMText and we're editing, add tab stops
        if(shape instanceof RMTextShape && _orientation==HORIZONTAL && _editor.getTextEditor()!=null) {
            
            // get the tabs from the paragraph style of the selected text
            _paragraph = _editor.getTextEditor().getInputParagraph();
            
            // Delete existing tab stop components (if any)
            this.removeAll();
            _tabStops.clear();
            
            // Add the new tabStops
            for(int i=0, iMax=_paragraph.getTabCount(); i<iMax; i++) {
                TabStop newStop = new TabStop(_paragraph.getTab(i), _paragraph.getTabType(i));
                _tabStops.add(newStop);
                this.add(newStop);
            }
        }
        
        // just make sure there aren't any tabs lying around
        else removeAll();
        
        // Register for repaint
        repaint();

        // Reposition to match shape bounds
        if(!(shape instanceof RMDocument) && !(shape instanceof RMPage)) {
            
            // Get shape bounds
            RMRect bounds = shape.getFrame();
            
            // Get shape origin in editor coords
            RMPoint point = _editor.convertPointFromShape(bounds.getOrigin(), shape.getParent());
            
            // If vertical, set bounds for vertical marker
            if(_orientation==VERTICAL)
                setBounds(0, (int)point.y + getVisibleEdgeY(), _rulerWidth, (int)Math.round(bounds.height*_zoomFactor));
            
            // If horizontal, set bounds for horizontal marker
            else setBounds((int)point.x + getVisibleEdgeX(), 0, (int)Math.round(bounds.width*_zoomFactor), _rulerWidth);
        }
        
        // If shape is doc or page, set bounds to zero
        else setBounds(0,0,0,0);
    } 
    
    /** Updates currently selected text shape paragraph from tab stops. */
    protected void updateParagraph()
    {
        // If no paragraph, just return
        if(_paragraph==null) return;
        
        // Iterate over tab stops till we find one that's different
        for(int i=0, iMax=_tabStops.size(); i<iMax; i++) { TabStop tabStop = _tabStops.get(i);
            
            // If different from paragraph, update paragraph
            if(i>=_paragraph.getTabCount() || _paragraph.getTab(i)!=tabStop._location ||
                    _paragraph.getTabType(i)!=tabStop._type) {
                _paragraph = _paragraph.deriveTab(i, tabStop._location, tabStop._type);
                break;
            }
        }
        
        // Update editor text editor input paragraph
        _editor.getTextEditor().setInputParagraph(_paragraph);
    }

    /** An inner class to represent, draw and manipulate tab stops. */
    protected class TabStop extends JLabel {
    
        // Tab stop location
        float      _location;
        
        // Tab stop type
        char       _type;
        
        // Whether tab stop was dragged
        boolean    _didDrag = false;
        
        /** Creates a new tab stop for the given location and type. */
        public TabStop(float aLocation, char aType)
        {
            // Set tab location
            setLocationAndType(aLocation, aType);
            
            // Create "Make Left Tab" menu
            JMenuItem makeLeftTabItem = new JMenuItem("Make Left Tab");
            makeLeftTabItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                setLocationAndType(_location, RMParagraph.TAB_LEFT);
                updateParagraph();
                repaint();
            }});
            
            // Create "Make Right Tab" menu
            JMenuItem makeRightTabItem = new JMenuItem("Make Right Tab");
            makeRightTabItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                setLocationAndType(_location, RMParagraph.TAB_RIGHT);
                updateParagraph();
                repaint();
            }});
            
            // Create "Make Center Tab" menu
            JMenuItem makeCenterTabItem = new JMenuItem("Make Center Tab");
            makeCenterTabItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                setLocationAndType(_location, RMParagraph.TAB_CENTER);
                updateParagraph();
                repaint();
            }});
            
            // Create "Make Decimal Tab" menu
            JMenuItem makeDecimalTabItem = new JMenuItem("Make Decimal Tab");
            makeDecimalTabItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                setLocationAndType(_location, RMParagraph.TAB_DECIMAL);
                updateParagraph();
                repaint();
            }});
            
            // Create "Delete tab" menu
            JMenuItem deleteTabItem = new JMenuItem("Delete Tab");
            deleteTabItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                _tabStops.remove(TabStop.this);
                ShapePositionMarker.this.remove(TabStop.this);
                updateParagraph();
            }});
            
            // Add "Make Left Tab", "Make Right Tab" and "Delete Tab" menu items
            JPopupMenu tabPopupMenu = new JPopupMenu();
            tabPopupMenu.add(makeLeftTabItem);
            tabPopupMenu.add(makeRightTabItem);
            tabPopupMenu.add(makeCenterTabItem);
            tabPopupMenu.add(makeDecimalTabItem);
            tabPopupMenu.add(deleteTabItem);
            setComponentPopupMenu(tabPopupMenu);
            
            // Make tabs draggable
            addMouseMotionListener(new MouseMotionAdapter() { public void mouseDragged(MouseEvent e) {
                
                // If mouse is way above or below tab stop, assume they want tab to go away, otherwise calc new location
                if(e.getY()>35 || e.getY()<-30)
                    setLocationAndType(-999, _type);
                else setLocationAndType(getX()/_zoomFactor + e.getX()/_zoomFactor, _type);

                // Set did drag ivar
                _didDrag = true;
            }});
            
            // If mouse dragged, update paragraph style when mouse released
            addMouseListener(new MouseAdapter() { public void mouseReleased(MouseEvent e) {
                    if(_didDrag) {
                        updateParagraph(); _didDrag = false; }
            }});
        }
        
        /** Sets tab stop type. */
        public void setLocationAndType(float aLocation, char aType)
        {
            // Set location
            _location = aLocation;
            
            // Get bounds x location
            int x = Math.round(_location*_zoomFactor);
            
            // Set type and handle If left tab, set icon left
            switch(_type=aType) {
                case RMParagraph.TAB_LEFT: setIcon(_tabLeft); x -= 2; break;
                case RMParagraph.TAB_RIGHT: setIcon(_tabRight); x -= 6; break;
                case RMParagraph.TAB_CENTER: setIcon(_tabCenter); x -= 4; break;
                case RMParagraph.TAB_DECIMAL: setIcon(_tabDecimal); x -= 4; break;
            }
            
            // Set tab bounds
            setBounds(x, 9, 9, 11);            
        }
    }
}

/**
 * An inner class to act as corner of scroll pane, in case we want to use it to add guides or something.
 */
protected class CornerComponent extends JComponent
{
    /** Returns preferred size from rulers. */
    public Dimension getPreferredSize() { return new Dimension(_rulerWidth, _rulerWidth); }
    
    /** Paints light gray rect. */
    public void paintComponent(Graphics g)
    {
        g.setColor(Color.lightGray); g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(Color.black); g.drawRect(0,0,getWidth(),getHeight());
    }
}
        
}