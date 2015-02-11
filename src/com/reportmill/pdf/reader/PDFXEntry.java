package com.reportmill.pdf.reader;

/**
 * This class represents a PDF XRef entry.
 */
public class PDFXEntry {
    
    // The state
    public int      state;
    
    // The object number
    public int      objectNumber;
    
    // The file offset
    public int      fileOffset;
    
    // The generation number
    public int      generation;
    
    // The object value
    public Object   value;
    
    // Constants for Entry types
    public static final int EntryUnknown = 0;
    public static final int EntryDeleted = 1;
    public static final int EntryRead = 2;
    public static final int EntryNotYetRead = 3;
    public static final int EntryCompressed = 4;
    
/**
 * Creates a new PDF XREF entry.
 */
public PDFXEntry(int index)
{
    state = EntryUnknown;
    objectNumber = index;
    generation = 0;
}

/**
 * Releases the reference to the object.
 */
public void reset()
{
    if(state==EntryRead) {
        state = EntryNotYetRead;
        value = null;
    }
}

/**
 * Returns a string representation.
 */
public String toString()
{
    return objectNumber + " 0 R";
}

}