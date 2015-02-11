package com.reportmill.pdf.reader;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;
import java.awt.geom.*;
import java.util.*;

/**
 * This class is used by RMPDFFile to read a PDF file (page) for parsing.
 */
public class PDFPage {
    
    // The PDF file that this page is a part of
    PDFFile         _pdfFile;
    
    // The PDF page dictionary
    Map             _page;
    
    // Cached map of font aliases to RMFonts
    Map             _fonts;
    
    // The index of this page
    int             _index;

    // Resource dictionaries
    List            _resources;
    
    // cache so page is only rendered once
    BufferedImage   _pageBuffer;
    
/**
 * Creates a new PDF page for the given PDF file and page index.
 */
public PDFPage(PDFFile aPdfFile, int anIndex)
{
    // Set PDF file
    _pdfFile = aPdfFile;
    
    // Set page index
    _index = anIndex;

    // Get page dict
    _page = (Map)getPageDict(_pdfFile._pages, anIndex, new int[] {0});
    
    // Get the page's resources (which may be inherited) and initialize the resource stack
    Object r = _pdfFile.resolveObject(getPageResources());
    _resources = new ArrayList(1);
    if (r != null)
        _resources.add(r);
}

/** Returns the media box of this page. */
public Rectangle2D getMediaBox()
{
    // Get the bounds of the page and the cliprect to figure out the bbox and matrix of the form
    Object obj = _pdfFile.resolveObject(_pdfFile.inheritedAttributeForKeyInPage("MediaBox", _page));
  
    // List elements are xmin, ymin, xmax, ymax
    List bbox = (List)obj;
    float bx = ((Number)bbox.get(0)).floatValue(), by = ((Number)bbox.get(1)).floatValue();
    float bw = ((Number)bbox.get(2)).floatValue() - bx, bh = ((Number)bbox.get(3)).floatValue() - by;    
    return new Rectangle2D.Float(bx, by, bw, bh);
}

/** Returns the crop box of this page. */
public Rectangle2D getCropBox()
{
    // Get the bounds of the page and the cliprect to figure out the bbox and matrix of the form
    Object obj = _pdfFile.resolveObject(_pdfFile.inheritedAttributeForKeyInPage("CropBox", _page));
    if(obj==null)
        obj = _pdfFile.resolveObject(_pdfFile.inheritedAttributeForKeyInPage("MediaBox", _page));
  
    // List elements are xmin, ymin, xmax, ymax
    List bbox = (List)obj;
    float bx = ((Number)bbox.get(0)).floatValue(), by = ((Number)bbox.get(1)).floatValue();
    float bw = ((Number)bbox.get(2)).floatValue() - bx, bh = ((Number)bbox.get(3)).floatValue() - by;    
    return new Rectangle2D.Float(bx, by, bw, bh);
}

/** Returns the resources stack for this page.  */
public List getResources()  { return _resources; }

/** Most pages will have only a single resource dictionary, which
 * it may have inherited from the parent file.
 * However, if the page has Forms or Patterns in it, there may be
 * additional, temporary resource dictionaries.
 */
public void pushResources(Map r)  { _resources.add(r); }

public void popResources()  { _resources.remove(_resources.size()-1); }

/** Returns the main resource directory for the page, which may be a reference. */
public Object getPageResources()  { return _pdfFile.inheritedAttributeForKeyInPage("Resources", _page); }

/** Returns the named object from the page's resources.  
 * Used in more specific methods below for Fonts, ExtGStates, ColorSpaces, etc.
 * This method is specific to entries in the resource dictionary that
 * are themseleves dictionaries, but the only Resource which currently 
 * isn't a dictionaries is the ProcSet.
 * If you need the ProcSet or something other than a dictionary, use
 * getResources() and pull it out from there.
 */
public Object findResource(String resourceName, String name) {
  List rezstack = getResources();
  int n=rezstack.size();
  
  while(n-->0) {
      Hashtable resources = (Hashtable)_pdfFile.resolveObject(rezstack.get(n));
      Object adict = _pdfFile.resolveObject(resources.get(resourceName));
      Object obj;
      if (adict instanceof Map) {
          obj = ((Map)adict).get(name);
          if (obj != null)
              return _pdfFile.resolveObject(obj);
      }
    }
  return null;
}

/** Return the various resource dictionary objects.  */
//public Map getFonts() { return getResourceDictionaryNamed("Font"); }
//public Map getColorSpaces() { return getResourceDictionaryNamed("ColorSpace"); }
//public Map getXObjects() { return getResourceDictionaryNamed("XObject"); }
//public Map getExtendedGStates() { return getResourceDictionaryNamed("ExtGState"); }

/** Accessors for the resource dictionaries */
public Map getExtendedGStateNamed(String name) { return (Map)findResource("ExtGState", name); }

/** Returns the pdf Font dictionary for a given name (like "/f1").  You
 * can use the FontFactory to get interesting objects from the dictionary.
 */  
public Map getFontDictForAlias(String alias) { return (Map)findResource("Font", alias); }

/** A colorspace can be specified in several ways.
 *  If a colorspace needs no arguments, in can just be the name.
 *  Otherwise, it is an entry in the page's resource dictionary.
 *  The format of the object in the resource dictionary is dependent upon
 *  the type of the colorspace.
 *  Once we figure out what kind of colorspace it is and the parameters, 
 *  we can call the ColorFactory to make an awt ColorSpace object.
 *
 *  Note that colorspaces in pdf are usually specified as either
 *  strings (for one of the built-ins) or arrays (eg. [/CalRGB << ... >>])
 *  
 **/
public java.awt.color.ColorSpace getColorspace(Object csobj)
{
    int type=-1;
    Object params=null;
    String pdfName;
    Map paramDict;
    List cslist = null;
    
    // Strings are either a name of a built in color space or the name of a
    // colorspace resource
    if (csobj instanceof String) {
        Object resource;
        pdfName = (String)csobj;
        
        // TODO:This shouldn't really be necessary
        if (pdfName.charAt(0)=='/')
            pdfName=pdfName.substring(1);
        
        // All Device spaces are subject to possible remapping through a Default space
        if (pdfName.startsWith("Device")) {
            String defName = "Default"+pdfName.substring(6);
            resource = findResource("ColorSpace", defName);
            if (resource != null)
                return getColorspace(resource);
        }
        
        // Device... & Pattern are all fully specified by the name
        if (pdfName.equals("DeviceGray"))
            type = ColorFactory.DeviceGrayColorspace;
        else if (pdfName.equals("DeviceRGB"))
            type = ColorFactory.DeviceRGBColorspace;
        else if (pdfName.equals("DeviceCMYK"))
            type = ColorFactory.DeviceCMYKColorspace;
        else if (pdfName.equals("Pattern"))
            type = ColorFactory.PatternColorspace;
        else {
            // Look up the name in the resource dictionary and try again
            resource = findResource("ColorSpace", pdfName);
            if (resource != null)
                return getColorspace(resource);
        }
    }
    else if (csobj instanceof List) {
        // The usual format is [/SpaceName obj1 obj2...]
        cslist = (List)csobj;
        // We do color space cacheing by adding the awt colorspace as 
        // the last object in the vector.
        // The normal hashtable cacheing strategy is to add an element
        // with the key _rb_cached_..., but Adobe, in their inifinite
        // wisdom, decided that color spaces should be arrays instead 
        // of dictionaries, like everything else.
        // TODO:  Make sure not to export this extra element when saving pdf
        Object cachedObj = cslist.get(cslist.size()-1);
        if (cachedObj instanceof ColorSpace)
            return (ColorSpace)cachedObj;
        
        pdfName = ((String)cslist.get(0)).substring(1);
        params = cslist.size()>1 ? _pdfFile.resolveObject(cslist.get(1)) : null;
        
        if (pdfName.equals("CalGray")) 
            type = ColorFactory.CalibratedGrayColorspace;
        else if (pdfName.equals("CalRGB"))
            type = ColorFactory.CalibratedRGBColorspace;
        else if (pdfName.equals("Lab"))
            type = ColorFactory.LabColorspace;
        else if (pdfName.equals("ICCBased"))
            type = ColorFactory.ICCBasedColorspace;
        else if (pdfName.equals("Pattern")) {
            type = ColorFactory.PatternColorspace;
            if (params != null)
                params = getColorspace(params);
        }
        else if (pdfName.equals("Separation")) {
            type = ColorFactory.SeparationColorspace;
            paramDict = new Hashtable(2);
            paramDict.put("Colorant", cslist.get(1));
            paramDict.put("Base",getColorspace(_pdfFile.resolveObject(cslist.get(2))));
            paramDict.put("TintTransform",
                    PDFFunction.getInstance(_pdfFile.resolveObject(cslist.get(3)),_pdfFile));
            params = paramDict;
         }
        else if (pdfName.equals("DeviceN")) {
            type = ColorFactory.DeviceNColorspace;
            paramDict = new Hashtable(2);
            paramDict.put("Colorants", _pdfFile.resolveObject(cslist.get(1)));
            paramDict.put("Base", getColorspace(_pdfFile.resolveObject(cslist.get(2))));
            paramDict.put("TintTransform",
                    PDFFunction.getInstance(_pdfFile.resolveObject(cslist.get(3)),_pdfFile));
            if (cslist.size()>4)
                paramDict.put("Attributes", _pdfFile.resolveObject(cslist.get(4)));
            
            params=paramDict;
        }
        else if (pdfName.equals("Indexed")) {
            type = ColorFactory.IndexedColorspace;
            //  [/Indexed basecolorspace hival <hex clut>]
            // params set above is the base colorspace.
            // Turn it into a real colorspace
            // NB: this is recursive and there's no check for 
            // following illegal sequence, which would cause
            // an infinite recursion:
            //   8 0 obj
            //   [/Indexed  8 0 R  1 <FF>]
            //   endobj
            // Also note that in the time it took to write this
            // comment, you could have put in a check for
            // this case.
            if (cslist.size() != 4)
                throw new PDFException("Wrong number of elements in colorspace definition");
 
            if ((params instanceof String) && ( ((String)params).charAt(0)=='/') )
                params = ((String)params).substring(1);
            
            ColorSpace base = getColorspace(params);
            Object val;
            Object hival = cslist.get(2);
            byte lookup_table[];
            
            if (!(hival instanceof Number))
                throw new PDFException("Illegal Colorspace definition "+cslist);

            // The lookuptable is next
            val = _pdfFile.resolveObject(cslist.get(3));
            if (val instanceof PDFStream) 
                lookup_table = ((PDFStream)val).decodeStream();
            else if (val instanceof String) {
                // get the pageparser to decode the hex
                // NB:  For historical reasons, the PDFParser doesn't
                // interpret the string as hex, but just leaves it alone.
                // It probably makes sense to move much of this parsing
                // back into the PDFParser and let javacc handle it.
                lookup_table = PDFPageParser.getPDFHexString((String)val);
            }
            else if (val instanceof byte[]) {
                // In the case of inline images, the pageparser has already
                // done the conversion.
                lookup_table = (byte [])val;
            }
            else 
                throw new PDFException("Can't read color lookup table");
            
            //Build a dictionary to pass to the colorFactory
            paramDict = new Hashtable(3);
            paramDict.put("Base", base);
            paramDict.put("HiVal",hival);
            paramDict.put("Lookup",lookup_table);
            params = paramDict;
        }
    }

    if (type != -1) {
        ColorSpace outerSpace = _pdfFile.getColorFactory().createColorSpace(type, params);
        // cache it
        if (cslist != null)
            cslist.add(outerSpace);
        return outerSpace;
    }

    throw new PDFException("Unknown colorspace : "+csobj);
}

// Like above, but for XObjects.
// XObjects can be Forms or Images.
// If the dictionary represents an Image, this routine calls the ImageFactory
// to create a java.awt.Image.
// If it's a Form XObject, the object returned will be a PDFForm.
// 
//public java.awt.Image getImageXObject(String pdfName)
public Object getXObject(String pdfName)
{
    PDFStream xobjStream = (PDFStream)findResource("XObject",pdfName);
    
    if (xobjStream != null) {
        Map xobjDict = xobjStream.getDictionary();
        // Check to see if we went through this already
        Object cached = xobjDict.get("_rbcached_xobject_");
        if (cached != null)
            return cached;
        
        String type = (String)xobjDict.get("Subtype");
        if (type==null)
            throw new PDFException("Unknown xobject type");
        
        // Image XObject - pass it to the ImageFactory
        if (type.equals("/Image")) {
            // First check for a colorspace entry for the image, and
            // create an awt colorspace.
            Object space = _pdfFile.resolveObject(xobjDict.get("ColorSpace"));
            ColorSpace imageCSpace = space==null ? null : getColorspace(space);
            cached = _pdfFile.getImageFactory().getImage(xobjStream, imageCSpace, _pdfFile);
        }
        else if (type.equals("/Form"))  // A PDFForm just saves the stream away for later parsing
            cached = new PDFForm(xobjStream);
        
        if (cached != null) {
            xobjDict.put("_rbcached_xobject_", cached);
            return cached;
        }
    }
    System.err.println("Unable to get xobject named \""+pdfName+"\"");
    return null;
}

/** Creates a new pattern object for the resource name */
public PDFPattern getPattern(String pdfName)
{
    Object pat = findResource("Pattern", pdfName);
    PDFPattern patobj = PDFPattern.getInstance(pat, _pdfFile);
    
    // Resolve the colorspace.
    if (patobj instanceof PDFPatternShading) {
        Map shmap = (Map)_pdfFile.resolveObject(((Map)pat).get("Shading"));
        Object csobj = _pdfFile.resolveObject(shmap.get("ColorSpace"));
        if (csobj != null) 
          ((PDFPatternShading)patobj).setColorSpace(this.getColorspace(csobj));
    }
    
    return patobj;
}

/** Creates a new shadingPattern for the resource name.  Used by the shading operator */
public PDFPatternShading getShading(String pdfName)
{
    Map pat = (Map)findResource("Shading", pdfName);
    PDFPatternShading patobj = PDFPatternShading.getInstance(pat, _pdfFile);
    
    // Resolve the colorspace.
    Object csobj = _pdfFile.resolveObject(pat.get("ColorSpace"));
    if (csobj != null) 
          patobj.setColorSpace(this.getColorspace(csobj));
    return patobj;
}

/** Returns the page contents for this page. */
public Object getPageContents()
{
    // Get the contents of the page
    Object contents = _pdfFile.resolveObject(_page.get("Contents"));
  
    // contents is always an indirect reference, and could be a stream or an array of streams.
    // A Form XObject is a stream, not a reference to one, so we have to take the old contents
    // of the page in whatever form and create a stream for the form.
    if((contents instanceof Vector) && (((Vector)contents).size() == 1))
        contents = _pdfFile.resolveObject(((Vector)contents).get(0));
    
    return contents;
}

/** Returns the page contents as a PDF stream. */
public PDFStream getPageContentsStream()
{
    // Catch exceptions
    try {
    
    // Get the contents of the page
    Object contents = getPageContents();

    // If page contents is array of streams, concat into one (decode & decompress first)
    if(contents instanceof Vector) {
        Vector carray = (Vector)contents;
        byte subdatas[][] = new byte[carray.size()][];
        int sdatalen = 0;
        
        for(int i=0, iMax=carray.size(); i<iMax; i++) {
        
            // Get object and complain if not PDFStream
            Object obj = _pdfFile.resolveObject(carray.get(i));
            if(!(obj instanceof PDFStream))
                throw new PDFException("Element of page's contents array is not a stream");
            PDFStream onestream = (PDFStream)obj;
            
            // Resolve the filter parameters 
            Map onestreamdict = onestream.getDictionary();
            obj = _pdfFile.resolveObject(onestreamdict.get("Filter"));
            if(obj!=null) 
                onestreamdict.put("Filter", obj);
            obj = _pdfFile.resolveObject(onestreamdict.get("DecodeParms"));  // need to resolve anything inside?
            if(obj!=null) 
                onestreamdict.put("DecodeParms", obj);
                
            // decode the data
            subdatas[i] = onestream.decodeStream();
            sdatalen += subdatas[i].length;
        }
            
        // Append all the subdatas into a single array
        byte sdata[] = new byte[sdatalen];
        int destpos=0;
        for(int i=0, iMax=carray.size(); i<iMax; i++) {
            System.arraycopy(subdatas[i],0,sdata,destpos,subdatas[i].length);
            destpos += subdatas[i].length;
        }

        contents = new PDFStream(sdata, null);
    }
    
    // If page contents is single stream,
    else if(!(contents instanceof PDFStream))
        contents = null;

    // Return page contents stream
    return (PDFStream)contents;
    
    // Catch exceptions
    } catch(Exception e) { 
        System.err.println("Error decoding page contents : "+e);
        return null;
        }
}

/**
 * Set everything to the default implementations and return an Image for this page.
 */
public BufferedImage getImage()
{
    // If image hasn't been loaded, load it
    if(_pageBuffer==null) {

        DefaultFactories factories = new DefaultFactories();
    
        // Create a markup handler to a handler that renders into an image
        BufferedMarkupHandler buf = new BufferedMarkupHandler();
        
        // Set the rendering parameters
        // TODO: The various markupHandlers might want to do some of these by default.
        buf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        buf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        buf.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        buf.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        buf.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        buf.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        _pdfFile.setMarkupHandler(buf);
        
        // Set the object creation factories to the defaults
        _pdfFile.setColorFactory(factories);
        _pdfFile.setFontFactory(new PDFFontFactory());
        _pdfFile.setPathFactory(factories);
        _pdfFile.setImageFactory(new PDFImageFactory());
        
        // we want the EXGStates
        _pdfFile.setStripsExtendedGStates(false);
        
        // let 'er rip
        parse();
        
        // return the image created by the markup handler
        _pageBuffer = buf.getImage();
    }
    
    // Return image
    return _pageBuffer;
}

/**
 * Draw the page to g,  scaled to fit the rectangle r.
 */
public void paint(Graphics2D g, Rectangle2D r)
{
    Java2DMarkupHandler j2d = new Java2DMarkupHandler(g);
    DefaultFactories factories = new DefaultFactories();
    
    // For printing, use whatever rendering hints the graphics2D already has

    // set the markup handler to the java2d version
    _pdfFile.setMarkupHandler(j2d);
    
    // Set the object creation factories to the defaults
    _pdfFile.setColorFactory(factories);
    _pdfFile.setFontFactory(new PDFFontFactory());
    _pdfFile.setPathFactory(factories);
    _pdfFile.setImageFactory(new PDFImageFactory());
    
    // we want the EXGStates
    _pdfFile.setStripsExtendedGStates(false);
    
    // set the destination rectangle
    j2d.setDestinationRect(r);
    
    // let 'er rip
    parse();
    
    // Clean up.
    j2d.endPage();
}
    
/**
 * Main entry point for parsing the page marking operations
 */
public void parse()
{
    try { PDFPageParser.parsePage(_pdfFile, _index); }
    catch(Exception e) { e.printStackTrace(); }
}

/**
 * Returns the page dict for this page.
 */
public Object getPageDict(Map pages, int pnum, int start[])
{
    Object type = pages.get("Type");
    
    // hit a leaf node
    if(type.equals("/Page")) {
        if(start[0] == pnum)
            return pages;
        ++start[0];
        return null;
    }
    
    if(!type.equals("/Pages")) {
        _pdfFile._valid = false;
        return null;
    }
    
    int count = ((Integer)_pdfFile.resolveObject(pages.get("Count"))).intValue();
    if(pnum>=start[0] && (start[0]+count>pnum)) {
        Vector kids = (Vector)_pdfFile.resolveObject(pages.get("Kids"));
        count = kids.size();
        for(int i=0; i<count; ++i) {
            Object obj = _pdfFile.resolveObject(kids.get(i));
            obj = getPageDict((Hashtable)obj, pnum, start);
            if(obj!=null)
                return obj;
        }
        _pdfFile._valid = false;
    }
    
    // Increment start and return null
    start[0] += count;
    return null;
}

}