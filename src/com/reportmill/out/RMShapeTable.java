package com.reportmill.out;
import com.reportmill.base.*;
import com.reportmill.shape.*;
import java.util.*;

/**
 * This class creates a table from a given list of shapes.
 */
public class RMShapeTable {

    // The columns
    List <STCol>  _cols = new ArrayList();
    
    // The rows
    List <STRow>  _rows = new ArrayList();
    
    // The cells
    STCell        _cells[][];
    
    // Cells within this horizontal or vertical distance are considered to be aligned.
    static final double CELL_ALIGNMENT_TOLERANCE = .5;
    
/** Returns the number of columns. */
public int getColumnCount()  { return _cols.size(); }

/** Returns the individual column at index. */
public STCol getColumn(int anIndex)  { return _cols.get(anIndex); }

/** Returns the number of rows. */
public int getRowCount()  { return _rows.size(); }

/** Returns the individual row at index. */
public STRow getRow(int anIndex)  { return _rows.get(anIndex); }

/** Returns the cell at index. */
public STCell getCell(int aRow, int aCol)  { return _cells[aRow][aCol]; }

/**
 * A class for rows.
 */
public static class STRow {
    public STRow(double aH)  { _height = aH; } double _height;
    public double getHeight()  { return _height; }
}

/**
 * A class for columns.
 */
public static class STCol {
    public STCol(double aW)  { _width = aW; } double _width;
    public double getWidth()  { return _width; }
}

/**
 * A class for cells.
 */
public static class STCell extends RMShape {
    int _row, _col, _rspan, _cspan;
    public int getRow()  { return _row; }
    public int getColumn()  { return _col; }
    public int getRowSpan()  { return _rspan; }
    public int getColumnSpan()  { return _cspan; }
    void setInfo(int row, int col, int rspan, int cspan)  { _row = row; _col = col; _rspan = rspan; _cspan = cspan; }
    public RMShape getCellShape()  { return _cshape; } RMShape _cshape;
}

/**
 * Takes a flattened list of shapes and builds a single RMCrossTab.
 * topLevel is a common ancestor of all the shapes (usually the RMPage)
 * minTableRect, if non-null, specifies a minimum size and origin for the final table.
 */
public static RMShapeTable createTable(List <RMShape> shapes, RMShape topLevel, RMRect minTableRect)
{
    // Get number of shapes (just return if no shapes and no min-table rect)
    int shapeCount = shapes.size(); if(shapeCount==0 && minTableRect==null) return null;
    
    int arraySizes = 2*shapeCount + (minTableRect!=null ? 2 : 0);
    double rowStarts[] = new double[arraySizes];
    double colStarts[] = new double[arraySizes];
    RMRect cellRects[] = new RMRect[shapeCount];
    RMRect bounds, maxBounds=null;
    
    // Iterate over shapes: Fill row & column boundary arrays   (beware of roll/scale/skew)
    for(int i=0; i<shapeCount; i++) { RMShape shape = shapes.get(i);
        
        // Get shape bounds
        if(shape instanceof RMTextShape) bounds = shape.getParent().convertRectToShape(shape.getFrame(), topLevel);
        else bounds = shape.convertRectToShape(shape.getBoundsMarked(), topLevel);

        // Toss away cells whose size is less than the alignment tolerance.
        if(bounds.getWidth()<=CELL_ALIGNMENT_TOLERANCE || bounds.getHeight()<=CELL_ALIGNMENT_TOLERANCE) {
            cellRects[i] = null; continue; }
        
        rowStarts[2*i] = bounds.getY(); rowStarts[2*i+1] = bounds.getMaxY();
        colStarts[2*i] = bounds.getX(); colStarts[2*i+1] = bounds.getMaxX();
        cellRects[i] = bounds;
        
        // Bounds of final table is union of all the cell rects
        if(maxBounds==null) maxBounds = new RMRect(bounds);
        else maxBounds.union(bounds);
    }
    
    // Add row/column boundaries to represent the full table bounds
    if(minTableRect != null) {
        rowStarts[2*shapeCount] = minTableRect.getY(); rowStarts[2*shapeCount+1] = minTableRect.getMaxY();
        colStarts[2*shapeCount] = minTableRect.getX(); colStarts[2*shapeCount+1] = minTableRect.getMaxX();
        if(maxBounds==null) maxBounds = new RMRect(minTableRect);
        else maxBounds.union(minTableRect);
    }
    
    // Sort both arrays min->max
    Arrays.sort(rowStarts);
    Arrays.sort(colStarts);
    
    // Remove duplicates
    int numRowBoundaries = uniqueArray(rowStarts, CELL_ALIGNMENT_TOLERANCE);
    int numColBoundaries = uniqueArray(colStarts, CELL_ALIGNMENT_TOLERANCE);

    // If no rows or boundaries were defined, just return
    if(numRowBoundaries<2 || numColBoundaries<2)
        return null;
    
    int numRows = numRowBoundaries-1;
    int numCols = numColBoundaries-1;
    STCell cells[][] = new STCell[numRows][numCols];
    
    // Walk through converted rects and assign row-column spans
    for(int i=0; i<shapeCount; i++) {
        
        // skip any cells we've tossed earlier
        if(cellRects[i]==null) continue;
        
        // find origin row,column
        int col = binarySearch(colStarts, 0, numColBoundaries-1, cellRects[i].getX(), CELL_ALIGNMENT_TOLERANCE);
        int row = binarySearch(rowStarts, 0, numRowBoundaries-1, cellRects[i].getY(), CELL_ALIGNMENT_TOLERANCE);
        if(row<0 || col<0 || row>=numRows || col>=numCols)
            throw new RuntimeException("Internal Error : search failed");
        if(cells[row][col] != null) {
            printOverlapWarning(); continue; }
        
        // Create a new cell for shape and add to cells
        RMShape shape = shapes.get(i);
        STCell newCell = new STCell(); newCell._cshape = shape;
        RMShape s = shape; while(s!=null && s.getFill()==null) s = s.getParent();
        if(s!=null) newCell.setFill(s.getFill());
        cells[row][col] = newCell;
        
        // Declare variables for row span & column span
        int rowspan, colspan;
        
        // Find the rowspan
        for(rowspan=1; row+rowspan<numRowBoundaries; ++rowspan) {
            if(Math.abs(rowStarts[row+rowspan] - cellRects[i].getMaxY()) <= CELL_ALIGNMENT_TOLERANCE)
                break;
            if(row+rowspan==numRows) // Another option is to make loop only go to numRows and check (or not) once at end
                throw new RuntimeException("Internal error: couldn't find last row boundary");
            if(cells[row+rowspan][col]!=null) printOverlapWarning();
            else cells[row+rowspan][col] = newCell;
        }
        
        // Find column span
        for(colspan=1; col+colspan<numColBoundaries; ++colspan) {
            if(Math.abs(colStarts[col+colspan] - cellRects[i].getMaxX()) <= CELL_ALIGNMENT_TOLERANCE)
                break;
            if(col+colspan == numCols)
                throw new RuntimeException("Internal error: couldn't find last column boundary");
            for(int r = 0; r < rowspan; ++r) {
                if(cells[row+r][col+colspan] != null) printOverlapWarning();
                else cells[row+r][col+colspan] = newCell;
            }
        }
        
        newCell.setInfo(row, col, rowspan, colspan);
        
        // Set the cell's frame and add it to the table
        newCell.setFrame(colStarts[col] - maxBounds.getX(), rowStarts[row] - maxBounds.getY(),
            colStarts[col+colspan]-colStarts[col], rowStarts[row+rowspan]-rowStarts[row]);
    }
    
    // Set bounds of new table to cover all the cells
    RMShapeTable stable = new RMShapeTable(); //newTable.setFrame(maxBounds);
    stable._cells = cells;
    
    // Create rows & columns.
    for(int i=0; i<numRows; ++i) stable._rows.add(new STRow(rowStarts[i+1]-rowStarts[i]));
    for(int i=0; i<numCols; ++i) stable._cols.add(new STCol(colStarts[i+1]-colStarts[i]));
    
    // Create empty cells for any area that not covered by a real cell
    fillInCells(cells, rowStarts, colStarts, maxBounds);
    _pow = false;
    
    // Return table
    return stable;
}

/**
 * Removes any duplicate entries in sorted array.
 * Elements are considered to be duplicates if they are within the specified tolerance of each other.
 * Returns the number of unique entries in the array.
 */
static int uniqueArray(double array[], double tolerance)
{
    int n = array.length; if(n==0) return 0;
    int last_unique_entry = 0;
    for(int i=1; i<n; i++)
        if(Math.abs(array[i]-array[last_unique_entry])>tolerance)
            array[++last_unique_entry] = array[i];
    return last_unique_entry+1;
}

/** 
 * Like the Arrays.binarySearch(), but allows you to specify a starting range 
 * in the array as well as a floating-point tolerance for equality comparisons.
 */
static int binarySearch(double array[], int first, int last, double value, double tolerance)
{
    // Iterate while first is less than last    
    while(first<=last) {
        int middle = (first+last)/2;
        if(Math.abs(array[middle]-value) <= tolerance) return middle;
        else if (value<array[middle]) last = middle-1;
        else first = middle+1;
    }
    
    // Return -1 since value not found
    return -1;
}

/**
 * Creates RMCells for any empty (null) cells.
 * Tries to coalesce neighboring empty cells into rectangular regions.
 */
static void fillInCells(STCell cells[][], double rowStarts[], double colStarts[], RMRect maxBounds)
{
    // Iterate over cells
    for(int row=0, rowCount=cells.length; row<rowCount; row++) {
        for(int col=0, colCount=cells[row].length; col<colCount; col++) {
            
            // Find a horizontal span of 1 or more null cells and fill them with a new cell
            if(cells[row][col]==null) {
                
                STCell newCell = new STCell();
                
                // Find colspan
                int colspan = 0;
                do {
                    cells[row][col+colspan] = newCell; ++colspan;
                } while((col+colspan<colCount) && (cells[row][col+colspan]==null));
                
                // Cell now spans 1 row and colspan cols, extend rowspan if cells col->col+colspan below also empty
                int rowspan = 1, c;
                while((row+rowspan<rowCount) && (cells[row+rowspan][col]==null)) {
                    
                    // 
                    for(c=0; c<colspan; ++c)
                        if(cells[row+rowspan][col+c]!=null)
                            break;
                    
                    // found matching empty region.  Fill in the array
                    if(c==colspan) {
                        for(c=0; c<colspan; ++c)
                            cells[row+rowspan][col+c] = newCell;
                        ++rowspan;
                    }
                    else break;
                }
                
                newCell.setInfo(row, col, rowspan, colspan);
                
                // Set the cell's bounds
                newCell.setFrame(colStarts[col] - maxBounds.getX(), rowStarts[row] - maxBounds.getY(),
                    colStarts[col+colspan]-colStarts[col], rowStarts[row+rowspan]-rowStarts[row]);
                
                // Make it invisible
                newCell.setVisible(false);
                
                // Skip over ones we've just filled in
                col += colspan-1;
                
                // If we've filled in multiple entire rows, skip them now, too.
                if(col==0 && colspan==colCount) 
                    row += rowspan-1;
            }
        }
    }
}

/** Prints overlap warning once. */
static void printOverlapWarning()  { if(!_pow) System.out.println("Warning: overlapping shapes"); _pow = true; }
static boolean _pow; 

}