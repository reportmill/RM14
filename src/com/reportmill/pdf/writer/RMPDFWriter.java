package com.reportmill.pdf.writer;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.util.*;
import java.util.zip.Deflater;

/**
 * PDF Writer.
 */
public class RMPDFWriter extends PDFFile {

    // The current PDF page
    PDFPage                     _pdfPage;

    // The current PDF Buffer
    PDFBuffer                   _buffer = new PDFBuffer();
    
    // Whether PDF stream objects should be compressed
    boolean                     _compress;
    
    // Shared deflater
    Deflater                    _deflater = new Deflater(6, false);

    // Security handler for adding password protection
    PDFEncryptor                _encryptor;
    
    // Map of pdf.reader XRefs to pdf.writer XRefs
    Map                         _readerWriterXRefMap = new HashMap();
  
    // The default viewer preferences map
    static Map <String,String>  _viewerPreferencesDefault = Collections.singletonMap("PrintScaling", "/None");
    
    // Whether writer should include newline and tab characters (like tab, newline, carriage return)
    boolean                     _includeNewlines = _includeNewlinesDefault;
    
    // Whether writer should include newline and tab characters (like tab, newline, carriage return)
    static boolean              _includeNewlinesDefault = true;
    
/**
 * Returns a PDF byte array for a given RMDocument.
 */
public byte[] getBytes(RMDocument aDoc)
{
    // Validate and resolve doc page references
    aDoc.layout();
    aDoc.resolvePageReferences();
    
    // Add watermark
    ReportMill.lc(aDoc);

    // Get doc pdf attributes
    _compress = aDoc.getCompress();
    
    // Set PDF file author
    setAuthor("ReportMill User");
    
    // Set PDF file creator
    String version = "ReportMill " + RMUtils.getVersion();
    String build = ", Build: " + RMUtils.getBuildInfo();
    String jvm = ", JVM: " + System.getProperty("java.version");
    setCreator(version + build + jvm);
    
    // Iterate over doc pages
    for(int i=0, iMax=aDoc.getPageCount(); i<iMax; i++) { RMShape page = aDoc.getPage(i);
        
        // Get pdf page, set media box and add to pages tree and xref
        _pdfPage = new PDFPage(this);
        _pdfPage.setMediaBox(page.getBoundsInside());
        
        // Have page pdfr write pdf
        RMObjectPdfr.getPdfr(page).writePDF(page, this);

        // Close page contents
        _pdfPage.closeContents(this);
    }
      
    // run a pass though all the _pages to resolve any forward references
    _pagesTree.resolvePageReferences();
    
    // Write out header but save away version in case it gets updated
    String versionString = getVersion();
    _buffer.appendln("%" + versionString);
    
    // Write 4 binary bytes in comment to indicate we may use 8 bit binary
    _buffer.append(new byte[] { (byte)'%', (byte)'\242', (byte)'\243', (byte)'\245', (byte)'\250' });
    _buffer.appendln();
    
    // The _objects & the xref table 
    int off = writeXRefTable(this);
    
    // The trailer
    _buffer.appendln("trailer");
    _buffer.appendln("<<");
    _buffer.append("/Size ").append(_xref.getEntryCount() + 1).appendln();
    _buffer.append("/Root ").appendln(_xref.getRefString(_catalogDict));
    _buffer.append("/Info ").appendln(_xref.getRefString(_infoDict));
    
    // If encryption was specified, add the encryption dict
    if(getEncryptor() != null) 
        _buffer.append("/Encrypt ").appendln(_xref.getRefString(getEncryptor().getEncryptionDict()));
    
    // Add a uniqueID to the trailer
    String idString = getFileIDString();
    _buffer.append("/ID [").append(idString).append(idString).append(']').appendln();

    // Write cross reference table and end of file marker
    _buffer.appendln(">>");
    _buffer.appendln("startxref");
    _buffer.append(off).appendln();
    _buffer.appendln("%%EOF");
    
    // Now get actual pdf bytes
    byte pdfBytes[] = _buffer.toByteArray();
    
    // Get new version string (might have been bumped during generation)
    String newVersion = getVersion();
    
    // If version string was bumped during generation, go back and update header
    if(!versionString.equals(newVersion)) {
        
        // pdf files are extremely sensitive to position, so make sure the headers are the same size
        int newLen = newVersion.length();
        int oldLen = versionString.length();

        // Complain if version string increases in length
        if(newLen > oldLen) 
            throw new RuntimeException("error trying to update pdf version number to " + newVersion);
        
        // Copy new version in (pad with spaces if new version is smaller)
        for(int i=0; i<oldLen; i++)
            pdfBytes[i+1] = (byte)(i<newLen? newVersion.charAt(i) : ' ');
    }
    
    // Return pdf bytes
    return pdfBytes;
}

/**
 * Returns the current PDF page.
 */
public PDFPage getPDFPage()  { return _pdfPage; }

/**
 * Returns the current pdf buffer.
 */
public PDFBuffer getBuffer()  { return _buffer; }

/**
 * Returns whether to compress or not.
 */
public boolean getCompress()  { return _compress; }

/**
 * Sets whether to compress or not.
 */
public void setCompress(boolean aValue)  { _compress = aValue; }

/**
 * Returns a shared deflater.
 */
public Deflater getDeflater()  { return _deflater; }

/**
 * Returns the current PDF encryptor.
 */
public PDFEncryptor getEncryptor()  { return _encryptor; }

/**
 * Set the access permissions on the file such that the document can be opened by anyone, but the user cannot
 * modify the document in any way. To modify these settings in Acrobat, the owner password would have to be provided.
 */
public void setUnmodifiable(String ownerPwd)
{
    setAccessPermissions(ownerPwd, null, PDFEncryptor.PRINTING_ALLOWED | PDFEncryptor.EXTRACT_TEXT_AND_IMAGES_ALLOWED |
        PDFEncryptor.ACCESSABILITY_EXTRACTS_ALLOWED | PDFEncryptor.MAXIMUM_RESOLUTION_PRINTING_ALLOWED);
}

/** 
 * Sets pdf user access restrictions.
 * 
 * The user password is the password that will be required to open the file.
 * The owner password is the password that will be required to make future changes to the security settings,
 * such as the passwords. Either of the passwords may be null.
 * If both passwords are null, the file will not be password protected, but it will still be encrypted.
 * Fine-grained access can be limited by setting accessFlags, to limit such things as printing or editing the file.
 * See com.ribs.pdf.PDFSecurityHandler for a list of the access flag constants. (or the pdf spec v1.6, pp. 99-100)
 */
public void setAccessPermissions(String ownerPwd, String userPwd, int accessFlags)
{
    // Create encryptor
    _encryptor = new PDFEncryptor(getFileID(), ownerPwd, userPwd, accessFlags);
    
    // Add the encryption dictionary to the file
    getXRefTable().addObject(_encryptor.getEncryptionDict());
    
    // Since we're using 128 bit keys, bump the pdf revision to 1.4. If we add a parameter to select the
    // encryption scheme, we could optionally use 40 bit keys and leave the version at 1.2
    setVersion(1.4f);
}

/**
 * Returns default viewer preferences map.
 */
public static Map<String, String> getViewerPreferencesDefault()  { return _viewerPreferencesDefault; } 

/**
 * Sets default viewer preferences map.
 */
public static void setViewerPreferencesDefault(Map<String, String> aMap)  { _viewerPreferencesDefault = aMap; } 

/**
 * Returns whether to include newline and tab characters characters.
 */
public boolean getIncludeNewlines()  { return _includeNewlines; } 

/**
 * Sets whether to include newline and tab characters.
 */
public void setIncludeNewlines(boolean aValue)  { _includeNewlines = aValue; } 

/**
 * Returns whether to include newline and tab characters characters.
 */
public static boolean getIncludeNewlinesDefault()  { return _includeNewlinesDefault; } 

/**
 * Sets whether to include newline and tab characters.
 */
public static void setIncludeNewlinesDefault(boolean aValue)  { _includeNewlinesDefault = aValue; } 

/**
 * Writes any kind of object to the PDF buffer.
 */
public void writeXRefEntry(Object anObj)
{
    // Get the buffer
    PDFBuffer buffer = getBuffer();
    
    // Handle strings
    if(anObj instanceof String) { String string = (String)anObj;
    
        // If not a PDF string, just append
        if(!string.startsWith("("))
            buffer.append(string);
        
        // If encryption is enabled, all strings get encrypted
        else if(getEncryptor()!= null) {
            buffer.append('(');
            buffer.append(getEncryptor().encryptString((String)anObj));
            buffer.append(')');
        }
        
        // Otherwise just add string
        else buffer.printPDFString((String)anObj);
    }
    
    // Handle numbers
    else if(anObj instanceof Number)
        buffer.append(((Number)anObj).doubleValue());
    
    // Handle fonts map
    else if(anObj==getFonts()) {
        buffer.appendln("<<");
        for(PDFFontEntry fontEntry : getFonts().values())
            buffer.appendln("/" + fontEntry.getPDFName() + " " + fontEntry._refString);
        buffer.appendln(">>");
    }
    
    // Handle Maps
    else if(anObj instanceof Map) { Map <String,Object>map = (Map)anObj;
    
        // Write dictionary contents surrounded by dictionary brackets
        buffer.appendln("<<");
        for(String key : map.keySet()) {
            
            // Skip entries that we put in for caching purposes
            if(key.startsWith("_rbcached"))
                continue;
            
            buffer.append('/').append(key).append(' ');
            writeXRefEntry(map.get(key));
            buffer.appendln();
        }
        buffer.append(">>");
    }
    
    // Handle Lists
    else if(anObj instanceof List) { List list = (List)anObj;
        buffer.append('[');
        for(int i=0, iMax=list.size(); i<iMax; i++) {
            if(i>0) buffer.append(' ');
            writeXRefEntry(list.get(i));
        }
        buffer.append(']');
    }
    
    // Handle PDFPage
    else if(anObj instanceof PDFPage)
        ((PDFPage)anObj).writePDF(this);
    
    // Handle font entries
    else if(anObj instanceof PDFFontEntry)
        ((PDFFontEntry)anObj).writePDF(this);
    
    // Handle image data
    else if(anObj instanceof RMImageData)
        RMImageFillPdfr.writeImageData((RMImageData)anObj, this);
    
    // Handle PDFPagesTree
    else if(anObj instanceof PDFPagesTree)
        ((PDFPagesTree)anObj).writePDF(this);
    
    // Handle PDFStream
    else if(anObj instanceof PDFStream)
        ((PDFStream)anObj).writePDF(this);
    
    // Handle PDFAnnotation
    else if(anObj instanceof PDFAnnotation) { PDFAnnotation annotation = (PDFAnnotation)anObj;
        writeXRefEntry(annotation.getAnnotationMap());
    }
    
    // Handle color
    else if(anObj instanceof RMColor)
        buffer.append((RMColor)anObj);
    
    // Handle boolean
    else if(anObj instanceof Boolean)
        buffer.append(anObj.toString());
    
    // Complain about anything else (maybe java.awt.color.ColorSpace)
    else System.err.println("RMPDFWriter: Unsupported PDF object: " + anObj.getClass().getName());
}

/** Obsolete */
@Deprecated public byte[] getBytesPDF(RMDocument aDoc) { return getBytes(aDoc); }

}