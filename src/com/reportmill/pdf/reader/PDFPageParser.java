package com.reportmill.pdf.reader;
import java.util.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.*;
import java.awt.color.*;
import java.io.UnsupportedEncodingException;

/**
 * This class parses the page marking operators for a specific page number (it gets the
 * contents for that page from n RMPDFParser.)  It uses the various factory objects for
 * graphic object creation and a MarkupHandler to do the actual drawing.
 *
 *  Currently unsupported:
 *       - Ignores hyperlinks (annotations)
 *       - Type 1 & Type 3 fonts
 *       - Transparency blend modes other than /Normal
 *       ...
 */
public class PDFPageParser {
    
    // The PDF file that owns this parser
    PDFFile              _pdfFile;
    
    // The page index of the page being parsed
    int                  _pageIndex;
    
    // The bounds of the area being parsed
    Rectangle2D           _bounds;
    
    // The tokens of the page being parsed
    List                 _tokens;
    
    // The gstates of the page being parsed
    Stack                _gstates;
    
    // The current text object
    // It will get created once and reused over & over
    PDFTextObject        _textObj = null;
    
/** Returns an RMShape for a given PDF file and a page index. */
static void parsePage(PDFFile aPdfFile, int aPageIndex)
{
    // Start parsing with a new pageParser instance
    PDFPageParser pp = new PDFPageParser(aPdfFile, aPageIndex);
    pp.parse();
}

/** Creates a new page parser for a given PDF file and a page index. */
public PDFPageParser(PDFFile aPdfFile, int aPageIndex)
{
    // Cache given pdf file
    _pdfFile = aPdfFile;
    
    // Cache page index
    _pageIndex = aPageIndex;
    
    // Get Media box
    Rectangle2D media = getPage().getMediaBox();
    
    // Get the crop box
    Rectangle2D crop = getPage().getCropBox();
    
    // The bounds of the area we're drawing into is defined as the 
    // intersection of the crop box and the media box.
    _bounds = media.createIntersection(crop);

    // Create the gstate list and the default gstate
    _gstates = new Stack();
    _gstates.push(new PDFGState());
    
    // Initialize gstate to bounds
    getGState().trans.setToTranslation(-_bounds.getX(),-_bounds.getY());
    
    //TODO need to set transform for pages with "/Rotate" key
    //AffineTransform  t = getGState().trans; t.setToTranslation(-_bounds.getX(),-_bounds.getY());
    //t.rotate(-Math.PI/2); t.translate(0,_bounds.getWidth());
    //Point2D pt = new Point2D.Float(0,0); System.out.println(pt+" -> "+t.transform(pt,null));
    //pt = new Point2D.Double(_bounds.getMaxX(), _bounds.getMaxY());
    //System.out.println(pt+" -> "+t.transform(pt,null));
    //TODO also need to make sure PDFPage returns right rect (ImageShape initialized from file) 
}

/** This is really kind of stupid.  Might as well just hold on to the page. */
PDFPage getPage() { return _pdfFile.getPage(_pageIndex); }

/**
 * The lexer.  Fills the tokens list from the page contents. 
 * Returns the index of the character after the last successfully consumed character.
 */
public int getTokens(byte pageBytes[], int offset, int end, List tokens)
{
    PageToken aToken;
    Range r;
    int i;
    
    // Iterate over token string chars
    for(i=offset; i<end; i++) {
        
        // Get current token char
        byte c = pageBytes[i];

        // Reset token
        aToken = null;
        
        // Handle comment - toss the rest of the line
        if (c=='%') {
            while((++i<end) && (pageBytes[i] != '\r') && (pageBytes[i] != '\n'));
        }
        
        // Handle array start
        else if (c=='[') {
            List arrayTokens = new ArrayList(4);
            // recurse
            i = getTokens(pageBytes, i+1, end, arrayTokens);
            aToken = new PageToken(PageToken.PDFArrayToken, arrayTokens);
        }
        
        // Handle array close
        else if (c==']') {
            return i;//+1;
        }
        
        // Handle string start
        else if (c=='(') {
            aToken = new PageToken(PageToken.PDFStringToken);
            i = getPDFString(pageBytes, i+1, end, aToken);
        }
        
        // Handle hex string or dict
        else if (c=='<') {
            if ((i<end-1) && (pageBytes[i+1]=='<')) {
                aToken = new PageToken(PageToken.PDFDictOpenToken);
                ++i;
            }
            else {
                aToken = new PageToken(PageToken.PDFStringToken);
                i = getPDFHexString(pageBytes, i+1, end, aToken);
            }
        }
        
        // Handle hex string end
        else if ((c=='>') && (i<end-1) && (pageBytes[i+1]=='>')) {
            aToken = new PageToken(PageToken.PDFDictCloseToken);
            ++i;
        }
        
        // Handle name
        else if (c=='/') {
            r = new Range(i,0);
            aToken = new PageToken(PageToken.PDFNameToken);
            while(++i<end) {
                c = pageBytes[i];
                // whitespace ends name
                if ((c==' ') || (c=='\t') || (c=='\r') || (c=='\n') || (c=='\f')) {
                    r.length = i-r.location;
                    break;
                }
                // other delimeter.  end name and back up 
                if ((c=='(') || (c==')') || (c=='<') || (c=='>') || (c=='[') || (c==']') ||
                    (c=='{') || (c=='}') || (c=='/') || (c=='%')) {
                    r.length = i-r.location;
                    --i;
                    break;
                }
            }
            // end of stream also ends name
            if (i==end)
                r.length = end-r.location;
            aToken.value = r;
        }
        
        // Handle
        else if ((c=='+')||(c=='-')||(c=='.')||((c>='0') && (c<='9'))) {
            aToken = new PageToken(PageToken.PDFNumberToken);
            i= getPDFNumber(pageBytes, i, end, aToken);
        }
        
        // Handle
        else if (((c=='t') || (c=='f')) && ((aToken=checkPDFBoolean(pageBytes,i,end))!=null)) {
            if (aToken.boolValue())
                i+=3;
            else i+=4;
        }
        
        // Handle ID
        else if ((c=='I') && (i<end-4) && (pageBytes[i+1]=='D')) {
            // skip over the ID
            i+=2;
            
            // Check for a single whitespace char.  This is present for data types other than asciihex & ascii85
            if ((pageBytes[i]==' ') || (pageBytes[i]=='\t') || (pageBytes[i]=='\n'))
                ++i;
            
            r = new Range(i,0);
            
            // Inline image data - slurp up all the data up to the EI token depending on the encoding stream,
            // the first byte might or might not be significant.
            //
            // I don't understand this.  The data is arbitrary binary data, and unlike the stream object, which has a
            // /Length parameter, the inline image has no known length.  How then, can you be guaranteed that you're
            // not going to have a sequence like 'EI' somewhere in the middle of your data?
            while(i<end) {
                if((pageBytes[i]=='\n' || pageBytes[i]==' ' || pageBytes[i]=='\t') &&
                    (i+2<end) && (pageBytes[i+1]=='E') && (pageBytes[i+2]=='I'))
                    break;
                ++i;
            }
            if (i>=end) 
                throw new PDFException("Unterminated inline image data");
            aToken = new PageToken(PageToken.PDFInlineImageData);
            r.length = i-r.location;
            aToken.value = r;
            // skip over 'EI'
            i+=2;
        }
        
        // Handle
        else if ((c!=' ') && (c!='\t') && (c!='\r') && (c!='\n') && (c!='\f')) {
            r = new Range(i,0);
            aToken = new PageToken(PageToken.PDFOperatorToken);
            while(++i<end) {
                c = pageBytes[i];
                if ((c==' ') || (c=='\t') || (c=='\r') || (c=='\n') || (c=='\f')) {
                    r.length = i-r.location;
                    break;
                }
                else if ((c=='(') || (c=='/') || (c=='[') || (c=='<') || (c=='%')) {
                    r.length=i-r.location;
                    --i;
                    break;
                }
            }
            if (i==end) r.length=i-r.location;
            aToken.value = r;
        }
        
        // Add new token to the array
        if (aToken != null)
            tokens.add(aToken);
    }
    
    // Return end index
    return i;
}

/** Returns the token at the given index. */
private PageToken getToken(int index) { return (PageToken)_tokens.get(index); }

/** Returns the token at the given index as a float. */
private float getFloat(int i) { return getToken(i).floatValue(); }

/** Returns the token at the given index as an int. */
private int getInt(int i) { return getToken(i).intValue(); }

/** Returns the token at the given index as an array of floats */
private float[] getFloatArray(int i) 
{
    List ftokens = (List)(getToken(i).value);
    int jMax = ftokens.size();
    float farray[] = new float[jMax];
    
    // Iterate over array tokens and assume they're all numbers
    for(int j=0; j<jMax; j++)
        farray[j] = ((PageToken)ftokens.get(j)).floatValue();
    return farray;
}

/** Gets the token at the given index as a point. */
void getPoint(int i, Point2D.Float pt)
{
    pt.x = getToken(i-2).floatValue();
    pt.y = getToken(i-1).floatValue();
}

/** Returns a new point at the given index */
Point2D.Float getPoint(int i) 
{
    Point2D.Float pt = new Point2D.Float();
    getPoint(i,pt);
    return pt;
}

/** Returns the token at the given index as a transform. */
private AffineTransform getTransform(int i)
{
    float a = getToken(i-6).floatValue(), b = getToken(i-5).floatValue();
    float c = getToken(i-4).floatValue(), d = getToken(i-3).floatValue();
    float tx = getToken(i-2).floatValue(), ty = getToken(i-1).floatValue();
    return new AffineTransform(a, b, c, d, tx, ty);
}

/**
 * Process any escape sequences. Note that this method and the one below are destructive. The pageBytes buffer gets
 * modified to the exact bytes represented by the escapes.  A buffer that starts out as "(He\154\154o)" would then
 * become "(Hello4\154o) and the token would point to "Hello". No new storage is required and everything can be
 * represented as a byte buffer. This means that if you wanted to parse the buffer a second time, you'd better get
 * the stream again from the PDFPage.
 */
private int getPDFString(byte pageBytes[], int start, int end, PageToken tok)
{
    int parenDepth = 1;
    int dest=start;
    Range r = new Range(start,start);
    
    while(start<end) {
        byte c = pageBytes[start++];
        if (c=='(')
            ++parenDepth;
        else if ((c==')') && (--parenDepth==0))
            break;
        else if (c=='\r') { //  replace '\r' or '\r\n' with a single '\n'
            c='\n';
            if ((start<end) && (pageBytes[start]=='\n'))
                ++start;
        }
        else if ((c=='\\') && (start<end)) {  // escapes
            c=pageBytes[start++];
            switch(c) {
            case 'n' : c='\n'; break;
            case 'r' : c='\r'; break;
            case 't' : c='\t'; break;
            case 'b' : c='\b'; break;
            case 'f' : c='\f'; break;
            case '(' : break;
            case ')' : break;
            case '\\' : break;
            case '\r' : if (start<end) {  // escape+EOL skips the EOL
                            // skip \n also if EOL is \r\n
                            if (pageBytes[start+1]=='\n') 
                                ++start;
                            continue;
                        }
                        break;
            case '\n' : continue;
            default :
                if ((c>='0') && (c<='7')) {
                    int octal = c-'0';
                    for(int i=1; i<3; ++i)
                        if (start<end) {
                            c=pageBytes[start++];
                            if ((c>='0') && (c<='7'))
                                octal = (octal<<3) | (c-'0');
                            else {
                                --start;
                                break;
                            }
                        }
                     c=(byte)octal;
                     }
                else {} // backslash ignored if not one of the above
           }
        }
        pageBytes[dest++]=c;
        }
    r.length = dest-r.location;
    tok.value = r;
    return start-1;
}

/**
 * Hex strings: <AABBCCDDEEFF0011...>. See comment above.
 */
private int getPDFHexString(byte pageBytes[], int start, int end, PageToken tok)
{
    Range r = new Range(0,0);
    int next_char = PDFPageParser.getPDFHexString(pageBytes, start, end, r);
    tok.value = r;
    return next_char;
}

public static byte[] getPDFHexString(String s)
{
   Range decodedRange = new Range();
   byte asciihex[], decoded[];
   char beginchar= s.charAt(0);
   
   // Strings beginning with '<' are hex, '(' are binary. I wonder what would happen if the binary string in the pdf
   // winds up starting with a unicode byte-order mark.  Does the string get weird?
   if (beginchar == '(') {
       // This same code is in at least 3 different places.
       // This should really be consolidated.
       int pos=0;
       int len = s.length();
       asciihex = new byte[len-2];
       char c;
       for(int i = 1; i<len-1; ++i) {
           c = s.charAt(i);
           if (c=='\\') {
               if (++i == len-1)
                   throw new PDFException("Bad character escape in binary string");
               c=s.charAt(i);
               if (c>='0' && c<='7') {
                   int oval = c-'0';
                   for(int j=0; (j<2) && (i<len-1); ++j) {
                       if (++i<len-1) {
                           c=s.charAt(i);
                           if (c>='0' && c<='7') {
                               oval = (oval*8)+(c-'0');
                           }
                           else {
                               --i;
                               break;
                           }
                       }
                   }
                   c = (char)oval;
               }

           }
           else if (c==')') // probably never happen 
               break;
           asciihex[pos++]=(byte)c;
       }
       decodedRange.location = 0;
       decodedRange.length = pos;
   }
   else if (beginchar == '<') {
    
       // Get the bytes
      try { asciihex = s.getBytes("US-ASCII"); }
      catch(UnsupportedEncodingException e) {
        // sould never happen.  it's ascii for crying out loud
        throw new PDFException("Internal error - can't decode an ascii string");
     }
    
      // decode the ascii
      getPDFHexString(asciihex, 1, asciihex.length, decodedRange);
   }
   else throw new PDFException("Illegal character in binary string");
   
    // copy decoded bytes into an array matching the decoded size
    decoded = new byte[decodedRange.length];
    System.arraycopy(asciihex, decodedRange.location, decoded, 0, decodedRange.length);
    return decoded;
}

/**
 * Replace ascii hex in pageBytes with actual bytes. Start points to first char after the '<', end is the upper limit
 * to seek through. r gets filled with the actual ranbge of the converted bytes return value is index of last character
 * swallowed. See comment for getPDFString... about destructive behavior.
 */
public static int getPDFHexString(byte pageBytes[], int start, int end, Range r)
{
    int dest=start;
    byte high=0;
    boolean needhigh=true;
    
    r.location = start;
    while(start<end) {
        byte c = pageBytes[start++];
        if (c=='>')
            break;
        if ((c>='a') && (c<='f')) 
            c=(byte)((c-'a')+10);
        else if ((c>='A') && (c<='F'))
            c=(byte)((c-'A')+10);
        else if ((c>='0') && (c<='9'))
            c=(byte)(c-'0');
        else if ((c==' ') || (c=='\t') || (c=='\n') || (c=='\r') || (c=='\f'))
            continue;
        else { throw new PDFException("invalid character in hex string"); }
        
        if (needhigh) {
            high=(byte)(c<<4);
            needhigh=false;
        }
        else {
            pageBytes[dest++]=(byte)(high|c);
            needhigh=true;
        }
    }
    if (!needhigh) // odd number of hex chars - last nibble is 0
        pageBytes[dest++]=high;
    r.length = dest-r.location;
    return start-1;
}

/** Numbers (floats or ints) Exponential notation not allowed in pdf */
private int getPDFNumber(byte pageBytes[], int start, int end, PageToken tok)
{
    int parts[]={0,0};
    int whichpart=0, div=1;
    int sign=1;
    boolean good=false;
    
    if (pageBytes[start]=='+')
        ++start;
    else if (pageBytes[start]=='-') {
        sign=-1;
        ++start;
    }
    
    while(start<end) {
        byte c = pageBytes[start];
        if (c=='.') {
            if (++whichpart>1)
                throw new PDFException("Illegal number");
        }
        else if ((c>='0') && (c<='9')) {
            parts[whichpart] = parts[whichpart]*10+(c-'0');
            if (whichpart==1)
                div*=10;
            good=true;
        }
        else break;
        ++start;
    }
    if (!good)
        throw new PDFException("Illegal number");
    
    if (whichpart==0)
        tok.value = sign*parts[0];
    else tok.value = sign*(parts[0]+((float)parts[1])/div);
    return start-1;
}

PageToken checkPDFBoolean(byte pageBytes[], int start, int end)
{
    Boolean v=null;
    // allocates a string, unlike most other parts of the lexer.
    // probably not a big deal, since it only happens for ops starting with 't' or 'f'.
    // This could always be unrolled or perhaps changed to use byte arrays
    try {
    if ((pageBytes[start]=='t') && (end-start>4) && 
        ("true".equals(new String(pageBytes, start, 4, "US-ASCII"))))
        v=Boolean.TRUE;
    else if ((end-start>5) && ("false".equals(new String(pageBytes, start, 5, "US-ASCII"))))
        v=Boolean.FALSE;
    // should really check next char so we dont match a token like 'trueness' or 'falsehood'
    if (v!=null) 
        return new PageToken(PageToken.PDFBooleanToken, v);
    }
    catch (UnsupportedEncodingException uee) {}
    
    return null;
}

/**
 * Main entry point. Runs the lexer on the pdf content and passes the list of tokens to the parser. By separating out
 * a routine that operates on the list of tokens, we can implement Forms & patterns by recursively calling the parse
 * routine with a subset of the token list.
 */
public void parse()
{
    // Get page contents as stream
    PDFStream pageStream = getPage().getPageContentsStream();
    if(pageStream==null)
        return;
    
    // Decompress, decode, etc.
    byte pageBytes[] = pageStream.decodeStream();
    
    // Create top-level list of tokens and run the lexer to fill the list
    List pageTokens = new ArrayList(32);
    getTokens(pageBytes, 0, pageBytes.length, pageTokens);
 
    // Start the markup handler
    PDFMarkupHandler engine = _pdfFile.getMarkupHandler();
    engine.beginPage((float)_bounds.getWidth(), (float)_bounds.getHeight());
    
    // Initialize a text object
    _textObj = new PDFTextObject(engine.getFontRenderContext());
    
    // Parse the tokens
    parse(pageTokens, pageBytes);
}

/** The meat and potatoes of the pdf parser.
 * Translates the token list into a series of calls to either a Factory class,
 * which creates a Java2D object (like GeneralPath, Font, Image, GlyphVector, etc.),
 * or the markup handler, which does the actual drawing.
 */
public void parse(List tokenList, byte pageBytes[]) 
{
    // save away the factory callback handler objects
    PDFMarkupHandler engine = _pdfFile.getMarkupHandler();
    PathFactory pathFactory = _pdfFile.getPathFactory();
    PDFGState gs;
    ColorSpace cspace;
    Color acolor;
    int compatibility_sections=0;
    // This routine is potentially recursive, so save the 
    // tokens list on the stack
    List oldTokens =_tokens;
    
    // Set the token list that will be used by routines like getToken()
    _tokens = tokenList;
    
    // Initialize the current path
    // Note that in PDF, the path is not part of the GState and so is not
    // saved and restored by the gstate operators
    GeneralPath path = null, future_clip=null;
    // The number of operands available for the current operator
    int numops = 0;
    // for errors and operations that require multiple tokens
    boolean swallowedToken, didDraw;
    
    // Get the current gstate
    gs = getGState();
    
    // Iterate over page contents tokens
    for(int i=0, iMax=_tokens.size(); i<iMax; i++) {
        
        // Get the current loop token
        PageToken token = getToken(i);
        swallowedToken = didDraw = false;
        
        if (token.type == PageToken.PDFOperatorToken) {
            int tstart = token.tokenLocation();
            int tlen = token.tokenLength();
            
            // Switch on first byte of the operator
            byte c = pageBytes[tstart];
            switch(c) {
            case 'b' : //closepath,fill,stroke (*=eostroke)   
                if (numops == 0) {
                    if (tlen==1)  // b
                        path.setWindingRule(GeneralPath.WIND_NON_ZERO);
                    else if ((tlen==2) && (pageBytes[tstart+1] =='*'))  // b*
                        path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                    else break;
                    path.closePath();
                    engine.fillPath(gs, path);
                    engine.strokePath(gs, path);
                    swallowedToken=true;
                    didDraw = true;
                }
                break;
            case 'B' : // fill,stroke (*=eostroke)
                if ((numops==0) && ((tlen==1) || ((tlen==2) && (pageBytes[tstart+1]=='*')))) {
                    if (tlen==1)
                        path.setWindingRule(GeneralPath.WIND_NON_ZERO);
                    else 
                        path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                    engine.fillPath(gs, path);
                    engine.strokePath(gs, path);
                    didDraw = true;
                    swallowedToken=true;
                }
                else if ((numops==0) && (tlen==2)) {
                    if (pageBytes[tstart+1]=='T') { //BT
                        // Begin text object
                        _textObj.begin();
                        swallowedToken=true;
                    }
                    else if (pageBytes[tstart+1]=='X') {//BX 
                        // start (possibly nested) compatibility section
                        ++compatibility_sections;
                        swallowedToken=true;
                    }
                    // BI - inline images
                    else if (pageBytes[tstart+1]=='I') {//BI
                        i=parseInlineImage(i+1, pageBytes);
                        swallowedToken=true;
                    }
                }
                else if ((tlen==3) && (pageBytes[tstart+2]=='C')) {
                    if ((pageBytes[tstart+1]=='D') || (pageBytes[tstart+1]=='M'))  //BDC, BMC
                        swallowedToken=true;
                } 
                break;
            case 'c' : // c, cm, cs
                if (tlen==1) {
                    // Cureveto
                    if (numops==6) {
                        getPoint(i, gs.cp);
                        path.curveTo(getFloat(i-6), getFloat(i-5),
                                getFloat(i-4), getFloat(i-3),
                                gs.cp.x, gs.cp.y);
                        swallowedToken=true;
                    }
                }
                else if (tlen==2) {
                    c=pageBytes[tstart+1];
                    if ((c=='m') && (numops==6)) { //cm
                        // Concat matrix
                        gs.trans.concatenate(getTransform(i));
                        swallowedToken=true;
                    }
                    else if ((c=='s') && (numops==1)) { //cs
                        // Set non-stroke colorspace 
                        String space = getToken(i-1).nameString(pageBytes);
                        gs.colorSpace = getPage().getColorspace(space);
                        swallowedToken=true;
                    }
                }
                break;
            case 'C' : // CS stroke colorspace
                if ((tlen==2) && (pageBytes[tstart+1]=='S') && (numops==1)) {
                    String space = getToken(i-1).nameString(pageBytes);
                    gs.scolorSpace = getPage().getColorspace(space);
                    swallowedToken=true;
                }
                break;
            case 'd' : //setdash
                if ((tlen==1) && (numops==2)) {
                    gs.lineDash = getFloatArray(i-2);
                    gs.dashPhase = getFloat(i-1);
                    gs.lineStroke = pathFactory.createStroke(gs);
                    swallowedToken=true;
                }
                // d0 & d1 are only available in charprocs streams
                break;
            case 'D' : // xobject Do [also DP]
                if (tlen==2) {
                    if ((pageBytes[tstart+1]=='o') && (numops==1)) {
                        String iname=getToken(i-1).nameString(pageBytes);
                        Object xobj = getPage().getXObject(iname);
                        if (xobj instanceof Image) {
                            drawImage((Image)xobj);
                            swallowedToken=true;
                        }
                        else if (xobj instanceof PDFForm) {
                            executeForm((PDFForm)xobj);
                            swallowedToken=true;
                        }
                        else throw new PDFException("Error reading XObject");
                    }
                    else if (pageBytes[tstart+1]=='P') { // DP marked content
                        swallowedToken=true;
                    }
                }
                break;
            case 'E' : // [also EI,EMC]
                if ((tlen==2) && (numops==0)) {
                    if (pageBytes[tstart+1]=='T') {  //ET
                        _textObj.end();
                        swallowedToken = true;
                    }
                    else if (pageBytes[tstart+1]=='X') { // EX
                        if (--compatibility_sections<0)
                            throw new PDFException("Unbalanced BX/EX operators");
                        swallowedToken = true;
                    }
                }
                else if ((tlen==3) && (pageBytes[tstart+1]=='M') && (pageBytes[tstart+2]=='C'))
                    swallowedToken = true;
                break;
            case 'f' : // fill (*=eofill)  
            case 'F' : // F is the same as f, but obsolete
                if (tlen==1)
                    path.setWindingRule(GeneralPath.WIND_NON_ZERO);
                else if ((tlen==2) && (pageBytes[tstart+1]=='*')) 
                    path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                else break;
                
                engine.fillPath(gs, path);
                didDraw = true;
                swallowedToken=true;
                break;
            case 'g' : // setgray
                if (tlen==1) {
                    cspace = getPage().getColorspace("DeviceGray");
                    gs.color = getColor(cspace,i,numops);
                    gs.colorSpace = cspace;
                    swallowedToken=true;
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='s') && (numops==1)) { // gs
                    // Extended graphics state
                    Map exg = getPage().getExtendedGStateNamed(getToken(i-1).nameString(pageBytes));
                    readExtendedGState(gs, exg);
                    swallowedToken=true;
                }
                break;
            case 'G' : // setgray
                if (tlen==1) {
                    cspace = getPage().getColorspace("DeviceGray");
                    gs.scolor = getColor(cspace,i,numops);
                    gs.scolorSpace = cspace;
                    swallowedToken=true;
                }
                break;
            case 'h' : // closepath
                if ((tlen==1) && (numops==0)) {
                    path.closePath();
                    Point2D lastPathPoint = path.getCurrentPoint(); 
                    gs.cp.x = (float)lastPathPoint.getX();
                    gs.cp.y = (float)lastPathPoint.getY();
                    swallowedToken=true;
                }
                break;
            case 'i' : // setflat
                if ((tlen==1) && (numops==1)) {
                    gs.flatness = getFloat(i-1);
                    swallowedToken=true;
                }
                break;
            case 'I' : // ID
                break;
            case 'j' : // setlinejoin
                if ((tlen==1) && (numops==1)) {
                    gs.lineJoin = getInt(i-1);
                    gs.lineStroke = pathFactory.createStroke(gs);
                    swallowedToken=true;
                }
                break;
            case 'J' : // setlinecap
                if ((tlen==1) && (numops==1)) {
                    gs.lineCap = getInt(i-1);
                    gs.lineStroke = pathFactory.createStroke(gs);
                    swallowedToken=true;
                }
                break;
            case 'k' : // setcmyk non-stroke
            case 'K' : // setcmyk stroke
                if ((tlen==1)) {
                    cspace = getPage().getColorspace("DeviceCMYK");
                    acolor = getColor(cspace,i,numops);
                    if (c=='k') {
                        gs.colorSpace = cspace;
                        gs.color = acolor;
                    }
                    else { // strokecolor
                        gs.scolorSpace = cspace;
                        gs.scolor = acolor;
                    }
                    swallowedToken=true;
                }
                break;
            case 'l' : // lineto
                if((tlen==1) && (numops==2)) {
                    getPoint(i, gs.cp);
                    path.lineTo(gs.cp.x, gs.cp.y);
                    swallowedToken=true;
                }
                break;
            case 'm' : //moveto
                if((tlen==1) && (numops==2)) {
                    getPoint(i, gs.cp);
                    // moveto creates a new path if there isn't one already,
                    // otherwise you get a subpath.
                    if (path==null)
                        path = pathFactory.createEmptyPath();
                    path.moveTo(gs.cp.x, gs.cp.y);
                    swallowedToken=true;
                }
                break;
            case 'M' : //setmiterlimit
                if (tlen==1) {
                    if (numops == 1) { 
                        gs.miterLimit = getFloat(i-1);
                        gs.lineStroke = pathFactory.createStroke(gs);
                        swallowedToken=true;
                    }
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='P') && (numops==1)) { //MP
                    // Marked content point
                    swallowedToken=true;
                }
                break;
            case 'n' : //endpath
                // End path without fill or stroke - used for clearing the path after a clipping operation ( W n )
                if ((tlen==1) && (numops==0)) {
                    didDraw = true;
                    swallowedToken=true;
                }
                break;
            case 'q' : //gsave;
                if ((tlen==1) && (numops==0)) {
                    gs=gsave();
                    swallowedToken=true;
                }
                break;
            case 'Q' : //grestore
                if ((tlen==1) && (numops==0)) {
                    gs=grestore();
                    swallowedToken=true;
                }
                break;
            case 'r' : //re=rectangle, rg=setrgbcolor, ri=renderingintent
                if (tlen==2) {
                    c=pageBytes[tstart+1];
                    if ((c=='e') && (numops==4)) { //x y w h re
                        // Add Rectangle
                        float x = getFloat(i-4), y = getFloat(i-3);
                        float w = getFloat(i-2), h = getFloat(i-1);
                        
                        // re either creates a new path or adds to the current one
                        if (path==null) 
                            path = pathFactory.createEmptyPath();
                        path.moveTo(x, y);
                        path.lineTo(x+w, y);
                        path.lineTo(x+w, y+h);
                        path.lineTo(x, y+h);
                        path.closePath();
                        // reset the current point to the start of the rect
                        // TODO: Check that this is what really happens in pdf
                        gs.cp.x = x;
                        gs.cp.y = y;
                        swallowedToken=true;
                    }
                    else if ((c=='i') && (numops==1)) {  //  /IntentName ri
                        gs.renderingIntent=getRenderingIntentID(getToken(i-1).nameValue(pageBytes));
                        swallowedToken=true;
                    }
                    else if (c=='g') { //r g b rg
                        cspace = getPage().getColorspace("DeviceRGB");
                        gs.color = getColor(cspace,i,numops);
                        gs.colorSpace = cspace;
                        swallowedToken=true;
                    }
                }
                break;
            case 'R' : //RG set stroke rgbcolor
                if((tlen==2) && (pageBytes[tstart+1]=='G')) {
                    cspace = getPage().getColorspace("DeviceRGB");
                    gs.scolor = getColor(cspace,i,numops);
                    gs.scolorSpace = cspace;
                    swallowedToken=true;
                }
                break;
            case 's' : 
                if (tlen==1) { // s
                    if (numops==0) {
                        // closepath, stroke
                        path.closePath();
                        engine.strokePath(gs, path);
                        didDraw=true;
                        swallowedToken=true;
                    }
                }
                else if (pageBytes[tstart+1]=='c') { //sc, scn   setcolor in colorspace
                    if (tlen==2) {
                        gs.color = getColor(gs.colorSpace,i,numops);
                        swallowedToken=true;
                    }
                    else if ((tlen==3) && (pageBytes[tstart+2]=='n')) { // scn
                        if ((gs.colorSpace instanceof PDFPatternSpace) && (numops>=1)) {
                            String pname = getToken(i-1).nameString(pageBytes);
                            PDFPattern pat = getPage().getPattern(pname);
                            gs.color = pat.getPaint();                            
                            // this is really stupid.  change this around
                            if ((pat instanceof PDFPatternTiling) && (gs.color==null)) {
                                // Uncolored tiling patterns require color values be passed.
                                // Note, however, that although you can draw an uncolored tiling
                                // pattern any number of times in different colors, we only do
                                // it once (after which it will be cached)
                                if (numops>1) {
                                    ColorSpace tileSpace=((PDFPatternSpace)gs.colorSpace).tileSpace;
                                    if (tileSpace==null)
                                        tileSpace=gs.colorSpace;
                                    gs.color = getColor(tileSpace,i-1, numops-1);
                                }
                                this.executePatternStream((PDFPatternTiling)pat);
                                gs.color = pat.getPaint();
                            }
                        }
                        else
                            gs.color = getColor(gs.colorSpace,i,numops);
                        swallowedToken=true;
                    }
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='h') /*&& (numops==1)*/) { //sh
                    String shadename = getToken(i-1).nameString(pageBytes);
                    GeneralPath shadearea;
                    java.awt.Paint oldPaint = gs.color;
                    PDFPatternShading shade = getPage().getShading(shadename);
                    
                    //save away old color
                    gs.color = shade.getPaint();
                    // Get the area to fill.  If the shading specifies a bounds, use that, if not, use the clip.
                    // If there's no clip, fill the whole page.
                    if (shade.getBounds() != null)
                        shadearea = new GeneralPath(shade.getBounds());
                    else {
                        shadearea = (gs.clip != null) ? (GeneralPath)gs.clip.clone() : new GeneralPath(_bounds);
                        // transform from page space into user space
                        try {
                            shadearea.transform(gs.trans.createInverse());
                        }
                        catch (NoninvertibleTransformException nte) {
                            throw new PDFException("Invalid user space transform");
                        }
                    }
                    engine.fillPath(gs, shadearea);
                    //restore the color
                    gs.color = oldPaint;
                    // TODO:probably did draw... check this
                    didDraw = true;
                    swallowedToken=true;
                }
                break;
            case 'S' : // Very similar to above
                if (tlen==1) { // S
                    if (numops==0) {
                        // stroke
                        engine.strokePath(gs, path);
                        didDraw=true;
                        swallowedToken=true;
                    }
                }
                else if (pageBytes[tstart+1]=='C') {  // SC : strokecolor in normal colorspaces
                    if (tlen==2) {
                        gs.scolor = getColor(gs.scolorSpace, i, numops);
                        swallowedToken=true;
                    }
                    else if ((tlen==3) && (pageBytes[tstart+2]=='N')) { // SCN
                        // TODO: deal with weird colorspaces
                        gs.scolor = getColor(gs.scolorSpace, i, numops);
                        swallowedToken=true;
                    }
                }
                break;
            case 'T' : // [T*, Tc, Td, TD, Tf, Tj, TJ, TL, Tm, Tr, Ts, Tw, Tz]
                // break text handling out
                if ((tlen==2) && parseTextOperator(pageBytes[tstart+1], i, numops, gs, pageBytes)) 
                    swallowedToken=true;
                break;
            case '\'':
            case '\"':  // ' and " also handled by text routine
                if ((tlen==1) && parseTextOperator(c, i, numops, gs, pageBytes))
                    swallowedToken=true;
                break;
            case 'v' :
                // Curveto (first control point is current point)
                if ((tlen==1) && (numops==4)) {
                    Point2D.Float cp1 = (Point2D.Float)gs.cp.clone();
                    Point2D.Float cp2 = getPoint(i-2);
                    getPoint(i, gs.cp);
                    path.curveTo(cp1.x, cp1.y, cp2.x, cp2.y, gs.cp.x, gs.cp.y);
                    swallowedToken=true;
                }
                break;
            case 'w' : // setlinewidth
                if((tlen==1) && (numops==1)) {
                    gs.lineWidth = getFloat(i-1);
                    gs.lineStroke = pathFactory.createStroke(gs);
                    swallowedToken=true;
                }
                break;
            case 'W' : // clip (*=eoclip)
                int wind;
                
                if (numops != 0) break;
                if (tlen==1) { // W
                    wind = GeneralPath.WIND_NON_ZERO;
                }
                else if ((tlen==2) && (pageBytes[tstart+1]=='*')) { // W*
                    wind = GeneralPath.WIND_EVEN_ODD;
                }
                else break;
                
                // Somebody at Adobe's been smoking crack.
                // The clipping operation doesn't modify the clipping in the gstate.
                // Instead, the next path drawing operation will do that, but only
                // AFTER it draws.  
                // So a sequence like 0 0 99 99 re W f will fill the rect first
                // and then set the clip path using the rect.
                // Because the W operation doesn't do anything, they had to introduce
                // the 'n' operation, which is a drawing no-op, in order to do a clip
                // and not also draw the path.
                // You might think it would be safe to just reset the clip here,
                // since the path it will draw is the same as the path it will clip to.
                // However, there's at least one (admittedly obscure) case I can think
                // of where clip(path),draw(path)  is different from draw(path),clip(path): 
                //     W* f  %eoclip, nonzero-fill
                // Also note that Acrobat considers it an error to have a W that isn't
                // immediately followed by a drawing operation (f, f*, F, s, S, B, b, n)
                if (path != null) {
                    path.setWindingRule(wind);
                    future_clip = (GeneralPath)path.clone();
                 }
                swallowedToken=true;
                break;
            case 'y' : // curveto (final point replicated)
                if ((tlen==1) && (numops==4)) {
                    Point2D.Float cp1 = getPoint(i-2);
                    getPoint(i, gs.cp);
                    path.curveTo(cp1.x, cp1.y, gs.cp.x, gs.cp.y, gs.cp.x, gs.cp.y);
                    swallowedToken=true;
                }
                break;
            }
            
            // If we made it down here with swallowedToken==false, it's because there
            // was no match, either because it was an illegal token, or there were the wrong
            // number of operands for the token.
            if (!swallowedToken) {
                // If we're in a compatibility section, just print a warning, since
                // we want to be alerted about anything that we don't currently support.
                if (compatibility_sections > 0) 
                    System.err.println("Warning - ignoring "+token.toString(pageBytes)+" in compatibility section");
                else
                    throw new PDFException("Error in content stream. Token = "+token.toString(pageBytes));
            }
            
            numops=0; // everything was fine, reset the number of operands
            
        }
        else {
            // It wasn't an operator, so it must be an operand (comments are tossed by the lexer)
            ++numops;
        }
        
        //Catch up on that clipping.  Plus be anal and return an error, just like Acrobat.
        if (didDraw) {
            if (future_clip != null) {
                // Note that unlike other operators that change the gstate,
                // there is a specific call into the markup handler when the clip changes.
                // 
                // The markup handler can choose whether to respond to the clipping
                // change or whether just to pull the clip out of the gstate when it
                // draws.
                establishClip(future_clip, true);
                future_clip = null;
            }
            // The current path and the current point are undefined after a draw
            path=null;
        }
        else {
            if (future_clip != null) {
                //TODO: an error unless the last token was W or W*
            }
        }
    }
    
    //restore previous token list
    _tokens = oldTokens;
}

public void executeForm(PDFForm f)
{
    Rectangle2D bbox = f.getBBox();
    AffineTransform xform = f.getTransform();
    PDFGState gs;
    
    // save the current gstate
    gs=gsave();
    // set the transform in the newgstate
    gs.trans.concatenate(xform);
    
    // clip to the form bbox
    establishClip(new GeneralPath(bbox), true);
  
    // add the form's resources to the page resource stack
    getPage().pushResources(f.getResources(_pdfFile));
    // recurse back into the parser with a new set of tokens
    parse(f.getTokens(this), f.getBytes());
    // restore the old resources, gstate,ctm, & clip
    getPage().popResources();
    grestore();
}

// A pattern could execute its pdf over and over, like a form (above)
// but for performance reasons, we only execute it once and cache a tile.
// To do this, we temporarily set the markup handler in the file to a new 
// BufferedMarkupHander, add the pattern's resource dictionary and fire up the parser.
//
public void executePatternStream(PDFPatternTiling pat)
{
    PDFMarkupHandler oldHandler = _pdfFile.getMarkupHandler();
    BufferedMarkupHandler patHandler = new BufferedMarkupHandler();
    PDFGState gs;
    ArrayList tokens;
    byte contents[];
    
    _pdfFile.setMarkupHandler(patHandler);
    // by adding the pattern's resources to the page's resource 
    // stack, it means the pattern will have access to resources
    // defined by the page.  I'll bet Acrobat doesn't allow you
    // to do this, but it shouldn't hurt anything.
    getPage().pushResources(pat.getResources());
    // save the current gstate
    gs=gsave();
    // Establish the pattern's transformation
    gs.trans.concatenate(pat.getTransform());
    // Begin the markup handler
    // TODO:probably going to have to add a translate by -x, -y of the bounds rect
    Rectangle2D prect = pat.getBounds();
    patHandler.beginPage((float)prect.getWidth(), (float)prect.getHeight());
    // get the pattern stream's tokens
    contents = pat.getContents();
    tokens = new ArrayList(32);
    getTokens(contents, 0, contents.length, tokens);
    // fire up the parser
    parse(tokens, contents);
    // Get the image and set the tile.  All the resources can be freed up now
    pat.setTile(patHandler.getImage());
    // restore everything
    grestore();
    _pdfFile.setMarkupHandler(oldHandler);
  
}

// Handles all the text operations [T*,Tc,Td,TD,Tf,Tj,TJ,TL,Tm,Tr,Ts,Tw,Tz,'."]
// For the "T" operations, oper represents the second letter.
public boolean parseTextOperator(byte oper, int tindex, int numops, PDFGState gs, byte pageBytes[])
{
     boolean swallowedToken = false;
     
     switch(oper) {
     case '*' : // T* - move to next line
         if (numops==0) {
             _textObj.positionText(0, -gs.tleading);
             swallowedToken=true;
         }
         break;
     case 'c' : // Tc - // Set character spacing
        if(numops==1) {
            gs.tcs = getFloat(tindex-1);
            swallowedToken = true;
        }
        break;
     case 'D' :
     case 'd' : // TD, td  - move relative to current line start (uppercase indicates to set leading to -ty)
        if (numops==2) {
            float x = getFloat(tindex-2);
            float y = getFloat(tindex-1);
            _textObj.positionText(x,y);
            if(oper=='D')
                gs.tleading = -y;
            swallowedToken = true;
        }
        break;
     case 'f' : // Tf - Set font name and size
         if (numops==2) {
            String fontalias = getToken(tindex-2).nameString(pageBytes); // name in dict is key, so lose leading /
            
            gs.font = getPage().getFontDictForAlias(fontalias);
            gs.fontSize = getFloat(tindex-1);
            swallowedToken = true;
        }
        break;
     case '"' : // w c string "   set word & charspacing, move to next line, show text
         if (numops!=3) break;
         gs.tws = getFloat(tindex-3);
         gs.tcs = getFloat(tindex-2);
         numops=1;
         // fall through
     case '\'' :  // ' - move to next line and show text
         _textObj.positionText(0, -gs.tleading);
         // Fall through
     case 'j' : // Tj - Show text
        if (numops==1) {
            Range crange = getToken(tindex-1).tokenRange();
            _textObj.showText(pageBytes, crange.location, crange.length, gs, _pdfFile);
            swallowedToken = true;
        }
        break;
     case 'J' : // TJ - Show text with spacing adjustment array
        if (numops==1) {
            List tArray = (List)(getToken(tindex-1).value);
            _textObj.showText(pageBytes, tArray, gs, _pdfFile);
            swallowedToken = true;
        }
        break;
     case 'L' : // TL -  set text leading
         if (numops==1) {
             gs.tleading = getFloat(tindex-1);
             swallowedToken = true;
         }
         break;
     case 'm' : // Tm - set text matrix
        if (numops==6) {
            _textObj.setTextMatrix(getFloat(tindex-6), getFloat(tindex-5),
                                   getFloat(tindex-4), getFloat(tindex-3),
                                   getFloat(tindex-2), getFloat(tindex-1));
            swallowedToken = true;
        }
        break;
     case 'r' : // Tr - set text rendering mode
         if (numops==1) {
             gs.trendermode = getInt(tindex-1);
             swallowedToken = true;
         }
         break;
     case 's' : // Ts - set text rise
         if (numops==1) {
             gs.trise = getFloat(tindex-1);
             swallowedToken = true;
         }
         break;
     case 'w' : // Tw - set text word spacing
        if (numops==1) {
            gs.tws = getFloat(tindex-1);
            swallowedToken = true;
        }
        break;
     case 'z' : // Tz - horizontal scale factor
         if (numops==1) {
             gs.thscale = getFloat(tindex-1)/100f;
             swallowedToken = true;
         }
         break;
     }

     return swallowedToken;
}

/** map for translating inline image abbreviations into standard tokens */
static final String _inline_image_key_abbreviations[][] = {
    {"BPC", "BitsPerComponent"},
    {"CS", "ColorSpace"},
    {"D", "Decode"},
    {"DP", "DecodeParms"},
    {"F", "Filter"},
    {"H", "Height"},
    {"IM", "ImageMask"},
    {"I", "Interpolate"},
    {"W", "Width"}};

/** Looks up an abbreviation in the above map. */
String translateInlineImageKey(String abbreviation)
{
    for(int i=0, n=_inline_image_key_abbreviations.length; i<n; ++i) {
        if (_inline_image_key_abbreviations[i][0].equals(abbreviation))
            return _inline_image_key_abbreviations[i][1];
    }
    // not found, so it's not an abbreviation
    return abbreviation;
}

static final String _inline_image_value_abbreviations[][] = {
    {"G", "DeviceGray"},
    {"RGB", "DeviceRGB"},
    {"CMYK", "DeviceCMYK"},
    {"I", "Indexed"},
    {"AHx", "ASCIIHexDecode"},
    {"A85", "ASCII85Decode"},
    {"LZW", "LZWDecode"},
    {"Fl", "FlateDecode"},
    {"RL", "RunLengthDecode"},
    {"CCF", "CCITTFaxDecode"},
    {"DCT", "DCTDecode"}
};

/** The values for keys in inline images are limited
 * to a small subset of names, numbers, arrays and maybe a dict.
 */
Object getInlineImageValue(PageToken token, byte pageBytes[])
{
    // Names (like /DeviceGray or /A85)
    if (token.type == PageToken.PDFNameToken) {
        String abbreviation = token.nameString(pageBytes); 
        // Names can optionally be abbreviated.
        for(int i=0, n=_inline_image_value_abbreviations.length; i<n; ++i) {
            if (_inline_image_value_abbreviations[i][0].equals(abbreviation))
            return '/' + _inline_image_value_abbreviations[i][1];
        }
        // not found, so it's not an abbreviation.  We assume it's valid
        return '/'+abbreviation;
    }
    // Numbers or bools
    else if ((token.type == PageToken.PDFNumberToken) ||
             (token.type == PageToken.PDFBooleanToken))
        return token.value;
    // An array of numbers or names (for Filter or Decode)
    else if (token.type == PageToken.PDFArrayToken) {
           List tokenarray = (List)token.value;
           List newarray = new ArrayList(tokenarray.size());
           // recurse
           for(int j=0, jMax=tokenarray.size(); j<jMax; ++j)
               newarray.add(getInlineImageValue((PageToken)tokenarray.get(j), pageBytes));
           return newarray;
    }
    // Hex strings for indexed color spaces
    else if (token.type == PageToken.PDFStringToken) {
        return token.byteArrayValue(pageBytes);
    }
    else {
        //TODO: One possible key in an inline image is DecodeParms (DP)
        // The normal decodeparms for an image is a dictionary.
        // The pdf spec doesn't give any information on the format
        // of the dictionary.  Does it use the normal dictionary
        // syntax, or does it use the inline image key/value syntax?
        // I have no idea, and I don't know how to generate a pdf file
        // that would have an inline image with a decodeparms dict.
    }
    throw new PDFException("Error parsing inline image dictionary");
}
        
/** 
 * Converts the tokens & data inside a BI/EI block into an image and draws it.
 * Returns the index of the last token consumed.
 **/
public int parseInlineImage(int tIndex, byte[] pageBytes)
{
    Hashtable imageDict = new Hashtable();
    imageDict.put("Subtype", "/Image");

    // Get the inline image key/value pairs and create a normal image dictionary
    for(int i=tIndex, iMax=_tokens.size(); i<iMax; ++i) {
        PageToken token = getToken(i);
        String key;
        Object value=null;

        if (token.type==PageToken.PDFNameToken) {
            // Translate the key
            key = translateInlineImageKey(token.nameString(pageBytes));
            // Get the value
            if (++i<iMax) {
                token=getToken(i);
                value = getInlineImageValue(token, pageBytes);
                // add translated key/value pair to the real dictionary
                imageDict.put(key,value);
            }
        }
        else if (token.type==PageToken.PDFInlineImageData) {
            // The actual inline data.  Create a stream with the dict & data
            // and create an image.  The image does not get cached.
            // The only way an inline image would ever get reused is if it were 
            // inside a form xobject.
            //
            // First get a colorspace object.  Inline images can use any colorspace
            // a regular image can.
            Object space = imageDict.get("ColorSpace");
            ColorSpace imageCSpace = space==null ? null : getPage().getColorspace(space);
            // Create the stream
            PDFStream imageStream = new PDFStream(pageBytes, token.tokenLocation(), token.tokenLength(), imageDict);
            // Tell the imageFactory to create an image and draw it
            drawImage(_pdfFile.getImageFactory().getImage(imageStream, imageCSpace, _pdfFile));
            // return the token index
            return i;
        }
    }
    
    // should only get here on an error (like running out of tokens or having bad key/value pairs)
    throw new PDFException("Syntax error parsing inline image dictionary");
}

/** Establishes an image transform and tells markup engine to draw the image */
public void drawImage(Image im) 
{
    // In pdf, an image is defined as occupying the unit square
    // no matter how many pixels wide or high it is (image space
    // goes from {0,0} - {1,1})
    // A pdf producer will scale up the ctm to get whatever
    // size they want.
    // We remove the pixelsWide & pixelsHigh from the scale
    // since awt image space goes from {0,0} - {width,height}
    // Also note that in pdf image space, {0,0} is at the upper-,
    // left.  Since this is flipped from all the other primatives,
    // we also include a flip here for consistency.

    AffineTransform ixform;
    int pixelsWide = im.getWidth(null);
    int pixelsHigh = im.getHeight(null);
    if ((pixelsWide<0) || (pixelsHigh<0)) {
        throw new PDFException("Error loading image"); //This shouldn't happen
    }
    ixform = new AffineTransform(1.0/pixelsWide, 0.0,
                                 0.0, -1.0/pixelsHigh,
                                 0, 1.0);
    _pdfFile.getMarkupHandler().drawImage(getGState(), im, ixform);
}

/** Pushes a copy of the current gstate onto the gstate stack and returns the new gstate. */
private PDFGState gsave()
{
    PDFGState newstate = (PDFGState)getGState().clone();
    _gstates.push(newstate);
    return newstate;
}

/** Pops the current gstate from the gstate stack and returns the restored gstate. */
private PDFGState grestore()
{
    // also calls into the markup handler if the change in gstate 
    // will cause the clipping path to change.
    GeneralPath currentclip = ((PDFGState)_gstates.pop()).clip;
    PDFGState gs = getGState();
    GeneralPath savedclip = gs.clip;
     if ((currentclip != null) && (savedclip != null)) {
        if (!currentclip.equals(savedclip))
            _pdfFile.getMarkupHandler().clipChanged(gs);
     }
     else if (currentclip != savedclip)
         _pdfFile.getMarkupHandler().clipChanged(gs);
     return gs;
}

/** Returns the last GState in the gstate list. */
private PDFGState getGState()
{
    return (PDFGState)_gstates.peek();
}

/** Called when the clipping path changes.
 * The clip in the gstate is defined to be in 
 * page space.  Whenever the clip is changed, we 
 * calculate the new clip, which can be intersected with
 * the old clip, and save it in the gstate.
 *
 * NB. This routine modifies the path that's passed in 
 * to it.
 */
public void establishClip(GeneralPath newclip, boolean intersect)
{
    PDFGState gs = getGState();
    
    // transform the new clip path into page space
    newclip.transform(gs.trans);
    
    // If we're adding a clip to an existing clip, calculate the intersection
    if (intersect && (gs.clip != null)) {
        Area clip_area = new Area(gs.clip);
        Area newclip_area = new Area(newclip);
        clip_area.intersect(newclip_area);
        newclip = new GeneralPath(clip_area);
    }
    gs.clip = newclip;
    
    // notify the markup handler of the new clip
    _pdfFile.getMarkupHandler().clipChanged(gs);
}

/** Called with any of the set color operations to create
 * new color instance from the values in the stream.
 * 
 * Currently considers having the wrong number of components
 * an error.
 */
private Color getColor(ColorSpace space, int tindex, int numops)
{
    int n = space.getNumComponents();
    float varray[] = new float[n]; // how much of a performance hit is allocating this every time?

    if (numops != n)
        throw new PDFException("Wrong number of color components for colorspace");
    for(int i=0; i<n; ++i)
        varray[i]=getFloat(tindex-(n-i));
    return _pdfFile.getColorFactory().createColor(space, varray);
}

static int getRenderingIntentID(String pdfName)
{
    if (pdfName.equals("/AbsoluteColorimetric"))
        return ColorFactory.AbsoluteColorimetricIntent;
    if (pdfName.equals("/RelativeColorimetric"))
        return ColorFactory.RelativeColorimetricIntent;
    if (pdfName.equals("/Saturation"))
        return ColorFactory.SaturationIntent;
    if (pdfName.equals("/Perceptual"))
        return ColorFactory.PerceptualIntent;
    throw new PDFException("Unknown rendering intent name \""+pdfName+"\"");
}

static int getBlendModeID(String pdfName)
{
    if (pdfName.equals("/Normal") || pdfName.equals("/Compatible"))
        return ColorFactory.NormalBlendMode;
    if (pdfName.equals("/Multiply"))
        return ColorFactory.MultiplyBlendMode;
    if (pdfName.equals("/Screen"))
        return ColorFactory.ScreenBlendMode;
    if (pdfName.equals("/Overlay"))
        return ColorFactory.OverlayBlendMode;
    if (pdfName.equals("/Darken"))
        return ColorFactory.DarkenBlendMode;
    if (pdfName.equals("/Lighten"))
        return ColorFactory.LightenBlendMode;
    if (pdfName.equals("/ColorDodge"))
        return ColorFactory.ColorDodgeBlendMode;
    if (pdfName.equals("/ColorBurn"))
        return ColorFactory.ColorBurnBlendMode;
    if (pdfName.equals("/HardLight"))
        return ColorFactory.HardLightBlendMode;
    if (pdfName.equals("/SoftLight"))
        return ColorFactory.SoftLightBlendMode;
    if (pdfName.equals("/Difference"))
        return ColorFactory.DifferenceBlendMode;
    if (pdfName.equals("/Exclusion"))
        return ColorFactory.ExclusionBlendMode;
    if (pdfName.equals("/Hue"))
        return ColorFactory.HueBlendMode;
    if (pdfName.equals("/Saturation"))
        return ColorFactory.SaturationBlendMode;
    if (pdfName.equals("/Color"))
        return ColorFactory.ColorBlendMode;
    if (pdfName.equals("/Luminosity"))
        return ColorFactory.LuminosityBlendMode;
    throw new PDFException("Unknown blend mode name \""+pdfName+"\"");
}


/** Pull out anything useful from an extended gstate dictionary */
void readExtendedGState(PDFGState gs, Map exgstate)
{
    String key;
    Object val=null;
    boolean strokeChanged = false;
    boolean transparencyChanged = false;
    Iterator entries;
    Map.Entry entry;
    float a;
    
    if (exgstate == null) return;
    // The dictionary will have been read in by the PDFParser, so
    // the elements will have been converted into the appropriate types, like
    // Integer, Float, Vector, etc.
    
    entries = exgstate.entrySet().iterator();
    while(entries.hasNext()) {
        entry=(Map.Entry)entries.next();
        key = (String)entry.getKey();
        val = entry.getValue();
        
    //line width, line cap, line join, & miter limit
    
        if (key.equals("LW")) {
        gs.lineWidth = ((Number)val).floatValue();
        strokeChanged = true;
    }
        else if (key.equals("LC")) {
        gs.lineCap = ((Number)val).intValue();
        strokeChanged = true;
    }
        else if (key.equals("LJ")) {
        gs.lineJoin = ((Number)val).intValue();
        strokeChanged = true;
    }
        else if (key.equals("ML")) {
        gs.miterLimit = ((Number)val).floatValue();
        strokeChanged = true;
    }
     
    // Dash:       "/D  [ [4 2 5 5] 0 ]"
        else if (key.equals("D")) {
        List dash = (List)val;
        List dashArray;
        int n;
        gs.dashPhase = ((Number)dash.get(1)).floatValue();
        dashArray = (List)dash.get(0);
        n = dashArray.size();
        gs.lineDash = new float[n];
        for(int i=0; i<n; ++i)
            gs.lineDash[i] = ((Number)dashArray.get(i)).floatValue();
        strokeChanged = true;
    }
    
    // Rendering intent
        else if (key.equals("RI"))
        gs.renderingIntent = getRenderingIntentID((String)val);
    
        // Transparency blending mode
        else if (key.equals("BM")) {
            int bm = PDFPageParser.getBlendModeID((String)val);
            if (bm != gs.blendMode) {
                gs.blendMode=bm;
                transparencyChanged=true;
            }
        }
        
        // Transparency - whether to treat alpha values as shape or transparency
        else if (key.equals("AIS")) {
            boolean ais = ((Boolean)val).booleanValue();
            if (ais != gs.alphaIsShape) {
                gs.alphaIsShape=ais;
                transparencyChanged=true;
            }
        }
        
        // Soft mask 
        else if (key.equals("SMask")) {
            if (val.equals("/None"))
                    gs.softMask = null;
            else
                System.err.println("Soft mask being specified : "+val);
        }
        
        // Transparency - stroke alpha
        else if (key.equals("CA")) {
           a = ((Number)val).floatValue();
           if (a != gs.salpha) {
               gs.alpha = a;
               transparencyChanged=true;
           }
        }
        
        // Transparency - nonstroke alpha
        else if (key.equals("ca")) {
            a = ((Number)val).floatValue();
            if (a != gs.alpha) {
                gs.alpha = a;
                transparencyChanged = true;
            }
        }
    // Some other possible entries in this dict that are not currently handled include:
    // Font, BG, BG2, UCR, UCR2, OP, op, OPM, TR, TR2, HT, FL, SM, SA,TK
    }
   
    // cache a new stroke object
    if (strokeChanged)
        gs.lineStroke = _pdfFile.getPathFactory().createStroke(gs);
    
    // cache new composite objects if necessary
    if (transparencyChanged) {
        gs.composite = _pdfFile.getColorFactory().createComposite(gs.colorSpace, gs.blendMode, gs.alphaIsShape, gs.alpha);
        gs.scomposite = _pdfFile.getColorFactory().createComposite(gs.colorSpace, gs.blendMode, gs.alphaIsShape, gs.salpha);
    }
}

    

}

