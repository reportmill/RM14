package com.reportmill.shape;
import com.reportmill.base.*;
import java.util.*;

/**
 * Defines a region of coordinates along with a list of shapes that occupy that region.
 */
public class RMShapeTable {

    // The list of table shapes
    List <RMShape>     _shapes;
    
    // The list of rows
    List <Row>         _rows;
    
    // The list of columns
    List <Column>      _columns;

    // The list of row dividers
    List <Double>      _rowDividers;

    // The list of column dividers
    List <Double>      _columnDividers;
    
    // The array of cells
    Cell[][]           _cells;

/**
 * Creates a table from a list of shapes.
 */
public RMShapeTable(List <RMShape> theShapes)  { _shapes = theShapes; }

/**
 * Returns the list of table shapes.
 */
public List <RMShape> getShapes()  { return _shapes; }

/**
 * Returns the number of rows.
 */
public int getRowCount()  { return getRows().size(); }

/**
 * Returns the individual row at given index.
 */
public Row getRow(int anIndex)  { return getRows().get(anIndex); }

/**
 * Returns a list of rows.
 */
public List <Row> getRows()
{
    // If not set, generate from dividers
    if(_rows==null) {
        _rows = new ArrayList();
        for(int i=0, iMax=getRowDividers().size()-1; i<iMax; i++)
            _rows.add(new Row(getRowDividers().get(i), getRowDividers().get(i+1)));
    }
    
    // Return rows
    return _rows;
}

/**
 * Returns the individual column at given index.
 */
public Column getColumn(int anIndex)  { return getColumns().get(anIndex); }

/**
 * Returns the number of columns.
 */
public int getColumnCount()  { return getColumns().size(); }

/**
 * Returns a list of columns.
 */
public List <Column> getColumns()
{
    // If not set, generate from dividers
    if(_columns==null) {
        _columns = new ArrayList();
        for(int i=0, iMax=getColumnDividers().size()-1; i<iMax; i++)
            _columns.add(new Column(getColumnDividers().get(i), getColumnDividers().get(i+1)));
    }
    
    // Return columns
    return _columns;
}

/**
 * Returns the list of row dividers.
 */
public List <Double> getRowDividers()
{
    // If not set, generate from shapes
    if(_rowDividers==null) {
        _rowDividers = new ArrayList();
        for(RMShape shape : getShapes()) {
            addDouble(_rowDividers, shape.getFrameY());
            addDouble(_rowDividers, shape.getFrameMaxY());
        }
    }
    
    // Return dividers
    return _rowDividers;
}

/**
 * Returns the list of column dividers.
 */
public List <Double> getColumnDividers()
{
    // If not set, generate from shapes
    if(_columnDividers==null) {
        _columnDividers = new ArrayList();
        for(RMShape shape : getShapes()) {
            addDouble(_columnDividers, shape.getFrameX());
            addDouble(_columnDividers, shape.getFrameMaxX());
        }
    }
    
    // Return dividers
    return _columnDividers;
}

/**
 * Returns the number of cells.
 */
public int getCellCount()  { return getRowCount()*getCellCount(); }

/**
 * Returns the individual cell at the given index.
 */
public Cell getCell(int anIndex)  { return getCells()[anIndex/getRowCount()][anIndex%getColumnCount()]; }

/**
 * Returns the individual cell at the given row and column.
 */
public Cell getCell(int aRow, int aColumn)  { return getCells()[aRow][aColumn]; }

/**
 * Returns the cells.
 */
public Cell[][] getCells()
{
    // If cells not set, get cells
    if(_cells==null) {
        
        // Allocate cells array
        _cells = new Cell[getRowCount()][getColumnCount()];
        
        // Create cells
        for(int i=0, iMax=getRowCount(); i<iMax; i++) { Row row = getRow(i);
            
            // Iterate over columns
            for(int j=0, jMax=getColumnCount(); j<jMax; j++) { Column column = getColumn(j);
            
                // create new cell
                _cells[i][j] = new Cell(row, column);
            
                // Iterate over shapes and add all that intersect cell
                for(RMShape shape : getShapes())
                    if(row.intersects(getSpanY(shape)) && column.intersects(getSpanX(shape)))
                        _cells[i][j].shapes.add(shape);
            }
        }
        
    }
    
    // Return cells
    return _cells;
}

/**
 * Returns the list of shapes from given rows and columns.
 */
public List <RMShape> getShapes(int aRow, int aColumn, int aRowMax, int aColumnMax)
{
    // Create list for shapes
    List <RMShape> shapes = new ArrayList();
    
    // Get span x and span y for cell range
    Span spanX = new Span(getColumn(aColumn).start, getColumn(aColumnMax-1).end);
    Span spanY = new Span(getRow(aRow).start, getRow(aRowMax-1).end);

    // Iterate over cells and add shapes fully contained by span
    for(int i=aRow; i<aRowMax; i++)
        for(int j=aColumn; j<aColumnMax; j++)
            for(RMShape shape : getCell(i,j).shapes)
                if(spanX.contains(shape.getFrameX()) && spanX.contains(shape.getFrameMaxX()) &&
                   spanY.contains(shape.getFrameY()) && spanY.contains(shape.getFrameMaxY()))
                    RMListUtils.addUniqueId(shapes, shape);
                else if(spanX.contains(shape.getFrameX()) && !spanX.contains(shape.getFrameMaxX()))
                    spanX.end = shape.getFrameX();
                else if(!spanX.contains(shape.getFrameX()) && spanX.contains(shape.getFrameMaxX()))
                    spanX.start = shape.getFrameMaxX();
    
    // Return shapes
    return shapes;
}

/**
 * Returns whether given range of cells is empty.
 */
public boolean isEmpty(int aRow, int aColumn, int aRowMax, int aColumnMax)
{
    for(int i=aRow; i<aRowMax; i++)
        for(int j=aColumn; j<aColumnMax; j++)
            if(!getCell(i,j).isEmpty())
                return false;
    return true;
}

/**
 * Returns whether given range of cells contains given shape.
 */
public boolean contains(int aRow, int aColumn, int aRowMax, int aColumnMax, RMShape aShape)
{
    Span spanX = new Span(getRow(aRow).start, getRow(aRowMax-1).end);
    Span spanY = new Span(getColumn(aColumn).start, getColumn(aColumnMax-1).end);
    return spanX.contains(aShape.getFrameX()) && spanX.contains(aShape.getFrameMaxX()) &&
        spanY.contains(aShape.getFrameY()) && spanY.contains(aShape.getFrameMaxY());
}

/**
 * An inner class representing rows.
 */
public class Row extends Span {

    /** Creates a new row. */
    public Row(double aStart, double anEnd)  { super(aStart, anEnd); }
    
    /** Returns the row height. */
    public double getHeight()  { return getLength(); }
}
    
/**
 * An inner class representing rows.
 */
public class Column extends Span {

    /** Creates a new column. */
    public Column(double aStart, double anEnd)  { super(aStart, anEnd); }
    
    /** Returns the column width. */
    public double getWidth()  { return getLength(); }
}
    
/**
 * An inner class that represents a table cell.
 */
public class Cell {

    // The cell row
    Row       row;
    
    // The cell column
    Column    column;

    // The list of shapes
    List <RMShape>  shapes = new ArrayList();
    
    /** Creates a new cell. */
    public Cell(Row aRow, Column aColumn)  { row = aRow; column = aColumn; }
    
    /** Returns the cell row. */
    public Row getRow()  { return row; }
    
    /** Returns the cell column. */
    public Column getColumn()  { return column; }
    
    /** Returns whether cell is empty. */
    public boolean isEmpty()  { return shapes.size()==0; }
    
    /** Returns whether cell is visible. */
    public boolean isVisible()
    {
        boolean visible = false;
        for(RMShape shape : shapes) visible = visible || shape.isShowing();
        return visible;
    }
}

/**
 * A class to represent an interval 
 */
public static class Span implements Comparable {

    // The start of the span
    double start;
    
    // The end of the span
    double end;
    
    /** Creates a new span. */
    public Span(double aStart, double anEnd)  { this.start = aStart; this.end = anEnd; }
    
    /** Returns the span length. */
    public double getLength()  { return end - start; }
    
    /** Returns whether given value is contained in the span (inclusive). */
    public boolean contains(double aValue)  { return RMMath.lte(this.start, aValue) && RMMath.lte(aValue, this.end); }
    
    /** Returns whether given span intersects this span. */
    public boolean intersects(Span aSpan)
    {
        return RMMath.equals(this.start, aSpan.start) ||
               RMMath.equals(this.end, aSpan.end) ||
               RMMath.lt(aSpan.start, end) && RMMath.gt(aSpan.end, start);
    }
    
    /** Returns string representation of span. */
    public String toString()  { return "Span { start: " + start + ", end: " + end + " }"; }

    /** Comparable implementation. */
    public int compareTo(Object aSpan)  { return new Double(start).compareTo(((Span)aSpan).start); }
}

/**
 * A class to represent a list of spans.
 */
public static class SpanList extends ArrayList <Span> {

    /**
     * Adds a span to a list of spans, either by extending an existing span or actually adding it to the list.
     */
    public void addSpan(Span aSpan)
    {
        // If empty span, just return
        if(RMMath.lte(aSpan.end, aSpan.start))
            return;
        
        // Iterate over spans and extends any overlapping span (and return)
        for(Span span : this) {
            
            // If given span starts inside loop span and ends after, extend current span, remove from list and re-add
            if(span.contains(aSpan.start) && !span.contains(aSpan.end)) {
                span.end = aSpan.end;
                this.remove(span);
                addSpan(span);
                return;
            }
            
            // If given span starts before loop span and ends inside, extend current span, remove from list and re-add
            if(!span.contains(aSpan.start) && span.contains(aSpan.end)) {
                span.start = aSpan.start;
                this.remove(span);
                addSpan(span);
                return;
            }
            
            // If loop span contains given span, just return
            if(span.contains(aSpan.start) && span.contains(aSpan.end))
                return;
        }
        
        // Since no overlapping span, add span
        add(aSpan);
    }
    
    /**
     * Removes a span from a list of spans, either by reducing a span or by removing a span.
     */
    public void removeSpan(Span aSpan)
    {
        // Iterate over spans and reduce any that need to be reduced
        for(Span span : this) {
            
            // If given span starts in loop span and ends outside, reduce loop span to given span start
            if(span.contains(aSpan.start) && !span.contains(aSpan.end))
                span.end = aSpan.start;
            
            // If given span starts outside loop span and ends in span, reset loop span start to given span end
            if(!span.contains(aSpan.start) && span.contains(aSpan.end))
                span.start = aSpan.end;
            
            // If loop span contains given span, remove given span and add two spans
            if(span.contains(aSpan.start) && span.contains(aSpan.end)) {
                this.remove(span);
                addSpan(new Span(span.start, aSpan.start));
                addSpan(new Span(aSpan.end, span.end));
                return;
            }
            
            // If given span contains loop span, remove it and re-run
            if(aSpan.contains(span.start) && aSpan.contains(span.end)) {
                this.remove(span);
                removeSpan(aSpan);
                return;
            }
        }
    }
}

/**
 * Returns the horizontal span of a given shape.
 */
public static Span getSpanX(RMShape aShape)  { return new Span(aShape.getFrameX(), aShape.getFrameMaxX()); }

/**
 * Returns the vertical span of a given shape.
 */
public static Span getSpanY(RMShape aShape)  { return new Span(aShape.getFrameY(), aShape.getFrameMaxY()); }

/**
 * Adds a double to a list.
 */
public void addDouble(List <Double> aDoubleList, Double aDouble)
{
    for(int i=0, iMax=aDoubleList.size(); i<iMax; i++) { double value = aDoubleList.get(i);
        if(equals(aDouble, value))
            return;
        else if(aDouble<value) {
            aDoubleList.add(i, aDouble);
            return;
        }
    }
    aDoubleList.add(aDouble);
}

/**
 * Returns whether double values are equal to the precision of the table.
 */
public boolean equals(double aValue1, double aValue2)  { return Math.abs(aValue1 - aValue2) < 0.001; }

}