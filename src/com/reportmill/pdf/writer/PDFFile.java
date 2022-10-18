package com.reportmill.pdf.writer;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import java.util.*;
import java.text.*;

/**
 * This class represents a PDF file.
 */
public class PDFFile {
    
    // The PDF version being generated
    float                       _version = 1.2f;
    
    // XRef table
    PDFXTable                   _xref = new PDFXTable();
    
    // Info dict
    Hashtable                   _infoDict = new Hashtable(4);
    
    // Catalog dict
    Hashtable                   _catalogDict = new Hashtable(4);
    
    // Pages tree
    PDFPagesTree                _pagesTree;
    
    // Font entry map
    Map <String, PDFFontEntry>  _fonts = new Hashtable();
    
    // The current font entry
    PDFFontEntry                _fontEntry;
    
    // Map of image data names to image reference strings
    Map <String,String>         _images = new Hashtable();
    
    // Map of unique image datas
    List <RMImageData>          _imageDatas = new ArrayList();
    
    // unique id
    byte                        _fileID[] = null;
    
/**
 * Creates a new pfile.
 */
public PDFFile()
{
    // Init and add info dict to xref
    _infoDict.put("CreationDate", new SimpleDateFormat("(dd MMM yyy HH:mm)").format(new Date()));
    _xref.addObject(_infoDict);

    // Init and add catalog to xref
    _catalogDict.put("Type", "/Catalog");
    _catalogDict.put("PageMode", "/UseNone");
    _xref.addObject(_catalogDict);
    
    // Init and add to xref and catalog
    _pagesTree = new PDFPagesTree(this);
    _catalogDict.put("Pages", _xref.addObject(_pagesTree));

    // Add fonts and images to xref
    _xref.addObject(_fonts);
    _xref.addObject(_images);
    
    // Tell acrobat reader not to scale when printing by default (only works in PDF 1.6, but is harmless in < 1.6)
    setViewerPreferences(RMPDFWriter.getViewerPreferencesDefault());
}

/**
 * Returns the version of pdf being generated.
 */
public String getVersion()  { return "PDF-" + _version; }

/**
 * Sets the version of the pdf being generated.
 */
public void setVersion(float aVersion)  { _version = Math.max(_version, aVersion); }

/**
 * Add a viewer preferences dictionary to the file. See section 8.1 of the pdf spec
 */
public void setViewerPreferences(Map vprefs)  { _catalogDict.put("ViewerPreferences", vprefs); }

/**
 * Returns the cross reference table.
 */
public PDFXTable getXRefTable()  { return _xref; }

/**
 * Returns the PDF file's info dictionary.
 */
public Map getInfoDict()  { return _infoDict; }

/**
 * Returns the PDF file's pages tree.
 */
public PDFPagesTree getPagesTree()  { return _pagesTree; }

/**
 * Sets the author of the pdf file.
 */
public void setInfoDictAuthor(String  s)  { _infoDict.put("Author", "(" + s + ")"); }

/**
 * Sets the creator of the pdf file.
 */
public void setInfoDictCreator(String s)  { _infoDict.put("Creator", "(" + s + ")"); }

/**
 * Generates and returns a unique file identifier.
 */
public byte[] getFileID()
{
    // In order to be unique to file's contents, fileID is generated with an MD5 digest of contents of info dictionary.
    // The pdf spec suggests using the file size and the file name, but we don't have access to those here.
    // The spec suggests using current time, but that's already present in info dict as value of /CreationDate key.
    if (_fileID == null) try {
        
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        Iterator vals = _infoDict.values().iterator();
        while(vals.hasNext()) {
            String infoString = vals.next().toString();
            md.update(infoString.getBytes());
        }
        
        // Rather than adding the file size, which we won't have until everything gets dumped out, add the number
        // of objects in the xref table (as 4 bytes). This is probably going to be the same for everyone.
        int i, size = _xref.getEntryCount();
        for(i=0; i<4; ++i) {
            md.update((byte)(size&0xff));
            size>>=8;
        }
        
        // Get the digest and cache it - MD5 is defined to return a 128 bit (16 byte) digest
        byte digest_bytes[] = md.digest();
        
        // This should never happen, so this is here just in case something goes wrong.
        if (digest_bytes.length>16) {
            _fileID=new byte[16];
            System.arraycopy(digest_bytes,0,_fileID,0,16);
        }
        else _fileID=digest_bytes;
    }
    
    // If the md5 fails, just create a fileID with random bytes
    catch (java.security.NoSuchAlgorithmException nsae) {
        Random rand = new Random();
        _fileID=new byte[16];
        rand.nextBytes(_fileID);
    }
    
    return _fileID;
}

/**
 * Returns the file identifier as a hex string.
 */
public String getFileIDString()
{        
    byte id_bytes[] = getFileID();
    StringBuffer out = new StringBuffer("<");

    for(int i=0, iMax=id_bytes.length; i<iMax; i++) {
        int c = (id_bytes[i]>>4) & 0xf;
        out.append((char)(c<10 ? '0'+c : 'a'+(c-10)));
        c = id_bytes[i] & 0xf;
        out.append((char)(c<10 ? '0'+c : 'a'+(c-10)));
    }
    out.append('>');
    return out.toString();
}

/**
 * Returns the pdf file's fonts.
 */
public Map <String, PDFFontEntry> getFonts()  { return _fonts; }

/**
 * Returns the current pdf font entry.
 */
public PDFFontEntry getFontEntry()  { return _fontEntry; }

/**
 * Sets the current font entry.
 */
public void setFontEntry(PDFFontEntry aFontEntry)  { _fontEntry = aFontEntry; }

/**
 * Returns the pdf font entry for a specific font.
 */
public PDFFontEntry getFontEntry(RMFont aFont, int fontCharSet)
{
    // Get font entry name
    String fontEntryName = fontCharSet==0? aFont.getFontName() : aFont.getFontName() + "." + fontCharSet;
    
    // Get FontEntry for base chars
    PDFFontEntry fontEntry = getFonts().get(fontEntryName);
    
    // If not present, create new font entry for font and add to fonts map
    if(fontEntry==null) {
        fontEntry = new PDFFontEntry(this, aFont, fontCharSet);
        _fonts.put(fontEntryName, fontEntry);
    }
    
    // Return font entry
    return fontEntry;
}

/**
 * Returns a map of image data names to image data references.
 */
public Map <String,String> getImages()  { return _images; }

/**
 * Adds an image data (uniqued) to file reference table, if not already present. 
 */
public void addImageData(RMImageData anImageData)
{
    // If not present, unique, add to xref table and add to image refs
    if(!_images.containsKey(anImageData.getName()))
        _images.put(anImageData.getName(), _xref.addObject(getUniqueImageData(anImageData)));
}

/**
 * Returns a unique image data for given image data.
 */
public RMImageData getUniqueImageData(RMImageData anImageData)
{
    int index = _imageDatas.indexOf(anImageData);
    if(index<0)
        _imageDatas.add(index = _imageDatas.size(), anImageData);
    return _imageDatas.get(index);
}

/**
 * Writes all entry objects to pdf buffer.
 */
public int writeXRefTable(RMPDFWriter aWriter)
{
    // Create list for offsets
    List <Integer> offsets = new Vector(_xref.getEntryCount());
    
    // Get buffer
    PDFBuffer buffer = aWriter.getBuffer();
    
    // First write the objects themselves, saving the file offsets for later use.
    // Call entries.size() every time in loop because objects are added as descriptions are generated.
    for(int i=0; i<_xref.getEntryCount(); i++) {
        
        offsets.add(buffer.length());
        buffer.appendln((i+1) + " 0 obj");
        Object entry = _xref.getEntry(i);
        
        // If encryption has been turned on, notify the encryptor of the top-level object we're about to write out.
        if(aWriter.getEncryptor() != null)
            aWriter.getEncryptor().startEncrypt(i+1, 0);
        
        aWriter.writeXRefEntry(entry);
            
        buffer.appendln();
        buffer.appendln("endobj");
    }
        
    // Record the offset where the xref table lands
    int xoff = buffer.length();
    
    // And spit out the table
    int count = _xref.getEntryCount();
    buffer.appendln("xref");
    buffer.appendln("0 " + (count+1));
    
    // The entries have to be 20 chars long each.
    DecimalFormat format = new DecimalFormat("0000000000");
    buffer.appendln("0000000000 65535 f ");
    for(int i=0; i<count; i++)
        buffer.appendln(format.format(offsets.get(i)) + " 00000 n ");
    
    // Return offset
    return xoff;
}

}