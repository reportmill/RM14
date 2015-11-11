package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import java.util.*;

/**
 * This class generates (and represents) a RPG'd pie graph area.
 */
public class RMGraphRPGPie extends RMGraphRPG {
    
    // The shape that hold pie graph
    PieGraphShape       _pieShape;

/**
 * Creates a pie graph area for the given graph area and graphRPG.
 */
public RMGraphRPGPie(RMGraph aGraph, ReportOwner anRptOwner)
{
    // Do normal version
    super(aGraph, anRptOwner);
    
    // Set pie shape and configure
    _pieShape = (PieGraphShape)getGraphShape();
    configure();
}

/**
 * Creates the graph shape.
 */
protected RMParentShape createGraphShape()
{
    return _graph.getDraw3D()? new PieGraphShape3D(_graph) : new PieGraphShape2D(_graph);
}

/**
 * Override to force pie charts to be meshed if only one key.
 */
public boolean isMeshed()  { if(_graph.getKeyCount()==1) return false; return super.isMeshed(); }

/**
 * This is the workhorse method that really configures the graph area.
 */
protected void configure()
{
    // Get graph area bounds
    RMRect bounds = _graph.getBoundsInside();
    
    // Get wedge label text
    RMGraphPartLabelAxis labelAxis = _graph.getLabelAxis();
    
    // Get wedge label string
    RMXString barLabelString = labelAxis.getShowAxisLabels()? labelAxis.getXString().clone() : null;
    if(barLabelString!=null)
        barLabelString.setText(labelAxis.getItemKey());
    
    // Get wedge prototype
    RMOvalShape prototype = new RMOvalShape(); prototype.setStrokeColor(RMColor.black);
    
    // Iterate over each graph section and add individual pies
    for(int i=0, iMax=getSectionCount(); i<iMax; i++) { RMGraphSection section = getSection(i);
        
        // Get pie bounds
        RMRect totalBounds = getPieBounds(i, iMax, bounds);
        
        // Declare variable for pie start angle and current sweep
        float startAngle = -90;
        float sweep = 0;

        // Declare variable for pie bounds
        RMRect pieBounds = totalBounds.insetRect(5);

        // If there are wedge labels, reset pieBounds to totalBounds inset by 1/5 width
        if(barLabelString!=null)
            pieBounds = totalBounds.insetRect(totalBounds.width/10, totalBounds.height/10);
        
        // if there are extruded pie wedges, pieBounds is totalBounds inset another 10%
        if(!_graph.getPie().getExtrusionKey().equals(RMGraphPartPie.EXTRUDE_NONE))
            pieBounds = pieBounds.insetRect(pieBounds.width/20);
            
        // Round pieBounds down to largest square in rect
        pieBounds = pieBounds.squareRectInRect();
        
        // Get total value of section
        float total = section.getSectionTotal();

        // If total is zero, then we can't draw wedges. Just add oval and continue
        if(total==0) {
            RMOvalShape oval = new RMOvalShape();
            oval.setFrame(pieBounds);
            _pieShape.addWedge(oval);
            continue;
        }

        // Iterate over section items and add wedges
        for(int j=0, jMax=section.getItemCount(); j<jMax; j++) { RMGraphSection.Item sectionItem = section.getItem(j);
            
            // Get wedge value and wedge group
            float value = sectionItem.getFloatValue();
            RMGroup group = sectionItem.getGroup();
            
            // Update start angle by last sweep and calculate sweep
            startAngle += sweep;
            sweep = value/total*360;
            
            // Create new oval shape for wedge
            _rptOwner.pushDataStack(group);
            RMOvalShape wedge = (RMOvalShape)_rptOwner.rpg(prototype, _graphShape);
            _rptOwner.popDataStack();
            
            // Set wedge bounds to pie bounds and set start angle and sweep
            wedge.setBounds(pieBounds);
            wedge.setStartAngle(startAngle);
            wedge.setSweepAngle(sweep);
            
            // if wedge should be extruded, calculate proper bounds
            String extrusionKey = _graph.getPie().getExtrusionKey();
            boolean extrude = !extrusionKey.equals(RMGraphPartPie.EXTRUDE_NONE) &&
                (extrusionKey.equals(RMGraphPartPie.EXTRUDE_ALL) ||
                (extrusionKey.equals(RMGraphPartPie.EXTRUDE_FIRST) && j==0) ||
                (extrusionKey.equals(RMGraphPartPie.EXTRUDE_LAST) && j==jMax-1));
                
            // If there's an extrusion key and it's false, it must be custom - try evaluating it for a boolean value
            if(!extrude && extrusionKey.length() > 0) 
                extrude = RMKeyChain.getBoolValue(group, extrusionKey);

            // If extruding, reset wedge bounds to extruded bounds
            if(extrude) {
                double wedgeAngle = RMMath.mod(startAngle + sweep/2, 360);
                double extrusionGap = pieBounds.width/20;
                double offsetX = RMMath.cos(wedgeAngle)*extrusionGap;
                double offsetY = RMMath.sin(wedgeAngle)*extrusionGap;
                RMRect wedgeBounds = pieBounds.offsetRect(offsetX, offsetY);
                wedge.setFrame(wedgeBounds);
            }
            
            // Set wedge color
            wedge.setColor(_graph.getColor(j));
            
            // Add wedge to section item
            sectionItem.setBar(wedge);
            
            // Add wedge shape to PieShape
            _pieShape.addWedge(wedge);
        }

        // Add Wedge Labels
        if(barLabelString!=null) {
            
            // Declare rect for last wedge label bounds
            RMRect lastLabelBounds = null;
            
            // Declare local variable for angle
            double lastAngle = 0;

            // Iterate over wedges and create and add wedge label for each
            for(int j=0, jMax=section.getItemCount(); j<jMax; j++) {
                
                // Get current loop section item and item wedge
                RMGraphSection.Item sectionItem = section.getItem(j);
                RMOvalShape wedge = (RMOvalShape)sectionItem.getBar();
                
                // Calcuate percent of 
                float percent = wedge.getSweepAngle()/360*100;
                
                // Calcuate angle of radian that bisects
                double angle = RMMath.mod(wedge.getStartAngle() + wedge.getSweepAngle()/2, 360);
                
                // Get string
                Map map = RMMapUtils.newMap("Percent", percent);
                
                // Add group
                _rptOwner.pushDataStack(sectionItem.getGroup());
                RMXString string = barLabelString.rpgClone(_rptOwner, map, null, true);
                _rptOwner.popDataStack();

                // Get new wedge label text
                RMTextShape label = new RMTextShape(string);
                
                // Set stroke and fill
                if(labelAxis.getStroke()!=null) label.setStrokeColor(labelAxis.getStrokeColor());
                if(labelAxis.getFill()!=null) label.setColor(labelAxis.getColor());
                
                // Have wedge label set size to fit
                label.setBestSize();

                // Calcuate mid point of wedge label bounds from pie bounds extended by 20 pts in width/height
                double labelMidX = pieBounds.getMidX() + RMMath.cos(angle)*(pieBounds.width/2 + 20);
                double labelMidY = pieBounds.getMidY() + RMMath.sin(angle)*(pieBounds.height/2 + 20);
                
                // Calculate wedge label location
                double labelX = angle>=90 && angle <= 270? labelMidX - label.width()/2 : labelMidX;
                double labelY = angle>=90 && angle <= 270? labelMidY - label.height()/2 : labelMidY - label.height()/2;
                
                // Set label position
                label.setXY(labelX, labelY);
                
                // If wedgeLabelBounds intersects lastWedgeLabelBounds, scoot it up or down
                if(j>0 && label.getFrame().insetRect(2).intersectsRect(lastLabelBounds))
                    label.setY(angle>=180? lastLabelBounds.y - label.getHeight()+4 : lastLabelBounds.getMaxY()-4);

                // Add wedge label text to PieShape
                _pieShape.addWedgeLabel(label);

                // draw a line from label to wedge, if specified in template
                if(_graph.getPie().getDrawWedgeLabelLines()) {
                    
                    // Get label frame
                    RMRect labelFrame = label.getFrame();
                    
                    // Declare wedge label line start point to middle of label
                    double startX = labelFrame.getMidX();
                    double startY = labelFrame.getMidY();
                    
                    // Adjust line to edge of label based on angle
                    if(angle>=45 && angle<135) startY = labelFrame.y;
                    else if(angle>=135 && angle<225) startX = labelFrame.getMaxX();
                    else if(angle>=225 && angle<315) startY = labelFrame.getMaxY();
                    else startX = labelFrame.x;
                    
                    // Calculate wedge label line end point
                    double endX = _graph.getWidth()/2 + RMMath.cos(angle)*((wedge.width()/2)*.8f);
                    double endY = _graph.getHeight()/2 + RMMath.sin(angle)*((wedge.width()/2)*.8f);
                    
                    // Create wedge label line shape, set StrokeColor to LightGray and add label line
                    RMLineShape line = new RMLineShape(startX, startY, endX, endY);
                    line.setStrokeColor(RMColor.lightGray);
                    _pieShape.addWedgeLabelLine(line);
                }
                
                // Calculate lastWedgeLabelBounds. If in same quadrant do union, if in new quadrant, copy
                if(j==0 || RMMath.trunc(lastAngle, 90)!=RMMath.trunc(angle, 90))
                    lastLabelBounds = label.getFrame();
                else lastLabelBounds.union(label.getFrame());
                
                // Update last angle
                lastAngle = angle;
            }
        }
    }
}

/**
 * Returns the bounds for an individual pie in the graph area (when there are multiple keys).
 */
private RMRect getPieBounds(int anIndex, int aCount, RMRect aRect)
{
    // Get width & height
    double width = aRect.width;
    double height = aRect.height;
    
    // Get x & y & total
    int x = 1, y = 1, total = x*y;

    // Find number of x & y chunks to break width and height into, such that
    //  ratio of x/y is as close as possible to ratio of width/height
    while(aCount > total) {
        if(Math.abs((x+1.)/y - width/height) <= Math.abs(x/(y+1.) - width/height)) x++;
        else y++;
        total = x*y;
    }

    // Calculate rect for index, assuming index traverses grid from left to right, top to bottom
    return new RMRect(aRect.x + (anIndex%x)*width/x, aRect.y + (y - 1 - anIndex/x)*height/y, width/x, height/y);
}

/**
 * An interface for a shape that renders a bar graph from bar graph pieces.
 */
public interface PieGraphShape extends RMGraphRPG.GraphShape {

    /** Add a wedge shape. */
    public void addWedge(RMShape aShape);
    
    /** Add a wedge label shape. */
    public void addWedgeLabel(RMTextShape aLabel);
    
    /** Add a wedge label line. */
    public void addWedgeLabelLine(RMLineShape aLine);
}

/**
 * A BarGraphShape implementation.
 */
static class PieGraphShape2D extends RMParentShape implements PieGraphShape {

    /** Creates a new PieGraphShape2D. */
    public PieGraphShape2D(RMGraph aGraph)  { copyShape(aGraph); }

    /** Returns the RMGraphRPG. */
    public RMGraphRPG getGraphRPG()  { return _grpg; } RMGraphRPG _grpg;
    
    /** Sets the RMGraphRPG. */
    public void setGraphRPG(RMGraphRPG aGRPG)  { _grpg = aGRPG; }

    /** Implements PieView method to just add wedge shape. */
    public void addWedge(RMShape aBar)  { addChild(aBar); }
    
    /** Implements PieView method to just add wedge label shape. */
    public void addWedgeLabel(RMTextShape aLabel)  { addChild(aLabel); }
    
    /** Implements PieView method to just add wedge label line shape. */
    public void addWedgeLabelLine(RMLineShape aLine)  { addChild(aLine); }
}

/**
 * This graph renders a pie graph in 3D.
 */
static class PieGraphShape3D extends RMScene3D implements PieGraphShape {

    // List of pie wedges, labels and lines
    List <RMShape>    _wedges = new ArrayList();
    List <RMShape>    _labels = new ArrayList();
    List <RMShape>    _lines = new ArrayList();

    /** Creates a new pie view 3d. */
    public PieGraphShape3D(RMGraph aGraph)
    {
        copyShape(aGraph); // Copy graph area attributes
        copy3D(aGraph.get3D()); // Copy 3D attributes from graph area 3D
    }
    
    /** Returns the RMGraphRPG. */
    public RMGraphRPG getGraphRPG()  { return _grpg; } RMGraphRPG _grpg;
    
    /** Sets the RMGraphRPG. */
    public void setGraphRPG(RMGraphRPG aGRPG)  { _grpg = aGRPG; }
    
    /** Adds a wedge shape to graph view. */
    public void addWedge(RMShape aWedge)  { _wedges.add(aWedge); }
    
    /** Adds a wedge label to graph view. */
    public void addWedgeLabel(RMTextShape aLabel)  { _labels.add(aLabel); }
    
    /** Adds a wedge label line to graph view. */
    public void addWedgeLabelLine(RMLineShape aLine)  { _lines.add(aLine); }
    
    /** Returns bar graph's camera transform (overrides Scene3D to make pitch always relative to camera). */
    public RMTransform3D getTransform3D()
    {
        // If pseudo 3d, just use original implementation
        if(isPseudo3D()) return super.getTransform3D();
        
        // Normal transform:    
        RMTransform3D t = new RMTransform3D();
        t.translate(-getWidth()/2, -getHeight()/2, -getDepth()/2);
        t.rotateY(getYaw());
        t.rotate(new RMVector3D(1, 0, 0), getPitch());
        t.rotate(new RMVector3D(0, 0, 1), getRoll3D());
        t.perspective(getFocalLength());
        t.translate(getWidth()/2, getHeight()/2, getDepth()/2);
        
        // Return transform
        return t;
    }
    
    /** Rebuilds 3D representation of shapes from shapes list (called by layout manager). */
    protected void layoutChildren()
    {
        // Remove all existing children
        removeChildren();
        
        // Iterate over wedges and add them as 3D
        for(int i=0, iMax=_wedges.size(); i<iMax; i++) { RMShape wedge = _wedges.get(i);
            addChild3D(wedge, 0, getDepth(), true); }
        
        // Iterate over lines and add them as 3D
        //for(int i=0, iMax=_lines.size(); i<iMax; i++) addChild3D(_lines.get(i), getDepth()/3-5, getDepth()/3-5);
        
        // Create label shapes
        for(int i=0, iMax=_labels.size(); i<iMax && !getValueIsAdjusting(); i++) { RMShape label = _labels.get(i);
            addChild3D(label, -5, -5, false); }
    
        // Do resort
        resort();
    }
}

}