package com.reportmill.pdf.reader;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * A test program for the parser.
 */
public class RMPDFShell {

public static void main(String args[]) {
    String filename=null;
    boolean stopOnError = false;
    boolean silent = false;
    int pageNum=-1;
    int totalPages=0;
    int p, startP=0, endP=0;
    PDFFile pdf=null;
    
    for(int i=0; i<args.length; ++i) {
        if (args[i].equalsIgnoreCase("-stopOnError"))
            stopOnError = true;
        else if (args[i].equalsIgnoreCase("-silent"))
            silent = true;
        else if (args[i].equalsIgnoreCase("-page") && (i<args.length-1)) {
          pageNum =  Integer.parseInt(args[++i]);
          }
        else if (filename == null) 
            filename = args[i];
        else {
            filename=null;
            break;
        }
    }
    
    if (filename==null) {
        System.err.println("usage: java com.ribs.pdf.PDFFile filename [-page xxx] [-silent] [-stopOnError]");
        System.exit(1);
    }
    try {
        pdf = readFile(new File(filename));
        totalPages = pdf.getPageCount();
        
        if (pageNum>=totalPages) {
            System.err.println("Invalid page number");
            System.exit(1);
        }
        if (pageNum<0) {
            startP = 0;
            endP = totalPages;
        }
        else {
            startP=pageNum;
            endP=pageNum+1;
        }
        
        pdf.setStripsExtendedGStates(false);
        pdf.setPathFactory(new DefaultFactories());
        pdf.setColorFactory((ColorFactory)pdf.getPathFactory());
        pdf.setFontFactory(new PDFFontFactory());
        pdf.setImageFactory(new PDFImageFactory());
        pdf.setMarkupHandler(new EmptyHandler());
    }
    catch(Throwable t) {
        if (!silent) {
            System.err.println(filename);
            t.printStackTrace(System.err);
        }
        System.exit(1);
    }
    
    if (!silent)
        System.out.print(filename+" ["+totalPages+" page"+(totalPages>1 ? "s":"")+"]:");
    for(p=startP; p<endP; ++p) {
        if ((p-startP)%10==0)
            System.out.print("\n\t");
        System.out.print(" "+p);
        try {
            PDFPageParser.parsePage(pdf, p);
        }
        catch (Throwable t) {
          if (!silent)
              t.printStackTrace(System.err);
          if (stopOnError)
              System.exit(1);
        }
        pdf.clearPageCache();
        pdf.resetXRefTable();
    }
    System.out.println();
}


private static class EmptyHandler extends PDFMarkupHandler {
  java.awt.Graphics2D _graphics=null;
  java.awt.image.BufferedImage _crapImage=null;
  public void beginPage(float width, float height) {}
  public void strokePath(PDFGState g, GeneralPath p) {}
  public void fillPath(PDFGState g, GeneralPath p) {}
  public void clipChanged(PDFGState g) {}
  public void drawImage(PDFGState g, Image i, AffineTransform ixform) {}
  public void showText(PDFGState g, GlyphVector v) {}
  public FontRenderContext getFontRenderContext() {
    return ((java.awt.Graphics2D)getGraphics()).getFontRenderContext();
  }
  public Graphics getGraphics() { 
    if (_graphics==null) {
        _crapImage = new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
        _graphics = _crapImage.createGraphics();
    }
    return _graphics;
  }
}

/** Convenience method to load a new PDFFile from a java.io.File */
public static PDFFile readFile(File file) throws IOException
{
    int len = (int)file.length();
    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
    byte fileBytes[] = new byte[len];
    if (buf.read(fileBytes,0,len) != len) 
        throw new IOException("Error reading file");
    return new PDFFile(fileBytes);
}
  
//---------- Debugging ---------
  public void dumpAll(PDFFile aFile, int max) throws ParseException
  {
      for(int i=0, iMax=aFile._xref.size(); i<iMax; ++i) {
          PDFXEntry entry = aFile._xref.get(i);
          if ((entry.fileOffset==0) || (entry.state==PDFXEntry.EntryDeleted))
              continue;
          Object object = aFile.getXRefObject(entry);
          if (object instanceof PDFStream) {
              Map m = ((PDFStream)object).getDictionary();
              byte dat[];
              System.out.println(" "+i+" = "+m);
              try {
                  dat = ((PDFStream)object).decodeStream();
                  if ((max>0) && (dat.length>max)) {
                      System.out.println(new String(dat,0,max));
                      System.out.println("...");
                  }
                  else
                     System.out.println(dat);
              } 
              catch(Exception e) { System.out.println(" error: "+e); }
              System.out.println();
          }
          else
              System.out.println(" " + i + " = " + object);
      }
  }

}