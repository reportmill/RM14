package com.reportmill.shape;
import com.reportmill.base.*;
import java.util.*;

/**
 * Report generation for table group.
 */
class RMTableGroupRPG extends RMTableRPG {

    // The table group
    RMTableGroup           _tgroup;
    
    // A map of current child table for table
    Map <RMTable,RMTable>  _childTables;
    
    // A map of current data for tables
    Map <RMTable,RMGroup>  _groups;

/**
 * Creates a new RMTableRPGXTableGroup.
 */
public RMTableGroupRPG(ReportOwner anRptOwner, RMTableGroup aTableGroup, RMTable aTable)
{
    super(anRptOwner, aTable); _tgroup = aTableGroup;
    copyShape(aTableGroup);
    
    // Legacy: RM13 used to base attributes on first table instead of table group
    RMTable table = _tgroup.getChildTableCount()>0? _tgroup.getChildTable(0) : null;
    if(table!=null && (table.getStroke()!=null || table.getFill()!=null)) {
        if(table.getStroke()!=null) setStroke(table.getStroke().clone());
        if(table.getFill()!=null) setFill(table.getFill().clone()); }
}

/**
 * Do RPG.
 */
public RMShape rpgAll()
{
    // Initialize ChildTables/Groups
    _childTables = new HashMap(); _groups = new HashMap();
    
    // Add Rows for group
    List <RMTable> tables = _tgroup.getChildTables();
    for(int i=0, iMax=tables.size(); i<iMax; i++) { RMTable table = tables.get(i);
    
        // If there is a Visible binding and it evaluates to false, continue
        if(table.getBinding("Visible")!=null) { String key = table.getBinding("Visible").getKey();
            if(!RMKeyChain.getBoolValue(_rptOwner, key))
                continue; }
        
        // If StartingPageBreak, add page
        if(table.getStartingPageBreak() && i>0) getPageLast().addPage();
        
        // Do table RPG
        rpgTable(table);
    }
    
    // If only one page generated, return it, otherwise return ShapeList
    ReportOwner.ShapeList slist = new ReportOwner.ShapeList(); //if(_nextPage==null) return this;
    for(RMTableRPG pg=this; pg!=null; pg=pg._nextPage) slist.addChild(pg);
    return slist;
}

/**
 * Creates a page.
 */
protected RMTableRPG createPage()
{
    RMTableGroupRPG page = new RMTableGroupRPG(_rptOwner, _tgroup, _table);
    page._childTables = _childTables; page._groups = _groups;
    return page;
}

/**
 * A hook to add extra rows or such at bottom of table.
 */
protected boolean addRowsExtra(RMGroup aGroup, RMTableRowRPG aParentRPG, RMTableRowRPG theLastRow)
{
    // Get current child table
    List <RMTable> ctables = _tgroup.getChildTables(_table); if(ctables==null || ctables.size()==0) return true;
    RMTable cctable = _childTables.get(_table);
    
    // Iterate over tables
    for(int i=0, iMax=ctables.size(); i<iMax; i++) { RMTable ctable = ctables.get(i);
        
        // If current table set and loop table isn't it, skip
        if(cctable!=null && ctable!=cctable) continue;
        else cctable = null;
        
        // If there is a Visible binding and it evaluates to false, continue
        if(ctable.getBinding("Visible")!=null) { String key = ctable.getBinding("Visible").getKey();
            _rptOwner.pushDataStack(aGroup);
            boolean visible = RMKeyChain.getBoolValue(_rptOwner, key);
            _rptOwner.popDataStack();
            if(!visible)
                continue;
        }

        // Get current group, evaluate if not yet set
        RMGroup group = _groups.get(ctable);
        
        // If first pass, get dataset/group for table and check for Table.StartingPageBreak
        if(group==null) {
            _rptOwner.pushDataStack(aGroup);
            group = getGroup(ctable); group.setParent(aGroup);
            _rptOwner.popDataStack();
            
            // If Table.StartingPageBreak, set LastRow+Group and return
            if(ctable.getStartingPageBreak() && i>0) {
                _lastRow = new RMTableRowRPG(); _lastRow._group = group; 
                _childTables.put(_table, ctable); _groups.put(ctable, group); return false;
            }
        }
        
        // If returning from page break and group matches LastRow.Group, clear LastRow
        else if(theLastRow!=null && theLastRow._group==group && theLastRow._row==null)
            theLastRow = null;
    
        // Add rows for child table
        RMTable oldTable = _table; _table = ctable;  //_rptOwner.pushDataStack(group);
        boolean rval = addRows(group, aParentRPG, theLastRow); _table = oldTable;  //_rptOwner.popDataStack();
        
        // If successful, remove table/group from current maps, otherwise add them
        if(rval) {
            _childTables.remove(oldTable); _groups.remove(ctable); }
        else { _childTables.put(oldTable, ctable); _groups.put(ctable, group); return false; }
        theLastRow = null;
    }
    
    // Return true
    return true;
}

}