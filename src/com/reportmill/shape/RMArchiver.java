package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.text.*;
import java.util.*;
import snap.util.XMLArchiver;
import snap.web.WebURL;

/**
 * This class handles RM document archival.
 */
public class RMArchiver extends XMLArchiver {

/**
 * Returns a parent shape for source.
 */
public RMParentShape getParentShape(Object aSource)  { return (RMParentShape)getShape(aSource, null); }

/**
 * Creates a document.
 */
public RMShape getShape(Object aSource, Archivable aRootObj)
{
    // If source is a document, just return it
    if(aSource instanceof RMDocument) return (RMDocument)aSource;
    
    // Get URL and/or bytes (complain if not found)
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    byte bytes[] = url!=null? (url.getFile()!=null? url.getFile().getBytes() : null) : RMUtils.getBytes(aSource);
    if(bytes==null)
        throw new RuntimeException("RMArchiver.getShape: Cannot read source: " + (url!=null? url : aSource));
    
    // If PDF, return PDF Doc
    if(bytes!=null && RMPDFImageReader.canRead(bytes))
        return getDocPDF(url!=null? url : bytes, aRootObj instanceof RMDocument? (RMDocument)aRootObj : null);

    // Create archiver, read, set source and return
    setRootObject(aRootObj);
    RMShape shape = (RMShape)readObject(url!=null? url : bytes);
    if(shape instanceof RMParentShape) ((RMParentShape)shape).setSourceURL(url);
    return shape;
}

/**
 * Creates a document.
 */
public RMDocument getDoc(Object aSource, Archivable aBaseDoc)
{
    RMShape shape = getShape(aSource, aBaseDoc);
    RMDocument doc = shape instanceof RMDocument? (RMDocument)shape : null;
    if(doc==null) { doc = new RMDocument(shape.getWidth(), shape.getHeight()); doc.getPage(0).addChild(shape); }
    doc.setSourceURL(getSourceURL());
    return doc;
}

/**
 * Creates a new document from a PDF source.
 */
private RMDocument getDocPDF(Object aSource, RMDocument aBaseDoc)
{
    // Get/create new document (with no pages)
    RMDocument doc = aBaseDoc!=null? aBaseDoc : new RMDocument();
    while(doc.getPageCount()>0) doc.removePage(0);
    
    // Get image data for source and iterate over each PDF page and create/add document page
    RMImageData imageData = RMImageData.getImageData(aSource);
    for(int i=0, iMax=imageData.getPageCount(); i<iMax; i++) { RMImageData pageData = imageData.getPage(i);
        RMPage page = doc.addPage();
        page.setSize(pageData.getImageWidth(), pageData.getImageHeight());
        page.addChild(new RMImageShape(pageData));
        if(i==0) doc.setSize(page.getWidth(), page.getHeight());
    }
    
    // Return doc
    return doc;
}

/**
 * Returns the class map.
 */
public Map <String, Class> getClassMap()  { return _rmCM!=null? _rmCM : (_rmCM=createClassMap()); }
static Map <String, Class> _rmCM;

/**
 * Creates the class map.
 */
protected Map <String, Class> createClassMap()
{
    // Create class map and add classes
    Map classMap = new HashMap();
    
    // Shape classes
    classMap.put("arrow-head", RMLineShape.ArrowHead.class);
    classMap.put("cell-table", RMCrossTab.class);
    classMap.put("cell-table-frame", RMCrossTabFrame.class);
    classMap.put("color", RMColor.class);
    classMap.put("document", RMDocument.class);
    classMap.put("flow-shape", RMFlowShape.class);
    classMap.put("font", RMFont.class);
    classMap.put("graph", RMGraph.class);
    classMap.put("graph-legend", RMGraphLegend.class);
    classMap.put("grouper", RMGrouper.class);
    classMap.put("grouping", RMGrouping.class);
    classMap.put("image-shape", RMImageShape.class);
    classMap.put("label", RMLabel.class);
    classMap.put("labels", RMLabels.class);
    classMap.put("line", RMLineShape.class);
    classMap.put("morph", RMMorphShape.class);
    classMap.put("oval", RMOvalShape.class);
    classMap.put("page", RMPage.class);
    classMap.put("painter-shape", RMPainterShape.class);
    classMap.put("polygon", RMPolygonShape.class);
    classMap.put("pgraph", RMParagraph.class);
    classMap.put("rect", RMRectShape.class);
    classMap.put("shape", RMParentShape.class);
    classMap.put("sound-shape", RMSoundShape.class);
    classMap.put("spring-shape", RMSpringShape.class);
    classMap.put("star", RMStarShape.class);
    classMap.put("subreport", RMSubreport.class);
    classMap.put("switchshape", RMSwitchShape.class);
    classMap.put("table", RMTable.class);
    classMap.put("table-group", RMTableGroup.class);
    classMap.put("tablerow", RMTableRow.class);
    classMap.put("text", RMTextShape.class);
    classMap.put("linked-text", RMLinkedText.class);
    classMap.put("xstring", RMXString.class);
    classMap.put("animpath", RMAnimPathShape.class);
    classMap.put("scene3d", RMScene3D.class);
    
    // Swing Component shapes
    classMap.put("panel", "com.reportmill.swing.shape.SpringsPaneShape");
    classMap.put("jbutton", "com.reportmill.swing.shape.JButtonShape");
    classMap.put("jcheckbox", "com.reportmill.swing.shape.JCheckBoxShape");
    classMap.put("jcheckboxmenuitem", "com.reportmill.swing.shape.JCheckBoxMenuItemShape");
    classMap.put("jcombobox", "com.reportmill.swing.shape.JComboBoxShape");
    classMap.put("jformattedtextfield", "com.reportmill.swing.shape.JFormattedTextFieldShape");
    classMap.put("jlabel", "com.reportmill.swing.shape.JLabelShape");
    classMap.put("jlist", "com.reportmill.swing.shape.JListShape");
    classMap.put("jmenu", "com.reportmill.swing.shape.JMenuShape");
    classMap.put("jmenubar", "com.reportmill.swing.shape.JMenuBarShape");
    classMap.put("jmenuitem", "com.reportmill.swing.shape.JMenuItemShape");
    classMap.put("jpasswordfield", "com.reportmill.swing.shape.JPasswordFieldShape");
    classMap.put("jprogressbar", "com.reportmill.swing.shape.JProgressBarShape");
    classMap.put("jradiobutton", "com.reportmill.swing.shape.JRadioButtonShape");
    classMap.put("jscrollpane", "com.reportmill.swing.shape.JScrollPaneShape");
    classMap.put("jseparator", "com.reportmill.swing.shape.JSeparatorShape");
    classMap.put("jslider", "com.reportmill.swing.shape.JSliderShape");
    classMap.put("jspinner", "com.reportmill.swing.shape.JSpinnerShape");
    classMap.put("jsplitpane", "com.reportmill.swing.shape.JSplitPaneShape");
    classMap.put("jtable", "com.reportmill.swing.shape.JTableShape");
    classMap.put("JTableColumn", "com.reportmill.swing.shape.JTableColumnShape");
    classMap.put("jtabbedpane", "com.reportmill.swing.shape.JTabbedPaneShape");
    classMap.put("jtextarea", "com.reportmill.swing.shape.JTextAreaShape");
    classMap.put("jtextfield", "com.reportmill.swing.shape.JTextFieldShape");
    classMap.put("jtogglebutton", "com.reportmill.swing.shape.JToggleButtonShape");
    classMap.put("jtree", "com.reportmill.swing.shape.JTreeShape");
    
    // Miscellaneous component shapes 
    classMap.put("colorwell", "com.reportmill.swing.shape.ColorWellShape");
    classMap.put("customview", "com.reportmill.swing.shape.CustomViewShape");
    classMap.put("menubutton", "com.reportmill.swing.shape.MenuButtonShape");
    classMap.put("switchpane", "com.reportmill.swing.shape.SwitchPaneShape");
    classMap.put("thumbwheel", "com.reportmill.swing.shape.ThumbWheelShape");
    
    // Strokes
    classMap.put("stroke", RMStroke.class);
    classMap.put("border-stroke", "com.reportmill.graphics.RMBorderStroke");
    classMap.put("double-stroke", "com.reportmill.graphics.RMDoubleStroke");
    
    // Fills
    classMap.put("fill", RMFill.class);
    classMap.put("gradient-fill", RMGradientFill.class);
    classMap.put("radial-fill", RMRadialGradientFill.class);
    classMap.put("image-fill", RMImageFill.class);
    classMap.put("contour-fill", "com.reportmill.graphics.RMContourFill");
    
    // Effects
    classMap.put("blur-effect", "com.reportmill.graphics.RMBlurEffect");
    classMap.put("shadow-effect", "com.reportmill.graphics.RMShadowEffect");
    classMap.put("reflection-effect", "com.reportmill.graphics.RMReflectionEffect");
    classMap.put("emboss-effect", "com.reportmill.graphics.RMEmbossEffect");
    classMap.put("chisel-effect", "com.reportmill.graphics.RMChiselEffect");

    // Sorts
    classMap.put("sort", "com.reportmill.base.RMSort");
    classMap.put("top-n-sort", "com.reportmill.base.RMTopNSort");
    classMap.put("value-sort", "com.reportmill.base.RMValueSort");
    
    // Return classmap
    return classMap;
}
    
}