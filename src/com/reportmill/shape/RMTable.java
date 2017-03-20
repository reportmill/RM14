package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.text.RMFont;
import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.beans.*;
import java.util.*;
import snap.util.*;

/**
 * This RMShape subclass provides functionality to graphically represent a list of data in a ReportMill report.
 * It also supports data manipulation such as grouping and sorting.
 */
public class RMTable extends RMParentShape {
    
    // The key chain used to get a list of objects from document's datasource
    String          _datasetKey;
    
    // An optional key chain expression string used to prune the table list derived from dataset key
    String          _filterKey;
    
    // The object that stores a list of groupings (and info like Sorts, ShowHeader, ShowDetails, etc.)
    RMGrouper       _grouper;
    
    // The (optional) grouping index that has an explicit page break
    int             _pageBreakGroupIndex = -1;
    
    // The number of columns that this table repeats horizontally for
    int             _columnCount = 1;
    
    // The space between any columns
    double          _columnSpacing = 10;
    
    // Whether table starts with a page break
    boolean         _startingPageBreak = false;
    
/**
 * Creates a plain table.
 */
public RMTable()  { setSize(400, 375); }

/**
 * Creates a table with given dataset key (and corresponding default grouping and details row).
 */
public RMTable(String aDatasetKey)
{
    // Do normal version
    this();
    
    // Set default dataset key and force default grouping and details row
    setDatasetKey(aDatasetKey);
    addDetails(aDatasetKey);
}

/**
 * Returns the dataset key associated with the table.
 */
public String getDatasetKey()  { return _datasetKey; }

/**
 * Sets the dataset key associated with the table.
 */
public void setDatasetKey(String aKeyPath)
{
    // Get key path with no @-signs
    String keyPath = RMStringUtils.delete(aKeyPath, "@");
    
    // If value already set, just return
    if(RMUtils.equals(keyPath, _datasetKey)) return;
    
    // Set value and fire property change
    firePropertyChange("DatasetKey", keyPath, _datasetKey = keyPath, -1);
    
    // If grouper has grouping with old dataset key, swap it out and update _titleMap entries, too
    if(getGroupingCount()>0) {
    
        // Get last grouping, new dataset key and old dataset key
        RMGrouping grouping = getGrouper().getGroupingLast();
        String newKey = _datasetKey!=null? _datasetKey : "Objects";
        String oldKey = grouping.getKey();
        
        // Rename children
        setTitleForChild(oldKey + " Header", newKey + " Header");
        setTitleForChild(oldKey + " Details", newKey + " Details");
        setTitleForChild(oldKey + " Summary", newKey + " Summary");
        
        // Set new key in grouping
        grouping.setKey(newKey);
    }
}

/**
 * Returns the optional key chain expression string used to prune the table list derived from dataset key.
 */
public String getFilterKey()  { return _filterKey; }

/**
 * Sets the optional key chain expression string used to prune the table list derived from dataset key.
 */
public void setFilterKey(String aKeyExpr)
{
    if(RMUtils.equals(aKeyExpr, _filterKey)) return;
    firePropertyChange("FilterKey", _filterKey, _filterKey = aKeyExpr, -1);
}

/**
 * Returns the grouper associated with the table.
 */
public RMGrouper getGrouper()  { if(_grouper==null) setGrouper(new RMGrouper()); return _grouper; }

/**
 * Sets the grouper associated with the table.
 */
public void setGrouper(RMGrouper aGrouper)
{
    if(_grouper!=null) _grouper.removePropertyChangeListener(this);
    _grouper = aGrouper;
    if(_grouper!=null) _grouper.addPropertyChangeListener(this);
}

/**
 * Returns the number of groups in this table.
 */
public int getGroupingCount()  { return getGrouper().getGroupingCount(); }

/**
 * Returns the individual grouping at the given index.
 */
public RMGrouping getGrouping(int anIndex)  { return getGrouper().getGrouping(anIndex); }

/**
 * Returns the individual grouping that has the same key as the one given.
 */
public RMGrouping getGrouping(String aKey)  { return getGrouper().getGrouping(aKey); }

/**
 * Returns the individual grouping that has the same key as the one given, with option to create at index if missing.
 */
public RMGrouping getGrouping(String aKey, boolean doCreate, int anIndex)
{
    RMGrouping grouping = getGrouper().getGrouping(aKey);
    if(grouping==null && doCreate)
        addGrouping(grouping = new RMGrouping(aKey), anIndex);
    return grouping;
}

/**
 * Adds a grouping to the table for the given key string at the given grouping index.
 */
public void addGroupingKey(String aKey, int anIndex)  { getGrouping(aKey, true, anIndex).setHasDetails(true); }

/**
 * Adds a given grouping to the table at the given grouping index.
 */
public void addGrouping(RMGrouping aGrouping, int anIndex)  { getGrouper().addGrouping(aGrouping, anIndex); }

/**
 * Removes the given grouping.
 */
public void removeGrouping(RMGrouping aGrouping)  { getGrouper().removeGrouping(aGrouping); }

/**
 * Moves the grouping at the first index so that it resides at the second index.
 */
public void moveGrouping(int fromIndex, int toIndex)  { getGrouper().moveGrouping(fromIndex, toIndex); }

/** Convenience to add header for grouping key. */
public RMTableRow addHeader(String aKey)
{
    getGrouping(aKey, true, 0).setHasHeader(true); return getRow(aKey + " Header");
}

/** Convenience to add details for grouping key. */
public RMTableRow addDetails(String aKey)
{
    getGrouping(aKey, true, 0).setHasDetails(true); return getRow(aKey + " Details");
}

/** Convenience to add summary for grouping key. */
public RMTableRow addSummary(String aKey)
{
    getGrouping(aKey, true, 0).setHasSummary(true); return getRow(aKey + " Summary");
}

/**
 * Override to update rows when grouper changes and revalidate when child height changes.
 */
public void propertyChange(PropertyChangeEvent anEvent)
{
    // Do normal version
    super.propertyChange(anEvent);
    
    // If source is grouper or grouping, update table rows to match grouper
    Object source = anEvent.getSource();
    if(source instanceof RMGrouper || source instanceof RMGrouping)
        grouperChanged();
}

/**
 * Called when there is a grouper change to ensure that table rows match groupings.
 */
protected void grouperChanged()
{
    // Get list of all rows and declare variable for row count
    List <RMShape> allRows = new ArrayList(Arrays.asList(getChildArray()));
    int rowCount = 0;
    
    // Iterate over groupings to add header and details bands for each grouping key if appropriate
    for(int i=0, iMax=getGroupingCount(); i<iMax; i++) { RMGrouping grouping = getGrouping(i);
        
        // If grouping has header, add if missing (otherwise remove from list of AllRows), and increment rowcount
        if(grouping.getHasHeader()) {
            RMTableRow row = getRow(grouping.getKey() + " Header");
            if(row==null)
                addChildWithTitle(new RMTableRow(true), rowCount, grouping.getKey() + " Header");
            else RMListUtils.removeId(allRows, row);
            rowCount++;
        }

        // If grouping has details, add if missing (otherwise remove from list of AllRows), and increment rowcount
        if(grouping.getHasDetails()) {
            RMTableRow row = getRow(grouping.getKey() + " Details");
            if(row==null)
                addChildWithTitle(new RMTableRow(true), rowCount, grouping.getKey() + " Details");
            else RMListUtils.removeId(allRows, row);
            rowCount++;
        }
    }

    // Iterate over groupings again and set Group Summary rows
    for(int i=getGroupingCount()-1; i>=0; i--) { RMGrouping grouping = getGrouping(i);

        // If grouping has summary, add if missing (otherwise remove from list of AllRows), and increment rowcount
        if(grouping.getHasSummary()) {
            RMTableRow row = getRow(grouping.getKey() + " Summary");
            if(row==null)
                addChildWithTitle(new RMTableRow(true), rowCount, grouping.getKey() + " Summary");
            else RMListUtils.removeId(allRows, row);
            rowCount++;
        }
    }
    
    // Remove any superfluous rows
    for(RMShape shape : allRows)
        removeChild(shape);
}

/**
 * Returns the index of the grouping in the grouper at which an explicit.
 */
public int getPageBreakGroupIndex()  { return _pageBreakGroupIndex; }

/**
 * Returns the index of the grouping in the grouper at which an explicit.
 */
public void setPageBreakGroupIndex(int aValue)
{
    if(aValue==_pageBreakGroupIndex) return;
    firePropertyChange("PageBreakGroupIndex", _pageBreakGroupIndex, _pageBreakGroupIndex = aValue, -1);
}

/**
 * Returns whether this table should always start on a new page (if in table group).
 */
public boolean getStartingPageBreak()  { return _startingPageBreak; }

/**
 * Sets whether this table should always start on a new page (if in table group).
 */
public void setStartingPageBreak(boolean aFlag)
{
    if(aFlag==_startingPageBreak) return;
    firePropertyChange("StartingPageBreak", _startingPageBreak, _startingPageBreak = aFlag, -1);
}

/**
 * Returns the number of columns that this table should be repeated over on the same page before paginating.
 */
public int getColumnCount()  { return _columnCount; }

/**
 * Sets the number of columns that this table should be repeated over on the same page before paginating.
 */
public void setColumnCount(int aValue)
{
    if(aValue==_columnCount) return;
    firePropertyChange("ColumnCount", _columnCount, _columnCount = aValue, -1);
}

/**
 * Returns the space between a table that has more than one column (int printer points).
 */
public double getColumnSpacing()  { return _columnSpacing; }

/**
 * Sets the space between a table that has more than one column (int printer points).
 */
public void setColumnSpacing(double aValue)
{
    if(aValue==_columnSpacing) return;
    firePropertyChange("ColumnSpacing", _columnSpacing, _columnSpacing = aValue, -1);
}

/**
 * Returns the specific table row at the given index.
 */
public RMTableRow getRow(int anIndex)  { return (RMTableRow)getChild(anIndex); }

/**
 * Returns the specific table row with the given name.
 */
public RMTableRow getRow(String aName)  { return (RMTableRow)getChildWithTitle(aName); }

/**
 * Returns a specific child with the given name.
 */
public RMShape getChildWithTitle(String aTitle)
{
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMTableRow child = getRow(i);
        if(child.getTitle().equals(aTitle))
            return child; }
    return null;
}

/**
 * Sets title for child of given name to new name.
 */
public void setTitleForChild(String aNm1, String aNm2)  { RMTableRow r = getRow(aNm1); if(r!=null) r.setTitle(aNm2); }

/**
 * Convenience to set a child title and add to the table.
 */
public void addChildWithTitle(RMTableRow aChild, int anIndex, String aTitle)
{
    aChild.setTitle(aTitle);
    addChild(aChild, anIndex);
}

/**
 * Returns the bounds rect of the resize bar for the child at the given index.
 */
public RMRect getResizeBarBounds(int anIndex)  { return new RMRect(0, getChild(anIndex).getMaxY(), getWidth(), 16); }

/**
 * Returns the index of the resize bar that is hit by the given point.
 */
public int getResizeBarAtPoint(RMPoint aPoint)
{
    // Iterate over children and return first whose resize bar bounds contains given point
    for(int i=0, iMax=getChildCount(); i<iMax; i++)
        if(aPoint.inRect(getResizeBarBounds(i)))
            return i;
    return -1;
}

/** Editor method - indicates that children should super select immediately when split shape is super selected. */
public boolean childrenSuperSelectImmediately()  { return true; }

/**
 * Performs layout.
 */
protected void layoutChildren()
{
    // Iterate over table rows and reset successive y values based on cumulative height of preceding rows
    double y = 0, maxy = getChildCount()>0? getChildLast().getMaxY() : 0;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMShape child = getChild(i);
        child.setXY(0, y); y += child.getHeight() + 16;
        child.setWidth(getWidth());
    }
    
    // If MaxY changed, repaint all
    if(maxy>0 && !RMMath.equals(maxy, getChildLast().getMaxY())) repaint();
}

/**
 * Report generation for table.
 */
public RMShape rpgAll(ReportOwner anOwner, RMShape aParent)  { return new RMTableRPG(anOwner, this).rpgAll(); }

/**
 * Paints a table shape.
 */
public void paintShape(RMShapePainter aPntr)
{
    // Call normal paintShape (just return if not editing)
    super.paintShape(aPntr); if(!aPntr.isEditing()) return;
    
    // Get table bounds and table base rect
    RMRect bounds = getBoundsInside();
    double y = getChildCount()>0? getChildLast().getMaxY() + 16 : 0;
    RMRect rect = new RMRect(0, y, getWidth(), bounds.getMaxY() - y);
    
    // Set color to light gray and fill
    aPntr.setColor(new Color(11/12f, 11/12f, 11/12f));
    aPntr.fill(rect);

    // Get whether or not to draw super-selected
    boolean drawSuperSelect = aPntr.isSuperSelected(this);

    // Draw dividers for each child; raised & with dimple if super selected and with title if one has been set
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMTableRow child = getRow(i);
        
        // Get current loop row resize bar bounds
        RMRect resizeBarBounds = getResizeBarBounds(i);

        // Turn anti-aliasing off to get sharper lines
        aPntr.setAntialiasing(false);    

        // Set stroke to 1
        aPntr.setStroke(RMAWTUtils.Stroke1);

        // If super-selected, draw resize bar as button
        if(drawSuperSelect) {
            aPntr.drawButton(resizeBarBounds, false);
            aPntr.setColor(Color.gray); // Draw line at top, because button has upper white border from table row
            aPntr.drawLine(1, resizeBarBounds.y, getWidth()-2, resizeBarBounds.y);
        }

        // If not super-selected, draw resize bar flat with gray border
        else {
            aPntr.setColor(Color.lightGray); aPntr.fill(resizeBarBounds);
            aPntr.setColor(Color.darkGray); aPntr.draw(resizeBarBounds);
        }

        // Turn anti-aliasing back on
        aPntr.setAntialiasing(true);
        
        // Draw Title
        aPntr.setColor(Color.black);
        aPntr.setFont(RMAWTUtils.Helvetica12);
        aPntr.drawString(child.getTitle(), (float)resizeBarBounds.x + 10, (float)resizeBarBounds.y + 12);

        // Draw child version name and structured button if child is RMTableRow
        if(drawSuperSelect) {

            // Draw Version name
            String version = child.getVersion();
            double titleWidth = RMFont.Helvetica12.stringAdvance(version);
            double titleX = resizeBarBounds.getMaxX() - titleWidth - 13, titleY = resizeBarBounds.y + 12;
            aPntr.setColor(Color.black);
            aPntr.drawString(version, (float)titleX, (float)titleY);
            
            // Draw drop-down arrow
            GeneralPath p = new GeneralPath();
            p.moveTo(titleX + titleWidth + 2.5f, titleY - 6f);
            p.lineTo(titleX + titleWidth + 5.5f, titleY - 1f);
            p.lineTo(titleX + titleWidth + 8.5f, titleY - 6f);
            p.closePath();
            aPntr.fill(p);
            
            // Draw button Structured button as a white bezel with jail bars
            double buttonX = titleX - 22, buttonY = titleY - 9, buttonW = 18, buttonH = 9;
            aPntr.setColor(Color.white);
            aPntr.fill3DRect(buttonX, buttonY, buttonW, buttonH, false);
            aPntr.setColor(new Color(.94f, .94f, .94f));
            aPntr.fillRect(buttonX + 2, buttonY + 2, buttonW - 4, buttonH - 4);
            
            // Draw jail bars
            if(child.isStructured()) {
                aPntr.setColor(new Color(.1f, .1f, .1f));
                aPntr.drawLine(buttonX+6, buttonY+2, buttonX+6, buttonY + buttonH - 2);
                aPntr.drawLine(buttonX+11, buttonY+2, buttonX+11, buttonY + buttonH - 2);
            }
            
            // If table row is selected draw red line around perimeter
            if(aPntr.isSelected(child) || aPntr.isSuperSelected(child)) {
                
                // Draw red line around perimeter
                Color c = aPntr.isSuperSelectedShape(child)? new Color(1f, 0, 0, .9f) : new Color(1f, 0, 0, .3f);
                aPntr.setColor(c); aPntr.setStroke(RMAWTUtils.StrokeDash1);
                aPntr.draw(child.getBounds().inset(-1));
                
                // Find selected column and highlight it
                if(child.isStructured())
                    for(int j=0, jMax=child.getChildCount(); j<jMax; j++) { RMShape trChild = child.getChild(j);
                        if(aPntr.isSuperSelectedShape(trChild)) {
                            RMRect colRect = convertedRectFromShape(trChild.getBounds().inset(-1), child);
                            aPntr.setColor(new Color(1f, 0, 0));
                            aPntr.draw(colRect); break;
                        }
                    }
            }
        }
    }
}

/**
 * Paints stroke around table after all children have drawn.
 */
public void paintShapeOver(RMShapePainter aPntr)
{
    // Do normal version
    super.paintShapeOver(aPntr);
    
    // If table already draws stroke or if not editing, just return
    if(getStroke()!=null || !aPntr.isEditing()) return;
        
    // Set color and stroke
    aPntr.setColor(Color.darkGray);
    aPntr.setStroke(RMAWTUtils.Stroke1);
        
    // Draw rect with antialiasing off
    aPntr.setAntialiasing(false); aPntr.draw(getBoundsInside());
    aPntr.setAntialiasing(true);
}

/**
 * Override to paint table stroke on top.
 */
public boolean getStrokeOnTop()  { return true; }

/**
 * Standard clone implementation.
 */
public RMTable clone()
{
    // Get normal clone, clone grouper and return
    RMTable clone = (RMTable)super.clone();
    clone.setGrouper(getGrouper().clone());
    return clone;
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("table");
    
    // Archive DatasetKey, FilterKey
    if(_datasetKey!=null && _datasetKey.length()>0) e.add("list-key", _datasetKey);
    if(_filterKey!=null && _filterKey.length()>0) e.add("filter-key", _filterKey);

    // Archive PageBreakGroupIndex, StartingPageBreak, ColumnCount, ColumnSpacing
    if(_pageBreakGroupIndex>=0) e.add("pagebreak", _pageBreakGroupIndex);
    if(_startingPageBreak) e.add("startbreak", true);
    if(_columnCount>1) e.add("columns", _columnCount);
    if(_columnSpacing!=10) e.add("column-spacing", _columnSpacing);

    // Archive grouper's groupings
    for(int i=0, iMax=getGroupingCount(); i<iMax; i++)
        e.add(getGrouping(i).toXML(anArchiver));
    
    // Return xml element
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes and split shape attributes
    super.fromXMLShape(anArchiver, anElement);
    
    // Unarchive DatasetKey, FilterKey
    setDatasetKey(anElement.getAttributeValue("list-key"));
    setFilterKey(anElement.getAttributeValue("filter-key"));

    // Unarchive PageBreakGroupIndex, StartingPageBreak, ColumnCount, ColumnSpacing
    setPageBreakGroupIndex(anElement.getAttributeIntValue("pagebreak", -1));
    setStartingPageBreak(anElement.getAttributeBoolValue("startbreak", false));
    setColumnCount(anElement.getAttributeIntValue("columns", 1));
    setColumnSpacing(anElement.getAttributeFloatValue("column-spacing", 10));
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive table rows
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && RMTableRow.class.isAssignableFrom(childClass)) {
            RMShape shape = (RMTableRow)anArchiver.fromXML(childXML, this);
            addChild(shape);
        }
    }

    // Unarchive grouper's groupings
    List groupings = anArchiver.fromXMLList(anElement, "grouping", null, this);
    RMGrouper grouper = new RMGrouper();
    grouper.addGroupings(groupings);
    setGrouper(grouper);
    
    // Legacy fix for unlikely case that vestigial PageBreak index exists
    if(getPageBreakGroupIndex()>=getGroupingCount()) setPageBreakGroupIndex(-1);
}

/**
 * Standard to string implementation (prints class name and shape bounds).
 */
public String toString() { return getClass().getSimpleName() + " " + getDatasetKey() + " " + getFrame().toString(); }

}