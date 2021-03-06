/* Generated By:JavaCC: Do not edit this line. PDFParser.java */
package com.reportmill.pdf.reader;
import java.io.*;
import java.util.*;

public class PDFParser implements PDFParserConstants {

    // A JavaCC char stream for binary data
    public PDFCharStream   pdfdata;

    // The PDF file
    PDFFile                _pdfFile;

/** Creates a new PDF parser. */
public PDFParser()
{
    this(new PDFParserTokenManager(null));
}

/** Creates a new PDF parser. */
public PDFParser(PDFFile aPdfFile, byte data[])
{
    this();
    _pdfFile = aPdfFile;
    pdfdata = new PDFCharStream(data);
    ReInit(pdfdata);
}

public void resetLexingLocation(int offset)
{
    pdfdata.ReInit(offset);
    ReInit(pdfdata);
}


public void resetLexingData(byte newdata[], int offset)
{
    pdfdata.ReInit(newdata, offset);
    ReInit(pdfdata);
}

/** Allocate space in the xref table.
 *  Fills any holes up to and including objNum with empty entries
 */
public void fillXRefTable(Vector xref, int objNum)
{
    int numEntries = xref.size();
    while(numEntries <= objNum)
        xref.add(new PDFXEntry(numEntries++));
}

  final public String pdfversion() throws ParseException {
                        token_source.SwitchTo(SPECIAL_COMMENTS);
    jj_consume_token(PDF_HEADER);
        String version = token.image;
        token_source.SwitchTo(DEFAULT);
        {if (true) return version;}
    throw new Error("Missing return statement in function");
  }

  final public void checkEOF() throws ParseException {
                    token_source.SwitchTo(SPECIAL_COMMENTS);
    jj_consume_token(EOF_MARKER);
                   token_source.SwitchTo(DEFAULT);
  }

  final public int startxref() throws ParseException {
                    Integer i;
    jj_consume_token(START_XREF);
    i = integer();
                               {if (true) return i.intValue();}
    throw new Error("Missing return statement in function");
  }

  final public Object pdf_object() throws ParseException {
                        Object node;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ARRAY_BEGIN:
      node = array();
                   {if (true) return node;}
      break;
    case DICT_BEGIN:
      node = dictionary();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STREAM_BEGIN:
        node = stream((Hashtable)node);
        break;
      default:
        jj_la1[0] = jj_gen;
        ;
      }
                                                          {if (true) return node;}
      break;
    default:
      jj_la1[1] = jj_gen;
      if (jj_2_1(3)) {
        node = reference();
                                    {if (true) return node;}
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case BOOLEAN_TRUE:
        case BOOLEAN_FALSE:
        case NULL_OBJECT:
        case NUM_INTEGER:
        case NUM_REAL:
        case PDF_HEX_STRING:
        case PDF_NAME:
        case PDF_STRING:
          node = leaf();
                  {if (true) return node;}
          break;
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public Object reference() throws ParseException {
                       Integer which, gen;
    which = integer();
    gen = integer();
    jj_consume_token(42);
        int entry = which.intValue();
        Vector xref = _pdfFile._xref;
        int numentries = xref.size();

        // Fill in any holes if necessary and return reference
        fillXRefTable(xref, entry);
        {if (true) return xref.get(entry);}
    throw new Error("Missing return statement in function");
  }

  final public Object object_definition() throws ParseException {
                               Object o; Integer oNum, genNum;
    oNum = integer();
    genNum = integer();
    jj_consume_token(OBJECT_BEGIN);
    o = pdf_object();
    jj_consume_token(OBJECT_END);
        {if (true) return o;}
    throw new Error("Missing return statement in function");
  }

  final public Vector array() throws ParseException {
                   Vector a; Object o;
    jj_consume_token(ARRAY_BEGIN);
                    a = new Vector();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case BOOLEAN_TRUE:
      case BOOLEAN_FALSE:
      case NULL_OBJECT:
      case NUM_INTEGER:
      case NUM_REAL:
      case PDF_HEX_STRING:
      case PDF_NAME:
      case ARRAY_BEGIN:
      case DICT_BEGIN:
      case PDF_STRING:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_1;
      }
      o = pdf_object();
                       a.addElement(o);
    }
    jj_consume_token(ARRAY_END);
                  {if (true) return a;}
    throw new Error("Missing return statement in function");
  }

  final public Hashtable dictionary() throws ParseException {
                           Hashtable d; Object key, val;
    jj_consume_token(DICT_BEGIN);
                   d = new Hashtable();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PDF_NAME:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
      jj_consume_token(PDF_NAME);
                   key = token.image.substring(1);
      val = pdf_object();
                                                                        d.put(key, val);
    }
    jj_consume_token(DICT_END);
                 {if (true) return d;}
    throw new Error("Missing return statement in function");
  }

  final public Object stream(Hashtable d) throws ParseException {
                               byte data[]; int length;
    jj_consume_token(STREAM_BEGIN);
        Integer lengthObj = (Integer)_pdfFile.resolveObject(d.get("Length"));
        length = lengthObj.intValue();
    data = stream_contents(length);
    jj_consume_token(STREAM_END);
                   {if (true) return new PDFStream(data, d);}
    throw new Error("Missing return statement in function");
  }

  byte[] stream_contents(int length) throws ParseException {
    byte bytes[];

    try {
    char c = pdfdata.readChar();

    // In PDF version 1.0 linefeed after 'stream' is mandatory (in 1+, it's optional)
    if(_pdfFile._version.equals("%PDF-1.0")) {
        if (c != '\n')
            throw new ParseException("Expecting line feed character after 'stream'");
    }

    // If char after 'stream' is not newline, backup so it is included as stream data
    else if(c != '\n')
        pdfdata.backup(1);

    bytes = pdfdata.GetNextBytes(length);

    // Catch IOException
    } catch(IOException e) { throw new ParseException("Premature end of input in stream"); }

    return bytes;
  }

  final public Object leaf() throws ParseException {
                  Object l;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case BOOLEAN_TRUE:
      jj_consume_token(BOOLEAN_TRUE);
                      {if (true) return Boolean.TRUE;}
      break;
    case BOOLEAN_FALSE:
      jj_consume_token(BOOLEAN_FALSE);
                       {if (true) return Boolean.FALSE;}
      break;
    case NULL_OBJECT:
      jj_consume_token(NULL_OBJECT);
                     {if (true) return "null";}
      break;
    case NUM_INTEGER:
      l = integer();
                   {if (true) return l;}
      break;
    case NUM_REAL:
      l = real();
                {if (true) return l;}
      break;
    case PDF_NAME:
      jj_consume_token(PDF_NAME);
                  {if (true) return token.image;}
      break;
    case PDF_STRING:
      jj_consume_token(PDF_STRING);
                    {if (true) return token.image;}
      break;
    case PDF_HEX_STRING:
      jj_consume_token(PDF_HEX_STRING);
                        {if (true) return token.image;}
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Integer integer() throws ParseException {
    jj_consume_token(NUM_INTEGER);
                  {if (true) return Integer.valueOf(token.image);}
    throw new Error("Missing return statement in function");
  }

  final public Double real() throws ParseException {
    jj_consume_token(NUM_REAL);
               {if (true) return Double.valueOf(token.image);}
    throw new Error("Missing return statement in function");
  }

  final public int xrefentrystate() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case XREF_F:
      jj_consume_token(XREF_F);
             {if (true) return XREF_F;}
      break;
    case XREF_N:
      jj_consume_token(XREF_N);
             {if (true) return XREF_N;}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Hashtable pdfXRefSection(Vector xref) throws ParseException {
                                         Hashtable trail;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case XREF:
      pdfXRefTable(xref);
      trail = trailer();
      break;
    case NUM_INTEGER:
      trail = pdfXRefStream(xref);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return trail;}
    throw new Error("Missing return statement in function");
  }

  final public void pdfXRefTable(Vector xref) throws ParseException {
                                   Integer entrypos, subentries;
    jj_consume_token(XREF);
    label_3:
    while (true) {
      entrypos = integer();
      subentries = integer();
        int ep = entrypos.intValue();
        int sub = subentries.intValue();
        int numentries = xref.size();

        // Allocate xref entries
        fillXRefTable(xref, ep+sub-1);

        while(sub>0) {
            Integer fileoff = integer();
            Integer gen = integer();
            int x = xrefentrystate();
            PDFXEntry anEntry = (PDFXEntry)xref.get(ep);

            // Only read entry if it's state hasn't been set - XRefs at end of file override ones that came earlier
            if(anEntry.state==PDFXEntry.EntryUnknown) {
                anEntry.state = x==XREF_F? PDFXEntry.EntryDeleted : PDFXEntry.EntryNotYetRead;
                anEntry.fileOffset = fileoff.intValue();
                anEntry.generation = gen.intValue();
            }
            ++ep;
            --sub;
        }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUM_INTEGER:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_3;
      }
    }

  }

  final public Hashtable pdfXRefStream(Vector xref) throws ParseException {
                                        Object obj;
    obj = object_definition();
      if (obj instanceof PDFStream) {
             PDFStream xstm=(PDFStream)obj;
             Hashtable xmap=(Hashtable)xstm.getDictionary();
             if ("/XRef".equals(xmap.get("Type"))) {
                 int maxObjPlusOne = ((Number)xmap.get("Size")).intValue();
                 int fieldWidths[] = PDFDictUtils.getIntArray(xmap, null, "W");
                 int fields[] = new int[fieldWidths.length];
                 int indices[] = PDFDictUtils.getIntArray(xmap, null, "Index");
                 if (indices==null)
                     indices = new int[]{0, maxObjPlusOne};
                 int i,j,k,l,nsubsections;
                 int subStart, numEntries;
                 PDFXEntry anEntry;
                 byte xrefdata[] = xstm.decodeStream();
                 int xrefdatapos=0;

                 // Allocate space for all xrefs to come
                 fillXRefTable(xref, maxObjPlusOne-1);
                 // Read in each subsection
                 nsubsections=indices.length/2;
                 for(i=0; i<nsubsections; ++i) {
                     subStart = indices[2*i];
                     numEntries = indices[2*i+1];
                     for(j=0; j<numEntries; ++j) {
                         // Pull out the fields from the stream data.
                         for(k=0; k<fields.length;++k) {
                             fields[k]=0;
                             for(l=0; l<fieldWidths[k]; ++l)
                                 fields[k]=(fields[k])<<8 | (xrefdata[xrefdatapos++] & 0xff);
                         }
                         // Get the xref and set the values if not already set
                         anEntry = (PDFXEntry)xref.get(subStart+j);
                         if (anEntry.state==PDFXEntry.EntryUnknown) {
                             switch(fields[0]) {
                             case 0: anEntry.state = PDFXEntry.EntryDeleted;
                                     break;
                             case 1: anEntry.state = PDFXEntry.EntryNotYetRead;
                                     anEntry.fileOffset = fields[1];
                                     anEntry.generation = fieldWidths[2]>0 ? fields[2] : 0;
                                     break;
                             case 2: anEntry.state = PDFXEntry.EntryCompressed;
                                     //really the object number of the object stream
                                     anEntry.fileOffset=fields[1];
                                     //and the index of the object within the object stream
                                     anEntry.generation=fields[2];
                                     break;
                             }
                         }
                     }
                 }
             {if (true) return xmap;}
             }
         }
         {if (true) throw new ParseException("Error reading cross-reference stream");}
    throw new Error("Missing return statement in function");
  }

  final public Hashtable trailer() throws ParseException {
                        Hashtable h;
    jj_consume_token(TRAILER);
    h = dictionary();
                               {if (true) return h;}
    throw new Error("Missing return statement in function");
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  final private boolean jj_3R_5() {
    if (jj_scan_token(NUM_INTEGER)) return true;
    return false;
  }

  final private boolean jj_3R_4() {
    if (jj_3R_5()) return true;
    if (jj_3R_5()) return true;
    if (jj_scan_token(42)) return true;
    return false;
  }

  final private boolean jj_3_1() {
    if (jj_3R_4()) return true;
    return false;
  }

  public PDFParserTokenManager token_source;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[9];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x100000,0x28000,0x7f00,0x2ff00,0x4000,0x7f00,0x3000000,0x400800,0x800,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x8,0x8,0x0,0x8,0x0,0x0,0x0,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public PDFParser(CharStream stream) {
    token_source = new PDFParserTokenManager(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(CharStream stream) {
    token_source.ReInit(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public PDFParser(PDFParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(PDFParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {
        int[] oldentry = (int[])(e.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[43];
    for (int i = 0; i < 43; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 9; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 43; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
