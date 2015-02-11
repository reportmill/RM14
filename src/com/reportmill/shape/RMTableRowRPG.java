package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.shape.RMShapeTable.*;
import java.util.*;

/**
 * Report generation shape for RMTableRow.
 */
public class RMTableRowRPG extends RMParentShape {

    // The TableRow used to do RPG (and potentially an alternate)
    RMTableRow            _row, _row2;
    
    // The group
    RMGroup               _group;
    
    // The parent RPG row
    RMTableRowRPG         _parentRPG;
    
    // Child rows
    List <RMTableRowRPG>  _childRPGs;
    
    // The split version of the row
    RMTableRowRPG         _split;

/**
 * Generate Report for ReportOwner, RMTableRow and group.
 */
public void rpgAll(ReportOwner anRptOwner, RMTableRow aRow, RMGroup aGroup, String aSuggestedVersion)
{
    // Add Group to RptOwner.DataStack
    anRptOwner.pushDataStack(aGroup);
    
    // Get version
    String version = getVersionName(anRptOwner, aRow, aGroup, aSuggestedVersion);
    RMTableRow row = (RMTableRow)aRow.getVersion(version);
    
    _row = aRow; _row2 = row; _group = aGroup; // Set ivars
    copyShape(row); // Copy attributes
    if(!_row2.isStructured()) setLayout(new RMSpringLayout()); // Set layout
    row.rpgChildren(anRptOwner, this); // RPG children
    
    // Set best height
    setBestHeight(); // Set best height
    layout();
    
    // Set Hover to row info - handy debug code for table groups and/or many versions
    //setHover(aRow.getTable().getDatasetKey() + " - " + aRow.getTitle() + " - " + version);
    
    // Handle invisible shapes
    if(!_row2.isStructured()) {
        if(_row2.getDeleteVerticalSpansOfHiddenShapes()) deleteVerticalSpansOfHiddenShapes();
        else if(_row2.getShiftShapesBelowHiddenShapesUp()) shiftShapesBelowHiddenShapesUp();
    }
    
    // Do bindings RPG
    row.rpgBindings(anRptOwner, this);
    
    // Remove Group from RptOwner.DataStack
    anRptOwner.popDataStack();
}

/**
 * Returns the template.
 */
public RMTableRow getTemplate()  { return _row; }

/**
 * Returns the group.
 */
public RMGroup getGroup()  { return _group; }

/**
 * Returns whether this row is header.
 */
public boolean isHeader()  { return _row.getTitle().endsWith("Header"); }

/**
 * Returns whether this row is details.
 */
public boolean isDetails()  { return _row.getTitle().endsWith("Details"); }

/**
 * Returns whether this row is summary.
 */
public boolean isSummary()  { return _row.getTitle().endsWith("Summary"); }

/**
 * Returns the appropriate version.
 */
String getVersionName(ReportOwner anRptOwner, RMTableRow aRow, RMGroup aGroup, String aSuggVersion)
{
    // If SuggestedVersion is present, return it
    if(aSuggVersion!=null && aRow.hasVersion(aSuggVersion))
        return aSuggVersion;
    
    // If group isTopNOthers, check for "TopN Others"/"TopN Others Reprint" and use if available
    if(aGroup.isTopNOthers()) {
        if(RMTableRow.VersionReprint.equals(aSuggVersion) && aRow.hasVersion("TopN Others Reprint")) {
            aGroup.setTopNOthers(false); return "TopN Others Reprint"; }
        if(aRow.hasVersion("TopN Others")) {  // Suppress TopNOthers aggregation since Version exists
            aGroup.setTopNOthers(false); return "TopN Others"; }
    }

    // If VersionKey is set and evaluates to a present version, return it
    if(aRow.getVersionKey()!=null) {
        String version = RMKeyChain.getStringValue(anRptOwner, aRow.getVersionKey());
        if(version!=null && aRow.hasVersion(version))
            return version;
    }

    // Try for FirstOnly and Alternate
    int index = aGroup.index();
    if(index==0 && aRow.hasVersion(RMTableRow.VersionFirstOnly))
        return RMTableRow.VersionFirstOnly;
    if(index%2==1 && aRow.hasVersion(RMTableRow.VersionAlternate))
        return RMTableRow.VersionAlternate;
    
    // Return Standard version
    return RMTableRow.VersionStandard;
}

/**
 * Returns the number of child rpgs.
 */
public int getChildRPGCount()  { return _childRPGs!=null? _childRPGs.size() : 0; }

/**
 * Adds a child row.
 */
public void addChildRPG(RMTableRowRPG aRow)
{
    if(_childRPGs==null) _childRPGs = new ArrayList();
    _childRPGs.add(aRow);
    aRow._parentRPG = this;
}

/**
 * Deletes vertical spans of hidden shapes.
 */
public void deleteVerticalSpansOfHiddenShapes()
{
    // Create list of spans
    SpanList spans = new SpanList();
    
    // Collect hidden shape spans
    for(RMShape child : getChildren())
        if(!child.isShowing())
            spans.addSpan(new Span(child.getFrameY(), getShapeBelowFrameY(this, child)));
    
    // Remove visible shape spans
    if(spans.size()>0)
        for(RMShape child : getChildren())
            if(child.isShowing())
                spans.removeSpan(new Span(child.getFrameY(), getShapeBelowFrameY(this, child)));
    
    // Sort spans and reverse
    Collections.sort(spans);
    Collections.reverse(spans);
    
    // Delete spans
    for(Span span : spans) {
        
        // Iterate over children and shift them up
        for(RMShape child : getChildren())
            if(child.getFrameY()>=span.end)
                child.setFrameY(child.getFrameY() - span.getLength());
        
        // Remove bottom of shape
        setHeight(getHeight() - span.getLength());
    }
}

/**
 * Returns the next shape y for a given parent and child (so we can find the gap).
 */
public static double getShapeBelowFrameY(RMParentShape aParent, RMShape aChild)
{
    double y = aParent.getHeight();
    for(RMShape child : aParent.getChildren())
        if(child!=aChild && child.getFrameY()>aChild.getFrameMaxY() && child.getFrameY()<y)
            y = child.getFrameY();
    return y;    
}

/**
 * Shifts shapes below hidden shapes up.
 */
public void shiftShapesBelowHiddenShapesUp()
{
    // If no hidden shapes, just return
    boolean visible = true;
    for(int i=0, iMax=getChildCount(); i<iMax && visible; i++) visible = getChild(i).isShowing();
    if(visible) return;
    
    // Get max frame y
    float maxFrameY = RMKeyChain.getFloatValue(getChildren(), "max.FrameMaxY");
    
    // Get shapes sorted by FrameY and FrameX
    List <RMShape> shapes = RMSort.sortedList(getChildren(), "FrameY", "FrameX");
    
    // Shift shapes for each hidden shape (from bottom up)
    for(int i=shapes.size()-1; i>=0; i--) { RMShape shape = shapes.get(i);
        if(!shape.isShowing())
            shiftShapesBelowHiddenRect(shapes, shape.getFrame());
    }
    
    // Get new max frame y and remove bottom of shape
    float maxFrameY2 = RMKeyChain.getFloatValue(getChildren(), "max(Visible? FrameMaxY : 0)");
    if(!RMMath.equals(maxFrameY, maxFrameY2))
        setHeight(getHeight() + maxFrameY2 - maxFrameY);
    
    // Reset layout
    ((RMSpringLayout)getLayout()).reset();
}

/**
 * Shifts shapes below hidden rect up.
 */
public void shiftShapesBelowHiddenRect(List <RMShape> theShapes, RMRect aRect)
{
    // Get rect for region below given rect
    RMRect belowRect = aRect.clone();
    belowRect.y = aRect.getMaxY(); belowRect.height = getHeight() - belowRect.y;
    
    // Iterate over shapes and get shape rects, minX/maxX/maxY and sort rects into static and floating lists
    List <RMRect> staticRects = new ArrayList();
    List <RMRect> floatingRects = new ArrayList();
    List <RMShape> floatingRectShapes = new ArrayList();
    
    // Iterate over shapes and get shape rects, minX/maxX/maxY and sort rects into static and floating lists
    for(RMShape shape : theShapes) {
        
        // If shape not visible, just continue
        if(!shape.isShowing())
            continue;
        
        // Get rect and add to list
        RMRect shapeRect = shape.getFrame();
        
        // If shape rect is below, add it to floating rects and expand below rect
        if(belowRect.intersectsRectEvenIfEmpty(shapeRect)) {
            floatingRects.add(shapeRect);
            floatingRectShapes.add(shape);
            belowRect.union(shapeRect);
        }
        
        // Otherwise add to static rects
        else staticRects.add(shapeRect);
    }
    
    // Get max y of floating rects and height to shift rects
    double maxY = getHeight(); for(RMRect rect : floatingRects) maxY = Math.min(maxY, rect.getY());
    double height = maxY - aRect.getY();
    
    // Add height to floating rects
    for(RMRect rect : floatingRects) { rect.y -= height; rect.height += height; }
    
    // Iterate over floating rects
    for(int i=0, iMax=floatingRects.size(); i<iMax; i++) { RMRect floatingRect = floatingRects.get(i);
        for(RMRect staticRect : staticRects) {
            if(floatingRect.intersectsRectEvenIfEmpty(staticRect)) {
                floatingRects.remove(i);
                floatingRectShapes.remove(i);
                floatingRect.y += height; floatingRect.height -= height;
                staticRects.add(floatingRect);
                i = -1; iMax = floatingRects.size();
                break;
            }
        }
    }
    
    // Shift remaining floating rect shapes
    for(RMShape shape : floatingRectShapes)
        shape.setFrameY(shape.getFrameY() - height);
}

/** Override to make selectable. */
public boolean superSelectable()  { return true; }

/**
 * Override to handle structured row.
 */
protected void layoutChildren()
{
    if(!_row2.isStructured()) { super.layoutChildren(); return; }
    float offset = 0;
    for(RMShape child : getChildren()) {
        child.setBounds(offset, 0, child.getWidth(), getHeight()); offset += child.getWidth(); }
}

/**
 * Override to handle structured row.
 */
protected double computePrefHeight(double aWidth)
{
    if(!_row2.isStructured()) return super.computePrefHeight(aWidth);
    double max = getHeight(); for(RMShape child : getChildren()) max = Math.max(max, child.getPrefHeight());
    return max;
}

}