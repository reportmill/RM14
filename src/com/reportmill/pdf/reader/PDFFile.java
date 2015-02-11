package com.reportmill.pdf.reader;
import java.util.*;

/**
 * This class is used to read a PDF file for parsing.
 */
public class PDFFile {
    
    // The PDF file version string
    String                  _version;
    
    // The PDF file catalog
    Map                     _catalog;
    
    // The PDF file pages
    Map                     _pages;
    
    // Cached PDFPage instances
    Map <Integer, PDFPage>  _rmpages = new Hashtable(4);
    
    // Encyption dictionary
    PDFSecurityHandler      _securityHandler;
    
    // File identifier
    Vector                  _fileID;
    
    // The PDF file XREF table
    Vector <PDFXEntry>      _xref = new Vector();
    
    // Whether this PDF file is usable
    boolean                 _valid = true;
    
    // The parser for this PDF file
    PDFParser               _parser;
    
    // Factory classes and callback handler
    FontFactory             _fontFactory;
    PathFactory             _pathFactory;
    ColorFactory            _colorFactory;
    ImageFactory            _imageFactory;
    PDFMarkupHandler        _markupHandler;
    
    // options
    boolean                 _stripExtendedGStates = true;
    
/**
 * Creates a new PDF file for the PDF file bytes.
 */
public PDFFile(byte bytes[])
{
    // Create new PDF parser for given pdf bytes
    _parser = new PDFParser(this, bytes);
    
    // Get PDF version
    try { _version = _parser.pdfversion(); }
    catch(Exception e) { e.printStackTrace(); }
    
    // Read XRef table
    readPDFXRefTables();
    
    // Get pages
    _pages = (Map)resolveObject(_catalog.get("Pages"));
}

/**
 * Gets the pdf version as a float.
 */
public float getVersion() 
{
    // parser guarantees that this string looks like %PDF-xxxx
    try { return Float.parseFloat(_version.substring(5)); }
    catch (NumberFormatException nfe) { throw new PDFException("Illegal PDF version header"); }
}

/**
 * Returns an individual PDF page for the given page index.
 * This function used to create a new instance every time, but it gets called repeatedly by imagereader and pageparser,
 * so half a dozen (at least) page instances were getting created for every pdf import.  This of course also defeated
 * any useful caching behavior in the RMPDFPage.
 *       
 * The file could be hundreds of pages big, and we're most likely only ever looking at one page.  Creating a sparse
 * array hundreds of elements large seems silly, so we'll use a map instead.
 */
public PDFPage getPage(int aPageIndex)
{ 
    PDFPage page = _rmpages.get(aPageIndex);
    if(page==null)
        _rmpages.put(aPageIndex, page = new PDFPage(this, aPageIndex)); 
    return page;
}

/**
 * Clears the page cache.
 */
public void clearPageCache() { _rmpages.clear(); }

/**
 * Returns the number of PDF pages in the PDF file.
 */
public int getPageCount()
{
    Object obj = _pages.get("Count");
    Integer npages = (Integer)resolveObject(obj); // Can Count really be a reference?
    return npages.intValue();
}

/**
 * Returns x.
 */
public Object inheritedAttributeForKeyInPage(String aKey, Map aPage)
{
    // Get value for key from page dict
    Object value = aPage.get(aKey);

    // If key not declared in this object, check parent
    if(value==null) {
        Object parent = aPage.get("Parent");
        if(parent!=null)
            value = inheritedAttributeForKeyInPage(aKey, (Hashtable)resolveObject(parent));
    }
    
    return value;
}

private void readPDFXRefTables()
{
    Map lastTrail=null;
    Map encrypter;
    int xrefstart = getXRefTablePosition();
    
    // For each update to the file...
    try {
        do {
        _parser.resetLexingLocation(xrefstart);
        
        // Read xref section and get the trailer dict         
        Map trail = _parser.pdfXRefSection(_xref);
        
        // If this is the real trailer (the one at the end of the file), use it
        // pull out the rest of the file info.
        if(lastTrail==null)
            lastTrail = trail;
        
        // Check for presence of previous xref table
        Integer newOffset = (Integer)trail.get("Prev");
        if(newOffset!=null)
            xrefstart = newOffset.intValue();
        else break;
      } while(true);
    }
    catch(ParseException e) { throw new PDFException("Error reading cross-reference section : "+e); }

    // Get the catalog
    _catalog = (Hashtable)resolveObject(lastTrail.get("Root"));
    if(_catalog==null)
        throw new PDFException("Couldn't find Catalog");

    // Get the file identifier (optional)
    _fileID = (Vector)resolveObject(lastTrail.get("ID"));

    // If there was an encryption dictionary, make a handler for it
    encrypter = (Hashtable)resolveObject(lastTrail.get("Encrypt"));
    if (encrypter != null)
        _securityHandler = PDFSecurityHandler.getInstance(encrypter, _fileID, _version);                   
}

/**
 * PDF reading starts at file end - this routine starts at end and searches backwards until it finds startxref key
 * It returns file offset to the xref table (also checks that a valid EOF is present).
 */
public int getXRefTablePosition()
{
    try {
    char find[] = "ferxtrats".toCharArray();  // "startxref" backwards
    byte pdfbytes[] = _parser.pdfdata.buffer();
    int fpos = 0, bpos = pdfbytes.length, xref;
    
    while(--bpos >= 0) {
    
        if((char)pdfbytes[bpos]==find[fpos]) {
            ++fpos;
            
            if(fpos == find.length) {
                _parser.resetLexingLocation(bpos);
                xref = _parser.startxref();
                _parser.checkEOF(); // check for %%EOF marker
                return xref;
            }
        }
        
        else fpos = 0;
    }
    
    } catch(Exception e) { e.printStackTrace(); }
    return -1;
}

/**
 * Returns the object from the xref table, reading it if necessary.
 */
public Object getXRefObject(PDFXEntry anEntry)
{
    Object obj = null;
    
    // Handle exceptions
    try {
        
    // Handle entry by entry state
    switch(anEntry.state) {
    
        // Handle read object
        case PDFXEntry.EntryRead :
            obj = anEntry.value;
            break;
            
        // Handle unread object
        case PDFXEntry.EntryNotYetRead :
            
            // Parse object
            int oldposition = _parser.pdfdata.currentLocation();
            _parser.resetLexingLocation(anEntry.fileOffset);
            obj = _parser.object_definition();
            _parser.resetLexingLocation(oldposition);
                
            // Validate and possibly decrypt object
            obj = validateObject(obj);
            if(_securityHandler!=null)
                obj = _securityHandler.decryptObject(obj, anEntry.objectNumber, anEntry.generation);

            // Update entry
            anEntry.state = PDFXEntry.EntryRead;
            anEntry.value = obj;
            break;
            
        // Handle compressed object
        case PDFXEntry.EntryCompressed :
            int ostreamObjNum = anEntry.fileOffset;
            int objIndex = anEntry.generation;
            obj = readCompressedEntry(ostreamObjNum, objIndex);
            anEntry.state = PDFXEntry.EntryRead;
            anEntry.value = obj;
            break;
        
        // Handle unknown object
        case PDFXEntry.EntryUnknown :
            throw new PDFException("Reference to unknown object");
            
        // Handle deleted object
        case PDFXEntry.EntryDeleted :
            break;
    }
    
    // Handle exceptions
    } catch(Exception e) { e.printStackTrace(); }
    
    // Return object
    return obj;
}

/**
 * Given an object, check to see if its an indirect reference - if so, resolve the reference.
 */
public Object resolveObject(Object obj)
{
    if(obj instanceof PDFXEntry)
        return getXRefObject((PDFXEntry)obj);
    return obj;
}

public Object readCompressedEntry(int ostmNum, int objIndex)
{
    // Get the cross-reference object for the object stream
    PDFXEntry entry = _xref.get(ostmNum);
    
    // Resolve the reference to get the actual object
    Object obj = getXRefObject(entry);
    PDFObjectStream oStm;
    
    // The first time through, the object will point to the stream representation of the object stream.
    // Get it and create a PDFObjectStream, then change the reference to point to the object stream
    if(obj instanceof PDFStream) {
        oStm = new PDFObjectStream((PDFStream)obj, this, _parser);
        entry.value = oStm;
    }
    else oStm = (PDFObjectStream)obj;
    
    return oStm.get(objIndex, _parser);
}

/**
 * This method is used to weed out any objects that shouldn't be included in the new file.
 */
Object validateObject(Object obj)
{
    //TODO:  Check this.  Hidden behind an option for the moment.
    // For the page marking, the extended gstate object is relevant, whereas for creating Form XObjects, it isn't.
    // (Or wasn't when this code was first written) By sticking the flag here, the implication (which is undoubtedly
    // wrong) becomes that this object is only doing one or the other, not both.
    if (_stripExtendedGStates) {
        
        // Extended GState Objects not allowed in Form XObjects - replace with empty dict
        if(obj instanceof Hashtable) {
            Object val = ((Hashtable)obj).get("Type");
            if(val!=null && val.equals("/ExtGState"))
                return new Hashtable();
        }
    }
    
    return obj;
}

/**
 * Call this to clear the state of all xref table entries.  This will allow
 * objects created while examining the file to get garbage collected.
 */
public void resetXRefTable()
{
    for(int i=0, n=_xref.size(); i<n; ++i)
        _xref.get(i).reset();
}

public void setStripsExtendedGStates(boolean flag)
{
    // See above comment.
    _stripExtendedGStates=flag;
}

/** Graphics object creation factories */
public FontFactory getFontFactory() { return _fontFactory; }
public void setFontFactory(FontFactory f) { _fontFactory = f; }
public PathFactory getPathFactory() { return _pathFactory; }
public void setPathFactory(PathFactory p) { _pathFactory = p; }
public ColorFactory getColorFactory() { return  _colorFactory; }
public void setColorFactory(ColorFactory c) { _colorFactory = c; }
public ImageFactory getImageFactory() { return _imageFactory; }
public void setImageFactory(ImageFactory f) { _imageFactory = f; }

/** The callback handler */
public PDFMarkupHandler getMarkupHandler() { return _markupHandler; }
public void setMarkupHandler(PDFMarkupHandler h) { _markupHandler = h; }

}