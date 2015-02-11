package com.reportmill.pdf.writer;

/**
 * This class represents a PDF XRef Table entry.
 */
public class PDFXEntry {
    
    // The state
    public int state;
    
    // The object number
    public int objectNumber;
    
    // The file offset
    public int fileOffset;
    
    // The object value
    public Object value;
    
    // Constants for Entry types
    public static final int EntryUnknown = 0;
    public static final int EntryDeleted = 1;
    public static final int EntryRead = 2;
    public static final int EntryNotYetRead = 3;
    
/**
 * Creates a new PDF XREF entry.
 */
public PDFXEntry(int index)
{
    state = PDFXEntry.EntryUnknown;
    objectNumber = index;
}

}